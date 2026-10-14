package com.hs.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "链路实时状态视图")
public class LinkStatusVO {

    @Schema(description = "链路静态信息")
    private TopologyVO.LinkVO link;

    @Schema(description = "源节点信息")
    private TopologyVO.NodeVO sourceNode;

    @Schema(description = "目标节点信息")
    private TopologyVO.NodeVO targetNode;

    @Schema(description = "源节点所属网络域")
    private TopologyVO.DomainVO sourceDomain;

    @Schema(description = "目标节点所属网络域")
    private TopologyVO.DomainVO targetDomain;

    @Schema(description = "最近链路资源采样")
    private TopologyVO.LinkResourceVO latestResource;

    @Schema(description = "最近 SLA 评估结论")
    private SlaVerdictVO latestSlaVerdict;

    @Schema(description = "最近遥测采样")
    private ReadModelVO.TelemetryDataVO latestTelemetry;

    @Schema(description = "最近24小时遥测趋势，按时间升序")
    private List<ReadModelVO.TelemetryDataVO> telemetryTrend = new ArrayList<>();

    @Schema(description = "链路详情数据完整性问题")
    private List<TopologyVO.DataIssueVO> dataIssues = new ArrayList<>();
}
