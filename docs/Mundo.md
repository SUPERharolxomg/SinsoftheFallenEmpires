# The World: Map, Progression Locks, Minimap and Resources

Minecraft is an open world, but SoFE tells a linear story with fixed places. This document explains how the two fit together:

1. **One world with a fixed map**: every empire is always in the same place.
2. **Progression locks**: regions, cities and dungeons stay sealed until the player has completed the right act or mission.
3. **Minimap**: integration with JourneyMap and Xaero's, plus a small built-in map for players without map mods.
4. **Resource distribution**: which material appears where (coordinates, height, frequency), so tools unlock in story order.
5. **The Nether and the End**: when they open and what they are needed for.
6. **The empires, built from scratch**: cities, camps and ruins are original builds with their own building blocks, not vanilla villages.

**Guiding rule: open inside, sealed across.** Inside every region the player has unlocked, it is normal Minecraft: mine, build, farm, explore. What they cannot do is cross into a region, city or dungeon the story has not reached yet.

---

## W1. One world with a fixed map

### Decision

The five empires are **regions of a single world** (the `sofe:aetheris` preset), not separate dimensions.

| Option | Pros | Cons |
|--------|------|------|
| **One world, fixed regions** (chosen) | Real open world; map mods show one continuous map; the story's arc (north, east, west, home) is a real journey; waypoints and coordinates are the same in every world | Needs region borders and enforcement |
| One dimension per empire | Easy isolation | Portals break the sense of travel; map mods show 5 disconnected maps; five times the worldgen work |

The terrain is still procedural (every seed gives different hills, caves and rivers), but **the layout is fixed**: a custom biome source decides the region from the coordinates, and main structures (cities, dungeons, arenas) are placed at fixed positions.

Small extra dimensions are used only where the story needs them:

| Dimension | Used for |
|-----------|----------|
| ~~`sofe:inverted_throne`~~ | Not needed: the Inverted Throne is built beneath Sulthari, reached by a winding stair |
| ~~`sofe:codex_interior`~~ | Not needed: Nahrazel's third phase turns his arena to the Codex's pages, and the ending inside the Codex is a scene (`EndingScreen`) |
| `sofe:echo` | Instanced Echo fights for late joiners (one instance per player) |

