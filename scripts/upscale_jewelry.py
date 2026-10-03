"""Turns the 16x16 jewelry and charm sprites (every item in the Curios ring, necklace and talisman tags) into
32x32 ones: Scale2x and the edge light of upscale_weapons.py, then the gems are cut again (a bright facet on
the upper left of each stone, a dark one on the lower right) and the brightest stone gets a four-point
sparkle. Only sprites still at 16x16 are touched, so it can be run again after make_jewelry.py and
make_uniques.py.

Run from the repository root after the sprite scripts: python scripts/upscale_jewelry.py
"""
import colorsys
import json
import os

from PIL import Image

from upscale_weapons import ITEM, refine, scale2x

TAGS = os.path.join("src", "main", "resources", "data", "curios", "tags", "items")


def ids():
    out = []
    for tag in ("ring", "necklace", "talisman"):
        with open(os.path.join(TAGS, tag + ".json"), encoding="utf-8") as f:
            out += [v.split(":")[1] for v in json.load(f)["values"] if isinstance(v, str) and v.startswith("sofe:")]
    return sorted(set(out))


def stone(c):
    """A gem pixel: saturated and bright enough, not metal (gold is saturated too, so its hue is left out)."""
    if c[3] == 0:
        return False
    h, l, s = colorsys.rgb_to_hls(*(v / 255 for v in c[:3]))
    metal = 0.08 <= h <= 0.16  # gold, brass, bronze
    return s > 0.55 and 0.3 < l < 0.85 and not metal


def facets(img):
    """Recut the stones: within each run of gem pixels, light the upper-left ones and shade the lower-right."""
    w, h = img.size
    px = img.load()
    gem = {(x, y) for y in range(h) for x in range(w) if stone(px[x, y])}
    out = img.copy()
    o = out.load()
    for (x, y) in gem:
        c = px[x, y]
        if (x - 1, y - 1) not in gem and ((x - 1, y) not in gem or (x, y - 1) not in gem):
            o[x, y] = tuple(min(255, int(v + (255 - v) * 0.45)) for v in c[:3]) + (255,)
        elif (x + 1, y + 1) not in gem and ((x + 1, y) not in gem or (x, y + 1) not in gem):
            o[x, y] = tuple(int(v * 0.7) for v in c[:3]) + (255,)
    return out, gem


def sparkle(img, gem):
    """A four-point glint on the brightest stone, inside the sprite and never over the outline."""
    if len(gem) < 3:
        return img
    px = img.load()
    x, y = min(gem, key=lambda p: (-sum(px[p][:3]), p[1], p[0]))
    w, h = img.size
    px[x, y] = (255, 255, 255, 255)
    for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
        q = (x + dx, y + dy)
        if 0 <= q[0] < w and 0 <= q[1] < h and px[q][3] > 0 and sum(px[q][:3]) > 120:
            c = px[q]
            px[q] = tuple(min(255, int(v + (255 - v) * 0.7)) for v in c[:3]) + (255,)
    return img


if __name__ == "__main__":
    done = 0
    for name in ids():
        path = os.path.join(ITEM, name + ".png")
        if not os.path.exists(path):
            continue
        img = Image.open(path).convert("RGBA")
        if img.size != (16, 16):
            continue
        big, gem = facets(refine(scale2x(img)))
        sparkle(big, gem).save(path)
        done += 1
    print("upscaled", done, "jewelry and charm sprites to 32x32")
