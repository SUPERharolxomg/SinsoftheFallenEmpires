# Core Gameplay Rules

Rules for everything a player runs into while playing that the other documents do not cover yet: death, travel, quests and dialogue, controls, storage, difficulty, what happens outside the campaign and after the ending, and server administration.

Each section gives the default behavior; the ones marked **Decided** were confirmed by the project owner. Every value is in the config.

---

## G1. Death and respawn: the Bearer's corpse

**Decided: Diablo II style.** When the player dies, their body stays where they fell with everything they were carrying, and they have to go back for it.

| On death | Rule |
|----------|------|
| Equipped gear and inventory | Stay on the **corpse** at the death spot |
| Story items (Bearer's Flask, Codex fragments) | Stay with the player (they are soulbound) |
| Dinars carried | −10% |
| Levels and XP | Never lost (optional `xpLossOnDeath` for Hard: a small part of the current level's progress, never a whole level) |
| Respawn | Last activated Waystone (see G2) or Sulthari, with no gear |

**The corpse** (`BearerCorpseEntity`) looks like the player's Bearer lying on the ground, with a light beam so it can be seen from afar.

- **Only the owner can loot it.** A Pact member can see it on the map, but not take anything (`corpseLootByPact = false`).
- **Recovering it:** right-click the corpse and everything goes back to the same slots, equipment included. If a slot is taken, the item goes to the inventory, and anything that does not fit stays on the corpse.
- **It never despawns** and nothing can destroy it: not lava, explosions, mobs or pistons.
- **Unreachable deaths:** if the player died in lava, in the void or inside a wall, the corpse appears at their last safe position.
- **Dying again** before recovering it leaves a second corpse with whatever the player had at that moment, like in Diablo II.
- **Finding it:** the Quest Compass points to the latest corpse, and with JourneyMap it gets a "Your corpse" waypoint.
- **Going back naked:** Yusuf sells cheap brass gear so the player can go back for their body. A companion or the Pact can escort them.
- **Boss arenas:** a participant who died can go back into the sealed arena through its gate while the fight is still on, to pick up their corpse and fight again.

**Why a corpse and not a grave block:** a block cannot be placed inside cities, dungeons or arenas (they are protected zones), and a block can be broken or covered. An entity does not have those problems.

- **Hardcore:** vanilla hardcore rules; no corpse.
- **Grave mods:** if the server uses another grave or corpse mod, `corpseSystem = false` turns ours off.

### Boss fights

- When the fight starts, the arena is sealed: no one enters or leaves until it ends.
- If every participant dies or leaves, the boss **resets** to full health after 30 s and the arena reopens.
- In multiplayer, a player at 0 health is downed first and can be revived (see [Anexos.md](Anexos.md#a5-co-op-bonus-pact-of-the-empires)).
- **Avarok** gives back every item he stole when he dies or when the fight resets. He never steals story items or Relics.

---

## G2. Travel

The map is 12,000 × 12,000 blocks. Walking back to Sulthari from Aureum to refill the Flask is not fun, so there is fast travel.

- **Aetherium Waystones** (`sofe:waystone`) are found in Sulthari, in every liberated camp and at every discovered dungeon entrance. The player activates one by touching it.
- Travel between activated Waystones is **free** from the Waystone screen. It is not allowed during combat, inside arenas, or toward a region the player has not unlocked.
- **Return Scroll** (Yusuf, 10 Dinars): teleports the player to Sulthari after 5 s of channeling. It also works as the "I am stuck" option.
- Horses, camels (Sulthari), boats and elytra work normally inside unlocked regions.
- In multiplayer, each player has their own activated Waystones. With `PACT_ESCORT`, a player can travel to a Waystone another Pact member has activated.

---

## G3. Quests and dialogue

### Quests

| Type | Description | Required |
|------|-------------|----------|
| **Main quests** | The acts of the story, in order. They drive the progression locks ([Mundo.md](Mundo.md#w2-progression-locks)). | Yes |
| **Bearer quests** | One short personal quest per act for the player's hero (Cassian and his order, Shirin and Laleh, etc.) | No, but they unlock extra dialogue and the full epilogue |
| **Side quests** | Region bounties, lost caravans, lore pieces. Reward: Dinars, Favor, Blueprints. Some of them set the region's **fate** (see below). | No, but they change the ending |

- Quests are **data-driven** (`data/sofe/quests/*.json`): steps, conditions, rewards and dialogue keys.
- The **Journal** shows active quests, the current act, completed quests and the Pact.
- The active objective feeds the Quest Compass and map waypoints ([Mundo.md](Mundo.md#w3-minimap-and-map)).

### Dialogue: Warcraft III style, text only

**Decided: no voice acting.** Bosses, Bearers and NPCs speak only with text, so there are no audio files to break and every line can be translated. Animations stay simple for now.

**The dialogue box** is drawn on top of the game, in the style of Warcraft III:

| Part | Description |
|------|-------------|
| Bar | Dark bar across the bottom of the screen with a gold Sulthari-style border |
| Portrait | 64×64 portrait of the speaker on the left, in the frame of the empire where the scene happens. NPCs and bosses use a still image; when the player speaks, their own head is drawn live from their skin and outfit ([Arte.md](Arte.md#asset-specs-and-briefs)) |
| Name | Speaker's name in gold above the text |
| Text | Appears letter by letter; a key finishes the line, the next press moves to the next line |
| Answers | Up to 4 answers when the player has a choice |
| Sound | Only a soft text blip while letters appear (can be turned off). No voices. |

Two modes:

- **Cinematic** (story scenes, boss intros, temptations, the ending): black letterbox bars at the top and bottom, the camera stays still and the player cannot move.
- **Conversation** (merchants, quest givers, companions): the box only, and the player can keep moving; walking away closes it.

**Simple animations for now:** the speaker turns to face the player and plays one talking gesture (a GeckoLib animation or a vanilla arm swing). Richer animations can come later without changing the dialogue files.

**Data:** every conversation is a JSON file in `data/sofe/dialogue/`: a list of lines with speaker, portrait, lang key and optional answers, each answer with its effect (set a fate, start a quest, give a reward). Every line is a lang key in `en_us.json` and `es_es.json`.

- Most choices change only the conversation and small rewards (a discount, extra lore).
- **Temptations:** each Archsin tempts the player before the fight. Resisting is always the path forward; the dialogue is stronger when it is the hero's own sin.

### Fates and epilogues (Fallout style)

**Decided.** Side quests change how the story ends for the world and for the hero, without splitting the main story into different branches:

- **The main story does not change:** Nahrazel is always defeated (plus the secret bad ending of Prython's offer, below).
- **Region fates:** each region has 2–3 side quests with a real choice. The choice sets a **fate** for that region. Example: in Nordrath, free the warriors trapped in Vorath's endless war, or let them fight forever.
- **Epilogue built from fates:** after the moment inside the Codex, the ending shows one slide per region (illustration + text), picked by that region's fate, then the Bearer's own epilogue.
- **Bearer variants:** the Bearer quests change the hero's epilogue. Example: if Shirin's quests are not done, Laleh does not find peace and the last slide is different.
- **Small reactions in the world:** an NPC remembers what the player did, a merchant gives or refuses a discount, a camp looks different.

| Region | Example choice | Fates |
|--------|----------------|-------|
| Sulthari | Protect the Low Bazaar during the invasion, or the Observatory | Bazaar thrives / Observatory rebuilt first |
| Nordrath | Free the warriors of the endless war, or let them fight | Clans at peace / Clans still at war |
| Parsivan | Wake the dreamers of the Gardens, or leave them in the dream | Court awakens / Court sleeps |
| Khemet | Guide the trapped souls, or bind them to protect the living | Souls at rest / Souls as guardians |
| Aureum | Rebuild the courts, or give the gold back to the people | Law restored / Wealth shared |

With 5 regions × 2–3 fates plus 5 Bearer variants, there are many different endings, but only about 15 slides and 10 epilogue texts to write, translate and test.

- Fates are stored **per player** in their story progress, so in multiplayer each player gets the ending of their own choices.
- A region fate is set once; the Journal shows which choices are still open.
- Fates are also **conditions** (`fate_is`), so a later quest or dialogue can react to them.

**Decided — Prython's offer: the secret bad ending.** The player *can* accept. Accepting plays **"Crowned in Ash"**, a short animation of that player's own Bearer ruling a burning Aetheris, different for each of the five heroes. Then the player returns to just before the choice.

- It is **not part of the official story**: the Journal, the Codex and the epilogues never mention it.
- It is **secret**: the only trace is a hidden advancement, with no hint of how to get it.
- It is **personal**: in multiplayer only the player who accepts sees it, on their own screen (see G14).

---

## G4. Controls

**Problem found:** [Pociones.md](Pociones.md) puts the potion belt on keys 1–4, but vanilla uses 1–9 for the hotbar.

**Recommended default:** a **Combat Bar** on top of the hotbar.

| Action | Default key |
|--------|-------------|
| Skill slots 1–5 | Hold **Left Alt** + 1–5 |
| Ultimate | Hold **Left Alt** + 6 |
| Potion belt 1–4 | Hold **Left Alt** + 7–9 and 0 |
| Bearer's Flask | **H** |
| Switch stance (Knight) / main class action | **R** |
| Skill tree | **K** |
| Character sheet | **I** |
| Journal | **U** |
| Codex Map | **N** |
| Waystone travel / Return Scroll | From the Waystone or the item |

- Every key can be changed in Minecraft's Controls menu (SoFE category).
- Optional setting: *Combat Bar toggle*, so the player presses Left Alt once instead of holding it.
- JourneyMap (J) and Xaero's (Y, B, M) are not used by default. Before release we check for conflicts with popular mods.

---

## G5. Storage

Players cannot place chests inside Sulthari (it is a protected zone), and loot adds up fast.

- **Personal Vault** at the Sulthari bank and in each liberated camp: 27 slots per player, expandable to 54 and 81 with Dinars. Same contents from every Vault block. Each player only sees their own.
- The **Bearer's Homestead** allows normal chests, barrels and building.

---

## G6. Difficulty

| Vanilla difficulty | SoFE effect |
|--------------------|-------------|
| Peaceful | **Not allowed in `sofe:aetheris`**: the story needs enemies. The game uses Easy instead. |
| Easy | Bosses −25% health and damage |
| Normal | Base values |
| Hard | Bosses +25% health, extra mechanics in phase 2 |
| Hardcore | Hard + vanilla hardcore death |

### Difficulty rises with each act

**Decided.** Every hostile mob in a journey (SoFE enemies, vanilla zombies, skeletons, spiders, creepers, endermen... and hostile mobs from other mods) gets a **level** when it spawns, and the level sets how strong it is.

**Its level** is the higher of two values:

1. **The region's level range** ([Mundo.md](Mundo.md#w1-one-world-with-a-fixed-map)): a mob in Nordrath is level 5–12, one in Aureum 20–27.
2. **The act floor** of the nearest player: once a player advances an act, mobs around them never spawn below it, even in regions from earlier acts. Coming back to Sulthari in Act IV means level 20 mobs, not level 1.

| Act | Act floor |
|-----|-----------|
| I | 1 |
| II | 5 |
| III | 12 |
| IV | 20 |
| V | 27 |

**What a level does**, for every level above 1 (values in `data/sofe/mob_scaling.json`):

| Stat | Per level | At level 30 |
|------|-----------|-------------|
| Health | +8% | ×3.3 |
| Damage | +6% | ×2.7 |
| Armor | +0.3 | +8.7 |

The vanilla difficulty (Easy, Normal, Hard) still applies on top.

**New traits for vanilla mobs as the story advances** (the act floor of the nearest player):

| Mob | From Act II | From Act III | From Act IV | From Act V |
|-----|-------------|--------------|-------------|------------|
| Zombie | Sometimes wears empire armor | Calls more reinforcements | Tougher reinforcements | Corrupted look (violet eyes), faster |
| Skeleton | — | Frost arrows (slowness) | Fire arrows | Volleys of two arrows |
| Spider | Poison bite, like cave spiders | Longer poison | Web shot that slows | — |
| Creeper | — | Shorter fuse | — | 10% chance to spawn charged |
| Enderman | — | Blinds for a moment when it hits | Teleports behind the player after being hit | — |

Creeper explosions still cannot break protected places ([Mundo.md](Mundo.md#layer-3--protected-places-cities-camps-dungeons-arenas)), so a shorter fuse makes them dangerous without wrecking the story's cities.

**Rewards follow the level:** a higher-level mob gives more experience and drops loot of its item level ([Pociones.md](Pociones.md#rarities-affixes-and-slots)).

- In multiplayer, the act floor comes from the nearest player within 64 blocks; with no player nearby, only the region's range is used.
- Mobs from other mods get the health, damage and armor scaling but none of the vanilla traits. A server can list mods to leave out in `mob_scaling.json`.
- Free mode worlds (not a journey) keep vanilla mobs as they are.

---

## G7. Other mods and free mode

**Decided.** The story keeps going when other mods do not touch it; when a mod breaks it, that world switches to **free mode**.

### Free mode

Free mode means: SoFE items, ores, gear and classes work (a class is picked with an Altar item), but there is **no story, no progression locks and no bosses**. It is used in two cases:

1. A world that was **not created with Begin the Journey** (a world the player already had, or one from a modpack). It does not have the map of Aetheris, so the story cannot happen there.
2. A SoFE journey where **another mod breaks the story** (see the table below).

The player always gets a message saying why: *"This world is in SoFE free mode: <reason>."* The server option `freeModeInOtherWorlds = false` turns SoFE content off in those worlds instead.

### Other mods in a SoFE journey

| Kind of mod | Examples | What happens |
|-------------|----------|--------------|
| **Content** | New swords, armor, tools, food, furniture, decoration | Story continues. Their items work like vanilla items (no affixes). |
| **Client and performance** | JEI, inventory tweaks, Sodium/Embeddium, shaders, sound mods | Story continues. |
| **Maps** | JourneyMap, Xaero's | Story continues (integration in [Mundo.md](Mundo.md#w3-minimap-and-map)). |
| **Travel, teleport and flight** | Waystone mods, `/home` and `/tpa` commands, jetpacks | Story continues. Locked regions stay locked: `RegionEnforcer` sends the player back no matter how they got there. |
| **Graves and death** | Grave or corpse mods | Story continues. The SoFE corpse is turned off (`corpseSystem = false`). |
| **Extra dimensions** | Twilight Forest, The Aether | Story continues. Coming back to Aetheris into a locked region is blocked like any other teleport. |
| **Structure mods** | Extra dungeons and villages | Story continues. They cannot generate inside SoFE protected zones or on the Seal Veil. |
| **Overworld generation** | Terrain and biome overhauls | Usually no effect: the SoFE journey uses its own world generator. If the mod **replaces** it, the world goes to free mode. |
| **Mods that break the story** | Mods that replace the SoFE world generator, remove bosses or story mobs, or change the SoFE dimensions | The world goes to **free mode**. |

### How SoFE detects it

- **Integrity check** every time a journey is loaded: the world still uses the SoFE generator, the saved region layout is there, and every story boss, NPC and structure is registered. If anything fails, the world switches to free mode and the reason is logged.
- **Incompatible list:** `data/sofe/compat/incompatible_mods.json` lists mod IDs known to break the story. If one is installed, the title screen warns the player before *Begin the Journey*, and the journey is created in free mode.
- **Adjustments instead of free mode** for mods that only need a switch: grave mods turn off the SoFE corpse, and the `keepInventory` game rule also skips the corpse.
- A server can force the story anyway with `allowIncompatibleMods = true`, at its own risk.

### Map layout versions

The region layout JSON is **saved into the world when it is created**. If a mod update changes coordinates, existing worlds keep their original layout; only new worlds use the new one. This keeps updates from moving a city into the middle of a player's base.

---

## G8. Late joiners and the prologue

A player who joins a server after the Night of the Eclipse has already happened still needs Act I:

- They play the **prologue instanced** in `sofe:echo`: the Eclipse Festival, the invasion, the shard, the tutorial and the Brass Sentinel.
- Then they arrive in the real Sulthari, in Act I, next to the Council.
- Regions already restored by other players stay restored; the player gets their credit through the Echo shrines ([Anexos.md](Anexos.md#a6-unique-items-per-player-online)).

---

## G9. After the ending

- The world stays open: every region is unlocked for that player and the landscape keeps healing.
- **Echo fights can be repeated** for Oath Gems, materials and Dinars. A Relic already owned does not drop again (one per player).
- **Ascended difficulty** (post-1.0): enemies scale beyond level 30 and gear gets one more affix.
- The **Eighth Lock** is only teased (the sequel hook); no playable content for it in 1.0.

---

## G10. Advancements

A custom advancement tab, **Sins of the Fallen Empires**, mirrors the story: one branch per act, plus branches for each Bearer quest, every Relic, every region's ore, and secrets (for example, accepting Prython's offer). This is how the player sees their progress in the vanilla way.

---

## G11. Disconnecting and leaving

| Situation | Rule |
|-----------|------|
| Disconnect during a boss fight | The player keeps their participation. If the boss dies while they are away, their Reward Coffer waits for them. |
| Everyone leaves the arena | The boss resets after 30 s (G1). |
| Leaving a Pact | Co-op bonuses stop at once; progress and items are not affected. |
| Quit in the middle of a dungeon | The dungeon keeps its state; the player logs in where they left. |
| Companion dies | Goes back to Sulthari and can be hired again after 5 minutes. |

---

## G12. Server administration

Commands, permission level 2 (operators):

| Command | Does |
|---------|------|
| `/sofe progress <player> get` | Shows act, quests, bosses and unlocks |
| `/sofe progress <player> set act <n>` | Moves the player to an act (grants earlier credit) |
| `/sofe progress <player> grant boss <id>` / `revoke boss <id>` | Adds or removes credit for a boss |
| `/sofe progress <player> reset` | Resets the player's campaign |
| `/sofe unstuck <player>` | Sends a player to their last safe position or Sulthari |
| `/sofe item restore <player>` | Gives back lost story items |
| `/sofe reload` | Reloads quests, conditions, merchant offers and skill values |

**Server performance.** The map is large and full of fixed structures. The server README recommends pre-generating the world with a chunk pre-generator before opening it to players.

---

## G13. Accessibility

- Item rarity is shown with **text and color**, never color alone (colorblind players).
- **Subtitles** for dialogue and for boss audio cues that warn of attacks.
- Options to reduce screen shake, flashes and particle density.
- Language follows the Minecraft setting (English and Spanish from 1.0).

---

## G14. Boss edge cases in multiplayer

| Boss | Rule |
|------|------|
| **Envyris** (copies the player) | Creates one copy per participant, up to 3. With more players, the copies take the classes of the players who have dealt the most damage. |
| **Luxara** (charms) | A charmed player cannot hurt their allies; the charm makes them walk toward Luxara instead. |
| **Avarok** (steals items) | Steals from each player separately and returns everything when he dies or the fight resets. |
| **Prython** (offer) | Each player answers the offer on their own screen. The fight continues for those who refused; anyone who accepts sees their own bad ending animation and then returns to just before the choice, rejoining the fight. |
| **Nahrazel phase 3** | Each player enters their own view inside the Codex; the phase ends when everyone has finished, or after a time limit. |
