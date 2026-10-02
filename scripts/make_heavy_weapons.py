"""The axes and hammers drawn at 32x32 from shapes rather than enlarged from 16x16: a haft on the diagonal and a
head built in the haft's own frame (along the haft and across it), so blades get real crescents and beards
and hammer heads real bevels. Each part is drawn as a polygon at four times the size, reduced to pixels,
then lit from the upper left, shaded on the lower right, given a bright cutting edge and outlined.

Run from the repository root after the other sprite scripts: python scripts/make_heavy_weapons.py
"""
import math
import os

from PIL import Image, ImageDraw

ITEM = os.path.join("src", "main", "resources", "assets", "sofe", "textures", "item")
N, K = 32, 4  # final size, oversampling
OUTLINE = (20, 14, 18)


def pal(base):
    def mix(a, b, t):
        return tuple(round(a[i] + (b[i] - a[i]) * t) for i in range(3))
    return {"d": mix(base, (0, 0, 0), 0.45), "m": base, "l": mix(base, (255, 255, 255), 0.3), "h": mix(base, (255, 255, 255), 0.65)}


STEEL, DARK_STEEL, ICE = pal((150, 156, 170)), pal((80, 84, 98)), pal((110, 190, 236))
EMBER, OBSIDIAN, GOLD = pal((200, 70, 30)), pal((44, 32, 58)), pal((226, 172, 50))
WOOD, DARK_WOOD, LEATHER = pal((116, 74, 40)), pal((66, 44, 30)), pal((120, 66, 38))
RED, MARBLE, STORM, VOID, BONE = pal((170, 30, 34)), pal((214, 214, 220)), pal((80, 120, 200)), pal((100, 44, 160)), pal((214, 202, 176))
TURQ, RUBY, AMBER, PURPLE, YELLOW = (60, 220, 200), (230, 40, 60), (255, 200, 60), (200, 120, 255), (255, 236, 110)

# the haft runs from the lower left to the upper right
START, END = (3.5, 28.5), (24, 8)


