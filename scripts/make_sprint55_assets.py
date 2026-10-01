"""Placeholder art for Sprint 5.5: gear (weapons, armor and its worn layers, jewelry, talismans),
Relics, potions, the Bearer's Flask, Dinars, Blueprints, secondary materials, the station blocks, the
Reward Coffer, Runestone and the Curios slot icons. Drawn from code; each file can be replaced alone.

Run from the repository root: python scripts/make_sprint55_assets.py
"""
import os
import random
from PIL import Image, ImageDraw

ROOT = os.path.join("src", "main", "resources", "assets", "sofe", "textures")
ITEM, BLOCK = os.path.join(ROOT, "item"), os.path.join(ROOT, "block")
ARMOR, SLOT = os.path.join(ROOT, "models", "armor"), os.path.join(ROOT, "slot")
for d in (ITEM, BLOCK, ARMOR, SLOT):
    os.makedirs(d, exist_ok=True)

BRASS = ((222, 178, 92), (181, 134, 58), (120, 84, 30))     # light, mid, dark
GLACIAL = ((200, 228, 245), (140, 180, 210), (70, 100, 130))
WOOD = ((120, 84, 50), (90, 60, 36))
GOLD = ((255, 220, 120), (232, 182, 74), (150, 100, 30))


def shade(c, f):
    return tuple(max(0, min(255, int(v * f))) for v in c[:3]) + (255,)


def save(img, folder, name):
    img.save(os.path.join(folder, name + ".png"))


def new():
    return Image.new("RGBA", (16, 16), (0, 0, 0, 0))


def blade(img, metal, length, width=1, guard=GOLD, hilt=WOOD[1]):
    """A diagonal blade from the bottom left (hilt) to the top right."""
    d = ImageDraw.Draw(img)
    for i in range(length):
        x, y = 3 + i, 12 - i
        for w in range(width + 1):
            d.point((x + w, y), fill=metal[1] + (255,))
        d.point((x, y), fill=metal[0] + (255,))
        d.point((x + width + 1, y), fill=metal[2] + (255,))
    d.line([(1, 10), (5, 14)], fill=guard[1] + (255,))       # cross guard
    d.line([(1, 15), (3, 13)], fill=hilt + (255,))           # grip
    d.point((0, 15), fill=guard[2] + (255,))


def staff(img, shaft, head, gem):
    d = ImageDraw.Draw(img)
    d.line([(2, 14), (11, 5)], fill=shaft + (255,))
    d.line([(3, 14), (12, 5)], fill=shade(shaft, 0.7))
    d.ellipse([10, 1, 15, 6], outline=head[1] + (255,))
    d.point((12, 3), fill=gem + (255,))
    d.point((13, 3), fill=gem + (255,))


# --- weapons
img = new(); blade(img, BRASS, 10, 1); ImageDraw.Draw(img).point((13, 1), fill=BRASS[0] + (255,)); save(img, ITEM, "brass_scimitar")
img = new(); blade(img, BRASS, 6, 0); save(img, ITEM, "brass_dagger")
img = new(); blade(img, BRASS, 11, 1); save(img, ITEM, "brass_longsword")
img = new(); staff(img, WOOD[0], BRASS, (90, 200, 240)); save(img, ITEM, "brass_staff")
img = new(); staff(img, WOOD[0], BRASS, (60, 200, 190)); d = ImageDraw.Draw(img); d.line([(10, 3), (15, 3)], fill=BRASS[1] + (255,)); save(img, ITEM, "brass_ankh_rod")
img = new(); blade(img, GLACIAL, 11, 1, guard=GLACIAL); save(img, ITEM, "glacial_iron_sword")
img = new(); d = ImageDraw.Draw(img)
d.line([(2, 14), (12, 4)], fill=WOOD[0] + (255,))
d.polygon([(9, 1), (15, 1), (15, 8), (12, 6), (10, 4)], fill=GLACIAL[1] + (255,), outline=GLACIAL[2] + (255,))
save(img, ITEM, "glacial_iron_greataxe")
img = new(); staff(img, GLACIAL[2], GLACIAL, (200, 240, 255)); save(img, ITEM, "glacial_iron_staff")
for name, metal in (("brass_pickaxe", BRASS), ("glacial_iron_pickaxe", GLACIAL)):
    img = new(); d = ImageDraw.Draw(img)
    d.line([(3, 14), (11, 6)], fill=WOOD[0] + (255,))
    d.arc([3, 0, 16, 13], 200, 340, fill=metal[1] + (255,), width=2)
    save(img, ITEM, name)
