# Sins of the Fallen Empires — Minecraft Mod
## Software Engineering Document & Game Design

---

# 1. Project Overview

## 1.1 Vision
A Minecraft mod that introduces a skill-based RPG class system, an original dark fantasy narrative set across fallen ancient empires, boss encounters inspired by the Seven Deadly Sins, and dungeon crawling mechanics. Built with Java using Minecraft Forge, designed as an open-source project.

## 1.2 Mod Name
**Sins of the Fallen Empires** (SoFE)

## 1.3 Target Platform
- Minecraft Java Edition 1.20.x+
- Minecraft Forge / NeoForge
- Single-player and Multiplayer compatible

## 1.4 Tech Stack
- Java 17
- Minecraft Forge MDK
- Gradle (build system)
- JSON (configurations, loot tables, recipes)
- Git + GitHub (version control)
- GitHub Actions (CI/CD for automated builds)

---

# 2. Original Story & Lore

## 2.1 The World — Aetheris

In a world called **Aetheris**, five great empires once ruled in harmony, each representing a pillar of civilization:

| Empire | Inspiration | Pillar | Region |
|--------|------------|--------|--------|
| **Sulthari Dominion** | Ottoman | Military & Strategy | Desert plateaus and grand fortresses |
| **Khemet Ascendancy** | Egyptian | Knowledge & Architecture | River valleys, pyramids, underground tombs |
| **Parsivan Court** | Persian | Art & Diplomacy | Lush gardens, palatial cities, mountain passes |
| **Nordrath Clans** | Viking/Germanic | Strength & Exploration | Frozen tundras, fjords, longship harbors |
| **Aureum Republic** | Roman/Byzantine | Law & Engineering | Marble cities, aqueducts, colosseums |

## 2.2 The Fall

A thousand years before the player's story begins, the empires discovered the **Void Codex**, an ancient artifact containing forbidden knowledge. The five emperors, consumed by their desire for power, performed a ritual to harness the Codex's energy. Instead of power, they unleashed **The Seven Archsins** — primordial entities representing the darkest aspects of humanity.

Each Archsin corrupted a region of Aetheris, and the empires fell. The once-advanced civilizations crumbled into ruins, their knowledge lost, their people scattered.

## 2.3 The Player's Journey

The player begins in **Sulthari**, the last standing fragment of civilization — a fortified city-state that has survived by sealing itself off from the corrupted world. The player is a **Codex Bearer**, the first person in a thousand years to resonate with the Void Codex, gaining the ability to absorb and wield its power without being corrupted.

The Elder Council of Sulthari sends the player on a mission: travel across the five fallen empires, defeat the Seven Archsins and their servants, and restore the Void Codex to its sealed state before the corruption consumes what remains of Aetheris.

## 2.4 The Seven Archsins (Main Bosses)

Each Archsin rules a corrupted domain and has unique mechanics:

| # | Archsin | Sin | Domain | Empire Region | Fight Mechanic |
|---|---------|-----|--------|---------------|----------------|
| 1 | **Vorath** | Wrath | Burning Citadel | Nordrath | Berserker rage phases, arena shrinks over time |
| 2 | **Luxara** | Lust | Enchanted Gardens | Parsivan | Charm mechanics, illusion clones, mirror puzzles |
| 3 | **Avarok** | Greed | Golden Vaults | Aureum | Steals player items during fight, hoards power-ups |
| 4 | **Morthis** | Sloth | Stagnant Marsh | Khemet (outskirts) | Slows player, summons minions while staying passive |
| 5 | **Gularth** | Gluttony | Feast Halls | Nordrath (underground) | Devours terrain, grows larger, area denial |
| 6 | **Envyris** | Envy | Shadow Throne | Aureum (ruins) | Copies player abilities, adapts to player's class |
| 7 | **Prython** | Pride | Celestial Spire | Above Sulthari | Final boss, all mechanics combined, multi-phase |

## 2.5 The Ten Heralds (Sub-Bosses)

Each Archsin commands Heralds — corrupted champions of the old empires. The Ten Heralds are mini-bosses found in dungeons before reaching each Archsin:

