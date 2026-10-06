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

- [x] All 5 Bearers can be chosen and start with their 3 level-1 skills and the right resource (`PlayerClassTest`, `SkillCatalogTest`)
- [x] Skills respect cooldowns and spend the right amount of their class resource (`SkillGameTests`, `CombatLogicTest`)
- [x] Skill costs and cooldowns are read from `data/sofe/skills/*.json` (`SkillCatalogTest.everySkillHasValuesInItsClassFile`)
- [ ] Knight: Shield Stance generates Resolve when blocking; Charge Stance spends it
- [x] Necromancer: a soul appears only when a marked or nearby enemy dies; max 3 Clay Wardens (`ClassSkillGameTests`; the Clay Wardens now grow slower to shape instead of a hard cap, `SkillTreeGameTests`)
- [x] Sorceress: 3 runes complete a constellation and the combination picks the right effect (`CombatLogicTest`, `SkillGameTests`)
- [x] Thief: Marks never exceed 5 and Cutthroat consumes them all (`ClassSkillGameTests.marksGatherAndTheFinisherSpendsThem`)
- [ ] King: only one Decree is active at a time (except during Crown of the Five Lands)
- [x] Leveling awards one skill point per level, max level 100, twenty per act (`ProgressionTest`)
- [x] A point cannot go into a locked skill, past rank 5, past the level each rank needs, or into an ultimate twice (`SkillBookTest`)
- [x] Each rank applies the per-rank growth from the class file (rank 5 damage = 2x rank 1 by default) (`SkillRankGrowthTest`, `SkillGameTests.higherRankHitsHarder`)
- [ ] Each level gives 5 attribute points; every class starts with 10 in each attribute and 20 in its primary one
- [ ] Attribute effects apply (Vitality health, Intellect mana, Agility dodge capped at 30%) and are removed after a respec
- [x] Respec refunds every point (`SkillBookTest.respecGivesEveryPointBackAndSaveRoundTrips`)

## Story and bosses

- [ ] The dialogue box shows portrait, name and text; a key completes the line, the next press advances
- [ ] Cinematic mode shows the letterbox bars and blocks movement; conversation mode does not
- [x] Every dialogue line is a lang key present in `en_us` and `es_es` (`DialogueCoverageTest`)
- [x] A region fate can be set only once and is stored per player (`StoryProgressTest`, `StoryGameTests`)
- [x] The ending shows one slide per region matching that player's fates, and the Bearer epilogue matches their personal quests (`EpilogueTest`, `Sprint8GameTests`)
- [x] The `fate_is` condition reads the player's fate (`ConditionParserTest`)
- [x] Every quest and dialogue file parses, and every quest, step, line, answer and speaker name has a lang key (`StoryDataFilesTest`) (`StoryDataFilesTest`)
- [ ] The Journal lists active and completed quests, the act and the fates; tracking a quest moves the Quest Compass
- [ ] The Festival intro plays before the Bearer selection, and closing it still opens the selection
- [x] Each Bearer sees their own shard scene; the player who is a Bearer never sees that hero as an NPC (`StoryGameTests`, `SultharisLifeGameTests`)
- [ ] The Brass Sentinel wakes when the player reaches the Observatory, turns to the Void phase below half health and gives credit to everyone who fought

- [ ] Boss phase transitions trigger at the right health thresholds
- [x] Kaleth's execution strike hits stunned targets far harder; Serath heals from the damage she deals; Vorath's rage raises his damage (`NorthernCampaignGameTests`)
- [ ] The Codex Shard of a sin is given once per player, and only on the first defeat
- [x] The Citadel gate stays shut until both Kaleth and Serath fall (`NorthernCampaignGameTests.theCitadelGateWaitsForBothOaths`)
- [x] Each participant gets their own boss loot; nothing drops on the ground (`LootAndEconomyGameTests`, `CraftingSprint7GameTests`)
- [ ] The SoFE advancement tab follows the story (in game: Forge never grants advancements to the fake players of GameTests)
- [ ] All 7 Archsins, the Brass Sentinel and Nahrazel (3 phases) can be defeated
- [ ] All 10 Broken Oaths can be defeated
- [x] Envyris copies the player's actual class skills (`Sprint7GameTests.envyrisCopiesTheThreeWhoHurtHerMost`)
- [x] Choosing the King switches the Council to Grand Vizier Ozhan's scenes (`StoryGameTests.theKingMeetsTheCouncilLedByOzhan`)
- [x] The epilogue shown matches the player's Bearer (`EpilogueTest`)
- [x] Dungeons generate without crashes (every place built by `-PplaceShots` and on a real server, `scripts/server_check.py`)
- [ ] No crash on dimension transitions

## World and locks ([Mundo.md](Mundo.md))

