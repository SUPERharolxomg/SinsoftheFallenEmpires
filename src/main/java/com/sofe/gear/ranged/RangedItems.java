package com.sofe.gear.ranged;

import com.sofe.entity.projectile.Bomb;
import com.sofe.entity.projectile.SpellBolt;
import com.sofe.entity.projectile.ThrownWeapon;
import com.sofe.gear.GearSlot;
import com.sofe.gear.SoFEGear;
import com.sofe.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * The arsenal, batch 3: ranged and arcane weapons, and the thrown ones. Every weapon here is SoFE gear (it
 * rolls affixes in the main hand); class items ({@link ClassBound}) serve only their Bearer.
 */
public final class RangedItems {
    public static final String SPELL_TAG = "sofe_spell";

    private RangedItems() {
    }

    private static Component named(ItemStack stack, Component plain) {
        return SoFEGear.name(stack, plain);
    }

    private static void spellLine(List<Component> tooltip, Spell spell) {
        if (spell != Spell.NONE) tooltip.add(Component.translatable("spell.sofe." + spell.id()).withStyle(ChatFormatting.DARK_AQUA));
    }

    // ------------------------------------------------------------------------------------------- thrown
    /** A javelin: held back like a trident and thrown; the Aetherium one flies back to its thrower. */
    public static class Javelin extends Item implements SoFEGear {
        private final float damage;
        private final Spell spell;
        private final boolean returning;

        public Javelin(float damage, Spell spell, boolean returning, Properties properties) {
            super(properties);
            this.damage = damage;
            this.spell = spell;
            this.returning = returning;
        }

        @Override
        public GearSlot gearSlot() {
            return GearSlot.WEAPON;
        }

        @Override
        public UseAnim getUseAnimation(ItemStack stack) {
            return UseAnim.SPEAR;
        }

        @Override
        public int getUseDuration(ItemStack stack) {
            return 72000;
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(player.getItemInHand(hand));
        }

        @Override
        public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
            if (!(entity instanceof Player player) || getUseDuration(stack) - timeLeft < 10) return;
            if (!level.isClientSide) {
                stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
                ThrownWeapon thrown = new ThrownWeapon(level, player, stack, damage, spell, returning);
                thrown.shootFromRotation(player, player.getXRot(), player.getYRot(), 0, 2.5f, 1.0f);
                thrown.pickup = player.getAbilities().instabuild ? AbstractArrow.Pickup.CREATIVE_ONLY : AbstractArrow.Pickup.ALLOWED;
                level.addFreshEntity(thrown);
                level.playSound(null, thrown, SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0f, 0.9f);
                if (!player.getAbilities().instabuild) player.getInventory().removeItem(stack);
            }
            player.awardStat(Stats.ITEM_USED.get(this));
        }

