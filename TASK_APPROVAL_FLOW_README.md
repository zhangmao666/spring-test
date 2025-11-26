# 工作任务审批流系统 - 完整实现文档

## 📋 系统概述

本系统是一个完整的工作任务审批流后端系统，支持多级审批节点、会签/或签模式、驳回、撤回、转交等完整功能。

## 🏗️ 技术架构

- **Spring Boot**: 3.2.0
- **MyBatis Plus**: 3.5.7
- **MySQL**: 8.0.33
- **Spring Security**: 权限控制
- **JWT**: 用户认证

## 📊 数据库设计

### 核心表结构（7张表）

1. **roles** - 角色表
2. **user_roles** - 用户角色关联表
3. **tasks** - 任务表
4. **task_approval_flows** - 审批流定义表
5. **task_approval_nodes** - 审批节点配置表
6. **task_approval_records** - 审批记录表

### SQL脚本位置
```
src/main/resources/sql/task_approval_flow_init.sql
```

**执行方式：**
```bash
mysql -h 118.24.128.221 -P 3306 -u admin -p'cxzx!@#123' spring_boot_test < task_approval_flow_init.sql
```

## 🔄 核心功能

### 1. 任务管理
- ✅ 创建任务（草稿状态）
- ✅ 提交审批
- ✅ 撤回任务
- ✅ 查询任务详情
- ✅ 我的待办任务
- ✅ 我创建的任务
- ✅ 我审批过的任务

### 2. 审批功能
- ✅ **会签（COUNTERSIGN）**：所有审批人都必须通过
- ✅ **或签（OR_SIGN）**：任意一个审批人通过即可
- ✅ **驳回到指定节点**：清除驳回节点之后的所有审批记录
- ✅ **转交他人审批**：将待审批任务转交给其他用户
- ✅ 审批历史查询

### 3. 审批流配置
- ✅ 创建审批流（支持多节点配置）
- ✅ 更新审批流（版本控制）
- ✅ 查询审批流详情
- ✅ 查询审批流列表

## 📁 项目结构

```
src/main/java/com/example/springboottest/
├── entity/                          # 实体类（6个）
│   ├── Role.java                    # 角色实体
│   ├── UserRole.java                # 用户角色关联
│   ├── Task.java                    # 任务实体
│   ├── TaskApprovalFlow.java        # 审批流定义
│   ├── TaskApprovalNode.java        # 审批节点配置
│   └── TaskApprovalRecord.java      # 审批记录
│
├── enums/                           # 枚举类（5个）
│   ├── TaskStatus.java              # 任务状态
│   ├── ApprovalAction.java          # 审批动作
│   ├── ApprovalResult.java          # 审批结果
│   ├── ApprovalType.java            # 审批类型（会签/或签）
│   └── ApproverType.java            # 审批人类型（角色/用户）
│
├── repository/                      # 数据访问层（6个）
│   ├── RoleRepository.java
│   ├── UserRoleRepository.java
│   ├── TaskRepository.java
│   ├── TaskApprovalFlowRepository.java
│   ├── TaskApprovalNodeRepository.java
│   └── TaskApprovalRecordRepository.java
│
├── DTO/                             # 数据传输对象
│   ├── CreateTaskRequest.java       # 创建任务请求
│   ├── ApproveRequest.java          # 审批通过请求
│   ├── RejectRequest.java           # 驳回请求
│   ├── TransferRequest.java         # 转交请求
│   ├── WithdrawRequest.java         # 撤回请求
│   ├── CreateFlowRequest.java       # 创建审批流请求
│   ├── FlowNodeRequest.java         # 审批节点请求
│   ├── UpdateFlowRequest.java       # 更新审批流请求
│   ├── TaskVO.java                  # 任务VO
│   ├── TaskDetailVO.java            # 任务详情VO
│   ├── ApprovalNodeVO.java          # 审批节点VO
│   ├── ApprovalRecordVO.java        # 审批记录VO
│   ├── ApprovalResultVO.java        # 审批结果VO
│   ├── TaskApprovalFlowVO.java      # 审批流VO
│   ├── TaskApprovalFlowDetailVO.java # 审批流详情VO
│   ├── TaskApprovalNodeVO.java      # 审批节点配置VO
│   └── PageResult.java              # 分页结果
│
├── service/                         # 业务逻辑层（3个核心服务）
│   ├── TaskService.java             # 任务服务
│   ├── TaskApprovalService.java     # 审批服务（核心）
│   └── TaskApprovalFlowService.java # 审批流配置服务
│
└── controller/                      # 控制器层（3个）
    ├── TaskController.java          # 任务接口
    ├── TaskApprovalController.java  # 审批接口
    └── TaskApprovalFlowController.java # 审批流配置接口
```

