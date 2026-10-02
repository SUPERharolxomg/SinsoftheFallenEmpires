package com.sofe.gear.ranged;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

/**
 * What SoFE's crossbows add to the bolts they loose (damage, element, the Repeater's volley), and what the
 * element of any SoFE arrow does on a hit. Bows set their arrows themselves ({@link RangedItems.SoFEBow#customArrow}).
 */
public final class RangedHandler {
    private static final String DONE = "sofe_crossbow_done";

    private RangedHandler() {
    }

    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof AbstractArrow arrow) || !arrow.shotFromCrossbow()) return;
        if (arrow.getPersistentData().getBoolean(DONE) || !(arrow.getOwner() instanceof LivingEntity owner)) return;
        RangedItems.SoFECrossbow crossbow = held(owner);
        if (crossbow == null) return;
        arrow.getPersistentData().putBoolean(DONE, true);
        arrow.setBaseDamage(arrow.getBaseDamage() * crossbow.damageMultiplier());
        if (crossbow.spell() != Spell.NONE) arrow.getPersistentData().putString(RangedItems.SPELL_TAG, crossbow.spell().id());
        if (crossbow.spell() == Spell.FROST) arrow.setPierceLevel((byte) 2); // glacial bolts pass through
        for (int i = 1; i < crossbow.volley(); i++) { // the Repeater: two more bolts, fanned out
            Arrow extra = new Arrow(event.getLevel(), owner);
            extra.getPersistentData().putBoolean(DONE, true);
            extra.setShotFromCrossbow(true);
            extra.setBaseDamage(arrow.getBaseDamage());
            extra.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            Vec3 v = arrow.getDeltaMovement();
            double angle = Math.toRadians(i % 2 == 0 ? 8 : -8);
            extra.setDeltaMovement(v.x * Math.cos(angle) - v.z * Math.sin(angle), v.y, v.x * Math.sin(angle) + v.z * Math.cos(angle));
            extra.setPos(arrow.getX(), arrow.getY(), arrow.getZ());
            event.getLevel().addFreshEntity(extra);
        }
    }

    private static RangedItems.SoFECrossbow held(LivingEntity owner) {
        for (ItemStack stack : new ItemStack[]{owner.getMainHandItem(), owner.getOffhandItem()}) {
            if (stack.getItem() instanceof RangedItems.SoFECrossbow c) return c;
        }
        return null;
    }

    public static void onHurt(LivingHurtEvent event) {
        if (!(event.getSource().getDirectEntity() instanceof AbstractArrow arrow)) return;
        String id = arrow.getPersistentData().getString(RangedItems.SPELL_TAG);
        if (id.isEmpty()) return;
        Spell spell = Spell.byId(id);
        event.setAmount(spell.damage(event.getEntity(), event.getAmount()));
        spell.apply(event.getEntity(), arrow.getOwner(), event.getAmount());
    }
}
