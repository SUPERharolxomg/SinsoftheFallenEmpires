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
            helper.assertTrue(GearDataManager.bases().stream().anyMatch(b -> b.item().equals(item.getId().toString())),
                    item.getId() + " has no gear base, so it never drops");
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
}
