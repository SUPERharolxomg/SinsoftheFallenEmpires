# Use Cases

Every use case of *Sins of the Fallen Empires* in one place. Story and terms: [README.md](../README.md). Classes: [Clases.md](Clases.md). Gear and economy: [Pociones.md](Pociones.md). Menu, merchants and multiplayer: [Anexos.md](Anexos.md). World and locks: [Mundo.md](Mundo.md). Core rules: [Jugabilidad.md](Jugabilidad.md).

| ID | Use case | Area |
|----|----------|------|
| UC-01 | Choose a Bearer | Classes |
| UC-02 | Use an active skill | Classes |
| UC-03 | Level up | Classes |
| UC-04 | Enter a dungeon | World |
| UC-05 | Fight an Archsin | Bosses |
| UC-06 | Respec skills | Classes |
| UC-07 | *Retired, replaced by UC-11* | — |
| UC-08 | Multiplayer boss fight | Multiplayer |
| UC-09 | Receive loot from an enemy | Gear |
| UC-10 | Mine and smelt an empire ore | Gear |
| UC-11 | Craft at the Imperial Forge | Gear |
| UC-12 | Socket a gem | Gear |
| UC-13 | Distill a potion | Potions |
| UC-14 | Use a potion or the Flask | Potions |
| UC-15 | *Retired, replaced by UC-17* | — |
| UC-16 | Start a new journey from the title screen | Menu |
| UC-17 | Trade with a SoFE merchant | Economy |
| UC-18 | Form a Pact | Multiplayer |
| UC-19 | Claim personal boss loot | Multiplayer |
| UC-20 | Late joiner fights an echo | Multiplayer |
| UC-21 | Recover a lost story item | Story |
| UC-22 | Hire a Bearer as a companion | Classes |
| UC-23 | Try to cross a sealed border | World |
| UC-24 | Unlock a region or dungeon | World |
| UC-25 | Change blocks in a protected place | World |
| UC-26 | Follow the objective on the map | World |
| UC-27 | Die and respawn | Core rules |
| UC-28 | Fast travel between Waystones | Core rules |
| UC-29 | Accept and complete a quest | Story |
| UC-30 | Talk to an NPC and choose an answer | Story |
| UC-31 | Use the Personal Vault | Core rules |
| UC-32 | Disconnect during a boss fight | Multiplayer |
| UC-33 | Keep playing after the ending | Story |
| UC-34 | Open a world that is not a SoFE journey | Menu |
| UC-35 | Join a server that is past Act I | Multiplayer |
| UC-36 | Administer a server | Administration |
| UC-37 | Enter the Burning Deep (Nether) | World |
| UC-38 | Open the Void Gate (End) | World |
| UC-39 | Recover your corpse | Core rules |
| UC-40 | Play a journey with other mods installed | Menu |
| UC-41 | Decide the fate of a region | Story |
| UC-42 | See the ending shaped by your choices | Story |

---

## Classes and progression

### UC-01: Choose a Bearer
- **Actor:** Player
- **Precondition:** the player starts a new journey (UC-16) or joins a world for the first time.
- **Flow:** the Night of the Eclipse intro plays → the class screen shows the five Bearers (hero, origin, role, resource) → the player picks one → the Codex shard pierces that hero in their own scene (Cassian protecting a child, Ankhareth in the morgue, Shirin in the Observatory, Rurik in the vault, Azhar on his throne) → the class, resource and first skills are assigned → the invasion tutorial begins.
- **Alternate flow (multiplayer):** with `uniqueBearersPerServer = true`, Bearers already taken by another player are shown as unavailable.
- **Alternate flow (King):** if the player is Azhar, the Council of Sulthari is led by Grand Vizier Ozhan and the King's scenes use the "in disguise" variant.
- **Postcondition:** the player has a class with its level 1 skills; the other Bearers exist as NPCs in Sulthari.

### UC-02: Use an active skill
- **Actor:** Player
- **Precondition:** the player has a class, the skill is unlocked and not on cooldown.
- **Flow:** the player presses the skill key → the client sends a packet → the server checks cooldown and resource (Resolve, Essence, Mana, Energy or Authority) → the skill runs → the cooldown starts → visual and sound effects play.
- **Class-specific effects:** the skill may leave a rune (Sorceress), a Mark (Thief), a soul (Necromancer), switch stance (Knight) or open a Decree zone (King).
- **Postcondition:** skill effect applied, resource spent, cooldown active.

