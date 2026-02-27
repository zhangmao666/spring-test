-- 创建基金历史净值表
CREATE TABLE IF NOT EXISTS `fund_history` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `fund_code` VARCHAR(20) NOT NULL COMMENT '基金代码',
  `fund_name` VARCHAR(100) NOT NULL COMMENT '基金名称',
  `net_value` DECIMAL(10, 4) NOT NULL COMMENT '单位净值',
  `accumulated_value` DECIMAL(10, 4) DEFAULT NULL COMMENT '累计净值',
  `change_percent` DECIMAL(10, 2) DEFAULT 0.00 COMMENT '日涨跌幅(%)',
  `trade_date` DATE NOT NULL COMMENT '交易日期',
  `create_time` DATE NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_fund_code` (`fund_code`),
  KEY `idx_trade_date` (`trade_date`),
  KEY `idx_fund_code_date` (`fund_code`, `trade_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='基金历史净值表';