| Herald | Title | Serves | Location | Mechanic |
|--------|-------|--------|----------|----------|
| Kaleth | The Burning Blade | Vorath | Nordrath Forge | Fire damage + weapon throws |
| Serath | The Blood Maiden | Vorath | Nordrath Arena | Lifesteal + blood pools |
| Mirael | The Whisperer | Luxara | Parsivan Baths | Confusion + invisibility |
| Thessyn | The Silk Weaver | Luxara | Parsivan Silk Road | Web traps + poison |
| Goldarc | The Coinlord | Avarok | Aureum Treasury | Gold projectiles + shields |
| Nixara | The Hollow Merchant | Avarok | Aureum Market | Trap doors + fake items |
| Dormiel | The Dreamer | Morthis | Khemet Catacombs | Sleep clouds + nightmare summons |
| Fenrath | The Devourer | Gularth | Nordrath Caverns | Swallow attack + acid |
| Shadeyn | The Mirror | Envyris | Aureum Colosseum | Clones player's gear |
| Solrath | The False Prophet | Prython | Sulthari Temple | Holy damage + resurrection |

## 2.6 Story Progression

```
Act I — The Awakening (Sulthari)
├── Tutorial: Learn class abilities in Sulthari Training Grounds
├── First quest: Clear corrupted creatures outside Sulthari walls
└── Unlock: Access to Nordrath region

Act II — The Northern Campaign (Nordrath)
├── Dungeon: Nordrath Forge → Herald: Kaleth
├── Dungeon: Nordrath Arena → Herald: Serath
├── Boss: Vorath (Wrath) at the Burning Citadel
└── Unlock: Access to Parsivan and Khemet

Act III — The Eastern Shadows (Parsivan & Khemet)
├── Dungeon: Parsivan Baths → Herald: Mirael
├── Dungeon: Parsivan Silk Road → Herald: Thessyn
├── Boss: Luxara (Lust) at the Enchanted Gardens
├── Dungeon: Khemet Catacombs → Herald: Dormiel
├── Boss: Morthis (Sloth) at the Stagnant Marsh
└── Unlock: Access to Aureum

Act IV — The Western Ruins (Aureum)
├── Dungeon: Aureum Treasury → Herald: Goldarc
├── Dungeon: Aureum Market → Herald: Nixara
├── Boss: Avarok (Greed) at the Golden Vaults
├── Dungeon: Nordrath Caverns → Herald: Fenrath
├── Boss: Gularth (Gluttony) at the Feast Halls
├── Dungeon: Aureum Colosseum → Herald: Shadeyn
├── Boss: Envyris (Envy) at the Shadow Throne
└── Unlock: Return to Sulthari

Act V — The Ascension (Sulthari)
├── Sulthari under siege by Prython's forces
├── Dungeon: Sulthari Temple → Herald: Solrath (The False Prophet)
├── Final Boss: Prython (Pride) at the Celestial Spire
└── Ending: Void Codex sealed, empires begin restoration
```

---

# 3. Player Classes & Skill System

## 3.1 Classes

Each class has a unique skill tree with active and passive abilities:

| Class | Role | Primary Stat | Playstyle |
|-------|------|-------------|-----------|
| **Vanguard** | Tank/Melee | Strength | Heavy armor, shields, crowd control |
| **Shadowstep** | DPS/Melee | Agility | Dual wield, dodge, critical strikes |
| **Arcanist** | DPS/Ranged | Intelligence | Elemental magic, area damage |
| **Warden** | Support/Heal | Wisdom | Healing, buffs, barriers |
| **Siegemaster** | DPS/Ranged | Strength | Crossbows, traps, explosives |

## 3.2 Skill Tree Structure (per class)

```
Tier 1 (Level 1-10): 3 basic abilities
Tier 2 (Level 11-20): 3 intermediate abilities + 1 passive
Tier 3 (Level 21-30): 2 advanced abilities + 1 ultimate
Total: 8 active + 2 passive + 1 ultimate per class
```

## 3.3 Example — Arcanist Skills

