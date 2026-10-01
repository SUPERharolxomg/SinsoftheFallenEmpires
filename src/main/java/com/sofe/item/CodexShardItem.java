package com.sofe.item;

import com.sofe.registry.ItemRegistry;
import com.sofe.story.Sin;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * A Codex Shard (docs/Anexos.md, A3): one per Archsin, per player. The story itself counts the credit
 * in StoryProgress, so a lost Shard can be given back by the Council (Sprint 7.5).
 */
public class CodexShardItem extends Item {
    private static final String TAG_SIN = "sin";

    public CodexShardItem(Properties properties) {
        super(properties);
    }

    public static ItemStack of(Sin sin) {
        ItemStack stack = new ItemStack(ItemRegistry.CODEX_SHARD.get());
        stack.getOrCreateTag().putString(TAG_SIN, sin.id());
        return stack;
    }

    public static Optional<Sin> sin(ItemStack stack) {
        if (!stack.hasTag()) return Optional.empty();
        String id = stack.getTag().getString(TAG_SIN);
        return Arrays.stream(Sin.values()).filter(s -> s.id().equals(id)).findFirst();
    }

    @Override
    public Component getName(ItemStack stack) {
        return sin(stack).map(s -> (Component) Component.translatable("item.sofe.codex_shard.of", Component.translatable(s.translationKey())))
                .orElseGet(() -> super.getName(stack));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.sofe.codex_shard.desc").withStyle(ChatFormatting.DARK_PURPLE));
    }
}
