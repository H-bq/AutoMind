from pathlib import Path
import xml.etree.ElementTree as ET


ROOT = Path(__file__).resolve().parents[1]
OUT_DIR = ROOT / "deliverables"
OUT_PATH = OUT_DIR / "automind-data-model-er.drawio"


TABLES = {
    "user": {
        "title": "user",
        "group": "用户与租户",
        "fields": [
            "PK user_id INT",
            "username VARCHAR(32)",
            "password VARCHAR(64)",
            "status VARCHAR(10)",
            "identity VARCHAR(16)",
        ],
        "x": 40,
        "y": 70,
        "w": 250,
        "h": 150,
    },
    "person": {
        "title": "person",
        "group": "用户与租户",
        "fields": [
            "FK user_id INT",
            "tenant_id VARCHAR(64)",
            "position VARCHAR(128)",
            "name VARCHAR(64)",
            "label VARCHAR(64)",
        ],
        "x": 340,
        "y": 70,
        "w": 270,
        "h": 150,
    },
    "audit_log": {
        "title": "audit_log",
        "group": "用户与租户",
        "fields": [
            "PK id INT",
            "tenant_id VARCHAR(64)",
            "FK actor_id INT",
            "actor_role VARCHAR(128)",
            "action VARCHAR(32)",
            "created_at TIMESTAMP",
        ],
        "x": 660,
        "y": 70,
        "w": 280,
        "h": 170,
    },
    "intent_request": {
        "title": "intent_request",
        "group": "意图执行链路",
        "fields": [
            "PK id INT",
            "tenant_id VARCHAR(64)",
            "FK user_id INT",
            "input_text CLOB",
            "parsed_intent CLOB",
            "priority INT",
            "status VARCHAR(32)",
            "trace_id VARCHAR(64)",
            "create_time TIMESTAMP",
        ],
        "x": 40,
        "y": 330,
        "w": 300,
        "h": 245,
    },
    "agent_task": {
        "title": "agent_task",
        "group": "意图执行链路",
        "fields": [
            "PK id BIGINT",
            "tenant_id VARCHAR(64)",
            "FK intent_id INT",
            "FK parent_task_id BIGINT",
            "trace_id VARCHAR(64)",
            "agent_role VARCHAR(32)",
            "task_type VARCHAR(64)",
            "pev_state VARCHAR(16)",
            "confidence DECIMAL(4,3)",
            "retry_count INT",
        ],
        "x": 410,
        "y": 330,
        "w": 320,
        "h": 265,
    },
    "approval_record": {
        "title": "approval_record",
        "group": "意图执行链路",
        "fields": [
            "PK id BIGINT",
            "tenant_id VARCHAR(64)",
            "FK intent_id INT",
            "FK agent_task_id BIGINT",
            "trace_id VARCHAR(64)",
            "FK approver_id INT",
            "approval_type VARCHAR(32)",
            "risk_level VARCHAR(16)",
            "status VARCHAR(16)",
            "expire_at TIMESTAMP",
        ],
        "x": 800,
        "y": 300,
        "w": 330,
        "h": 265,
    },
    "mcp_call_log": {
        "title": "mcp_call_log",
        "group": "意图执行链路",
        "fields": [
            "PK id BIGINT",
            "tenant_id VARCHAR(64)",
            "FK agent_task_id BIGINT",
            "FK intent_id INT",
            "trace_id VARCHAR(64)",
            "tool_name VARCHAR(128)",
            "error_type VARCHAR(32)",
            "latency_ms INT",
            "attempt_no INT",
            "is_high_risk BOOLEAN",
        ],
        "x": 800,
        "y": 620,
        "w": 330,
        "h": 265,
    },
    "a2a_message": {
        "title": "a2a_message",
        "group": "意图执行链路",
        "fields": [
            "PK id BIGINT",
            "tenant_id VARCHAR(64)",
            "FK source_domain_id VARCHAR(64)",
            "FK target_domain_id VARCHAR(64)",
            "FK task_id BIGINT",
            "trace_id VARCHAR(64)",
            "message_type VARCHAR(16)",
            "status VARCHAR(16)",
            "sent_at TIMESTAMP",
            "received_at TIMESTAMP",
        ],
        "x": 1210,
        "y": 300,
        "w": 340,
        "h": 265,
    },
    "healing_record": {
        "title": "healing_record",
        "group": "意图执行链路",
        "fields": [
            "PK id BIGINT",
            "tenant_id VARCHAR(64)",
            "FK intent_id INT",
            "FK trigger_agent_task_id BIGINT",
            "trace_id VARCHAR(64)",
            "trigger_source VARCHAR(32)",
            "root_cause VARCHAR(512)",
            "attempt_no INT",
            "strategy VARCHAR(64)",
            "result VARCHAR(32)",
        ],
        "x": 410,
        "y": 650,
        "w": 320,
        "h": 265,
    },
    "network_domain": {
        "title": "network_domain",
        "group": "网络资源与遥测",
        "fields": [
            "PK id VARCHAR(36)",
            "tenant_id VARCHAR(64)",
            "domain_code VARCHAR(64)",
            "domain_name VARCHAR(128)",
            "controller_type VARCHAR(32)",
            "controller_endpoint VARCHAR(255)",
            "status VARCHAR(32)",
        ],
        "x": 40,
        "y": 1040,
        "w": 330,
        "h": 205,
    },
    "network_node": {
        "title": "network_node",
        "group": "网络资源与遥测",
        "fields": [
            "PK id VARCHAR(36)",
            "tenant_id VARCHAR(64)",
            "FK domain_id VARCHAR(36)",
            "node_code VARCHAR(64)",
            "node_name VARCHAR(128)",
            "node_type VARCHAR(32)",
            "location VARCHAR(128)",
            "management_ip VARCHAR(64)",
            "status VARCHAR(32)",
        ],
        "x": 450,
        "y": 1030,
        "w": 330,
        "h": 245,
    },
    "network_link": {
        "title": "network_link",
        "group": "网络资源与遥测",
        "fields": [
            "PK id VARCHAR(36)",
            "tenant_id VARCHAR(64)",
            "link_code VARCHAR(64)",
            "link_name VARCHAR(128)",
            "FK source_node_id VARCHAR(36)",
            "FK target_node_id VARCHAR(36)",
            "link_type VARCHAR(32)",
            "total_bandwidth_mbps DECIMAL(10,2)",
            "reserved_bandwidth_mbps DECIMAL(10,2)",
            "max_latency_ms DECIMAL(8,2)",
            "status VARCHAR(32)",
        ],
        "x": 860,
        "y": 1010,
        "w": 360,
        "h": 285,
    },
    "network_link_resource": {
        "title": "network_link_resource",
        "group": "网络资源与遥测",
        "fields": [
            "PK id VARCHAR(36)",
            "tenant_id VARCHAR(64)",
            "FK link_id VARCHAR(36)",
            "qos_profile VARCHAR(32)",
            "priority_queue VARCHAR(16)",
            "available_bandwidth_mbps DECIMAL(10,2)",
            "current_latency_ms DECIMAL(8,2)",
            "current_packet_loss_rate DECIMAL(5,4)",
            "current_jitter_ms DECIMAL(8,2)",
            "is_anomaly BOOLEAN",
        ],
        "x": 1310,
        "y": 980,
        "w": 380,
        "h": 285,
    },
    "network_resource_reservation": {
        "title": "network_resource_reservation",
        "group": "网络资源与遥测",
        "fields": [
            "PK id VARCHAR(36)",
            "tenant_id VARCHAR(64)",
            "FK intent_request_id INT",
            "FK link_id VARCHAR(36)",
            "reserved_bandwidth_mbps DECIMAL(10,2)",
            "target_latency_ms DECIMAL(8,2)",
            "target_packet_loss_rate DECIMAL(5,4)",
            "priority INT",
            "reservation_status VARCHAR(32)",
        ],
        "x": 1310,
        "y": 1325,
        "w": 380,
        "h": 245,
    },
    "telemetry_data": {
        "title": "telemetry_data",
        "group": "网络资源与遥测",
        "fields": [
            "time DATETIME",
            "tenant_id VARCHAR(64)",
            "FK link_id VARCHAR(36)",
            "link_code VARCHAR(64)",
            "latency_ms DECIMAL(8,2)",
            "packet_loss_rate DECIMAL(5,4)",
            "bandwidth_usage DECIMAL(5,2)",
            "jitter_ms DECIMAL(8,2)",
            "is_anomaly TINYINT(1)",
        ],
        "x": 860,
        "y": 1350,
        "w": 360,
        "h": 245,
    },
    "sla_record": {
        "title": "sla_record",
        "group": "网络资源与遥测",
        "fields": [
            "PK id BIGINT",
            "tenant_id VARCHAR(64)",
            "FK intent_id INT",
            "FK link_id VARCHAR(64)",
            "metric_type VARCHAR(32)",
            "target_value DECIMAL(12,4)",
            "observed_value DECIMAL(12,4)",
            "breach_flag BOOLEAN",
            "evaluation_window VARCHAR(32)",
            "evaluated_at TIMESTAMP",
        ],
        "x": 410,
        "y": 1350,
        "w": 320,
        "h": 265,
    },
}


