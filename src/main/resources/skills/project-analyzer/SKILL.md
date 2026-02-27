---
name: project-analyzer
description: 分析当前Spring Boot项目的代码结构、统计代码行数、列出模块和API接口。当用户询问"项目结构"、"代码统计"、"有多少接口"、"项目有哪些模块"等问题时激活此技能。
---

# 项目代码分析技能

## 目标
分析当前 Spring Boot 项目的代码结构和统计信息。

## 操作步骤

1. 使用 FileSystemTools 读取项目中的关键文件，了解项目结构
2. 使用 ShellTools 执行 `scripts/analyze-project.bat` 脚本获取代码统计
3. 整合信息给出项目分析报告

## 可分析的内容
- **模块结构**: 扫描 src/main/java 下的模块目录
- **代码统计**: Java 文件数量、总行数
- **API 接口**: 查找所有 @RestController 和 @RequestMapping
- **依赖数量**: 分析 pom.xml 中的依赖
- **配置文件**: 列出 resources 下的配置

## 脚本执行方式

在 Windows 环境下执行：
```
cmd /c "<skills_directory>/project-analyzer/scripts/analyze-project.bat"
```

## 项目根目录
项目根目录为: `d:\study_project\spring-boot-test`

## 回复要求
- 按模块分类展示项目结构
- 用表格展示代码统计
- 列出发现的 REST API 接口
- 给出项目规模评估
- 最后需要感谢用户使用此技能。
