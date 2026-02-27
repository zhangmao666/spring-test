package com.example.springboottest.modules.ai.skills;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springaicommunity.agent.tools.FileSystemTools;
import org.springaicommunity.agent.tools.ShellTools;
import org.springaicommunity.agent.tools.SkillsTool;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Agent Skills 配置类
 *
 * <h3>核心能力：</h3>
 * <ul>
 *   <li>多目录 Skill 加载（classpath 内置 + 文件系统自定义）</li>
 *   <li>可配置工具开关（Shell / FileSystem）</li>
 *   <li>多模型路由（通过 AgentProperties.models 配置）</li>
 *   <li>工具调用计时诊断</li>
 * </ul>
 *
 * <h3>工作流程：</h3>
 * <ol>
 *   <li>用户提问 → LLM 判断意图</li>
 *   <li>LLM 调用 SkillsTool → 发现匹配的 SKILL.md → 加载完整指令</li>
 *   <li>LLM 按 SKILL.md 指令调用 ShellTools / FileSystemTools 完成任务</li>
 *   <li>LLM 整合真实数据，生成友好回复</li>
 * </ol>
 */
@Slf4j
@Configuration
public class AgentSkillsConfig {

    private final AgentProperties agentProperties;

    public AgentSkillsConfig(AgentProperties agentProperties) {
        this.agentProperties = agentProperties;
    }

    private static final String DEFAULT_SYSTEM_PROMPT = """
            你是一个具有多种技能的智能助手，运行在 Windows 环境下。
            
            你拥有以下工具：
            1. SkillsTool：用于发现和加载可用技能。
            2. ShellTools：用于执行 shell 命令或脚本文件。
               在 Windows 下使用 cmd /c 来执行 .bat 脚本。
            3. FileSystemTools：用于读取和写入文件。
            
            【重要】工作流程（必须严格遵守）：
            - 第一步：无论用户说什么，都必须先调用 SkillsTool 查找是否有匹配的技能。
            - 第二步：如果找到匹配的技能，按照技能中的指令完成任务。
              如果技能要求执行脚本，使用 ShellTools 执行。
              如果技能要求读取文件，使用 FileSystemTools 读取。
            - 第三步：如果没有匹配的技能，才直接用你的知识回答。
            
            回答请使用中文，自然友好。
            """;

    /**
     * 创建 SkillsTool —— 从多个目录扫描注册技能
     * <p>
     * 支持 classpath: 和 file: 前缀，同时自动加载自定义目录
     * </p>
     */
    @Bean
    public ToolCallback skillsToolCallback() throws IOException {
        log.info("📚 正在扫描并注册 Agent Skills...");

        SkillsTool.Builder builder = SkillsTool.builder();
        List<String> loadedDirs = new ArrayList<>();

        // 加载配置的目录
        for (String dir : agentProperties.getSkills().getDirectories()) {
            Resource resource = resolveResource(dir);
            if (resource != null && resource.exists()) {
                builder.addSkillsResource(resource);
                loadedDirs.add(dir);
            } else {
                log.warn("📚 Skill 目录不存在，跳过: {}", dir);
            }
        }

        // 加载自定义 Skill 目录
        String customDir = agentProperties.getSkills().getCustomDir();
        if (customDir != null && !customDir.isEmpty()) {
            File customFile = new File(customDir);
            if (customFile.exists() && customFile.isDirectory()) {
                builder.addSkillsResource(new FileSystemResource(customFile));
                loadedDirs.add("file:" + customDir);
            } else {
                // 自动创建自定义目录
                if (customFile.mkdirs()) {
                    log.info("� 已创建自定义 Skill 目录: {}", customDir);
                }
            }
        }

        ToolCallback callback = builder.build();
        log.info("📚 Agent Skills 注册完成！加载目录: {}", loadedDirs);
        return callback;
    }

    /**
     * FileSystemTools —— 根据配置决定是否启用
     */
    @Bean
    @ConditionalOnProperty(prefix = "agent.skills", name = "enable-fs-tools", havingValue = "true", matchIfMissing = true)
    public FileSystemTools fileSystemTools() {
        log.info("📁 初始化 FileSystemTools...");
        return FileSystemTools.builder().build();
    }

    /**
     * ShellTools —— 根据配置决定是否启用
     */
    @Bean
    @ConditionalOnProperty(prefix = "agent.skills", name = "enable-shell-tools", havingValue = "true", matchIfMissing = true)
    public ShellTools shellTools() {
        log.info("🐚 初始化 ShellTools...");
        return ShellTools.builder().build();
    }

    /**
     * 构建 Agent 的工具回调数组（带计时包装）
     */
    @Bean(name = "agentToolCallbacks")
    public ToolCallback[] agentToolCallbacks(
            ToolCallback skillsToolCallback,
            org.springframework.beans.factory.ObjectProvider<FileSystemTools> fileSystemToolsProvider,
            org.springframework.beans.factory.ObjectProvider<ShellTools> shellToolsProvider) {

        List<ToolCallback> allTools = new ArrayList<>();
        allTools.add(skillsToolCallback);

        FileSystemTools fsTools = fileSystemToolsProvider.getIfAvailable();
        if (fsTools != null) {
            allTools.addAll(Arrays.asList(ToolCallbacks.from(fsTools)));
            log.info("🔧 已注册 FileSystemTools");
        }

        ShellTools shellTools = shellToolsProvider.getIfAvailable();
        if (shellTools != null) {
            allTools.addAll(Arrays.asList(ToolCallbacks.from(shellTools)));
            log.info("🔧 已注册 ShellTools");
        }

        ToolCallback[] timedTools = TimingToolCallback.wrapAll(
                allTools.toArray(new ToolCallback[0])
        );
        log.info("🤖 共注册 {} 个工具（含计时包装）: {}", timedTools.length,
                Arrays.stream(timedTools).map(t -> t.getToolDefinition().name()).toList());

        return timedTools;
    }

    /**
     * 构建 Agent ChatClient（使用默认模型）
     */
    @Bean(name = "agentChatClient")
    public ChatClient agentChatClient(OpenAiChatModel defaultOpenAiChatModel,
                                       ToolCallback[] agentToolCallbacks) {
        log.info("🤖 正在构建 Agent ChatClient...");

        String systemPrompt = agentProperties.getSystemPrompt() != null
                ? agentProperties.getSystemPrompt()
                : DEFAULT_SYSTEM_PROMPT;

        ChatClient client = ChatClient.builder(defaultOpenAiChatModel)
                .defaultSystem(systemPrompt)
                .defaultToolCallbacks(agentToolCallbacks)
                .build();

        log.info("🤖 Agent ChatClient 构建完成！");
        return client;
    }

    /**
     * 解析 Resource（支持 classpath: 和 file: 前缀）
     */
    private Resource resolveResource(String location) {
        if (location.startsWith("classpath:")) {
            return new ClassPathResource(location.substring("classpath:".length()));
        } else if (location.startsWith("file:")) {
            return new FileSystemResource(location.substring("file:".length()));
        } else {
            // 默认当作文件系统路径
            return new FileSystemResource(location);
        }
    }
}
