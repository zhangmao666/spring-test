package com.example.springboottest.modules.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * AI 简历生成请求
 */
@Data
public class ResumeGenerateRequest {

    /** 姓名 */
    @NotBlank(message = "姓名不能为空")
    @Size(max = 30, message = "姓名不能超过30字")
    private String name;

    /** 目标岗位 */
    @NotBlank(message = "目标岗位不能为空")
    @Size(max = 100, message = "目标岗位不能超过100字")
    private String targetPosition;

    /** 目标行业 */
    @Size(max = 100)
    private String targetIndustry;

    /** 工作年限，如：应届、1-3年、3-5年、5年以上 */
    @NotBlank(message = "工作年限不能为空")
    @Size(max = 50)
    private String workYears;

    /** 最高学历，如：本科、硕士、博士 */
    @Size(max = 50)
    private String education;

    /** 毕业院校 */
    @Size(max = 100)
    private String school;

    /** 专业 */
    @Size(max = 100)
    private String major;

    /** 核心技能（逗号分隔） */
    @Size(max = 500, message = "核心技能不能超过500字")
    private String coreSkills;

    /** 工作经历描述（关键词或句子，用于AI扩写） */
    @Size(max = 2000, message = "工作经历不能超过2000字")
    private String workExperience;

    /** 项目经历描述 */
    @Size(max = 2000, message = "项目经历不能超过2000字")
    private String projectExperience;

    /** 个人优势/自我描述 */
    @Size(max = 500)
    private String personalSummary;

    /** 其他附加信息 */
    @Size(max = 500)
    private String additionalInfo;

    /** 简历风格：concise（简洁）、detailed（详细）、technical（技术向） */
    private String style = "detailed";

    /** 是否开启深度思考 */
    private Boolean useDeepThinking = false;

    /** 会话ID */
    @Size(max = 64)
    private String conversationId;
}