- [x] `RegionMap.regionAt` returns the right region at every border coordinate (no overlaps, no gaps) (`RegionMapTest`)
- [ ] The Seal Veil is solid for a player without the unlock and passable for one with it, on both client and server
- [x] The Seal Veil cannot be broken, exploded or moved by pistons (`SultharisLifeGameTests.theSealVeilStopsMobsAndCannotBeBroken`)
- [x] A player who flies over or pearls into a locked region is returned to a safe position within 1 second (`CompatCheck`: back within a second from teleports, /home and /rtp)
- [ ] Creative, spectator and `opsBypass` operators are never blocked
- [x] No block in a protected zone changes from breaking, placing, explosions, pistons, fire or mob griefing (`ZoneRulesTest`, `SultharisLifeGameTests`, `ProtectCheck`)
- [x] Blocks in the Bearer's Homestead can be changed (`ZoneRulesTest`, `SultharisLifeGameTests`)
- [x] The Seal Veil stands on every shared border and not on the coast; its color changes when the player opens the region (`RegionLocksTest`)
- [x] The Ashen Wastes ring surrounds the city; a world created before it keeps its old biomes (`WorldRulesTest`, `JourneyWorldGameTests`)
- [x] New players appear in the plaza of Sulthari (a real client joining a real server, `scripts/pack_check.py --check join`)
- [ ] The buildings, NPCs, Waystones and the Vault of Sulthari are placed once per world, never twice
- [ ] The Nordrath Caverns gate stays closed in Act II and opens in Act IV
- [x] `all_of`, `any_of` and `not` conditions evaluate correctly from JSON (`ConditionParserTest`)
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
- [x] A merchant validates price, Dinars and stock on the server; a soulbound item cannot be sold (`LootAndEconomyGameTests`, `GearAndEconomyTest`)
- [x] The Imperial Forge needs the Blueprint and the ingredients, and always makes Tempered gear or better (`LootAndEconomyGameTests.theImperialForgeMakesTemperedGearFromABlueprint`)
- [x] An Archsin or Broken Oath leaves a Reward Coffer; each participant takes only their own share, once (`CraftingSprint7GameTests.everyBearerGetsACofferOfTheirOwnThatOnlyTheyOpenAndThatFades`)
- [ ] Brass mines Glacial Iron and Star Lapis; stone does not
- [x] An equipped item with +Vitality raises maximum health, and taking it off removes the bonus (`LootAndEconomyGameTests.gearRaisesTheSheetOnlyWhenItsRequirementsAreMet`)
- [ ] +1 to all Sorceress skills raises only learned skills, and never above rank 8
- [x] An item whose level or attribute requirement is not met gives nothing (`GearAndEconomyTest.gearCountsOnlyWhenItsRequirementsAreMet`)
- [ ] Talismans only count inside the Talisman Pouch
- [x] An item never has more affixes than its rarity allows (`GearAndEconomyTest`)
- [x] Affix values stay within the range for their item level (`GearAndEconomyTest.affixRangesGrowWithItemLevel`)
- [x] Class bias yields between 55% and 65% class-affine items (`GearAndEconomyTest.theClassBiasKeepsAboutSixtyPercentOfTheLootForTheClass`)
- [ ] The Forge consumes nothing when the recipe is invalid
- [ ] The Flask never drops below 0 charges or exceeds its max
- [ ] Shared potion cooldown blocks double use in the same tick
- [ ] Dinars save and load correctly when leaving the world

## Menu, materials and merchants

- [ ] `SoFETitleScreen` replaces `TitleScreen`, and does not with `replaceTitleScreen = false`
- [x] *Begin the Journey* creates the world with `sofe:aetheris` and spawns the player in Sulthari (`WorldPresetGameTests`, and a real server with `level-type=sofe:aetheris`)
- [x] Every material form has a texture, a model, tags and lang keys in `en_us` and `es_es` (`MaterialGameTests`, `MaterialTest`)
- [ ] An offer never appears below its `min_act` or `min_favor`
- [x] The Favor discount is applied once and never goes above 10% (`CraftingSprint7GameTests.favorRisesInRanksAndLowersPrices`)
- [x] Player A buying does not change player B's stock (`GearAndEconomyTest.stockIsPerPlayerAndRestocksAtDawn`)
- [ ] The server rejects a purchase with a manipulated price from the client
- [x] Relics and story items cannot be sold (`GearAndEconomyTest.relicsCannotBeSoldAndPricesRiseWithLevel`)

## Multiplayer

