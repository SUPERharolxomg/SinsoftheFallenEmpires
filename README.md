# Sins of the Fallen Empires — Minecraft Mod

A linear dark fantasy RPG campaign for Minecraft: five Bearers, five fallen empires, seven Archsins, ten Broken Oaths and the First Fallen waiting beneath the last free city.

---

# 1. Overview

## 1.1 Vision
A Minecraft mod with a skill-based class system, an original dark fantasy story told in five acts, boss fights inspired by the Seven Deadly Sins and dungeon crawling. Built in Java on Minecraft Forge as an open-source project.

## 1.2 Mod Name
**Sins of the Fallen Empires** (SoFE)

## 1.3 Target Platform
- Minecraft Java Edition **1.20.1**
- Minecraft Forge **47.x**
- Single-player and multiplayer (co-op)

## 1.4 Tech Stack
- Java 17 (Gradle toolchain; newer JDKs can run Gradle)
- Minecraft Forge MDK
- Gradle (build system)
- JSON (configuration, loot tables, recipes, skill values)
- GeckoLib (animated bosses) and Curios (extra equipment slots)
- Optional: JourneyMap and Xaero's Minimap / World Map integration
- Git + GitHub, GitHub Actions (CI/CD)

## 1.5 Game Structure
- **Linear campaign** (Act I → Act V). The mod has its own title screen, main menu and default world preset (`sofe:aetheris`) that starts the player in Sulthari on the Night of the Eclipse.
- **Open world, sealed by the story**: one world with a fixed map of the five empires. Inside unlocked regions it is normal Minecraft; regions, cities and dungeons stay sealed until the player completes the act or mission that opens them. The Nether (the Burning Deep) and the End (the Outer Void) open with the story too.
- **Five playable Bearers**, each a named hero with their own class, story and temptation.
- **Co-op multiplayer**: playing in a group (Pact) gives bonuses, and unique items are unique **per player**, not per world.

---

# 2. Story

## 2.1 Premise and Tone

The adventure does not begin in a village. It begins in **Sulthari**, the most advanced city in Aetheris, on the night the seal that held the **First Fallen** breaks beneath its Great Observatory. Five people are marked by shards of the **Void Codex** and must cross the five fallen empires, defeat the **Seven Archsins** and their **Ten Broken Oaths**, and finally kill **Nahrazel, the First Fallen**, the devil of this world.