### UC-03: Level up
- **Actor:** Player
- **Precondition:** the player earns enough XP.
- **Flow:** XP threshold reached → level-up notification → one skill point awarded → the player opens the skill tree → puts the point in an unlocked skill whose next rank their level allows → the skill gets stronger.
- **Also:** 5 attribute points are awarded; the player spends them on the character sheet (Strength, Agility, Intellect, Will, Charisma, Vitality).
- **Exception:** a point cannot go into a locked skill, a skill whose next rank needs a higher level, or an ultimate that already has its rank.
- **Postcondition:** level increased, new skill available.

### UC-06: Respec skills
- **Actor:** Player
- **Precondition:** the player is at the Sulthari Training Grounds and has enough Dinars.
- **Flow:** the player talks to the trainer → confirms the respec → all skill points are refunded → the player reassigns them.
- **Postcondition:** skills reset, points available.

### UC-22: Hire a Bearer as a companion
- **Actor:** Player (single-player or Pact without that Bearer)
- **Precondition:** the Bearer is not the player's own and is not played by anyone in the Pact; the player has no active companion.
- **Flow:** the player talks to the Bearer NPC in Sulthari → picks "Travel with me" → the companion joins with a level matched to the player → follows, fights with its class skills and comments on the story (especially near the Archsin of its own temptation).
- **Alternate flow:** dismissing the companion sends it back to Sulthari; another can be hired afterwards.
- **Postcondition:** one AI companion active.

## World and bosses

### UC-04: Enter a dungeon
- **Actor:** Player
- **Precondition:** the player reaches a dungeon entrance in the right empire region.
- **Flow:** the player enters the structure → the dungeon generates its rooms → mobs spawn → the player progresses through the rooms → the Broken Oath waits at the end → the Oath speaks the Law it betrayed and the fight uses the inverted mechanic.
- **Postcondition:** Broken Oath defeated, loot dropped (UC-19), path to the Archsin unlocked.

### UC-05: Fight an Archsin
- **Actor:** Player
- **Precondition:** every Broken Oath serving that Archsin has been defeated.
- **Flow:** the player enters the arena → intro cutscene; the Archsin tempts the player (stronger dialogue if it is the hero's own sin) → Phase 1 → at 50% health Phase 2 → unique mechanics activate → Archsin defeated → Codex fragment obtained (the fragment speaks with Nahrazel's voice after Vorath).
- **Special cases:** Envyris fights with a copy of the player's own character; Prython offers to trade power for the six fragments and the player must refuse; the Inverted Throne only opens if the player carries the Sealing Quill; Nahrazel has three phases and the third is fought inside the Codex, rewriting the seal with the Quill.
- **Postcondition:** Archsin defeated, region begins to heal, next region unlocked.

### UC-23: Try to cross a sealed border
- **Actor:** Player
- **Precondition:** the player reaches the Seal Veil of a region they have not unlocked.
- **Flow:** the player walks into the Veil → the Veil is solid for them → a message says what unlocks it ("Unlocks in Act II").
- **Alternate flow (bypass attempt):** the player flies over, throws an ender pearl or sails around → the server detects them in a locked region (once per second) or cancels the teleport → they return to their last safe position with *Void Rebuke*.
- **Exception:** creative, spectator and operators with `opsBypass` are not blocked.
- **Postcondition:** the player stays in unlocked regions.

### UC-24: Unlock a region or dungeon
- **Actor:** Player
- **Precondition:** the player completes the act, boss or quest step named in a lock condition.
- **Flow:** `StoryProgress` updates → the server re-evaluates the player's conditions → the new state is synced to the client → the Veil or Sealed Gate turns faint for that player and loses collision → the region title, Quest Compass and map waypoints point to the new place.
- **Postcondition:** the player can enter; other players keep their own state (`gateMode = PER_PLAYER`).

### UC-25: Change blocks in a protected place
- **Actor:** Player (or an explosion, piston, fire or mob)
- **Precondition:** the target block is inside a protected zone.
- **Flow:** the change is attempted → `ProtectionHandler` checks the zone rules → the change is cancelled (explosions only lose the protected blocks) → the player sees "This place is protected".
- **Alternate flow:** in the Bearer's Homestead or a marked breakable dungeon wall, the change is allowed.
- **Postcondition:** the story structure stays intact.