| Tier | Skill Name | Type | Description | Cooldown |
|------|-----------|------|-------------|----------|
| 1 | Ember Bolt | Active | Launches a fire projectile | 2s |
| 1 | Frost Shard | Active | Throws ice that slows enemies | 3s |
| 1 | Static Pulse | Active | AoE lightning around player | 5s |
| 2 | Inferno Wave | Active | Cone of fire damage | 8s |
| 2 | Glacial Wall | Active | Creates ice barrier | 12s |
| 2 | Storm Conduit | Active | Chain lightning between enemies | 10s |
| 2 | Arcane Affinity | Passive | +15% elemental damage | — |
| 3 | Meteor Strike | Active | AoE fire from sky | 30s |
| 3 | Void Rift | Active | Creates damaging void zone | 25s |
| 3 | Cataclysm | Ultimate | Massive AoE all elements | 120s |

## 3.4 Leveling System

- XP gained from killing mobs, completing dungeons, defeating bosses
- Skill points awarded every 2 levels
- Respec available at Sulthari Training Grounds (costs in-game currency)
- Max level: 30

---

# 4. Software Architecture

## 4.1 Project Structure

```
sins-of-fallen-empires/
├── src/main/java/com/sofe/
│   ├── SoFEMod.java                    # Main mod class
│   ├── config/
│   │   └── SoFEConfig.java             # Mod configuration
│   ├── entity/
│   │   ├── boss/
│   │   │   ├── ArchsinEntity.java       # Abstract base boss
│   │   │   ├── VorathEntity.java        # Wrath boss
│   │   │   ├── LuxaraEntity.java        # Lust boss
│   │   │   └── ...                      # Other Archsins
│   │   ├── herald/
│   │   │   ├── HeraldEntity.java        # Abstract sub-boss
│   │   │   ├── KalethEntity.java        # The Burning Blade
│   │   │   └── ...                      # Other Heralds
│   │   └── mob/
│   │       ├── CorruptedSoldier.java
│   │       ├── VoidCreeper.java
│   │       └── ...                      # Common enemies
│   ├── player/
│   │   ├── PlayerClass.java             # Enum of classes
│   │   ├── PlayerClassData.java         # Class data capability
│   │   ├── ClassSelectionHandler.java   # Class selection logic
│   │   └── LevelingSystem.java          # XP and leveling
│   ├── skill/
│   │   ├── Skill.java                   # Abstract skill class
│   │   ├── SkillTree.java               # Skill tree manager
│   │   ├── SkillCooldownManager.java    # Cooldown tracking
│   │   ├── active/
│   │   │   ├── EmberBolt.java
│   │   │   ├── FrostShard.java
│   │   │   └── ...                      # All active skills
│   │   └── passive/
│   │       ├── ArcaneAffinity.java
│   │       └── ...                      # All passive skills
│   ├── world/
│   │   ├── dimension/
│   │   │   ├── SulthariDimension.java
│   │   │   ├── NordrathDimension.java
│   │   │   └── ...                      # Empire dimensions
│   │   ├── structure/
│   │   │   ├── DungeonGenerator.java
│   │   │   ├── BossArena.java
│   │   │   └── ...                      # Structure generation
│   │   └── biome/
│   │       ├── BurningCitadelBiome.java
│   │       └── ...                      # Custom biomes
│   ├── item/
│   │   ├── weapon/
│   │   │   ├── SoFEWeapon.java          # Abstract weapon
│   │   │   ├── VoidBlade.java
│   │   │   └── ...
│   │   ├── armor/
│   │   │   ├── SoFEArmor.java           # Abstract armor
│   │   │   └── ...
│   │   └── consumable/
│   │       ├── HealthElixir.java
│   │       └── ...
│   ├── network/
│   │   ├── SoFENetwork.java             # Network handler
│   │   ├── SkillCastPacket.java
│   │   └── ClassSelectPacket.java
│   ├── event/
│   │   ├── PlayerEventHandler.java
│   │   ├── CombatEventHandler.java
│   │   └── WorldEventHandler.java
│   ├── gui/
│   │   ├── SkillTreeScreen.java
│   │   ├── ClassSelectScreen.java
│   │   └── BossHealthBar.java
│   └── registry/
│       ├── EntityRegistry.java
│       ├── ItemRegistry.java
│       ├── SkillRegistry.java
│       └── DimensionRegistry.java
├── src/main/resources/
│   ├── assets/sofe/
│   │   ├── textures/
│   │   │   ├── entity/
│   │   │   ├── item/
│   │   │   ├── gui/
│   │   │   └── block/
│   │   ├── models/
│   │   │   ├── entity/
│   │   │   └── item/
│   │   ├── lang/
│   │   │   ├── en_us.json
│   │   │   └── es_es.json
│   │   └── sounds/
│   ├── data/sofe/
│   │   ├── loot_tables/
│   │   ├── recipes/
│   │   ├── worldgen/
│   │   └── tags/
│   └── META-INF/mods.toml
├── build.gradle
├── settings.gradle
├── Dockerfile
├── .github/
│   └── workflows/
│       └── build.yml                    # CI/CD pipeline
├── README.md
├── CONTRIBUTING.md
├── LICENSE (MIT)
└── docs/
    ├── LORE.md
    ├── CLASSES.md
    └── SKILLS.md
```

