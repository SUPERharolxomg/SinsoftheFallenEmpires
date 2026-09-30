"""Placeholder textures for Sprint 4 (Seal Veil, Waystone, Personal Vault, Sulthari building set,
Return Scroll and the Bearer outfit layers). All drawn here from code, so they can be replaced
one by one by final art without touching anything else.

Run from the repository root: python scripts/make_sprint4_textures.py
"""
import os
import random
from PIL import Image, ImageDraw

ROOT = os.path.join("src", "main", "resources", "assets", "sofe", "textures")
BLOCK = os.path.join(ROOT, "block")
ITEM = os.path.join(ROOT, "item")
OUTFIT = os.path.join(ROOT, "entity", "outfit")
for d in (BLOCK, ITEM, OUTFIT):
    os.makedirs(d, exist_ok=True)


def shade(c, f):
    return tuple(max(0, min(255, int(v * f))) for v in c[:3]) + ((c[3],) if len(c) > 3 else ())


def noise_fill(img, base, spread, seed):
    rnd = random.Random(seed)
    px = img.load()
    for y in range(img.height):
        for x in range(img.width):
            px[x, y] = shade(base, 1 + rnd.uniform(-spread, spread))


def save(img, folder, name):
    img.save(os.path.join(folder, name + ".png"))


# --- Seal Veil: pale, see-through aetherium with brighter veins; the block color tints it per player
veil = Image.new("RGBA", (16, 16))
rnd = random.Random(7)
px = veil.load()
for y in range(16):
    for x in range(16):
        v = 200 + rnd.randint(-20, 20)
        px[x, y] = (v, v, v, 90)
for i in range(3):
    x = rnd.randint(0, 15)
    for y in range(16):
        x = (x + rnd.choice((-1, 0, 1))) % 16
        px[x, y] = (255, 255, 255, 170)
save(veil, BLOCK, "seal_veil")

# --- Sulthari sandstone bricks
bricks = Image.new("RGBA", (16, 16))
noise_fill(bricks, (214, 182, 124, 255), 0.06, 1)
d = ImageDraw.Draw(bricks)
mortar = (160, 128, 82, 255)
for y in (0, 4, 8, 12):
    d.line([(0, y), (15, y)], fill=mortar)
for row, y in enumerate((0, 4, 8, 12)):
    offset = 0 if row % 2 == 0 else 4
    for x in range(offset, 16, 8):
        d.line([(x, y), (x, y + 3)], fill=mortar)
save(bricks, BLOCK, "sulthari_sandstone_bricks")

# --- Brass plating with rivets
plating = Image.new("RGBA", (16, 16))
noise_fill(plating, (181, 134, 58, 255), 0.08, 2)
d = ImageDraw.Draw(plating)
d.rectangle([0, 0, 15, 15], outline=(122, 86, 30, 255))
d.line([(0, 7), (15, 7)], fill=(140, 100, 38, 255))
for x, y in ((2, 2), (13, 2), (2, 12), (13, 12)):
    d.point((x, y), fill=(240, 205, 120, 255))
save(plating, BLOCK, "sulthari_brass_plating")

# --- Brass trim (pillar): vertical bands, and its end with a gear
trim = Image.new("RGBA", (16, 16))
noise_fill(trim, (190, 142, 62, 255), 0.06, 3)
d = ImageDraw.Draw(trim)
for x in (0, 15):
    d.line([(x, 0), (x, 15)], fill=(120, 84, 28, 255))
for x in (4, 11):
    d.line([(x, 0), (x, 15)], fill=(226, 190, 104, 255))
save(trim, BLOCK, "sulthari_brass_trim")
trim_end = Image.new("RGBA", (16, 16))
noise_fill(trim_end, (170, 124, 50, 255), 0.05, 4)
d = ImageDraw.Draw(trim_end)
d.ellipse([3, 3, 12, 12], outline=(236, 200, 112, 255))
d.ellipse([6, 6, 9, 9], fill=(122, 86, 30, 255))
for x, y in ((7, 1), (7, 13), (1, 7), (13, 7)):
    d.rectangle([x, y, x + 1, y + 1], fill=(236, 200, 112, 255))
save(trim_end, BLOCK, "sulthari_brass_trim_end")

