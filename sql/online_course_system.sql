-- ==========================================
-- 在线课程系统数据库脚本
-- ==========================================

-- 1. 课程系统用户表
CREATE TABLE IF NOT EXISTS course_users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
    role VARCHAR(20) NOT NULL COMMENT '角色：ADMIN, TEACHER, STUDENT',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_role (role),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程系统用户表';

-- 2. 课程表
CREATE TABLE IF NOT EXISTS courses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '课程ID',
    name VARCHAR(100) NOT NULL COMMENT '课程名称',
    teacher_id BIGINT NOT NULL COMMENT '授课教师ID',
    price DECIMAL(10, 2) NOT NULL DEFAULT 0 COMMENT '课程价格',
    status TINYINT DEFAULT 0 COMMENT '课程状态：0-草稿，1-已发布',
    description TEXT COMMENT '课程描述',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_status (status),
    INDEX idx_name (name),
    CONSTRAINT fk_course_teacher FOREIGN KEY (teacher_id) REFERENCES course_users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程表';

-- 3. 选课记录表
CREATE TABLE IF NOT EXISTS course_enrolls (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '选课记录ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    enroll_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '选课时间',
    UNIQUE KEY uk_student_course (student_id, course_id) COMMENT '防止重复选课',
    INDEX idx_student_id (student_id),
    INDEX idx_course_id (course_id),
    CONSTRAINT fk_enroll_student FOREIGN KEY (student_id) REFERENCES course_users(id),
    CONSTRAINT fk_enroll_course FOREIGN KEY (course_id) REFERENCES courses(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='选课记录表';

-- ==========================================
-- 初始化测试数据
-- ==========================================

-- 插入管理员
INSERT INTO course_users (username, password, role, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'ADMIN', 1);
-- 密码: 123456

-- 插入教师
INSERT INTO course_users (username, password, role, status) VALUES
('teacher1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'TEACHER', 1),
('teacher2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'TEACHER', 1);

-- 插入学生
INSERT INTO course_users (username, password, role, status) VALUES
('student1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'STUDENT', 1),
('student2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'STUDENT', 1),
('student3', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'STUDENT', 1);

-- 插入课程（teacher1的ID假设为2）
INSERT INTO courses (name, teacher_id, price, status, description) VALUES
('Java 入门到精通', 2, 199.00, 1, 'Java基础课程，适合初学者'),
('Spring Boot 实战', 2, 299.00, 1, 'Spring Boot框架实战开发'),
('MySQL 数据库', 3, 149.00, 0, 'MySQL数据库基础与高级特性'),
('Python 数据分析', 3, 249.00, 1, 'Python数据分析与可视化');

-- 插入选课记录（student1选了Java课程和Spring Boot课程）
INSERT INTO course_enrolls (student_id, course_id) VALUES
(4, 1),  -- student1 选 Java入门
(4, 2),  -- student1 选 Spring Boot
(5, 1),  -- student2 选 Java入门
(5, 4);  -- student2 选 Python数据分析

-- ==========================================
-- 查询验证
-- ==========================================
-- SELECT * FROM course_users;
-- SELECT * FROM courses;
-- SELECT * FROM course_enrolls;
