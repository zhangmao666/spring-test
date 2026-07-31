package com.example.springboottest.modules.market.service;

import com.example.springboottest.modules.market.dto.MarketQuoteResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class MarketQuoteServiceTest {

    private RestTemplate restTemplate;
    private MockRestServiceServer server;
    private MarketQuoteService marketQuoteService;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        marketQuoteService = new MarketQuoteService(restTemplate, new ObjectMapper());
    }

    @Test
    void shouldResolveStockQuoteFromEastmoney() {
        server.expect(requestTo(containsString("searchapi.eastmoney.com/api/suggest/get")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "QuotationCodeTable": {
                            "Data": [
                              {
                                "QuoteID": "1.600519",
                                "Code": "600519",
                                "Name": "贵州茅台",
                                "SecurityTypeName": "沪A"
                              }
                            ]
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        server.expect(requestTo(containsString("push2.eastmoney.com/api/qt/stock/get")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "data": {
                            "f57": "600519",
                            "f58": "贵州茅台",
                            "f43": 168899,
                            "f169": 205,
                            "f170": 121,
                            "f46": 167300,
                            "f44": 169500,
                            "f45": 166800,
                            "f60": 166849,
                            "f47": 255012,
                            "f48": 4301500000,
                            "f116": 2140000000000
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        MarketQuoteResponse response = marketQuoteService.getQuote("stock", "贵州茅台");

        assertEquals("stock", response.getAssetType());
        assertEquals("600519", response.getCode());
        assertEquals("贵州茅台", response.getName());
        assertEquals(1688.99, response.getPrice(), 0.001);
        assertEquals(2.05, response.getChange(), 0.001);
        assertEquals(1.21, response.getChangePercent(), 0.001);
        assertEquals("东方财富", response.getSourceName());
        server.verify();
    }

    @Test
    void shouldResolveFundQuoteFromEastmoney() {
        server.expect(requestTo(containsString("fundmobapi.eastmoney.com/FundMNewApi/FundMNFInfo")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "Datas": [
                            {
                              "FCODE": "161725",
                              "SHORTNAME": "招商中证白酒指数(LOF)A",
                              "FTYPE": "LOF",
                              "DWJZ": "0.8450",
                              "PDATE": "2026-05-07",
                              "GSZ": "0.8531",
                              "GSZZL": "0.96",
                              "GZTIME": "2026-05-07 15:00"
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        MarketQuoteResponse response = marketQuoteService.getQuote("fund", "161725");

        assertEquals("fund", response.getAssetType());
        assertEquals("161725", response.getCode());
        assertEquals("招商中证白酒指数(LOF)A", response.getName());
        assertEquals(0.8450, response.getLatestNetValue(), 0.0001);
        assertEquals(0.8531, response.getEstimatedNetValue(), 0.0001);
        assertEquals(0.96, response.getChangePercent(), 0.0001);
        assertEquals("天天基金", response.getSourceName());
        server.verify();
    }

    @Test
    void shouldResolveCryptoQuoteFromCoinGecko() {
        server.expect(requestTo(containsString("api.coingecko.com/api/v3/search")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "coins": [
                            {
                              "id": "pepe",
                              "symbol": "pepe",
                              "name": "Pepe"
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        server.expect(requestTo(containsString("api.coingecko.com/api/v3/simple/price")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "pepe": {
                            "usd": 0.00001234,
                            "cny": 0.000089,
                            "usd_24h_change": 5.67,
                            "usd_market_cap": 5000000000,
                            "usd_24h_vol": 800000000
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        MarketQuoteResponse response = marketQuoteService.getQuote("crypto", "pepe");

        assertEquals("crypto", response.getAssetType());
        assertEquals("PEPE", response.getSymbol());
        assertEquals("pepe", response.getName());
        assertNotNull(response.getSecondaryPrice());
        assertEquals(0.00001234, response.getPrice(), 0.00000001);
        assertEquals(0.000089, response.getSecondaryPrice(), 0.0000001);
        assertEquals(5.67, response.getChangePercent(), 0.001);
        assertEquals("CoinGecko", response.getSourceName());
        server.verify();
    }
}
