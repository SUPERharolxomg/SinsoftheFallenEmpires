"""The building blocks of Parsivan, Khemet and Aureum (docs/Mundo.md, W6), intact and corrupted: their
registration in SoFEBlocks (the generated-empire-blocks block), the corrupted-to-intact pairs used when a
region is liberated (EmpireBlocks), their English and Spanish names and their 16x16 textures. Models, loot
and tags come from the data generators (./gradlew runData) like every other block.

Run from the repository root: python scripts/make_empire_blocks.py
"""
import json
import math
import os
import random

from PIL import Image

ROOT = os.path.join("src", "main")
BLOCK_TEX = os.path.join(ROOT, "resources", "assets", "sofe", "textures", "block")
LANG = os.path.join(ROOT, "resources", "assets", "sofe", "lang")
BLOCKS_JAVA = os.path.join(ROOT, "java", "com", "sofe", "registry", "SoFEBlocks.java")
EMPIRE_JAVA = os.path.join(ROOT, "java", "com", "sofe", "world", "build", "EmpireBlocks.java")

# id, shape, vanilla block whose properties it copies, map color, English, Spanish, corrupted-of (intact id) or None
BLOCKS = [
    # --- Parsivan: turquoise tiles, white plaster, lapis mosaics; faded tiles and overgrown plaster
    ("parsivan_turquoise_tiles", "CUBE", "CYAN_TERRACOTTA", "COLOR_CYAN", "Parsivan Turquoise Tiles", "Azulejos Turquesa de Parsivan", None),
    ("parsivan_white_plaster", "CUBE", "SMOOTH_QUARTZ", "SNOW", "Parsivan White Plaster", "Yeso Blanco de Parsivan", None),
    ("parsivan_lapis_mosaic", "CUBE", "LAPIS_BLOCK", "LAPIS", "Parsivan Lapis Mosaic", "Mosaico de Lapislázuli de Parsivan", None),
    ("corrupted_parsivan_turquoise_tiles", "CUBE", "CYAN_TERRACOTTA", "COLOR_GRAY", "Faded Turquoise Tiles", "Azulejos Turquesa Desvaídos", "parsivan_turquoise_tiles"),
    ("corrupted_parsivan_white_plaster", "CUBE", "SMOOTH_QUARTZ", "COLOR_GREEN", "Overgrown Plaster", "Yeso Invadido de Enredaderas", "parsivan_white_plaster"),
    # --- Khemet: carved sandstone, painted limestone, gold hieroglyphs, obelisks; sunken sandstone and cracked paint
    ("khemet_carved_sandstone", "CUBE", "CUT_SANDSTONE", "SAND", "Khemet Carved Sandstone", "Arenisca Tallada de Khemet", None),
    ("khemet_painted_limestone", "CUBE", "CALCITE", "TERRACOTTA_WHITE", "Khemet Painted Limestone", "Caliza Pintada de Khemet", None),
    ("khemet_gold_hieroglyphs", "CUBE", "CUT_SANDSTONE", "GOLD", "Khemet Gold Hieroglyphs", "Jeroglíficos de Oro de Khemet", None),
    ("khemet_obelisk", "PILLAR", "CUT_SANDSTONE", "SAND", "Khemet Obelisk Stone", "Piedra de Obelisco de Khemet", None),
    ("corrupted_khemet_carved_sandstone", "CUBE", "CUT_SANDSTONE", "DIRT", "Sunken Sandstone", "Arenisca Hundida", "khemet_carved_sandstone"),
    ("corrupted_khemet_painted_limestone", "CUBE", "CALCITE", "COLOR_BROWN", "Cracked Limestone", "Caliza Agrietada", "khemet_painted_limestone"),
    # --- Aureum: Imperial Marble polished, bricks and pillars, gold mosaics; cracked marble with black-gold veins
    ("imperial_marble", "CUBE", "CALCITE", "QUARTZ", "Imperial Marble", "Mármol Imperial", None),     # raw, quarried in Aureum
    ("aureum_polished_marble", "CUBE", "POLISHED_DIORITE", "QUARTZ", "Polished Imperial Marble", "Mármol Imperial Pulido", None),
    ("aureum_marble_bricks", "CUBE", "STONE_BRICKS", "QUARTZ", "Imperial Marble Bricks", "Ladrillos de Mármol Imperial", None),
    ("aureum_marble_pillar", "PILLAR", "QUARTZ_PILLAR", "QUARTZ", "Imperial Marble Pillar", "Columna de Mármol Imperial", None),
    ("aureum_gold_mosaic", "CUBE", "GOLD_BLOCK", "GOLD", "Aureum Gold Mosaic", "Mosaico de Oro de Aureum", None),
    # the roofs of the concepts: Aureum's royal blue and Parsivan's violet domes and roofs
    ("aureum_royal_tiles", "CUBE", "BLUE_TERRACOTTA", "COLOR_BLUE", "Aureum Royal Tiles", "Tejas Reales de Aureum", None),
    ("parsivan_violet_tiles", "CUBE", "PURPLE_TERRACOTTA", "COLOR_PURPLE", "Parsivan Violet Tiles", "Azulejos Violeta de Parsivan", None),
    ("corrupted_aureum_marble_bricks", "CUBE", "STONE_BRICKS", "COLOR_BLACK", "Cracked Imperial Marble", "Mármol Imperial Agrietado", "aureum_marble_bricks"),
    ("corrupted_aureum_marble_pillar", "PILLAR", "QUARTZ_PILLAR", "COLOR_BLACK", "Cracked Marble Pillar", "Columna de Mármol Agrietada", "aureum_marble_pillar"),
]
# stairs, slab and wall for the main building block of each empire
FAMILIES = [
    ("parsivan_white_plaster", "parsivan_white_plaster", "Parsivan Plaster", "Yeso de Parsivan"),
    ("khemet_carved_sandstone", "khemet_sandstone", "Khemet Sandstone", "Arenisca de Khemet"),
    ("aureum_marble_bricks", "aureum_marble_brick", "Imperial Marble Brick", "Ladrillo de Mármol Imperial"),
    ("aureum_royal_tiles", "aureum_royal_tile", "Aureum Royal Tile", "Teja Real de Aureum"),
    ("parsivan_violet_tiles", "parsivan_violet_tile", "Parsivan Violet Tile", "Azulejo Violeta de Parsivan"),
]


