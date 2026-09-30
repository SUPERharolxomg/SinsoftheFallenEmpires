# The Five Bearers

On the Night of the Eclipse, five shards of the Void Codex pierce five people in Sulthari. The player picks one; the other four stay alive as NPCs in Sulthari and can be hired as companions (one at a time). Each comes from a different empire and carries the temptation of one sin. Story context: [README.md](../README.md#2-story).

| Class | Hero | Origin | Role | Primary stat | Resource | Unique mechanic | Temptation |
|-------|------|--------|------|--------------|----------|-----------------|------------|
| **Knight** | Cassian Dravo | Aureum | Tank / melee | Strength | Resolve | Stances: Shield or Charge | Wrath |
| **Necromancer** | Ankhareth | Khemet | Summoner / control | Will | Essence | Binds the souls of fallen enemies | Sloth |
| **Sorceress** | Shirin Azarvand | Parsivan | Ranged magic damage | Intellect | Mana | Constellations: combine 3 runes | Lust |
| **Thief** | Rurik "Ash" Grimsson | Nordrath | Fast damage / stealth | Agility | Energy + Marks | Steals buffs and items from enemies | Greed |
| **King** | Sultan Azhar Tevfirán | Sulthari | Leader / offensive support | Charisma | Authority | Decrees and the royal guard | Pride |

## Skill tree structure

Every class has 10 skills:

| Levels | Skills |
|--------|--------|
| 1–10 | 3 active |
| 11–20 | 3 active + 1 passive |
| 21–30 | 1 active + 1 passive + 1 ultimate |

One skill point every 2 levels; max level 30. Costs and cooldowns below are starting values: they live in data files so they can be balanced without recompiling (see [Arquitectura.md](Arquitectura.md#4-implementation-notes-for-the-classes)).

---

## The Knight — Cassian Dravo

> "My shield has no empire. Only whoever stands behind it."

**Story.** Cassian is the last knight of the **Order of the Scale**, the guard that protected the courts of Aureum. When Greed and Envy took the republic, his order was slaughtered by its own corrupted brothers. Cassian fled to Sulthari carrying the broken banner and makes a living guarding caravans. On the Night of the Eclipse he steps between a shard and a child, and the shard chooses him.

**Motivation and temptation.** He wants to return to Aureum and judge those who betrayed his order. His weakness is **Wrath**: in Nordrath, Vorath offers him unlimited strength if he stops protecting and only destroys.

**Unique mechanic — Stances.** He switches between **Shield Stance** (less damage taken, generates Resolve when blocking) and **Charge Stance** (more damage and speed, spends Resolve).

| Level | Skill | Type | Effect | Cost | Cooldown |
|-------|-------|------|--------|------|----------|
| 1 | Scale Strike | Active | Deals more damage the more health the enemy has than you | 10 Resolve | 3 s |
| 1 | Shield Wall | Active | Blocks all frontal damage for 2 s | 15 Resolve | 8 s |
| 1 | Banner Cry | Active | Taunts nearby enemies into attacking you | 10 Resolve | 10 s |
| 11 | Legionary Charge | Active | Charges in a straight line and knocks enemies down | 20 Resolve | 8 s |
| 11 | Verdict | Active | Area strike that stuns enemies marked by Banner Cry | 25 Resolve | 12 s |
| 11 | Protector's Oath | Active | Absorbs 30% of the damage an ally takes for 6 s | 20 Resolve | 15 s |
| 11 | Iron Discipline | Passive | +15% armor in Shield Stance | — | — |
| 21 | Living Rampart | Active | Raises a 5-block barrier that blocks projectiles | 35 Resolve | 25 s |
| 21 | Contained Wrath | Passive | Blocked damage builds up and is released on the next hit | — | — |
| 30 | Last One Standing | Ultimate | For 10 s you cannot drop below 1 health and every hit heals allies | 100 Resolve | 120 s |

**Visual.** Silver plate armor with a royal blue cape and the scale engraved on the chest; a rectangular scutum-style shield with a golden crack. Blockbench model at player scale. Palette: marble, silver, royal blue, gold detail. Particles: golden sparks when blocking.

---

## The Necromancer — Ankhareth

> "Death is not the end. It is a contract, and I know how to read the fine print."

**Story.** Ankhareth was an embalmer priest of the **House of Thresholds** in Khemet, the order that guided souls to their rest. When Morthis (Sloth) fell upon Khemet, souls stopped departing and the dead stayed still, with neither rest nor life. Ankhareth fled to Sulthari with his order's papyri and works in secret in the city morgue. The shard pierces him while he prepares a body, and the dead man opens his eyes.

**Motivation and temptation.** He wants to free the trapped souls of Khemet, his master's among them. His weakness is **Sloth**: Morthis offers him eternal sleep beside his people, with no more pain.

**Unique mechanic — Binding souls.** He does not raise skeletons. When an enemy dies nearby it leaves a soul that the Necromancer can bind for a while to a **Clay Warden** (a canopic automaton) or consume as Essence. When the time runs out, the soul is released and leaves a small benefit.

| Level | Skill | Type | Effect | Cost | Cooldown |
|-------|-------|------|--------|------|----------|
| 1 | Threshold Touch | Active | Short beam that drains life and marks the enemy to leave a soul on death | 8 Essence | 1 s |
| 1 | Clay Warden | Active | Binds a soul to a clay golem that fights for 30 s (max 3) | 1 soul | 4 s |
| 1 | Burial Wraps | Active | Wraps that root an enemy for 3 s | 15 Essence | 10 s |
| 11 | Scales of Anubet | Active | Judges an enemy: below 25% health it dies instantly | 30 Essence | 20 s |
| 11 | Scarab Plague | Active | A swarm that deals damage over time and jumps between enemies | 20 Essence | 8 s |
| 11 | Canopic Jars | Active | Consumes 3 souls to heal you and your Wardens | 3 souls | 15 s |
| 11 | Rite of Passage | Passive | Each released soul gives +5% damage for 10 s (stacks ×5) | — | — |
| 21 | Boat of the Dead | Active | Summons a spectral boat that crosses the field and drags enemies | 50 Essence | 30 s |
| 21 | Heavy Heart | Passive | Wardens explode when they expire, dealing area damage | — | — |
| 30 | The Great Judgment | Ultimate | Opens a gate to the underworld for 12 s: enemies that die inside become allies | 100 Essence | 150 s |

**Visual.** Black linen robe with turquoise wraps, a bronze jackal mask and a staff shaped like a scale. The Wardens are clay golems with gold-painted faces. Palette: sand, black, turquoise, gold. Particles: golden dust and small scarabs.

---

## The Sorceress — Shirin Azarvand

> "The stars already wrote your ending. I just read it aloud."

**Story.** Shirin was an astronomer and poet of the court of Parsivan, the youngest member of the **Circle of the Astrolabe**. Her mentor and older sister, **Laleh**, was the first to fall to Luxara: today she is the voice that speaks through the Enchanted Gardens. Shirin escaped with her family's astrolabe and teaches at the Academy of Sulthari. On the Night of the Eclipse she is in the Great Observatory; the shard falls from the sky straight into her hand.

**Motivation and temptation.** She wants to rescue Laleh, or at least give her peace. Her weakness is **Lust**, understood as desire: Luxara shows her a perfect life with her sister and asks her to stay in the dream.

**Unique mechanic — Constellations.** Each basic spell leaves a star rune (Fire, Frost or Storm). With 3 runes a constellation is completed and fires an extra effect depending on the combination (for example, Fire + Frost + Storm = a steam explosion that stuns).

| Level | Skill | Type | Effect | Cost | Cooldown |
|-------|-------|------|--------|------|----------|
| 1 | Ember Verse | Active | Fire projectile; leaves a Fire rune | 8 Mana | 1 s |
| 1 | Frost Lance | Active | Icicle that slows; leaves a Frost rune | 10 Mana | 2 s |
| 1 | Wandering Spark | Active | Lightning that bounces between 3 enemies; leaves a Storm rune | 12 Mana | 3 s |
| 11 | Burning Calligraphy | Active | Writes a line of fire on the ground that burns whoever crosses it | 25 Mana | 10 s |
| 11 | Water Mirror | Active | Short teleport that leaves an ice clone as a decoy | 20 Mana | 8 s |
| 11 | Petal Tempest | Active | Whirlwind of petals and cutting wind around her | 30 Mana | 12 s |
| 11 | Sky Map | Passive | Completed constellations refund 20 Mana | — | — |
| 21 | Starfall | Active | A rain of 7 stars over a marked area | 60 Mana | 25 s |
| 21 | Arcane Poetry | Passive | +20% elemental damage if the 3 runes are all different | — | — |
| 30 | Written Eclipse | Ultimate | Darkens the sky for 10 s: every spell costs 0 and leaves 2 runes | 100 Mana | 150 s |

**Visual.** Purple robe with silver constellation embroidery, a turquoise veil and an astrolabe floating behind her. Palette: purple, turquoise, silver. Particles: glowing calligraphic letters that dissolve in the air.

---

## The Thief — Rurik "Ash" Grimsson

> "Everything has an owner until I walk by."

**Story.** Rurik was born into a minor Nordrath clan and became a relic smuggler along the fjords. They call him "Ash" because he survived the burning of his village hidden under the embers. In Sulthari he lives in the Low Bazaar, selling stolen objects to collectors. On the Night of the Eclipse he tries to steal a Codex shard from the Observatory vault; the shard fuses with his hand before he can sell it.

**Motivation and temptation.** At first he only wants to get rid of the shard and collect the Council's reward. Little by little he finds out that his village was destroyed by **Fenrath**, servant of Gluttony. His weakness is **Greed**: Avarok offers him all the gold in the Vaults if he betrays the group.

**Unique mechanic — Marks and stealing.** Basic attacks leave Marks (max 5) on an enemy; finishing skills consume them for extra damage. He can also steal from enemies: an active buff, a potion or, sometimes, a rare item.

| Level | Skill | Type | Effect | Cost | Cooldown |
|-------|-------|------|--------|------|----------|
| 1 | Double Edge | Active | Two quick slashes; 2 Marks | 10 Energy | 1 s |
| 1 | Light Fingers | Active | Steals a buff or consumable from the enemy | 15 Energy | 8 s |
| 1 | Smoke Step | Active | Smoke bomb: invisible for 3 s | 20 Energy | 12 s |
| 11 | Cutthroat | Active | Finisher: consumes Marks, +20% damage per Mark | 25 Energy | 6 s |
| 11 | Fjord Snare | Active | Sets a rope trap that roots and causes bleeding | 20 Energy | 10 s |
| 11 | Throwing Axe | Active | Throws an axe that returns to his hand and leaves 1 Mark per enemy | 15 Energy | 5 s |
| 11 | Deep Pockets | Passive | +25% gold and 10% chance of double loot | — | — |
| 21 | Thousand Cuts | Active | Leaps between 6 nearby enemies in 1 s | 40 Energy | 20 s |
| 21 | Scavenger's Instinct | Passive | Hits from behind are always critical | — | — |
| 30 | The Great Heist | Ultimate | For 8 s every hit steals an enemy skill that he can use once | 100 Energy | 120 s |

**Visual.** Wolf-fur hood, red scarf, light leather armor with a belt of pouches and two short axes. Palette: dark gray, ice blue, red. Particles: ash and embers while moving in stealth.

---

## The King — Sultan Azhar Tevfirán

> "My ancestor opened this door. I will be the one to close it."

**Story.** Azhar is the young sultan of Sulthari, crowned barely a year ago. He is a direct descendant of the emperor who read the first page of the Codex and later died sealing it. He grew up among books and advisors, above all Grand Vizier **Ozhan**, who raised him like a son. On the Night of the Eclipse, the shard pierces him on his own throne, in front of the whole court.

**Motivation and temptation.** He wants to atone for his bloodline's sin and prove he deserves the crown. His weakness is **Pride**: Prython does not offer him power, he offers him being *right*, being the emperor who unites the five realms under one throne. Ozhan's betrayal in Act V hits him harder than any other character.

**If the player does not pick the King**, Azhar is the NPC who runs the operations center in Sulthari. **If the player picks him**, the Council is led by the Grand Vizier and the scenes change: the King leaves his own city in disguise.

**Unique mechanic — Decrees and Authority.** Authority rises when he kills enemies and heals allies. **Decrees** are zones that change the rules of combat while you stand inside (for example, "no one may flee" or "allies share damage"). Only one Decree can be active at a time. He also summons the **Janissary Guard**, soldiers of brass and aetherium.

| Level | Skill | Type | Effect | Cost | Cooldown |
|-------|-------|------|--------|------|----------|
| 1 | Scepter Slash | Active | Strike with the scimitar-scepter; +Authority per hit | — | 1 s |
| 1 | Decree of Steadfastness | Active | Zone: allies inside get +20% armor | 20 Authority | 10 s |
| 1 | Janissary Guard | Active | Summons 2 brass soldiers for 30 s | 30 Authority | 20 s |
| 11 | Siege Decree | Active | Zone: enemies inside cannot leave and take +15% damage | 35 Authority | 18 s |
| 11 | Command | Active | Orders all allies and guards to attack one target with +30% damage | 25 Authority | 12 s |
| 11 | Royal Treasury | Active | Throws aetherium coins that heal the allies who pick them up | 30 Authority | 15 s |
| 11 | Imperial Lineage | Passive | Allies near the King regenerate 1% health per second | — | — |
| 21 | Bronze Cannon | Active | Deploys a siege cannon that fires 5 times | 50 Authority | 30 s |
| 21 | Voice of the Throne | Passive | Decrees last 50% longer and cover twice the area | — | — |
| 30 | Crown of the Five Lands | Ultimate | For 12 s every Decree is active at once and the Guard grows to 6 soldiers | 100 Authority | 180 s |

**Visual.** Red and gold kaftan over chainmail, a plumed turban and a small brass crown with an aetherium crystal; a scimitar that serves as a scepter. The Janissaries wear brass armor with visible gears. Palette: gold, red, dark brown. Particles: banners of light and golden sparks.

---

## Companions and multiplayer

- **Single-player:** the four Bearers not chosen live in Sulthari. The player can hire one at a time as an AI companion (see [CasosDeUso.md](CasosDeUso.md#uc-22-hire-a-bearer-as-a-companion)).
- **Multiplayer:** each player picks their own Bearer. A Bearer played by someone in the Pact does not appear as an NPC or companion for that Pact. By default two players may pick the same class; the server option `uniqueBearersPerServer = true` makes each Bearer available only once (up to 5 players, matching the story). Details in [Anexos.md](Anexos.md#a6-unique-items-per-player-online).
