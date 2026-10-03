package com.sofe.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraftforge.client.event.RenderPlayerEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * The casting poses of the Bearers on this client (SkillFx): both arms thrown forward to hurl a spell, or
 * raised overhead to call one down, held for a moment after the cast.
 */
public final class CastPoses {
    private record Held(int pose, long until) {
    }

    private static final Map<Integer, Held> HELD = new HashMap<>();

    private CastPoses() {
    }

    public static void start(int entityId, int pose, int ticks) {
        if (Minecraft.getInstance().level == null) return;
        HELD.put(entityId, new Held(pose, Minecraft.getInstance().level.getGameTime() + ticks));
    }

    /** Runs after the player's model has its arm poses from the items in hand, before it is drawn. */
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        Held held = HELD.get(event.getEntity().getId());
        if (held == null) return;
        if (event.getEntity().level().getGameTime() >= held.until()) {
            HELD.remove(event.getEntity().getId());
            return;
        }
        HumanoidModel.ArmPose arm = held.pose() == 0 ? HumanoidModel.ArmPose.BOW_AND_ARROW : HumanoidModel.ArmPose.THROW_SPEAR;
        var model = event.getRenderer().getModel();
        model.rightArmPose = arm;
        model.leftArmPose = arm;
    }
}
