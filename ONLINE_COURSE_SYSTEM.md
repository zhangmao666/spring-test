# 在线课程系统

## 概述

在线课程系统提供用户管理、课程管理、选课功能，支持 ADMIN、TEACHER、STUDENT 三种角色。

---

## 模块结构

```
├── entity/
│   ├── CourseUser.java       # 课程系统用户
│   ├── Course.java           # 课程
│   └── CourseEnroll.java     # 选课记录
├── enums/
│   ├── CourseStatus.java     # 课程状态 (DRAFT, PUBLISHED)
│   └── CourseUserRole.java   # 用户角色 (ADMIN, TEACHER, STUDENT)
├── repository/
│   ├── CourseUserRepository.java
│   ├── CourseRepository.java
│   └── CourseEnrollRepository.java
├── service/
│   ├── CourseUserService.java
│   ├── CourseService.java
│   └── EnrollService.java
├── controller/
│   ├── CourseUserController.java
│   ├── CourseController.java
│   └── EnrollController.java
└── DTO/course/
    ├── CourseUserDTO.java
    ├── CreateCourseUserRequest.java
    ├── CourseDTO.java
    ├── CreateCourseRequest.java
    ├── UpdateCourseRequest.java
    ├── EnrollRequest.java
    └── EnrollDTO.java
```

---

## 数据库设置

执行 SQL 脚本创建表和测试数据：

```bash
mysql -h your_host -u your_user -p your_database < sql/online_course_system.sql
```

### 表结构

| 表名 | 说明 |
|------|------|
| `course_users` | 课程系统用户表 |
| `courses` | 课程表 |
| `course_enrolls` | 选课记录表 |

---

## API 接口

### 用户管理 `/api/course-users`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/course-users` | 创建用户 | 需要 |
| GET | `/api/course-users` | 获取所有用户 | 需要 |
| GET | `/api/course-users/{userId}` | 获取用户详情 | 需要 |
| GET | `/api/course-users/teachers` | 获取所有教师 | 需要 |
| GET | `/api/course-users/students` | 获取所有学生 | 需要 |
| PUT | `/api/course-users/{userId}/enable` | 启用用户 | 需要 |
| PUT | `/api/course-users/{userId}/disable` | 禁用用户 | 需要 |
| DELETE | `/api/course-users/{userId}` | 删除用户 | 需要 |

#### 创建用户示例

```bash
POST /api/course-users
Content-Type: application/json

{
  "username": "new_teacher",
  "password": "123456",
  "role": "TEACHER"
}
```

---

### 课程管理 `/api/courses`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/courses` | 创建课程（草稿） | 需要 |
| PUT | `/api/courses/{courseId}` | 更新课程 | 需要 |
| POST | `/api/courses/{courseId}/publish` | 发布课程 | 需要 |
| POST | `/api/courses/{courseId}/unpublish` | 下架课程 | 需要 |
| GET | `/api/courses` | 获取已发布课程 | **公开** |
| GET | `/api/courses/all` | 获取所有课程 | 需要 |
| GET | `/api/courses/{courseId}` | 获取课程详情 | 需要 |
| GET | `/api/courses/teacher/{teacherId}` | 获取教师课程 | 需要 |
| GET | `/api/courses/search?keyword=xxx` | 搜索课程 | **公开** |
| DELETE | `/api/courses/{courseId}` | 删除课程 | 需要 |

#### 创建课程示例

```bash
POST /api/courses
Content-Type: application/json
X-API-Key: your_api_key

{
  "name": "Vue.js 入门教程",
  "teacherId": 2,
  "price": 199.00,
  "description": "前端框架Vue.js从零开始学习"
}
```

#### 发布课程

```bash
POST /api/courses/1/publish
X-API-Key: your_api_key
```

> **注意**：发布课程时会校验 teacherId 对应的教师是否存在且有效

---

### 选课管理 `/api/enrolls`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/enrolls` | 选课 | 需要 |
| DELETE | `/api/enrolls?studentId=x&courseId=y` | 取消选课 | 需要 |
| GET | `/api/enrolls/student/{studentId}` | 学生选课列表 | 需要 |
| GET | `/api/enrolls/course/{courseId}` | 课程学生列表 | 需要 |
| GET | `/api/enrolls/check?studentId=x&courseId=y` | 检查是否已选 | 需要 |
| GET | `/api/enrolls/stats/course/{courseId}` | 课程选课统计 | 需要 |
| GET | `/api/enrolls/stats/student/{studentId}` | 学生选课统计 | 需要 |

#### 选课示例

```bash
POST /api/enrolls
Content-Type: application/json
X-API-Key: your_api_key

{
  "studentId": 4,
  "courseId": 1
}
```

> **注意**：同一学生不能重复选同一课程，系统会自动校验

---

## 业务规则

### 课程状态流转

```
创建课程 → DRAFT（草稿）
         ↓ 发布
     PUBLISHED（已发布）
         ↓ 下架
       DRAFT
```

### 发布课程校验

发布课程时会进行以下校验：
1. 课程必须存在
2. 课程不能是已发布状态
3. **教师必须存在且状态为启用**

### 选课校验

1. 学生必须存在且为 STUDENT 角色
2. 课程必须存在且为 PUBLISHED 状态
3. **学生不能重复选同一课程**

---

## 测试数据

执行 SQL 脚本后会创建以下测试数据：

### 用户

| ID | 用户名 | 角色 | 密码 |
|----|--------|------|------|
| 1 | admin | ADMIN | 123456 |
| 2 | teacher1 | TEACHER | 123456 |
| 3 | teacher2 | TEACHER | 123456 |
| 4 | student1 | STUDENT | 123456 |
| 5 | student2 | STUDENT | 123456 |
| 6 | student3 | STUDENT | 123456 |

### 课程

| ID | 名称 | 教师 | 价格 | 状态 |
|----|------|------|------|------|
| 1 | Java 入门到精通 | teacher1 | 199.00 | 已发布 |
| 2 | Spring Boot 实战 | teacher1 | 299.00 | 已发布 |
| 3 | MySQL 数据库 | teacher2 | 149.00 | 草稿 |
| 4 | Python 数据分析 | teacher2 | 249.00 | 已发布 |

---

## 快速测试

### 1. 查看已发布课程（无需认证）

```bash
curl http://localhost:8879/api/courses
```

### 2. 创建用户（需要认证）

```bash
curl -X POST http://localhost:8879/api/course-users \
  -H "Content-Type: application/json" \
  -H "X-API-Key: your_api_key" \
  -d '{"username":"test_student","password":"123456","role":"STUDENT"}'
```

### 3. 选课

```bash
curl -X POST http://localhost:8879/api/enrolls \
  -H "Content-Type: application/json" \
  -H "X-API-Key: your_api_key" \
  -d '{"studentId":4,"courseId":4}'
```

---

## 错误码

| 错误信息 | 说明 |
|----------|------|
| 用户名已存在 | 注册时用户名重复 |
| 教师不存在或不是有效的教师 | 创建/发布课程时教师校验失败 |
| 学生不存在或不是有效的学生 | 选课时学生校验失败 |
| 课程未发布，无法选课 | 只能选已发布的课程 |
| 您已选过该课程，不能重复选课 | 防止重复选课 |
| 课程已有学生选课，无法删除 | 有选课记录的课程不能删除 |
