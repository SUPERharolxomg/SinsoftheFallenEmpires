"""Shared pieces for the creature and boss models (make_mob_models.py's Model and Painter): curved horns, bat
wings with ribs and membrane, spikes, drawn faces, capes and hair, so silhouettes read from afar (the user's
bestiary sheet, art/concepts/bosses/bestiary_sheet.png: bigger, readable silhouettes, glowing eyes and marks).

Conventions as make_mob_models.py: Bedrock units (16 a block), y up, the model faces north (-z).
"""
import math

from make_mob_models import mat, mix


def glow_mat(color):
    """A material that is all light (eyes, runes, cores, lava)."""
    return mat(mix(color, (0, 0, 0), 0.35), color, mix(color, (255, 255, 255), 0.55), "glow")


def pattern(rows, palette, face="north", at=(0, 0), glow_chars=""):
    """Paints a small picture on a face: rows of characters, each one pixel; '.' leaves the face as it is.
    palette maps characters to colours; characters in glow_chars glow."""
    def deco(p, f, m, rnd):
        for dy, row in enumerate(rows):
            for dx, ch in enumerate(row):
                if ch == "." or ch not in palette:
                    continue
                p.at(f[face], at[0] + dx, at[1] + dy, palette[ch], glow=ch in glow_chars)
    return deco


def centered(rows, palette, face="north", top=0, glow_chars=""):
    """The same, centred across the face."""
    def deco(p, f, m, rnd):
        x, y, w, h = f[face]
        width = max(len(r) for r in rows)
        left = (w - width) // 2
        pattern(rows, palette, face, (left, top), glow_chars)(p, f, m, rnd)
    return deco


def veins(color, density=0.015, faces=("north", "east", "west", "south", "up")):
    """Glowing veins wandering down the faces (Void, lava, acid)."""
    def deco(p, f, m, rnd):
        for face in faces:
            x, y, w, h = f[face]
            for _ in range(max(1, int(w * h * density))):
                cx, cy = rnd.randrange(w), rnd.randrange(h)
                for _ in range(rnd.randint(3, 7)):
                    p.at(f[face], cx, cy, color, glow=True)
                    cx = max(0, min(w - 1, cx + rnd.choice((-1, 0, 1))))
                    cy = max(0, min(h - 1, cy + 1))
    return deco


def horn(m, bone, root, direction, segments, material, start=2.0, taper=0.82, curl=(0, 0, 0), step=2.5):
    """A horn of tapering cubes from root, growing along direction and bending by curl (degrees per segment,
    applied to the direction as a simple rotation in xy, yz and xz)."""
    x, y, z = root
    dx, dy, dz = direction
    size = start
    ax, ay, az = (math.radians(a) for a in curl)
    for i in range(segments):
        m.cube(bone, (x - size / 2, y - size / 2, z - size / 2), (size, size, size), material)
        x, y, z = x + dx * step, y + dy * step, z + dz * step
        # bend the direction a little each segment
        dx, dy = dx * math.cos(az) - dy * math.sin(az), dx * math.sin(az) + dy * math.cos(az)
        dy, dz = dy * math.cos(ax) - dz * math.sin(ax), dy * math.sin(ax) + dz * math.cos(ax)
        dx, dz = dx * math.cos(ay) + dz * math.sin(ay), -dx * math.sin(ay) + dz * math.cos(ay)
        size = max(0.6, size * taper)


def bat_wing(m, parent, side, pivot, span, height, membrane, rib, claws=None, sweep=14, lift=8):
    """A bat wing on a bone of its own (so it can beat), swept back and lifted from the shoulder at pivot: a bony arm
    out to the wrist, a thumb claw there, four long fingers fanning from it (up, out and down), and the membrane
    stretched between them as fans of thin panels whose edge dips between each pair of fingers (the scalloped
    trailing edge of a real bat's wing). side is 1 (left, +x) or -1 (right, -x)."""
    name = ("left" if side > 0 else "right") + "_wing"
    wing = m.bone(name, parent, pivot, rotation=(0, -sweep * side, -lift * side))
    px, py, pz = pivot
    arm_len = span * 0.42
    wrist_dir = (side * 0.9, 0.44, 0.0)
    wrist = (px + wrist_dir[0] * arm_len, py + wrist_dir[1] * arm_len, pz)
    shaft(m, wing, pivot, wrist_dir, arm_len, 1.8, rib)                                   # the arm
    m.cube(wing, (wrist[0] - 1.1, wrist[1] - 1.1, pz - 1.1), (2.2, 2.2, 2.2), rib)          # the wrist
    if claws:
        spike(m, wing, (wrist[0], wrist[1] + 0.8, pz), (side * 0.2, 1, -0.2), 3.5, 1.2, claws, segments=2)
    angles = (38, 8, -24, -56)                                                              # the fingers, from the top
    lengths = (span * 0.64, span * 0.66, span * 0.56, span * 0.44)
    tips = []
    for a, l in zip(angles, lengths):
        d = (side * math.cos(math.radians(a)), math.sin(math.radians(a)), 0)
        shaft(m, wing, wrist, d, l, 1.0, rib)
        tips.append((a, l))
    # membrane between each pair of fingers: a fan of panels from the wrist, shorter toward the middle of the gap
    def fan(origin, a0, l0, a1, l1, steps=7, dip=0.78):
        for k in range(steps + 1):
            t = k / steps
            a = a0 + (a1 - a0) * t
            l = (l0 + (l1 - l0) * t) * (1 - (1 - dip) * math.sin(math.pi * t))
            width = max(1.6, l * math.radians(abs(a1 - a0)) / steps * 2.4)
            d = (side * math.cos(math.radians(a)), math.sin(math.radians(a)), 0)
            shaft(m, wing, origin, d, l * 0.98, width, membrane, depth=0.35)
    for (a0, l0), (a1, l1) in zip(tips, tips[1:]):
        fan(wrist, a0, l0, a1, l1)
    # the inner membrane, from the last finger back to the body below the shoulder
    fan(wrist, tips[-1][0], tips[-1][1], -118, height * 0.9, steps=6, dip=0.85)
    return wing


def spikes(m, bone, row, along="x", count=5, length=4, material=None, up=(0, 1, 0), size=1.4, spread=2.5):
    """A row of spikes (shoulders, spine, crowns), each a short tapering column."""
    x0, y0, z0 = row
    for i in range(count):
        off = (i - (count - 1) / 2) * spread
        x = x0 + (off if along == "x" else 0)
        z = z0 + (off if along == "z" else 0)
        h = length * (1 - 0.25 * abs(off) / max(1, spread * count / 2))
        m.cube(bone, (x - size / 2, y0, z - size / 2), (size, h, size), material)
        m.cube(bone, (x - size / 4, y0 + h, z - size / 4), (size / 2, h * 0.5, size / 2), material)


