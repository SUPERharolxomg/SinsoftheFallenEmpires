package com.sofe.client.render;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.sofe.SoFEMod;
import com.sofe.entity.npc.StoryNpcEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The parts of the story's people that stand out of the skin in 3D, as the armour does (scripts/make_npc_skins.py
 * paints them in textures/entity/npc/3d/&lt;id&gt;.png and lists who wears what in npc_accessories.json):
 * <ul>
 * <li>turban: a wound turban, wider and taller than the head, and its crown on top;</li>
 * <li>beard: a long beard that hangs from the chin over the chest;</li>
 * <li>fez: a tall felt cap with its tassel;</li>
 * <li>cape: a cape from the shoulders down the back;</li>
 * <li>skirt: the robe's skirt round the legs, to the ankles.</li>
 * </ul>
 * The parts follow the head and the body of the model as it moves.
 */
public class NpcAccessoryLayer<T extends net.minecraft.world.entity.LivingEntity> extends RenderLayer<T, PlayerModel<T>> {
    private static final ResourceLocation LIST = SoFEMod.id("npc_accessories.json");
    private static Map<String, Set<String>> worn;
    private final ModelPart head, turban, turbanTop, beard, fez, body, cape, skirt;
    /** The skin id of the one who wears the parts (an NPC's id, a soldier's empire and rank). */
    private final java.util.function.Function<T, String> skinOf;

    /** For the story's people. */
    public static NpcAccessoryLayer<StoryNpcEntity> forNpcs(RenderLayerParent<StoryNpcEntity, PlayerModel<StoryNpcEntity>> parent) {
        return new NpcAccessoryLayer<>(parent, npc -> SoFEEntityRenderers.skinId(npc.npcId()));
    }

    public NpcAccessoryLayer(RenderLayerParent<T, PlayerModel<T>> parent, java.util.function.Function<T, String> skinOf) {
        super(parent);
        this.skinOf = skinOf;
        ModelPart root = definition().bakeRoot();
        head = root.getChild("head");
        turban = head.getChild("turban");
        turbanTop = head.getChild("turban_top");
        beard = head.getChild("beard");
        fez = head.getChild("fez");
        body = root.getChild("body");
        cape = body.getChild("cape");
        skirt = body.getChild("skirt");
    }

    /** The boxes on a 64x64 texture (the painter in make_npc_skins.py uses the same origins). */
    static LayerDefinition definition() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition h = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        h.addOrReplaceChild("turban", CubeListBuilder.create().texOffs(0, 0).addBox(-5, -12.5f, -5, 10, 5, 10, new CubeDeformation(0.05f)), PartPose.ZERO);
        h.addOrReplaceChild("turban_top", CubeListBuilder.create().texOffs(0, 15).addBox(-4, -14.5f, -4, 8, 2, 8), PartPose.ZERO);
        h.addOrReplaceChild("beard", CubeListBuilder.create().texOffs(40, 0).addBox(-3.5f, -1, -4.6f, 7, 8, 1), PartPose.ZERO); // from the chin down the chest
        h.addOrReplaceChild("fez", CubeListBuilder.create().texOffs(40, 10).addBox(-3, -11, -3, 6, 3, 6), PartPose.ZERO);
        PartDefinition b = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        b.addOrReplaceChild("cape", CubeListBuilder.create().texOffs(0, 26).addBox(-5, 0, 2.2f, 10, 21, 1), PartPose.ZERO);
        b.addOrReplaceChild("skirt", CubeListBuilder.create().texOffs(24, 26).addBox(-4.5f, 11, -2.6f, 9, 12, 5, new CubeDeformation(0.1f)), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }

    /** Who wears what, read once from npc_accessories.json. */
    static Set<String> worn(String id) {
        if (worn == null) {
            worn = new HashMap<>();
            Minecraft.getInstance().getResourceManager().getResource(LIST).ifPresent(r -> {
                try (Reader reader = r.openAsReader()) {
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                    for (var e : json.entrySet()) {
                        Set<String> parts = new java.util.HashSet<>();
                        e.getValue().getAsJsonArray().forEach(p -> parts.add(p.getAsString()));
                        worn.put(e.getKey(), parts);
                    }
                } catch (Exception ex) {
                    SoFEMod.LOGGER.warn("Could not read {}: {}", LIST, ex.getMessage());
                }
            });
        }
        return worn.getOrDefault(id, Set.of());
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, T entity, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity.isInvisible()) return;
        String id = skinOf.apply(entity);
        Set<String> parts = worn(id);
        if (parts.isEmpty()) return;
        PlayerModel<T> model = getParentModel();
        head.copyFrom(model.head);
        body.copyFrom(model.body);
        turban.visible = parts.contains("turban");
        turbanTop.visible = parts.contains("turban");
        beard.visible = parts.contains("beard");
        fez.visible = parts.contains("fez");
        cape.visible = parts.contains("cape");
        skirt.visible = parts.contains("skirt");
        // the cape and the skirt swing a little with the walk
        cape.xRot = Math.min(0.5f, limbSwingAmount * 0.6f);
        skirt.xRot = 0;
        var vc = buffers.getBuffer(RenderType.entityCutoutNoCull(SoFEMod.id("textures/entity/npc/3d/" + id + ".png")));
        int overlay = LivingEntityRenderer.getOverlayCoords(entity, 0);
        head.render(pose, vc, light, overlay == 0 ? OverlayTexture.NO_OVERLAY : overlay);
        body.render(pose, vc, light, overlay == 0 ? OverlayTexture.NO_OVERLAY : overlay);
    }

    /** For a resource reload. */
    public static void forget() {
        worn = null;
    }
}
