# Running a server

How to host *Sins of the Fallen Empires* for friends or a community: installing, creating the journey, pre-generating the world, the options that change multiplayer, and the pitfalls. Performance and the pack's mods are in [Rendimiento.md](Rendimiento.md); the multiplayer rules in [Anexos.md](Anexos.md#a5-co-op-bonus-pact-of-the-empires).

Everything here was checked on a real Forge 1.20.1 dedicated server (`scripts/server_check.py`), with a client joining it (`scripts/pack_check.py --check join`).

## Install

1. Install the Forge **1.20.1-47.4.23** server: `java -jar forge-1.20.1-47.4.23-installer.jar --installServer` in an empty folder. It needs **Java 17**.
2. Put in `mods/` the mod's jar and the **server side of the pack**:

   | Mod | On the server | On the client |
   |-----|---------------|---------------|
   | Sins of the Fallen Empires, GeckoLib, Curios | Yes | Yes |
   | Furniture Mod: Refurbished and Framework, Supplementaries and Moonlight, Small Ships | Yes | Yes |
   | JEI | Yes | Yes |
   | ModernFix, FerriteCore, Canary, Saturn, Memory Leak Fix | Yes | Yes |
   | Xaero's Minimap, Embeddium, Entity Culling, ImmediatelyFast, Dynamic FPS | **No** (client only) | Yes |

3. Copy the pack's `config/` folder (from `pack/config`) next to `mods/`.
4. Accept the EULA (`eula.txt`: `eula=true`).

## Create the journey

The story only happens in a world made with the **Aetheris** world type. On a dedicated server, set it in `server.properties` **before the first start**:

```properties
level-type=sofe\:aetheris
```

The colon must be escaped as `\:`. A server started once without it has already made a vanilla world: stop it, delete the `world` folder and start again. On the first start the log says `Journey integrity check passed`; a world that is not a journey, or one in free mode, says so to every player who joins.

Recommended for a few friends:

```properties
view-distance=8
simulation-distance=6
spawn-protection=0
```

`spawn-protection=0` because Sulthari, at spawn, is already protected by the mod itself (no one can break or take anything there), and vanilla's spawn protection would also stop non-operators from opening doors and stations.

## Memory

| Players | Memory (`-Xmx`) |
|---------|-----------------|
| 1 to 5 | 4 GB |
| 6 to 10 | 6 GB |

Put the Java arguments in `user_jvm_args.txt`:

```text
-Xms4G -Xmx4G -XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200
```

Measured: with the pack's server mods and FTB Essentials, the server starts in about 25 seconds and runs at 20 TPS with a mean tick of about 1.6 ms while idle.

## Pre-generate the world

The world is 25,000 × 25,000 blocks, too large to pre-generate whole. Generate where the story goes first, with **Chunky** (Forge 1.20.1, Modrinth):

```text
chunky center 0 0
chunky radius 2500
chunky start
```

That covers Sulthari and the way north into Nordrath (Acts I and II): about 98,000 chunks. Checked with the Aetheris generator: on the development PC Chunky made about 180 chunks a second (a radius of 400 in 13 seconds), so a radius of 2,500 takes about ten minutes there, and longer on a smaller machine. Before each act, generate the next region around its places (their coordinates are in [Mundo.md](Mundo.md#w1-one-world-with-a-fixed-map)), for example Parsivan and Khemet: `chunky center 5000 2000` and `chunky radius 4000`. Pre-generate while nobody plays: it takes all the server's cores.

The story's places (Sulthari, the dungeons, the camps, the capitals) are built by the mod when the world is made or when their act comes, not by Chunky; pre-generating only spares the players the terrain.

## Options that change multiplayer

In `world/serverconfig/sofe-server.toml`:

| Option | Default | What it does |
|--------|---------|--------------|
| `uniqueBearersPerServer` | `false` | Each Bearer (class) can be chosen by one player only, as in the story |
| `gateMode` | `per_player` | `pact_escort`: a Pact member who meets a Sealed Gate's condition lets the members within 8 blocks through |
| `maxMembers` | 5 | Players in a Pact |
| `bossHealthPerPlayer` | 0.6 | Extra boss health per extra player in its arena (+60%) |
| `revive`, `downedSeconds`, `reviveSeconds` | on, 30, 3 | Downed instead of dead in boss arenas, while an ally stands |
| `relicBinding` | `free` | `pact_only` or `soulbound` to keep Relics with their owners |
| `opsBypass` | `true` | Operators pass through the Seal Veil (they never break protected places) |
| `protectZones` | `true` | Keep the story's places as they were built |
| `corpseSystem` | `true` | The Bearer's corpse with their gear where they fell; turn off with a graves mod |

Admin commands (permission 2, logged): `/sofe progress <player> show|act|defeat`, `/sofe unstuck`, `/sofe item restore`, `/sofe pacts`. Players: `/sofe pact invite|accept|decline|leave|kick|list`, `/sofe council restore` (in Sulthari: the story items they lost).

## Fair play

- **The map:** Xaero's Minimap shows caves and nearby creatures. To turn those off for everyone, use Xaero's fair-play mode (see Xaero's documentation for your version; it is set from the server, not by the mod).
- **Teleport mods:** `/home`, `/tpa`, `/rtp` and `/back` (FTB Essentials, checked) cannot open a sealed region: a player sent into one is sent back to their last safe place within a second. Waystones stay the story's way to travel.
- **Creative and operators:** an operator passes the Seal Veil while `opsBypass` is on; turn it off to play the story as an operator.

## Offline mode and player data

Every Bearer's progress (class, level, skills, story, Dinars, Favor, Waystones, Pact) is saved under the player's **UUID**.

- An **online-mode** server (`online-mode=true`, the default) keeps the UUID of each Minecraft account: progress follows the player.
- An **offline-mode** server makes the UUID from the **name**: a player who changes their name, or a server that switches between online and offline mode, finds a new Bearer with nothing. Choose the mode before anyone plays, and keep it.
- `uniqueBearersPerServer` also counts Bearers by UUID.

## Other mods

- **FTB Essentials** (with FTB Library and Architectury): checked on the server and in a client; its commands work, and the seals hold against its teleports.
- **Essential** (client): checked. Its menu (friends, wardrobe, hosting a world) opens from the **Essential menu** button on the SoFE title screen; hosting a world with Essential works like opening it to LAN.
- A mod known to break the story is listed in `data/sofe/compat/incompatible_mods.json`; with one installed, a journey plays in **free mode** (the items work, the story waits) and every player is told why. The list is empty for now.

## Checking a server

From the repository, with the Forge server installed in `build/packcheck/server`:

```text
python scripts/server_check.py                   # starts it, runs a few commands, says what went wrong
python scripts/server_check.py --extra ftb --hold 300
python scripts/pack_check.py --check join        # in another terminal: a real client joins it
```
