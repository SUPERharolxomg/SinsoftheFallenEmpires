# Visual Design Guide

How the art of *Sins of the Fallen Empires* is made. The full list of unique assets (logo, panorama, splash arts, 3D models) with sizes and placeholders is in [Anexos.md](Anexos.md#a2-splash-arts-and-3d-models). Hero visuals are described in [Clases.md](Clases.md).

## Asset types

| Asset type | Tool | Format | Notes |
|------------|------|--------|-------|
| **Block/item textures** | Aseprite, Piskel, GIMP | 16×16 or 32×32 PNG | Pixel art, Minecraft style |
| **Entity models** | Blockbench | Java model, JSON or GeckoLib | GeckoLib for animated bosses |
| **GUI screens** | GIMP, Photoshop, Figma | 256×256 PNG | Follow Minecraft GUI conventions |
| **Splash arts / key art** | To be decided | 1920×1080 PNG | See Anexos A2 |
| **Particles** | Java code | — | Minecraft particle system |
| **Structures** | Structure Block in-game | .nbt | Build in-game, export |
| **Sounds** | Audacity, Freesound.org | .ogg | OGG Vorbis required |

## Recommended tools

- **Blockbench** (blockbench.net): free 3D model editor for Minecraft entities, with a GeckoLib plugin
- **Aseprite** ($20) or **Piskel** (free): pixel art
- **GIMP**: free image editor for GUI screens
- **Audacity**: free audio editor
- **Freesound.org**: free sound library (check licenses)

## Art style

- Follow Minecraft's 16×16 pixel art style for consistency.
- **Sulthari** contrasts with the rest of the map: brass, clockwork, domes and glowing aetherium crystals. The other empires are corrupted ruins with black aetherium.
- Empire palettes:

| Empire | Palette |
|--------|---------|
| Sulthari | Gold, red, dark brown |
| Nordrath | Ice blue, gray, blood red |
| Parsivan | Purple, turquoise, silver |
| Khemet | Sand, turquoise, gold |
| Aureum | Marble, gold, royal blue |

- **Archsins:** 3–4 blocks tall, glowing eyes in their sin's color.
- **Broken Oaths:** 2–3 blocks tall, armor of the empire whose Law they guarded, visibly cracked or corrupted.
- **Nahrazel:** Phase 1 is a colossus of ash; later phases mix elements of all seven sins.
- **Bearers:** player scale, each with the palette of their home empire and a signature particle (see [Clases.md](Clases.md)).
- **Aetherium:** clear cyan-white when pure, black with violet glow when corrupted.

---

## Asset specs and briefs

Exact sizes, file names and what each image must show. All files are PNG with transparency, in `src/main/resources/assets/sofe/textures/`. Minecraft draws GUI textures without smoothing, so pixel art at the listed size looks sharpest; painted art should be exported at exactly the listed size.

### The player is drawn live, not as a fixed image

Every player has a different skin, so any image of *the player's own Bearer* is rendered by the game in real time: their skin plus the Bearer outfit they are wearing.

| Where | What the game draws | What the artist makes |
|-------|---------------------|-----------------------|
| Bearer selection screen | The player's own 3D model, turning slowly, wearing the outfit of the Bearer currently selected | A **card background** per Bearer (emblem and scene, no face) |
| Dialogue box, when the player speaks | The player's head and shoulders, live, inside the portrait frame | Nothing; only the empire's portrait frame |
| Everyone else (NPCs, bosses, companion Bearers) | A fixed portrait | A **portrait** per character |

### Bearer selection cards

`gui/bearer/<class>_card.png` — **512×640** (4:5), one per class: `knight`, `necromancer`, `sorceress`, `thief`, `king`.

The player's model is drawn on the lower-center part of the card, so keep the **center column (about 200 px wide) and the bottom 60%** free of important details. Top area: the Bearer's emblem. Background: the scene where the shard found them.

| Card | Emblem (top) | Scene (background) | Palette |
|------|--------------|--------------------|---------|
| Knight — Cassian | The Scale of the Order, cracked | Broken royal blue banner, Aureum courtroom columns | Marble, silver, royal blue, gold |
| Necromancer — Ankhareth | Jackal mask over a scale | The Sulthari morgue lit by turquoise lamps, papyri | Sand, black, turquoise, gold |
| Sorceress — Shirin | Astrolabe with seven stars | The Great Observatory's dome at night, the eclipse | Purple, turquoise, silver |
| Thief — Rurik | Two crossed short axes over an ember | The Low Bazaar at night, a burning village in the smoke | Dark gray, ice blue, red |
| King — Azhar | Brass crown with an aetherium crystal | The throne room of Sulthari, the court in shadow | Gold, red, dark brown |

### Dialogue box, one style per empire

Six styles: `sulthari`, `nordrath`, `parsivan`, `khemet`, `aureum` and `void`. The style comes from **where the conversation happens**; `void` is used in the Outer Void, inside the Codex, for Nahrazel and for the secret bad ending.

Per style, in `gui/dialogue/`:

| File | Size | Description |
|------|------|-------------|
| `<style>_box.png` | **64×64** | The bar behind the text. **9-slice:** the 8 px border on each side is kept as is in the corners and stretched along the edges; the 48×48 center is stretched to fill. So the same file works on any screen width. The center should be dark and about 85% opaque, so the text is readable over the game. |
| `<style>_portrait.png` | **80×80** | The frame around the portrait: 8 px border, with a **transparent 64×64 window** in the middle (from pixel 8 to 72). |
| `<style>_name.png` | **96×16** | Small plate behind the speaker's name. 9-slice with a 4 px border. |
| `<style>_ornament.png` | **48×16** | Decoration centered on the top edge of the box. Optional. |

| Style | Look |
|-------|------|
| Sulthari | Riveted brass plates, small clockwork gears in the corners, thin red lacquer line inside |
| Nordrath | Dark timber with iron nails, carved runes along the edges, knotwork corners |
| Parsivan | Silver filigree, turquoise tiles, eight-pointed star in the corners |
| Khemet | Sandstone with a band of gold hieroglyphs, lotus or scarab corners |
| Aureum | White marble, gold meander (Greek key) band, laurel corners |
| Void | Black, with violet cracks and torn Codex pages drifting at the edges |

### Portraits (NPCs and bosses)

`gui/portrait/<id>.png` — **64×64**. Head and shoulders, facing slightly right (towards the text), on a transparent or simple dark background. Pixel art matches Minecraft best; painted art is fine if exported at 64×64.

First ones needed (Act I): the five Bearers as NPCs and companions (`cassian`, `ankhareth`, `shirin`, `rurik`, `azhar`; any of them can be a companion, and the one the player picks is drawn live instead), `ozhan` (Grand Vizier), `council_elder`, `ferid`, `dilara`, `yusuf`, `selim`, `brass_sentinel`.

### Title screen

| File | Size | Description |
|------|------|-------------|
| `gui/title/logo.png` | **1024×256** | "Sins of the Fallen Empires" on a transparent background. Idea: the Void Codex open behind the text, with a crack of violet light. |
| `gui/title/background/panorama_0.png` … `_5.png` | **6 × 1024×1024** | Rotating panorama of Sulthari during the Eclipse Festival: brass domes, tramways, the Great Observatory lit up, the eclipse in the sky. Order: 0 front, 1 right, 2 back, 3 left, 4 up, 5 down. Easiest once Sulthari is built in game. |
| `gui/title/keyart.png` | **1920×1080** | Alternative to the panorama: one static illustration. |

### Skill icons

`gui/skill/<skill_id>.png` — **32×32**, full square (the UI draws the slot frame around it). The icon must still read at half size, so one clear shape per icon, strong outline, few colors. Background: a dark gradient in the class's color.

**Sprint 2 — Sorceress (Shirin), Persian style: calligraphy, stars and constellations; purple, turquoise and silver:**

| Skill | File | What it shows |
|-------|------|---------------|
| Ember Verse | `ember_verse.png` | A line of glowing calligraphy (invented letters, not a real script) that turns into a flaming arrow flying toward the top right. Ember orange and red with gold highlights, deep purple background. |
| Frost Lance | `frost_lance.png` | A long crystalline spear of ice, diagonal from bottom left to top right, with an ornate silver tip and small frost stars around it. Pale cyan and white with silver, dark blue-purple background. |
| Wandering Spark | `wandering_spark.png` | A bright spark that zigzags through three small points (the three enemies it bounces between). Turquoise and white, purple background. |

**Runes** — `gui/rune/<rune>.png`, **16×16**, drawn on the HUD when a spell leaves one. Each is a small glyph made of connected stars, with a silver outline:

| Rune | File | Glyph |
|------|------|-------|
| Fire | `fire.png` | Five-pointed star glyph, orange with a gold center |
| Frost | `frost.png` | Six-pointed snowflake glyph, cyan with a white center |
| Storm | `storm.png` | Zigzag bolt glyph, turquoise with a pale yellow center |

Icons for the other four classes are briefed in the sprint that adds their skills (Sprint 6), in the style of each Bearer's empire.

### Bearer outfits

Vanilla armor texture format, so the Blockbench or vanilla armor templates work:

| File | Size | Covers |
|------|------|--------|
| `models/armor/<class>_outfit_layer_1.png` | **64×32** | Helmet, chestplate, boots |
| `models/armor/<class>_outfit_layer_2.png` | **64×32** | Leggings |

Follow the visuals in [Clases.md](Clases.md). Capes, hoods and floating objects (Shirin's astrolabe) need extra 3D parts, which come later with GeckoLib; the first version is armor texture only.
