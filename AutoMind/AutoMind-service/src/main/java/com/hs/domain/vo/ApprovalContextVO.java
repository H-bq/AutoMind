package com.hs.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@Schema(description = "审批上下文视图")
public class ApprovalContextVO {
    private ApprovalRecordVO approval;
    private ReadModelVO.IntentRequestVO intent;
    private List<AgentTaskVO> agentTasks;
}
