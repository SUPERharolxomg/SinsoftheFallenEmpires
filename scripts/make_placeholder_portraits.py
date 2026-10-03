"""Placeholder portraits for every dialogue speaker without a painted one: the face of the skin the NPC wears
(its own, or the default skin SoFEEntityRenderers gives it), large, on a backdrop in its region's colors, in a
frame. The dialogue box uses a painted portrait first, then the live NPC when it stands near, then these.

Run from the repository root: python scripts/make_placeholder_portraits.py
"""
import glob
import io
import json
import os
import re
import zipfile

from PIL import Image, ImageDraw

ASSETS = os.path.join("src", "main", "resources", "assets", "sofe")
OUT = os.path.join(ASSETS, "textures", "gui", "portrait", "placeholder")
JAR = os.path.expanduser(os.path.join("~", ".gradle", "caches", "forge_gradle", "minecraft_repo", "versions", "1.20.1", "client-extra.jar"))
SIZE = 64


def speakers():
    found = set()
    for p in glob.glob(os.path.join("src", "main", "resources", "data", "sofe", "dialogue", "**", "*.json"), recursive=True):
        for line in json.load(open(p, encoding="utf-8"))["lines"]:
            found.add(line.get("speaker", "narrator"))
    return found


def default_skins():
    src = open(os.path.join("src", "main", "java", "com", "sofe", "client", "render", "SoFEEntityRenderers.java"), encoding="utf-8").read()
    return dict(re.findall(r'Map\.entry\("([a-z_]+)", "([a-z]+)"\)', src))


def backdrop(speaker):
    if speaker.startswith("nordrath"):
        return (40, 52, 70), (110, 130, 150)
    if speaker in ("vorath", "unknown_voice"):
        return (40, 10, 10), (150, 40, 20)
    return (90, 50, 30), (200, 150, 80)


def face(skin):
    jar = zipfile.ZipFile(JAR)
    img = Image.open(io.BytesIO(jar.read("assets/minecraft/textures/entity/player/wide/%s.png" % skin))).convert("RGBA")
    head = img.crop((8, 8, 16, 16))
    hat = img.crop((40, 8, 48, 16))
    head.alpha_composite(hat)
    return head


def portrait(speaker, head):
    dark, light = backdrop(speaker)
    out = Image.new("RGBA", (SIZE, SIZE), dark + (255,))
    d = ImageDraw.Draw(out)
    for y in range(SIZE):  # a soft light from above
        t = y / SIZE
        d.line([(0, y), (SIZE, y)], fill=tuple(int(light[i] * (1 - t) * 0.45 + dark[i] * (0.55 + t * 0.45)) for i in range(3)) + (255,))
    if head is not None:
        out.alpha_composite(head.resize((40, 40), Image.NEAREST), (12, 12))
        d.rectangle([12, 52, 51, 63], fill=tuple(int(c * 0.7) for c in light) + (255,))  # the shoulders
    else:  # a voice without a face: a hooded shadow with burning eyes
        d.ellipse([16, 10, 48, 50], fill=(10, 6, 8, 255))
        d.rectangle([12, 40, 52, 63], fill=(10, 6, 8, 255))
        for x in (25, 36):
            d.rectangle([x, 26, x + 3, 28], fill=(255, 110, 40, 255))
    d.rectangle([0, 0, SIZE - 1, SIZE - 1], outline=(30, 20, 12, 255), width=2)
    d.rectangle([2, 2, SIZE - 3, SIZE - 3], outline=light + (255,), width=1)
    out.save(os.path.join(OUT, speaker + ".png"))


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    painted = {f[:-4] for f in os.listdir(os.path.join(ASSETS, "textures", "gui", "portrait")) if f.endswith(".png")}
    skins = default_skins()
    made = 0
    for s in sorted(speakers() - painted - {"player"}):
        own = os.path.join(ASSETS, "textures", "entity", "npc", s + ".png")
        if os.path.exists(own):
            img = Image.open(own).convert("RGBA")
            head = img.crop((8, 8, 16, 16))
            head.alpha_composite(img.crop((40, 8, 48, 16)))
        elif s in ("vorath", "unknown_voice"):
            head = None  # voices without a face
        else:
            head = face(skins.get(s, "steve"))  # the NPC renderer's default too
        portrait(s, head)
        made += 1
    print("placeholder portraits:", made)
