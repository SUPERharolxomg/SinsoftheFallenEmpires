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
Split into five parts. The dungeons and the Citadel are blockouts until their hand-made builds exist (a template in `data/sofe/structures/nordrath/<piece>.nbt` replaces each one; see [Mundo.md](Mundo.md#w6-the-empires-built-from-scratch)).

### Part 5.1 — Nordrath
- [x] Nordrath region biomes and fixed structure positions; Nordrath Forge and Arena dungeons (tundra, ice fields and volcanic forges as biome zones saved with the world; the Forge, the Arena, the Citadel and the Caverns entrance in `structure_positions.json`, built when a player first comes within 192 blocks)
- [x] *(World)* Nordrath building blocks (runestone, dark timber) with corrupted variants (runestone bricks with stairs, slab and wall, dark timber and planks, iron brazier; scorched runestone and burning timber)
- [x] *(Rules)* Act II traits for vanilla mobs (zombies with empire armor, poison spiders)

### Part 5.2 — Bosses: the common base
- [x] Implement ArchsinEntity abstract class (on `SoFEBossEntity`, which the Brass Sentinel now uses too)
- [x] `BrokenOathEntity` base
- [x] *(Annex)* Boss participant tracking (damage dealt or 30 s in the arena)
- [x] Implement boss health bar GUI (brass frame, boss color, a notch every 10%; vanilla bosses keep theirs)
- [x] *(Rules)* Arena seal, boss reset after 30 s without participants, and re-entry to recover a corpse
- [x] *(World)* Sealed Gates for dungeons and arena; protection for dungeons and arenas (conditions in `data/sofe/conditions/`, the message says what is missing)

### Part 5.3 — The enemies of Act II
- [x] Broken Oaths Kaleth (Law I: thrown fire blades, stunning slam, execution strike on stunned targets) and Serath (Law II: life steal, blood pools that heal her)
- [x] Create VorathEntity with 2-phase fight (rage that grows with every wound; in phase 2 the ring of fire closes in)
- [x] Build Burning Citadel structure (blockout)
- [x] *(Annex)* Add GeckoLib and use a placeholder animated model for Vorath (idle, walk and attack)
- [x] Vorath's temptation dialogue (stronger for the Knight) and the "Thank you, Bearer" twist; the Act II main quest

### Part 5.4 — Rewards and progress
- [x] Add loot tables for boss drops (each participant rolls for themselves; Relics come in Sprint 5.5)
- [x] *(Annex)* Codex Shard given per player on Archsin defeat
- [x] *(Rules)* SoFE advancement tab (Act I and II branches)

### Part 5.5 — The Burning Deep
- [x] *(World)* The Burning Deep: Nether portals locked until Vorath falls, 1:8 region locks, portal link rules (the Seal Veil also stands at the scaled borders)
- [x] *(World)* Region title in the Nether: "Welcome to" the region the Nether position maps to
- [x] *(World)* Nether materials: Infernal Ember (basalt deltas, magma cubes), Wailing Soul (soul sand valleys); Nether mobs are two levels above the region they lie under

## Sprint 5.5 — Loot and Economy (Weeks 11-12)
Design: [Pociones.md](Pociones.md).
- [x] `Rarity` enum with name colors and loot beams
- [x] `Affix`, `AffixRegistry` and the starter affixes in JSON (36 affixes in `data/sofe/affixes/`)
- [x] Gear bonuses added to the character sheet (green values) and to skill ranks from gear (up to rank 8); elemental damage, life steal, resistances (75% cap) and cooldown reduction in combat
- [x] Item level and attribute requirements; Diablo II style tooltips
- [x] Talismans and the Talisman Pouch (Curios, 6 spaces); amulet and two rings as Curios slots too
- [x] `LootGenerator` with Builder and class bias (about 60%, tested over 10,000 rolls)
- [x] Global Loot Modifier for mod enemies (gear of the enemy's level and Dinars from every levelled enemy of a journey, twice as often from SoFE's own)
- [x] Sulthari Brass and Glacial Iron ores with worldgen by region
- [x] *(World)* Custom tool tiers with `TierSortingRegistry` (Brass, Glacial Iron), with brass and glacial iron pickaxes
- [x] Imperial Forge with custom recipe type and 5 Blueprints (Blueprints drop from the Brass Sentinel, Kaleth and Serath)
- [x] Alembic, Pomegranate Elixir and Bearer's Tonic (pomegranate and desert lotus are sold by Ferid until herbs can be farmed)
- [x] Bearer's Flask (key H) and potion belt (Curios, Left Alt + 7, 8, 9, 0)
- [x] `Wallet` capability and merchant Ferid (plus Dilara, Yusuf and Selim)
- [x] Relics of Kaleth, Serath and Vorath
- [x] *(Annex)* Merchant offers in JSON (`data/sofe/merchant_offers/`) filtered by act (Favor filters come with Sprint 7)
- [x] *(Annex)* Per-player stock with daily restock
- [x] *(Annex)* Selling back and buyback of the last 5 items
- [x] *(Annex)* Selim the Money Changer (emeralds ↔ Dinars)
- [x] *(Annex)* `sofe:owner` component and `BindPolicy` (Flask and Codex Shards soulbound: never dropped or sold, kept through death). Keeping them out of other players' chests and `PACT_ONLY` come with the Pact in Sprint 7.5
- [x] *(Annex)* Reward Coffer with personal loot for Vorath and his Broken Oaths
- [x] *(Annex)* Secondary materials for Act I–II: Dune Leather, Frostpelt, Runestone, Void Ash
- [x] *(Extra)* Herbs that can be farmed: Mountain Sage (Sulthari), Pomegranate (Parsivan) and Desert Lotus (Khemet) grow wild in their region and as crops on farmland; Ferid sells their seeds
- [x] *(Extra, art pass after the first playtest)* GeckoLib models with animations and glow masks for the Void Wretch, Void Stalker, Kaleth, Serath, the Brass Sentinel (with its Void phase) and Vorath (`scripts/make_mob_models.py`); hand-drawn 16×16 sprites for gear and Relics (`scripts/make_gear_sprites.py`); high-resolution dialogue bars split per empire with their portrait windows (`scripts/split_dialogue_bars.py`); a larger framed portrait and engraved text in the dialogue box; a smaller Observatory dome; NPCs placed inside buildings instead of on their roofs
- [x] *(Extra)* Sulthari rebuilt after its concept art: banded terracotta walls on an escarpment with towers, gatehouses and banners; the palace with a golden dome, red domes, minarets and a great portal; a golden-domed Observatory; varied houses, bazaar stalls and trees (`world/build/Architecture.java`, a kit the other empires will reuse with the concepts in `art/concepts/city_*.png`); portraits drawn at their own resolution
- [x] *(Extra)* Sulthari on its terracotta mesa (`SultharisCity`): a rounded mesa with banded cliffs above the desert and the sea, cream walls with towers, crescent banners and four gatehouses, raised terraces for the palace and the Observatory, dirt avenues and roads going down from the gates as fenced causeways over the water (`Roads`), and the city filled with houses, chapels, gardens and wells
- [x] *(Extra)* Citizens of Sulthari who talk about what is happening, act by act; Rasim the meddah tells the old tales on request; four bazaar merchants with their own shops; NPCs without a painted portrait are drawn live in the dialogue box (`scripts/make_citizens.py`)
- [x] *(Extra)* The palace rebuilt with pointed arches in red and cream voussoirs, a front arcade and a hall with dark wooden columns, arches, a coffered ceiling, chandeliers, banners and a throne; blue tiles only as the palace floor; the city ground mixes earth, coarse earth, terracotta, sand and packed mud; 16 citizens who stroll near their homes (`CitizenEntity`); houses furnished; no monsters spawn by themselves inside a city; panes, fences and walls placed by the builders join up
- [x] *(Extra)* MrCrayfish's Furniture Mod: Refurbished as a dependency, for the interiors (chairs, tables, desks, drawers, cabinets, jars, crates and divans; looked up by id with vanilla stand-ins); larger palace domes and arcades on three sides; seating areas in the hall; the Observatory with three floors, a stair along the wall, a library, a map room and a telescope; a furnished forge around the Imperial Forge and a bank with clerks' desks and a strongroom; a smaller archery range; lanterns on streets, walls, terraces, gardens and house doors
- [x] *(Extra)* Farms in Sulthari (fenced fields of wheat, carrots, potatoes and beetroots, and pens of cows, pigs, sheep and chickens); the plaza with a sadirvan, a star mosaic, flower beds and benches; the palace hall after its reference (gold capitals and banners on the columns, patterned rugs, divans and armchairs, plants, a tapestry behind the throne, a grand chandelier and a gold ring under the dome); no gap under the first row of house roofs; cinematic dialogue inside a taller lower black band
- [x] *(Extra)* The palace hall rebuilt again: square columns of dark wood banded in gold up to the ceiling, the domes on solid rings (no gap between roof and drum), the main dome toward the door and a second golden dome over the throne on a dais of three steps, aetherium lamps on brass posts along the carpet and around the grounds; the Observatory with straight flights on solid supports, rugs, plants and lamps; grass between the houses; a lantern post at every plot

## Extra — Skarnhold, the hold of the Nordrath clans

- [x] Skarnhold at x 0, z -2400 (`NordrathCity`), after `art/concepts/city_nordrath.png`: four walled terraces of grey stone climbing a volcano, round towers with dark conical roofs, gatehouses with banners, rivers of lava from the crater into stone basins, the jarl's great hall on the hold, longhouses with steep slate roofs and long hearths, a smithy by the fire, a market, pens of sheep and goats
- [x] Ways up: flights of stone stairs between every terrace on three sides, a path round each terrace and the main street up the middle, braziers along them
- [x] A fjord to the west with a long stair down to the harbour, a pier, jetties and longboats; trestle bridges on the roads in; snow and spruce forests around
- [x] People: nine citizens, Hrolf the skald who tells the sagas, and two merchants (furrier and runesmith); their lines change when Vorath falls
- [x] After the first visit: still lava (every lava block sealed so none of it runs), the volcano moved back behind the hold, the jarl's hall rebuilt in warm spruce on a podium with a deck, a cross-wing, dragon heads, a round shield and runestones; no gap under the longhouse gables; more furniture in the longhouses
- [x] Second pass after the concept render: solid gatehouses on the real line of the wall (passage, timber lintel, portcullis, crossed axes, braziers), towers with solid slate cones, a wooden hoarding, fire and a giant banner, a Viking market (hide-roofed stalls, drying racks, furs, a fire pit), wheat fields, larger drakkars with dragon prows and striped sails, patchy snow that leaves the paved roads visible, and a mixed ground of earth, podzol and snow
- [x] Supplementaries (flags on every tower) and Small Ships (four sailing drakkars moored at the jetties, and a longship of blocks at anchor) as dependencies; blue slate roofs with timber eaves for most houses, glass windows, spruces, berry bushes and ferns between the houses, two giant swords at the stair to the hold, striped tents in the market, lantern posts along the main street; no more trees growing on the pier
- [x] Last pass: the ridge row of every roof is placed (no opening along the top), green grass between the houses, towers you can enter and climb (a door at the terrace, a ladder to a hatch, headroom under the cone), flags of the clans with the white knot on every tower, flagpoles on the gatehouses and before the hall, and a great market square with stalls under striped awnings, item shelves, carts, barrels, chests, lanterns and a fire pit
- [x] The Great Banner of the Clans (`ClanBanner`, a block with its own renderer): a cloth three blocks wide and six long, red with the white knot, that moves in the wind, on every tower and gatehouse; larger towers with doors through the full thickness of the wall; the market reserved before the houses (no more cut-off cabins) and ringed by a timber gallery with windows, planters and trees; the smithy walled; red sails on the drakkars; the game's own spruces and pines; moss and grass for a green ground; more plants
- [x] The curtain wall closed all round (its builder stopped two blocks past the base ring while the wavy edge reaches five), checked by a flood of the lowest terrace; the stairs up to terrace 2 moved to both sides of the market, with paved ways to them; three blocks of headroom on the tower tops; the jarl's hall dressed after the concept: stone plinth, timber posts out of the walls, two storeys of wide windows with sconces, great banners on the front
- [x] The market hall gets doors in the middle of every side; the market no longer cuts the towers beside it (their ladders stay whole); six pens (cows, pigs, sheep, chickens, goats) and six fields (wheat, carrots, potatoes, beetroots) on grass, placed before the houses; a wide lava flow down the eastern flank of the volcano
- [x] Skarnhold enlarged (terraces out to 140 blocks, about 34 longhouses round the volcano); the flights beside the market placed where no tower stands; the curtain wall also runs up the volcano's flank where it reaches the rim
- [x] The combat HUD moved to the bottom right, larger, in a brass frame, with values inside the bars and short key names inside the slots
## Extra — The arsenal, batch 1: melee weapons and shields

- [x] 18 melee weapons (`TraitWeapon`), each SoFE gear that rolls affixes and drops by item level, with traits applied on hit (`WeaponTrait`, `WeaponTraitHandler`): reach (halberd, lance, trident, scythe, greatsword, hook-blade), sweep, stun, slam, bleeding (a new effect), beast-hunter, charge, plant-cutting, armor-piercing, flurry, frost, soul reaping, slow, pull, knock-up, Void and blood price
- [x] 4 empire shields (`EmpireShield`) with a power when they block: slow (Sulthari), reflect 30% (Nordrath), heal (Observatory), weaken and wither (Void); raised model while blocking
- [x] Dilara sells the brass weapons from Act I and the glacial ones from Act II; sprites drawn by `scripts/make_arsenal_sprites.py`
- [x] Tests: `WeaponTraitTest` and `ArsenalGameTests` (gear bases, reach, slam, bleeding)

## Extra — The arsenal, batch 2, and the art pass

- [x] Nine armor sets of four pieces (Scout, Bronze, Gearwork, Observatory, Emerald, Cobalt, Solari, Chronomancer, Void), SoFE gear that rolls affixes, with a bonus for the full set (`ArmorSets`); icons and worn layers drawn by `scripts/make_armor_sets.py`
- [x] Art pass (`scripts/make_art_pass.py`): ingots, nuggets, raw metal, gems, shards and powders with the shapes players know; ores as stone with clusters; storage blocks; round potion flasks; the Imperial Forge and the iron brazier as 3D models (the brazier a burning bowl on legs, with flames)
- [x] The empire shields drawn with the vanilla shield's model and pose (`EmpireShieldRenderer`)
- [x] Weapons at 32x32 (`scripts/upscale_weapons.py`, Scale2x and a light pass), long and heavy ones held larger
- [x] Five creative tabs: weapons and armor, ores and materials, blocks, potions and items, creatures

## Extra — The arsenal, batch 2b: more armor and weapons

- [x] Every armor set redrawn on a vanilla armor shape (iron, chainmail, gold, diamond, netherite or leather) recolored to its palette, with a vanilla trim pattern in the set's trim color, gems and an icon ornament (plume, horns, crest, sun, jackal ears and so on). `scripts/make_armor_sets.py` reads the vanilla textures from the Minecraft jar in the Gradle cache
- [x] The brass and glacial iron pickaxes drawn on the vanilla pickaxe
- [x] Seven new armor sets (28 pieces), all with a full-set bonus:

  | Set | Empire | Bonus |
  |---|---|---|
  | Berserker | Nordrath | Strength II below half health |
  | Seafarer | Nordrath | Water breathing; Dolphin's Grace in water |
  | Scarab | Khemet | Poison and wither are cleansed |
  | Embalmer | Khemet | Regeneration while crouching |
  | Legion | Aureum | Knocks back melee attackers |
  | Order of the Scale | Aureum | Reflects 25% of melee damage |
  | Infernal | Relic | Sets melee attackers on fire |

  The Legion, Scale and Infernal bonuses answer blows (`ArmorSets.onHurt`).
- [x] Thirteen new melee weapons: Bearded Axe, Seax, Frost Spear (Nordrath); Khopesh, Scarab Sickle, Jackal Glaive, Was Sceptre (Khemet); Gladius, Legion Pilum, Justicar's Maul, Blade of the Scale (Aureum); Ember Blade and Infernal Greataxe (Infernal relics)
- [x] Two new weapon traits: Burning (sets the target on fire) and Withering curse (Weakness)
- [x] Dilara sells chestplates of the new sets and some of the new weapons, unlocked by act
- [ ] Batch 3: ranged and arcane weapons (bows, crossbows, firearms, spell staves, tomes and gadgets), with their projectiles

## Extra — Uniques, jewelry and the gambler

- [x] Droppable Relics ("uniques"): a Relic definition can be `droppable`, with a `slot` and a `min_level`. 2% of the gear an enemy drops is a unique of its level instead (`GearLoot.GearDrops`). Uniques that drop at random or come out of a gamble are not bound
- [x] Nineteen uniques with their own sprites and effects (`RelicEffects`):

  | Kind | Uniques |
  |---|---|
  | Weapons | Dunesunder, The Widow's Kiss, Skaldbreaker, Rimetooth, Judgement of the Jackal, Stormcaller, Greed's Chain |
  | Armor | Crown of the Five Sultans, Mantle of the White Wolf, Treads of the Caravan Master, Wrappings of the Undying, Helm of the Blind Judge, Heart of the Furnace |
  | Jewelry | Eye of the False Prophet, Ring of the Last Caravan, Soulkeeper, Band of the Frozen Throne, Coil of the Serpent, Sigil of the Broken Pact |

- [x] Ten new random jewelry bases (five rings, five necklaces and amulets) in the Curios ring and necklace slots, drawn by `scripts/make_jewelry.py`
- [x] The gambler, after Diablo II's Gheed: Kasim the Veiled in the Sulthari bazaar and Hrafna Bone-Dice in the Skarnhold market sell veiled weapons, armor and jewels (`VeiledItem`, `Gamble`). The veil is lifted at the counter:

  | Outcome | Chance |
  |---|---|
  | Swindle (a handful of Void ash) | 8% |
  | Common | 47% |
  | Tempered | 28% |
  | Imperial | 14% |
  | A unique | 3% (6% for jewels) |

## Extra — 3D armor, ten sets per class

- [x] Every SoFE armor is worn as a GeckoLib 3D model (`ModeledArmor`, `SoFEArmorRenderer`), as in Armor of the Ages:
  - `scripts/armor_catalog.py` holds all 50 class sets and the 6 unique pieces.
  - `scripts/armor_lib.py` holds the parts (horns, crests, wizard and witch hats, hoods, skulls, lich crowns, plague masks, nemes, jackal heads, turbans, crowns, keffiyehs, tricorns, wolf and lion pelts, pauldrons, robes, capes) and the painter.
  - `scripts/make_armor_models.py` writes the models and textures. It also draws every inventory icon from the model itself, so an icon always shows what is worn.
- [x] Ten sets per class:

  | Class | Sets |
  |---|---|
  | Knight | Bronze, Sentinel, Glacial Iron, Cobalt, Warlord, Solari, Dragonknight, Legion, Order of the Scale, Infernal |
  | Necromancer | Gravewarden, Plaguebearer, Soulreaver, Bonelord, Embalmer, Scarab, Mummy Lord, Void, Anubis, Lich |
  | Sorceress | Witch, Enchantress, Frost Witch, Observatory, Emerald, Pyromancer, Chronomancer, Tempest, Archmage, Astral Sage |
  | Thief | Scout, Huntsman, Gearwork, Seafarer, Berserker, Shadow, Wolf Raider, Frost Stalker, Corsair, Nightblade |
  | King | Brass, Desert Emir, Royal Guard, Peacock, Vizier, Mirage, Sultan, Lion King, Pharaoh, Golden King |

- [x] Heavy sets ask for Strength, as in Diablo (10 + 2 × level). Medium sets ask for less (8 + level), and robes and leathers ask for nothing. `scripts/make_armor_data.py` writes the materials, items, names, gear bases and bonuses
- [x] Full-set bonuses are data (`data/sofe/armor_sets/<set>.json`, read by `ArmorSets`):
  - effects (always, or below half health, crouching, in water, by night or day, when falling);
  - cleansing;
  - heals and absorption on a timer;
  - answers to melee blows (knockback, reflect, ignite, slow, poison, wither, weakness);
  - on-kill heals and effects;
  - immunity to freezing;
  - enemies revealed through walls.
- [x] `ArmorShots`: `./gradlew runClient -PshotSets=a,b` opens `run/saves/shots`, dresses the player in each set, screenshots it from the front and the back into `run/screenshots`, and quits
- [x] Axes and hammers redrawn at 32x32 from shapes (`scripts/make_heavy_weapons.py`). Great axes have one wide crescent and a back spike

## Extra — The arsenal, batch 3: tools, thrown, ranged, arcane and class items

- [x] New tool tiers: Star Lapis, Solar Gold, Orichalcum and Aetherium (`SoFETiers`). Their pickaxes are drawn on the vanilla pickaxe; mining hammers break a 3x3 face (`Tools.MiningHammer`); the Gearwork Drill digs stone and earth (`Tools.Drill`)
- [x] Thrown weapons (`ThrownWeapon`): javelins of brass, glacial iron and aetherium (the aetherium one returns); throwing knives of brass, glacial iron and venom
- [x] Ranged (`RangedItems`):
  - five bows, drawn on the vanilla bow, each with its own draw time, damage and element;
  - three crossbows, one of them a repeater that looses three bolts;
  - three firearms that fire Brass Cartridges: pistol, musket and blunderbuss.
- [x] Arcane: five staves that cast bolts, a draining beam or lightning (`SpellBolt`); five tomes that burst round the reader; three bombs (clockwork, smoke, fire)
- [x] Class items, as in Diablo II (`ClassBound`: only their Bearer uses them; anyone else strikes for 1 and cannot cast):

  | Class | Items |
  |---|---|
  | Knight | Oathblade, Aureum Warmace, Lance of the Scale (holy) |
  | Necromancer | Bone Wand, Soul Wand, Reaper's Sickle |
  | Sorceress | Ember, Frost and Storm Orbs |
  | Thief | Shadow and Viper Claws, Throwing Stars |
  | King | Royal Scepter (a decree that strengthens allies), Sultan's Saber, Royal Flintlock |

- [x] Elements (`Spell`): fire, frost, storm, Void, souls, bone, arcane, holy, poison and bleeding, shared by bolts, arrows and thrown weapons
- [x] `scripts/arsenal3_catalog.py` holds the items. `make_arsenal3_data.py` writes the registry, lang, gear bases, recipes and bow and crossbow models; `make_arsenal3_sprites.py` draws them
- [x] The gambler of Sulthari renamed Kasim the Veiled; Zahir the Wanderer stays the travelling caravan of Sprint 7

## Sprint 6 — Remaining Classes (Weeks 13-14)
Skill tables in [Clases.md](Clases.md).
- [x] Knight: Shield and Charge stances (key G) + 10 skills. Shield Stance takes less and gains Resolve from blocks and blows; Charge Stance hits harder and runs, burning Resolve. Banner marks for the Verdict, Shield Wall, the Oath, the Rampart that stops projectiles, Contained Wrath, Last One Standing (`KnightSkills`, `ClassMechanics`)
- [x] Necromancer: soul binding (enemies dying near him or marked by Threshold Touch give a soul, up to 10), Clay Wardens (`SummonedAlly`) + 10 skills, with soul costs, Rite of Passage stacks, Heavy Heart bursts and the Great Judgment raising the fallen (`NecromancerSkills`)
- [x] Thief: Marks (basic blows and skills, up to 5, spent by Cutthroat) and stealing (blessings, potions, Dinars, now and then gear) + 10 skills, with backstab criticals, Deep Pockets and the Great Heist (`ThiefSkills`)
- [x] King: Decrees (one zone at a time; Voice of the Throne makes them longer and wider), Authority from his scepter, kills and healing, the Janissary Guard, the Bronze Cannon (`BronzeCannon`), Command, the Royal Treasury coins + 10 skills (`KingSkills`)
- [x] Remaining Sorceress skills (levels 11-30): Burning Calligraphy, Water Mirror (with an ice clone), Petal Tempest, Starfall, Written Eclipse (free spells, two runes each); Sky Map and Arcane Poetry on the constellations
- [x] Balance damage/cooldown values in `data/sofe/skills/` (first pass: every skill has its numbers; tuning after playtests)
- [x] *(Extra)* Every learned active skill can be placed in any Combat Bar slot: hover it in the skill tree and press 1-5
- [x] *(Extra)* English and Spanish descriptions for all 50 skills in the skill tree
- [x] *(Extra)* Skill trees of 30 per class, Diablo II style: three branch tabs of 10, the 10 base skills plus 20 upgrades per class (synergies that raise a skill's numbers and changes to how it works), written by `scripts/make_skill_tree.py` from `scripts/skill_tree_catalog.py` and applied by `Upgrades`
- [x] *(Extra)* The Necromancer raises the Embalmed Dead (linen-wrapped husks of Khemet) with upgrades for their time, health, damage and number; the Clay Warden is drawn on the vanilla iron golem; the King's Janissaries wear their own skin; the Sorceress's ice clone wears its caster's skin
- [x] *(Extra)* Casting poses (arms thrown forward or raised overhead), a glow in the hand and bolts that fly from the hand (`SkillFx`, `CastPoses`)
- [x] *(Extra)* Dev tool: `./gradlew runClient -PskillShots=all` casts each skill on husks, screenshots it and logs the result (`SkillShots`)
- [x] *(Extra)* Batch 4 of uniques (`scripts/uniques_catalog.py`, `scripts/make_uniques.py`): 110 class uniques, 22 per Bearer (6 weapons, 8 armor pieces as 3D models, 2 rings, 2 amulets, 4 charms) that only their class can use, many giving ranks to their skills; 39 uniques for anyone (20 charms, 8 jewels, 7 weapons, 4 armor pieces); and the Grand Talisman, a third size of random charm. Their effects are data (`UniqueEffects`: on hit, on kill, when hurt, while worn) and their tooltips are written from them in English and Spanish. Drops and the gamblers only offer uniques the Bearer can use
- [x] *(Extra)* Level cap 100, twenty levels per act (`act_caps` in `leveling.json`); mobs and bosses follow the Bearer's level (a boss one above the strongest near); every item level spread over 1-100 by empire, uniques ask their full level, and the strongest four uniques of each class (and four for anyone) are level 100
- [x] Companion system (UC-22): talk to another Bearer in Sulthari and choose "Travel with me". The companion follows at the player's level, fights with its class skills (the Knight's Verdict and Shield Wall, the Necromancer's drain, wraps and Embalmed, the Sorceress's fire, frost, sparks and stars, the Thief's strikes from behind and smoke, the King's decree and Janissaries), waits or follows on command, speaks when entering each region and when a boss falls, and goes back to Sulthari when dismissed or beaten (`CompanionEntity`, `Companions`)
- [x] *(Rules)* Bearer quests for Acts I–II: one per hero and act (10), only for the player's own hero, each with its giver, its fight and a class unique as reward (`data/sofe/quests/bearer`, written by `scripts/make_story.py` from `scripts/story_catalog.py`)
- [x] *(Rules)* Nordrath fate side quests: The Endless War (free the warriors or let them fight: the clans at peace or still at war, which Hrolf the skald remembers) and The Cold Forge
- [x] Write and translate (en/es) the Act I–II dialogue; placeholder portraits for every speaker (`scripts/make_placeholder_portraits.py`; the box uses a painted portrait, then the live NPC, then the placeholder). `DialogueCoverageTest` checks every line in both languages and a face for every speaker
- [x] *(Annex)* Class Concord passives (one per class) ready for Sprint 7.5 (`ClassConcord`): for now the group is the Bearer, their companion and the Bearers within 32 blocks; Knight +5% armor, Necromancer +3% life steal, Sorceress +5% regeneration, Thief +3% critical chance, King +5% healing, all five +10% damage
- [x] *(Extra)* Multiplayer with repeated classes: each Thief keeps their own Marks and can rob an enemy once, several Necromancers can mark one enemy with Threshold Touch, a companion is refused only when a Bearer of that class is in the player's group (not anywhere on the server), mobs follow the average level of the group near them, and the Concord forgets players who log out
- [x] *(Extra)* Jewelry seen on the Bearer (`JewelryRenderer`, Curios): a 3D chain round the neck with the pendant cut from the icon, a band with its stone on each hand for the rings, and the charms hanging from cords on the belt (two in front, two on the hips, two behind); colors read from each icon. Relic and Legacy jewelry shimmers, and anything above Common gives off sparks of its rarity's color. The 89 jewelry and charm icons are redrawn at 32×32 with recut gems and a glint (`scripts/upscale_jewelry.py`). `-PshotSets=jewelry` screenshots them, and the screenshot tools create `run/saves/shots` when it is missing (`ShotsWorld`)

## Sprint 7 — Empires & Bosses (Weeks 15-18)
- [x] *(World)* The world is 25,000 x 25,000 (regions within +/-12,000): every empire grows outwards, Parsivan to the north-east, Aureum to the north-west and south-west, the Southern Sea between Aureum and Khemet (`RegionMap`)
- [x] *(World)* Parsivan, Khemet and Aureum building sets, intact and corrupted, with stairs, slabs and walls (`scripts/make_empire_blocks.py`, `EmpireBlocks` for the swap when a region is liberated)
- [x] *(World)* The dungeons and arenas of Acts III and IV and the Nordrath underground: Baths, Silk Road caravanserai, Enchanted Gardens, Catacombs, Stagnant Marsh, Treasury, Market, Golden Vaults, Colosseum, Shadow Throne, Caverns and Feast Halls (`EmpireBuilder`, `scripts/act34_places.py`), each on shaped land with a causeway over water, its Sealed Gate and a Waystone
- [x] Boss lairs: each boss rises in its room when a Bearer who has not beaten it comes in (`BossLairs`; also fixes Act II, whose bosses only came from eggs)
- [x] Bosses and Broken Oaths: a presence (title, Law or Sin, darkness) and a second phase that is stronger every time
- [x] The twelve bosses of Acts III and IV: GeckoLib models (`scripts/make_boss_models.py`), registration, loot and story advancements (`scripts/make_boss_data.py`), portraits cut from their models, and blocks or minions they leave always undone after the fight (`BossKit`)
- [x] *(Extra)* Eight creatures of the empires, two per act, each spawning only in its region (`EmpireMob`, `scripts/make_empire_mobs.py`, `scripts/make_empire_mob_data.py`)
- [x] *(Extra)* Elites (mini-bosses) and Hordes of the Void (`EliteMobs`, `VoidHordes`)
- [x] *(Extra)* Bearers have 50 health; Vitality gives 2 health and a little speed per point; an elixir and a tonic per act with new bottles (`scripts/make_potions.py`)
- [x] *(Extra)* Armor set pieces give their own bonus, and a full set all of them with its own (`scripts/set_pieces.py`); class unique weapons roll ranks in a random skill of their class
- [x] *(Extra)* Longer cooldowns on the strongest skills; each Clay Warden standing makes the next slower to shape; the Clay Warden redrawn as a guardian of Khemet; the Necromancer's Concord steals 5% for one below a quarter of their health
- [x] *(Extra)* Fire never takes hold in a protected place; the Waystone redrawn as a lit runed obelisk; Homeward, a boots enchantment that takes the Bearer back to Sulthari
- [x] *(Extra)* Enemies follow the Bearer's level (within their region's and act's floor) instead of rolling up to the region's top
- [x] *(Extra)* Dev tools: `-PplaceShots=empire/place,waystone,mobs,bosses,boss:<id>` (an Aetheris world made on demand)
- [x] *(Extra)* Every creature redrawn after the user's bestiary sheet (`art/concepts/bosses/bestiary_sheet.png`): bigger, readable silhouettes, glowing eyes and marks (`scripts/creature_kit.py`: horns, bat wings, spikes, halos, drawn faces; `Model.scale` for bigger creatures with more pixels); the Void Wretch and Stalker rebuilt; Void Zombies and Void Skeletons in every region and in the hordes (`scripts/make_void_kin.py`)
- [x] *(Extra)* The nineteen bosses remade so their presence shows they are broken (`scripts/make_boss_figures.py`): a jointed body of boss proportions with hands that have fingers and a thumb, a face sculpted in 3D (a jutting brow, nose, cheekbones, a jaw that opens, teeth, eyes of light in dark sockets), snapped horns, cracked halos and crowns, rag capes that sway, the torn shackles and split Law seal of every Broken Oath, the cracked sin core of every Archsin; in the second phase the boss is drawn broken (its `_broken` texture split by fractures of its colour, its `p2_` bones breaking out: shards orbiting, flames, eyes torn open), synced by `SoFEBossEntity.shownPhase`; dialogue portraits drawn from the models (`scripts/model_portrait.py`); `-PplaceShots=boss2:<id>` shows a boss in its second phase
- [x] *(Extra)* A Reward Coffer for each Bearer in the fight, in a ring in the arena: only its owner opens it, it vanishes when opened and fades after thirty minutes unopened (`RewardCoffer`); death shows "You have died" and lets the Bearer watch as a spectator over where they fell for ten seconds before they stand again (`DeathSpectate`); guns held the right way round; a soulbound item that does not fit in a full pack falls at the owner's feet instead of looping forever; Blueprint loot without a named recipe gives one the Bearer does not know
- [x] *(Extra)* Every boss has a signature attack born of its weapon (`SoFEBossEntity.Signature`, `Signatures`): a wind-up the Bearers can read (its name on screen, its shape drawn on the floor in particles, its own `signature` animation), then the blow; damage scales with the boss's attack, and it comes sooner in the second phase. Kaleth's Cleave of Embers, Serath's Crimson Frenzy, Mirael's Embrace of the Veil, Thessyn's Eight-Legged Impalement, Dormiel's Lantern of the Last Sleep, Goldarc's Charge of the Gilded Shield, Nixara's Weighing of Debts, Fenrath's Rending Pounce, Shadeyn's Shattered Blade, Solrath's Spear of the False Sun, the Sentinel's Piston Slam, Vorath's Wrath Splitter, Luxara's Storm of Wings, Morthis's Grasp of the Marsh, Avarok's Sack of Avarice, Gularth's Butcher's Chop, Envyris's Envious Reaping, Prython's Fall of the Proud Sun, Nahrazel's Rending of the Seal; boss textures at twice the pixels (per-face UVs, `Model.density`), angry brows, bigger heads, bat wings with fingers and a scalloped membrane
- [x] Parsivan, Khemet and Aureum regions: biomes, fixed structures, Veil unlocks per act, Star Lapis / Solar Gold / Orichalcum worldgen and tiers (`SoFEFeatures`: Star Lapis in Parsivan's heights, Solar Gold in Khemet's sands, Orichalcum deep in Aureum, Raw Aetherium rare and very deep in every region, Imperial Marble in Aureum's hills)
- [x] *(World)* Parsivan, Khemet and Aureum building blocks with corrupted variants; corrupted-to-intact swap when a region is liberated (`RegionHealing`: the Archsin's first fall heals every corrupted block in its region's places, a slice each tick, once per world; places built later are healed as they are built)
- [x] Parsivan: Luxara (Laleh's voice in the Enchanted Gardens): charms (a charmed Bearer walks to her and cannot hurt their allies), illusions in her shape, mirrors that throw blows back in her second phase (`LuxaraEntity`)
- [x] Khemet: Morthis (Catacombs gated until Luxara falls): never moves, slows everyone near, drowsy bolts, the marsh's mummies rise for him (`MorthisEntity`); the sultan's diary twist (`act3/the_sultans_diary`)
- [x] Aureum: Avarok (steals from each Bearer, never story items or Relics, grows with every theft, gives it all back on death or reset, even to those who left: `AvarokEntity.Hoard`), Gularth (eats the floor, which comes back, and grows up to twice his size), Envyris (a copy of each Bearer's own hero, up to three, the classes of those who hurt her most; answers each Bearer with their last skill)
- [x] Broken Oaths III–IX, each with the inverted mechanic of its Law: Mirael (fades, comes back behind you, whispers confusion), Thessyn (webs, venom, false chests that hide spiders), Dormiel (clouds of slumber, nightmares), Goldarc (volleys of coins, a ward of gold), Nixara (slips between stalls, false gold, the forum's trapdoors), Fenrath (swallows a Bearer, acid), Shadeyn (shades in copies of a Bearer's gear, blows glance back)
- [x] Temptation dialogues for each Archsin, with a line of its own for the Bearer whose weakness it is (`scripts/make_temptations.py`)
- [x] "Sulthari is under siege" transition to Act V (`act4/sulthari_besieged`); the main quests of Acts III and IV (`scripts/make_act34_story.py`)
- [x] Add empire-specific mobs and loot (eight creatures, see below; each boss leaves its own Relic, twelve new ones)
- [x] *(Rules)* Act III and IV traits for vanilla mobs (frost and fire arrows, shorter creeper fuse, blinding and teleporting endermen, web-shooting spiders), and Act V's (two-arrow volleys, charged creepers, faster zombies) (`MobTraits`)
- [x] Jeweler, Purifier, Tempering Anvil and remaining ores (from [Pociones.md](Pociones.md)): the Jeweler's Bench cuts rough gems for Dinars, opens sockets (250 / 750 / 2,000 Dinars) and sets cut or Oath gems (`JewelerBlock`, `Sockets`); the Purifier turns Black Aetherium (dropped by the Void's creatures) into aetherium; the Tempering Anvil tempers an affix by a tenth (three times per item, 1 aetherium) or rerolls the weakest (2 aetherium) (`TemperingAnvilBlock`)
- [x] *(Annex)* Remaining secondary materials: Moonsilk (Mirage Dancers and Thessyn), Sunreed Papyrus (sugar cane and Solar Gold powder), Imperial Marble (quarried in Aureum; polished, bricks and pillars from it); Void Crystal and Void Ink
- [x] *(Annex)* 7 sin gems (rough, cut, oath) (`SinGem`): rough from mining a mod ore (3%, the region's sin; the Thief doubles one in ten), cut at the Jeweler, Oath Gems from the Broken Oaths (their Archsin's sin: always the first time, then one fight in three)
- [x] *(Annex)* Favor per empire (6 ranks, discounts, offer unlocks): Stranger to Exalted at 0 / 100 / 300 / 600 / 1,000 / 1,600 points, 2% off per rank, `min_favor` offers; earned by trading with the empire's merchants (a tenth of the price; a twentieth when selling), finishing quests (80, in the empire where they end) and its bosses (150, an Archsin 400) (`EconomyData`, `MerchantService.gainFavor`); shown in the shop's title
- [x] *(Annex)* Liberated camps with merchant copies after each Archsin: four camps (Nordrath by the Burning Citadel, Parsivan between the Baths and the Silk Road, Khemet between the Catacombs and the Marsh, Aureum between the Market and the Golden Vaults), protected, with a palisade, tents in the empire's colours, a Waystone and a Personal Vault; their alchemist and smith (Ferid's and Dilara's offers, half the stock, the empire's Favor) trade once the region's Archsin has fallen (`EmpireBuilder.camp`, `scripts/make_camps.py`)
- [x] *(Annex)* Zahir the Wanderer (traveling caravan every 3 days): he stops for three days at a random liberated camp, then moves on; rough gems, secondary materials, ingots and Void Crystal, one of each per Bearer (`ZahirCaravan`)
- [x] *(World)* Void Gate under the Great Observatory (after Envyris, 12 Eyes of Ender); Void Crystal and Void Ink: a pavilion beside the Observatory sealed until Envyris falls, a shaft down to a vault with twelve empty End portal frames (`SultharisBuilder.voidGate`); Void Crystal ore on the End's outer islands and in End city chests; Void Ink at the Alembic
- [x] *(Rules)* Waystones and Vaults in liberated camps and dungeon entrances; side quests per region, with the Parsivan, Khemet and Aureum fate choices; Bearer quests for Acts III–IV: three refugees in each camp give The Dreaming Court (wake the court or let it dream), The Souls Below (guide the kings or bind them as guardians), The Gold of the Courts (restore the Law or share the gold), The Last Silk Caravan, The Sunken Archive and Bread and Circuses; ten Bearer quests (two per hero, with their class Relics); one refugee in each camp remembers the fate (`scripts/story_catalog_act34.py`, `scripts/make_story.py`)
- [x] Write and translate (en/es) the Act III–IV dialogue: the main story (Sprint 7, phase B), the nine refugees' lines before and after their region is freed, the quests' offers, reminders, choices and endings, and what each Bearer says in Acts III and IV
- [x] *(Annex)* Kerem and Sister Nilufar as NPCs: Kerem the Cutter in the Bank beside his Jeweler's Bench (cut gems, buys rough ones), Sister Nilufar by the Void Gate beside her Purifier (aetherium, buys Black Aetherium and Void Ash); the Tempering Anvil stands by Dilara
- [x] *(Annex)* Personal loot (Reward Coffer) for every Archsin and Broken Oath

## Extra — The empire capitals

- [x] Three capitals after the concept art, on a terrace whose shore slopes back to the land around (up a hill or down to the sea), with walls, four gates (the south one the empire's own), two avenues, a plaza, a bazaar, furnished houses (one in four with corrupted walls that heal with the region), roads out and five citizens (`CapitalCity`, one class per city):
  - **Isfaran** (Parsivan, 7200, -400, 224 × 224, `Isfaran`): violet houses under stepped teal roofs, some domed; the palace on its terrace with a great violet dome on a drum of windows, teal domes, an iwan for its door and four minarets with purple flags; the pool glowing violet from below; hanging gardens with vines, glow berries and a pavilion on top; domed kiosks; the south gate between white towers with spires
  - **Neferet** (Khemet, 6800, 3200, 256 × 256, `Neferet`): the river along the west wall with quays, palms and boats; the temple behind two pylons painted with golden ankhs, a hall of columns under a teal dome; an avenue of sphinxes and the Great Sphinx; the Great Pyramid capped in gold and a lesser one; the royal tomb cut into a massif of red rock; stepped pyramids; houses under little teal pyramids; a pylon gate
  - **Aurelion** (Aureum, -5400, 2800, 320 × 320, `Aurelion`): marble streets and blue and gold avenues; the Imperial Palace, a Byzantine hall under a great golden dome with golden half-domes and bell towers, behind a portico under a blue pediment; the Colosseum on its own hill (`Colosseum`: three storeys of arches, the cavea, the emperor's box); the Pantheon with its oculus; two long basilicas; temples with columns all round; little round temples; the column of victory and a triumphal arch at the forum; the aqueduct on two storeys of arches coming from the west hills; a gatehouse crested with gold; fields outside the walls
- [x] New roof blocks for the concepts: Aureum Royal Tiles and Parsivan Violet Tiles, each with stairs, slab and wall (`scripts/make_empire_blocks.py`)
- [x] Interiors: every house has a rug in the empire's colours, a bed, a kitchen corner, a table with candles and chairs, storage, a trade (a loom in Isfaran, a cauldron in Neferet, shelves in Aurelion), a plant and a banner; the palaces, temples, basilicas and the Pantheon have carpets, thrones on daises, statues (armor stands that cannot be stripped), braziers, divans, banners and chandeliers
- [x] The Colosseum of the Shadeyn fight grows to 144 × 120 (four storeys, the sand 56 × 44), built by the same `Colosseum` builder as Aurelion's, its marble cracked and its ground arches barred so the only way in is behind the Sealed Gate
- [x] `./gradlew runClient -PplaceShots=at:x:y:z:yaw:pitch` takes a picture from any spot, for the insides of buildings
- [x] Flags and great banners of the empires, as Skarnhold's: emblems in banner patterns on flags and banners, flagpoles round the plazas and on the towers, a great banner for each empire (`EmpireFlags`, `scripts/make_empire_banners.py`)
- [x] Nothing can be taken from a protected place, by anyone: containers, pots, lecterns, berries, signs, armor stands, item frames, paintings, boats and carts are protected from players (even in creative or as operators), projectiles and explosions (`ZoneAction.TAKE`); checked with `./gradlew runClient -PprotectCheck`
- [x] JEI and Xaero's Minimap in the pack (runtime dependencies in `build.gradle`; checked that the game loads, enters a world and opens the screens with them)
- [x] Skins for the 66 story NPCs (`scripts/make_npc_skins.py`, `textures/entity/npc/`): clothes by empire and trade, each person's skin, hair, beard, age and headwear; the Bearers in plain base looks so they never outshine a player (players keep their own skins; the class outfit is drawn the same on a player and on the hero NPC); `-PplaceShots=npcs:0,npcs:1` shows them all
- [x] A Waystone in each capital; `CapitalsTest` checks their region, zone, Waystone and distance to the other places
- [x] Fix: a Bearer kept nothing after dying (class, level, skills, attributes, story, Dinars, Favor, Waystones), so the skill tree and the character sheet would not open; the capabilities no longer invalidate their data before it is copied to the respawned player (checked with `./gradlew runClient -PdeathCheck`)

## Sprint 7.5 — Multiplayer & Co-op (Weeks 19-20)
See [Anexos.md](Anexos.md#a5-co-op-bonus-pact-of-the-empires).
- [x] *(Annex)* `Pact` and `PactManager` (up to 5 players, `/sofe pact` commands): `Pact`, `PactData` (saved with the world), `Pacts`; `/sofe pact invite|accept|decline|leave|kick|list`, the invitation with buttons in the chat, the leader passes on when they leave, a Pact of one dissolves
- [x] *(Annex)* Shared XP and loot find bonus while within 48 blocks: kill XP split among the members together with +10% per extra member; +5% rarity per ally, up to +20% (`PactRules`, all values in the server config `[pact]`)
- [x] *(Annex)* Class Concord and Five Pillars bonus: the group is the Pact together (or the Bearers near when there is no Pact); the Five Pillars add a Flask charge
- [x] *(Annex)* Downed state and revive in boss arenas: a participant who would die is downed for 30 s while another stands; an ally crouching beside them for 3 s lifts them to 30% health (`PactHooks`)
- [x] *(Annex)* Kinship Chests in dungeons (2+ players): beside the Waystone of every dungeon; each Bearer present takes their own share once (Dinars by act, often a rough sin gem), never gear the story needs (`KinshipChest`)
- [x] *(Annex)* Echo shrines so late joiners get boss credit without changing the world: done by the lairs (`BossLairs`): a boss rises again for any Bearer who has not beaten it, the arena seals only for the fight and the healed land stays healed, so no separate `sofe:echo` dimension is needed
- [x] *(Annex)* Council of Sulthari recovery of lost story items: `/sofe council restore` in Sulthari gives back the Flask and the Shard of every Archsin beaten, from the story's credit (`StoryItems`)
- [x] *(Annex)* `uniqueBearersPerServer` option and per-player story cutscenes (`TakenBearers`; story scenes were already per player)
- [x] *(World)* `gateMode` option (`PER_PLAYER`, `PACT_ESCORT`): with `pact_escort`, a Pact member who meets a Sealed Gate's condition lets the members within 8 blocks through
- [x] *(Rules)* Instanced prologue for late joiners: the Act I quest is per player (its invasion spawns for the player who reaches it, the Brass Sentinel rises from its lair for whoever owes it), so a late joiner plays it in the real Sulthari
- [x] *(Rules)* Disconnect rules during boss fights: a participant who leaves keeps their place; if the boss falls while they are away, their credit and loot wait for their return (`OwedRewards`, UC-32); boss health grows +60% per extra player in the arena and never shrinks mid-fight (damage unchanged)
- [x] *(Rules)* `/sofe` admin commands: `/sofe progress <player> show|act|defeat`, `/sofe unstuck`, `/sofe item restore`, `/sofe pacts` (permission 2; changes are logged)
- [x] *(World)* JourneyMap plugin: region borders, locked shading, objective waypoints; check the Xaero API for basic waypoints: Xaero's Minimap is in the pack; it has no stable waypoint API, so it maps the world without integration (as planned in Mundo.md W3); a JourneyMap plugin stays optional and unbuilt while JourneyMap is not in the pack
- [x] *(Annex)* One-copy-equipped rule for Relics; `relicBinding = pact_only` (only the owner and their Pact may pick a bound Relic or Legacy up)
- [x] *(Annex)* Pact members' health on the HUD (`PactHudOverlay`: name, Bearer, health, near/away/offline/downed) and the Pact in the Journal
- [ ] *(Annex)* Test on a dedicated server with 4+ players (the mod loads and its GameTests pass on a dedicated server; a real production server made a journey at 20 TPS and a real client joined it and got the Eclipse Festival, `scripts/server_check.py`; a session with 4+ real players is still to be played)
- [ ] *(Annex, stretch)* Combo skills between two players
- [x] *(Extra)* The character sheet fits its columns to the language in use (Spanish names no longer overlap)
- [x] *(Extra)* Story NPCs: the great of each act embroidered after the user's reference of Ozhan (folds, embroidered hems and cuffs in the empire's pattern, frogging, sashes, capes, stern faces), and 3D parts for 41 of them: turbans, beards, capes and robe skirts (`NpcAccessoryLayer`, `scripts/make_npc_skins.py`)

## Sprint 8 — Act V: The Ascension & Polish (Weeks 21-24)
- [x] Sulthari under siege; Ozhan revealed as Solrath, the False Prophet (Law X), in the Temple of Sulthari: pillars of holy light, holy bolts, and in his second phase the beaten Broken Oaths raised as echoes (`SolrathEntity`; echoes are weaker and give nothing)
- [x] Prython (Pride) at the Celestial Spire, using the mechanics of the six previous Archsins in turn (`SinPowers`), his Pride turning blows back in the second phase; his offer at the edge of defeat
- [x] Prython's offer: refusal, and the secret "Crowned in Ash" bad ending with one animation per Bearer and a hidden advancement: taking the crown plays a scene on that player's screen only (`CrownedInAshScreen`, `SceneService`, "play_scene" and "award_advancement" effects): their own Bearer, in their own skin and gear, crowned before a throne as the city of their empire burns under a false sun (Aureum's temples, Khemet's pyramids, Parsivan's domes and falling stars, Nordrath's longhouses, Sulthari's minarets), their people kneeling, four lines of their own; then the offer comes again. The hidden advancement `sofe:secret/crowned_in_ash` shows no hint and says nothing in the chat; nobody can hurt a Bearer weighing the offer or watching the scene. `-PplaceShots=scene:<bearer>:<second>` screenshots it
- [x] Aetherium-tier gear (Orichalcum + Raw Aetherium + Void Crystal): a 3D set per class at level 98, pale aetherium crystal shot with the Void's violet (`scripts/armor_catalog.py`: the Aetherium Aegis, Requiem, Astrolabe, Shade and Dominion, each with its full-set bonus, repaired with aetherium), made at the Imperial Forge from a Blueprint with Orichalcum, aetherium shards and Void Crystal (or found as loot at their level); the Aetherium axe, shovel, hoe and mining hammer beside the pickaxe, each with an aetherium head, a Void Crystal and an Orichalcum haft (`scripts/arsenal3_catalog.py`; tools drawn on the vanilla ones, `make_pickaxes.tool`)
- [x] Sealing Quill quest: Void Ink from the Outer Void, required to open the Inverted Throne: after Prython, Sister Nilufar sends the Bearer through the Void Gate (`act5/the_sealing_quill`); the Quill is crafted from a feather, Void Ink, a Void Crystal and an aetherium shard; carrying it finishes the step ("obtain_item" objective); the Inverted Throne's gate asks for Prython beaten and the Quill carried; soulbound, and the Council gives it back if lost
- [x] The Spire inverts and sinks to the Inverted Throne (built beneath Sulthari, reached by a winding stair; a dimension of its own is not needed)
- [x] Dimensions `sofe:inverted_throne` and `sofe:codex_interior`: neither is needed. The Inverted Throne is built beneath Sulthari, reached by its stair, and the moment inside the Codex is a scene on the Bearer's screen (`EndingScreen`), so the world keeps one dimension (and no mod sees a dimension of its own to break)
- [x] Nahrazel, the First Fallen: colossus of ash, seven sins at once (and the Archsins raised as echoes), rewriting the seal inside the Codex (the arena turns to its pages; seven Seals hold him until they are broken) (`NahrazelEntity`, `SealGlyph`)
- [x] Write and translate (en/es) the Act V dialogue (`scripts/make_act5_story.py`), the ending, the fifteen region slides and the ten epilogues
- [x] Ending inside the Codex, the region fate slides and the Bearer epilogues (full and unfinished versions), plus the eighth-lock sequel hook: when Nahrazel falls, a scene on that Bearer's screen (`EndingScreen`, rules in `com.sofe.story.Epilogue`): inside the Codex the seven locks turn under the Quill and the Bearer's own sin tempts them one last time; they wake as the black aetherium runs clear; one slide per region, its skyline at dawn, picked by the fate they chose there (a greyer "unsettled" slide where they chose none); their own epilogue with their own figure on a hill, full when their four Bearer quests are done; the eighth, dark lock; the credits. Space moves on, Escape skips. The advancement "The Seal Rewritten"; then the Council tells them how to call Echoes. The drawn scenes stand in for the final splash arts (`-PplaceShots=ending:<bearer>:<card>`)
- [x] Implement multiplayer boss scaling: health +60% per extra player (Sprint 7.5), and with two Bearers or more every signature attack also marks up to three of the others, farthest from its target first, with a ring of its own that strikes for less as the blow falls (`SoFEBossEntity.markEchoes`; not for echo bosses)
- [x] *(Rules)* Act V traits for vanilla mobs (corrupted zombies, arrow volleys, charged creepers): faster zombies, two-arrow volleys and charged creepers came in Sprint 7 (`MobTraits`); now Act V's vanilla zombies, husks and drowned are corrupted, with violet eyes that glow in the dark (`CorruptedEyesLayer`, sent to the players who see them)
- [ ] Add sound effects and ambient music
- [ ] Localization (English + Spanish)
- [x] *(Rules)* Post-game: all regions open, repeatable Echo fights: every region is open from Act IV on; once Nahrazel has fallen to a Bearer, kneeling (crouching) for a few seconds in the middle of the lair of a boss they have beaten raises it again, whole, for its gems, materials and Dinars (`BossLairs.kneel`); a Relic a boss already gave them never drops again (`StoryProgress.relics`, `SoFEBossEntity.onlyNewRelics`)
- [x] *(Rules)* Free mode, integrity check on load and `incompatible_mods.json`: a journey whose story cannot run plays in free mode (`FreeMode`: `SoFEWorld.regionMap` answers as for no journey, so the story, seals, lairs and journey rules wait while items, gear, ores and stations work); the integrity check on load (the map of Aetheris, the story's data, every story boss) logs its result; a mod on the list (empty for now) is named on the title screen, and `allowIncompatibleMods` lets a server play on; every player is told why on joining, in free mode or in a world that is no journey
- [x] *(Rules)* Compatibility pass with popular mods (content, maps, waystones, graves, extra dimensions): checked in a real (production) client and server: **FTB Essentials** (`/home`, `/rtp`, `/back`, `/spawn`: a Bearer sent into a sealed region by any of them is sent back within a second, `CompatCheck`), **Essential** (its menu opens from an *Essential menu* button on the SoFE title screen, `TitleShot`), Chunky, JEI, Xaero's Minimap and the performance mods; grave mods (Corpse, Gravestone, Corail Tombstone, You're in Grave Danger) turn the SoFE corpse off on their own; Supplementaries' Amendments screen is off (`pack/config`). Tools: `scripts/pack_check.py --check compat|title|join --extra essential,ftb`, `scripts/server_check.py`
- [x] *(Rules)* Accessibility: rarity text labels, subtitles, shake/flash/particle options: the rarity's name over gear on the ground (`rarityLabels`), the client option `reduceMotion` for the story's scenes, and the vanilla accessibility options covering the rest (docs/Jugabilidad.md, G13)
- [x] *(Rules)* Server README: pre-generation, fair-play map settings, offline-mode UUID note ([Servidor.md](Servidor.md)): install, `level-type=sofe\:aetheris`, which mods go on the server, memory, Chunky, the multiplayer options, fair play, online and offline mode, other mods; checked on a real dedicated server that a client joined
- [ ] Performance optimization: the pack's performance mods are in and checked ([Rendimiento.md](Rendimiento.md): Embeddium, Entity Culling, ImmediatelyFast, ModernFix, FerriteCore, Canary, Saturn, Memory Leak Fix, Dynamic FPS; `pack/config`; `scripts/pack_check.py` plays a production client with 4 GB and logs fps and memory); still to do: the bosses' rendering, and an hour on a PC like the target (Ryzen 3 3200G, GTX 1650, 8 GB)
- [ ] Full testing pass
- [ ] *(Annex)* Final title screen: logo, panorama, key art and main theme
- [ ] *(Annex)* The Codex screen (lore, bestiary, splash art gallery) and the illustrated Codex Map
- [ ] *(Annex)* Row of icon buttons on the title screen (Codex, Codex Map and others from `art/concepts/menu_icons_mockup.jpg`), once those screens exist
- [ ] *(Annex)* Themed pause menu, Journal and loading screens with lore tips
- [ ] *(Annex)* Swap every splash art and 3D model placeholder for the final asset (list in [Anexos.md](Anexos.md#a2-splash-arts-and-3d-models))
