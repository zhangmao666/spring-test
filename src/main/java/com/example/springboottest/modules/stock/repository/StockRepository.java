package com.example.springboottest.modules.stock.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottest.modules.stock.entity.Stock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface StockRepository extends BaseMapper<Stock> {

    @Select("SELECT * FROM stocks WHERE stock_code = #{stockCode}")
    Optional<Stock> findByStockCode(@Param("stockCode") String stockCode);

    @Select("SELECT * FROM stocks WHERE status = 1 ORDER BY stock_code")
    List<Stock> findAllActive();

    @Select("SELECT * FROM stocks WHERE (stock_code LIKE CONCAT('%', #{keyword}, '%') OR stock_name LIKE CONCAT('%', #{keyword}, '%')) AND status = 1 LIMIT 20")
    List<Stock> searchStocks(@Param("keyword") String keyword);

    @Select("SELECT * FROM stocks WHERE market = #{market} AND status = 1")
    List<Stock> findByMarket(@Param("market") String market);

    @Select("SELECT * FROM stocks WHERE industry = #{industry} AND status = 1")
    List<Stock> findByIndustry(@Param("industry") String industry);
}