# --- Blue-white glazed tiles with an eight-point star
tiles = Image.new("RGBA", (16, 16), (236, 240, 244, 255))
d = ImageDraw.Draw(tiles)
blue = (46, 96, 168, 255)
d.rectangle([0, 0, 15, 15], outline=blue)
d.polygon([(8, 2), (10, 6), (14, 8), (10, 10), (8, 14), (6, 10), (2, 8), (6, 6)], fill=(70, 130, 200, 255), outline=blue)
d.point((8, 8), fill=(232, 182, 74, 255))
save(tiles, BLOCK, "sulthari_glazed_tiles")

# --- Aetherium lamp: brass frame around a glowing cyan crystal
lamp = Image.new("RGBA", (16, 16))
noise_fill(lamp, (150, 230, 220, 255), 0.08, 5)
d = ImageDraw.Draw(lamp)
d.rectangle([0, 0, 15, 15], outline=(160, 116, 44, 255))
d.rectangle([1, 1, 14, 14], outline=(200, 156, 70, 255))
d.polygon([(8, 3), (12, 8), (8, 13), (4, 8)], fill=(210, 255, 250, 255))
save(lamp, BLOCK, "sulthari_aetherium_lamp")

# --- Waystone: dark stone with an aetherium rune
ways = Image.new("RGBA", (16, 16))
noise_fill(ways, (70, 72, 84, 255), 0.1, 6)
d = ImageDraw.Draw(ways)
d.line([(8, 2), (8, 13)], fill=(80, 220, 230, 255))
d.line([(5, 5), (11, 5)], fill=(80, 220, 230, 255))
d.line([(5, 10), (11, 10)], fill=(80, 220, 230, 255))
save(ways, BLOCK, "waystone")
ways_top = Image.new("RGBA", (16, 16))
noise_fill(ways_top, (60, 62, 74, 255), 0.1, 8)
ImageDraw.Draw(ways_top).ellipse([4, 4, 11, 11], outline=(80, 220, 230, 255))
save(ways_top, BLOCK, "waystone_top")

# --- Personal Vault: brass-bound strongbox
side = Image.new("RGBA", (16, 16))
noise_fill(side, (84, 58, 40, 255), 0.08, 9)
d = ImageDraw.Draw(side)
d.rectangle([0, 0, 15, 15], outline=(181, 134, 58, 255))
d.line([(0, 5), (15, 5)], fill=(181, 134, 58, 255))
d.rectangle([6, 7, 9, 11], fill=(214, 170, 80, 255))
d.point((7, 9), fill=(40, 30, 20, 255))
save(side, BLOCK, "personal_vault_side")
top = Image.new("RGBA", (16, 16))
noise_fill(top, (92, 64, 44, 255), 0.08, 10)
ImageDraw.Draw(top).rectangle([0, 0, 15, 15], outline=(181, 134, 58, 255))
save(top, BLOCK, "personal_vault_top")
save(top, BLOCK, "personal_vault_bottom")

# --- Return Scroll (item)
scroll = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
d = ImageDraw.Draw(scroll)
d.rectangle([4, 3, 11, 12], fill=(232, 214, 170, 255), outline=(150, 120, 80, 255))
d.rectangle([3, 2, 12, 3], fill=(181, 134, 58, 255))
d.rectangle([3, 12, 12, 13], fill=(181, 134, 58, 255))
for y in (5, 7, 9):
    d.line([(6, y), (10, y)], fill=(120, 96, 150, 255))
d.point((8, 11), fill=(80, 220, 230, 255))
save(scroll, ITEM, "return_scroll")