- [x] Boss health scales with player count and damage does not (`Sprint75GameTests`); with more players the signature marks the others too (`Sprint8GameTests`)
- [x] A mob spawning in Nordrath gets a level in 5-12; near a player in Act IV it is at least level 20 (`ProgressionTest`)
- [x] Level scaling multiplies health and damage as configured and survives a world reload (`ProgressionGameTests`, `NorthernCampaignGameTests.actTwoMobsRememberTheirAct`)
- [x] Vanilla mob traits appear only from their act; mobs from other mods get scaling but no traits; free mode worlds are untouched (`CraftingSprint7GameTests.vanillaMobsGrowMeanerInTheLaterActs`, `ProgressionGameTests.mobsOutsideAJourneyHaveNoLevel`)
- [x] XP split plus bonus in a Pact of 3 is correct, and a solo player gets exactly 100% (`PactRulesTest`)
- [ ] Co-op bonuses turn off beyond 48 blocks or in another dimension
- [ ] 4 participants in a boss fight → 4 Relics, each with its own `sofe:owner`
- [x] A non-participant gets nothing from the Reward Coffer (`CraftingSprint7GameTests`)
- [x] A `SOULBOUND` item cannot be dropped or put in someone else's chest and is kept on death (`LootAndEconomyGameTests.theFlaskHealsAndSoulboundItemsSurviveDeath`)
- [ ] Equipping a second copy of the same Relic is blocked
- [ ] The Echo of an Archsin does not change world blocks and gives the player their credit
- [x] The Council gives back a lost story item only when it is truly missing (`Sprint75GameTests`, `Sprint8GameTests`)
- [ ] With `uniqueBearersPerServer = true` a Bearer cannot be picked twice
- [ ] A Bearer played in the Pact cannot be hired as a companion

## Core rules ([Jugabilidad.md](Jugabilidad.md))

- [ ] On death, every equipped and carried item is on the corpse, and only story items stay with the player
- [x] Only the owner can take items from a corpse, even inside a Pact (`SultharisLifeGameTests.theBodyKeepsTheGearForItsOwnerOnly`)
- [ ] Recovering a corpse puts every item back in its original slot, and nothing is lost or duplicated
- [ ] A corpse never despawns and cannot be destroyed by lava, explosions, mobs or pistons
- [ ] A death in lava or the void places the corpse at the last safe position
- [ ] Two deaths in a row leave two corpses with the right contents
- [ ] `corpseSystem = false` disables corpses (for grave mods)
- [x] A boss resets to full health 30 s after every participant dies or leaves (`NorthernCampaignGameTests.aBossResetsAndForgetsItsParticipants`)
- [x] Avarok returns every stolen item when he dies or the fight resets (`Sprint7GameTests.avarokStealsButNeverTheFlaskOrARelicAndGivesItBack`)
- [x] Waystone travel is refused during combat, inside arenas and toward locked regions (`WorldRulesTest.waystoneTravelRules`)
- [ ] Combat Bar keys do not trigger vanilla hotbar selection; every SoFE key can be rebound
- [x] The Personal Vault shows the same items in every Vault block and only to its owner (`SultharisLifeGameTests.waystonesAreActivatedPerPlayerAndTheVaultIsPersonal`)
- [ ] Peaceful is replaced by Easy in `sofe:aetheris`
- [x] Bosses have −25% health and damage on Easy and +25% on Hard (`WorldRulesTest.bossesFollowTheDifficulty`)
- [ ] No villager, wandering trader or trader llama appears in `sofe:aetheris`, even from a spawn egg or a cured zombie villager
- [ ] The Quest Compass points to the player's latest body until it is recovered
- [ ] Without a bed, a player respawns at the last activated Waystone; the Return Scroll takes them to Sulthari after 5 s
- [x] A world keeps its saved region layout after a mod update that changes the layout JSON (`JourneyWorldGameTests.theLayoutAndTheAshenWastesAreSavedWithTheWorld`)
- [ ] Free mode works in a vanilla world: the items work, the story does not, and the player is told why
- [x] With content mods (e.g. an extra swords mod), JEI and a map mod installed, the story runs normally (`scripts/pack_check.py`: Supplementaries, the furniture mod, Small Ships, JEI, Xaero's Minimap)
- [x] A teleport mod cannot move a player into a locked region (`CompatCheck` with FTB Essentials)
- [ ] With a grave mod installed, the SoFE corpse is off and no items are duplicated
- [ ] A mod on the incompatible list makes the new journey start in free mode, with a warning
- [x] If the SoFE world generator is replaced, the world switches to free mode on load and logs the reason (`Sprint8GameTests.theIntegrityCheckPassesAWholeJourneyAndCatchesAMissingMap`)
- [x] The Inverted Throne gate stays closed without the Sealing Quill (`Sprint8GameTests`)
- [x] Accepting Prython's offer plays the bad ending and returns the player to before the choice (`Sprint8GameTests`)
- [ ] A late joiner plays the Act I prologue in the real Sulthari (no `sofe:echo` dimension: the invasion and the Brass Sentinel rise for whoever owes them) and arrives in Act I
- [ ] A disconnected participant finds their Reward Coffer on return
- [ ] `/sofe progress set act` grants the credit of earlier acts and updates locks at once
- [x] Rarity is readable without color (text label in the tooltip) (the tooltip and the label over gear on the ground (`rarityLabels`, `-PplaceShots=loot`))
