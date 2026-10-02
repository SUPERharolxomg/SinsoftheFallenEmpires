package com.sofe.gear.ranged;

import com.sofe.player.PlayerClass;
import com.sofe.player.PlayerClassCapability;
import com.sofe.player.PlayerClassData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.util.List;
import java.util.Optional;

/**
 * Class items, as in Diablo II (the Sorceress's orbs, the Necromancer's wands, the Assassin's claws): only
 * the Bearer of that class can use them. Anyone else strikes for 1 and cannot cast. Creative players can.
 */
public final class ClassBound {
    private ClassBound() {
    }

    public static Optional<String> classOf(Player player) {
        return PlayerClassCapability.get(player).flatMap(PlayerClassData::get).map(PlayerClass::id);
    }

    /** Whether this player may use an item of this class (null: any class). */
    public static boolean allows(Player player, String requiredClass) {
        return requiredClass == null || player.isCreative() || classOf(player).map(requiredClass::equals).orElse(false);
    }

    /** Tells the player the item is not theirs and returns true when they may not use it. */
    public static boolean refuses(Player player, String requiredClass) {
        if (allows(player, requiredClass)) return false;
        player.displayClientMessage(Component.translatable("gear.sofe.class_only.refused",
                Component.translatable("class.sofe." + requiredClass)).withStyle(ChatFormatting.RED), true);
        return true;
    }

    /** "Knight only", gold for the right Bearer and red for anyone else. */
    public static void tooltip(List<Component> tooltip, String requiredClass) {
        if (requiredClass == null) return;
        boolean mine = FMLEnvironment.dist == Dist.CLIENT && Client.allows(requiredClass);
        tooltip.add(Component.translatable("gear.sofe.class_only", Component.translatable("class.sofe." + requiredClass))
                .withStyle(mine ? ChatFormatting.GOLD : ChatFormatting.RED));
    }

    private static final class Client {
        static boolean allows(String requiredClass) {
            Player player = Minecraft.getInstance().player;
            return player != null && ClassBound.allows(player, requiredClass);
        }
    }
}