EDGES = [
    ("person", "user", "user_id -> user_id"),
    ("intent_request", "user", "user_id -> user_id"),
    ("audit_log", "user", "actor_id -> user_id"),
    ("agent_task", "intent_request", "intent_id -> id"),
    ("agent_task", "agent_task", "parent_task_id -> id"),
    ("approval_record", "intent_request", "intent_id -> id"),
    ("approval_record", "agent_task", "agent_task_id -> id"),
    ("approval_record", "user", "approver_id -> user_id"),
    ("mcp_call_log", "intent_request", "intent_id -> id"),
    ("mcp_call_log", "agent_task", "agent_task_id -> id"),
    ("a2a_message", "agent_task", "task_id -> id"),
    ("healing_record", "intent_request", "intent_id -> id"),
    ("healing_record", "agent_task", "trigger_agent_task_id -> id"),
    ("network_node", "network_domain", "domain_id -> id"),
    ("network_link", "network_node", "source_node_id -> id"),
    ("network_link", "network_node", "target_node_id -> id"),
    ("network_link_resource", "network_link", "link_id -> id"),
    ("network_resource_reservation", "intent_request", "intent_request_id -> id"),
    ("network_resource_reservation", "network_link", "link_id -> id"),
    ("telemetry_data", "network_link", "link_id -> id"),
    ("sla_record", "intent_request", "intent_id -> id"),
    ("sla_record", "network_link", "link_id -> id"),
    ("a2a_message", "network_domain", "source_domain_id -> id"),
    ("a2a_message", "network_domain", "target_domain_id -> id"),
]