def halo(m, bone, center, radius, thickness, material, rays=0, ray_len=0):
    """A ring of light behind a head, optionally with rays (Mirael's, Solrath's sun, Prython's spikes)."""
    cx, cy, cz = center
    steps = max(12, int(radius * 3))
    for i in range(steps):
        a = 2 * math.pi * i / steps
        x, y = cx + math.cos(a) * radius, cy + math.sin(a) * radius
        m.cube(bone, (x - thickness / 2, y - thickness / 2, cz), (thickness, thickness, 0.8), material)
    for i in range(rays):
        a = 2 * math.pi * i / rays + math.pi / 2
        for k in range(1, ray_len + 1):
            r = radius + k * 1.6
            x, y = cx + math.cos(a) * r, cy + math.sin(a) * r
            s = max(0.6, thickness * (1 - k / (ray_len + 1)))
            m.cube(bone, (x - s / 2, y - s / 2, cz), (s, s, 0.8), material)


def cape(m, bone, top, width, length, material, z):
    """A cape hanging from the shoulders behind the body."""
    m.cube(bone, (-width / 2, top - length, z), (width, length, 1), material)


# ================================================================================================ broken things
# Pieces that make a creature read as broken (the user: "que su presencia demuestre que estan rotos"): shards of
# itself floating round it, horns snapped off, halos cracked open, the shackles of an oath torn apart, capes in
# rags, seals split by a crack of light. They use turned cubes (Model.cube's rotation), so they are sharp and lean.

def _norm(v):
    length = math.sqrt(sum(c * c for c in v)) or 1.0
    return tuple(c / length for c in v)


def aim(direction):
    """The rotation that turns a cube standing on +y to point along direction (GeckoLib turns x first, then z:
    a positive x leans +y toward -z, a positive z leans +y toward +x)."""
    dx, dy, dz = _norm(direction)
    rx = math.degrees(math.asin(max(-1.0, min(1.0, -dz))))
    rz = math.degrees(math.atan2(dx, dy))
    return (rx, 0, rz)


def _turn(v, axis, degrees):
    """v turned round axis (Rodrigues)."""
    k = _norm(axis)
    a = math.radians(degrees)
    c, s = math.cos(a), math.sin(a)
    dot = sum(v[i] * k[i] for i in range(3))
    cross = (k[1] * v[2] - k[2] * v[1], k[2] * v[0] - k[0] * v[2], k[0] * v[1] - k[1] * v[0])
    return tuple(v[i] * c + cross[i] * s + k[i] * dot * (1 - c) for i in range(3))


def shaft(m, bone, base, direction, length, width, material, depth=None, deco=None):
    """One straight piece from base along direction, turned to it (its base stays put)."""
    depth = width if depth is None else depth
    bx, by, bz = base
    return m.cube(bone, (bx - width / 2, by, bz - depth / 2), (width, length, depth), material, deco=deco,
                  rotation=aim(direction), pivot=base)


def spike(m, bone, base, direction, length, width, material, tip=None, segments=3):
    """A spike: pieces narrowing along direction, the last one in the tip material (a glowing point)."""
    d = _norm(direction)
    x, y, z = base
    seg = length / segments
    for i in range(segments):
        w = max(0.5, width * (1 - i / (segments + 0.4)))
        shaft(m, bone, (x, y, z), d, seg * 1.15, w, tip if (tip and i == segments - 1) else material)
        x, y, z = x + d[0] * seg, y + d[1] * seg, z + d[2] * seg
    return (x, y, z)


def curved_horn(m, bone, base, direction, bend_axis, length, width, material, segments=6, bend=22, taper=0.8,
                broken=False, break_mat=None):
    """A horn that bends round bend_axis a little every segment. broken: it ends early in a jagged stump, its
    break glowing (break_mat), with a splinter still hanging on."""
    d = _norm(direction)
    x, y, z = base
    seg = length / segments
    w = width
    last = segments if not broken else max(2, int(segments * 0.55))
    for i in range(last):
        shaft(m, bone, (x, y, z), d, seg * 1.2, w, material)
        x, y, z = x + d[0] * seg, y + d[1] * seg, z + d[2] * seg
        d = _norm(_turn(d, bend_axis, bend))
        w = max(0.6, w * taper)
    if broken:
        shaft(m, bone, (x, y, z), _turn(d, bend_axis, 40), w * 0.9, w * 0.55, break_mat or material)
        shaft(m, bone, (x, y, z), _turn(d, bend_axis, -35), w * 0.7, w * 0.45, break_mat or material)
    return (x, y, z)


def shard_ring(m, parent, name, center, radius, count, material, length=4.0, width=1.4, rise=3.0, tilt=25, seed=1):
    """Pieces of the creature torn off and floating round it on a bone of their own (spin it in the animations)."""
    import random
    rnd = random.Random(seed)
    bone = m.bone(name, parent, center)
    cx, cy, cz = center
    for i in range(count):
        a = 2 * math.pi * i / count + rnd.uniform(-0.3, 0.3)
        r = radius * rnd.uniform(0.85, 1.15)
        x, z = cx + math.cos(a) * r, cz + math.sin(a) * r
        y = cy + rnd.uniform(-rise, rise)
        lean = (math.cos(a) * math.sin(math.radians(tilt)), 1, math.sin(a) * math.sin(math.radians(tilt)))
        l = length * rnd.uniform(0.6, 1.2)
        spike(m, bone, (x, y - l / 2, z), lean, l, width * rnd.uniform(0.7, 1.2), material, segments=2)
    return bone


