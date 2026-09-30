# Development Sprints

Sprint plan for *Sins of the Fallen Empires*. Story in [README.md](../README.md); design in [Clases.md](Clases.md), [Pociones.md](Pociones.md) and [Anexos.md](Anexos.md); technical design in [Arquitectura.md](Arquitectura.md), [CasosDeUso.md](CasosDeUso.md) and [Pruebas.md](Pruebas.md).

Total: **24 weeks** plus a setup week (Sprint 0). Sprint 5.5 comes from [Pociones.md](Pociones.md) and Sprint 7.5 from [Anexos.md](Anexos.md). Tasks marked *(Annex)* come from [Anexos.md](Anexos.md), *(World)* from [Mundo.md](Mundo.md) and *(Rules)* from [Jugabilidad.md](Jugabilidad.md).

## Summary

| Sprint | Weeks | Focus |
|--------|-------|-------|
| 0 | Week 0 | Project setup: Forge 1.20.1 MDK, build, CI, repository files |
| 1 | 1-2 | Foundation, Bearer selection, title screen and main menu skeleton |
| 2 | 3-4 | Skill system, Sorceress first skills |
| 3 | 5-6 | Combat & leveling |
| 4 | 7-8 | Act I: Night of the Eclipse, Brass Sentinel, merchant NPCs |
| 5 | 9-10 | Act II: Kaleth, Serath and Vorath |
| 5.5 | 11-12 | Loot, economy, per-player loot |
| 6 | 13-14 | Knight, Necromancer, Thief, King; companions |
| 7 | 15-18 | Acts III–IV: empires, Archsins, Broken Oaths, materials, Favor |
| 7.5 | 19-20 | Multiplayer & co-op |
| 8 | 21-24 | Act V: Solrath, Prython, Nahrazel, epilogues, final art |

