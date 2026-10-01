package com.sofe.entity.boss;

import com.sofe.item.CodexShardItem;
import com.sofe.story.Sin;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * One of the seven Archsins (README, "The Seven Archsins"). Besides what every boss does, an Archsin
 * tempts the player before the fight and leaves each participant their own Codex Shard.
 */
public abstract class ArchsinEntity extends SoFEBossEntity {

    protected ArchsinEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level, BossEvent.BossBarColor.RED);
        this.xpReward = 300;
    }

    public abstract Sin sin();

    /** The first defeat gives the Shard; later ones (Echo fights) do not give another. */
    @Override
    protected void onCredited(ServerPlayer player, boolean firstTime) {
        if (!firstTime) return;
        ItemStack shard = CodexShardItem.of(sin());
        if (!player.getInventory().add(shard)) player.drop(shard, false);
        player.sendSystemMessage(Component.translatable("message.sofe.codex_shard", Component.translatable(sin().translationKey()))
                .withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
