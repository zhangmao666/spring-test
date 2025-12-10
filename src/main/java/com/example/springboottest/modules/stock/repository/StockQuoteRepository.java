package com.example.springboottest.modules.stock.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottest.modules.stock.entity.StockQuote;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface StockQuoteRepository extends BaseMapper<StockQuote> {

    @Select("SELECT * FROM stock_quotes WHERE stock_id = #{stockId} ORDER BY quote_time DESC LIMIT 1")
    Optional<StockQuote> findLatestByStockId(@Param("stockId") Long stockId);

    @Select("SELECT * FROM stock_quotes WHERE stock_id = #{stockId} ORDER BY quote_time DESC LIMIT #{limit}")
    List<StockQuote> findRecentByStockId(@Param("stockId") Long stockId, @Param("limit") Integer limit);

    @Select("SELECT * FROM stock_quotes WHERE stock_id = #{stockId} AND quote_time BETWEEN #{startTime} AND #{endTime} ORDER BY quote_time ASC")
    List<StockQuote> findByStockIdAndTimeRange(
            @Param("stockId") Long stockId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Select("SELECT * FROM stock_quotes WHERE stock_id = #{stockId} AND DATE(quote_time) = DATE(#{date}) ORDER BY quote_time ASC")
    List<StockQuote> findByStockIdAndDate(@Param("stockId") Long stockId, @Param("date") LocalDateTime date);

    @Select("SELECT COUNT(*) FROM stock_quotes WHERE stock_id = #{stockId}")
    Long countByStockId(@Param("stockId") Long stockId);
}
