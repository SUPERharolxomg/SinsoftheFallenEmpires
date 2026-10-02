"""The library behind SoFE's 3D armor (GeckoLib), in the style of Armor of the Ages: parts to build a set from
(helmets, horns, hats, hoods, masks, crowns, pauldrons, breastplates, robes, capes, boots), the painter of
their textures, and a small renderer that draws each piece's inventory icon from the same model, so the
icon always shows what the player will wear.

Every set is one model with the eight GeckoLib armor bones; GeckoLib shows the bones of the slot each piece
is worn in. Cubes use box UV and are packed into one texture per set; every face is painted by material
(plate, trim, cloth, fur, leather, chain, bone, gem, dark, and numbered variants such as cloth2) with light
from above and a dark rim, then decals (visors, skulls, emblems, folds) go on top.

Coordinates are Bedrock units with the front at -Z and the right side at -X. The base boxes: head
[-4, 24, -4] 8x8x8, body [-4, 12, -2] 8x12x4, right arm [-8, 12, -2] 4x12x4, right leg [-3.9, 0, -2] 4x12x4.
"""
import json
import math
import os

from PIL import Image, ImageDraw

ASSETS = os.path.join("src", "main", "resources", "assets", "sofe")
GEO = os.path.join(ASSETS, "geo", "armor")
TEX = os.path.join(ASSETS, "textures", "models", "armor", "geo")
ITEM = os.path.join(ASSETS, "textures", "item")
TEX_W = 128
ICON = 32
OUTLINE = (20, 14, 18)

BONES = {"armorHead": [0, 24, 0], "armorBody": [0, 24, 0], "armorRightArm": [-5, 22, 0], "armorLeftArm": [5, 22, 0],
         "armorRightLeg": [-1.9, 12, 0], "armorLeftLeg": [1.9, 12, 0], "armorRightBoot": [-1.9, 12, 0], "armorLeftBoot": [1.9, 12, 0]}
PIECE_BONES = {"helmet": ("armorHead",), "chestplate": ("armorBody", "armorRightArm", "armorLeftArm"),
               "leggings": ("armorRightLeg", "armorLeftLeg"), "boots": ("armorRightBoot", "armorLeftBoot")}


# ----------------------------------------------------------------------------------------------- colors
def mix(a, b, t):
    return tuple(round(a[i] + (b[i] - a[i]) * t) for i in range(3))


def lighter(c, t):
    return mix(c, (255, 255, 255), t)


def darker(c, t):
    return mix(c, (0, 0, 0), t)


def ramp(base):
    """Five shades of one color, dark to light."""
    return [darker(base, 0.55), darker(base, 0.28), base, lighter(base, 0.28), lighter(base, 0.6)]


def family(mat):
    """cloth2 paints like cloth, trim2 like trim."""
    return mat.rstrip("0123456789")


# ----------------------------------------------------------------------------------------------- cubes
class Cube:
    def __init__(self, bone, origin, size, mat, inflate=0.0, rot=None, pivot=None, decal=None):
        self.bone, self.origin, self.size, self.mat = bone, list(origin), [max(1, int(round(s))) for s in size], mat
        self.inflate, self.rot, self.pivot, self.decal = inflate, rot, pivot, decal
        self.uv = None


def mirrored(c):
    """The same cube on the other side of the body (X flipped, right and left bones swapped)."""
    bone = c.bone.replace("Right", "#").replace("Left", "Right").replace("#", "Left")
    o = [-(c.origin[0] + c.size[0]), c.origin[1], c.origin[2]]
    rot = [c.rot[0], -c.rot[1], -c.rot[2]] if c.rot else None
    pivot = [-c.pivot[0], c.pivot[1], c.pivot[2]] if c.pivot else None
    return Cube(bone, o, c.size, c.mat, c.inflate, rot, pivot, c.decal)


def pair(cubes):
    out = []
    for c in cubes:
        out.append(c)
        out.append(mirrored(c))
    return out


# ----------------------------------------------------------------------------------------------- helmets and headwear
def helmet_shell(mat="plate", open_face=True, inflate=1.0):
    return [Cube("armorHead", (-4, 24, -4), (8, 8, 8), mat, inflate, decal="open_face" if open_face else "visor")]


def great_helm(mat="plate", trim="trim"):
    return [Cube("armorHead", (-4, 24, -4), (8, 8, 8), mat, 1.1, decal="visor"),
            Cube("armorHead", (-4.5, 32, -4.5), (9, 1, 9), trim, 0.1),
            Cube("armorHead", (-0.5, 24.5, -5.6), (1, 7, 1), trim, 0.05)]


def crest(mat="trim", height=2):
    return [Cube("armorHead", (-1, 33, -4.5), (2, height, 9), mat, 0.1)]


def plume(mat="cloth2"):
    """A horsehair crest running front to back, falling at the back."""
    return [Cube("armorHead", (-1, 33.5, -5), (2, 3, 9), mat, 0.1, decal="folds"),
            Cube("armorHead", (-1, 29, 4.5), (2, 5, 2), mat, 0.0, rot=[-20, 0, 0], pivot=[0, 33, 5])]


def brow(mat="trim", gem=None):
    cubes = [Cube("armorHead", (-4.5, 30, -5.4), (9, 2, 1), mat, 0.05)]
    if gem:
        cubes.append(Cube("armorHead", (-1, 30.2, -5.9), (2, 2, 1), gem, 0.1))
    return cubes


def cheek_guards(mat="plate"):
    return [Cube("armorHead", (-4.6, 24.5, -5.2), (2, 5, 1), mat, 0.0), Cube("armorHead", (2.6, 24.5, -5.2), (2, 5, 1), mat, 0.0)]


def nasal(mat="plate"):
    return [Cube("armorHead", (-0.5, 25.5, -5.4), (1, 5, 1), mat, 0.05)]


def sun_crown(mat="trim", gem="gem"):
    """A sunburst rising from the brow (the Solari helm)."""
    return [Cube("armorHead", (-1.5, 31, -6.0), (3, 3, 1), gem, 0.15),
            Cube("armorHead", (-0.5, 33.5, -6.2), (1, 3, 1), mat, 0.05),
            Cube("armorHead", (-3.5, 32.2, -5.9), (1, 2, 1), mat, 0.0, rot=[0, 0, 30], pivot=[-3, 32, -5.5]),
            Cube("armorHead", (2.5, 32.2, -5.9), (1, 2, 1), mat, 0.0, rot=[0, 0, -30], pivot=[3, 32, -5.5])]


