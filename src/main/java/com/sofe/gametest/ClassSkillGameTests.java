package com.sofe.gametest;

import com.mojang.authlib.GameProfile;
import com.sofe.SoFEMod;
import com.sofe.combat.CombatCapability;
import com.sofe.player.PlayerClass;
import com.sofe.player.PlayerClassCapability;
import com.sofe.skill.ClassMechanics;
import com.sofe.skill.ClassState;
import com.sofe.skill.SkillCatalog;
import com.sofe.skill.SkillRegistry;
import com.sofe.skill.SkillType;
import com.sofe.skill.data.SkillDataManager;
import com.sofe.skill.thief.ThiefSkills;
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

/** Sprint 6: the four classes' skills and mechanics (docs/Clases.md). */
@GameTestHolder(SoFEMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class ClassSkillGameTests {

    private static ServerPlayer bearer(GameTestHelper helper, String name, PlayerClass cls) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), name));
        PlayerClassCapability.get(player).orElseThrow().set(cls);
        com.sofe.combat.CombatHandler.refresh(player);
        Vec3 at = helper.absoluteVec(new Vec3(1.5, 1, 1.5));
        player.moveTo(at.x, at.y, at.z);
        return player;
    }

    @GameTest(template = "empty")
    public static void everyActiveSkillHasAnEffectAndItsNumbers(GameTestHelper helper) {
        for (var info : SkillCatalog.all()) {
            var data = SkillDataManager.forClass(info.owner());
            helper.assertTrue(data.flatMap(d -> d.skill(info.id())).isPresent(), info.id() + " has no values in its class file");
            if (info.type() != SkillType.PASSIVE) {
                helper.assertTrue(SkillRegistry.isImplemented(info.id()), info.id() + " has no effect in code");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void theShieldStanceTakesLessThanTheCharge(GameTestHelper helper) {
        ServerPlayer knight = bearer(helper, "sofe_test_stance", PlayerClass.KNIGHT);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 2, 2, 2);
        var shield = new net.minecraftforge.event.entity.living.LivingHurtEvent(knight, knight.damageSources().mobAttack(zombie), 10f);
        ClassMechanics.onHurt(shield);
        ClassState.of(knight).stance = ClassState.Stance.CHARGE;
        var charge = new net.minecraftforge.event.entity.living.LivingHurtEvent(knight, knight.damageSources().mobAttack(zombie), 10f);
        ClassMechanics.onHurt(charge);
        helper.assertTrue(shield.getAmount() < charge.getAmount(), "shield " + shield.getAmount() + " vs charge " + charge.getAmount());
        ClassState.forget(knight);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void marksGatherAndTheFinisherSpendsThem(GameTestHelper helper) {
        ServerPlayer thief = bearer(helper, "sofe_test_marks", PlayerClass.THIEF);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 2, 2, 2);
        ThiefSkills.addMarks(thief, zombie, 2);
        ThiefSkills.addMarks(thief, zombie, 9);
        helper.assertTrue(ThiefSkills.marks(thief, zombie) == ThiefSkills.MAX_MARKS, "marks did not stop at " + ThiefSkills.MAX_MARKS);
        helper.assertTrue(ThiefSkills.takeMarks(thief, zombie) == ThiefSkills.MAX_MARKS, "the finisher took the wrong count");
        helper.assertTrue(ThiefSkills.marks(thief, zombie) == 0, "marks stayed after the finisher");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void anEnemyDyingNearTheNecromancerLeavesASoul(GameTestHelper helper) {
        ServerPlayer necromancer = bearer(helper, "sofe_test_souls", PlayerClass.NECROMANCER);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 2, 2, 2);
        int before = CombatCapability.get(necromancer).orElseThrow().souls();
        ClassMechanics.onDeath(new net.minecraftforge.event.entity.living.LivingDeathEvent(zombie, necromancer.damageSources().playerAttack(necromancer)));
        helper.assertTrue(CombatCapability.get(necromancer).orElseThrow().souls() == before + 1, "no soul was bound");
        helper.succeed();
    }
}
