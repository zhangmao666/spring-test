# Agent Skills 使用文档

## 目录

- [1. 概述](#1-概述)
- [2. 技术栈](#2-技术栈)
- [3. 架构设计](#3-架构设计)
- [4. 配置说明](#4-配置说明)
- [5. API 接口](#5-api-接口)
- [6. 内置 Skill 列表](#6-内置-skill-列表)
- [7. 自定义 Skill 开发指南](#7-自定义-skill-开发指南)
- [8. 工作流程](#8-工作流程)
- [9. 性能诊断](#9-性能诊断)
- [10. 常见问题](#10-常见问题)

---

## 1. 概述

Agent Skills 是基于 **Spring AI + spring-ai-agent-utils** 构建的智能代理系统，具备以下核心能力：

- **技能发现与执行**：Agent 自动匹配用户意图到对应 Skill，按 SKILL.md 指令完成任务
- **多模型路由**：支持在对话时动态切换 AI 模型（GLM-4、DeepSeek、GPT 等）
- **同步/流式输出**：支持普通同步调用和 SSE 实时流式推送
- **动态 Skill 管理**：通过 API 创建、查看、删除自定义 Skill
- **工具调用计时**：自动记录每次工具调用耗时，便于性能诊断

## 2. 技术栈

| 组件 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.5.9 | 基础框架 |
| Spring AI | 1.0.3 | AI 集成框架 |
| spring-ai-agent-utils | 0.4.2 | Agent Skill 支持（SkillsTool / ShellTools / FileSystemTools） |
| OpenAI 兼容代理 | yunwu.ai | 统一代理国内外大模型 |
| Java | 17 | 运行时 |

## 3. 架构设计

```
┌─────────────────────────────────────────────────────────────┐
│                    AgentSkillsController                     │
│         /ai/agent/chat  /skills  /models  /ask              │
└──────────┬──────────────────────┬────────────────────────────┘
           │                      │
    ┌──────▼──────┐     ┌────────▼─────────┐
    │ AgentService │     │ SkillGenerator   │
    │ (对话+路由)  │     │ Service(CRUD)    │
    └──────┬──────┘     └──────────────────┘
           │
    ┌──────▼──────────────────────────────┐
    │         AgentSkillsConfig           │
    │  ChatClient + ToolCallbacks 装配    │
    └──────┬──────────┬──────────┬────────┘
           │          │          │
    ┌──────▼───┐ ┌───▼────┐ ┌──▼────────┐
    │SkillsTool│ │Shell   │ │FileSystem │
    │(技能发现)│ │Tools   │ │Tools      │
    └──────────┘ └────────┘ └───────────┘
           │
    ┌──────▼──────────────────────────────┐
    │     SKILL.md（技能定义文件）         │
    │  classpath:skills/ + custom-skills/ │
    └─────────────────────────────────────┘
```

### 核心类说明

| 类 | 路径 | 职责 |
|---|---|---|
| `AgentProperties` | `modules/ai/skills/` | 外部化配置（模型、目录、工具开关） |
| `AgentSkillsConfig` | `modules/ai/skills/` | Bean 装配：ChatClient、ToolCallbacks |
| `AgentService` | `modules/ai/skills/` | 核心业务：模型路由、同步/流式对话 |
| `SkillGeneratorService` | `modules/ai/skills/` | Skill 动态 CRUD |
| `AgentSkillsController` | `modules/ai/skills/` | REST API 入口 |
| `TimingToolCallback` | `modules/ai/skills/` | 工具调用计时包装器 |
| `AgentChatRequest` | `modules/ai/skills/` | 请求 DTO |
| `AgentChatResponse` | `modules/ai/skills/` | 响应 DTO |

## 4. 配置说明

在 `application-dev.yml` 中添加以下配置：

```yaml
# Agent Skills 配置
agent:
  # 默认使用的模型路由名（对应 models 中的 key）
  default-model: default

  # Skills 相关配置
  skills:
    # Skill 加载目录（支持 classpath: 和 file: 前缀）
    directories:
      - classpath:skills/          # 内置 Skill（项目 resources/skills/）
      # - file:/opt/extra-skills/  # 额外的文件系统目录
    # 用户自定义 Skill 存放目录（通过 API 创建的 Skill 存在这里）
    custom-dir: ./custom-skills
    # 是否启用 Shell 脚本执行工具
    enable-shell-tools: true
    # 是否启用文件系统读写工具
    enable-fs-tools: true

  # 可用模型列表（key 为路由名）
  models:
    default:
      model: glm-4.7
      description: 智谱GLM-4.7（默认）
    fast:
      model: glm-4-flash
      description: 智谱GLM-4-Flash（快速响应）
    thinking:
      model: deepseek-v3.2-thinking
      description: DeepSeek深度思考
    gpt:
      model: gpt-5.2-chat-latest
      description: GPT-5.2（高质量）

  # 自定义系统提示词（可选，留空则使用内置默认提示词）
  # system-prompt: |
  #   你是一个智能助手...
```

### 配置项说明

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `agent.default-model` | String | `default` | 默认模型路由名 |
| `agent.skills.directories` | List | `[classpath:skills/]` | Skill 扫描目录 |
| `agent.skills.custom-dir` | String | `./custom-skills` | 自定义 Skill 目录 |
| `agent.skills.enable-shell-tools` | boolean | `true` | 启用 Shell 工具 |
| `agent.skills.enable-fs-tools` | boolean | `true` | 启用文件系统工具 |
| `agent.models.<key>.model` | String | — | 模型标识（API model 字段） |
| `agent.models.<key>.description` | String | — | 模型描述 |
| `agent.system-prompt` | String | `null` | 自定义系统提示词 |

## 5. API 接口

**Base URL**: `http://localhost:8879/api`

### 5.1 智能对话（同步）

```
POST /ai/agent/chat
Content-Type: application/json
```

**请求体**：

```json
{
  "message": "查一下服务器的系统信息",
  "model": "default",
  "conversationId": "可选的会话ID",
  "stream": false
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `message` | String | ✅ | 用户消息 |
| `model` | String | ❌ | 模型路由名（default/fast/thinking/gpt），留空用默认 |
| `conversationId` | String | ❌ | 会话 ID |
| `stream` | Boolean | ❌ | 设为 `true` 自动切换到 SSE 流式 |

**响应示例**：

```json
{
  "code": 200,
  "data": {
    "reply": "您的系统信息如下：\n- 操作系统：Windows 11 ...",
    "responseTimeMs": 12340,
    "success": true,
    "error": null,
    "skillsUsed": null
  }
}
```

**指定不同模型**：

```bash
# 快速模型
curl -X POST http://localhost:8879/api/ai/agent/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "你好", "model": "fast"}'

# 深度思考模型
curl -X POST http://localhost:8879/api/ai/agent/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "分析这个架构的优缺点", "model": "thinking"}'

# GPT 模型
curl -X POST http://localhost:8879/api/ai/agent/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "审查这段代码", "model": "gpt"}'
```

---

### 5.2 流式对话（SSE）

**方式一**：通过 `/chat` + `stream=true`

```bash
curl -N -X POST http://localhost:8879/api/ai/agent/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "帮我分析项目结构", "model": "default", "stream": true}'
```

**方式二**：直接调用 `/chatStream`

```
POST /ai/agent/chatStream
Content-Type: application/json
Accept: text/event-stream
```

```bash
curl -N -X POST http://localhost:8879/api/ai/agent/chatStream \
  -H "Content-Type: application/json" \
  -d '{"message": "查一下系统信息", "model": "fast"}'
```

**SSE 事件流格式**：

```
event:message
data:{"choices":[{"delta":{"content":"您的"}}]}

event:message
data:{"choices":[{"delta":{"content":"系统信息"}}]}

event:message
data:{"choices":[{"delta":{"content":"如下..."}}]}

event:done
data:{"status":"completed","responseTimeMs":8520}
```

| 事件名 | 说明 |
|--------|------|
| `message` | 增量文本片段 |
| `error` | 错误信息 |
| `done` | 完成标记，含总耗时 |

---

### 5.3 快速提问（GET）

```
GET /ai/agent/ask?q={问题}&model={模型}
```

```bash
# 默认模型
curl "http://localhost:8879/api/ai/agent/ask?q=查一下系统信息"

# 指定模型
curl "http://localhost:8879/api/ai/agent/ask?q=你好&model=fast"
```

---

### 5.4 获取可用模型列表

```
GET /ai/agent/models
```

```bash
curl http://localhost:8879/api/ai/agent/models
```

**响应**：

```json
{
  "code": 200,
  "data": [
    { "key": "default",  "model": "glm-4.7",              "description": "智谱GLM-4.7（默认）",       "isDefault": "true"  },
    { "key": "fast",     "model": "glm-4-flash",           "description": "智谱GLM-4-Flash（快速响应）", "isDefault": "false" },
    { "key": "thinking", "model": "deepseek-v3.2-thinking", "description": "DeepSeek深度思考",          "isDefault": "false" },
    { "key": "gpt",      "model": "gpt-5.2-chat-latest",   "description": "GPT-5.2（高质量）",          "isDefault": "false" }
  ]
}
```

---

### 5.5 获取技能列表

```
GET /ai/agent/skills
```

```bash
curl http://localhost:8879/api/ai/agent/skills
```

**响应**：

```json
{
  "code": 200,
  "data": [
    {
      "name": "weather-query",
      "displayName": "weather-query",
      "description": "查询天气",
      "builtIn": true,
      "path": "D:\\...\\skills\\weather-query"
    },
    {
      "name": "my-custom-skill",
      "displayName": "自定义技能",
      "description": "...",
      "author": "张三",
      "builtIn": false,
      "path": "D:\\...\\custom-skills\\my-custom-skill"
    }
  ]
}
```

---

### 5.6 创建自定义 Skill

```
POST /ai/agent/skills
Content-Type: application/json
```

**示例一：纯指令 Skill**

```bash
curl -X POST http://localhost:8879/api/ai/agent/skills \
  -H "Content-Type: application/json" \
  -d '{
    "name": "weekly-report",
    "description": "生成周报，包含本周完成、下周计划和风险点",
    "instructions": "1. 询问用户本周完成的主要工作\n2. 询问下周计划\n3. 询问存在的风险\n4. 按模板生成 Markdown 格式周报",
    "author": "张三"
  }'
```

**示例二：带脚本的 Skill**

```bash
curl -X POST http://localhost:8879/api/ai/agent/skills \
  -H "Content-Type: application/json" \
  -d '{
    "name": "disk-usage",
    "description": "检查磁盘使用情况，当用户询问磁盘空间、存储容量时触发",
    "instructions": "1. 使用 ShellTools 执行 scripts/disk-usage.bat\n2. 解析输出并以表格展示各分区使用情况",
    "author": "admin",
    "scriptName": "disk-usage.bat",
    "scriptContent": "@echo off\nwmic logicaldisk get size,freespace,caption"
  }'
```

**请求体字段**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `name` | String | ✅ | Skill 名称 |
| `description` | String | ✅ | 功能描述（Agent 用此匹配意图） |
| `instructions` | String | ✅ | 执行步骤指令 |
| `author` | String | ❌ | 作者 |
| `scriptName` | String | ❌ | 脚本文件名（默认 `{name}.bat`） |
| `scriptContent` | String | ❌ | 脚本内容 |

**响应**：

```json
{
  "code": 200,
  "data": {
    "name": "weekly-report",
    "displayName": "weekly-report",
    "description": "生成周报...",
    "author": "张三",
    "path": "D:\\...\\custom-skills\\weekly-report",
    "builtIn": false
  }
}
```

---

### 5.7 删除自定义 Skill

```
DELETE /ai/agent/skills/{skillName}
```

```bash
curl -X DELETE http://localhost:8879/api/ai/agent/skills/weekly-report
```

> **注意**：仅允许删除自定义目录中的 Skill，内置 Skill 不可删除。

---

## 6. 内置 Skill 列表

以下 Skill 位于 `src/main/resources/skills/` 目录中，随项目一起部署：

| Skill | 目录 | 说明 | 触发示例 |
|-------|------|------|----------|
| **weather-query** | `skills/weather-query/` | 调用 wttr.in API 查询实时天气 | "北京天气怎么样" |
| **project-analyzer** | `skills/project-analyzer/` | 扫描项目代码结构、模块、行数 | "分析一下这个项目" |
| **current-time-skill** | `skills/current-time-skill/` | 获取当前系统时间 | "现在几点了" |
| **code-reviewer** | `skills/code-reviewer/` | 审查 Java/Spring Boot 代码质量 | "帮我审查这段代码" |

每个 Skill 目录结构：

```
skills/
└── weather-query/
    ├── SKILL.md           # 技能定义文件（必需）
    └── scripts/
        └── get-weather.bat # 执行脚本（可选）
```

## 7. 自定义 Skill 开发指南

### 7.1 通过 API 创建

最简单的方式，参考 [5.6 创建自定义 Skill](#56-创建自定义-skill)。

### 7.2 手动创建

在 `custom-skills/`（或配置的 `agent.skills.custom-dir`）目录下创建子目录：

```
custom-skills/
└── my-skill/
    ├── SKILL.md
    └── scripts/         # 可选
        └── my-script.bat
```

### 7.3 SKILL.md 编写规范

```markdown
---
name: my-skill
description: 当用户要求XXX时使用此技能。简要描述功能，Agent据此匹配意图。
author: your-name
created: 2026-02-27
version: 1.0.0
---

# 技能名称

## 功能说明
详细描述此技能的用途。

## 执行步骤
1. 第一步：使用 FileSystemTools 读取某文件
2. 第二步：使用 ShellTools 执行 `cmd /c scripts/my-script.bat`
3. 第三步：解析输出结果，生成友好回复

## 回复要求
- 使用表格展示数据
- 使用中文回复
- 添加 emoji 增加可读性
```

**关键要点**：

1. **`description` 字段最重要**：Agent 根据此字段匹配用户意图，写清楚触发条件
2. **执行步骤要具体**：告诉 Agent 该调用哪个工具、执行什么命令
3. **Windows 环境**：脚本用 `.bat` 格式，通过 `cmd /c` 执行
4. **重启生效**：手动添加的 Skill 需重启服务才能被 SkillsTool 发现

### 7.4 Skill 匹配流程

```
用户消息 → Agent(LLM) → 调用 SkillsTool
                            │
                            ├─ 找到匹配 Skill → 读取 SKILL.md → 按指令执行
                            │                                      │
                            │                          ┌───────────┴───────────┐
                            │                          │                       │
                            │                     ShellTools              FileSystemTools
                            │                    (执行脚本)              (读写文件)
                            │
                            └─ 未找到 → Agent 用自身知识直接回答
```

## 8. 工作流程

### 完整请求处理流程

```
1. 用户发送 POST /ai/agent/chat
       │
2. AgentSkillsController 接收请求
       │
3. AgentService.chat() 解析模型路由
       │
4. ChatClient.prompt().user(message).options(model).call()
       │
5. LLM 第一轮：分析用户意图
       │
6. LLM 决定调用 SkillsTool → TimingToolCallback 计时
       │
7. SkillsTool 扫描所有 SKILL.md，返回匹配的技能内容
       │
8. LLM 第二轮：根据 SKILL.md 指令，决定调用 ShellTools/FileSystemTools
       │
9. ShellTools 执行脚本 / FileSystemTools 读取文件 → 获取真实数据
       │
10. LLM 第三轮：整合真实数据，生成友好回复
        │
11. 返回 AgentChatResponse
```

### 模型切换说明

所有模型共享同一个 yunwu.ai 代理，通过 `OpenAiChatOptions.builder().model(modelName)` 动态切换：

```
请求 {"model": "fast"} 
  → AgentService 查找 models.fast.model = "glm-4-flash"
    → ChatClient.options(model="glm-4-flash")
      → yunwu.ai 代理转发到智谱 GLM-4-Flash
```

## 9. 性能诊断

### TimingToolCallback 日志

每次工具调用都会输出计时日志：

```
⏱️ [工具调用开始] SkillsTool | 输入: {"query":"系统信息"}
⏱️ [工具调用完成] SkillsTool | 耗时: 45ms | 输出: Found skill: system-info...
⏱️ [工具调用开始] execCommand | 输入: {"command":"cmd /c scripts\\system-info.bat"}
⏱️ [工具调用完成] execCommand | 耗时: 1230ms | 输出: ...
```

### 典型耗时分布

| 阶段 | 说明 | 典型耗时 |
|------|------|----------|
| LLM 第一轮 | 意图分析 + 决定调用 SkillsTool | 3-8s |
| SkillsTool | 扫描匹配技能 | 10-50ms |
| LLM 第二轮 | 读取 SKILL.md + 决定调用工具 | 3-8s |
| ShellTools | 执行脚本获取数据 | 0.5-3s |
| LLM 第三轮 | 整合数据生成回复 | 3-10s |
| **总计** | | **10-30s** |

> 主要瓶颈在 LLM API 调用（多轮对话），可通过选择 `fast` 模型减少单轮延迟。

### 开启 Debug 日志

```yaml
logging:
  level:
    org.springframework.ai.chat.client: DEBUG
    org.springframework.ai.openai: DEBUG
    org.springframework.ai.tool: DEBUG
```

## 10. 常见问题

### Q: 新添加的 Skill 不生效？

**A**: 
- 通过 API 创建的 Skill 存在 `custom-skills/` 目录，需要重启服务让 SkillsTool 重新扫描
- 确认 `SKILL.md` 文件存在且 `description` 字段描述清晰

### Q: Agent 回复很慢？

**A**: 
- Agent 需要多轮 LLM 调用（意图→匹配→执行→回复），总耗时 = N × 单轮 LLM 延迟
- 使用 `"model": "fast"` 选择低延迟模型
- 查看 TimingToolCallback 日志定位瓶颈

### Q: 如何添加新的 AI 模型？

**A**: 在 `application-dev.yml` 的 `agent.models` 下添加：

```yaml
agent:
  models:
    my-new-model:
      model: some-model-id    # yunwu.ai 支持的模型标识
      description: 我的新模型
```

### Q: 如何禁用 Shell/文件系统工具？

**A**: 
```yaml
agent:
  skills:
    enable-shell-tools: false
    enable-fs-tools: false
```

### Q: 如何自定义系统提示词？

**A**:
```yaml
agent:
  system-prompt: |
    你是一个专业的运维助手...
    （自定义提示词内容）
```

### Q: `ToolContext is required` 报错？

**A**: 已在 `TimingToolCallback` 中修复。`call(String)` 方法会自动转发到 `call(String, ToolContext)` 并传入空上下文。确保使用最新代码即可。

### Q: Spring AI 版本兼容性？

**A**: 
- ✅ **Spring AI 1.0.3** + **Spring Boot 3.5.9** — 当前使用，稳定
- ❌ Spring AI 2.0.0-M2 — 需要 Spring Framework 7.0，目前不可用

---

## 附录：API 速查表

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/api/ai/agent/chat` | 智能对话（`stream=true` 切 SSE） |
| `POST` | `/api/ai/agent/chatStream` | 强制 SSE 流式对话 |
| `GET` | `/api/ai/agent/ask?q=&model=` | 快速提问 |
| `GET` | `/api/ai/agent/models` | 查看可用模型列表 |
| `GET` | `/api/ai/agent/skills` | 列出所有 Skill |
| `POST` | `/api/ai/agent/skills` | 创建自定义 Skill |
| `DELETE` | `/api/ai/agent/skills/{name}` | 删除自定义 Skill |