# Relics: richer colors and a glow
img = new(); blade(img, ((255, 170, 80), (230, 90, 30), (130, 40, 10)), 11, 1, guard=GOLD); save(img, ITEM, "kaleth_blade")
img = new(); blade(img, ((230, 80, 90), (170, 20, 30), (90, 10, 15)), 7, 0, guard=GOLD); save(img, ITEM, "serath_fang")
img = new(); d = ImageDraw.Draw(img)
d.line([(2, 14), (12, 4)], fill=(50, 30, 25, 255))
d.polygon([(8, 0), (15, 0), (15, 9), (12, 6), (9, 4)], fill=(60, 20, 15, 255), outline=(240, 100, 30, 255))
d.point((13, 3), fill=(255, 200, 80, 255))
save(img, ITEM, "vorath_wrath")


# --- armor items
def armor_icon(metal, kind):
    img = new(); d = ImageDraw.Draw(img)
    m, dark, light = metal[1] + (255,), metal[2] + (255,), metal[0] + (255,)
    if kind == "helmet":
        d.rectangle([3, 4, 12, 10], fill=m, outline=dark); d.rectangle([5, 8, 10, 10], fill=(0, 0, 0, 0)); d.line([(4, 4), (11, 4)], fill=light)
    elif kind == "chestplate":
        d.rectangle([3, 2, 12, 13], fill=m, outline=dark); d.rectangle([6, 2, 9, 4], fill=(0, 0, 0, 0)); d.line([(7, 5), (7, 12)], fill=light)
    elif kind == "leggings":
        d.rectangle([4, 2, 11, 5], fill=m, outline=dark); d.rectangle([4, 5, 6, 14], fill=m, outline=dark); d.rectangle([9, 5, 11, 14], fill=m, outline=dark)
    else:
        d.rectangle([3, 9, 6, 13], fill=m, outline=dark); d.rectangle([9, 9, 12, 13], fill=m, outline=dark)
    return img


for metal_name, metal in (("brass", BRASS), ("glacial_iron", GLACIAL)):
    for kind in ("helmet", "chestplate", "leggings", "boots"):
        save(armor_icon(metal, kind), ITEM, f"{metal_name}_{kind}")


# worn layers (64x32): fill the vanilla armor UV areas with the metal
def armor_layer(metal, layer):
    img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    rnd = random.Random(layer)
    m = metal[1]
    if layer == 1:
        areas = [(0, 0, 32, 16), (16, 16, 40, 32), (40, 16, 56, 32), (0, 16, 16, 32)]   # head, body, arms, legs (boots)
    else:
        areas = [(16, 16, 40, 32), (0, 16, 16, 32)]                                      # waist and legs
    for x0, y0, x1, y1 in areas:
        for x in range(x0, x1):
            for y in range(y0, y1):
                img.putpixel((x, y), shade(m, 1 + rnd.uniform(-0.1, 0.1)))
    if layer == 1:
        d.rectangle([8, 8, 15, 15], fill=(0, 0, 0, 0))                                    # open face
        d.rectangle([9, 8, 14, 9], fill=metal[2] + (255,))
        d.line([(20, 20), (27, 20)], fill=metal[0] + (255,))
    return img


for metal_name, metal in (("brass", BRASS), ("glacial_iron", GLACIAL)):
    armor_layer(metal, 1).save(os.path.join(ARMOR, f"{metal_name}_layer_1.png"))
    armor_layer(metal, 2).save(os.path.join(ARMOR, f"{metal_name}_layer_2.png"))

# --- jewelry and talismans
img = new(); d = ImageDraw.Draw(img); d.arc([3, 1, 12, 10], 0, 180, fill=BRASS[1] + (255,)); d.ellipse([6, 9, 10, 13], fill=(60, 200, 190, 255), outline=BRASS[2] + (255,)); save(img, ITEM, "brass_amulet")
img = new(); d = ImageDraw.Draw(img); d.ellipse([4, 5, 11, 12], outline=BRASS[1] + (255,)); d.ellipse([5, 6, 10, 11], outline=BRASS[0] + (255,)); d.rectangle([7, 3, 8, 5], fill=(200, 60, 60, 255)); save(img, ITEM, "brass_ring")
img = new(); d = ImageDraw.Draw(img); d.polygon([(8, 3), (12, 8), (8, 13), (4, 8)], fill=(150, 120, 200, 255), outline=(80, 60, 120, 255)); save(img, ITEM, "small_talisman")
img = new(); d = ImageDraw.Draw(img); d.rectangle([3, 2, 12, 13], fill=(120, 90, 170, 255), outline=(60, 40, 100, 255)); d.line([(7, 4), (7, 11)], fill=(240, 200, 120, 255)); d.line([(5, 7), (10, 7)], fill=(240, 200, 120, 255)); save(img, ITEM, "large_talisman")