# --- Bearer outfits: a 64x64 player-skin layer (outer layer areas), mostly transparent.
# Areas follow the vanilla skin layout: body overlay (16..40, 32..48), leg and arm overlays.
OUTFITS = {
    "knight": ((140, 150, 170), (190, 40, 40)),       # steel-grey tabard, red Scale emblem
    "necromancer": ((30, 34, 38), (60, 200, 190)),    # black linen wraps, turquoise trim
    "sorceress": ((90, 50, 150), (232, 182, 74)),     # violet robe, gold stars
    "thief": ((70, 60, 50), (170, 40, 40)),           # dark leather, red sash
    "king": ((150, 30, 30), (232, 182, 74)),          # red kaftan, gold trim
}
for cls, (main, accent) in OUTFITS.items():
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    m, a = main + (255,), accent + (255,)
    # body overlay: front 20..28 x 36..48, back 32..40, sides 16..20 and 28..32
    d.rectangle([16, 36, 39, 47], fill=m)
    d.rectangle([16, 32, 39, 35], fill=(0, 0, 0, 0))
    d.rectangle([20, 32, 27, 35], fill=shade(m, 0.8))  # top of the body
    d.line([(20, 36), (27, 36)], fill=a)
    d.line([(23, 37), (24, 47)], fill=a)  # front trim
    d.line([(20, 44), (27, 44)], fill=a)  # belt
    # right arm overlay (40..56, 32..48): sleeve down to the elbow
    d.rectangle([40, 36, 55, 41], fill=m)
    d.line([(40, 41), (55, 41)], fill=a)
    # left arm overlay (48..64, 48..64)
    d.rectangle([48, 52, 63, 57], fill=m)
    d.line([(48, 57), (63, 57)], fill=a)
    # leg overlays (0..16, 32..48 right; 0..16, 48..64 left): long coat skirt
    d.rectangle([0, 36, 15, 41], fill=m)
    d.rectangle([0, 52, 15, 57], fill=m)
    save(img, OUTFIT, cls)

print("textures written")

# --- Void creatures: 64x64 humanoid skins (zombie/player layout), dark violet with glowing eyes
ENTITY = os.path.join(ROOT, "entity")
VOID = os.path.join(ENTITY, "void")
os.makedirs(VOID, exist_ok=True)
for name, base, eye, seed in (("wretch", (38, 22, 52, 255), (200, 120, 255, 255), 11),
                              ("stalker", (24, 14, 34, 255), (255, 90, 200, 255), 12)):
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    base_img = Image.new("RGBA", (64, 64))
    noise_fill(base_img, base, 0.25, seed)
    # the base (inner) layer areas of the skin layout
    for box in ((0, 0, 32, 16), (16, 16, 40, 32), (40, 16, 56, 32), (0, 16, 16, 32), (16, 48, 32, 64), (32, 48, 48, 64)):
        img.paste(base_img.crop(box), box[:2])
    d = ImageDraw.Draw(img)
    rnd = random.Random(seed)
    for _ in range(14):  # violet cracks
        x, y = rnd.randint(16, 39), rnd.randint(20, 31)
        d.point((x, y), fill=shade(eye, 0.6))
    # face: no mouth, two glowing eyes (head front is x 8..15, y 8..15)
    d.rectangle([8, 8, 15, 15], fill=shade(base, 0.7))
    d.rectangle([9, 11, 10, 12], fill=eye)
    d.rectangle([13, 11, 14, 12], fill=eye)
    save(img, VOID, name)

# --- Brass Sentinel: 128x128, brass plates with rivets, cyan eyes and a gear on the chest; a Void-taken variant
for suffix, plate, glow in (("", (181, 134, 58, 255), (80, 220, 230, 255)), ("_void", (110, 70, 90, 255), (200, 110, 255, 255))):
    img = Image.new("RGBA", (128, 128))
    noise_fill(img, plate, 0.1, 21)
    d = ImageDraw.Draw(img)
    for y in range(0, 128, 8):
        for x in range(0, 128, 8):
            d.point((x + 1, y + 1), fill=shade(plate, 1.4))
    for y in range(0, 128, 16):
        d.line([(0, y), (127, y)], fill=shade(plate, 0.7))
    # head front face (box at texOffs 0,0 size 10x10x10: front is x 10..19, y 10..19)
    d.rectangle([10, 10, 19, 19], fill=shade(plate, 0.8))
    d.rectangle([11, 13, 13, 14], fill=glow)
    d.rectangle([16, 13, 18, 14], fill=glow)
    # chest gear (texOffs 60,0: 6x6x1 box, front at x 61..66, y 1..6)
    d.ellipse([61, 1, 66, 6], fill=shade(plate, 1.25), outline=shade(plate, 0.6))
    d.point((63, 3), fill=glow)
    save(img, ENTITY, "brass_sentinel" + suffix)

print("entity textures written")
