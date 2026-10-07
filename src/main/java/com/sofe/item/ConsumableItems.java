package com.sofe.item;

import com.sofe.combat.CombatCapability;
import com.sofe.combat.CombatHandler;
import com.sofe.economy.EconomyCapability;
import com.sofe.economy.EconomyHandler;
import com.sofe.registry.ItemRegistry;
import com.sofe.registry.SoFEEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * The potions of docs/Pociones.md ("Potions and alchemy") and the Bearer's Flask. Every potion shares
 * a 1 s cooldown with the others, so drinking two in the same moment is not possible.
 */
public final class ConsumableItems {
    public static final int SHARED_COOLDOWN = 20;

    private ConsumableItems() {
    }

    /** Puts every potion on cooldown at once. */
    public static void sharedCooldown(Player player) {
        for (var potion : ItemRegistry.potions()) player.getCooldowns().addCooldown(potion.get(), SHARED_COOLDOWN);
        player.getCooldowns().addCooldown(ItemRegistry.BEARERS_FLASK.get(), SHARED_COOLDOWN);
    }

    /** Heals or restores, then gives back the empty brass flask. */
    public abstract static class Potion extends Item {
        private final String description;

        protected Potion(Properties properties, String description) {
            super(properties.stacksTo(8));
            this.description = description;
        }

        protected abstract void drink(ServerPlayer player);

        @Override
        public int getUseDuration(ItemStack stack) {
            return 16;
        }

        @Override
        public UseAnim getUseAnimation(ItemStack stack) {
            return UseAnim.DRINK;
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            return ItemUtils.startUsingInstantly(level, player, hand);
        }

        @Override
        public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
            if (!(entity instanceof ServerPlayer player)) return stack;
            drink(player);
            sharedCooldown(player);
            if (player.getAbilities().instabuild) return stack;
            stack.shrink(1);
            ItemStack flask = new ItemStack(ItemRegistry.BRASS_FLASK.get());
            if (stack.isEmpty()) return flask;
            if (!player.getInventory().add(flask)) player.drop(flask, false);
            return stack;
        }

        @Override
        public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.translatable(description != null ? description : getDescriptionId() + ".desc").withStyle(ChatFormatting.GRAY));
        }
    }

    /**
     * A Pomegranate Elixir, one strength per act (20, 35, 50, 75 or 100 health; scripts/make_potions.py): half
     * heals at once, the rest over 3 seconds (Mending heals amplifier + 1 every second).
     */
    public static class Elixir extends Potion {
        private final int heal;

        public Elixir(Properties properties, int heal) {
            super(properties, null);
            this.heal = heal;
        }

        public int heal() {
            return heal;
        }

        @Override
        protected void drink(ServerPlayer player) {
            int now = heal / 2, later = heal - now;
            player.heal(now);
            player.addEffect(new MobEffectInstance(SoFEEffects.MENDING.get(), 60, Math.max(0, Math.round(later / 3f) - 1), false, true));
        }
    }

    /** A Bearer's Tonic, one strength per act: restores 25, 35, 50, 65 or 80% of the class resource. */
    public static class Tonic extends Potion {
        private final float fraction;

        public Tonic(Properties properties, float fraction) {
            super(properties, null);
            this.fraction = fraction;
        }

        public float fraction() {
            return fraction;
        }

        @Override
        protected void drink(ServerPlayer player) {
            restoreResource(player, fraction);
        }
    }

    static void restoreResource(ServerPlayer player, float fraction) {
        CombatCapability.get(player).flatMap(c -> c.resource()).ifPresent(pool -> {
            pool.set(Math.min(pool.max(), pool.current() + pool.max() * fraction));
            CombatCapability.get(player).ifPresent(c -> c.markDirty());
        });
        CombatHandler.sync(player);
    }

    /**
     * The Bearer's Flask (docs/Pociones.md): a story item with charges (3, +1 per Archsin, up to 10)
     * that heals 40% of life and resource together. The charges live in the player, not the item.
     */
    public static class Flask extends Item {
        public Flask(Properties properties) {
            super(properties.stacksTo(1).fireResistant());
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (player instanceof ServerPlayer server) drinkFlask(server);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        @Override
        public boolean isFoil(ItemStack stack) {
            return true;
        }

        @Override
        public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
            com.sofe.client.ClientEconomyData.get().ifPresent(e -> tooltip.add(Component.translatable("item.sofe.bearers_flask.charges",
                    e.flaskCharges(), e.flaskMax()).withStyle(ChatFormatting.AQUA)));
            tooltip.add(Component.translatable("item.sofe.bearers_flask.desc").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("gear.sofe.tooltip.soulbound").withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }

    /** Drinks from the Flask (the item or the Flask key, H). */
    public static boolean drinkFlask(ServerPlayer player) {
        if (player.getCooldowns().isOnCooldown(ItemRegistry.BEARERS_FLASK.get())) return false;
        if (!player.getInventory().contains(new ItemStack(ItemRegistry.BEARERS_FLASK.get()))) return false;
        if (atFullStrength(player)) { // a sip at full health only wasted a charge, and it looked as if the Flask did nothing
            player.displayClientMessage(Component.translatable("message.sofe.flask.full").withStyle(ChatFormatting.GRAY), true);
            return false;
        }
        float before = player.getHealth();
        boolean drank = EconomyCapability.get(player).map(e -> e.useFlask()).orElse(false);
        if (!drank) {
            player.displayClientMessage(Component.translatable("message.sofe.flask.empty").withStyle(ChatFormatting.GRAY), true);
            return false;
        }
        player.heal(player.getMaxHealth() * 0.4f);
        restoreResource(player, 0.4f);
        sharedCooldown(player);
        player.level().playSound(null, player.blockPosition(), SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 1f, 1.1f);
        EconomyHandler.sync(player);
        int healed = Math.round(player.getHealth() - before);
        EconomyCapability.get(player).ifPresent(e -> player.displayClientMessage(Component.translatable("message.sofe.flask.drank",
                healed, e.flaskCharges(), EconomyHandler.flaskMax(player)).withStyle(ChatFormatting.AQUA), true));
        return true;
    }

    /** Full life, and the class's resource (mana, rage...) full too: nothing for the Flask to give back. */
    private static boolean atFullStrength(ServerPlayer player) {
        if (player.getHealth() < player.getMaxHealth()) return false;
        return CombatCapability.get(player).flatMap(c -> c.resource()).map(pool -> pool.current() >= pool.max()).orElse(true);
    }
}