        @Override
        public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.translatable(returning ? "item.sofe.javelin.returning" : "item.sofe.javelin.tooltip", (int) damage)
                    .withStyle(ChatFormatting.GRAY));
            spellLine(tooltip, spell);
        }

        @Override
        public Component getName(ItemStack stack) {
            return named(stack, super.getName(stack));
        }
    }

    /** Throwing knives and stars: thrown at once with a right click, picked up where they land. */
    public static class ThrowingKnife extends Item implements SoFEGear {
        private final float damage;
        private final Spell spell;
        private final int fan;
        private final String requiredClass;

        public ThrowingKnife(float damage, Spell spell, int fan, String requiredClass, Properties properties) {
            super(properties);
            this.damage = damage;
            this.spell = spell;
            this.fan = fan;
            this.requiredClass = requiredClass;
        }

        @Override
        public GearSlot gearSlot() {
            return GearSlot.WEAPON;
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (ClassBound.refuses(player, requiredClass)) return InteractionResultHolder.fail(stack);
            if (!level.isClientSide) {
                for (int i = 0; i < fan; i++) {
                    float yaw = player.getYRot() + (i - (fan - 1) / 2f) * 9f;
                    ThrownWeapon thrown = new ThrownWeapon(level, player, stack.copyWithCount(1), damage, spell, false);
                    thrown.shootFromRotation(player, player.getXRot(), yaw, 0, 2.2f, 0.6f);
                    // one item thrown: only the middle star of a fan can be picked up again
                    thrown.pickup = player.getAbilities().instabuild || i != (fan - 1) / 2 ? AbstractArrow.Pickup.CREATIVE_ONLY : AbstractArrow.Pickup.ALLOWED;
                    level.addFreshEntity(thrown);
                }
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 0.6f, 1.6f);
            }
            player.getCooldowns().addCooldown(this, 8);
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        @Override
        public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.translatable(fan > 1 ? "item.sofe.throwing_star.tooltip" : "item.sofe.throwing_knife.tooltip", (int) damage, fan)
                    .withStyle(ChatFormatting.GRAY));
            spellLine(tooltip, spell);
            ClassBound.tooltip(tooltip, requiredClass);
        }

        @Override
        public Component getName(ItemStack stack) {
            return named(stack, super.getName(stack));
        }
    }

    // ------------------------------------------------------------------------------------------- bows and crossbows
    /** A bow with its own draw time, arrow damage and element. */
    public static class SoFEBow extends BowItem implements SoFEGear {
        private final int drawTicks;
        private final float damageMultiplier, velocity;
        private final Spell spell;

        public SoFEBow(int drawTicks, float damageMultiplier, float velocity, Spell spell, Properties properties) {
            super(properties);
            this.drawTicks = drawTicks;
            this.damageMultiplier = damageMultiplier;
            this.velocity = velocity;
            this.spell = spell;
        }

        public int drawTicks() {
            return drawTicks;
        }

        @Override
        public GearSlot gearSlot() {
            return GearSlot.WEAPON;
        }

        private float power(int used) {
            float f = used / (float) drawTicks;
            f = (f * f + f * 2.0f) / 3.0f;
            return Math.min(f, 1.0f);
        }

        @Override
        public AbstractArrow customArrow(AbstractArrow arrow) {
            arrow.setBaseDamage(arrow.getBaseDamage() * damageMultiplier);
            if (spell != Spell.NONE) arrow.getPersistentData().putString(SPELL_TAG, spell.id());
            if (spell == Spell.EMBER) arrow.setSecondsOnFire(100);
            return arrow;
        }

        /** The vanilla bow's release with this bow's draw time and velocity. */
        @Override
        public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
            if (!(entity instanceof Player player)) return;
            boolean infinite = player.getAbilities().instabuild || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0;
            ItemStack ammo = player.getProjectile(stack);
            int used = net.minecraftforge.event.ForgeEventFactory.onArrowLoose(stack, level, player, getUseDuration(stack) - timeLeft, !ammo.isEmpty() || infinite);
            if (used < 0 || ammo.isEmpty() && !infinite) return;
            if (ammo.isEmpty()) ammo = new ItemStack(Items.ARROW);
            float f = power(used);
            if (f < 0.1f) return;
            boolean free = player.getAbilities().instabuild || ammo.getItem() instanceof ArrowItem a && a.isInfinite(ammo, stack, player);
            if (!level.isClientSide) {
                ArrowItem arrowItem = (ArrowItem) (ammo.getItem() instanceof ArrowItem ? ammo.getItem() : Items.ARROW);
                AbstractArrow arrow = customArrow(arrowItem.createArrow(level, ammo, player));
                arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, f * 3.0f * velocity, 1.0f);
                if (f == 1.0f) arrow.setCritArrow(true);
                int power = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
                if (power > 0) arrow.setBaseDamage(arrow.getBaseDamage() + power * 0.5 + 0.5);
                int punch = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);
                if (punch > 0) arrow.setKnockback(punch);
                if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, stack) > 0) arrow.setSecondsOnFire(100);
                stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
                if (free || player.getAbilities().instabuild && (ammo.is(Items.SPECTRAL_ARROW) || ammo.is(Items.TIPPED_ARROW))) {
                    arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                }
                level.addFreshEntity(arrow);
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0f,
                    1.0f / (level.getRandom().nextFloat() * 0.4f + 1.2f) + f * 0.5f);
            if (!free && !player.getAbilities().instabuild) {
                ammo.shrink(1);
                if (ammo.isEmpty()) player.getInventory().removeItem(ammo);
            }
            player.awardStat(Stats.ITEM_USED.get(this));
        }

        @Override
        public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.translatable("item.sofe.bow.tooltip", Math.round(damageMultiplier * 100), String.format("%.1f", drawTicks / 20f))
                    .withStyle(ChatFormatting.GRAY));
            spellLine(tooltip, spell);
        }

        @Override
        public Component getName(ItemStack stack) {
            return named(stack, super.getName(stack));
        }
    }

    /** A crossbow whose bolts hit harder, carry an element, or come in a volley of three (the Repeater). */
    public static class SoFECrossbow extends CrossbowItem implements SoFEGear {
        private final float damageMultiplier;
        private final Spell spell;
        private final int volley;

        public SoFECrossbow(float damageMultiplier, Spell spell, int volley, Properties properties) {
            super(properties);
            this.damageMultiplier = damageMultiplier;
            this.spell = spell;
            this.volley = volley;
        }

        public float damageMultiplier() {
            return damageMultiplier;
        }

        public Spell spell() {
            return spell;
        }

        public int volley() {
            return volley;
        }

        @Override
        public GearSlot gearSlot() {
            return GearSlot.WEAPON;
        }

        @Override
        public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
            super.appendHoverText(stack, level, tooltip, flag);
            tooltip.add(Component.translatable(volley > 1 ? "item.sofe.crossbow.volley" : "item.sofe.crossbow.tooltip",
                    Math.round(damageMultiplier * 100), volley).withStyle(ChatFormatting.GRAY));
            spellLine(tooltip, spell);
        }

        @Override
        public Component getName(ItemStack stack) {
            return named(stack, super.getName(stack));
        }
    }

    // ------------------------------------------------------------------------------------------- firearms
    /** A brass firearm: each shot burns a Brass Cartridge; the blunderbuss throws a cone of shot. */
    public static class Firearm extends Item implements SoFEGear {
        private final float damage, spread;
        private final int pellets, cooldown;
        private final String requiredClass;

        public Firearm(float damage, int pellets, float spread, int cooldown, String requiredClass, Properties properties) {
            super(properties);
            this.damage = damage;
            this.pellets = pellets;
            this.spread = spread;
            this.cooldown = cooldown;
            this.requiredClass = requiredClass;
        }

        @Override
        public GearSlot gearSlot() {
            return GearSlot.WEAPON;
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (ClassBound.refuses(player, requiredClass)) return InteractionResultHolder.fail(stack);
            if (!player.getAbilities().instabuild) {
                ItemStack ammo = ItemStack.EMPTY;
                for (ItemStack s : player.getInventory().items) {
                    if (s.is(ItemRegistry.BRASS_CARTRIDGE.get())) {
                        ammo = s;
                        break;
                    }
                }
                if (ammo.isEmpty()) {
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.8f, 1.4f);
                    player.displayClientMessage(Component.translatable("item.sofe.firearm.no_ammo").withStyle(ChatFormatting.RED), true);
                    return InteractionResultHolder.fail(stack);
                }
                ammo.shrink(1);
            }
            if (level instanceof ServerLevel server) {
                for (int i = 0; i < pellets; i++) {
                    SpellBolt shot = new SpellBolt(level, player, Spell.NONE, damage, true);
                    shot.shootFromRotation(player, player.getXRot(), player.getYRot(), 0, 4.0f, spread);
                    level.addFreshEntity(shot);
                }
                Vec3 muzzle = player.getEyePosition().add(player.getLookAngle().scale(1.2));
                server.sendParticles(ParticleTypes.LARGE_SMOKE, muzzle.x, muzzle.y - 0.1, muzzle.z, 6, 0.1, 0.1, 0.1, 0.02);
                server.sendParticles(ParticleTypes.FLAME, muzzle.x, muzzle.y - 0.1, muzzle.z, 3, 0.05, 0.05, 0.05, 0.02);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.5f, 1.7f);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, 1.0f, 0.6f);
                stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
            }
            player.getCooldowns().addCooldown(this, cooldown);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        @Override
        public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.translatable(pellets > 1 ? "item.sofe.firearm.pellets" : "item.sofe.firearm.tooltip", (int) damage, pellets,
                    String.format("%.1f", cooldown / 20f)).withStyle(ChatFormatting.GRAY));
            ClassBound.tooltip(tooltip, requiredClass);
        }

        @Override
        public Component getName(ItemStack stack) {
            return named(stack, super.getName(stack));
        }
    }

    // ------------------------------------------------------------------------------------------- staves, wands and orbs
    public enum CastMode { BOLT, BEAM, LIGHTNING }

    /** A staff, wand or orb: right click casts its spell (a bolt, a draining beam or a lightning strike). */
    public static class SpellCaster extends Item implements SoFEGear {
        private final Spell spell;
        private final CastMode mode;
        private final float damage;
        private final int cooldown;
        private final String requiredClass;

        public SpellCaster(Spell spell, CastMode mode, float damage, int cooldown, String requiredClass, Properties properties) {
            super(properties);
            this.spell = spell;
            this.mode = mode;
            this.damage = damage;
            this.cooldown = cooldown;
            this.requiredClass = requiredClass;
        }

        @Override
        public GearSlot gearSlot() {
            return GearSlot.WEAPON;
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (ClassBound.refuses(player, requiredClass)) return InteractionResultHolder.fail(stack);
            if (level instanceof ServerLevel server) {
                switch (mode) {
                    case BOLT -> {
                        SpellBolt bolt = new SpellBolt(level, player, spell, damage, false);
                        bolt.shootFromRotation(player, player.getXRot(), player.getYRot(), 0, 1.8f, 0.5f);
                        level.addFreshEntity(bolt);
                        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 0.6f, 1.5f);
                    }
                    case BEAM -> beam(server, player);
                    case LIGHTNING -> lightning(server, player);
                }
                stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
            }
            player.getCooldowns().addCooldown(this, cooldown);
            player.swing(hand);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        private static EntityHitResult aim(Player player, double range) {
            Vec3 eye = player.getEyePosition();
            Vec3 end = eye.add(player.getLookAngle().scale(range));
            HitResult block = player.level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            Vec3 stop = block.getType() == HitResult.Type.MISS ? end : block.getLocation();
            AABB box = player.getBoundingBox().expandTowards(stop.subtract(eye)).inflate(1.0);
            return ProjectileUtil.getEntityHitResult(player, eye, stop, box, e -> e instanceof LivingEntity && e.isAlive() && e != player, range * range);
        }

        /** A draining beam: hurts the first creature in sight and, for Soul, heals the caster. */
        private void beam(ServerLevel level, Player player) {
            EntityHitResult hit = aim(player, 20);
            Vec3 eye = player.getEyePosition();
            Vec3 to = hit != null ? hit.getLocation() : eye.add(player.getLookAngle().scale(20));
            for (double t = 0; t < 1; t += 0.05) {
                Vec3 p = eye.add(to.subtract(eye).scale(t));
                level.sendParticles(spell.particle(), p.x, p.y - 0.2, p.z, 1, 0.02, 0.02, 0.02, 0);
            }
            if (hit != null && hit.getEntity() instanceof LivingEntity target) {
                float amount = spell.damage(target, damage);
                if (target.hurt(player.damageSources().indirectMagic(player, player), amount)) spell.apply(target, player, amount);
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_SONIC_CHARGE, SoundSource.PLAYERS, 0.4f, 1.8f);
        }

        /** A lightning strike where the caster looks (no fire is left behind), hurting everything near it. */
        private void lightning(ServerLevel level, Player player) {
            Vec3 eye = player.getEyePosition();
            Vec3 end = eye.add(player.getLookAngle().scale(32));
            EntityHitResult hit = aim(player, 32);
            Vec3 at = hit != null ? hit.getLocation() : level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getLocation();
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
            if (bolt != null) {
                bolt.moveTo(at.x, at.y, at.z);
                bolt.setVisualOnly(true);
                level.addFreshEntity(bolt);
            }
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(at, at).inflate(2.5), e -> e != player)) {
                float amount = spell.damage(e, damage);
                if (e.hurt(player.damageSources().indirectMagic(player, player), amount)) spell.apply(e, player, amount);
            }
        }

        @Override
        public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.translatable("item.sofe.caster." + mode.name().toLowerCase(java.util.Locale.ROOT), (int) damage,
                    String.format("%.1f", cooldown / 20f)).withStyle(ChatFormatting.GRAY));
            spellLine(tooltip, spell);
            ClassBound.tooltip(tooltip, requiredClass);
        }

        @Override
        public Component getName(ItemStack stack) {
            return named(stack, super.getName(stack));
        }
    }

    // ------------------------------------------------------------------------------------------- tomes and the scepter
    public enum TomeKind { EMBERS, FROST, WARDS, GALE, SOULS, DECREE }

    /** A tome read aloud: a burst round the reader. The King's scepter decrees the same way. */
    public static class Tome extends Item implements SoFEGear {
        private final TomeKind kind;
        private final float power;
        private final int cooldown;
        private final String requiredClass;

        public Tome(TomeKind kind, float power, int cooldown, String requiredClass, Properties properties) {
            super(properties);
            this.kind = kind;
            this.power = power;
            this.cooldown = cooldown;
            this.requiredClass = requiredClass;
        }

        @Override
        public GearSlot gearSlot() {
            return GearSlot.WEAPON;
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (ClassBound.refuses(player, requiredClass)) return InteractionResultHolder.fail(stack);
            if (level instanceof ServerLevel server) {
                read(server, player);
                stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
            }
            player.getCooldowns().addCooldown(this, cooldown);
            player.swing(hand);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        private void read(ServerLevel level, Player player) {
            AABB around = player.getBoundingBox().inflate(5);
            List<LivingEntity> foes = level.getEntitiesOfClass(LivingEntity.class, around, e -> e != player && e.isAlive() && e instanceof Enemy);
            switch (kind) {
                case EMBERS -> {
                    ring(level, player, ParticleTypes.FLAME);
                    for (LivingEntity e : foes) {
                        e.hurt(player.damageSources().indirectMagic(player, player), power);
                        e.setSecondsOnFire(4);
                    }
                    level.playSound(null, player.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1, 0.8f);
                }
                case FROST -> {
                    ring(level, player, ParticleTypes.SNOWFLAKE);
                    for (LivingEntity e : foes) {
                        e.hurt(player.damageSources().indirectMagic(player, player), power);
                        Spell.FROST.apply(e, player, power);
                    }
                    level.playSound(null, player.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1, 0.6f);
                }
                case WARDS -> {
                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 400, (int) power));
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 0));
                    ring(level, player, ParticleTypes.ENCHANT);
                    level.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.7f, 1.4f);
                }
                case GALE -> {
                    ring(level, player, ParticleTypes.CLOUD);
                    for (LivingEntity e : foes) {
                        Vec3 away = e.position().subtract(player.position()).normalize().scale(power);
                        e.setDeltaMovement(e.getDeltaMovement().add(away.x, 0.5, away.z));
                        e.hurtMarked = true;
                    }
                    level.playSound(null, player.blockPosition(), SoundEvents.ELYTRA_FLYING, SoundSource.PLAYERS, 0.6f, 1.6f);
                }
                case SOULS -> {
                    ring(level, player, ParticleTypes.SOUL);
                    for (LivingEntity e : foes) {
                        if (e.hurt(player.damageSources().indirectMagic(player, player), power)) player.heal(power * 0.4f);
                    }
                    level.playSound(null, player.blockPosition(), SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 1, 1);
                }
                case DECREE -> { // the King's word: everyone of the court near him fights harder
                    for (Player ally : level.getEntitiesOfClass(Player.class, player.getBoundingBox().inflate(8))) {
                        ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, (int) power));
                        ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 0));
                        ally.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 0));
                    }
                    ring(level, player, ParticleTypes.WAX_ON);
                    level.playSound(null, player.blockPosition(), SoundEvents.RAID_HORN.value(), SoundSource.PLAYERS, 0.5f, 1.3f);
                }
            }
        }

        private static void ring(ServerLevel level, Entity at, net.minecraft.core.particles.ParticleOptions particle) {
            for (int i = 0; i < 40; i++) {
                double a = i * Math.PI * 2 / 40;
                level.sendParticles(particle, at.getX() + Math.cos(a) * 3, at.getY() + 0.6, at.getZ() + Math.sin(a) * 3, 1, 0, 0.1, 0, 0.01);
            }
        }

        @Override
        public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.translatable("tome.sofe." + kind.name().toLowerCase(java.util.Locale.ROOT), (int) power,
                    String.format("%.0f", cooldown / 20f)).withStyle(ChatFormatting.GRAY));
            ClassBound.tooltip(tooltip, requiredClass);
        }

        @Override
        public Component getName(ItemStack stack) {
            return named(stack, super.getName(stack));
        }
    }

    // ------------------------------------------------------------------------------------------- gadgets
    /** A thrown gadget: bombs of clockwork, smoke and fire. */
    public static class Gadget extends Item {
        private final Bomb.Kind kind;

        public Gadget(Bomb.Kind kind, Properties properties) {
            super(properties.stacksTo(16));
            this.kind = kind;
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (!level.isClientSide) {
                Bomb bomb = new Bomb(level, player, kind, stack);
                bomb.shootFromRotation(player, player.getXRot(), player.getYRot(), -10, 1.1f, 1.0f);
                level.addFreshEntity(bomb);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.6f, 0.6f);
            }
            player.getCooldowns().addCooldown(this, 20);
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        @Override
        public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.translatable("gadget.sofe." + kind.name().toLowerCase(java.util.Locale.ROOT)).withStyle(ChatFormatting.GRAY));
        }
    }
}
