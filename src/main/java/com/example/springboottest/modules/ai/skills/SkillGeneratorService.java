package com.example.springboottest.modules.ai.skills;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Skill 生成器服务
 * <p>
 * 支持通过 API 动态创建、列出、删除自定义 Skill。
 * 生成的 Skill 存放在 {@code agent.skills.custom-dir} 配置的目录中。
 * </p>
 */
@Slf4j
@Service
public class SkillGeneratorService {

    private final AgentProperties agentProperties;

    public SkillGeneratorService(AgentProperties agentProperties) {
        this.agentProperties = agentProperties;
    }

    /**
     * 通过自然语言描述生成 Skill
     *
     * @param request 生成请求
     * @return 生成的 Skill 信息
     */
    public SkillInfo generateSkill(GenerateSkillRequest request) {
        try {
            String baseDir = getCustomSkillsDir();
            String dirName = sanitizeName(request.getName());
            Path skillDir = Paths.get(baseDir, dirName);

            // 检查是否已存在
            if (Files.exists(skillDir)) {
                throw new IllegalArgumentException("Skill '" + dirName + "' 已存在，请使用不同的名称");
            }

            // 创建目录结构
            Files.createDirectories(skillDir);

            // 如果有脚本内容，创建 scripts 子目录
            if (request.getScriptContent() != null && !request.getScriptContent().isBlank()) {
                Path scriptsDir = skillDir.resolve("scripts");
                Files.createDirectories(scriptsDir);

                String scriptName = request.getScriptName() != null
                        ? request.getScriptName()
                        : dirName + ".bat";
                Files.writeString(scriptsDir.resolve(scriptName), request.getScriptContent(), StandardCharsets.UTF_8);
                log.info("📝 已创建脚本: {}", scriptsDir.resolve(scriptName));
            }

            // 生成 SKILL.md
            String skillContent = buildSkillMarkdown(request);
            Path skillMdPath = skillDir.resolve("SKILL.md");
            Files.writeString(skillMdPath, skillContent, StandardCharsets.UTF_8);

            log.info("✅ Skill 生成成功: {} -> {}", request.getName(), skillDir);

            return SkillInfo.builder()
                    .name(dirName)
                    .displayName(request.getName())
                    .description(request.getDescription())
                    .author(request.getAuthor())
                    .path(skillDir.toAbsolutePath().toString())
                    .builtIn(false)
                    .build();

        } catch (IOException e) {
            log.error("生成 Skill 失败", e);
            throw new RuntimeException("生成 Skill 失败: " + e.getMessage(), e);
        }
    }

    /**
     * 列出所有已注册的 Skill（内置 + 自定义）
     */
    public List<SkillInfo> listSkills() {
        List<SkillInfo> skills = new ArrayList<>();

        // 扫描 classpath skills（内置）
        for (String dir : agentProperties.getSkills().getDirectories()) {
            if (dir.startsWith("classpath:")) {
                String path = dir.substring("classpath:".length());
                // classpath 资源在编译后位于 target/classes 下
                try {
                    var resource = getClass().getClassLoader().getResource(path);
                    if (resource != null) {
                        File folder = new File(resource.toURI());
                        if (folder.isDirectory()) {
                            scanSkillDirectory(folder, true, skills);
                        }
                    }
                } catch (Exception e) {
                    log.debug("扫描 classpath skills 失败: {}", dir, e);
                }
            }
        }

        // 扫描自定义目录
        String customDir = agentProperties.getSkills().getCustomDir();
        if (customDir != null && !customDir.isEmpty()) {
            File customFile = new File(customDir);
            if (customFile.exists() && customFile.isDirectory()) {
                scanSkillDirectory(customFile, false, skills);
            }
        }

        return skills;
    }