def horns(mat="bone"):
    """Horns curling out and up from the temples."""
    return pair([Cube("armorHead", (-6.5, 29, -1.5), (2, 2, 3), mat, 0.1),
                 Cube("armorHead", (-10, 29.5, -1), (4, 2, 2), mat, 0.0, rot=[0, 0, -25], pivot=[-6, 30, 0]),
                 Cube("armorHead", (-11.5, 31.5, -1), (2, 4, 2), mat, 0.0, rot=[0, 0, -10], pivot=[-10, 31, 0]),
                 Cube("armorHead", (-11, 35, -0.5), (1, 3, 1), mat, 0.0, rot=[0, 0, 15], pivot=[-10.5, 35, 0])])


def demon_horns(mat="bone"):
    """Thick horns sweeping up and back from the brow."""
    return pair([Cube("armorHead", (-4.5, 31, -3), (3, 3, 3), mat, 0.1),
                 Cube("armorHead", (-5, 33, -2), (2, 4, 2), mat, 0.0, rot=[30, 0, -15], pivot=[-4, 33.5, -1]),
                 Cube("armorHead", (-5.5, 35.5, 0.5), (1, 4, 1), mat, 0.0, rot=[55, 0, -20], pivot=[-5, 36, 1])])


def ram_horns(mat="bone"):
    return pair([Cube("armorHead", (-6.5, 28, -1), (2, 3, 3), mat, 0.0),
                 Cube("armorHead", (-8, 26.5, -2.5), (2, 3, 2), mat, 0.0, rot=[0, 0, 20], pivot=[-7, 28, -1]),
                 Cube("armorHead", (-8, 24.5, -4), (2, 2, 2), mat, 0.0, rot=[-30, 0, 10], pivot=[-7, 26, -3])])


def wings(mat="trim"):
    """Small wings on the sides of the helm."""
    return pair([Cube("armorHead", (-6, 28, -1), (1, 4, 4), mat, 0.0, rot=[0, 0, -20], pivot=[-5, 29, 0]),
                 Cube("armorHead", (-7, 31, 0), (1, 3, 3), mat, 0.0, rot=[0, 0, -35], pivot=[-6, 31, 1])])


def jackal_headdress(mat="dark", trim="trim"):
    """A jackal head over the helmet: snout forward, tall ears."""
    cubes = [Cube("armorHead", (-2, 26.5, -7.5), (4, 3, 4), mat, 0.0, decal="snout"),
             Cube("armorHead", (-1.5, 25.5, -9), (3, 2, 2), mat, 0.0)]
    cubes += pair([Cube("armorHead", (-4, 33, -1), (2, 5, 1), mat, 0.0, rot=[0, 0, -8], pivot=[-3, 33, 0])])
    cubes += [Cube("armorHead", (-4.5, 29.5, -5.2), (9, 1, 1), trim, 0.05)]
    return cubes


def nemes(cloth="cloth", stripe="trim"):
    """The striped royal headcloth falling on the shoulders."""
    return [Cube("armorHead", (-4, 24, -4), (8, 8, 8), cloth, 1.15, decal="stripes_open"),
            Cube("armorHead", (-5.5, 19, -3.5), (2, 6, 2), cloth, 0.0, decal="stripes"),
            Cube("armorHead", (3.5, 19, -3.5), (2, 6, 2), cloth, 0.0, decal="stripes"),
            Cube("armorHead", (-4.6, 31.5, -5.3), (9, 1, 1), stripe, 0.05)]


def uraeus(mat="trim", gem="gem"):
    """The rearing cobra on the brow of a pharaoh."""
    return [Cube("armorHead", (-0.5, 31, -6.2), (1, 3, 1), mat, 0.0), Cube("armorHead", (-1, 33.5, -6.4), (2, 1, 1), gem, 0.0)]


def hood(mat="cloth"):
    return [Cube("armorHead", (-4, 24, -4), (8, 8, 8), mat, 1.1, decal="open_face"),
            Cube("armorHead", (-3, 30, 4.5), (6, 3, 2), mat, 0.0, rot=[25, 0, 0], pivot=[0, 31, 4.5])]


def hood_deep(mat="cloth", mask=None):
    """A deep cowl overhanging the face, a pointed tail at the back; a cloth mask over the mouth if asked."""
    cubes = [Cube("armorHead", (-4, 24, -4), (8, 8, 8), mat, 1.2, decal="open_face"),
             Cube("armorHead", (-4.5, 31, -5.8), (9, 2, 2), mat, 0.0),
             Cube("armorHead", (-2.5, 29.5, 4.5), (5, 4, 2), mat, 0.0, rot=[30, 0, 0], pivot=[0, 31, 4.5])]
    if mask:
        cubes.append(Cube("armorHead", (-4, 24.4, -5.4), (8, 3, 1), mask, 0.1))
    return cubes


def wizard_hat(mat="cloth", band="trim", gem=None, brim=12):
    """A tall pointed hat with a wide brim, its tip bent back."""
    b = brim
    cubes = [Cube("armorHead", (-b / 2, 31.5, -b / 2), (b, 1, b), mat, 0.0),
             Cube("armorHead", (-4, 32.5, -4), (8, 3, 8), mat, 0.2),
             Cube("armorHead", (-4, 32.5, -4), (8, 1, 8), band, 0.45),
             Cube("armorHead", (-3, 35.5, -3), (6, 3, 6), mat, 0.0, rot=[-6, 0, 0], pivot=[0, 35.5, 0]),
             Cube("armorHead", (-2, 38, -1.5), (4, 3, 4), mat, 0.0, rot=[-16, 0, 0], pivot=[0, 38, 0]),
             Cube("armorHead", (-1, 40, 0.5), (2, 3, 2), mat, 0.0, rot=[-40, 0, 0], pivot=[0, 40, 1])]
    if gem:
        cubes.append(Cube("armorHead", (-1, 32.6, -5.0), (2, 2, 1), gem, 0.0))
    return cubes


def witch_hat(mat="cloth", band="trim", gem=None):
    """A wide flat brim and a crooked cone."""
    cubes = [Cube("armorHead", (-7.5, 31.5, -7.5), (15, 1, 15), mat, 0.0),
             Cube("armorHead", (-4, 32.5, -4), (8, 2, 8), mat, 0.15),
             Cube("armorHead", (-4, 32.5, -4), (8, 1, 8), band, 0.35),
             Cube("armorHead", (-3, 34.5, -3), (6, 3, 6), mat, 0.0, rot=[-10, 0, 8], pivot=[0, 34.5, 0]),
             Cube("armorHead", (-1.5, 37, -1), (3, 3, 3), mat, 0.0, rot=[-25, 0, 20], pivot=[0, 37, 0]),
             Cube("armorHead", (0, 38.5, 1), (2, 2, 2), mat, 0.0, rot=[-50, 0, 40], pivot=[0.5, 39, 1])]
    if gem:
        cubes.append(Cube("armorHead", (-1, 32.5, -4.9), (2, 2, 1), gem, 0.0))
    return cubes