- **Tone:** medieval dark fantasy with empires inspired by the Ottomans, Egyptians, Persians, Norse and Romans/Byzantines. Everything is fictional: names, religions and maps are our own.
- **Technology:** Sulthari mixes brass, clockwork and aetherium crystals (the world's energy), in sharp contrast with the corrupted ruins everywhere else.
- **Originality:** we take the *structure* of Diablo II (acts, classes with skill trees, regional bosses) but no names, skills, characters or text. The Necromancer does not raise skeletons, he binds souls; the Sorceress writes spells with constellations; the King issues decrees that change the rules of combat.
- **Theme:** every hero carries the temptation of one sin. Defeating an Archsin also means overcoming a weakness of their own.

## 2.2 The World — Aetheris

A thousand years ago, five empires signed the **Pact of the Five Crowns** and ruled Aetheris in peace under ten shared laws. Today only Sulthari still stands; the other four are corrupted ruins.

| Empire | Inspiration | Pillar | Landscape | Current state | Palette |
|--------|-------------|--------|-----------|---------------|---------|
| **Sulthari Dominion** | Ottoman | Strategy & science | Plateaus, walls, domes, brass observatories | Last free city, sealed behind its walls | Gold, red, dark brown |
| **Nordrath Clans** | Norse | Strength & exploration | Tundra, fjords, volcanic forges | Taken by Wrath and Gluttony | Ice blue, gray, blood red |
| **Parsivan Court** | Persian | Art & diplomacy | Hanging gardens, palaces, the Silk Road | Taken by Lust | Purple, turquoise, silver |
| **Khemet Ascendancy** | Egyptian | Knowledge & death | River valley, pyramids, catacombs | Taken by Sloth | Sand, turquoise, gold |
| **Aureum Republic** | Roman/Byzantine | Law & engineering | Marble cities, aqueducts, colosseums | Taken by Greed and Envy | Marble, gold, royal blue |

**Aetherium** is the crystalline energy that powers magic and machines. Where an Archsin reigns, aetherium turns black (**Black Aetherium**), and that is the resource the player purifies to upgrade gear.

## 2.3 The Fall

The Void Codex is not a book of power: it is the **prison of Nahrazel**, the First Fallen, a being older than the gods of Aetheris who feeds on mortal pride.

1. **The discovery.** The five emperors found the Codex beneath the Sulthari desert. Its pages promised "the knowledge you lack to become gods".
2. **The Ritual of the Five Crowns.** Each emperor read one page aloud on the same night. The seal cracked, and seven fragments of Nahrazel came out of the crack: the **Seven Archsins**.
3. **The corruption.** Each Archsin possessed a realm and twisted its best guardians, those who kept the Ten Laws of the Pact. By breaking their law they became the **Ten Broken Oaths**.
4. **The last seal.** The sultan of Sulthari, repentant, gave his life to close the crack beneath the Great Observatory. Sulthari survived by sealing itself behind walls of aetherium. The other empires fell.
5. **A thousand years later: the Night of the Eclipse.** An eclipse weakens the seal. The crack opens again, the Codex splits, and five shards pierce five people in the city. They are the playable characters: the **Bearers**.

**What nobody in Sulthari knows yet:** the Archsins do not want to conquer the world. They want to gather the seven fragments to free Nahrazel. Every Archsin the player defeats hands over a fragment of the Codex, and the player, without knowing it, is doing their work for them.

## 2.4 The Seven Archsins

| # | Archsin | Sin | Realm | Region | Fight mechanic |
|---|---------|-----|-------|--------|----------------|
| 1 | **Vorath** | Wrath | Burning Citadel | Nordrath | Rage phases; the arena shrinks |
| 2 | **Luxara** | Lust | Enchanted Gardens | Parsivan | Charms the player, illusory clones, mirrors |
| 3 | **Morthis** | Sloth | Stagnant Marsh | Khemet | Slows, summons minions without moving |
| 4 | **Avarok** | Greed | Golden Vaults | Aureum | Steals items from the inventory during the fight |
| 5 | **Gularth** | Gluttony | Feast Halls | Nordrath (underground) | Devours the terrain and grows |
| 6 | **Envyris** | Envy | Shadow Throne | Aureum (ruins) | Copies the skills of the player's class |
| 7 | **Prython** | Pride | Celestial Spire | Above Sulthari | Uses the mechanics of the six before |
| — | **Nahrazel, the First Fallen** | Source of all | Inverted Throne | Beneath Sulthari | Final boss, three phases (see Act V) |

## 2.5 The Ten Broken Oaths

Each Broken Oath was once the guardian of one of the Ten Laws of the Pact. Each one fights with a mechanic that is the inversion of the law it betrayed.

| # | Law of the Pact it guarded | Broken Oath | Title | Serves | Dungeon | Mechanic |
|---|----------------------------|-------------|-------|--------|---------|----------|
| I | You shall not raise your sword against one who surrenders | **Kaleth** | The Burning Blade | Vorath | Nordrath Forge | Fire and thrown weapons; finishes off stunned targets |
| II | You shall honor the blood of your fathers | **Serath** | The Blood Maiden | Vorath | Nordrath Arena | Life steal, blood pools |
| III | You shall not take a heart that is not given to you | **Mirael** | The Whisperer | Luxara | Parsivan Baths | Confusion and invisibility |
| IV | You shall speak the truth before the throne | **Thessyn** | The Silk Weaver | Luxara | Silk Road | Webs, poison, fake chests |
| V | You shall keep watch while others sleep | **Dormiel** | The Dreamer | Morthis | Khemet Catacombs | Sleep clouds, summoned nightmares |
| VI | You shall not hoard what another needs | **Goldarc** | The Coinlord | Avarok | Aureum Treasury | Gold projectiles, shields |
| VII | You shall not rob one who trusts you | **Nixara** | The Hollow Merchant | Avarok | Aureum Market | Trapdoors, fake items |
| VIII | You shall share the winter bread | **Fenrath** | The Devourer | Gularth | Nordrath Caverns | Swallows the player, acid |
| IX | You shall not covet another's crown | **Shadeyn** | The Mirror | Envyris | Aureum Colosseum | Clones the player's gear |
| X | No one shall stand above the Law | **Solrath** | The False Prophet | Prython | Sulthari Temple | Holy damage, resurrects fallen Oaths |

## 2.6 The Story in Five Acts

The player travels the map in an arc: out of Sulthari to the north, then east and west, and back home for the ending.

### Act I — The Night of the Eclipse (Sulthari)
The Eclipse Festival in Sulthari: brass tramways, markets, the Great Observatory lit up. During the eclipse the seal breaks, Void creatures invade the lower district and a Codex shard pierces the chosen character.
- **Tutorial:** survive the invasion and learn the first 3 skills.
- **Key encounter:** the other four Bearers appear as NPCs; they gather before the Council of Sulthari.
- **Mission:** Grand Vizier **Ozhan** reveals the truth about the Pact and orders them to recover the fragments before the Archsins do.
- **Act boss:** the **Brass Sentinel**, a city automaton corrupted by the crack.

### Act II — The Northern Campaign (Nordrath)
The Norse clans live in endless war: Vorath brings them back to life every dawn to fight again.
- Nordrath Forge → **Kaleth** (Law I)
- Nordrath Arena → **Serath** (Law II)
- **Vorath** (Wrath) at the Burning Citadel. When he falls, the Norse warriors can finally rest.
- **Twist:** Vorath's fragment speaks with Nahrazel's voice: "Thank you, Bearer."

### Act III — The Eastern Shadows (Parsivan and Khemet)
Parsivan is a paradise of illusion where no one wants to wake up; Khemet is a necropolis where the living refuse to move.
- Parsivan Baths → **Mirael** (Law III)
- Silk Road → **Thessyn** (Law IV)
- **Luxara** (Lust) at the Enchanted Gardens
- Khemet Catacombs → **Dormiel** (Law V)
- **Morthis** (Sloth) at the Stagnant Marsh
- **Twist:** in Khemet the player finds the diary of the sultan who sealed the crack. The Archsins need a Bearer to hand over the fragments *willingly*.

### Act IV — The Western Ruins (Aureum)
Aureum is a marble republic turned into a market of souls, with a colosseum where the dead fight for coins.
- Treasury → **Goldarc** (Law VI) and Market → **Nixara** (Law VII)
- **Avarok** (Greed) at the Golden Vaults
- Nordrath Caverns → **Fenrath** (Law VIII)
- **Gularth** (Gluttony) at the Feast Halls
- Aureum Colosseum → **Shadeyn** (Law IX)
- **Envyris** (Envy) at the Shadow Throne: the player fights a copy of their own character.
- **Twist:** a message arrives: Sulthari is under siege.

### Act V — The Ascension (Sulthari)
Prython has raised the Celestial Spire above the city, and Grand Vizier Ozhan turns out to be his servant.
- Sulthari Temple → **Solrath, the False Prophet** (Law X), who is Ozhan transformed.
- **The Sealing Quill:** Nahrazel's seal can only be rewritten with ink from the other side of the prison. The Bearer opens the Void Gate beneath the Great Observatory, crosses into the **Outer Void** and forges the Sealing Quill with Void Ink.
- **Prython** (Pride) at the Celestial Spire. Before dying, he offers the player the power to rule Aetheris in exchange for the six fragments.
- **The choice:** the player refuses. When Prython falls, the Spire inverts and sinks beneath the city down to the Inverted Throne.
- **Final boss: Nahrazel, the First Fallen.**
  - Phase 1: a colossus of ash.
  - Phase 2: uses all seven sins at once.
  - Phase 3: the player uses the seven fragments and the Sealing Quill to rewrite the seal from inside the Codex.

## 2.7 Ending and Epilogues

When Nahrazel dies, the Codex closes with the Bearer inside its pages for a moment. There the player sees the temptation of their sin one last time and rejects it. They wake up in the ruins of the Inverted Throne, and the black aetherium of Aetheris begins to turn clear.

The ending then shows what became of each empire, one scene per region, shaped by the choices the player made in the side quests (the **fates** of the regions, see [docs/Jugabilidad.md](docs/Jugabilidad.md#fates-and-epilogues-fallout-style)). It closes with the Bearer's own epilogue:

| Hero | Epilogue |
|------|----------|
| **Cassian** (Knight) | Refounds the Order of the Scale in Aureum, but now accepts anyone, whatever their empire. |
| **Ankhareth** (Necromancer) | Opens the gates of Khemet's underworld and walks with the souls, his master among them, on their last journey. |
| **Shirin** (Sorceress) | Frees Laleh, who dies in peace. She writes a new map of the sky in the stars with her sister's name. |
| **Rurik** (Thief) | Returns the gold of the Vaults to the Nordrath clans and rebuilds his village. He keeps a single coin. |
| **Azhar** (King) | Refuses to be emperor of Aetheris and calls a new Pact between equals. He keeps the sealed Codex beneath the throne. |

These are the full epilogues; a Bearer who did not finish their personal quests gets a different, sadder version.

**Sequel hook:** the seal has eight locks, not seven. No one knows which sin is missing.

---

# 3. The Five Bearers

The player picks one Bearer. The other four stay alive as NPCs in Sulthari and can be hired as companions (one at a time). Each comes from a different empire and carries the temptation of one sin.

| Class | Hero | Origin | Role | Primary stat | Resource | Unique mechanic | Temptation |
|-------|------|--------|------|--------------|----------|-----------------|------------|
| **Knight** | Cassian Dravo | Aureum | Tank / melee | Strength | Resolve | Stances: Shield or Charge | Wrath |
| **Necromancer** | Ankhareth | Khemet | Summoner / control | Will | Essence | Binds the souls of fallen enemies | Sloth |
| **Sorceress** | Shirin Azarvand | Parsivan | Ranged magic damage | Intellect | Mana | Constellations: combine 3 runes | Lust |
| **Thief** | Rurik "Ash" Grimsson | Nordrath | Fast damage / stealth | Agility | Energy + Marks | Steals buffs and items from enemies | Greed |
| **King** | Sultan Azhar Tevfirán | Sulthari | Leader / offensive support | Charisma | Authority | Decrees and the royal guard | Pride |

**Skill tree (10 skills per class):**
- Levels 1–10: 3 active
- Levels 11–20: 3 active + 1 passive
- Levels 21–30: 1 active + 1 passive + 1 ultimate

One skill point every 2 levels; max level 30. Respec at the Sulthari Training Grounds for Dinars.

Full hero stories, skill tables and visuals: [docs/Clases.md](docs/Clases.md).

---

# 4. Documentation

| Document | Contents |
|----------|----------|
| [docs/Clases.md](docs/Clases.md) | The five Bearers: story, motivation, temptation, unique mechanic, skills, visuals |
| [docs/Mundo.md](docs/Mundo.md) | World map, progression locks, protected places, minimap integration, where each material appears |
| [docs/Jugabilidad.md](docs/Jugabilidad.md) | Core gameplay rules: death, fast travel, quests and dialogue, controls, storage, difficulty, free mode, post-game, admin commands |
| [docs/Pociones.md](docs/Pociones.md) | Gear, rarities, affixes, mining, forges, potions, progression by act |
| [docs/Anexos.md](docs/Anexos.md) | Title screen and menu, splash arts and 3D models, material names, merchants, co-op bonus, per-player unique items |
| [docs/CasosDeUso.md](docs/CasosDeUso.md) | All use cases |
| [docs/Arquitectura.md](docs/Arquitectura.md) | Project structure, packages, design patterns, code examples |
| [docs/Pruebas.md](docs/Pruebas.md) | Testing strategy and test cases |
| [docs/Arte.md](docs/Arte.md) | Visual design guide, tools and art style |
| [docs/Sprints.md](docs/Sprints.md) | Sprint plan and tasks (Sprint 0 + 24 weeks) |

---

# 5. Open Source & Publishing

- **License:** MIT
- **Source code, issues, wiki and CI/CD:** GitHub (automated build on every PR)
- **Distribution:** CurseForge and Modrinth
- **Languages:** English and Spanish (`lang/en_us.json`, `lang/es_es.json`)
- Contributions: see `CONTRIBUTING.md` (issue and pull request templates included)
