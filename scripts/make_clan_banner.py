"""The cloth of the great banner of the clans (48x96, three blocks by six) and its item icon: deep red,
a gold border, the white interlaced knot of the clans in the middle, and a pointed hem.

Run from the repository root: python scripts/make_clan_banner.py
"""
import math
import os
import random

from PIL import Image

ASSETS = os.path.join("src", "main", "resources", "assets", "sofe", "textures")
W, H = 48, 96
RED, DARK, GOLD, WHITE = (150, 22, 28), (96, 12, 18), (214, 170, 72), (236, 230, 220)

img = Image.new("RGBA", (W, H), (0, 0, 0, 0))
rnd = random.Random(7)
hem = 14  # the point at the bottom
for y in range(H):
    for x in range(W):
        if y >= H - hem and abs(x - (W - 1) / 2) > (H - 1 - y) * (W / 2) / hem:
            continue  # cut away: the pointed hem
        n = rnd.randint(-8, 8)
        c = tuple(max(0, min(255, v + n)) for v in RED)
        if (y // 2) % 2 == 0 and x % 3 == 0:
            c = tuple(max(0, v - 10) for v in c)  # the weave
        img.putpixel((x, y), c + (255,))
# gold border down the sides and under the bar, following the hem
for y in range(H):
    for x in range(W):
        if img.getpixel((x, y))[3] == 0:
            continue
        edge = x < 3 or x > W - 4 or y < 3
        if not edge:
            for dx, dy in ((-3, 0), (3, 0), (0, 3)):
                nx, ny = x + dx, y + dy
                if not (0 <= nx < W and 0 <= ny < H) or img.getpixel((nx, ny))[3] == 0:
                    edge = True
        if edge:
            img.putpixel((x, y), GOLD + (255,))


def stroke(points, width=2.2, color=WHITE):
    for (x0, y0), (x1, y1) in zip(points, points[1:]):
        steps = int(max(abs(x1 - x0), abs(y1 - y0)) * 3) + 1
        for i in range(steps + 1):
            t = i / steps
            px, py = x0 + (x1 - x0) * t, y0 + (y1 - y0) * t
            for dx in range(-3, 4):
                for dy in range(-3, 4):
                    if dx * dx + dy * dy <= width * width:
                        qx, qy = int(round(px + dx)), int(round(py + dy))
                        if 0 <= qx < W and 0 <= qy < H and img.getpixel((qx, qy))[3]:
                            img.putpixel((qx, qy), color + (255,))


# the knot: two interlaced diamonds and a cross through them, outlined in dark red
cx, cy, r = W / 2 - 0.5, 40, 14
diamond = [(cx, cy - r * 1.6), (cx + r, cy), (cx, cy + r * 1.6), (cx - r, cy), (cx, cy - r * 1.6)]
small = [(cx, cy - r * 0.8), (cx + r * 0.5, cy), (cx, cy + r * 0.8), (cx - r * 0.5, cy), (cx, cy - r * 0.8)]
for shape in (diamond, small):
    stroke(shape, 3.3, DARK)
stroke([(cx - r * 1.2, cy - r * 1.2), (cx + r * 1.2, cy + r * 1.2)], 3.3, DARK)
stroke([(cx + r * 1.2, cy - r * 1.2), (cx - r * 1.2, cy + r * 1.2)], 3.3, DARK)
for shape in (diamond, small):
    stroke(shape, 2.0)
stroke([(cx - r * 1.2, cy - r * 1.2), (cx + r * 1.2, cy + r * 1.2)], 2.0)
stroke([(cx + r * 1.2, cy - r * 1.2), (cx - r * 1.2, cy + r * 1.2)], 2.0)
# a band of runes under the knot
for i, x in enumerate(range(10, W - 10, 6)):
    for y in range(70, 76):
        if (y - 70 + i) % 3 != 1:
            img.putpixel((x + (y % 2), y), GOLD + (255,))
img.save(os.path.join(ASSETS, "entity", "clan_banner.png"))

icon = img.resize((8, 16), Image.NEAREST)
item = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
item.paste(icon, (4, 0), icon)
for x in range(2, 14):
    item.putpixel((x, 0), (70, 46, 26, 255))  # the bar
item.save(os.path.join(ASSETS, "item", "clan_banner.png"))
print("clan banner")
