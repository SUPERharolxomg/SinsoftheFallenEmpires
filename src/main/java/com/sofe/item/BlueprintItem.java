package com.sofe.item;

import com.sofe.economy.EconomyCapability;
import com.sofe.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Optional;

/**
 * A Blueprint (docs/Pociones.md, "Recipes"): dropped by Broken Oaths and dungeon chests. Using it
 * teaches the player its Imperial Forge recipe for good.
 */
public class BlueprintItem extends Item {
    private static final String TAG = "blueprint";

    public BlueprintItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static ItemStack of(String blueprint) {
        ItemStack stack = new ItemStack(ItemRegistry.BLUEPRINT.get());
        stack.getOrCreateTag().putString(TAG, blueprint);
        return stack;
    }

    public static Optional<String> blueprint(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains(TAG) ? Optional.of(stack.getTag().getString(TAG)) : Optional.empty();
    }

    /** The name of what the Blueprint makes (the result item's name). */
    public static Component target(String blueprint) {
        Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(blueprint));
        return item == null ? Component.literal(blueprint) : item.getDescription();
    }

    @Override
    public Component getName(ItemStack stack) {
        return blueprint(stack).map(b -> (Component) Component.translatable("item.sofe.blueprint.of", target(b))).orElseGet(() -> super.getName(stack));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Optional<String> blueprint = blueprint(stack);
        if (blueprint.isEmpty()) return InteractionResultHolder.fail(stack);
        if (player instanceof ServerPlayer server) {
            boolean learned = EconomyCapability.get(server).map(e -> e.learn(blueprint.get())).orElse(false);
            if (learned) {
                server.displayClientMessage(Component.translatable("message.sofe.blueprint.learned", target(blueprint.get())).withStyle(ChatFormatting.GOLD), true);
                level.playSound(null, server.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1f, 1f);
                if (!server.getAbilities().instabuild) stack.shrink(1);
            } else {
                server.displayClientMessage(Component.translatable("message.sofe.blueprint.known").withStyle(ChatFormatting.GRAY), true);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.sofe.blueprint.use").withStyle(ChatFormatting.GRAY));
    }
}
