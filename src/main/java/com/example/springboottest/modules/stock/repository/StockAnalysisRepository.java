package com.example.springboottest.modules.stock.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottest.modules.stock.entity.StockAnalysis;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface StockAnalysisRepository extends BaseMapper<StockAnalysis> {

    @Select("SELECT * FROM stock_analysis WHERE stock_id = #{stockId} AND status = 1 ORDER BY analysis_time DESC LIMIT 1")
    Optional<StockAnalysis> findLatestByStockId(@Param("stockId") Long stockId);

    @Select("SELECT * FROM stock_analysis WHERE stock_id = #{stockId} AND status = 1 ORDER BY analysis_time DESC LIMIT #{limit}")
    List<StockAnalysis> findRecentByStockId(@Param("stockId") Long stockId, @Param("limit") Integer limit);

    @Select("SELECT * FROM stock_analysis WHERE stock_id = #{stockId} AND analysis_time BETWEEN #{startTime} AND #{endTime} AND status = 1 ORDER BY analysis_time DESC")
    List<StockAnalysis> findByStockIdAndTimeRange(
            @Param("stockId") Long stockId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Select("SELECT * FROM stock_analysis WHERE trend_type = #{trendType} AND status = 1 ORDER BY analysis_time DESC LIMIT #{limit}")
    List<StockAnalysis> findByTrendType(@Param("trendType") String trendType, @Param("limit") Integer limit);

    @Select("SELECT COUNT(*) FROM stock_analysis WHERE stock_id = #{stockId} AND status = 1")
    Long countByStockId(@Param("stockId") Long stockId);
}
