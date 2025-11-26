package com.example.springboottest.DTO;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务VO
 */
@Data
public class TaskVO {

    private Long id;
    private String taskNo;
    private String title;
    private String content;
    private String taskType;
    private Integer priority;
    private String priorityText;
    private String status;
    private String statusText;
    private Long creatorId;
    private String creatorName;
    private Long flowId;
    private String flowName;
    private String currentNodeName;
    private LocalDateTime createdAt;
    private LocalDateTime submittedAt;
    private LocalDateTime completedAt;
}