## Sprint 0 — Project Setup (Week 0)
- [x] Forge 1.20.1 MDK with the Gradle wrapper and a Java 17 toolchain
- [x] Mod metadata: mod ID `sofe`, package `com.sofe`, `mods.toml`, MIT license
- [x] Main mod class (`SoFEMod.java`) that loads in a GameTest server
- [x] Try the mod in the dev client (`./gradlew runClient`; reaches the SoFE title screen)
- [x] Dependencies declared: GeckoLib, Curios (JourneyMap API later, optional)
- [x] JUnit 5 for unit tests of pure game logic
- [x] `LICENSE`, `CONTRIBUTING.md`, `.gitignore`, `.gitattributes`
- [x] Issue templates (bug, feature) and pull request template
- [x] GitHub Actions: build and tests on every push and pull request (first runs passed on PR #1 and PR #2)

## Sprint 1 — Foundation (Weeks 1-2)
- [x] `PlayerClass` enum (Knight, Necromancer, Sorceress, Thief, King) with `ResourceType` and temptation `Sin`
- [x] `ClassSelectScreen` to choose one of the five Bearers (card + the player's live model), with the chosen class saved per player and synced to the client
- [x] `lang/en_us.json` and `lang/es_es.json` set up; no player-facing text in code
- [x] Set up entity registry
- [x] Create basic config system (client and server; common is added with its first option) with the in-game config screen
- [x] *(Annex)* `SoFETitleScreen` skeleton replacing the vanilla title screen, with `replaceTitleScreen` config
- [x] *(Annex)* Main menu buttons: Begin the Journey, Continue, Join an Expedition, Options, Mods, Quit
- [x] *(Annex)* `sofe:aetheris` world preset as the default for new worlds; vanilla strongholds disabled
- [x] *(World)* `Region` enum and `RegionMap` with the default layout
- [x] *(World)* `Condition` system (`act_reached`, `boss_defeated`, `quest_step`, `all_of`, `any_of`, `not`)
- [x] *(Annex)* Placeholder logo (text), panorama (vanilla) and lore splash texts
- [x] *(Annex)* `MaterialRegistry` with the naming convention `sofe:<material>_<form>` (en_us + es_es)

## Sprint 2 — Skill System (Weeks 3-4)
- [x] Implement the Skill interface (effect only; cost, cooldown and checks are shared in `SkillCaster`) and the catalog of the 50 skills
- [x] Cooldowns per player and per skill (`CooldownTracker`)
- [x] `SkillDataManager`: costs, cooldowns, damage and resource rules from `data/sofe/skills/*.json` (reloads with /reload)
- [x] Resource system for the 5 resources (Resolve, Essence, Mana, Energy, Authority): maximum, start value and regeneration per class; class-specific gains (blocking, kills, healing) come with each class in Sprint 6
- [x] Minimal HUD: the class resource bar with its icon, the Sorceress runes and the Combat Bar slots with cooldowns (moved up from Sprint 3 so skills can be tested)
- [x] Sorceress level-1 skills (Ember Verse, Frost Lance, Wandering Spark) and the rune / constellation system (5 constellations)
- [x] *(Rules)* Combat Bar keybinding system (Left Alt + 1-6), Flask key reserved on H, all rebindable; the potion belt keys come with the belt in Sprint 5.5
- [x] Implement skill visual effects (particles and vanilla sounds)

## Sprint 3 — Combat & Leveling (Weeks 5-6)
- [x] Implement XP and leveling system (levels 1-30, XP from kills by mob level, values in `data/sofe/leveling.json`)
- [x] *(Rules)* Mob levels for every hostile mob (vanilla, SoFE and other mods): region range and act floor, health/damage/armor scaling from `data/sofe/mob_scaling.json` ([Jugabilidad.md](Jugabilidad.md#difficulty-rises-with-each-act)). The act floor reads the player's act, which is Act I for everyone until `StoryProgress` arrives in Sprint 4

- The remaining Sprint 3 tasks moved to Sprint 4 (part 4.1) after PR #5 was merged.

## Sprint 4 — Act I: The Night of the Eclipse (Weeks 7-8)
Split into five parts, one pull request each.

### Part 4.1 — Character: ranks, skill tree, attributes
- [x] *(from Sprint 3)* Skill points (1 per level) and ranks 1-5 with the unlock and level rules ([Clases.md](Clases.md#skill-points-and-ranks-diablo-ii-style))
- [x] *(from Sprint 3)* Per-rank growth of damage, duration, cost and cooldown from `per_rank` in the class files
- [x] *(from Sprint 3)* Diablo II style SkillTreeScreen (key K): grid by unlock level, arrows, ranks, "Points spent", Active and Passive tabs
- [x] *(from Sprint 3)* Combat Bar slots filled from the skills the player has learned (each new active takes the first free slot, the ultimate slot 6; moving skills between slots comes later)
- [x] *(from Sprint 3)* Attributes: 6 attributes, 5 points per level, starting values per class, effects from `data/sofe/attributes.json`
- [x] *(from Sprint 3)* Diablo II style character sheet screen (key I) (spend attribute points, see the resulting values)
- [x] *(from Sprint 3)* Implement combat damage modifiers per class (Strength and Intellect, with each class starting 10 points ahead in its primary attribute; critical hits and dodge from Agility)

### Part 4.2 — Story: progress, quests, dialogue
- [x] *(Rules)* Quest system (`data/sofe/quests/`) and Journal (key U; quests by type, steps with counts, fates, tracking for the Quest Compass)
- [x] *(Rules)* Warcraft III style dialogue box (portrait, name, letter-by-letter text, answers), cinematic letterbox mode, `data/sofe/dialogue/`; text only, no voices. Uses the wide bars per style ([Arte.md](Arte.md#dialogue-box-one-style-per-empire)); the square box is the fallback. In a conversation the player keeps walking; walking away ends it
- [x] *(Rules)* Region fates in `StoryProgress` and the `fate_is` condition (add it to `ConditionParser`); first Sulthari fate side quest ("Embers of the Eclipse": the Low Bazaar or the Observatory)
- [x] *(Annex)* `StoryProgress` player capability (act, quests, credit per boss); mob levels now read the real act
- [x] *(Annex)* King variant: the Council led by Ozhan when the player is Azhar (`class_is` condition on dialogue lines)

### Part 4.3 — World: locks and protected places
- [x] *(World)* `AetherisBiomeSource` with the fixed layout; Sulthari and Ashen Wastes biomes (the Wastes are the outer ring of Sulthari; saved with the world, optional so older worlds keep loading)
- [x] *(World)* Seal Veil on every region border (per-player collision and tint)
- [x] *(World)* `RegionEnforcer` (flight, pearls, coast) and `opsBypass`
- [x] *(World)* Protected zones for Sulthari city and the Bearer's Homestead plot (fire spread is not covered yet: Forge has no event for it)
- [x] *(World)* Region title ("Welcome to ..." when entering a region; done in Sprint 1)
- [x] *(World)* Quest Compass on the HUD; lock status in the region title ("— Sealed" / "— Liberated")
- [x] *(World)* New players spawn inside the city of Sulthari (the plaza, from `structure_positions.json`)
- [x] *(Rules)* Save the region layout into the world at creation (done in Sprint 1: it is part of the world preset)

### Part 4.4 — Life in Sulthari: NPCs, death, travel, storage
- [x] *(from Sprint 3)* HUD overlay for health, Marks and souls (the resource bar and runes come in Sprint 2), final HUD art (brass frame; the Marks and souls counters fill in with their classes in Sprint 6)
- [x] *(from Sprint 3)* Create basic Void creature entities (Void Wretch and Void Stalker; they spawn in the Ashen Wastes)
- [x] The other four Bearers as NPCs (`BearerNpcEntity`) and the Council of Sulthari with Grand Vizier Ozhan
- [x] *(Rules)* Diablo II style corpse (`BearerCorpseEntity`) and compass (the JourneyMap marker comes with the plugin in Sprint 7.5)
- [x] *(Rules)* Waystones in Sulthari and the Return Scroll
- [x] *(Rules)* Personal Vault at the Sulthari bank (27 slots; the 54 and 81 slot upgrades need Dinars, Sprint 5.5)
- [x] *(Rules)* Peaceful replaced by Easy; difficulty scaling for bosses
- [x] *(Annex)* `MerchantNpcEntity` base class and `MerchantRole` enum (not vanilla villagers)
- [x] *(Annex)* Place Ferid, Dilara, Yusuf and Selim in Sulthari with placeholder models
- [x] *(Annex)* Disable vanilla villager spawning in the `sofe:aetheris` world (villagers, wandering traders and their llamas)

### Part 4.5 — Act I: Sulthari, the Eclipse and the Brass Sentinel
- [x] Build Sulthari: Great Observatory, lower district, Low Bazaar, palace, Training Grounds, forge (blockouts made of the Sulthari blocks; each is replaced by its hand-made build as soon as the template exists)
- [x] *(World)* Build pipeline for the empires: Structure Block pieces, jigsaw pools, `structure_positions.json` ([Mundo.md](Mundo.md#w6-the-empires-built-from-scratch)). Templates in `data/sofe/structures/sulthari/<piece>.nbt` are placed at their position; the jigsaw pools for ordinary houses come with the first real builds
- [x] *(World)* Sulthari building blocks: sandstone bricks (with stairs, slab and wall), brass plating and trims, glazed tiles, aetherium lamps
- [x] Eclipse Festival and Void invasion tutorial (first 3 skills)
- [x] Bearer-specific intro scenes (the shard piercing each hero), as cinematic dialogue with sound and light
- [x] Bearer outfits: a cosmetic armor layer per class over the player's own skin, with a setting to hide it ([Clases.md](Clases.md#how-the-player-looks))
- [x] Act I boss: the Brass Sentinel
- [x] Add Sulthari-themed textures and models (brass, clockwork, aetherium): placeholders drawn by `scripts/make_sprint4_textures.py` until the final art

## Sprint 5 — Act II: The Northern Campaign (Weeks 9-10)
- [ ] Implement ArchsinEntity abstract class
- [ ] Create VorathEntity with 2-phase fight
- [ ] Build Burning Citadel structure
- [ ] `BrokenOathEntity` base and Broken Oaths Kaleth (Law I) and Serath (Law II)
- [ ] Nordrath region biomes and fixed structure positions; Nordrath Forge and Arena dungeons
- [ ] *(World)* Nordrath building blocks (runestone, dark timber) with corrupted variants
- [ ] *(World)* Sealed Gates for dungeons and arena; protection for dungeons and arenas
- [ ] Implement boss health bar GUI
- [ ] Add loot tables for boss drops
- [ ] *(Rules)* Act II traits for vanilla mobs (zombies with empire armor, poison spiders)
- [ ] *(Rules)* Arena seal, boss reset after 30 s without participants, and re-entry to recover a corpse
- [ ] *(World)* The Burning Deep: Nether portals locked until Vorath falls, 1:8 region locks, portal link rules
- [ ] *(World)* Region title in the Nether: "Welcome to" the region the Nether position maps to
- [ ] *(World)* Nether materials: Infernal Ember, Wailing Soul
- [ ] *(Rules)* SoFE advancement tab (Act I and II branches)
- [ ] Vorath's temptation dialogue (stronger for the Knight) and the "Thank you, Bearer" twist
- [ ] *(Annex)* Add GeckoLib and use a placeholder animated model for Vorath
- [ ] *(Annex)* Boss participant tracking (damage dealt or 30 s in the arena)
- [ ] *(Annex)* Codex Shard given per player on Archsin defeat

## Sprint 5.5 — Loot and Economy (Weeks 11-12)
Design: [Pociones.md](Pociones.md).
- [ ] `Rarity` enum with name colors and loot beams
- [ ] `Affix`, `AffixRegistry` and the starter affixes in JSON, including the ones that raise the character: attributes, health, resource, damage, critical, dodge, resistances and +skill ranks ([Pociones.md](Pociones.md#affixes-that-raise-the-character-diablo-ii-style))
- [ ] Gear bonuses added to the character sheet (green values) and to skill ranks from gear (up to rank 8)
- [ ] Item level and attribute requirements; Diablo II style tooltips
- [ ] Talismans and the Talisman Pouch (Curios, 6 spaces)
- [ ] `LootGenerator` with Builder and class bias
- [ ] Global Loot Modifier for mod enemies
- [ ] Sulthari Brass and Glacial Iron ores with worldgen by region (heights and frequencies from [Mundo.md](Mundo.md#w4-resource-distribution))
- [ ] *(World)* Custom tool tiers with `TierSortingRegistry` (Brass, Glacial Iron)
- [ ] Imperial Forge with custom recipe type and 5 Blueprints
- [ ] Alembic, Pomegranate Elixir and Bearer's Tonic
- [ ] Bearer's Flask and potion belt (Curios)
- [ ] `Wallet` capability and merchant Ferid
- [ ] Relics of Kaleth, Serath and Vorath
- [ ] *(Annex)* Merchant offers in JSON (`data/sofe/merchant_offers/`) filtered by act
- [ ] *(Annex)* Per-player stock with daily restock
- [ ] *(Annex)* Selling back and buyback of the last 5 items
- [ ] *(Annex)* Selim the Money Changer (emeralds ↔ Dinars)
- [ ] *(Annex)* `sofe:owner` component and `BindPolicy` (Flask and Codex Shards soulbound)
- [ ] *(Annex)* Reward Coffer with personal loot for Vorath and his Broken Oaths
- [ ] *(Annex)* Secondary materials for Act I–II: Dune Leather, Frostpelt, Runestone, Void Ash

## Sprint 6 — Remaining Classes (Weeks 13-14)
Skill tables in [Clases.md](Clases.md).
- [ ] Knight: Shield and Charge stances + 10 skills
- [ ] Necromancer: soul binding, Clay Wardens + 10 skills
- [ ] Thief: Marks and stealing + 10 skills
- [ ] King: Decrees, Authority, Janissary Guard, Bronze Cannon + 10 skills
- [ ] Remaining Sorceress skills (levels 11-30)
- [ ] Balance damage/cooldown values in `data/sofe/skills/`
- [ ] Companion system: hire one of the other four Bearers (UC-22)
- [ ] *(Rules)* Bearer quests for Acts I–II
- [ ] *(Rules)* Nordrath fate side quests
- [ ] Write and translate (en/es) the Act I–II dialogue; placeholder portraits for every speaker
- [ ] *(Annex)* Class Concord passives (one per class) ready for Sprint 7.5

## Sprint 7 — Empires & Bosses (Weeks 15-18)
- [ ] Parsivan, Khemet and Aureum regions: biomes, fixed structures, Veil unlocks per act, Star Lapis / Solar Gold / Orichalcum / Void Rifts worldgen and tiers
- [ ] *(World)* Parsivan, Khemet and Aureum building blocks with corrupted variants; corrupted-to-intact swap when a region is liberated
- [ ] Parsivan: Luxara (Laleh's voice in the Enchanted Gardens)
- [ ] Khemet: Morthis (Catacombs gated until Luxara falls); the sultan's diary twist
- [ ] Aureum: Avarok, Gularth, Envyris (fight against a copy of the player's character)
- [ ] Broken Oaths III–IX (Mirael, Thessyn, Dormiel, Goldarc, Nixara, Fenrath, Shadeyn), each with the inverted mechanic of its Law
- [ ] Temptation dialogues for each Archsin, per Bearer
- [ ] "Sulthari is under siege" transition to Act V
- [ ] Add empire-specific mobs and loot
- [ ] *(Rules)* Act III and IV traits for vanilla mobs (frost and fire arrows, shorter creeper fuse, blinding and teleporting endermen, web-shooting spiders)
- [ ] Jeweler, Purifier, Tempering Anvil and remaining ores (from [Pociones.md](Pociones.md))
- [ ] *(Annex)* Remaining secondary materials: Moonsilk, Sunreed Papyrus, Imperial Marble
- [ ] *(Annex)* 7 sin gems (rough, cut, oath)
- [ ] *(Annex)* Favor per empire (6 ranks, discounts, offer unlocks)
- [ ] *(Annex)* Liberated camps with merchant copies after each Archsin
- [ ] *(Annex)* Zahir the Wanderer (traveling caravan every 3 days)
- [ ] *(World)* Void Gate under the Great Observatory (after Envyris, 12 Eyes of Ender); Void Crystal and Void Ink
- [ ] *(Rules)* Waystones and Vaults in liberated camps and dungeon entrances; side quests per region, with the Parsivan, Khemet and Aureum fate choices; Bearer quests for Acts III–IV
- [ ] Write and translate (en/es) the Act III–IV dialogue
- [ ] *(Annex)* Kerem and Sister Nilufar as NPCs
- [ ] *(Annex)* Personal loot (Reward Coffer) for every Archsin and Broken Oath

## Sprint 7.5 — Multiplayer & Co-op (Weeks 19-20)
See [Anexos.md](Anexos.md#a5-co-op-bonus-pact-of-the-empires).
- [ ] *(Annex)* `Pact` and `PactManager` (up to 5 players, `/sofe pact` commands)
- [ ] *(Annex)* Shared XP and loot find bonus while within 48 blocks
- [ ] *(Annex)* Class Concord and Five Pillars bonus
- [ ] *(Annex)* Downed state and revive in boss arenas
- [ ] *(Annex)* Kinship Chests in dungeons (2+ players)
- [ ] *(Annex)* Echo shrines so late joiners get boss credit without changing the world (instanced in `sofe:echo`)
- [ ] *(Annex)* Council of Sulthari recovery of lost story items
- [ ] *(Annex)* `uniqueBearersPerServer` option and per-player story cutscenes
- [ ] *(World)* `gateMode` option (`PER_PLAYER`, `PACT_ESCORT`)
- [ ] *(Rules)* Instanced prologue for late joiners
- [ ] *(Rules)* Disconnect rules during boss fights; boss edge cases (Envyris copies, Luxara charm, Avarok per player)
- [ ] *(Rules)* `/sofe` admin commands
- [ ] *(World)* JourneyMap plugin: region borders, locked shading, objective waypoints; check the Xaero API for basic waypoints
- [ ] *(Annex)* One-copy-equipped rule for Relics
- [ ] *(Annex)* Pact members' health on the HUD
- [ ] *(Annex)* Test on a dedicated server with 4+ players
- [ ] *(Annex, stretch)* Combo skills between two players

## Sprint 8 — Act V: The Ascension & Polish (Weeks 21-24)
- [ ] Sulthari under siege; Ozhan revealed as Solrath, the False Prophet (Law X)
- [ ] Prython (Pride) at the Celestial Spire, using the mechanics of the six previous Archsins
- [ ] Prython's offer: refusal, and the secret "Crowned in Ash" bad ending with one animation per Bearer and a hidden advancement
- [ ] Aetherium-tier gear (Orichalcum + Raw Aetherium + Void Crystal)
- [ ] Sealing Quill quest: Void Ink from the Outer Void, required to open the Inverted Throne
- [ ] The Spire inverts and sinks to the Inverted Throne
- [ ] Dimensions `sofe:inverted_throne` and `sofe:codex_interior`
- [ ] Nahrazel, the First Fallen: colossus of ash, seven sins at once, rewriting the seal inside the Codex
- [ ] Write and translate (en/es) the Act V dialogue and epilogues
- [ ] Ending inside the Codex, the region fate slides and the Bearer epilogues (full and unfinished versions), plus the eighth-lock sequel hook
- [ ] Implement multiplayer boss scaling
- [ ] *(Rules)* Act V traits for vanilla mobs (corrupted zombies, arrow volleys, charged creepers)
- [ ] Add sound effects and ambient music
- [ ] Localization (English + Spanish)
- [ ] *(Rules)* Post-game: all regions open, repeatable Echo fights
- [ ] *(Rules)* Free mode, integrity check on load and `incompatible_mods.json`
- [ ] *(Rules)* Compatibility pass with popular mods (content, maps, waystones, graves, extra dimensions)
- [ ] *(Rules)* Accessibility: rarity text labels, subtitles, shake/flash/particle options
- [ ] *(Rules)* Server README: pre-generation, fair-play map settings, offline-mode UUID note
- [ ] Performance optimization
- [ ] Full testing pass
- [ ] *(Annex)* Final title screen: logo, panorama, key art and main theme
- [ ] *(Annex)* The Codex screen (lore, bestiary, splash art gallery) and the illustrated Codex Map
- [ ] *(Annex)* Row of icon buttons on the title screen (Codex, Codex Map and others from `art/concepts/menu_icons_mockup.jpg`), once those screens exist
- [ ] *(Annex)* Themed pause menu, Journal and loading screens with lore tips
- [ ] *(Annex)* Swap every splash art and 3D model placeholder for the final asset (list in [Anexos.md](Anexos.md#a2-splash-arts-and-3d-models))
