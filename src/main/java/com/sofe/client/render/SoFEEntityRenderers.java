package com.sofe.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.sofe.SoFEMod;
import com.sofe.client.ClientBearers;
import com.sofe.config.SoFEConfig;
import com.sofe.entity.BearerCorpseEntity;
import com.sofe.entity.VoidCreature;
import com.sofe.entity.VoidStalker;
import com.sofe.entity.boss.BrassSentinelEntity;
import com.sofe.entity.npc.BearerNpcEntity;
import com.sofe.entity.npc.StoryNpcEntity;
import com.sofe.player.PlayerClass;
import com.sofe.registry.EntityRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.client.event.EntityRenderersEvent;

import java.util.Map;
import java.util.Optional;

/**
 * Renderers for the Sprint 4 entities. Placeholders until the Blockbench/GeckoLib models of
 * docs/Anexos.md (A2): SoFE textures for the Void creatures and the Sentinel, and vanilla default
 * skins (read from the game, not copied) for NPCs that have no skin of their own yet.
 */
public final class SoFEEntityRenderers {

    private SoFEEntityRenderers() {
    }

    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(BrassSentinelModel.LAYER, BrassSentinelModel::createLayer);
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityRegistry.VOID_WRETCH.get(), ctx -> new VoidRenderer<>(ctx, "wretch", 1.0f));
        event.registerEntityRenderer(EntityRegistry.VOID_STALKER.get(), ctx -> new VoidRenderer<VoidStalker>(ctx, "stalker", 0.85f));
        event.registerEntityRenderer(EntityRegistry.STORY_NPC.get(), NpcRenderer::new);
        event.registerEntityRenderer(EntityRegistry.BEARER_NPC.get(), NpcRenderer::new);
        event.registerEntityRenderer(EntityRegistry.MERCHANT.get(), NpcRenderer::new);
        event.registerEntityRenderer(EntityRegistry.BEARER_CORPSE.get(), CorpseRenderer::new);
        event.registerEntityRenderer(EntityRegistry.BRASS_SENTINEL.get(), SentinelRenderer::new);
    }

    /** The Bearer outfit over every player's own skin (docs/Clases.md, "How the player looks"). */
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (String skin : event.getSkins()) {
            EntityRenderer<? extends net.minecraft.world.entity.player.Player> renderer = event.getSkin(skin);
            if (renderer instanceof PlayerRenderer player) player.addLayer(new OutfitLayer(player));
        }
    }

    static ResourceLocation outfit(PlayerClass playerClass) {
        return SoFEMod.id("textures/entity/outfit/" + playerClass.id() + ".png");
    }

    // --- Void creatures

    static class VoidRenderer<T extends VoidCreature> extends HumanoidMobRenderer<T, HumanoidModel<T>> {
        private final ResourceLocation texture;
        private final float scale;

        VoidRenderer(EntityRendererProvider.Context ctx, String name, float scale) {
            super(ctx, new HumanoidModel<>(ctx.bakeLayer(ModelLayers.ZOMBIE)), 0.5f * scale);
            this.texture = SoFEMod.id("textures/entity/void/" + name + ".png");
            this.scale = scale;
        }

        @Override
        protected void scale(T entity, PoseStack pose, float partialTick) {
            pose.scale(scale, scale, scale);
        }

        @Override
        public ResourceLocation getTextureLocation(T entity) {
            return texture;
        }
    }

    // --- NPCs

    /** Default skins for NPCs without their own texture yet (textures/entity/npc/&lt;id&gt;.png). */
    private static final Map<String, String> DEFAULT_SKINS = Map.ofEntries(
            Map.entry("ozhan", "noor"), Map.entry("council_elder", "ari"), Map.entry("azhar", "kai"),
            Map.entry("cassian", "steve"), Map.entry("ankhareth", "efe"), Map.entry("shirin", "alex"),
            Map.entry("rurik", "zuri"), Map.entry("ferid", "makena"), Map.entry("dilara", "sunny"),
            Map.entry("yusuf", "steve"), Map.entry("selim", "ari"));

    static class NpcRenderer extends HumanoidMobRenderer<StoryNpcEntity, PlayerModel<StoryNpcEntity>> {
        NpcRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new PlayerModel<>(ctx.bakeLayer(ModelLayers.PLAYER), false), 0.5f);
            addLayer(new NpcOutfitLayer(this));
        }

        /** Looked up once per NPC id: its own skin when a resource pack or the mod has one, else a default skin. */
        private final Map<String, ResourceLocation> textures = new java.util.concurrent.ConcurrentHashMap<>();

        @Override
        public ResourceLocation getTextureLocation(StoryNpcEntity entity) {
            return textures.computeIfAbsent(entity.npcId(), id -> {
                ResourceLocation own = SoFEMod.id("textures/entity/npc/" + id + ".png");
                if (Minecraft.getInstance().getResourceManager().getResource(own).isPresent()) return own;
                String skin = DEFAULT_SKINS.getOrDefault(id, "steve");
                return ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/player/wide/" + skin + ".png");
            });
        }

        /** The player who is this Bearer does not see their own hero standing in the city. */
        @Override
        public boolean shouldRender(StoryNpcEntity entity, Frustum frustum, double x, double y, double z) {
            if (entity instanceof BearerNpcEntity bearer && Minecraft.getInstance().player != null
                    && bearer.bearer().equals(com.sofe.client.ClientClassData.get())) {
                return false;
            }
            return super.shouldRender(entity, frustum, x, y, z);
        }
    }

    /** Bearer NPCs wear their outfit too. */
    static class NpcOutfitLayer extends RenderLayer<StoryNpcEntity, PlayerModel<StoryNpcEntity>> {
        NpcOutfitLayer(RenderLayerParent<StoryNpcEntity, PlayerModel<StoryNpcEntity>> parent) {
            super(parent);
        }

        @Override
        public void render(PoseStack pose, MultiBufferSource buffers, int light, StoryNpcEntity entity, float limbSwing, float limbSwingAmount,
                           float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
            if (!(entity instanceof BearerNpcEntity bearer) || bearer.bearer().isEmpty()) return;
            renderColoredCutoutModel(getParentModel(), outfit(bearer.bearer().get()), pose, buffers, light, entity, 1f, 1f, 1f);
        }
    }

    static class OutfitLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
        OutfitLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
            super(parent);
        }

        @Override
        public void render(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                           float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
            if (SoFEConfig.CLIENT.hideBearerOutfit.get() || player.isInvisible()) return;
            Optional<PlayerClass> playerClass = ClientBearers.of(player);
            playerClass.ifPresent(c -> renderColoredCutoutModel(getParentModel(), outfit(c), pose, buffers, light, player, 1f, 1f, 1f));
        }
    }

    // --- The Bearer's corpse: the owner's skin lying on the ground, with a light beam

    static class CorpseRenderer extends EntityRenderer<BearerCorpseEntity> {
        private static final ResourceLocation BEAM = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/beacon_beam.png");
        private static final float[] BEAM_COLOR = {0.95f, 0.8f, 0.4f};
        private final PlayerModel<LivingEntity> wide, slim;

        CorpseRenderer(EntityRendererProvider.Context ctx) {
            super(ctx);
            this.wide = new PlayerModel<>(ctx.bakeLayer(ModelLayers.PLAYER), false);
            this.slim = new PlayerModel<>(ctx.bakeLayer(ModelLayers.PLAYER_SLIM), true);
        }

        private Optional<PlayerInfo> ownerInfo(BearerCorpseEntity corpse) {
            var connection = Minecraft.getInstance().getConnection();
            return corpse.owner().flatMap(id -> connection == null ? Optional.empty() : Optional.ofNullable(connection.getPlayerInfo(id)));
        }

        @Override
        public ResourceLocation getTextureLocation(BearerCorpseEntity corpse) {
            return ownerInfo(corpse).map(PlayerInfo::getSkinLocation)
                    .orElseGet(() -> corpse.owner().map(DefaultPlayerSkin::getDefaultSkin).orElse(DefaultPlayerSkin.getDefaultSkin()));
        }

        @Override
        public boolean shouldRender(BearerCorpseEntity corpse, Frustum frustum, double x, double y, double z) {
            return true; // the beam must be visible from afar
        }

        @Override
        public void render(BearerCorpseEntity corpse, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
            boolean isSlim = ownerInfo(corpse).map(i -> "slim".equals(i.getModelName()))
                    .orElseGet(() -> corpse.owner().map(id -> "slim".equals(DefaultPlayerSkin.getSkinModelName(id))).orElse(false));
            PlayerModel<LivingEntity> model = isSlim ? slim : wide;
            model.young = false;
            model.crouching = false;
            model.setAllVisible(true);

            pose.pushPose();
            // the standing model (as living renderers draw it) turned 90 degrees so it lies face down,
            // centered on the entity and resting on the ground
            pose.mulPose(Axis.YP.rotationDegrees(180 - corpse.getYRot()));
            pose.translate(0, 0.25, 0.9);
            pose.mulPose(Axis.XP.rotationDegrees(-90));
            pose.scale(-0.9375f, -0.9375f, 0.9375f);
            pose.translate(0, -1.501, 0);
            model.renderToBuffer(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(corpse))), light,
                    OverlayTexture.NO_OVERLAY, 0.85f, 0.85f, 0.9f, 1f);
            pose.popPose();

            if (Minecraft.getInstance().player != null && corpse.owner().map(Minecraft.getInstance().player.getUUID()::equals).orElse(false)) {
                pose.pushPose();
                pose.translate(-0.5, 0, -0.5);
                BeaconRenderer.renderBeaconBeam(pose, buffers, BEAM, partialTick, 1f, corpse.level().getGameTime(), 0, 96, BEAM_COLOR, 0.12f, 0.18f);
                pose.popPose();
            }
            super.render(corpse, yaw, partialTick, pose, buffers, light);
        }
    }

    // --- The Brass Sentinel

    static class SentinelRenderer extends MobRenderer<BrassSentinelEntity, BrassSentinelModel> {
        private static final ResourceLocation TEXTURE = SoFEMod.id("textures/entity/brass_sentinel.png");
        private static final ResourceLocation VOID_TEXTURE = SoFEMod.id("textures/entity/brass_sentinel_void.png");

        SentinelRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new BrassSentinelModel(ctx.bakeLayer(BrassSentinelModel.LAYER)), 1.0f);
        }

        @Override
        public ResourceLocation getTextureLocation(BrassSentinelEntity entity) {
            return entity.getHealth() < entity.getMaxHealth() / 2 ? VOID_TEXTURE : TEXTURE;
        }
    }
}
