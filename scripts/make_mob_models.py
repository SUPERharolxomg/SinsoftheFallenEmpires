"""GeckoLib models (Blockbench's Bedrock format) for the Void creatures, the Broken Oaths, the Brass
Sentinel and Vorath: geometry with bones, painted textures, glow masks and animations.

Every model is built from bones and cubes; the cubes get box UVs packed automatically, and each
face is painted from its material: a shaded gradient (lighter on top), a darker bevel on the edges,
noise, and the material's own details (seams and rivets on metal, folds and ragged hems on cloth,
glowing cracks on Void flesh and lava rock). Glowing pixels also go to <name>_glowmask.png, which
GeckoLib's AutoGlowingGeoLayer draws at full brightness.

Conventions (Bedrock): 16 units per block, y up, the model faces north (-z). Positive X rotation
tilts a bone forward; negative X raises a hanging arm forward.

Run from the repository root: python scripts/make_mob_models.py
"""
import json
import math
import os
import random

from PIL import Image

ASSETS = os.path.join("src", "main", "resources", "assets", "sofe")
GEO = os.path.join(ASSETS, "geo", "entity")
ANIM = os.path.join(ASSETS, "animations", "entity")
TEX = os.path.join(ASSETS, "textures", "entity")
for folder in (GEO, ANIM, TEX):
    os.makedirs(folder, exist_ok=True)


def mix(a, b, t):
    return tuple(int(round(a[i] + (b[i] - a[i]) * t)) for i in range(3))


def clamp(c):
    return tuple(max(0, min(255, int(v))) for v in c)


# --- materials: (dark, mid, light) plus a style and its options

def mat(dark, mid, light, style="plain", **options):
    return {"pal": (dark, mid, light), "style": style, **options}


class Model:
    def __init__(self, name, tex_width=128, scale=1.0, density=None):
        self.name = name
        self.tex_width = tex_width
        # pixels of texture per unit: None is GeckoLib's box UV (one pixel a unit); 2 paints every face with twice the
        # pixels each way (per-face UVs), so a big creature is not blocky
        self.density = density
        self.bones = []
        self.cubes = []
        self.scale = scale  # a bigger creature: its geometry, and the pixels painted on it, grow together

    def bone(self, name, parent=None, pivot=(0, 0, 0), rotation=None):
        bone = {"name": name, "pivot": [float(v) for v in pivot]}
        if parent:
            bone["parent"] = parent
        if rotation:
            bone["rotation"] = [float(v) for v in rotation]
        self.bones.append(bone)
        return name

    def cube(self, bone, origin, size, material, deco=None, inflate=0.0, mirror_x=False, rotation=None, pivot=None):
        """A box: origin is its lowest corner. deco(painter, faces) adds details after the material. A box may be
        turned on its own (rotation in degrees round pivot, which defaults to its middle): tilted plates, spikes, blades."""
        cube = {"bone": bone, "origin": list(origin), "size": list(size), "mat": material, "deco": deco, "inflate": inflate}
        if rotation and any(rotation):
            cube["rotation"] = list(rotation)
            cube["pivot"] = list(pivot) if pivot else [origin[i] + size[i] / 2 for i in range(3)]
        self.cubes.append(cube)
        if mirror_x:  # the same box on the other side of the model
            mirrored_pivot = None
            if "pivot" in cube:
                mirrored_pivot = (-cube["pivot"][0], cube["pivot"][1], cube["pivot"][2])
            self.cube(bone if not isinstance(mirror_x, str) else mirror_x,
                      (-origin[0] - size[0], origin[1], origin[2]), size, material, deco, inflate,
                      rotation=(rotation[0], -rotation[1], -rotation[2]) if rotation else None, pivot=mirrored_pivot)
        return cube

    # --- UV packing: shelves of boxes sorted by height

    def pack(self):
        boxes = sorted(self.cubes, key=lambda c: -(c["size"][1] + c["size"][2]))
        x = y = shelf = 0
        for c in boxes:
            w, h, d = self.texels(c)
            bw, bh = 2 * (w + d), h + d
            if x + bw > self.tex_width:
                x, y, shelf = 0, y + shelf, 0
            c["uv"] = [x, y]
            x += bw
            shelf = max(shelf, bh)
        height = y + shelf
        self.tex_height = 1 << max(5, math.ceil(math.log2(max(1, height))))

    def texels(self, c):
        """The size of a box in texture pixels (per-face UVs) or as box UV counts it for packing."""
        if self.density:
            return tuple(max(1, int(round(v * self.density))) for v in c["size"])
        return tuple(math.ceil(v) for v in c["size"])

    def face_uvs(self, c):
        """Per-face UVs laid out like a box UV (Blockbench's layout, the one the Painter paints)."""
        u, v = c["uv"]
        w, h, d = self.texels(c)
        return {"north": {"uv": [u + d, v + d], "uv_size": [w, h]}, "east": {"uv": [u, v + d], "uv_size": [d, h]},
                "south": {"uv": [u + 2 * d + w, v + d], "uv_size": [w, h]}, "west": {"uv": [u + d + w, v + d], "uv_size": [d, h]},
                "up": {"uv": [u + d, v], "uv_size": [w, d]}, "down": {"uv": [u + d + w, v], "uv_size": [w, d]}}

    def faces(self, c):
        """Where each face of a box reads its texture. GeckoLib floors the box's size for its box UV, so the faces
        are laid out with the floored size (a face thinner than a pixel still gets one pixel painted)."""
        u, v = c["uv"]
        if self.density:
            w, h, d = self.texels(c)
            return {"up": (u + d, v, w, d), "down": (u + d + w, v, w, d), "east": (u, v + d, d, h),
                    "north": (u + d, v + d, w, h), "west": (u + d + w, v + d, d, h), "south": (u + 2 * d + w, v + d, w, h)}
        fw, fh, fd = (math.floor(v) for v in c["size"])
        w, h, d = max(1, fw), max(1, fh), max(1, fd)
        return {"up": (u + fd, v, w, d), "down": (u + fd + fw, v, w, d), "east": (u, v + fd, d, h),
                "north": (u + fd, v + fd, w, h), "west": (u + fd + fw, v + fd, d, h), "south": (u + 2 * fd + fw, v + fd, w, h)}

    # --- output

    def apply_scale(self):
        s = self.scale
        if s == 1.0:
            return
        for b in self.bones:
            b["pivot"] = [v * s for v in b["pivot"]]
        for c in self.cubes:
            c["origin"] = [v * s for v in c["origin"]]
            c["size"] = [v * s for v in c["size"]]
            c["inflate"] = c["inflate"] * s
            if "pivot" in c:
                c["pivot"] = [v * s for v in c["pivot"]]
        self.scale = 1.0

    def write(self, variants, animations, bounds=(2, 3)):
        self.apply_scale()
        self.pack()
        bones = []
        for b in self.bones:
            out = dict(b)
            cubes = []
            for c in self.cubes:
                if c["bone"] != b["name"]:
                    continue
                cube = {"origin": [float(v) for v in c["origin"]], "size": [float(v) for v in c["size"]],
                        "uv": self.face_uvs(c) if self.density else c["uv"]}
                if c["inflate"]:
                    cube["inflate"] = c["inflate"]
                if "rotation" in c:
                    cube["pivot"] = [float(v) for v in c["pivot"]]
                    cube["rotation"] = [float(v) for v in c["rotation"]]
                cubes.append(cube)
            if cubes:
                out["cubes"] = cubes
            bones.append(out)
        geo = {"format_version": "1.12.0", "minecraft:geometry": [{
            "description": {"identifier": "geometry." + self.name, "texture_width": self.tex_width, "texture_height": self.tex_height,
                            "visible_bounds_width": bounds[0], "visible_bounds_height": bounds[1],
                            "visible_bounds_offset": [0, bounds[1] / 2, 0]},
            "bones": bones}]}
        dump(os.path.join(GEO, self.name + ".geo.json"), geo)
        names = {b["name"] for b in self.bones}
        for value in animations.values():  # a shared animation may move bones this creature does not have
            value["bones"] = {k: v for k, v in value["bones"].items() if k in names}
        dump(os.path.join(ANIM, self.name + ".animation.json"), {"format_version": "1.8.0", "animations": {
            "animation.%s.%s" % (self.name, key): value for key, value in animations.items()}})
        for texture_name, recolor in variants.items():
            painter = Painter(self.tex_width, self.tex_height, recolor)
            for i, c in enumerate(self.cubes):
                painter.paint(c, self.faces(c), seed=i * 7919 + len(self.name))
            painter.save(texture_name)


