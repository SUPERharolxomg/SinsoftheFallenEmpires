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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;

import java.util.List;

/**
 * Nixara, the Hollow Merchant, Broken Oath of Law VII ("You shall not rob one who trusts you"), in the Aureum
 * Market. She cheats: she slips from stall to stall, the gold she scatters is false and bursts into venom in the
 * hand, and in her second phase the trapdoors of the forum open under the Bearers over the pits.
 */
public class NixaraEntity extends BrokenOathEntity {
    public static final String BOSS_ID = "sofe:nixara";
    public static final String FALSE_GOLD = "sofe_false_gold";

    public NixaraEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 270.0).add(Attributes.ATTACK_DAMAGE, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.33).add(Attributes.ARMOR, 8.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.5)
                .add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    public String modelName() {
        return "nixara";
    }

    @Override
    public String bossId() {
        return BOSS_ID;
    }

    @Override
    public int law() {
        return 7;
    }

    @Override
    public String archsin() {
        return AvarokEntity.BOSS_ID;
    }

    @Override
    protected double arenaRadius() {
        return 16;
    }

    @Override
    protected void fightTick(ServerLevel level, List<ServerPlayer> fighters) {
        if (this.tickCount % 160 == 0) slip(level, fighters);
        if (this.tickCount % 200 == 100) falseGold(level);
        if (phase() >= 2 && this.tickCount % 140 == 70) openTheFloor(level, fighters);
    }

    /** From stall to stall: she is somewhere else. */
    private void slip(ServerLevel level, List<ServerPlayer> fighters) {
        level.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 1, getZ(), 20, 0.4, 0.6, 0.4, 0.02);
        for (int i = 0; i < 8; i++) {
            if (randomTeleport(getX() + random.nextInt(17) - 8, getY(), getZ() + random.nextInt(17) - 8, true)) break;
        }
        setTarget(fighters.get(random.nextInt(fighters.size())));
    }

    /** Gold on the floor, that is not gold. */
    private void falseGold(ServerLevel level) {
        for (int i = 0; i < 4; i++) {
            ItemEntity gold = new ItemEntity(level, getX(), getY() + 1, getZ(), new ItemStack(random.nextBoolean() ? Items.GOLD_INGOT : Items.GOLD_NUGGET));
            gold.setDeltaMovement((random.nextDouble() - 0.5) * 0.6, 0.3, (random.nextDouble() - 0.5) * 0.6);
            gold.getPersistentData().putBoolean(FALSE_GOLD, true);
            gold.lifespan = 300;
            level.addFreshEntity(gold);
        }
        level.playSound(null, blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.HOSTILE, 1.5f, 0.5f);
    }

    /** The trapdoors of the forum fall open under the Bearers for a moment. */
    private void openTheFloor(ServerLevel level, List<ServerPlayer> fighters) {
        BlockPos center = blockPosition();
        int r = (int) arenaRadius();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-r, -3, -r), center.offset(r, 1, r))) {
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof TrapDoorBlock && !state.getValue(TrapDoorBlock.OPEN)) {
                BossKit.temporary(level, pos, state.setValue(TrapDoorBlock.OPEN, true), 60, this);
            }
        }
        level.playSound(null, center, SoundEvents.WOODEN_TRAPDOOR_OPEN, SoundSource.HOSTILE, 2f, 0.6f);
        fighters.forEach(p -> p.displayClientMessage(Component.translatable("message.sofe.nixara.floor").withStyle(ChatFormatting.RED), true));
    }

    /** False gold bursts into venom in the hand that takes it. */
    public static void onPickup(EntityItemPickupEvent event) {
        ItemEntity item = event.getItem();
        if (!item.getPersistentData().getBoolean(FALSE_GOLD)) return;
        event.setCanceled(true);
        if (item.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.ITEM_SLIME, item.getX(), item.getY() + 0.3, item.getZ(), 20, 0.3, 0.3, 0.3, 0.1);
            level.playSound(null, item.blockPosition(), SoundEvents.SLIME_SQUISH, SoundSource.HOSTILE, 1.2f, 0.6f);
        }
        event.getEntity().addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1));
        event.getEntity().hurt(event.getEntity().damageSources().magic(), 3);
        event.getEntity().displayClientMessage(Component.translatable("message.sofe.nixara.false_gold").withStyle(ChatFormatting.DARK_GREEN), true);
        item.discard();
    }

    // --- signature attack (SoFEBossEntity.Signature): she tips her scales: the Bearer weighed loses a third of the life they have, and she keeps it

    private net.minecraft.world.phys.Vec3 signatureAim = net.minecraft.world.phys.Vec3.ZERO;

    @Override
    protected Signature signature() {
        return new Signature("nixara_scales", 30, 260, 14);
    }

    @Override
    protected void signatureWindup(net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.LivingEntity target, int tick,
                                   java.util.List<net.minecraft.server.level.ServerPlayer> fighters) {
        if (tick == 1) signatureAim = Signatures.toward(this, target);
        if (target != null && tick % 3 == 0) level.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD, target.getX(), target.getY() + 2.6, target.getZ(), 4, 0.3, 0.1, 0.3, 0);
    }

    @Override
    protected void signatureStrike(net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.LivingEntity target,
                                   java.util.List<net.minecraft.server.level.ServerPlayer> fighters) {
        if (target == null || !target.isAlive()) return;
        float debt = Math.max(signatureDamage(0.8), target.getHealth() * 0.3f);
        target.hurt(damageSources().indirectMagic(this, this), debt);
        heal(debt);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.WAX_ON, target.getX(), target.getY() + 1, target.getZ(), 30, 0.5, 0.8, 0.5, 0.1);
        level.playSound(null, blockPosition(), net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME, net.minecraft.sounds.SoundSource.HOSTILE, 2.0f, 0.5f);
    }
}
