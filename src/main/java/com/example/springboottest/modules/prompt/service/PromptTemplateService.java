package com.example.springboottest.modules.prompt.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboottest.modules.prompt.dto.PromptTemplateQueryRequest;
import com.example.springboottest.modules.prompt.dto.PromptTemplateRequest;
import com.example.springboottest.modules.prompt.dto.PromptTemplateResponse;
import com.example.springboottest.modules.prompt.entity.PromptTemplate;
import com.example.springboottest.modules.prompt.repository.PromptTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PromptTemplateService {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{\\s*([a-zA-Z0-9_]+)\\s*}}");

    private final PromptTemplateRepository promptTemplateRepository;

    public Long createPromptTemplate(PromptTemplateRequest request) {
        if (promptTemplateRepository.existsByPromptCode(request.getPromptCode())) {
            throw new RuntimeException("Prompt code already exists: " + request.getPromptCode());
        }

        PromptTemplate template = new PromptTemplate();
        BeanUtils.copyProperties(request, template);
        if (template.getStatus() == null) {
            template.setStatus(1);
        }
        template.setCreateTime(LocalDateTime.now());
        template.setUpdateTime(LocalDateTime.now());
        promptTemplateRepository.insert(template);
        return template.getId();
    }

    @Cacheable(value = "promptTemplateCache", key = "#id")
    @Transactional(readOnly = true)
    public PromptTemplateResponse getPromptTemplateById(Long id) {
        PromptTemplate template = promptTemplateRepository.selectById(id);
        if (template == null) {
            throw new RuntimeException("Prompt template not found: " + id);
        }
        return new PromptTemplateResponse(template);
    }

    @Cacheable(value = "promptTemplateCache", key = "'code_' + #promptCode")
    @Transactional(readOnly = true)
    public PromptTemplateResponse getPromptTemplateByCode(String promptCode) {
        PromptTemplate template = promptTemplateRepository.selectByPromptCode(promptCode);
        if (template == null) {
            throw new RuntimeException("Prompt template not found: " + promptCode);
        }
        return new PromptTemplateResponse(template);
    }

    @Cacheable(value = "promptContentCache", key = "#promptCode", unless = "#result == null")
    @Transactional(readOnly = true)
    public String getActivePromptContent(String promptCode) {
        PromptTemplate template = promptTemplateRepository.selectActiveByPromptCode(promptCode);
        return template == null ? null : template.getPromptContent();
    }

    @CacheEvict(value = {"promptTemplateCache", "promptContentCache"}, allEntries = true)
    public PromptTemplateResponse updatePromptTemplate(Long id, PromptTemplateRequest request) {
        PromptTemplate existing = promptTemplateRepository.selectById(id);
        if (existing == null) {
            throw new RuntimeException("Prompt template not found: " + id);
        }

        if (!existing.getPromptCode().equals(request.getPromptCode())
                && promptTemplateRepository.existsByPromptCode(request.getPromptCode())) {
            throw new RuntimeException("Prompt code already exists: " + request.getPromptCode());
        }

        LocalDateTime originalCreateTime = existing.getCreateTime();
        Long originalCreateBy = existing.getCreateBy();
        BeanUtils.copyProperties(request, existing);
        existing.setId(id);
        existing.setCreateTime(originalCreateTime);
        existing.setCreateBy(originalCreateBy);
        existing.setUpdateTime(LocalDateTime.now());
        promptTemplateRepository.updateById(existing);
        return new PromptTemplateResponse(existing);
    }

    @CacheEvict(value = {"promptTemplateCache", "promptContentCache"}, allEntries = true)
    public void deletePromptTemplate(Long id) {
        PromptTemplate template = promptTemplateRepository.selectById(id);
        if (template == null) {
            throw new RuntimeException("Prompt template not found: " + id);
        }
        promptTemplateRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public IPage<PromptTemplateResponse> queryPromptTemplates(PromptTemplateQueryRequest request) {
        Page<PromptTemplate> page = new Page<>(request.getPage() + 1L, request.getSize());
        IPage<PromptTemplate> result = promptTemplateRepository.findByConditions(
                page,
                request.getPromptCode(),
                request.getPromptName(),
                request.getPromptType(),
                request.getStatus()
        );
        return result.convert(PromptTemplateResponse::new);
    }

    @Transactional(readOnly = true)
    public List<PromptTemplateResponse> getAllActivePromptTemplates() {
        return promptTemplateRepository.findAllActive().stream()
                .map(PromptTemplateResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public String renderPrompt(String promptCode, Map<String, Object> variables) {
        String template = getActivePromptContent(promptCode);
        if (!StringUtils.hasText(template)) {
            throw new RuntimeException("Active prompt template not found: " + promptCode);
        }
        return renderContent(template, variables);
    }

    @Transactional(readOnly = true)
    public String renderPromptWithFallback(String promptCode, String fallbackTemplate, Map<String, Object> variables) {
        String template;
        try {
            template = getActivePromptContent(promptCode);
        } catch (Exception e) {
            log.warn("Load prompt template failed, using fallback. promptCode={}", promptCode, e);
            template = null;
        }
        if (!StringUtils.hasText(template)) {
            template = fallbackTemplate;
        }
        return renderContent(template, variables);
    }

    public String renderContent(String template, Map<String, Object> variables) {
        if (!StringUtils.hasText(template)) {
            return "";
        }

        Map<String, Object> safeVariables = variables == null ? Collections.emptyMap() : variables;
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            Object value = safeVariables.get(key);
            String replacement = value == null ? "" : String.valueOf(value);
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }
}
