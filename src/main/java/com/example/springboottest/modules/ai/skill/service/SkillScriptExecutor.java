package com.example.springboottest.modules.ai.skill.service;

import com.example.springboottest.modules.ai.skill.dto.SkillTestResponse;
import com.example.springboottest.modules.ai.skill.entity.AiSkill;
import com.example.springboottest.modules.ai.skill.entity.AiSkillExecutionLog;
import com.example.springboottest.modules.ai.skill.mapper.AiSkillExecutionLogMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SkillScriptExecutor {

    private static final int MAX_OUTPUT_LENGTH = 20 * 1024;
    private static final long DEFAULT_TIMEOUT_SECONDS = 30;

    private final ObjectMapper objectMapper;
    private final AiSkillExecutionLogMapper executionLogMapper;

    public SkillTestResponse execute(AiSkill skill, String conversationId, Map<String, Object> arguments) {
        long start = System.currentTimeMillis();
        String output = null;
        String error = null;
        boolean success = false;

        try {
            validateExecutableSkill(skill);
            validateArguments(skill, arguments == null ? Map.of() : arguments);
            List<String> command = buildCommand(skill, arguments == null ? Map.of() : arguments);
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.directory(resolveScriptsDir(skill).toFile());
            builder.redirectErrorStream(true);

            Process process = builder.start();
            boolean finished = process.waitFor(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                error = "技能执行超时";
            } else {
                output = truncate(new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
                success = process.exitValue() == 0;
                if (!success) {
                    error = StringUtils.hasText(output) ? output : "技能脚本退出码：" + process.exitValue();
                }
            }
        } catch (Exception e) {
            error = StringUtils.hasText(e.getMessage()) ? e.getMessage() : "技能执行失败";
        }

        long duration = System.currentTimeMillis() - start;
        saveLog(skill, conversationId, arguments, success, output, error, duration);
        return SkillTestResponse.builder()
                .success(success)
                .output(success ? output : null)
                .error(success ? null : error)
                .durationMs(duration)
                .build();
    }

    private List<String> buildCommand(AiSkill skill, Map<String, Object> arguments) {
        String entryCommand = skill.getEntryCommand().trim();
        if (entryCommand.contains("..") || entryCommand.contains("/") || entryCommand.contains("\\")) {
            throw new IllegalArgumentException("入口脚本必须是 scripts 目录下的文件名");
        }

        Path scriptPath = resolveScriptsDir(skill).resolve(entryCommand).normalize();
        if (!scriptPath.startsWith(resolveScriptsDir(skill)) || !scriptPath.toFile().isFile()) {
            throw new IllegalArgumentException("入口脚本不存在或不在 scripts 目录下");
        }

        String lower = entryCommand.toLowerCase();
        List<String> command = new ArrayList<>();
        if (lower.endsWith(".py")) {
            command.add("python");
            command.add(scriptPath.toString());
        } else if (lower.endsWith(".bat") || lower.endsWith(".cmd")) {
            command.add("cmd");
            command.add("/c");
            command.add(scriptPath.toString());
        } else if (lower.endsWith(".ps1")) {
            command.add("powershell");
            command.add("-ExecutionPolicy");
            command.add("Bypass");
            command.add("-File");
            command.add(scriptPath.toString());
        } else {
            throw new IllegalArgumentException("仅支持 .py、.bat、.cmd、.ps1 脚本");
        }

        for (Map.Entry<String, Object> entry : arguments.entrySet()) {
            command.add("--" + entry.getKey());
            command.add(entry.getValue() == null ? "" : String.valueOf(entry.getValue()));
        }
        return command;
    }

    private Path resolveScriptsDir(AiSkill skill) {
        return Path.of(skill.getSkillDir()).resolve("scripts").toAbsolutePath().normalize();
    }

    private void validateExecutableSkill(AiSkill skill) {
        if (skill == null || skill.getId() == null) {
            throw new IllegalArgumentException("技能不存在");
        }
        if (!StringUtils.hasText(skill.getSkillDir())) {
            throw new IllegalArgumentException("技能目录为空");
        }
        if (!StringUtils.hasText(skill.getEntryCommand())) {
            throw new IllegalArgumentException("技能未配置入口脚本");
        }
        File scriptsDir = resolveScriptsDir(skill).toFile();
        if (!scriptsDir.isDirectory()) {
            throw new IllegalArgumentException("技能 scripts 目录不存在");
        }
    }

    @SuppressWarnings("unchecked")
    private void validateArguments(AiSkill skill, Map<String, Object> arguments) {
        if (!StringUtils.hasText(skill.getParameterSchema())) {
            return;
        }
        try {
            Map<String, Object> schema = objectMapper.readValue(skill.getParameterSchema(), Map.class);
            Object requiredValue = schema.get("required");
            if (requiredValue instanceof Collection<?> required) {
                for (Object item : required) {
                    String key = String.valueOf(item);
                    if (!arguments.containsKey(key) || arguments.get(key) == null || !StringUtils.hasText(String.valueOf(arguments.get(key)))) {
                        throw new IllegalArgumentException("缺少必填参数：" + key);
                    }
                }
            }

            Object propertiesValue = schema.get("properties");
            if (!(propertiesValue instanceof Map<?, ?> properties)) {
                return;
            }
            for (Map.Entry<?, ?> entry : properties.entrySet()) {
                String key = String.valueOf(entry.getKey());
                Object value = arguments.get(key);
                if (value == null || !(entry.getValue() instanceof Map<?, ?> property)) {
                    continue;
                }
                Object typeValue = property.get("type");
                if (typeValue == null) {
                    continue;
                }
                validateType(key, value, String.valueOf(typeValue));
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("参数 Schema 解析失败：" + e.getMessage());
        }
    }

    private void validateType(String key, Object value, String type) {
        boolean valid = switch (type) {
            case "string" -> value instanceof String;
            case "integer" -> value instanceof Integer || value instanceof Long || value instanceof Short || value instanceof Byte;
            case "number" -> value instanceof Number;
            case "boolean" -> value instanceof Boolean;
            case "object" -> value instanceof Map<?, ?>;
            case "array" -> value instanceof Collection<?> || value.getClass().isArray();
            default -> true;
        };
        if (!valid) {
            throw new IllegalArgumentException("参数类型不匹配：" + key + " 应为 " + type);
        }
    }

    private void saveLog(AiSkill skill,
                         String conversationId,
                         Map<String, Object> arguments,
                         boolean success,
                         String output,
                         String error,
                         long duration) {
        try {
            AiSkillExecutionLog log = new AiSkillExecutionLog()
                    .setConversationId(conversationId)
                    .setSkillId(skill == null ? null : skill.getId())
                    .setToolName(skill == null ? null : "skill_" + skill.getSkillKey())
                    .setArgumentsPayload(objectMapper.writeValueAsString(arguments == null ? Map.of() : arguments))
                    .setStatus(success ? "SUCCESS" : "FAILED")
                    .setOutputSummary(truncate(output))
                    .setErrorMessage(truncate(error))
                    .setDurationMs(duration)
                    .setCreatedAt(LocalDateTime.now());
            executionLogMapper.insert(log);
        } catch (Exception ignored) {
        }
    }

    private String truncate(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        return value.length() > MAX_OUTPUT_LENGTH ? value.substring(0, MAX_OUTPUT_LENGTH) : value;
    }
}
