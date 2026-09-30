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