    /**
     * 删除自定义 Skill（仅允许删除自定义 Skill，不允许删除内置 Skill）
     */
    public boolean deleteSkill(String skillName) {
        String customDir = getCustomSkillsDir();
        Path skillDir = Paths.get(customDir, skillName);

        if (!Files.exists(skillDir)) {
            log.warn("Skill 不存在: {}", skillName);
            return false;
        }

        try {
            // 递归删除目录
            try (Stream<Path> walk = Files.walk(skillDir)) {
                walk.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }
            log.info("🗑️ Skill 已删除: {}", skillName);
            return true;
        } catch (IOException e) {
            log.error("删除 Skill 失败: {}", skillName, e);
            throw new RuntimeException("删除 Skill 失败: " + e.getMessage(), e);
        }
    }

    // ==================== 私有方法 ====================

    private void scanSkillDirectory(File dir, boolean builtIn, List<SkillInfo> result) {
        File[] subDirs = dir.listFiles(File::isDirectory);
        if (subDirs == null) return;

        for (File subDir : subDirs) {
            File skillMd = new File(subDir, "SKILL.md");
            if (skillMd.exists()) {
                try {
                    String content = Files.readString(skillMd.toPath(), StandardCharsets.UTF_8);
                    SkillInfo info = parseSkillMd(content, subDir, builtIn);
                    result.add(info);
                } catch (IOException e) {
                    log.warn("读取 SKILL.md 失败: {}", skillMd, e);
                }
            }
        }
    }

    private SkillInfo parseSkillMd(String content, File dir, boolean builtIn) {
        // 解析 YAML frontmatter
        String name = dir.getName();
        String description = "";

        if (content.startsWith("---")) {
            int endIndex = content.indexOf("---", 3);
            if (endIndex > 0) {
                String frontmatter = content.substring(3, endIndex);
                for (String line : frontmatter.split("\n")) {
                    line = line.trim();
                    if (line.startsWith("name:")) {
                        name = line.substring("name:".length()).trim();
                    } else if (line.startsWith("description:")) {
                        description = line.substring("description:".length()).trim();
                    }
                }
            }
        }

        return SkillInfo.builder()
                .name(dir.getName())
                .displayName(name)
                .description(description)
                .path(dir.getAbsolutePath())
                .builtIn(builtIn)
                .build();
    }

    private String buildSkillMarkdown(GenerateSkillRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("---\n");
        sb.append("name: ").append(sanitizeName(request.getName())).append("\n");
        sb.append("description: ").append(request.getDescription()).append("\n");
        if (request.getAuthor() != null) {
            sb.append("author: ").append(request.getAuthor()).append("\n");
        }
        sb.append("created: ").append(LocalDate.now()).append("\n");
        sb.append("version: 1.0.0\n");
        sb.append("---\n\n");
        sb.append("# ").append(request.getName()).append("\n\n");
        sb.append("## 功能说明\n");
        sb.append(request.getDescription()).append("\n\n");
        sb.append("## 执行步骤\n");
        sb.append(request.getInstructions()).append("\n");

        if (request.getScriptContent() != null && !request.getScriptContent().isBlank()) {
            String scriptName = request.getScriptName() != null
                    ? request.getScriptName()
                    : sanitizeName(request.getName()) + ".bat";
            sb.append("\n## 脚本\n");
            sb.append("- `<skills目录>/").append(sanitizeName(request.getName()))
                    .append("/scripts/").append(scriptName).append("`\n");
        }

        return sb.toString();
    }

    private String getCustomSkillsDir() {
        String customDir = agentProperties.getSkills().getCustomDir();
        if (customDir == null || customDir.isEmpty()) {
            customDir = "./custom-skills";
        }
        File dir = new File(customDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return customDir;
    }

    private String sanitizeName(String name) {
        return name.toLowerCase()
                .replaceAll("[\\s]+", "-")
                .replaceAll("[^a-z0-9\\u4e00-\\u9fa5\\-]", "")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    // ==================== 内部 DTO ====================

    @Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class GenerateSkillRequest {
        private String name;
        private String description;
        private String instructions;
        private String author;
        /** 可选：脚本文件名 */
        private String scriptName;
        /** 可选：脚本内容 */
        private String scriptContent;
    }

    @Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SkillInfo {
        private String name;
        private String displayName;
        private String description;
        private String author;
        private String path;
        private boolean builtIn;
    }
}
