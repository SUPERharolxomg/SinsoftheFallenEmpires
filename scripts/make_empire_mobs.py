"""GeckoLib models of the empires' own creatures, two for each of Acts I to IV, with the bones, painted
textures, glow masks and animations of make_mob_models.py (the same Model and Painter):

  Act I   Sulthari  sand_ghoul (a gaunt desert ghoul that leaps), clockwork_scarab (a brass beetle, in swarms)
  Act II  Nordrath  draugr (an undead Norse warrior, frost in its axe), rime_wolf (a pack hunter of the ice)
  Act III Parsivan  mirage_dancer (an illusion in silks that blinks behind you)
          Khemet    bog_mummy (linen wrappings soaked in marsh mud)
  Act IV  Aureum    gilded_legionnaire (a corrupted legionary with shield and spear), gladiator_shade (a ghost
                    of the colosseum with two blades)

Run from the repository root: python scripts/make_empire_mobs.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from make_mob_models import (Model, mat, anim, rot, rotpos, swing, humanoid_walk, face_slit, combine, rift,  # noqa: E402
                             clear_front_below, buckle, mix)
from creature_kit import centered, horn, spikes, veins  # noqa: E402


# ------------------------------------------------------------------------------------------------ helpers

def eyes(color, row, width=1, gap=2):
    return face_slit(color, row, width, gap)


def stripes(color, every=3, face="north"):
    """Horizontal bands across a face: wrappings, segmented plates."""
    def deco(p, f, m, rnd):
        x, y, w, h = f[face]
        for py in range(h):
            if py % every == every - 1:
                for px in range(w):
                    p.at(f[face], px, py, mix(color, m["pal"][0], 0.3 if (px + py) % 2 else 0))
    return deco


def band(color, row, faces=("north", "east", "west", "south")):
    def deco(p, f, m, rnd):
        for face in faces:
            x, y, w, h = f[face]
            if row < h:
                for px in range(w):
                    p.at(f[face], px, row, color)
    return deco


def spots(color, chance, glow=False, faces=("north", "east", "west", "south", "up")):
    def deco(p, f, m, rnd):
        for face in faces:
            x, y, w, h = f[face]
            for px in range(w):
                for py in range(h):
                    if rnd.random() < chance:
                        p.at(f[face], px, py, color, glow)
    return deco


def humanoid(m, skin, body_mat, legs_mat, arms_mat, head_deco=None, body_deco=None, scale=1.0, arm_len=12, hunch=0):
    """A humanoid of the vanilla proportions (32 units tall at scale 1): bones root, body, head, arms, legs."""
    s = scale
    m.bone("root")
    for side, sx in (("left", 1), ("right", -1)):
        leg = m.bone(side + "_leg", "root", (2 * sx * s, 12 * s, 0))
        m.cube(leg, ((0 if sx > 0 else -4) * s, 0, -2 * s), (4 * s, 12 * s, 4 * s), legs_mat)
    body = m.bone("body", "root", (0, 12 * s, 0), rotation=(hunch, 0, 0) if hunch else None)
    m.cube(body, (-4 * s, 12 * s, -2 * s), (8 * s, 12 * s, 4 * s), body_mat, deco=body_deco)
    head = m.bone("head", "body", (0, 24 * s, 0))
    m.cube(head, (-4 * s, 24 * s, -4 * s), (8 * s, 8 * s, 8 * s), skin, deco=head_deco)
    for side, sx in (("left", 1), ("right", -1)):
        arm = m.bone(side + "_arm", "body", (5 * sx * s, 22 * s, 0))
        m.cube(arm, ((4 if sx > 0 else -8) * s, (24 - arm_len) * s - 2 * s, -2 * s), (4 * s, arm_len * s, 4 * s), arms_mat)
    return body, head


def melee_anims(period=1.2, legs=28, arms=20, swing_arm="right_arm", body=None):
    return {
        "idle": anim(2.4, {"body": rot(*swing([0, 0, 0], [2, 0, 0], 2.4)),
                           "left_arm": rot(*swing([0, 0, -3], [0, 0, -6], 2.4)),
                           "right_arm": rot(*swing([0, 0, 3], [0, 0, 6], 2.4))}),
        "walk": humanoid_walk(period, legs, arms, body=body),
        "attack": anim(0.6, {swing_arm: rot((0.0, [0, 0, 0]), (0.2, [-120, 0, 10]), (0.4, [20, 0, 0]), (0.6, [0, 0, 0])),
                             "body": rot((0.0, [0, 0, 0]), (0.2, [-6, 0, 0]), (0.4, [10, 0, 0]), (0.6, [0, 0, 0]))}, loop=False),
    }


# ------------------------------------------------------------------------------------------------ Act I: Sulthari

GHOUL_SKIN = mat((96, 74, 52), (150, 120, 84), (196, 166, 120), "skin_cracked", noise=8)
GHOUL_RAG = mat((60, 40, 26), (104, 74, 48), (146, 110, 76), "cloth", ragged=4, noise=6)
GHOUL_CLAW = mat((70, 60, 50), (160, 146, 120), (220, 210, 186), "plain", noise=4)
AMBER = (255, 170, 40)


def sand_ghoul():
    m = Model("sand_ghoul", 128)
    body, head = humanoid(m, GHOUL_SKIN, GHOUL_SKIN, GHOUL_SKIN, GHOUL_SKIN, scale=1.0, arm_len=15, hunch=24,
                          head_deco=combine(eyes(AMBER, 3, 2, 2), lambda p, f, mm, r: [p.at(f["north"], x, 6, (40, 24, 16)) for x in range(2, 6)]),
                          body_deco=lambda p, f, mm, r: [p.at(f["north"], x, y, (110, 86, 60)) for y in (3, 5, 7) for x in range(1, 7) if x != 4])
    m.cube(body, (-4.5, 11, -2.5), (9, 7, 5), GHOUL_RAG, inflate=0.2)                   # a loincloth of rags
    m.cube(head, (-4.5, 23, -4.5), (9, 10, 9), GHOUL_RAG, inflate=0.4, deco=clear_front_below(1))  # a deep torn hood
    m.cube(head, (-3, 24.5, -4.3), (6, 6, 1), mat((150, 140, 116), (206, 196, 168), (236, 230, 210), "plain", noise=4),
           deco=centered([".....", "AA.AA", "AA.AA", "..x..", ".xxx.", "x.x.x"], {"A": AMBER, "x": (40, 30, 24)}, top=0, glow_chars="A"))  # the skull
    for side, sx in (("left", 1), ("right", -1)):
        for cx in (0, 2):
            m.cube(side + "_arm", ((4.5 if sx > 0 else -7.5) + cx, 4, -1.5), (1, 3, 1), GHOUL_CLAW)
    anims = melee_anims(1.0, 30, 30, body=rot(*swing([24, 0, 3], [26, 0, -3], 1.0)))
    anims["attack"] = anim(0.5, {"left_arm": rot((0.0, [0, 0, 0]), (0.2, [-140, 0, -20]), (0.35, [20, 0, 0]), (0.5, [0, 0, 0])),
                                 "right_arm": rot((0.0, [0, 0, 0]), (0.2, [-140, 0, 20]), (0.35, [20, 0, 0]), (0.5, [0, 0, 0])),
                                 "body": rotpos([(0.0, [24, 0, 0]), (0.2, [10, 0, 0]), (0.35, [34, 0, 0]), (0.5, [24, 0, 0])],
                                                [(0.0, [0, 0, 0]), (0.2, [0, 1, 0]), (0.5, [0, 0, 0])])}, loop=False)
    m.write({"sand_ghoul": None}, anims, (1.4, 2.4))


SCARAB_BRASS = mat((100, 62, 20), (176, 124, 48), (236, 194, 104), "metal", rivets=True, seam=3, noise=5)
SCARAB_SHELL = mat((20, 70, 80), (40, 150, 150), (120, 220, 210), "metal", noise=4)
SCARAB_CORE = mat((20, 120, 110), (60, 210, 200), (200, 255, 245), "glow")


def clockwork_scarab():
    """A brass beetle as big as a dog: wing cases set with domes of turquoise glass, an aetherium heart, six legs."""
    m = Model("clockwork_scarab", 128)
    m.bone("root")
    body = m.bone("body", "root", (0, 6, 0))
    m.cube(body, (-6, 4, -8), (12, 5, 16), SCARAB_BRASS)
    m.cube(body, (-7, 8, -7), (6.8, 4, 15), SCARAB_SHELL, deco=stripes((200, 160, 60), 3, "up"))
    m.cube(body, (0.2, 8, -7), (6.8, 4, 15), SCARAB_SHELL, deco=stripes((200, 160, 60), 3, "up"))
    for x, z in ((-4.5, -4), (2.5, -4), (-4.5, 2), (2.5, 2)):                                   # the glass domes
        m.cube(body, (x, 12, z), (2.4, 1.6, 3), SCARAB_CORE)
    m.cube(body, (-1.5, 9, 5), (3, 3, 3), SCARAB_CORE)                                          # the aetherium heart behind
    head = m.bone("head", "body", (0, 7, -8))
    m.cube(head, (-4, 4, -13), (8, 5, 5), SCARAB_BRASS, deco=centered(["X....X"], {"X": (120, 255, 240)}, top=1, glow_chars="X"))
    m.cube(head, (-4.5, 4, -16), (2, 2, 4), SCARAB_BRASS, mirror_x=True)                         # mandibles
    for i, z in enumerate((-5, 0, 5)):
        for side, sx in (("l", 1), ("r", -1)):
            leg = m.bone("leg_%s%d" % (side, i), "body", (6 * sx, 6, z))
            m.cube(leg, ((6 if sx > 0 else -11), 5, z - 0.75), (5, 1.5, 1.5), SCARAB_BRASS)
            m.cube(leg, ((10 if sx > 0 else -11.5), 0, z - 0.75), (1.5, 6, 1.5), SCARAB_BRASS)
    legs = {}
    for i in range(3):
        for side in ("l", "r"):
            a1, b1 = ([0, 0, 18], [0, 0, -12]) if (i + (side == "l")) % 2 else ([0, 0, -12], [0, 0, 18])
            legs["leg_%s%d" % (side, i)] = rot(*swing(a1, b1, 0.4))
    anims = {
        "idle": anim(1.6, {"body": rotpos(swing([0, 0, 0], [-3, 0, 0], 1.6), swing([0, 0, 0], [0, 0.4, 0], 1.6))}),
        "walk": anim(0.4, dict(legs, body=rotpos(swing([0, 0, 2], [0, 0, -2], 0.4), swing([0, 0, 0], [0, 0.4, 0], 0.2)))),
        "attack": anim(0.4, {"head": rot((0.0, [0, 0, 0]), (0.15, [-25, 0, 0]), (0.3, [15, 0, 0]), (0.4, [0, 0, 0])),
                             "body": rotpos([(0.0, [0, 0, 0]), (0.15, [-10, 0, 0]), (0.4, [0, 0, 0])],
                                            [(0.0, [0, 0, 0]), (0.15, [0, 2, -1]), (0.4, [0, 0, 0])])}, loop=False),
    }
    m.write({"clockwork_scarab": None}, anims, (1.6, 1.0))


# ------------------------------------------------------------------------------------------------ Act II: Nordrath

DRAUGR_SKIN = mat((60, 74, 86), (104, 124, 136), (156, 176, 186), "skin_cracked", cracks=((140, 230, 255), 0.004), noise=6)
DRAUGR_MAIL = mat((34, 36, 40), (72, 76, 82), (120, 124, 128), "metal", seam=2, noise=7)
DRAUGR_FUR = mat((46, 40, 36), (90, 82, 74), (140, 132, 122), "fur", ragged=3, noise=10)
DRAUGR_HELM = mat((30, 28, 30), (70, 64, 62), (124, 116, 110), "metal", rivets=True, noise=5)
FROST = (150, 235, 255)
FROST_AXE = mat((60, 120, 160), (130, 210, 240), (220, 250, 255), "metal", cracks=(FROST, 0.03), noise=4)
HAFT = mat((34, 22, 14), (70, 46, 28), (104, 72, 46), "cloth", noise=6)


def draugr():
    m = Model("draugr", 128)
    body, head = humanoid(m, DRAUGR_SKIN, DRAUGR_MAIL, DRAUGR_MAIL, DRAUGR_SKIN, scale=1.1,
                          head_deco=combine(eyes(FROST, 4, 2, 2), lambda p, f, mm, r: [p.at(f["north"], x, 7, (30, 40, 50)) for x in range(2, 7)]),
                          body_deco=buckle((150, 140, 120)))
    m.cube(head, (-4.9, 29, -4.9), (9.8, 7, 9.8), DRAUGR_HELM, deco=combine(band((170, 160, 140), 2), clear_front_below(4)))  # a closed helm
    m.cube(head, (-0.6, 26.5, -5.6), (1.2, 7, 1.2), DRAUGR_HELM)                                     # the nasal guard
    horn(m, head, (4.6, 33.5, -1), (1, 0.6, 0), 4, mat((150, 140, 120), (210, 200, 180), (240, 234, 220), "plain"), start=2.2, curl=(0, 0, 18))
    horn(m, head, (-4.6, 33.5, -1), (-1, 0.6, 0), 4, mat((150, 140, 120), (210, 200, 180), (240, 234, 220), "plain"), start=2.2, curl=(0, 0, -18))
    m.cube(body, (-5, 22, -3), (10, 5, 6), DRAUGR_FUR, inflate=0.3)                                   # a fur mantle
    m.cube(body, (-4.5, 6, 2.4), (9, 20, 1), DRAUGR_FUR)                                             # the cloak behind
    sword = m.bone("sword", "right_arm", (-7, 12, 0), rotation=(75, 0, 0))
    m.cube(sword, (-8, 11, -2), (2, 2, 5), HAFT)
    m.cube(sword, (-9.5, 10.5, -3), (5, 3, 1), DRAUGR_HELM)                                          # the guard
    m.cube(sword, (-8.5, 11, -24), (3, 1, 21), FROST_AXE)                                            # a great frost blade
    spikes(m, body, (0, 27, 0), along="x", count=4, length=2, material=FROST_AXE, size=1, spread=3)
    anims = melee_anims(1.4, 24, 18)
    anims["attack"] = anim(0.8, {"right_arm": rot((0.0, [0, 0, 0]), (0.35, [-160, 0, 10]), (0.5, [20, 0, 0]), (0.8, [0, 0, 0])),
                                 "body": rot((0.0, [0, 0, 0]), (0.35, [-10, 0, 0]), (0.5, [14, 0, 0]), (0.8, [0, 0, 0]))}, loop=False)
    m.write({"draugr": None}, anims, (1.6, 3))


WOLF_FUR = mat((150, 170, 186), (206, 222, 232), (244, 250, 255), "fur", noise=8)
WOLF_DARK = mat((60, 76, 90), (110, 130, 146), (160, 180, 196), "fur", noise=8)
ICE_SPIKE = mat((80, 160, 210), (150, 220, 250), (230, 250, 255), "glow")


def rime_wolf():
    m = Model("rime_wolf", 128)
    m.bone("root")
    body = m.bone("body", "root", (0, 10, 0))
    m.cube(body, (-3.5, 7, -6), (7, 7, 13), WOLF_FUR)
    m.cube(body, (-4, 8, -7), (8, 7, 6), WOLF_FUR, inflate=0.4)                                      # the ruff
    spikes(m, body, (0, 14, -2), along="z", count=6, length=4, material=ICE_SPIKE, size=1.6, spread=2.2)
    for sx in (1, -1):
        spikes(m, body, ((3 if sx > 0 else -3), 13, -5), along="z", count=2, length=3, material=ICE_SPIKE, size=1.2, spread=2)
    head = m.bone("head", "body", (0, 13, -7))
    m.cube(head, (-3, 10, -12), (6, 6, 5), WOLF_FUR, deco=eyes(FROST, 2, 1, 2))
    m.cube(head, (-1.5, 10, -15), (3, 3, 3), WOLF_DARK, deco=lambda p, f, mm, r: p.at(f["north"], 1, 0, (20, 20, 26)))
    m.cube(head, (-3, 16, -10), (2, 2, 1), WOLF_DARK, mirror_x=True)                                  # ears
    tail = m.bone("tail", "body", (0, 13, 7), rotation=(-40, 0, 0))
    m.cube(tail, (-1, 12, 7), (2, 2, 8), WOLF_FUR)
    for name, x, z in (("front_left_leg", 1.5, -5), ("front_right_leg", -3.5, -5), ("back_left_leg", 1.5, 4), ("back_right_leg", -3.5, 4)):
        leg = m.bone(name, "root", (x + 1, 8, z + 1))
        m.cube(leg, (x, 0, z), (2, 8, 2), WOLF_DARK if "back" in name else WOLF_FUR)
    anims = {
        "idle": anim(2.0, {"tail": rot(*swing([-40, -10, 0], [-40, 10, 0], 2.0)),
                           "head": rot(*swing([0, 0, 0], [6, 0, 0], 2.0))}),
        "walk": anim(0.5, {"front_left_leg": rot(*swing([35, 0, 0], [-35, 0, 0], 0.5)),
                           "back_right_leg": rot(*swing([35, 0, 0], [-35, 0, 0], 0.5)),
                           "front_right_leg": rot(*swing([-35, 0, 0], [35, 0, 0], 0.5)),
                           "back_left_leg": rot(*swing([-35, 0, 0], [35, 0, 0], 0.5)),
                           "tail": rot(*swing([-30, -15, 0], [-30, 15, 0], 0.5))}),
        "attack": anim(0.45, {"head": rot((0.0, [0, 0, 0]), (0.15, [-30, 0, 0]), (0.3, [20, 0, 0]), (0.45, [0, 0, 0])),
                              "body": rotpos([(0.0, [0, 0, 0]), (0.15, [-12, 0, 0]), (0.45, [0, 0, 0])],
                                             [(0.0, [0, 0, 0]), (0.15, [0, 2, -2]), (0.45, [0, 0, 0])])}, loop=False),
    }
    m.write({"rime_wolf": None}, anims, (2, 2))


# ------------------------------------------------------------------------------------------------ Act III

SILK = mat((60, 20, 80), (120, 50, 150), (190, 120, 220), "cloth", noise=5)
SILK_TEAL = mat((20, 80, 90), (40, 150, 160), (120, 220, 220), "cloth", ragged=3, noise=5)
VEIL_SKIN = mat((150, 120, 170), (200, 170, 220), (240, 220, 255), "plain", noise=4)
BANGLE = mat((140, 96, 26), (220, 170, 60), (255, 230, 140), "metal", noise=3)
PINK = (255, 120, 220)


def mirage_dancer():
    m = Model("mirage_dancer", 128)
    m.bone("root")
    for side, sx in (("left", 1), ("right", -1)):
        leg = m.bone(side + "_leg", "root", (1.5 * sx, 12, 0))
        m.cube(leg, ((0 if sx > 0 else -3), 0, -1.5), (3, 12, 3), SILK)
    body = m.bone("body", "root", (0, 12, 0))
    m.cube(body, (-3.5, 12, -1.5), (7, 12, 3), SILK, deco=band((230, 190, 90), 4))
    m.cube(body, (-4.5, 6, -2.5), (9, 9, 5), SILK_TEAL, inflate=0.2)                                  # the flowing skirt
    head = m.bone("head", "body", (0, 24, 0))
    m.cube(head, (-3, 24, -3), (6, 7, 6), VEIL_SKIN, deco=eyes(PINK, 3, 1, 2))
    m.cube(head, (-3.5, 23, -3.5), (7, 9, 7), SILK_TEAL, inflate=0.2, deco=clear_front_below(2))      # the veil
    m.cube(head, (-1, 31, -1), (2, 2, 2), BANGLE)                                                    # a jewel on the crown
    for side, sx in (("left", 1), ("right", -1)):
        arm = m.bone(side + "_arm", "body", (4.5 * sx, 22, 0), rotation=(0, 0, 25 * -sx))
        m.cube(arm, ((3.5 if sx > 0 else -6.5), 11, -1.5), (3, 12, 3), VEIL_SKIN)
        m.cube(arm, ((3.4 if sx > 0 else -6.6), 13, -1.6), (3.2, 2, 3.2), BANGLE)
        ribbon = m.bone(side + "_ribbon", side + "_arm", ((5 if sx > 0 else -5), 12, 0))
        m.cube(ribbon, ((5 if sx > 0 else -6), 2, 1), (1, 10, 4), SILK_TEAL)
    anims = {
        "idle": anim(2.0, {"body": rotpos(swing([0, -8, 0], [0, 8, 0], 2.0), swing([0, 0, 0], [0, 0.6, 0], 1.0)),
                           "left_arm": rot(*swing([-20, 0, -25], [-40, 0, -45], 2.0)),
                           "right_arm": rot(*swing([-40, 0, 45], [-20, 0, 25], 2.0)),
                           "left_ribbon": rot(*swing([10, 0, 0], [-20, 0, 0], 1.0)),
                           "right_ribbon": rot(*swing([-20, 0, 0], [10, 0, 0], 1.0))}),
        "walk": humanoid_walk(0.9, 22, 30, body=rot(*swing([0, -10, 0], [0, 10, 0], 0.9))),
        "attack": anim(0.6, {"body": rot((0.0, [0, 0, 0]), (0.3, [0, 180, 0]), (0.6, [0, 360, 0])),
                             "left_arm": rot((0.0, [0, 0, -25]), (0.3, [-90, 0, -60]), (0.6, [0, 0, -25])),
                             "right_arm": rot((0.0, [0, 0, 25]), (0.3, [-90, 0, 60]), (0.6, [0, 0, 25]))}, loop=False),
    }
    m.write({"mirage_dancer": None}, anims, (1.4, 2.4))


LINEN = mat((120, 108, 80), (176, 162, 126), (214, 202, 170), "cloth", fold=1, noise=6)
LINEN_MUD = mat((50, 44, 30), (98, 86, 60), (150, 136, 100), "cloth", ragged=3, noise=8)
MOSS = (70, 110, 50)
MARSH_GLOW = (140, 255, 120)


def bog_mummy():
    m = Model("bog_mummy", 128)
    body, head = humanoid(m, LINEN, LINEN, LINEN_MUD, LINEN, hunch=10, scale=1.2,
                          head_deco=combine(stripes((150, 136, 104), 2), eyes(MARSH_GLOW, 3, 1, 3)),
                          body_deco=combine(stripes((150, 136, 104), 2), spots(MOSS, 0.06)))
    m.cube(body, (-1.8, 21, -3.1), (3.6, 3.6, 1), BANGLE)                                            # a scarab amulet
    m.cube(body, (-5.4, 12, -3), (10.8, 6, 6), LINEN_MUD, inflate=0.2, deco=spots(MOSS, 0.14))       # mud-soaked hips
    m.cube(body, (-5.8, 25, -3.2), (11.6, 4, 6.4), mat((40, 70, 30), (70, 110, 50), (110, 150, 80), "fur", ragged=3, noise=8),
           deco=veins(MARSH_GLOW, 0.02))                                                            # moss hanging from the shoulders
    anims = melee_anims(1.8, 16, 10, body=rot(*swing([10, 0, 4], [10, 0, -4], 1.8)))
    anims["idle"] = anim(3.0, {"body": rot(*swing([10, 0, 0], [12, 0, 2], 3.0)),
                               "right_arm": rot(*swing([-80, 0, 4], [-74, 0, 8], 3.0)),
                               "left_arm": rot(*swing([0, 0, -4], [0, 0, -6], 3.0))})
    anims["walk"] = humanoid_walk(1.8, 16, 6, body=rot(*swing([10, 0, 4], [10, 0, -4], 1.8)),
                                  extra={"right_arm": rot(*swing([-80, 0, 4], [-70, 0, 4], 1.8))})
    m.write({"bog_mummy": None}, anims, (1.4, 2.4))


# ------------------------------------------------------------------------------------------------ Act IV: Aureum

GILT = mat((110, 74, 20), (196, 146, 50), (250, 214, 120), "metal", rivets=True, seam=3, cracks=((40, 30, 24), 0.004), noise=4)
GILT_PLAIN = mat((110, 74, 20), (196, 146, 50), (250, 214, 120), "metal", noise=4)
TUNIC = mat((70, 10, 14), (140, 24, 30), (196, 60, 60), "cloth", ragged=2, noise=5)
SHADOW_FACE = mat((10, 8, 12), (24, 20, 28), (40, 36, 46), "plain", noise=3)
SHIELD_FACE = mat((90, 12, 16), (150, 26, 30), (200, 60, 60), "plain", noise=5)
CREST = mat((90, 10, 14), (170, 30, 34), (230, 80, 80), "fur", noise=8)
SPEAR = mat((60, 40, 24), (110, 80, 50), (150, 116, 80), "cloth", noise=5)
GOLD_GLOW = (255, 210, 90)


def scutum(p, f, m, rnd):
    """The red scutum's face: a gold rim, a gold boss in the middle and lightning wings beside it."""
    x, y, w, h = f["east"]
    gold, dark = (232, 180, 60), (150, 100, 24)
    for px in range(w):
        for py in range(h):
            if px in (0, w - 1) or py in (0, h - 1):
                p.at(f["east"], px, py, gold if (px + py) % 2 else dark)
    cx, cy = w // 2, h // 2
    for px in range(cx - 1, cx + 1):
        for py in range(cy - 1, cy + 1):
            p.at(f["east"], px, py, GOLD_GLOW, glow=True)
    for i in range(1, 4):
        p.at(f["east"], cx - 1 - i, cy - i, gold)
        p.at(f["east"], cx + i, cy - i, gold)
        p.at(f["east"], cx - 1 - i, cy + i, gold)
        p.at(f["east"], cx + i, cy + i, gold)