## 🔑 核心API接口

### 任务管理接口

#### 1. 创建任务
```http
POST /api/tasks
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "title": "项目立项申请",
  "content": "申请开展XXX项目",
  "taskType": "STANDARD",
  "priority": 3,
  "flowId": 1
}
```

#### 2. 提交审批
```http
POST /api/tasks/{taskId}/submit
Authorization: Bearer {jwt_token}
```

#### 3. 撤回任务
```http
POST /api/tasks/{taskId}/withdraw
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "reason": "需要补充材料"
}
```

#### 4. 获取任务详情
```http
GET /api/tasks/{taskId}
Authorization: Bearer {jwt_token}
```

#### 5. 我的待办任务
```http
GET /api/tasks/pending?page=1&size=10
Authorization: Bearer {jwt_token}
```

#### 6. 我创建的任务
```http
GET /api/tasks/created?page=1&size=10
Authorization: Bearer {jwt_token}
```

#### 7. 我审批过的任务
```http
GET /api/tasks/approved?page=1&size=10
Authorization: Bearer {jwt_token}
```

### 审批操作接口

#### 1. 审批通过
```http
POST /api/tasks/{taskId}/approvals/approve
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "comment": "同意通过"
}
```

#### 2. 驳回到指定节点
```http
POST /api/tasks/{taskId}/approvals/reject
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "rejectToNodeId": 2,
  "comment": "材料不完整，请补充后重新提交"
}
```

#### 3. 转交他人审批
```http
POST /api/tasks/{taskId}/approvals/transfer
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "transferToUserId": 5,
  "comment": "转交给张三处理"
}
```

#### 4. 获取审批历史
```http
GET /api/tasks/{taskId}/approvals/history
Authorization: Bearer {jwt_token}
```

### 审批流配置接口

#### 1. 创建审批流
```http
POST /api/approval-flows
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "flowCode": "FLOW_CUSTOM",
  "flowName": "自定义审批流",
  "description": "描述信息",
  "taskType": "CUSTOM",
  "nodes": [
    {
      "nodeCode": "NODE_1",
      "nodeName": "科长审批",
      "nodeOrder": 1,
      "approvalType": "OR_SIGN",
      "approverType": "ROLE",
      "approverRoles": ["ROLE_SECTION_CHIEF"],
      "timeoutHours": 24
    },
    {
      "nodeCode": "NODE_2",
      "nodeName": "处长审批",
      "nodeOrder": 2,
      "approvalType": "COUNTERSIGN",
      "approverType": "USER",
      "approverIds": [1, 2, 3],
      "timeoutHours": 48
    }
  ]
}
```

#### 2. 更新审批流
```http
PUT /api/approval-flows/{flowId}
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "flowName": "更新后的审批流名称",
  "description": "更新描述",
  "taskType": "CUSTOM",
  "nodes": [...]
}
```

#### 3. 获取审批流详情
```http
GET /api/approval-flows/{flowId}
Authorization: Bearer {jwt_token}
```

#### 4. 获取审批流列表
```http
GET /api/approval-flows?taskType=STANDARD
Authorization: Bearer {jwt_token}
```

## 🎯 状态机设计

### 任务状态流转

