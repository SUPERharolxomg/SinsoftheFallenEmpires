"""A portrait drawn from a GeckoLib model (the bosses of make_boss_figures.py): the bust seen from the front, every
cube near the head painted with its texture and glow, nearest last, so the face keeps its brow, eyes, horns and
crown. Turned cubes (horns, spikes, halos) are drawn as their turned outline in the average colour of the face
that looks at the viewer. Bone rotations are left out: the creature stands straight for its portrait.

Used by make_temptations.py (boss_face) for the dialogue portraits.
"""
import json
import math
import os

from PIL import Image, ImageDraw

ASSETS = os.path.join("src", "main", "resources", "assets", "sofe")
GEO = os.path.join(ASSETS, "geo", "entity")
TEX = os.path.join(ASSETS, "textures", "entity")


def _rotate(p, pivot, rotation):
    """The point p turned round pivot the way GeckoLib turns a cube: x first, then y, then z (degrees)."""
    x, y, z = (p[i] - pivot[i] for i in range(3))
    rx, ry, rz = (math.radians(a) for a in rotation)
    y, z = y * math.cos(rx) + z * math.sin(rx), -y * math.sin(rx) + z * math.cos(rx)
    x, z = x * math.cos(ry) - z * math.sin(ry), x * math.sin(ry) + z * math.cos(ry)
    x, y = x * math.cos(rz) + y * math.sin(rz), -x * math.sin(rz) + y * math.cos(rz)
    return (x + pivot[0], y + pivot[1], z + pivot[2])


def _faces(cube):
    """The six faces of a cube: corners (in the order of its texture's top-left, top-right, bottom-right,
    bottom-left as seen from outside), outward normal and the face's place in the texture."""
    (ox, oy, oz), (sx, sy, sz) = cube["origin"], cube["size"]
    x0, y0, z0, x1, y1, z1 = ox, oy, oz, ox + sx, oy + sy, oz + sz
    if isinstance(cube["uv"], dict):        # per-face UVs (Model.density)
        r = {k: tuple(f["uv"]) + tuple(f["uv_size"]) for k, f in cube["uv"].items()}
        return [
            ([(x0, y1, z0), (x1, y1, z0), (x1, y0, z0), (x0, y0, z0)], (0, 0, -1), r["north"]),
            ([(x1, y1, z1), (x0, y1, z1), (x0, y0, z1), (x1, y0, z1)], (0, 0, 1), r["south"]),
            ([(x0, y1, z1), (x0, y1, z0), (x0, y0, z0), (x0, y0, z1)], (-1, 0, 0), r["east"]),
            ([(x1, y1, z0), (x1, y1, z1), (x1, y0, z1), (x1, y0, z0)], (1, 0, 0), r["west"]),
            ([(x0, y1, z1), (x1, y1, z1), (x1, y1, z0), (x0, y1, z0)], (0, 1, 0), r["up"]),
            ([(x0, y0, z0), (x1, y0, z0), (x1, y0, z1), (x0, y0, z1)], (0, -1, 0), r["down"]),
        ]
    u, v = cube["uv"]
    w, h, d = (math.floor(a) for a in (sx, sy, sz))
    return [
        ([(x0, y1, z0), (x1, y1, z0), (x1, y0, z0), (x0, y0, z0)], (0, 0, -1), (u + d, v + d, w, h)),       # north, toward the viewer
        ([(x1, y1, z1), (x0, y1, z1), (x0, y0, z1), (x1, y0, z1)], (0, 0, 1), (u + 2 * d + w, v + d, w, h)),
        ([(x0, y1, z1), (x0, y1, z0), (x0, y0, z0), (x0, y0, z1)], (-1, 0, 0), (u, v + d, d, h)),
        ([(x1, y1, z0), (x1, y1, z1), (x1, y0, z1), (x1, y0, z0)], (1, 0, 0), (u + d + w, v + d, d, h)),
        ([(x0, y1, z1), (x1, y1, z1), (x1, y1, z0), (x0, y1, z0)], (0, 1, 0), (u + d, v, w, d)),
        ([(x0, y0, z0), (x1, y0, z0), (x1, y0, z1), (x0, y0, z1)], (0, -1, 0), (u + d + w, v, w, d)),
    ]


