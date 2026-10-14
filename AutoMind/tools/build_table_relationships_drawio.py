from pathlib import Path
import xml.etree.ElementTree as ET


ROOT = Path(__file__).resolve().parents[1]
OUT_DIR = ROOT / "deliverables"
OUT_PATH = OUT_DIR / "automind-table-relationships.drawio"


TABLES = {
    "user": (80, 60, 260, 150, ["PK user_id", "username", "identity"]),
    "person": (430, 60, 280, 150, ["FK user_id", "tenant_id", "position", "name"]),
    "audit_log": (800, 60, 280, 150, ["PK id", "tenant_id", "FK actor_id", "action"]),
    "intent_request": (80, 320, 320, 210, ["PK id", "tenant_id", "FK user_id", "status", "trace_id"]),
    "agent_task": (520, 320, 330, 230, ["PK id", "tenant_id", "FK intent_id", "FK parent_task_id", "trace_id", "agent_role", "pev_state"]),
    "approval_record": (1010, 260, 340, 230, ["PK id", "tenant_id", "FK intent_id", "FK agent_task_id", "FK approver_id", "approval_type", "status"]),
    "mcp_call_log": (1010, 570, 340, 230, ["PK id", "tenant_id", "FK agent_task_id", "FK intent_id", "trace_id", "tool_name", "error_type"]),
    "a2a_message": (520, 640, 340, 230, ["PK id", "tenant_id", "FK task_id", "FK source_domain_id", "FK target_domain_id", "trace_id", "message_type"]),
    "healing_record": (80, 640, 330, 230, ["PK id", "tenant_id", "FK intent_id", "FK trigger_agent_task_id", "trace_id", "trigger_source", "result"]),
    "network_domain": (80, 1010, 320, 190, ["PK id", "tenant_id", "domain_code", "domain_name", "controller_type", "status"]),
    "network_node": (520, 1000, 320, 210, ["PK id", "tenant_id", "FK domain_id", "node_code", "node_type", "status"]),
    "network_link": (1010, 980, 360, 250, ["PK id", "tenant_id", "FK source_node_id", "FK target_node_id", "total_bandwidth_mbps", "reserved_bandwidth_mbps", "status"]),
    "network_link_resource": (1500, 920, 390, 230, ["PK id", "tenant_id", "FK link_id", "qos_profile", "available_bandwidth_mbps", "current_latency_ms", "is_anomaly"]),
    "network_resource_reservation": (1500, 1240, 390, 230, ["PK id", "tenant_id", "FK intent_request_id", "FK link_id", "reserved_bandwidth_mbps", "reservation_status"]),
    "telemetry_data": (1010, 1360, 360, 230, ["time", "tenant_id", "FK link_id", "latency_ms", "packet_loss_rate", "bandwidth_usage", "is_anomaly"]),
    "sla_record": (520, 1360, 320, 230, ["PK id", "tenant_id", "FK intent_id", "FK link_id", "metric_type", "breach_flag", "evaluated_at"]),
}


RELATIONS = [
    ("person", "user", "person.user_id = user.user_id", "1:1"),
    ("intent_request", "user", "intent_request.user_id = user.user_id", "N:1"),
    ("audit_log", "user", "audit_log.actor_id = user.user_id", "N:1"),
    ("agent_task", "intent_request", "agent_task.intent_id = intent_request.id", "N:1"),
    ("agent_task", "agent_task", "agent_task.parent_task_id = agent_task.id", "N:1"),
    ("approval_record", "intent_request", "approval_record.intent_id = intent_request.id", "N:1"),
    ("approval_record", "agent_task", "approval_record.agent_task_id = agent_task.id", "N:1"),
    ("approval_record", "user", "approval_record.approver_id = user.user_id", "N:1"),
    ("mcp_call_log", "intent_request", "mcp_call_log.intent_id = intent_request.id", "N:1"),
    ("mcp_call_log", "agent_task", "mcp_call_log.agent_task_id = agent_task.id", "N:1"),
    ("a2a_message", "agent_task", "a2a_message.task_id = agent_task.id", "N:1"),
    ("healing_record", "intent_request", "healing_record.intent_id = intent_request.id", "N:1"),
    ("healing_record", "agent_task", "healing_record.trigger_agent_task_id = agent_task.id", "N:1"),
    ("network_node", "network_domain", "network_node.domain_id = network_domain.id", "N:1"),
    ("network_link", "network_node", "network_link.source_node_id = network_node.id", "N:1"),
    ("network_link", "network_node", "network_link.target_node_id = network_node.id", "N:1"),
    ("network_link_resource", "network_link", "network_link_resource.link_id = network_link.id", "N:1"),
    ("network_resource_reservation", "intent_request", "network_resource_reservation.intent_request_id = intent_request.id", "N:1"),
    ("network_resource_reservation", "network_link", "network_resource_reservation.link_id = network_link.id", "N:1"),
    ("telemetry_data", "network_link", "telemetry_data.link_id = network_link.id", "N:1"),
    ("sla_record", "intent_request", "sla_record.intent_id = intent_request.id", "N:1"),
    ("sla_record", "network_link", "sla_record.link_id = network_link.id", "N:1"),
    ("a2a_message", "network_domain", "a2a_message.source_domain_id = network_domain.id", "N:1"),
    ("a2a_message", "network_domain", "a2a_message.target_domain_id = network_domain.id", "N:1"),
]