### UC-26: Follow the objective on the map
- **Actor:** Player
- **Flow:** the player accepts or advances a quest → the Quest Compass shows the arrow and distance → if JourneyMap is installed, a waypoint appears on the minimap and world map → the Codex Map in the Journal marks the objective → when the objective is completed, the waypoint is removed.
- **Postcondition:** the player always knows where the story continues, with or without a map mod.


### UC-37: Enter the Burning Deep (Nether)
- **Actor:** Player
- **Precondition:** the player has defeated Vorath.
- **Flow:** the player builds and lights a Nether portal → the portal opens → in the Nether, the same region locks apply at 1:8 → when the player goes back through a portal, the exit is checked against the overworld region.
- **Exception:** before Vorath falls, the portal cannot be lit and a message explains why.
- **Exception:** a portal whose exit would land in a locked region does not link.
- **Postcondition:** the player can gather Nether materials and travel faster between unlocked regions.

### UC-38: Open the Void Gate (End)
- **Actor:** Player
- **Precondition:** the player has defeated Envyris and carries 12 Eyes of Ender.
- **Flow:** the player goes to the Void Gate beneath the Great Observatory → places the 12 Eyes → the Gate opens for that player → they travel to the Outer Void (End).
- **Exception:** without the condition, the Gate stays sealed and shows what is missing.
- **Postcondition:** the player can gather Void Crystal and, optionally, fight the Ender Dragon.

## Gear, potions and economy

### UC-09: Receive loot from an enemy
- **Actor:** Player
- **Precondition:** the player kills a mod enemy or opens a dungeon chest.
- **Flow:** the system reads the enemy's loot table → picks a rarity from zone level and find bonuses → generates the base item → applies random affixes (60% class bias) → drops the item with a light beam in its rarity color.
- **Alternate flow:** a Broken Oath or Archsin gives its Relic and Blueprint through personal loot (UC-19).
- **Postcondition:** item on the ground with level, rarity and affixes stored in its NBT/components.

### UC-10: Mine and smelt an empire ore
- **Actor:** Player
- **Precondition:** the player is in the right region with the minimum tool.
- **Flow:** breaks the ore block → gets raw ore (3% rough gem) → smelts it in a vanilla furnace → gets an ingot.
- **Exception:** with a lower-tier tool the block drops nothing (as in vanilla).
- **Postcondition:** ingots in inventory.

### UC-11: Craft at the Imperial Forge
- **Actor:** Player
- **Precondition:** the player learned the Blueprint and has the ingots.
- **Flow:** opens the Forge → picks a recipe → the system validates materials → consumes them → creates a Tempered item with 1 guaranteed affix.
- **Postcondition:** crafted item in inventory; logged in the player's recipe codex.

### UC-12: Socket a gem
- **Actor:** Player
- **Precondition:** item with a free socket and a cut gem.
- **Flow:** opens the Jeweler → places item and gem → pays Dinars → the gem is set and adds its bonus.
- **Alternate flow:** removing a gem costs double and destroys it unless it is an Oath Gem.
- **Postcondition:** item with gem bonus.

### UC-13: Distill a potion
- **Actor:** Player
- **Precondition:** has the recipe, ingredients and brass flasks.
- **Flow:** places ingredients in the Alembic → it processes for 20 s (like a furnace) → outputs 3 potions.
- **Postcondition:** potions in the Alembic output.

### UC-14: Use a potion or the Flask
- **Actor:** Player
- **Precondition:** potion on the belt or charges in the Bearer's Flask; no shared cooldown active.
- **Flow:** presses the key → the client sends a packet → the server validates → applies the effect → removes the potion or charge → starts the 1 s cooldown.
- **Postcondition:** life or resource restored; HUD updated.

### UC-17: Trade with a SoFE merchant
- **Actor:** Player
- **Precondition:** the merchant NPC is in Sulthari or a liberated camp.
- **Flow:** the player talks to the NPC → the server builds the offer list filtered by act and Favor → applies the Favor discount → the player buys or sells → the server validates Dinars and stock → updates `Wallet`, stock and Favor.
- **Exception:** a Relic or story item cannot be sold; the button is disabled.
- **Postcondition:** Dinars, inventory, stock and Favor updated for *that player only*.

## Menu and story

