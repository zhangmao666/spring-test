package com.example.springboottest.modules.task.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

/**
 * 任务详情VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TaskDetailVO extends TaskVO {
    private TaskApprovalFlowVO flow;
    private List<ApprovalNodeVO> nodes;
    private List<ApprovalRecordVO> records;
    private List<Long> currentApprovers;
    private Boolean canApprove;
    private Boolean canWithdraw;
}
