package com.example.springboottest.modules.ai.skills;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Agent 配置属性
 * <p>
 * 支持多模型路由、多目录 Skill 加载、工具开关等配置
 * </p>
 *
 * <pre>
 * agent:
 *   skills:
 *     directories:
 *       - classpath:skills/
 *     custom-dir: ./custom-skills
 *     enable-shell-tools: true
 *     enable-fs-tools: true
 *   system-prompt: ...
 * </pre>
 */
@Data
@Component
@ConfigurationProperties(prefix = "agent")
public class AgentProperties {

    /**
     * Skills 相关配置
     */
    private SkillsConfig skills = new SkillsConfig();

    /**
     * Agent 系统提示词（可覆盖默认值）
     */
    private String systemPrompt;

    @Data
    public static class SkillsConfig {
        /**
         * Skill 加载目录（支持 classpath: 和 file: 前缀）
         */
        private List<String> directories = List.of("classpath:skills/");

        /**
         * 用户自定义 Skill 存储目录（文件系统路径）
         */
        private String customDir = "./custom-skills";

        /**
         * 是否启用 Shell 脚本执行工具
         */
        private boolean enableShellTools = true;

        /**
         * 是否启用文件系统读写工具
         */
        private boolean enableFsTools = true;
    }

}
