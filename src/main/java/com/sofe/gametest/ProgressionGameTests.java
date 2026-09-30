package com.sofe.gametest;

import com.mojang.authlib.GameProfile;
import com.sofe.SoFEMod;
import com.sofe.mob.MobLevels;
import com.sofe.mob.MobScalingRules;
import com.sofe.progression.ProgressionCapability;
import com.sofe.progression.ProgressionData;
import com.sofe.progression.ProgressionRulesManager;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(SoFEMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class ProgressionGameTests {

    /** The GameTest world is not a journey, so it behaves like free mode: vanilla mobs stay vanilla. */
    @GameTest(template = "empty")
    public static void mobsOutsideAJourneyHaveNoLevel(GameTestHelper helper) {
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new Vec3(1.5, 1, 1.5));
        helper.assertTrue(MobLevels.levelOf(zombie).isEmpty(), "a zombie outside a journey got a level");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void aLevelTenZombieIsTougher(GameTestHelper helper) {
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new Vec3(1.5, 1, 1.5));
        zombie.setNoAi(true);
        MobScalingRules rules = ProgressionRulesManager.mobScaling();
        MobLevels.apply(zombie, 10, rules);

        double expected = 20 * rules.healthMultiplier(10);
        helper.assertTrue(Math.abs(zombie.getMaxHealth() - expected) < 0.01, "expected " + expected + " max health, got " + zombie.getMaxHealth());
        helper.assertTrue(zombie.getHealth() == zombie.getMaxHealth(), "a scaled mob should spawn at full health");
        helper.assertTrue(MobLevels.levelOf(zombie).orElse(0) == 10, "the level was not stored on the mob");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void killingALevelledMobGivesExperience(GameTestHelper helper) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "sofe_test_hunter"));
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new Vec3(1.5, 1, 1.5));
        zombie.setNoAi(true);
        MobLevels.apply(zombie, 5, ProgressionRulesManager.mobScaling());

        zombie.hurt(player.damageSources().playerAttack(player), 10_000);
        ProgressionData progress = ProgressionCapability.get(player).orElseThrow();
        helper.assertTrue(progress.xp() > 0 || progress.level() > 1, "killing a level 5 zombie gave no experience");
        helper.succeed();
    }
}