def dump(path, data):
    with open(path, "w", newline="\n") as f:
        json.dump(data, f, indent=1)
        f.write("\n")


# --- painting

FACE_LIGHT = {"up": 1.12, "north": 1.0, "east": 0.93, "west": 0.93, "south": 0.86, "down": 0.7}


class Painter:
    def __init__(self, width, height, recolor):
        self.img = Image.new("RGBA", (width, height), (0, 0, 0, 0))
        self.glow = Image.new("RGBA", (width, height), (0, 0, 0, 0))
        self.recolor = recolor or (lambda m: m)
        self.variant = recolor is not None  # a recolored texture (the Sentinel's Void phase)

    def put(self, x, y, color, glow=False, alpha=255):
        if 0 <= x < self.img.width and 0 <= y < self.img.height:
            self.img.putpixel((x, y), clamp(color) + (alpha,))
            if glow:
                self.glow.putpixel((x, y), clamp(color) + (255,))
            elif alpha and self.glow.getpixel((x, y))[3]:
                self.glow.putpixel((x, y), (0, 0, 0, 0))

    def clear(self, x, y):
        if 0 <= x < self.img.width and 0 <= y < self.img.height:
            self.img.putpixel((x, y), (0, 0, 0, 0))
            self.glow.putpixel((x, y), (0, 0, 0, 0))

    def at(self, face, dx, dy, color, glow=False):
        x, y, w, h = face
        if 0 <= dx < w and 0 <= dy < h:
            self.put(x + dx, y + dy, color, glow)

    def paint(self, cube, faces, seed):
        m = self.recolor(cube["mat"])
        rnd = random.Random(seed)
        for name, face in faces.items():
            self.face(m, name, face, rnd)
        if cube["deco"]:
            cube["deco"](self, faces, m, rnd)

    def face(self, m, name, face, rnd):
        x0, y0, w, h = face
        dark, mid, light = m["pal"]
        style = m["style"]
        noise = m.get("noise", 10)
        if style == "glow":
            noise = m.get("noise", 0)               # light is smooth
        elif min(w, h) <= 3:
            noise = noise // 4                      # small pieces (fingers, eyes, claws) stay clean, not speckled
        k = FACE_LIGHT[name]
        for py in range(h):
            for px in range(w):
                if name == "up":
                    t = 0.15
                elif name == "down":
                    t = 0.85
                else:  # light at the top, dark at the bottom
                    t = 0.2 + 0.6 * (py / max(1, h - 1))
                base = mix(light, mid, t / 0.5) if t < 0.5 else mix(mid, dark, (t - 0.5) / 0.5)
                c = tuple(v * k for v in base)
                n = rnd.randint(-noise, noise)
                c = (c[0] + n, c[1] + n, c[2] + n)
                glow = False
                if style == "metal":
                    if name not in ("up", "down") and h > 6 and py % m.get("seam", 5) == m.get("seam", 5) - 1:
                        c = mix(c, dark, 0.6)
                    if name not in ("up", "down") and py == 0:
                        c = mix(c, (255, 250, 230), 0.25)
                elif style == "cloth":
                    if (px + m.get("fold", 0)) % 3 == 0:
                        c = mix(c, dark, 0.35)
                elif style == "fur":
                    if (px * 7 + py * 3 + rnd.randint(0, 2)) % 4 == 0:
                        c = mix(c, light, 0.4)
                    elif rnd.random() < 0.25:
                        c = mix(c, dark, 0.5)
                elif style in ("void", "rock", "skin_cracked"):
                    if rnd.random() < 0.08:
                        c = mix(c, light, 0.5)
                elif style == "glow":
                    glow = True
                    c = mix(light, mid, min(1.0, py / max(1, h - 1))) if h > 2 else mid   # bright at the top, never dark
                elif style == "flame":
                    tt = py / max(1, h - 1)
                    c = mix(m.get("hot", (255, 230, 120)), mid, tt)
                    glow = True
                # bevel: a darker rim around each face gives the modelled, Blockbench look
                if m.get("bevel", True) and w >= 3 and h >= 3 and (px in (0, w - 1) or py in (0, h - 1)) and not glow:
                    c = mix(c, dark, 0.45)
                self.put(x0 + px, y0 + py, c, glow)
        # cracks of light over the surface
        cracks = m.get("cracks")
        if cracks and name != "down":
            color, density = cracks
            for _ in range(max(0, int(w * h * density))):
                cx, cy = rnd.randrange(w), rnd.randrange(h)
                for _ in range(rnd.randint(2, 5)):
                    self.put(x0 + cx, y0 + cy, color, glow=True)
                    cx = max(0, min(w - 1, cx + rnd.choice((-1, 0, 1))))
                    cy = max(0, min(h - 1, cy + 1))
        # fractures: long jagged cracks of light with dark lips, branching; a body that is breaking apart
        fracture = m.get("fracture")
        if fracture and name != "down" and w >= 3 and h >= 3:
            color, count = fracture
            n = count * (w * h) / 400.0
            n = int(n) + (1 if rnd.random() < n - int(n) else 0)
            if w * h < 40:
                n = 1 if rnd.random() < count * 0.08 else 0
            for _ in range(n):
                self.crack(x0, y0, w, h, color, rnd, rnd.randint(max(3, h // 2), max(4, (h + w) * 2 // 3)))
        # rivets on metal plates
        if style == "metal" and m.get("rivets") and name not in ("up", "down") and w >= 7 and h >= 6:
            for rx, ry in ((1, 1), (w - 2, 1)):
                self.put(x0 + rx, y0 + ry, mix(light, (255, 255, 255), 0.4))
        # ragged hem on cloth and fur
        if m.get("ragged") and name not in ("up", "down"):
            for px in range(w):
                for py in range(h - rnd.randint(0, m["ragged"]), h):
                    self.clear(x0 + px, y0 + py)

    def crack(self, x0, y0, w, h, color, rnd, length, branch=True):
        """One fracture: it starts at an edge and wanders across, a hot core with a darker lip on each side."""
        side = rnd.randrange(4)
        cx, cy = ((rnd.randrange(w), 0), (rnd.randrange(w), h - 1), (0, rnd.randrange(h)), (w - 1, rnd.randrange(h)))[side]
        dx, dy = ((0, 1), (0, -1), (1, 0), (-1, 0))[side]
        lip = mix(color, (0, 0, 0), 0.75)
        for i in range(length):
            for ox, oy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                px, py = cx + ox, cy + oy
                if 0 <= px < w and 0 <= py < h and not self.glow.getpixel((x0 + px, y0 + py))[3]:
                    self.put(x0 + px, y0 + py, lip)
            hot = mix(color, (255, 255, 255), 0.45) if i % 4 == 0 else color
            self.put(x0 + cx, y0 + cy, hot, glow=True)
            if rnd.random() < 0.45:  # zig-zag
                if dx == 0:
                    cx += rnd.choice((-1, 1))
                else:
                    cy += rnd.choice((-1, 1))
            else:
                cx, cy = cx + dx, cy + dy
            if not (0 <= cx < w and 0 <= cy < h):
                break
            if branch and rnd.random() < 0.05:
                self.crack(x0, y0, w, h, color, rnd, length // 2, branch=False)

    def save(self, name):
        self.img.save(os.path.join(TEX, name + ".png"))
        self.glow.save(os.path.join(TEX, name + "_glowmask.png"))
        print("texture", name, self.img.size)


# --- details shared by several models

def face_slit(color, row, width, gap=1):
    """Two glowing eyes on the front of a head."""
    def deco(p, f, m, rnd):
        x, y, w, h = f["north"]
        left = (w - (width * 2 + gap)) // 2
        for i in range(width):
            p.at(f["north"], left + i, row, color, glow=True)
            p.at(f["north"], left + width + gap + i, row, color, glow=True)
    return deco


def combine(*decos):
    def deco(p, f, m, rnd):
        for d in decos:
            if d:
                d(p, f, m, rnd)
    return deco


def clear_face(*names):
    def deco(p, f, m, rnd):
        for n in names:
            x, y, w, h = f[n]
            for px in range(w):
                for py in range(h):
                    p.clear(x + px, y + py)
    return deco


def clear_front_below(rows):
    """A hood or hair layer: keep the top rows of the front face, open the rest for the face."""
    def deco(p, f, m, rnd):
        x, y, w, h = f["north"]
        for px in range(1, w - 1):
            for py in range(rows, h):
                p.clear(x + px, y + py)
        x, y, w, h = f["down"]
        for px in range(w):
            for py in range(h):
                p.clear(x + px, y + py)
    return deco


def rift(color, x_frac=0.5, top=0, bottom=0, face="north", width=1):
    """A vertical glowing rift down a face."""
    def deco(p, f, m, rnd):
        x, y, w, h = f[face]
        cx = int(w * x_frac)
        for py in range(top, h - bottom):
            jitter = rnd.choice((0, 0, 1, -1)) if py % 2 else 0
            for i in range(width):
                p.at(f[face], cx + jitter + i, py, color, glow=True)
            if rnd.random() < 0.2:
                p.at(f[face], cx + jitter - 1, py, mix(color, m["pal"][1], 0.5), glow=True)
    return deco


def ribs(color, rows):
    def deco(p, f, m, rnd):
        x, y, w, h = f["north"]
        for r in rows:
            for px in range(1, w - 1):
                if px != w // 2:
                    p.at(f["north"], px, r, color)
    return deco


def gear_face(teeth_color, hub_color, glow_hub=True, variant_hub=None):
    """A cog drawn on the front face; outside the teeth it is transparent."""
    def deco(p, f, m, rnd):
        hub = variant_hub if p.variant and variant_hub else hub_color
        teeth = m["pal"][1] if p.variant else teeth_color
        x, y, w, h = f["north"]
        cx, cy, r = (w - 1) / 2, (h - 1) / 2, min(w, h) / 2
        for px in range(w):
            for py in range(h):
                d = math.hypot(px - cx, py - cy)
                angle = math.atan2(py - cy, px - cx)
                tooth = math.cos(angle * 8) > 0.3
                if d > r - (0 if tooth else 1.2):
                    p.clear(x + px, y + py)
                elif d < r * 0.35:
                    p.put(x + px, y + py, hub, glow=glow_hub)
                elif d < r * 0.5 or r * 0.72 < d < r * 0.85:
                    p.put(x + px, y + py, mix(teeth, (0, 0, 0), 0.45))  # the hub ring and the rim
                elif abs(px - cx) < 0.6 or abs(py - cy) < 0.6:
                    p.put(x + px, y + py, mix(teeth, (0, 0, 0), 0.3))  # spokes
                else:
                    p.put(x + px, y + py, mix(teeth, (255, 240, 200), 0.15 if py < cy else 0))
    return deco


def buckle(color):
    def deco(p, f, m, rnd):
        x, y, w, h = f["north"]
        for dx in (-1, 0, 1):
            for dy in range(h):
                if dx == 0 and 0 < dy < h - 1:
                    p.at(f["north"], w // 2 + dx, dy, mix(color, (0, 0, 0), 0.5))
                else:
                    p.at(f["north"], w // 2 + dx, dy, color)
    return deco


# --- animation helpers

def keys(frames):
    return {("%.2f" % t).rstrip("0").rstrip(".") if t else "0.0": [float(v) for v in value] for t, value in frames}


def rot(*frames):
    return {"rotation": keys(frames)}


def rotpos(rframes, pframes):
    return {"rotation": keys(rframes), "position": keys(pframes)}


def anim(length, bones, loop=True):
    return {"loop": loop, "animation_length": length, "bones": bones}


def swing(a, b, period):
    return [(0.0, a), (period / 2, b), (period, a)]


def spin(axis_index, period, steps=4):
    frames = []
    for i in range(steps + 1):
        v = [0, 0, 0]
        v[axis_index] = 360 * i / steps
        frames.append((period * i / steps, v))
    return {"rotation": keys(frames)}


# ======================================================================================
# Void Wretch: a gaunt, hunched shape without a face, long arms ending in claws
# ======================================================================================

VOID_GLOW = (196, 110, 255)
VOID_FLESH = mat((14, 8, 24), (38, 22, 58), (76, 50, 108), "void", cracks=(VOID_GLOW, 0.012), noise=6)
VOID_RAG = mat((10, 6, 14), (26, 18, 34), (52, 40, 62), "cloth", ragged=3, noise=5)
VOID_CLAW = mat((90, 60, 120), (170, 130, 210), (230, 210, 250), "plain", noise=4)


def void_wretch():
    """The bestiary sheet's Void Wretch, a head taller than a man: plates of purple-black chitin over a body of Void
    flesh, a ribcage split open on a burning violet core, a long crested skull with mandibles and a slit of light,
    blades along the forearms, claws as long as a hand, and two rows of shards down the back."""
    from creature_kit import glow_mat, veins, centered, spikes
    m = Model("void_wretch", 128)
    m.bone("root")
    plate = mat((12, 6, 22), (44, 26, 70), (96, 68, 140), "metal", seam=3, cracks=(VOID_GLOW, 0.004), noise=4)
    flesh = mat((10, 6, 16), (30, 18, 46), (60, 40, 86), "void", cracks=(VOID_GLOW, 0.02), noise=6)
    core = glow_mat(VOID_GLOW)
    edge = mat((120, 70, 190), (190, 140, 250), (240, 220, 255), "plain", noise=3)
    for side, sx in (("left", 1), ("right", -1)):
        leg = m.bone(side + "_leg", "root", (3 * sx, 17, 0))
        x = 1 if sx > 0 else -5
        m.cube(leg, (x - 0.3, 10, -2.3), (4.6, 7, 4.6), plate, deco=veins(VOID_GLOW, 0.01))      # the thigh plate
        m.cube(leg, (x + 0.4, 3, -1.6), (3.2, 7, 3.2), flesh)                                     # the shin
        m.cube(leg, (x + 0.6, 4, -2.6), (2.8, 5, 1), plate)                                        # its greave
        m.cube(leg, (x + 1.4, 7.5, -3.6), (1, 1, 2), edge)                                         # a spur at the knee
        m.cube(leg, (x - 0.2, 0, -4.4), (4.4, 3, 6), plate)                                        # a clawed foot
        for cx in (x, x + 1.7, x + 3.4):
            m.cube(leg, (cx, 0, -5.6), (0.8, 1, 1.4), edge)
    body = m.bone("body", "root", (0, 17, 0), rotation=(8, 0, 0))
    m.cube(body, (-3.5, 17, -2), (7, 5, 4), flesh)                                                 # the waist
    m.cube(body, (-4, 18, -2.8), (8, 1, 1), plate)
    m.cube(body, (-4, 20, -2.8), (8, 1, 1), plate)                                                 # the plated belly
    m.cube(body, (-5.5, 22, -3.2), (11, 11, 6.4), plate,
           deco=combine(ribs((150, 80, 230), (3, 5, 7)), veins(VOID_GLOW, 0.008)))                  # the chest
    m.cube(body, (-2.5, 24, -3.8), (5, 5, 1), core,
           deco=centered(["..W..", ".WWW.", "WWWWW", ".WWW.", "..W.."], {"W": (250, 230, 255)}, top=0, glow_chars="W"))  # the open core
    for sx in (1, -1):
        m.cube(body, ((-5.5 if sx < 0 else 3.5), 23, -3.9), (2, 7, 1), plate)                     # the ribs round it
    for sx in (1, -1):                                                                             # great shoulder plates
        m.cube(body, ((4.5 if sx > 0 else -10.5), 29, -4), (6, 5, 8), plate)
        spikes(m, body, ((7.5 if sx > 0 else -7.5), 34, 0), along="z", count=3, length=4, material=edge, size=1.3, spread=2.6)
    for row in (-2, 2):                                                                            # two rows of shards down the back
        for i, y in enumerate((22, 25, 28, 31)):
            m.cube(body, (row - 0.6, y, 3.2), (1.2, 2.2, 2 + i * 0.5), edge)
    neck = m.bone("neck", "body", (0, 33, -1), rotation=(-12, 0, 0))
    m.cube(neck, (-1.5, 32, -2.5), (3, 3, 3), flesh)
    head = m.bone("head", "neck", (0, 34, -1))
    m.cube(head, (-3, 34, -6.5), (6, 7, 7), plate,
           deco=centered(["", "", "WWWWWW", ".W..W."], {"W": VOID_GLOW}, top=0, glow_chars="W"))  # a slit of light
    m.cube(head, (-2.4, 39, -5), (4.8, 4, 8), plate)                                               # the long crest of the skull
    m.cube(head, (-1.6, 42, -3), (3.2, 2, 8), plate)
    m.cube(head, (-0.6, 44, -1), (1.2, 3, 5), edge)
    for sx in (1, -1):                                                                             # mandibles
        m.cube(head, ((1.6 if sx > 0 else -2.6), 32.5, -7.4), (1, 3, 1.4), edge)
    for side, sx in (("left", 1), ("right", -1)):
        arm = m.bone(side + "_arm", "body", (7.5 * sx, 31, 0), rotation=(-8, 0, 8 * -sx))
        x = 6 if sx > 0 else -10
        m.cube(arm, (x, 22, -2), (4, 9, 4), plate)                                                 # the upper arm
        m.cube(arm, (x + 0.5, 12, -1.5), (3, 10, 3), flesh, deco=veins(VOID_GLOW, 0.02))          # the forearm
        m.cube(arm, ((x + 3.4) if sx > 0 else (x - 0.6), 12, -1), (1.2, 11, 2), edge)              # a blade along it
        m.cube(arm, (x, 9, -2), (4, 3, 4), plate)                                                  # the hand
        for i, cx in enumerate((x - 0.2, x + 1.5, x + 3.2)):                                       # three long claws
            m.cube(arm, (cx, 1, -1.6 + i * 0.6), (1, 8, 1), edge)
    anims = {
        "idle": anim(2.6, {
            "body": rotpos(swing([8, 0, 0], [12, 0, 0], 2.6), swing([0, 0, 0], [0, 0.5, 0], 2.6)),
            "neck": rot((0.0, [-12, 0, 0]), (1.0, [-12, 0, 0]), (1.1, [-12, 18, 8]), (1.25, [-12, -12, -6]), (1.4, [-12, 0, 0]), (2.6, [-12, 0, 0])),
            "left_arm": rot(*swing([-8, 0, -8], [0, 0, -14], 2.6)),
            "right_arm": rot(*swing([-8, 0, 8], [0, 0, 14], 2.6))}),
        "walk": anim(1.2, {
            "left_leg": rot(*swing([24, 0, 0], [-24, 0, 0], 1.2)),
            "right_leg": rot(*swing([-24, 0, 0], [24, 0, 0], 1.2)),
            "left_arm": rot(*swing([-28, 0, -8], [16, 0, -8], 1.2)),
            "right_arm": rot(*swing([16, 0, 8], [-28, 0, 8], 1.2)),
            "body": rotpos([(0.0, [10, 0, 3]), (0.6, [10, 0, -3]), (1.2, [10, 0, 3])],
                           [(0.0, [0, 0, 0]), (0.3, [0, -0.7, 0]), (0.6, [0, 0, 0]), (0.9, [0, -0.7, 0]), (1.2, [0, 0, 0])])}),
        "attack": anim(0.6, {
            "left_arm": rot((0.0, [-8, 0, -8]), (0.22, [-160, 0, -30]), (0.38, [35, 0, 0]), (0.6, [-8, 0, -8])),
            "right_arm": rot((0.0, [-8, 0, 8]), (0.27, [-160, 0, 30]), (0.43, [35, 0, 0]), (0.6, [-8, 0, 8])),
            "body": rot((0.0, [8, 0, 0]), (0.22, [-8, 0, 0]), (0.42, [26, 0, 0]), (0.6, [8, 0, 0])),
            "neck": rot((0.0, [-12, 0, 0]), (0.22, [-40, 0, 0]), (0.6, [-12, 0, 0]))}, loop=False),
    }
    m.write({"void_wretch": None}, anims, (2.4, 3.2))


# ======================================================================================
# Void Stalker: a low, long hunter that leaps; spine shards, mandibles and a whipping tail
# ======================================================================================

def void_stalker():
    """The bestiary sheet's Void Stalker: a long beast of black chitin and violet light, a crest of shards down its
    back, a long skull with one burning eye and jaws full of fangs, a spiked tail."""
    from creature_kit import glow_mat, veins, spikes
    m = Model("void_stalker", 128)
    m.bone("root")
    chitin = mat((10, 6, 18), (34, 20, 54), (72, 50, 104), "metal", seam=3, cracks=(VOID_GLOW, 0.008), noise=5)
    fang = mat((150, 120, 190), (210, 190, 240), (250, 240, 255), "plain", noise=3)
    eye = glow_mat(VOID_GLOW)
    body = m.bone("body", "root", (0, 12, 0))
    m.cube(body, (-4.5, 9, -9), (9, 8, 18), chitin, deco=veins(VOID_GLOW, 0.012))
    m.cube(body, (-5, 10, -10), (10, 8, 6), chitin)                                         # the shoulders
    spikes(m, body, (0, 17, -6), along="z", count=6, length=5, material=VOID_CLAW, size=1.4, spread=3)
    head = m.bone("head", "body", (0, 14, -10))
    m.cube(head, (-3, 11, -19), (6, 6, 9), chitin)
    m.cube(head, (-1, 15.5, -17), (2, 1, 2), eye)                                           # one burning eye
    m.cube(head, (-2.5, 11, -22), (5, 3, 3), chitin)
    for sx in (1, -1):
        m.cube(head, ((2 if sx > 0 else -3), 10, -21.5), (1, 2, 1), fang)
        m.cube(head, ((1 if sx > 0 else -2), 10, -19.5), (1, 2, 1), fang)
    jaw = m.bone("jaw", "head", (0, 11, -11))
    m.cube(jaw, (-2.5, 9, -21), (5, 2, 9), chitin)
    for i in range(4):
        m.cube(jaw, (-2 + i * 1.3, 11, -20.5), (0.8, 1.6, 0.8), fang)
    tail = m.bone("tail", "body", (0, 14, 9), rotation=(-20, 0, 0))
    m.cube(tail, (-1.5, 12.5, 9), (3, 3, 9), chitin)
    tip = m.bone("tail_tip", "tail", (0, 14, 18), rotation=(15, 0, 0))
    m.cube(tip, (-1, 13, 18), (2, 2, 8), chitin)
    spikes(m, tip, (0, 15, 21), along="z", count=3, length=2.5, material=VOID_CLAW, size=1, spread=2.5)
    m.cube(tip, (-1.5, 12.5, 26), (3, 3, 3), eye)
    for name, x, z in (("front_left_leg", 3.5, -8), ("front_right_leg", -6.5, -8), ("back_left_leg", 3.5, 5), ("back_right_leg", -6.5, 5)):
        leg = m.bone(name, "root", (x + 1.5, 12, z + 1.5))
        m.cube(leg, (x, 5, z), (3, 7, 3), chitin)
        m.cube(leg, (x + 0.5, 0, z - 1), (2, 5, 2), VOID_CLAW)
        m.cube(leg, (x, 0, z - 2.5), (3, 1, 2), VOID_CLAW)
    anims = {
        "idle": anim(2.0, {
            "body": rotpos(swing([0, 0, 0], [-2, 0, 0], 2.0), swing([0, 0, 0], [0, 0.4, 0], 2.0)),
            "tail": rot(*swing([-20, -15, 0], [-20, 15, 0], 2.0)),
            "tail_tip": rot(*swing([15, 20, 0], [15, -20, 0], 2.0)),
            "jaw": rot((0.0, [0, 0, 0]), (1.4, [0, 0, 0]), (1.5, [25, 0, 0]), (1.6, [0, 0, 0]), (2.0, [0, 0, 0]))}),
        "walk": anim(0.6, {
            "front_left_leg": rot(*swing([35, 0, 0], [-35, 0, 0], 0.6)),
            "back_right_leg": rot(*swing([35, 0, 0], [-35, 0, 0], 0.6)),
            "front_right_leg": rot(*swing([-35, 0, 0], [35, 0, 0], 0.6)),
            "back_left_leg": rot(*swing([-35, 0, 0], [35, 0, 0], 0.6)),
            "tail": rot(*swing([-10, -20, 0], [-10, 20, 0], 0.6)),
            "body": rotpos(swing([0, 0, 2], [0, 0, -2], 0.6), swing([0, 0, 0], [0, 0.8, 0], 0.3))}),
        "attack": anim(0.5, {
            "body": rotpos([(0.0, [0, 0, 0]), (0.15, [-14, 0, 0]), (0.3, [16, 0, 0]), (0.5, [0, 0, 0])],
                           [(0.0, [0, 0, 0]), (0.15, [0, 0, 2]), (0.3, [0, 0, -4]), (0.5, [0, 0, 0])]),
            "jaw": rot((0.0, [0, 0, 0]), (0.15, [45, 0, 0]), (0.3, [-5, 0, 0]), (0.5, [0, 0, 0])),
            "tail": rot((0.0, [0, 0, 0]), (0.2, [-50, 0, 0]), (0.5, [0, 0, 0]))}, loop=False),
    }
    m.write({"void_stalker": None}, anims, (2.4, 1.8))


# ======================================================================================
# Humanoid helpers for the bosses
# ======================================================================================

def humanoid_walk(period, legs=30, arms=24, body=None, extra=None):
    bones = {
        "left_leg": rot(*swing([legs, 0, 0], [-legs, 0, 0], period)),
        "right_leg": rot(*swing([-legs, 0, 0], [legs, 0, 0], period)),
        "left_arm": rot(*swing([-arms, 0, 0], [arms, 0, 0], period)),
        "right_arm": rot(*swing([arms, 0, 0], [-arms, 0, 0], period)),
    }
    if body:
        bones["body"] = body
    bones.update(extra or {})
    return anim(period, bones)


# ======================================================================================
# Kaleth, the Burning Blade: a Norse oath-breaker in blackened iron, fire in the cracks
# ======================================================================================

FIRE = (255, 140, 40)
IRON = mat((26, 22, 24), (58, 52, 54), (104, 96, 94), "metal", rivets=True, noise=6)
IRON_BURNT = mat((26, 22, 24), (58, 52, 54), (104, 96, 94), "metal", cracks=(FIRE, 0.01), noise=6)
LEATHER = mat((36, 22, 14), (70, 44, 28), (106, 72, 46), "cloth", noise=6)
FUR = mat((44, 34, 26), (88, 70, 52), (138, 116, 90), "fur", noise=10)
FUR_GREY = mat((70, 66, 62), (120, 114, 108), (176, 170, 160), "fur", noise=10)
ASH_SKIN = mat((70, 60, 56), (112, 98, 92), (150, 136, 128), "plain", noise=6)
CAPE_RED = mat((40, 8, 8), (90, 18, 16), (140, 36, 28), "cloth", ragged=4, cracks=(FIRE, 0.004), noise=5)
GOLD = mat((110, 74, 20), (190, 140, 48), (246, 210, 110), "metal", noise=4)
BLADE_FIRE = mat((200, 60, 10), (255, 140, 40), (255, 230, 140), "flame")
IVORY = mat((120, 108, 86), (190, 178, 150), (236, 228, 204), "plain", noise=6)


def kaleth():
    m = Model("kaleth", 128, scale=1.2)
    m.bone("root")
    for side, sx in (("left", 1), ("right", -1)):
        leg = m.bone(side + "_leg", "root", (2.5 * sx, 18, 0))
        x = 0.5 if sx > 0 else -4.5
        m.cube(leg, (x, 3, -2), (4, 15, 4), LEATHER)
        m.cube(leg, (x - 0.25, 9, -2.6), (4.5, 3, 1), IRON)
        m.cube(leg, (x - 0.5, 0, -2.5), (5, 4, 5), IRON)
    body = m.bone("body", "root", (0, 18, 0))
    m.cube(body, (-5, 12, -3), (10, 6, 6), mat((36, 22, 14), (70, 44, 28), (106, 72, 46), "cloth", ragged=2), inflate=0.3)
    m.cube(body, (-4.5, 17, -2.5), (9, 3, 5), LEATHER, deco=buckle(GOLD["pal"][2]))
    m.cube(body, (-5, 20, -3), (10, 11, 6), IRON_BURNT, deco=rift(FIRE, 0.4, 1, 2, width=1))
    m.cube(body, (-6, 29, -3.5), (12, 3, 7), FUR, inflate=0.3)
    red_fur = mat((60, 6, 6), (130, 20, 16), (190, 60, 40), "fur", ragged=3, cracks=(FIRE, 0.006), noise=10)
    m.cube(body, (-7.5, 27, -4.5), (15, 6, 9), red_fur, inflate=0.4)                         # the mantle of red fur
    cape = m.bone("cape", "body", (0, 30, 3.5))
    m.cube(cape, (-5, 11, 3.5), (10, 19, 1), CAPE_RED)
    head = m.bone("head", "body", (0, 31, 0))
    m.cube(head, (-3.5, 31, -3.5), (7, 7, 7), ASH_SKIN, deco=face_slit(FIRE, 3, 2, 1))
    m.cube(head, (-4, 33, -4), (8, 6, 8), IRON, inflate=0.2,
           deco=combine(clear_front_below(2), lambda p, f, mm, r: [p.at(f["north"], f["north"][2] // 2, y, mm["pal"][2]) for y in range(2, 5)]))
    m.cube(head, (-0.5, 39, -4.5), (1, 2, 9), GOLD)
    m.cube(head, (-3, 28, -4.6), (6, 4, 1), FUR_GREY, deco=lambda p, f, mm, r: [p.clear(f["north"][0] + x, f["north"][1] + 3) for x in (0, 5)])
    for sx in (1, -1):
        m.cube(head, (4 if sx > 0 else -6, 36, -1), (2, 2, 2), IVORY)
        m.cube(head, (5 if sx > 0 else -7, 38, -1), (2, 3, 2), IVORY)
    for side, sx in (("left", 1), ("right", -1)):
        arm = m.bone(side + "_arm", "body", (6 * sx, 29, 0))
        x = 5 if sx > 0 else -8
        m.cube(arm, (x - 0.5 if sx > 0 else x - 0.5, 26, -3), (4, 5, 6), IRON)
        m.cube(arm, (x, 18, -2), (3, 9, 4), LEATHER)
        m.cube(arm, (x - 0.5, 15, -2.5), (4, 4, 5), IRON)
    sword = m.bone("sword", "right_arm", (-6.5, 16.5, 0), rotation=(55, 0, 0))
    m.cube(sword, (-7, 15.5, -1.5), (1, 2, 3), LEATHER)                  # grip
    m.cube(sword, (-8, 15, -2.5), (3, 3, 1), GOLD)                      # guard
    m.cube(sword, (-7, 15.5, -17.5), (1, 2, 15), BLADE_FIRE)            # the burning blade
    anims = {
        "idle": anim(2.4, {
            "body": rot(*swing([0, 0, 0], [2, 0, 0], 2.4)),
            "cape": rot(*swing([6, 0, 0], [10, 0, 0], 2.4)),
            "left_arm": rot(*swing([0, 0, -3], [0, 0, -6], 2.4)),
            "right_arm": rot(*swing([-10, 0, 3], [-14, 0, 6], 2.4))}),
        "walk": humanoid_walk(1.1, 28, 20, extra={"cape": rot(*swing([18, 0, 0], [26, 0, 0], 0.55))}),
        "attack": anim(0.7, {
            "right_arm": rot((0.0, [-10, 0, 0]), (0.25, [-160, 0, 15]), (0.45, [10, 0, -10]), (0.7, [-10, 0, 0])),
            "body": rot((0.0, [0, 0, 0]), (0.25, [-8, 15, 0]), (0.45, [12, -20, 0]), (0.7, [0, 0, 0])),
            "cape": rot((0.0, [8, 0, 0]), (0.45, [30, 0, 0]), (0.7, [8, 0, 0]))}, loop=False),
    }
    m.write({"kaleth": None}, anims, (2, 3))


# ======================================================================================
# Serath, the Blood Maiden: a pale shield-maiden in crimson leather with two bone fangs
# ======================================================================================

BLOOD = (230, 30, 50)
CRIMSON = mat((40, 6, 12), (92, 16, 26), (148, 34, 44), "cloth", noise=5)
CRIMSON_RAG = mat((40, 6, 12), (92, 16, 26), (148, 34, 44), "cloth", ragged=4, noise=5)
PALE = mat((150, 130, 128), (196, 178, 172), (230, 216, 210), "plain", noise=4)
HAIR = mat((50, 6, 10), (100, 18, 24), (150, 40, 44), "fur", noise=8)
BONE = mat((120, 108, 92), (186, 174, 152), (232, 224, 204), "plain", noise=5)
FANG = mat((120, 10, 20), (200, 30, 44), (255, 120, 120), "glow")


def serath_face(p, f, m, rnd):
    face_slit(BLOOD, 3, 2, 1)(p, f, m, rnd)
    w = f["north"][2]
    for px in range(1, w - 1):  # a band of blood paint across the eyes
        if px not in (1, 2, w - 3, w - 2):
            p.at(f["north"], px, 3, (120, 14, 24))
    p.at(f["north"], w // 2, 5, (90, 40, 44))
    p.at(f["north"], w // 2 - 1, 5, (90, 40, 44))


def serath():
    m = Model("serath", 128, scale=1.15)
    m.bone("root")
    for side, sx in (("left", 1), ("right", -1)):
        leg = m.bone(side + "_leg", "root", (2 * sx, 17, 0))
        x = 0.5 if sx > 0 else -3.5
        m.cube(leg, (x, 3, -1.5), (3, 14, 3), CRIMSON)
        m.cube(leg, (x - 0.5, 0, -2), (4, 5, 4), LEATHER, deco=lambda p, f, mm, r: [p.at(f["north"], x2, 2, BONE["pal"][2]) for x2 in range(4)])
    body = m.bone("body", "root", (0, 17, 0))
    m.cube(body, (-4, 10, -2.5), (8, 7, 5), CRIMSON_RAG, inflate=0.3)
    m.cube(body, (-3.5, 17, -2), (7, 10, 4), LEATHER,
           deco=lambda p, f, mm, r: [p.at(f["north"], 3 + (y % 2), y, BLOOD, glow=y in (3, 4)) for y in range(1, 9)])
    m.cube(body, (-4.5, 25, -2.5), (9, 2, 5), BONE)
    for sx in (1, -1):
        m.cube(body, (3.5 if sx > 0 else -5.5, 26, -1), (2, 2, 2), BONE)
    cloak = m.bone("cloak", "body", (0, 26, 2))
    m.cube(cloak, (-4, 9, 2), (8, 17, 1), CRIMSON_RAG)
    head = m.bone("head", "body", (0, 27, 0))
    m.cube(head, (-3.5, 27, -3.5), (7, 7, 7), PALE, deco=serath_face)
    m.cube(head, (-3.5, 27, -3.5), (7, 7, 7), HAIR, inflate=0.5, deco=clear_front_below(2))
    m.cube(head, (-4, 14, 2.5), (8, 14, 1.6), HAIR)                                            # a mane of red hair down her back
    for sx in (1, -1):
        braid = m.bone("braid_" + ("left" if sx > 0 else "right"), "head", (2.5 * sx, 28, 2), rotation=(12, 0, 0))
        m.cube(braid, (2 if sx > 0 else -4, 19, 1.5), (2, 9, 2), HAIR)
        m.cube(braid, (2 if sx > 0 else -4, 18, 1.5), (2, 1, 2), BONE)
    for side, sx in (("left", 1), ("right", -1)):
        arm = m.bone(side + "_arm", "body", (5 * sx, 26, 0))
        x = 3.5 if sx > 0 else -6.5
        m.cube(arm, (x, 16, -1.5), (3, 10, 3), PALE)
        m.cube(arm, (x - 0.5, 16, -2), (4, 4, 4), LEATHER)
        m.cube(arm, (x - 0.5, 24, -2), (4, 2, 4), BONE)
        blade = m.bone(side + "_fang", side + "_arm", (x + 1.5, 15, 0), rotation=(70, 0, 0))
        m.cube(blade, (x + 1, 14, -1.5), (1, 2, 2), BONE)
        m.cube(blade, (x + 1, 14.5, -9.5), (1, 1, 8), FANG)
    anims = {
        "idle": anim(2.0, {
            "body": rot(*swing([0, 0, 0], [0, 4, 2], 2.0)),
            "cloak": rot(*swing([5, 0, 0], [9, 0, 0], 2.0)),
            "left_arm": rot(*swing([-15, 0, -8], [-20, 0, -12], 2.0)),
            "right_arm": rot(*swing([-15, 0, 8], [-20, 0, 12], 2.0)),
            "braid_left": rot(*swing([10, 0, 0], [16, 0, 0], 2.0)),
            "braid_right": rot(*swing([16, 0, 0], [10, 0, 0], 2.0))}),
        "walk": humanoid_walk(0.9, 32, 28, extra={"cloak": rot(*swing([20, 0, 0], [30, 0, 0], 0.45)),
                                                 "braid_left": rot(*swing([25, 0, 0], [35, 0, 0], 0.45)),
                                                 "braid_right": rot(*swing([35, 0, 0], [25, 0, 0], 0.45))}),
        "attack": anim(0.6, {
            "right_arm": rot((0.0, [-15, 0, 0]), (0.15, [-120, 0, 30]), (0.3, [0, 0, -20]), (0.6, [-15, 0, 0])),
            "left_arm": rot((0.0, [-15, 0, 0]), (0.25, [-15, 0, 0]), (0.4, [-120, 0, -30]), (0.55, [0, 0, 20]), (0.6, [-15, 0, 0])),
            "body": rot((0.0, [0, 0, 0]), (0.15, [0, 20, 0]), (0.4, [0, -20, 0]), (0.6, [0, 0, 0]))}, loop=False),
    }
    m.write({"serath": None}, anims, (2, 3))


# ======================================================================================
# The Brass Sentinel: a clockwork guardian of Sulthari, a cog turning in its chest
# ======================================================================================

TURQ = (90, 236, 214)
BRASS = mat((108, 68, 22), (176, 124, 48), (232, 188, 98), "metal", rivets=True, seam=6, noise=6)
BRASS_PLAIN = mat((108, 68, 22), (176, 124, 48), (232, 188, 98), "metal", noise=5)
IRON_FRAME = mat((40, 36, 34), (74, 68, 62), (118, 110, 100), "metal", seam=4, noise=5)
COPPER = mat((86, 40, 22), (156, 82, 46), (214, 132, 84), "metal", rivets=True, noise=6)
CORE = mat((20, 120, 110), (60, 200, 190), (180, 255, 240), "glow")


def sentinel_void(m):
    """The second phase: the crack corrupts the metal and the light turns violet."""
    dark, mid, light = m["pal"]
    out = dict(m)
    tint = (60, 20, 90)
    out["pal"] = tuple(mix(c, tint, 0.45) for c in (dark, mid, light))
    if m["style"] == "glow":
        out["pal"] = ((90, 30, 140), (170, 80, 240), (230, 180, 255))
    elif m["style"] == "metal":
        out["cracks"] = (VOID_GLOW, 0.012)
    return out


def sentinel_visor(p, f, m, rnd):
    x, y, w, h = f["north"]
    glow = VOID_GLOW if p.variant else TURQ
    for px in range(1, w - 1):
        p.at(f["north"], px, 3, glow, glow=True)
    for px in range(1, w - 1):
        p.at(f["north"], px, 2, mix(m["pal"][0], (0, 0, 0), 0.4))
        p.at(f["north"], px, 4, mix(m["pal"][0], (0, 0, 0), 0.4))


def brass_sentinel():
    m = Model("brass_sentinel", 256, scale=1.3)
    m.bone("root")
    for side, sx in (("left", 1), ("right", -1)):
        leg = m.bone(side + "_leg", "root", (5 * sx, 16, 0))
        x = lambda a, w: a if sx > 0 else -a - w
        m.cube(leg, (x(3, 4), 10, -2), (4, 6, 4), IRON_FRAME)
        m.cube(leg, (x(2.5, 5), 7, -2.5), (5, 3, 5), BRASS_PLAIN)
        m.cube(leg, (x(2, 6), 2, -3), (6, 6, 6), BRASS)
        m.cube(leg, (x(1.5, 7), 0, -4.5), (7, 2, 8), IRON_FRAME)
    body = m.bone("body", "root", (0, 16, 0))
    m.cube(body, (-6, 14, -4), (12, 4, 8), IRON_FRAME)
    m.cube(body, (-4, 18, -3), (8, 5, 6), COPPER)
    m.cube(body, (-9, 23, -6), (18, 13, 11), BRASS)
    m.cube(body, (-6, 30, 5), (3, 9, 3), COPPER, deco=lambda p, f, mm, r: [p.at(f["up"], a, b, (20, 16, 14)) for a in (1,) for b in (1,)])
    m.cube(body, (3, 30, 5), (3, 9, 3), COPPER, deco=lambda p, f, mm, r: [p.at(f["up"], a, b, (20, 16, 14)) for a in (1,) for b in (1,)])
    m.cube(body, (-3, 27, -6.8), (6, 6, 1.2), CORE)                                            # the great aetherium core
    gear = m.bone("chest_gear", "body", (0, 31, -7))
    m.cube(gear, (-5, 26, -7.5), (10, 10, 1), BRASS_PLAIN, deco=gear_face((214, 170, 80), TURQ, variant_hub=VOID_GLOW))
    head = m.bone("head", "body", (0, 36, -1))
    m.cube(head, (-4, 36, -5), (8, 7, 8), BRASS, deco=sentinel_visor)
    m.cube(head, (-3, 43, -4), (6, 1, 6), BRASS_PLAIN)
    m.cube(head, (-0.5, 44, -1.5), (1, 3, 1), IRON_FRAME)
    m.cube(head, (-1, 47, -2), (2, 1, 2), CORE)
    for side, sx in (("left", 1), ("right", -1)):
        arm = m.bone(side + "_arm", "body", (10 * sx, 34, 0))
        x = lambda a, w: a if sx > 0 else -a - w
        m.cube(arm, (x(8, 7), 31, -4.5), (7, 6, 9), BRASS)
        m.cube(arm, (x(9.5, 4), 22, -2), (4, 9, 4), IRON_FRAME)
        m.cube(arm, (x(8.5, 6), 12, -3.5), (6, 10, 7), BRASS)
        m.cube(arm, (x(9, 5), 8, -3), (5, 4, 6), IRON_FRAME)
    gear_spin = spin(2, 4.0)
    anims = {
        "idle": anim(4.0, {
            "chest_gear": gear_spin,
            "body": rot((0.0, [0, 0, 0]), (2.0, [1.5, 0, 0]), (4.0, [0, 0, 0])),
            "head": rot((0.0, [0, 0, 0]), (2.6, [0, 0, 0]), (2.8, [0, 12, 0]), (3.4, [0, 12, 0]), (3.6, [0, 0, 0]), (4.0, [0, 0, 0]))}),
        "walk": anim(2.0, {
            "chest_gear": spin(2, 2.0),
            "left_leg": rot(*swing([20, 0, 0], [-20, 0, 0], 2.0)),
            "right_leg": rot(*swing([-20, 0, 0], [20, 0, 0], 2.0)),
            "left_arm": rot(*swing([-14, 0, 0], [14, 0, 0], 2.0)),
            "right_arm": rot(*swing([14, 0, 0], [-14, 0, 0], 2.0)),
            "body": rotpos([(0.0, [0, 0, 3]), (1.0, [0, 0, -3]), (2.0, [0, 0, 3])],
                           [(0.0, [0, 0, 0]), (0.5, [0, -1, 0]), (1.0, [0, 0, 0]), (1.5, [0, -1, 0]), (2.0, [0, 0, 0])])}),
        "attack": anim(0.9, {
            "chest_gear": spin(2, 0.9, 2),
            "left_arm": rot((0.0, [0, 0, 0]), (0.35, [-150, 0, 10]), (0.55, [10, 0, 0]), (0.9, [0, 0, 0])),
            "right_arm": rot((0.0, [0, 0, 0]), (0.35, [-150, 0, -10]), (0.55, [10, 0, 0]), (0.9, [0, 0, 0])),
            "body": rot((0.0, [0, 0, 0]), (0.35, [-10, 0, 0]), (0.55, [14, 0, 0]), (0.9, [0, 0, 0]))}, loop=False),
    }
    m.write({"brass_sentinel": None, "brass_sentinel_void": sentinel_void}, anims, (3, 3.2))


# ======================================================================================
# Vorath, Archsin of Wrath: a giant of charred rock and lava, horned, with a burning beard and axe
# ======================================================================================

LAVA = (255, 120, 30)
ROCK = mat((26, 14, 12), (58, 30, 22), (96, 52, 36), "rock", cracks=(LAVA, 0.008), noise=8)
ROCK_DEEP = mat((26, 14, 12), (58, 30, 22), (96, 52, 36), "rock", cracks=(LAVA, 0.016), noise=8)
DARK_IRON = mat((18, 16, 18), (46, 42, 44), (92, 86, 84), "metal", rivets=True, seam=5, noise=5)
EMBER_FUR = mat((30, 20, 16), (64, 46, 34), (110, 84, 60), "fur", ragged=3, noise=10)
FLAME = mat((200, 50, 10), (255, 120, 30), (255, 220, 120), "flame", ragged=3)
CHARRED_HORN = mat((40, 30, 26), (130, 112, 92), (210, 196, 170), "plain", noise=6)
WOOD = mat((34, 20, 12), (70, 44, 26), (108, 72, 44), "cloth", noise=6)
AXE_HEAD = mat((30, 26, 26), (70, 62, 60), (130, 120, 116), "metal", cracks=(LAVA, 0.015), noise=5)


def vorath_face(p, f, m, rnd):
    w = f["north"][2]
    for dx in (2, 3, w - 4, w - 3):  # burning eyes under a heavy brow
        p.at(f["north"], dx, 4, (255, 220, 120), glow=True)
        p.at(f["north"], dx, 3, mix(m["pal"][0], (0, 0, 0), 0.4))
    for dx in range(3, w - 3):  # a mouth of fire
        p.at(f["north"], dx, 7, LAVA, glow=True)


def axe_edge(p, f, m, rnd):
    x, y, w, h = f["north"]
    for py in range(h):
        p.at(f["north"], 0, py, (255, 200, 90), glow=True)


def vorath():
    m = Model("vorath", 256, scale=1.25)
    m.bone("root")
    for side, sx in (("left", 1), ("right", -1)):
        leg = m.bone(side + "_leg", "root", (5 * sx, 24, 0))
        x = lambda a, w: a if sx > 0 else -a - w
        m.cube(leg, (x(1.5, 7), 13, -3.5), (7, 11, 7), ROCK)
        m.cube(leg, (x(1, 8), 4, -4), (8, 9, 8), DARK_IRON)
        m.cube(leg, (x(1, 8), 0, -5), (8, 4, 9), DARK_IRON)
    body = m.bone("body", "root", (0, 24, 0))
    m.cube(body, (-9, 22, -5), (18, 5, 10), DARK_IRON, deco=buckle((255, 190, 80)))
    m.cube(body, (-6, 12, -6), (12, 10, 1), EMBER_FUR)
    m.cube(body, (-8, 27, -5), (16, 8, 10), ROCK)
    m.cube(body, (-11, 35, -7), (22, 12, 13), ROCK_DEEP, deco=rift((255, 200, 90), 0.5, 2, 3, width=2))
    m.cube(body, (-12, 44, -7.5), (24, 4, 14), EMBER_FUR)
    for sx in (1, -1):
        for z in (-5, 1):
            m.cube(body, (9 if sx > 0 else -11, 48, z), (2, 3, 2), CHARRED_HORN)
    head = m.bone("head", "body", (0, 47, -2))
    m.cube(head, (-5, 47, -8), (10, 10, 10), ROCK, deco=vorath_face)
    m.cube(head, (-4, 41, -9), (8, 6, 2), FLAME)
    m.cube(head, (-5.5, 57, -8.5), (11, 1, 11), mat((70, 40, 10), (150, 96, 24), (230, 170, 60), "metal"))
    from creature_kit import horn as great_horn
    for sx in (1, -1):                                                                         # great horns sweeping out and up
        great_horn(m, head, ((6 if sx > 0 else -6), 53, -3), (sx, 0.35, 0.1), 7, CHARRED_HORN, start=3.6, taper=0.85,
                   curl=(0, 0, 16 * sx), step=3)
    for dx in (-4, -1, 2):
        m.cube(head, (dx + 0.5, 58, -8.5), (1, 2, 1), mat((70, 40, 10), (150, 96, 24), (230, 170, 60), "metal"))
    for side, sx in (("left", 1), ("right", -1)):
        horn = m.bone("horn_" + side, "head", (5 * sx, 54, -3))
        x = lambda a, w: a if sx > 0 else -a - w
        m.cube(horn, (x(5, 4), 53, -4.5), (4, 3, 3), CHARRED_HORN)
        m.cube(horn, (x(8, 3), 55, -4.5), (3, 3, 3), CHARRED_HORN)
        m.cube(horn, (x(10, 2), 58, -4), (2, 4, 2), CHARRED_HORN)
    for side, sx in (("left", 1), ("right", -1)):
        arm = m.bone(side + "_arm", "body", (12 * sx, 44, -1))
        x = lambda a, w: a if sx > 0 else -a - w
        m.cube(arm, (x(10, 8), 40, -6.5), (8, 7, 11), DARK_IRON)
        for z in (-4, 1):
            m.cube(arm, (x(13, 2), 47, z), (2, 3, 2), CHARRED_HORN)
        m.cube(arm, (x(11, 6), 26, -4.5), (6, 14, 7), ROCK)
        m.cube(arm, (x(10.5, 7), 18, -5), (7, 8, 8), DARK_IRON)
    axe = m.bone("axe", "right_arm", (-14, 21, -1), rotation=(50, 0, 0))
    m.cube(axe, (-15, 20, -27), (2, 2, 30), WOOD)
    m.cube(axe, (-15, 19, -28), (2, 4, 3), DARK_IRON)                    # the socket on the haft
    m.cube(axe, (-14.5, 13, -32), (1, 16, 5), AXE_HEAD, deco=axe_edge)  # a wide bearded blade
    m.cube(axe, (-14.5, 20, -27), (1, 2, 4), AXE_HEAD)                  # the back spike
    anims = {
        "idle": anim(2.4, {
            "body": rot(*swing([0, 0, 0], [2, 0, 0], 2.4)),
            "left_arm": rot(*swing([0, 0, -4], [0, 0, -7], 2.4)),
            "right_arm": rot(*swing([-8, 0, 4], [-12, 0, 7], 2.4))}),
        "walk": humanoid_walk(1.4, 25, 18, body=rot(*swing([4, 0, 0], [6, 0, 0], 0.7))),
        "attack": anim(0.8, {
            "right_arm": rot((0.0, [-8, 0, 4]), (0.3, [-170, 0, 10]), (0.5, [20, 0, 4]), (0.8, [-8, 0, 4])),
            "left_arm": rot((0.0, [0, 0, -4]), (0.3, [-120, 0, -20]), (0.5, [10, 0, -4]), (0.8, [0, 0, -4])),
            "body": rot((0.0, [0, 0, 0]), (0.3, [-10, 0, 0]), (0.5, [16, 0, 0]), (0.8, [0, 0, 0]))}, loop=False),
    }
    m.write({"vorath": None}, anims, (4, 5))


if __name__ == "__main__":
    void_wretch()
    void_stalker()
    # Kaleth, Serath, the Brass Sentinel and Vorath are drawn by make_boss_figures.py now
