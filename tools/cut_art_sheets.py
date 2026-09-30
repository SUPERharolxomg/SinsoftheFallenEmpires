"""
Cuts the transparent art sheets (art/concepts/*_v2.png, 2026-09-30) into separate files:

- art/extracted/<set>/<name>.png   native size, for review and future edits
- game textures in src/main/resources/assets/sofe/textures/gui at the sizes in docs/Arte.md

    pip install pillow numpy
    python tools/cut_art_sheets.py

Coordinates are specific to these two sheets. New art is easiest as one PNG per asset.
"""
import os
import shutil

import numpy as np
from PIL import Image

REPO = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..')
CONCEPTS = os.path.join(REPO, 'art', 'concepts')
EXTRACTED = os.path.join(REPO, 'art', 'extracted')
TEX = os.path.join(REPO, 'src', 'main', 'resources', 'assets', 'sofe', 'textures', 'gui')

SOURCES = {  # file dropped in the repo root -> name in art/concepts
    '081c8154-c232-408c-aa05-0d34acb9754e.png': 'asset_sheet_v2.png',
    '9b4aa7a0-46b8-4bf9-a217-a93c92f64d59.png': 'hero_cards_sheet_v2.png',
}
os.makedirs(CONCEPTS, exist_ok=True)
for src, dst in SOURCES.items():
    if os.path.exists(os.path.join(REPO, src)):
        shutil.move(os.path.join(REPO, src), os.path.join(CONCEPTS, dst))
assets = Image.open(os.path.join(CONCEPTS, 'asset_sheet_v2.png')).convert('RGBA')
heroes = Image.open(os.path.join(CONCEPTS, 'hero_cards_sheet_v2.png')).convert('RGBA')
written = []


def trim(img, threshold=40):
    """Crops to the visible pixels."""
    alpha = np.array(img)[..., 3] > threshold
    ys, xs = np.where(alpha)
    return img.crop((xs.min(), ys.min(), xs.max() + 1, ys.max() + 1))


def fit(img, w, h):
    """Center-crops to the target aspect ratio, then resizes with Lanczos."""
    iw, ih = img.size
    if iw / ih > w / h:
        nw = round(ih * w / h); x0 = (iw - nw) // 2; img = img.crop((x0, 0, x0 + nw, ih))
    else:
        nh = round(iw * h / w); y0 = (ih - nh) // 2; img = img.crop((0, y0, iw, y0 + nh))
    return img.resize((w, h), Image.LANCZOS)


def save(img, *parts):
    path = os.path.join(*parts)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)
    written.append(path)


def both(img, set_name, name, game_path, size):
    """Saves the native crop for review and the game-size texture."""
    save(img, EXTRACTED, set_name, name + '.png')
    save(fit(img, *size), TEX, *game_path)


# --- Dialogue box styles (64x64, 9-slice) ---
styles = ['sulthari', 'nordrath', 'parsivan', 'khemet', 'aureum', 'void']
boxes = [(12, 18, 266, 262), (418, 16, 672, 264), (686, 18, 936, 262),
         (946, 18, 1180, 260), (1190, 16, 1426, 260), (1438, 16, 1680, 260)]
for style, box in zip(styles, boxes):
    both(trim(assets.crop(box)), 'dialogue_box', f'{style}_box', ('dialogue', f'{style}_box.png'), (64, 64))
save(trim(assets.crop((296, 30, 382, 116))), EXTRACTED, 'reference', 'sulthari_gear_ornament.png')

# --- Portrait frames (80x80 with a transparent 64x64 window) ---
frames = [(16, 288, 156, 422), (170, 288, 308, 422), (320, 288, 462, 422), (476, 288, 620, 420)]
for style, box in zip(styles, frames):
    frame = trim(assets.crop(box))
    save(frame, EXTRACTED, 'portrait_frame', f'{style}_portrait.png')
    a = np.array(fit(frame, 80, 80)); a[8:72, 8:72, 3] = 0
    save(Image.fromarray(a), TEX, 'dialogue', f'{style}_portrait.png')

