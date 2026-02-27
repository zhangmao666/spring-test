-- 日志管理表结构
-- 执行此脚本以创建登录日志和操作日志表

-- 登录日志表
CREATE TABLE IF NOT EXISTS `sys_login_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) DEFAULT '' COMMENT '用户名',
    `ip_address` VARCHAR(128) DEFAULT '' COMMENT '登录IP地址',
    `login_location` VARCHAR(255) DEFAULT '' COMMENT '登录地点',
    `browser` VARCHAR(50) DEFAULT '' COMMENT '浏览器类型',
    `os` VARCHAR(50) DEFAULT '' COMMENT '操作系统',
    `status` TINYINT DEFAULT 0 COMMENT '登录状态（0失败 1成功）',
    `msg` VARCHAR(255) DEFAULT '' COMMENT '提示消息',
    `login_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    PRIMARY KEY (`id`),
    INDEX `idx_username` (`username`),
    INDEX `idx_status` (`status`),
    INDEX `idx_login_time` (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS `sys_operation_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title` VARCHAR(50) DEFAULT '' COMMENT '模块标题',
    `business_type` INT DEFAULT 0 COMMENT '业务类型（0其它 1新增 2修改 3删除 4查询 5导出 6导入）',
    `method` VARCHAR(200) DEFAULT '' COMMENT '方法名称',
    `request_method` VARCHAR(10) DEFAULT '' COMMENT '请求方式',
    `operator_name` VARCHAR(50) DEFAULT '' COMMENT '操作人员',
    `request_url` VARCHAR(255) DEFAULT '' COMMENT '请求URL',
    `ip_address` VARCHAR(128) DEFAULT '' COMMENT '主机地址',
    `request_param` TEXT COMMENT '请求参数',
    `json_result` TEXT COMMENT '返回参数',
    `status` TINYINT DEFAULT 0 COMMENT '操作状态（0失败 1成功）',
    `error_msg` TEXT COMMENT '错误消息',
    `operation_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    `cost_time` BIGINT DEFAULT 0 COMMENT '消耗时间（毫秒）',
    PRIMARY KEY (`id`),
    INDEX `idx_business_type` (`business_type`),
    INDEX `idx_status` (`status`),
    INDEX `idx_operation_time` (`operation_time`),
    INDEX `idx_operator_name` (`operator_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';
