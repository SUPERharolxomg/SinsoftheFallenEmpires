"""Placeholder art for Sprint 5: Nordrath blocks, the Sealed Gate, the Nether ores and items, the
Broken Oaths Kaleth and Serath, and Vorath's GeckoLib model (geometry, animations and texture).
Everything is drawn from code so each file can be replaced by final art on its own.

Run from the repository root: python scripts/make_sprint5_assets.py
"""
import json
import os
import random
from PIL import Image, ImageDraw

ASSETS = os.path.join("src", "main", "resources", "assets", "sofe")
BLOCK = os.path.join(ASSETS, "textures", "block")
ITEM = os.path.join(ASSETS, "textures", "item")
ENTITY = os.path.join(ASSETS, "textures", "entity")
for d in (BLOCK, ITEM, ENTITY, os.path.join(ASSETS, "geo", "entity"), os.path.join(ASSETS, "animations", "entity"),
          os.path.join(ASSETS, "models", "block")):
    os.makedirs(d, exist_ok=True)


def shade(c, f):
    return tuple(max(0, min(255, int(v * f))) for v in c[:3]) + ((c[3],) if len(c) > 3 else (255,))


def noise(size, base, spread, seed):
    img = Image.new("RGBA", size)
    rnd = random.Random(seed)
    px = img.load()
    for y in range(size[1]):
        for x in range(size[0]):
            px[x, y] = shade(base, 1 + rnd.uniform(-spread, spread))
    return img


def save(img, folder, name):
    img.save(os.path.join(folder, name + ".png"))


def bricks(base, mortar, seed, runes=None):
    img = noise((16, 16), base, 0.07, seed)
    d = ImageDraw.Draw(img)
    for y in (0, 5, 10, 15):
        d.line([(0, y), (15, y)], fill=mortar)
    for row, y in enumerate((0, 5, 10)):
        off = 0 if row % 2 == 0 else 4
        for x in range(off, 16, 8):
            d.line([(x, y), (x, y + 4)], fill=mortar)
    if runes:
        for x, y in ((3, 2), (11, 7), (6, 12)):
            d.point((x, y), fill=runes)
            d.point((x + 1, y + 1), fill=runes)
    return img


# --- Nordrath building set
save(bricks((120, 124, 132, 255), (70, 72, 80, 255), 1, runes=(120, 190, 230, 255)), BLOCK, "nordrath_runestone_bricks")
save(bricks((46, 40, 40, 255), (22, 18, 18, 255), 2, runes=(230, 90, 40, 255)), BLOCK, "corrupted_nordrath_runestone_bricks")
timber = noise((16, 16), (62, 44, 32, 255), 0.08, 3)
d = ImageDraw.Draw(timber)
for x in (2, 6, 10, 13):
    d.line([(x, 0), (x, 15)], fill=(44, 30, 22, 255))
save(timber, BLOCK, "nordrath_dark_timber")
end = noise((16, 16), (80, 58, 40, 255), 0.05, 4)
ImageDraw.Draw(end).ellipse([3, 3, 12, 12], outline=(52, 36, 26, 255))
save(end, BLOCK, "nordrath_dark_timber_end")
burning = noise((16, 16), (40, 22, 16, 255), 0.1, 5)
d = ImageDraw.Draw(burning)
rnd = random.Random(5)
for _ in range(10):
    x, y = rnd.randint(0, 15), rnd.randint(0, 15)
    d.line([(x, y), (x, min(15, y + 3))], fill=(240, 110, 30, 255))
save(burning, BLOCK, "corrupted_nordrath_dark_timber")
burning_end = noise((16, 16), (50, 26, 18, 255), 0.1, 6)
ImageDraw.Draw(burning_end).ellipse([3, 3, 12, 12], outline=(240, 110, 30, 255))
save(burning_end, BLOCK, "corrupted_nordrath_dark_timber_end")
planks = noise((16, 16), (70, 50, 36, 255), 0.06, 7)
d = ImageDraw.Draw(planks)
for y in (3, 7, 11, 15):
    d.line([(0, y), (15, y)], fill=(46, 32, 24, 255))
save(planks, BLOCK, "nordrath_dark_planks")
brazier = noise((16, 16), (60, 60, 64, 255), 0.1, 8)
d = ImageDraw.Draw(brazier)
d.rectangle([0, 0, 15, 15], outline=(30, 30, 34, 255))
d.rectangle([3, 3, 12, 8], fill=(250, 150, 40, 255))
d.rectangle([5, 4, 10, 6], fill=(255, 230, 120, 255))
save(brazier, BLOCK, "nordrath_iron_brazier")

# --- Sealed Gate: dark aetherium bars with a violet glow
gate = noise((16, 16), (40, 20, 60, 255), 0.15, 9)
d = ImageDraw.Draw(gate)
for x in (1, 5, 10, 14):
    d.line([(x, 0), (x, 15)], fill=(150, 80, 220, 255))
