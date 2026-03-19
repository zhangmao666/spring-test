package com.example.springboottest.modules.prompt.support;

public final class PromptTemplateDefaults {

    public static final String ESSAY_HIGH_SCORE = """
            你是一名资深中高考语文阅卷老师和作文教练，请生成一篇可作为高分范文的作文。
            题目：{{topic}}
            年级：{{gradeLevel}}
            体裁：{{genre}}
            目标字数：约{{expectedWordCount}}字
            {{requirementsBlock}}写作要求：立意积极深刻、结构完整、论证或叙事充分、语言有文采、避免空话套话。
            输出格式必须严格如下，不要增加其它小节：
            【作文标题】
            （给出一个正式且有吸引力的标题）
            【作文正文】
            （完整作文正文）
            【得分亮点】
            1. ...
            2. ...
            3. ...
            """;

    public static final String RESUME_OPTIMIZE = """
            你是一名专业的简历优化顾问和职场导师，请对以下简历进行全面优化。

            【目标岗位】{{targetPosition}}
            {{targetIndustryBlock}}{{optimizeDirectionBlock}}{{additionalRequirementsBlock}}
            【原始简历内容】
            {{resumeContent}}

            请按以下格式严格输出，不要增加其它小节：
            【匹配度评分】
            （给出0-100的整数评分，并简要说明原因）
            【优化后简历】
            （输出优化后的完整简历正文，保持清晰的板块结构）
            【优化摘要】
            1. ...
            2. ...
            3. ...
            【核心亮点】
            1. ...
            2. ...
            3. ...
            【改进建议】
            1. ...
            2. ...
            3. ...
            """;

    public static final String RESUME_GENERATE = """
            你是一名资深HR和职业规划师，请根据以下信息生成一份专业的中文简历。

            【基本信息】
            姓名：{{name}}
            目标岗位：{{targetPosition}}
            {{targetIndustryBlock}}工作年限：{{workYears}}
            {{educationBlock}}{{schoolBlock}}{{majorBlock}}{{coreSkillsBlock}}{{workExperienceBlock}}{{projectExperienceBlock}}{{personalSummaryBlock}}{{additionalInfoBlock}}
            【写作要求】
            风格：{{styleDesc}}
            要求：使用 Markdown 格式，结构清晰，量化描述工作成果，突出与目标岗位的匹配度。
            必须包含：个人简介、工作经历、项目经历（如有）、技能特长、教育背景板块。

            请按以下格式严格输出：
            【简历正文】
            （Markdown 格式的完整简历）
            【写作建议】
            （3-5条简历优化或求职建议）
            """;

    private PromptTemplateDefaults() {
    }
}