```
DRAFT (草稿)
  ↓ 提交审批
PENDING (待审批)
  ↓ 开始审批
IN_PROGRESS (审批中)
  ├→ APPROVED (已通过) - 全部节点审批通过
  ├→ REJECTED (已驳回) - 任意节点驳回
  └→ WITHDRAWN (已撤回) - 创建人撤回
```

### 审批类型说明

#### 会签（COUNTERSIGN）
- **定义**: 所有审批人都必须通过才算节点完成
- **适用场景**: 重要决策需要多人共同确认
- **实现逻辑**:
  ```java
  approvedUserIds.size() >= allApprovers.size() &&
  approvedUserIds.containsAll(allApprovers)
  ```

#### 或签（OR_SIGN）
- **定义**: 任意一个审批人通过即算节点完成
- **适用场景**: 多人中任意一人有权审批
- **实现逻辑**:
  ```java
  countApproved > 0
  ```

## 🔐 权限控制

### 角色体系
- **ROLE_USER**: 普通用户（可创建任务、审批）
- **ROLE_SECTION_CHIEF**: 科长
- **ROLE_DEPARTMENT_HEAD**: 处长
- **ROLE_BUREAU_CHIEF**: 局长
- **ROLE_ADMIN**: 系统管理员（可配置审批流）

### 权限注解
```java
@PreAuthorize("hasRole('USER')")    // 普通用户权限
@PreAuthorize("hasRole('ADMIN')")   // 管理员权限
```

## 📝 审批节点配置说明

### 审批人类型

#### 1. 按角色审批（ROLE）
```json
{
  "approverType": "ROLE",
  "approverRoles": ["ROLE_SECTION_CHIEF", "ROLE_DEPARTMENT_HEAD"]
}
```
- 系统自动查询拥有该角色的所有用户
- 动态：角色成员变化时自动生效

#### 2. 按用户审批（USER）
```json
{
  "approverType": "USER",
  "approverIds": [1, 2, 3]
}
```
- 指定具体用户ID
- 静态：固定审批人

## 🔍 核心功能实现

### 1. 任务编号生成
```
格式：TASK + 年月日 + 6位序列号
示例：TASK20250121000001
```

### 2. 驳回机制
- 驳回到指定节点（必须是之前的节点）
- 自动清除驳回节点之后的所有审批记录
- 重新创建驳回节点的待审批记录

### 3. 转交机制
- 将当前待审批记录标记为"已转交"
- 创建新的待审批记录给目标用户
- 保留完整的转交链路

### 4. 版本控制
- 审批流更新时创建新版本
- 旧版本标记为禁用
- 已启动的任务继续使用原版本

## 🎨 初始化数据

系统已预置3个标准审批流：

### 1. 标准三级审批流（FLOW_STANDARD）
```
科长（或签） → 处长（或签） → 局长（会签）
```

### 2. 紧急两级审批流（FLOW_URGENT）
```
处长（或签） → 局长（或签）
```

### 3. 简易单级审批流（FLOW_SIMPLE）
```
处长（或签）
```

## 🚀 使用示例

### 完整审批流程示例

```bash
# 1. 用户登录获取Token
POST /auth/login
{
  "username": "zhangsan",
  "password": "password123"
}
# 返回: { "accessToken": "eyJhbGc..." }

# 2. 创建任务
POST /api/tasks
Authorization: Bearer eyJhbGc...
{
  "title": "项目立项申请",
  "content": "申请开展XXX项目，预算100万",
  "taskType": "STANDARD",
  "priority": 3,
  "flowId": 1
}
# 返回: { "taskNo": "TASK20250121000001", "status": "DRAFT" }

# 3. 提交审批
POST /api/tasks/1/submit
Authorization: Bearer eyJhbGc...
# 返回: { "status": "PENDING", "currentNodeName": "科长审批" }

# 4. 科长审批通过
POST /api/tasks/1/approvals/approve
Authorization: Bearer <科长Token>
{
  "comment": "同意"
}
# 返回: { "message": "审批通过，已进入下一节点: 处长审批" }

# 5. 处长驳回到科长节点
POST /api/tasks/1/approvals/reject
Authorization: Bearer <处长Token>
{
  "rejectToNodeId": 1,
  "comment": "材料不完整"
}
# 返回: { "status": "REJECTED", "message": "任务已驳回到科长审批" }

# 6. 查看审批历史
GET /api/tasks/1/approvals/history
Authorization: Bearer eyJhbGc...
# 返回: [ { "nodeName": "科长审批", "result": "APPROVED" }, ... ]
```

