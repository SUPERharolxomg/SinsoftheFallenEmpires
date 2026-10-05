"""GeckoLib models of the bosses of Acts III and IV, with the Model and Painter of make_mob_models.py: bigger than
the creatures round them, with glowing eyes and marks so their presence is felt (docs/Jugabilidad.md, boss fights).

  Broken Oaths  mirael (a veiled woman floating in silks), thessyn (half woman, half spider), dormiel (a hunched
                watchman with a lantern, eyes shut), goldarc (a knight of gold with a shield of coins), nixara (a
                hooded merchant with no face), fenrath (a beast that is mostly maw), shadeyn (a knight of mirrors)
  Archsins      luxara (a horned queen with wings of silk), morthis (a swollen pharaoh on his throne), avarok (a
                colossal king of gold with his sack), gularth (a giant glutton with a cleaver), envyris (a queen of
                shadow and green fire with a scythe)

Run from the repository root: python scripts/make_boss_models.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from make_mob_models import Model, mat, anim, rot, rotpos, swing, humanoid_walk, combine, rift, clear_front_below, mix  # noqa: E402
from make_empire_mobs import eyes, stripes, band, spots, humanoid, melee_anims  # noqa: E402
from creature_kit import bat_wing, centered, glow_mat, halo, horn, spikes, veins  # noqa: E402


def glow(color):
    return mat(mix(color, (0, 0, 0), 0.4), color, mix(color, (255, 255, 255), 0.5), "glow")


def boss_anims(period=1.4, legs=24, arms=16, heavy=False, swing_arm="right_arm"):
    a = melee_anims(period, legs, arms, swing_arm=swing_arm)
    t = 1.0 if heavy else 0.7
    a["attack"] = anim(t, {swing_arm: rot((0.0, [0, 0, 0]), (t * 0.45, [-160, 0, 12]), (t * 0.65, [25, 0, 0]), (t, [0, 0, 0])),
                           "body": rot((0.0, [0, 0, 0]), (t * 0.45, [-12, 0, 0]), (t * 0.65, [16, 0, 0]), (t, [0, 0, 0]))}, loop=False)
    return a


# ------------------------------------------------------------------------------------------------ Mirael

VEIL = mat((50, 30, 80), (110, 70, 160), (190, 160, 230), "cloth", ragged=3, noise=4)
SILVER = mat((100, 104, 120), (170, 176, 192), (236, 240, 250), "metal", noise=3)
PALE_SKIN = mat((150, 130, 160), (206, 186, 214), (242, 230, 246), "plain", noise=3)
VIOLET = (220, 120, 255)


def mirael():
    m = Model("mirael", 128, scale=1.3)
    s = 1.25
    m.bone("root")
    body = m.bone("body", "root", (0, 12 * s, 0))
    m.cube(body, (-4.5 * s, 0, -3 * s), (9 * s, 14 * s, 6 * s), VEIL, deco=band((200, 200, 220), 2))       # the long skirt, she floats
    m.cube(body, (-3.5 * s, 14 * s, -2 * s), (7 * s, 10 * s, 4 * s), VEIL, deco=band((220, 220, 240), 3))
    head = m.bone("head", "body", (0, 24 * s, 0))
    m.cube(head, (-3 * s, 24 * s, -3 * s), (6 * s, 7 * s, 6 * s), PALE_SKIN, deco=eyes(VIOLET, 3, 1, 2))
    m.cube(head, (-3.5 * s, 23 * s, -3.5 * s), (7 * s, 9 * s, 7 * s), VEIL, inflate=0.3, deco=clear_front_below(2))
    m.cube(head, (-4 * s, 23 * s, -4 * s), (8 * s, 10 * s, 8 * s), VEIL, inflate=0.5, deco=clear_front_below(2))   # the deep hood
    m.cube(body, (-5 * s, 0, 3 * s), (10 * s, 24 * s, 1), VEIL)                                                    # the long cloak
    halo = m.bone("halo", "head", (0, 30 * s, 3 * s))
    m.cube(halo, (-5 * s, 27 * s, 4 * s), (10 * s, 10 * s, 1), SILVER,
           deco=lambda p, f, mm, r: [p.clear(f["north"][0] + x, f["north"][1] + y) for x in range(3, 9) for y in range(3, 9)] and None)
    for side, sx in (("left", 1), ("right", -1)):
        arm = m.bone(side + "_arm", "body", (4 * s * sx, 22 * s, 0), rotation=(0, 0, 18 * -sx))
        m.cube(arm, ((3.5 if sx > 0 else -6.5) * s, 10 * s, -1.5 * s), (3 * s, 13 * s, 3 * s), PALE_SKIN)
        m.cube(arm, ((3.4 if sx > 0 else -6.6) * s, 12 * s, -1.6 * s), (3.2 * s, 2, 3.2 * s), SILVER)
        trail = m.bone(side + "_trail", side + "_arm", ((5 if sx > 0 else -5) * s, 12 * s, 0))
        m.cube(trail, ((5 if sx > 0 else -6) * s, 0, 1), (1, 14 * s, 5 * s), VEIL)
    anims = {
        "idle": anim(2.4, {"root": rotpos(swing([0, 0, 0], [0, 0, 0], 2.4), swing([0, 2, 0], [0, 4, 0], 2.4)),
                           "left_arm": rot(*swing([-20, 0, -18], [-34, 0, -30], 2.4)),
                           "right_arm": rot(*swing([-34, 0, 30], [-20, 0, 18], 2.4)),
                           "left_trail": rot(*swing([10, 0, 0], [-20, 0, 0], 1.2)), "right_trail": rot(*swing([-20, 0, 0], [10, 0, 0], 1.2)),
                           "halo": rot(*swing([0, 0, -8], [0, 0, 8], 2.4))}),
        "walk": anim(1.2, {"root": rotpos(swing([0, 0, 0], [0, 0, 0], 1.2), swing([0, 2, 0], [0, 3, 0], 0.6)),
                           "body": rot(*swing([8, -6, 0], [8, 6, 0], 1.2)),
                           "left_trail": rot(*swing([20, 0, 0], [40, 0, 0], 0.6)), "right_trail": rot(*swing([40, 0, 0], [20, 0, 0], 0.6))}),
        "attack": anim(0.6, {"right_arm": rot((0.0, [0, 0, 18]), (0.25, [-120, -30, 40]), (0.45, [10, 30, 0]), (0.6, [0, 0, 18])),
                             "left_arm": rot((0.0, [0, 0, -18]), (0.3, [-120, 30, -40]), (0.5, [10, -30, 0]), (0.6, [0, 0, -18]))}, loop=False),
    }
    m.write({"mirael": None}, anims, (2.4, 4))


# ------------------------------------------------------------------------------------------------ Thessyn

CHITIN = mat((14, 18, 16), (36, 46, 40), (80, 96, 84), "metal", seam=3, noise=5)
SILK_WRAP = mat((170, 176, 160), (214, 218, 204), (244, 246, 236), "cloth", fold=1, noise=4)
SPIDER_GREEN = (140, 255, 120)
HAIR_BLACK = mat((8, 8, 10), (24, 24, 30), (50, 50, 60), "fur", ragged=3, noise=6)


def thessyn():
    m = Model("thessyn", 128, scale=1.4)
    m.bone("root")
    abdomen = m.bone("abdomen", "root", (0, 12, 4))
    m.cube(abdomen, (-6, 8, 4), (12, 10, 14), CHITIN, deco=combine(spots(SPIDER_GREEN, 0.02, glow=True, faces=("up",)), stripes((60, 80, 66), 3, "up")))
    m.cube(abdomen, (-4.5, 9, -2), (9, 7, 7), CHITIN)                                                  # the cephalothorax
    for i, z in enumerate((-1, 2, 5, 8)):
        for side, sx in (("l", 1), ("r", -1)):
            leg = m.bone("leg_%s%d" % (side, i), "abdomen", (4.5 * sx, 13, z))
            m.cube(leg, ((4.5 if sx > 0 else -12.5), 12, z - 0.75), (8, 1.5, 1.5), CHITIN)
            m.cube(leg, ((11.5 if sx > 0 else -13), 0, z - 0.75), (1.5, 13, 1.5), CHITIN)
    body = m.bone("body", "abdomen", (0, 16, -1), rotation=(-6, 0, 0))
    m.cube(body, (-4, 16, -3), (8, 11, 5), SILK_WRAP, deco=stripes((190, 194, 180), 2))
    head = m.bone("head", "body", (0, 27, -1))
    m.cube(head, (-3.5, 27, -4.5), (7, 7, 7), PALE_SKIN,
           deco=lambda p, f, mm, r: [p.at(f["north"], x, y, SPIDER_GREEN, glow=True) for x, y in ((1, 2), (5, 2), (2, 3), (4, 3), (1, 4), (5, 4))])
    m.cube(head, (-4, 26, -5), (8, 9, 8), HAIR_BLACK, inflate=0.2, deco=clear_front_below(2))
    m.cube(head, (-3.5, 16, 2), (7, 11, 1.5), HAIR_BLACK)                                                # long hair down her back
    m.cube(abdomen, (-1.5, 18, 8), (3, 0.6, 6), glow_mat((230, 30, 40)),
           deco=centered(["XXX", ".X.", "XXX"], {"X": (255, 60, 60)}, face="up", top=0, glow_chars="X"))  # the red hourglass
    for side, sx in (("left", 1), ("right", -1)):
        arm = m.bone(side + "_arm", "body", (5 * sx, 26, -0.5))
        m.cube(arm, ((4 if sx > 0 else -7), 15, -2), (3, 11, 3), PALE_SKIN)
        m.cube(arm, ((4.5 if sx > 0 else -6.5), 13, -2), (1, 2, 1), CHITIN)
    legs = {}
    for i in range(4):
        for side in ("l", "r"):
            a, b = ([0, 0, 14], [0, 0, -10]) if (i + (side == "l")) % 2 else ([0, 0, -10], [0, 0, 14])
            legs["leg_%s%d" % (side, i)] = rot(*swing(a, b, 0.6))
    anims = {
        "idle": anim(2.0, {"abdomen": rotpos(swing([0, 0, 0], [-2, 0, 0], 2.0), swing([0, 0, 0], [0, 0.5, 0], 2.0)),
                           "left_arm": rot(*swing([-30, 0, -10], [-40, 0, -14], 2.0)), "right_arm": rot(*swing([-40, 0, 14], [-30, 0, 10], 2.0))}),
        "walk": anim(0.6, dict(legs, abdomen=rotpos(swing([0, 0, 2], [0, 0, -2], 0.6), swing([0, 0, 0], [0, 0.6, 0], 0.3)))),
        "attack": anim(0.6, {"body": rot((0.0, [-6, 0, 0]), (0.25, [-30, 0, 0]), (0.4, [20, 0, 0]), (0.6, [-6, 0, 0])),
                             "left_arm": rot((0.0, [0, 0, 0]), (0.25, [-140, 0, -20]), (0.4, [20, 0, 0]), (0.6, [0, 0, 0])),
                             "right_arm": rot((0.0, [0, 0, 0]), (0.25, [-140, 0, 20]), (0.4, [20, 0, 0]), (0.6, [0, 0, 0]))}, loop=False),
    }
    m.write({"thessyn": None}, anims, (3, 3.5))


# ------------------------------------------------------------------------------------------------ Dormiel

KHEMET_ROBE = mat((90, 76, 50), (150, 130, 92), (200, 182, 140), "cloth", ragged=2, noise=5)
NEMES = mat((20, 40, 90), (40, 70, 160), (90, 130, 220), "cloth", noise=4)
GOLD_M = mat((110, 74, 20), (196, 146, 50), (250, 214, 120), "metal", noise=4)
SLEEP_BLUE = (120, 180, 255)
LANTERN = mat((60, 120, 200), (120, 200, 255), (220, 240, 255), "glow")


def dormiel():
    m = Model("dormiel", 128, scale=1.15)
    body, head = humanoid(m, PALE_SKIN, KHEMET_ROBE, KHEMET_ROBE, KHEMET_ROBE, scale=1.3, arm_len=14, hunch=16,
                          head_deco=lambda p, f, mm, r: [p.at(f["north"], x, 4, SLEEP_BLUE, glow=True) for x in (1, 2, 5, 6)],  # eyes shut, a line of light
                          body_deco=band((200, 160, 60), 6))
    m.cube(head, (-5.2, 31.2, -5.2), (10.4, 10.4, 10.4), NEMES, inflate=0.5,
           deco=combine(stripes((200, 160, 60), 2, "east"), stripes((200, 160, 60), 2, "west"), stripes((200, 160, 60), 2, "south"),
                        clear_front_below(2)))                                                          # the nemes, open for the face
    m.cube(head, (-6.2, 25, -2), (2, 7, 5), NEMES, mirror_x=True)                                       # its lappets on the shoulders
    m.cube(head, (-3.6, 41.6, -3.6), (7.2, 10, 7.2), NEMES, deco=stripes((220, 180, 70), 2, "north"))     # a tall headdress
    m.cube(head, (-4.2, 41, -4.2), (8.4, 1.4, 8.4), GOLD_M)
    m.cube(head, (-1, 51.6, -1), (2, 3, 2), GOLD_M)
    m.cube(body, (-6, 0, -3.5), (12, 16, 7), KHEMET_ROBE)                                               # the long robe over the legs
    lantern = m.bone("lantern", "left_arm", (7, 11, 0))
    m.cube(lantern, (6.5, 2, -1.5), (3, 4, 3), LANTERN)
    m.cube(lantern, (6.2, 6, -1.8), (3.6, 1, 3.6), GOLD_M)
    m.cube(lantern, (7.5, 7, -0.5), (1, 4, 1), GOLD_M)
    staff = m.bone("staff", "right_arm", (-8, 12, 0))
    m.cube(staff, (-9, -6, -1), (2, 44, 2), GOLD_M)
    m.cube(staff, (-10, 37, -2), (4, 3, 4), LANTERN)
    anims = boss_anims(1.8, 16, 10, heavy=True)
    anims["idle"] = anim(3.2, {"body": rot(*swing([16, 0, 0], [20, 0, 2], 3.2)), "head": rot(*swing([0, 0, 0], [14, 0, 4], 3.2)),
                               "left_arm": rot(*swing([-40, 0, -6], [-44, 0, -8], 3.2))})
    m.write({"dormiel": None}, anims, (2.4, 5))


# ------------------------------------------------------------------------------------------------ Luxara

GOWN = mat((90, 10, 60), (170, 40, 120), (240, 120, 190), "cloth", ragged=2, noise=4)
SILK_WING = mat((140, 60, 160), (220, 140, 230), (255, 220, 250), "cloth", cracks=((255, 200, 255), 0.02), noise=3)
HORN = mat((40, 20, 40), (90, 60, 90), (170, 140, 170), "plain", noise=4)
PINK = (255, 110, 200)


def luxara():
    """The sheet's Luxara: a tall temptress in a magenta gown, great bat wings of crimson membrane, ram horns curling
    back from her brow, long red hair, gold at her throat and a stare of pink light."""
    m = Model("luxara", 128, scale=1.25)
    s = 1.45
    body, head = humanoid(m, PALE_SKIN, GOWN, GOWN, PALE_SKIN, scale=s, body_deco=band((250, 210, 110), 3),
                          head_deco=centered(["", "", "", "BB....BB", "PPP..PPP", "", "", "...RR...", "..RRRR.."],
                                             {"B": (60, 10, 30), "P": PINK, "R": (200, 30, 80)}, top=0, glow_chars="P"))   # brows, eyes, lips
    hair = mat((70, 6, 14), (140, 20, 30), (200, 60, 60), "fur", ragged=4, noise=6)
    m.cube(head, (-4.5 * s, 24 * s, -4.5 * s), (9 * s, 8.5 * s, 9 * s), hair, inflate=0.3, deco=clear_front_below(2))
    m.cube(head, (-4 * s, 12 * s, 3.5 * s), (8 * s, 13 * s, 1.5), hair)                                  # hair down her back
    dark_horn = mat((20, 6, 14), (60, 20, 40), (120, 70, 100), "plain", noise=4)
    for sx in (1, -1):                                                                                  # great ram horns, curling back
        horn(m, head, ((4.2 if sx > 0 else -4.2) * s, 31.5 * s, -1 * s), (0.75 * sx, 0.65, 0.25), 8, dark_horn, start=4.2, taper=0.86,
             curl=(28, 0, -22 * sx), step=3.2)
    m.cube(head, (-4.6 * s, 31.2 * s, -4.6 * s), (9.2 * s, 1.2, 9.2 * s), GOLD_M)                         # a circlet
    m.cube(body, (-2 * s, 22 * s, -2.4 * s), (4 * s, 1.5, 0.6), GOLD_M)                                   # gold at her throat
    m.cube(body, (-6 * s, 0, -4 * s), (12 * s, 14 * s, 8 * s), GOWN)                                       # the wide gown
    membrane = mat((70, 6, 40), (150, 20, 80), (220, 70, 140), "cloth", cracks=((255, 120, 200), 0.006), noise=4)
    rib = mat((30, 6, 20), (60, 14, 40), (110, 40, 80), "plain", noise=3)
    bat_wing(m, "body", 1, (2.5 * s, 10 * s, 2.5 * s), 22 * s, 13 * s, membrane, rib, claws=dark_horn)
    bat_wing(m, "body", -1, (-2.5 * s, 10 * s, 2.5 * s), 22 * s, 13 * s, membrane, rib, claws=dark_horn)
    anims = boss_anims(1.4, 18, 14)
    # the wing bones already hold their sweep and lift: the animations only add a slow beat on top
    anims["idle"] = anim(3.0, {"left_wing": rot(*swing([0, 0, 0], [0, -10, -8], 3.0)), "right_wing": rot(*swing([0, 0, 0], [0, 10, 8], 3.0)),
                               "body": rot(*swing([0, -4, 0], [0, 4, 0], 3.0)),
                               "left_arm": rot(*swing([-10, 0, -10], [-20, 0, -20], 3.0)), "right_arm": rot(*swing([-20, 0, 20], [-10, 0, 10], 3.0))})
    anims["walk"]["bones"]["left_wing"] = rot(*swing([0, 0, 0], [0, -12, -12], 0.7))
    anims["walk"]["bones"]["right_wing"] = rot(*swing([0, 0, 0], [0, 12, 12], 0.7))
    m.write({"luxara": None}, anims, (6, 6))


# ------------------------------------------------------------------------------------------------ Morthis

ROT_SKIN = mat((70, 80, 50), (120, 130, 90), (170, 176, 130), "skin_cracked", cracks=((120, 255, 120), 0.004), noise=7)
MUD_LINEN = mat((60, 52, 36), (110, 100, 70), (160, 150, 112), "cloth", ragged=3, noise=7)
THRONE = mat((70, 56, 36), (120, 100, 66), (170, 150, 110), "rock", noise=8)
MARSH = (120, 255, 120)


def morthis():
    m = Model("morthis", 128, scale=1.3)
    m.bone("root")
    throne = m.bone("throne", "root", (0, 0, 0))
    m.cube(throne, (-13, 0, -2), (26, 14, 16), THRONE)
    m.cube(throne, (-13, 14, 10), (26, 26, 4), THRONE, deco=band((200, 170, 70), 2))
    m.cube(throne, (-14, 14, -2), (3, 10, 12), THRONE, mirror_x=True)
    body = m.bone("body", "root", (0, 16, 4))
    m.cube(body, (-11, 14, -6), (22, 20, 15), ROT_SKIN, deco=combine(spots((80, 110, 60), 0.05), band((200, 170, 70), 3)))   # the swollen body
    m.cube(body, (-11.5, 14, -6.5), (23, 6, 16), MUD_LINEN)
    head = m.bone("head", "body", (0, 34, 2))
    m.cube(head, (-6, 34, -5), (12, 11, 11), ROT_SKIN, deco=eyes(MARSH, 5, 2, 4))
    m.cube(head, (-7, 40, -6), (14, 8, 13), NEMES, deco=stripes((200, 170, 70), 2))
    m.cube(head, (-7, 34, -6), (3, 8, 10), NEMES, mirror_x=True)
    m.cube(head, (-1.5, 29, -5.6), (3, 6, 2), NEMES, deco=stripes((200, 170, 70), 2))                   # the false beard
    m.cube(head, (-7.2, 47.5, -6.2), (14.4, 2, 13.4), GOLD_M)                                          # the gold band of the crown
    for side, sx in (("left", 1), ("right", -1)):
        arm = m.bone(side + "_arm", "body", (11 * sx, 31, 2), rotation=(-30, 0, 0))
        m.cube(arm, ((11 if sx > 0 else -18), 17, -2), (7, 15, 7), ROT_SKIN)
        leg = m.bone(side + "_leg", "root", (5 * sx, 14, -2))
        m.cube(leg, ((1 if sx > 0 else -10), 12, -14), (9, 7, 13), ROT_SKIN)
        m.cube(leg, ((1.5 if sx > 0 else -9.5), 0, -15), (8, 12, 7), MUD_LINEN)
    anims = {
        "idle": anim(4.0, {"body": rotpos(swing([0, 0, 0], [3, 0, 0], 4.0), swing([0, 0, 0], [0, 0.8, 0], 4.0)),
                           "head": rot(*swing([0, -6, 0], [8, 6, 4], 4.0)),
                           "left_arm": rot(*swing([-30, 0, 0], [-26, 0, -4], 4.0)), "right_arm": rot(*swing([-30, 0, 0], [-26, 0, 4], 4.0))}),
        "walk": anim(4.0, {"body": rot(*swing([0, 0, 0], [3, 0, 0], 4.0))}),
        "attack": anim(1.2, {"right_arm": rot((0.0, [-30, 0, 0]), (0.5, [-120, 0, -10]), (0.8, [0, 0, 0]), (1.2, [-30, 0, 0])),
                             "body": rot((0.0, [0, 0, 0]), (0.5, [-6, 0, 0]), (0.8, [8, 0, 0]), (1.2, [0, 0, 0]))}, loop=False),
    }
    m.write({"morthis": None}, anims, (3.5, 4.5))


# ------------------------------------------------------------------------------------------------ Goldarc

GOLD_PLATE = mat((110, 74, 20), (196, 146, 50), (250, 214, 120), "metal", rivets=True, seam=4, noise=4)
RED_CAPE = mat((70, 10, 14), (140, 24, 30), (196, 60, 60), "cloth", ragged=3, noise=5)
COIN_GLOW = (255, 220, 110)


def coin_shield(p, f, m, rnd):
    x, y, w, h = f["east"]
    for px in range(w):
        for py in range(h):
            if (px - w / 2) ** 2 + (py - h / 2) ** 2 < (min(w, h) / 2) ** 2 and (px * 3 + py * 5) % 7 == 0:
                p.at(f["east"], px, py, COIN_GLOW, glow=True)


def goldarc():
    m = Model("goldarc", 128, scale=1.25)
    s = 1.3
    body, head = humanoid(m, GOLD_PLATE, GOLD_PLATE, GOLD_PLATE, GOLD_PLATE, scale=s, head_deco=eyes(COIN_GLOW, 4, 2, 1),
                          body_deco=stripes((150, 100, 30), 3))
    m.cube(head, (-4.5 * s, 32 * s, -4.5 * s), (9 * s, 3, 9 * s), GOLD_PLATE,
           deco=lambda p, f, mm, r: [p.at(f["north"], x, 0, COIN_GLOW, glow=True) for x in range(0, 12, 2)])      # a crown of points
    m.cube(body, (-4.5 * s, 4 * s, 2.5 * s), (9 * s, 20 * s, 1), RED_CAPE)
    m.cube(body, (-6 * s, 21 * s, -3 * s), (12 * s, 4 * s, 6 * s), GOLD_PLATE)                           # pauldrons
    shield = m.bone("shield", "left_arm", (8 * s, 14 * s, 0))
    m.cube(shield, (8.5 * s, 4 * s, -7 * s), (1.5, 16 * s, 14 * s), GOLD_PLATE, deco=coin_shield)
    sword = m.bone("sword", "right_arm", (-7 * s, 11 * s, 0), rotation=(75, 0, 0))
    m.cube(sword, (-8 * s, 10.5 * s, -3 * s), (2 * s, 2 * s, 5 * s), RED_CAPE)
    m.cube(sword, (-10 * s, 10 * s, -4 * s), (6 * s, 3 * s, 1.2), GOLD_PLATE)
    m.cube(sword, (-8.4 * s, 11 * s, -26 * s), (2.8 * s, 1.2, 22 * s), mat((150, 150, 160), (210, 210, 220), (250, 250, 255), "metal", noise=3))
    m.write({"goldarc": None}, boss_anims(1.6, 20, 10, heavy=True), (2.6, 4.5))


# ------------------------------------------------------------------------------------------------ Nixara

MERCHANT = mat((30, 22, 18), (70, 52, 40), (120, 96, 72), "cloth", ragged=3, noise=5)
HOLLOW = mat((4, 4, 6), (10, 8, 12), (20, 16, 24), "plain", noise=2)
POUCH = mat((90, 56, 30), (150, 100, 56), (200, 150, 96), "cloth", noise=5)


def nixara():
    m = Model("nixara", 128, scale=1.25)
    s = 1.2
    body, head = humanoid(m, HOLLOW, MERCHANT, MERCHANT, MERCHANT, scale=s, arm_len=15,
                          head_deco=lambda p, f, mm, r: [p.at(f["north"], x, 3, COIN_GLOW, glow=True) for x in (2, 5)])   # two coins for eyes
    m.cube(head, (-4.5 * s, 23 * s, -4.5 * s), (9 * s, 10 * s, 9 * s), MERCHANT, inflate=0.3, deco=clear_front_below(1))   # the deep hood
    for i, (x, y) in enumerate(((-4, 14), (2, 15), (-1, 12), (3, 18))):                                  # pouches hung everywhere
        m.cube(body, (x * s, y * s, -3 * s), (2.5 * s, 3 * s, 1.5 * s), POUCH)
    m.cube(body, (-5 * s, 0, -3 * s), (10 * s, 13 * s, 6 * s), MERCHANT)
    scales = m.bone("scales", "left_arm", (7 * s, 9 * s, -2 * s))
    m.cube(scales, (6.5 * s, 0, -2.5 * s), (1, 9 * s, 1), GOLD_M)
    m.cube(scales, (3 * s, 0, -4 * s), (8 * s, 1, 1), GOLD_M)
    m.cube(scales, (2 * s, -3 * s, -5 * s), (3 * s, 2, 3 * s), GOLD_M, mirror_x=False)
    m.cube(scales, (9 * s, -3 * s, -5 * s), (3 * s, 2, 3 * s), GOLD_M)
    anims = boss_anims(1.1, 22, 18)
    anims["idle"] = anim(2.0, {"body": rot(*swing([6, -6, 0], [6, 6, 0], 2.0)), "scales": rot(*swing([0, 0, -6], [0, 0, 6], 1.0))})
    m.write({"nixara": None}, anims, (2.2, 4))


# ------------------------------------------------------------------------------------------------ Avarok

AVAROK_GOLD = mat((120, 80, 20), (210, 160, 50), (255, 226, 130), "metal", rivets=True, seam=5, noise=4)
SACK = mat((70, 46, 24), (120, 84, 46), (170, 126, 80), "cloth", noise=6)
GEM_RED = glow((230, 40, 50))


def avarok():
    m = Model("avarok", 128, scale=1.3)
    s = 1.65
    face = mat((150, 100, 80), (210, 160, 130), (240, 200, 176), "plain", noise=4)
    body, head = humanoid(m, face, AVAROK_GOLD, AVAROK_GOLD, AVAROK_GOLD, scale=s, head_deco=eyes(COIN_GLOW, 3, 2, 2))
    beard = mat((180, 176, 170), (226, 224, 220), (250, 250, 248), "fur", noise=5)
    m.cube(head, (-4.2 * s, 22 * s, -4.4 * s), (8.4 * s, 5.5 * s, 3 * s), beard)                       # a white beard
    m.cube(body, (-7 * s, -1 * s, -6 * s), (14 * s, 3 * s, 6 * s), AVAROK_GOLD,
           deco=spots(COIN_GLOW, 0.12, glow=True, faces=("up", "north")))                             # a heap of coins at his feet
    m.cube(body, (-5.5 * s, 13 * s, -4 * s), (11 * s, 9 * s, 8 * s), AVAROK_GOLD, deco=stripes((150, 100, 30), 3))   # the great belly of gold
    m.cube(body, (-4.5 * s, 2 * s, 2.5 * s), (9 * s, 22 * s, 1), RED_CAPE)
    m.cube(head, (-4.5 * s, 32 * s, -4.5 * s), (9 * s, 4 * s, 9 * s), AVAROK_GOLD,
           deco=lambda p, f, mm, r: [p.at(f[face], x, 1, (230, 40, 50), glow=True) for face in ("north", "east", "west", "south") for x in (2, 7, 12)])
    sack = m.bone("sack", "body", (0, 20 * s, 4 * s))
    m.cube(sack, (-4 * s, 14 * s, 3 * s), (8 * s, 9 * s, 6 * s), SACK, deco=spots(COIN_GLOW, 0.06, glow=True, faces=("up",)))
    for side, sx in (("left", 1), ("right", -1)):
        m.cube(side + "_arm", ((4.2 if sx > 0 else -7.8) * s, 9 * s, -2.2 * s), (3.6 * s, 2, 4.4 * s), GEM_RED)   # rings on every hand
    m.write({"avarok": None}, boss_anims(1.8, 18, 10, heavy=True), (3.5, 6))


# ------------------------------------------------------------------------------------------------ Fenrath

DEVOURER_FUR = mat((40, 36, 32), (84, 76, 66), (140, 130, 116), "fur", noise=10)
MAW = mat((60, 10, 14), (120, 30, 36), (180, 70, 70), "plain", noise=5)
TOOTH = mat((150, 140, 110), (210, 204, 180), (246, 242, 226), "plain", noise=3)
ACID = (150, 255, 90)


def fenrath():
    m = Model("fenrath", 128, scale=1.35)
    m.bone("root")
    body = m.bone("body", "root", (0, 16, 0))
    m.cube(body, (-8, 12, -10), (16, 14, 22), DEVOURER_FUR, deco=spots(ACID, 0.01, glow=True))
    spikes(m, body, (0, 26, 0), along="z", count=7, length=6, material=TOOTH, size=1.8, spread=3)    # a mane of bone spines
    for sx in (1, -1):
        spikes(m, body, ((5 if sx > 0 else -5), 24, -4), along="z", count=3, length=4, material=TOOTH, size=1.4, spread=3)
    head = m.bone("head", "body", (0, 20, -10))
    m.cube(head, (-7, 16, -24), (14, 12, 14), DEVOURER_FUR, deco=eyes(ACID, 2, 2, 6))
    m.cube(head, (-6, 17, -25), (12, 2, 1), TOOTH)                                                     # upper teeth
    jaw = m.bone("jaw", "head", (0, 17, -12))
    m.cube(jaw, (-6.5, 11, -24), (13, 5, 13), MAW, deco=lambda p, f, mm, r: [p.at(f["up"], x, y, ACID, glow=True) for x in range(2, 11, 3) for y in range(2, 11, 4)])
    m.cube(jaw, (-6, 15.5, -24.5), (12, 2, 1), TOOTH)
    for i in range(5):                                                                                  # acid dripping from the jaw
        m.cube(jaw, (-5 + i * 2.4, 7 - (i % 2) * 2, -23), (1, 4 + (i % 2) * 2, 1), glow_mat(ACID))
    for name, x, z in (("front_left_leg", 3, -8), ("front_right_leg", -8, -8), ("back_left_leg", 3, 6), ("back_right_leg", -8, 6)):
        leg = m.bone(name, "root", (x + 2.5, 14, z + 2.5))
        m.cube(leg, (x, 0, z), (5, 14, 5), DEVOURER_FUR)
        m.cube(leg, (x - 0.5, 0, z - 1), (6, 2, 2), TOOTH)
    anims = {
        "idle": anim(2.0, {"jaw": rot(*swing([0, 0, 0], [16, 0, 0], 2.0)), "head": rot(*swing([0, -6, 0], [4, 6, 0], 2.0)),
                           "body": rotpos(swing([0, 0, 0], [0, 0, 0], 2.0), swing([0, 0, 0], [0, 0.6, 0], 1.0))}),
        "walk": anim(0.9, {"front_left_leg": rot(*swing([30, 0, 0], [-30, 0, 0], 0.9)), "back_right_leg": rot(*swing([30, 0, 0], [-30, 0, 0], 0.9)),
                           "front_right_leg": rot(*swing([-30, 0, 0], [30, 0, 0], 0.9)), "back_left_leg": rot(*swing([-30, 0, 0], [30, 0, 0], 0.9)),
                           "jaw": rot(*swing([0, 0, 0], [10, 0, 0], 0.45))}),
        "attack": anim(0.7, {"jaw": rot((0.0, [0, 0, 0]), (0.25, [45, 0, 0]), (0.4, [0, 0, 0]), (0.7, [0, 0, 0])),
                             "head": rot((0.0, [0, 0, 0]), (0.25, [-20, 0, 0]), (0.4, [15, 0, 0]), (0.7, [0, 0, 0]))}, loop=False),
    }
    m.write({"fenrath": None}, anims, (3.5, 3.5))


# ------------------------------------------------------------------------------------------------ Gularth

GLUTTON = mat((120, 70, 60), (180, 120, 104), (226, 176, 156), "plain", noise=7)
CHAIN = mat((40, 40, 44), (90, 90, 96), (150, 150, 160), "metal", seam=2, noise=4)
CLEAVER = mat((70, 70, 76), (140, 140, 150), (210, 210, 220), "metal", cracks=((220, 60, 40), 0.01), noise=4)
MOUTH_RED = (230, 60, 40)


def gularth():
    m = Model("gularth", 128, scale=1.3)
    m.bone("root")
    for side, sx in (("left", 1), ("right", -1)):
        leg = m.bone(side + "_leg", "root", (5 * sx, 12, 0))
        m.cube(leg, ((1 if sx > 0 else -9), 0, -4), (8, 12, 8), GLUTTON)
    body = m.bone("body", "root", (0, 12, 0))
    m.cube(body, (-13, 10, -10), (26, 22, 20), GLUTTON, deco=combine(band((90, 50, 40), 10), spots((150, 90, 80), 0.04)))   # the vast belly
    m.cube(body, (-13.5, 18, -10.5), (27, 2, 21), CHAIN)
    m.cube(body, (-13.5, 26, -10.5), (27, 2, 21), CHAIN)
    m.cube(body, (-8, 10, -10.6), (2, 22, 1), CHAIN)                                                    # chains across the belly
    m.cube(body, (6, 10, -10.6), (2, 22, 1), CHAIN)
    head = m.bone("head", "body", (0, 32, -2))
    m.cube(head, (-5, 32, -7), (10, 8, 9), GLUTTON,
           deco=centered(["..........", "..Y....Y..", "..........", ".WRRRRRRW.", ".RWRWRWRR.", ".RRRRRRRR.", ".WRWRWRWR.", ".........."],
                         {"Y": (255, 200, 60), "R": MOUTH_RED, "W": (240, 236, 220)}, top=0, glow_chars="YR"))   # a gaping maw of teeth
    for side, sx in (("left", 1), ("right", -1)):
        arm = m.bone(side + "_arm", "body", (14 * sx, 30, 0))
        m.cube(arm, ((13 if sx > 0 else -20), 12, -3.5), (7, 18, 7), GLUTTON)
        m.cube(arm, ((12.8 if sx > 0 else -20.2), 22, -3.7), (7.4, 2, 7.4), CHAIN)
    cleaver = m.bone("cleaver", "right_arm", (-17, 13, 0), rotation=(70, 0, 0))
    m.cube(cleaver, (-17.5, 12, -16), (2, 2, 14), mat((40, 30, 20), (80, 60, 40), (120, 96, 66), "cloth"))
    m.cube(cleaver, (-18, 6, -28), (1.5, 12, 14), CLEAVER)
    anims = boss_anims(2.0, 16, 8, heavy=True)
    anims["idle"] = anim(3.0, {"body": rotpos(swing([0, 0, 0], [0, 0, 2], 3.0), swing([0, 0, 0], [0, 1, 0], 1.5)),
                               "head": rot(*swing([0, -8, 0], [0, 8, 0], 3.0))})
    m.write({"gularth": None}, anims, (4, 5))


# ------------------------------------------------------------------------------------------------ Shadeyn

MIRROR = mat((150, 160, 180), (210, 220, 236), (250, 252, 255), "metal", seam=3, noise=2)
MIRROR_SHARD = glow((230, 240, 255))


def shadeyn():
    m = Model("shadeyn", 128, scale=1.3)
    s = 1.3
    body, head = humanoid(m, MIRROR, MIRROR, MIRROR, MIRROR, scale=s,
                          head_deco=lambda p, f, mm, r: [p.at(f["north"], x, y, (255, 255, 255), glow=True) for x in range(1, 9) for y in range(2, 7)],  # a face of mirror
                          body_deco=spots((255, 255, 255), 0.05, glow=True))
    m.cube(body, (-6 * s, 21 * s, -3 * s), (12 * s, 4 * s, 6 * s), MIRROR)
    for i, (x, y) in enumerate(((-3, 17), (1, 19), (-1, 14))):
        m.cube(body, (x * s, y * s, -2.6 * s), (2 * s, 3 * s, 0.5), MIRROR_SHARD)
    shield = m.bone("shield", "left_arm", (8 * s, 14 * s, 0))
    m.cube(shield, (8.5 * s, 5 * s, -5 * s), (1.5, 12 * s, 10 * s), MIRROR_SHARD)
    for sx in (1, -1):                                                                                  # crystal shards from the shoulders
        spikes(m, body, ((5 if sx > 0 else -5) * s, 25 * s, 0), along="z", count=3, length=5, material=MIRROR_SHARD, size=1.4, spread=2.5)
    spikes(m, head, (0, 32 * s, 0), along="x", count=3, length=4, material=MIRROR_SHARD, size=1.2, spread=2.5)
    blade = m.bone("blade", "right_arm", (-7 * s, 11 * s, 0), rotation=(80, 0, 0))
    m.cube(blade, (-7.5 * s, 10.5 * s, -16 * s), (1.2, 1.2, 16 * s), MIRROR)
    m.write({"shadeyn": None}, boss_anims(1.2, 26, 16), (2.6, 4.4))


# ------------------------------------------------------------------------------------------------ Envyris

SHADOW_ROBE = mat((6, 10, 8), (16, 28, 20), (36, 56, 42), "void", cracks=((90, 255, 140), 0.012), noise=5)
ENVY_GREEN = (90, 255, 140)
SCYTHE = mat((40, 60, 46), (90, 120, 100), (160, 200, 170), "metal", cracks=(ENVY_GREEN, 0.02), noise=4)


def envyris():
    m = Model("envyris", 128, scale=1.3)
    s = 1.45
    body, head = humanoid(m, SHADOW_ROBE, SHADOW_ROBE, SHADOW_ROBE, SHADOW_ROBE, scale=s,
                          head_deco=eyes(ENVY_GREEN, 4, 2, 2), body_deco=rift(ENVY_GREEN, 0.5, 2, 2))
    m.cube(head, (-4.5 * s, 31 * s, -4.5 * s), (9 * s, 3, 9 * s), SHADOW_ROBE,
           deco=lambda p, f, mm, r: [p.at(f[face], x, 0, ENVY_GREEN, glow=True) for face in ("north", "east", "west", "south") for x in range(0, 13, 3)])  # a crown of thorns
    m.cube(body, (-7 * s, 0, -4.5 * s), (14 * s, 22 * s, 9 * s), SHADOW_ROBE, inflate=0.3)              # the shadow cloak
    for sx in (1, -1):                                                                                  # thorned shoulders
        spikes(m, body, ((5.5 if sx > 0 else -5.5) * s, 23.5 * s, 0), along="z", count=3, length=7, material=SCYTHE, size=1.6, spread=3)
    spikes(m, head, (0, 32.5 * s, 0), along="x", count=5, length=6, material=glow_mat(ENVY_GREEN), size=1.2, spread=2.6)   # the crown of thorns
    scythe = m.bone("scythe", "right_arm", (-7 * s, 11 * s, 0), rotation=(70, 0, 0))
    m.cube(scythe, (-7.5 * s, 10.5 * s, -22 * s), (1.5, 1.5, 30 * s), mat((10, 14, 12), (30, 40, 34), (60, 76, 66), "cloth"))
    m.cube(scythe, (-8 * s, 4 * s, -24 * s), (1, 8 * s, 3), SCYTHE)
    m.cube(scythe, (-8 * s, -4 * s, -24 * s), (1, 3, 12 * s), SCYTHE)
    anims = boss_anims(1.3, 20, 14)
    anims["idle"] = anim(2.6, {"body": rotpos(swing([0, -5, 0], [0, 5, 0], 2.6), swing([0, 0, 0], [0, 0.8, 0], 1.3)),
                               "left_arm": rot(*swing([-30, 0, -12], [-44, 0, -20], 2.6))})
    m.write({"envyris": None}, anims, (3.5, 5))



# ------------------------------------------------------------------------------------------------ Act V: Solrath, Prython, Nahrazel

PROPHET_ROBE = mat((40, 10, 60), (86, 30, 120), (150, 80, 200), "cloth", ragged=2, noise=4)
SUN_GOLD = mat((140, 90, 20), (230, 170, 50), (255, 236, 150), "metal", noise=3)
SUN_GLOW = glow_mat((255, 210, 90))


def solrath():
    """The sheet's Solrath: the vizier become a false prophet, tall in purple robes trimmed with gold, a sun halo of
    rays behind his head, a face lost in shadow but for golden eyes, a staff crowned with a sun disk."""
    m = Model("solrath", 128, scale=1.35)
    shadow = mat((10, 6, 16), (24, 16, 34), (44, 32, 60), "plain", noise=2)
    body, head = humanoid(m, shadow, PROPHET_ROBE, PROPHET_ROBE, PROPHET_ROBE, scale=1.2,
                          head_deco=centered(["", "", "YY..YY", "", ""], {"Y": (255, 220, 100)}, top=1, glow_chars="Y"),
                          body_deco=combine(band((230, 180, 60), 2), band((230, 180, 60), 9)))
    m.cube(head, (-5.2, 27, -5.2), (10.4, 12, 10.4), PROPHET_ROBE, inflate=0.4, deco=clear_front_below(2))   # the hood
    m.cube(body, (-6, 0, -3.6), (12, 15, 7.2), PROPHET_ROBE, deco=band((230, 180, 60), 13))                    # the long robe
    m.cube(body, (-1, 6, -3.8), (2, 22, 0.6), SUN_GOLD)                                                         # a stole of gold
    halo(m, head, (0, 36, 5), 7, 1.2, SUN_GLOW, rays=12, ray_len=3)                                             # the sun halo
    staff = m.bone("staff", "right_arm", (-8.4, 14, 0))
    m.cube(staff, (-9.2, -4, -1), (1.6, 46, 1.6), SUN_GOLD)
    halo(m, staff, (-8.4, 44, 0), 3, 1, SUN_GLOW, rays=8, ray_len=2)
    m.cube(staff, (-9.4, 43, -1.2), (2, 2, 2), SUN_GLOW)
    anims = boss_anims(1.6, 18, 10)
    anims["idle"] = anim(3.0, {"body": rot(*swing([0, 0, 0], [3, 0, 0], 3.0)), "right_arm": rot(*swing([-20, 0, 6], [-26, 0, 8], 3.0)),
                               "left_arm": rot(*swing([-30, 0, -20], [-44, 0, -30], 3.0))})
    anims["attack"] = anim(0.9, {"right_arm": rot((0.0, [-20, 0, 6]), (0.4, [-170, 0, 6]), (0.6, [-40, 0, 6]), (0.9, [-20, 0, 6])),
                                 "left_arm": rot((0.0, [0, 0, 0]), (0.4, [-120, 0, -40]), (0.9, [0, 0, 0]))}, loop=False)
    m.write({"solrath": None}, anims, (5, 6))


PRIDE_PLATE = mat((14, 12, 10), (40, 34, 26), (90, 76, 50), "metal", rivets=True, seam=4, cracks=((255, 210, 90), 0.003), noise=4)
PRIDE_BLUE = (120, 190, 255)


def prython():
    """The sheet's Prython: a king of dark armour edged with gold and spikes, a great halo of golden spikes behind
    him, tattered dark wings with golden ribs and eyes of cold blue light."""
    m = Model("prython", 128, scale=1.5)
    s = 1.3
    body, head = humanoid(m, PRIDE_PLATE, PRIDE_PLATE, PRIDE_PLATE, PRIDE_PLATE, scale=s,
                          head_deco=centered(["", "", "BB....BB", ".B....B."], {"B": PRIDE_BLUE}, top=1, glow_chars="B"),
                          body_deco=combine(band((230, 180, 60), 3), veins((255, 210, 90), 0.004)))
    spikes(m, head, (0, 32 * s, 0), along="x", count=5, length=6, material=SUN_GOLD, size=1.4, spread=2.6)        # a crown of spikes
    for sx in (1, -1):
        spikes(m, body, ((5 if sx > 0 else -5) * s, 24 * s, 0), along="z", count=3, length=6, material=SUN_GOLD, size=1.5, spread=3)
    halo(m, head, (0, 33 * s, 6 * s), 11, 1.4, SUN_GOLD, rays=16, ray_len=4)                                     # the halo of Pride
    m.cube(body, (-5 * s, 2 * s, 2.6 * s), (10 * s, 22 * s, 1), mat((10, 8, 8), (30, 24, 20), (60, 50, 40), "cloth", ragged=4))   # a dark cloak
    membrane = mat((8, 6, 8), (26, 20, 24), (54, 44, 50), "cloth", ragged=3, noise=4)
    bat_wing(m, "body", 1, (2.5 * s, 12 * s, 3 * s), 22 * s, 14 * s, membrane, SUN_GOLD, claws=SUN_GOLD)
    bat_wing(m, "body", -1, (-2.5 * s, 12 * s, 3 * s), 22 * s, 14 * s, membrane, SUN_GOLD, claws=SUN_GOLD)
    sword = m.bone("sword", "right_arm", (-7 * s, 11 * s, 0), rotation=(75, 0, 0))
    m.cube(sword, (-8 * s, 10.5 * s, -3 * s), (2 * s, 2 * s, 5 * s), PRIDE_PLATE)
    m.cube(sword, (-10.5 * s, 10 * s, -4 * s), (7 * s, 3 * s, 1.2), SUN_GOLD)
    m.cube(sword, (-8.6 * s, 11 * s, -30 * s), (3.2 * s, 1.2, 26 * s), mat((40, 50, 70), (90, 120, 170), (180, 220, 255), "metal", cracks=(PRIDE_BLUE, 0.02)))
    anims = boss_anims(1.4, 20, 12, heavy=True)
    anims["idle"]["bones"]["left_wing"] = rot(*swing([0, 0, 0], [0, -10, -8], 3.0))
    anims["idle"]["bones"]["right_wing"] = rot(*swing([0, 0, 0], [0, 10, 8], 3.0))
    m.write({"prython": None}, anims, (8, 8))


NAH_HIDE = mat((10, 4, 18), (32, 14, 52), (70, 40, 110), "void", cracks=((196, 110, 255), 0.014), noise=6)
NAH_PLATE = mat((6, 2, 12), (24, 10, 40), (60, 34, 90), "metal", seam=4, cracks=((196, 110, 255), 0.006), noise=4)
NAH_HORN = mat((20, 10, 26), (60, 36, 70), (130, 100, 150), "plain", noise=4)
NAH_GLOW = glow_mat((200, 110, 255))


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
    """The sheet's Nahrazel, the First Fallen: a winged colossus of the Void, great horns, plated shoulders bristling
    with spikes, claws like scythes, a core of violet light in his chest. His first form is the same shape in ash and
    ember (nahrazel_ash)."""
    m = Model("nahrazel", 256)
    s = 2.0
    body, head = humanoid(m, NAH_HIDE, NAH_HIDE, NAH_HIDE, NAH_HIDE, scale=s, arm_len=14,
                          head_deco=centered(["", "", "PPP..PPP", ".PP..PP.", "", "WWWWWWWW", "W.W..W.W"],
                                             {"P": (230, 160, 255), "W": (230, 200, 255)}, top=2, glow_chars="PW"),
                          body_deco=veins((196, 110, 255), 0.006))
    m.cube(body, (-3 * s, 17 * s, -2.4 * s), (6 * s, 5 * s, 1), NAH_GLOW)                                        # the core
    for sx in (1, -1):
        m.cube(body, ((4 if sx > 0 else -10) * s, 21 * s, -3.4 * s), (6 * s, 5 * s, 6.8 * s), NAH_PLATE)          # shoulder plates
        spikes(m, body, ((7 if sx > 0 else -7) * s, 26 * s, 0), along="z", count=4, length=10, material=NAH_HORN, size=2.4, spread=4)
        horn(m, head, ((4.4 if sx > 0 else -4.4) * s, 30 * s, -1 * s), (0.6 * sx, 0.8, 0.2), 9, NAH_HORN, start=6, taper=0.84,
             curl=(18, 0, -14 * sx), step=4.5)
        for i in range(3):                                                                                       # claws like scythes
            m.cube(("left" if sx > 0 else "right") + "_arm", ((4.4 if sx > 0 else -7.6) * s + i * 2.2, -4 * s, -2 * s), (1.6, 6 * s, 1.6), NAH_HORN)
    spikes(m, body, (0, 24 * s, 2.6 * s), along="x", count=5, length=8, material=NAH_HORN, size=2, spread=4)       # spikes down the back
    membrane = mat((10, 4, 18), (40, 14, 70), (90, 50, 140), "cloth", ragged=4, cracks=((196, 110, 255), 0.01), noise=4)
    bat_wing(m, "body", 1, (3 * s, 14 * s, 3 * s), 30 * s, 20 * s, membrane, NAH_HORN, claws=NAH_HORN)
    bat_wing(m, "body", -1, (-3 * s, 14 * s, 3 * s), 30 * s, 20 * s, membrane, NAH_HORN, claws=NAH_HORN)
    anims = boss_anims(2.0, 18, 10, heavy=True)
    anims["idle"] = anim(3.4, {"body": rotpos(swing([0, 0, 0], [3, 0, 0], 3.4), swing([0, 0, 0], [0, 1.2, 0], 3.4)),
                               "left_wing": rot(*swing([0, 0, 0], [0, -14, -10], 3.4)), "right_wing": rot(*swing([0, 0, 0], [0, 14, 10], 3.4)),
                               "left_arm": rot(*swing([-6, 0, -10], [-12, 0, -16], 3.4)), "right_arm": rot(*swing([-6, 0, 10], [-12, 0, 16], 3.4))})
    anims["attack"] = anim(1.2, {"right_arm": rot((0.0, [0, 0, 10]), (0.5, [-170, 0, 20]), (0.75, [30, 0, 0]), (1.2, [0, 0, 10])),
                                 "left_arm": rot((0.0, [0, 0, -10]), (0.55, [-170, 0, -20]), (0.8, [30, 0, 0]), (1.2, [0, 0, -10])),
                                 "body": rot((0.0, [0, 0, 0]), (0.5, [-14, 0, 0]), (0.78, [20, 0, 0]), (1.2, [0, 0, 0]))}, loop=False)
    m.write({"nahrazel": None, "nahrazel_ash": ash_form}, anims, (12, 12))


def seal_glyph():
    """A Seal of the Codex: a page of the old seal hanging in the air, framed in gold, its runes lit violet, turning."""
    m = Model("seal_glyph", 64)
    m.bone("root")
    page = m.bone("page", "root", (0, 16, 0))
    paper = mat((190, 180, 160), (226, 218, 200), (250, 246, 236), "plain", noise=4)
    runes = ["P.PPP.P", ".P...P.", "PP.P.PP", "...P...", "PP.P.PP", ".P...P.", "P.PPP.P"]
    m.cube(page, (-5, 8, -0.5), (10, 14, 1), paper, deco=combine(
        centered(runes, {"P": (200, 110, 255)}, face="north", top=3, glow_chars="P"),
        centered(runes, {"P": (200, 110, 255)}, face="south", top=3, glow_chars="P")))
    m.cube(page, (-5.6, 7.4, -0.6), (11.2, 0.8, 1.2), SUN_GOLD)
    m.cube(page, (-5.6, 21.8, -0.6), (11.2, 0.8, 1.2), SUN_GOLD)
    m.cube(page, (-5.6, 7.4, -0.6), (0.8, 15.2, 1.2), SUN_GOLD)
    m.cube(page, (4.8, 7.4, -0.6), (0.8, 15.2, 1.2), SUN_GOLD)
    anims = {"idle": anim(4.0, {"page": rotpos([(0.0, [0, 0, 0]), (2.0, [0, 180, 0]), (4.0, [0, 360, 0])], swing([0, 0, 0], [0, 1.5, 0], 4.0))}),
             "walk": anim(4.0, {"page": rotpos([(0.0, [0, 0, 0]), (2.0, [0, 180, 0]), (4.0, [0, 360, 0])], swing([0, 0, 0], [0, 1.5, 0], 4.0))}),
             "attack": anim(0.5, {"page": rot((0.0, [0, 0, 0]), (0.25, [0, 0, 20]), (0.5, [0, 0, 0]))}, loop=False)}
    m.write({"seal_glyph": None}, anims, (1.4, 2))


if __name__ == "__main__":
    # the bosses themselves are drawn by make_boss_figures.py now; only the Seal of the Codex is left here
    seal_glyph()
