-- 每日资讯表
CREATE TABLE IF NOT EXISTS `daily_news` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` VARCHAR(500) NOT NULL COMMENT '标题',
  `content` TEXT COMMENT '内容',
  `source` VARCHAR(100) COMMENT '来源',
  `url` VARCHAR(500) COMMENT '链接',
  `category` VARCHAR(50) COMMENT '分类',
  `publish_time` DATETIME COMMENT '发布时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_create_time` (`create_time`),
  INDEX `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日资讯表';

-- 订阅用户表
CREATE TABLE IF NOT EXISTS `news_subscriber` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `email` VARCHAR(100) NOT NULL COMMENT '邮箱',
  `username` VARCHAR(100) COMMENT '用户名',
  `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_email` (`email`),
  INDEX `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订阅用户表';
