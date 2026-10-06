"""The pickaxes, drawn on the vanilla pickaxe: its head recolored to the material by brightness, so the vanilla
shading survives, and the handle left as vanilla wood. The vanilla texture is read from the Minecraft jar in
the Gradle cache. (The armor is 3D now: scripts/make_armor_models.py.)

Run from the repository root: python scripts/make_pickaxes.py
"""
import io
import os
import zipfile

from PIL import Image

ITEM = os.path.join("src", "main", "resources", "assets", "sofe", "textures", "item")
JAR = os.path.expanduser(os.path.join("~", ".gradle", "caches", "forge_gradle", "minecraft_repo", "versions", "1.20.1",
                                      "client-extra.jar"))

# dark to light
PICKAXES = {
    "brass_pickaxe": [(92, 56, 18), (150, 100, 40), (204, 152, 70), (240, 200, 120), (255, 240, 190)],
    "glacial_iron_pickaxe": [(40, 86, 130), (90, 150, 200), (150, 206, 240), (204, 238, 255), (250, 255, 255)],
}


def mix(a, b, t):
    return tuple(round(a[i] + (b[i] - a[i]) * t) for i in range(3))


def along(ramp, t):
    t = max(0.0, min(1.0, t)) * (len(ramp) - 1)
    i = min(int(t), len(ramp) - 2)
    return mix(ramp[i], ramp[i + 1], t - i)


def lum(c):
    return 0.299 * c[0] + 0.587 * c[1] + 0.114 * c[2]


def pickaxe(name, ramp):
    tool(name, ramp, "pickaxe")


def tool(name, ramp, kind):
    """Any vanilla iron tool (pickaxe, axe, shovel, hoe) with its head recolored."""
    img = Image.open(io.BytesIO(zipfile.ZipFile(JAR).read("assets/minecraft/textures/item/iron_%s.png" % kind))).convert("RGBA")
    px = img.load()
    head = [(x, y) for y in range(16) for x in range(16)
            if px[x, y][3] > 0 and abs(px[x, y][0] - px[x, y][2]) < 12]  # the grays are the head, the browns the stick
    shades = sorted({round(lum(px[x, y])) for (x, y) in head})
    full = [mix((14, 10, 14), ramp[0], 0.35)] + ramp
    for (x, y) in head:
        px[x, y] = along(full, shades.index(round(lum(px[x, y]))) / max(1, len(shades) - 1)) + (255,)
    img.save(os.path.join(ITEM, name + ".png"))


if __name__ == "__main__":
    for n, r in PICKAXES.items():
        pickaxe(n, r)
    print("pickaxes:", len(PICKAXES))