# ------------------------------------------------------------------------------------------------ java
def const(id):
    return id.upper()


def java_blocks():
    out = ["    // <generated-empire-blocks> by scripts/make_empire_blocks.py: Parsivan, Khemet and Aureum, intact and corrupted"]
    for id, shape, copy, color, en, es, _ in BLOCKS:
        props = "BlockBehaviour.Properties.copy(Blocks.%s).mapColor(MapColor.%s)" % (copy, color)
        if shape == "PILLAR":
            out.append('    public static final RegistryObject<Block> %s = register("%s", Shape.PILLAR, null, true,' % (const(id), id))
            out.append("            () -> new net.minecraft.world.level.block.RotatedPillarBlock(%s));" % props)
        else:
            out.append('    public static final RegistryObject<Block> %s = cube("%s", () -> new Block(%s));' % (const(id), id, props))
    for base, stem, _, _ in FAMILIES:
        copy = next(b[2] for b in BLOCKS if b[0] == base)
        props = "BlockBehaviour.Properties.copy(Blocks.%s)" % copy
        B = const(base)
        out.append('    public static final RegistryObject<Block> %s_STAIRS = register("%s_stairs", Shape.STAIRS, %s, true,' % (const(stem), stem, B))
        out.append("            () -> new StairBlock(() -> %s.get().defaultBlockState(), %s));" % (B, props))
        out.append('    public static final RegistryObject<Block> %s_SLAB = register("%s_slab", Shape.SLAB, %s, true,' % (const(stem), stem, B))
        out.append("            () -> new SlabBlock(%s));" % props)
        out.append('    public static final RegistryObject<Block> %s_WALL = register("%s_wall", Shape.WALL, %s, true,' % (const(stem), stem, B))
        out.append("            () -> new WallBlock(%s.forceSolidOn()));" % props)
    out.append("    // </generated-empire-blocks>")
    return "\n".join(out)


