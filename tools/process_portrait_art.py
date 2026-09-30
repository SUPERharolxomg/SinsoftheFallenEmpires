"""
Prepares the art added on 2026-09-30 for Sprint 2:

- Aureum and Void portrait frames -> textures/gui/dialogue/<style>_portrait.png (80x80, empty window)
- Ferid and the Council elder -> textures/gui/portrait/<id>.png (64x64)
- the scene sheet: panel 4 (Sulthari from the walls) becomes the provisional title key art
  textures/gui/title/keyart.png (1920x1080); the other scenes are kept in art/extracted/scene
- the two armor drawings are kept as outfit references

    pip install pillow numpy
    python tools/process_portrait_art.py
"""
import colorsys
import os
import shutil
from collections import deque

import numpy as np
from PIL import Image

REPO = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..')
CONCEPTS = os.path.join(REPO, 'art', 'concepts')
EXTRACTED = os.path.join(REPO, 'art', 'extracted')
TEX = os.path.join(REPO, 'src', 'main', 'resources', 'assets', 'sofe', 'textures', 'gui')

DROPPED = {
    '9d91fbbb-b320-4116-b940-9f8a85ec6866.jpg': 'frames_portraits_sheet.jpg',
    'Gemini_Generated_Image_5r8ekg5r8ekg5r8e.jpg': 'scenes_sheet.jpg',
}
os.makedirs(CONCEPTS, exist_ok=True)
for src, dst in DROPPED.items():
    if os.path.exists(os.path.join(REPO, src)):
        shutil.move(os.path.join(REPO, src), os.path.join(CONCEPTS, dst))
frames_sheet = Image.open(os.path.join(CONCEPTS, 'frames_portraits_sheet.jpg')).convert('RGBA')
scenes = Image.open(os.path.join(CONCEPTS, 'scenes_sheet.jpg')).convert('RGBA')


def save(img, *parts):
    path = os.path.join(*parts)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)
    print(os.path.relpath(path, REPO), img.size)


def is_window(rgb):
    """Parchment, the placeholder silhouette and the Void's pale glow: light or medium, not very saturated."""
    r, g, b = (c / 255 for c in rgb)
    h, s, v = colorsys.rgb_to_hsv(r, g, b)
    return v > 0.33 and s < 0.38


def empty_window(frame):
    """Makes the frame's inner window transparent: flood from the center over window colors,
    then clear each row between the first and last window pixel (the window is convex)."""
    a = np.array(frame)
    H, W = a.shape[:2]
    inside = np.zeros((H, W), bool)
    start = (H // 2, W // 2)
    q = deque([start]); inside[start] = True
    while q:
        y, x = q.popleft()
        for dy, dx in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            ny, nx = y + dy, x + dx
            if 0 <= ny < H and 0 <= nx < W and not inside[ny, nx] and is_window(a[ny, nx, :3]):
                inside[ny, nx] = True; q.append((ny, nx))
    for y in range(H):
        xs = np.where(inside[y])[0]
        if len(xs):
            a[y, xs.min():xs.max() + 1, 3] = 0
    return Image.fromarray(a)


def fit(img, w, h):
    iw, ih = img.size
    if iw / ih > w / h:
        nw = round(ih * w / h); x0 = (iw - nw) // 2; img = img.crop((x0, 0, x0 + nw, ih))
    else:
        nh = round(iw * h / w); y0 = (ih - nh) // 2; img = img.crop((0, y0, iw, y0 + nh))
    return img.resize((w, h), Image.LANCZOS)


# --- Portrait frames: first variant of each is the game texture, the second is kept as an alternative ---
frames = {
    'aureum': (35, 20, 235, 268), 'aureum_alt': (272, 25, 470, 270),
    'void': (548, 20, 758, 272), 'void_alt': (785, 22, 990, 272),
}
for name, box in frames.items():
    frame = empty_window(frames_sheet.crop(box))
    save(frame, EXTRACTED, 'portrait_frame', f'{name}_portrait.png')
    if not name.endswith('_alt'):
        # Frames are drawn over the portrait, so squashing the tall frame into 80x80 only hides its edges
        save(frame.resize((80, 80), Image.LANCZOS), TEX, 'dialogue', f'{name}_portrait.png')

# --- Portraits ---
portraits = {'ferid': (30, 322, 225, 517), 'council_elder': (258, 322, 453, 517)}
for name, box in portraits.items():
    face = frames_sheet.crop(box)
    save(face, EXTRACTED, 'portrait', f'{name}.png')
    save(face.resize((64, 64), Image.LANCZOS), TEX, 'portrait', f'{name}.png')

# --- Outfit references ---
save(frames_sheet.crop((575, 300, 728, 552)), EXTRACTED, 'reference', 'armor_blue_gold.png')
save(frames_sheet.crop((812, 300, 968, 552)), EXTRACTED, 'reference', 'armor_void.png')

# --- Scenes: four panels split by 4 px black lines ---
panels = {
    'eclipse_invasion': (0, 0, 686, 382),
    'sulthari_airship': (690, 0, 1376, 382),
    'dungeon': (0, 386, 686, 768),
    'sulthari_walls': (690, 386, 1376, 768),
}
for name, box in panels.items():
    save(scenes.crop(box), EXTRACTED, 'scene', f'{name}.png')
# Provisional key art: the characters stand on the sides and the center stays free for the menu buttons
save(fit(scenes.crop(panels['sulthari_walls']), 1920, 1080), TEX, 'title', 'keyart.png')