def gilded_legionnaire():
    m = Model("gilded_legionnaire", 128)
    body, head = humanoid(m, SHADOW_FACE, GILT, TUNIC, GILT, scale=1.05,
                          head_deco=eyes(GOLD_GLOW, 4, 1, 2), body_deco=stripes((150, 110, 30), 3))
    m.cube(head, (-4.6, 29, -4.6), (9.2, 5, 9.2), GILT_PLAIN, deco=band((120, 80, 20), 4))            # the galea
    m.cube(head, (-4.6, 25, -4.6), (1, 5, 6), GILT_PLAIN, mirror_x=True)                              # cheek guards
    m.cube(head, (-0.8, 34, -4), (1.6, 3, 9), CREST)                                                  # the red crest
    m.cube(body, (-4.5, 10, -2.5), (9, 4, 5), TUNIC, inflate=0.2)                                     # the tunic's hem
    m.cube(body, (-4.8, 2, 2.4), (9.6, 23, 1), TUNIC)                                                  # the red cloak
    shield = m.bone("shield", "left_arm", (8, 16, 0))
    m.cube(shield, (8.5, 6, -6), (1, 16, 12), SHIELD_FACE, deco=scutum)
    spear = m.bone("spear", "right_arm", (-7, 12, 0), rotation=(80, 0, 0))
    m.cube(spear, (-7.5, 11, -22), (1, 1, 30), SPEAR)
    m.cube(spear, (-8, 10.5, -26), (2, 2, 4), GILT_PLAIN)
    anims = melee_anims(1.3, 24, 10, swing_arm="right_arm")
    anims["attack"] = anim(0.6, {"right_arm": rot((0.0, [0, 0, 0]), (0.2, [-30, 0, 0]), (0.35, [-80, 0, 0]), (0.6, [0, 0, 0])),
                                 "spear": rotpos([(0.0, [0, 0, 0]), (0.35, [0, 0, 0]), (0.6, [0, 0, 0])],
                                                 [(0.0, [0, 0, 0]), (0.2, [0, 0, 3]), (0.35, [0, 0, -6]), (0.6, [0, 0, 0])])}, loop=False)
    m.write({"gilded_legionnaire": None}, anims, (2, 3))


