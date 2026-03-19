package com.example.springboottest.modules.ai.setvice;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.springboottest.config.AiProperties;
import com.example.springboottest.entity.DTO.AiChatResponse;
import com.example.springboottest.modules.ai.dto.AiModelRequest;
import com.example.springboottest.modules.ai.dto.AiModelResponse;
import com.example.springboottest.modules.ai.dto.AiModelTestRequest;
import com.example.springboottest.modules.ai.dto.AiModelTestResponse;
import com.example.springboottest.modules.ai.entity.AiModel;
import com.example.springboottest.modules.ai.repository.AiModelRepository;
import com.example.springboottest.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AiModelService {

    private final AiModelRepository aiModelRepository;
    private final AiProperties aiProperties;

    @Transactional(readOnly = true)
    public List<AiModelResponse> listModels() {
        return aiModelRepository.selectList(new LambdaQueryWrapper<AiModel>()
                        .orderByDesc(AiModel::getIsDefault)
                        .orderByDesc(AiModel::getEnabled)
                        .orderByDesc(AiModel::getUpdateTime))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AiModelResponse> listEnabledModels() {
        return aiModelRepository.selectList(new LambdaQueryWrapper<AiModel>()
                        .eq(AiModel::getEnabled, true)
                        .orderByDesc(AiModel::getIsDefault)
                        .orderByAsc(AiModel::getDisplayName))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AiModel findById(Long id) {
        return id == null ? null : aiModelRepository.selectById(id);
    }

    @Transactional(readOnly = true)
    public AiModel findDefaultModel() {
        return aiModelRepository.selectOne(new LambdaQueryWrapper<AiModel>()
                .eq(AiModel::getEnabled, true)
                .eq(AiModel::getIsDefault, true)
                .last("LIMIT 1"));
    }

    public Long createAiModel(AiModelRequest request) {
        validateUpsertRequest(request, false);
        AiModel aiModel = new AiModel();
        applyRequest(aiModel, request, false);
        Long currentUserId = SecurityUtils.getCurrentUserId();
        aiModel.setCreateBy(currentUserId);
        aiModel.setUpdateBy(currentUserId);
        aiModel.setCreateTime(LocalDateTime.now());
        aiModel.setUpdateTime(LocalDateTime.now());
        aiModelRepository.insert(aiModel);
        if (Boolean.TRUE.equals(aiModel.getIsDefault())) {
            markAsDefault(aiModel.getId());
        }
        return aiModel.getId();
    }

    public void updateAiModel(Long id, AiModelRequest request) {
        AiModel existing = requireModel(id);
        validateUpsertRequest(request, true);
        applyRequest(existing, request, true);
        existing.setUpdateBy(SecurityUtils.getCurrentUserId());
        existing.setUpdateTime(LocalDateTime.now());
        aiModelRepository.updateById(existing);
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            markAsDefault(id);
        } else if (Boolean.FALSE.equals(request.getEnabled()) && Boolean.TRUE.equals(existing.getIsDefault())) {
            existing.setIsDefault(false);
            aiModelRepository.updateById(existing);
        }
    }

    public void markAsDefault(Long id) {
        AiModel target = requireModel(id);
        if (!Boolean.TRUE.equals(target.getEnabled())) {
            throw new IllegalArgumentException("默认模型必须处于启用状态");
        }
        aiModelRepository.update(null, new LambdaUpdateWrapper<AiModel>()
                .set(AiModel::getIsDefault, false));
        target.setIsDefault(true);
        target.setUpdateBy(SecurityUtils.getCurrentUserId());
        target.setUpdateTime(LocalDateTime.now());
        aiModelRepository.updateById(target);
    }

    public void updateStatus(Long id, Boolean enabled) {
        AiModel target = requireModel(id);
        if (Boolean.FALSE.equals(enabled) && Boolean.TRUE.equals(target.getIsDefault())) {
            target.setIsDefault(false);
        }
        target.setEnabled(Boolean.TRUE.equals(enabled));
        target.setUpdateBy(SecurityUtils.getCurrentUserId());
        target.setUpdateTime(LocalDateTime.now());
        aiModelRepository.updateById(target);
    }

    public void deleteModel(Long id) {
        AiModel target = requireModel(id);
        if (Boolean.TRUE.equals(target.getIsDefault())) {
            throw new IllegalArgumentException("默认模型不能直接删除，请先切换默认模型");
        }
        aiModelRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public AiModelTestResponse testConnection(AiModelTestRequest request) {
        long startTime = System.currentTimeMillis();
        String apiKey = resolveApiKeyForTest(request);
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalArgumentException("API Key不能为空");
        }

        try {
            OpenAiChatModel chatModel = createChatModel(request.getBaseUrl(), apiKey, request.getModelName(), 0.1, 32);
            String content = chatModel.call(new Prompt(new UserMessage("Reply with OK only.")))
                    .getResult()
                    .getOutput()
                    .getText();
            return AiModelTestResponse.builder()
                    .success(true)
                    .message(StringUtils.hasText(content) ? content.trim() : "OK")
                    .responseTime(System.currentTimeMillis() - startTime)
                    .build();
        } catch (Exception ex) {
            return AiModelTestResponse.builder()
                    .success(false)
                    .message("连接测试失败: " + ex.getMessage())
                    .responseTime(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    public String resolveFallbackModelName(Boolean useDeepThinking, Boolean useWebSearch) {
        if (Boolean.TRUE.equals(useDeepThinking) && StringUtils.hasText(aiProperties.getOpenai().getThinkingModel())) {
            return aiProperties.getOpenai().getThinkingModel();
        }
        if (Boolean.TRUE.equals(useWebSearch) && StringUtils.hasText(aiProperties.getOpenai().getSearchModel())) {
            return aiProperties.getOpenai().getSearchModel();
        }
        return aiProperties.getOpenai().getModel();
    }

    public OpenAiChatModel createChatModel(String baseUrl, String apiKey, String modelName, Double temperature, Integer maxTokens) {
        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .build();

        OpenAiChatOptions.Builder optionsBuilder = OpenAiChatOptions.builder()
                .model(modelName);

        if (temperature != null) {
            optionsBuilder.temperature(temperature);
        }
        if (maxTokens != null) {
            optionsBuilder.maxTokens(maxTokens);
        }

        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(optionsBuilder.build())
                .build();
    }

    @Transactional(readOnly = true)
    public AiChatResponse buildFallbackErrorResponse(long startTime, String message) {
        return AiChatResponse.builder()
                .success(false)
                .error(message)
                .provider("openai")
                .responseTime(System.currentTimeMillis() - startTime)
                .build();
    }

    private void validateUpsertRequest(AiModelRequest request, boolean update) {
        if (request == null) {
            throw new IllegalArgumentException("请求不能为空");
        }
        if (Boolean.TRUE.equals(request.getIsDefault()) && Boolean.FALSE.equals(request.getEnabled())) {
            throw new IllegalArgumentException("默认模型必须处于启用状态");
        }
        if (!update && !StringUtils.hasText(request.getApiKey())) {
            throw new IllegalArgumentException("新建模型时API Key不能为空");
        }
    }

    private void applyRequest(AiModel target, AiModelRequest request, boolean keepApiKeyWhenBlank) {
        target.setProvider(trimToNull(request.getProvider()));
        target.setDisplayName(trimToNull(request.getDisplayName()));
        target.setBaseUrl(trimToNull(request.getBaseUrl()));
        target.setModelName(trimToNull(request.getModelName()));
        target.setEnabled(Boolean.TRUE.equals(request.getEnabled()));
        target.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));
        target.setSupportsDeepThinking(Boolean.TRUE.equals(request.getSupportsDeepThinking()));
        target.setSupportsWebSearch(Boolean.TRUE.equals(request.getSupportsWebSearch()));
        target.setRemark(trimToNull(request.getRemark()));

        if (keepApiKeyWhenBlank && !StringUtils.hasText(request.getApiKey())) {
            return;
        }
        target.setApiKey(trimToNull(request.getApiKey()));
    }

    private AiModel requireModel(Long id) {
        AiModel model = findById(id);
        if (model == null) {
            throw new IllegalArgumentException("模型不存在: " + id);
        }
        return model;
    }

    private String resolveApiKeyForTest(AiModelTestRequest request) {
        if (StringUtils.hasText(request.getApiKey())) {
            return request.getApiKey().trim();
        }
        if (request.getId() == null) {
            return null;
        }
        AiModel existing = findById(request.getId());
        return existing == null ? null : existing.getApiKey();
    }

    private AiModelResponse toResponse(AiModel entity) {
        return AiModelResponse.builder()
                .id(entity.getId())
                .provider(entity.getProvider())
                .displayName(entity.getDisplayName())
                .baseUrl(entity.getBaseUrl())
                .modelName(entity.getModelName())
                .enabled(Boolean.TRUE.equals(entity.getEnabled()))
                .isDefault(Boolean.TRUE.equals(entity.getIsDefault()))
                .supportsDeepThinking(Boolean.TRUE.equals(entity.getSupportsDeepThinking()))
                .supportsWebSearch(Boolean.TRUE.equals(entity.getSupportsWebSearch()))
                .remark(entity.getRemark())
                .maskedApiKey(maskApiKey(entity.getApiKey()))
                .apiKeyConfigured(StringUtils.hasText(entity.getApiKey()))
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }

    private String maskApiKey(String apiKey) {
        if (!StringUtils.hasText(apiKey)) {
            return "";
        }
        if (apiKey.length() <= 8) {
            return "****";
        }
        return apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4);
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
