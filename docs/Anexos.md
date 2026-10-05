# Annexes: Presentation, Materials, Merchants and Multiplayer

This document extends the story in [README.md](../README.md), the classes in [Clases.md](Clases.md) and the gear in [Pociones.md](Pociones.md). It covers six topics added to the sprint plan ([Sprints.md](Sprints.md)):

1. A title screen and main menu of our own (the mod is a linear campaign)
2. Splash arts and 3D models (asset list and placeholders; how they get made is still open)
3. Naming for the new materials
4. The new merchant system
5. A co-op bonus for playing with other people
6. Unique items per player when the mod is played online

---

## A1. Linear campaign: title screen and main menu

### Why

SoFE is a **linear story** (Act I → Act V), not a sandbox add-on. From the first screen the player should know they are starting *Sins of the Fallen Empires*, not a vanilla world with extra blocks. So the mod has its own title screen, its own main menu and a default world preset that drops the player straight into Sulthari.

### Title screen (`SoFETitleScreen`)

| Element | Description | Asset |
| --- | --- | --- |
| **Panorama** | Slowly rotating view of Sulthari during the Eclipse Festival: brass domes, tramways and the Great Observatory lit up under the eclipse | 6 cubemap images `panorama_0..5.png` (1024×1024) |
| **Logo** | "Sins of the Fallen Empires", replaces the Minecraft logo | `logo.png` 1024×256, transparent background |
| **Key art (optional)** | Static background shown instead of the panorama if set in the config | `title_keyart.png` 1920×1080 |
| **Main theme** | Title screen music, loops | `music/title_theme.ogg` |
| **Splash text** | The yellow text swaps vanilla jokes for lines from the lore ("The Codex remembers.", "Five empires. Seven sins.") | Override of `assets/minecraft/texts/splashes.txt` |
| **Version and credits** | Bottom corner: mod version and a Credits button | — |

**How it is done technically:** listen for `ScreenEvent.Opening` on the client and, when the new screen is a vanilla `TitleScreen`, replace it with `SoFETitleScreen`. A client config option `replaceTitleScreen = true` turns this off for modpacks that ship their own menu.

### Main menu

| Button | Action |
| --- | --- |
| **Begin the Journey** | Creates a new world with the `sofe:aetheris` preset. The player picks difficulty and a world name. The Night of the Eclipse intro plays, the player chooses their Bearer and spawns in Sulthari. |
| **Continue** | Opens the most recently played world. Hidden when no world exists. |
| **Join an Expedition** | Vanilla multiplayer screen (servers / LAN) with the mod's style. |
| **The Codex** | In-menu lore book: story, bestiary of defeated bosses and a gallery of **unlocked splash arts**. Progress is read from the local worlds. |
| **Options / Mods / Quit** | Vanilla behavior, restyled. The Forge Mods button stays. |

**Other screens with the mod's look:**

- **Pause menu:** adds a *Journal* button (active quests, current act).
- **Icon row (later):** a row of icon buttons under the menu for the Codex, the Codex Map and similar screens (idea in `art/concepts/menu_icons_mockup.jpg`), added in Sprints 7.5 and 8 when those screens exist.
- **World loading screen:** random key art + a lore tip ("Every road to an Archsin is guarded by a Broken Oath").
- **World creation:** the `sofe:aetheris` preset is the default. A server/common config `allowVanillaWorldPresets` (default `true`) controls whether the other presets can still be picked.

> Forge's *early* loading screen (the one before the menu) can only change colors through `fml.toml`. We do not touch it.

---

## A2. Splash arts and 3D models

**Status: production method still open.** These tables are the asset list so the code can reference each file from day one. Every entry starts with a **placeholder** so development is never blocked by art.

### Splash arts (2D illustrations)

