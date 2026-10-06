package com.sofe.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sofe.SoFEMod;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The violet eyes of Act V's corrupted zombies (docs/Jugabilidad.md, "New traits for vanilla mobs"), glowing in the
 * dark, on the vanilla zombie, husk and drowned the server marked (CorruptedPacket).
 */
public class CorruptedEyesLayer<T extends LivingEntity, M extends EntityModel<T>> extends EyesLayer<T, M> {
    private static final RenderType EYES = RenderType.eyes(SoFEMod.id("textures/entity/corrupted_zombie_eyes.png"));
    private static final Set<Integer> CORRUPTED = ConcurrentHashMap.newKeySet();

    public CorruptedEyesLayer(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    public static void mark(int entity) {
        CORRUPTED.add(entity);
    }

    /** A new world or server: the old ids mean nothing. */
    public static void clear() {
        CORRUPTED.clear();
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, T entity, float limbSwing, float limbSwingAmount,
                       float partialTick, float age, float headYaw, float headPitch) {
        if (CORRUPTED.contains(entity.getId())) super.render(pose, buffers, light, entity, limbSwing, limbSwingAmount, partialTick, age, headYaw, headPitch);
    }

    @Override
    public RenderType renderType() {
        return EYES;
    }
}
