# Gear, Potions and Mining

## Decision: yes to mining, as one of three sources

Recommendation: **yes to mining**, because it is what makes the mod feel like Minecraft rather than a separate game. But if everything comes from mining, combat loses its point; if everything comes from killing enemies, mining is pointless. The answer is a hybrid system where each source gives something the others do not:

| Source | How you get it | What it gives | What it does NOT give |
| --- | --- | --- | --- |
| **Loot** (enemies, dungeon chests, bosses) | Combat | Gear with random affixes, unique Relics, gems, recipe blueprints | Forge materials in bulk |
| **Mining and forging** | Mining each empire's ores and smelting them | Materials to craft and upgrade gear, gem sockets | High affixes or Relics: you cannot reach top gear without combat |
| **Trade** (Sulthari merchants) | Dinars dropped by enemies or earned by selling | Basic potions, common gear, repairs, respec | Nothing high tier: support, not a shortcut |

**Golden rule:** the best gear of each act needs all three. For example, a Relic dropped by Vorath is *awakened* at the forge with Glacial Iron mined in Nordrath and socketed with gems paid for in Dinars.

**Vanilla gear still works** (iron, diamond, netherite), but has no affixes or sockets. It carries Act I and then falls behind naturally.

## Rarities, affixes and slots

Every mod item has an **item level** (the level of the enemy or zone it came from) and a **rarity**. Rarity names are original, not Diablo II's.

| Rarity | Color | Affixes | Source | Example |
| --- | --- | --- | --- | --- |
| **Common** | White | 0 | Merchants, basic enemies, forge without gems | Brass scimitar |
| **Tempered** | Blue | 1–2 | Enemies and chests | *Burning* brass scimitar |
| **Imperial** | Yellow | 3–4 | Elite enemies, Broken Oaths | *Vizier's Fang* (generated name) |
| **Relic** | Gold | Fixed + 1 unique effect | Archsins and Oaths (targeted loot) | *Kaleth's Blade*: hits on stunned enemies burn |
| **Imperial Legacy** | Green | Fixed + set bonus | Pieces spread across an empire's dungeons | *Legacy of Nordrath* (4 pieces): +30% fire damage |

**Affixes.** One prefix and one suffix per 2 affixes, defined in JSON (`data/sofe/affixes/*.json`) with value ranges per item level. Examples: *Burning* (+fire damage), *of the Sentinel* (+armor), *Thirsting* (life steal), *of the Eclipse* (−skill cooldown).

**Class affinity.** Some affixes only roll on items tied to a class: +1 Clay Warden (Necromancer), +runes per spell (Sorceress), +Authority per hit (King). Every class can use everything, but loot is 60% biased toward the player's class to avoid a junk-filled inventory.

**Equipment slots.**

- **Vanilla:** helmet, chestplate, leggings, boots, main hand and off hand.
- **Mod:** amulet, 2 rings and potion belt, via the **Curios** API (a widely used dependency on Forge 1.20.1).
- **Sockets:** weapons and chestplates can have 0–3 sockets for **Oath Gems** (see Forges).

## Mining: ores by empire

