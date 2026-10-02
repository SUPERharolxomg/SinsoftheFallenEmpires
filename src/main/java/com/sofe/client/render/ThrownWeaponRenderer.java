package com.sofe.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.sofe.entity.projectile.ThrownWeapon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;

/** A thrown javelin, knife or star, drawn as its item turned along its flight, point first. */
public class ThrownWeaponRenderer extends EntityRenderer<ThrownWeapon> {
    public ThrownWeaponRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(ThrownWeapon entity, float yaw, float partialTicks, PoseStack pose, MultiBufferSource buffers, int light) {
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0f));
        pose.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot())));
        // the sprites point to their upper right: turn that corner forward
        pose.mulPose(Axis.ZP.rotationDegrees(-45.0f));
        pose.scale(1.4f, 1.4f, 1.4f);
        Minecraft.getInstance().getItemRenderer().renderStatic(entity.weapon(), ItemDisplayContext.GROUND, light, OverlayTexture.NO_OVERLAY,
                pose, buffers, entity.level(), entity.getId());
        pose.popPose();
        super.render(entity, yaw, partialTicks, pose, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(ThrownWeapon entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
