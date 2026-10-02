package com.sofe.client;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A development tool, off unless the JVM is started with -Dsofe.armorShots=set1,set2,... (the "armorShots"
 * run): once in a world it dresses the player in each armor set in turn and takes a screenshot of it from
 * the front, the side and the back (third person), then closes the game. Screenshots land in
 * run/screenshots/armor_&lt;set&gt;_&lt;view&gt;.png, so the 3D armor can be checked without playing.
 */
public final class ArmorShots {
    private static final String[] PIECES = {"helmet", "chestplate", "leggings", "boots"};
    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private static final CameraType[] VIEWS = {CameraType.THIRD_PERSON_FRONT, CameraType.THIRD_PERSON_BACK};
    private static final String[] VIEW_NAMES = {"front", "back"};
    private static final int SETTLE = 12;

    private static List<String> sets;
    private static int ticks, set = -1, view, wait;

    private ArmorShots() {
    }

    public static boolean enabled() {
        return System.getProperty("sofe.armorShots") != null;
    }

    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.getSingleplayerServer() == null) return;
        if (sets == null) {
            sets = new ArrayList<>(Arrays.asList(System.getProperty("sofe.armorShots").split(",")));
            mc.options.hideGui = true;
            mc.options.fov().set(30); // a narrow view fills the picture with the player
        }
        if (++ticks < 200) return; // let the world load around the player
        if (wait-- > 0) return;
        if (set >= 0) {
            Screenshot.grab(mc.gameDirectory, "armor_" + sets.get(set) + "_" + VIEW_NAMES[view] + ".png", mc.getMainRenderTarget(), m -> { });
            view++;
        }
        if (set < 0 || view >= VIEWS.length) {
            set++;
            view = 0;
            if (set >= sets.size()) {
                mc.stop();
                return;
            }
            dress(mc, sets.get(set));
            wait = SETTLE * 3; // the teleport and the new armor need a moment to reach the client
            mc.options.setCameraType(VIEWS[view]);
            return;
        }
        mc.options.setCameraType(VIEWS[view]);
        wait = SETTLE;
    }

    /** All the pieces of a set on the player (or the one piece of a unique, by its own id). */
    private static void dress(Minecraft mc, String name) {
        var server = mc.getSingleplayerServer();
        server.execute(() -> {
            ServerPlayer p = server.getPlayerList().getPlayer(mc.player.getUUID());
            if (p == null) return;
            server.setDifficulty(Difficulty.PEACEFUL, true);
            p.serverLevel().setDayTime(6000);
            // a small stone floor high in the sky, so nothing stands between the camera and the player
            net.minecraft.core.BlockPos floor = new net.minecraft.core.BlockPos(p.getBlockX(), 230, p.getBlockZ());
            for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
                p.serverLevel().setBlockAndUpdate(floor.offset(dx, 0, dz), net.minecraft.world.level.block.Blocks.SMOOTH_STONE.defaultBlockState());
            }
            p.connection.teleport(floor.getX() + 0.5, floor.getY() + 1, floor.getZ() + 0.5, 0, 0);
            for (int i = 0; i < 4; i++) {
                Item piece = ForgeRegistries.ITEMS.getValue(com.sofe.SoFEMod.id(name + "_" + PIECES[i]));
                p.setItemSlot(SLOTS[i], piece == null || piece == Items.AIR ? ItemStack.EMPTY : new ItemStack(piece));
            }
            Item unique = ForgeRegistries.ITEMS.getValue(com.sofe.SoFEMod.id(name));
            if (unique != null && unique != Items.AIR && unique instanceof net.minecraft.world.item.ArmorItem armor) {
                p.setItemSlot(armor.getEquipmentSlot(), new ItemStack(unique));
            }
        });
    }
}
