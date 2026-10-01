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
public abstract class BrokenOathEntity extends SoFEBossEntity {

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

    /** As it falls, the Oath speaks the Law it betrayed. */
    @Override
    protected void onCredited(ServerPlayer player, boolean firstTime) {
        player.sendSystemMessage(Component.translatable("message.sofe.oath_broken", getDisplayName(), Component.translatable(lawKey()))
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }

    @Override
    protected boolean usesRewardCoffer() {
        return true;
    }
}
