-- 转账订单表
CREATE TABLE IF NOT EXISTS `transfer_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `out_trade_no` VARCHAR(64) NOT NULL COMMENT '商户订单号',
    `trade_no` VARCHAR(128) DEFAULT NULL COMMENT '第三方交易号',
    `channel` VARCHAR(20) NOT NULL COMMENT '支付渠道: alipay-支付宝, wechat-微信',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '转账金额(元)',
    `payee_account` VARCHAR(128) NOT NULL COMMENT '收款账户(支付宝账号/微信openid)',
    `payee_name` VARCHAR(64) NOT NULL COMMENT '收款人姓名',
    `remark` VARCHAR(200) DEFAULT NULL COMMENT '转账备注',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '转账状态: 0-待处理, 1-处理中, 2-成功, 3-失败, 4-已关闭',
    `fail_reason` VARCHAR(500) DEFAULT NULL COMMENT '失败原因',
    `user_id` BIGINT NOT NULL COMMENT '发起用户ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `finish_time` DATETIME DEFAULT NULL COMMENT '完成时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_out_trade_no` (`out_trade_no`),
    KEY `idx_trade_no` (`trade_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_channel_status` (`channel`, `status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='转账订单表';

-- 添加示例数据（可选）
-- INSERT INTO `transfer_order` (`out_trade_no`, `channel`, `amount`, `payee_account`, `payee_name`, `remark`, `status`, `user_id`) VALUES
-- ('TF20231225120000ABCD1234', 'alipay', 100.00, 'test@alipay.com', '测试用户', '测试转账', 2, 1);