def broken_halo(m, bone, center, radius, thickness, material, gaps=((40, 75), (200, 230)), loose=None, rays=0, ray_len=0):
    """A halo cracked open: arcs missing (gaps, in degrees), and the pieces that fell out hanging askew below
    (in loose, or the same material)."""
    cx, cy, cz = center
    steps = max(16, int(radius * 3.2))
    for i in range(steps):
        deg = 360.0 * i / steps
        if any(a <= deg <= b for a, b in gaps):
            continue
        a = math.radians(deg)
        x, y = cx + math.cos(a) * radius, cy + math.sin(a) * radius
        m.cube(bone, (x - thickness / 2, y - thickness / 2, cz), (thickness, thickness, 0.8), material, rotation=(0, 0, -deg), pivot=(x, y, cz))
    for k, (a0, b0) in enumerate(gaps):                    # the fallen piece of each gap, below the ring and turned
        mid = math.radians((a0 + b0) / 2)
        x = cx + math.cos(mid) * radius * 0.8
        y = cy - radius * 1.05 - k * 1.5
        m.cube(bone, (x - thickness * 1.6, y, cz + 0.6), (thickness * 3.2, thickness, 0.8), loose or material,
               rotation=(0, 0, 35 if k % 2 else -50), pivot=(x, y, cz))
    for i in range(rays):
        deg = 360.0 * i / rays + 90
        if any(a <= deg % 360 <= b for a, b in gaps):
            continue
        a = math.radians(deg)
        base = (cx + math.cos(a) * (radius + thickness), cy + math.sin(a) * (radius + thickness), cz)
        spike(m, bone, base, (math.cos(a), math.sin(a), 0), ray_len * (0.7 + 0.3 * (i % 2)), thickness, material, segments=2)


def shackle(m, parent, name, wrist, arm_half, links, material, spark=None, side=1):
    """The shackle of a broken oath on a wrist: a heavy cuff and a chain hanging from it, its last link torn
    open (a spark of light where it broke). On a bone of its own so the chain can swing."""
    wx, wy, wz = wrist
    bone = m.bone(name, parent, (wx, wy, wz))
    r = arm_half + 0.6
    m.cube(bone, (wx - r, wy - 1, wz - r), (2 * r, 2.4, 2 * r), material)
    m.cube(bone, (wx - r - 0.3, wy - 0.4, wz - r - 0.3), (2 * r + 0.6, 0.8, 2 * r + 0.6), material)
    x, y, z = wx + side * r * 0.6, wy - 1.6, wz - r * 0.3
    for i in range(links):
        if i % 2 == 0:
            m.cube(bone, (x - 0.6, y - 1.8, z - 0.25), (1.2, 2.0, 0.5), material)
        else:
            m.cube(bone, (x - 0.25, y - 1.8, z - 0.6), (0.5, 2.0, 1.2), material)
        y -= 1.5
        x += side * 0.25
    m.cube(bone, (x - 0.6, y - 0.8, z - 0.25), (1.2, 1.0, 0.5), material, rotation=(0, 0, 40 * side))   # the torn link
    if spark:
        m.cube(bone, (x - 0.35, y - 1.2, z - 0.35), (0.7, 0.7, 0.7), spark)
    return bone


def rag_strips(m, parent, prefix, top, width, length, strips, material, z, lengths=None, flare=4, thickness=1.0):
    """A cape or a skirt in rags: strips on bones of their own (sway them), each a different length, flaring
    out a little. Returns the bone names."""
    names = []
    w = width / strips
    for i in range(strips):
        x = -width / 2 + i * w
        name = "%s_%d" % (prefix, i)
        off = (i - (strips - 1) / 2) / max(1, (strips - 1) / 2)
        bone = m.bone(name, parent, (x + w / 2, top, z), rotation=(0, 0, -off * flare))
        l = length * (lengths[i % len(lengths)] if lengths else 1.0)
        m.cube(bone, (x, top - l, z - thickness / 2), (w + 0.05, l, thickness), material)
        names.append(name)
    return names


def claws(m, bone, hand_bottom, count, spread, length, width, material, forward=0.35, curl=-28):
    """Long claws from the bottom of a hand, curving forward."""
    hx, hy, hz = hand_bottom
    for i in range(count):
        x = hx + (i - (count - 1) / 2) * spread
        curved_horn(m, bone, (x, hy + 0.3, hz), (0, -1, -forward), (1, 0, 0), length, width, material, segments=3, bend=curl, taper=0.75)


ROMAN = {1: "I", 2: "II", 3: "III", 4: "IV", 5: "V", 6: "VI", 7: "VII", 8: "VIII", 9: "IX", 10: "X"}
_GLYPH = {"I": ["X", "X", "X", "X", "X"], "V": ["X.X", "X.X", "X.X", "X.X", ".X."], "X": ["X.X", "X.X", ".X.", "X.X", "X.X"]}


def law_seal(law, color, face="north", lip=None):
    """The broken seal of a Law of the Pact on a chest: a ring of light with the Law's number in it, split by a
    crack straight through (the oath this creature broke)."""
    def deco(p, f, m, rnd):
        x, y, w, h = f[face]
        cx, cy = (w - 1) / 2, (h - 1) / 2
        r = min(w, h) / 2 - 0.6
        for px in range(w):
            for py in range(h):
                if abs(math.hypot(px - cx, py - cy) - r) < 0.55:
                    p.at(f[face], px, py, color, glow=True)
        rows = ["", "", "", "", ""]
        for ch in ROMAN[law]:
            for i in range(5):
                rows[i] += _GLYPH[ch][i] + "."
        rows = [r_[:-1] for r_ in rows]
        width = len(rows[0])
        left, top = int(round(cx - (width - 1) / 2)), int(round(cy - 2))
        for dy, row in enumerate(rows):
            for dx, ch in enumerate(row):
                if ch == "X":
                    p.at(f[face], left + dx, top + dy, mix(color, (255, 255, 255), 0.35), glow=True)
        dark = lip or mix(color, (0, 0, 0), 0.8)
        px, py = int(cx) + 2, 0
        while py < h:                                     # the crack that broke the seal, from top to bottom
            p.at(f[face], px, py, dark)
            p.at(f[face], px + 1, py, mix(color, (255, 255, 255), 0.6), glow=True)
            py += 1
            px += rnd.choice((-1, 0, 0, 1))
    return deco


def broken_form(color, darken=0.25, fractures=3):
    """The second phase of a boss (its _broken texture): the same body, darker, split by fractures of its colour;
    what glowed glows hotter."""
    def recolor(m):
        out = dict(m)
        if m["style"] in ("glow", "flame"):
            out["pal"] = tuple(mix(c, (255, 255, 255), 0.2) for c in m["pal"])
            return out
        out["pal"] = tuple(mix(c, (0, 0, 0), darken) for c in m["pal"])
        out["fracture"] = (color, fractures)
        return out
    return recolor


# ================================================================================================ bodies, hands, faces
# The bosses are no longer boxes of the vanilla proportions: a figure has a chest wider than its waist, shoulders,
# elbows and knees (arms and legs in two bones, so they bend), a neck, hands with fingers and a thumb, and a face
# sculpted in 3D (a brow that juts, a nose, cheekbones, a jaw on its own bone, teeth, eyes of light set deep).

