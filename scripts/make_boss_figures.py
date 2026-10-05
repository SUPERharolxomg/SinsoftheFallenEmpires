"""The bosses, remade so their presence shows they are broken (the user's bestiary sheet,
art/concepts/bosses/bestiary_sheet.png, and "que su presencia demuestre que estan rotos"):

- a body of boss proportions (creature_kit.figure): a chest wider than the waist, shoulders, elbows and knees that
  bend, a neck, hands with fingers and a thumb;
- a face sculpted in 3D (creature_kit.sculpted_face): a brow that juts and frowns, nose, cheekbones, a jaw that
  opens, teeth, eyes of light set deep;
- what broke them, worn on the body: the Broken Oaths carry the shackles of their oath torn open and the seal of
  their Law split by a crack of light; the Archsins a crown or halo broken, pieces of themselves floating round;
- a second phase (SoFEBossEntity.shownPhase): the _broken texture splits every surface with fractures of the boss's
  colour, p2_ bones break out of the body and p1_ bones are gone.

Run from the repository root: python scripts/make_boss_figures.py [name ...]
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from make_mob_models import Model, mat, mix  # noqa: E402
from creature_kit import (figure, sculpted_face, figure_anims, shackle, rag_strips, law_seal, curved_horn, spike,  # noqa: E402
                          shard_ring, broken_halo, broken_form, glow_mat, shaft, bat_wing, veins, centered, claws, hand,
                          combine_decos, signature_anim)


def steel(dark, mid, light, **o):
    return mat(dark, mid, light, "metal", **o)


# ------------------------------------------------------------------------------------------------ Kaleth, Law I

FIRE = (255, 130, 40)


def kaleth():
    """Kaleth, the Burning Blade (Law I): a horned warlord in blackened iron under a mantle of red fur, fire in
    every crack, his right horn snapped off, a burning greatsword. He broke the first Law: the shackles hang from
    his wrists and the seal of Law I burns split on his chest."""
    m = Model("kaleth", 512, density=2, scale=1.25)
    iron = steel((20, 16, 18), (52, 44, 46), (104, 92, 90), rivets=True, seam=4, cracks=(FIRE, 0.006), noise=5)
    skin = mat((60, 22, 18), (110, 46, 36), (160, 86, 66), "skin_cracked", cracks=(FIRE, 0.01), noise=6)
    leather = mat((30, 18, 12), (64, 40, 26), (100, 68, 44), "cloth", noise=5)
    red_fur = mat((60, 6, 6), (134, 22, 16), (200, 70, 46), "fur", ragged=3, noise=10)
    horn_m = mat((40, 30, 26), (120, 100, 82), (210, 194, 166), "plain", noise=5)
    ember = glow_mat(FIRE)
    eye = glow_mat((255, 200, 80))
    p = figure(m, 1.0, {"skin": skin, "chest": iron, "waist": leather, "upper_arm": skin, "forearm": iron, "hand": skin,
                        "thigh": leather, "shin": iron, "foot": iron, "pads": iron, "neck": skin},
               chest=(12, 8.5, 7), waist=(8, 4, 5.5), arm=3.8, leg=4.4, hunch=12, head=8, claw=horn_m)
    cx0, cy0, cz0, cw, ch, cd = p["chest"]
    m.cube("body", (cx0 + cw / 2 - 3, cy0 + 1.5, cz0 - 0.4), (6, 6, 0.6), ember, deco=law_seal(1, (255, 170, 60)))   # the seal of Law I
    m.cube("body", (cx0 - 1.6, cy0 + ch - 3.6, cz0 - 1.6), (cw + 3.2, 5, cd + 3.2), red_fur, inflate=0.2)              # the mantle of red fur
    m.cube("body", (-4.6, p["hips"] - 3.4, -3.4), (9.2, 1.6, 6.8), leather)                                            # the belt
    m.cube("body", (-1.4, p["hips"] - 3.8, -3.8), (2.8, 2.4, 0.6), steel((90, 60, 20), (180, 130, 50), (240, 200, 110)))
    rags = rag_strips(m, "body", "cape", p["top"] - 1, 11, 20, 4, mat((40, 6, 6), (96, 18, 14), (150, 40, 30), "cloth", ragged=3, cracks=(FIRE, 0.004)),
                      p["back"] + 1.0, lengths=(1.0, 0.8, 0.95, 0.7))
    for side, sx in (("left", 1), ("right", -1)):
        a = p["arms"][side]
        shackle(m, side + "_forearm", side + "_chain", (a["wrist"][0], a["wrist"][1] + 2.4, 0), a["half"] + 0.3, 4, iron, spark=ember, side=sx)
        spike(m, side + "_arm", (a["shoulder"][0] + sx * 2.4, a["shoulder"][1] + 1.2, 0), (sx * 0.6, 1, 0.2), 5, 1.6, horn_m)
    head = "head"
    sculpted_face(m, head, p["head"], skin, eye, kind="demon", brow=iron, anger=18)
    hx, hy, hz, hs = p["head"]
    m.cube(head, (hx - 0.5, hy + hs * 0.55, hz - 0.5), (hs + 1, hs * 0.5, hs + 1), iron,
           deco=lambda pp, f, mm, r: [pp.clear(f["north"][0] + x, f["north"][1] + y) for x in range(2, f["north"][2] - 2) for y in range(f["north"][3] - 2, f["north"][3])])  # the helm
    m.cube(head, (hx + hs / 2 - 0.5, hy + hs * 0.55, hz - 0.7), (1, hs * 0.5, 0.6), iron)                                # its nasal
    curved_horn(m, head, (hx + hs - 0.2, hy + hs * 0.85, hz + hs * 0.45), (1, 0.6, 0.1), (0, 0, 1), 16, 2.8, horn_m, segments=6, bend=24)
    curved_horn(m, head, (hx + 0.2, hy + hs * 0.85, hz + hs * 0.45), (-1, 0.6, 0.1), (0, 0, -1), 16, 2.8, horn_m, segments=6, bend=24,
                broken=True, break_mat=ember)                                                                           # snapped off
    a = p["arms"]["right"]
    sword = m.bone("sword", "right_forearm", a["hand_bottom"], rotation=(-70, 0, 0))
    bx, by, bz = a["hand_bottom"]
    m.cube(sword, (bx - 0.8, by - 1, bz - 1.5), (1.6, 4.5, 1.6), leather)
    m.cube(sword, (bx - 3.5, by - 2.4, bz - 1), (7, 1.4, 2), iron)
    m.cube(sword, (bx - 1.6, by - 26, bz - 0.4), (3.2, 23.6, 0.8), mat((200, 60, 10), (255, 140, 40), (255, 230, 140), "flame"))
    # the second phase: the fire breaks out of him, a crown of flame and embers of his armour circling
    crown = m.bone("p2_crown", head, (0, hy + hs, hz + hs / 2))
    for i in range(7):
        x = hx + 0.6 + i * (hs - 1.2) / 6
        spike(m, crown, (x, hy + hs + 0.3, hz + hs / 2 + (i % 2) * 1.5), (0, 1, 0.15 * (i - 3) / 3), 3 + (i % 3) * 1.5, 1.2, ember, segments=2)
    shard_ring(m, "root", "p2_embers", (0, 26, 0), 16, 9, iron, length=4, width=1.6, rise=5, seed=11)
    anims = figure_anims(1.15, 26, heavy=True, rags=rags, chains=("left_chain", "right_chain"), orbit={"p2_embers": 4.0})
    anims["signature"] = signature_anim(*SIGNATURES["kaleth"])
    m.write({"kaleth": None, "kaleth_broken": broken_form(FIRE)}, anims, (5, 5))



# ------------------------------------------------------------------------------------------------ shared dressing

def held(m, side, places, name, tilt=-70, roll=0):
    """A bone in the fist of side, its weapon hanging down from the grip (cubes below hand_bottom) and swung
    forward by tilt."""
    hb = places["arms"][side]["hand_bottom"]
    grip = (hb[0], hb[1] + 2.2, hb[2])
    return m.bone(name, side + "_forearm", grip, rotation=(tilt, 0, roll)), grip


def lady(m, mats, s=1.0, **kw):
    """A slender figure (the women of the Oaths and Archsins)."""
    args = dict(chest=(8.6, 7.5, 5.2), waist=(6.2, 4.5, 4.4), arm=2.8, leg=3.4, hunch=4, head=7.6, arm_len=(8, 8),
                leg_len=(8, 8.5), shoulder_pads=False, finger_len=3.2)
    args.update(kw)
    return figure(m, s, mats, **args)


def gown(m, bone, hips, width, depth, length, material, flare=1.6, tiers=3):
    """A long skirt flaring to the ground in tiers (it hides the legs)."""
    top = hips + 1
    step = length / tiers
    for i in range(tiers):
        w = width + flare * 2 * (i + 1)
        d = depth + flare * (i + 1)
        m.cube(bone, (-w / 2, top - step * (i + 1), -d / 2), (w, step + 0.4, d), material)


def long_hair(m, places, material, length=16, strips=4, width=None):
    """Hair over the crown and falling down the back in locks that sway; returns the lock bones."""
    hx, hy, hz, hs = places["head"]
    m.cube("head", (hx - 0.4, hy + hs * 0.74, hz - 0.4), (hs + 0.8, hs * 0.34, hs + 0.8), material)
    m.cube("head", (hx - 0.4, hy + hs * 0.2, hz + hs * 0.35), (0.6, hs * 0.5, hs * 0.7), material)
    m.cube("head", (hx + hs - 0.2, hy + hs * 0.2, hz + hs * 0.35), (0.6, hs * 0.5, hs * 0.7), material)
    return rag_strips(m, "head", "hair", hy + hs * 0.9, width or hs + 0.8, length, strips, material, hz + hs + 0.3,
                      lengths=(0.85, 1.0, 0.95, 0.8, 1.0), flare=3, thickness=1.2)


def seal_and_shackles(m, places, law, color, metal, spark, links=4):
    """What every Broken Oath wears: the seal of its Law split on the chest, and the torn shackles on its wrists."""
    cx0, cy0, cz0, cw, ch, cd = places["chest"]
    size = min(cw * 0.55, ch * 0.75)
    m.cube("body", (cx0 + cw / 2 - size / 2, cy0 + ch / 2 - size / 2, cz0 - 0.5), (size, size, 0.6), glow_mat(mix(color, (0, 0, 0), 0.5)),
           deco=law_seal(law, color))
    names = []
    for side, sx in (("left", 1), ("right", -1)):
        a = places["arms"][side]
        names.append(shackle(m, side + "_forearm", side + "_chain", (a["wrist"][0], a["wrist"][1] + 2.2, 0), a["half"] + 0.2, links, metal,
                             spark=spark, side=sx))
    return names


# ------------------------------------------------------------------------------------------------ Serath, Law II

BLOOD = (235, 30, 50)


def serath():
    """Serath, the Blood Maiden (Law II): a pale shield-maiden drenched in blood, a mane of red hair in wet locks,
    eyes and mouth of red light, fangs; blades of bone grown from her forearms."""
    m = Model("serath", 512, density=2, scale=1.1)
    pale = mat((150, 120, 124), (200, 176, 178), (236, 222, 222), "plain", cracks=(BLOOD, 0.004), noise=4)
    crimson = mat((40, 4, 10), (96, 14, 26), (156, 36, 48), "cloth", noise=5)
    leather = mat((30, 10, 12), (66, 26, 28), (104, 52, 50), "metal", seam=3, noise=5)
    hair = mat((60, 4, 10), (130, 16, 24), (196, 50, 50), "fur", noise=8)
    bone = mat((120, 104, 90), (190, 176, 156), (236, 228, 210), "plain", noise=4)
    blood = glow_mat(BLOOD)
    p = lady(m, {"skin": pale, "chest": leather, "waist": crimson, "upper_arm": pale, "forearm": leather, "hand": pale,
                 "thigh": crimson, "shin": leather, "foot": leather, "neck": pale}, claw=blood)
    face = centered(["", "", "", "", "", "", "..R..R..", "..R..R..", "...RR..."], {"R": BLOOD}, top=0, glow_chars="R")   # blood running from eyes
    sculpted_face(m, "head", p["head"], pale, glow_mat((255, 60, 70)), kind="noble", lips=glow_mat((200, 20, 40)), deco=face, eye_size=(1.8, 0.7))
    hx, hy, hz, hs = p["head"]
    for sx in (1, -1):                                                   # fangs over the lip
        m.cube("head", (hx + hs / 2 + sx * 0.9 - 0.3, hy + hs * 0.2, hz - 0.3), (0.6, 0.9, 0.5), bone)
    locks = long_hair(m, p, hair, length=19, strips=5)
    chains = seal_and_shackles(m, p, 2, (255, 70, 80), leather, blood)
    gown(m, "body", p["hips"], 6.6, 5, 9, mat((40, 4, 10), (96, 14, 26), (156, 36, 48), "cloth", ragged=3, cracks=(BLOOD, 0.006)), flare=1.0, tiers=2)
    for side, sx in (("left", 1), ("right", -1)):                       # blades of bone grown from the forearms
        a = p["arms"][side]
        spike(m, side + "_forearm", (a["elbow"][0] + sx * 1.6, a["elbow"][1] - 2, 0.6), (sx * 0.25, -1, -0.9), 12, 1.4, bone, tip=blood)
        spike(m, side + "_arm", (a["shoulder"][0] + sx * 1.4, a["shoulder"][1] + 0.6, 0.4), (sx * 0.5, 1, 0.3), 4, 1.2, bone)
    cx0, cy0, cz0, cw, ch, cd = p["chest"]
    for i in range(5):                                                   # blood running down her front
        m.cube("body", (cx0 + 1 + i * (cw - 2) / 4, cy0 - 3 - (i % 3) * 2, cz0 - 0.35), (0.6, 3 + (i % 3) * 2, 0.3), blood)
    shard_ring(m, "root", "p2_blood", (0, 22, 0), 13, 10, blood, length=2.4, width=1.0, rise=6, seed=22)
    anims = figure_anims(0.9, 30, rags=locks, chains=chains, orbit={"p2_blood": 3.0}, two_handed=True)
    anims["signature"] = signature_anim(*SIGNATURES["serath"])
    m.write({"serath": None, "serath_broken": broken_form(BLOOD)}, anims, (4, 4))


# ------------------------------------------------------------------------------------------------ Mirael, Law III

VIOLET = (210, 120, 255)


def mirael():
    """Mirael, the Veiled Saint (Law III): a woman who floats in layers of violet silk, a deep hood over a pale
    face with eyes of violet light, her silver halo cracked open and its pieces hanging askew, hands open."""
    m = Model("mirael", 512, density=2, scale=1.3)
    veil = mat((40, 20, 70), (104, 64, 158), (186, 156, 230), "cloth", ragged=3, noise=4)
    silk = mat((70, 40, 110), (150, 110, 210), (220, 200, 250), "cloth", fold=1, noise=3)
    pale = mat((150, 130, 160), (206, 186, 214), (242, 230, 246), "plain", noise=3)
    silver = steel((100, 104, 120), (176, 180, 196), (240, 244, 252), noise=3)
    eye = glow_mat(VIOLET)
    p = lady(m, {"skin": pale, "chest": silk, "waist": silk, "upper_arm": silk, "forearm": pale, "hand": pale, "thigh": veil,
                 "shin": veil, "foot": veil}, forearm_bend=-30, finger_len=3.6)
    sculpted_face(m, "head", p["head"], pale, eye, kind="noble", lips=mat((90, 50, 110), (130, 80, 150), (170, 120, 190)))
    hx, hy, hz, hs = p["head"]
    m.cube("head", (hx - 1, hy - 0.5, hz - 1), (hs + 2, hs + 2, hs + 2), veil, inflate=0.2,
           deco=lambda pp, f, mm, r: [pp.clear(f["north"][0] + x, f["north"][1] + y) for x in range(2, f["north"][2] - 2) for y in range(2, f["north"][3])]
           + [pp.clear(f["down"][0] + x, f["down"][1] + y) for x in range(f["down"][2]) for y in range(f["down"][3])])  # the deep hood
    chains = seal_and_shackles(m, p, 3, VIOLET, silver, eye)
    gown(m, "body", p["hips"], 7, 5.4, 16, veil, flare=0.8, tiers=6)
    rags = rag_strips(m, "body", "veil", p["top"], 12, 26, 5, veil, p["back"] + 0.8, lengths=(1, 0.8, 0.95, 0.7, 0.9))
    halo = m.bone("halo", "head", (0, hy + hs * 0.8, hz + hs + 2))
    broken_halo(m, halo, (0, hy + hs * 0.9, hz + hs + 2), 7.5, 1.2, silver, gaps=((20, 55), (230, 260)), loose=silver)
    for side, sx in (("left", 1), ("right", -1)):                       # silk trailing from the arms
        a = p["arms"][side]
        trail = m.bone(side + "_trail", side + "_forearm", a["elbow"])
        m.cube(trail, (a["elbow"][0] + sx * 1.2 - 0.5, a["elbow"][1] - 14, 0.5), (1, 14, 4), silk)
    shard_ring(m, "root", "p2_halo", (0, 34, 0), 12, 8, glow_mat((240, 220, 255)), length=3, width=1.2, rise=3, seed=33)
    anims = figure_anims(1.4, 10, rags=rags, chains=chains, orbit={"p2_halo": 5.0},
                         extra_idle={"root": {"position": {"0.0": [0.0, 2.0, 0.0], "1.6": [0.0, 4.0, 0.0], "3.2": [0.0, 2.0, 0.0]}},
                                     "halo": {"rotation": {"0.0": [0.0, 0.0, -8.0], "1.6": [0.0, 0.0, 8.0], "3.2": [0.0, 0.0, -8.0]}},
                                     "left_arm": {"rotation": {"0.0": [-20.0, 0.0, -24.0], "1.6": [-34.0, 0.0, -34.0], "3.2": [-20.0, 0.0, -24.0]}},
                                     "right_arm": {"rotation": {"0.0": [-34.0, 0.0, 34.0], "1.6": [-20.0, 0.0, 24.0], "3.2": [-34.0, 0.0, 34.0]}}},
                         extra_walk={"root": {"position": {"0.0": [0.0, 2.0, 0.0], "0.7": [0.0, 3.0, 0.0], "1.4": [0.0, 2.0, 0.0]}}})
    anims["signature"] = signature_anim(*SIGNATURES["mirael"])
    m.write({"mirael": None, "mirael_broken": broken_form(VIOLET)}, anims, (4, 5))


# ------------------------------------------------------------------------------------------------ Thessyn, Law IV

SPIDER = (150, 255, 120)


def thessyn():
    """Thessyn, the Weaver (Law IV): a pale woman to the waist on the body of a great black spider, long black
    hair, six green eyes, claws; eight legs that bend at every joint, the red hourglass on her back."""
    m = Model("thessyn", 512, density=2, scale=1.35)
    chitin = steel((10, 12, 12), (30, 38, 34), (76, 92, 82), seam=3, cracks=(SPIDER, 0.003), noise=4)
    pale = mat((140, 130, 140), (200, 190, 198), (236, 230, 236), "plain", noise=3)
    hair = mat((6, 6, 8), (22, 22, 28), (50, 50, 60), "fur", noise=6)
    silk = mat((170, 176, 160), (214, 218, 204), (244, 246, 236), "cloth", fold=1, noise=4)
    eye = glow_mat(SPIDER)
    m.bone("root")
    abdomen = m.bone("abdomen", "root", (0, 12, 2))
    m.cube(abdomen, (-5, 8, -5), (10, 8, 9), chitin)                                                    # the cephalothorax
    m.cube(abdomen, (-7, 7, 4), (14, 12, 16), chitin, deco=veins(SPIDER, 0.004))                         # the great abdomen
    m.cube(abdomen, (-1.5, 19, 9), (3, 0.6, 6), glow_mat((230, 30, 40)),
           deco=centered(["XXX", ".X.", "XXX"], {"X": (255, 60, 60)}, face="up", glow_chars="X"))       # the red hourglass
    for i, (z, ang) in enumerate(((-3, -40), (0, -12), (3, 14), (6, 38))):
        for side, sx in (("l", 1), ("r", -1)):
            leg = m.bone("leg_%s%d" % (side, i), "abdomen", (4.5 * sx, 12, z))
            import math
            out = (sx * math.cos(math.radians(ang)), 0.9, math.sin(math.radians(ang)))
            knee = spike(m, leg, (4.5 * sx, 12, z), out, 15, 2.6, chitin, segments=2)
            curved_horn(m, leg, knee, (out[0] * 0.5, -1, out[2] * 0.5), (out[2], 0, -out[0]), 21, 2.2, chitin, segments=3, bend=-8 * sx, taper=0.72)
    body = m.bone("body", "abdomen", (0, 15, -3), rotation=(-6, 0, 0))
    m.cube(body, (-3.4, 15, -5), (6.8, 5, 4.2), pale)
    m.cube(body, (-4.2, 20, -5.4), (8.4, 7.5, 5), silk)
    m.bone("neck", "body", (0, 27.5, -3))
    m.cube("neck", (-1.3, 27, -4.2), (2.6, 2, 2.6), pale)
    m.bone("head", "neck", (0, 29, -3.2))
    place = (-4.3, 29, -7.4, 8.6)
    sculpted_face(m, "head", place, pale, eye, kind="noble", lips=mat((20, 30, 24), (40, 60, 50), (70, 90, 80)), eye_size=(1.2, 0.7))
    for x, y in ((-3.2, 35.4), (2.4, 35.4), (-1.8, 36.6), (1.0, 36.6)):                                  # her other eyes
        m.cube("head", (x, y, -7.8), (0.8, 0.6, 0.3), eye)
    p = {"head": place}
    locks = long_hair(m, p, hair, length=14, strips=4)
    arms = {}
    for side, sx in (("left", 1), ("right", -1)):
        m.bone(side + "_arm", "body", (5 * sx, 26, -3), rotation=(0, 0, -10 * sx))
        m.cube(side + "_arm", (5 * sx - 1.3, 19, -4.3), (2.6, 8, 2.6), pale)
        m.bone(side + "_forearm", side + "_arm", (5 * sx, 19.5, -3), rotation=(-40, 0, 0))
        m.cube(side + "_forearm", (5 * sx - 1.3, 12, -4.3), (2.6, 8, 2.6), chitin)
        hand(m, side + "_forearm", (5 * sx, 12, -3), 2.8, pale, sx, claw=chitin, finger_len=3)
        arms[side] = True
    m.cube("body", (-2.5, 21, -5.9), (5, 5, 0.6), glow_mat((70, 120, 60)), deco=law_seal(4, SPIDER))
    chains = []
    for side, sx in (("left", 1), ("right", -1)):
        chains.append(shackle(m, side + "_forearm", side + "_chain", (5 * sx, 14.5, -3), 1.5, 3, chitin, spark=eye, side=sx))
    for k in range(4):                                                   # p2: mandibles split open, the fangs out
        pass
    p2 = m.bone("p2_fangs", "head", (0, 30, -6))
    for sx in (1, -1):
        curved_horn(m, p2, (sx * 1.6, 30, -7), (sx * 0.3, -1, -0.4), (0, 0, 1), 6, 1.2, glow_mat(SPIDER), segments=3, bend=-20 * sx)
    shard_ring(m, "root", "p2_eggs", (0, 18, 6), 14, 8, silk, length=2.6, width=2.0, rise=3, seed=44)
    legs = {}
    for i in range(4):
        for side in ("l", "r"):
            a, b = ([0, 0, 14], [0, 0, -10]) if (i + (side == "l")) % 2 else ([0, 0, -10], [0, 0, 14])
            legs["leg_%s%d" % (side, i)] = {"rotation": {"0.0": a, "0.3": b, "0.6": a}}
    anims = figure_anims(0.6, 0, rags=locks, chains=chains, orbit={"p2_eggs": 6.0}, two_handed=True,
                         extra_walk=dict(legs, abdomen={"position": {"0.0": [0, 0, 0], "0.15": [0, 0.6, 0], "0.3": [0, 0, 0], "0.45": [0, 0.6, 0], "0.6": [0, 0, 0]}}))
    for leg_bone in [k for k in anims["walk"]["bones"] if k.endswith("_leg") or k.endswith("_shin")]:
        del anims["walk"]["bones"][leg_bone]
    anims["signature"] = signature_anim(*SIGNATURES["thessyn"])
    m.write({"thessyn": None, "thessyn_broken": broken_form(SPIDER)}, anims, (5, 4))


# ------------------------------------------------------------------------------------------------ Dormiel, Law V

SLEEP = (120, 180, 255)


def dormiel():
    """Dormiel, the Sleepless Watch (Law V): a gaunt, stooped watchman of Khemet in a blue and gold nemes and a tall
    headdress, eyes sewn shut and leaking blue light, a lantern of cold fire in one hand and a staff in the other.
    In his second phase his eyes tear open."""
    m = Model("dormiel", 512, density=2, scale=1.25)
    skin = mat((70, 60, 50), (130, 116, 96), (180, 166, 140), "skin_cracked", cracks=(SLEEP, 0.004), noise=6)
    robe = mat((80, 66, 44), (146, 126, 90), (200, 182, 140), "cloth", ragged=2, noise=5)
    nemes = mat((20, 40, 90), (40, 70, 160), (90, 130, 220), "cloth", noise=4)
    gold = steel((110, 74, 20), (196, 146, 50), (250, 214, 120), noise=4)
    lantern_glow = glow_mat(SLEEP)
    p = figure(m, 1.0, {"skin": skin, "chest": robe, "waist": robe, "upper_arm": robe, "forearm": skin, "hand": skin, "thigh": robe,
                        "shin": robe, "foot": gold, "pads": gold}, chest=(9.5, 8, 5.6), waist=(7, 4, 4.8), arm=2.8, leg=3.6, hunch=22,
               arm_len=(10, 10), head=7.6, finger_len=3.6, claw=gold)
    hx, hy, hz, hs = p["head"]
    shut = centered(["", "", "", "", "", "", "", "B.BB..BB.B"], {"B": SLEEP}, top=0, glow_chars="B")
    sculpted_face(m, "head", p["head"], skin, mat((40, 50, 70), (60, 70, 90), (80, 90, 110)), kind="skull", eye_size=(1.8, 0.3), deco=shut)
    m.cube("head", (hx - 0.8, hy + hs * 0.35, hz - 0.6), (hs + 1.6, hs * 0.7, hs + 1.4), nemes,
           deco=lambda pp, f, mm, r: [pp.clear(f["north"][0] + x, f["north"][1] + y) for x in range(2, f["north"][2] - 2) for y in range(2, f["north"][3])]
           + [pp.at(f[n], x, y, (220, 180, 70)) for n in ("east", "west", "south") for x in range(f[n][2]) for y in range(f[n][3]) if y % 2 == 0])
    for sx in (1, -1):                                                  # the lappets of the nemes
        m.cube("head", (hx + (hs + 0.2 if sx > 0 else -2.0), hy - 5, hz + 1), (1.8, hs * 0.9, hs * 0.5), nemes,
               deco=lambda pp, f, mm, r: [pp.at(f["north"], x, y, (220, 180, 70)) for x in range(f["north"][2]) for y in range(f["north"][3]) if y % 2 == 0])
    m.cube("head", (hx + 0.6, hy + hs, hz + 0.6), (hs - 1.2, 9, hs - 1.2), nemes,
           deco=lambda pp, f, mm, r: [pp.at(f[n], x, y, (220, 180, 70)) for n in ("north", "east", "west", "south") for x in range(f[n][2]) for y in range(f[n][3]) if y % 3 == 0])
    m.cube("head", (hx + 0.2, hy + hs - 0.4, hz + 0.2), (hs - 0.4, 1.2, hs - 0.4), gold)
    m.cube("head", (-1, hy + hs + 9, hz + hs / 2 - 1), (2, 2.5, 2), gold)
    chains = seal_and_shackles(m, p, 5, SLEEP, gold, lantern_glow)
    gown(m, "body", p["hips"], 7, 5, 14, robe, flare=1.0, tiers=2)
    lantern, grip = held(m, "left", p, "lantern", tilt=0)
    gx, gy, gz = grip
    m.cube(lantern, (gx - 0.4, gy - 6, gz - 0.4), (0.8, 4, 0.8), gold)
    m.cube(lantern, (gx - 2, gy - 11, gz - 2), (4, 5, 4), lantern_glow)
    m.cube(lantern, (gx - 2.4, gy - 6.4, gz - 2.4), (4.8, 1, 4.8), gold)
    m.cube(lantern, (gx - 2.4, gy - 11.6, gz - 2.4), (4.8, 1, 4.8), gold)
    staff, sgrip = held(m, "right", p, "staff", tilt=-10)
    sx_, sy_, sz_ = sgrip
    m.cube(staff, (sx_ - 0.8, sy_ - 26, sz_ - 0.8), (1.6, 46, 1.6), gold)
    spike(m, staff, (sx_, sy_ + 20, sz_), (0, 1, 0), 6, 2.6, gold, tip=lantern_glow)
    p2 = m.bone("p2_eyes", "head", (0, hy + hs * 0.5, hz))                # the eyes torn open
    for sx in (1, -1):
        m.cube(p2, (sx * 1.9 - 1.2, hy + hs * 0.48, hz - 0.5), (2.4, 1.5, 0.4), glow_mat((200, 230, 255)))
    shard_ring(m, "root", "p2_lights", (0, 30, 0), 14, 7, lantern_glow, length=2, width=1.6, rise=4, seed=55)
    anims = figure_anims(1.6, 16, heavy=True, chains=chains, orbit={"p2_lights": 6.0}, idle_len=4.0,
                         extra_idle={"lantern": {"rotation": {"0.0": [0, 0, -6], "2.0": [0, 0, 6], "4.0": [0, 0, -6]}}})
    anims["signature"] = signature_anim(*SIGNATURES["dormiel"])
    m.write({"dormiel": None, "dormiel_broken": broken_form(SLEEP)}, anims, (4, 5))


# ------------------------------------------------------------------------------------------------ Goldarc, Law VI

COIN = (255, 220, 110)


def goldarc():
    """Goldarc, the Gilded Usurper (Law VI): a king of gold plate whose face is a gilded skull under a crown of
    points, a red cape in rags, a tower shield of coins and a broad sword."""
    m = Model("goldarc", 512, density=2, scale=1.25)
    plate = steel((110, 74, 20), (196, 146, 50), (250, 214, 120), rivets=True, seam=4, noise=4)
    dark = steel((60, 40, 12), (120, 84, 30), (180, 140, 60), noise=4)
    skull = mat((150, 110, 40), (220, 180, 90), (255, 236, 160), "plain", noise=3)
    cape = mat((70, 10, 14), (140, 24, 30), (196, 60, 60), "cloth", ragged=3, noise=5)
    eye = glow_mat((255, 60, 40))
    p = figure(m, 1.0, {"skin": skull, "chest": plate, "waist": dark, "upper_arm": dark, "forearm": plate, "hand": plate, "thigh": dark,
                        "shin": plate, "foot": plate, "pads": plate}, chest=(12, 8.5, 7), arm=3.8, leg=4.4, hunch=6, head=8)
    sculpted_face(m, "head", p["head"], skull, eye, kind="skull", brow=plate, anger=10)
    hx, hy, hz, hs = p["head"]
    m.cube("head", (hx - 0.4, hy + hs * 0.86, hz - 0.4), (hs + 0.8, 1.6, hs + 0.8), plate)            # the crown
    for i in range(6):
        x = hx + 0.2 + i * (hs - 0.4) / 5
        spike(m, "head", (x, hy + hs + 0.6, hz - 0.2), (0, 1, 0), 3.5 if i % 2 else 2.2, 1.1, plate, tip=glow_mat(COIN), segments=2)
    rags = rag_strips(m, "body", "cape", p["top"] - 0.6, 11, 22, 5, cape, p["back"] + 0.8, lengths=(1, 0.75, 0.9, 0.7, 0.95))
    chains = seal_and_shackles(m, p, 6, COIN, dark, glow_mat(COIN))
    for side, sx in (("left", 1), ("right", -1)):
        a = p["arms"][side]
        m.cube(side + "_arm", (a["shoulder"][0] - 3.2, a["shoulder"][1] - 1, -3.6), (6.4, 3, 7.2), plate,
               rotation=(0, 0, -16 * sx))                                                                # flared pauldrons
    shield, grip = held(m, "left", p, "shield", tilt=-8)
    gx, gy, gz = grip
    m.cube(shield, (gx + 1.4, gy - 14, gz - 7), (1.4, 18, 14), plate,
           deco=lambda pp, f, mm, r: [pp.at(f["east"], x, y, COIN, glow=True) for x in range(f["east"][2]) for y in range(f["east"][3]) if (x * 3 + y * 5) % 7 == 0])
    m.cube(shield, (gx + 2.6, gy - 6, gz - 1.5), (0.8, 3, 3), glow_mat((230, 40, 50)))
    sword, grip = held(m, "right", p, "sword", tilt=-60)
    bx, by, bz = grip
    m.cube(sword, (bx - 0.7, by - 4, bz - 0.7), (1.4, 5, 1.4), cape)
    m.cube(sword, (bx - 3.4, by - 5, bz - 1), (6.8, 1.4, 2), plate)
    m.cube(sword, (bx - 1.6, by - 27, bz - 0.4), (3.2, 22, 0.8), steel((150, 150, 160), (210, 210, 220), (250, 250, 255), noise=3))
    p2 = m.bone("p2_coins", "root", (0, 26, 0))
    shard_ring(m, p2, "p2_coin_ring", (0, 26, 0), 15, 12, glow_mat(COIN), length=1.6, width=1.8, rise=6, seed=66)
    anims = figure_anims(1.3, 22, heavy=True, rags=rags, chains=chains, orbit={"p2_coin_ring": 4.0})
    anims["signature"] = signature_anim(*SIGNATURES["goldarc"])
    m.write({"goldarc": None, "goldarc_broken": broken_form((255, 200, 80))}, anims, (4, 5))


# ------------------------------------------------------------------------------------------------ Nixara, Law VII

def nixara():
    """Nixara, the Faceless Broker (Law VII): a tall hooded merchant with no face but two coins of light, long
    bony hands, scales in one and purses hanging from every strap."""
    m = Model("nixara", 512, density=2, scale=1.25)
    robe = mat((24, 18, 16), (60, 44, 34), (110, 86, 64), "cloth", ragged=3, noise=5)
    bone_hand = mat((100, 90, 76), (170, 158, 136), (220, 210, 190), "plain", noise=4)
    pouch = mat((90, 56, 30), (150, 100, 56), (200, 150, 96), "cloth", noise=5)
    gold = steel((110, 74, 20), (196, 146, 50), (250, 214, 120), noise=4)
    eye = glow_mat(COIN)
    p = figure(m, 1.0, {"skin": robe, "chest": robe, "waist": robe, "upper_arm": robe, "forearm": robe, "hand": bone_hand, "thigh": robe,
                        "shin": robe, "foot": robe, "pads": robe}, chest=(10, 8, 6), arm=3.2, leg=3.8, hunch=14, arm_len=(9, 10),
               head=8, finger_len=4.2, claw=bone_hand, shoulder_pads=False)
    sculpted_face(m, "head", p["head"], robe, eye, kind="hooded", eye_size=(1.8, 1.2))
    hx, hy, hz, hs = p["head"]
    m.cube("head", (hx - 1, hy - 0.6, hz - 1.6), (hs + 2, hs + 2.6, hs + 2.4), robe,
           deco=lambda pp, f, mm, r: [pp.clear(f["north"][0] + x, f["north"][1] + y) for x in range(2, f["north"][2] - 2) for y in range(3, f["north"][3])]
           + [pp.clear(f["down"][0] + x, f["down"][1] + y) for x in range(f["down"][2]) for y in range(f["down"][3])])
    spike(m, "head", (0, hy + hs + 1.6, hz + hs * 0.7), (0, 0.5, 1), 6, 2.6, robe)                         # the hood's point
    chains = seal_and_shackles(m, p, 7, COIN, gold, eye)
    gown(m, "body", p["hips"], 8, 6, 15, robe, flare=0.7, tiers=5)
    cx0, cy0, cz0, cw, ch, cd = p["chest"]
    for i, (x, y) in enumerate(((-4, -2), (2.6, -3), (-0.8, -6), (4, -7), (-5, -8))):                    # purses hung everywhere
        m.cube("body", (x, p["hips"] + y, cz0 - 1.8), (2.6, 3.2, 1.8), pouch, deco=lambda pp, f, mm, r: [pp.at(f["up"], 1, 0, COIN, glow=True)])
    scales, grip = held(m, "left", p, "scales", tilt=0)
    gx, gy, gz = grip
    m.cube(scales, (gx - 0.3, gy - 9, gz - 0.3), (0.6, 7, 0.6), gold)
    m.cube(scales, (gx - 5, gy - 9.4, gz - 0.3), (10, 0.6, 0.6), gold)
    for sx in (1, -1):
        m.cube(scales, (gx + sx * 4.5 - 0.15, gy - 13, gz - 0.15), (0.3, 3.6, 0.3), gold)
        m.cube(scales, (gx + sx * 4.5 - 1.6, gy - 13.6, gz - 1.6), (3.2, 0.6, 3.2), gold, deco=lambda pp, f, mm, r: [pp.at(f["up"], 1, 1, COIN, glow=True)])
    shard_ring(m, "root", "p2_coins", (0, 24, 0), 14, 14, glow_mat(COIN), length=1.4, width=1.6, rise=7, seed=77)
    anims = figure_anims(1.1, 20, chains=chains, orbit={"p2_coins": 3.5},
                         extra_idle={"scales": {"rotation": {"0.0": [0, 0, -6], "1.6": [0, 0, 6], "3.2": [0, 0, -6]}}})
    anims["signature"] = signature_anim(*SIGNATURES["nixara"])
    m.write({"nixara": None, "nixara_broken": broken_form(COIN)}, anims, (4, 5))


# ------------------------------------------------------------------------------------------------ Fenrath, Law VIII

ACID = (150, 255, 90)


def fenrath():
    """Fenrath, the Devourer (Law VIII): a great grey wolf-beast hunched on four legs, a mane of bone spines, a
    long maw full of teeth dripping green acid, eyes of acid light."""
    import math
    m = Model("fenrath", 512, density=2, scale=1.35)
    fur = mat((40, 36, 34), (84, 78, 72), (140, 132, 122), "fur", cracks=(ACID, 0.003), noise=10)
    hide = mat((30, 26, 24), (66, 60, 56), (110, 102, 94), "skin_cracked", noise=7)
    tooth = mat((150, 140, 110), (210, 204, 180), (246, 242, 226), "plain", noise=3)
    maw = mat((60, 10, 14), (120, 30, 36), (180, 70, 70), "plain", noise=5)
    acid = glow_mat(ACID)
    m.bone("root")
    body = m.bone("body", "root", (0, 16, 2), rotation=(-8, 0, 0))
    m.cube(body, (-8, 14, -10), (16, 15, 13), fur)                                                       # the great chest and shoulders
    m.cube(body, (-6.5, 13, 2), (13, 12, 12), fur)                                                       # the haunch
    m.cube(body, (-5, 12, -9), (10, 2, 20), hide)                                                        # the belly, the ribs showing
    for i in range(4):
        m.cube(body, (-5.4, 13.5, -6 + i * 3), (10.8, 0.8, 0.8), tooth)
    for i, z in enumerate((-9, -6, -3, 0, 3, 6, 9)):                                                     # the mane of bone spines
        spike(m, body, (0, 28.5 - i * 0.4, z), (0, 1, 0.5), 9 - abs(i - 2) * 1.0, 2.2, tooth, segments=3)
        for sx in (1, -1):
            spike(m, body, (sx * 4.5, 27.5 - i * 0.4, z), (sx * 0.7, 1, 0.4), 6 - abs(i - 2) * 0.6, 1.6, tooth, segments=2)
    tail = m.bone("tail", "body", (0, 22, 14))
    spike(m, tail, (0, 22, 14), (0, 0.2, 1), 14, 3, fur, segments=4)
    neck = m.bone("neck", "body", (0, 22, -10), rotation=(18, 0, 0))
    m.cube(neck, (-5, 17, -16), (10, 10, 8), fur)
    head = m.bone("head", "neck", (0, 21, -16), rotation=(-12, 0, 0))
    m.cube(head, (-5.5, 17, -24), (11, 9, 9), fur)
    m.cube(head, (-3.6, 17.5, -33), (7.2, 5, 10), hide)                                                  # the long snout
    m.cube(head, (-4, 23.5, -25), (8, 1.5, 3), hide, rotation=(0, 0, 0))
    for sx in (1, -1):
        m.cube(head, (sx * 2.6 - 1, 21.5, -24.4), (2, 1, 0.4), acid, rotation=(0, 0, -16 * sx))            # slanted eyes
        spike(m, head, (sx * 3.8, 25.5, -19), (sx * 0.4, 1, 0.6), 6, 2.4, fur, segments=2)               # ears laid back
        curved_horn(m, head, (sx * 4.5, 24, -21), (sx * 0.6, 0.4, 1), (0, 1, 0), 10, 1.8, tooth, segments=4, bend=-18 * sx)
    for i in range(6):                                                                                   # upper teeth
        x = -3.2 + i * 1.28
        m.cube(head, (x, 16, -33 + (0 if i in (0, 5) else 0.5)), (0.7, 2.2 if i in (0, 5) else 1.4, 0.7), tooth)
    jaw = m.bone("jaw", "head", (0, 18, -22), rotation=(14, 0, 0))
    m.cube(jaw, (-3.4, 14, -32.5), (6.8, 3, 10), hide)
    m.cube(jaw, (-2.6, 16.8, -31.5), (5.2, 0.4, 8), maw, deco=lambda pp, f, mm, r: [pp.at(f["up"], x, y, ACID, glow=True) for x in range(1, 5, 2) for y in range(1, 8, 3)])
    for i in range(5):
        m.cube(jaw, (-3 + i * 1.4, 16.6, -32.2), (0.7, 1.6, 0.7), tooth)
    for i in range(4):                                                                                   # acid dripping from the jaw
        m.cube(jaw, (-2.4 + i * 1.6, 9 - (i % 2) * 2, -31), (0.7, 5 + (i % 2) * 2, 0.7), acid)
    legs = (("front_left_leg", 5, -6), ("front_right_leg", -5, -6), ("back_left_leg", 5, 8), ("back_right_leg", -5, 8))
    for name, x, z in legs:
        leg = m.bone(name, "root", (x, 18, z))
        front = "front" in name
        m.cube(leg, (x - 2.6, 8, z - 2.8), (5.2, 11, 5.6), fur, rotation=((-14 if front else 20), 0, 0), pivot=(x, 18, z))
        m.cube(leg, (x - 2, 0, z - 2 - (1 if front else -2)), (4, 9, 4), hide)
        m.cube(leg, (x - 2.6, 0, z - 5 - (1 if front else -2)), (5.2, 2, 5), hide)
        for k in range(3):
            spike(m, leg, (x - 1.6 + k * 1.6, 0.6, z - 5.4 - (1 if front else -2)), (0, -0.4, -1), 2.6, 0.8, tooth, segments=2)
    sea = m.bone("p2_spines", "body", (0, 28, 0))                      # the acid bursts out between the spines
    for i, z in enumerate((-8, -4, 0, 4, 8)):
        spike(m, sea, (0, 28, z), (0.3 * ((i % 3) - 1), 1, 0.3), 7, 1.4, acid, segments=2)
    shard_ring(m, "root", "p2_acid", (0, 18, 0), 18, 9, acid, length=2.2, width=1.2, rise=5, seed=88)
    t = 2.0
    anims = {
        "idle": {"loop": True, "animation_length": t, "bones": {
            "jaw": {"rotation": {"0.0": [0, 0, 0], "1.0": [14, 0, 0], "2.0": [0, 0, 0]}},
            "head": {"rotation": {"0.0": [0, -6, 0], "0.7": [4, 8, 0], "0.8": [-4, -4, 4], "2.0": [0, -6, 0]}},
            "body": {"position": {"0.0": [0, 0, 0], "1.0": [0, -0.8, 0], "2.0": [0, 0, 0]}},
            "tail": {"rotation": {"0.0": [0, -14, 0], "1.0": [0, 14, 0], "2.0": [0, -14, 0]}},
            "p2_acid": {"rotation": {"0.0": [0, 0, 0], "2.0": [0, 360, 0]}}}},
        "walk": {"loop": True, "animation_length": 0.8, "bones": {
            "front_left_leg": {"rotation": {"0.0": [30, 0, 0], "0.4": [-30, 0, 0], "0.8": [30, 0, 0]}},
            "back_right_leg": {"rotation": {"0.0": [30, 0, 0], "0.4": [-30, 0, 0], "0.8": [30, 0, 0]}},
            "front_right_leg": {"rotation": {"0.0": [-30, 0, 0], "0.4": [30, 0, 0], "0.8": [-30, 0, 0]}},
            "back_left_leg": {"rotation": {"0.0": [-30, 0, 0], "0.4": [30, 0, 0], "0.8": [-30, 0, 0]}},
            "body": {"rotation": {"0.0": [0, 0, 3], "0.4": [0, 0, -3], "0.8": [0, 0, 3]}, "position": {"0.0": [0, 0, 0], "0.2": [0, 1, 0], "0.4": [0, 0, 0], "0.6": [0, 1, 0], "0.8": [0, 0, 0]}},
            "tail": {"rotation": {"0.0": [10, -10, 0], "0.4": [10, 10, 0], "0.8": [10, -10, 0]}},
            "jaw": {"rotation": {"0.0": [0, 0, 0], "0.4": [12, 0, 0], "0.8": [0, 0, 0]}},
            "p2_acid": {"rotation": {"0.0": [0, 0, 0], "0.8": [0, 180, 0]}}}},
        "attack": {"loop": False, "animation_length": 0.7, "bones": {
            "jaw": {"rotation": {"0.0": [0, 0, 0], "0.25": [50, 0, 0], "0.4": [0, 0, 0], "0.7": [0, 0, 0]}},
            "neck": {"rotation": {"0.0": [0, 0, 0], "0.25": [-24, 0, 0], "0.4": [20, 0, 0], "0.7": [0, 0, 0]}},
            "body": {"rotation": {"0.0": [0, 0, 0], "0.25": [-8, 0, 0], "0.4": [8, 0, 0], "0.7": [0, 0, 0]}}}},
    }
    anims["signature"] = signature_anim(*SIGNATURES["fenrath"])
    m.write({"fenrath": None, "fenrath_broken": broken_form(ACID)}, anims, (5, 5))


# ------------------------------------------------------------------------------------------------ Shadeyn, Law IX

def shadeyn():
    """Shadeyn, the Mirror Knight (Law IX): a knight of mirror and crystal, shards rising from his shoulders and
    back, a helm whose visor is a slit of white light, a crystal shield and a long blade of glass."""
    m = Model("shadeyn", 512, density=2, scale=1.25)
    mirror = steel((120, 130, 156), (196, 206, 226), (250, 252, 255), seam=3, noise=2)
    dark = steel((40, 46, 66), (80, 90, 120), (150, 160, 190), seam=4, noise=3)
    crystal = glow_mat((200, 230, 255))
    shard_m = mat((140, 170, 220), (200, 225, 255), (250, 252, 255), "plain", noise=2)
    p = figure(m, 1.0, {"skin": dark, "chest": mirror, "waist": dark, "upper_arm": dark, "forearm": mirror, "hand": dark, "thigh": dark,
                        "shin": mirror, "foot": mirror, "pads": mirror}, chest=(11, 8.5, 6.5), arm=3.5, leg=4, hunch=6, head=7.6)
    hx, hy, hz, hs = p["head"]
    m.cube("head", (hx, hy, hz), (hs, hs, hs), mirror,
           deco=lambda pp, f, mm, r: [pp.at(f["north"], x, int(f["north"][3] * 0.45), (255, 255, 255), glow=True) for x in range(1, f["north"][2] - 1)])
    m.cube("head", (hx + hs / 2 - 0.4, hy - 0.2, hz - 0.8), (0.8, hs, 0.8), mirror)                       # the ridge of the visor
    for i in range(5):                                                                                  # a crest of shards
        spike(m, "head", (hx + hs / 2, hy + hs, hz + 1 + i * 1.5), (0, 1, 0.3 + i * 0.15), 7 - i, 1.6, shard_m, tip=crystal, segments=2)
    chains = seal_and_shackles(m, p, 9, (220, 240, 255), dark, crystal)
    for sx in (1, -1):                                                                                  # shards from the shoulders and back
        a = p["arms"]["left" if sx > 0 else "right"]
        for k in range(3):
            spike(m, ("left" if sx > 0 else "right") + "_arm", (a["shoulder"][0] + sx * (1 + k), a["shoulder"][1] + 1.2, -1 + k * 1.4),
                  (sx * 0.5, 1, 0.3), 8 - k * 2, 2.0 - k * 0.4, shard_m, tip=crystal, segments=2)
        for k in range(3):
            spike(m, "body", (sx * (2 + k * 1.5), p["top"] - 2 - k * 2, p["back"]), (sx * 0.5, 0.7, 1), 9 - k * 2, 2.2, shard_m, tip=crystal, segments=2)
    shield, grip = held(m, "left", p, "shield", tilt=-8)
    gx, gy, gz = grip
    m.cube(shield, (gx + 1.4, gy - 13, gz - 5.5), (1.2, 16, 11), crystal)
    m.cube(shield, (gx + 1.2, gy - 13.4, gz - 5.9), (1.6, 0.8, 11.8), mirror)
    m.cube(shield, (gx + 1.2, gy + 2.6, gz - 5.9), (1.6, 0.8, 11.8), mirror)
    blade, grip = held(m, "right", p, "blade", tilt=-60)
    bx, by, bz = grip
    m.cube(blade, (bx - 0.6, by - 4, bz - 0.6), (1.2, 5, 1.2), dark)
    m.cube(blade, (bx - 2.6, by - 4.6, bz - 0.8), (5.2, 1.2, 1.6), mirror)
    spike(m, blade, (bx, by - 4.4, bz), (0, -1, 0), 26, 2.6, shard_m, tip=crystal, segments=3)
    shard_ring(m, "root", "p2_mirrors", (0, 26, 0), 15, 10, shard_m, length=5, width=2, rise=6, seed=99)
    anims = figure_anims(1.0, 28, chains=chains, orbit={"p2_mirrors": 5.0})
    anims["signature"] = signature_anim(*SIGNATURES["shadeyn"])
    m.write({"shadeyn": None, "shadeyn_broken": broken_form((200, 230, 255), darken=0.35)}, anims, (4, 5))


# ------------------------------------------------------------------------------------------------ Solrath, Law X

SUN = (255, 210, 90)


def solrath():
    """Solrath, the False Prophet (Law X): the vizier Ozhan unmasked, tall in purple robes edged with gold, a mask of
    gold over a face of shadow with golden eyes, a halo of rays behind his head cracked through, a staff crowned
    with a sun disk."""
    m = Model("solrath", 512, density=2, scale=1.35)
    robe = mat((40, 10, 60), (86, 30, 120), (150, 80, 200), "cloth", ragged=2, noise=4)
    gold = steel((140, 90, 20), (230, 170, 50), (255, 236, 150), noise=3)
    shadow = mat((10, 6, 16), (24, 16, 34), (44, 32, 60), "plain", noise=2)
    sun = glow_mat(SUN)
    p = figure(m, 1.0, {"skin": shadow, "chest": robe, "waist": robe, "upper_arm": robe, "forearm": robe, "hand": gold, "thigh": robe,
                        "shin": robe, "foot": gold, "pads": gold}, chest=(10, 8.5, 6), arm=3.2, leg=3.8, hunch=5, head=7.6, finger_len=3.6, claw=gold)
    sculpted_face(m, "head", p["head"], gold, sun, kind="noble", brow=gold, eye_size=(1.8, 0.6))
    hx, hy, hz, hs = p["head"]
    m.cube("head", (hx - 0.9, hy - 0.4, hz - 0.6), (hs + 1.8, hs + 1.6, hs + 1.6), robe, inflate=0.1,
           deco=lambda pp, f, mm, r: [pp.clear(f["north"][0] + x, f["north"][1] + y) for x in range(2, f["north"][2] - 2) for y in range(2, f["north"][3])]
           + [pp.clear(f["down"][0] + x, f["down"][1] + y) for x in range(f["down"][2]) for y in range(f["down"][3])])
    for i in range(5):                                                                                  # a crown of golden spikes
        spike(m, "head", (hx + 0.8 + i * (hs - 1.6) / 4, hy + hs + 0.8, hz + 0.6), (0.25 * (i - 2), 1, 0), 3 + (2 - abs(i - 2)) * 1.6, 1.2, gold, segments=2)
    chains = seal_and_shackles(m, p, 10, SUN, gold, sun)
    gown(m, "body", p["hips"], 8, 6, 15, robe, flare=0.7, tiers=5)
    m.cube("body", (-1.2, p["hips"] - 14, p["chest"][2] - 1.8), (2.4, 24, 0.6), gold)                    # a stole of gold
    halo = m.bone("halo", "head", (0, hy + hs * 0.6, hz + hs + 2))
    broken_halo(m, halo, (0, hy + hs * 0.6, hz + hs + 2), 8, 1.2, sun, gaps=((300, 330),), loose=gold, rays=14, ray_len=4)
    staff, grip = held(m, "right", p, "staff", tilt=-6)
    gx, gy, gz = grip
    m.cube(staff, (gx - 0.8, gy - 24, gz - 0.8), (1.6, 46, 1.6), gold)
    broken_halo(m, staff, (gx, gy + 24, gz), 3.4, 1, sun, gaps=(), rays=8, ray_len=2)
    m.cube(staff, (gx - 1.2, gy + 22.8, gz - 1.2), (2.4, 2.4, 2.4), sun)
    shard_ring(m, "root", "p2_sunfire", (0, 30, 0), 15, 10, sun, length=3.5, width=1.4, rise=6, seed=111)
    anims = figure_anims(1.4, 16, chains=chains, orbit={"p2_sunfire": 4.5},
                         extra_idle={"halo": {"rotation": {"0.0": [0, 0, 0], "3.2": [0, 0, 360]}}})
    anims["signature"] = signature_anim(*SIGNATURES["solrath"])
    m.write({"solrath": None, "solrath_broken": broken_form(SUN)}, anims, (5, 6))


# ================================================================================================ the Archsins

def sin_core(m, places, color, size=None):
    """The heart of a sin in the chest, a gem of its colour cracked through (what the Bearers will break)."""
    cx0, cy0, cz0, cw, ch, cd = places["chest"]
    size = size or min(cw, ch) * 0.42
    core = mat(mix(color, (0, 0, 0), 0.5), color, mix(color, (255, 255, 255), 0.5), "glow")

    def crack(p, f, mm, rnd):
        x, y, w, h = f["north"]
        px = w // 2
        for py in range(h):
            p.at(f["north"], px, py, mix(color, (0, 0, 0), 0.85))
            px = max(0, min(w - 1, px + rnd.choice((-1, 0, 1))))
        for py in range(h):
            for qx in range(w):
                if abs(qx - (w - 1) / 2) + abs(py - (h - 1) / 2) > max(w, h) / 2:
                    p.at(f["north"], qx, py, mix(mm["pal"][0], (0, 0, 0), 0.3))
    m.cube("body", (cx0 + cw / 2 - size / 2, cy0 + ch * 0.55 - size / 2, cz0 - 0.7), (size, size, 1), core, deco=crack,
           rotation=(0, 0, 45), pivot=(cx0 + cw / 2, cy0 + ch * 0.55, cz0 - 0.2))


def broken_crown(m, bone, places, material, gem=None, points=6, height=4.0, snapped=(1, 4)):
    """A crown with some of its points snapped off (their stumps glowing in gem)."""
    hx, hy, hz, hs = places["head"]
    m.cube(bone, (hx - 0.5, hy + hs * 0.86, hz - 0.5), (hs + 1, 1.8, hs + 1), material)
    for i in range(points):
        x = hx + 0.1 + i * (hs - 0.2) / (points - 1)
        if i in snapped:
            m.cube(bone, (x - 0.6, hy + hs + 1.2, hz - 0.6), (1.2, 1.0, 1.2), gem or material, rotation=(0, 0, 20 if i % 2 else -20))
        else:
            spike(m, bone, (x, hy + hs + 1.2, hz - 0.2), (0.15 * (i - (points - 1) / 2), 1, 0), height * (1.2 if i in (0, points - 1) else 1.0),
                  1.3, material, tip=gem, segments=2)


ARCH_ANIM = dict(heavy=True)


# ------------------------------------------------------------------------------------------------ the Brass Sentinel

TURQ = (90, 236, 214)


def brass_sentinel():
    """The Brass Sentinel (Act I): a clockwork colossus of brass and iron with a cog turning in its chest round an
    aetherium core, a visor slit of turquoise light, great three-fingered hands. Half broken by the Void, it turns
    violet and splits (brass_sentinel_void), and pieces of its plating break away to orbit it."""
    from make_mob_models import sentinel_void, gear_face, VOID_GLOW
    m = Model("brass_sentinel", 512, density=2, scale=1.5)
    brass = steel((108, 68, 22), (176, 124, 48), (232, 188, 98), rivets=True, seam=5, noise=5)
    iron = steel((40, 36, 34), (74, 68, 62), (118, 110, 100), seam=4, noise=5)
    copper = steel((86, 40, 22), (156, 82, 46), (214, 132, 84), rivets=True, noise=6)
    core = mat((20, 120, 110), (60, 200, 190), (180, 255, 240), "glow")
    p = figure(m, 1.0, {"skin": brass, "chest": brass, "waist": iron, "upper_arm": iron, "forearm": brass, "hand": iron, "thigh": iron,
                        "shin": brass, "foot": iron, "pads": brass, "neck": iron},
               chest=(15, 10, 9), waist=(8, 4, 6), arm=4.6, leg=5, hunch=8, head=7.5, fingers=3, finger_len=4.5, arm_len=(9, 9))
    cx0, cy0, cz0, cw, ch, cd = p["chest"]
    m.cube("body", (-3, cy0 + 2.5, cz0 - 0.8), (6, 6, 1), core)
    gear = m.bone("chest_gear", "body", (0, cy0 + 5.5, cz0 - 1.2))
    m.cube(gear, (-5, cy0 + 0.5, cz0 - 1.6), (10, 10, 1), brass, deco=gear_face((214, 170, 80), TURQ, variant_hub=VOID_GLOW))
    for sx in (1, -1):                                                   # exhaust pipes on the back
        m.cube("body", (sx * 4 - 1.5, p["top"] - 4, p["back"]), (3, 9, 3), copper)
    hx, hy, hz, hs = p["head"]

    def visor(pp, f, mm, r):
        x, y, w, h = f["north"]
        glow = VOID_GLOW if pp.variant else TURQ
        for px in range(1, w - 1):
            pp.at(f["north"], px, h // 2, glow, glow=True)
            pp.at(f["north"], px, h // 2 - 1, mix(mm["pal"][0], (0, 0, 0), 0.5))
            pp.at(f["north"], px, h // 2 + 1, mix(mm["pal"][0], (0, 0, 0), 0.5))
    m.cube("head", (hx, hy, hz), (hs, hs * 0.9, hs), brass, deco=visor)
    m.cube("head", (hx - 0.6, hy + hs * 0.25, hz + 1), (hs + 1.2, hs * 0.4, hs - 2), iron)                  # ear bolts
    m.cube("head", (-0.5, hy + hs * 0.9, hz + hs / 2 - 0.5), (1, 3, 1), iron)
    m.cube("head", (-1, hy + hs * 0.9 + 3, hz + hs / 2 - 1), (2, 1.5, 2), core)
    for side, sx in (("left", 1), ("right", -1)):
        a = p["arms"][side]
        m.cube(side + "_arm", (a["shoulder"][0] - 3.6, a["shoulder"][1] - 2.4, -4.4), (7.2, 5, 8.8), brass, rotation=(0, 0, -12 * sx))
    shard_ring(m, "root", "p2_plates", (0, 34, 0), 18, 8, brass, length=4, width=2.6, rise=6, seed=5)
    anims = figure_anims(1.6, 18, heavy=True, orbit={"p2_plates": 5.0}, two_handed=True,
                         extra_idle={"chest_gear": {"rotation": {"0.0": [0, 0, 0], "1.6": [0, 0, 180], "3.2": [0, 0, 360]}}},
                         extra_walk={"chest_gear": {"rotation": {"0.0": [0, 0, 0], "1.6": [0, 0, 360]}}})
    anims["signature"] = signature_anim(*SIGNATURES["brass_sentinel"])
    m.write({"brass_sentinel": None, "brass_sentinel_void": sentinel_void}, anims, (5, 6))


# ------------------------------------------------------------------------------------------------ Vorath, Wrath

LAVA = (255, 120, 30)


def vorath():
    """Vorath, Archsin of Wrath (Act II): a giant of charred rock split by lava, great horns, a beard and mouth of
    fire, a scowl of burning eyes, a huge bearded axe. His heart of wrath glows cracked in his chest."""
    m = Model("vorath", 512, density=2, scale=1.7)
    rock = mat((24, 12, 10), (58, 30, 22), (100, 54, 38), "rock", cracks=(LAVA, 0.012), noise=8)
    iron = steel((18, 16, 18), (46, 42, 44), (92, 86, 84), rivets=True, seam=5, noise=5)
    horn_m = mat((40, 30, 26), (120, 100, 82), (210, 194, 166), "plain", noise=6)
    flame = mat((200, 50, 10), (255, 120, 30), (255, 220, 120), "flame")
    wood = mat((34, 20, 12), (70, 44, 26), (108, 72, 44), "cloth", noise=6)
    p = figure(m, 1.0, {"skin": rock, "chest": rock, "waist": iron, "upper_arm": rock, "forearm": rock, "hand": rock, "thigh": rock,
                        "shin": iron, "foot": iron, "pads": iron, "neck": rock},
               chest=(15, 10, 9), waist=(10, 4, 7), arm=5, leg=5.4, hunch=14, head=8.5, claw=horn_m, arm_len=(9, 9.5))
    sculpted_face(m, "head", p["head"], rock, glow_mat((255, 220, 120)), kind="demon", brow=rock, mouth=glow_mat(LAVA), anger=22)
    hx, hy, hz, hs = p["head"]
    m.cube("jaw", (hx + 1, hy - 4, hz - 0.6), (hs - 2, 4.5, 2), flame)                                   # the beard of fire
    for sx in (1, -1):                                                   # the great horns, out and up
        curved_horn(m, "head", (hx + (hs if sx > 0 else 0), hy + hs * 0.78, hz + hs * 0.4), (sx, 0.25, 0.1), (0, 0, sx), 22, 3.6, horn_m,
                    segments=7, bend=16)
    broken_crown(m, "head", p, steel((70, 40, 10), (150, 96, 24), (230, 170, 60)), gem=glow_mat(LAVA), points=5, height=3)
    sin_core(m, p, LAVA)
    for sx in (1, -1):
        a = p["arms"]["left" if sx > 0 else "right"]
        for k in range(3):
            spike(m, ("left" if sx > 0 else "right") + "_arm", (a["shoulder"][0] + sx * (1.5 + k), a["shoulder"][1] + 1.5, -2 + k * 2),
                  (sx * 0.4, 1, 0.1), 5 - k, 1.6, horn_m)
    m.cube("body", (-6, p["hips"] - 9, p["chest"][2] + 0.6), (12, 10, 1), mat((30, 20, 16), (64, 46, 34), (110, 84, 60), "fur", ragged=3))
    axe, grip = held(m, "right", p, "axe", tilt=-55)
    gx, gy, gz = grip
    m.cube(axe, (gx - 1, gy - 30, gz - 1), (2, 34, 2), wood)
    m.cube(axe, (gx - 0.6, gy - 34, gz - 9), (1.2, 12, 9), steel((30, 26, 26), (70, 62, 60), (130, 120, 116), cracks=(LAVA, 0.02)),
           deco=lambda pp, f, mm, r: [pp.at(f["east"], 0, y, (255, 200, 90), glow=True) for y in range(f["east"][3])])
    m.cube(axe, (gx - 0.5, gy - 30, gz + 1), (1, 3, 4), iron)
    shard_ring(m, "root", "p2_magma", (0, 30, 0), 20, 10, rock, length=4, width=2.6, rise=8, seed=7)
    m.bone("p2_flames", "head", (0, hy + hs, hz))
    for i in range(6):
        spike(m, "p2_flames", (hx + 0.8 + i * (hs - 1.6) / 5, hy + hs * 0.95, hz + 1 + (i % 2) * 2), (0, 1, 0.3), 4 + (i % 3) * 2, 1.6,
              glow_mat(LAVA), segments=2)
    anims = figure_anims(1.4, 22, heavy=True, orbit={"p2_magma": 5.0}, two_handed=True)
    anims["signature"] = signature_anim(*SIGNATURES["vorath"])
    m.write({"vorath": None, "vorath_broken": broken_form(LAVA)}, anims, (7, 7))


# ------------------------------------------------------------------------------------------------ Luxara, Lust

PINK = (255, 110, 200)


def luxara():
    """Luxara, Archsin of Lust (Act III): a tall temptress in a magenta gown, great bat wings of crimson membrane,
    ram horns curling back, long red hair, gold at her throat, a face of cruel beauty with eyes of pink light."""
    m = Model("luxara", 512, density=2, scale=1.35)
    skin = mat((170, 120, 140), (226, 186, 200), (250, 230, 236), "plain", noise=3)
    gown_m = mat((90, 10, 60), (170, 40, 120), (240, 120, 190), "cloth", ragged=2, noise=4)
    hair = mat((70, 6, 14), (140, 20, 30), (200, 60, 60), "fur", noise=6)
    horn_m = mat((20, 6, 14), (60, 20, 40), (120, 70, 100), "plain", noise=4)
    gold = steel((110, 74, 20), (196, 146, 50), (250, 214, 120), noise=4)
    p = lady(m, {"skin": skin, "chest": gown_m, "waist": gown_m, "upper_arm": skin, "forearm": skin, "hand": skin, "thigh": gown_m,
                 "shin": gown_m, "foot": gown_m}, claw=glow_mat(PINK), finger_len=3.6)
    sculpted_face(m, "head", p["head"], skin, glow_mat(PINK), kind="noble", brow=horn_m, lips=glow_mat((220, 30, 90)), eye_size=(1.8, 0.7))
    hx, hy, hz, hs = p["head"]
    locks = long_hair(m, p, hair, length=20, strips=5)
    for sx in (1, -1):                                                   # ram horns curling back and round
        curved_horn(m, "head", (hx + (hs - 0.6 if sx > 0 else 0.6), hy + hs * 0.95, hz + hs * 0.3), (sx * 0.5, 1, 0.5), (sx, 0, 0), 20, 3,
                    horn_m, segments=8, bend=-40 * 1, taper=0.86)
    broken_crown(m, "head", p, gold, gem=glow_mat(PINK), points=5, height=2.2, snapped=(2,))
    m.cube("body", (-2.6, p["top"] - 2, p["chest"][2] - 0.6), (5.2, 1.6, 0.6), gold)                    # gold at her throat
    sin_core(m, p, PINK, size=3.2)
    gown(m, "body", p["hips"], 7, 5.4, 16, gown_m, flare=0.8, tiers=6)
    membrane = mat((70, 6, 40), (150, 20, 80), (220, 70, 140), "cloth", cracks=((255, 120, 200), 0.006), noise=4)
    rib = mat((30, 6, 20), (60, 14, 40), (110, 40, 80), "plain", noise=3)
    bat_wing(m, "body", 1, (2.5, p["top"] - 6, p["back"]), 30, 18, membrane, rib, claws=horn_m)
    bat_wing(m, "body", -1, (-2.5, p["top"] - 6, p["back"]), 30, 18, membrane, rib, claws=horn_m)
    shard_ring(m, "root", "p2_petals", (0, 30, 0), 14, 12, glow_mat(PINK), length=2.2, width=1.4, rise=8, seed=9)
    anims = figure_anims(1.2, 16, rags=locks, orbit={"p2_petals": 5.0},
                         extra_idle={"left_wing": {"rotation": {"0.0": [0, 0, 0], "1.6": [0, -10, -8], "3.2": [0, 0, 0]}},
                                     "right_wing": {"rotation": {"0.0": [0, 0, 0], "1.6": [0, 10, 8], "3.2": [0, 0, 0]}}},
                         extra_walk={"left_wing": {"rotation": {"0.0": [0, 0, 0], "0.6": [0, -12, -12], "1.2": [0, 0, 0]}},
                                     "right_wing": {"rotation": {"0.0": [0, 0, 0], "0.6": [0, 12, 12], "1.2": [0, 0, 0]}}})
    anims["signature"] = signature_anim(*SIGNATURES["luxara"])
    m.write({"luxara": None, "luxara_broken": broken_form(PINK)}, anims, (8, 7))


# ------------------------------------------------------------------------------------------------ Morthis, Sloth

MARSH = (120, 255, 120)


def morthis():
    """Morthis, Archsin of Sloth (Act III): a swollen pharaoh rotting on his throne, moss over his linen, a striped
    nemes and gold crown, a false beard, marsh light in his heavy-lidded eyes, great slack hands on the arms of
    the throne."""
    m = Model("morthis", 512, density=2, scale=1.3)
    rot = mat((70, 80, 50), (120, 130, 90), (170, 176, 130), "skin_cracked", cracks=(MARSH, 0.004), noise=7)
    linen = mat((60, 52, 36), (110, 100, 70), (160, 150, 112), "cloth", ragged=3, noise=7)
    moss = mat((20, 50, 16), (40, 90, 30), (80, 140, 60), "fur", ragged=2, noise=10)
    stone = mat((70, 56, 36), (120, 100, 66), (170, 150, 110), "rock", noise=8)
    nemes = mat((20, 40, 90), (40, 70, 160), (90, 130, 220), "cloth", noise=4)
    gold = steel((110, 74, 20), (196, 146, 50), (250, 214, 120), noise=4)
    stripes = lambda pp, f, mm, r: [pp.at(f[n], x, y, (220, 180, 70)) for n in ("north", "east", "west", "south")
                                    for x in range(f[n][2]) for y in range(f[n][3]) if y % 2 == 0]
    m.bone("root")
    throne = m.bone("throne", "root", (0, 0, 0))
    m.cube(throne, (-14, 0, -4), (28, 13, 18), stone)
    m.cube(throne, (-14, 13, 10), (28, 30, 4), stone, deco=lambda pp, f, mm, r: [pp.at(f["north"], x, 3, (220, 180, 70)) for x in range(f["north"][2])])
    m.cube(throne, (-15, 13, -4), (3, 11, 14), stone, mirror_x=True)
    for sx in (1, -1):
        spike(m, throne, (sx * 12, 43, 12), (sx * 0.3, 1, 0), 7, 3, gold, tip=glow_mat(MARSH), segments=2)
    m.cube(throne, (-14.5, 12.5, -4.5), (29, 1, 19), moss)
    body = m.bone("body", "root", (0, 16, 4), rotation=(-8, 0, 0))
    m.cube(body, (-11, 13, -7), (22, 18, 15), rot, deco=veins(MARSH, 0.003))                             # the swollen belly
    m.cube(body, (-11.5, 13, -7.5), (23, 6, 16), linen)
    m.cube(body, (-9.5, 30, -5), (19, 7, 12), rot)                                                      # the heavy chest
    m.cube(body, (-10, 34, -5.5), (20, 3, 13), moss, inflate=0.2)
    p = {"chest": (-9.5, 30, -5, 19, 7, 12), "head": (-5.5, 37, -6.5, 11)}
    sin_core(m, p, MARSH, size=3.6)
    m.bone("neck", "body", (0, 37, 0))
    head = m.bone("head", "neck", (0, 37, -1))
    lids = centered(["", "", "", "", "", "", "", "MMMMMMMMMM"], {"M": mix(MARSH, (0, 0, 0), 0.6)}, top=0)
    sculpted_face(m, head, p["head"], rot, glow_mat(MARSH), kind="demon", brow=rot, anger=14, eye_size=(2, 0.6), deco=lids)
    hx, hy, hz, hs = p["head"]
    m.cube(head, (hx - 1, hy + hs * 0.5, hz - 0.6), (hs + 2, hs * 0.6, hs + 1.6), nemes,
           deco=combine_decos(stripes, lambda pp, f, mm, r: [pp.clear(f["north"][0] + x, f["north"][1] + y) for x in range(2, f["north"][2] - 2) for y in range(3, f["north"][3])]))
    for sx in (1, -1):
        m.cube(head, (hx + (hs + 0.2 if sx > 0 else -2.2), hy - 6, hz + 1), (2, hs, hs * 0.6), nemes, deco=stripes)
    m.cube(head, (-1.2, hy - 6, hz - 0.8), (2.4, 6, 1.6), nemes, deco=stripes)                             # the false beard
    broken_crown(m, head, p, gold, gem=glow_mat(MARSH), points=5, height=3.5, snapped=(3,))
    for side, sx in (("left", 1), ("right", -1)):
        arm = m.bone(side + "_arm", "body", (11 * sx, 34, 0), rotation=(0, 0, -8 * sx))
        m.cube(arm, (11 * sx - 3.5, 24, -3.5), (7, 11, 7), rot)
        fore = m.bone(side + "_forearm", side + "_arm", (11 * sx, 25, 0), rotation=(-70, 0, 0))
        m.cube(fore, (11 * sx - 3.2, 14, -3.2), (6.4, 11.5, 6.4), linen)
        hand(m, fore, (11 * sx, 14, 0), 6, rot, sx, finger_len=5, claw=mat((40, 40, 20), (90, 90, 50), (140, 140, 90)))
        leg = m.bone(side + "_leg", "root", (5.5 * sx, 16, 0), rotation=(-80, 0, 0))
        m.cube(leg, (5.5 * sx - 4.5, 3, -4.5), (9, 13, 9), rot)
        shin = m.bone(side + "_shin", side + "_leg", (5.5 * sx, 3, 0), rotation=(80, 0, 0))
        m.cube(shin, (5.5 * sx - 4, -10, -4), (8, 13.5, 8), linen)
    shard_ring(m, "root", "p2_flies", (0, 34, -4), 16, 16, mat((10, 14, 8), (20, 30, 16), (40, 60, 30)), length=0.9, width=0.9, rise=8, seed=13)
    anims = figure_anims(4.0, 0, rags=(), orbit={"p2_flies": 2.0}, idle_len=4.0)
    anims["walk"]["bones"] = {"body": {"rotation": {"0.0": [0, 0, 0], "2.0": [3, 0, 0], "4.0": [0, 0, 0]}}, "p2_flies": anims["walk"]["bones"].get("p2_flies")}
    anims["signature"] = signature_anim(*SIGNATURES["morthis"])
    m.write({"morthis": None, "morthis_broken": broken_form(MARSH)}, anims, (5, 6))


# ------------------------------------------------------------------------------------------------ Avarok, Greed

def avarok():
    """Avarok, Archsin of Greed (Act IV): a colossal king swollen with gold, a crown of gold and rubies, a white
    beard, a red cloak, rings on every finger, standing on a heap of coins with his sack over his shoulder."""
    m = Model("avarok", 512, density=2, scale=1.5)
    face = mat((150, 100, 80), (210, 160, 130), (240, 200, 176), "plain", noise=4)
    gold = steel((120, 80, 20), (210, 160, 50), (255, 226, 130), rivets=True, seam=5, noise=4)
    cape = mat((70, 10, 14), (140, 24, 30), (196, 60, 60), "cloth", ragged=3, noise=5)
    beard = mat((180, 176, 170), (226, 224, 220), (250, 250, 248), "fur", noise=5)
    sack = mat((70, 46, 24), (120, 84, 46), (170, 126, 80), "cloth", noise=6)
    coin = glow_mat(COIN)
    p = figure(m, 1.0, {"skin": face, "chest": gold, "waist": gold, "upper_arm": cape, "forearm": gold, "hand": face, "thigh": gold,
                        "shin": gold, "foot": gold, "pads": gold}, chest=(15, 10, 10), waist=(14, 6, 11), arm=4.6, leg=5.4, hunch=4,
               head=8.5, claw=glow_mat((230, 40, 50)), finger_len=3.4)
    m.cube("body", (-8.5, p["hips"] - 1, -7.5), (17, 9, 13), gold, deco=lambda pp, f, mm, r: [pp.at(f["north"], x, 4, (230, 40, 50), glow=True) for x in range(2, f["north"][2] - 2, 3)])
    sculpted_face(m, "head", p["head"], face, glow_mat(COIN), kind="noble", brow=beard, eye_size=(1.8, 0.7))
    hx, hy, hz, hs = p["head"]
    m.bone("beard", "head", (0, hy + hs * 0.3, hz))
    m.cube("beard", (hx + 0.4, hy - 6, hz - 0.8), (hs - 0.8, 9, 3), beard)
    m.cube("beard", (hx + 1.6, hy - 9, hz - 0.6), (hs - 3.2, 3, 2), beard)
    broken_crown(m, "head", p, gold, gem=glow_mat((230, 40, 50)), points=7, height=4.5, snapped=(2, 5))
    sin_core(m, p, COIN, size=4)
    rags = rag_strips(m, "body", "cape", p["top"] - 1, 15, 28, 5, cape, p["back"] + 1, lengths=(1, 0.85, 0.95, 0.8, 1))
    m.bone("heap", "root", (0, 0, 0))
    m.cube("heap", (-14, 0, -12), (28, 3, 22), gold, deco=lambda pp, f, mm, r: [pp.at(f[n], x, y, COIN, glow=True) for n in ("up", "north")
                                                                                 for x in range(f[n][2]) for y in range(f[n][3]) if r.random() < 0.15])
    m.cube("heap", (-9, 3, -9), (18, 2, 14), gold, deco=lambda pp, f, mm, r: [pp.at(f["up"], x, y, COIN, glow=True) for x in range(f["up"][2]) for y in range(f["up"][3]) if r.random() < 0.15])
    sk = m.bone("sack", "body", (-4, p["top"], p["back"]))
    m.cube(sk, (-9, p["top"] - 12, p["back"] + 0.5), (11, 13, 8), sack, deco=lambda pp, f, mm, r: [pp.at(f["up"], x, y, COIN, glow=True) for x in range(f["up"][2]) for y in range(f["up"][3]) if r.random() < 0.3])
    shard_ring(m, "root", "p2_coins", (0, 34, 0), 20, 18, coin, length=1.4, width=2.0, rise=10, seed=17)
    anims = figure_anims(1.6, 16, heavy=True, rags=rags, orbit={"p2_coins": 4.0},
                         extra_idle={"beard": {"rotation": {"0.0": [0, 0, 0], "1.6": [6, 0, 0], "3.2": [0, 0, 0]}}})
    anims["signature"] = signature_anim(*SIGNATURES["avarok"])
    m.write({"avarok": None, "avarok_broken": broken_form(COIN)}, anims, (7, 7))


# ------------------------------------------------------------------------------------------------ Gularth, Gluttony

def gularth():
    """Gularth, Archsin of Gluttony (Act IV): a giant of raw pink flesh, belly split by chains, a head that is
    mostly a gaping maw of teeth glowing red within, small mad yellow eyes, a bloody cleaver."""
    m = Model("gularth", 512, density=2, scale=1.45)
    flesh = mat((120, 66, 60), (186, 120, 106), (228, 176, 158), "plain", cracks=((200, 40, 40), 0.003), noise=7)
    chain = steel((40, 40, 44), (90, 90, 96), (150, 150, 160), seam=2, noise=4)
    mouth_red = (230, 60, 40)
    p = figure(m, 1.0, {"skin": flesh, "chest": flesh, "waist": flesh, "upper_arm": flesh, "forearm": flesh, "hand": flesh, "thigh": flesh,
                        "shin": flesh, "foot": flesh, "pads": flesh}, chest=(18, 10, 13), waist=(18, 6, 16), arm=6, leg=6.4, hunch=10,
               head=10, neck=0.6, leg_len=(6, 6), arm_len=(9, 10), shoulder_pads=False, finger_len=3.4)
    m.cube("body", (-12, p["hips"] - 4, -10), (24, 14, 19), flesh, deco=lambda pp, f, mm, r: [pp.at(f["north"], f["north"][2] // 2, y, (110, 50, 46)) for y in range(f["north"][3])])   # the vast belly
    for y in (p["hips"] + 1, p["hips"] + 6):
        m.cube("body", (-12.5, y, -10.5), (25, 1.6, 20), chain)
    for x in (-7, 5):
        m.cube("body", (x, p["hips"] - 4, -10.6), (1.6, 18, 1), chain)
    hx, hy, hz, hs = p["head"]
    sculpted_face(m, "head", p["head"], flesh, glow_mat((255, 210, 60)), kind="demon", mouth=glow_mat(mouth_red), anger=24, jaw_drop=24,
                  eye_size=(1.0, 0.8))
    for i in range(7):                                                   # a second row of teeth, crooked
        x = hx + 0.8 + i * (hs - 1.6) / 6
        m.cube("jaw", (x - 0.4, hy + hs * 0.3, hz + 1.4), (0.8, 1.6 + (i % 2), 0.8), mat((150, 140, 110), (220, 214, 196), (250, 248, 240)),
               rotation=(0, 0, (-12 if i % 2 else 10)))
    sin_core(m, {"chest": (-12, p["hips"] - 4, -10, 24, 14, 19)}, mouth_red, size=4)
    names = []
    for side, sx in (("left", 1), ("right", -1)):
        a = p["arms"][side]
        names.append(shackle(m, side + "_forearm", side + "_chain", (a["wrist"][0], a["wrist"][1] + 3, 0), a["half"] + 0.4, 5, chain, side=sx))
    cl, grip = held(m, "right", p, "cleaver", tilt=-50)
    gx, gy, gz = grip
    m.cube(cl, (gx - 1, gy - 8, gz - 1), (2, 10, 2), mat((40, 30, 20), (80, 60, 40), (120, 96, 66), "cloth"))
    m.cube(cl, (gx - 0.8, gy - 22, gz - 9), (1.6, 15, 12), steel((70, 70, 76), (140, 140, 150), (210, 210, 220), cracks=((220, 40, 30), 0.02)),
           deco=lambda pp, f, mm, r: [pp.at(f[n], x, y, (160, 20, 20)) for n in ("east", "west") for x in range(f[n][2]) for y in range(f[n][3]) if r.random() < 0.12])
    shard_ring(m, "root", "p2_bones", (0, 30, 0), 20, 10, mat((150, 140, 110), (220, 214, 196), (250, 248, 240)), length=4, width=1.6, rise=8, seed=19)
    anims = figure_anims(1.8, 14, heavy=True, chains=names, orbit={"p2_bones": 6.0},
                         extra_idle={"jaw": {"rotation": {"0.0": [0, 0, 0], "0.8": [18, 0, 0], "1.0": [4, 0, 0], "1.2": [18, 0, 0], "3.2": [0, 0, 0]}}})
    anims["signature"] = signature_anim(*SIGNATURES["gularth"])
    m.write({"gularth": None, "gularth_broken": broken_form((230, 40, 40))}, anims, (7, 7))


# ------------------------------------------------------------------------------------------------ Envyris, Envy

ENVY = (90, 255, 140)


def envyris():
    """Envyris, Archsin of Envy (Act IV): a queen of shadow veined with green fire, a crown of thorns, thorns from
    her shoulders, a face pale as a mask with green eyes and tears of light, a scythe."""
    m = Model("envyris", 512, density=2, scale=1.45)
    shadow = mat((6, 10, 8), (16, 28, 20), (36, 56, 42), "void", cracks=(ENVY, 0.012), noise=5)
    mask = mat((150, 170, 156), (200, 220, 206), (236, 246, 240), "plain", noise=3)
    thorn = steel((20, 40, 26), (50, 90, 60), (110, 170, 120), cracks=(ENVY, 0.02), noise=4)
    p = lady(m, {"skin": mask, "chest": shadow, "waist": shadow, "upper_arm": shadow, "forearm": shadow, "hand": mask, "thigh": shadow,
                 "shin": shadow, "foot": shadow}, claw=glow_mat(ENVY), finger_len=4)
    tears = centered(["", "", "", "", "", "", "", "..G..G..", "..G..G..", "........", "..G..G.."], {"G": ENVY}, top=0, glow_chars="G")
    sculpted_face(m, "head", p["head"], mask, glow_mat(ENVY), kind="noble", lips=mat((10, 40, 20), (20, 70, 40), (40, 100, 60)), deco=tears)
    hx, hy, hz, hs = p["head"]
    locks = long_hair(m, p, shadow, length=18, strips=5)
    for i in range(7):                                                   # the crown of thorns, one broken
        x = hx + i * hs / 6
        if i == 4:
            m.cube("head", (x - 0.5, hy + hs * 1.0, hz + 1), (1, 1.2, 1), glow_mat(ENVY))
            continue
        spike(m, "head", (x, hy + hs * 0.98, hz + 1 + (i % 2)), (0.2 * (i - 3), 1, 0.2), 4 + (i % 2) * 3, 1.2, thorn, tip=glow_mat(ENVY), segments=2)
    sin_core(m, p, ENVY, size=3.2)
    gown(m, "body", p["hips"], 7, 5.4, 16, shadow, flare=0.9, tiers=6)
    rags = rag_strips(m, "body", "cloak", p["top"], 13, 28, 5, shadow, p["back"] + 0.8, lengths=(1, 0.8, 0.95, 0.75, 1))
    for sx in (1, -1):
        a = p["arms"]["left" if sx > 0 else "right"]
        for k in range(3):
            spike(m, ("left" if sx > 0 else "right") + "_arm", (a["shoulder"][0] + sx * (0.5 + k * 0.8), a["shoulder"][1] + 0.8, -1 + k * 1.2),
                  (sx * 0.6, 1, 0.3), 7 - k * 1.5, 1.6, thorn, tip=glow_mat(ENVY), segments=2)
    sc, grip = held(m, "right", p, "scythe", tilt=-20)
    gx, gy, gz = grip
    m.cube(sc, (gx - 0.7, gy - 30, gz - 0.7), (1.4, 44, 1.4), mat((10, 14, 12), (30, 40, 34), (60, 76, 66), "cloth"))
    curved_horn(m, sc, (gx, gy + 13, gz), (0, 0.2, -1), (1, 0, 0), 22, 2.6, thorn, segments=6, bend=14, taper=0.85)
    shard_ring(m, "root", "p2_eyes", (0, 30, 0), 15, 9, glow_mat(ENVY), length=1.2, width=1.6, rise=8, seed=23)
    anims = figure_anims(1.2, 16, rags=locks + rags, orbit={"p2_eyes": 6.0})
    anims["signature"] = signature_anim(*SIGNATURES["envyris"])
    m.write({"envyris": None, "envyris_broken": broken_form(ENVY)}, anims, (7, 7))


# ------------------------------------------------------------------------------------------------ Prython, Pride

PRIDE_BLUE = (120, 190, 255)


def prython():
    """Prython, Archsin of Pride (Act V): a tall king in black armour edged with gold, a great halo of golden spikes
    behind him (cracked, like all he claims), tattered dark wings with golden ribs, a helm with a face of cold blue
    light, a long sword of blue steel."""
    m = Model("prython", 512, density=2, scale=1.6)
    plate = steel((14, 12, 10), (40, 34, 26), (90, 76, 50), rivets=True, seam=4, cracks=((255, 210, 90), 0.003), noise=4)
    gold = steel((140, 90, 20), (230, 170, 50), (255, 236, 150), noise=3)
    face = mat((10, 12, 20), (24, 28, 44), (50, 56, 80), "plain", noise=2)
    p = figure(m, 1.0, {"skin": face, "chest": plate, "waist": plate, "upper_arm": plate, "forearm": plate, "hand": plate, "thigh": plate,
                        "shin": plate, "foot": plate, "pads": gold}, chest=(13, 9, 7.5), arm=4, leg=4.6, hunch=2, head=8, claw=gold)
    sculpted_face(m, "head", p["head"], face, glow_mat(PRIDE_BLUE), kind="skull", brow=gold, anger=16)
    hx, hy, hz, hs = p["head"]
    m.cube("head", (hx - 0.6, hy + hs * 0.6, hz - 0.6), (hs + 1.2, hs * 0.45, hs + 1.2), plate,
           deco=lambda pp, f, mm, r: [pp.clear(f["north"][0] + x, f["north"][1] + y) for x in range(2, f["north"][2] - 2) for y in range(f["north"][3] - 2, f["north"][3])])
    for i in range(5):
        spike(m, "head", (hx + 0.4 + i * (hs - 0.8) / 4, hy + hs * 1.02, hz + 0.4), (0.25 * (i - 2), 1, 0), 4 + (2 - abs(i - 2)) * 2.4, 1.4, gold, segments=2)
    sin_core(m, p, PRIDE_BLUE)
    for sx in (1, -1):
        a = p["arms"]["left" if sx > 0 else "right"]
        for k in range(3):
            spike(m, ("left" if sx > 0 else "right") + "_arm", (a["shoulder"][0] + sx * (1 + k), a["shoulder"][1] + 1.6, -1.5 + k * 1.5),
                  (sx * 0.5, 1, 0.2), 7 - k * 1.5, 1.6, gold, segments=2)
    halo = m.bone("halo", "body", (0, p["top"] + 6, p["back"] + 3))
    broken_halo(m, halo, (0, p["top"] + 6, p["back"] + 3), 12, 1.6, gold, gaps=((60, 80), (250, 268)), rays=18, ray_len=5)
    rags = rag_strips(m, "body", "cloak", p["top"] - 1, 12, 26, 5, mat((10, 8, 8), (30, 24, 20), (60, 50, 40), "cloth", ragged=4),
                      p["back"] + 0.8, lengths=(1, 0.8, 0.95, 0.75, 1))
    membrane = mat((8, 6, 8), (26, 20, 24), (54, 44, 50), "cloth", ragged=3, noise=4)
    bat_wing(m, "body", 1, (3, p["top"] - 6, p["back"] + 1), 30, 20, membrane, gold, claws=gold)
    bat_wing(m, "body", -1, (-3, p["top"] - 6, p["back"] + 1), 30, 20, membrane, gold, claws=gold)
    sw, grip = held(m, "right", p, "sword", tilt=-60)
    gx, gy, gz = grip
    m.cube(sw, (gx - 0.8, gy - 5, gz - 0.8), (1.6, 6, 1.6), plate)
    m.cube(sw, (gx - 4.5, gy - 6, gz - 1.2), (9, 1.6, 2.4), gold)
    spike(m, sw, (gx, gy - 5.6, gz), (0, -1, 0), 32, 3.2, steel((40, 50, 70), (90, 120, 170), (180, 220, 255), cracks=(PRIDE_BLUE, 0.02)),
          tip=glow_mat(PRIDE_BLUE), segments=3)
    shard_ring(m, "root", "p2_halo", (0, 40, 0), 20, 12, gold, length=5, width=1.6, rise=8, seed=29)
    anims = figure_anims(1.4, 18, heavy=True, rags=rags, orbit={"p2_halo": 6.0},
                         extra_idle={"halo": {"rotation": {"0.0": [0, 0, 0], "3.2": [0, 0, 360]}},
                                     "left_wing": {"rotation": {"0.0": [0, 0, 0], "1.6": [0, -10, -8], "3.2": [0, 0, 0]}},
                                     "right_wing": {"rotation": {"0.0": [0, 0, 0], "1.6": [0, 10, 8], "3.2": [0, 0, 0]}}})
    anims["signature"] = signature_anim(*SIGNATURES["prython"])
    m.write({"prython": None, "prython_broken": broken_form((255, 210, 90))}, anims, (10, 9))


# ------------------------------------------------------------------------------------------------ Nahrazel, the First Fallen

NAH = (196, 110, 255)


def ash_form(m):
    """Nahrazel's first form: the Void turned to ash, its light to embers."""
    dark, mid, light = m["pal"]
    out = dict(m)
    grey = lambda c: (int(0.3 * c[0] + 0.59 * c[1] + 0.11 * c[2]),) * 3
    out["pal"] = tuple(mix(grey(c), (60, 54, 50), 0.4) for c in (dark, mid, light))
    if m["style"] == "glow":
        out["pal"] = ((160, 50, 10), (255, 120, 30), (255, 210, 120))
    if m.get("cracks"):
        out["cracks"] = ((255, 120, 30), m["cracks"][1])
    return out


