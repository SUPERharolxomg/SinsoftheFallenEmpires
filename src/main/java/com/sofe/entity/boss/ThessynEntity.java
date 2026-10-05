package com.sofe.entity.boss;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Thessyn, the Silk Weaver, Broken Oath of Law IV ("You shall speak the truth before the throne"), on the Silk
 * Road. She lies with silk: webs fall under the Bearers' feet, her bite poisons, and in her second phase chests
 * appear in the court that are no chests at all: open one and spiders and venom come out of it.
 */
public class ThessynEntity extends BrokenOathEntity {
    public static final String BOSS_ID = "sofe:thessyn";
    /** Every false chest standing now, and the Weaver who set it. */
    private static final Map<BlockPos, ThessynEntity> FALSE_CHESTS = new HashMap<>();

    public ThessynEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 250.0).add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.MOVEMENT_SPEED, 0.30).add(Attributes.ARMOR, 8.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.6)
                .add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    public String modelName() {
        return "thessyn";
    }

    @Override
    public String bossId() {
        return BOSS_ID;
    }

    @Override
    public int law() {
        return 4;
    }

    @Override
    public String archsin() {
        return LuxaraEntity.BOSS_ID;
    }

    @Override
    protected double arenaRadius() {
        return 20;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity living) living.addEffect(new MobEffectInstance(MobEffects.POISON, 80, phase() >= 2 ? 1 : 0), this);
        return hit;
    }

    @Override
    protected void fightTick(ServerLevel level, List<ServerPlayer> fighters) {
        int webs = phase() >= 2 ? 100 : 160;
        if (this.tickCount % webs == 0) {
            for (ServerPlayer p : fighters) web(level, p);
            level.playSound(null, blockPosition(), SoundEvents.SPIDER_AMBIENT, SoundSource.HOSTILE, 1.5f, 0.6f);
        }
        if (phase() >= 2 && this.tickCount % 400 == 200) falseChests(level, fighters);
    }

    /** A web cast at a Bearer's feet, gone after five seconds. */
    private void web(ServerLevel level, ServerPlayer target) {
        BlockPos at = target.blockPosition();
        if (level.getBlockState(at).isAir()) BossKit.temporary(level, at, Blocks.COBWEB.defaultBlockState(), 100, this);
        level.sendParticles(ParticleTypes.ITEM_SNOWBALL, target.getX(), target.getY() + 1, target.getZ(), 12, 0.4, 0.4, 0.4, 0.1);
    }

    /** Three chests in the court, none of them true. */
    private void falseChests(ServerLevel level, List<ServerPlayer> fighters) {
        for (int i = 0; i < 3; i++) {
            BlockPos at = BlockPos.containing(getX() + random.nextInt(13) - 6, getY(), getZ() + random.nextInt(13) - 6);
            if (!level.getBlockState(at).isAir() || !level.getBlockState(at.below()).isSolid()) continue;
            BossKit.temporary(level, at, Blocks.CHEST.defaultBlockState(), 400, this);
            FALSE_CHESTS.put(at.immutable(), this);
        }
        fighters.forEach(p -> p.displayClientMessage(Component.translatable("message.sofe.thessyn.chests").withStyle(ChatFormatting.GOLD), true));
    }

    /** Opening a false chest: spiders and venom. */
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ThessynEntity weaver = FALSE_CHESTS.remove(event.getPos());
        if (weaver == null || !(event.getLevel() instanceof ServerLevel level)) return;
        event.setCanceled(true);
        level.setBlock(event.getPos(), Blocks.AIR.defaultBlockState(), 3);
        event.getEntity().addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1), weaver);
        if (weaver.isAlive()) {
            BossKit.minions(level, weaver, EntityType.CAVE_SPIDER, 3, 8, 1, List.of());
        }
        level.playSound(null, event.getPos(), SoundEvents.SPIDER_HURT, SoundSource.HOSTILE, 1.2f, 0.5f);
        event.getEntity().displayClientMessage(Component.translatable("message.sofe.thessyn.lie").withStyle(ChatFormatting.DARK_GREEN), true);
    }

    @Override
    protected void onFightOver(ServerLevel level, boolean defeated) {
        FALSE_CHESTS.values().removeIf(w -> w == this);
    }

    // --- signature attack (SoFEBossEntity.Signature): she rears up and drives her eight legs down round her, pinning whoever is caught

    private net.minecraft.world.phys.Vec3 signatureAim = net.minecraft.world.phys.Vec3.ZERO;

    @Override
    protected Signature signature() {
        return new Signature("thessyn_impale", 25, 220, 6);
    }

    @Override
    protected void signatureWindup(net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.LivingEntity target, int tick,
                                   java.util.List<net.minecraft.server.level.ServerPlayer> fighters) {
        if (tick == 1) signatureAim = Signatures.toward(this, target);
        if (tick % 4 == 0) Signatures.drawRing(level, position(), 5.5, net.minecraft.core.particles.ParticleTypes.CRIT);
    }

    @Override
    protected void signatureStrike(net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.LivingEntity target,
                                   java.util.List<net.minecraft.server.level.ServerPlayer> fighters) {
        for (var p : Signatures.ring(position(), 5.5, fighters)) {
            Signatures.strike(this, p, signatureDamage(2.0), position(), 0, 0);
            p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 60, 5), this);
            p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.POISON, 80, 1), this);
        }
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.CRIT, getX(), getY() + 0.3, getZ(), 60, 3, 0.2, 3, 0.3);
        level.playSound(null, blockPosition(), net.minecraft.sounds.SoundEvents.SPIDER_AMBIENT, net.minecraft.sounds.SoundSource.HOSTILE, 1.8f, 0.5f);
    }
}
