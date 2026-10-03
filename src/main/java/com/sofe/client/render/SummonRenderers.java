package com.sofe.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.sofe.SoFEMod;
import com.sofe.entity.summon.BronzeCannon;
import com.sofe.entity.summon.SummonedAlly;
import com.sofe.registry.ItemRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** How the Bearers' summons are drawn: the allies as figures with their own skins, the cannon as its model. */
public final class SummonRenderers {
    private SummonRenderers() {
    }

    /**
     * The Janissary Guard, the ice clone and the risen, as figures. The Janissaries wear their own skin
     * (textures/entity/summon/janissary.png); the ice clone wears its caster's own skin, tinted to ice and
     * half seen through; the risen are pale, see-through souls.
     */
    public static class Ally extends HumanoidMobRenderer<SummonedAlly, Ally.TintedModel> {
        public Ally(EntityRendererProvider.Context ctx) {
            super(ctx, new TintedModel(ctx.bakeLayer(ModelLayers.PLAYER)), 0.5f);
        }

        @Override
        public ResourceLocation getTextureLocation(SummonedAlly ally) {
            if (ally.kind() == SummonedAlly.Kind.ICE_CLONE && ally.syncedOwner().isPresent() && Minecraft.getInstance().level != null
                    && Minecraft.getInstance().level.getPlayerByUUID(ally.syncedOwner().get()) instanceof net.minecraft.client.player.AbstractClientPlayer caster) {
                return caster.getSkinTextureLocation();
            }
            return SoFEMod.id("textures/entity/summon/" + ally.kind().id() + ".png");
        }

        @Override
        public void render(SummonedAlly ally, float yaw, float partialTicks, PoseStack pose, MultiBufferSource buffers, int light) {
            switch (ally.kind()) {
                case ICE_CLONE -> model.tint(0.62f, 0.86f, 1.0f, 0.62f);
                case RISEN -> model.tint(0.55f, 1.0f, 0.92f, 0.55f);
                default -> model.tint(1, 1, 1, 1);
            }
            super.render(ally, yaw, partialTicks, pose, buffers, light);
        }

        @Override
        protected net.minecraft.client.renderer.RenderType getRenderType(SummonedAlly ally, boolean bodyVisible, boolean translucent, boolean glowing) {
            if (ally.kind() == SummonedAlly.Kind.ICE_CLONE || ally.kind() == SummonedAlly.Kind.RISEN) {
                return net.minecraft.client.renderer.RenderType.entityTranslucent(getTextureLocation(ally));
            }
            return super.getRenderType(ally, bodyVisible, translucent, glowing);
        }

        @Override
        protected void scale(SummonedAlly ally, PoseStack pose, float partialTicks) {
            float s = ally.kind().scale;
            pose.scale(s, s, s);
        }

        /** The player model, drawn in a color and see-through when asked. */
        public static class TintedModel extends PlayerModel<SummonedAlly> {
            private float r = 1, g = 1, b = 1, a = 1;

            TintedModel(net.minecraft.client.model.geom.ModelPart root) {
                super(root, false);
            }

            void tint(float red, float green, float blue, float alpha) {
                r = red;
                g = green;
                b = blue;
                a = alpha;
            }

            @Override
            public void renderToBuffer(PoseStack pose, com.mojang.blaze3d.vertex.VertexConsumer buffer, int light, int overlay,
                                       float red, float green, float blue, float alpha) {
                super.renderToBuffer(pose, buffer, light, overlay, red * r, green * g, blue * b, alpha * a);
            }
        }
    }

    /** The Clay Warden: the vanilla iron golem's model with a body of baked clay, smaller (textures/entity/summon/clay_golem.png). */
    public static class Golem extends net.minecraft.client.renderer.entity.IronGolemRenderer {
        private static final ResourceLocation TEXTURE = SoFEMod.id("textures/entity/summon/clay_golem.png");

        public Golem(EntityRendererProvider.Context ctx) {
            super(ctx);
            this.shadowRadius = 0.55f;
        }

        @Override
        public ResourceLocation getTextureLocation(net.minecraft.world.entity.animal.IronGolem golem) {
            return TEXTURE;
        }

        @Override
        protected void scale(net.minecraft.world.entity.animal.IronGolem golem, PoseStack pose, float partialTicks) {
            pose.scale(0.78f, 0.78f, 0.78f);
        }
    }

    /** The Embalmed Dead: the vanilla husk's model, wrapped in linen (textures/entity/summon/embalmed_dead.png). */
    public static class Embalmed extends net.minecraft.client.renderer.entity.HuskRenderer {
        private static final ResourceLocation TEXTURE = SoFEMod.id("textures/entity/summon/embalmed_dead.png");

        public Embalmed(EntityRendererProvider.Context ctx) {
            super(ctx);
        }

        @Override
        public ResourceLocation getTextureLocation(net.minecraft.world.entity.monster.Zombie dead) {
            return TEXTURE;
        }
    }

    /** The Bronze Cannon, drawn with the block-shaped model of its item (models/item/bronze_cannon_model.json). */
    public static class Cannon extends EntityRenderer<BronzeCannon> {
        public Cannon(EntityRendererProvider.Context ctx) {
            super(ctx);
            this.shadowRadius = 0.6f;
        }

        @Override
        public void render(BronzeCannon cannon, float yaw, float partialTicks, PoseStack pose, MultiBufferSource buffers, int light) {
            pose.pushPose();
            pose.translate(0, 0.5, 0);
            pose.mulPose(Axis.YP.rotationDegrees(180 - cannon.getYRot()));
            pose.scale(2.0f, 2.0f, 2.0f);
            Minecraft.getInstance().getItemRenderer().renderStatic(new ItemStack(ItemRegistry.BRONZE_CANNON_MODEL.get()), ItemDisplayContext.FIXED,
                    light, OverlayTexture.NO_OVERLAY, pose, buffers, cannon.level(), cannon.getId());
            pose.popPose();
            super.render(cannon, yaw, partialTicks, pose, buffers, light);
        }

        @Override
        public ResourceLocation getTextureLocation(BronzeCannon cannon) {
            return TextureAtlas.LOCATION_BLOCKS;
        }
    }
}