SHADE = mat((16, 12, 22), (40, 32, 52), (78, 64, 96), "void", cracks=((220, 220, 255), 0.006), noise=6)
SHADE_RAG = mat((10, 8, 14), (28, 24, 36), (54, 48, 66), "cloth", ragged=4, noise=5)
BRONZE = mat((80, 46, 20), (150, 96, 46), (210, 150, 90), "metal", rivets=True, noise=5)
BLADE = mat((90, 96, 104), (170, 176, 186), (230, 236, 246), "metal", noise=3)
WHITE_GLOW = (240, 240, 255)


def gladiator_shade():
    m = Model("gladiator_shade", 128)
    body, head = humanoid(m, SHADE, SHADE, SHADE, SHADE,
                          body_deco=rift((200, 190, 255), 0.5, 2, 2))
    m.cube(head, (-4.6, 23, -4.6), (9.2, 11, 9.2), SHADE_RAG, inflate=0.3, deco=clear_front_below(1))  # a deep hood
    m.cube(head, (-3.5, 25, -4.4), (7, 6, 1), mat((4, 2, 8), (10, 6, 16), (20, 14, 30), "plain", noise=2),
           deco=centered(["", "WW..WW", "", ""], {"W": (230, 190, 255)}, top=1, glow_chars="W"))       # nothing in it but two eyes
    m.cube(body, (-4.5, 10, -2.5), (9, 5, 5), SHADE_RAG, inflate=0.2)
    m.cube("right_arm", (-8.4, 14, -2.4), (4.8, 8, 4.8), BRONZE, deco=stripes((90, 50, 20), 2))         # the manica
    for side, sx in (("left", 1), ("right", -1)):
        blade = m.bone(side + "_blade", side + "_arm", ((6 if sx > 0 else -6), 11, 0), rotation=(80, 0, 0))
        m.cube(blade, ((5.5 if sx > 0 else -6.5), 10.5, -12), (1, 1, 12), BLADE)
        m.cube(blade, ((5 if sx > 0 else -7), 10, -1), (2, 2, 2), BRONZE)
    anims = melee_anims(0.9, 30, 26)
    anims["attack"] = anim(0.5, {"right_arm": rot((0.0, [0, 0, 0]), (0.15, [-110, 30, 0]), (0.3, [10, -30, 0]), (0.5, [0, 0, 0])),
                                 "left_arm": rot((0.0, [0, 0, 0]), (0.25, [-110, -30, 0]), (0.4, [10, 30, 0]), (0.5, [0, 0, 0])),
                                 "body": rot((0.0, [0, 0, 0]), (0.2, [0, 20, 0]), (0.4, [0, -20, 0]), (0.5, [0, 0, 0]))}, loop=False)
    m.write({"gladiator_shade": None}, anims, (1.4, 2.4))


if __name__ == "__main__":
    sand_ghoul()
    clockwork_scarab()
    draugr()
    rime_wolf()
    mirage_dancer()
    bog_mummy()
    gilded_legionnaire()
    gladiator_shade()