## 4.2 Design Patterns Used

| Pattern | Usage | Java Concept |
|---------|-------|-------------|
| **Abstract Factory** | Creating different entity types (bosses, heralds, mobs) | Abstract classes, interfaces |
| **Strategy** | Different skill behaviors per class | Interfaces, polymorphism |
| **Observer** | Event system for combat, leveling, deaths | Event listeners, callbacks |
| **Singleton** | Skill registry, config manager | Static instances |
| **State** | Boss fight phases (phase 1, enrage, etc.) | Enums, state machines |
| **Command** | Skill execution with cooldowns and undo | Command pattern |
| **Builder** | Complex entity construction | Builder pattern |

## 4.3 Key OOP Concepts Demonstrated

```java
// Abstract class example — Base for all bosses
public abstract class ArchsinEntity extends Monster {
    protected int phase = 1;
    protected final Sin sinType;

    public ArchsinEntity(EntityType type, Level level, Sin sinType) {
        super(type, level);
        this.sinType = sinType;
    }

    public abstract void onPhaseChange(int newPhase);
    public abstract void useSpecialAbility(Player target);

    @Override
    public void tick() {
        super.tick();
        if (getHealth() < getMaxHealth() * 0.5 && phase == 1) {
            phase = 2;
            onPhaseChange(2);
        }
    }
}

// Concrete implementation — Vorath (Wrath)
public class VorathEntity extends ArchsinEntity {
    public VorathEntity(EntityType type, Level level) {
        super(type, level, Sin.WRATH);
    }

    @Override
    public void onPhaseChange(int newPhase) {
        // Enrage: increase speed and damage
        getAttribute(Attributes.MOVEMENT_SPEED).addModifier(...);
        getAttribute(Attributes.ATTACK_DAMAGE).addModifier(...);
    }

    @Override
    public void useSpecialAbility(Player target) {
        // Berserker charge toward player
    }
}

// Interface example — Skill system
public interface Skill {
    String getName();
    int getCooldown();
    int getManaCost();
    boolean canCast(Player player);
    void execute(Player player, Level level);
}

// Enum example
public enum Sin {
    WRATH, LUST, GREED, SLOTH, GLUTTONY, ENVY, PRIDE
}

public enum PlayerClass {
    VANGUARD, SHADOWSTEP, ARCANIST, WARDEN, SIEGEMASTER
}
```

---

# 5. Use Cases

## UC-01: Select Player Class
- **Actor:** Player
- **Precondition:** Player joins world for first time
- **Flow:** Player enters Sulthari → approaches Class Altar → GUI opens with 5 classes → Player selects class → abilities assigned → tutorial begins
- **Postcondition:** Player has class with Tier 1 skills

## UC-02: Use Active Skill
- **Actor:** Player
- **Precondition:** Player has class, skill unlocked, skill not on cooldown
- **Flow:** Player presses skill keybind → system checks cooldown and mana → skill executes → cooldown starts → visual/sound effects play
- **Postcondition:** Skill effect applied, cooldown timer active

## UC-03: Level Up
- **Actor:** Player
- **Precondition:** Player earns enough XP
- **Flow:** XP threshold reached → level up notification → skill point awarded (every 2 levels) → player opens skill tree → assigns point
- **Postcondition:** Player level increased, new skill available

## UC-04: Enter Dungeon
- **Actor:** Player
- **Precondition:** Player reaches dungeon entrance in the correct empire region
- **Flow:** Player enters structure → dungeon generates rooms → mobs spawn → player progresses through rooms → Herald boss at the end
- **Postcondition:** Herald defeated, loot dropped, path to Archsin unlocked

