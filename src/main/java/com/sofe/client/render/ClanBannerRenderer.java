package com.sofe.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.sofe.SoFEMod;
import com.sofe.block.ClanBanner;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Draws the cloth of a great banner of the clans: three blocks wide, six long, hanging from its bar
 * and moving in the wind. The cloth is cut in strips down its length; each strip leans out a little
 * more than the one above, on a wave that runs down the cloth, so the hem moves the most.
 */
public class ClanBannerRenderer implements BlockEntityRenderer<ClanBanner.Entity> {
    private static final ResourceLocation TEXTURE = SoFEMod.id("textures/entity/clan_banner.png");
    private static final int STRIPS = 24;

    public ClanBannerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ClanBanner.Entity banner, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        var level = banner.getLevel();
        float time = (level == null ? 0 : level.getGameTime()) + partialTick;
        float phase = (banner.getBlockPos().getX() * 7 + banner.getBlockPos().getZ() * 13) % 100;
        pose.pushPose();
        pose.translate(0.5, 0.0, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(-banner.getBlockState().getValue(ClanBanner.FACING).toYRot()));
        // in the block's own frame the cloth faces +z, against the wall at -z
        pose.translate(0, 0.875, -0.40);
        Matrix4f matrix = pose.last().pose();
        Matrix3f normal = pose.last().normal();
        VertexConsumer vc = buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        float half = ClanBanner.WIDTH / 2f, length = ClanBanner.LENGTH;
        for (int i = 0; i < STRIPS; i++) {
            float v0 = i / (float) STRIPS, v1 = (i + 1) / (float) STRIPS;
            float y0 = -v0 * length, y1 = -v1 * length;
            float z0 = sway(v0, time, phase), z1 = sway(v1, time, phase);
            quad(vc, matrix, normal, -half, y0, z0, half, y1, z1, v0, v1, light);
        }
        pose.popPose();
    }

    /** How far the cloth leans out at a point down its length (0 at the bar, 1 at the hem). */
    private static float sway(float along, float time, float phase) {
        float wave = Mth.sin(time * 0.06f + phase - along * 3.5f) * 0.18f + Mth.sin(time * 0.13f + phase * 0.5f - along * 6f) * 0.05f;
        return 0.02f + along * along * wave;
    }

    private static void quad(VertexConsumer vc, Matrix4f m, Matrix3f n, float x0, float y0, float z0, float x1, float y1, float z1,
                             float v0, float v1, int light) {
        vertex(vc, m, n, x0, y0, z0, 0, v0, light);
        vertex(vc, m, n, x0, y1, z1, 0, v1, light);
        vertex(vc, m, n, x1, y1, z1, 1, v1, light);
        vertex(vc, m, n, x1, y0, z0, 1, v0, light);
    }

    private static void vertex(VertexConsumer vc, Matrix4f m, Matrix3f n, float x, float y, float z, float u, float v, int light) {
        vc.vertex(m, x, y, z).color(255, 255, 255, 255).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                .normal(n, 0, 0, 1).endVertex();
    }

    @Override
    public boolean shouldRenderOffScreen(ClanBanner.Entity banner) {
        return true; // the cloth hangs far below the block
    }

    @Override
    public int getViewDistance() {
        return 128;
    }
}
