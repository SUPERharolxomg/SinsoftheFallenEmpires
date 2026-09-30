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

### Hero cards

`gui/bearer/<class>_hero.png` — **448×640**. The same cards **with the hero drawn** (Cassian, Ankhareth, Shirin, Rurik, Azhar). They are not used on the selection screen (that one shows the player's own model); they show the Bearers as characters of the story:

- the Bearers page of the Codex;
- the companion screen, when hiring one of the other Bearers (UC-22);
- the Bearer's epilogue in the ending.

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
| `gui/title/logo.png` | **1024 px wide**, any height (the current one is 1024×599) | "Sins of the Fallen Empires" on a transparent background, with the cracked Void Codex behind the text. The title screen reads its real size and fits it above the buttons without stretching it. |
| `gui/title/background/panorama_0.png` … `_5.png` | **6 × 1024×1024** | Rotating panorama of Sulthari during the Eclipse Festival: brass domes, tramways, the Great Observatory lit up, the eclipse in the sky. Order: 0 front, 1 right, 2 back, 3 left, 4 up, 5 down. Easiest once Sulthari is built in game. |
| `gui/title/keyart.png` | **1920×1080** | Alternative to the panorama: one static illustration. **Used automatically as soon as the file exists.** The logo covers the top-center (about the upper 25%) and the menu buttons the center column from 40% to 90% of the height, so keep the main subject to the sides or behind the logo, and the center calm and dark. On screens that are not 16:9 the sides or top are cropped a little. |

The logo is also **used automatically when `gui/title/logo.png` exists**; until then the menu shows the title as text.

**Class emblems** — `gui/bearer/<class>_emblem.png`, **64×64**: the Bearer's emblem on a dark square (the Scale on a shield, the jackal mask, the astrolabe, the crossed axes, the crown). Used as the class buttons of the selection screen.

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

#### All skill icons

Same rules for every icon: `gui/skill/<id>.png`, **32×32** in game (deliver 128×128 or larger in the same square shape; it is downscaled), full square, one clear shape. The UI adds the frame: square for active skills, round for passives, gold for ultimates, so the icon itself never needs a frame.

**Knight — Cassian (Aureum):** Roman and Byzantine motifs (scutum shield, laurels, the Scale); marble white, silver, royal blue and gold; dark royal blue background.

| Skill | `id` | What it shows |
|-------|------|---------------|
| Scale Strike | `scale_strike` | A sword striking down, with a golden scale behind it tipping to one side |
| Shield Wall | `shield_wall` | A rectangular scutum seen from the front, with a golden crack, sparks on its edge |
| Banner Cry | `banner_cry` | The royal blue banner of the Order waving, with sound waves coming out |
| Legionary Charge | `legionary_charge` | A shield rushing forward with speed lines behind it |
| Verdict | `verdict` | A sword planted in the ground with a golden shockwave ring |
| Protector's Oath | `protectors_oath` | Two hands clasped under a small shield, a silver-blue glow |
| Iron Discipline (passive) | `iron_discipline` | A shield with a thick steel border and a laurel wreath |
| Living Rampart | `living_rampart` | A glowing wall of shields in a row, arrows breaking against it |
| Contained Wrath (passive) | `contained_wrath` | A shield with red cracks, red light leaking out |
| Last One Standing (ultimate) | `last_one_standing` | A lone knight silhouette in golden light, the broken banner behind |

**Necromancer — Ankhareth (Khemet):** Egyptian motifs (jackal, scarabs, canopic jars, papyrus, linen wraps); souls are turquoise wisps; sand, black, turquoise and gold; black and dark teal background.

| Skill | `id` | What it shows |
|-------|------|---------------|
| Threshold Touch | `threshold_touch` | An open hand with a short turquoise beam and a small soul wisp |
| Clay Warden | `clay_warden` | The head of a clay golem with a gold-painted face, a soul in its chest |
| Burial Wraps | `burial_wraps` | Linen bandages wrapping around a figure, turquoise hieroglyphs |
| Scales of Anubet | `scales_of_anubet` | A golden scale: a heart on one side, a feather on the other |
| Scarab Plague | `scarab_plague` | A swarm of golden scarabs in a spiral |
| Canopic Jars | `canopic_jars` | Three canopic jars with animal heads, souls flowing in |
| Rite of Passage (passive) | `rite_of_passage` | A soul rising through an open stone doorway |
| Boat of the Dead | `boat_of_the_dead` | A spectral turquoise boat sailing across the icon |
| Heavy Heart (passive) | `heavy_heart` | A clay heart cracking with turquoise light |
| The Great Judgment (ultimate) | `the_great_judgment` | A huge underworld gate half open, a jackal head above it |

**Sorceress — Shirin (Parsivan), the rest of her skills** (same style as the first three):

| Skill | `id` | What it shows |
|-------|------|---------------|
| Burning Calligraphy | `burning_calligraphy` | A line of calligraphy on the ground, burning |
| Water Mirror | `water_mirror` | A silhouette next to its ice copy, a water ripple between them |
| Petal Tempest | `petal_tempest` | A whirlwind of pink and silver petals |
| Sky Map (passive) | `sky_map` | A star map with three stars joined by lines |
| Starfall | `starfall` | Seven stars falling on a marked circle |
| Arcane Poetry (passive) | `arcane_poetry` | An open book with three runes of different colors above it |
| Written Eclipse (ultimate) | `written_eclipse` | The eclipse (black sun with a violet ring) framed by calligraphy |

**Thief — Rurik "Ash" (Nordrath):** Norse motifs (short axes, wolf fur, embers and ash, rope); dark gray, ice blue and red; dark slate background.

| Skill | `id` | What it shows |
|-------|------|---------------|
| Double Edge | `double_edge` | Two short axes crossing, two red slash lines |
| Light Fingers | `light_fingers` | A gloved hand grabbing a glowing potion |
| Smoke Step | `smoke_step` | A figure fading into a cloud of gray smoke |
| Cutthroat | `cutthroat` | An axe with five small red marks around it |
| Fjord Snare | `fjord_snare` | A rope noose tightening, ice blue background |
| Throwing Axe | `throwing_axe` | A spinning axe with a curved arrow showing its return |
| Deep Pockets (passive) | `deep_pockets` | An open pouch spilling coins |
| Thousand Cuts | `thousand_cuts` | Many red slash lines crossing in every direction |
| Scavenger's Instinct (passive) | `scavengers_instinct` | A wolf eye glowing in the dark |
| The Great Heist (ultimate) | `the_great_heist` | A hand stealing a glowing orb of power, embers around it |

**King — Azhar (Sulthari):** Ottoman motifs (scimitar-scepter, brass, Janissaries, decrees with a wax seal, aetherium); gold, red and dark brown; deep red-brown background.

| Skill | `id` | What it shows |
|-------|------|---------------|
| Scepter Slash | `scepter_slash` | A curved scimitar with a crowned pommel, a gold arc |
| Decree of Steadfastness | `decree_of_steadfastness` | A rolled decree with a red seal over a shield |
| Janissary Guard | `janissary_guard` | The helmet of a brass Janissary with visible gears |
| Siege Decree | `siege_decree` | A decree with a seal over a closed ring of chains |
| Command | `command` | A pointing scepter with a target mark |
| Royal Treasury | `royal_treasury` | Aetherium coins (cyan crystal in the center) falling |
| Imperial Lineage (passive) | `imperial_lineage` | A family tree with a crown at the top |
| Bronze Cannon | `bronze_cannon` | A bronze siege cannon firing |
| Voice of the Throne (passive) | `voice_of_the_throne` | A throne with golden sound waves |
| Crown of the Five Lands (ultimate) | `crown_of_the_five_lands` | A crown with five colored gems (one per empire) |

#### Class mechanic and resource icons

`gui/hud/<id>.png` — **16×16** (deliver 64×64 or larger), transparent background. Shown on the HUD next to the resource bar.

| `id` | What it shows |
|------|---------------|
| `resolve` | Knight's resource: a small silver-blue shield |
| `essence` | Necromancer's resource: a turquoise flame |
| `mana` | Sorceress's resource: a purple drop with a star inside |
| `energy` | Thief's resource: a red lightning bolt |
| `authority` | King's resource: a small gold crown |
| `stance_shield` / `stance_charge` | Knight's two stances: a shield / a sword pointing forward |
| `soul` | A bound soul of the Necromancer: a turquoise wisp |
| `mark` | A Mark of the Thief on an enemy: a red ember sigil |

### Bearer outfits

Vanilla armor texture format, so the Blockbench or vanilla armor templates work:

| File | Size | Covers |
|------|------|--------|
| `models/armor/<class>_outfit_layer_1.png` | **64×32** | Helmet, chestplate, boots |
| `models/armor/<class>_outfit_layer_2.png` | **64×32** | Leggings |

Follow the visuals in [Clases.md](Clases.md). Capes, hoods and floating objects (Shirin's astrolabe) need extra 3D parts, which come later with GeckoLib; the first version is armor texture only.
