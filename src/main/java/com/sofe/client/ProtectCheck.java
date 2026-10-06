package com.sofe.client;

import com.sofe.SoFEMod;
import com.sofe.world.zone.ProtectedZoneData;
import com.sofe.world.zone.StructurePositions;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Dev check (./gradlew runClient -PprotectCheck): in the plaza of Sulthari, a player in creative who is also an
 * operator tries to take what a protected place holds: opens a chest, takes the flower from a pot, strips and strikes
 * an armor stand, takes the item from a frame and strikes it, breaks a block. Each try goes through the same calls
 * as the game's own packets; run/protectcheck.log says what held. Then the game quits.
 */
public final class ProtectCheck {
    private static int ticks;
    private static final List<String> LOG = new ArrayList<>();
    private static BlockPos chest, pot, stone;
    private static ArmorStand stand;
    private static ItemFrame frame;

    private ProtectCheck() {
    }

    public static boolean enabled() {
        return System.getProperty("sofe.protectCheck") != null;
    }

    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.getSingleplayerServer() == null) return;
        mc.options.pauseOnLostFocus = false;
        if (mc.screen != null && ticks < 150) mc.setScreen(null);
        ticks++;
        var server = mc.getSingleplayerServer();
        if (ticks == 150) server.execute(() -> setUp(server.getPlayerList().getPlayer(mc.player.getUUID())));
        if (ticks == 190) server.execute(() -> tryAll(server.getPlayerList().getPlayer(mc.player.getUUID())));
        if (ticks == 210) {
            try {
                Files.write(Path.of(mc.gameDirectory.getPath(), "protectcheck.log"), LOG);
            } catch (IOException e) {
                SoFEMod.LOGGER.error("protectcheck", e);
            }
            mc.stop();
        }
    }

    /** The things to take, put down in the plaza by the builder (no events), and the player beside them. */
    private static void setUp(ServerPlayer p) {
        if (p == null) return;
        ServerLevel level = p.serverLevel();
        int x = StructurePositions.get().spawnX() + 4, z = StructurePositions.get().spawnZ() + 4;
        level.getChunk(x >> 4, z >> 4);
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        chest = new BlockPos(x, y, z);
        pot = chest.east(2);
        stone = chest.east(4);
        level.setBlock(chest, Blocks.CHEST.defaultBlockState(), 2);
        if (level.getBlockEntity(chest) instanceof ChestBlockEntity c) c.setItem(0, new ItemStack(Items.DIAMOND, 5));
        level.setBlock(pot, Blocks.POTTED_POPPY.defaultBlockState(), 2);
        level.setBlock(stone, Blocks.STONE.defaultBlockState(), 2);
        BlockPos wall = chest.south(3);
        level.setBlock(wall, Blocks.STONE.defaultBlockState(), 2);
        level.setBlock(wall.above(), Blocks.STONE.defaultBlockState(), 2);
        stand = EntityType.ARMOR_STAND.create(level);
        stand.moveTo(x + 6.5, y, z + 0.5, 0, 0);
        stand.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        level.addFreshEntity(stand);
        frame = new ItemFrame(level, wall.above().north(), Direction.NORTH);
        frame.setItem(new ItemStack(Items.EMERALD));
        level.addFreshEntity(frame);
        p.setGameMode(GameType.CREATIVE);
        p.teleportTo(level, x + 0.5, y, z - 1.5, 0, 20);
        p.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        boolean zoned = ProtectedZoneData.get(level.getServer()).zones().stream().anyMatch(zn -> zn.contains(chest.getX(), chest.getY(), chest.getZ()));
        LOG.add("set up at " + chest.toShortString() + " | inside a protected zone: " + zoned + " | creative: " + p.isCreative()
                + " | operator: " + p.hasPermissions(2));
    }

    private static void tryAll(ServerPlayer p) {
        if (p == null || chest == null) return;
        ServerLevel level = p.serverLevel();
        // open the chest
        p.gameMode.useItemOn(p, level, ItemStack.EMPTY, InteractionHand.MAIN_HAND, hit(chest));
        boolean opened = p.containerMenu != p.inventoryMenu;
        if (opened) p.closeContainer();
        LOG.add("chest: opened=" + opened + " (should be false), diamonds still in it="
                + (level.getBlockEntity(chest) instanceof ChestBlockEntity c && c.getItem(0).is(Items.DIAMOND)));
        // take the flower from the pot
        p.gameMode.useItemOn(p, level, ItemStack.EMPTY, InteractionHand.MAIN_HAND, hit(pot));
        LOG.add("flower pot: still has its poppy=" + level.getBlockState(pot).is(Blocks.POTTED_POPPY));
        // strip the armor stand, then strike it
        Vec3 at = new Vec3(0, 1.8, 0);
        if (net.minecraftforge.common.ForgeHooks.onInteractEntityAt(p, stand, at, InteractionHand.MAIN_HAND) == null) {
            stand.interactAt(p, at, InteractionHand.MAIN_HAND);
        }
        LOG.add("armor stand: still wears its helmet=" + stand.getItemBySlot(EquipmentSlot.HEAD).is(Items.IRON_HELMET));
        p.attack(stand);
        p.attack(stand);
        LOG.add("armor stand: still standing after two blows=" + stand.isAlive());
        // take the item from the frame, then strike the frame
        p.interactOn(frame, InteractionHand.MAIN_HAND);
        p.attack(frame);
        LOG.add("item frame: still holds its emerald=" + frame.getItem().is(Items.EMERALD) + ", still hangs=" + frame.isAlive());
        // break a block
        p.gameMode.destroyBlock(stone);
        LOG.add("block: still there after breaking it in creative=" + level.getBlockState(stone).is(Blocks.STONE));
    }

    private static BlockHitResult hit(BlockPos pos) {
        return new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
    }
}