class Sprite:
    def __init__(self):
        self.layers = []  # (mask, palette, edge mask or None)
        dx, dy = END[0] - START[0], END[1] - START[1]
        n = math.hypot(dx, dy)
        self.d = (dx / n, dy / n)          # along the haft, towards the head
        self.n = (self.d[1], -self.d[0])   # across it, to the upper left

    def at(self, u, v, origin=END):
        """A point u along the haft and v across it, from the head end."""
        return (origin[0] + u * self.d[0] + v * self.n[0], origin[1] + u * self.d[1] + v * self.n[1])

    def mask(self, polys):
        img = Image.new("L", (N * K, N * K), 0)
        dr = ImageDraw.Draw(img)
        for poly in polys:
            dr.polygon([(x * K, y * K) for (x, y) in poly], fill=255)
        small = img.resize((N, N), Image.BOX)
        return {(x, y) for y in range(N) for x in range(N) if small.getpixel((x, y)) >= 110}

    def add(self, polys, palette, edge=None):
        self.layers.append((self.mask(polys), palette, self.mask([edge]) if edge else set()))

    def haft(self, palette, wraps=None, length=27, width=1.1, at=-1):
        a, b = self.at(at - length, 0), self.at(at, 0)
        poly = [self.at(at - length, width), self.at(at, width), self.at(at, -width), self.at(at - length, -width)]
        self.add([poly], palette)
        if wraps:
            for k in range(2):
                u0 = at - length + 3 + k * 4
                self.add([[self.at(u0, width + 0.3), self.at(u0 + 2, width + 0.3), self.at(u0 + 2, -width - 0.3), self.at(u0, -width - 0.3)]], wraps)
        self.add([[self.at(at - length - 1.5, 1.6), self.at(at - length + 1, 1.6), self.at(at - length + 1, -1.6), self.at(at - length - 1.5, -1.6)]],
                 wraps or palette)

    def gem(self, u, v, color, r=1.2):
        pts = [self.at(u + r, v), self.at(u, v + r), self.at(u - r, v), self.at(u, v - r)]
        self.add([pts], {"d": tuple(int(c * 0.6) for c in color), "m": color, "l": color, "h": tuple(min(255, c + 90) for c in color)})

    def save(self, name):
        out = {}
        for mask, p, edge in self.layers:
            for (x, y) in mask:
                ul = (x - 1, y) not in mask or (x, y - 1) not in mask
                dr = (x + 1, y) not in mask or (x, y + 1) not in mask
                c = p["m"]
                if ul and not dr:
                    c = p["l"]
                elif dr and not ul:
                    c = p["d"]
                if (x, y) in edge:
                    c = p["h"]
                out[(x, y)] = c
        img = Image.new("RGBA", (N, N), (0, 0, 0, 0))
        for (x, y), c in out.items():
            img.putpixel((x, y), c + (255,))
        for y in range(N):
            for x in range(N):
                if (x, y) not in out and any((x + dx, y + dy) in out for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                    img.putpixel((x, y), OUTLINE + (255,))
        img.save(os.path.join(ITEM, name + ".png"))


def blade(s, side=1, reach=11.0, top=6.0, beard=8.0, socket=2.5, at=-2):
    """One axe blade: a crescent edge from the top horn down to the beard, on one side of the haft."""
    v = lambda x: x * side
    body = [s.at(at + socket, v(0)), s.at(at + socket + 1.5, v(3)), s.at(at + top, v(reach - 1)), s.at(at + top - 1, v(reach + 1)),
            s.at(at + top - 4, v(reach + 1.8)), s.at(at - 2, v(reach + 1.5)), s.at(at - beard + 1, v(reach)), s.at(at - beard, v(reach - 2)),
            s.at(at - socket - 2, v(3)), s.at(at - socket, v(0))]
    edge = [s.at(at + top, v(reach - 1)), s.at(at + top - 1, v(reach + 1)), s.at(at + top - 4, v(reach + 1.8)), s.at(at - 2, v(reach + 1.5)),
            s.at(at - beard + 1, v(reach)), s.at(at - beard, v(reach - 2)), s.at(at - beard + 1.5, v(reach - 2.2)), s.at(at - 2, v(reach - 0.6)),
            s.at(at + top - 4, v(reach - 0.4)), s.at(at + top - 1.5, v(reach - 1.4))]
    return body, edge


def axe(name, metal, haft, wraps=None, double=False, gem=None, beard=8.0, reach=10.0, spike=False):
    s = Sprite()
    s.haft(haft, wraps)
    sides = (1, -1) if double else (1,)
    for side in sides:
        body, edge = blade(s, side, reach=reach, beard=beard if not double else 5.0, top=6.0 if not double else 5.0)
        s.add([body], metal, edge)
    s.add([[s.at(0.5, 2.6), s.at(0.5, -2.6), s.at(-5, -2.6), s.at(-5, 2.6)]], metal)   # the socket round the haft
    if spike:  # a back spike opposite the blade
        s.add([[s.at(-0.5, -2.4), s.at(-2.2, -8.5), s.at(-4, -2.4)]], metal)
    if gem:
        s.gem(-2.2, 0, gem)
    s.save(name)


def hammer(name, metal, haft, wraps=None, band=None, gem=None, spike=None, long=7.0, half=6.0):
    s = Sprite()
    s.haft(haft, wraps)
    at = -2.5
    # the head: a block across the haft with bevelled corners
    b = 1.2
    head = [s.at(at + long / 2 - b, half), s.at(at + long / 2, half - b), s.at(at + long / 2, -half + b), s.at(at + long / 2 - b, -half),
            s.at(at - long / 2 + b, -half), s.at(at - long / 2, -half + b), s.at(at - long / 2, half - b), s.at(at - long / 2 + b, half)]
    face = [s.at(at + long / 2, half - b), s.at(at - long / 2, half - b), s.at(at - long / 2, half - b - 1), s.at(at + long / 2, half - b - 1)]
    s.add([head], metal, face)
    if band:
        s.add([[s.at(at + 1, half + 0.3), s.at(at - 1, half + 0.3), s.at(at - 1, -half - 0.3), s.at(at + 1, -half - 0.3)]], band)
    if spike:
        s.add([[s.at(at + 1.4, -half), s.at(at, -half - 5), s.at(at - 1.4, -half)]], spike)
    s.add([[s.at(at + long / 2 + 3, 0.9), s.at(at + long / 2, 1.2), s.at(at + long / 2, -1.2), s.at(at + long / 2 + 3, -0.9)]], metal)  # top spike
    if gem:
        s.gem(at, 0, gem, 1.6)
    s.save(name)


if __name__ == "__main__":
    # great axes: one wide crescent and a back spike (two round blades read as a bow tie at this size)
    axe("glacial_iron_greataxe", ICE, DARK_WOOD, STEEL, gem=(200, 250, 255), beard=10.5, reach=12.0, spike=True)
    axe("bearded_axe", STEEL, WOOD, LEATHER, beard=9.5, reach=10.0)
    axe("skaldbreaker", STEEL, DARK_WOOD, RED, gem=RUBY, beard=11.0, reach=12.5, spike=True)
    axe("infernal_greataxe", EMBER, OBSIDIAN, EMBER, gem=AMBER, beard=11.0, reach=13.0, spike=True)
    hammer("war_hammer", STEEL, DARK_WOOD, LEATHER, spike=STEEL, long=8, half=6.5)
    hammer("city_hammer", pal((190, 136, 52)), WOOD, LEATHER, band=GOLD, gem=TURQ, long=8, half=6.5)
    hammer("gravity_hammer", DARK_STEEL, OBSIDIAN, VOID, band=VOID, gem=PURPLE, long=9, half=7.5)
    hammer("justicar_maul", MARBLE, DARK_WOOD, GOLD, band=GOLD, gem=RUBY, long=9, half=8.0)
    hammer("stormcaller", STORM, DARK_WOOD, STORM, band=GOLD, gem=YELLOW, long=9, half=7.5)
    print("axes and hammers drawn")
