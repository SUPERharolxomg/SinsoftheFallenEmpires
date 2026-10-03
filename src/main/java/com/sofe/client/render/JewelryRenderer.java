package com.sofe.client.render;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.sofe.SoFEMod;
import com.sofe.gear.GearNbt;
import com.sofe.gear.Rarity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Jewelry and charms seen on the Bearer (Curios slots), in 3D like the armor:
 * <ul>
 * <li>a necklace is a chain of small links round the neck, over the shoulders and down the chest in a V, with
 * its pendant (cut from the 32x32 icon, so its pixels are fine) hanging at the bottom;</li>
 * <li>a ring is a band round the hand (the first slot the right hand, the second the left) with its stone in
 * front;</li>
 * <li>the charms of the Talisman Pouch hang from cords on the belt: two in front, two on the hips, two behind.</li>
 * </ul>
 * The colors of each chain, band and stone are read from the item's own icon, so every piece (random or unique)
 * looks like its icon. The rarity is seen too: Relics and Legacy pieces shimmer (their foil), and every piece
 * above Common gives off a spark of its rarity's color now and then. Model space has y pointing down, so every
 * flat picture is turned over before it is drawn.
 */
public class JewelryRenderer implements ICurioRenderer {
    public static final JewelryRenderer INSTANCE = new JewelryRenderer();
    private static final ResourceLocation WHITE = SoFEMod.id("textures/entity/jewelry_white.png");
    private static final int CORD = 0x5A3A22, OUTLINE = 0x140E12;
    private static final Map<Item, Look> LOOKS = new HashMap<>();
    private static final Map<String, Long> LAST = new HashMap<>();

