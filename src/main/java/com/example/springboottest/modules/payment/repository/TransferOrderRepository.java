package com.example.springboottest.modules.payment.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboottest.modules.payment.entity.TransferOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

/**
 * 转账订单数据访问层
 */
@Mapper
public interface TransferOrderRepository extends BaseMapper<TransferOrder> {
    
    /**
     * 根据商户订单号查询
     */
    @Select("SELECT * FROM transfer_order WHERE out_trade_no = #{outTradeNo}")
    Optional<TransferOrder> findByOutTradeNo(@Param("outTradeNo") String outTradeNo);
    
    /**
     * 根据第三方交易号查询
     */
    @Select("SELECT * FROM transfer_order WHERE trade_no = #{tradeNo}")
    Optional<TransferOrder> findByTradeNo(@Param("tradeNo") String tradeNo);
    
    /**
     * 分页查询用户的转账订单
     */
    @Select("<script>" +
            "SELECT * FROM transfer_order WHERE user_id = #{userId}" +
            "<if test='channel != null and channel != \"\"'> AND channel = #{channel}</if>" +
            "<if test='status != null'> AND status = #{status}</if>" +
            " ORDER BY create_time DESC" +
            "</script>")
    IPage<TransferOrder> findByUserIdWithCondition(
            Page<TransferOrder> page,
            @Param("userId") Long userId,
            @Param("channel") String channel,
            @Param("status") Integer status
    );
}
