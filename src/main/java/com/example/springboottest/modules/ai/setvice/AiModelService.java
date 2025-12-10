package com.example.springboottest.modules.ai.setvice;

import com.example.springboottest.modules.ai.dto.AiModelRequest;
import com.example.springboottest.modules.ai.entity.AiModel;
import com.example.springboottest.modules.ai.repository.AiModelRepository;
import com.example.springboottest.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * @Author: zm
 * @Created: 2025/12/2 上午10:57
 * @Description:
 */
@Service
@Transactional
@RequiredArgsConstructor
public class AiModelService {

    private final AiModelRepository aiModelRepository;

    /**
     * 新增AI模型
     */
    public long createAiModel(AiModelRequest request) {

        AiModel aiModel = new AiModel();
        aiModel.setAiEnum(request.getAiEnum());
        aiModel.setModelName(request.getName());
        aiModel.setCreateBy(SecurityUtils.getCurrentUserId());
        aiModel.setCreateTime(LocalDateTime.now());
        aiModel.setUpdateBy(SecurityUtils.getCurrentUserId());
        aiModel.setUpdateTime(LocalDateTime.now());
        int insert = aiModelRepository.insert(aiModel);

        return insert > 0 ? aiModel.getId() : null;
    }

}