def skull_helm(mat="bone", horns_mat=None):
    """A skull over the head: eye sockets, nose, teeth; curled horns if asked."""
    cubes = [Cube("armorHead", (-4, 24, -4), (8, 8, 8), mat, 1.0, decal="skull_face"),
             Cube("armorHead", (-3.5, 22.8, -5), (7, 2, 3), mat, 0.0, decal="teeth")]
    if horns_mat:
        cubes += pair([Cube("armorHead", (-6.5, 29.5, -0.5), (2, 2, 2), horns_mat, 0.0),
                       Cube("armorHead", (-8, 30.5, 0.5), (2, 3, 2), horns_mat, 0.0, rot=[30, 0, -20], pivot=[-7, 31, 1]),
                       Cube("armorHead", (-8.5, 31.5, 2.5), (2, 2, 3), horns_mat, 0.0, rot=[60, 0, -10], pivot=[-7.5, 32, 3])])
    return cubes


def lich_crown(mat="bone", gem="gem"):
    """A ring of bone spikes over the brow, a soul gem at the front."""
    cubes = [Cube("armorHead", (-4.5, 31, -4.5), (9, 1, 9), mat, 0.1)]
    for (x, z, hgt) in ((-4, -4.8, 4), (-0.5, -5, 5), (3, -4.8, 4), (-4.9, -1, 3), (3.9, -1, 3), (-4.9, 2, 3), (3.9, 2, 3), (-0.5, 3.9, 4)):
        cubes.append(Cube("armorHead", (x, 32, z), (1, hgt, 1), mat, 0.0))
    cubes.append(Cube("armorHead", (-0.5, 31.2, -5.6), (1, 2, 1), gem, 0.15))
    return cubes


def plague_mask(mat="leather", hat="dark"):
    """The beaked mask with glass eyes and a wide-brimmed hat."""
    return [Cube("armorHead", (-4, 24, -4), (8, 8, 8), mat, 1.0, decal="lenses"),
            Cube("armorHead", (-1.5, 24.5, -9.5), (3, 3, 5), mat, 0.0, rot=[18, 0, 0], pivot=[0, 26, -5]),
            Cube("armorHead", (-6.5, 31.5, -6.5), (13, 1, 13), hat, 0.0),
            Cube("armorHead", (-4, 32.5, -4), (8, 3, 8), hat, 0.2)]


def gold_mask(mat="trim"):
    """A funeral mask over the face, with a braided beard."""
    return [Cube("armorHead", (-4, 24.2, -5.3), (8, 7, 1), mat, 0.0, decal="mask_face"),
            Cube("armorHead", (-0.5, 21.5, -5.2), (1, 3, 1), mat, 0.0)]


def mummy_wraps(mat="cloth"):
    return [Cube("armorHead", (-4, 24, -4), (8, 8, 8), mat, 1.05, decal="wraps")]


def wolf_head(fur="fur"):
    """A wolf head worn over the hood: snout forward over the brow, ears, the pelt down the back."""
    return [Cube("armorHead", (-4, 24, -4), (8, 8, 8), fur, 1.15, decal="open_face"),
            Cube("armorHead", (-3, 31, -6.5), (6, 3, 6), fur, 0.2, decal="eyes"),
            Cube("armorHead", (-2, 30.5, -10), (4, 2, 4), fur, 0.0),
            Cube("armorHead", (-3.5, 34, -1), (2, 2, 1), fur, 0.0), Cube("armorHead", (1.5, 34, -1), (2, 2, 1), fur, 0.0),
            Cube("armorBody", (-4.5, 12, 2.6), (9, 12, 1), fur, 0.2, rot=[4, 0, 0], pivot=[0, 24, 3])]


def lion_mane(fur="fur"):
    return [Cube("armorHead", (-4, 24, -4), (8, 8, 8), fur, 1.1, decal="open_face"),
            Cube("armorHead", (-6, 23, -3), (12, 11, 8), fur, 0.0, decal="mane"),
            Cube("armorHead", (-2.5, 32, -6), (5, 2, 3), fur, 0.0)]


def turban(cloth="cloth", band="trim", gem="gem", feather="cloth2"):
    return [Cube("armorHead", (-5, 29, -5), (10, 5, 10), cloth, 0.0, decal="wraps"),
            Cube("armorHead", (-4, 33.5, -4), (8, 2, 8), cloth, 0.0, decal="wraps"),
            Cube("armorHead", (-2, 29.5, -5.5), (4, 4, 1), band, 0.0),
            Cube("armorHead", (-1.5, 30.2, -5.9), (3, 3, 1), gem, 0.1),
            Cube("armorHead", (-0.5, 33, -5.6), (1, 6, 1), feather, 0.0, rot=[-15, 0, 0], pivot=[0, 33, -5])]


def crown(mat="trim", gem="gem", points=True):
    cubes = [Cube("armorHead", (-4.5, 31, -4.5), (9, 2, 9), mat, 0.15)]
    if points:
        for (x, z) in ((-4.6, -4.8), (-0.5, -4.9), (3.6, -4.8), (-4.6, 3.8), (3.6, 3.8), (-0.5, 3.9), (-4.9, -0.5), (3.9, -0.5)):
            cubes.append(Cube("armorHead", (x, 33, z), (1, 2, 1), mat, 0.0))
    cubes.append(Cube("armorHead", (-1, 31.3, -5.7), (2, 2, 1), gem, 0.1))
    return cubes


def circlet(mat="trim", gem="gem"):
    return [Cube("armorHead", (-4, 29.5, -4), (8, 1, 8), mat, 1.15), Cube("armorHead", (-0.5, 29.6, -5.6), (1, 1, 1), gem, 0.2)]


def veil(mat="cloth2"):
    return [Cube("armorHead", (-4, 24.2, -5.4), (8, 3, 1), mat, 0.0, decal="folds")]


def keffiyeh(cloth="cloth", rope="dark"):
    return [Cube("armorHead", (-4, 24, -4), (8, 8, 8), cloth, 1.15, decal="open_face"),
            Cube("armorHead", (-5, 17, -1), (10, 9, 5), cloth, 0.0, decal="folds"),
            Cube("armorHead", (-4.5, 30.5, -4.5), (9, 1, 9), rope, 0.35)]