    /** What a piece looks like, read once from its icon: its metal, a darker metal, its stone and its pendant. */
    private record Look(int metal, int dark, int gem, ResourceLocation texture, int size, int u0, int v0, int u1, int v1) {
    }

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slot, PoseStack pose,
                                                                          RenderLayerParent<T, M> parent, MultiBufferSource buffers, int light,
                                                                          float limbSwing, float limbSwingAmount, float partialTicks,
                                                                          float ageInTicks, float netHeadYaw, float headPitch) {
        if (!(parent.getModel() instanceof HumanoidModel<?> model)) return;
        LivingEntity entity = slot.entity();
        if (entity.isInvisible()) return;
        sparks(stack, slot, entity);
        Look look = look(stack.getItem(), slot.identifier().equals("ring"));
        VertexConsumer solid = buffers.getBuffer(RenderType.entityCutoutNoCull(WHITE));
        pose.pushPose();
        switch (slot.identifier()) {
            case "necklace" -> {
                ICurioRenderer.translateIfSneaking(pose, entity);
                ICurioRenderer.rotateIfSneaking(pose, entity);
                model.body.translateAndRotate(pose);
                necklace(pose, solid, buffers, look, light, !entity.getItemBySlot(EquipmentSlot.CHEST).isEmpty());
            }
            case "ring" -> {
                boolean right = slot.index() % 2 == 0;
                (right ? model.rightArm : model.leftArm).translateAndRotate(pose);
                // the hand is 4 pixels wide, 3 on a slim skin, and sits off the arm's pivot by half its width less 2 pixels
                boolean slim = entity instanceof net.minecraft.client.player.AbstractClientPlayer p && "slim".equals(p.getModelName());
                float half = (slim ? 1.5f : 2f) / 16f, center = (slim ? 0.5f : 1f) / 16f;
                ring(pose, solid, look, light, right ? -center : center, half);
            }
            default -> {
                ICurioRenderer.translateIfSneaking(pose, entity);
                ICurioRenderer.rotateIfSneaking(pose, entity);
                model.body.translateAndRotate(pose);
                charm(stack, slot.index() % 6, pose, solid, buffers, light, entity, ageInTicks, limbSwing, limbSwingAmount);
            }
        }
        pose.popPose();
    }

    // ------------------------------------------------------------------------------------------------ the pieces

    /** The chain round the neck and down the chest, and the pendant at the bottom of the V. */
    private static void necklace(PoseStack pose, VertexConsumer solid, MultiBufferSource buffers, Look look, int light, boolean armored) {
        float out = armored ? 0.07f : 0f;                // over a chestplate the chain lies on the armor
        float zf = -0.135f - out, zb = 0.135f + out, sx = 0.135f + out * 0.5f, bottom = 0.25f, l = 0.0125f;
        int n = 0;
        for (float x = -sx; x <= sx + 1e-4; x += 0.027f) {              // behind the neck
            link(pose, solid, x, -0.005f, zb, l, n++ % 2 == 0 ? look.metal : look.dark, light);
        }
        for (float z = zb; z >= zf - 1e-4; z -= 0.027f) {               // over the shoulders
            link(pose, solid, -sx, -0.005f, z, l, n % 2 == 0 ? look.metal : look.dark, light);
            link(pose, solid, sx, -0.005f, z, l, n++ % 2 == 0 ? look.metal : look.dark, light);
        }
        for (int i = 0; i <= 10; i++) {                                 // down the chest, a V
            float t = i / 10f, x = sx * (1 - t), y = -0.005f + bottom * t;
            link(pose, solid, -x, y, zf, l, n % 2 == 0 ? look.metal : look.dark, light);
            if (i < 10) link(pose, solid, x, y, zf, l, n % 2 == 0 ? look.metal : look.dark, light);
            n++;
        }
        // the pendant: the lower half of the icon, its own pixels, hanging from the bottom link
        float h = 0.17f, w = h * (look.u1 - look.u0) / (float) Math.max(1, look.v1 - look.v0);
        VertexConsumer pic = buffers.getBuffer(RenderType.entityCutoutNoCull(look.texture));
        float s = look.size;
        quad(pose, pic, -w / 2, bottom + 0.005f, w / 2, bottom + 0.005f + h, zf - 0.012f,
                look.u0 / s, look.v0 / s, look.u1 / s, look.v1 / s, light);
    }

    /** A band round the hand, a little above the knuckles, and the stone set on the back of the hand. */
    private static void ring(PoseStack pose, VertexConsumer solid, Look look, int light, float cx, float half) {
        float rx = half + 0.004f, r = 0.129f, y0 = 0.5f, y1 = 0.535f, t = 0.014f;
        box(pose, solid, cx - rx, y0, -r - t, cx + rx, y1, -r, look.metal, light);   // front
        box(pose, solid, cx - rx, y0, r, cx + rx, y1, r + t, look.dark, light);      // back
        box(pose, solid, cx - rx - t, y0, -r - t, cx - rx, y1, r + t, look.dark, light);
        box(pose, solid, cx + rx, y0, -r - t, cx + rx + t, y1, r + t, look.metal, light);
        // the setting and its stone, on the front of the hand
        box(pose, solid, cx - 0.04f, y0 - 0.012f, -r - t - 0.012f, cx + 0.04f, y1 + 0.012f, -r - t, look.dark, light);
        box(pose, solid, cx - 0.028f, y0 - 0.003f, -r - t - 0.03f, cx + 0.028f, y1 + 0.003f, -r - t - 0.012f, look.gem, light);
        box(pose, solid, cx - 0.026f, y0 - 0.001f, -r - t - 0.032f, cx - 0.008f, y0 + 0.012f, -r - t - 0.029f, lighten(look.gem, 0.75f), light);
    }

    /**
     * One charm on its cord. Slots 0 and 1 hang in front of the belt, 2 and 3 on the hips, 4 and 5 behind; they
     * sway a little, and more as the Bearer walks.
     */
    private static void charm(ItemStack stack, int i, PoseStack pose, VertexConsumer solid, MultiBufferSource buffers, int light,
                              LivingEntity entity, float age, float limbSwing, float limbSwingAmount) {
        float side = i % 2 == 0 ? -1 : 1;
        float x, z;
        float yaw;              // the way the charm faces, about y, after it is turned upright
        switch (i / 2) {
            case 0 -> { x = side * 0.13f; z = -0.15f; yaw = 0; }   // the FIXED view already turns the icon to face -z
            case 1 -> { x = side * 0.275f; z = 0; yaw = side < 0 ? -90 : 90; }
            default -> { x = side * 0.12f; z = 0.15f; yaw = 180; }
        }
        if (!entity.getItemBySlot(EquipmentSlot.LEGS).isEmpty() || !entity.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) {
            x *= i / 2 == 1 ? 1.2f : 1f;
            z *= 1.35f;
        }
        float top = 0.6f, hang = 0.69f;
        for (float y = top; y < hang - 0.02f; y += 0.018f) link(pose, solid, x, y, z, 0.007f, CORD, light);
        pose.pushPose();
        pose.translate(x, hang, z);
        float sway = (float) Math.sin(age * 0.1f + i) * 5 + (float) (limbSwingAmount * Math.cos(limbSwing * 0.6662f + i) * 22);
        pose.mulPose(Axis.ZP.rotationDegrees(180));      // model space is upside down: turn the picture upright
        pose.mulPose(Axis.YP.rotationDegrees(yaw));
        pose.mulPose(Axis.ZP.rotationDegrees(sway));      // swing from the cord, at the top of the charm
        pose.translate(0, -0.07, 0);
        pose.scale(0.15f, 0.15f, 0.15f);
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, pose, buffers,
                entity.level(), entity.getId());
        pose.popPose();
    }

    // ------------------------------------------------------------------------------------------------ geometry

    private static void link(PoseStack pose, VertexConsumer vc, float x, float y, float z, float half, int color, int light) {
        box(pose, vc, x - half, y - half, z - half, x + half, y + half, z + half, color, light);
    }

    /** An axis-aligned box of one color (the white texture tinted), lit by its normals like the rest of the model. */
    private static void box(PoseStack pose, VertexConsumer vc, float x0, float y0, float z0, float x1, float y1, float z1, int color, int light) {
        Matrix4f m = pose.last().pose();
        Matrix3f n = pose.last().normal();
        int r = color >> 16 & 255, g = color >> 8 & 255, b = color & 255;
        float[][] faces = {
                {x0, y0, z0, x1, y0, z0, x1, y1, z0, x0, y1, z0, 0, 0, -1},
                {x1, y0, z1, x0, y0, z1, x0, y1, z1, x1, y1, z1, 0, 0, 1},
                {x0, y0, z1, x0, y0, z0, x0, y1, z0, x0, y1, z1, -1, 0, 0},
                {x1, y0, z0, x1, y0, z1, x1, y1, z1, x1, y1, z0, 1, 0, 0},
                {x0, y0, z1, x1, y0, z1, x1, y0, z0, x0, y0, z0, 0, -1, 0},
                {x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1, 0, 1, 0}};
        for (float[] f : faces) {
            for (int v = 0; v < 4; v++) {
                vc.vertex(m, f[v * 3], f[v * 3 + 1], f[v * 3 + 2]).color(r, g, b, 255).uv(0.5f, 0.5f)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(n, f[12], f[13], f[14]).endVertex();
            }
        }
    }

    /** A flat picture facing -z (the front of the body), its top at y0: model space has y pointing down. */
    private static void quad(PoseStack pose, VertexConsumer vc, float x0, float y0, float x1, float y1, float z,
                             float u0, float v0, float u1, float v1, int light) {
        Matrix4f m = pose.last().pose();
        Matrix3f n = pose.last().normal();
        // seen from the front, the picture's left is the Bearer's right, the model's -x
        vc.vertex(m, x0, y0, z).color(255, 255, 255, 255).uv(u0, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(n, 0, 0, -1).endVertex();
        vc.vertex(m, x1, y0, z).color(255, 255, 255, 255).uv(u1, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(n, 0, 0, -1).endVertex();
        vc.vertex(m, x1, y1, z).color(255, 255, 255, 255).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(n, 0, 0, -1).endVertex();
        vc.vertex(m, x0, y1, z).color(255, 255, 255, 255).uv(u0, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(n, 0, 0, -1).endVertex();
    }

    // ------------------------------------------------------------------------------------------------ colors

    /**
     * Reads a piece's icon once: its metal is the commonest color of the chain (upper half) or the band (lower half), the
     * darker metal that color shaded, its stone the most vivid color of another hue, and its pendant the box of
     * the lower half that is drawn.
     */
    private static Look look(Item item, boolean ring) {
        return LOOKS.computeIfAbsent(item, it -> {
            ResourceLocation key = ForgeRegistries.ITEMS.getKey(it);
            ResourceLocation texture = new ResourceLocation(key.getNamespace(), "textures/item/" + key.getPath() + ".png");
            int fallback = 0xE8BA46;
            try (InputStream in = Minecraft.getInstance().getResourceManager().getResourceOrThrow(texture).open();
                 NativeImage img = NativeImage.read(in)) {
                int size = img.getWidth(), half = size / 2;
                Map<Integer, Integer> counts = new HashMap<>();
                int u0 = size, v0 = size, u1 = 0, v1 = 0;
                for (int y = 0; y < size; y++) {
                    for (int x = 0; x < size; x++) {
                        int abgr = img.getPixelRGBA(x, y);
                        if ((abgr >>> 24) < 128) continue;
                        int rgb = (abgr & 255) << 16 | (abgr >> 8 & 255) << 8 | (abgr >> 16 & 255);
                        if (y >= half) {
                            u0 = Math.min(u0, x);
                            u1 = Math.max(u1, x + 1);
                            v0 = Math.min(v0, y);
                            v1 = Math.max(v1, y + 1);
                        }
                        boolean metalHalf = ring ? y >= half : y < half; // a ring's band is below its stone, a chain above its pendant
                        if (metalHalf && brightness(rgb) > 22 && rgb != OUTLINE && rgb != 0xFFFFFF) counts.merge(rgb, 1, Integer::sum);
                    }
                }
                int metal = counts.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(fallback);
                float metalHue = hsb(metal)[0], metalSat = hsb(metal)[1];
                int gem = lighten(metal, 0.4f);
                float best = 0.25f;
                for (int y = 0; y < size; y++) {
                    for (int x = 0; x < size; x++) {
                        int abgr = img.getPixelRGBA(x, y);
                        if ((abgr >>> 24) < 128) continue;
                        int rgb = (abgr & 255) << 16 | (abgr >> 8 & 255) << 8 | (abgr >> 16 & 255);
                        float[] h = hsb(rgb);
                        float dh = Math.abs(h[0] - metalHue);
                        if (metalSat > 0.25f && Math.min(dh, 1 - dh) < 0.07f) continue; // a grey metal has no hue to avoid
                        float score = h[1] * h[2];
                        if (score > best) {
                            best = score;
                            gem = rgb;
                        }
                    }
                }
                if (u1 <= u0) {
                    u0 = 0; v0 = half; u1 = size; v1 = size;
                }
                return new Look(metal, darken(metal, 0.3f), gem, texture, size, u0, v0, u1, v1);
            } catch (Exception e) {
                return new Look(fallback, darken(fallback, 0.3f), 0xD0303C, texture, 16, 0, 8, 16, 16);
            }
        });
    }

    private static float[] hsb(int rgb) {
        return java.awt.Color.RGBtoHSB(rgb >> 16 & 255, rgb >> 8 & 255, rgb & 255, null);
    }

    private static int brightness(int rgb) {
        return ((rgb >> 16 & 255) + (rgb >> 8 & 255) + (rgb & 255)) / 3;
    }

    private static int lighten(int rgb, float t) {
        int r = rgb >> 16 & 255, g = rgb >> 8 & 255, b = rgb & 255;
        return (int) (r + (255 - r) * t) << 16 | (int) (g + (255 - g) * t) << 8 | (int) (b + (255 - b) * t);
    }

    private static int darken(int rgb, float t) {
        return (int) ((rgb >> 16 & 255) * (1 - t)) << 16 | (int) ((rgb >> 8 & 255) * (1 - t)) << 8 | (int) ((rgb & 255) * (1 - t));
    }

    // ------------------------------------------------------------------------------------------------ sparks

    /**
     * Once every ten ticks per piece, a small dust of the rarity's color where the piece is worn. Rendering runs
     * every frame, so the spark is keyed to the entity's tick and the slot, not to the frame.
     */
    private static void sparks(ItemStack stack, SlotContext slot, LivingEntity entity) {
        Rarity rarity = GearNbt.read(stack).map(g -> g.rarity()).orElse(Rarity.COMMON);
        if (rarity == Rarity.COMMON || rarity.beamColor() < 0) return;
        int phase = (slot.identifier().hashCode() + slot.index() * 3) & 7;
        if ((entity.tickCount + phase) % 10 != 0 || Minecraft.getInstance().isPaused()) return;
        String key = entity.getId() + slot.identifier() + slot.index();
        long tick = entity.tickCount;
        if (LAST.size() > 512) LAST.clear();
        if (Long.valueOf(tick).equals(LAST.put(key, tick))) return;
        double yaw = Math.toRadians(entity.yBodyRot);
        Vec3 forward = new Vec3(-Math.sin(yaw), 0, Math.cos(yaw)), right = new Vec3(-Math.cos(yaw), 0, -Math.sin(yaw));
        Vec3 at = switch (slot.identifier()) {
            case "necklace" -> entity.position().add(0, 1.15, 0).add(forward.scale(0.2));
            case "ring" -> entity.position().add(0, 0.75, 0).add(right.scale(slot.index() % 2 == 0 ? 0.38 : -0.38));
            default -> entity.position().add(0, 0.8, 0).add(right.scale(slot.index() % 2 == 0 ? 0.28 : -0.28));
        };
        if (entity.isCrouching()) at = at.add(0, -0.2, 0);
        int c = rarity.beamColor();
        var dust = new DustParticleOptions(new Vector3f((c >> 16 & 255) / 255f, (c >> 8 & 255) / 255f, (c & 255) / 255f), 0.6f);
        var r = entity.getRandom();
        entity.level().addParticle(dust, at.x + (r.nextDouble() - 0.5) * 0.15, at.y + (r.nextDouble() - 0.5) * 0.15,
                at.z + (r.nextDouble() - 0.5) * 0.15, 0, 0.01, 0);
    }
}
