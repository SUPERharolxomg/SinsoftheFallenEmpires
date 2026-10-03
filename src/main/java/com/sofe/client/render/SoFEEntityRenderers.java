package com.sofe.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.sofe.SoFEMod;
import com.sofe.client.ClientBearers;
import com.sofe.config.SoFEConfig;
import com.sofe.entity.BearerCorpseEntity;
import com.sofe.entity.boss.BrassSentinelEntity;
import com.sofe.entity.npc.BearerNpcEntity;
import com.sofe.entity.npc.StoryNpcEntity;
import com.sofe.player.PlayerClass;
import com.sofe.registry.EntityRegistry;
import net.minecraft.client.Minecraft;
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
 * Renderers for the SoFE entities: GeckoLib models for the Void creatures and the bosses
 * (scripts/make_mob_models.py), and player models for NPCs, with vanilla default skins (read from
 * the game, not copied) for NPCs that have no skin of their own yet.
 */
public final class SoFEEntityRenderers {

    private SoFEEntityRenderers() {
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityRegistry.VOID_WRETCH.get(), ctx -> new GeoMobRenderer<>(ctx, "void_wretch", 0.5f));
        event.registerEntityRenderer(EntityRegistry.THROWN_WEAPON.get(), ThrownWeaponRenderer::new);
        event.registerEntityRenderer(EntityRegistry.SUMMONED_ALLY.get(), SummonRenderers.Ally::new);
        event.registerEntityRenderer(EntityRegistry.BRONZE_CANNON.get(), SummonRenderers.Cannon::new);
        event.registerEntityRenderer(EntityRegistry.CLAY_GOLEM.get(), SummonRenderers.Golem::new);
        event.registerEntityRenderer(EntityRegistry.EMBALMED_DEAD.get(), SummonRenderers.Embalmed::new);
        event.registerEntityRenderer(EntityRegistry.COMPANION.get(), CompanionRenderer::new);
        event.registerEntityRenderer(EntityRegistry.SPELL_BOLT.get(), ctx -> new net.minecraft.client.renderer.entity.ThrownItemRenderer<>(ctx, 1.0f, true));
        event.registerEntityRenderer(EntityRegistry.BOMB.get(), ctx -> new net.minecraft.client.renderer.entity.ThrownItemRenderer<>(ctx, 0.9f, false));
        event.registerEntityRenderer(EntityRegistry.VOID_STALKER.get(), ctx -> new GeoMobRenderer<>(ctx, "void_stalker", 0.5f));
        event.registerEntityRenderer(EntityRegistry.STORY_NPC.get(), NpcRenderer::new);
        event.registerEntityRenderer(EntityRegistry.BEARER_NPC.get(), NpcRenderer::new);
        event.registerEntityRenderer(EntityRegistry.MERCHANT.get(), NpcRenderer::new);
        event.registerEntityRenderer(EntityRegistry.CITIZEN.get(), NpcRenderer::new);
        event.registerBlockEntityRenderer(com.sofe.registry.SoFEBlocks.CLAN_BANNER_ENTITY.get(), ClanBannerRenderer::new);
        event.registerEntityRenderer(EntityRegistry.BEARER_CORPSE.get(), CorpseRenderer::new);
        event.registerEntityRenderer(EntityRegistry.BRASS_SENTINEL.get(), ctx -> new GeoMobRenderer<BrassSentinelEntity>(ctx,
                new SentinelModel(), 1.0f));
        event.registerEntityRenderer(EntityRegistry.KALETH.get(), ctx -> new GeoMobRenderer<>(ctx, "kaleth", 0.7f));
        event.registerEntityRenderer(EntityRegistry.SERATH.get(), ctx -> new GeoMobRenderer<>(ctx, "serath", 0.6f));
        event.registerEntityRenderer(EntityRegistry.VORATH.get(), ctx -> new GeoMobRenderer<>(ctx, "vorath", 1.2f));
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

    // --- GeckoLib mobs (scripts/make_mob_models.py): model, animations, texture and glow mask by name

    static class GeoMobRenderer<T extends net.minecraft.world.entity.LivingEntity & software.bernie.geckolib.core.animatable.GeoAnimatable>
            extends software.bernie.geckolib.renderer.GeoEntityRenderer<T> {
        GeoMobRenderer(EntityRendererProvider.Context ctx, String model, float shadow) {
            this(ctx, new software.bernie.geckolib.model.DefaultedEntityGeoModel<>(SoFEMod.id(model), true), shadow);
        }

        GeoMobRenderer(EntityRendererProvider.Context ctx, software.bernie.geckolib.model.GeoModel<T> model, float shadow) {
            super(ctx, model);
            this.shadowRadius = shadow;
            addRenderLayer(new software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer<>(this));
        }
    }