GROUP_BOXES = [
    ("g_user", "用户与租户上下文", 20, 25, 950, 245, "#E8F1FF", "#6C8EBF"),
    ("g_intent", "意图执行、审批、Agent、MCP、A2A、自愈审计链路", 20, 275, 1560, 670, "#F3F6FA", "#8497B0"),
    ("g_network", "网络资源、遥测、SLA 与资源预留", 20, 955, 1700, 700, "#EAF7EA", "#82B366"),
]


def table_label(table):
    title = table["title"]
    fields = "".join(f"<div align=\"left\">{field}</div>" for field in table["fields"])
    return (
        f"<div><b>{title}</b></div>"
        f"<hr size=\"1\">{fields}"
    )


def add_cell(root, cell_id, value, style, x, y, w, h, vertex=True, parent="1"):
    cell = ET.SubElement(root, "mxCell", {
        "id": cell_id,
        "value": value,
        "style": style,
        "parent": parent,
    })
    if vertex:
        cell.set("vertex", "1")
    geom = ET.SubElement(cell, "mxGeometry", {
        "x": str(x),
        "y": str(y),
        "width": str(w),
        "height": str(h),
        "as": "geometry",
    })
    return cell


def add_edge(root, edge_id, src, dst, label):
    style = (
        "edgeStyle=orthogonalEdgeStyle;rounded=0;orthogonalLoop=1;jettySize=auto;"
        "html=1;endArrow=ERmany;startArrow=ERone;endFill=0;startFill=0;"
        "strokeColor=#666666;fontSize=10;fontColor=#333333;"
    )
    cell = ET.SubElement(root, "mxCell", {
        "id": edge_id,
        "value": label,
        "style": style,
        "edge": "1",
        "parent": "1",
        "source": src,
        "target": dst,
    })
    ET.SubElement(cell, "mxGeometry", {"relative": "1", "as": "geometry"})
    return cell


def build():
    OUT_DIR.mkdir(exist_ok=True)
    model = ET.Element("mxGraphModel", {
        "dx": "1800",
        "dy": "1700",
        "grid": "1",
        "gridSize": "10",
        "guides": "1",
        "tooltips": "1",
        "connect": "1",
        "arrows": "1",
        "fold": "1",
        "page": "1",
        "pageScale": "1",
        "pageWidth": "1800",
        "pageHeight": "1700",
        "math": "0",
        "shadow": "0",
        "adaptiveColors": "auto",
    })
    root = ET.SubElement(model, "root")
    ET.SubElement(root, "mxCell", {"id": "0"})
    ET.SubElement(root, "mxCell", {"id": "1", "parent": "0"})

    for box_id, title, x, y, w, h, fill, stroke in GROUP_BOXES:
        add_cell(
            root,
            box_id,
            title,
            (
                f"rounded=0;whiteSpace=wrap;html=1;fillColor={fill};strokeColor={stroke};"
                "fontStyle=1;fontSize=15;align=left;verticalAlign=top;spacing=10;opacity=35;"
            ),
            x,
            y,
            w,
            h,
        )

    note = (
        "说明：图中 PK 表示主键，FK 表示业务关联字段；tenant_id 是多租户逻辑隔离键，"
        "trace_id 用于串联意图、Agent、审批、MCP、A2A、SLA 与自愈记录。"
    )
    add_cell(
        root,
        "note",
        note,
        "rounded=0;whiteSpace=wrap;html=1;fillColor=#FFF2CC;strokeColor=#D6B656;fontSize=12;align=left;spacing=8;",
        1020,
        45,
        650,
        90,
    )

    table_style = (
        "rounded=0;whiteSpace=wrap;html=1;fillColor=#FFFFFF;strokeColor=#4F81BD;"
        "fontSize=11;align=center;verticalAlign=top;spacing=8;spacingTop=4;"
    )
    for table_id, table in TABLES.items():
        add_cell(root, table_id, table_label(table), table_style, table["x"], table["y"], table["w"], table["h"])

    for idx, (src, dst, label) in enumerate(EDGES, start=1):
        add_edge(root, f"e{idx}", src, dst, label)

    xml = ET.tostring(model, encoding="unicode", short_empty_elements=False)
    OUT_PATH.write_text(xml, encoding="utf-8")
    ET.parse(OUT_PATH)
    print(OUT_PATH)


if __name__ == "__main__":
    build()
