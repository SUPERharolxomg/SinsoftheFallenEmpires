package com.sofe.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.sofe.SoFEMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ShieldModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Draws the empire shields with the vanilla shield's own model (a plate and a handle), each with its
 * texture laid out like the vanilla one: textures/entity/shield/&lt;id&gt;.png, 64 by 64.
 */
public class EmpireShieldRenderer extends BlockEntityWithoutLevelRenderer {
    public static final EmpireShieldRenderer INSTANCE = new EmpireShieldRenderer();
    private ShieldModel model;

    private EmpireShieldRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        model = new ShieldModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.SHIELD));
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (model == null) onResourceManagerReload(null);
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        ResourceLocation texture = SoFEMod.id("textures/entity/shield/" + (id == null ? "sulthari_shield" : id.getPath()) + ".png");
        pose.pushPose();
        pose.scale(1.0f, -1.0f, -1.0f);
        VertexConsumer vc = ItemRenderer.getFoilBufferDirect(buffers, model.renderType(texture), true, stack.hasFoil());
        model.handle().render(pose, vc, light, overlay, 1f, 1f, 1f, 1f);
        model.plate().render(pose, vc, light, overlay, 1f, 1f, 1f, 1f);
        pose.popPose();
    }
}
