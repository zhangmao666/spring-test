package com.example.springboottest.modules.ai.skill.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.springboottest.modules.ai.skill.dto.AiSkillQueryRequest;
import com.example.springboottest.modules.ai.skill.dto.AiSkillRequest;
import com.example.springboottest.modules.ai.skill.dto.AiSkillResponse;
import com.example.springboottest.modules.ai.skill.dto.SkillTestResponse;
import com.example.springboottest.modules.ai.skill.entity.AiConversationSkill;
import com.example.springboottest.modules.ai.skill.entity.AiSkill;
import com.example.springboottest.modules.ai.skill.mapper.AiConversationSkillMapper;
import com.example.springboottest.modules.ai.skill.mapper.AiSkillMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiSkillServiceImpl implements AiSkillService {

    private static final int MAX_MOUNTED_SKILLS = 50;
    private static final long MAX_IMPORT_ZIP_SIZE = 20L * 1024 * 1024;
    private static final Set<String> PROMPT_TYPES = Set.of("PROMPT", "MIXED");

    private final AiSkillMapper skillMapper;
    private final AiConversationSkillMapper conversationSkillMapper;
    private final SkillScriptExecutor skillScriptExecutor;

    @Override
    public List<AiSkillResponse> listSkills(AiSkillQueryRequest query) {
        LambdaQueryWrapper<AiSkill> wrapper = new LambdaQueryWrapper<AiSkill>()
                .eq(AiSkill::getDeleted, false)
                .orderByDesc(AiSkill::getEnabled)
                .orderByAsc(AiSkill::getName);
        if (query != null) {
            if (StringUtils.hasText(query.getKeyword())) {
                String keyword = query.getKeyword().trim();
                wrapper.and(item -> item.like(AiSkill::getName, keyword)
                        .or()
                        .like(AiSkill::getSkillKey, keyword)
                        .or()
                        .like(AiSkill::getDescription, keyword));
            }
            eqIfPresent(wrapper, AiSkill::getSourceType, query.getSourceType());
            eqIfPresent(wrapper, AiSkill::getOriginType, query.getOriginType());
            eqIfPresent(wrapper, AiSkill::getSkillType, query.getSkillType());
            eqIfPresent(wrapper, AiSkill::getCategory, query.getCategory());
            eqIfPresent(wrapper, AiSkill::getScenario, query.getScenario());
            if (Boolean.TRUE.equals(query.getEnabledOnly())) {
                wrapper.eq(AiSkill::getEnabled, true);
            }
        }
        return skillMapper.selectList(wrapper).stream().map(this::toResponse).toList();
    }

    @Override
    public AiSkillResponse getSkill(Long id) {
        return toResponse(requireSkill(id));
    }

    @Override
    @Transactional
    public Long createSkill(AiSkillRequest request) {
        validateRequest(request);
        if (findByKey(request.getSkillKey()) != null) {
            throw new IllegalArgumentException("技能标识已存在");
        }
        AiSkill skill = new AiSkill()
                .setSkillKey(normalizeKey(request.getSkillKey()))
                .setName(request.getName().trim())
                .setDescription(trimToNull(request.getDescription()))
                .setSourceType("DB")
                .setOriginType(defaultIfBlank(request.getOriginType(), "MY"))
                .setSkillType(defaultIfBlank(request.getSkillType(), "PROMPT"))
                .setCategory(trimToNull(request.getCategory()))
                .setScenario(trimToNull(request.getScenario()))
                .setTags(trimToNull(request.getTags()))
                .setContent(trimToNull(request.getContent()))
                .setEntryCommand(trimToNull(request.getEntryCommand()))
                .setParameterSchema(trimToNull(request.getParameterSchema()))
                .setEnabled(!Boolean.FALSE.equals(request.getEnabled()))
                .setReadonly(false)
                .setDeleted(false)
                .setCreateTime(LocalDateTime.now())
                .setUpdateTime(LocalDateTime.now());
        skillMapper.insert(skill);
        return skill.getId();
    }

    @Override
    @Transactional
    public void updateSkill(Long id, AiSkillRequest request) {
        validateRequest(request);
        AiSkill skill = requireSkill(id);
        if (Boolean.TRUE.equals(skill.getReadonly())) {
            skill.setName(request.getName().trim());
            skill.setDescription(trimToNull(request.getDescription()));
            skill.setOriginType(defaultIfBlank(request.getOriginType(), skill.getOriginType()));
            skill.setSkillType(defaultIfBlank(request.getSkillType(), skill.getSkillType()));
            skill.setCategory(trimToNull(request.getCategory()));
            skill.setScenario(trimToNull(request.getScenario()));
            skill.setTags(trimToNull(request.getTags()));
            skill.setEnabled(!Boolean.FALSE.equals(request.getEnabled()));
        } else {
            AiSkill existing = findByKey(request.getSkillKey());
            if (existing != null && !Objects.equals(existing.getId(), id)) {
                throw new IllegalArgumentException("技能标识已存在");
            }
            skill.setSkillKey(normalizeKey(request.getSkillKey()));
            skill.setName(request.getName().trim());
            skill.setDescription(trimToNull(request.getDescription()));
            skill.setOriginType(defaultIfBlank(request.getOriginType(), "MY"));
            skill.setSkillType(defaultIfBlank(request.getSkillType(), "PROMPT"));
            skill.setCategory(trimToNull(request.getCategory()));
            skill.setScenario(trimToNull(request.getScenario()));
            skill.setTags(trimToNull(request.getTags()));
            skill.setContent(trimToNull(request.getContent()));
            skill.setEntryCommand(trimToNull(request.getEntryCommand()));
            skill.setParameterSchema(trimToNull(request.getParameterSchema()));
            skill.setEnabled(!Boolean.FALSE.equals(request.getEnabled()));
        }
        skill.setUpdateTime(LocalDateTime.now());
        skillMapper.updateById(skill);
    }

    @Override
    public void updateStatus(Long id, boolean enabled) {
        AiSkill skill = requireSkill(id);
        skill.setEnabled(enabled);
        skill.setUpdateTime(LocalDateTime.now());
        skillMapper.updateById(skill);
    }

    @Override
    @Transactional
    public void deleteSkill(Long id) {
        AiSkill skill = requireSkill(id);
        if (Boolean.TRUE.equals(skill.getReadonly()) || "LOCAL".equalsIgnoreCase(skill.getSourceType())) {
            throw new IllegalArgumentException("本地技能不可删除");
        }
        skill.setDeleted(true);
        skill.setUpdateTime(LocalDateTime.now());
        skillMapper.updateById(skill);
    }

    @Override
    @Transactional
    public int syncLocalSkills() {
        int count = 0;
        count += syncLocalRoot(Path.of("src", "main", "resources", "skills"), "OFFICIAL");
        count += syncLocalRoot(customSkillRoot(), "COMMUNITY");
        return count;
    }

    @Override
    @Transactional
    public AiSkillResponse importSkillZip(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请上传技能 zip 文件");
        }
        String originalFilename = defaultIfBlank(file.getOriginalFilename(), "");
        if (!originalFilename.toLowerCase().endsWith(".zip")) {
            throw new IllegalArgumentException("仅支持 zip 格式的技能包");
        }
        if (file.getSize() > MAX_IMPORT_ZIP_SIZE) {
            throw new IllegalArgumentException("技能包不能超过 20MB");
        }

        Path tempRoot = null;
        try {
            tempRoot = Files.createTempDirectory("ai-skill-import-");
            unzipSkillPackage(file.getInputStream(), tempRoot);
            Path skillFile = findSkillFile(tempRoot);
            String raw = Files.readString(skillFile, StandardCharsets.UTF_8);
            FrontMatter frontMatter = parseFrontMatter(raw);
            String fallbackName = stripZipExtension(originalFilename);
            String name = defaultIfBlank(frontMatter.values().get("name"), fallbackName);
            String key = normalizeKey(defaultIfBlank(name, fallbackName));
            if (!StringUtils.hasText(key)) {
                throw new IllegalArgumentException("技能包缺少有效的 name");
            }

            Path skillSourceDir = skillFile.getParent();
            Path targetDir = uniqueSkillDir(customSkillRoot(), key);
            Files.createDirectories(targetDir.getParent());
            copyDirectory(skillSourceDir, targetDir);

            AiSkill skill = syncSkillFile(targetDir, targetDir.resolve("SKILL.md"), "MY");
            return toResponse(skill);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Failed to import skill zip {}", originalFilename, e);
            throw new IllegalArgumentException("技能包导入失败: " + e.getMessage(), e);
        } finally {
            deleteQuietly(tempRoot);
        }
    }

    @Override
    public List<AiSkillResponse> getConversationSkills(String conversationId) {
        return getMountedEnabledSkills(conversationId).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public void saveConversationSkills(String conversationId, List<Long> skillIds) {
        if (!StringUtils.hasText(conversationId)) {
            throw new IllegalArgumentException("conversationId 不能为空");
        }
        List<Long> normalized = skillIds == null ? List.of() : skillIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .limit(MAX_MOUNTED_SKILLS + 1L)
                .toList();
        if (normalized.size() > MAX_MOUNTED_SKILLS) {
            throw new IllegalArgumentException("最多挂载 50 个技能");
        }

        conversationSkillMapper.delete(new LambdaQueryWrapper<AiConversationSkill>()
                .eq(AiConversationSkill::getConversationId, conversationId));
        for (int i = 0; i < normalized.size(); i++) {
            AiSkill skill = requireSkill(normalized.get(i));
            if (!Boolean.TRUE.equals(skill.getEnabled())) {
                continue;
            }
            conversationSkillMapper.insert(new AiConversationSkill()
                    .setConversationId(conversationId)
                    .setSkillId(skill.getId())
                    .setSortOrder(i)
                    .setCreatedAt(LocalDateTime.now()));
        }
    }

    @Override
    public List<AiSkill> getMountedEnabledSkills(String conversationId) {
        if (!StringUtils.hasText(conversationId)) {
            return List.of();
        }
        List<AiConversationSkill> mounted = conversationSkillMapper.selectList(new LambdaQueryWrapper<AiConversationSkill>()
                .eq(AiConversationSkill::getConversationId, conversationId)
                .orderByAsc(AiConversationSkill::getSortOrder));
        if (mounted.isEmpty()) {
            return List.of();
        }
        List<Long> ids = mounted.stream().map(AiConversationSkill::getSkillId).toList();
        Map<Long, AiSkill> skills = skillMapper.selectList(new LambdaQueryWrapper<AiSkill>()
                        .in(AiSkill::getId, ids)
                        .eq(AiSkill::getEnabled, true)
                        .eq(AiSkill::getDeleted, false))
                .stream()
                .collect(Collectors.toMap(AiSkill::getId, item -> item));
        return ids.stream().map(skills::get).filter(Objects::nonNull).toList();
    }

    @Override
    public SkillTestResponse testSkill(Long id, String conversationId, Map<String, Object> arguments) {
        return skillScriptExecutor.execute(requireSkill(id), conversationId, arguments);
    }

    @Override
    public String buildMountedSkillPrompt(String conversationId) {
        List<AiSkill> skills = getMountedEnabledSkills(conversationId).stream()
                .filter(item -> PROMPT_TYPES.contains(defaultIfBlank(item.getSkillType(), "PROMPT").toUpperCase()))
                .toList();
        if (skills.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("已挂载技能：\n");
        for (AiSkill skill : skills) {
            builder.append("\n---\n");
            builder.append("技能名称：").append(skill.getName()).append("\n");
            builder.append("技能描述：").append(defaultIfBlank(skill.getDescription(), "无")).append("\n");
            builder.append("技能内容：\n").append(defaultIfBlank(skill.getContent(), "")).append("\n");
        }
        return builder.toString().trim();
    }

    private int syncLocalRoot(Path root, String originType) {
        if (!Files.isDirectory(root)) {
            return 0;
        }
        int count = 0;
        try (var paths = Files.list(root)) {
            for (Path skillDir : paths.filter(Files::isDirectory).toList()) {
                Path skillFile = skillDir.resolve("SKILL.md");
                if (Files.isRegularFile(skillFile)) {
                    try {
                        syncSkillFile(skillDir, skillFile, originType);
                        count++;
                    } catch (Exception e) {
                        log.warn("Failed to sync skill file {}", skillFile, e);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to sync local skills from {}", root, e);
        }
        return count;
    }

    private AiSkill syncSkillFile(Path skillDir, Path skillFile, String originType) {
        try {
            String raw = Files.readString(skillFile, StandardCharsets.UTF_8);
            FrontMatter frontMatter = parseFrontMatter(raw);
            String key = normalizeKey(defaultIfBlank(frontMatter.values().get("name"), skillDir.getFileName().toString()));
            AiSkill skill = findByKey(key);
            if (skill == null) {
                skill = new AiSkill()
                        .setSkillKey(key)
                        .setCreateTime(LocalDateTime.now())
                        .setEnabled(true)
                        .setDeleted(false);
            }
            skill.setName(defaultIfBlank(frontMatter.values().get("name"), key));
            skill.setDescription(trimToNull(frontMatter.values().get("description")));
            skill.setSourceType("LOCAL");
            skill.setOriginType(originType);
            skill.setSkillType(hasScripts(skillDir) ? "MIXED" : "PROMPT");
            skill.setCategory(defaultIfBlank(skill.getCategory(), "开发能力"));
            skill.setScenario(defaultIfBlank(skill.getScenario(), "网页开发"));
            skill.setContent(frontMatter.body());
            skill.setSkillDir(skillDir.toAbsolutePath().normalize().toString());
            skill.setReadonly(true);
            skill.setUpdateTime(LocalDateTime.now());
            if (skill.getId() == null) {
                skillMapper.insert(skill);
            } else {
                skillMapper.updateById(skill);
            }
            return skill;
        } catch (Exception e) {
            log.warn("Failed to sync skill file {}", skillFile, e);
            throw new IllegalArgumentException("同步技能文件失败: " + skillFile.getFileName(), e);
        }
    }

    private void unzipSkillPackage(InputStream inputStream, Path tempRoot) throws Exception {
        try (ZipInputStream zipInputStream = new ZipInputStream(inputStream, StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                Path target = tempRoot.resolve(entry.getName()).normalize();
                if (!target.startsWith(tempRoot)) {
                    throw new IllegalArgumentException("技能包包含非法路径: " + entry.getName());
                }
                Files.createDirectories(target.getParent());
                Files.copy(zipInputStream, target, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private Path findSkillFile(Path root) throws Exception {
        try (var paths = Files.walk(root)) {
            return paths.filter(Files::isRegularFile)
                    .filter(path -> "SKILL.md".equalsIgnoreCase(path.getFileName().toString()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("技能包中未找到 SKILL.md"));
        }
    }

    private Path uniqueSkillDir(Path root, String key) {
        Path target = root.resolve(key).normalize();
        if (!Files.exists(target)) {
            return target;
        }
        return root.resolve(key + "-" + System.currentTimeMillis()).normalize();
    }

    private void copyDirectory(Path source, Path target) throws Exception {
        try (var paths = Files.walk(source)) {
            for (Path path : paths.toList()) {
                Path relative = source.relativize(path);
                Path nextTarget = target.resolve(relative).normalize();
                if (!nextTarget.startsWith(target)) {
                    throw new IllegalArgumentException("技能包包含非法路径");
                }
                if (Files.isDirectory(path)) {
                    Files.createDirectories(nextTarget);
                } else {
                    Files.createDirectories(nextTarget.getParent());
                    Files.copy(path, nextTarget, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    private void deleteQuietly(Path root) {
        if (root == null || !Files.exists(root)) {
            return;
        }
        try (var paths = Files.walk(root)) {
            for (Path path : paths.sorted((left, right) -> right.getNameCount() - left.getNameCount()).toList()) {
                Files.deleteIfExists(path);
            }
        } catch (Exception e) {
            log.debug("Failed to delete temp skill import directory {}", root, e);
        }
    }

    private String stripZipExtension(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "imported-skill";
        }
        String trimmed = filename.trim();
        return trimmed.toLowerCase().endsWith(".zip") ? trimmed.substring(0, trimmed.length() - 4) : trimmed;
    }

    Path customSkillRoot() {
        return Path.of("custom-skills");
    }

    private boolean hasScripts(Path skillDir) {
        Path scripts = skillDir.resolve("scripts");
        return Files.isDirectory(scripts);
    }

    private FrontMatter parseFrontMatter(String raw) {
        if (!StringUtils.hasText(raw) || !raw.startsWith("---")) {
            return new FrontMatter(Map.of(), raw == null ? "" : raw);
        }
        String[] parts = raw.split("---", 3);
        if (parts.length < 3) {
            return new FrontMatter(Map.of(), raw);
        }
        Map<String, String> values = new LinkedHashMap<>();
        for (String line : parts[1].split("\\r?\\n")) {
            int index = line.indexOf(':');
            if (index > 0) {
                values.put(line.substring(0, index).trim(), stripQuotes(line.substring(index + 1).trim()));
            }
        }
        return new FrontMatter(values, parts[2].trim());
    }

    private String stripQuotes(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if ((trimmed.startsWith("\"") && trimmed.endsWith("\"")) || (trimmed.startsWith("'") && trimmed.endsWith("'"))) {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }

    private AiSkillResponse toResponse(AiSkill skill) {
        if (skill == null) {
            return null;
        }
        return AiSkillResponse.builder()
                .id(skill.getId())
                .skillKey(skill.getSkillKey())
                .name(skill.getName())
                .description(skill.getDescription())
                .sourceType(skill.getSourceType())
                .originType(skill.getOriginType())
                .skillType(skill.getSkillType())
                .category(skill.getCategory())
                .scenario(skill.getScenario())
                .tags(skill.getTags())
                .content(skill.getContent())
                .skillDir(skill.getSkillDir())
                .entryCommand(skill.getEntryCommand())
                .parameterSchema(skill.getParameterSchema())
                .enabled(skill.getEnabled())
                .readonly(skill.getReadonly())
                .scripts(listScripts(skill))
                .createTime(skill.getCreateTime())
                .updateTime(skill.getUpdateTime())
                .build();
    }

    private List<String> listScripts(AiSkill skill) {
        if (skill == null || !StringUtils.hasText(skill.getSkillDir())) {
            return List.of();
        }
        Path scripts = Path.of(skill.getSkillDir()).resolve("scripts");
        if (!Files.isDirectory(scripts)) {
            return List.of();
        }
        try (var paths = Files.list(scripts)) {
            return paths.filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .sorted()
                    .toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    private AiSkill requireSkill(Long id) {
        AiSkill skill = id == null ? null : skillMapper.selectById(id);
        if (skill == null || Boolean.TRUE.equals(skill.getDeleted())) {
            throw new IllegalArgumentException("技能不存在: " + id);
        }
        return skill;
    }

    private AiSkill findByKey(String key) {
        if (!StringUtils.hasText(key)) {
            return null;
        }
        return skillMapper.selectOne(new LambdaQueryWrapper<AiSkill>()
                .eq(AiSkill::getSkillKey, normalizeKey(key))
                .last("LIMIT 1"));
    }

    private void validateRequest(AiSkillRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("请求不能为空");
        }
        if (!StringUtils.hasText(request.getSkillKey()) || !StringUtils.hasText(request.getName())) {
            throw new IllegalArgumentException("技能标识和名称不能为空");
        }
    }

    private <T> void eqIfPresent(LambdaQueryWrapper<AiSkill> wrapper,
                                 com.baomidou.mybatisplus.core.toolkit.support.SFunction<AiSkill, T> column,
                                 String value) {
        if (StringUtils.hasText(value)) {
            wrapper.eq(column, value.trim());
        }
    }

    private String normalizeKey(String value) {
        return value == null ? "" : value.trim().replaceAll("[^A-Za-z0-9_-]", "-").toLowerCase();
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String defaultIfBlank(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }

    private record FrontMatter(Map<String, String> values, String body) {
    }
}
