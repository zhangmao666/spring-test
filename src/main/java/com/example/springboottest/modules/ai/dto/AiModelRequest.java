package com.example.springboottest.modules.ai.dto;

import com.example.springboottest.enums.AIEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: zm
 * @Created: 2025/12/2 上午10:58
 * @Description:
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiModelRequest {

    /**
     * 主键id
     */
    private Long id;

    /**
     * 模型枚举
     */
    private AIEnum aiEnum;

    /**
     * 名称
     */
    private String name;

}