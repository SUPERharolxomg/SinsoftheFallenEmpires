package com.sofe.entity.boss;

import com.sofe.gear.ranged.Spell;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

/**
 * Solrath, the False Prophet, Broken Oath of Law X ("No one shall stand above the Law"), in the Temple of Sulthari:
 * Grand Vizier Ozhan, transformed. Holy damage: pillars of light fall where the Bearers stand (marked a moment
 * before), and bolts of holy fire. In his second phase he raises the Broken Oaths the Bearers have beaten, as echoes
 * of themselves, two at a time: the one who stood above the Law commands the ones who broke it.
 */
public class SolrathEntity extends BrokenOathEntity {
    public static final String BOSS_ID = "sofe:solrath";
    public static final List<String> OATHS = List.of("sofe:kaleth", "sofe:serath", "sofe:mirael", "sofe:thessyn", "sofe:dormiel",
            "sofe:goldarc", "sofe:nixara", "sofe:fenrath", "sofe:shadeyn");
    private final List<BlockPos> marked = new ArrayList<>();
    private int pillarTicks;

    public SolrathEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 340.0).add(Attributes.ATTACK_DAMAGE, 13.0)
                .add(Attributes.MOVEMENT_SPEED, 0.27).add(Attributes.ARMOR, 12.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.9)
                .add(Attributes.FOLLOW_RANGE, 48.0);
    }

    @Override
    public String modelName() {
        return "solrath";
    }

    @Override
    public String bossId() {
        return BOSS_ID;
    }

    @Override
    public int law() {
        return 10;
    }

    @Override
    public String archsin() {
        return PrythonEntity.BOSS_ID;
    }

    @Override
    protected String introDialogue() {
        return "sofe:act5/ozhan_unmasked";
    }

    @Override
    protected double arenaRadius() {
        return 20;
    }

    @Override
    protected void fightTick(ServerLevel level, List<ServerPlayer> fighters) {
        if (pillarTicks > 0 && --pillarTicks == 0) strike(level, fighters);
        if (this.tickCount % (phase() >= 2 ? 100 : 140) == 0 && pillarTicks == 0) mark(level, fighters);
        if (this.tickCount % (phase() >= 2 ? 50 : 70) == 25) {
            for (ServerPlayer p : fighters) BossKit.bolt(level, this, p, Spell.HOLY, phase() >= 2 ? 8 : 6, 1.3f, 3);
        }
        if (phase() >= 2 && this.tickCount % 600 == 300) raiseOaths(level, fighters, 1);
    }

    /** Light gathers on the ground where each Bearer stands. */
    private void mark(ServerLevel level, List<ServerPlayer> fighters) {
        marked.clear();
        for (ServerPlayer p : fighters) marked.add(p.blockPosition());
        for (BlockPos pos : marked) level.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 0.2, pos.getZ() + 0.5, 30, 1, 0.1, 1, 0.01);
        level.playSound(null, blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 1.5f, 1.5f);
        pillarTicks = 30;
    }

    /** The pillars fall: holy fire on whoever did not leave the light. */
    private void strike(ServerLevel level, List<ServerPlayer> fighters) {
        for (BlockPos pos : marked) {
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
            if (bolt != null) {
                bolt.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                bolt.setVisualOnly(true);
                level.addFreshEntity(bolt);
            }
            for (ServerPlayer p : fighters) {
                if (p.blockPosition().distSqr(pos) <= 4) p.hurt(damageSources().magic(), phase() >= 2 ? 14 : 10);
            }
        }
        marked.clear();
    }

    /** The Broken Oaths the Bearers have beaten rise again for him, as echoes. */
    private void raiseOaths(ServerLevel level, List<ServerPlayer> fighters, int count) {
        long echoes = level.getEntitiesOfClass(SoFEBossEntity.class, getBoundingBox().inflate(40), b -> b.isEcho() && b.isAlive()).size();
        for (int i = 0; i < count && echoes < 2; i++) {
            String id = OATHS.get(random.nextInt(OATHS.size()));
            EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.tryParse(id));
            if (type == null) continue;
            double a = random.nextDouble() * Math.PI * 2;
            BlockPos at = BlockPos.containing(getX() + Math.cos(a) * 6, getY(), getZ() + Math.sin(a) * 6);
            if (type.spawn(level, at, MobSpawnType.MOB_SUMMONED) instanceof SoFEBossEntity oath) {
                oath.makeEcho();
                oath.getPersistentData().putString(BossKit.MINION_OF, getStringUUID());
                if (!fighters.isEmpty()) oath.setTarget(fighters.get(random.nextInt(fighters.size())));
                echoes++;
                fighters.forEach(p -> p.displayClientMessage(Component.translatable("message.sofe.solrath.raises", oath.getDisplayName())
                        .withStyle(ChatFormatting.GOLD), true));
            }
        }
        level.playSound(null, blockPosition(), SoundEvents.EVOKER_PREPARE_SUMMON, SoundSource.HOSTILE, 2f, 0.7f);
    }

    @Override
    protected void onPhase(int newPhase, ServerLevel level, List<ServerPlayer> fighters) {
        raiseOaths(level, fighters, 2);
    }

    // --- signature attack (SoFEBossEntity.Signature): he levels his sun staff: a spear of holy light runs straight through the temple

    private net.minecraft.world.phys.Vec3 signatureAim = net.minecraft.world.phys.Vec3.ZERO;

    @Override
    protected Signature signature() {
        return new Signature("solrath_sunspear", 35, 240, 16);
    }

    @Override
    protected void signatureWindup(net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.LivingEntity target, int tick,
                                   java.util.List<net.minecraft.server.level.ServerPlayer> fighters) {
        if (tick == 1) signatureAim = Signatures.toward(this, target);
        if (tick % 3 == 0) Signatures.drawLine(level, position(), signatureAim, 16, net.minecraft.core.particles.ParticleTypes.END_ROD);
    }

    @Override
    protected void signatureStrike(net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.LivingEntity target,
                                   java.util.List<net.minecraft.server.level.ServerPlayer> fighters) {
        for (var p : Signatures.line(position(), signatureAim, 16, 2.2, fighters)) {
            p.hurt(damageSources().indirectMagic(this, this), signatureDamage(2.0));
            p.setSecondsOnFire(3);
        }
        for (double t = 0; t <= 16; t += 0.5)
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.FLASH, getX() + signatureAim.x * t, getY() + 1.2, getZ() + signatureAim.z * t, 1, 0, 0, 0, 0);
        level.playSound(null, blockPosition(), net.minecraft.sounds.SoundEvents.BEACON_POWER_SELECT, net.minecraft.sounds.SoundSource.HOSTILE, 2.0f, 1.2f);
    }
}