# --- trade, alchemy and materials
img = new(); d = ImageDraw.Draw(img); d.ellipse([3, 3, 12, 12], fill=GOLD[1] + (255,), outline=GOLD[2] + (255,)); d.ellipse([6, 6, 9, 9], outline=GOLD[2] + (255,)); save(img, ITEM, "dinar")
img = new(); d = ImageDraw.Draw(img); d.rectangle([2, 3, 13, 12], fill=(60, 90, 150, 255), outline=(30, 50, 90, 255))
for y in (5, 7, 9):
    d.line([(4, y), (11, y)], fill=(200, 220, 255, 255))
save(img, ITEM, "blueprint")


def flask(liquid):
    img = new(); d = ImageDraw.Draw(img)
    d.rectangle([7, 1, 8, 4], fill=BRASS[1] + (255,))
    d.ellipse([3, 4, 12, 14], fill=(liquid or (210, 220, 230)) + (255 if liquid else 120,), outline=BRASS[2] + (255,))
    d.point((5, 7), fill=(255, 255, 255, 200))
    return img


save(flask(None), ITEM, "brass_flask")
save(flask((200, 30, 50)), ITEM, "minor_pomegranate_elixir")
save(flask((70, 140, 230)), ITEM, "bearers_tonic")
img = flask((240, 200, 90)); ImageDraw.Draw(img).ellipse([2, 3, 13, 15], outline=(80, 220, 230, 255)); save(img, ITEM, "bearers_flask")
img = new(); d = ImageDraw.Draw(img); d.ellipse([3, 4, 12, 13], fill=(170, 30, 40, 255), outline=(100, 15, 20, 255)); d.rectangle([7, 2, 8, 4], fill=(90, 140, 60, 255)); save(img, ITEM, "pomegranate")
img = new(); d = ImageDraw.Draw(img)
for a in ((8, 2), (3, 7), (13, 7), (5, 12), (11, 12)):
    d.line([(8, 8), a], fill=(240, 200, 230, 255), width=2)
d.ellipse([6, 6, 9, 9], fill=(250, 220, 90, 255)); save(img, ITEM, "desert_lotus")
img = new(); d = ImageDraw.Draw(img); d.polygon([(2, 4), (13, 3), (14, 12), (3, 13)], fill=(190, 150, 100, 255), outline=(120, 90, 60, 255)); save(img, ITEM, "dune_leather")
img = new(); d = ImageDraw.Draw(img); d.polygon([(2, 4), (13, 3), (14, 12), (3, 13)], fill=(230, 235, 240, 255), outline=(150, 170, 190, 255)); d.line([(5, 6), (10, 10)], fill=(180, 200, 220, 255)); save(img, ITEM, "frostpelt")
img = new(); d = ImageDraw.Draw(img); rnd = random.Random(9)
for _ in range(30):
    x, y = rnd.randint(4, 11), rnd.randint(6, 13); d.point((x, y), fill=(80 + rnd.randint(0, 40), 50, 110, 255))
save(img, ITEM, "void_ash")


# --- blocks
def noise(base, spread, seed):
    img = Image.new("RGBA", (16, 16)); rnd = random.Random(seed)
    for y in range(16):
        for x in range(16):
            img.putpixel((x, y), shade(base, 1 + rnd.uniform(-spread, spread)))
    return img


img = noise((150, 152, 160), 0.1, 31); d = ImageDraw.Draw(img)
for x, y in ((4, 4), (11, 6), (6, 11)):
    d.point((x, y), fill=(120, 190, 230, 255))
