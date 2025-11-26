-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS spring_boot_test 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE spring_boot_test;

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    status INT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0禁用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 插入测试用户（密码是 123456 的BCrypt加密结果）
INSERT INTO users (username, password, email, status) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVajIK', 'admin@example.com', 1),
('test', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVajIK', 'test@example.com', 1)
ON DUPLICATE KEY UPDATE 
username = VALUES(username);

-- 创建设备类型表
CREATE TABLE IF NOT EXISTS device_type (
    device_type_code VARCHAR(40) PRIMARY KEY COMMENT '设备类型代码',
    name VARCHAR(40) COMMENT '名称',
    status TINYINT COMMENT '状态：1启用，0禁用',
    full_name VARCHAR(100) COMMENT '全名',
    tb_type_code VARCHAR(20) COMMENT '类型代码',
    icon VARCHAR(1024) COMMENT '图标URL',
    introduction VARCHAR(255) COMMENT '介绍',
    create_time DATETIME COMMENT '创建时间',
    create_by BIGINT COMMENT '创建人ID',
    update_time DATETIME COMMENT '更新时间',
    update_by BIGINT COMMENT '更新人ID',
    icon_pc VARCHAR(1024) COMMENT 'PC端图标URL',
    icon_colour VARCHAR(10) COMMENT '图标颜色',
    device_img VARCHAR(255) COMMENT '设备图片URL',
    INDEX idx_name (name),
    INDEX idx_status (status),
    INDEX idx_tb_type_code (tb_type_code),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备类型表';

-- 插入测试设备类型数据
INSERT INTO device_type (device_type_code, name, status, full_name, tb_type_code, icon, introduction, create_time, create_by, update_time, update_by, icon_pc, icon_colour, device_img) VALUES 
('SENSOR_001', '温度传感器', 1, '高精度温度传感器', 'TEMP', '/icons/temperature.png', '用于监测环境温度的高精度传感器', NOW(), 1, NOW(), 1, '/icons/pc/temperature.png', '#FF5722', '/images/temp_sensor.jpg'),
('SENSOR_002', '湿度传感器', 1, '数字湿度传感器', 'HUMI', '/icons/humidity.png', '用于监测空气湿度的数字传感器', NOW(), 1, NOW(), 1, '/icons/pc/humidity.png', '#2196F3', '/images/humi_sensor.jpg'),
('CAMERA_001', '监控摄像头', 1, '高清网络摄像头', 'CAM', '/icons/camera.png', '1080P高清网络监控摄像头', NOW(), 1, NOW(), 1, '/icons/pc/camera.png', '#4CAF50', '/images/camera.jpg')
ON DUPLICATE KEY UPDATE 
device_type_code = VALUES(device_type_code);

-- 创建API客户端表
CREATE TABLE IF NOT EXISTS api_clients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    client_id VARCHAR(100) NOT NULL UNIQUE COMMENT '客户端ID',
    client_secret VARCHAR(255) NOT NULL COMMENT '客户端密钥（加密存储）',
    client_name VARCHAR(200) NOT NULL COMMENT '客户端名称',
    scopes VARCHAR(500) COMMENT '权限范围（逗号分隔）',
    token_expiration BIGINT COMMENT 'Token过期时间（毫秒）',
    status INT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0禁用',
    description VARCHAR(1000) COMMENT '描述信息',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_client_id (client_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='API客户端表';

-- 插入测试API客户端
-- 注意：这里的client_secret是 "123456" 的BCrypt加密结果
-- 测试客户端凭证：
-- client_id: client_test_demo_001
-- client_secret: 123456
INSERT INTO api_clients (client_id, client_secret, client_name, scopes, token_expiration, status, description) VALUES 
('client_test_demo_001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVajIK', '测试客户端', 'read,write', 7200000, 1, '用于开发测试的API客户端')
ON DUPLICATE KEY UPDATE 
client_id = VALUES(client_id);

-- 创建字典表
CREATE TABLE IF NOT EXISTS dict (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '字典ID',
    dict_code VARCHAR(50) NOT NULL UNIQUE COMMENT '字典编码',
    dict_name VARCHAR(100) NOT NULL COMMENT '字典名称',
    description VARCHAR(500) COMMENT '字典描述',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by BIGINT COMMENT '创建人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by BIGINT COMMENT '更新人ID',
    INDEX idx_dict_code (dict_code),
    INDEX idx_dict_name (dict_name),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典表';

-- 创建字典项表
CREATE TABLE IF NOT EXISTS dict_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '字典项ID',
    dict_id BIGINT NOT NULL COMMENT '字典ID',
    item_label VARCHAR(100) NOT NULL COMMENT '字典项标签',
    item_value VARCHAR(100) NOT NULL COMMENT '字典项值',
    item_sort INT NOT NULL DEFAULT 0 COMMENT '排序号',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0禁用',
    description VARCHAR(500) COMMENT '描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by BIGINT COMMENT '创建人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by BIGINT COMMENT '更新人ID',
    INDEX idx_dict_id (dict_id),
    INDEX idx_item_value (item_value),
    INDEX idx_status (status),
    INDEX idx_item_sort (item_sort),
    CONSTRAINT fk_dict_item_dict FOREIGN KEY (dict_id) REFERENCES dict(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典项表';

-- 插入测试字典数据
INSERT INTO dict (dict_code, dict_name, description, status, create_by, update_by) VALUES
('user_status', '用户状态', '用户账户状态字典', 1, 1, 1),
('device_status', '设备状态', '设备运行状态字典', 1, 1, 1),
('gender', '性别', '性别字典', 1, 1, 1),
('task_status', '任务状态', '任务状态字典', 1, 1, 1)
ON DUPLICATE KEY UPDATE
dict_code = VALUES(dict_code);

-- 插入测试字典项数据
INSERT INTO dict_item (dict_id, item_label, item_value, item_sort, status, description, create_by, update_by) VALUES
-- 用户状态
((SELECT id FROM dict WHERE dict_code = 'user_status'), '启用', '1', 1, 1, '用户账户正常启用', 1, 1),
((SELECT id FROM dict WHERE dict_code = 'user_status'), '禁用', '0', 2, 1, '用户账户已禁用', 1, 1),
((SELECT id FROM dict WHERE dict_code = 'user_status'), '锁定', '-1', 3, 1, '用户账户已锁定', 1, 1),
-- 设备状态
((SELECT id FROM dict WHERE dict_code = 'device_status'), '在线', 'online', 1, 1, '设备在线运行', 1, 1),
((SELECT id FROM dict WHERE dict_code = 'device_status'), '离线', 'offline', 2, 1, '设备离线', 1, 1),
((SELECT id FROM dict WHERE dict_code = 'device_status'), '故障', 'error', 3, 1, '设备故障', 1, 1),
((SELECT id FROM dict WHERE dict_code = 'device_status'), '维护', 'maintenance', 4, 1, '设备维护中', 1, 1),
-- 性别
((SELECT id FROM dict WHERE dict_code = 'gender'), '男', 'male', 1, 1, '男性', 1, 1),
((SELECT id FROM dict WHERE dict_code = 'gender'), '女', 'female', 2, 1, '女性', 1, 1),
((SELECT id FROM dict WHERE dict_code = 'gender'), '未知', 'unknown', 3, 1, '未知性别', 1, 1),
-- 任务状态
((SELECT id FROM dict WHERE dict_code = 'task_status'), '待处理', 'pending', 1, 1, '任务待处理', 1, 1),
((SELECT id FROM dict WHERE dict_code = 'task_status'), '进行中', 'in_progress', 2, 1, '任务进行中', 1, 1),
((SELECT id FROM dict WHERE dict_code = 'task_status'), '已完成', 'completed', 3, 1, '任务已完成', 1, 1),
((SELECT id FROM dict WHERE dict_code = 'task_status'), '已取消', 'cancelled', 4, 1, '任务已取消', 1, 1)
ON DUPLICATE KEY UPDATE
item_label = VALUES(item_label);

-- 查看创建结果
SELECT 'Database and tables created successfully!' as message;