def write_blocks_java():
    s = open(BLOCKS_JAVA, encoding="utf-8").read()
    start, end = "    // <generated-empire-blocks>", "    // </generated-empire-blocks>"
    block = java_blocks()
    if start in s:
        a = s.index(start)
        b = s.index(end) + len(end)
        s = s[:a] + block + s[b:]
    else:
        anchor = "    // --- Sealed Gates (docs/Mundo.md, W2)"
        assert anchor in s
        s = s.replace(anchor, block + "\n\n" + anchor, 1)
    open(BLOCKS_JAVA, "w", encoding="utf-8").write(s)


def write_empire_java():
    pairs = [(b[6], b[0]) for b in BLOCKS if b[6]]
    pairs += [("nordrath_runestone_bricks", "corrupted_nordrath_runestone_bricks"), ("nordrath_dark_timber", "corrupted_nordrath_dark_timber")]
    lines = "\n".join("        HEALED.put(SoFEBlocks.%s.get(), SoFEBlocks.%s.get());" % (const(c), const(i)) for i, c in pairs)
    src = """package com.sofe.world.build;

import com.sofe.registry.SoFEBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The corrupted building blocks of every empire and the intact block each becomes when its region is liberated
 * (docs/Mundo.md, W6: "the landscape heals"). Written by scripts/make_empire_blocks.py.
 */
public final class EmpireBlocks {
    private static final Map<Block, Block> HEALED = new HashMap<>();

    private EmpireBlocks() {
    }

    private static Map<Block, Block> healed() {
        if (HEALED.isEmpty()) {
%s
        }
        return HEALED;
    }

    /** The intact state of a corrupted block (its axis kept), or empty when the block is not corrupted. */
    public static Optional<BlockState> heal(BlockState state) {
        Block intact = healed().get(state.getBlock());
        if (intact == null) return Optional.empty();
        BlockState out = intact.defaultBlockState();
        for (Property<?> property : state.getProperties()) {
            if (out.hasProperty(property)) out = copy(state, out, property);
        }
        return Optional.of(out);
    }

    private static <T extends Comparable<T>> BlockState copy(BlockState from, BlockState to, Property<T> property) {
        return to.setValue(property, from.getValue(property));
    }
}
""" % lines
    open(EMPIRE_JAVA, "w", encoding="utf-8").write(src)


def write_lang():
    entries = {}
    for id, _, _, _, en, es, _ in BLOCKS:
        entries["block.sofe." + id] = (en, es)
    for _, stem, en, es in FAMILIES:
        entries["block.sofe.%s_stairs" % stem] = (en + " Stairs", "Escaleras de " + es)
        entries["block.sofe.%s_slab" % stem] = (en + " Slab", "Losa de " + es)
        entries["block.sofe.%s_wall" % stem] = (en + " Wall", "Muro de " + es)
    for f, i in (("en_us", 0), ("es_es", 1)):
        path = os.path.join(LANG, f + ".json")
        data = json.load(open(path, encoding="utf-8"))
        for k, v in entries.items():
            data[k] = v[i]
        open(path, "w", encoding="utf-8").write(json.dumps(data, ensure_ascii=False, indent=2) + "\n")


# ------------------------------------------------------------------------------------------------ art
def clamp(c):
    return tuple(max(0, min(255, int(round(v)))) for v in c[:3]) + (255,)


def mul(c, f):
    return clamp([v * f for v in c[:3]])


def mix(a, b, t):
    return clamp([a[i] + (b[i] - a[i]) * t for i in range(3)])


class Tex:
    def __init__(self, base, seed, spread=0.06):
        self.rnd = random.Random(seed)
        self.px = [[mul(base, 1 + self.rnd.uniform(-spread, spread)) for _ in range(16)] for _ in range(16)]

    def put(self, x, y, c):
        if 0 <= x < 16 and 0 <= y < 16:
            self.px[y][x] = clamp(c)

    def get(self, x, y):
        return self.px[y % 16][x % 16]

    def shade(self, x, y, f):
        self.put(x, y, mul(self.get(x, y), f))

    def save(self, name):
        img = Image.new("RGBA", (16, 16))
        for y in range(16):
            for x in range(16):
                img.putpixel((x, y), self.px[y][x])
        img.save(os.path.join(BLOCK_TEX, name + ".png"))