def hand(m, bone, wrist, width, material, side, fingers=4, finger_len=None, claw=None, curl=-30, spread=None, thumb=True):
    """A hand hanging from wrist (the middle of the wrist's bottom): a palm, thick fingers bending forward in one
    smooth curve, a thumb on the inner side, a claw at each tip if given. side: 1 left (+x), -1 right."""
    wx, wy, wz = wrist
    depth = width * 0.66
    palm_h = width * 0.8
    m.cube(bone, (wx - width / 2, wy - palm_h, wz - depth / 2), (width, palm_h, depth), material)
    m.cube(bone, (wx - width / 2 - 0.15, wy - palm_h - 0.2, wz - depth / 2 - 0.2), (width + 0.3, palm_h * 0.42, depth + 0.4), material)   # knuckles
    finger_len = finger_len or width * 1.0
    fw = max(0.9, width / (fingers + 0.25))
    spread = spread or (width - fw) / max(1, fingers - 1)
    for i in range(fingers):
        x = wx - (width - fw) / 2 + i * spread
        length = finger_len * (0.85 if i in (0, fingers - 1) else 1.0)
        base = (x, wy - palm_h + 0.2, wz - depth * 0.1)
        d1 = (0, -1, -0.15)
        shaft(m, bone, base, d1, length * 0.6, fw, material)                               # the first joint
        mid = (base[0], base[1] - length * 0.55, base[2] - length * 0.08)
        d2 = (0, -0.75, -0.66)
        shaft(m, bone, mid, d2, length * 0.55, fw * 0.88, material)                        # the second, curling forward
        if claw:
            tip = (mid[0], mid[1] - length * 0.38, mid[2] - length * 0.34)
            shaft(m, bone, tip, (0, -0.4, -1), length * 0.5, fw * 0.62, claw)
    if thumb:
        inner = -side
        base = (wx + inner * width * 0.42, wy - palm_h * 0.3, wz - depth * 0.25)
        shaft(m, bone, base, (inner * 0.55, -0.7, -0.45), finger_len * 0.65, fw * 1.05, material)
        if claw:
            tip = (base[0] + inner * finger_len * 0.33, base[1] - finger_len * 0.42, base[2] - finger_len * 0.27)
            shaft(m, bone, tip, (inner * 0.2, -0.4, -1), finger_len * 0.4, fw * 0.6, claw)


def figure(m, s=1.0, mats=None, chest=(11, 8, 6.5), waist=(7.5, 4, 5), arm=3.6, leg=4.2, arm_len=(8.5, 8.5), leg_len=(7.5, 8),
           hunch=10, head=8.5, neck=1.6, stance=1.0, forearm_bend=-18, hands=True, fingers=4, claw=None, finger_len=None,
           hand_scale=1.0, shoulder_pads=True):
    """A figure in the boss proportions. mats: skin, chest, waist, upper_arm, forearm, hand, thigh, shin, foot,
    pads (any missing fall back to skin). Every size is in units of a 32-unit-tall man and grows with s. Returns
    the places the dressing hangs on: head (x0, y0, z0, size), chest front z, shoulders, wrists, hips."""
    mats = dict(mats or {})
    skin = mats["skin"]
    head = head * 1.2                       # bosses carry big heads: a face that reads from across the arena
    for k in ("chest", "waist", "upper_arm", "forearm", "hand", "thigh", "shin", "foot", "pads", "neck"):
        mats.setdefault(k, skin)
    S = lambda *v: tuple(c * s for c in v)
    m.bone("root")
    foot_h, shin_l, thigh_l = 2.0, leg_len[1], leg_len[0]
    hip_y = foot_h + shin_l + thigh_l
    waist_w, waist_h, waist_d = waist
    chest_w, chest_h, chest_d = chest
    places = {"legs": {}, "arms": {}}
    for side, sx in (("left", 1), ("right", -1)):
        lx = sx * (waist_w / 2 - leg / 2 + 0.6) * stance
        thigh = m.bone(side + "_leg", "root", S(lx, hip_y, 0))
        m.cube(thigh, S(lx - leg / 2, hip_y - thigh_l, -leg / 2), S(leg, thigh_l, leg), mats["thigh"])
        knee_y = hip_y - thigh_l
        shin = m.bone(side + "_shin", side + "_leg", S(lx, knee_y, 0))
        sw = leg * 0.88
        m.cube(shin, S(lx - sw / 2, foot_h, -sw / 2), S(sw, shin_l + 0.4, sw), mats["shin"])
        m.cube(shin, S(lx - sw / 2 - 0.3, knee_y - 2.2, -sw / 2 - 0.5), S(sw + 0.6, 2.6, 1.4), mats["shin"])     # the knee cap
        m.cube(shin, S(lx - leg / 2 - 0.2, 0, -leg / 2 - 2.2), S(leg + 0.4, foot_h, leg + 2.6), mats["foot"])       # the foot, long
        places["legs"][side] = {"knee": S(lx, knee_y, 0), "foot": S(lx, 0, -leg / 2 - 2.2)}
    body = m.bone("body", "root", S(0, hip_y, 0), rotation=(hunch, 0, 0) if hunch else None)
    m.cube(body, S(-waist_w / 2, hip_y - 2, -waist_d / 2), S(waist_w, waist_h + 2, waist_d), mats["waist"])        # pelvis and belly
    chest_y = hip_y + waist_h
    m.cube(body, S(-chest_w * 0.42, chest_y - 1, -chest_d / 2 + 0.3), S(chest_w * 0.84, 2.5, chest_d - 0.6), mats["waist"])   # the ribs narrowing
    m.cube(body, S(-chest_w / 2, chest_y + 1, -chest_d / 2), S(chest_w, chest_h, chest_d), mats["chest"])
    top = chest_y + 1 + chest_h
    places["chest_front"] = -chest_d / 2 * s
    places["chest"] = S(-chest_w / 2, chest_y + 1, -chest_d / 2) + S(chest_w, chest_h, chest_d)
    places["back"] = chest_d / 2 * s
    places["top"] = top * s
    places["hips"] = hip_y * s
    neck_bone = m.bone("neck", "body", S(0, top, -0.6), rotation=(-hunch * 0.6, 0, 0) if hunch else None)
    m.cube(neck_bone, S(-1.6, top - 0.5, -2.2), S(3.2, neck + 0.5, 3.2), mats["neck"])
    head_y = top + neck
    head_bone = m.bone("head", "neck", S(0, head_y, -0.8), rotation=(-hunch * 0.4, 0, 0) if hunch else None)
    places["head"] = (-head / 2 * s, head_y * s, (-head / 2 - 0.8) * s, head * s)
    shoulder_y = top - 1.2
    for side, sx in (("left", 1), ("right", -1)):
        ax = sx * (chest_w / 2 + arm / 2 - 0.4)
        upper = m.bone(side + "_arm", "body", S(ax, shoulder_y, 0), rotation=(0, 0, -6 * sx))
        ul, fl = arm_len
        m.cube(upper, S(ax - arm / 2, shoulder_y - ul, -arm / 2), S(arm, ul + 1, arm), mats["upper_arm"])
        if shoulder_pads:
            m.cube(upper, S(ax - arm / 2 - 0.9, shoulder_y - 1.6, -arm / 2 - 0.9), S(arm + 1.8, 3.4, arm + 1.8), mats["pads"])
        elbow_y = shoulder_y - ul
        fore = m.bone(side + "_forearm", side + "_arm", S(ax, elbow_y, 0), rotation=(forearm_bend, 0, 0))
        fw = arm * 1.08
        m.cube(fore, S(ax - fw / 2, elbow_y - fl, -fw / 2), S(fw, fl + 0.6, fw), mats["forearm"])
        wrist = S(ax, elbow_y - fl, 0)
        if hands:
            hand(m, fore, wrist, arm * 1.05 * s * hand_scale, mats["hand"], sx, fingers=fingers, claw=claw,
                 finger_len=(finger_len * s if finger_len else None))
        places["arms"][side] = {"shoulder": S(ax, shoulder_y, 0), "elbow": S(ax, elbow_y, 0), "wrist": wrist, "half": arm / 2 * s,
                                "hand_bottom": (wrist[0], wrist[1] - arm * 0.9 * s * hand_scale, wrist[2])}
    return places


