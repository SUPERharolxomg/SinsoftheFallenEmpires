# Testing Strategy

How *Sins of the Fallen Empires* is tested. Use cases referenced here are in [CasosDeUso.md](CasosDeUso.md).

| Test type | Scope | Tool |
|-----------|-------|------|
| Unit tests | Skill damage, cooldowns, resources, XP formulas, loot odds, prices | JUnit 5 |
| Integration tests | Bearer selection → skill assignment, save/load of capabilities | Forge test framework (GameTests) |
| Manual testing | Boss fights, dungeon generation, cutscenes, multiplayer | In-game playtesting |
| Multiplayer testing | Pact, personal loot, per-player stock | Dedicated server with 4+ players |
| Performance | Particles, mob spawning, dimension loading, summons | Spark profiler |

## Classes and skills

- [ ] All 5 Bearers can be chosen and start with their 3 level-1 skills and the right resource
- [ ] Skills respect cooldowns and spend the right amount of their class resource
- [ ] Skill costs and cooldowns are read from `data/sofe/skills/*.json`
- [ ] Knight: Shield Stance generates Resolve when blocking; Charge Stance spends it
- [ ] Necromancer: a soul appears only when a marked or nearby enemy dies; max 3 Clay Wardens
- [ ] Sorceress: 3 runes complete a constellation and the combination picks the right effect
- [ ] Thief: Marks never exceed 5 and Cutthroat consumes them all
- [ ] King: only one Decree is active at a time (except during Crown of the Five Lands)
- [ ] Leveling awards a skill point every 2 levels, max level 30
- [ ] Respec refunds every point

## Story and bosses

- [ ] Boss phase transitions trigger at the right health thresholds
- [ ] All 7 Archsins, the Brass Sentinel and Nahrazel (3 phases) can be defeated
- [ ] All 10 Broken Oaths can be defeated
- [ ] Envyris copies the player's actual class skills
- [ ] Choosing the King switches the Council to Grand Vizier Ozhan's scenes
- [ ] The epilogue shown matches the player's Bearer
- [ ] Dungeons generate without crashes
- [ ] No crash on dimension transitions

## World and locks ([Mundo.md](Mundo.md))

- [ ] `RegionMap.regionAt` returns the right region at every border coordinate (no overlaps, no gaps)
- [ ] The Seal Veil is solid for a player without the unlock and passable for one with it, on both client and server
- [ ] The Seal Veil cannot be broken, exploded or moved by pistons
- [ ] A player who flies over or pearls into a locked region is returned to a safe position within 1 second
- [ ] Creative, spectator and `opsBypass` operators are never blocked
- [ ] No block in a protected zone changes from breaking, placing, explosions, pistons, fire or mob griefing
- [ ] Blocks in the Bearer's Homestead can be changed
- [ ] The Nordrath Caverns gate stays closed in Act II and opens in Act IV
- [ ] `all_of`, `any_of` and `not` conditions evaluate correctly from JSON
- [ ] With `PER_PLAYER`, unlocking a region for player A does not unlock it for player B; with `PACT_ESCORT`, B can pass next to A
- [ ] A Nether portal cannot be lit before Vorath falls and can be lit after
- [ ] Nether positions map to the right overworld region at 8× and the same locks apply
- [ ] A portal whose exit is in a locked region does not link
- [ ] No vanilla stronghold generates in `sofe:aetheris`; the Void Gate opens only after Envyris with 12 Eyes of Ender
- [ ] Infernal Ember, Wailing Soul and Void Crystal only generate in their Nether / End biomes
- [ ] Each mod ore only generates in its region and within its height range
- [ ] A vanilla diamond pickaxe mines Solar Gold but not Orichalcum
- [ ] The mod runs without JourneyMap or Xaero installed; with JourneyMap, objective waypoints appear and disappear correctly

## Gear, potions and economy