def veins(t, color, seed, count=2, length=14, wide=False):
    """Wandering lines: marble veins, cracks, black-aetherium seams."""
    rnd = random.Random(seed)
    for _ in range(count):
        x, y = rnd.uniform(0, 15), rnd.uniform(0, 15)
        a = rnd.uniform(0, math.pi * 2)
        for _ in range(length):
            t.put(int(x) % 16, int(y) % 16, color if not callable(color) else color(t.get(int(x) % 16, int(y) % 16)))
            if wide:
                t.put((int(x) + 1) % 16, int(y) % 16, mul(color, 0.8) if not callable(color) else color(t.get((int(x) + 1) % 16, int(y) % 16)))
            a += rnd.uniform(-0.7, 0.7)
            x += math.cos(a)
            y += math.sin(a)


def bricks(t, mortar, h=4, w=8, bevel=True):
    for y in range(16):
        row = y // h
        off = 0 if row % 2 == 0 else w // 2
        for x in range(16):
            if y % h == h - 1 or (x + off) % w == w - 1:
                t.put(x, y, mortar)
            elif bevel and (y % h == 0 or (x + off) % w == 0):
                t.shade(x, y, 1.1)
            elif bevel and (y % h == h - 2 or (x + off) % w == w - 2):
                t.shade(x, y, 0.92)


def grime(t, seed, amount=0.25, bottom=True):
    rnd = random.Random(seed)
    for y in range(16):
        for x in range(16):
            weight = (y / 15) if bottom else 0.5
            if rnd.random() < amount * weight:
                t.shade(x, y, rnd.uniform(0.7, 0.88))


TURQ = (52, 186, 182)
COBALT = (36, 70, 170)
WHITE = (238, 234, 224)
GOLD = (232, 182, 58)
SAND = (214, 182, 122)
LIME = (230, 220, 196)
MARBLE = (232, 230, 226)
VOID = (70, 20, 90)
BLACKGOLD = (40, 30, 24)


def parsivan_tiles(name, faded=False):
    t = Tex(TURQ if not faded else (120, 150, 146), 11 if not faded else 12, 0.05)
    grout = WHITE if not faded else (176, 170, 160)
    for y in range(16):
        for x in range(16):
            if x % 8 == 0 or y % 8 == 0:
                t.put(x, y, grout)
            elif x % 8 == 1 or y % 8 == 1:
                t.shade(x, y, 1.15)
            elif x % 8 == 7 or y % 8 == 7:
                t.shade(x, y, 0.85)
    for cx, cy in ((4, 4), (12, 4), (4, 12), (12, 12)):  # an eight-point star in each tile
        star = COBALT if not faded else (90, 96, 120)
        for dx, dy in ((0, 0), (1, 0), (-1, 0), (0, 1), (0, -1), (0, 2), (0, -2), (2, 0), (-2, 0)):
            t.put(cx + dx, cy + dy, star)
        for dx, dy in ((1, 1), (-1, -1), (1, -1), (-1, 1)):
            t.put(cx + dx, cy + dy, mix(star, grout, 0.5))
        t.put(cx, cy, WHITE if not faded else (190, 180, 170))
    if faded:
        veins(t, (60, 64, 60), 13, 2, 12)
        veins(t, VOID, 14, 1, 10)
        grime(t, 15, 0.3)
    t.save(name)


def plaster(name, overgrown=False):
    t = Tex(WHITE, 21 if not overgrown else 22, 0.035)
    rnd = random.Random(23)
    for _ in range(10):
        t.shade(rnd.randint(0, 15), rnd.randint(0, 15), 0.94)
    if overgrown:
        grime(t, 24, 0.4)
        leaf, dark = (70, 120, 50), (40, 80, 34)
        for start in (2, 9, 13):  # vines hanging from the top
            x = start
            for y in range(0, 11 + (start % 4)):
                t.put(x, y, dark)
                if y % 3 == 1:
                    t.put(x + 1, y, leaf)
                    t.put(x - 1, y + 1, leaf)
                x += rnd.choice((-1, 0, 0, 1))
                x = max(0, min(15, x))
        veins(t, VOID, 25, 1, 8)
    t.save(name)


