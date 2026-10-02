"""Turns the 16x16 weapon sprites into 32x32 ones with more detail: Scale2x (the pixel-art upscaler that
smooths diagonals without blurring), then a one-pixel outline, a bright edge on the side the light comes
from and a soft shade on the other. Only sprites still at 16x16 are touched, so it can be run again after
the sprite scripts. Minecraft draws any square item texture, so these show at twice the detail.

Run from the repository root after the sprite scripts: python scripts/upscale_weapons.py
"""
import os

from PIL import Image

ITEM = os.path.join("src", "main", "resources", "assets", "sofe", "textures", "item")
WEAPONS = ["brass_scimitar", "brass_dagger", "brass_longsword", "brass_staff", "brass_ankh_rod",
           "glacial_iron_sword", "glacial_iron_greataxe", "glacial_iron_staff", 
           "kaleth_blade", "serath_fang", "vorath_wrath",
           "imperial_halberd", "city_hammer", "war_hammer", "serrated_dagger", "hunting_knife", "jousting_lance",
           "explorer_machete", "war_mace", "chain_sword", "crystal_trident", "shadow_scythe", "battle_greatsword",
           "phlegm_mace", "double_flail", "hook_blade", "gravity_hammer", "vortex_dagger", "obsidian_ritual_dagger",
           "bearded_axe", "seax", "frost_spear", "khopesh", "scarab_sickle", "jackal_glaive", "was_sceptre", "gladius", "legion_pilum",
           "justicar_maul", "scale_blade", "ember_blade", "infernal_greataxe",
           "dunesunder", "widows_kiss", "skaldbreaker", "rimetooth", "jackals_judgement", "stormcaller", "greeds_chain"]
OUTLINE = (20, 14, 18, 255)


def dark(c):
    return c[3] > 0 and sum(c[:3]) < 110


def scale2x(src):
    w, h = src.size
    out = Image.new("RGBA", (w * 2, h * 2))
    px = src.load()

    def at(x, y):
        return px[min(max(x, 0), w - 1), min(max(y, 0), h - 1)]

    for y in range(h):
        for x in range(w):
            p = at(x, y)
            a, b, c, d = at(x, y - 1), at(x + 1, y), at(x - 1, y), at(x, y + 1)
            e0 = c if (c == a and c != d and a != b) else p
            e1 = a if (a == b and a != c and b != d) else p
            e2 = c if (d == c and d != b and c != a) else p
            e3 = b if (b == d and b != a and d != c) else p
            out.putpixel((2 * x, 2 * y), e0)
            out.putpixel((2 * x + 1, 2 * y), e1)
            out.putpixel((2 * x, 2 * y + 1), e2)
            out.putpixel((2 * x + 1, 2 * y + 1), e3)
    return out


def refine(img):
    """Thin the doubled outline to one pixel, then light the upper-left edges and shade the lower-right ones."""
    w, h = img.size
    px = img.load()
    solid = {(x, y) for y in range(h) for x in range(w) if px[x, y][3] > 0 and not dark(px[x, y])}
    out = Image.new("RGBA", (w, h))
    o = out.load()
    for (x, y) in solid:
        c = px[x, y]
        ul = (x - 1, y) not in solid or (x, y - 1) not in solid
        dr = (x + 1, y) not in solid or (x, y + 1) not in solid
        if ul and not dr:
            c = tuple(min(255, int(v * 1.18) + 12) for v in c[:3]) + (255,)
        elif dr and not ul:
            c = tuple(int(v * 0.78) for v in c[:3]) + (255,)
        o[x, y] = c
    for (x, y) in solid:
        for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            q = (x + dx, y + dy)
            if q not in solid and 0 <= q[0] < w and 0 <= q[1] < h:
                o[q] = OUTLINE
    # keep the dark details of the sprite (grips, pupils) that are inside the shape
    for y in range(h):
        for x in range(w):
            if dark(px[x, y]) and o[x, y][3] == 0:
                inside = sum((x + dx, y + dy) in solid for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
                if inside >= 3:
                    o[x, y] = px[x, y]
    return out


if __name__ == "__main__":
    done = 0
    for name in WEAPONS:
        path = os.path.join(ITEM, name + ".png")
        img = Image.open(path).convert("RGBA")
        if img.size != (16, 16):
            continue
        refine(scale2x(img)).save(path)
        done += 1
    print("upscaled", done, "weapon sprites to 32x32")