def sculpted_face(m, head_bone, place, skin, eye, kind="demon", brow=None, mouth=None, teeth=None, lips=None,
                  eye_size=(1.6, 0.8), anger=14, jaw_drop=0, deco=None, hair=None, deep=True):
    """A head with a face in 3D. kind: demon (a heavy angry brow, cheekbones, a fanged jaw), noble (a fine nose and
    lips, calm brow), skull (deep sockets, a row of teeth, no nose), hooded (a dark hollow, only eyes), beast
    (a muzzle). place is figure()'s head. Eyes are little cubes of light set under the brow, so they read from
    far away; the jaw is a bone of its own (named 'jaw')."""
    x0, y0, z0, size = place
    brow = brow or skin
    mouth = mouth or (mix(skin["pal"][0], (0, 0, 0), 0.6), mix(skin["pal"][0], (0, 0, 0), 0.4), skin["pal"][0])
    if isinstance(mouth, tuple):
        mouth = {"pal": mouth, "style": "plain", "noise": 2}
    teeth = teeth or {"pal": ((150, 140, 120), (220, 214, 196), (250, 248, 240)), "style": "plain", "noise": 2}
    u = size / 8.0                                                   # one face-pixel of a head 8 across
    front = z0
    cx = x0 + size / 2
    sockets = lambda p, f, mm, r: [p.at(f["north"], px, py, mix(mm["pal"][0], (0, 0, 0), 0.55))
                                   for py in range(int(f["north"][3] * 0.5), int(f["north"][3] * 0.74))
                                   for px in list(range(1, int(f["north"][2] * 0.42))) + list(range(int(f["north"][2] * 0.58), f["north"][2] - 1))]
    head_deco = combine_decos(sockets if deep and kind != "hooded" else None, deco)
    m.cube(head_bone, (x0, y0 + size * 0.22, z0), (size, size * 0.78, size), skin, deco=head_deco)    # skull and cheeks
    eye_y = y0 + size * 0.5
    ew, eh = eye_size[0] * u, eye_size[1] * u
    if kind == "hooded":
        hollow = {"pal": ((2, 2, 4), (6, 5, 8), (12, 10, 14)), "style": "plain", "noise": 1, "bevel": False}
        m.cube(head_bone, (x0 + u, y0 + size * 0.15, z0 - 0.05), (size - 2 * u, size * 0.7, 0.3), hollow)
        for sx in (1, -1):
            m.cube(head_bone, (cx + sx * 1.6 * u - ew / 2, eye_y, z0 - 0.35), (ew, eh, 0.3), eye)
            m.cube(head_bone, (cx + sx * 1.6 * u - ew * 0.75, eye_y - eh * 0.25, z0 - 0.2), (ew * 1.5, eh * 1.5, 0.15), dict(eye, pal=tuple(
                mix(c, (0, 0, 0), 0.6) for c in eye["pal"])))                          # a faint glow round them
        return
    # the jaw on its own bone, hinged under the ears
    jaw = m.bone("jaw", head_bone, (cx, y0 + size * 0.3, z0 + size * 0.6), rotation=(jaw_drop, 0, 0) if jaw_drop else None)
    jw = size * (0.84 if kind != "beast" else 0.7)
    m.cube(jaw, (cx - jw / 2, y0, z0 + 0.2 * u), (jw, size * 0.3, size * 0.85), skin)
    if kind in ("demon", "skull", "beast"):                          # the teeth along the jaw and from the upper lip
        n = 5 if kind != "skull" else 6
        for i in range(n):
            tx = cx - jw / 2 + 0.6 * u + i * (jw - 1.2 * u) / (n - 1)
            fang = kind == "demon" and i in (0, n - 1)
            th = (1.4 if fang else 0.8) * u
            m.cube(jaw, (tx - 0.35 * u, y0 + size * 0.3 - 0.1, z0 + 0.1 * u), (0.7 * u, th, 0.6 * u), teeth)
            m.cube(head_bone, (tx - 0.3 * u, y0 + size * 0.22 - (1.6 * u if fang else 0.7 * u), z0 - 0.05), (0.6 * u, (1.6 if fang else 0.7) * u, 0.6 * u), teeth)
        m.cube(jaw, (cx - jw / 2 + 0.4 * u, y0 + size * 0.22, z0 + 0.3 * u), (jw - 0.8 * u, 0.4 * u, 0.4 * u), mouth)   # the dark of the mouth
    elif lips:
        m.cube(head_bone, (cx - 1.3 * u, y0 + size * 0.24, z0 - 0.25 * u), (2.6 * u, 0.55 * u, 0.4 * u), lips)
    # the brow: two halves, their inner ends low over the eyes and their outer ends raised (a glare of anger); the
    # halves turn on their inner ends (a negative z lifts a bar's +x end, so the left half turns by -tilt, the right by +tilt)
    if kind in ("demon", "beast", "skull", "noble"):
        bh = (1.3 if kind != "noble" else 0.7) * u
        out = (1.2 if kind != "noble" else 0.55) * u
        tilt = anger if kind != "noble" else max(8, anger // 2)
        length = size * 0.42
        bottom = eye_y + eh + 0.12 * u
        for sx in (1, -1):
            inner = cx + sx * 0.25 * u
            m.cube(head_bone, (inner if sx > 0 else inner - length, bottom, z0 - out), (length, bh, out + 0.4), brow,
                   rotation=(0, 0, -tilt * sx), pivot=(inner, bottom, z0 - out))
    if kind in ("demon", "noble", "beast"):                          # the nose: a bridge from the brow and a heavy tip
        nw = (1.9 if kind == "demon" else 1.3) * u
        nd = (1.3 if kind == "demon" else 0.9) * u
        m.cube(head_bone, (cx - nw * 0.35, eye_y - 0.2 * u, z0 - nd * 0.6), (nw * 0.7, eh + 1.2 * u, nd * 0.6 + 0.2), skin)
        m.cube(head_bone, (cx - nw / 2, y0 + size * 0.34, z0 - nd), (nw, 1.5 * u, nd + 0.2), skin)
        if kind == "demon":
            for sx in (1, -1):                                       # flared nostrils
                m.cube(head_bone, (cx + sx * nw * 0.35 - 0.3 * u, y0 + size * 0.34, z0 - nd - 0.05),
                       (0.6 * u, 0.5 * u, 0.3), {"pal": ((10, 4, 4), (24, 10, 10), (40, 20, 20)), "style": "plain", "noise": 0, "bevel": False})
    if kind in ("demon", "skull"):                                   # cheekbones
        for sx in (1, -1):
            m.cube(head_bone, (cx + sx * size * 0.3 - 0.8 * u, y0 + size * 0.36, z0 - 0.4 * u), (1.6 * u, 0.9 * u, 0.5 * u), brow)
    if kind == "beast":                                              # a muzzle out in front
        m.cube(head_bone, (cx - size * 0.3, y0 + size * 0.22, z0 - size * 0.45), (size * 0.6, size * 0.32, size * 0.5), skin)
        m.cube(head_bone, (cx - size * 0.12, y0 + size * 0.48, z0 - size * 0.47), (size * 0.24, size * 0.1, 0.4), mouth)
    socket = {"pal": ((4, 2, 4), (10, 6, 10), (20, 14, 20)), "style": "plain", "noise": 0, "bevel": False}
    for sx in (1, -1):                                               # the eyes, set in dark sockets under the brow
        m.cube(head_bone, (cx + sx * 1.9 * u - ew / 2 - 0.35 * u, eye_y - 0.3 * u, z0 - 0.15), (ew + 0.7 * u, eh + 0.6 * u, 0.2), socket)
        m.cube(head_bone, (cx + sx * 1.9 * u - ew / 2, eye_y, z0 - 0.4), (ew, eh, 0.3), eye)
    if hair:
        m.cube(head_bone, (x0 - 0.3, y0 + size * 0.62, z0 - 0.3), (size + 0.6, size * 0.4, size + 0.6), hair)
        m.cube(head_bone, (x0 - 0.3, y0 - size * 0.4, z0 + size * 0.45), (size + 0.6, size * 1.05, size * 0.6), hair)


def combine_decos(*decos):
    def deco(p, f, m, rnd):
        for d in decos:
            if d:
                d(p, f, m, rnd)
    return deco


# --- animations of a figure (figure()'s bones): menace in the idle, weight in the walk, wind-up in the blow

def _sw(a, b, period):
    return [(0.0, a), (period / 2, b), (period, a)]


def _keys(frames):
    return {("%.2f" % t).rstrip("0").rstrip(".") if t else "0.0": [float(v) for v in value] for t, value in frames}


def _rot(frames):
    return {"rotation": _keys(frames)}


def figure_anims(period=1.3, stride=26, heavy=False, swing="right", extra_idle=None, extra_walk=None, orbit=None, rags=(), chains=(),
                 two_handed=False, idle_len=3.2):
    """The idle (heavy breathing, the head turning to the prey with a twitch, fingers flexing), the walk (knees bend,
    the body rolls with each step) and the attack (a wind-up with the body twisting, then the blow, slamming down)."""
    t = idle_len
    idle = {
        "body": {"rotation": _keys(_sw([0, 0, 0], [3, 0, 0], t)), "position": _keys(_sw([0, 0, 0], [0, -0.4, 0], t))},
        "head": _rot([(0.0, [0, 0, 0]), (t * 0.35, [0, 14, 0]), (t * 0.4, [6, -10, 4]), (t * 0.46, [0, -12, 0]), (t * 0.8, [0, 0, 0]), (t, [0, 0, 0])]),
        "left_arm": _rot(_sw([0, 0, 0], [-4, 0, -4], t)), "right_arm": _rot(_sw([0, 0, 0], [-4, 0, 4], t)),
        "left_forearm": _rot(_sw([0, 0, 0], [-8, 0, 0], t)), "right_forearm": _rot(_sw([-6, 0, 0], [0, 0, 0], t)),
        "jaw": _rot(_sw([0, 0, 0], [10, 0, 0], t)),
    }
    p = period
    walk = {
        "left_leg": _rot(_sw([stride, 0, 0], [-stride, 0, 0], p)), "right_leg": _rot(_sw([-stride, 0, 0], [stride, 0, 0], p)),
        "left_shin": _rot([(0.0, [0, 0, 0]), (p * 0.25, [stride * 1.2, 0, 0]), (p * 0.5, [0, 0, 0]), (p, [0, 0, 0])]),
        "right_shin": _rot([(0.0, [0, 0, 0]), (p * 0.5, [0, 0, 0]), (p * 0.75, [stride * 1.2, 0, 0]), (p, [0, 0, 0])]),
        "left_arm": _rot(_sw([-stride * 0.7, 0, 0], [stride * 0.7, 0, 0], p)), "right_arm": _rot(_sw([stride * 0.7, 0, 0], [-stride * 0.7, 0, 0], p)),
        "body": {"rotation": _keys([(0.0, [4, 0, 3]), (p / 2, [4, 0, -3]), (p, [4, 0, 3])]),
                 "position": _keys([(0.0, [0, 0, 0]), (p / 4, [0, -0.8 if heavy else -0.4, 0]), (p / 2, [0, 0, 0]), (p * 0.75, [0, -0.8 if heavy else -0.4, 0]), (p, [0, 0, 0])])},
        "head": _rot(_sw([-4, 0, -2], [-4, 0, 2], p)),
    }
    a = 1.1 if heavy else 0.75
    arm = swing + "_arm"
    other = ("left" if swing == "right" else "right") + "_arm"
    attack = {
        arm: _rot([(0.0, [0, 0, 0]), (a * 0.45, [-170, 0, 14]), (a * 0.62, [30, 0, -6]), (a, [0, 0, 0])]),
        swing + "_forearm": _rot([(0.0, [0, 0, 0]), (a * 0.45, [-30, 0, 0]), (a * 0.62, [0, 0, 0]), (a, [0, 0, 0])]),
        "body": _rot([(0.0, [0, 0, 0]), (a * 0.45, [-14, 22 if swing == "right" else -22, 0]), (a * 0.62, [22, -10 if swing == "right" else 10, 0]), (a, [0, 0, 0])]),
        "jaw": _rot([(0.0, [0, 0, 0]), (a * 0.5, [26, 0, 0]), (a, [0, 0, 0])]),
        "head": _rot([(0.0, [0, 0, 0]), (a * 0.45, [-14, 0, 0]), (a * 0.62, [10, 0, 0]), (a, [0, 0, 0])]),
    }
    if two_handed:
        attack[other] = _rot([(0.0, [0, 0, 0]), (a * 0.45, [-170, 0, -14]), (a * 0.62, [30, 0, 6]), (a, [0, 0, 0])])
    for i, name in enumerate(rags):                                   # the rags and chains sway, each a beat apart
        lag = (i % 3) * 0.15
        idle[name] = _rot([(0.0, [4, 0, 0]), (t * (0.3 + lag / 3), [10, 0, 2]), (t * 0.7, [2, 0, -2]), (t, [4, 0, 0])])
        walk[name] = _rot(_sw([18 + 4 * (i % 2), 0, 0], [30, 0, 0], p / 2))
        attack[name] = _rot([(0.0, [4, 0, 0]), (a * 0.62, [40, 0, 0]), (a, [4, 0, 0])])
    for i, name in enumerate(chains):
        idle[name] = _rot(_sw([0, 0, 6 if i % 2 else -6], [0, 0, -6 if i % 2 else 6], t / 2))
        walk[name] = _rot(_sw([20, 0, 0], [-20, 0, 0], p))
    if orbit:
        for name, spin_period in orbit.items():
            idle[name] = {"rotation": _keys([(0.0, [0, 0, 0]), (spin_period / 2, [0, 180, 0]), (spin_period, [0, 360, 0])]),
                          "position": _keys(_sw([0, 0, 0], [0, 1.5, 0], spin_period))}
            walk[name] = idle[name]
            attack[name] = {"rotation": _keys([(0.0, [0, 0, 0]), (a, [0, 360, 0])])}
    idle.update(extra_idle or {})
    walk.update(extra_walk or {})
    return {"idle": {"loop": True, "animation_length": t, "bones": idle},
            "walk": {"loop": True, "animation_length": p, "bones": walk},
            "attack": {"loop": False, "animation_length": a, "bones": attack}}


# --- the signature attack's animation (SoFEBossEntity.Signature): a wind-up as long as the boss's own, the blow
# landing at its end, then a moment of follow-through (SIGNATURE_FOLLOW, 12 ticks)

def signature_anim(kind, windup_ticks, swing="right"):
    T = windup_ticks / 20.0
    L = round(T + 0.6, 2)
    hit, after = round(T, 2), round(T + 0.15, 2)
    ready = round(T * 0.75, 2)
    arm, other = swing + "_arm", ("left" if swing == "right" else "right") + "_arm"
    fore = swing + "_forearm"
    k = lambda *frames: _rot(list(frames))
    b = {}
    if kind in ("overhead", "overhead2"):
        b[arm] = k((0.0, [0, 0, 0]), (ready, [-175, 0, 12]), (hit - 0.05, [-178, 0, 12]), (after, [40, 0, -4]), (L, [0, 0, 0]))
        b[fore] = k((0.0, [0, 0, 0]), (ready, [-35, 0, 0]), (after, [0, 0, 0]), (L, [0, 0, 0]))
        if kind == "overhead2":
            b[other] = k((0.0, [0, 0, 0]), (ready, [-175, 0, -12]), (hit - 0.05, [-178, 0, -12]), (after, [40, 0, 4]), (L, [0, 0, 0]))
        b["body"] = k((0.0, [0, 0, 0]), (ready, [-18, 0, 0]), (hit - 0.05, [-20, 0, 0]), (after, [26, 0, 0]), (L, [0, 0, 0]))
        b["head"] = k((0.0, [0, 0, 0]), (ready, [-20, 0, 0]), (after, [12, 0, 0]), (L, [0, 0, 0]))
        b["jaw"] = k((0.0, [0, 0, 0]), (ready, [30, 0, 0]), (L, [0, 0, 0]))
    elif kind == "sweep":
        b[arm] = k((0.0, [0, 0, 0]), (ready, [-90, -40, 30]), (after, [-90, 60, -10]), (L, [0, 0, 0]))
        b["body"] = k((0.0, [0, 0, 0]), (ready, [-6, 55, 0]), (after, [10, -65, 0]), (L, [0, 0, 0]))
        b["jaw"] = k((0.0, [0, 0, 0]), (ready, [24, 0, 0]), (L, [0, 0, 0]))
    elif kind == "thrust":
        b[arm] = k((0.0, [0, 0, 0]), (ready, [35, 0, 10]), (hit - 0.05, [40, 0, 10]), (after, [-100, 0, 0]), (L, [0, 0, 0]))
        b[other] = k((0.0, [0, 0, 0]), (ready, [-70, 0, -10]), (after, [-80, 0, -10]), (L, [0, 0, 0]))
        b["body"] = k((0.0, [0, 0, 0]), (ready, [-10, 25, 0]), (after, [22, -15, 0]), (L, [0, 0, 0]))
    elif kind == "raise_left":
        b["left_arm"] = k((0.0, [0, 0, 0]), (ready, [-165, 0, -12]), (hit, [-170, 0, -16]), (after, [-150, 0, -10]), (L, [0, 0, 0]))
        b["right_arm"] = k((0.0, [0, 0, 0]), (ready, [-60, 0, 20]), (after, [-90, 0, 10]), (L, [0, 0, 0]))
        b["body"] = k((0.0, [0, 0, 0]), (ready, [-12, 0, 0]), (after, [8, 0, 0]), (L, [0, 0, 0]))
        b["head"] = k((0.0, [0, 0, 0]), (ready, [-16, 0, 0]), (L, [0, 0, 0]))
    elif kind == "spin":
        b[arm] = k((0.0, [0, 0, 0]), (ready, [-90, 0, 50]), (after, [-90, 0, 50]), (L, [0, 0, 0]))
        b["body"] = k((0.0, [0, 0, 0]), (ready, [0, -60, 0]), (hit - 0.1, [0, -70, 0]), (round(hit + 0.25, 2), [0, 290, 0]), (L, [0, 360, 0]))
    elif kind == "spread":
        b["left_arm"] = k((0.0, [0, 0, 0]), (ready, [-40, 0, -85]), (hit - 0.05, [-40, 0, -90]), (after, [-95, 0, 10]), (L, [0, 0, 0]))
        b["right_arm"] = k((0.0, [0, 0, 0]), (ready, [-40, 0, 85]), (hit - 0.05, [-40, 0, 90]), (after, [-95, 0, -10]), (L, [0, 0, 0]))
        b["left_wing"] = k((0.0, [0, 0, 0]), (ready, [0, 20, 25]), (after, [0, -45, -20]), (L, [0, 0, 0]))
        b["right_wing"] = k((0.0, [0, 0, 0]), (ready, [0, -20, -25]), (after, [0, 45, 20]), (L, [0, 0, 0]))
        b["body"] = k((0.0, [0, 0, 0]), (ready, [-14, 0, 0]), (after, [12, 0, 0]), (L, [0, 0, 0]))
        b["head"] = k((0.0, [0, 0, 0]), (ready, [-22, 0, 0]), (L, [0, 0, 0]))
    elif kind == "slam2":
        for side, sx in (("left", -1), ("right", 1)):
            b[side + "_arm"] = k((0.0, [0, 0, 0]), (ready, [-170, 0, 14 * sx]), (hit - 0.05, [-172, 0, 14 * sx]), (after, [25, 0, 0]), (L, [0, 0, 0]))
        b["body"] = k((0.0, [0, 0, 0]), (ready, [-14, 0, 0]), (after, [24, 0, 0]), (L, [0, 0, 0]))
        b["head"] = k((0.0, [0, 0, 0]), (ready, [-20, 0, 0]), (L, [0, 0, 0]))
        b["jaw"] = k((0.0, [0, 0, 0]), (ready, [30, 0, 0]), (L, [0, 0, 0]))
    elif kind == "cross":
        for side, sx in (("left", -1), ("right", 1)):
            b[side + "_arm"] = k((0.0, [0, 0, 0]), (ready, [-150, 0, 45 * sx]), (hit - 0.05, [-155, 0, 48 * sx]), (after, [15, 0, -35 * sx]), (L, [0, 0, 0]))
        b["body"] = k((0.0, [0, 0, 0]), (ready, [-16, 0, 0]), (after, [22, 0, 0]), (L, [0, 0, 0]))
        b["jaw"] = k((0.0, [0, 0, 0]), (ready, [34, 0, 0]), (L, [0, 0, 0]))
    elif kind == "leap":
        b["root"] = {"position": _keys([(0.0, [0, 0, 0]), (0.5, [0, -2, 0]), (0.6, [0, 4, 0]), (round(T * 0.85, 2), [0, 60, 0]),
                                        (hit, [0, 0, 0]), (L, [0, 0, 0])])}
        b[arm] = k((0.0, [0, 0, 0]), (0.6, [-175, 0, 10]), (hit - 0.05, [-178, 0, 10]), (after, [30, 0, 0]), (L, [0, 0, 0]))
        b["body"] = k((0.0, [0, 0, 0]), (0.5, [20, 0, 0]), (0.7, [-12, 0, 0]), (after, [28, 0, 0]), (L, [0, 0, 0]))
        b["left_wing"] = k((0.0, [0, 0, 0]), (0.6, [0, -30, -30]), (round(T * 0.85, 2), [0, 20, 20]), (L, [0, 0, 0]))
        b["right_wing"] = k((0.0, [0, 0, 0]), (0.6, [0, 30, 30]), (round(T * 0.85, 2), [0, -20, -20]), (L, [0, 0, 0]))
    elif kind == "rear":
        b["abdomen"] = k((0.0, [0, 0, 0]), (ready, [-28, 0, 0]), (hit - 0.05, [-30, 0, 0]), (after, [14, 0, 0]), (L, [0, 0, 0]))
        for i in range(4):
            for side, sx in (("l", 1), ("r", -1)):
                b["leg_%s%d" % (side, i)] = k((0.0, [0, 0, 0]), (ready, [0, 0, 26 * sx]), (after, [0, 0, -18 * sx]), (L, [0, 0, 0]))
        b["left_arm"] = k((0.0, [0, 0, 0]), (ready, [-150, 0, -30]), (after, [20, 0, 0]), (L, [0, 0, 0]))
        b["right_arm"] = k((0.0, [0, 0, 0]), (ready, [-150, 0, 30]), (after, [20, 0, 0]), (L, [0, 0, 0]))
    elif kind == "pounce":
        b["body"] = {"rotation": _keys([(0.0, [0, 0, 0]), (ready, [12, 0, 0]), (after, [-18, 0, 0]), (L, [0, 0, 0])]),
                     "position": _keys([(0.0, [0, 0, 0]), (ready, [0, -3, 3]), (hit, [0, 4, -6]), (after, [0, 0, -4]), (L, [0, 0, 0])])}
        b["neck"] = k((0.0, [0, 0, 0]), (ready, [12, 0, 0]), (after, [-28, 0, 0]), (L, [0, 0, 0]))
        b["jaw"] = k((0.0, [0, 0, 0]), (ready, [20, 0, 0]), (hit, [55, 0, 0]), (after, [5, 0, 0]), (L, [0, 0, 0]))
        for leg in ("front_left_leg", "front_right_leg"):
            b[leg] = k((0.0, [0, 0, 0]), (ready, [20, 0, 0]), (hit, [-70, 0, 0]), (L, [0, 0, 0]))
        for leg in ("back_left_leg", "back_right_leg"):
            b[leg] = k((0.0, [0, 0, 0]), (ready, [-25, 0, 0]), (hit, [40, 0, 0]), (L, [0, 0, 0]))
    return {"loop": False, "animation_length": L, "bones": b}
