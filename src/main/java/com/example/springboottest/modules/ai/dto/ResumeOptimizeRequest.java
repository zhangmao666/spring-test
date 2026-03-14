package com.example.springboottest.modules.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 简历优化请求
 */
@Data
public class ResumeOptimizeRequest {

    /** 原始简历内容（纯文本） */
    @NotBlank(message = "简历内容不能为空")
    @Size(max = 6000, message = "简历内容不能超过6000字")
    private String resumeContent;

    /** 目标岗位 */
    @NotBlank(message = "目标岗位不能为空")
    @Size(max = 100, message = "目标岗位不能超过100字")
    private String targetPosition;

    /** 目标行业，如：互联网、金融、教育 */
    @Size(max = 100, message = "目标行业不能超过100字")
    private String targetIndustry;

    /** 优化方向（可多选），如：表达优化、结构调整、量化成果、突出亮点 */
    @Size(max = 200, message = "优化方向描述不能超过200字")
    private String optimizeDirection;

    /** 附加要求 */
    @Size(max = 500, message = "附加要求不能超过500字")
    private String additionalRequirements;

    /** 是否开启深度思考 */
    private Boolean useDeepThinking = false;

    /** 会话ID */
    @Size(max = 64)
    private String conversationId;
}
