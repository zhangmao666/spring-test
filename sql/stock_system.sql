-- ========================================
-- 股票实时分析系统数据库表设计
-- 创建时间: 2025-11-27
-- ========================================

-- 1. 股票基本信息表
CREATE TABLE IF NOT EXISTS stocks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '股票ID',
    stock_code VARCHAR(10) NOT NULL UNIQUE COMMENT '股票代码（如：600000）',
    stock_name VARCHAR(50) NOT NULL COMMENT '股票名称',
    market VARCHAR(10) NOT NULL COMMENT '市场：SH-上海，SZ-深圳',
    industry VARCHAR(50) COMMENT '所属行业',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-停牌',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_stock_code (stock_code),
    INDEX idx_market (market),
    INDEX idx_status (status),
    INDEX idx_industry (industry)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='股票基本信息表';

-- 2. 实时行情表
CREATE TABLE IF NOT EXISTS stock_quotes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '行情ID',
    stock_id BIGINT NOT NULL COMMENT '股票ID',
    current_price DECIMAL(10, 2) NOT NULL COMMENT '当前价格',
    open_price DECIMAL(10, 2) COMMENT '开盘价',
    close_price DECIMAL(10, 2) COMMENT '收盘价',
    high_price DECIMAL(10, 2) COMMENT '最高价',
    low_price DECIMAL(10, 2) COMMENT '最低价',
    volume BIGINT COMMENT '成交量（股）',
    turnover DECIMAL(15, 2) COMMENT '成交额（元）',
    change_amount DECIMAL(10, 2) COMMENT '涨跌额',
    change_percent DECIMAL(6, 2) COMMENT '涨跌幅(%)',
    quote_time DATETIME NOT NULL COMMENT '行情时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_stock_id (stock_id),
    INDEX idx_quote_time (quote_time),
    INDEX idx_stock_quote_time (stock_id, quote_time DESC),
    CONSTRAINT fk_quote_stock FOREIGN KEY (stock_id) REFERENCES stocks(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='实时行情表';

-- 3. AI分析记录表
CREATE TABLE IF NOT EXISTS stock_analysis (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分析ID',
    stock_id BIGINT NOT NULL COMMENT '股票ID',
    analysis_time DATETIME NOT NULL COMMENT '分析时间',
    analysis_content TEXT NOT NULL COMMENT 'AI分析内容',
    trend_type VARCHAR(20) COMMENT '走势判断：UP-上涨，DOWN-下跌，STABLE-平稳',
    confidence_score DECIMAL(5, 2) COMMENT '置信度分数（0-100）',
    predicted_price DECIMAL(10, 2) COMMENT '预测价格',
    risk_level VARCHAR(20) COMMENT '风险等级：LOW-低，MEDIUM-中，HIGH-高',
    ai_provider VARCHAR(20) COMMENT 'AI提供商：openai, qwen, wenxin',
    status TINYINT DEFAULT 1 COMMENT '状态：1-有效，0-失效',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_stock_id (stock_id),
    INDEX idx_analysis_time (analysis_time),
    INDEX idx_stock_analysis_time (stock_id, analysis_time DESC),
    INDEX idx_trend_type (trend_type),
    INDEX idx_ai_provider (ai_provider),
    CONSTRAINT fk_analysis_stock FOREIGN KEY (stock_id) REFERENCES stocks(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='股票AI分析记录表';

-- 4. 用户关注股票表
CREATE TABLE IF NOT EXISTS user_stock_watch (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关注ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    stock_id BIGINT NOT NULL COMMENT '股票ID',
    watch_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
    alert_enabled TINYINT DEFAULT 0 COMMENT '是否启用价格提醒：1-是，0-否',
    alert_price DECIMAL(10, 2) COMMENT '提醒价格',
    alert_type VARCHAR(20) COMMENT '提醒类型：UP-涨到，DOWN-跌到',
    notes VARCHAR(500) COMMENT '备注',
    UNIQUE KEY uk_user_stock (user_id, stock_id),
    INDEX idx_user_id (user_id),
    INDEX idx_stock_id (stock_id),
    INDEX idx_watch_time (watch_time),
    CONSTRAINT fk_watch_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_watch_stock FOREIGN KEY (stock_id) REFERENCES stocks(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户关注股票表';

-- ========================================
-- 初始化数据
-- ========================================

-- 插入常见股票数据（示例）
INSERT INTO stocks (stock_code, stock_name, market, industry, status) VALUES
('sh600000', '浦发银行', 'SH', '银行', 1),
('sh600036', '招商银行', 'SH', '银行', 1),
('sh600519', '贵州茅台', 'SH', '白酒', 1),
('sh601318', '中国平安', 'SH', '保险', 1),
('sz000001', '平安银行', 'SZ', '银行', 1),
('sz000002', '万科A', 'SZ', '房地产', 1),
('sz000858', '五粮液', 'SZ', '白酒', 1),
('sz300750', '宁德时代', 'SZ', '新能源', 1)
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- ========================================
-- 创建视图（方便查询）
-- ========================================

-- 股票最新行情视图
CREATE OR REPLACE VIEW v_latest_stock_quotes AS
SELECT
    s.id AS stock_id,
    s.stock_code,
    s.stock_name,
    s.market,
    s.industry,
    sq.current_price,
    sq.open_price,
    sq.close_price,
    sq.high_price,
    sq.low_price,
    sq.volume,
    sq.turnover,
    sq.change_amount,
    sq.change_percent,
    sq.quote_time
FROM stocks s
LEFT JOIN (
    SELECT
        stock_id,
        current_price,
        open_price,
        close_price,
        high_price,
        low_price,
        volume,
        turnover,
        change_amount,
        change_percent,
        quote_time,
        ROW_NUMBER() OVER (PARTITION BY stock_id ORDER BY quote_time DESC) as rn
    FROM stock_quotes
) sq ON s.id = sq.stock_id AND sq.rn = 1
WHERE s.status = 1;

-- 股票最新分析视图
CREATE OR REPLACE VIEW v_latest_stock_analysis AS
SELECT
    s.id AS stock_id,
    s.stock_code,
    s.stock_name,
    sa.analysis_time,
    sa.analysis_content,
    sa.trend_type,
    sa.confidence_score,
    sa.predicted_price,
    sa.risk_level,
    sa.ai_provider
FROM stocks s
LEFT JOIN (
    SELECT
        stock_id,
        analysis_time,
        analysis_content,
        trend_type,
        confidence_score,
        predicted_price,
        risk_level,
        ai_provider,
        ROW_NUMBER() OVER (PARTITION BY stock_id ORDER BY analysis_time DESC) as rn
    FROM stock_analysis
    WHERE status = 1
) sa ON s.id = sa.stock_id AND sa.rn = 1
WHERE s.status = 1;

-- ========================================
-- 创建存储过程（数据清理）
-- ========================================

-- 清理30天前的历史行情数据
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS sp_clean_old_quotes()
BEGIN
    DELETE FROM stock_quotes
    WHERE quote_time < DATE_SUB(NOW(), INTERVAL 30 DAY);

    SELECT ROW_COUNT() AS deleted_rows;
END //
DELIMITER ;

-- 清理90天前的AI分析记录
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS sp_clean_old_analysis()
BEGIN
    UPDATE stock_analysis
    SET status = 0
    WHERE analysis_time < DATE_SUB(NOW(), INTERVAL 90 DAY)
    AND status = 1;

    SELECT ROW_COUNT() AS updated_rows;
END //
DELIMITER ;

-- ========================================
-- 数据统计查询示例
-- ========================================

-- 查询用户关注股票的最新行情和分析
-- SELECT
--     usw.user_id,
--     vlq.*,
--     vla.analysis_content,
--     vla.trend_type,
--     vla.risk_level
-- FROM user_stock_watch usw
-- JOIN v_latest_stock_quotes vlq ON usw.stock_id = vlq.stock_id
-- LEFT JOIN v_latest_stock_analysis vla ON usw.stock_id = vla.stock_id
-- WHERE usw.user_id = ?;

-- ========================================
-- 创建完成
-- ========================================