### UC-16: Start a new journey from the title screen
- **Actor:** Player
- **Precondition:** the mod is installed and `replaceTitleScreen = true`.
- **Flow:** the game opens → `SoFETitleScreen` shows the panorama, logo and theme → the player presses *Begin the Journey* → picks difficulty and name → the world is created with `sofe:aetheris` → the Eclipse Festival intro plays → UC-01.
- **Alternate flow:** *Continue* opens the most recent world directly.
- **Postcondition:** the player is in Sulthari at the start of Act I.

### UC-21: Recover a lost story item
- **Actor:** Player
- **Flow:** the player talks to the Council of Sulthari → the server checks the capability (the player should have the item, but it is not in their inventory) → the item is given back with its data.
- **Postcondition:** the player has the item again, with the same charges and owner.

## Multiplayer

### UC-08: Multiplayer boss fight
- **Actor:** several players
- **Precondition:** all players are in the arena and the boss has not been defeated.
- **Flow:** boss health scales with player count (+60% per extra player, damage unchanged) → players coordinate their class roles → boss defeated → UC-19 for each participant.
- **Postcondition:** every participant gets their own reward.

### UC-18: Form a Pact
- **Actor:** 2–5 players
- **Flow:** player A runs `/sofe pact invite B` → B accepts in chat or the Journal → the server creates the Pact and syncs it to members → co-op bonuses apply while they are within 48 blocks.
- **Postcondition:** Pact visible in the Journal; the HUD shows the members' health. Bearers played in the Pact are no longer hireable as companions.

### UC-19: Claim personal boss loot
- **Actor:** each participating player
- **Precondition:** an Archsin or Broken Oath dies with the player registered as a participant.
- **Flow:** the Reward Coffer appears → the player opens it → the server generates their loot (Relic, Codex fragment, rolled items) with `sofe:owner` → the player takes it.
- **Exception:** a non-participant sees the Coffer empty.
- **Postcondition:** each participant has their own reward; the Coffer is marked as claimed for that UUID.

### UC-20: Late joiner fights an echo
- **Actor:** Player
- **Precondition:** the region's Archsin is already dead in the world, but the player has no credit for it.
- **Flow:** the player activates the Echo shrine → an instanced fight starts (world blocks do not change) → echo defeated → UC-19 for that player.
- **Postcondition:** the player's act advances; the world is unchanged.

### UC-32: Disconnect during a boss fight
- **Actor:** Player
- **Precondition:** the player is a registered participant in an active boss fight.
- **Flow:** the player disconnects → their participation is kept → the fight continues for the others → if the boss dies, the player's Reward Coffer waits for them.
- **Alternate flow:** if everyone leaves or dies, the boss resets after 30 s.
- **Postcondition:** the player gets their reward on return, or the boss is back at full health.

### UC-35: Join a server that is past Act I
- **Actor:** new player
- **Precondition:** the Night of the Eclipse already happened on that world.
- **Flow:** the player joins → chooses a Bearer (UC-01) → plays the prologue instanced in `sofe:echo` (festival, invasion, shard, tutorial, Brass Sentinel) → arrives in the real Sulthari in Act I.
- **Postcondition:** the player has their own Act I progress; restored regions stay restored and Echo shrines give them their credit later (UC-20).

## Core rules

### UC-27: Die and respawn
- **Actor:** Player
- **Flow:** the player dies → their corpse stays at the death spot with all equipped gear and inventory → the player keeps only story items and loses 10% of carried Dinars → they respawn at the last activated Waystone or Sulthari with no gear → the Quest Compass points to the corpse.
- **Alternate flow:** if the death spot is unreachable (lava, void, inside a wall), the corpse appears at the last safe position.
- **Alternate flow:** in hardcore, vanilla hardcore rules apply and there is no corpse.
- **Postcondition:** the player is alive again; levels are never lost; the corpse waits for them (UC-39).

### UC-39: Recover your corpse
- **Actor:** Player (the owner of the corpse)
- **Precondition:** the player has at least one corpse in the world.
- **Flow:** the player follows the compass or map waypoint → reaches the corpse → right-clicks it → every item goes back to its original slot, equipment included → the corpse disappears.
- **Alternate flow:** items whose slot is taken go to the inventory; what does not fit stays on the corpse.
- **Alternate flow:** the corpse is inside a boss arena with an active fight → the player, as a participant, goes back in through the arena gate.
- **Exception:** another player (even from the Pact) cannot take anything from it.
- **Postcondition:** the player has their gear back.

