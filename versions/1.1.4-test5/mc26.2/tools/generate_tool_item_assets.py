from __future__ import annotations

import json
from pathlib import Path

from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src" / "main" / "resources" / "assets"

COLORS = {
    "": (224, 224, 224),
    "black": (31, 31, 35),
    "blue": (45, 63, 166),
    "brown": (111, 72, 43),
    "cyan": (22, 145, 158),
    "gray": (71, 79, 82),
    "green": (73, 91, 36),
    "light_blue": (58, 179, 218),
    "light_gray": (143, 143, 135),
    "lime": (112, 185, 25),
    "magenta": (179, 80, 188),
    "orange": (240, 118, 19),
    "pink": (237, 141, 172),
    "purple": (126, 61, 181),
    "red": (176, 46, 38),
    "yellow": (249, 198, 39),
}


def shade(color: tuple[int, int, int], factor: float) -> tuple[int, int, int, int]:
    return tuple(max(0, min(255, round(channel * factor))) for channel in color) + (255,)


def draw_line(draw: ImageDraw.ImageDraw, points, fill, width: int) -> None:
    draw.line(points, fill=fill, width=width)


def make_building_wand() -> Image.Image:
    image = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    draw_line(draw, [(5, 28), (21, 12)], (76, 42, 22, 255), 5)
    draw_line(draw, [(6, 27), (22, 11)], (126, 75, 32, 255), 2)
    for x, y in ((8, 25), (14, 19)):
        draw.rectangle((x - 2, y - 2, x + 2, y + 2), fill=(238, 159, 31, 255))
        draw.point((x - 1, y - 1), fill=(255, 222, 91, 255))
    draw.polygon([(19, 7), (24, 4), (29, 7), (24, 10)], fill=(255, 188, 38, 255))
    draw.polygon([(19, 7), (24, 10), (24, 16), (19, 13)], fill=(186, 104, 20, 255))
    draw.polygon([(24, 10), (29, 7), (29, 13), (24, 16)], fill=(226, 135, 24, 255))
    draw.line([(17, 5), (17, 15), (27, 19)], fill=(255, 204, 74, 255), width=1)
    draw.line([(21, 3), (21, 15), (30, 17)], fill=(255, 226, 118, 255), width=1)
    draw.rectangle((3, 27, 7, 31), fill=(191, 113, 24, 255))
    draw.rectangle((4, 28, 6, 30), fill=(255, 196, 57, 255))
    return image


def make_move_wand() -> Image.Image:
    image = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    draw_line(draw, [(5, 28), (20, 13)], (35, 39, 53, 255), 5)
    draw_line(draw, [(6, 27), (21, 12)], (74, 83, 112, 255), 2)
    for x, y in ((8, 25), (14, 19)):
        draw.rectangle((x - 2, y - 2, x + 2, y + 2), fill=(34, 189, 226, 255))
        draw.point((x - 1, y - 1), fill=(159, 241, 255, 255))
    draw.polygon([(20, 7), (24, 4), (28, 7), (24, 10)], fill=(95, 224, 255, 255))
    draw.polygon([(20, 7), (24, 10), (24, 15), (20, 12)], fill=(24, 133, 204, 255))
    draw.polygon([(24, 10), (28, 7), (28, 12), (24, 15)], fill=(35, 175, 231, 255))
    draw.line([(18, 5), (15, 8), (17, 9)], fill=(173, 190, 255, 255), width=2)
    draw.line([(29, 14), (31, 11), (29, 10)], fill=(82, 204, 255, 255), width=2)
    draw.line([(17, 9), (16, 13), (19, 16)], fill=(104, 128, 246, 255), width=1)
    draw.line([(29, 10), (30, 7), (27, 4)], fill=(117, 229, 255, 255), width=1)
    draw.rectangle((3, 27, 7, 31), fill=(77, 87, 119, 255))
    draw.rectangle((4, 28, 6, 30), fill=(42, 199, 231, 255))
    return image


def make_remote_switch(color: tuple[int, int, int]) -> Image.Image:
    image = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    # Thin handheld remote, matching the approved inventory preview silhouette.
    draw.polygon([(8, 7), (22, 4), (28, 10), (18, 28), (5, 23)], fill=shade(color, 0.58))
    draw.polygon([(8, 7), (22, 4), (25, 8), (15, 25), (5, 23)], fill=shade(color, 0.92))
    draw.polygon([(10, 8), (21, 6), (23, 9), (14, 23), (7, 21)], fill=(43, 48, 57, 255))
    draw.line([(9, 9), (21, 7)], fill=(72, 79, 90, 255), width=1)
    draw.line([(7, 21), (14, 23), (23, 9)], fill=(24, 28, 34, 255), width=1)
    # Silver toggle.
    draw.polygon([(14, 11), (17, 9), (19, 11), (15, 18), (12, 18)], fill=(210, 214, 220, 255))
    draw.polygon([(15, 18), (19, 11), (20, 13), (16, 20), (12, 18)], fill=(129, 135, 145, 255))
    draw.line([(12, 20), (16, 22), (20, 17)], fill=(152, 158, 167, 255), width=2)
    # Redstone status lamp and metal corner fasteners.
    draw.rectangle((10, 19, 12, 21), fill=(218, 20, 18, 255))
    draw.point((11, 19), fill=(255, 102, 54, 255))
    for point in ((10, 10), (21, 10), (8, 21), (16, 24)):
        draw.point(point, fill=(190, 195, 201, 255))
    return image


def write_json(path: Path, value: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8", newline="\n")


def main() -> None:
    wand_textures = ASSETS / "building_wand" / "textures" / "item"
    make_building_wand().save(wand_textures / "building_wand.png")
    make_move_wand().save(wand_textures / "move_wand.png")

    light_textures = ASSETS / "light" / "textures" / "item"
    light_models = ASSETS / "light" / "models" / "item"
    light_items = ASSETS / "light" / "items"
    for suffix, color in COLORS.items():
        item_name = "redstone_energy_remote_switch" + (f"_{suffix}" if suffix else "")
        icon_name = item_name + "_inventory"
        block_name = item_name + "_off"
        make_remote_switch(color).save(light_textures / f"{icon_name}.png")
        write_json(light_models / f"{icon_name}.json", {
            "parent": "minecraft:item/generated",
            "textures": {"layer0": f"light:item/{icon_name}"},
        })
        write_json(light_items / f"{item_name}.json", {
            "model": {
                "type": "minecraft:select",
                "cases": [{
                    "model": {"type": "minecraft:model", "model": f"light:item/{icon_name}"},
                    "when": "gui",
                }],
                "fallback": {"type": "minecraft:model", "model": f"light:block/{block_name}"},
                "property": "minecraft:display_context",
            }
        })


if __name__ == "__main__":
    main()
