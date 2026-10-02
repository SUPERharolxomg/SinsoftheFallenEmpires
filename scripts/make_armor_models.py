"""Builds every SoFE armor set of scripts/armor_catalog.py as a 3D GeckoLib model, with its texture and the
icons of its pieces drawn from the model itself (scripts/armor_lib.py has the parts, painter and renderer).

Outputs: assets/sofe/geo/armor/<set>.geo.json, assets/sofe/textures/models/armor/geo/<set>.png and
assets/sofe/textures/item/<set>_<piece>.png (the unique pieces: <id>.png).
Run from the repository root: python scripts/make_armor_models.py [set,set,...]
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from armor_catalog import SETS, UNIQUES  # noqa: E402
from armor_lib import build  # noqa: E402

if __name__ == "__main__":
    only = set(sys.argv[1].split(",")) if len(sys.argv) > 1 else None
    total = 0
    for s in SETS:
        if only and s["name"] not in only:
            continue
        build(s["name"], s["pal"], s["parts"])
        total += 1
    for u in UNIQUES:
        if only and u["name"] not in only:
            continue
        build(u["name"], u["pal"], u["parts"], pieces=[u["piece"]], icon_names={u["piece"]: u["name"]})
        total += 1
    print("armor models:", total)
