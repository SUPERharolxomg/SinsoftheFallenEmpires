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
        if (mc.screen != null) mc.setScreen(null); // the pause menu, the class choice and the prologue's dialogues of a journey
        if (places == null) {
            places = new ArrayList<>(Arrays.asList(System.getProperty("sofe.placeShots").split(",")));
            mc.options.hideGui = true;
            mc.options.setCameraType(CameraType.FIRST_PERSON);
            if (places.stream().anyMatch(pl -> pl.startsWith("boss"))) mc.options.fov().set(40); // close portraits of single creatures
        }
        if (++ticks < 200) return;
        if (wait-- > 0) return;
        if (place >= 0) {
            String name = places.get(place).replace('/', '_').replace(':', '_');
            Screenshot.grab(mc.gameDirectory, "place_" + name + "_" + (view == 0 ? "high" : "door") + ".png", mc.getMainRenderTarget(), m -> { });
            view++;
            if (view < 2) {
                look(mc, places.get(place), view);
                wait = SETTLE;
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
        wait = places.get(place).equals("death") ? 40 : SETTLE * 4; // the chunks round the place need to load, and the build to reach the client (a death is caught while it is watched)
    }

    private static void build(Minecraft mc, String piece) {
        var server = mc.getSingleplayerServer();
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
                var type = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getValue(net.minecraft.resources.ResourceLocation.tryParse("sofe:" + piece.substring(piece.indexOf(':') + 1)));
                if (type == null || !(type.create(level) instanceof net.minecraft.world.entity.Mob mob)) return;
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
                int reach = Math.max(s.sizeX(), s.sizeZ());
                if (piece.endsWith("void_gate") && view == 1) { // down in the vault, looking at the ring of frames
                    p.teleportTo(p.serverLevel(), s.x() + 0.5, ground - 31, s.z() - 1.5, 180, 30); // the ground here is the dome (7 above the floor)
                    return;
                }
                if (view == 0) {
                    double x = s.x() - reach * 0.55, z = s.z() + reach * 0.55, y = ground + reach * 0.55;
                    p.teleportTo(p.serverLevel(), x, y, z, -135, 38);
                } else {
                    boolean east = s.x() < 0; // Aureum's doors face east, the others west
                    double x = s.x() + (east ? 1 : -1) * (s.sizeX() / 2.0 + 14), y = ground + 6;
                    p.teleportTo(p.serverLevel(), x, y, s.z(), east ? 90 : -90, 12);
                }
            });
        });
    }
}
