package com.sofe.client.hud;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sofe.config.SoFEConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraftforge.client.event.RenderLivingEvent;
import org.joml.Matrix4f;

/**
 * How much health an enemy has left, written over its head as numbers ("❤ 18 / 24"), not as a bar: green while it is
 * hale, yellow when hurt, red when nearly down. Only enemies near the player and in their sight; a boss has its own
 * bar at the top of the screen (BossHealthBar). The client option enemyHealth turns it off.
 */
public final class EnemyHealthLabels {
    /** How far an enemy's health is written. */
    private static final double RANGE = 24;

    private EnemyHealthLabels() {
    }

    public static void onRenderLiving(RenderLivingEvent.Post<?, ?> event) {
        LivingEntity mob = event.getEntity();
        Minecraft mc = Minecraft.getInstance();
        if (!(mob instanceof Enemy) || mob instanceof com.sofe.entity.boss.SoFEBossEntity || !mob.isAlive() || mob.isInvisible()
                || mc.player == null || mc.options.hideGui || !SoFEConfig.CLIENT.enemyHealth.get()) return;
        if (mob.distanceToSqr(mc.player) > RANGE * RANGE || !mc.player.hasLineOfSight(mob)) return;
        float health = mob.getHealth(), max = mob.getMaxHealth();
        float part = max <= 0 ? 0 : health / max;
        int color = part > 0.6f ? 0x7CE07C : part > 0.3f ? 0xF0D060 : 0xF05A4A;
        Component text = Component.literal("❤ ").withStyle(s -> s.withColor(0xE04848))
                .append(Component.literal(Math.max(1, (int) Math.ceil(health)) + " / " + (int) Math.ceil(max)).withStyle(s -> s.withColor(color)));
        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        // over the head, above the name when the creature shows one (an elite, a lord)
        pose.translate(0, mob.getBbHeight() + (mob.shouldShowName() ? 0.75 : 0.5), 0);
        pose.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
        pose.scale(-0.025f, -0.025f, 0.025f);
        Matrix4f matrix = pose.last().pose();
        Font font = mc.font;
        float x = -font.width(text) / 2f;
        int background = (int) (mc.options.getBackgroundOpacity(0.25f) * 255f) << 24;
        font.drawInBatch(text, x, 0, 0xFFFFFFFF, false, matrix, event.getMultiBufferSource(), Font.DisplayMode.SEE_THROUGH, background, event.getPackedLight());
        font.drawInBatch(text, x, 0, 0xFFFFFFFF, false, matrix, event.getMultiBufferSource(), Font.DisplayMode.NORMAL, 0, event.getPackedLight());
        pose.popPose();
    }
}
