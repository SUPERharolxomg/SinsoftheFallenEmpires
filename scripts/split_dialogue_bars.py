"""Splits the high-resolution dialogue bar sheets (art/concepts/dialogue_bars_hd_*.png) into one
transparent texture per style: textures/gui/dialogue/<style>_bar.png.

Bars that touch on the sheet are cut at the thinnest row between them; near the cut, each pixel goes
to the bar whose average color is closer (Khemet's keystone pokes into the Void bar's rows). The
enclosed transparent window of each bar is written to <style>_bar.json for DialogueArt. Faint halo pixels
(alpha < 40) are cleared so no colored noise shows around the edges in game.

Run from the repository root: python scripts/split_dialogue_bars.py
"""
import json
import os
from collections import deque

import numpy as np
from PIL import Image

OUT = os.path.join("src", "main", "resources", "assets", "sofe", "textures", "gui", "dialogue")
SHEETS = {
    "art/concepts/dialogue_bars_hd_sulthari.png": ["sulthari"],
    "art/concepts/dialogue_bars_hd_regions.png": ["nordrath", "burning_deep", "void", "khemet"],
}


def bands(alpha, count):
    """Row ranges of the bars: cut at the emptiest rows so each piece holds one bar."""
    rows = (alpha > 40).sum(1)
    h = len(rows)
    edges = [0]
    for k in range(1, count):
        lo, hi = h * k // count - h // (count * 3), h * k // count + h // (count * 3)
        edges.append(lo + int(np.argmin(rows[lo:hi])))
    edges.append(h)
    return list(zip(edges, edges[1:]))


OVERLAP = 30


def body_color(data, top, bottom):
    middle = data[(top * 2 + bottom) // 3:(top + bottom * 2) // 3]
    solid = middle[middle[:, :, 3] > 200][:, :3]
    return solid.mean(0)


def warm(color):
    return float(color[0]) - float(color[2])


def fill(mask, seeds):
    """Flood fill from the seeds over True pixels of the mask; returns the reached pixels."""
    h, w = mask.shape
    seen = np.zeros_like(mask)
    queue = deque()
    for p in seeds:
        if mask[p] and not seen[p]:
            seen[p] = True
            queue.append(p)
    while queue:
        y, x = queue.popleft()
        for q in ((y + 1, x), (y - 1, x), (y, x + 1), (y, x - 1)):
            if 0 <= q[0] < h and 0 <= q[1] < w and mask[q] and not seen[q]:
                seen[q] = True
                queue.append(q)
    return seen


def window(piece):
    """The portrait window: the largest transparent region in the left part that the top and left edges cannot reach."""
    alpha = np.array(piece)[:, :, 3]
    h, w = alpha.shape
    scan = min(w, h * 2)
    clear = alpha[:, :scan] < 20
    inside = clear & ~fill(clear, [(0, x) for x in range(scan)] + [(y, 0) for y in range(h)])
    best, done = None, np.zeros_like(inside)
    for y, x in zip(*np.where(inside)):
        if done[y, x]:
            continue
        region = fill(inside, [(y, x)])
        done |= region
        if best is None or region.sum() > best.sum():
            best = region
    ys, xs = np.where(best)
    return [int(xs.min()), int(ys.min()), int(xs.max() - xs.min() + 1), int(ys.max() - ys.min() + 1)]


for sheet, styles in SHEETS.items():
    img = Image.open(sheet).convert("RGBA")
    data = np.array(img)
    data[data[:, :, 3] < 40] = 0
    cuts = bands(data[:, :, 3], len(styles))
    colors = [body_color(data, top, bottom) for top, bottom in cuts]
    pieces = [data.copy() for _ in styles]
    for k, (top, bottom) in enumerate(cuts):
        keep = np.zeros(data.shape[:2], bool)
        keep[top:bottom] = True
        pieces[k][~keep] = 0
    for k in range(len(styles) - 1):
        # each blob of pixels around the cut follows the bar it is attached to: the one whose body it
        # touches at the top or bottom of the overlap rows, or by color when it touches both or neither
        cut = cuts[k][1]
        top, bottom = max(0, cut - OVERLAP), min(data.shape[0], cut + OVERLAP)
        solid = data[top:bottom, :, 3] > 0
        done = np.zeros_like(solid)
        for y, x in zip(*np.where(solid)):
            if done[y, x]:
                continue
            blob = fill(solid, [(y, x)])
            done |= blob
            ys = np.where(blob)[0]
            up, down = ys.min() == 0, ys.max() == bottom - top - 1
            if up != down:
                upper = np.full(blob.shape, up)
            elif up:
                # both bars meet in this blob: split it pixel by pixel by warmth (red minus blue), which
                # tells the Void's purples from Khemet's sand and gold whatever the shading
                rgb = data[top:bottom, :, :3].astype(float)
                warmth = rgb[:, :, 0] - rgb[:, :, 2]
                upper = np.abs(warmth - warm(colors[k])) <= np.abs(warmth - warm(colors[k + 1]))
            else:
                mean = data[top:bottom][blob][:, :3].astype(float).mean(0)
                upper = np.full(blob.shape, np.linalg.norm(mean - colors[k]) <= np.linalg.norm(mean - colors[k + 1]))
            mask_up = np.zeros(data.shape[:2], bool)
            mask_up[top:bottom] = blob & upper
            mask_down = np.zeros(data.shape[:2], bool)
            mask_down[top:bottom] = blob & ~upper
            pieces[k][mask_up] = data[mask_up]
            pieces[k][mask_down] = 0
            pieces[k + 1][mask_down] = data[mask_down]
            pieces[k + 1][mask_up] = 0
        # islands left detached from their own bar's body (highlights split off by color) go to the other one
        for own, other, edge in ((k, k + 1, "top"), (k + 1, k, "bottom")):
            solid = pieces[own][top:bottom, :, 3] > 0
            done = np.zeros_like(solid)
            for y, x in zip(*np.where(solid)):
                if done[y, x]:
                    continue
                island = fill(solid, [(y, x)])
                done |= island
                ys = np.where(island)[0]
                attached = ys.min() == 0 if edge == "top" else ys.max() == bottom - top - 1
                if not attached:
                    mask = np.zeros(data.shape[:2], bool)
                    mask[top:bottom] = island
                    pieces[other][mask] = data[mask]
                    pieces[own][mask] = 0
    for style, piece_data in zip(styles, pieces):
        piece = Image.fromarray(piece_data)
        piece = piece.crop(piece.getbbox())
        piece.save(os.path.join(OUT, style + "_bar.png"))
        rect = window(piece)
        with open(os.path.join(OUT, style + "_bar.json"), "w", newline="\n") as f:
            json.dump({"window": rect}, f)
            f.write("\n")
        print(style, piece.size, rect)
