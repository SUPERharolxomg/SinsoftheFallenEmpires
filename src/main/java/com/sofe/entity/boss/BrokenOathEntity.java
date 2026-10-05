package com.sofe.entity.boss;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * One of the Ten Broken Oaths (README, "The Ten Broken Oaths"): a guardian who betrayed one Law of the
 * Codex and fights with its inverted mechanic. Each serves an Archsin and guards the way to them.
 */
public abstract class BrokenOathEntity extends SoFEBossEntity implements com.sofe.entity.SoFEAnimated {

    private final software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache animationCache =
            software.bernie.geckolib.util.GeckoLibUtil.createInstanceCache(this);

    @Override
    public software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache getAnimatableInstanceCache() {
        return animationCache;
    }

    protected BrokenOathEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level, BossEvent.BossBarColor.WHITE);
        this.xpReward = 200;
    }

    /** The number of the Law it betrayed, 1 to 10. */
    public abstract int law();

    /** The Archsin it serves, e.g. "sofe:vorath". */
    public abstract String archsin();

    /** "You shall not raise your sword against one who surrenders": law.sofe.&lt;n&gt;. */
    public String lawKey() {
        return "law.sofe." + law();
    }

    /** Under its name, as a Bearer first comes near: the Law it broke. */
    @Override
    protected Component presenceSubtitle() {
        return Component.translatable("message.sofe.boss.oath_presence", Component.translatable(lawKey()));
    }

    /** As it falls, the Oath speaks the Law it betrayed. */
    @Override
    protected void onCredited(ServerPlayer player, boolean firstTime) {
        player.sendSystemMessage(Component.translatable("message.sofe.oath_broken", getDisplayName(), Component.translatable(lawKey()))
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        // an Oath Gem of its master's sin (docs/Pociones.md: Oath Gems drop only from the Broken Oaths): always the first
        // time, then one fight in three
        oathGem().ifPresent(gem -> {
            if (!firstTime && player.getRandom().nextFloat() >= OATH_GEM_CHANCE) return;
            var stack = new net.minecraft.world.item.ItemStack(com.sofe.registry.ItemRegistry.sinGem(gem, com.sofe.gear.SinGem.Form.OATH));
            if (!player.getInventory().add(stack)) player.drop(stack, false);
        });
    }

    public static final float OATH_GEM_CHANCE = 0.35f;

    /** The Oath Gem of the sin its Archsin embodies. */
    public java.util.Optional<com.sofe.gear.SinGem> oathGem() {
        return java.util.Optional.ofNullable(switch (archsin()) {
            case "sofe:vorath" -> com.sofe.gear.SinGem.WRATH_RUBY;
            case "sofe:luxara" -> com.sofe.gear.SinGem.LUST_AMETHYST;
            case "sofe:morthis" -> com.sofe.gear.SinGem.SLOTH_MOONSTONE;
            case "sofe:avarok" -> com.sofe.gear.SinGem.GREED_TOPAZ;
            case "sofe:gularth" -> com.sofe.gear.SinGem.GLUTTONY_AMBER;
            case "sofe:envyris" -> com.sofe.gear.SinGem.ENVY_EMERALD;
            case "sofe:prython" -> com.sofe.gear.SinGem.PRIDE_SUNSTONE;
            default -> null;
        });
    }

    @Override
    protected boolean usesRewardCoffer() {
        return true;
    }
}
