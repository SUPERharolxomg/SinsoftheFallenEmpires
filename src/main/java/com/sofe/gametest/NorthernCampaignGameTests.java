package com.sofe.gametest;

import com.mojang.authlib.GameProfile;
import com.sofe.SoFEMod;
import com.sofe.entity.boss.BrassSentinelEntity;
import com.sofe.entity.boss.KalethEntity;
import com.sofe.entity.boss.SerathEntity;
import com.sofe.entity.boss.VorathEntity;
import com.sofe.item.CodexShardItem;
import com.sofe.mob.MobTraits;
import com.sofe.player.PlayerClass;
import com.sofe.player.PlayerClassCapability;
import com.sofe.quest.DialogueService;
import com.sofe.quest.QuestEngine;
import com.sofe.registry.EntityRegistry;
import com.sofe.story.SoFEAdvancements;
import com.sofe.story.Sin;
import com.sofe.story.StoryCapability;
import com.sofe.world.lock.BurningDeep;
import com.sofe.world.lock.SealedGateBlock;
import com.sofe.world.zone.StructurePositions;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

/** Sprint 5, Act II on a real server: the Broken Oaths, Vorath, the Codex Shard, the gates and the Burning Deep. */
@GameTestHolder(SoFEMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class NorthernCampaignGameTests {
    private static final String ACT2 = "sofe:act2_north";

    private static ServerPlayer bearer(GameTestHelper helper, String name) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), name));
        PlayerClassCapability.get(player).orElseThrow().set(PlayerClass.KNIGHT);
        return player;
    }

    private static Zombie dummy(GameTestHelper helper, double x) {
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new Vec3(x, 1, 3.5));
        zombie.setNoAi(true);
        return zombie;
    }

    @GameTest(template = "empty")
    public static void kalethExecutesStunnedTargets(GameTestHelper helper) {
        KalethEntity kaleth = helper.spawn(EntityRegistry.KALETH.get(), new Vec3(2.5, 1, 1.5));
        kaleth.setNoAi(true);
        Zombie normal = dummy(helper, 1.5), stunned = dummy(helper, 3.5);
        kaleth.doHurtTarget(normal);
        kaleth.stun(stunned);
        helper.assertTrue(kaleth.isStunned(stunned), "the stun did not take");
        kaleth.doHurtTarget(stunned);
        float lostNormal = normal.getMaxHealth() - normal.getHealth(), lostStunned = stunned.getMaxHealth() - stunned.getHealth();
        helper.assertTrue(lostStunned > lostNormal * 1.5f, "the execution strike should hit far harder: " + lostStunned + " vs " + lostNormal);
        helper.assertTrue(normal.isOnFire(), "Kaleth's blade should burn");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void serathHealsFromTheBloodSheSpills(GameTestHelper helper) {
        SerathEntity serath = helper.spawn(EntityRegistry.SERATH.get(), new Vec3(2.5, 1, 1.5));
        serath.setNoAi(true);
        serath.setHealth(serath.getMaxHealth() / 2);
        float before = serath.getHealth();
        serath.doHurtTarget(dummy(helper, 2.5));
        helper.assertTrue(serath.getHealth() > before, "Serath did not drink the blood she spilled");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void vorathGrowsAngrierWithEveryWound(GameTestHelper helper) {
        VorathEntity vorath = helper.spawn(EntityRegistry.VORATH.get(), new Vec3(2.5, 1, 2.5));
        vorath.setNoAi(true);
        ServerPlayer hero = bearer(helper, "sofe_test_rage");
        double baseDamage = vorath.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
        for (int i = 0; i < 4; i++) {
            vorath.invulnerableTime = 0;
            vorath.hurt(hero.damageSources().playerAttack(hero), 30);
        }
        helper.assertTrue(vorath.rageStacks() > 0, "no rage after taking 120 damage");
        helper.assertTrue(vorath.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE) > baseDamage, "rage did not raise his damage");
        DialogueService.close(hero);
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void vorathsFallGivesTheShardAndOpensActThree(GameTestHelper helper) {
        ServerPlayer hero = bearer(helper, "sofe_test_north");
        var story = StoryCapability.get(hero).orElseThrow();
        story.advanceTo(2);
        QuestEngine.startQuest(hero, ACT2);
        int steps = com.sofe.quest.StoryDataManager.quest(ACT2).orElseThrow().steps().size(); // Vorath is the last
        for (int i = 0; i < steps - 1; i++) QuestEngine.advance(hero, ACT2);
        DialogueService.close(hero);
        helper.assertTrue(story.questStep(ACT2) == steps, "the hero should be facing Vorath");

        VorathEntity vorath = helper.spawn(EntityRegistry.VORATH.get(), new Vec3(2.5, 1, 2.5));
        vorath.setNoAi(true);
        vorath.hurt(hero.damageSources().playerAttack(hero), 100_000);
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(story.hasDefeated(VorathEntity.BOSS_ID), "no credit for Vorath");
            helper.assertTrue(story.act() == 3, "Act III did not begin");
            boolean shard = false;
            for (ItemStack stack : hero.getInventory().items) {
                shard |= CodexShardItem.sin(stack).map(s -> s == Sin.WRATH).orElse(false);
            }
            helper.assertTrue(shard, "no Codex Shard of Wrath in the inventory");
            // Forge never grants advancements to fake players, so here we only check it is loaded and named right
            helper.assertTrue(hero.server.getAdvancements().getAdvancement(SoFEMod.id(SoFEAdvancements.bossPath(VorathEntity.BOSS_ID))) != null,
                    "the advancement sofe:story/boss_vorath is not loaded");
            helper.assertTrue(BurningDeep.isOpenFor(hero), "the Burning Deep should be open");
            DialogueService.close(hero);
            helper.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void theCitadelGateWaitsForBothOaths(GameTestHelper helper) {
        ServerPlayer hero = bearer(helper, "sofe_test_gate");
        StructurePositions.Gate gate = new StructurePositions.Gate("sofe:test/citadel", 0, 0, "sofe:burning_citadel", "message.sofe.gate.burning_citadel", false);
        helper.assertFalse(SealedGateBlock.canPass(hero, gate), "a new Bearer passed the Citadel gate");
        StoryCapability.get(hero).orElseThrow().defeat(KalethEntity.BOSS_ID);
        helper.assertFalse(SealedGateBlock.canPass(hero, gate), "one Oath is not enough");
        StoryCapability.get(hero).orElseThrow().defeat(SerathEntity.BOSS_ID);
        helper.assertTrue(SealedGateBlock.canPass(hero, gate), "both Oaths fell: the gate should open");
        helper.assertFalse(BurningDeep.isOpenFor(hero), "the Deep opens only with Vorath");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void aBossResetsAndForgetsItsParticipants(GameTestHelper helper) {
        BrassSentinelEntity sentinel = helper.spawn(EntityRegistry.BRASS_SENTINEL.get(), new Vec3(2.5, 1, 2.5));
        sentinel.setNoAi(true);
        ServerPlayer hero = bearer(helper, "sofe_test_reset");
        sentinel.hurt(hero.damageSources().playerAttack(hero), 40);
        helper.assertTrue(sentinel.participants().contains(hero.getUUID()), "hitting the boss should make a participant");
        sentinel.reset();
        helper.assertTrue(sentinel.getHealth() == sentinel.getMaxHealth(), "the reset did not heal the boss");
        helper.assertTrue(sentinel.participants().isEmpty() && sentinel.phase() == 1, "the reset kept the old fight");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void actTwoMobsRememberTheirAct(GameTestHelper helper) {
        Zombie zombie = dummy(helper, 2.5);
        MobTraits.apply(zombie, 2);
        helper.assertTrue(MobTraits.traitAct(zombie) == 2, "the act was not saved on the mob");
        helper.succeed();
    }
}