**The Nether and the End are part of the campaign.** They open with the story, follow the same locks and give materials the player needs (see [W5](#w5-the-nether-and-the-end)).

### Map of Aetheris

North is −Z, as in Minecraft. The world is **25,000 × 25,000 blocks**: the regions fill ±12,000 and the ocean runs on to ±12,500. The empires are large on purpose, with room for their dungeons, camps and zones of their own; Waystones make the distances bearable.

```text
                                   z = -12000
   ┌───────────────────┬─────────────────────────────────┬───────────────────┐
   │  AUREUM (north-   │                                 │  PARSIVAN (north- │
   │  west): the Shadow│        NORDRATH CLANS           │  east): Enchanted │
   │  Throne ruins     │        (Act II)                 │  Gardens          │
   │                   │        Wrath · Gluttony (below) │                   │
   │      x = -6000    │                                 │    x = 6000       │
   ├───────────────────┴──────────┬──────────┬───────────┴───────────────────┤ z = -1500
   │  AUREUM REPUBLIC (Act IV)    │ SULTHARI │  PARSIVAN COURT (Act III)     │
x =│  Greed · Envy                │ (Act I)  │  Lust                         │ x =
-12000 Treasury, Market, Vaults  ├────┬─────┴───────────────────────────────┤ 12000
   │                              │    │ z = 1500                          │
   ├──────────────────────────────┤ S  │  KHEMET ASCENDANCY (Act III)      │
   │  AUREUM (south-west): coast, │ E  │  Sloth                            │
   │  quarries, the Colosseum     │ A  │  Catacombs, Stagnant Marsh        │
   │                              │    │                                   │
   └──────────────────────────────┴────┴───────────────────────────────────┘
                                   z = 12000      (ocean beyond, to ±12,500)
```

Regions only touch at their borders; the Seal Veil runs along every shared border. A region can be made of several rectangles (Parsivan, Aureum).

| Region | X range | Z range | Main biomes | Opens at |
|--------|---------|---------|-------------|----------|
| **Sulthari** (city + Ashen Wastes) | −1,500 → 1,500 | −1,500 → 1,500 | Desert plateau, mesa, oasis, Ashen Wastes | Start (Act I) |
| **Nordrath** | −6,000 → 6,000 | −12,000 → −1,500 | Tundra, fjords, ice caves, volcanic forges | Act II |
| **Parsivan** | 1,500 → 12,000 and 6,000 → 12,000 | −1,500 → 1,500 and −12,000 → −1,500 | Hanging gardens, mountain passes, Silk Road steppe | Act III |
| **Khemet** | 800 → 12,000 | 1,500 → 12,000 | River valley, dunes, marsh | Act III (Catacombs after Luxara) |
| **Aureum** | −12,000 → −1,500, −12,000 → −6,000 and −12,000 → −1,500 | −1,500 → 3,500, −12,000 → −1,500 and 3,500 → 12,000 | Marble hills, quarries, coast | Act IV |
| **Southern Sea** | −1,500 → 800 | 1,500 → 12,000 | Ocean between Aureum and Khemet | Never |
| **Ocean** | Everything else inside the border | | Deep ocean, islands | Never (see W2) |

**Enemy levels.** Each region has an enemy level range, which sets mob stats and the item level of their loot ([Pociones.md](Pociones.md#gear-progression-by-act)):

| Region | Enemy level |
|--------|-------------|
| Sulthari and Ashen Wastes | 1–5 (27–30 during Act V) |
| Nordrath | 5–12 (Nordrath Caverns: 20–24) |
| Parsivan | 12–16 |
| Khemet | 16–20 |
| Aureum | 20–27 |
| Nether (Burning Deep) | Same as the overworld region it maps to, +2 |
| End (Outer Void) | 27–30 |

**Biome zones.** Inside a region, round zones of other biomes give it variety; like the layout they are saved with the world. Nordrath: tundra, two ice fields to the west and east, and volcanic forges around the Burning Citadel and the Nordrath Forge.

**Fixed locations.** Sulthari city at (0, 0). Nordrath: Skarnhold (0, −2,400), Nordrath Forge (−1,200, −3,000), Nordrath Arena (1,200, −3,000), Burning Citadel (0, −4,500), the Caverns' entrance (−600, −2,400) with the Caverns and the Feast Halls below it. Parsivan: Baths (3,600, −500), Silk Road caravanserai (5,200, 700), Enchanted Gardens (9,000, −5,000). Khemet: Catacombs (3,200, 4,000), Stagnant Marsh (8,000, 8,500). Aureum: Treasury (−3,600, −500), Market (−3,600, 1,500), Golden Vaults (−8,000, 500), Colosseum (−6,000, 7,000), Shadow Throne (−9,000, −8,000). Each boss waits in its lair (`data/sofe/boss_lairs.json`). The Celestial Spire rises above Sulthari only in Act V. The region bounds are part of the `sofe:aetheris` world preset (`data/sofe/worldgen/world_preset/aetheris.json`, generated from `RegionMap.defaultLayout()`) and are **saved inside each world** when it is created, so a later change to the layout only affects new worlds. Structure positions live in `data/sofe/structure_positions.json` (spawn, story structures and their protected zones, NPCs and Waystones), so they can change without code; a journey copies its zones when it is created.

---

## W2. Progression locks

Three layers: the first is what the player sees, the second guarantees nobody gets through, the third protects the places of the story.

### Layer 1 — The Seal Veil (region borders)

Every region border is a wall of **Seal Veil** (`sofe:seal_veil`), a translucent wall of aetherium from the bottom of the world (Y −64) up to the build limit (Y 320). It fits the lore: Sulthari survived by sealing itself behind aetherium walls.

- **Unbreakable:** hardness −1 and maximum blast resistance, like bedrock. Pistons cannot move it.
- **Per-player passage:** the block's collision depends on who touches it. If the player has unlocked the region on the other side, the Veil has no collision for them; for everyone else it is solid. (Forge gives the entity in `getCollisionShape(..., CollisionContext)`, and the player's progress is synced to the client so movement does not stutter.)
- **Per-player color:** locked is dark red-violet, unlocked is a faint shimmer (client tint based on the local player's progress).
- **Mobs never pass**, so corrupted creatures stay out of Sulthari.
- Touching a locked Veil shows: *"The seal of Nordrath holds. (Unlocks in Act II)"*.

### Layer 2 — Region enforcement (the guarantee)

A wall alone can be bypassed: flying with elytra above the build limit, ender pearls, boats along the coast. So the **server** checks, once per second, which region each player is in (`RegionMap.regionAt(x, z)`):

- A player standing in a locked region is sent back to their last safe position and gets a short *Void Rebuke* effect (slowness + message). No damage, no item loss.
- Ender pearls and chorus fruit whose destination is in a locked region are cancelled (`EntityTeleportEvent`).
- The ocean is always blocked beyond 300 blocks from the coast, so no one sails around the Veil.
- **Creative and spectator** players, and operators with `opsBypass = true`, are never blocked (map building and testing).

### Layer 3 — Protected places (cities, camps, dungeons, arenas)

Story structures are **protected zones**. When a structure is generated, its bounding box is stored in `ProtectedZoneData` (world SavedData) with a rule set:

| Zone | Break blocks | Place blocks | Take what it holds | Unlocks |
|------|--------------|--------------|--------------------|---------|
| Cities (Sulthari, Skarnhold, the capitals) | No | No | No; doors, shops, stations and the Personal Vault work; vaults and palace locked by quest | Story missions |
| **Bearer's Homestead** (plot outside the walls) | Yes | Yes | Yes | Start |
| Liberated camp | No | No | No; its merchants and Vault work | When its Archsin falls |
| Dungeon | No (except marked breakable walls) | No | No; Reward Coffers work, doors by progress | Previous Broken Oath or quest step |
| Boss arena | No | No | No | All Broken Oaths of that Archsin |

Protection covers every way of changing a place: player breaking/placing, explosions (only the protected blocks are removed from the explosion), pistons pushing in from outside, fire spread, fluids, and mob griefing (endermen, Void creatures). **Nothing can be taken** from a place either (`ZoneAction.TAKE`): its chests, barrels, furnaces and the drawers of the furniture mods do not open, the plants stay in their pots, the books on their lecterns, the berries on their vines, and its armor stands (the statues), item frames, paintings, boats and carts cannot be struck, stripped, shot or blown up. Only SoFE's own blocks (stations, the Personal Vault, Reward Coffers, Waystones), doors, gates, buttons, beds and seats can be used.

**No one is let off** inside a protected place: not an operator, not a player in creative (unlike the Seal Veil, below, which creative players and `opsBypass` operators may cross for testing). To change a place while building the mod, turn `protectZones` off in the server config; commands such as `/setblock` and `/fill` are not stopped.

**Sealed Gates.** Dungeon entrances, city vaults and arena doors use `sofe:sealed_gate`: the same per-player passage as the Veil, but on a single door with its own condition. Right-clicking it shows what is missing (*"Defeat Luxara to open the Catacombs of Khemet"*).

### Conditions (data-driven)

Every lock points to a condition in `data/sofe/conditions/`, so progression can be tuned without code:

```json
// data/sofe/conditions/khemet_catacombs.json
{
  "type": "all_of",
  "conditions": [
    { "type": "act_reached", "act": 3 },
    { "type": "boss_defeated", "boss": "sofe:luxara" }
  ]
}
```

Types: `act_reached`, `boss_defeated`, `quest_step`, `item_owned`, `fate_is` (a region's fate, see [Jugabilidad.md](Jugabilidad.md#fates-and-epilogues-fallout-style)), `all_of`, `any_of`, `not`.

**Example of why two layers are needed:** the Nordrath region opens in Act II, but the **Nordrath Caverns** (Fenrath and Gularth) are only for Act IV. The region is open, the dungeon gate is not.

### Multiplayer

Locks follow each player's own progress (it is already per player in `StoryProgress`). The server can choose:

| `gateMode` | Behavior |
|------------|----------|
| `PER_PLAYER` (default) | Each player passes only what they have unlocked. Friends who are behind catch up with the Echo shrines. |
| `PACT_ESCORT` | A player may cross a Veil or Gate if a Pact member who has unlocked it is within 16 blocks. For groups of friends who want to play together from any point. |

---

## W3. Minimap and map

### Recommendation

Do **not** build a full minimap: good ones already exist and players already use them. Instead:

1. **JourneyMap: full integration** (optional dependency). It has an official plugin API (waypoints, polygon overlays, markers). A `SoFEJourneyMapPlugin` would:
   - draw region borders and names;
   - shade locked regions;
   - add waypoints for the current objective, discovered dungeon entrances, liberated camps and merchants;
   - remove waypoints when the objective is done.
2. **Xaero's Minimap / World Map: compatible, basic support.** It maps the world the player explores with no work from us. Its integration API is more limited than JourneyMap's; when we implement it, we check what it allows (at least objective waypoints). If there is nothing stable, it keeps working without integration.
3. **Built-in, always available** (no map mod needed):
   - **Region title** when crossing into a region: a small *"Welcome to"* line and the region name in large letters (*"Nordrath Clans"*), later with its status (*"— Liberated"*). It also shows when the player joins, for the region they are in.
   - **Quest Compass** on the HUD: an arrow and distance to the current objective.
   - **Codex Map** (screen from the Journal): an illustrated map of Aetheris with regions, lock status and objective markers. As the layout is fixed, it can be a hand-drawn piece of art (add it to the asset list in [Anexos.md](Anexos.md#a2-splash-arts-and-3d-models)).

**Locked regions stay hidden naturally:** map mods only draw chunks the player has explored, and the player cannot enter a locked region. For fair play on servers, both map mods let the server turn off entity radar and cave mode; the server README explains how.

---

## W4. Resource distribution

### Tool chain

Every region's ore needs a pickaxe made from an earlier region's material, so gear follows the story:

```text
Stone ─► Sulthari Brass ─► Glacial Iron ─► Solar Gold ─► Orichalcum ─► Aetherium
 (Act I)   (Act I)           (Act II)        (Act III)      (Act IV)      (Act V)
                  └─► Star Lapis (Act III, needs brass/iron tier; a gem, not a tool tier)
```

Custom tool tiers are registered with Forge's `TierSortingRegistry`:

| Tier | Level | Equivalent | Mines |
|------|-------|------------|-------|
| Stone | 1 | Vanilla stone | Sulthari Brass |
| **Sulthari Brass** | 2 | Vanilla iron | Glacial Iron, Star Lapis |
| **Glacial Iron** | 3 | Between iron and diamond | Solar Gold |
| Vanilla diamond | 3+ | — | Solar Gold (not Orichalcum) |
| Vanilla netherite | 4 | Same as Solar Gold | Orichalcum (only reachable in Act IV anyway) |
| **Solar Gold** | 4 | Above diamond | Orichalcum |
| **Orichalcum** | 5 | Top craftable | Raw Aetherium |

**Diamond is placed below Solar Gold** so vanilla diamond tools cannot skip Aureum. **Aetherium-tier tools** also need Void Crystal from the End (see W5). Region locks already stop anyone from mining an ore early; the tool chain keeps gear moving in step with the story.

### Mod ores

Mod ores generate **only in their region's biomes** (biome tags `sofe:is_nordrath`, `sofe:is_parsivan`, etc.). Since biomes follow the coordinates in W1, ores follow them too.

| Ore | Region (X / Z) | Biomes | Height (Y) | Veins per chunk | Vein size | Notes |
|-----|----------------|--------|------------|-----------------|-----------|-------|
| **Sulthari Brass** | Sulthari (±1,500) | Plateau, mesa | 40 → 120 | 10 | 6–9 | Also in cliff faces; start material |
| **Glacial Iron** | Nordrath (z −1,500 → −12,000) | Tundra, ice caves | −16 → 48 | 6 | 5–8 | Deepslate form below Y 0; double in ice caves |
| **Star Lapis** | Parsivan (x 1,500 → 12,000) | Mountains, passes | 80 → 200 | 4 | 3–5 | Hard to spot by day, sparkles at night |
| **Solar Gold** | Khemet (x 800 → 12,000, z 1,500 → 12,000) | Dunes, river valley | −32 → 32 | 3 | 4–6 | Extra veins inside tombs |
| **Orichalcum** | Aureum (x −1,500 → −12,000) | Marble hills, quarries | −64 → −16 | 2 | 3–5 | Deep only |
| **Raw Aetherium** | All unlocked regions | Near Void Rifts | −64 → −40 | 1 rift per ~8 chunks | 2–4 around the rift | Needs an Orichalcum pickaxe. Before Act V, aetherium comes from purifying Black Aetherium |

**Vanilla ores** (coal, copper, iron, gold, redstone, lapis, diamond) generate as usual in every region, so the early game still feels like Minecraft.

### Sin gems by region

The 3% rough gem chance when mining a mod ore gives the gem of that region's sin:

| Region | Gem |
|--------|-----|
| Nordrath | Ruby of Wrath; Amber of Gluttony (below Y 0) |
| Parsivan | Amethyst of Lust |
| Khemet | Moonstone of Sloth |
| Aureum | Topaz of Greed; Emerald of Envy (in ruins) |
| Sulthari | Sunstone of Pride (Act V only, near Void Rifts) |

### Secondary materials and herbs

| Material / herb | Region | Where | Source |
|-----------------|--------|-------|--------|
| Dune Leather | Sulthari | Ashen Wastes, desert | Desert beasts |
| Frostpelt | Nordrath | Tundra | Tundra beasts |
| Runestone | Nordrath | Fjord outcrops, Y 50 → 90 | Mined (large blobs) |
| Moonsilk | Parsivan | Silk Road | Weaver mobs |
| Sunreed Papyrus | Khemet | River banks | Reed plant → crafted |
| Imperial Marble | Aureum | Surface, Y 50 → 100 | Mined (large blobs) |
| Void Ash | All | Anywhere corrupted | Corrupted mobs |
| Mountain sage | Sulthari | Plateau edges | Wild plant, can be planted |
| Pomegranate | Parsivan | Gardens | Wild tree, can be planted |
| Lotus | Khemet | River and marsh | Water plant, can be planted |

Seeds and saplings can be taken home and **planted on the Bearer's Homestead**, so the player's farm grows with the story.

### Configuration

Every value in these tables lives in worldgen JSON (`data/sofe/worldgen/placed_feature/*.json` and biome modifiers), so modpacks and balance passes can change them without code.

---

## W5. The Nether and the End

Both dimensions are used and both are needed to get certain materials. They open with the story, so they are not a shortcut.

### The Nether — the Burning Deep

**Lore.** The volcanic forges of Nordrath burn with a fire that comes from below. When Vorath falls, the crack under the Burning Citadel stays open: it leads to the **Burning Deep** (the Nether).

- **Opens:** after defeating Vorath (Act II). Before that, Nether portals cannot be lit: *"The Deep does not answer yet."* Condition: `boss_defeated: sofe:vorath`.
- **Same locks inside:** every Nether position maps to the overworld region at 8× its coordinates, as in vanilla. The Seal Veil also stands at those scaled borders, and `RegionEnforcer` checks the Nether too.
- **Portals:** a portal whose exit would land in a locked overworld region does not link and shows which act unlocks it. The Nether keeps its vanilla reward (travel 8× faster), but only between regions the player has already unlocked.

| Material | Where | Used for |
|----------|-------|----------|
| Blaze rods / powder | Nether fortresses | Vanilla brewing; Eyes of Ender for the Void Gate |
| Nether wart | Nether fortresses | Vanilla brewing; base of the Sin Resistance potions |
| Nether quartz | Everywhere | Clockwork parts: Janissary Guard upgrades, brass automatons |
| **Infernal Ember** (mod) | Ore in basalt deltas; magma cube drop | Fire War Oil; catalyst for awakening Relics after Act II |
| **Wailing Soul** (mod) | Soul sand valleys; ghast and soul mob drop | Necromancer affixes, Sin Resistance potions |
| Ancient debris / netherite | Vanilla | Vanilla netherite gear: strong, but no affixes or sockets |

### The End — the Outer Void

**Lore.** The Void Codex is a prison with two sides. The **Outer Void** (the End) is the side of the seal that faces away from Aetheris, where the torn pages of the Codex drift.

- **Access:** vanilla strongholds do not generate in `sofe:aetheris`. The only End portal is the **Void Gate** beneath the Great Observatory of Sulthari.
- **Opens:** after defeating Envyris (end of Act IV), by placing 12 Eyes of Ender in the Gate. The Eyes need blaze powder from the Nether, so the Nether comes first. Condition: `all_of [boss_defeated: sofe:envyris, item_owned: 12 × minecraft:ender_eye]`.
- **Ender Dragon:** stays as an optional vanilla boss. Killing it opens the outer islands and End cities as usual.
- **Elytra:** usable inside unlocked regions; flying over a Veil is still caught by `RegionEnforcer`.

| Material | Where | Used for |
|----------|-------|----------|
| Ender pearls, chorus fruit | Vanilla | Eyes of Ender; Void Ink |
| **Void Crystal** (mod) | End stone veins on the outer islands (Y 20 → 60); End city chests | Aetherium-tier tools and armor (with Orichalcum and Raw Aetherium); final Relic awakening |
| **Void Ink** (mod) | Alembic: chorus fruit + Void Crystal + ink sac | The Sealing Quill (see below) |
| Dragon's breath | Ender Dragon | Vanilla lingering potions |
| Elytra, shulker shells | End cities | Travel; shulker boxes work as usual |

**The Sealing Quill (Act V, required).** To rewrite the seal from inside the Codex in Nahrazel's phase 3, the Bearer needs the **Sealing Quill**, forged with Void Ink. It is a main quest step before the Inverted Throne, so the End is a required part of the story. Condition for the Inverted Throne gate: `all_of [boss_defeated: sofe:prython, item_owned: sofe:sealing_quill]`. The Quill is a story item (soulbound).

### Order in the campaign

```text
Act II   Vorath falls ──► the Burning Deep (Nether) opens
Act III  Nether materials: War Oil, Sin Resistance, Relic awakening
Act IV   Envyris falls ──► the Void Gate can be opened (12 Eyes of Ender)
Act V    End materials: Aetherium gear, Void Ink ──► Sealing Quill ──► Nahrazel
```

---

## W6. The empires, built from scratch

**Decided: no vanilla villages.** Vanilla villages look like Minecraft, not like five fallen empires, so they do not generate in a journey (the SoFE biomes are in none of the vanilla structure tags). Every city, camp, dungeon and ruin is an **original build**, made with building blocks that match each empire's story, palette and state (living Sulthari vs. corrupted ruins).

### How the builds are made

1. **Build in game:** in a dev world, with the SoFE building blocks and vanilla blocks where they fit.
2. **Save with Structure Blocks** as `.nbt` files in `data/sofe/structures/<empire>/<piece>.nbt` (a city is split into pieces: gate, market, palace, houses, walls).
3. **Assemble with jigsaw pools** (`data/sofe/worldgen/template_pool/`), so a city has a fixed layout for the story buildings and some variety in the houses around them.
4. **Place at fixed coordinates** from `data/sofe/structure_positions.json` (see W1), and register the bounds as protected zones (W2, layer 3).

Big landmarks (the Great Observatory, the Burning Citadel, the Celestial Spire) are single hand-made builds; ordinary houses and ruins come from pools so the map does not feel copied.

### Flags and banners

Each empire flies its own colours, as Skarnhold flies the clans' red knot (`EmpireFlags`): Supplementaries flags and vanilla banners in the empire's colour with its emblem in banner patterns, flagpoles with two flags round each capital's plaza and on the towers, and **great banners** (the clans' `ClanBanner` with the empire's cloth, three blocks wide and six long, moving in the wind) on the gates and the palaces:

| Empire | Colours | Great banner (`scripts/make_empire_banners.py`) | Flag pattern |
|--------|---------|------------------------------------------------|--------------|
| Parsivan | Violet, turquoise, gold | A turquoise eight-pointed star under a white crescent | Violet, turquoise disk, white flower, gold border |
| Khemet | Blue, gold | A golden ankh under the winged sun disk, a band of zigzags | Blue, gold sun over gold teeth, cyan border |
| Aureum | Royal blue, gold | A golden crown in a laurel wreath, SPQA | Blue, gold lozenge with a blue flower, gold border |

### The capitals

Parsivan, Khemet and Aureum each have a capital built in code (`CapitalCity` and one class per city: `Isfaran`, `Neferet`, `Aurelion`), not from `.nbt` pieces: a square terrace at the middle height of the land under it (never below sea level + 3), walls with four gates (the south gate in the empire's own style), two crossing avenues and a central plaza, the empire's landmarks with furnished interiors, furnished houses on 13-block plots and roads out. Aureum, the last empire before Sulthari's siege, has the largest: 320 × 320 with its own Colosseum on a hill. Over the 14 blocks outside the walls the ground slopes from the terrace back to the land around, so a capital on a hill or by the sea has no trench or cliff at its gates.

| Capital | Empire | Position | Landmarks |
|---------|--------|----------|-----------|
| Isfaran | Parsivan | 7200, -400 (224 × 224) | Palace with a violet dome, an iwan and four minarets, glowing pool, hanging gardens, domed kiosks, white gate with spires |
| Neferet | Khemet | 6800, 3200 (256 × 256) | Temple behind pylons with golden ankhs, sphinxes, the Great Pyramid, the rock-cut royal tomb, river with quays and boats, pylon gate |
| Aurelion | Aureum | -5400, 2800 (320 × 320) | Byzantine Imperial Palace under a golden dome, Colosseum on a hill, Pantheon, basilicas, temples, triumphal arch, aqueduct, gold-crested gatehouse, fields |

### Building blocks per empire

Each empire gets its own set of building blocks, in two states: **intact** (for Sulthari, liberated camps and restored areas) and **corrupted** (cracked, blackened, with veins of black aetherium) for the ruins. Liberating a region swaps corrupted blocks near the camp for intact ones, which is how "the landscape heals".

| Empire | Main building blocks | Details and props | Corrupted variant |
|--------|----------------------|-------------------|-------------------|
| **Sulthari** | Sandstone bricks, brass plating and trims | Blue-white glazed tiles, brass domes, clockwork gears, aetherium lamps, brass tramway rails | (only in Act V, when Prython attacks) |
| **Nordrath** | Runestone bricks, dark timber | Carved beams with dragon heads, iron braziers, fur rugs, volcanic basalt forges | Scorched runestone, burning timber |
| **Parsivan** | Turquoise glazed tiles, white plaster | Lapis mosaics, silver lattice screens, Moonsilk carpets and awnings, garden fountains | Faded tiles, overgrown vines, illusion-glass |
| **Khemet** | Carved sandstone, painted limestone | Gold hieroglyph blocks, obelisk pieces, Sunreed thatch, canopic urns, jackal statues | Sunken sandstone, marsh mud, cracked urns |
| **Aureum** | Imperial Marble (polished, bricks, pillars) | Gold mosaics, bronze statues, aqueduct arches, colosseum seating, coin piles | Cracked marble, black-gold veins |

**Naming** follows A3 in [Anexos.md](Anexos.md#a3-naming-the-new-materials): `sofe:<empire>_<block>` plus the vanilla shape suffixes (`_stairs`, `_slab`, `_wall`), and `corrupted_` in front for the ruined version (`sofe:aureum_marble_bricks`, `sofe:corrupted_aureum_marble_bricks`). Each set is added in the sprint of its empire, and like every other block it has en/es names, placeholder textures and data-generated models, loot and tags.

**Players can use them too:** the blocks are craftable from the empire's materials (Imperial Marble, Runestone, Sulthari Brass...), so players can build with them on their Homestead.