- [ ] Rolled rarity matches configured odds (10,000 rolls, 2% margin)
- [ ] An item never has more affixes than its rarity allows
- [ ] Affix values stay within the range for their item level
- [ ] Class bias yields between 55% and 65% class-affine items
- [ ] The Forge consumes nothing when the recipe is invalid
- [ ] The Flask never drops below 0 charges or exceeds its max
- [ ] Shared potion cooldown blocks double use in the same tick
- [ ] Dinars save and load correctly when leaving the world

## Menu, materials and merchants

- [ ] `SoFETitleScreen` replaces `TitleScreen`, and does not with `replaceTitleScreen = false`
- [ ] *Begin the Journey* creates the world with `sofe:aetheris` and spawns the player in Sulthari
- [ ] Every material form has a texture, a model, tags and lang keys in `en_us` and `es_es`
- [ ] An offer never appears below its `min_act` or `min_favor`
- [ ] The Favor discount is applied once and never goes above 10%
- [ ] Player A buying does not change player B's stock
- [ ] The server rejects a purchase with a manipulated price from the client
- [ ] Relics and story items cannot be sold

## Multiplayer

- [ ] Boss health scales with player count and damage does not
- [ ] XP split plus bonus in a Pact of 3 is correct, and a solo player gets exactly 100%
- [ ] Co-op bonuses turn off beyond 48 blocks or in another dimension
- [ ] 4 participants in a boss fight → 4 Relics, each with its own `sofe:owner`
- [ ] A non-participant gets nothing from the Reward Coffer
- [ ] A `SOULBOUND` item cannot be dropped or put in someone else's chest and is kept on death
- [ ] Equipping a second copy of the same Relic is blocked
- [ ] The Echo of an Archsin does not change world blocks and gives the player their credit
- [ ] The Council gives back a lost story item only when it is truly missing
- [ ] With `uniqueBearersPerServer = true` a Bearer cannot be picked twice
- [ ] A Bearer played in the Pact cannot be hired as a companion

## Core rules ([Jugabilidad.md](Jugabilidad.md))

- [ ] On death, every equipped and carried item is on the corpse, and only story items stay with the player
- [ ] Only the owner can take items from a corpse, even inside a Pact
- [ ] Recovering a corpse puts every item back in its original slot, and nothing is lost or duplicated
- [ ] A corpse never despawns and cannot be destroyed by lava, explosions, mobs or pistons
- [ ] A death in lava or the void places the corpse at the last safe position
- [ ] Two deaths in a row leave two corpses with the right contents
- [ ] `corpseSystem = false` disables corpses (for grave mods)
- [ ] A boss resets to full health 30 s after every participant dies or leaves
- [ ] Avarok returns every stolen item when he dies or the fight resets
- [ ] Waystone travel is refused during combat, inside arenas and toward locked regions
- [ ] Combat Bar keys do not trigger vanilla hotbar selection; every SoFE key can be rebound
- [ ] The Personal Vault shows the same items in every Vault block and only to its owner
- [ ] Peaceful is replaced by Easy in `sofe:aetheris`
- [ ] A world keeps its saved region layout after a mod update that changes the layout JSON
- [ ] Free mode works in a vanilla world and can be turned off
- [ ] With content mods (e.g. an extra swords mod), JEI and a map mod installed, the story runs normally
- [ ] A teleport mod cannot move a player into a locked region
- [ ] With a grave mod installed, the SoFE corpse is off and no items are duplicated
- [ ] A mod on the incompatible list makes the new journey start in free mode, with a warning
- [ ] If the SoFE world generator is replaced, the world switches to free mode on load and logs the reason
- [ ] The Inverted Throne gate stays closed without the Sealing Quill
- [ ] Accepting Prython's offer plays the bad ending and returns the player to before the choice
- [ ] A late joiner plays the prologue in `sofe:echo` and arrives in Act I
- [ ] A disconnected participant finds their Reward Coffer on return
- [ ] `/sofe progress set act` grants the credit of earlier acts and updates locks at once
- [ ] Rarity is readable without color (text label in the tooltip)
