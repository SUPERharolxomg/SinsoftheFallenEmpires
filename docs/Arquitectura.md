# Software Architecture

Technical design of *Sins of the Fallen Empires*. Game design lives in [README.md](../README.md), [Clases.md](Clases.md), [Pociones.md](Pociones.md) and [Anexos.md](Anexos.md); behavior in [CasosDeUso.md](CasosDeUso.md).

## 1. Project structure

```text
sins-of-fallen-empires/
├── src/main/java/com/sofe/
│   ├── SoFEMod.java                        # Main mod class
│   ├── config/
│   │   └── SoFEConfig.java                 # Client, common and server config
│   ├── client/
│   │   ├── screen/SoFETitleScreen.java     # Title screen and main menu
│   │   ├── screen/CodexScreen.java         # Lore, bestiary, splash art gallery
│   │   ├── screen/JournalScreen.java       # Quests, act, fates, Pact (key U)
│   │   ├── screen/DialogueScreen.java      # Warcraft III style box: portrait, name, typewriter text, letterbox
│   │   ├── screen/WaystoneScreen.java      # Travel between activated Waystones
│   │   ├── screen/ClassSelectScreen.java   # Choose a Bearer
│   │   ├── screen/SkillTreeScreen.java     # Diablo II tree (key K)
│   │   ├── screen/CharacterSheetScreen.java # Attributes (key I)
│   │   ├── hud/BossHealthBar.java
│   │   ├── hud/CombatHudOverlay.java       # Health + class resource, runes, Marks, souls, Combat Bar
│   │   ├── hud/QuestCompassOverlay.java    # Strip + distance to the objective (or the player's body)
│   │   ├── hud/RegionTitleOverlay.java     # Title when entering a region, with "Sealed" / "Liberated"
│   │   ├── render/SoFEEntityRenderers.java # Placeholder renderers, Bearer outfit layer
│   │   ├── ClientLockData.java             # Region layout + act on the client, for the Seal Veil
│   │   ├── screen/CodexMapScreen.java      # Illustrated map of Aetheris
│   │   └── TitleScreenHandler.java         # ScreenEvent.Opening → replace TitleScreen
│   ├── entity/
│   │   ├── boss/
│   │   │   ├── ArchsinEntity.java          # Abstract base for the seven Archsins
│   │   │   ├── VorathEntity.java           # Wrath
│   │   │   ├── LuxaraEntity.java           # Lust
│   │   │   ├── ...                         # Morthis, Avarok, Gularth, Envyris, Prython
│   │   │   ├── NahrazelEntity.java         # The First Fallen, 3 phases
│   │   │   └── BrassSentinelEntity.java    # Act I boss (2 phases, difficulty scaling, reset)
│   │   ├── oath/
│   │   │   ├── BrokenOathEntity.java       # Abstract base for the Ten Broken Oaths
│   │   │   ├── KalethEntity.java           # Law I — The Burning Blade
│   │   │   └── ...                         # Serath ... Solrath
│   │   ├── summon/
│   │   │   ├── ClayWardenEntity.java       # Necromancer
│   │   │   ├── JanissaryGuardEntity.java   # King
│   │   │   └── BronzeCannonEntity.java     # King
│   │   ├── npc/
│   │   │   ├── StoryNpcEntity.java         # Ozhan, the Council elder...: talk through dialogue files
│   │   │   ├── BearerNpcEntity.java        # The four Bearers not chosen; companions
│   │   │   ├── MerchantNpcEntity.java      # Ferid, Dilara, Kerem, ...
│   │   │   ├── MerchantRole.java           # enum: ALCHEMIST, SMITH, JEWELER, ...
│   │   │   └── CaravanSpawner.java         # Zahir every 3 days
│   │   ├── VoidCreature.java               # VoidWretch, VoidStalker
│   │   └── BearerCorpseEntity.java         # Holds the gear, owner only (see death/)
│   ├── player/
│   │   ├── PlayerClass.java                # enum: KNIGHT, NECROMANCER, SORCERESS, THIEF, KING
│   │   ├── ResourceType.java               # enum: RESOLVE, ESSENCE, MANA, ENERGY, AUTHORITY
│   │   ├── PlayerClassData.java            # Class data capability
│   │   ├── LevelingSystem.java             # XP and levels
│   │   └── companion/CompanionManager.java # UC-22
│   ├── combat/
│   │   ├── CombatData.java                 # Resource, cooldowns and runes of a player (capability)
│   │   ├── ResourcePool.java / CooldownTracker.java / RuneTracker.java
│   │   ├── Constellation.java              # What three runes make
│   │   └── CombatHandler.java              # Regeneration and HUD sync
│   ├── skill/
│   │   ├── Skill.java                      # Skill interface (the effect only)
│   │   ├── SkillCatalog.java               # The 50 skills: class, level, type
│   │   ├── SkillCaster.java                # Checks class, slot, cooldown and resource, then casts
│   │   ├── SkillRegistry.java              # Skills with code; the rest are data until their sprint
│   │   ├── SkillTargeting.java             # Aim, area search, damage, particle lines
│   │   ├── SkillTree.java
│   │   ├── data/SkillDataManager.java      # Loads data/sofe/skills/*.json
│   │   ├── knight/                         # Stances, ScaleStrike, ...
│   │   ├── necromancer/                    # SoulBinding, ThresholdTouch, ...
│   │   ├── sorceress/                      # Constellations, EmberVerse, ...
│   │   ├── thief/                          # Marks, LightFingers, ...
│   │   └── king/                           # Decrees, JanissaryGuard, ...
│   ├── condition/
│   │   ├── Condition.java                  # test(ServerPlayer)
│   │   ├── ConditionLoader.java            # data/sofe/conditions/*.json
│   │   └── types/                          # ActReached, BossDefeated, QuestStep, AllOf, AnyOf, Not
│   ├── compat/
│   │   ├── WorldIntegrityCheck.java        # Story or free mode on load
│   │   ├── IncompatibleMods.java           # data/sofe/compat/incompatible_mods.json
│   │   ├── journeymap/SoFEJourneyMapPlugin.java  # Regions, locks, waypoints
│   │   └── xaero/XaeroCompat.java          # Basic support, only if the API allows
│   ├── quest/
│   │   ├── QuestDefinition.java            # Steps, objectives, rewards (data/sofe/quests/*.json)
│   │   ├── DialogueDefinition.java         # data/sofe/dialogue/*.json: lines, answers, effects, on_end
│   │   ├── StoryParser.java / StoryDataManager.java  # JSON → definitions, reload listeners
│   │   ├── QuestLogic.java / DialogueLogic.java      # Pure rules (unit tested)
│   │   ├── QuestEngine.java                # Game events → objectives, effect runner, Journal/compass sync
│   │   ├── DialogueService.java            # Server-side conversation sessions
│   │   └── QuestEffect.java / Objective.java / QuestEvent.java
│   ├── travel/
│   │   ├── WaystoneBlock.java / WaystoneService.java # Activation, travel rules, respawn at the last one
│   │   ├── TravelData.java / TravelCapability.java   # Activated Waystones + Personal Vault per player
│   │   ├── VaultBlock.java                 # Same storage from every Vault block
│   │   └── ReturnScrollItem.java           # 5 s channel back to Sulthari
│   ├── death/
│   │   ├── CorpseHandler.java              # Diablo II style death
│   │   └── CorpseRegistry.java             # Where each player's bodies lie (for the compass)
│   ├── command/SoFECommands.java           # /sofe progress, unstuck, item restore, reload
│   ├── story/
│   │   ├── StoryProgress.java              # Per-player act, quests, boss credit, fates (StoryCapability)
│   │   ├── RegionFates.java                # The fates each region can end with
│   │   ├── PlayerProgressView.java         # What conditions see of a real player
│   │   ├── Act.java                        # enum: ECLIPSE, NORTH, EAST, WEST, ASCENSION
│   │   └── CutsceneManager.java            # Intros, temptations, epilogues per hero
│   ├── item/
│   │   ├── tier/SoFETiers.java             # Brass, Glacial Iron, Solar Gold, Orichalcum (TierSortingRegistry)
│   │   ├── rarity/Rarity.java              # COMMON, TEMPERED, IMPERIAL, RELIC, LEGACY
│   │   ├── affix/Affix.java
│   │   ├── affix/AffixRegistry.java        # Loads data/sofe/affixes/*.json
│   │   ├── gem/GemItem.java
│   │   ├── weapon/SoFEWeapon.java
│   │   ├── armor/SoFEArmor.java
│   │   └── consumable/BearerFlaskItem.java
│   ├── loot/
│   │   ├── LootGenerator.java              # UC-09
│   │   └── SoFELootModifier.java           # Forge Global Loot Modifier
│   ├── block/
│   │   ├── ore/                            # 6 empire ores
│   │   └── station/                        # Forge, Anvil, Awakener, Jeweler, Purifier, Alembic
│   ├── crafting/
│   │   ├── ImperialForgeRecipe.java
│   │   ├── AlchemyRecipe.java
│   │   └── BlueprintKnowledge.java         # Learned blueprints (capability)
│   ├── economy/
│   │   ├── Wallet.java                     # Dinars (capability)
│   │   ├── MerchantOfferLoader.java        # data/sofe/merchant_offers/*.json
│   │   ├── PlayerStock.java                # Per-player stock (capability)
│   │   └── Favor.java                      # Per-empire reputation (capability)
│   ├── pact/
│   │   ├── Pact.java
│   │   ├── PactManager.java                # SavedData on the server
│   │   └── CoopBonusHandler.java           # XP, loot, Class Concord
│   ├── ownership/
│   │   ├── OwnerComponent.java             # sofe:owner
│   │   ├── BindPolicy.java                 # FREE, PACT_ONLY, SOULBOUND
│   │   └── RewardCofferBlockEntity.java    # Personal boss loot
│   ├── world/
│   │   ├── preset/AetherisPreset.java      # sofe:aetheris
│   │   ├── region/
│   │   │   ├── Region.java                 # enum: SULTHARI, NORDRATH, PARSIVAN, KHEMET, AUREUM, OCEAN
│   │   │   ├── RegionMap.java              # regionAt(x, z); layout saved in each world by the biome source
│   │   │   ├── AetherisBiomeSource.java    # Biomes by region + the Ashen Wastes ring (fixed layout, procedural terrain)
│   │   │   └── AshenWastes.java
│   │   ├── lock/
│   │   │   ├── RegionLocks.java            # Pure rules: where a player may stand, Veil columns, coast limit
│   │   │   ├── LockAccess.java             # Same answer on server and client (act, bypass)
│   │   │   ├── RegionEnforcer.java         # Server check once per second + teleport events
│   │   │   ├── SealVeilBlock.java          # Region border wall, per-player collision and tint
│   │   │   └── SealVeilFeature.java        # Raises the Veil on border columns (biome modifier)
│   │   ├── gate/
│   │   │   ├── SealedGateBlock.java        # Doors with a condition
│   │   │   ├── VoidGateBlock.java          # The only End portal, under the Observatory
│   │   │   └── NetherPortalLock.java       # Lighting and linking rules for Nether portals
│   │   ├── zone/
│   │   │   ├── ProtectedZone.java / ZoneRules.java # Bounds + rules (Chain of Responsibility)
│   │   │   ├── ProtectedZoneData.java      # World SavedData, copied once from structure_positions.json
│   │   │   ├── StructurePositions.java     # data/sofe/structure_positions.json: spawn, structures, NPCs, Waystones
│   │   │   └── ZoneProtectionHandler.java  # Break, place, explosions, pistons, fluids, griefing; spawn in the city
│   │   ├── build/SultharisBuilder.java     # Structure Block templates, or blockouts until the builds exist
│   │   ├── StoryPlacements.java            # Buildings, NPCs, Waystones and the Vault, placed once per world
│   │   ├── JourneyRules.java               # No villagers or traders; Peaceful becomes Easy
│   │   ├── dimension/                      # inverted_throne, codex_interior, echo
│   │   ├── structure/                      # Cities, dungeons, arenas at fixed positions
│   │   ├── biome/
│   │   └── feature/OreFeatures.java        # Ores by region biome tag
│   ├── network/
│   │   ├── SoFENetwork.java
│   │   ├── SkillCastPacket.java
│   │   └── ClassSelectPacket.java
│   ├── event/
│   │   ├── PlayerEventHandler.java
│   │   ├── CombatEventHandler.java
│   │   └── WorldEventHandler.java
│   └── registry/
│       ├── EntityRegistry.java
│       ├── ItemRegistry.java
│       ├── MaterialRegistry.java           # Every material form in one loop
│       ├── SkillRegistry.java
│       └── DimensionRegistry.java
├── src/main/resources/
│   ├── assets/sofe/
│   │   ├── textures/{entity,item,block,gui,gui/splash}/
│   │   ├── models/{entity,item,block}/
│   │   ├── geo/ and animations/            # GeckoLib models
│   │   ├── lang/en_us.json, es_es.json     # ALL player-facing text
│   │   └── sounds/
│   ├── assets/minecraft/texts/splashes.txt # Lore splash texts
│   ├── data/sofe/
│   │   ├── skills/                         # Costs and cooldowns per class
│   │   ├── affixes/
│   │   ├── merchant_offers/
│   │   ├── conditions/                     # Lock conditions
│   │   ├── quests/                         # Main, Bearer and side quests
│   │   ├── advancements/                   # SoFE advancement tab
│   │   ├── loot_tables/
│   │   ├── recipes/
│   │   ├── worldgen/
│   │   └── tags/
│   └── META-INF/mods.toml
├── build.gradle
├── settings.gradle
├── .github/workflows/build.yml             # CI/CD
├── README.md
├── CONTRIBUTING.md
├── LICENSE (MIT)
└── docs/
```