d.line([(0, 7), (15, 7)], fill=(190, 120, 255, 255))
d.ellipse([6, 5, 9, 9], fill=(220, 180, 255, 255))
save(gate, BLOCK, "sealed_gate")

# --- Nether ores: basalt with embers, soul soil with pale wisps
ember_ore = noise((16, 16), (70, 70, 76, 255), 0.12, 10)
d = ImageDraw.Draw(ember_ore)
for x, y in ((3, 4), (10, 3), (6, 10), (12, 11), (2, 13)):
    d.rectangle([x, y, x + 1, y + 1], fill=(255, 120, 30, 255))
save(ember_ore, BLOCK, "infernal_ember_ore")
soul_ore = noise((16, 16), (78, 60, 48, 255), 0.12, 11)
d = ImageDraw.Draw(soul_ore)
for x, y in ((4, 3), (11, 6), (5, 11), (12, 12)):
    d.rectangle([x, y, x + 1, y + 2], fill=(120, 230, 230, 255))
save(soul_ore, BLOCK, "wailing_soul_ore")

# --- items
ember = Image.new("RGBA", (16, 16))
d = ImageDraw.Draw(ember)
d.polygon([(8, 2), (12, 7), (10, 13), (6, 13), (4, 7)], fill=(230, 90, 20, 255), outline=(120, 30, 10, 255))
d.polygon([(8, 5), (10, 8), (8, 11), (6, 8)], fill=(255, 210, 90, 255))
save(ember, ITEM, "infernal_ember")
soul = Image.new("RGBA", (16, 16))
d = ImageDraw.Draw(soul)
d.ellipse([4, 3, 11, 10], fill=(150, 240, 240, 200))
d.polygon([(5, 9), (10, 9), (8, 14)], fill=(110, 210, 220, 170))
d.point((6, 6), fill=(30, 60, 70, 255))
d.point((9, 6), fill=(30, 60, 70, 255))
save(soul, ITEM, "wailing_soul")
shard = Image.new("RGBA", (16, 16))
d = ImageDraw.Draw(shard)
d.polygon([(7, 1), (11, 5), (9, 15), (5, 10), (4, 4)], fill=(60, 30, 90, 255), outline=(200, 150, 255, 255))
d.line([(7, 3), (8, 12)], fill=(240, 200, 120, 255))
save(shard, ITEM, "codex_shard")


# --- humanoid skins (zombie/player 64x64 layout) for the Broken Oaths
def humanoid(name, body, accent, skin, eye, seed):
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    base = noise((64, 64), body, 0.15, seed)
    for box in ((0, 0, 32, 16), (16, 16, 40, 32), (40, 16, 56, 32), (0, 16, 16, 32), (16, 48, 32, 64), (32, 48, 48, 64)):
        img.paste(base.crop(box), box[:2])
    d = ImageDraw.Draw(img)
    d.rectangle([8, 8, 15, 15], fill=skin)  # face
    d.rectangle([9, 11, 10, 11], fill=eye)
    d.rectangle([13, 11, 14, 11], fill=eye)
    d.rectangle([0, 0, 31, 7], fill=shade(accent, 0.8))  # hair or helm top
    d.line([(20, 20), (27, 31)], fill=accent)  # sash across the chest
    d.line([(20, 26), (27, 26)], fill=accent)
    save(img, ENTITY, name)


humanoid("kaleth", (70, 58, 50, 255), (220, 100, 30, 255), (150, 110, 90, 255), (255, 140, 40, 255), 12)
humanoid("serath", (60, 14, 20, 255), (190, 20, 30, 255), (200, 170, 160, 255), (255, 40, 60, 255), 13)

# --- Vorath: GeckoLib geometry (Bedrock format), animations and a 128x128 texture
W = H = 128


def cube(origin, size, uv, mirror=False):
    c = {"origin": origin, "size": size, "uv": uv}
    if mirror:
        c["mirror"] = True
    return c


bones = [
    {"name": "root", "pivot": [0, 0, 0]},
    {"name": "body", "parent": "root", "pivot": [0, 26, 0], "cubes": [cube([-9, 26, -5], [18, 20, 10], [0, 0])]},
    {"name": "head", "parent": "body", "pivot": [0, 46, 0], "cubes": [cube([-5, 46, -5], [10, 10, 10], [60, 0])]},
    {"name": "horn_left", "parent": "head", "pivot": [4, 54, 0], "cubes": [cube([4, 54, -1], [2, 7, 2], [104, 0])]},
    {"name": "horn_right", "parent": "head", "pivot": [-4, 54, 0], "cubes": [cube([-6, 54, -1], [2, 7, 2], [104, 0], True)]},
    {"name": "left_arm", "parent": "body", "pivot": [12, 44, 0], "cubes": [cube([9, 20, -3], [6, 26, 6], [0, 32])]},
    {"name": "right_arm", "parent": "body", "pivot": [-12, 44, 0], "cubes": [cube([-15, 20, -3], [6, 26, 6], [0, 32], True)]},
    {"name": "left_leg", "parent": "root", "pivot": [4, 26, 0], "cubes": [cube([1, 0, -3], [6, 26, 6], [30, 32])]},
    {"name": "right_leg", "parent": "root", "pivot": [-4, 26, 0], "cubes": [cube([-7, 0, -3], [6, 26, 6], [30, 32], True)]},
]
geo = {"format_version": "1.12.0", "minecraft:geometry": [{
    "description": {"identifier": "geometry.vorath", "texture_width": W, "texture_height": H,
                    "visible_bounds_width": 4, "visible_bounds_height": 5, "visible_bounds_offset": [0, 2, 0]},
    "bones": bones}]}