def janissary_hat(felt="cloth", band="trim"):
    return [Cube("armorHead", (-4, 31, -4), (8, 2, 8), band, 0.25),
            Cube("armorHead", (-3, 32.5, -2), (6, 8, 4), felt, 0.0, rot=[-15, 0, 0], pivot=[0, 32.5, 0]),
            Cube("armorHead", (-3, 33, 3), (6, 10, 1), felt, 0.0, rot=[25, 0, 0], pivot=[0, 40, 2])]


def tricorn(mat="dark", trim="trim"):
    return [Cube("armorHead", (-6, 31.5, -6), (12, 1, 12), mat, 0.0),
            Cube("armorHead", (-4, 32.5, -4), (8, 3, 8), mat, 0.15),
            Cube("armorHead", (-6, 32, -6.2), (12, 3, 1), trim, 0.0, rot=[-20, 0, 0], pivot=[0, 32, -6]),
            Cube("armorHead", (-6.2, 32, -6), (1, 3, 12), mat, 0.0, rot=[0, 0, 20], pivot=[-6, 32, 0]),
            Cube("armorHead", (5.2, 32, -6), (1, 3, 12), mat, 0.0, rot=[0, 0, -20], pivot=[6, 32, 0])]


def goggles(strap="leather", frame="trim"):
    return [Cube("armorHead", (-4, 29, -4), (8, 2, 8), strap, 1.1),
            Cube("armorHead", (-3.5, 29.3, -5.8), (3, 3, 1), frame, 0.0, decal="lens"),
            Cube("armorHead", (0.5, 29.3, -5.8), (3, 3, 1), frame, 0.0, decal="lens")]


def halo(mat="trim"):
    return [Cube("armorHead", (-4, 35.5, -4), (8, 1, 1), mat, 0.0), Cube("armorHead", (-4, 35.5, 3), (8, 1, 1), mat, 0.0),
            Cube("armorHead", (-4, 35.5, -3), (1, 1, 6), mat, 0.0), Cube("armorHead", (3, 35.5, -3), (1, 1, 6), mat, 0.0)]


def dragon_helm(mat="plate", horn="bone", gem="gem"):
    """A dragon skull crest: snout over the brow, a ridge of scales, swept-back horns."""
    return [Cube("armorHead", (-4, 24, -4), (8, 8, 8), mat, 1.05, decal="open_face"),
            Cube("armorHead", (-2, 31, -8), (4, 2, 7), mat, 0.0, decal="eyes"),
            Cube("armorHead", (-1, 33, -6), (2, 3, 9), mat, 0.0),
            Cube("armorHead", (-0.5, 35.5, -3), (1, 2, 6), gem, 0.0)] + \
        pair([Cube("armorHead", (-5.5, 31, 1), (2, 2, 5), horn, 0.0, rot=[25, 0, 0], pivot=[-4.5, 32, 2]),
              Cube("armorHead", (-5.5, 33, 5), (1, 1, 4), horn, 0.0, rot=[45, 0, 0], pivot=[-5, 33.5, 5])])


def kabuto(mat="plate", trim="trim"):
    """A flared neck guard and a crescent crest."""
    return [Cube("armorHead", (-4, 24, -4), (8, 8, 8), mat, 1.0, decal="open_face"),
            Cube("armorHead", (-6, 26, -2), (12, 3, 8), mat, 0.0, rot=[15, 0, 0], pivot=[0, 29, 2]),
            Cube("armorHead", (-4, 32, -6), (3, 3, 1), trim, 0.0, rot=[0, 0, 25], pivot=[-2, 32, -6]),
            Cube("armorHead", (1, 32, -6), (3, 3, 1), trim, 0.0, rot=[0, 0, -25], pivot=[2, 32, -6])]


# ----------------------------------------------------------------------------------------------- body
def body_plate(mat="plate", inflate=1.05):
    return [Cube("armorBody", (-4, 12, -2), (8, 12, 4), mat, inflate)]


def breastplate(mat="plate", decal=None):
    return [Cube("armorBody", (-3.5, 15, -3.9), (7, 8, 1), mat, 0.15, decal=decal)]


def gorget(mat="trim"):
    return [Cube("armorBody", (-3.5, 22.8, -3.4), (7, 2, 7), mat, 0.0)]


def belt(mat="trim", buckle="gem"):
    return [Cube("armorBody", (-4, 11.8, -2.2), (8, 2, 4), mat, 1.25),
            Cube("armorBody", (-1, 12, -3.9), (2, 2, 1), buckle, 0.1)]


def tassets(mat="plate"):
    return pair([Cube("armorBody", (-4.3, 8, -3.3), (4, 4, 1), mat, 0.05, rot=[-6, 0, 0], pivot=[0, 12, -3])]) + \
        [Cube("armorBody", (-4, 8.5, 2.6), (8, 4, 1), mat, 0.05, rot=[6, 0, 0], pivot=[0, 12, 3])]


def robe_skirt(mat="cloth"):
    return [Cube("armorBody", (-4.5, 3, -2.8), (9, 9, 1), mat, 0.1, decal="folds"),
            Cube("armorBody", (-4.5, 3, 1.8), (9, 9, 1), mat, 0.1, decal="folds")]


def robe_full(mat="cloth", trim="trim"):
    """A long robe falling to the ankles all round the legs."""
    return [Cube("armorBody", (-4.5, 1, -2.8), (9, 11, 1), mat, 0.1, decal="folds"),
            Cube("armorBody", (-4.5, 1, 1.8), (9, 11, 1), mat, 0.1, decal="folds"),
            Cube("armorBody", (-4.8, 1, -2.5), (1, 11, 5), mat, 0.05), Cube("armorBody", (3.8, 1, -2.5), (1, 11, 5), mat, 0.05),
            Cube("armorBody", (-4.6, 1, -2.9), (9, 1, 1), trim, 0.15)]


def tabard(mat="cloth", decal=None):
    return [Cube("armorBody", (-2, 4, -3.4), (4, 9, 1), mat, 0.05, decal=decal)]


def surcoat(mat="cloth", decal=None):
    """A knight's tunic over the plate, falling between the legs."""
    return [Cube("armorBody", (-4, 12, -2), (8, 12, 4), mat, 1.15, decal=decal),
            Cube("armorBody", (-3.5, 5, -3.3), (7, 7, 1), mat, 0.0, decal="folds"),
            Cube("armorBody", (-3.5, 5, 2.3), (7, 7, 1), mat, 0.0, decal="folds")]


def cape(mat="cloth"):
    return [Cube("armorBody", (-4, 3, 2.6), (8, 21, 1), mat, 0.2, rot=[5, 0, 0], pivot=[0, 24, 3], decal="folds")]