Each empire has its own ore, generated only in its region, so unlocking a new region also opens a new crafting tier, like iron and diamond in vanilla. Coordinates, heights, vein frequency and tool tiers: [Mundo.md](Mundo.md#w4-resource-distribution).

| Ore | Empire | Where it is mined | Minimum tool | Main use |
| --- | --- | --- | --- | --- |
| **Sulthari Brass** (copper + zinc) | Sulthari | Plateaus, upper layers | Stone | Starter gear, automatons, flasks |
| **Glacial Iron** | Nordrath | Frozen caves, mid depth | Iron | Heavy weapons and armor, cold resistance |
| **Star Lapis** | Parsivan | Mountains, visible only at night | Iron | Staves, rings, mana affixes |
| **Solar Gold** | Khemet | Tombs and deep sand | Glacial Iron | Amulets, Essence and life affixes |
| **Orichalcum** | Aureum | Deep marble quarries | Solar Gold | Best craftable tier, sockets |
| **Raw Aetherium** | All | Near Void rifts, very deep | Orichalcum | Upgrading Relics, major potion ingredient |

**Nether and End materials.** The Nether (the Burning Deep, from Act II) gives Infernal Ember for fire War Oil and Relic awakening, and Wailing Soul for Sin Resistance potions and Necromancer affixes. The End (the Outer Void, from Act IV) gives Void Crystal for Aetherium-tier gear. Details: [Mundo.md](Mundo.md#w5-the-nether-and-the-end).

**Corrupted aetherium.** Corrupted enemies drop **Black Aetherium**. It is not mined: it is **purified** in the Sulthari Purifier into Raw Aetherium, so combat also feeds the forge.

**Gems.** Mining has a 3% chance of a **rough gem** (Ruby of Wrath, Emerald of Envy, etc., one per sin). They are cut at the Jeweler and socketed into gear. **Oath Gems** (perfect version) drop only from the Broken Oaths.

**Class synergies.** The Thief applies his 10% double loot (*Deep Pockets*) to mined gems too. The Necromancer's Clay Wardens can mine a block when no enemies are near.

## Forges and crafting

Crafting happens at **custom stations** in Sulthari (with a simpler copy at the camp of each liberated empire). The vanilla crafting table only makes Common items.

| Station | NPC | What it does | Input | Output |
| --- | --- | --- | --- | --- |
| **Imperial Forge** | Master smith Dilara | Crafts weapons and armor with 1 guaranteed affix | Empire ingots + blueprint | Tempered item of that empire |
| **Tempering Anvil** | Dilara | Rerolls 1 affix or raises its value | Item + Raw Aetherium | Upgraded item |
| **Relic Awakener** | Dilara | Levels up a Relic so it stays useful in later acts | Relic + act ore + Aetherium | Higher-level Relic |
| **Jeweler** | Kerem the cutter | Cuts gems, opens and fills sockets | Rough gem / item + Dinars | Cut gem / socketed item |
| **Purifier** | Sister Nilufar | Turns Black Aetherium into Raw Aetherium | Black Aetherium + coal | Raw Aetherium |
| **Alembic** | Alchemist Ferid | Potions (see next section) | Herbs + flasks | Potions |

**Recipes.** Imperial Forge recipes are not available at the start: they are learned from **Blueprints** dropped by Broken Oaths and dungeon chests (for example, *Blueprint: Nordrath Forge Axe*). This ties story progress to crafting (UC-11 in [CasosDeUso.md](CasosDeUso.md), which replaces the old UC-07).

**Durability.** Mod gear uses Minecraft durability. It is repaired at the Forge with its empire's ore or with Dinars; Relics never fully break, they stop at 1 and lose their bonuses.

## Potions and alchemy

Three routes, from easiest to strongest: **buy**, **distill** and **the Bearer's Flask**. Vanilla brewing-stand potions still work, but they do not restore Mana, Essence, Energy, Resolve or Authority.

| Potion | Effect | Buy (Ferid) | Distill at the Alembic | Enemy drop |
| --- | --- | --- | --- | --- |
| **Pomegranate Elixir** (minor / major / imperial) | Heals 4 / 8 / 14 hearts over 3 s | Minor, 15 Dinars | Parsivan pomegranate + brass flask (+ Aetherium for imperial) | Common |
| **Bearer's Tonic** | Restores 40% of your class resource | 25 Dinars | Khemet lotus + powdered Star Lapis | Common |
| **Oasis Water** | Slowly heals life and resource over 10 s | No | Cactus + water + powdered Solar Gold | Rare |
| **Sage Antidote** | Removes poison, bleed and slow | 20 Dinars | Mountain sage + milk | Uncommon |
| **War Oil** (fire / frost / storm) | Weapon deals elemental damage for 60 s | No | Nordrath fish oil + element ore | Rare |
| **Sin Resistance** (7, one per Archsin) | −30% damage from one Archsin for 3 min | No | Recipe dropped by that sin's Broken Oath | Recipe only |

**Ingredients = Minecraft farming.** Herbs (pomegranate, lotus, sage, etc.) grow in each empire's biomes and **can be planted** on the player's farm (the Bearer's Homestead, see [Mundo.md](Mundo.md#layer-3--protected-places-cities-camps-dungeons-arenas)), using another part of Minecraft besides mining.

**The Bearer's Flask.** A story item every character receives in Act I. It has charges (3 at start, +1 per Archsin defeated, up to 10) that heal life and resource together. It refills only when returning to Sulthari or killing an elite. It keeps the inventory from filling with potions.

**Potion belt.** A Curios slot with 4 spaces, used from the Combat Bar (Left Alt + 7–9 and 0 by default, configurable; see [Jugabilidad.md](Jugabilidad.md#g4-controls)), separate from the skill slots and the vanilla hotbar. All potions share a 1 s cooldown to prevent abuse.

**Class synergies.** The King's *Royal Treasury* drops coins that act as mini-elixirs. The Necromancer's *Canopic Jars* is his soul "potion". The Thief can steal potions with *Light Fingers*.

## Gear progression by act

Each act unlocks an ore, a maximum craftable rarity and a potion type. Level ranges follow the level-30 cap (see [Clases.md](Clases.md)).

| Act | Levels | New ore | Best craftable gear | Best loot | New potions |
| --- | --- | --- | --- | --- | --- |
| I — Sulthari | 1–5 | Sulthari Brass | Common brass (or vanilla iron) | Tempered | Minor elixir, Tonic, Bearer's Flask |
| II — Nordrath | 5–12 | Glacial Iron | Nordrath Tempered | Imperial + Vorath Relics | War Oil, Wrath Resistance |
| III — Parsivan and Khemet | 12–20 | Star Lapis, Solar Gold | Tempered + sockets | Parsivan and Khemet Legacies | Major elixir, Oasis Water, Antidote |
| IV — Aureum | 20–27 | Orichalcum | Orichalcum Tempered with 3 sockets | Aureum Legacy, Relics of 3 Archsins | Remaining resistances |
| V — Sulthari | 27–30 | Deep Raw Aetherium | Fully awakened Relics | Prython and Nahrazel Relics | Imperial elixir |

## Related documents

- Use cases UC-09 to UC-14 (loot, mining, forge, gems, potions): [CasosDeUso.md](CasosDeUso.md#gear-potions-and-economy). The old UC-15 is replaced by UC-17.
- Packages and patterns for loot, crafting and economy: [Arquitectura.md](Arquitectura.md)
- Sprint 5.5 — Loot and Economy: [Sprints.md](Sprints.md#sprint-55--loot-and-economy-weeks-11-12)
- Test cases: [Pruebas.md](Pruebas.md#gear-potions-and-economy)
