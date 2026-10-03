package com.sofe.gear;

import com.sofe.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

/**
 * A veiled weapon, armor piece or jewel from the gambler (see {@link Gamble}). Bought from a gambler it is
 * unveiled at once; one found any other way is unveiled with a right click.
 */
public class VeiledItem extends Item {
    private final GearSlot slot;

    public VeiledItem(GearSlot slot, Properties properties) {
        super(properties.stacksTo(16));
        this.slot = slot;
    }

    public GearSlot slot() {
        return slot;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer server)) return InteractionResultHolder.success(held);
        ItemStack revealed = unveil(server, slot);
        held.shrink(1);
        if (!player.getInventory().add(revealed)) player.drop(revealed, false);
        return InteractionResultHolder.consume(held);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.sofe.veiled.tooltip").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }

    /** Lifts the veil: rolls the outcome and makes the item, with a sound and a message for the outcome. */
    public static ItemStack unveil(ServerPlayer player, GearSlot slot) {
        var random = GearMaker.random(player.getRandom());
        Gamble.Outcome outcome = Gamble.roll(random, slot);
        int itemLevel = Gamble.itemLevel(GearMaker.levelOf(player), random);
        ItemStack result = ItemStack.EMPTY;
        if (outcome == Gamble.Outcome.SWINDLE) {
            result = new ItemStack(ItemRegistry.VOID_ASH.get(), 1 + player.getRandom().nextInt(3));
        } else if (outcome == Gamble.Outcome.RELIC) {
            Optional<GearDataManager.Relic> relic = Gamble.pickRelic(GearDataManager.droppableRelicsFor(
                    com.sofe.skill.ClassState.classOf(player).map(com.sofe.player.PlayerClass::id).orElse(null)), slot, itemLevel, random);
            // a unique out of a veil is not bound: it can be traded like any other find
            result = relic.flatMap(r -> GearMaker.relic(r.id(), null)).orElse(ItemStack.EMPTY);
            if (result.isEmpty()) outcome = Gamble.Outcome.IMPERIAL;
        }
        if (result.isEmpty()) {
            LootGenerator generator = GearMaker.builder(player, itemLevel).slot(slot).rarity(outcome.rarity()).build();
            result = GearMaker.roll(generator, player.getRandom()).orElse(new ItemStack(ItemRegistry.VOID_ASH.get()));
        }
        announce(player, outcome, result);
        return result;
    }

    private static void announce(ServerPlayer player, Gamble.Outcome outcome, ItemStack result) {
        ChatFormatting color = switch (outcome) {
            case SWINDLE -> ChatFormatting.DARK_GRAY;
            case COMMON -> ChatFormatting.GRAY;
            case TEMPERED -> ChatFormatting.BLUE;
            case IMPERIAL -> ChatFormatting.YELLOW;
            case RELIC -> ChatFormatting.GOLD;
        };
        player.displayClientMessage(Component.translatable(outcome.messageKey(), result.getHoverName()).withStyle(color), false);
        var sound = switch (outcome) {
            case SWINDLE -> SoundEvents.VILLAGER_NO;
            case COMMON -> SoundEvents.ARMOR_EQUIP_LEATHER;
            case TEMPERED -> SoundEvents.AMETHYST_BLOCK_CHIME;
            case IMPERIAL -> SoundEvents.PLAYER_LEVELUP;
            case RELIC -> SoundEvents.UI_TOAST_CHALLENGE_COMPLETE;
        };
        player.level().playSound(null, player.blockPosition(), sound, SoundSource.PLAYERS, 0.9f, 1.0f);
        if (outcome == Gamble.Outcome.RELIC && player.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1, player.getZ(), 40, 0.5, 0.8, 0.5, 0.3);
        }
    }
}