def ermine_cape(mat="cloth", fur="fur"):
    return cape(mat) + [Cube("armorBody", (-5, 22, -3), (10, 2, 6), fur, 0.5)]


def fur_mantle(mat="fur"):
    return [Cube("armorBody", (-5, 21, -3), (10, 3, 6), mat, 0.4),
            Cube("armorBody", (-4.5, 13, 2.3), (9, 9, 1), mat, 0.2)]


def high_collar(mat="cloth", trim="trim"):
    return [Cube("armorBody", (-4.5, 22, 1.5), (9, 5, 1), mat, 0.2, rot=[-12, 0, 0], pivot=[0, 22, 2]),
            Cube("armorBody", (-4.5, 26.5, 1.4), (9, 1, 1), trim, 0.2, rot=[-12, 0, 0], pivot=[0, 22, 2])]


def ribcage(mat="bone"):
    return [Cube("armorBody", (-3.5, 14, -3.4), (7, 9, 1), mat, 0.05, decal="ribs")]


def straps(mat="leather"):
    return [Cube("armorBody", (-4, 12, -2), (8, 12, 4), mat, 1.1, decal="xstraps")]


def sash(mat="cloth2"):
    return [Cube("armorBody", (-4, 13, -2), (8, 3, 4), mat, 1.3),
            Cube("armorBody", (1, 8, -3.6), (2, 5, 1), mat, 0.0)]


def coat_tails(mat="cloth"):
    return [Cube("armorBody", (-4.5, 2, 1.8), (9, 10, 1), mat, 0.1, rot=[8, 0, 0], pivot=[0, 12, 2], decal="folds"),
            Cube("armorBody", (-4.5, 4, -3), (3, 8, 1), mat, 0.05), Cube("armorBody", (1.5, 4, -3), (3, 8, 1), mat, 0.05)]


def emblem(kind, mat="trim"):
    return [Cube("armorBody", (-2.5, 16, -4.1), (5, 5, 1), mat, 0.2, decal=kind)]


def back_emblem(kind, mat="trim"):
    return [Cube("armorBody", (-2.5, 16, 3.0), (5, 5, 1), mat, 0.2, decal=kind)]


# ----------------------------------------------------------------------------------------------- arms
def arm_plate(mat="plate", inflate=0.8):
    return pair([Cube("armorRightArm", (-8, 12, -2), (4, 12, 4), mat, inflate)])


def pauldron(mat="plate", trim="trim", layers=3, big=1.0):
    """Layered shoulder plates, wider than the arm."""
    w = round(6 * big)
    cubes = [Cube("armorRightArm", (-9 - (w - 6) / 2, 21, -3 - (w - 6) / 2), (w, 3, w), mat, 0.1)]
    if layers >= 2:
        cubes.append(Cube("armorRightArm", (-9.5 - (w - 6) / 2, 19.6, -3.4 - (w - 6) / 2), (w + 1, 1, w + 1), trim, 0.0))
    if layers >= 3:
        cubes.append(Cube("armorRightArm", (-8.5, 23.6, -2.5), (5, 1, 5), trim, 0.1))
    return pair(cubes)


def spiked_pauldron(mat="plate", spike="bone", trim="trim"):
    return pauldron(mat, trim, 2) + pair([Cube("armorRightArm", (-8, 24, -0.5), (1, 3, 1), spike, 0.0, rot=[0, 0, 20], pivot=[-7.5, 24, 0]),
                                          Cube("armorRightArm", (-8, 24, -2.5), (1, 2, 1), spike, 0.0, rot=[0, 0, 20], pivot=[-7.5, 24, -2])])


def bone_pauldron(mat="bone"):
    return pair([Cube("armorRightArm", (-9, 21, -3), (6, 3, 6), mat, 0.0, decal="skull_small"),
                 Cube("armorRightArm", (-9.5, 24, -0.5), (1, 3, 1), mat, 0.0, rot=[0, 0, 25], pivot=[-9, 24, 0]),
                 Cube("armorRightArm", (-7.5, 24, -2.5), (1, 2, 1), mat, 0.0, rot=[-20, 0, 15], pivot=[-7, 24, -2])])


def shoulder_orbs(mat="gem", ring="trim"):
    return pair([Cube("armorRightArm", (-8.5, 24.5, -1.5), (3, 3, 3), mat, 0.0),
                 Cube("armorRightArm", (-9, 23.5, -2), (4, 1, 4), ring, 0.0)])


def crystals(mat="gem"):
    return pair([Cube("armorRightArm", (-8, 23, -1), (2, 4, 2), mat, 0.0, rot=[0, 0, 20], pivot=[-7, 23, 0]),
                 Cube("armorRightArm", (-7, 23, 1), (1, 3, 1), mat, 0.0, rot=[15, 0, 10], pivot=[-6.5, 23, 1.5])])


def flames(mat="gem"):
    return pair([Cube("armorRightArm", (-8, 23.5, -1), (2, 3, 2), mat, 0.0, decal="flame"),
                 Cube("armorRightArm", (-7.5, 26, -0.5), (1, 2, 1), mat, 0.0)])


def vambrace(mat="plate", trim="trim"):
    return pair([Cube("armorRightArm", (-8.5, 12, -2.5), (5, 4, 5), mat, 0.1),
                 Cube("armorRightArm", (-8.6, 15.6, -2.6), (5, 1, 5), trim, 0.1)])


def sleeve(mat="cloth"):
    return pair([Cube("armorRightArm", (-8.5, 11, -2.5), (5, 5, 5), mat, 0.15, decal="folds")])


def wide_sleeve(mat="cloth", trim="trim"):
    """A mage sleeve flaring at the wrist."""
    return pair([Cube("armorRightArm", (-9, 10, -3), (6, 6, 6), mat, 0.0, decal="folds"),
                 Cube("armorRightArm", (-9.1, 10, -3.1), (6, 1, 6), trim, 0.05)])


# ----------------------------------------------------------------------------------------------- legs and feet
def leg_plate(mat="plate", inflate=0.55):
    return pair([Cube("armorRightLeg", (-3.9, 0, -2), (4, 12, 4), mat, inflate)])


def knee(mat="trim"):
    return pair([Cube("armorRightLeg", (-3.4, 5, -3.2), (3, 3, 1), mat, 0.1)])


def thigh_guard(mat="plate"):
    return pair([Cube("armorRightLeg", (-4.5, 8, -2.6), (1, 4, 5), mat, 0.0)])