### UC-28: Fast travel between Waystones
- **Actor:** Player
- **Precondition:** at least two activated Waystones; the player is not in combat or inside an arena.
- **Flow:** the player uses a Waystone → the screen lists the activated Waystones in unlocked regions → the player picks one → teleport.
- **Alternate flow:** the player reads a Return Scroll → 5 s channel → teleport to Sulthari.
- **Postcondition:** the player is at the destination.

### UC-31: Use the Personal Vault
- **Actor:** Player
- **Flow:** the player opens any Vault block (Sulthari bank or a liberated camp) → sees their own storage → moves items → optionally pays Dinars to expand it (27 → 54 → 81 slots).
- **Postcondition:** items stored for that player only, the same in every Vault.

## Story and quests

### UC-29: Accept and complete a quest
- **Actor:** Player
- **Flow:** an NPC offers a quest (or the main quest advances by itself) → the quest appears in the Journal → the objective feeds the Quest Compass and map waypoints → the player completes each step → rewards are given → conditions are re-evaluated (UC-24).
- **Postcondition:** quest completed; new quests or places may open.

### UC-30: Talk to an NPC and choose an answer
- **Actor:** Player
- **Flow:** the player talks to an NPC → the Warcraft III style dialogue box opens at the bottom of the screen (portrait, name, text appearing letter by letter, no voice) → the player presses a key to advance → at a choice, picks one of up to 4 answers → the conversation continues or ends with its effect (lore, discount, quest, fate).
- **Alternate flow (cinematic):** in story scenes the letterbox bars appear and the player cannot move until the scene ends.
- **Special case:** accepting Prython's offer plays the "Crowned in Ash" bad ending and brings the player back to just before the choice.
- **Postcondition:** dialogue state and any fate saved in `StoryProgress`.

### UC-41: Decide the fate of a region
- **Actor:** Player
- **Precondition:** the player is on a side quest that sets a region's fate, and that fate is not set yet.
- **Flow:** the quest reaches its choice → the dialogue box shows the options → the player picks one → the region's fate is saved for that player → small reactions change in the world (an NPC, a merchant, a camp) → the Journal marks the choice as made.
- **Postcondition:** the fate is final and will pick that region's slide in the ending (UC-42).

### UC-42: See the ending shaped by your choices
- **Actor:** Player
- **Precondition:** Nahrazel has been defeated by this player.
- **Flow:** the moment inside the Codex plays → one slide per region is shown, picked by that region's fate → the Bearer's epilogue is shown, full or sad version depending on their personal quests → credits.
- **Postcondition:** the ending is recorded in the Journal and advancements; UC-33 follows.

### UC-33: Keep playing after the ending
- **Actor:** Player
- **Precondition:** Nahrazel has been defeated by this player.
- **Flow:** the epilogue of the player's Bearer plays → the player returns to Aetheris with every region unlocked → can repeat Echo fights for gems, materials and Dinars (no duplicate Relics).
- **Postcondition:** the campaign is marked as complete in the Journal and advancements.

## Menu and administration

### UC-34: Open a world that is not a SoFE journey
- **Actor:** Player
- **Flow:** the player opens a vanilla or modpack world → a message explains that the campaign needs a new journey → with `freeModeInOtherWorlds = true`, mod items and ores work and a class can be picked with an Altar item; no story, locks or bosses.
- **Postcondition:** the world plays in free mode, or with SoFE content off.

### UC-40: Play a journey with other mods installed
- **Actor:** Player
- **Precondition:** the player has other mods installed next to SoFE.
- **Flow:** the player starts or loads a journey → SoFE checks the incompatible list and the world integrity → everything passes → the story continues and the other mods work alongside it (content, maps, travel, graves adjusted as in [Jugabilidad.md](Jugabilidad.md#g7-other-mods-and-free-mode)).
- **Alternate flow:** a mod on the incompatible list is installed → the title screen warns the player → the journey is created in free mode.
- **Alternate flow:** the integrity check fails on load → the world switches to free mode and the player sees the reason.
- **Postcondition:** the journey runs with the story, or in free mode with a clear reason.

### UC-36: Administer a server
- **Actor:** server operator
- **Flow:** the operator uses `/sofe progress`, `/sofe unstuck`, `/sofe item restore` or `/sofe reload` → the server validates the permission level → applies the change and syncs the affected player.
- **Postcondition:** progress, position, items or data updated; the change is logged.
