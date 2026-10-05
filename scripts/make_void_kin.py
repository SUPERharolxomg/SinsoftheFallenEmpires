"""The Void Zombie and the Void Skeleton: the vanilla zombie and skeleton textures (read from the Minecraft jar)
turned to Void flesh and black bone with violet veins, and their eyes on a layer of their own that glows in the
dark. Also their registration data: names, loot, and spawns in every region of Aetheris.

Run from the repository root: python scripts/make_void_kin.py
"""
import io
import json
import os
import random
import zipfile

from PIL import Image

JAR = os.path.expanduser(os.path.join("~", ".gradle", "caches", "forge_gradle", "minecraft_repo", "versions", "1.20.1", "client-extra.jar"))
OUT = os.path.join("src", "main", "resources", "assets", "sofe", "textures", "entity", "voidkin")
DATA = os.path.join("src", "main", "resources", "data", "sofe")
LANG = os.path.join("src", "main", "resources", "assets", "sofe", "lang")
VIOLET = (196, 110, 255)


def vanilla(path):
    return Image.open(io.BytesIO(zipfile.ZipFile(JAR).read(path))).convert("RGBA")


def lum(c):
    return (0.299 * c[0] + 0.587 * c[1] + 0.114 * c[2]) / 255


def recolor(img, palette, seed, vein_chance):
    rnd = random.Random(seed)
    px = img.load()
    for y in range(img.height):
        for x in range(img.width):
            c = px[x, y]
            if not c[3]:
                continue
            t = lum(c)
            i = min(len(palette) - 1, int(t * len(palette)))
            px[x, y] = palette[i] + (c[3],)
    for _ in range(int(img.width * img.height * vein_chance)):  # violet veins
        x, y = rnd.randrange(img.width), rnd.randrange(img.height)
        for _ in range(rnd.randint(2, 5)):
            if px[x, y][3]:
                px[x, y] = VIOLET + (255,)
            x = max(0, min(img.width - 1, x + rnd.choice((-1, 0, 1))))
            y = max(0, min(img.height - 1, y + 1))
    return img


def eyes(size, spots):
    img = Image.new("RGBA", size, (0, 0, 0, 0))
    for x, y in spots:
        img.putpixel((x, y), (220, 140, 255, 255))
    return img


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    flesh = [(14, 10, 22), (34, 24, 50), (56, 42, 78), (84, 66, 108), (118, 98, 142)]
    zombie = recolor(vanilla("assets/minecraft/textures/entity/zombie/zombie.png"), flesh, 1, 0.01)
    # the face: dark sockets where the eyes glow (the head's front is 8..16 x 8..16)
    for x, y in ((9, 12), (10, 12), (13, 12), (14, 12)):
        zombie.putpixel((x, y), (8, 4, 14, 255))
    zombie.save(os.path.join(OUT, "void_zombie.png"))
    eyes((64, 64), [(9, 12), (10, 12), (13, 12), (14, 12)]).save(os.path.join(OUT, "void_zombie_eyes.png"))
    bone = [(10, 8, 16), (28, 20, 40), (50, 38, 70), (80, 64, 108), (128, 108, 160)]
    skeleton = recolor(vanilla("assets/minecraft/textures/entity/skeleton/skeleton.png"), bone, 2, 0.006)
    skeleton.save(os.path.join(OUT, "void_skeleton.png"))
    eyes((64, 32), [(9, 12), (10, 12), (13, 12), (14, 12)]).save(os.path.join(OUT, "void_skeleton_eyes.png"))

    regions = ["sulthari_desert", "ashen_wastes", "nordrath_tundra", "nordrath_ice_fields", "nordrath_volcanic_forges",
               "parsivan_gardens", "khemet_valley", "aureum_hills"]
    folder = os.path.join(DATA, "forge", "biome_modifier")
    for mob, weight, lo, hi in (("void_zombie", 60, 2, 4), ("void_skeleton", 45, 1, 3)):
        data = {"type": "forge:add_spawns", "biomes": ["sofe:" + b for b in regions],
                "spawners": [{"type": "sofe:" + mob, "weight": weight, "minCount": lo, "maxCount": hi}]}
        with open(os.path.join(folder, "spawn_" + mob + ".json"), "w", encoding="utf-8") as f:
            f.write(json.dumps(data, indent=2) + "\n")
    loot = {
        "void_zombie": [("minecraft:rotten_flesh", 0, 2), ("sofe:void_ash", 0, 1)],
        "void_skeleton": [("minecraft:bone", 0, 2), ("minecraft:arrow", 0, 2), ("sofe:void_ash", 0, 1)],
    }
    for mob, drops in loot.items():
        pools = [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": item, "functions": [
            {"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": lo, "max": hi}},
            {"function": "minecraft:looting_enchant", "count": {"type": "minecraft:uniform", "min": 0, "max": 1}}]}]}
                 for item, lo, hi in drops]
        with open(os.path.join(DATA, "loot_tables", "entities", mob + ".json"), "w", encoding="utf-8") as f:
            f.write(json.dumps({"type": "minecraft:entity", "pools": pools}, indent=2) + "\n")
    names = {"entity.sofe.void_zombie": ("Void Zombie", "Zombi del Vacío"), "entity.sofe.void_skeleton": ("Void Skeleton", "Esqueleto del Vacío"),
             "item.sofe.void_zombie_spawn_egg": ("Void Zombie Spawn Egg", "Huevo de Zombi del Vacío"),
             "item.sofe.void_skeleton_spawn_egg": ("Void Skeleton Spawn Egg", "Huevo de Esqueleto del Vacío")}
    for f, i in (("en_us", 0), ("es_es", 1)):
        p = os.path.join(LANG, f + ".json")
        d = json.load(open(p, encoding="utf-8"))
        for k, v in names.items():
            d[k] = v[i]
        open(p, "w", encoding="utf-8").write(json.dumps(d, ensure_ascii=False, indent=2) + "\n")
    print("void kin")
