"""The cloths of the great banners of Parsivan, Khemet and Aureum (48x96, three blocks by six, like the clans' in
make_clan_banner.py) and their item icons:

- Parsivan: violet, a turquoise eight-pointed star under a white crescent, a gold border, a fringe of turquoise;
- Khemet: deep blue, a golden ankh under the sun disk, a band of gold zigzags, a gold border;
- Aureum: royal blue, a golden laurel wreath round the eagle's crown, SPQA under it, a gold border.

Run from the repository root: python scripts/make_empire_banners.py
"""
import math
import os
import random

from PIL import Image

ASSETS = os.path.join("src", "main", "resources", "assets", "sofe", "textures")
W, H = 48, 96
GOLD, WHITE = (222, 178, 70), (238, 232, 222)


def cloth(base, seed, hem=14):
    img = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    rnd = random.Random(seed)
    for y in range(H):
        for x in range(W):
            if y >= H - hem and abs(x - (W - 1) / 2) > (H - 1 - y) * (W / 2) / hem:
                continue  # the pointed hem
            n = rnd.randint(-7, 7)
            c = tuple(max(0, min(255, v + n)) for v in base)
            if (y // 2) % 2 == 0 and x % 3 == 0:
                c = tuple(max(0, v - 10) for v in c)  # the weave
            img.putpixel((x, y), c + (255,))
    return img


def border(img, color=GOLD):
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
                img.putpixel((x, y), color + (255,))


def put(img, x, y, color):
    x, y = int(round(x)), int(round(y))
    if 0 <= x < W and 0 <= y < H and img.getpixel((x, y))[3]:
        img.putpixel((x, y), color + (255,))


def disc(img, cx, cy, r, color):
    for y in range(int(cy - r - 1), int(cy + r + 2)):
        for x in range(int(cx - r - 1), int(cx + r + 2)):
            if (x - cx) ** 2 + (y - cy) ** 2 <= r * r:
                put(img, x, y, color)


def stroke(img, points, width, color):
    for (x0, y0), (x1, y1) in zip(points, points[1:]):
        steps = int(max(abs(x1 - x0), abs(y1 - y0)) * 3) + 1
        for i in range(steps + 1):
            t = i / steps
            disc(img, x0 + (x1 - x0) * t, y0 + (y1 - y0) * t, width, color)


def polygon(img, pts, color):
    ys = [p[1] for p in pts]
    for y in range(int(min(ys)), int(max(ys)) + 1):
        for x in range(W):
            inside = False
            for (x0, y0), (x1, y1) in zip(pts, pts[1:] + pts[:1]):
                if (y0 > y) != (y1 > y) and x < x0 + (y - y0) * (x1 - x0) / (y1 - y0):
                    inside = not inside
            if inside:
                put(img, x, y, color)


def save(img, name, bar):
    img.save(os.path.join(ASSETS, "entity", name + ".png"))
    icon = img.resize((8, 16), Image.NEAREST)
    item = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    item.paste(icon, (4, 0), icon)
    for x in range(2, 14):
        item.putpixel((x, 0), bar + (255,))
    item.save(os.path.join(ASSETS, "item", name + ".png"))


# --- Parsivan
VIOLET, TURQ, DARKV = (104, 40, 150), (56, 190, 186), (60, 18, 92)
img = cloth(VIOLET, 31)
border(img)
cx, cy = W / 2 - 0.5, 44
for a in (0, 45):  # the eight-pointed star: two squares turned on each other
    pts = [(cx + 13 * math.cos(math.radians(a + 45 + 90 * k)), cy + 13 * math.sin(math.radians(a + 45 + 90 * k))) for k in range(4)]
    polygon(img, pts, TURQ)
disc(img, cx, cy, 5, GOLD)
disc(img, cx, cy, 2.5, VIOLET)
disc(img, cx, 18, 7, WHITE)  # the crescent over it
disc(img, cx + 3, 16, 6.5, VIOLET)
for x in range(4, W - 4, 4):  # a fringe of turquoise above the hem
    for y in range(68, 72):
        put(img, x + (y % 2), y, TURQ)
save(img, "parsivan_banner", (60, 36, 24))

# --- Khemet
BLUE, LAPIS = (30, 56, 150), (20, 36, 110)
img = cloth(BLUE, 41)
border(img)
cx = W / 2 - 0.5
disc(img, cx, 16, 7, GOLD)  # the sun disk with its two wings
for side in (-1, 1):
    stroke(img, [(cx + side * 7, 16), (cx + side * 17, 12)], 1.4, GOLD)
    stroke(img, [(cx + side * 7, 18), (cx + side * 16, 16)], 1.2, GOLD)
# the ankh
for t in range(0, 360, 4):
    a = math.radians(t)
    disc(img, cx + 7 * math.cos(a), 36 + 9 * math.sin(a), 1.9, GOLD)
stroke(img, [(cx - 13, 47), (cx + 13, 47)], 2.2, GOLD)
stroke(img, [(cx, 46), (cx, 70)], 2.4, GOLD)
for i, x in enumerate(range(5, W - 5, 4)):  # the band of zigzags
    for k in range(4):
        put(img, x + k, 76 + (k if i % 2 == 0 else 3 - k), GOLD)
save(img, "khemet_banner", (214, 182, 122))

# --- Aureum
ROYAL = (36, 62, 168)
img = cloth(ROYAL, 51)
border(img)
cx, cy = W / 2 - 0.5, 34
for side in (-1, 1):  # the laurel wreath: two branches of leaves meeting at the top
    for k in range(13):
        theta = math.radians(-75 + k * 13)  # from near the top down round the side to the bottom
        x, y = cx + side * 14 * math.cos(theta), cy + 14 * math.sin(theta)
        disc(img, x, y, 1.6, GOLD)
        disc(img, x + side * 2.2, y - 1.2, 1.1, (246, 214, 120))
# the crown in the wreath
polygon(img, [(cx - 7, 38), (cx + 7, 38), (cx + 8, 28), (cx + 4, 33), (cx, 26), (cx - 4, 33), (cx - 8, 28)], GOLD)
stroke(img, [(cx - 7, 39), (cx + 7, 39)], 1.0, (246, 214, 120))
LETTERS = {  # 3x5
    "S": ["###", "#..", "###", "..#", "###"],
    "P": ["###", "#.#", "###", "#..", "#.."],
    "Q": ["###", "#.#", "#.#", "###", "..#"],
    "A": ["###", "#.#", "###", "#.#", "#.#"],
}
x0 = int(cx - 8)
for i, ch in enumerate("SPQA"):
    for row, line in enumerate(LETTERS[ch]):
        for col, c in enumerate(line):
            if c == "#":
                for dy in range(2):  # twice as tall
                    put(img, x0 + i * 4 + col, 58 + row * 2 + dy, GOLD)
save(img, "aureum_banner", (232, 230, 226))
print("empire banners")
