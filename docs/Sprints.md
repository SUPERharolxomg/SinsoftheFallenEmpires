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
- [ ] Implement Skill interface and abstract class
- [ ] Create SkillCooldownManager
- [ ] `SkillDataLoader`: costs and cooldowns from `data/sofe/skills/*.json`
- [ ] Resource system for the 5 resources (Resolve, Essence, Mana, Energy, Authority)
- [ ] Sorceress level-1 skills (Ember Verse, Frost Lance, Wandering Spark) and the rune / constellation system
- [ ] *(Rules)* Combat Bar keybinding system (Left Alt + numbers), Flask on H, all rebindable
- [ ] Implement skill visual effects (particles)

## Sprint 3 — Combat & Leveling (Weeks 5-6)
- [ ] Implement XP and leveling system
- [ ] Create SkillTree with point allocation
- [ ] Add SkillTreeScreen GUI
- [ ] Implement combat damage modifiers per class
- [ ] HUD overlay for health, class resource, runes, Marks and souls
- [ ] Create basic Void creature entities

## Sprint 4 — Act I: The Night of the Eclipse (Weeks 7-8)
- [ ] *(World)* `AetherisBiomeSource` with the fixed layout; Sulthari and Ashen Wastes biomes
- [ ] Build Sulthari: Great Observatory, lower district, Low Bazaar, palace, Training Grounds, forge
- [ ] *(World)* Build pipeline for the empires: Structure Block pieces, jigsaw pools, `structure_positions.json` ([Mundo.md](Mundo.md#w6-the-empires-built-from-scratch))
- [ ] *(World)* Sulthari building blocks: sandstone bricks, brass plating and trims, glazed tiles, aetherium lamps
- [ ] Eclipse Festival and Void invasion tutorial (first 3 skills)
- [ ] Bearer-specific intro scenes (the shard piercing each hero)
- [ ] Bearer outfits: a cosmetic armor layer per class over the player's own skin, with a setting to hide it ([Clases.md](Clases.md#how-the-player-looks))
- [ ] The other four Bearers as NPCs (`BearerNpcEntity`) and the Council of Sulthari with Grand Vizier Ozhan
- [ ] Act I boss: the Brass Sentinel
- [ ] Add Sulthari-themed textures and models (brass, clockwork, aetherium)
- [ ] *(World)* Seal Veil on every region border (per-player collision and tint)
- [ ] *(World)* `RegionEnforcer` (flight, pearls, coast) and `opsBypass`
- [ ] *(World)* Protected zones for Sulthari city and the Bearer's Homestead plot
- [x] *(World)* Region title ("Welcome to ..." when entering a region; done in Sprint 1)
- [ ] *(World)* Quest Compass on the HUD; lock status in the region title ("— Sealed" / "— Liberated")
- [ ] *(Rules)* Quest system (`data/sofe/quests/`) and Journal
- [ ] *(Rules)* Warcraft III style dialogue box (portrait, name, letter-by-letter text, answers), cinematic letterbox mode, `data/sofe/dialogue/`; text only, no voices
- [ ] *(Rules)* Region fates in `StoryProgress` and the `fate_is` condition; first Sulthari fate side quest
- [ ] *(Rules)* Diablo II style corpse (`BearerCorpseEntity`), compass and map marker
- [ ] *(Rules)* Waystones in Sulthari and the Return Scroll
- [ ] *(Rules)* Personal Vault at the Sulthari bank
- [ ] *(Rules)* Peaceful replaced by Easy; difficulty scaling for bosses
- [x] *(Rules)* Save the region layout into the world at creation (done in Sprint 1: it is part of the world preset)
- [ ] *(Annex)* `MerchantNpcEntity` base class and `MerchantRole` enum (not vanilla villagers)
- [ ] *(Annex)* Place Ferid, Dilara, Yusuf and Selim in Sulthari with placeholder models
- [ ] *(Annex)* Disable vanilla villager spawning in the `sofe:aetheris` world
- [ ] *(Annex)* `StoryProgress` player capability (act, quests, credit per boss)
- [ ] *(Annex)* King variant: the Council led by Ozhan when the player is Azhar

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
- [ ] *(Rules)* Arena seal, boss reset after 30 s without participants, and re-entry to recover a corpse
- [ ] *(World)* The Burning Deep: Nether portals locked until Vorath falls, 1:8 region locks, portal link rules
- [ ] *(World)* Nether materials: Infernal Ember, Wailing Soul
- [ ] *(Rules)* SoFE advancement tab (Act I and II branches)
- [ ] Vorath's temptation dialogue (stronger for the Knight) and the "Thank you, Bearer" twist
- [ ] *(Annex)* Add GeckoLib and use a placeholder animated model for Vorath
- [ ] *(Annex)* Boss participant tracking (damage dealt or 30 s in the arena)
- [ ] *(Annex)* Codex Shard given per player on Archsin defeat

## Sprint 5.5 — Loot and Economy (Weeks 11-12)
Design: [Pociones.md](Pociones.md).
- [ ] `Rarity` enum with name colors and loot beams
- [ ] `Affix`, `AffixRegistry` and 20 starter affixes in JSON
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
- [ ] *(Annex)* Themed pause menu, Journal and loading screens with lore tips
- [ ] *(Annex)* Swap every splash art and 3D model placeholder for the final asset (list in [Anexos.md](Anexos.md#a2-splash-arts-and-3d-models))