def nahrazel():
    """Nahrazel, the First Fallen (the end of Act V): a winged colossus of the Void, great horns (one snapped), a
    crown of spikes, plated shoulders bristling, claws like scythes, a core of violet light in a chest split open,
    a face of fangs and burning eyes. First ash and ember (nahrazel_ash), then the Void, then breaking apart in
    the Codex (nahrazel_broken)."""
    m = Model("nahrazel", 512, density=2, scale=2.3)
    hide = mat((10, 4, 18), (32, 14, 52), (70, 40, 110), "void", cracks=(NAH, 0.014), noise=6)
    plate = steel((6, 2, 12), (24, 10, 40), (60, 34, 90), seam=4, cracks=(NAH, 0.006), noise=4)
    horn_m = mat((20, 10, 26), (60, 36, 70), (130, 100, 150), "plain", noise=4)
    core = glow_mat(NAH)
    p = figure(m, 1.0, {"skin": hide, "chest": plate, "waist": hide, "upper_arm": hide, "forearm": plate, "hand": hide, "thigh": hide,
                        "shin": plate, "foot": plate, "pads": plate}, chest=(15, 10, 9), waist=(9, 4, 7), arm=4.8, leg=5.2, hunch=14,
               head=8, claw=horn_m, finger_len=4, arm_len=(9.5, 10))
    sculpted_face(m, "head", p["head"], hide, glow_mat((230, 160, 255)), kind="demon", brow=plate, mouth=core, anger=24, jaw_drop=10)
    hx, hy, hz, hs = p["head"]
    curved_horn(m, "head", (hx + hs - 0.4, hy + hs * 0.85, hz + hs * 0.4), (0.7, 0.8, 0.2), (0, 0, 1), 26, 4, horn_m, segments=8, bend=-16)
    curved_horn(m, "head", (hx + 0.4, hy + hs * 0.85, hz + hs * 0.4), (-0.7, 0.8, 0.2), (0, 0, 1), 26, 4, horn_m, segments=8, bend=16,
                broken=True, break_mat=core)
    for i in range(5):
        spike(m, "head", (hx + 1 + i * (hs - 2) / 4, hy + hs, hz + 2), (0.2 * (i - 2), 1, 0.4), 4 + (2 - abs(i - 2)) * 2, 1.4, horn_m, segments=2)
    cx0, cy0, cz0, cw, ch, cd = p["chest"]
    m.cube("body", (-3.5, cy0 + 2, cz0 - 0.4), (7, 6, 0.6), core,
           deco=centered(["..WWW..", ".WWWWW.", "WWWWWWW", ".WWWWW.", "..WWW.."], {"W": (250, 230, 255)}, top=0, glow_chars="W"))
    for sx in (1, -1):                                                   # the chest split open round the core
        m.cube("body", (sx * 4 - 1, cy0 + 1, cz0 - 1.2), (2, 8, 1), plate, rotation=(0, 0, 10 * sx))
        a = p["arms"]["left" if sx > 0 else "right"]
        for k in range(4):
            spike(m, ("left" if sx > 0 else "right") + "_arm", (a["shoulder"][0] + sx * (1 + k * 0.8), a["shoulder"][1] + 1.6, -3 + k * 2),
                  (sx * 0.5, 1, 0.2), 9 - k * 1.5, 2.2, horn_m, segments=3)
    for i in range(5):
        spike(m, "body", (0, p["top"] - 2 - i * 2.4, p["back"]), (0, 0.6, 1), 8 - i, 2, horn_m, segments=2)
    membrane = mat((10, 4, 18), (40, 14, 70), (90, 50, 140), "cloth", ragged=4, cracks=(NAH, 0.01), noise=4)
    bat_wing(m, "body", 1, (3, p["top"] - 5, p["back"] + 1), 36, 24, membrane, horn_m, claws=horn_m)
    bat_wing(m, "body", -1, (-3, p["top"] - 5, p["back"] + 1), 36, 24, membrane, horn_m, claws=horn_m)
    shard_ring(m, "root", "p2_void", (0, 30, 0), 20, 12, plate, length=5, width=2.4, rise=10, seed=31)
    anims = figure_anims(2.0, 18, heavy=True, orbit={"p2_void": 6.0}, two_handed=True,
                         extra_idle={"left_wing": {"rotation": {"0.0": [0, 0, 0], "1.6": [0, -14, -10], "3.2": [0, 0, 0]}},
                                     "right_wing": {"rotation": {"0.0": [0, 0, 0], "1.6": [0, 14, 10], "3.2": [0, 0, 0]}}})
    anims["signature"] = signature_anim(*SIGNATURES["nahrazel"])
    m.write({"nahrazel": None, "nahrazel_ash": ash_form, "nahrazel_broken": broken_form(NAH)}, anims, (14, 14))