def render(name, frame=None, px=8, size_hint=None):
    """The model seen from the front. frame (x0, y0, x1, y1) in model units limits what is drawn (by default the
    head and shoulders); px is the pixels per unit."""
    geo = json.load(open(os.path.join(GEO, name + ".geo.json"), encoding="utf-8"))["minecraft:geometry"][0]
    tex = Image.open(os.path.join(TEX, name + ".png")).convert("RGBA")
    glow_path = os.path.join(TEX, name + "_glowmask.png")
    if os.path.exists(glow_path):
        tex.alpha_composite(Image.open(glow_path).convert("RGBA"))
    bones = {b["name"]: b for b in geo["bones"]}
    if frame is None:
        head = bones.get("head")
        cubes = head.get("cubes", []) if head else []
        if not cubes:
            raise ValueError(name + " has no head")
        skull = max(cubes, key=lambda c: c["size"][0] * c["size"][1] * c["size"][2])   # the face is on the biggest block
        (sx0, sy0, _), (sw, sh, _) = skull["origin"], skull["size"]
        size = max(sw, sh) * 2.4
        cx, cy = sx0 + sw / 2, sy0 + sh * 0.45
        frame = (cx - size / 2, cy - size * 0.55, cx + size / 2, cy + size * 0.45)
    fx0, fy0, fx1, fy1 = frame
    W, H = int((fx1 - fx0) * px), int((fy1 - fy0) * px)
    out = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    draw = ImageDraw.Draw(out)
    to_px = lambda p: ((p[0] - fx0) * px, (fy1 - p[1]) * px)
    faces = []
    def second_phase(bone):  # what breaks out in the second phase is not in the portrait
        while bone:
            if bone["name"].startswith("p2_"):
                return True
            bone = bones.get(bone.get("parent"))
        return False
    for b in geo["bones"]:
        if second_phase(b):
            continue
        for c in b.get("cubes", []):
            rot = c.get("rotation")
            for corners, normal, rect in _faces(c):
                if rot:
                    corners = [_rotate(p, c["pivot"], rot) for p in corners]
                    normal = tuple(n - o for n, o in zip(_rotate(normal, (0, 0, 0), rot), (0, 0, 0)))
                if normal[2] > -0.05:
                    continue
                xs = [p[0] for p in corners]
                ys = [p[1] for p in corners]
                if max(xs) < fx0 or min(xs) > fx1 or max(ys) < fy0 or min(ys) > fy1:
                    continue
                depth = sum(p[2] for p in corners) / 4
                faces.append((depth, corners, normal, rect, rot is not None or normal != (0, 0, -1)))
    faces.sort(key=lambda f: -f[0])                                     # far first
    for depth, corners, normal, rect, turned in faces:
        u, v, w, h = rect
        region = tex.crop((u, v, u + max(1, w), v + max(1, h)))
        if not turned:
            (ax, ay), (bx, by) = to_px(corners[0]), to_px(corners[2])
            tw, th = max(1, round(bx - ax)), max(1, round(by - ay))
            out.alpha_composite(region.resize((tw, th), Image.NEAREST), (round(ax), round(ay)))
            continue
        pixels = [p for p in region.getdata() if p[3] > 0]
        if not pixels:
            continue
        shade = 0.75 + 0.25 * max(0.0, -normal[2])
        color = tuple(int(sum(p[i] for p in pixels) / len(pixels) * shade) for i in range(3)) + (255,)
        draw.polygon([to_px(p) for p in corners], fill=color)
    return out


if __name__ == "__main__":
    import sys
    for n in sys.argv[1:]:
        render(n).save(n + "_portrait_test.png")