## UC-05: Fight Archsin Boss
- **Actor:** Player
- **Precondition:** All Heralds in that region defeated
- **Flow:** Player enters boss arena → cutscene intro → Phase 1 begins → at 50% HP Phase 2 triggers → unique mechanics activate → boss defeated → Codex fragment obtained
- **Postcondition:** Archsin defeated, region begins restoration, next region unlocked

## UC-06: Respec Skills
- **Actor:** Player
- **Precondition:** Player is in Sulthari, has enough gold
- **Flow:** Player interacts with Training Altar → confirms respec → all skill points refunded → player reassigns
- **Postcondition:** Skills reset, points available for redistribution

## UC-07: Craft Empire Weapon
- **Actor:** Player
- **Precondition:** Player has materials from defeated Herald/Archsin
- **Flow:** Player uses Forge station → selects recipe → materials consumed → weapon created with empire-specific bonuses
- **Postcondition:** Unique weapon in inventory

## UC-08: Multiplayer Boss Fight
- **Actor:** Multiple Players
- **Precondition:** All players in boss arena, boss not yet defeated
- **Flow:** Boss scales HP based on player count → players coordinate using different class roles → boss defeated → loot distributed
- **Postcondition:** All players receive rewards

---

# 6. Development Sprints

## Sprint 1 — Foundation (Weeks 1-2)
- [ ] Set up Forge MDK project with Gradle
- [ ] Create mod main class (SoFEMod.java)
- [ ] Implement PlayerClass enum and selection system
- [ ] Create ClassSelectScreen GUI
- [ ] Set up entity registry
- [ ] Create basic config system
- [ ] Initialize GitHub repo with CI/CD

## Sprint 2 — Skill System (Weeks 3-4)
- [ ] Implement Skill interface and abstract class
- [ ] Create SkillCooldownManager
- [ ] Implement 3 Arcanist Tier 1 skills (Ember Bolt, Frost Shard, Static Pulse)
- [ ] Create keybinding system for skills
- [ ] Add mana/resource system
- [ ] Implement skill visual effects (particles)

## Sprint 3 — Combat & Leveling (Weeks 5-6)
- [ ] Implement XP and leveling system
- [ ] Create SkillTree with point allocation
- [ ] Add SkillTreeScreen GUI
- [ ] Implement combat damage modifiers per class
- [ ] Add health/mana HUD overlay
- [ ] Create basic corrupted mob entities

## Sprint 4 — First Empire: Sulthari (Weeks 7-8)
- [ ] Create Sulthari dimension/biome
- [ ] Build Sulthari city structure (training grounds, altar, forge)
- [ ] Add NPCs for quests and class selection
- [ ] Implement tutorial quest chain
- [ ] Add Sulthari-themed textures and models
- [ ] Create portal system to other empires

## Sprint 5 — First Boss: Vorath (Weeks 9-10)
- [ ] Implement ArchsinEntity abstract class
- [ ] Create VorathEntity with 2-phase fight
- [ ] Build Burning Citadel structure
- [ ] Create Herald entities (Kaleth, Serath)
- [ ] Build Nordrath Forge and Arena dungeons
- [ ] Implement boss health bar GUI
- [ ] Add loot tables for boss drops

## Sprint 6 — Remaining Classes (Weeks 11-12)
- [ ] Implement Vanguard skills (Tier 1-3)
- [ ] Implement Shadowstep skills (Tier 1-3)
- [ ] Implement Warden skills (Tier 1-3)
- [ ] Implement Siegemaster skills (Tier 1-3)
- [ ] Balance damage/cooldown values
- [ ] Add remaining Arcanist Tier 2-3 skills

## Sprint 7 — Empires & Bosses (Weeks 13-16)
- [ ] Create Parsivan dimension + Luxara boss
- [ ] Create Khemet dimension + Morthis boss
- [ ] Create Aureum dimension + Avarok, Gularth, Envyris bosses
- [ ] Implement all Herald encounters
- [ ] Add empire-specific mobs and loot

## Sprint 8 — Final Act & Polish (Weeks 17-20)
- [ ] Implement Prython (Pride) final boss — multi-phase
- [ ] Create Celestial Spire structure
- [ ] Add ending sequence
- [ ] Implement multiplayer boss scaling
- [ ] Add sound effects and ambient music
- [ ] Localization (English + Spanish)
- [ ] Performance optimization
- [ ] Full testing pass

