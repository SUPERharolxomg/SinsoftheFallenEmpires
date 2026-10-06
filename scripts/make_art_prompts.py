"""Writes the "Ready to paste" section of docs/ArteFinal.md: for every illustration still missing (its file not yet in
textures/gui/splash/), the whole message for an image AI, built from the piece's row in the brief's tables. Run it again
after adding illustrations and the done ones leave the list.

Run from the repository root: python scripts/make_art_prompts.py
"""
import io
import os
import re

DOC = os.path.join("docs", "ArteFinal.md")
SPLASH = os.path.join("src", "main", "resources", "assets", "sofe", "textures", "gui", "splash")
MARK = "## Ready to paste"

STYLE = ("Create {shape} illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, "
         "dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).")
RULES = "Rules: no text, no letters, no logos."
BOTTOM = "Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there."
STAGE = "Leave an empty, lit spot in the center of the lower third: no character standing there."
GREY = "This is the version where nothing was settled: greyer and dimmer than a hopeful dawn, the sun weak behind clouds."


def pieces(doc):
    """(folder, file, prompt) for every row of the brief's tables, in order."""
    folder = None
    for line in doc.split("\n"):
        m = re.match(r"^### \d+\..*\(`splash/([a-z]+)/", line)
        if m:
            folder = m.group(1)
            continue
        if line.startswith("### 7."):
            folder = None
        row = re.match(r"^\| `([^`]+)` \|(.*)\|\s*$", line)
        if not row:
            continue
        cells = [c.strip() for c in row.group(2).split("|")]
        path, prompt = row.group(1), cells[-1]
        if folder is None:  # section 7 names its whole path
            if not path.startswith("splash/") or "**Done**" in line:
                continue
            folder, name = path[len("splash/"):].rsplit("/", 1)
            yield folder, name, prompt
            folder = None
            continue
        yield folder, path, prompt


def message(folder, name, prompt):
    shape = {"hero": "a tall portrait-format", "map": "a square"}.get(folder, "a wide landscape")
    lines = [STYLE.format(shape=shape), "", "Scene: " + prompt.rstrip(".") + ".", ""]
    rules = [RULES]
    if folder not in ("hero", "map"):
        rules.append(BOTTOM)
    if folder in ("epilogue", "crowned"):
        rules.append(STAGE)
    if name.endswith("_unsettled.png"):
        rules.append(GREY)
    rules.append({"hero": "Portrait format.", "map": "Square format."}.get(folder, "Landscape format."))
    lines.append(" ".join(rules))
    return "\n".join(lines)


def main():
    doc = io.open(DOC, encoding="utf-8").read()
    body = doc.split("\n" + MARK)[0].rstrip() + "\n"
    todo = [(f, n, p) for f, n, p in pieces(body) if not any(os.path.exists(os.path.join(SPLASH, f, n[:-4] + ext)) for ext in (".png", ".jpg"))]
    out = [MARK, "",
           "Every illustration still missing, with its whole message for the image AI (ChatGPT, or any other). For each one:",
           "",
           "1. Open a new chat and attach the style reference (`art/concepts/style_reference_bearers.png`).",
           "2. Copy the message in the box and send it.",
           "3. Hand the picture over to be put in place, or save it yourself as JPG (quality 90, 1920×1080) with the name shown (`.jpg` instead of `.png`) in `src/main/resources/assets/sofe/textures/gui/splash/<folder>/`.",
           "",
           "This list is written by `scripts/make_art_prompts.py` from the tables above; run it again and the pieces already in the game leave it.",
           "",
           "%d to go." % len(todo), ""]
    for i, (folder, name, prompt) in enumerate(todo, 1):
        out += ["### %d. `%s/%s`" % (i, folder, name), "", "```text", message(folder, name, prompt), "```", ""]
    io.open(DOC, "w", encoding="utf-8").write(body + "\n" + "\n".join(out))
    print("%d prompts ready" % len(todo))


if __name__ == "__main__":
    main()