with open(os.path.join(ASSETS, "geo", "entity", "vorath.geo.json"), "w", newline="\n") as f:
    json.dump(geo, f, indent=2)
    f.write("\n")


def rot(frames):
    return {"rotation": {str(t): v for t, v in frames}}


animations = {"format_version": "1.8.0", "animations": {
    "animation.vorath.idle": {"loop": True, "animation_length": 2.0, "bones": {
        "body": rot([(0.0, [0, 0, 0]), (1.0, [2, 0, 0]), (2.0, [0, 0, 0])]),
        "head": rot([(0.0, [0, 0, 0]), (1.0, [-3, 0, 0]), (2.0, [0, 0, 0])]),
        "left_arm": rot([(0.0, [0, 0, -4]), (1.0, [0, 0, -7]), (2.0, [0, 0, -4])]),
        "right_arm": rot([(0.0, [0, 0, 4]), (1.0, [0, 0, 7]), (2.0, [0, 0, 4])])}},
    "animation.vorath.walk": {"loop": True, "animation_length": 1.2, "bones": {
        "left_leg": rot([(0.0, [25, 0, 0]), (0.6, [-25, 0, 0]), (1.2, [25, 0, 0])]),
        "right_leg": rot([(0.0, [-25, 0, 0]), (0.6, [25, 0, 0]), (1.2, [-25, 0, 0])]),
        "left_arm": rot([(0.0, [-20, 0, -4]), (0.6, [20, 0, -4]), (1.2, [-20, 0, -4])]),
        "right_arm": rot([(0.0, [20, 0, 4]), (0.6, [-20, 0, 4]), (1.2, [20, 0, 4])]),
        "body": rot([(0.0, [4, 0, 0]), (0.6, [6, 0, 0]), (1.2, [4, 0, 0])])}},
    "animation.vorath.attack": {"loop": False, "animation_length": 0.6, "bones": {
        "right_arm": rot([(0.0, [0, 0, 4]), (0.25, [-150, 0, 10]), (0.6, [0, 0, 4])]),
        "left_arm": rot([(0.0, [0, 0, -4]), (0.25, [-150, 0, -10]), (0.6, [0, 0, -4])]),
        "body": rot([(0.0, [0, 0, 0]), (0.25, [-8, 0, 0]), (0.45, [14, 0, 0]), (0.6, [0, 0, 0])])}},
}}
with open(os.path.join(ASSETS, "animations", "entity", "vorath.animation.json"), "w", newline="\n") as f:
    json.dump(animations, f, indent=2)
    f.write("\n")

tex = noise((W, H), (40, 20, 16, 255), 0.2, 14)
d = ImageDraw.Draw(tex)
rnd = random.Random(14)
for _ in range(90):  # lava cracks over the dark armor
    x, y = rnd.randint(0, W - 1), rnd.randint(0, 80)
    d.line([(x, y), (x + rnd.randint(-3, 3), y + rnd.randint(2, 6))], fill=(240, 90, 20, 255))
# face on the head's front (head uv 60,0 with size 10: the front face starts at 70,10)
d.rectangle([70, 10, 79, 19], fill=(30, 12, 10, 255))
d.rectangle([71, 13, 73, 14], fill=(255, 200, 60, 255))
d.rectangle([76, 13, 78, 14], fill=(255, 200, 60, 255))
d.rectangle([72, 17, 77, 17], fill=(255, 90, 20, 255))
# horns
d.rectangle([104, 0, 111, 8], fill=(200, 180, 150, 255))
tex.save(os.path.join(ENTITY, "vorath.png"))

# --- the Sealed Gate model: a full cube, cutout so the bars show the glow behind
model = {"parent": "block/cube_all", "render_type": "minecraft:cutout", "textures": {"all": "sofe:block/sealed_gate"}}
with open(os.path.join(ASSETS, "models", "block", "sealed_gate.json"), "w", newline="\n") as f:
    json.dump(model, f, indent=2)
    f.write("\n")

print("sprint 5 assets written")