## ⚠️ 注意事项

### 1. 数据库执行
- SQL脚本需要手动在服务器上执行
- 确保数据库连接信息正确

### 2. 权限配置
- 新用户默认没有角色，需要在 `user_roles` 表中分配
- ADMIN 角色才能创建和管理审批流

### 3. 审批流配置
- 节点顺序必须从1开始连续递增
- 驳回只能驳回到之前的节点
- 会签需要所有审批人通过

### 4. 任务状态
- 只有 DRAFT 和 REJECTED 状态的任务可以重新提交
- 只有 PENDING 和 IN_PROGRESS 状态的任务可以撤回

## 📚 扩展功能建议

### 可选扩展（未实现）
1. **消息通知**：集成ActiveMQ实现异步审批通知
2. **审批日志**：使用AOP记录审批操作日志
3. **审批超时**：实现审批超时自动提醒
4. **数据统计**：添加审批数据统计接口
5. **审批缓存**：使用Caffeine缓存审批流配置

## 📊 性能优化建议

1. **索引优化**：已在关键字段添加索引
2. **分页查询**：所有列表接口都支持分页
3. **事务管理**：关键操作已添加 `@Transactional`
4. **只读事务**：查询方法使用 `@Transactional(readOnly = true)`

## 🐛 调试技巧

### 查看审批流配置
```sql
SELECT * FROM task_approval_flows WHERE status = 1;
SELECT * FROM task_approval_nodes WHERE flow_id = 1 ORDER BY node_order;
```

### 查看任务审批状态
```sql
SELECT
  t.task_no,
  t.status,
  n.node_name AS current_node,
  t.created_at,
  t.submitted_at
FROM tasks t
LEFT JOIN task_approval_nodes n ON t.current_node_id = n.id
WHERE t.creator_id = 1;
```

### 查看审批记录
```sql
SELECT
  r.node_name,
  r.approver_name,
  r.action,
  r.result,
  r.comment,
  r.approval_time
FROM task_approval_records r
WHERE r.task_id = 1
ORDER BY r.node_order, r.created_at;
```

## ✅ 完成清单

- [x] 数据库表设计和SQL脚本
- [x] 5个枚举类
- [x] 6个实体类
- [x] 6个Repository接口
- [x] 8个请求DTO
- [x] 10个响应DTO
- [x] TaskService（任务管理）
- [x] TaskApprovalService（审批逻辑）
- [x] TaskApprovalFlowService（审批流配置）
- [x] TaskController（任务接口）
- [x] TaskApprovalController（审批接口）
- [x] TaskApprovalFlowController（审批流配置接口）
- [x] 会签和或签逻辑
- [x] 驳回到指定节点
- [x] 转交他人审批
- [x] 任务撤回
- [x] 状态机管理
- [x] 权限控制

## 🎉 总结

本系统完整实现了工作任务审批流的所有核心功能，代码严格遵循现有项目的架构风格和规范，可直接集成到现有Spring Boot项目中使用。

**核心特性：**
- ✨ 完整的多级审批流
- ✨ 灵活的会签/或签模式
- ✨ 强大的驳回和转交功能
- ✨ 清晰的状态机管理
- ✨ 完善的权限控制
- ✨ 版本化的审批流配置

**技术亮点：**
- 使用MyBatis Plus简化数据访问
- 事务管理保证数据一致性
- 枚举类型保证类型安全
- DTO模式实现层次解耦
- 状态机模式管理任务流转

---

**作者**: Claude Code
**日期**: 2025-01-21
**版本**: 1.0.0