    /** The Sentinel's metal is corrupted by the Void below half health. */
    static class SentinelModel extends software.bernie.geckolib.model.DefaultedEntityGeoModel<BrassSentinelEntity> {
        private static final ResourceLocation VOID_TEXTURE = SoFEMod.id("textures/entity/brass_sentinel_void.png");

        SentinelModel() {
            super(SoFEMod.id("brass_sentinel"), true);
        }

        @Override
        public ResourceLocation getTextureResource(BrassSentinelEntity entity) {
            return entity.getHealth() < entity.getMaxHealth() / 2 ? VOID_TEXTURE : super.getTextureResource(entity);
        }
    }

    // --- NPCs

    /** Default skins for NPCs without their own texture yet (textures/entity/npc/&lt;id&gt;.png). */
    private static final Map<String, String> DEFAULT_SKINS = Map.ofEntries(
            Map.entry("ozhan", "noor"), Map.entry("council_elder", "ari"), Map.entry("azhar", "kai"),
            Map.entry("cassian", "steve"), Map.entry("ankhareth", "efe"), Map.entry("shirin", "alex"),
            Map.entry("rurik", "zuri"), Map.entry("ferid", "makena"), Map.entry("dilara", "sunny"),
            Map.entry("yusuf", "steve"), Map.entry("selim", "ari"),
            Map.entry("citizen_baker", "sunny"), Map.entry("citizen_water_carrier", "kai"), Map.entry("citizen_scholar", "noor"),
            Map.entry("citizen_guard", "zuri"), Map.entry("citizen_weaver", "makena"), Map.entry("citizen_pilgrim", "efe"),
            Map.entry("citizen_widow", "alex"), Map.entry("citizen_clockmaker", "ari"), Map.entry("citizen_storyteller", "steve"),
            Map.entry("citizen_child", "kai"), Map.entry("bazaar_spicer", "noor"), Map.entry("bazaar_weaver", "sunny"),
            Map.entry("bazaar_fruiterer", "efe"), Map.entry("bazaar_lampwright", "zuri"),
            Map.entry("nordrath_shieldmaiden", "alex"), Map.entry("nordrath_fisher", "steve"), Map.entry("nordrath_widow", "sunny"),
            Map.entry("nordrath_apprentice", "kai"), Map.entry("nordrath_hunter", "makena"), Map.entry("nordrath_elder", "noor"),
            Map.entry("nordrath_child", "efe"), Map.entry("nordrath_brewer", "zuri"), Map.entry("nordrath_raider", "ari"),
            Map.entry("nordrath_skald", "steve"), Map.entry("nordrath_furrier", "alex"), Map.entry("nordrath_runesmith", "zuri"),
            Map.entry("kasim", "efe"), Map.entry("nordrath_gambler", "makena"));

    /** The skin of an NPC id: its own when the mod or a resource pack has one, else a default skin. */
    public static ResourceLocation skinFor(String id) {
        ResourceLocation own = SoFEMod.id("textures/entity/npc/" + id + ".png");
        if (Minecraft.getInstance().getResourceManager().getResource(own).isPresent()) return own;
        return ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/player/wide/" + DEFAULT_SKINS.getOrDefault(id, "steve") + ".png");
    }

    /** A companion looks like its hero as an NPC in Sulthari, with what it carries in hand. */
    static class CompanionRenderer extends net.minecraft.client.renderer.entity.HumanoidMobRenderer<com.sofe.companion.CompanionEntity,
            PlayerModel<com.sofe.companion.CompanionEntity>> {
        CompanionRenderer(EntityRendererProvider.Context ctx) {
            super(ctx, new PlayerModel<>(ctx.bakeLayer(ModelLayers.PLAYER), false), 0.5f);
            addLayer(new net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer<>(this,
                    new net.minecraft.client.model.HumanoidModel<>(ctx.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                    new net.minecraft.client.model.HumanoidModel<>(ctx.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)), ctx.getModelManager()));
        }

        @Override
        public ResourceLocation getTextureLocation(com.sofe.companion.CompanionEntity companion) {
            return skinFor(companion.bearer().npcId());
        }
    }

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
}