## 2. Design patterns

### General

| Pattern | Usage | Java concept |
|---------|-------|--------------|
| **Abstract Factory** | Creating entity families (Archsins, Broken Oaths, mobs) | Abstract classes, interfaces |
| **Strategy** | Different skill behaviors per class | Interfaces, polymorphism |
| **Observer** | Events for combat, leveling, deaths | Forge event listeners |
| **Singleton** | Skill registry, config | Static instances |
| **State** | Boss phases (phase 1, enrage, Nahrazel's 3 phases) | Enums, state machines |
| **Command** | Skill execution with cooldowns | Command objects |
| **Builder** | Complex entity and item construction | Builder pattern |

### Class mechanics

| Class | Resource | Mechanic | Pattern |
|-------|----------|----------|---------|
| Knight | Resolve (rises when blocking) | Stances | **State** (`ShieldStance`, `ChargeStance`) |
| Necromancer | Essence + souls | Binding souls to Wardens | **Factory** for Wardens + **Observer** on `LivingDeathEvent` |
| Sorceress | Mana | 3-rune constellations | **Strategy** per rune combination |
| Thief | Energy + Marks | Marks and stealing | **Decorator** for stolen buffs |
| King | Authority (rises on kills and heals) | Decrees and guard | **Command** for Decrees + **Composite** for the guard |

### Gear and economy ([Pociones.md](Pociones.md))

| Piece | Pattern | Why |
|-------|---------|-----|
| Affixes on an item | **Decorator** | Each affix wraps the base stats without touching the item class |
| Item generation | **Builder** | `new ItemRoll.Builder(base).level(14).rarity(IMPERIAL).classBias(KING).build()` |
| Rarity and affix selection | **Strategy** | One strategy per source: normal enemy, elite, boss, chest |
| Crafting stations | **Template Method** | `AbstractStationBlockEntity` defines validate → consume → produce |
| Potion use | **Command** | Each use is a server-validated command logged for the cooldown |

### World and progression locks ([Mundo.md](Mundo.md))

| Piece | Pattern | Why |
|-------|---------|-----|
| Lock conditions | **Specification** + **Composite** | `act_reached`, `boss_defeated`, … combined with `all_of` / `any_of` / `not`, loaded from JSON |
| Region lookup | **Facade** | `RegionMap` hides biome source, JSON bounds and coordinates behind `regionAt(x, z)` |
| Map mod integration | **Adapter** | One `MapIntegration` interface; JourneyMap and Xaero adapters load only if the mod is present |
| Protection rules | **Chain of Responsibility** | Each rule (zone, homestead, bypass) can allow or deny a block change |

### Menu, merchants and multiplayer ([Anexos.md](Anexos.md))

| Piece | Pattern | Why |
|-------|---------|-----|
| Screen replacement | **Observer** | Reacts to `ScreenEvent.Opening` without touching vanilla code |
| Merchant offers | **Strategy** + data-driven | Each role filters and prices offers differently; values in JSON |
| Materials | **Factory** | `MaterialRegistry.register("glacial_iron", Forms.METAL_DEEP)` creates every form, tag and lang key |
| Co-op bonuses | **Chain of Responsibility** | Each bonus (XP, loot, Concord) modifies the value and passes it on |
| Item ownership | **Decorator** | The owner component adds rules without changing the item class |
| Personal loot | **Proxy** | The Coffer shows each player their own inventory view |

## 3. Code examples

```java
// Abstract base for all Archsins
public abstract class ArchsinEntity extends Monster {
    protected int phase = 1;
    protected final Sin sin;

    protected ArchsinEntity(EntityType<? extends Monster> type, Level level, Sin sin) {
        super(type, level);
        this.sin = sin;
    }

    public abstract void onPhaseChange(int newPhase);
    public abstract void useSpecialAbility(Player target);

    @Override
    public void tick() {
        super.tick();
        if (phase == 1 && getHealth() < getMaxHealth() * 0.5f) {
            phase = 2;
            onPhaseChange(2);
        }
    }
}

// Vorath (Wrath): rage phases, shrinking arena
public class VorathEntity extends ArchsinEntity {
    public VorathEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level, Sin.WRATH);
    }

    @Override
    public void onPhaseChange(int newPhase) {
        // Enrage: more speed and damage, start shrinking the arena
    }

    @Override
    public void useSpecialAbility(Player target) {
        // Berserker charge toward the player
    }
}

// Skill interface: the cost is in the class resource, not always mana
public interface Skill {
    String getId();                 // lang key: skill.sofe.<id>
    int getCooldownTicks();         // from data/sofe/skills/<class>.json
    int getCost();
    ResourceType getResource();
    boolean canCast(Player player);
    void execute(Player player, Level level);
}

public enum Sin {
    WRATH, LUST, GREED, SLOTH, GLUTTONY, ENVY, PRIDE
}

public enum ResourceType {
    RESOLVE, ESSENCE, MANA, ENERGY, AUTHORITY
}

public enum PlayerClass {
    KNIGHT("knight", ResourceType.RESOLVE, Sin.WRATH),
    NECROMANCER("necromancer", ResourceType.ESSENCE, Sin.SLOTH),
    SORCERESS("sorceress", ResourceType.MANA, Sin.LUST),
    THIEF("thief", ResourceType.ENERGY, Sin.GREED),
    KING("king", ResourceType.AUTHORITY, Sin.PRIDE);

    private final String id;
    private final ResourceType resource;
    private final Sin temptation; // used in dialogue and in the Envyris fight

    PlayerClass(String id, ResourceType resource, Sin temptation) {
        this.id = id;
        this.resource = resource;
        this.temptation = temptation;
    }
}
```

## 4. Implementation notes for the classes

- **All text in lang files.** Names, dialogue and skill descriptions go in `lang/en_us.json` and `lang/es_es.json`, never in code. The English version of the story document is the source for `en_us.json`.
- **Balance in data files.** Costs and cooldowns from [Clases.md](Clases.md) live in `data/sofe/skills/<class>.json`, so values can be tuned without recompiling:

```json
// data/sofe/skills/knight.json
{
  "scale_strike":     { "cost": 10, "cooldown_s": 3 },
  "shield_wall":      { "cost": 15, "cooldown_s": 8 },
  "last_one_standing": { "cost": 100, "cooldown_s": 120 }
}
```

- **Server authority.** Every skill cast, purchase, potion use and loot roll is validated on the server; the client only sends intent and shows the result.
- **Per-player story.** Act, quests, boss credit, Codex fragments, Favor and blueprints live in player capabilities. Lock checks read this same data (see [Mundo.md](Mundo.md#w2-progression-locks)). Only region restoration and protected zones are stored per world (see [Anexos.md](Anexos.md#a6-unique-items-per-player-online)).

## 5. Interview value

When discussing this project in interviews, highlight:

- **OOP:** abstract classes for bosses, interfaces for skills, enums for classes, resources and sins
- **Design patterns:** Factory, Strategy, Observer, State, Command, Builder, Decorator, Composite, Template Method, Chain of Responsibility, Proxy
- **Event-driven architecture:** the Forge event system is the same concept as enterprise event buses
- **Networking:** client-server packets with server-side validation = distributed systems fundamentals
- **Data-driven design:** balance, loot, affixes and merchant offers in JSON
- **CI/CD:** GitHub Actions automated builds
- **Testing:** JUnit for game logic, same principles as enterprise software
- **Open source:** community management, code reviews, contribution guidelines