def lapis_mosaic(name):
    t = Tex(COBALT, 31, 0.0)
    rnd = random.Random(32)
    for y in range(0, 16, 2):
        for x in range(0, 16, 2):  # tesserae of two pixels with a dark joint
            c = mul(COBALT, rnd.uniform(0.8, 1.25))
            for dx in range(2):
                for dy in range(2):
                    t.put(x + dx, y + dy, c if dx == 0 or dy == 0 else mul(c, 0.7))
    for i in range(16):  # a gold star of eight points through the middle
        for x, y in ((i, 8), (8, i), (i, i), (i, 15 - i)):
            if abs(x - 8) + abs(y - 8) <= 7 and (abs(x - 8) <= 5 and abs(y - 8) <= 5):
                t.put(x, y, GOLD if (x + y) % 2 else mix(GOLD, WHITE, 0.4))
    t.put(8, 8, WHITE)
    t.save(name)


def carved_sandstone(name, sunken=False):
    t = Tex(SAND if not sunken else (168, 138, 92), 41 if not sunken else 42, 0.05)
    mortar = mul(SAND, 0.72) if not sunken else (100, 80, 56)
    bricks(t, mortar, h=8, w=16)
    for y in (3, 11):  # a carved band across each block
        for x in range(1, 15):
            if x % 3 != 0:
                t.shade(x, y, 0.82)
            t.shade(x, y - 1, 1.08)
    if sunken:
        for y in range(10, 16):  # marsh mud up the lower part
            for x in range(16):
                if t.rnd.random() < (y - 9) / 7:
                    t.put(x, y, mul((70, 60, 40), t.rnd.uniform(0.85, 1.1)))
        veins(t, (60, 46, 30), 43, 2, 10)
        veins(t, VOID, 44, 1, 8)
    t.save(name)


def painted_limestone(name, cracked=False):
    t = Tex(LIME, 51 if not cracked else 52, 0.04)
    red, blue = (176, 50, 40), (40, 90, 170)
    f = 0.65 if cracked else 1
    for x in range(16):  # painted bands: red, gold, blue at the top and bottom
        for y, c in ((0, red), (1, GOLD), (2, blue), (13, blue), (14, GOLD), (15, red)):
            t.put(x, y, mix(c, LIME, 1 - f) if not (cracked and t.rnd.random() < 0.3) else LIME)
    for x in range(1, 16, 4):  # a row of lotus buds between the bands
        for dy, w in ((6, 0), (7, 1), (8, 1), (9, 0)):
            for dx in range(-w, w + 1):
                t.put(x + dx, dy, mix(blue if dy < 8 else (60, 140, 90), LIME, 1 - f))
        t.put(x, 10, mix((60, 140, 90), LIME, 1 - f))
    if cracked:
        veins(t, (110, 96, 76), 53, 3, 12)
        grime(t, 54, 0.3)
    t.save(name)


GLYPHS = [  # 5x5 hieroglyphs: an eye, an ankh, a bird, waves
    ["..x..", ".xxx.", "xx.xx", ".xxx.", "x...."],
    [".xxx.", ".x.x.", ".xxx.", "xxxxx", "..x.."],
    [".xx..", "xxx..", ".xxxx", "..xx.", "..x.x"],
    ["x.x.x", ".x.x.", ".....", "x.x.x", ".x.x."],
]


def gold_hieroglyphs(name):
    t = Tex(SAND, 61, 0.04)
    bricks(t, mul(SAND, 0.75), h=8, w=16, bevel=False)
    for i, (gx, gy) in enumerate(((1, 1), (9, 1), (1, 9), (9, 9))):
        for dy, row in enumerate(GLYPHS[i]):
            for dx, ch in enumerate(row):
                if ch == "x":
                    t.put(gx + dx, gy + dy, GOLD if (dx + dy) % 3 else mix(GOLD, WHITE, 0.45))
                    t.shade(gx + dx + 1, gy + dy + 1, 0.8)
    t.save(name)


