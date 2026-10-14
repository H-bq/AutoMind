# MCP 工具登记

| name | version | highRisk | 说明 |
|---|---|---:|---|
| get_topo | v1 | false | 查询当前域拓扑（节点 + 链路） |
| set_qos | v1 | true | 设置链路 QoS 策略（带宽 + 优先级） |
| block_flow | v1 | true | 阻断指定流量五元组，阶段三仅写审计 |

阶段三所有 MCP 调用必须通过 `McpClient.invoke(...)`，调用记录写入 `mcp_call_log`。
