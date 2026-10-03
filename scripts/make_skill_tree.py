"""Writes the skill trees of scripts/skill_tree_catalog.py: the catalog block of SkillCatalog.java, the upgrades'
values in data/sofe/skills/<class>.json (the base skills keep theirs), the English and Spanish names and
descriptions of the upgrades and the branch names, and the upgrades' icons: each one is drawn from the icon of
the skill it improves, shifted in hue and marked with a small gold badge.

Run from the repository root: python scripts/make_skill_tree.py
"""
import collections
import colorsys
import json
import os
import sys

from PIL import Image, ImageDraw

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from skill_tree_catalog import BRANCHES, TREES  # noqa: E402

RES = os.path.join("src", "main", "resources")
CATALOG = os.path.join("src", "main", "java", "com", "sofe", "skill", "SkillCatalog.java")
ICONS = os.path.join(RES, "assets", "sofe", "textures", "gui", "skill")
TYPES = {"active": "ACTIVE", "passive": "PASSIVE", "ultimate": "ULTIMATE"}
HUES = [0.03, 0.55, 0.78]  # a tint per branch: warm, cold, violet


def java():
    lines = ["    static {", "        // <generated-tree> by scripts/make_skill_tree.py from scripts/skill_tree_catalog.py"]
    for cls, skills in TREES.items():
        lines.append("")
        lines.append("        // %s" % cls)
        for s in skills:
            parent = '"%s"' % s["parent"] if s["parent"] else "null"
            ups = ", ".join('"%s"' % u for u in s["upgrades"])
            lines.append('        add(%s, %d, %s, "%s", %s, %d, %d, List.of(%s));' % (
                cls.upper(), s["level"], TYPES[s["type"]], s["id"], parent, s["column"], s["tab"], ups))
    lines.append("        // </generated-tree>")
    lines.append("    }")
    src = open(CATALOG, encoding="utf-8").read()
    a = src.index("    static {")
    b = src.index("    private SkillCatalog() {")
    src = src[:a] + "\n".join(lines) + "\n\n" + src[b:]
    src = src.replace("""    private static void add(PlayerClass owner, int level, SkillType type, String id, String parent, int column) {
        ALL.add(new SkillInfo(id, owner, level, type, parent, column));
    }""", """    private static void add(PlayerClass owner, int level, SkillType type, String id, String parent, int column, int tab, List<String> upgrades) {
        ALL.add(new SkillInfo(id, owner, level, type, parent, column, tab, upgrades));
    }""")
    src = src.replace(""" * The 50 skills of docs/Clases.md, 10 per class, as a Diablo II style tree: three columns of
 * actives that grow down from the level-1 skills, and a column of passives (their own tab).""",
                      """ * The skill trees, 30 per class as in Diablo II: three branches (tabs) of 10, each growing down from
 * one level-1 skill. The 10 base skills of docs/Clases.md and 20 upgrades per class (synergies and
 * changes to the base skills); written by scripts/make_skill_tree.py.""")
    src = src.replace("""    /** The three level-1 skills of a class, in column order. */
    public static List<SkillInfo> startingSkills(PlayerClass owner) {
        return forClass(owner).stream().filter(s -> s.level() == 1 && s.type() == ACTIVE).toList();
    }""", """    /** The three level-1 skills of a class, one per branch, in branch order. */
    public static List<SkillInfo> startingSkills(PlayerClass owner) {
        return forClass(owner).stream().filter(s -> s.level() == 1 && s.type() == ACTIVE).toList();
    }

    /** The upgrades that improve this skill. */
    public static List<SkillInfo> upgradesOf(String skill) {
        return ALL.stream().filter(s -> s.upgrades().contains(skill)).toList();
    }""")
    open(CATALOG, "w", encoding="utf-8").write(src)


def data():
    for cls, skills in TREES.items():
        p = os.path.join(RES, "data", "sofe", "skills", cls + ".json")
        d = json.load(open(p, encoding="utf-8"), object_pairs_hook=collections.OrderedDict)
        for s in skills:
            if s["values"] is not None:
                d["skills"][s["id"]] = collections.OrderedDict(s["values"])
        open(p, "w", encoding="utf-8").write(json.dumps(d, indent=2) + "\n")


def lang():
    for code, k in (("en_us", 0), ("es_es", 1)):
        p = os.path.join(RES, "assets", "sofe", "lang", code + ".json")
        d = json.load(open(p, encoding="utf-8"), object_pairs_hook=collections.OrderedDict)
        for cls, skills in TREES.items():
            for s in skills:
                if s["names"]:
                    d["skill.sofe." + s["id"]] = s["names"][k]
                    d["skill.sofe.%s.desc" % s["id"]] = s["desc"][k]
            for i, names in enumerate(BRANCHES[cls]):
                d["gui.sofe.skill_tree.branch.%s.%d" % (cls, i)] = names[k]
        d["gui.sofe.skill_tree.improves"] = "Improves: %s" if k == 0 else "Mejora: %s"
        open(p, "w", encoding="utf-8").write(json.dumps(d, ensure_ascii=False, indent=2) + "\n")


def shift(img, hue):
    out = img.copy()
    px = out.load()
    for y in range(out.height):
        for x in range(out.width):
            r, g, b, a = px[x, y]
            if not a:
                continue
            h, l, s = colorsys.rgb_to_hls(r / 255, g / 255, b / 255)
            h = (h * 0.4 + hue * 0.6) % 1.0
            s = min(1, s * 1.15 + 0.05)
            r2, g2, b2 = colorsys.hls_to_rgb(h, l, s)
            px[x, y] = (int(r2 * 255), int(g2 * 255), int(b2 * 255), a)
    return out


def icons():
    for cls, skills in TREES.items():
        for s in skills:
            if s["names"] is None:
                continue
            source = s["upgrades"][0] if s["upgrades"] else s.get("icon_from", s["parent"])
            base_icon = Image.open(os.path.join(ICONS, source + ".png")).convert("RGBA").resize((32, 32), Image.NEAREST)
            icon = shift(base_icon, HUES[s["tab"]])
            dr = ImageDraw.Draw(icon)
            # the badge: a gold diamond with a plus, bottom right
            dr.polygon([(25, 19), (31, 25), (25, 31), (19, 25)], fill=(30, 22, 12, 255))
            dr.polygon([(25, 20), (30, 25), (25, 30), (20, 25)], fill=(232, 182, 74, 255))
            dr.line([(25, 22), (25, 28)], fill=(60, 36, 10, 255))
            dr.line([(22, 25), (28, 25)], fill=(60, 36, 10, 255))
            icon.save(os.path.join(ICONS, s["id"] + ".png"))


if __name__ == "__main__":
    java()
    data()
    lang()
    icons()
    total = sum(len(v) for v in TREES.values())
    print("skill trees: %d skills (%s)" % (total, ", ".join("%s %d" % (c, len(v)) for c, v in TREES.items())))