def boots(mat="plate", trim="trim", toe=True):
    cubes = [Cube("armorRightBoot", (-3.9, 0, -2), (4, 5, 4), mat, 0.95),
             Cube("armorRightBoot", (-4.0, 4.3, -2.1), (4, 1, 4), trim, 1.0)]
    if toe:
        cubes.append(Cube("armorRightBoot", (-3.7, 0, -3.9), (4, 2, 2), mat, 0.2))
    return pair(cubes)


def fur_boots(mat="leather", fur="fur"):
    return pair([Cube("armorRightBoot", (-3.9, 0, -2), (4, 5, 4), mat, 0.9),
                 Cube("armorRightBoot", (-4.2, 4, -2.3), (5, 2, 5), fur, 0.4)])


def curled_shoes(mat="cloth", trim="trim"):
    return pair([Cube("armorRightBoot", (-3.9, 0, -2), (4, 3, 4), mat, 0.8),
                 Cube("armorRightBoot", (-2.9, 0.5, -4.5), (2, 1, 2), trim, 0.0, rot=[-25, 0, 0], pivot=[-2, 1, -4])])


def wraps(mat="cloth"):
    return pair([Cube("armorRightBoot", (-3.9, 0, -2), (4, 6, 4), mat, 0.7, decal="bands")])


# ----------------------------------------------------------------------------------------------- painting
def palette(spec):
    p = {k: ramp(v) for k, v in spec.items()}
    p.setdefault("dark", ramp((34, 28, 34)))
    p.setdefault("gem", ramp((230, 40, 40)))
    p.setdefault("bone", ramp((214, 200, 170)))
    p.setdefault("trim", ramp((226, 176, 56)))
    p.setdefault("plate", ramp((150, 154, 166)))
    return p


def colors(p, mat):
    return p.get(mat, p.get(family(mat), p["plate"]))


def shade(p, mat, x, y, w, h, face):
    r = colors(p, mat)
    kind = family(mat)
    t = y / max(1, h - 1)
    if kind == "gem":
        if (x + y) <= 1:
            return r[4]
        return r[3] if x + y < (w + h) / 2 else r[2]
    if kind == "cloth":
        c = r[3] if t < 0.15 else r[2] if t < 0.8 else r[1]
        if x % 3 == 2 and h > 2:
            c = r[1]
    elif kind == "fur":
        n = (x * 7 + y * 13 + (x * y) % 5) % 9
        c = r[4] if n < 2 else r[3] if n < 5 else r[2] if n < 8 else r[1]
    elif kind == "leather":
        c = r[3] if t < 0.2 else r[2] if t < 0.85 else r[1]
        if (y == 1 or y == h - 2) and x % 2 == 0 and h > 3:
            c = r[4]
    elif kind == "chain":
        c = r[3] if (x + y) % 2 == 0 else r[1]
    else:  # plate, trim, bone, dark: metal with a lit top and a dark foot
        c = r[3] if t < 0.2 else r[2] if t < 0.75 else r[1]
        thin = h <= 2 or w <= 2
        if (x == 0 or y == 0) and not thin:
            c = r[4] if kind != "dark" else r[3]
        elif thin:
            c = r[3] if y == 0 else r[2]
        if kind == "plate" and h >= 8 and w >= 4 and face not in ("up", "down"):  # overlapping lames, rivets
            if y % 4 == 3:
                c = r[1]
            elif y % 4 == 0 and y > 0:
                c = r[3]
            if y % 4 == 1 and x in (1, w - 2):
                c = r[4]
    if face == "up":
        c = lighter(c, 0.12)
    if face == "down":
        c = darker(c, 0.2)
    if w > 2 and h > 2 and (x == w - 1 or y == h - 1):
        c = r[0]
    return c


SHAPES = {"star": ["..x..", ".xxx.", "xxgxx", ".xxx.", "..x.."], "crescent": [".xx..", "x....", "x...g", "x....", ".xx.."],
          "flame": ["..x..", ".xg..", ".xgx.", "xgggx", ".xxx."], "scale": ["xxxxx", "..x..", "x.x.x", "xxxxx", ".x.x."],
          "rune": ["x...x", ".x.x.", "..g..", ".x.x.", "x...x"], "skull_emblem": [".xxx.", "xgxgx", "xxxxx", ".x.x.", "....."],
          "eye_emblem": [".....", ".xxx.", "xxgxx", ".xxx.", "....."], "hourglass": ["xxxxx", ".xgx.", "..x..", ".xgx.", "xxxxx"],
          "snowflake": ["x.x.x", ".xxx.", "xxgxx", ".xxx.", "x.x.x"], "wolf": ["x...x", "xx.xx", "xgxgx", ".xxx.", "..x.."],
          "lion": [".xxx.", "xgxgx", "xxxxx", "x.x.x", ".xxx."], "dragon": ["x...x", ".xxx.", "xgxgx", ".xxx.", "..x.."],
          "cog": [".x.x.", "xxxxx", ".xgx.", "xxxxx", ".x.x."], "peacock": ["x.x.x", ".xgx.", "xgggx", ".xgx.", "..x.."],
          "laurel": ["x...x", "x...x", ".x.x.", "..g..", "....."], "ankh": [".xxx.", ".x.x.", ".xxx.", "xxgxx", "..x.."],
          "sun": ["x.x.x", ".xxx.", "xxgxx", ".xxx.", "x.x.x"]}


def put(px, x, y, c):
    px[x, y] = tuple(c) + (255,)


