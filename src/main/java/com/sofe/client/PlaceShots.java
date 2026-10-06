package com.sofe.client;

import com.sofe.world.build.StructureBuilder;
import com.sofe.world.zone.StructurePositions;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;
import net.minecraftforge.event.TickEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A development tool, off unless the JVM is started with -Dsofe.placeShots=empire/place,... (the "placeShots"
 * run): once in a world it builds each story place at its position from structure_positions.json, flies the
 * player above it and takes two screenshots, from high over the south-west corner and from low beside the door,
 * then closes the game. It runs in run/saves/shots_aetheris, an Aetheris world, so each place is on its empire's land. Screenshots land in run/screenshots/place_&lt;empire&gt;_&lt;place&gt;_&lt;view&gt;.png.
 */
public final class PlaceShots {
    private static final int SETTLE = 60;
    private static List<String> places;
    private static int ticks, place = -1, view, wait;

    private PlaceShots() {
    }

    public static boolean enabled() {
        return System.getProperty("sofe.placeShots") != null;
    }

    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.getSingleplayerServer() == null) return;
        mc.options.pauseOnLostFocus = false;
        boolean talking = place >= 0 && place < (places == null ? 0 : places.size()) && places.get(place).startsWith("dialogue:");
        if (mc.screen != null && !(mc.screen instanceof com.sofe.client.screen.CrownedInAshScreen)
                && !(mc.screen instanceof com.sofe.client.screen.EndingScreen)
                && !(talking && mc.screen instanceof com.sofe.client.screen.DialogueScreen)) mc.setScreen(null); // the pause menu, the class choice and the prologue's dialogues of a journey
        if (places == null) {
            places = new ArrayList<>(Arrays.asList(System.getProperty("sofe.placeShots").split(",")));
            mc.options.hideGui = true;
            mc.options.setCameraType(CameraType.FIRST_PERSON);
            if (places.stream().anyMatch(pl -> pl.startsWith("boss") || pl.startsWith("npc:"))) mc.options.fov().set(40); // close portraits of single creatures
        }
        if (++ticks < 200) return;
        if (wait-- > 0) return;
        if (place >= 0) {
            String name = places.get(place).replace('/', '_').replace(':', '_');
            Screenshot.grab(mc.gameDirectory, "place_" + name + "_" + (view == 0 ? "high" : "door") + ".png", mc.getMainRenderTarget(), m -> { });
            logPerformance(mc, name + "_" + (view == 0 ? "high" : "door"));
            view++;
            if (view < 2 && !places.get(place).startsWith("at:") && !places.get(place).startsWith("npcs:") && !places.get(place).startsWith("npc:")
                    && !places.get(place).startsWith("scene:") && !places.get(place).startsWith("ending:") && !places.get(place).equals("loot")
                    && !places.get(place).startsWith("dialogue:")) {
                look(mc, places.get(place), view);
                wait = places.get(place).endsWith("/city") ? SETTLE * 4 : SETTLE; // a capital's far side takes a while to load
                return;
            }
        }
        place++;
        view = 0;
        if (place >= places.size()) {
            mc.stop();
            return;
        }
        build(mc, places.get(place));
        wait = places.get(place).equals("death") ? 40 : places.get(place).startsWith("scene:") || places.get(place).startsWith("ending:") || places.get(place).startsWith("dialogue:") ? 40 : places.get(place).equals("loot") ? SETTLE * 2 : places.get(place).startsWith("at:") ? SETTLE * 2
                : places.get(place).endsWith("/city") ? SETTLE * 12 : SETTLE * 4; // the chunks round the place need to load, and the build to reach the client (a death is caught while it is watched)
    }

    /** One line of run/placeshots_perf.log per screenshot: frames per second and the memory in use (docs/Rendimiento.md). */
    private static void logPerformance(Minecraft mc, String shot) {
        Runtime rt = Runtime.getRuntime();
        long usedMb = (rt.totalMemory() - rt.freeMemory()) >> 20, maxMb = rt.maxMemory() >> 20;
        String line = String.format("%s fps=%d memory=%d/%dMB chunks=%s%n", shot, mc.getFps(), usedMb, maxMb, mc.levelRenderer.getChunkStatistics());
        try {
            java.nio.file.Files.writeString(mc.gameDirectory.toPath().resolve("placeshots_perf.log"), line,
                    java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        } catch (java.io.IOException e) {
            com.sofe.SoFEMod.LOGGER.warn("Could not write placeshots_perf.log", e);
        }
    }

    private static void build(Minecraft mc, String piece) {
        var server = mc.getSingleplayerServer();
        if (piece.startsWith("npc:")) { // npc:<id>, one story NPC close up, from the front three-quarters
            String[] parts = piece.substring(4).split(":");
            String id = parts[0];
            boolean back = parts.length > 1 && parts[1].equals("back");
            server.execute(() -> {
                ServerPlayer p = server.getPlayerList().getPlayer(mc.player.getUUID());
                if (p == null) return;
                p.setGameMode(GameType.SPECTATOR);
                var level = p.serverLevel();
                level.setDayTime(6000);
                int x0 = p.getBlockX() - 60, z0 = p.getBlockZ();
                for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) {
                    level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x0 + dx, 219, z0 + dz), net.minecraft.world.level.block.Blocks.SMOOTH_SANDSTONE.defaultBlockState());
                }
                var hero = java.util.Arrays.stream(com.sofe.player.PlayerClass.values()).filter(c -> c.npcId().equals(id)).findFirst(); // a hero, with their class outfit
                var npc = hero.map(c -> new com.sofe.world.zone.StructurePositions.Npc(c.id(), "bearer", x0, z0, 0, null))
                        .orElse(new com.sofe.world.zone.StructurePositions.Npc(id, "citizen", x0, z0, 0, null));
                com.sofe.world.StoryPlacements.spawnNpc(level, npc).ifPresent(n -> {
                    n.setNoAi(true);
                    n.setYRot(20);
                    n.setYHeadRot(20);
                    n.setYBodyRot(20);
                });
                if (back) p.teleportTo(level, x0 + 0.5 + 1.5, 221.3, z0 + 0.5 - 2.6, 30, 15);
                else p.teleportTo(level, x0 + 0.5 - 1.3, 221.0, z0 + 0.5 + 3.6, 200, 6);
            });
            return;
        }
        if (piece.startsWith("npcs:")) { // npcs:<page>, 33 story NPCs a page on three tiers in the sky, still, facing the camera
            int page = Integer.parseInt(piece.substring(5));
            server.execute(() -> {
                ServerPlayer p = server.getPlayerList().getPlayer(mc.player.getUUID());
                if (p == null) return;
                p.setGameMode(GameType.SPECTATOR);
                var level = p.serverLevel();
                level.setDayTime(6000);
                java.util.List<String> ids = com.sofe.client.render.SoFEEntityRenderers.npcIds();
                int x0 = p.getBlockX() + 60 + page * 40, z0 = p.getBlockZ();
                for (int r = 0; r < 3; r++) {
                    for (int dx = -12; dx <= 12; dx++) for (int dz = -1; dz <= 0; dz++) {
                        for (int yy = 216; yy < 220 + r * 2; yy++) {
                            level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x0 + dx, yy, z0 - r * 2 + dz), net.minecraft.world.level.block.Blocks.SMOOTH_STONE.defaultBlockState());
                        }
                    }
                    for (int i = 0; i < 11; i++) {
                        int k = page * 33 + r * 11 + i;
                        if (k >= ids.size()) break;
                        com.sofe.world.StoryPlacements.spawnNpc(level, new com.sofe.world.zone.StructurePositions.Npc(ids.get(k), "citizen",
                                x0 - 10 + i * 2, z0 - r * 2, 0, null)).ifPresent(n -> {
                            n.setNoAi(true);
                            n.setYRot(0);
                            n.setYHeadRot(0);
                            n.setYBodyRot(0);
                        });
                    }
                }
                p.teleportTo(level, x0 + 0.5, 221.5, z0 + 6.5, 180, 12);
            });
            return;
        }
        if (piece.startsWith("scene:")) { // scene:<bearer>:<second>, "Crowned in Ash" for that Bearer at that second
            String[] v = piece.split(":");
            ClientClassData.set(com.sofe.player.PlayerClass.byId(v[1]));
            server.execute(() -> { // a spectator is drawn as a ghostly head: the scene needs the whole Bearer
                ServerPlayer p = server.getPlayerList().getPlayer(mc.player.getUUID());
                if (p != null) p.setGameMode(GameType.CREATIVE);
            });
            mc.setScreen(new com.sofe.client.screen.CrownedInAshScreen((int) (Float.parseFloat(v[2]) * 20) - 30));
            return;
        }
        if (piece.startsWith("dialogue:")) { // dialogue:act1/shard, a scene of the story on screen (with its illustration, if any)
            String id = "sofe:" + piece.substring("dialogue:".length());
            server.execute(() -> {
                ServerPlayer p = server.getPlayerList().getPlayer(mc.player.getUUID());
                if (p != null) com.sofe.quest.DialogueService.open(p, id, null);
            });
            return;
        }
        if (piece.equals("loot")) { // loot: gear of each rarity on the ground, its beam and its rarity label (accessibility)
            server.execute(() -> {
                ServerPlayer p = server.getPlayerList().getPlayer(mc.player.getUUID());
                if (p == null) return;
                p.setGameMode(GameType.SPECTATOR);
                var level = p.serverLevel();
                level.setDayTime(18000);
                net.minecraft.core.BlockPos at = new net.minecraft.core.BlockPos(0, 220, 0);
                for (int dx = -6; dx <= 6; dx++) for (int dz = -4; dz <= 4; dz++) {
                    level.setBlockAndUpdate(at.offset(dx, -1, dz), net.minecraft.world.level.block.Blocks.POLISHED_DEEPSLATE.defaultBlockState());
                }
                var rarities = new com.sofe.gear.Rarity[]{com.sofe.gear.Rarity.TEMPERED, com.sofe.gear.Rarity.IMPERIAL};
                for (int i = 0; i < rarities.length; i++) {
                    var stack = com.sofe.gear.GearMaker.rollItem("sofe:glacial_iron_sword", 30, rarities[i], p, level.random);
                    var relic = com.sofe.gear.GearMaker.relic("vorath_wrath", p);
                    var item = (i == 0 ? stack : stack).orElse(null);
                    if (item == null) continue;
                    var e = new net.minecraft.world.entity.item.ItemEntity(level, at.getX() - 2.5 + i * 2.5, at.getY(), at.getZ() + 0.5, item, 0, 0, 0);
                    e.setNeverPickUp();
                    e.setUnlimitedLifetime();
                    level.addFreshEntity(e);
                    if (i == rarities.length - 1 && relic.isPresent()) {
                        var r = new net.minecraft.world.entity.item.ItemEntity(level, at.getX() + 2.5, at.getY(), at.getZ() + 0.5, relic.get(), 0, 0, 0);
                        r.setNeverPickUp();
                        r.setUnlimitedLifetime();
                        level.addFreshEntity(r);
                    }
                }
                p.teleportTo(level, at.getX() + 0.5, at.getY() + 0.6, at.getZ() + 4.5, 180, 8);
            });
            return;
        }
        if (piece.startsWith("ending:")) { // ending:<bearer>:<card>, the ending's card for that Bearer, a second and a half in
            String[] v = piece.split(":");
            ClientClassData.set(com.sofe.player.PlayerClass.byId(v[1]));
            server.execute(() -> {
                ServerPlayer p = server.getPlayerList().getPlayer(mc.player.getUUID());
                if (p != null) p.setGameMode(GameType.CREATIVE);
            });
            java.util.Map<String, String> fates = new java.util.HashMap<>();
            for (int i = 3; i < v.length; i++) fates.put(v[i].split("=")[0], v[i].split("=")[1]); // ending:<bearer>:<card>:khemet=rest
            mc.setScreen(new com.sofe.client.screen.EndingScreen(Integer.parseInt(v[2]), v.length > 3 ? fates : null));
            return;
        }
        if (piece.startsWith("at:")) { // at:x:y:z:yaw:pitch, one look from there (inside a place built earlier in the same run)
            String[] v = piece.split(":");
            server.execute(() -> {
                ServerPlayer p = server.getPlayerList().getPlayer(mc.player.getUUID());
                if (p == null) return;
                p.setGameMode(GameType.SPECTATOR);
                p.serverLevel().setDayTime(6000);
                p.teleportTo(p.serverLevel(), Double.parseDouble(v[1]) + 0.5, Double.parseDouble(v[2]), Double.parseDouble(v[3]) + 0.5,
                        Float.parseFloat(v[4]), Float.parseFloat(v[5]));
            });
            return;
        }
        if (piece.equals("death")) { // the player dies: the screenshots show the title and the spectator's view with its countdown
            mc.options.hideGui = false;
            server.execute(() -> {
                ServerPlayer p = server.getPlayerList().getPlayer(mc.player.getUUID());
                if (p == null) return;
                p.setGameMode(GameType.SURVIVAL);
                p.serverLevel().setDayTime(6000);
                p.hurt(p.damageSources().fellOutOfWorld(), Float.MAX_VALUE);
            });
            return;
        }
        if (piece.startsWith("boss:") || piece.startsWith("boss2:")) { // one creature alone (boss2: in its second phase) on a floor in the sky, close: from the front (view 0) and three-quarters (view 1)
            server.execute(() -> {
                ServerPlayer p = server.getPlayerList().getPlayer(mc.player.getUUID());
                if (p == null) return;
                p.setGameMode(GameType.SPECTATOR);
                var level = p.serverLevel();
                level.setDayTime(6000);
                server.setDifficulty(Difficulty.EASY, true);
                net.minecraft.core.BlockPos at = new net.minecraft.core.BlockPos(0, 220, 0);
                level.getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(net.minecraft.world.entity.Mob.class),
                        e -> e.getPersistentData().getBoolean("sofe_shot")).forEach(net.minecraft.world.entity.Entity::discard);
                for (int dx = -8; dx <= 8; dx++) for (int dz = -8; dz <= 8; dz++) {
                    level.setBlockAndUpdate(at.offset(dx, -1, dz), net.minecraft.world.level.block.Blocks.POLISHED_DEEPSLATE.defaultBlockState());
                }
                String name = piece.substring(piece.indexOf(':') + 1);
                boolean corrupted = name.equals("corrupted_zombie"); // boss:corrupted_zombie, a vanilla zombie of Act V, by night
                var type = corrupted ? net.minecraft.world.entity.EntityType.ZOMBIE
                        : net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getValue(net.minecraft.resources.ResourceLocation.tryParse("sofe:" + name));
                if (type == null || !(type.create(level) instanceof net.minecraft.world.entity.Mob mob)) return;
                if (corrupted) {
                    com.sofe.mob.MobTraits.apply(mob, 5);
                    level.setDayTime(18000);
                }
                mob.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 0, 0);
                mob.setYHeadRot(0);
                mob.setYBodyRot(0);
                mob.setNoAi(true);
                mob.getPersistentData().putBoolean("sofe_shot", true);
                level.addFreshEntity(mob);
                if (piece.startsWith("boss2:") && mob instanceof com.sofe.entity.boss.SoFEBossEntity boss) boss.enterPhase(level, 2, java.util.List.of());
                double far = mob.getBbHeight() * 1.15 + 2.5;
                p.teleportTo(level, at.getX() + 0.5, at.getY() + mob.getBbHeight() * 0.6, at.getZ() + 0.5 + far, 180, 5);
            });
            return;
        }
        if (piece.equals("mobs") || piece.equals("bosses")) { // creatures (or the bosses) in a row on a floor in the sky, front and side
            server.execute(() -> {
                ServerPlayer p = server.getPlayerList().getPlayer(mc.player.getUUID());
                if (p == null) return;
                p.setGameMode(GameType.SPECTATOR);
                var level = p.serverLevel();
                level.setDayTime(6000);
                server.setDifficulty(Difficulty.EASY, true); // monsters vanish in Peaceful
                net.minecraft.core.BlockPos at = new net.minecraft.core.BlockPos(p.getBlockX() + 40, 220, p.getBlockZ());
                for (int dx = -32; dx <= 32; dx++) for (int dz = -4; dz <= 4; dz++) {
                    level.setBlockAndUpdate(at.offset(dx, -1, dz), net.minecraft.world.level.block.Blocks.SMOOTH_STONE.defaultBlockState());
                }
                java.util.List<net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.Mob>> mobs = new java.util.ArrayList<>();
                if (piece.equals("bosses")) {
                    for (var t : java.util.List.of(com.sofe.registry.EntityRegistry.MIRAEL, com.sofe.registry.EntityRegistry.THESSYN,
                            com.sofe.registry.EntityRegistry.DORMIEL, com.sofe.registry.EntityRegistry.LUXARA, com.sofe.registry.EntityRegistry.MORTHIS,
                            com.sofe.registry.EntityRegistry.GOLDARC, com.sofe.registry.EntityRegistry.NIXARA, com.sofe.registry.EntityRegistry.AVAROK,
                            com.sofe.registry.EntityRegistry.FENRATH, com.sofe.registry.EntityRegistry.GULARTH, com.sofe.registry.EntityRegistry.SHADEYN,
                            com.sofe.registry.EntityRegistry.ENVYRIS)) mobs.add(t.get());
                } else {
                    com.sofe.registry.EntityRegistry.empireMobs().forEach(t -> mobs.add(t.get()));
                }
                double gap = piece.equals("bosses") ? 5.0 : 3.5;
                for (int i = 0; i < mobs.size(); i++) {
                    var mob = mobs.get(i).create(level);
                    if (mob == null) continue;
                    mob.moveTo(at.getX() - (mobs.size() - 1) * gap / 2 + i * gap, at.getY(), at.getZ() + 0.5, 0, 0);
                    mob.setYHeadRot(0);
                    mob.setYBodyRot(0);
                    mob.setNoAi(true);
                    mob.setPersistenceRequired();
                    level.addFreshEntity(mob);
                }
                double back = piece.equals("bosses") ? 12.5 : 13.5, side = piece.equals("bosses") ? -15 : 0;
                p.teleportTo(level, at.getX() + 0.5 + side, at.getY() + 3.0, at.getZ() + back, 180, 10);
            });
            return;
        }
        if (piece.equals("waystone")) { // a Waystone alone on a floor in the sky, by night (view 0) and by day (view 1)
            server.execute(() -> {
                ServerPlayer p = server.getPlayerList().getPlayer(mc.player.getUUID());
                if (p == null) return;
                p.setGameMode(GameType.SPECTATOR);
                var level = p.serverLevel();
                net.minecraft.core.BlockPos at = new net.minecraft.core.BlockPos(p.getBlockX(), 220, p.getBlockZ() - 4);
                for (int dx = -3; dx <= 3; dx++) for (int dz = -3; dz <= 3; dz++) {
                    level.setBlockAndUpdate(at.offset(dx, -1, dz), net.minecraft.world.level.block.Blocks.SMOOTH_SANDSTONE.defaultBlockState());
                }
                level.setBlockAndUpdate(at, com.sofe.registry.SoFEBlocks.WAYSTONE.get().defaultBlockState());
                level.setDayTime(18000);
                p.teleportTo(level, at.getX() + 0.5, at.getY() + 1.2, at.getZ() + 4.5, 180, 8);
            });
            return;
        }
        server.execute(() -> {
            ServerPlayer p = server.getPlayerList().getPlayer(mc.player.getUUID());
            if (p == null) return;
            server.setDifficulty(Difficulty.PEACEFUL, true);
            p.serverLevel().setDayTime(6000);
            p.setGameMode(GameType.SPECTATOR);
            StructurePositions.get().structure("sofe:" + piece).ifPresent(s -> {
                p.teleportTo(p.serverLevel(), s.x(), 200, s.z(), 0, 90);
                p.serverLevel().getChunk(s.x() >> 4, s.z() >> 4);
                // in an Aetheris world the story places are built as in a journey (each once, with its NPCs and
                // Waystones); elsewhere the place and the arenas inside its footprint are built directly
                if (com.sofe.world.SoFEWorld.isJourney(server)) {
                    com.sofe.world.StoryPlacements.placeNear(server, StructurePositions.get(),
                            (x, z) -> Math.abs(x - s.x()) <= s.sizeX() && Math.abs(z - s.z()) <= s.sizeZ());
                } else {
                    StructureBuilder.build(p.serverLevel(), s);
                    StructurePositions.get().structures().values().stream()
                            .filter(o -> o != s && Math.abs(o.x() - s.x()) < s.sizeX() && Math.abs(o.z() - s.z()) < s.sizeZ())
                            .forEach(o -> StructureBuilder.build(p.serverLevel(), o));
                }
            });
        });
        look(mc, piece, 0);
    }

    /** View 0: high over the south-west corner, looking at the middle; view 1: low before the place, looking in. */
    private static void look(Minecraft mc, String piece, int view) {
        var server = mc.getSingleplayerServer();
        if (piece.equals("waystone")) {
            if (view == 1) server.execute(() -> server.overworld().setDayTime(6000));
            return;
        }
        if (piece.equals("sulthari/inverted_throne") || piece.equals("sulthari/celestial_spire")) { // places high up or deep down
            server.execute(() -> {
                ServerPlayer p = server.getPlayerList().getPlayer(mc.player.getUUID());
                if (p == null) return;
                StructurePositions.get().structure("sofe:" + piece).ifPresent(s -> {
                    if (piece.endsWith("inverted_throne")) p.teleportTo(p.serverLevel(), s.x() + 0.5, -14, s.z() + (view == 0 ? 22 : -10), view == 0 ? 180 : 0, 10);
                    else p.teleportTo(p.serverLevel(), s.x() + (view == 0 ? 14 : -20), view == 0 ? 210 : 190, s.z() + (view == 0 ? 14 : 20), view == 0 ? 135 : -135, view == 0 ? 25 : 0);
                });
            });
            return;
        }
        if (piece.startsWith("boss:") || piece.startsWith("boss2:")) {
            if (view == 1) server.execute(() -> {
                ServerPlayer p = server.getPlayerList().getPlayer(mc.player.getUUID());
                if (p == null) return;
                double far = p.getZ() - 0.5;
                p.teleportTo(p.serverLevel(), 0.5 + far * 0.7, p.getY(), 0.5 + far * 0.7, 135, 5);
            });
            return;
        }
        if (piece.equals("mobs") || piece.equals("bosses")) {
            if (view == 1) server.execute(() -> {
                ServerPlayer p = server.getPlayerList().getPlayer(mc.player.getUUID());
                if (p == null) return;
                if (piece.equals("bosses")) p.teleportTo(p.serverLevel(), p.getX() + 30, p.getY(), p.getZ(), 180, 10); // the other half of the row
                else p.teleportTo(p.serverLevel(), p.getX() + 14, p.getY() - 1, p.getZ() - 7, 120, 6);
            });
            return;
        }
        server.execute(() -> {
            ServerPlayer p = server.getPlayerList().getPlayer(mc.player.getUUID());
            if (p == null) return;
            StructurePositions.get().structure("sofe:" + piece).ifPresent(s -> {
                p.serverLevel().getChunk(s.x() >> 4, s.z() >> 4);
                int ground = p.serverLevel().getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, s.x(), s.z());
                int reach = Math.min(Math.max(s.sizeX(), s.sizeZ()), 150); // a capital is wider than the render distance: closer, the near half shows
                if (piece.endsWith("void_gate") && view == 1) { // down in the vault, looking at the ring of frames
                    p.teleportTo(p.serverLevel(), s.x() + 0.5, ground - 31, s.z() - 1.5, 180, 30); // the ground here is the dome (7 above the floor)
                    return;
                }
                if (view == 0) {
                    double x = s.x() - reach * 0.55, z = s.z() + reach * 0.55, y = ground + reach * 0.55;
                    p.teleportTo(p.serverLevel(), x, y, z, -135, 38);
                } else {
                    boolean east = s.x() < 0; // Aureum's doors face east, the others west
                    double x = s.x() + (east ? 1 : -1) * (s.sizeX() / 2.0 + 14);
                    double y = Math.max(ground, p.serverLevel().getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) x, s.z())) + 6; // above a hill outside
                    p.teleportTo(p.serverLevel(), x, y, s.z(), east ? 90 : -90, 12);
                }
            });
        });
    }
}
