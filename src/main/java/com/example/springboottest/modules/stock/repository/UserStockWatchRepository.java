package com.example.springboottest.modules.stock.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottest.modules.stock.entity.UserStockWatch;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface UserStockWatchRepository extends BaseMapper<UserStockWatch> {

    @Select("SELECT * FROM user_stock_watch WHERE user_id = #{userId} ORDER BY watch_time DESC")
    List<UserStockWatch> findByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM user_stock_watch WHERE user_id = #{userId} AND stock_id = #{stockId}")
    Optional<UserStockWatch> findByUserIdAndStockId(@Param("userId") Long userId, @Param("stockId") Long stockId);

    @Select("SELECT stock_id FROM user_stock_watch WHERE user_id = #{userId}")
    List<Long> findStockIdsByUserId(@Param("userId") Long userId);

    @Delete("DELETE FROM user_stock_watch WHERE user_id = #{userId} AND stock_id = #{stockId}")
    int deleteByUserIdAndStockId(@Param("userId") Long userId, @Param("stockId") Long stockId);

    @Select("SELECT * FROM user_stock_watch WHERE alert_enabled = 1")
    List<UserStockWatch> findAllWithAlertEnabled();

    @Select("SELECT COUNT(*) FROM user_stock_watch WHERE user_id = #{userId}")
    Long countByUserId(@Param("userId") Long userId);
}