def decal(img, p, kind, face, ox, oy, w, h, mat):
    px = img.load()
    dark, gem, trim = p["dark"], p["gem"], p["trim"]
    if kind in ("open_face", "stripes_open") and face == "north":
        for y in range(3, 8):   # the face stays visible
            for x in range(1, 7):
                px[ox + x, oy + y] = (0, 0, 0, 0)
    if kind == "visor" and face == "north":
        for x in range(1, 7):
            put(px, ox + x, oy + 3, dark[0])
        for x in (2, 5):
            put(px, ox + x, oy + 4, dark[0])
        for y in range(5, 7):
            for x in range(2, 6, 2):
                put(px, ox + x, oy + y, dark[1])
    if kind in ("stripes", "stripes_open"):
        r = colors(p, "trim2" if "trim2" in p else "trim")
        for y in range(h):
            if y % 2 == 1:
                for x in range(w):
                    if px[ox + x, oy + y][3]:
                        put(px, ox + x, oy + y, r[2])
    if kind == "skull_face" and face == "north":
        for (x, y) in ((1, 3), (2, 3), (5, 3), (6, 3), (1, 4), (2, 4), (5, 4), (6, 4), (3, 5), (4, 5), (2, 7), (4, 7), (6, 7)):
            put(px, ox + x, oy + y, dark[0])
        put(px, ox + 2, oy + 4, gem[3])
        put(px, ox + 5, oy + 4, gem[3])
    if kind == "teeth" and face == "north":
        for x in range(0, w, 2):
            put(px, ox + x, oy, dark[0])
    if kind == "skull_small" and face in ("north", "east", "west"):
        for (x, y) in ((1, 1), (3, 1), (2, 2)):
            if x < w and y < h:
                put(px, ox + x, oy + y, dark[0])
    if kind == "ribs" and face == "north":
        for y in range(1, h, 2):
            for x in range(w):
                if x != w // 2:
                    put(px, ox + x, oy + y, dark[1])
    if kind == "lenses" and face == "north":
        for (x, y) in ((1, 3), (2, 3), (5, 3), (6, 3), (1, 4), (2, 4), (5, 4), (6, 4)):
            put(px, ox + x, oy + y, gem[4] if y == 3 else gem[2])
    if kind == "lens" and face == "north":
        put(px, ox + 1, oy + 1, gem[4])
    if kind == "mask_face" and face == "north":
        for (x, y) in ((1, 2), (2, 2), (5, 2), (6, 2), (3, 4), (4, 4), (2, 5), (3, 5), (4, 5), (5, 5)):
            put(px, ox + x, oy + y, dark[0])
        put(px, ox + 3, oy, gem[3])
        put(px, ox + 4, oy, gem[3])
    if kind == "eyes" and face == "north":
        put(px, ox, oy + h // 2, gem[4])
        put(px, ox + w - 1, oy + h // 2, gem[4])
    if kind == "snout" and face == "north":
        put(px, ox + 1, oy, gem[3])
        put(px, ox + w - 2, oy, gem[3])
    if kind == "mane" and face == "north":
        for y in range(2, 10):
            for x in range(2, 10):
                px[ox + x, oy + y] = (0, 0, 0, 0)
    if kind == "wraps":
        r = colors(p, mat)
        for y in range(h):
            for x in range(w):
                if (x + y * 2) % 5 == 0 and px[ox + x, oy + y][3]:
                    put(px, ox + x, oy + y, r[1])
    if kind == "xstraps" and face in ("north", "south"):
        r = colors(p, "trim")
        for y in range(h):
            for x in range(w):
                if abs(x - y * w / h) < 1 or abs((w - 1 - x) - y * w / h) < 1:
                    put(px, ox + x, oy + y, r[2])
    if kind == "folds":
        r = colors(p, mat)
        for y in range(1, h):
            for x in range(w):
                if x % 3 == 1 and px[ox + x, oy + y][3]:
                    put(px, ox + x, oy + y, r[1])
    if kind == "bands":
        r = colors(p, mat)
        for y in range(0, h, 2):
            for x in range(w):
                if px[ox + x, oy + y][3]:
                    put(px, ox + x, oy + y, r[1])
    if kind in SHAPES and face == "north":
        rows = SHAPES[kind]
        oy0, ox0 = max(0, (h - len(rows)) // 2), max(0, (w - 5) // 2)
        for dy, row in enumerate(rows):
            for dx, ch in enumerate(row):
                if ch != "." and ox0 + dx < w and oy0 + dy < h:
                    put(px, ox + ox0 + dx, oy + oy0 + dy, gem[3] if ch == "g" else lighter(trim[3], 0.1))


def face_rects(c):
    """Box UV: [top][bottom] on the first row, [right][front][left][back] on the second."""
    w, h, d = c.size
    u, v = c.uv
    return {"up": (u + d, v, w, d), "down": (u + d + w, v, w, d),
            "east": (u, v + d, d, h), "north": (u + d, v + d, w, h), "west": (u + d + w, v + d, d, h), "south": (u + d + w + d, v + d, w, h)}


def pack(cubes):
    """Shelf packing of the box-UV regions into a texture TEX_W wide."""
    x = y = shelf = 0
    for c in sorted(cubes, key=lambda c: -(c.size[2] + c.size[1])):
        w, h, d = c.size
        rw, rh = 2 * (w + d), d + h
        if x + rw > TEX_W:
            x, y, shelf = 0, y + shelf, 0
        c.uv = [x, y]
        x += rw
        shelf = max(shelf, rh)
    return 1 << max(5, math.ceil(math.log2(max(1, y + shelf))))


def paint(cubes, spec):
    th = pack(cubes)
    p = palette(spec)
    img = Image.new("RGBA", (TEX_W, th), (0, 0, 0, 0))
    px = img.load()
    for c in cubes:
        for face, (ox, oy, w, h) in face_rects(c).items():
            for y in range(h):
                for x in range(w):
                    put(px, ox + x, oy + y, shade(p, c.mat, x, y, w, h, face))
            if c.decal:
                decal(img, p, c.decal, face, ox, oy, w, h, c.mat)
    return img


def geo_json(name, cubes, th):
    bones = []
    for bone, pivot in BONES.items():
        entry = {"name": bone, "pivot": pivot, "cubes": []}
        for c in cubes:
            if c.bone != bone:
                continue
            cube = {"origin": [round(v, 3) for v in c.origin], "size": c.size, "uv": c.uv}
            if c.inflate:
                cube["inflate"] = c.inflate
            if c.rot:
                cube["rotation"] = c.rot
                cube["pivot"] = c.pivot
            entry["cubes"].append(cube)
        bones.append(entry)
    return {"format_version": "1.12.0", "minecraft:geometry": [{
        "description": {"identifier": "geometry.sofe.armor." + name, "texture_width": TEX_W, "texture_height": th,
                        "visible_bounds_width": 4, "visible_bounds_height": 4, "visible_bounds_offset": [0, 1.5, 0]},
        "bones": bones}]}


# ----------------------------------------------------------------------------------------------- icons from the model
def rot_matrix(rx, ry, rz):
    rx, ry, rz = (math.radians(a) for a in (rx, ry, rz))
    cx, sx, cy, sy, cz, sz = math.cos(rx), math.sin(rx), math.cos(ry), math.sin(ry), math.cos(rz), math.sin(rz)
    X = [[1, 0, 0], [0, cx, -sx], [0, sx, cx]]
    Y = [[cy, 0, sy], [0, 1, 0], [-sy, 0, cy]]
    Z = [[cz, -sz, 0], [sz, cz, 0], [0, 0, 1]]

    def mul(a, b):
        return [[sum(a[i][k] * b[k][j] for k in range(3)) for j in range(3)] for i in range(3)]
    return mul(Z, mul(Y, X))


def apply(m, v):
    return [sum(m[i][k] * v[k] for k in range(3)) for i in range(3)]


def texel_quads(c, tex, shift=0.0):
    """Every texel of a cube as a 3D quad with its color, light by face."""
    (ox, oy, oz), (w, h, d) = c.origin, c.size
    inf = c.inflate
    x0, y0, z0, x1, y1, z1 = ox - inf, oy - inf, oz - inf, ox + w + inf, oy + h + inf, oz + d + inf
    rects = face_rects(c)
    faces = {"north": lambda s, t: (x1 - (x1 - x0) * s, y1 - (y1 - y0) * t, z0),
             "south": lambda s, t: (x0 + (x1 - x0) * s, y1 - (y1 - y0) * t, z1),
             "east": lambda s, t: (x1, y1 - (y1 - y0) * t, z1 - (z1 - z0) * s),
             "west": lambda s, t: (x0, y1 - (y1 - y0) * t, z0 + (z1 - z0) * s),
             "up": lambda s, t: (x1 - (x1 - x0) * s, y1, z1 - (z1 - z0) * t),
             "down": lambda s, t: (x1 - (x1 - x0) * s, y0, z0 + (z1 - z0) * t)}
    light = {"up": 1.0, "north": 0.94, "south": 0.8, "east": 0.84, "west": 0.84, "down": 0.6}
    normals = {"north": (0, 0, -1), "south": (0, 0, 1), "east": (1, 0, 0), "west": (-1, 0, 0), "up": (0, 1, 0), "down": (0, -1, 0)}
    m = rot_matrix(*c.rot) if c.rot else None
    piv = c.pivot or [0, 0, 0]
    out = []
    for face, f in faces.items():
        fu, fv, fw, fh = rects[face]
        for ty in range(fh):
            for tx in range(fw):
                col = tex.getpixel((fu + tx, fv + ty))
                if col[3] == 0:
                    continue
                pts = [f(tx / fw, ty / fh), f((tx + 1) / fw, ty / fh), f((tx + 1) / fw, (ty + 1) / fh), f(tx / fw, (ty + 1) / fh)]
                n = normals[face]
                if m:
                    pts = [[a + b for a, b in zip(apply(m, [q[0] - piv[0], q[1] - piv[1], q[2] - piv[2]]), piv)] for q in pts]
                    n = apply(m, list(n))
                if shift:
                    pts = [[q[0] + shift, q[1], q[2]] for q in pts]
                out.append((pts, tuple(int(ch * light[face]) for ch in col[:3]), n))
    return out


def project(quads, yaw, pitch):
    a, b = math.radians(yaw), math.radians(pitch)
    res = []
    for pts, col, n in quads:
        nx = n[0] * math.cos(a) + n[2] * math.sin(a)
        nz = -n[0] * math.sin(a) + n[2] * math.cos(a)
        if -n[1] * math.sin(b) + nz * math.cos(b) > 1e-6:
            continue   # a face turned away from the camera: inner faces would fill open helmets
        pp = []
        for (x, y, z) in pts:
            x2 = x * math.cos(a) + z * math.sin(a)
            z2 = -x * math.sin(a) + z * math.cos(a)
            y2 = y * math.cos(b) + z2 * math.sin(b)   # seen from a little above: the far top rises
            z3 = -y * math.sin(b) + z2 * math.cos(b)
            pp.append((x2, -y2, z3))
        res.append((pp, col))
    return res


def draw(quads, size, margin=1, max_scale=None):
    """Painter's algorithm into a size x size image, the model fitted inside the margin."""
    xs = [q[0] for pts, _ in quads for q in pts]
    ys = [q[1] for pts, _ in quads for q in pts]
    w, h = max(xs) - min(xs), max(ys) - min(ys)
    scale = (size - 2 * margin) / max(w, h, 1e-6)
    if max_scale:
        scale = min(scale, max_scale)
    cx, cy = (max(xs) + min(xs)) / 2, (max(ys) + min(ys)) / 2
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    dr = ImageDraw.Draw(img)
    for pts, col in sorted(quads, key=lambda t: -sum(q[2] for q in t[0]) / 4):
        poly = [(size / 2 + (q[0] - cx) * scale, size / 2 + (q[1] - cy) * scale) for q in pts]
        dr.polygon(poly, fill=col + (255,))
    return img


def outline(img):
    px = img.load()
    w, h = img.size
    edge = [(x, y) for y in range(h) for x in range(w) if px[x, y][3] == 0 and any(
        0 <= x + dx < w and 0 <= y + dy < h and px[x + dx, y + dy][3] > 0 for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))]
    for (x, y) in edge:
        px[x, y] = OUTLINE + (255,)
    return img


def icon(cubes, tex, piece):
    """The inventory icon of one piece: its bones, seen a little from above and from the right, outlined."""
    chosen = [c for c in cubes if c.bone in PIECE_BONES[piece]]
    # the legs a little apart, so leggings and boots read as a pair
    spread = {"armorRightLeg": -2.0, "armorLeftLeg": 2.0, "armorRightBoot": -2.0, "armorLeftBoot": 2.0}
    quads = [q for c in chosen for q in texel_quads(c, tex, spread.get(c.bone, 0.0))]
    proj = project(quads, 20, 12)
    big = draw(proj, ICON * 4, margin=6)
    small = big.resize((ICON, ICON), Image.NEAREST)
    # crisp pixel art: drop half-covered pixels, then outline
    px = small.load()
    for y in range(ICON):
        for x in range(ICON):
            if px[x, y][3] < 128:
                px[x, y] = (0, 0, 0, 0)
            else:
                px[x, y] = px[x, y][:3] + (255,)
    return outline(small)


def build(name, spec, parts, pieces=None, icon_names=None):
    """Writes the model and texture of a set, and the icons of its pieces (all four, or only the given ones)."""
    cubes = [c for part in parts for c in part]
    tex = paint(cubes, spec)
    os.makedirs(TEX, exist_ok=True)
    os.makedirs(GEO, exist_ok=True)
    tex.save(os.path.join(TEX, name + ".png"))
    with open(os.path.join(GEO, name + ".geo.json"), "w", encoding="utf-8") as f:
        json.dump(geo_json(name, cubes, tex.height), f, indent=1)
    for piece in pieces or PIECE_BONES:
        if not any(c.bone in PIECE_BONES[piece] for c in cubes):
            continue
        file = (icon_names or {}).get(piece, "%s_%s" % (name, piece))
        icon(cubes, tex, piece).save(os.path.join(ITEM, file + ".png"))
    return len(cubes)
