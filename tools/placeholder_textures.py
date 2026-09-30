"""
Generates 16x16 placeholder textures for every SoFE material form.

    python tools/placeholder_textures.py

They are simple shapes in each material's color so the game never shows the purple-black
"missing texture". Replace a file with real art at any time; re-running this script only
creates files that do not exist yet (use --force to overwrite).
"""
import os
import random
import struct
import sys
import zlib

ROOT = os.path.join(os.path.dirname(__file__), '..', 'src', 'main', 'resources', 'assets', 'sofe', 'textures')

# Keep in sync with com.sofe.registry.material.Material
MATERIALS = {
    'sulthari_brass': ((205, 160, 60), ['ore', 'raw', 'raw_block', 'ingot', 'nugget', 'block']),
    'glacial_iron': ((160, 205, 230), ['ore', 'deepslate_ore', 'raw', 'raw_block', 'ingot', 'nugget', 'block']),
    'star_lapis': ((70, 80, 200), ['ore', 'gem', 'powder', 'block']),
    'solar_gold': ((250, 205, 45), ['ore', 'deepslate_ore', 'raw', 'raw_block', 'ingot', 'nugget', 'block', 'powder']),
    'orichalcum': ((220, 120, 70), ['deepslate_ore', 'raw', 'raw_block', 'ingot', 'nugget', 'block']),
    'aetherium': ((150, 240, 255), ['deepslate_ore', 'shard', 'block']),
    'black_aetherium': ((70, 30, 95), ['shard']),
}

ID_PATTERN = {
    'ore': '{}_ore', 'deepslate_ore': 'deepslate_{}_ore', 'raw_block': 'raw_{}_block', 'block': '{}_block',
    'raw': 'raw_{}', 'ingot': '{}_ingot', 'nugget': '{}_nugget', 'gem': '{}', 'powder': '{}_powder', 'shard': '{}_shard',
}
BLOCK_FORMS = {'ore', 'deepslate_ore', 'raw_block', 'block'}

# 16x16 masks for item shapes ('#' = material color, '+' = highlight, '.' = transparent)
SHAPES = {
    'ingot': [
        '................', '................', '................', '................',
        '................', '.....++++++.....', '....+#######....', '...+#########...',
        '..+#########....', '..###########...', '..##########....', '...#######......',
        '................', '................', '................', '................'],
    'nugget': [
        '................', '................', '................', '................',
        '................', '................', '......++........', '.....+###.......',
        '.....####.#.....', '......##.+#.....', '.........##.....', '................',
        '................', '................', '................', '................'],
    'raw': [
        '................', '................', '................', '.....++.........',
        '....+###+.......', '...+######......', '...########+....', '..###########...',
        '..##########....', '...#########....', '....#######.....', '......###.......',
        '................', '................', '................', '................'],
    'gem': [
        '................', '................', '................', '......++++......',
        '.....+####+.....', '....+######+....', '...+########+...', '...##########...',
        '....########....', '.....######.....', '......####......', '.......##.......',
        '................', '................', '................', '................'],
    'powder': [
        '................', '................', '................', '................',
        '................', '................', '................', '.......+........',
        '......###.......', '.....#####......', '....#######.+...', '...#########....',
        '..###########...', '................', '................', '................'],
    'shard': [
        '................', '................', '.........+......', '........+#......',
        '.......+##......', '......+###......', '.....+####......', '.....#####......',
        '....#####.......', '....####........', '...####.........', '...###..........',
        '..##............', '................', '................', '................'],
}


def png(pixels, path):
    """Writes a 16x16 RGBA PNG. pixels: list of 16 rows of 16 (r, g, b, a)."""
    raw = b''.join(b'\x00' + b''.join(struct.pack('BBBB', *p) for p in row) for row in pixels)

    def chunk(kind, data):
        return struct.pack('>I', len(data)) + kind + data + struct.pack('>I', zlib.crc32(kind + data) & 0xffffffff)

    data = b'\x89PNG\r\n\x1a\n' + chunk(b'IHDR', struct.pack('>IIBBBBB', 16, 16, 8, 6, 0, 0, 0)) \
        + chunk(b'IDAT', zlib.compress(raw, 9)) + chunk(b'IEND', b'')
    with open(path, 'wb') as f:
        f.write(data)


def shade(color, factor):
    return tuple(max(0, min(255, int(c * factor))) for c in color)


def block(color, form, rng):
    if form in ('ore', 'deepslate_ore'):
        base = (125, 125, 125) if form == 'ore' else (75, 75, 82)
        rows = [[(*shade(base, rng.uniform(0.85, 1.1)), 255) for _ in range(16)] for _ in range(16)]
        for _ in range(7):  # ore specks
            x, y = rng.randrange(1, 14), rng.randrange(1, 14)
            for dx, dy in ((0, 0), (1, 0), (0, 1), (1, 1)):
                rows[y + dy][x + dx] = (*shade(color, rng.uniform(0.8, 1.15)), 255)
        return rows
    lumpy = form == 'raw_block'
    rows = []
    for y in range(16):
        row = []
        for x in range(16):
            factor = 0.7 if x in (0, 15) or y in (0, 15) else rng.uniform(0.8, 1.1) if lumpy else 1.0 - (x + y) / 80
            row.append((*shade(color, factor), 255))
        rows.append(row)
    return rows


def item(color, form):
    rows = []
    for line in SHAPES[form]:
        row = []
        for ch in line:
            if ch == '#':
                row.append((*color, 255))
            elif ch == '+':
                row.append((*shade(color, 1.35), 255))
            else:
                row.append((0, 0, 0, 0))
        rows.append(row)
    return rows


def main():
    force = '--force' in sys.argv
    written = 0
    for material, (color, forms) in MATERIALS.items():
        rng = random.Random(material)  # same output every run
        for form in forms:
            is_block = form in BLOCK_FORMS
            folder = os.path.join(ROOT, 'block' if is_block else 'item')
            os.makedirs(folder, exist_ok=True)
            path = os.path.join(folder, ID_PATTERN[form].format(material) + '.png')
            if os.path.exists(path) and not force:
                continue
            png(block(color, form, rng) if is_block else item(color, form), path)
            written += 1
    print(f'wrote {written} textures')


if __name__ == '__main__':
    main()
