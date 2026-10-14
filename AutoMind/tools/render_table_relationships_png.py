from collections import defaultdict
from pathlib import Path
import textwrap

from PIL import Image, ImageDraw, ImageFont

from build_table_relationships_drawio import TABLES, RELATIONS, OUT_DIR


OUT_PATH = OUT_DIR / "automind-table-relationships.png"
CANVAS_W = 2000
CANVAS_H = 1650
SCALE = 2


def font(size, bold=False):
    candidates = [
        r"C:\Windows\Fonts\msyhbd.ttc" if bold else r"C:\Windows\Fonts\msyh.ttc",
        r"C:\Windows\Fonts\simhei.ttf",
        r"C:\Windows\Fonts\arial.ttf",
    ]
    for path in candidates:
        if Path(path).exists():
            return ImageFont.truetype(path, size * SCALE)
    return ImageFont.load_default()


TITLE_FONT = font(24, True)
NOTE_FONT = font(13)
HEADER_FONT = font(13, True)
FIELD_FONT = font(11)
EDGE_FONT = font(10)


def s(value):
    return int(value * SCALE)


def rect(table_id):
    x, y, w, h, _ = TABLES[table_id]
    return x, y, w, h


def center(table_id):
    x, y, w, h = rect(table_id)
    return x + w / 2, y + h / 2


def anchor(src, dst):
    sx, sy = center(src)
    dx, dy = center(dst)
    x, y, w, h = rect(src)
    if abs(dx - sx) > abs(dy - sy):
        return (x + w if dx > sx else x, sy)
    return (sx, y + h if dy > sy else y)


def wrap_label(text, width=42):
    if len(text) <= width:
        return text
    return "\n".join(textwrap.wrap(text, width=width, break_long_words=False))


def draw_relation(draw, src, dst, label, cardinality, index_offset):
    if src == dst:
        x, y, w, h = rect(src)
        pts = [
            (s(x + w), s(y + h * 0.35)),
            (s(x + w + 50), s(y + h * 0.35)),
            (s(x + w + 50), s(y + h * 0.65)),
            (s(x + w), s(y + h * 0.65)),
        ]
    else:
        p1 = anchor(src, dst)
        p2 = anchor(dst, src)
        offset = index_offset * 18
        if abs(p2[0] - p1[0]) > abs(p2[1] - p1[1]):
            mid_x = (p1[0] + p2[0]) / 2
            pts = [(s(p1[0]), s(p1[1] + offset)), (s(mid_x), s(p1[1] + offset)), (s(mid_x), s(p2[1] - offset)), (s(p2[0]), s(p2[1] - offset))]
        else:
            mid_y = (p1[1] + p2[1]) / 2
            pts = [(s(p1[0] + offset), s(p1[1])), (s(p1[0] + offset), s(mid_y)), (s(p2[0] - offset), s(mid_y)), (s(p2[0] - offset), s(p2[1]))]

    draw.line(pts, fill=(80, 80, 80), width=s(2), joint="curve")

    start_card, end_card = ("N", "1") if cardinality == "N:1" else ("1", "1")
    draw.text((pts[0][0] + s(4), pts[0][1] + s(4)), start_card, font=EDGE_FONT, fill=(40, 40, 40))
    draw.text((pts[-1][0] + s(4), pts[-1][1] + s(4)), end_card, font=EDGE_FONT, fill=(40, 40, 40))

    mx = sum(p[0] for p in pts) // len(pts)
    my = sum(p[1] for p in pts) // len(pts)
    wrapped = wrap_label(label)
    bbox = draw.multiline_textbbox((mx, my), wrapped, font=EDGE_FONT, spacing=s(2))
    pad = s(4)
    box = (bbox[0] - pad, bbox[1] - pad, bbox[2] + pad, bbox[3] + pad)
    draw.rectangle(box, fill=(255, 255, 255), outline=(210, 210, 210))
    draw.multiline_text((bbox[0], bbox[1]), wrapped, font=EDGE_FONT, fill=(50, 50, 50), spacing=s(2))


def draw_table(draw, table_id, data):
    x, y, w, h, fields = data
    x, y, w, h = map(s, (x, y, w, h))
    header_h = s(34)
    draw.rectangle((x, y, x + w, y + h), fill=(255, 255, 255), outline=(31, 78, 121), width=s(2))
    draw.rectangle((x, y, x + w, y + header_h), fill=(31, 78, 121), outline=(31, 78, 121))
    draw.text((x + s(8), y + s(7)), table_id, font=HEADER_FONT, fill=(255, 255, 255))
    line_y = y + header_h
    draw.line((x, line_y, x + w, line_y), fill=(31, 78, 121), width=s(1))
    ty = y + header_h + s(8)
    for field in fields:
        color = (150, 35, 35) if field.startswith("PK") else (31, 85, 40) if field.startswith("FK") else (35, 35, 35)
        draw.text((x + s(10), ty), field, font=FIELD_FONT, fill=color)
        ty += s(22)


def main():
    OUT_DIR.mkdir(exist_ok=True)
    img = Image.new("RGB", (CANVAS_W * SCALE, CANVAS_H * SCALE), (248, 250, 252))
    draw = ImageDraw.Draw(img)
    draw.text((s(80), s(12)), "AutoMind 数据表关系图（ERD）", font=TITLE_FONT, fill=(31, 78, 121))
    draw.text(
        (s(540), s(20)),
        "连线标签显示关联字段；N/1 标识多端与一端；tenant_id 是逻辑隔离字段，未逐一连线。",
        font=NOTE_FONT,
        fill=(90, 90, 90),
    )

    pair_counts = defaultdict(int)
    for src, dst, label, cardinality in RELATIONS:
        pair_key = tuple(sorted((src, dst)))
        pair_counts[pair_key] += 1
        offset_index = pair_counts[pair_key] - 1
        if offset_index % 2 == 0:
            offset_index = offset_index // 2
        else:
            offset_index = -((offset_index + 1) // 2)
        draw_relation(draw, src, dst, label, cardinality, offset_index)

    for table_id, data in TABLES.items():
        draw_table(draw, table_id, data)

    img = img.resize((CANVAS_W, CANVAS_H), Image.Resampling.LANCZOS)
    img.save(OUT_PATH)
    print(OUT_PATH)


if __name__ == "__main__":
    main()