def obelisk(name):
    t = Tex(SAND, 71, 0.04)
    for y in range(16):
        t.shade(0, y, 0.8)
        t.shade(15, y, 0.8)
        for x in (4, 11):
            t.shade(x, y, 0.85)
    for i, gy in enumerate((1, 9)):  # a column of glyphs down the middle
        for dy, row in enumerate(GLYPHS[(i + 1) % 4]):
            for dx, ch in enumerate(row):
                if ch == "x":
                    t.put(5 + dx + 1, gy + dy, mul(SAND, 0.62))
    t.save(name)
    e = Tex(mul(SAND, 1.05), 72, 0.04)
    for i in range(16):
        for x, y in ((i, 0), (i, 15), (0, i), (15, i)):
            e.shade(x, y, 0.78)
    for i in range(3, 13):
        for x, y in ((i, 3), (i, 12), (3, i), (12, i)):
            e.shade(x, y, 0.9)
    e.save(name + "_end")


def marble(name):
    t = Tex(MARBLE, 81, 0.025)
    veins(t, lambda c: mul(c, 0.84), 82, 3, 18)
    veins(t, lambda c: mul(c, 0.92), 83, 2, 12)
    for i in range(16):
        t.shade(i, 0, 1.03)
        t.shade(0, i, 1.03)
        t.shade(i, 15, 0.9)
        t.shade(15, i, 0.9)
    t.save(name)


def raw_marble(name):
    """Imperial Marble as it comes out of the quarry: rougher than the polished block, grey-veined, a fleck of gold."""
    t = Tex(mul(MARBLE, 0.96), 71, 0.05)
    veins(t, lambda c: mul(c, 0.78), 72, 3, 16)
    veins(t, lambda c: mul(c, 0.88), 73, 3, 10)
    rnd = random.Random(74)
    for _ in range(3):
        t.put(rnd.randrange(16), rnd.randrange(16), (226, 186, 90))
    grime(t, 75, 0.12, bottom=False)
    t.save(name)


def marble_bricks(name, cracked=False):
    t = Tex(MARBLE if not cracked else (196, 192, 186), 91 if not cracked else 92, 0.03)
    veins(t, lambda c: mul(c, 0.86), 93, 2, 14)
    bricks(t, (178, 176, 172) if not cracked else (120, 114, 104))
    if cracked:
        veins(t, BLACKGOLD, 94, 2, 12, wide=False)
        veins(t, (200, 150, 40), 95, 1, 8)
        grime(t, 96, 0.28)
        for x, y in ((3, 3), (12, 10)):  # chipped corners
            t.put(x, y, (90, 84, 76))
            t.put(x + 1, y, (120, 112, 100))
    t.save(name)


def marble_pillar(name, cracked=False):
    base = MARBLE if not cracked else (196, 192, 186)
    t = Tex(base, 101 if not cracked else 102, 0.025)
    for y in range(16):  # fluting: a light ridge and a dark groove, three times across
        for x in range(16):
            k = x % 5
            t.shade(x, y, (1.1, 1.0, 0.82, 0.9, 1.04)[k])
    for x in range(16):
        t.shade(x, 0, 0.85)
        t.shade(x, 15, 0.85)
    if cracked:
        veins(t, BLACKGOLD, 103, 2, 14)
        veins(t, (200, 150, 40), 104, 1, 8)
        grime(t, 105, 0.25)
    t.save(name)
    e = Tex(base, 106 if not cracked else 107, 0.025)
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            if 6.2 < d <= 7.4:
                e.shade(x, y, 0.82)
            elif d <= 2.4:
                e.shade(x, y, 0.92)
    if cracked:
        veins(e, BLACKGOLD, 108, 1, 10)
    e.save(name + "_end")


