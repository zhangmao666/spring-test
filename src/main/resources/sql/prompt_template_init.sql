CREATE TABLE IF NOT EXISTS prompt_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key',
    prompt_code VARCHAR(100) NOT NULL UNIQUE COMMENT 'Unique prompt code',
    prompt_name VARCHAR(100) NOT NULL COMMENT 'Prompt display name',
    prompt_type VARCHAR(50) NOT NULL DEFAULT 'BUSINESS' COMMENT 'Prompt type',
    prompt_content LONGTEXT NOT NULL COMMENT 'Prompt template content',
    variables TEXT NULL COMMENT 'Variables metadata in JSON/text form',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1 enabled, 0 disabled',
    remark VARCHAR(500) NULL COMMENT 'Remark',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    create_by BIGINT NULL COMMENT 'Create by',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    update_by BIGINT NULL COMMENT 'Update by',
    INDEX idx_prompt_type (prompt_type),
    INDEX idx_status (status),
    INDEX idx_update_time (update_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Prompt template table';

INSERT INTO prompt_template (prompt_code, prompt_name, prompt_type, prompt_content, variables, status, remark)
VALUES
(
    'ai.essay.high-score',
    'AI高分作文模板',
    'AI_BUSINESS',
    '你是一名资深中高考语文阅卷老师和作文教练，请生成一篇可作为高分范文的作文。
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
3. ...',
    '{"topic":"作文题目","gradeLevel":"年级","genre":"体裁","expectedWordCount":"目标字数","requirementsBlock":"补充要求块"}',
    1,
    'Initial built-in essay prompt'
),
(
    'ai.resume.optimize',
    'AI简历优化模板',
    'AI_BUSINESS',
    '你是一名专业的简历优化顾问和职场导师，请对以下简历进行全面优化。

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
3. ...',
    '{"targetPosition":"目标岗位","targetIndustryBlock":"目标行业块","optimizeDirectionBlock":"优化方向块","additionalRequirementsBlock":"附加要求块","resumeContent":"原始简历正文"}',
    1,
    'Initial built-in resume optimize prompt'
),
(
    'ai.resume.generate',
    'AI简历生成模板',
    'AI_BUSINESS',
    '你是一名资深HR和职业规划师，请根据以下信息生成一份专业的中文简历。

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
（3-5条简历优化或求职建议）',
    '{"name":"姓名","targetPosition":"目标岗位","targetIndustryBlock":"目标行业块","workYears":"工作年限","educationBlock":"学历块","schoolBlock":"毕业院校块","majorBlock":"专业块","coreSkillsBlock":"核心技能块","workExperienceBlock":"工作经历块","projectExperienceBlock":"项目经历块","personalSummaryBlock":"个人优势块","additionalInfoBlock":"其他信息块","styleDesc":"风格描述"}',
    1,
    'Initial built-in resume generate prompt'
)
ON DUPLICATE KEY UPDATE
prompt_name = VALUES(prompt_name),
prompt_type = VALUES(prompt_type),
prompt_content = VALUES(prompt_content),
variables = VALUES(variables),
status = VALUES(status),
remark = VALUES(remark),
update_time = CURRENT_TIMESTAMP;
