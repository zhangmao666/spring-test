-- 添加订阅用户
INSERT INTO `news_subscriber` (`email`, `username`, `is_active`, `create_time`, `update_time`)
VALUES ('1097765371@qq.com', '用户', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE `is_active` = 1, `update_time` = NOW();