# the animation of each boss's signature attack: its kind and the wind-up in ticks (as in its Java Signature)
SIGNATURES = {'kaleth': ('overhead', 30),
              'serath': ('cross', 20),
              'mirael': ('spread', 25),
              'thessyn': ('rear', 25),
              'dormiel': ('raise_left', 30),
              'goldarc': ('thrust', 20),
              'nixara': ('raise_left', 30),
              'fenrath': ('pounce', 18),
              'shadeyn': ('thrust', 25),
              'solrath': ('thrust', 35),
              'brass_sentinel': ('slam2', 30),
              'vorath': ('overhead2', 30),
              'luxara': ('spread', 25),
              'morthis': ('slam2', 35),
              'avarok': ('sweep', 28),
              'gularth': ('overhead', 30),
              'envyris': ('spin', 25),
              'prython': ('leap', 40),
              'nahrazel': ('cross', 30)}


BOSSES = {f.__name__: f for f in (kaleth, serath, mirael, thessyn, dormiel, goldarc, nixara, fenrath, shadeyn, solrath,
                                  brass_sentinel, vorath, luxara, morthis, avarok, gularth, envyris, prython, nahrazel)}

if __name__ == "__main__":
    for name in sys.argv[1:] or BOSSES:
        BOSSES[name]()