---

# 7. Visual Design Guide

## 7.1 How to Create Mod Visuals

| Asset Type | Tool | Format | Notes |
|-----------|------|--------|-------|
| **Block/Item textures** | Aseprite, Piskel, GIMP | 16x16 or 32x32 PNG | Pixel art, Minecraft style |
| **Entity models** | Blockbench | .java or .json | Free, designed for MC mods |
| **GUI screens** | Photoshop, GIMP, Figma | 256x256 PNG | Follow MC GUI conventions |
| **Particles** | Code-based in Java | — | Use Minecraft's particle system |
| **Structures** | Structure Block in-game or MCEdit | .nbt | Build in-game, export |
| **Sounds** | Audacity, Freesound.org | .ogg | OGG Vorbis format required |

## 7.2 Recommended Tools (All Free)

- **Blockbench** (blockbench.net): 3D model editor for Minecraft entities, free and browser-based
- **Aseprite** ($20) or **Piskel** (free): Pixel art for textures
- **GIMP**: Free image editor for GUI screens
- **Audacity**: Free audio editor for sound effects
- **Freesound.org**: Free sound effects library (check licenses)

## 7.3 Art Style Guidelines

- Follow Minecraft's 16x16 pixel art style for consistency
- Each empire has a color palette:
  - Sulthari: Gold, red, dark brown
  - Khemet: Sand, turquoise, gold
  - Parsivan: Purple, teal, silver
  - Nordrath: Ice blue, dark gray, blood red
  - Aureum: White marble, gold, royal blue
- Archsin bosses should be 3-4 blocks tall with glowing eyes matching their sin color
- Heralds are 2-3 blocks tall with empire-themed armor

---

# 8. Testing Strategy

| Test Type | Scope | Tool |
|-----------|-------|------|
| Unit Tests | Skill damage calculations, cooldown logic, XP formulas | JUnit 5 |
| Integration Tests | Class selection → skill assignment flow | Forge test framework |
| Manual Testing | Boss fights, dungeon generation, multiplayer | In-game playtesting |
| Performance | Particle effects, mob spawning, dimension loading | Spark profiler |

## Key Test Cases

- [ ] All 5 classes can be selected and have correct initial skills
- [ ] Skills respect cooldown timers
- [ ] Skills consume correct mana amount
- [ ] Boss phase transitions trigger at correct HP thresholds
- [ ] Leveling awards skill points every 2 levels
- [ ] Dungeons generate correctly without crashes
- [ ] Multiplayer boss HP scales with player count
- [ ] Respec refunds all points correctly
- [ ] All 7 Archsins can be defeated
- [ ] All 10 Heralds can be defeated
- [ ] Game does not crash on dimension transitions

---

# 9. Publishing & Open Source

## 9.1 Platforms
- **GitHub**: Source code, issues, wiki, CI/CD
- **CurseForge**: Mod distribution for players
- **Modrinth**: Alternative mod distribution

## 9.2 Open Source Setup
- License: MIT
- CONTRIBUTING.md with contribution guidelines
- Issue templates for bugs and feature requests
- Pull request template
- GitHub Actions for automated builds on every PR
- Wiki with lore, class guides, and development docs

## 9.3 README Structure
- Mod description with screenshots
- Installation instructions
- Feature list
- Class and skill overview
- Development setup for contributors
- Credits and license

---

# 10. Interview Value

When discussing this project in interviews, highlight:

- **OOP**: Abstract classes for bosses, interfaces for skills, enums for classes and sins
- **Design Patterns**: Factory, Strategy, Observer, State, Command, Builder
- **Event-Driven Architecture**: Forge event system = same concept as enterprise microservices
- **Networking**: Client-server packets for multiplayer = distributed systems fundamentals
- **CI/CD**: GitHub Actions automated builds = DevOps practices
- **Testing**: JUnit for game logic = same testing principles as enterprise software
- **Open Source**: Community management, code reviews, contribution guidelines

This project demonstrates every Java concept that Colombian enterprises ask for, packaged in a creative project that stands out from every other CRUD on GitHub.
