"""
Cuts art/concepts/skill_icons_sheet.png (2026-09-30) into the skill and HUD icons of docs/Arte.md:

- art/extracted/skill/<id>.png and art/extracted/hud/<id>.png   native size
- textures/gui/skill/<id>.png (32x32, opaque) and textures/gui/hud/<id>.png (16x16, transparent)

    pip install pillow numpy
    python tools/cut_skill_icons.py
"""
import os
import shutil
from collections import deque

import numpy as np
from PIL import Image

REPO = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..')
SHEET = os.path.join(REPO, 'art', 'concepts', 'skill_icons_sheet.png')
DROPPED = os.path.join(REPO, 'e01daa42-01ce-4c1f-84a2-52de970c8aec.png')
EXTRACTED = os.path.join(REPO, 'art', 'extracted')
TEX = os.path.join(REPO, 'src', 'main', 'resources', 'assets', 'sofe', 'textures', 'gui')

if os.path.exists(DROPPED):
    os.makedirs(os.path.dirname(SHEET), exist_ok=True)
    shutil.move(DROPPED, SHEET)
sheet = Image.open(SHEET).convert('RGBA')

# Rows as laid out on the sheet, in the order of the tables in docs/Arte.md
ROWS = [
    ['scale_strike', 'shield_wall', 'banner_cry', 'legionary_charge', 'verdict', 'protectors_oath',
     'iron_discipline', 'living_rampart', 'contained_wrath', 'last_one_standing'],
    ['threshold_touch', 'clay_warden', 'burial_wraps', 'scales_of_anubet', 'scarab_plague', 'canopic_jars',
     'rite_of_passage', 'boat_of_the_dead', 'heavy_heart', 'the_great_judgment'],
    ['burning_calligraphy', 'water_mirror', 'petal_tempest', 'sky_map', 'starfall', 'arcane_poetry', 'written_eclipse'],
    ['double_edge', 'light_fingers', 'smoke_step', 'cutthroat', 'fjord_snare', 'throwing_axe', 'deep_pockets',
     'thousand_cuts', 'scavengers_instinct', 'the_great_heist'],
    ['scepter_slash', 'decree_of_steadfastness', 'janissary_guard', 'siege_decree', 'command', 'royal_treasury',
     'imperial_lineage', 'bronze_cannon', 'voice_of_the_throne', 'crown_of_the_five_lands'],
]
# The third HUD tile has no file name under it and repeats the soul idea, so it is skipped (None)
HUD = ['resolve', 'essence', None, 'mana', 'energy', 'authority', 'stance_shield', 'stance_charge', 'soul', 'mark']


