package com.example.springboottest.DTO;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 审批流详情VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TaskApprovalFlowDetailVO extends TaskApprovalFlowVO {

    private List<TaskApprovalNodeVO> nodes;
}