| Set | Quantity | Resolution | Used in | Placeholder |
| --- | --- | --- | --- | --- |
| Key art for the title screen | 1 | 1920×1080 | Title screen, CurseForge/Modrinth page | Screenshot of Sulthari |
| Bearer selection cards (emblem and scene; the player's own model is drawn live on top, see [Arte.md](Arte.md#bearer-selection-cards)) | 5 | 512×640 | Bearer selection GUI | Solid color card with the hero name |
| Hero cards (the Bearers drawn as characters, see [Arte.md](Arte.md#hero-cards)) | 5 | 448×640 | Codex, companion screen, epilogues | Selection card |
| Archsins + Nahrazel | 8 | 1920×1080 | Boss intro, Codex (unlocked on defeat) | In-game screenshot of the boss |
| Broken Oaths | 10 | 1920×1080 | Codex (unlocked on defeat) | In-game screenshot |
| Empires | 5 | 1920×1080 | Region loading screen, Codex | Biome screenshot |
| Codex Map (illustrated map of Aetheris, see [Mundo.md](Mundo.md#w3-minimap-and-map)) | 1 | 2048×2048 | Journal map screen | Rendered top-down map |
| Night of the Eclipse (intro) | 1 | 1920×1080 | Intro, Act I | Black screen with text |
| Epilogues | 5 Bearers × 2 (full / unfinished quests) | 1920×1080 | Ending sequence | Black screen with text |
| Region fate slides | ~12 (2–3 per region) | 1920×1080 | Ending sequence ([Jugabilidad.md](Jugabilidad.md#fates-and-epilogues-fallout-style)) | Black screen with text |

### Dialogue portraits

| Set | Quantity | Resolution | Used in | Placeholder |
| --- | --- | --- | --- | --- |
| Bearers (as companions and NPCs; the player's own Bearer is drawn live from their skin) | 5 | 64×64 | Dialogue box | Head of the Bearer's skin |
| Dialogue box styles (one per empire + Void, see [Arte.md](Arte.md#dialogue-box-one-style-per-empire)) | 6 × 4 files | 64×64, 80×80, 96×16, 48×16 | Dialogue box | Plain dark box |
| Archsins, Nahrazel, Brass Sentinel | 9 | 64×64 | Boss intros, temptations | Colored silhouette |
| Broken Oaths | 10 | 64×64 | Dungeon dialogue | Colored silhouette |
| Story NPCs (Ozhan, Council, Laleh, merchants) | ~12 | 64×64 | Dialogue box | Villager-style head |

Files live in `assets/sofe/textures/gui/portrait/<id>.png`. Portraits are still images; there is no lip sync because there are no voices.

Files live in `assets/sofe/textures/gui/splash/<set>/<id>.png`. A PNG that big is heavy inside a jar, so export at the listed size and compress (target < 1 MB each).

### 3D models

| Model | Quantity | Size (blocks) | Animated | Recommended tool |
| --- | --- | --- | --- | --- |
| Archsins | 7 | 3–4 tall | Yes (phases, attacks) | Blockbench + **GeckoLib** |
| Nahrazel (3 phases) and Brass Sentinel | 2 | 3–6 tall | Yes | Blockbench + GeckoLib |
| Broken Oaths | 10 | 2–3 tall | Yes | Blockbench + GeckoLib |
| Bearers (as NPCs and companions) | 5 | Player size | Yes | Blockbench + GeckoLib |
| Bearer outfits (cosmetic armor worn by the player over their own skin) | 5 | Player size | No | Blockbench (armor layer texture 64×32) |
| Summons (Clay Warden, Janissary, Bronze Cannon) | 3 | 1–2 | Yes | Blockbench + GeckoLib |
| Common corrupted mobs | ~15 | 1–2 | Yes | Blockbench (vanilla-style Java model or GeckoLib) |
| Merchant and story NPCs (merchants, Grand Vizier Ozhan, Council) | ~9 | Player size | Idle only | Blockbench |
| Relic weapons | 17 (1 per boss) | Item | No | Blockbench (JSON item model) |
| Crafting stations | 6 | 1–2 blocks | No | Blockbench (JSON block model) |

### Music and sound

| Set | Quantity | Used in | Placeholder |
| --- | --- | --- | --- |
| Main theme | 1 | Title screen | Vanilla menu music |
| Region ambience | 5 + Nether + End | Background music per region | Vanilla music |
| Boss themes | 7 Archsins + Nahrazel + Brass Sentinel | Boss fights | Vanilla boss music |
| Broken Oath theme | 1 shared | Broken Oath fights | Vanilla combat music |
| Skill sounds | ~50 | Every skill | Vanilla sounds |
| UI sounds | ~10 | Menus, Journal, Waystones, level up | Vanilla UI sounds |
| Story stingers | ~10 | Twists, temptations, the bad ending, epilogues | Silence |
| Dialogue text blip | 1–3 | Letters appearing in the dialogue box | Vanilla UI click |

**No voice acting:** characters speak only with text in the dialogue box ([Jugabilidad.md](Jugabilidad.md#dialogue-warcraft-iii-style-text-only)).

Files go in `assets/sofe/sounds/` as OGG Vorbis and are registered in `sounds.json`. Every sound has a subtitle (see [Jugabilidad.md](Jugabilidad.md#g13-accessibility)).

**Recommendation:** use **GeckoLib** as a dependency for bosses, Broken Oaths, Bearers and summons. It supports multi-phase keyframe animation made in Blockbench, which vanilla Java models make very painful. **Placeholders:** vanilla models scaled and tinted (e.g. a scaled zombie for Vorath) until the real model exists.

---

## A3. Naming the new materials

### Convention

- Registry ID: `sofe:<material>_<form>`, all lowercase, `snake_case`.
- Forms follow vanilla: `<material>_ore`, `deepslate_<material>_ore`, `raw_<material>`, `raw_<material>_block`, `<material>_ingot`, `<material>_nugget`, `<material>_block`.
- Every name goes in `en_us.json` and `es_es.json` at the same time.
- Tags for compatibility: `forge:ingots/<material>`, `forge:ores/<material>`, `forge:raw_materials/<material>`.

### Metals (from `docs/Pociones.md`)

| ID base | English | Spanish | Forms |
| --- | --- | --- | --- |
| `sulthari_brass` | Sulthari Brass | Latón Sulthari | ore, raw, ingot, nugget, block |
| `glacial_iron` | Glacial Iron | Hierro Glacial | ore, deepslate ore, raw, ingot, nugget, block |
| `star_lapis` | Star Lapis | Lapislázuli Estelar | ore, gem (no ingot), powder, block |
| `solar_gold` | Solar Gold | Oro Solar | ore, deepslate ore, raw, ingot, nugget, block, powder |
| `orichalcum` | Orichalcum | Oricalco | deepslate ore, raw, ingot, nugget, block |
| `aetherium` | Raw Aetherium | Aeterio en Bruto | deepslate ore, shard, block |
| `black_aetherium` | Black Aetherium | Aeterio Negro | shard only (enemy drop, not mined) |

### Secondary materials (new)

One non-metal material per empire, for armor linings, bows, blueprints and building.

| ID | English | Spanish | Empire | Source | Use |
| --- | --- | --- | --- | --- | --- |
| `dune_leather` | Dune Leather | Cuero de Duna | Sulthari | Desert beasts | Light armor, potion belt |
| `frostpelt` | Frostpelt | Piel de Escarcha | Nordrath | Tundra beasts | Cold-resistant armor |
| `runestone` | Runestone | Piedra Rúnica | Nordrath | Mined near fjords | Building, rune affixes |
| `moonsilk` | Moonsilk | Seda Lunar | Parsivan | Silk Road weaver mobs | Robes, bowstrings |
| `sunreed_papyrus` | Sunreed Papyrus | Papiro de Junco Solar | Khemet | Crafted from river reeds | Blueprints, scrolls |
| `imperial_marble` | Imperial Marble | Mármol Imperial | Aureum | Quarries | Building, station blocks |
| `void_ash` | Void Ash | Ceniza del Vacío | All | Corrupted mobs | Purifier fuel, dark potions |
| `infernal_ember` | Infernal Ember | Brasa Infernal | Nether (Burning Deep) | Basalt delta ore, magma cubes | Fire War Oil, Relic awakening |
| `wailing_soul` | Wailing Soul | Alma Plañidera | Nether (Burning Deep) | Soul sand valleys | Sin Resistance, Necromancer affixes |
| `void_crystal` | Void Crystal | Cristal del Vacío | End (Outer Void) | Outer islands, End cities | Aetherium-tier gear, final Relic awakening |
| `void_ink` | Void Ink | Tinta del Vacío | End (Outer Void) | Crafted at the Alembic | Sealing Quill (required for Nahrazel) |
| `codex_shard` | Codex Shard | Fragmento del Códice | Story | Archsins (1 each, per player) | Story progress, final boss key |

### Sin gems

Each gem has three forms: `rough_<gem>`, `cut_<gem>`, `oath_<gem>` (the perfect one).

| ID | English | Spanish | Sin |
| --- | --- | --- | --- |
| `wrath_ruby` | Ruby of Wrath | Rubí de la Ira | Wrath |
| `lust_amethyst` | Amethyst of Lust | Amatista de la Lujuria | Lust |
| `greed_topaz` | Topaz of Greed | Topacio de la Avaricia | Greed |
| `sloth_moonstone` | Moonstone of Sloth | Piedra Luna de la Pereza | Sloth |
| `gluttony_amber` | Amber of Gluttony | Ámbar de la Gula | Gluttony |
| `envy_emerald` | Emerald of Envy | Esmeralda de la Envidia | Envy |
| `pride_sunstone` | Sunstone of Pride | Piedra Sol de la Soberbia | Pride |

---

## A4. The new merchant system

### Summary

SoFE merchants are **not vanilla villagers**. They are named NPCs (`MerchantNpcEntity`) with a fixed role, a home station and a stock that grows as the story moves forward. They trade in **Dinars** (the `Wallet` capability from `docs/Pociones.md`).

- **Vanilla villagers** do not spawn in the `sofe:aetheris` world. In vanilla worlds they are left untouched.
- The **Money Changer** swaps emeralds for Dinars, so a player coming from vanilla does not lose their emeralds.

### Merchant roster

| NPC | Role | Location | Sells | Buys |
| --- | --- | --- | --- | --- |
| **Ferid** | Alchemist | Sulthari + camps | Potions, flasks, herb seeds | Herbs |
| **Dilara** | Smith | Sulthari + camps | Common gear, repairs | Ingots, gear |
| **Kerem** | Jeweler | Sulthari | Socket services, cut gems | Rough gems |
| **Sister Nilufar** | Purifier | Sulthari | Purification | Black Aetherium |
| **Yusuf** | Quartermaster | Sulthari | Food, torches, basic tools, arrows | Any Common item |
| **Selim** | Money Changer | Sulthari | Dinars for emeralds and back | Emeralds |
| **Zahir the Wanderer** | Traveling caravan | A random liberated camp every 3 days | Rare stock: Blueprints, rough gems, secondary materials | — |

### Rules

**Stock by act.** Each offer declares the minimum act it needs. Dilara sells brass gear in Act I and glacial iron gear once Vorath falls. Offers are defined in JSON:

```json
// data/sofe/merchant_offers/dilara.json
{
  "role": "smith",
  "offers": [
    { "sell": "sofe:brass_scimitar", "price": 40, "min_act": 1, "min_favor": 0, "stock": 3 },
    { "sell": "sofe:glacial_iron_axe", "price": 180, "min_act": 2, "min_favor": 2, "stock": 1 }
  ]
}
```

**Favor per empire.** Each player has a Favor rank (0–5) with each empire, earned through quests, liberating the region and trading. Ranks: *Stranger, Known, Trusted, Honored, Sworn, Exalted*. Higher Favor unlocks offers and gives a 2% discount per rank (10% at Exalted).

**Restock.** At dawn (every Minecraft day). Zahir's rare stock is limited to 1 of each item per player per visit.

**Liberated camps.** When an Archsin falls, the camp of that empire opens with a copy of Ferid and Dilara (reduced stock).

**Selling back.** Price = rarity base × item level factor × 25%. Relics and story items **cannot be sold**. The last 5 items sold can be bought back at the same price (buyback).

**Multiplayer.** Stock and Favor are **per player**: one player buying Zahir's Blueprint does not take it away from the rest. Every transaction is validated on the server (price, Dinars, stock). The client only shows the screen.

---

## A5. Co-op bonus: Pact of the Empires

Playing with other people gives extra rewards, but **solo play is never blocked or made weaker**. Every co-op value lives in the server config.

### Party (Pact)

- Up to **5 players**, like the five Bearers of the story (repeated classes are allowed unless `uniqueBearersPerServer = true`).
- AI companions (hired Bearers) are for solo play: a Pact of 2+ players cannot hire the Bearers its members are playing.
- Made with `/sofe pact invite <player>` or from the Journal GUI.
- The Pact counts as "together" when its members are within **48 blocks** in the same dimension.

### Bonuses while together

| Bonus | Effect | Default value |
| --- | --- | --- |
| **Shared experience** | Kill XP is split among nearby members, plus a bonus per extra member | +10% per extra member |
| **Better loot** | Find bonus for rarity rolls | +5% per ally, capped at +20% |
| **Class Concord** | Each *different* class in the Pact gives everyone a small passive | See table below |
| **Five Pillars** | All 5 classes present | +10% damage and +1 Bearer's Flask charge |
| **Revive** | In boss arenas a player at 0 HP is *downed* for 30 s instead of dying; an ally revives them by crouching next to them for 3 s | On |
| **Kinship Chests** | Dungeon chests that need 2+ players (pressure plates, levers) | Cosmetics, Dinars, rough gems; never required gear |
| **Combo skills** *(stretch goal)* | Skills from two players interact (e.g. Sorceress Frost Lance + Thief Throwing Axe = *Shatter*; King Siege Decree + Necromancer Boat of the Dead) | — |

**Class Concord:**

| Class in the Pact | Everyone gets |
| --- | --- |
| Knight | +5% armor |
| Necromancer | +3% life steal; +5% for a member below 25% health |
| Sorceress | +5% resource regeneration |
| Thief | +3% critical chance |
| King | +5% healing received |

**Boss scaling (extends UC-08).** Each extra player adds +60% HP to the boss, but not more damage. Some attacks start targeting several players at once so the fight does not become trivial.

---

## A6. Unique items per player (online)

### Principle

**"Unique" means unique per player, not per world.** On a server, each person lives their own campaign: if 4 players defeat Vorath together, all 4 get *their own* Vorath Relic and *their own* Codex Shard. No one misses story content because someone else picked it up first.

### Mechanics

**1. Personal loot for bosses and Broken Oaths.** When an Archsin or Broken Oath dies, every player who **took part** (dealt damage, or stayed in the arena for at least 30 s) gets their own roll. The loot does not fall on the floor: a **Reward Coffer** appears in the arena that each player opens with their own contents (similar to vanilla 1.21 vaults, built in-house because we target 1.20.1).

**2. Ownership on the item.** Relics, Legacy pieces and story items carry an owner component:

```text
sofe:owner = { uuid: "<player UUID>", name: "<player name>" }
```

The tooltip shows "Bound to <name>". Stored in the item NBT.

**3. Policy by item type** (configurable on the server):

| Type | Default | Options |
| --- | --- | --- |
| Story items (Bearer's Flask, Codex Shards, Sealing Quill, Void Codex) | `SOULBOUND`: kept on death, cannot be dropped or stored in other people's chests | `SOULBOUND` only |
| Relics | `FREE`: can be traded; as each player gets their own, there is no fighting over them | `FREE`, `PACT_ONLY`, `SOULBOUND` |
| Imperial Legacy | `FREE` | `FREE`, `PACT_ONLY`, `SOULBOUND` |
| Everything else | No owner | — |

**4. One copy equipped.** A player cannot equip two copies of the same Relic. Prevents stacking unique effects with items gifted by friends.

**5. Recovery.** If a story item is lost (lava, void), the Council of Sulthari gives it back. The Flask keeps its charges and the Shards come from the player's capability, not from the item.

### Story progress in multiplayer

- **Per player** (player capability): current act, active quests, Codex Shards, Favor, blueprints learned, unlocked splash arts.
- **Per world:** whether a region has been restored (the landscape heals for everyone once its Archsin falls).
- **Bearer choice:** each player is their own Bearer, with their own intro scene, temptation dialogue and epilogue. With `uniqueBearersPerServer = true` each Bearer can be picked only once. If a player is the King, *their* scenes use the "in disguise" variant with Ozhan leading the Council, while the other players still see Azhar as a fellow Bearer.
- **Personal cutscenes:** story scenes (the fragment speaking with Nahrazel's voice, Prython's offer, the choice in Act V, the moment inside the Codex) play for each player separately; Prython's offer must be refused by each player.
- **Late joiner:** if a player joins after Vorath is already dead, the Burning Citadel has an **Echo of Vorath** shrine: an echo of the fight (same mechanics, no effect on the world) that gives that player their credit, Shard and Relic. This keeps the linear campaign playable for everyone without resetting the world.

> Offline-mode servers (no Mojang login) generate UUIDs from the name, so changing name loses item ownership. Document it in the server README.

---

## Related documents

- Use cases UC-16 to UC-22 (title screen, merchants, Pact, personal loot, echoes, story items, companions): [CasosDeUso.md](CasosDeUso.md)
- Packages and patterns: [Arquitectura.md](Arquitectura.md)
- Sprint tasks: [Sprints.md](Sprints.md)
- Test cases: [Pruebas.md](Pruebas.md)