save(img, BLOCK, "runestone")
side = noise((70, 70, 76), 0.1, 32); d = ImageDraw.Draw(side); d.rectangle([0, 0, 15, 3], fill=BRASS[1] + (255,)); d.rectangle([4, 7, 11, 12], fill=(240, 120, 30, 255)); d.rectangle([5, 8, 10, 11], fill=(255, 210, 90, 255))
save(side, BLOCK, "imperial_forge_side")
top = noise((60, 60, 66), 0.08, 33); ImageDraw.Draw(top).rectangle([2, 2, 13, 13], outline=BRASS[1] + (255,)); save(top, BLOCK, "imperial_forge_top")
save(noise((55, 55, 60), 0.08, 34), BLOCK, "imperial_forge_bottom")
side = noise((140, 104, 50), 0.08, 35); d = ImageDraw.Draw(side); d.ellipse([3, 5, 12, 14], fill=(200, 230, 240, 200), outline=BRASS[2] + (255,)); d.rectangle([7, 0, 8, 5], fill=BRASS[1] + (255,))
save(side, BLOCK, "alembic_side")
top = noise((150, 112, 56), 0.08, 36); ImageDraw.Draw(top).ellipse([5, 5, 10, 10], fill=BRASS[0] + (255,)); save(top, BLOCK, "alembic_top")
save(noise((110, 80, 40), 0.08, 37), BLOCK, "alembic_bottom")
side = noise((60, 40, 30), 0.08, 38); d = ImageDraw.Draw(side); d.rectangle([0, 0, 15, 15], outline=GOLD[1] + (255,)); d.line([(0, 5), (15, 5)], fill=GOLD[1] + (255,)); d.rectangle([6, 6, 9, 10], fill=GOLD[0] + (255,))
save(side, BLOCK, "reward_coffer_side")
top = noise((70, 48, 34), 0.08, 39); ImageDraw.Draw(top).rectangle([0, 0, 15, 15], outline=GOLD[1] + (255,)); save(top, BLOCK, "reward_coffer_top")
save(top, BLOCK, "reward_coffer_bottom")

# --- Curios slot icons (empty slot hints)
img = new(); ImageDraw.Draw(img).polygon([(8, 3), (12, 8), (8, 13), (4, 8)], outline=(120, 120, 120, 160)); save(img, SLOT, "talisman")
img = new(); d = ImageDraw.Draw(img); d.rectangle([7, 2, 8, 4], fill=(120, 120, 120, 160)); d.ellipse([4, 4, 11, 13], outline=(120, 120, 120, 160)); save(img, SLOT, "potion_belt")
print("sprint 5.5 assets written")


# --- herbs: wild plants, 4 growth stages per crop, seeds and the sage leaves
HERBS = {
    "mountain_sage": ((120, 160, 120), (170, 150, 220)),     # grey-green leaves, lilac flowers
    "pomegranate": ((70, 130, 50), (190, 30, 45)),           # leaves, red fruit
    "desert_lotus": ((60, 150, 110), (245, 200, 230)),       # leaves, pale pink petals
}


def plant(leaf, flower, stage):
    """A cross-model plant: taller with each stage, flowers or fruit on the last one."""
    img = new(); d = ImageDraw.Draw(img)
    height = [4, 7, 10, 13][stage]
    for x in (4, 8, 11):
        d.line([(x, 15), (x + (1 if x < 8 else -1), 15 - height)], fill=leaf + (255,))
        d.point((x - 1, 15 - height // 2), fill=shade(leaf, 1.2))
        d.point((x + 1, 14 - height // 2), fill=shade(leaf, 0.8))
    if stage == 3:
        for x, y in ((5, 15 - height), (8, 14 - height), (10, 16 - height)):
            d.rectangle([x - 1, y, x, y + 1], fill=flower + (255,))
    return img


for herb, (leaf, flower) in HERBS.items():
    for stage in range(4):
        save(plant(leaf, flower, stage), BLOCK, f"{herb}_crop_stage{stage}")
    save(plant(leaf, flower, 3), BLOCK, f"wild_{herb}")
    seeds = new(); d = ImageDraw.Draw(seeds)
    for x, y in ((5, 6), (9, 5), (7, 9), (10, 10), (4, 11)):
        d.ellipse([x, y, x + 2, y + 2], fill=shade(flower, 0.6), outline=shade(leaf, 0.6))
    save(seeds, ITEM, f"{herb}_seeds")
sage = new(); d = ImageDraw.Draw(sage)
for a in ((4, 12), (11, 12), (8, 3)):
    d.line([(8, 14), a], fill=(130, 170, 130, 255), width=2)
d.point((8, 3), fill=(170, 150, 220, 255))
save(sage, ITEM, "mountain_sage")
print("herb textures written")
