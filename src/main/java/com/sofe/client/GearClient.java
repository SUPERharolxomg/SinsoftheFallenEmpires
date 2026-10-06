package com.sofe.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sofe.gear.GearData;
import com.sofe.gear.GearNbt;
import com.sofe.gear.GearStat;
import com.sofe.gear.Rarity;
import com.sofe.item.Soulbound;
import com.sofe.network.SyncProgressPacket;
import com.sofe.player.PlayerClass;
import com.sofe.progression.AttributeSheet;
import com.sofe.progression.CharacterAttribute;
import com.sofe.skill.SkillCatalog;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

import java.util.List;
import java.util.Optional;

/**
 * How gear looks on the client (docs/Pociones.md): Diablo II style tooltips (affixes in blue, unmet
 * requirements in red, the Relic's effect in gold) and a light beam over good loot on the ground.
 */
public final class GearClient {
    private static final int AFFIX_BLUE = 0x7FA8FF, RELIC_GOLD = 0xE8B64A, INACTIVE = 0x777777;
    private static final ResourceLocation BEAM = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/beacon_beam.png");
    private static final double BEAM_RANGE = 48;

    private GearClient() {
    }

    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        List<Component> lines = event.getToolTip();
        Optional<GearData> maybe = GearNbt.read(stack);
        if (maybe.isPresent()) {
            GearData gear = maybe.get();
            boolean met = meets(gear);
            int at = Math.min(1, lines.size());
            lines.add(at++, Component.translatable(gear.rarity().translationKey()).withStyle(Style.EMPTY.withColor(gear.rarity().color())));
            lines.add(at++, Component.translatable("gear.sofe.tooltip.item_level", gear.itemLevel()).withStyle(ChatFormatting.GRAY));
            for (GearData.Roll roll : gear.affixes()) {
                if (com.sofe.gear.Sockets.isGem(roll)) continue; // set gems are listed under the sockets
                lines.add(at++, affixLine(roll).withStyle(Style.EMPTY.withColor(met ? (gear.rarity() == Rarity.RELIC ? RELIC_GOLD : AFFIX_BLUE) : INACTIVE)));
            }
            int sockets = com.sofe.gear.Sockets.opened(stack);
            if (sockets > 0) {   // the sockets: each set gem with what it gives, then the empty ones
                var gems = com.sofe.gear.Sockets.gems(gear);
                for (GearData.Roll gem : gems) {
                    String id = gem.affix().substring(com.sofe.gear.Sockets.GEM_PREFIX.length());
                    int color = com.sofe.gear.SinGem.of(id).map(f -> f.gem().color()).orElse(0xFFFFFF);
                    lines.add(at++, Component.literal("◆ ").append(Component.translatable("item.sofe." + id)).append(": ").append(affixLine(gem))
                            .withStyle(Style.EMPTY.withColor(met ? color : INACTIVE)));
                }
                for (int i = gems.size(); i < sockets; i++) {
                    lines.add(at++, Component.literal("◇ ").append(Component.translatable("gear.sofe.tooltip.empty_socket")).withStyle(ChatFormatting.DARK_GRAY));
                }
            }
            if (gear.relic() != null) {
                lines.add(at++, Component.translatable("item.sofe." + gear.relic() + ".effect").withStyle(Style.EMPTY.withColor(RELIC_GOLD).withItalic(true)));
                String cls = UniqueClasses.of(gear.relic()); // the relic data lives on the server; the client has this table
                if (cls != null) {
                    boolean mine = ClientClassData.get().map(c -> c.id().equals(cls)).orElse(false);
                    lines.add(at++, Component.translatable("gear.sofe.class_only", Component.translatable("class.sofe." + cls))
                            .withStyle(mine ? ChatFormatting.GOLD : ChatFormatting.RED));
                }
            }
            int level = ClientProgressData.get().map(SyncProgressPacket::level).orElse(1);
            lines.add(at++, Component.translatable("gear.sofe.tooltip.requires_level", gear.requiredLevel())
                    .withStyle(level >= gear.requiredLevel() ? ChatFormatting.GRAY : ChatFormatting.RED));
            if (gear.requiredAttribute() != null) {
                boolean enough = attribute(gear.requiredAttribute()) >= gear.requiredValue();
                lines.add(at++, Component.translatable("gear.sofe.tooltip.requires_attribute", gear.requiredValue(),
                        Component.translatable(gear.requiredAttribute().translationKey())).withStyle(enough ? ChatFormatting.GRAY : ChatFormatting.RED));
            }
            if (!met) lines.add(at++, Component.translatable("gear.sofe.tooltip.inactive").withStyle(ChatFormatting.RED));
        }
        GearNbt.ownerName(stack).ifPresent(name -> lines.add(Component.translatable("gear.sofe.tooltip.bound", name).withStyle(ChatFormatting.LIGHT_PURPLE)));
        if (Soulbound.is(stack) && !(stack.getItem() instanceof com.sofe.item.ConsumableItems.Flask)) {
            lines.add(Component.translatable("gear.sofe.tooltip.soulbound").withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }

    /** "+5 Strength", "+1 to all Sorceress skills", "+2 to Frost Lance". */
    static MutableComponent affixLine(GearData.Roll roll) {
        if (roll.stat() == GearStat.CLASS_SKILL_RANKS) {
            return Component.translatable(roll.stat().translationKey(), roll.value(), Component.translatable("class.sofe." + roll.parameter()));
        }
        if (roll.stat() == GearStat.SKILL_RANKS) {
            Component skill = SkillCatalog.byId(roll.parameter()).map(s -> (Component) Component.translatable(s.translationKey()))
                    .orElse(Component.literal(String.valueOf(roll.parameter())));
            return Component.translatable(roll.stat().translationKey(), roll.value(), skill);
        }
        return Component.translatable(roll.stat().translationKey(), roll.value());
    }

    private static int attribute(CharacterAttribute attribute) {
        Optional<PlayerClass> playerClass = ClientClassData.get();
        return ClientProgressData.get().map(p -> playerClass.map(c -> AttributeSheet.base(attribute, c)).orElse(AttributeSheet.BASE) + p.added(attribute))
                .orElse(AttributeSheet.BASE);
    }

    private static boolean meets(GearData gear) {
        int level = ClientProgressData.get().map(SyncProgressPacket::level).orElse(1);
        return com.sofe.gear.GearBonuses.meetsRequirements(gear, level, GearClient::attribute);
    }

    /** How near a Bearer must be to read the rarity over gear on the ground (accessibility: not colour alone). */
    private static final double LABEL_RANGE = 16;

    /** The rarity's name over an item on the ground, facing the camera, in the rarity's colour. */
    private static void rarityLabel(Minecraft mc, PoseStack pose, MultiBufferSource buffers, Vec3 at, com.sofe.gear.Rarity rarity) {
        net.minecraft.network.chat.Component name = net.minecraft.network.chat.Component.translatable(rarity.translationKey());
        pose.pushPose();
        pose.translate(at.x, at.y, at.z);
        pose.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
        pose.scale(-0.025f, -0.025f, 0.025f);
        var font = mc.font;
        font.drawInBatch(name, -font.width(name) / 2f, 0, 0xFF000000 | rarity.color(), false, pose.last().pose(), buffers,
                net.minecraft.client.gui.Font.DisplayMode.NORMAL, 0x60000000, 0xF000F0);
        pose.popPose();
    }

    /** A colored beam over Tempered or better gear lying on the ground, so it is seen from afar. */
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        Vec3 camera = event.getCamera().getPosition();
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        boolean any = false;
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof ItemEntity item) || entity.distanceToSqr(mc.player) > BEAM_RANGE * BEAM_RANGE) continue;
            Optional<GearData> gear = GearNbt.read(item.getItem());
            if (gear.isEmpty() || !gear.get().rarity().hasBeam()) continue;
            int c = gear.get().rarity().beamColor();
            float[] color = {((c >> 16) & 0xFF) / 255f, ((c >> 8) & 0xFF) / 255f, (c & 0xFF) / 255f};
            Vec3 at = item.getPosition(event.getPartialTick());
            pose.pushPose();
            pose.translate(at.x - camera.x - 0.5, at.y - camera.y, at.z - camera.z - 0.5);
            BeaconRenderer.renderBeaconBeam(pose, buffers, BEAM, event.getPartialTick(), 1f, mc.level.getGameTime(), 0, 12, color, 0.08f, 0.12f);
            pose.popPose();
            if (com.sofe.config.SoFEConfig.CLIENT.rarityLabels.get() && entity.distanceToSqr(mc.player) <= LABEL_RANGE * LABEL_RANGE) {
                rarityLabel(mc, pose, buffers, at.subtract(camera).add(0, 0.85, 0), gear.get().rarity());
            }
            any = true;
        }
        if (any) buffers.endBatch();
    }
}
