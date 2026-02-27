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
 *   models:
 *     default:
 *       model: glm-4.7
 *       description: 智谱GLM-4.7（默认）
 *     fast:
 *       model: glm-4-flash
 *       description: 智谱GLM-4-Flash（快速）
 *     thinking:
 *       model: deepseek-v3.2-thinking
 *       description: DeepSeek深度思考
 *   default-model: default
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
     * 可用模型列表（key 为路由名称，如 default / fast / thinking）
     */
    private Map<String, ModelConfig> models = new LinkedHashMap<>();

    /**
     * 默认使用的模型路由名称
     */
    private String defaultModel = "default";

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

    @Data
    public static class ModelConfig {
        /**
         * 模型标识（传给 API 的 model 字段）
         */
        private String model;

        /**
         * 模型显示名 / 描述
         */
        private String description;
    }
}
