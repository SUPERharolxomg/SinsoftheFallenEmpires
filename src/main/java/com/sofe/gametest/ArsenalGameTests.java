package com.sofe.gametest;

import com.mojang.authlib.GameProfile;
import com.sofe.SoFEMod;
import com.sofe.gear.EmpireShield;
import com.sofe.gear.GearDataManager;
import com.sofe.gear.TraitWeapon;
import com.sofe.registry.ItemRegistry;
import com.sofe.registry.SoFEEffects;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.RegistryObject;

import java.util.UUID;

/** The arsenal, batch 1, on a real server: the weapons are gear, their traits work, the shields are shields. */
@GameTestHolder(SoFEMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class ArsenalGameTests {

    private static ServerPlayer wielder(GameTestHelper helper, String name, net.minecraft.world.item.Item weapon) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), name));
        Vec3 at = helper.absoluteVec(new Vec3(1.5, 1, 1.5));
        player.moveTo(at.x, at.y, at.z);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(weapon));
        return player;
    }

    @GameTest(template = "empty")
    public static void everyWeaponOfTheArsenalRollsAsGear(GameTestHelper helper) {
        for (RegistryObject<net.minecraft.world.item.Item> item : ItemRegistry.handheld()) {
            if (!(item.get() instanceof TraitWeapon)) continue;
            String id = item.getId().toString();
            // a unique (droppable Relic) drops through its Relic definition instead of a gear base
            boolean unique = GearDataManager.droppableRelics().stream().anyMatch(r -> r.item().equals(id));
            helper.assertTrue(unique || GearDataManager.bases().stream().anyMatch(b -> b.item().equals(id)),
                    id + " has no gear base nor Relic, so it never drops");
        }
        for (RegistryObject<net.minecraft.world.item.Item> shield : ItemRegistry.shields()) {
            helper.assertTrue(shield.get() instanceof EmpireShield, shield.getId() + " is not an empire shield");
            helper.assertTrue(new ItemStack(shield.get()).canPerformAction(net.minecraftforge.common.ToolActions.SHIELD_BLOCK),
                    shield.getId() + " cannot block");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void theHalberdReachesFartherThanASword(GameTestHelper helper) {
        var modifiers = new ItemStack(ItemRegistry.IMPERIAL_HALBERD.get()).getAttributeModifiers(EquipmentSlot.MAINHAND);
        helper.assertTrue(modifiers.get(ForgeMod.ENTITY_REACH.get()).stream().anyMatch(m -> m.getAmount() > 1),
                "the halberd adds no reach");
        helper.assertTrue(new ItemStack(ItemRegistry.BRASS_SCIMITAR.get()).getAttributeModifiers(EquipmentSlot.MAINHAND)
                .get(ForgeMod.ENTITY_REACH.get()).isEmpty(), "a plain sword should keep the normal reach");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60)
    public static void theWarHammerSlamsTheEnemiesAround(GameTestHelper helper) {
        ServerPlayer player = wielder(helper, "sofe_test_slam", ItemRegistry.WAR_HAMMER.get());
        Zombie target = helper.spawn(EntityType.ZOMBIE, new Vec3(2.5, 1, 1.5));
        Zombie neighbour = helper.spawn(EntityType.ZOMBIE, new Vec3(3.5, 1, 2.5));
        float before = neighbour.getHealth();
        player.attack(target);
        helper.assertTrue(neighbour.getHealth() < before, "the slam did not reach the zombie beside the target");
        target.discard();
        neighbour.discard();
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60)
    public static void theSerratedDaggerMakesTheTargetBleed(GameTestHelper helper) {
        ServerPlayer player = wielder(helper, "sofe_test_bleed", ItemRegistry.SERRATED_DAGGER.get());
        Zombie target = helper.spawn(EntityType.ZOMBIE, new Vec3(2.5, 1, 1.5));
        player.attack(target);
        helper.assertTrue(target.hasEffect(SoFEEffects.BLEEDING.get()), "the dagger's hit did not cause bleeding");
        target.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void aFullCobaltSetGivesItsBonusAndAMixedOneDoesNot(GameTestHelper helper) {
        ServerPlayer player = wielder(helper, "sofe_test_set", ItemRegistry.BRASS_SCIMITAR.get());
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ItemRegistry.COBALT_HELMET.get()));
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ItemRegistry.COBALT_CHESTPLATE.get()));
        player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ItemRegistry.COBALT_LEGGINGS.get()));
        player.setItemSlot(EquipmentSlot.FEET, new ItemStack(ItemRegistry.SOLARI_BOOTS.get()));
        player.tickCount = 20;
        com.sofe.gear.ArmorSets.onPlayerTick(new net.minecraftforge.event.TickEvent.PlayerTickEvent(net.minecraftforge.event.TickEvent.Phase.END, player));
        helper.assertFalse(player.hasEffect(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE), "a mixed set gave the cobalt bonus");
        player.setItemSlot(EquipmentSlot.FEET, new ItemStack(ItemRegistry.COBALT_BOOTS.get()));
        com.sofe.gear.ArmorSets.onPlayerTick(new net.minecraftforge.event.TickEvent.PlayerTickEvent(net.minecraftforge.event.TickEvent.Phase.END, player));
        helper.assertTrue(player.hasEffect(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE), "the full cobalt set gave no resistance");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void aFullScaleSetReflectsMeleeDamage(GameTestHelper helper) {
        ServerPlayer player = wielder(helper, "sofe_test_scale", ItemRegistry.BRASS_SCIMITAR.get());
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ItemRegistry.SCALE_HELMET.get()));
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ItemRegistry.SCALE_CHESTPLATE.get()));
        player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ItemRegistry.SCALE_LEGGINGS.get()));
        player.setItemSlot(EquipmentSlot.FEET, new ItemStack(ItemRegistry.SCALE_BOOTS.get()));
        net.minecraft.world.entity.monster.Zombie zombie = helper.spawnWithNoFreeWill(net.minecraft.world.entity.EntityType.ZOMBIE, 2, 2, 2);
        float before = zombie.getHealth();
        com.sofe.gear.ArmorSets.onHurt(new net.minecraftforge.event.entity.living.LivingHurtEvent(player, player.damageSources().mobAttack(zombie), 8f));
        helper.assertTrue(zombie.getHealth() < before, "the Order of the Scale set reflected nothing");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void aVeilHoldsGearOfItsSlotOrAHandfulOfAsh(GameTestHelper helper) {
        ServerPlayer player = wielder(helper, "sofe_test_gamble", ItemRegistry.BRASS_SCIMITAR.get());
        for (int i = 0; i < 40; i++) {
            ItemStack revealed = com.sofe.gear.VeiledItem.unveil(player, com.sofe.gear.GearSlot.JEWELRY);
            boolean ash = revealed.is(ItemRegistry.VOID_ASH.get());
            boolean jewel = revealed.getItem() instanceof com.sofe.gear.SoFEGear gear && gear.gearSlot() == com.sofe.gear.GearSlot.JEWELRY;
            helper.assertTrue(ash || jewel, "a jewelry veil held " + revealed);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void theCaravanMastersTreadsQuickenTheirWearer(GameTestHelper helper) {
        ServerPlayer player = wielder(helper, "sofe_test_treads", ItemRegistry.BRASS_SCIMITAR.get());
        ItemStack treads = com.sofe.gear.GearMaker.relic("caravan_treads", null).orElseThrow();
        player.setItemSlot(EquipmentSlot.FEET, treads);
        com.sofe.gear.PlayerGear.invalidate(player);
        player.tickCount = 20;
        com.sofe.gear.RelicEffects.onPlayerTick(new net.minecraftforge.event.TickEvent.PlayerTickEvent(net.minecraftforge.event.TickEvent.Phase.END, player));
        helper.assertTrue(player.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED), "the Treads gave no speed");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void classItemsServeOnlyTheirBearer(GameTestHelper helper) {
        ServerPlayer player = wielder(helper, "sofe_test_class_item", ItemRegistry.EMBER_ORB.get());
        helper.assertFalse(com.sofe.gear.ranged.ClassBound.allows(player, "sorceress"), "a player without a class used a Sorceress orb");
        helper.assertTrue(com.sofe.gear.ranged.ClassBound.allows(player, null), "an item of no class was refused");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void frostChillsAndSoulHeals(GameTestHelper helper) {
        ServerPlayer player = wielder(helper, "sofe_test_spell", ItemRegistry.FROST_STAFF.get());
        net.minecraft.world.entity.monster.Zombie zombie = helper.spawnWithNoFreeWill(net.minecraft.world.entity.EntityType.ZOMBIE, 2, 2, 2);
        com.sofe.gear.ranged.Spell.FROST.apply(zombie, player, 5);
        helper.assertTrue(zombie.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN), "frost did not slow");
        player.setHealth(10);
        com.sofe.gear.ranged.Spell.SOUL.apply(zombie, player, 10);
        helper.assertTrue(player.getHealth() > 10, "the soul element did not heal");
        helper.assertTrue(com.sofe.gear.ranged.Spell.HOLY.damage(zombie, 10) > 10, "holy is not stronger against the undead");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void theMiningHammerBreaksAThreeByThreeFace(GameTestHelper helper) {
        ServerPlayer player = wielder(helper, "sofe_test_hammer", ItemRegistry.GLACIAL_MINING_HAMMER.get());
        net.minecraft.core.BlockPos center = helper.absolutePos(new net.minecraft.core.BlockPos(2, 1, 2));
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
            helper.getLevel().setBlockAndUpdate(center.offset(dx, 0, dz), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
        }
        player.moveTo(center.getX() + 0.5, center.getY() + 1, center.getZ() + 0.5, 0, 90); // standing on it, looking down
        player.gameMode.destroyBlock(center);
        int left = 0;
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
            if (!helper.getLevel().getBlockState(center.offset(dx, 0, dz)).isAir()) left++;
        }
        helper.assertTrue(left == 0, left + " of the 9 stones are still there");
        helper.succeed();
    }
}