# --- Portraits (64x64) ---
portraits = ['cassian', 'ankhareth', 'shirin', 'rurik', 'azhar', 'ozhan', 'dilara', 'yusuf', 'selim', 'brass_sentinel']
portrait_boxes = [(24, 448, 128, 550), (146, 448, 250, 550), (268, 448, 370, 550), (388, 448, 492, 550), (508, 448, 610, 548),
                  (24, 570, 128, 670), (146, 570, 250, 670), (268, 570, 370, 670), (388, 570, 492, 670), (508, 570, 610, 670)]
for name, box in zip(portraits, portrait_boxes):
    both(trim(assets.crop(box)), 'portrait', name, ('portrait', f'{name}.png'), (64, 64))

# --- Bearer selection cards without the hero (512x640) ---
classes = ['knight', 'necromancer', 'sorceress', 'thief', 'king']
card_boxes = [(660, 282, 854, 532), (866, 280, 1060, 532), (1072, 280, 1264, 532), (1274, 280, 1472, 532), (1484, 280, 1682, 532)]
def solid_edges(img, threshold=160):
    """Cards: pixels are either fully visible or fully transparent, so the sheet's soft halo
    does not leave dark specks around the frame and the emblem."""
    a = np.array(img)
    a[..., 3] = np.where(a[..., 3] >= threshold, 255, 0)
    return Image.fromarray(a)


for name, box in zip(classes, card_boxes):
    card = solid_edges(trim(assets.crop(box)))
    save(card, EXTRACTED, 'bearer_card', f'{name}_card.png')
    save(solid_edges(fit(card, 512, 640)), TEX, 'bearer', f'{name}_card.png')

# --- Skill icons (32x32); the fourth icon repeats Wandering Spark ---
skills = {'ember_verse': (648, 560, 748, 660), 'frost_lance': (760, 560, 856, 660), 'wandering_spark': (864, 560, 966, 660)}
for name, box in skills.items():
    both(trim(assets.crop(box)), 'skill', name, ('skill', f'{name}.png'), (32, 32))

# --- Runes (16x16): fire and storm from the first row, the six-pointed snowflake from the second ---
runes = {'fire': (40, 696, 116, 768), 'storm': (228, 696, 300, 768), 'frost': (142, 776, 212, 852)}
for name, box in runes.items():
    both(trim(assets.crop(box)), 'rune', name, ('rune', f'{name}.png'), (16, 16))

# --- Wide dialogue bars (reference until their empire is decided) ---
# The bars' glow touches, so they are split at the thinnest rows (found from the alpha profile)
bar_rows = [(6, 54), (55, 104), (105, 150), (151, 200), (201, 250)]
for i, (y0, y1) in enumerate(bar_rows, 1):
    save(trim(assets.crop((640, 668 + y0, 1094, 668 + y1))), EXTRACTED, 'dialogue_bars', f'bar_{i}.png')

# --- Character skin drawings (reference for the outfit textures) ---
save(trim(assets.crop((1120, 552, 1680, 752))), EXTRACTED, 'reference', 'character_skins.png')

# --- Hero cards: the Bearers themselves, for the Codex and the companion screen (448x640) ---
hero_boxes = {'knight': (14, 6, 630, 904), 'necromancer': (670, 12, 972, 442), 'sorceress': (1008, 18, 1326, 442)}
column = [trim(heroes.crop((1350, 10, 1672, 442))), trim(heroes.crop((1350, 442, 1672, 904)))]  # thief on top, king below
for name, box in hero_boxes.items():
    both(trim(heroes.crop(box)), 'hero_card', name, ('bearer', f'{name}_hero.png'), (448, 640))
for name, img in zip(['thief', 'king'], column):
    both(img, 'hero_card', name, ('bearer', f'{name}_hero.png'), (448, 640))

print(len(written), 'files written')