def boxes(img, threshold=230, min_height=40):
    """Bounding boxes of the icon tiles (the sheet's background is only partly transparent)."""
    solid = np.array(img)[::2, ::2, 3] > threshold
    H, W = solid.shape
    seen = np.zeros_like(solid)
    found = []
    for y in range(H):
        for x in range(W):
            if solid[y, x] and not seen[y, x]:
                q = deque([(y, x)]); seen[y, x] = True
                x0 = x1 = x; y0 = y1 = y; n = 0
                while q:
                    cy, cx = q.popleft(); n += 1
                    x0, x1, y0, y1 = min(x0, cx), max(x1, cx), min(y0, cy), max(y1, cy)
                    for dy, dx in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                        ny, nx = cy + dy, cx + dx
                        if 0 <= ny < H and 0 <= nx < W and solid[ny, nx] and not seen[ny, nx]:
                            seen[ny, nx] = True; q.append((ny, nx))
                if n > 200 and (y1 - y0 + 1) * 2 > min_height:
                    found.append((x0 * 2, y0 * 2, x1 * 2 + 2, y1 * 2 + 2))
    found.sort(key=lambda b: (b[1] // 100, b[0]))
    return found


def opaque(img):
    """Skill icons are full squares: flatten the partial alpha over black."""
    base = Image.new('RGBA', img.size, (0, 0, 0, 255))
    base.alpha_composite(img)
    return base


def square_crop(img):
    w, h = img.size
    s = min(w, h)
    return img.crop(((w - s) // 2, (h - s) // 2, (w - s) // 2 + s, (h - s) // 2 + s))


def remove_tile(img):
    """Removes the light checker tile painted behind each HUD icon, flooding in from the edges."""
    a = np.array(img)
    rgb = a[..., :3].astype(int)
    light = ((rgb.max(2) - rgb.min(2)) <= 18) & (rgb.min(2) >= 185)
    H, W = light.shape
    gone = np.zeros_like(light)
    q = deque((y, x) for y in range(H) for x in (0, W - 1) if light[y, x])
    q.extend((y, x) for x in range(W) for y in (0, H - 1) if light[y, x])
    for y, x in q:
        gone[y, x] = True
    while q:
        y, x = q.popleft()
        for dy, dx in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            ny, nx = y + dy, x + dx
            if 0 <= ny < H and 0 <= nx < W and light[ny, nx] and not gone[ny, nx]:
                gone[ny, nx] = True; q.append((ny, nx))
    a[gone, 3] = 0
    a[~gone, 3] = 255
    a[..., 3] = keep_main_shape(a[..., 3] > 0) * 255
    # The tile's bottom edge can stay glued to an icon (the shield's tip): clear gray pixels in the last rows
    ys = np.where(a[..., 3].any(1))[0]
    bottom = ys.max() - max(3, (ys.max() - ys.min()) // 12)
    gray = ((rgb.max(2) - rgb.min(2)) <= 28) & (rgb.min(2) >= 50)
    a[bottom:, :, 3][gray[bottom:]] = 0
    out = Image.fromarray(a)
    return out.crop(out.getbbox())


def keep_main_shape(mask):
    """Keeps the icon and drops leftover slivers of the tile edge: every piece smaller than
    15% of the largest one is removed."""
    H, W = mask.shape
    label = np.zeros(mask.shape, dtype=int)
    sizes = [0]
    for y in range(H):
        for x in range(W):
            if mask[y, x] and not label[y, x]:
                n = len(sizes); sizes.append(0)
                q = deque([(y, x)]); label[y, x] = n
                while q:
                    cy, cx = q.popleft(); sizes[n] += 1
                    for dy in (-1, 0, 1):
                        for dx in (-1, 0, 1):
                            ny, nx = cy + dy, cx + dx
                            if 0 <= ny < H and 0 <= nx < W and mask[ny, nx] and not label[ny, nx]:
                                label[ny, nx] = n; q.append((ny, nx))
    biggest = max(sizes)
    keep = [i for i, s in enumerate(sizes) if i and s >= 0.15 * biggest]
    return np.isin(label, keep)


def pad_square(img):
    w, h = img.size
    s = max(w, h)
    canvas = Image.new('RGBA', (s, s), (0, 0, 0, 0))
    canvas.alpha_composite(img, ((s - w) // 2, (s - h) // 2))
    return canvas


def save(img, *parts):
    path = os.path.join(*parts)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)


found = boxes(sheet)
expected = sum(len(r) for r in ROWS) + len(HUD)
if len(found) != expected:
    raise SystemExit(f'expected {expected} icons on the sheet, found {len(found)}')

names = [n for row in ROWS for n in row]
for name, box in zip(names, found[:len(names)]):
    icon = opaque(sheet.crop(box))
    save(icon, EXTRACTED, 'skill', f'{name}.png')
    save(square_crop(icon).resize((32, 32), Image.LANCZOS), TEX, 'skill', f'{name}.png')

for name, box in zip(HUD, found[len(names):]):
    if name is None:
        continue
    icon = remove_tile(sheet.crop(box))
    save(icon, EXTRACTED, 'hud', f'{name}.png')
    save(pad_square(icon).resize((16, 16), Image.LANCZOS), TEX, 'hud', f'{name}.png')

print(f'{len(names)} skill icons and {sum(n is not None for n in HUD)} HUD icons written')
