"""
Prepares the menu art added on 2026-09-30:

- the logo -> textures/gui/title/logo.png (trimmed to its visible pixels, 1024 px wide, aspect kept)
- the class emblems sheet -> textures/gui/bearer/<class>_emblem.png (64x64) for the selection buttons
- the menu mockups are kept in art/concepts as reference

    pip install pillow numpy
    python tools/process_menu_art.py
"""
import os
import shutil

import numpy as np
from PIL import Image

REPO = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..')
CONCEPTS = os.path.join(REPO, 'art', 'concepts')
EXTRACTED = os.path.join(REPO, 'art', 'extracted')
TEX = os.path.join(REPO, 'src', 'main', 'resources', 'assets', 'sofe', 'textures', 'gui')

DROPPED = {  # file dropped in the repo root -> name in art/concepts
    '36de0b38-c258-49e3-abf9-f26be27f6491.png': 'logo_v1.png',
    'c34c3913-dfad-4c1c-a06f-bc807ab208bc.jpg': 'class_emblems_sheet.jpg',
    '3bfdbb4a-ecef-4abd-aa58-adf48a1db4a6.jpg': 'title_screen_mockup.jpg',
    'watermarked_img_15967767774121306787.jpg': 'menu_icons_mockup.jpg',
}
os.makedirs(CONCEPTS, exist_ok=True)
for src, dst in DROPPED.items():
    if os.path.exists(os.path.join(REPO, src)):
        shutil.move(os.path.join(REPO, src), os.path.join(CONCEPTS, dst))


def save(img, *parts):
    path = os.path.join(*parts)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)
    print(os.path.relpath(path, REPO), img.size)


# --- Logo: trim the empty border, keep its own aspect ratio (the title screen reads the real size) ---
logo = Image.open(os.path.join(CONCEPTS, 'logo_v1.png')).convert('RGBA')
alpha = np.array(logo)[..., 3] > 40
ys, xs = np.where(alpha)
margin = 8  # keep a little of the soft shadow
box = (max(0, xs.min() - margin), max(0, ys.min() - margin),
       min(logo.width, xs.max() + 1 + margin), min(logo.height, ys.max() + 1 + margin))
logo = logo.crop(box)
save(logo.resize((1024, round(1024 * logo.height / logo.width)), Image.LANCZOS), TEX, 'title', 'logo.png')

# --- Class emblems: the dark square tile of each emblem, without the caption below it ---
sheet = Image.open(os.path.join(CONCEPTS, 'class_emblems_sheet.jpg')).convert('RGBA')
emblems = {'knight': (86, 66), 'necromancer': (419, 66), 'sorceress': (86, 314), 'thief': (419, 314), 'king': (753, 314)}
for name, (x, y) in emblems.items():
    tile = sheet.crop((x, y, x + 194, y + 194))
    save(tile, EXTRACTED, 'emblem', f'{name}.png')
    save(tile.resize((64, 64), Image.LANCZOS), TEX, 'bearer', f'{name}_emblem.png')