def label(table_name, fields):
    field_rows = "".join(
        f"<tr><td align=\"left\" style=\"padding:2px 6px;\">{field}</td></tr>"
        for field in fields
    )
    return (
        "<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">"
        f"<tr><td bgcolor=\"#1F4E79\" style=\"color:#FFFFFF;padding:5px 8px;\"><b>{table_name}</b></td></tr>"
        f"{field_rows}"
        "</table>"
    )


def add_table(root, table_id, x, y, w, h, fields):
    style = (
        "shape=rectangle;whiteSpace=wrap;html=1;rounded=0;fillColor=#FFFFFF;"
        "strokeColor=#1F4E79;fontSize=12;align=center;verticalAlign=top;spacing=0;"
    )
    cell = ET.SubElement(
        root,
        "mxCell",
        {"id": table_id, "value": label(table_id, fields), "style": style, "vertex": "1", "parent": "1"},
    )
    ET.SubElement(
        cell,
        "mxGeometry",
        {"x": str(x), "y": str(y), "width": str(w), "height": str(h), "as": "geometry"},
    )


def add_relation(root, idx, src, dst, title, cardinality):
    if cardinality == "N:1":
        start_arrow = "ERmany"
        end_arrow = "ERone"
    else:
        start_arrow = "ERone"
        end_arrow = "ERone"
    style = (
        "edgeStyle=orthogonalEdgeStyle;rounded=0;orthogonalLoop=1;jettySize=auto;"
        f"html=1;startArrow={start_arrow};endArrow={end_arrow};startFill=0;endFill=0;"
        "strokeColor=#404040;fontSize=10;fontColor=#404040;labelBackgroundColor=#FFFFFF;"
    )
    cell = ET.SubElement(
        root,
        "mxCell",
        {
            "id": f"r{idx}",
            "value": title,
            "style": style,
            "edge": "1",
            "parent": "1",
            "source": src,
            "target": dst,
        },
    )
    ET.SubElement(cell, "mxGeometry", {"relative": "1", "as": "geometry"})


def add_title(root):
    cell = ET.SubElement(
        root,
        "mxCell",
        {
            "id": "title",
            "value": "AutoMind 数据表关系图（ERD）",
            "style": "text;html=1;strokeColor=none;fillColor=none;fontSize=24;fontStyle=1;fontColor=#1F4E79;",
            "vertex": "1",
            "parent": "1",
        },
    )
    ET.SubElement(cell, "mxGeometry", {"x": "80", "y": "10", "width": "450", "height": "35", "as": "geometry"})
    note = ET.SubElement(
        root,
        "mxCell",
        {
            "id": "note",
            "value": "连线标签显示关联字段；乌鸦脚表示多端。tenant_id 为逻辑隔离字段，未逐一连线。",
            "style": "text;html=1;strokeColor=none;fillColor=none;fontSize=13;fontColor=#666666;align=left;",
            "vertex": "1",
            "parent": "1",
        },
    )
    ET.SubElement(note, "mxGeometry", {"x": "540", "y": "15", "width": "700", "height": "30", "as": "geometry"})


def build():
    OUT_DIR.mkdir(exist_ok=True)
    model = ET.Element(
        "mxGraphModel",
        {
            "dx": "1900",
            "dy": "1650",
            "grid": "1",
            "gridSize": "10",
            "guides": "1",
            "tooltips": "1",
            "connect": "1",
            "arrows": "1",
            "fold": "1",
            "page": "1",
            "pageScale": "1",
            "pageWidth": "2000",
            "pageHeight": "1650",
            "math": "0",
            "shadow": "0",
            "adaptiveColors": "auto",
        },
    )
    root = ET.SubElement(model, "root")
    ET.SubElement(root, "mxCell", {"id": "0"})
    ET.SubElement(root, "mxCell", {"id": "1", "parent": "0"})
    add_title(root)
    for table_id, (x, y, w, h, fields) in TABLES.items():
        add_table(root, table_id, x, y, w, h, fields)
    for idx, (src, dst, title, cardinality) in enumerate(RELATIONS, start=1):
        add_relation(root, idx, src, dst, title, cardinality)

    xml = ET.tostring(model, encoding="unicode", short_empty_elements=False)
    OUT_PATH.write_text(xml, encoding="utf-8")
    ET.parse(OUT_PATH)
    print(OUT_PATH)


if __name__ == "__main__":
    build()