def gold_mosaic(name):
    t = Tex(GOLD, 111, 0.0)
    rnd = random.Random(112)
    for y in range(16):
        for x in range(16):
            border = x in (0, 15) or y in (0, 15)
            inner = x in (2, 13) or y in (2, 13)
            if border:
                t.put(x, y, (30, 50, 130) if (x + y) % 2 else (40, 70, 170))
            elif inner and 2 <= x <= 13 and 2 <= y <= 13:
                t.put(x, y, (176, 40, 40))
            else:
                c = mul(GOLD, rnd.uniform(0.82, 1.18))
                t.put(x, y, c if (x + y) % 2 else mul(c, 0.92))
    for dx, dy in ((0, 0), (1, 0), (-1, 0), (0, 1), (0, -1)):  # a laurel centre
        t.put(8 + dx, 8 + dy, (250, 236, 170))
    t.save(name)


def royal_tiles(name):
    """Roof tiles in royal blue: rows of rounded tiles, each lit on top and shadowed below, a gold glint here and there."""
    blue = (38, 64, 168)
    t = Tex(blue, 121, 0.04)
    for y in range(16):
        row = y // 4
        off = 0 if row % 2 == 0 else 2
        for x in range(16):
            k = (x + off) % 4
            f = (1.22, 1.08, 0.94, 0.72)[k] * (1.1 if y % 4 == 0 else 0.8 if y % 4 == 3 else 1.0)
            t.put(x, y, mul(blue, f))
    rnd = random.Random(122)
    for _ in range(3):
        t.put(rnd.randrange(16), rnd.randrange(0, 16, 4), mix(GOLD, WHITE, 0.2))
    t.save(name)


def violet_tiles(name):
    """Parsivan's violet glazed tiles: the turquoise tiles' grid in violet, an amethyst star in each, white grout."""
    violet = (118, 52, 160)
    t = Tex(violet, 131, 0.05)
    for y in range(16):
        for x in range(16):
            if x % 8 == 0 or y % 8 == 0:
                t.put(x, y, (226, 214, 236))
            elif x % 8 == 1 or y % 8 == 1:
                t.shade(x, y, 1.18)
            elif x % 8 == 7 or y % 8 == 7:
                t.shade(x, y, 0.82)
    for cx, cy in ((4, 4), (12, 4), (4, 12), (12, 12)):
        for dx, dy in ((0, 0), (1, 0), (-1, 0), (0, 1), (0, -1), (0, 2), (0, -2), (2, 0), (-2, 0)):
            t.put(cx + dx, cy + dy, TURQ)
        t.put(cx, cy, GOLD)
    t.save(name)


ART = {
    "parsivan_turquoise_tiles": lambda n: parsivan_tiles(n),
    "corrupted_parsivan_turquoise_tiles": lambda n: parsivan_tiles(n, faded=True),
    "parsivan_white_plaster": lambda n: plaster(n),
    "corrupted_parsivan_white_plaster": lambda n: plaster(n, overgrown=True),
    "parsivan_lapis_mosaic": lapis_mosaic,
    "khemet_carved_sandstone": lambda n: carved_sandstone(n),
    "corrupted_khemet_carved_sandstone": lambda n: carved_sandstone(n, sunken=True),
    "khemet_painted_limestone": lambda n: painted_limestone(n),
    "corrupted_khemet_painted_limestone": lambda n: painted_limestone(n, cracked=True),
    "khemet_gold_hieroglyphs": gold_hieroglyphs,
    "khemet_obelisk": obelisk,
    "imperial_marble": raw_marble,
    "aureum_polished_marble": marble,
    "aureum_marble_bricks": lambda n: marble_bricks(n),
    "corrupted_aureum_marble_bricks": lambda n: marble_bricks(n, cracked=True),
    "aureum_marble_pillar": lambda n: marble_pillar(n),
    "corrupted_aureum_marble_pillar": lambda n: marble_pillar(n, cracked=True),
    "aureum_gold_mosaic": gold_mosaic,
    "aureum_royal_tiles": royal_tiles,
    "parsivan_violet_tiles": violet_tiles,
}

if __name__ == "__main__":
    os.makedirs(BLOCK_TEX, exist_ok=True)
    for b in BLOCKS:
        ART[b[0]](b[0])
    write_blocks_java()
    write_empire_java()
    write_lang()
    print("%d empire blocks and %d stair/slab/wall families written" % (len(BLOCKS), len(FAMILIES)))
