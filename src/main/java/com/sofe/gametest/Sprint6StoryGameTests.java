package com.sofe.gametest;

import com.mojang.authlib.GameProfile;
import com.sofe.SoFEMod;
import com.sofe.companion.Companions;
import com.sofe.player.PlayerClass;
import com.sofe.player.PlayerClassCapability;
import com.sofe.progression.ProgressionCapability;
import com.sofe.quest.QuestEffect;
import com.sofe.quest.QuestEngine;
import com.sofe.quest.QuestEvent;
import com.sofe.world.region.Region;
import com.sofe.story.StoryCapability;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

/** Sprint 6, the story side: companions, the Bearers' own quests and Nordrath's fate. */
@GameTestHolder(SoFEMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class Sprint6StoryGameTests {

    private static ServerPlayer bearer(GameTestHelper helper, PlayerClass cls) {
        return bearer(helper, cls, "sofe_test_story");
    }

    private static ServerPlayer bearer(GameTestHelper helper, PlayerClass cls, String name) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), name));
        Vec3 at = helper.absoluteVec(new Vec3(2.5, 1, 2.5));
        player.moveTo(at.x, at.y, at.z, 0, 0);
        PlayerClassCapability.get(player).orElseThrow().set(cls);
        ProgressionCapability.get(player).orElseThrow().load(20, 0, 0, 0, true);
        player.getPersistentData().remove(Companions.TAG);
        return player;
    }

    @GameTest(template = "empty")
    public static void aKnightHiresTheSorceressButNotHimself(GameTestHelper helper) {
        ServerPlayer knight = bearer(helper, PlayerClass.KNIGHT);
        helper.assertFalse(Companions.hire(knight, PlayerClass.KNIGHT), "a Knight hired himself");
        helper.assertTrue(Companions.hire(knight, PlayerClass.SORCERESS), "the Sorceress did not come");
        helper.assertTrue(Companions.entity(knight).isPresent(), "no companion stands beside the Knight");
        helper.assertTrue(Companions.entity(knight).get().getMaxHealth() > 40, "the companion is not fitted to a level-20 Bearer");
        helper.assertFalse(Companions.hire(knight, PlayerClass.THIEF), "a second companion was hired");
        Companions.order(knight, "dismiss");
        helper.assertTrue(Companions.hired(knight).isEmpty(), "dismissed, she is still hired");
        helper.assertTrue(Companions.entity(knight).isEmpty(), "dismissed, she is still there");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void cassiansFirstQuestRunsToItsReward(GameTestHelper helper) {
        ServerPlayer knight = bearer(helper, PlayerClass.KNIGHT);
        String quest = "sofe:bearer/cassian_act1";
        QuestEngine.run(knight, List.of(new QuestEffect.StartQuest(quest)));
        var story = StoryCapability.get(knight).orElseThrow();
        helper.assertTrue(story.questStep(quest) == 1, "the quest did not start");
        for (int i = 0; i < 6; i++) QuestEngine.event(knight, new QuestEvent.Killed("sofe:void_wretch"));
        helper.assertTrue(story.questStep(quest) == 2, "six Void creatures did not finish the first step");
        QuestEngine.run(knight, List.of(new QuestEffect.AdvanceQuest(quest)));
        helper.assertTrue(story.questStep(quest) == Integer.MAX_VALUE, "the quest is not complete");
        boolean charm = knight.getInventory().items.stream().anyMatch(s -> s.is(com.sofe.registry.ItemRegistry.KNIGHT_SHIELD_CHARM.get()));
        helper.assertTrue(charm, "Cassian's charm was not given");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void aSorceressCannotStartCassiansQuest(GameTestHelper helper) {
        ServerPlayer sorceress = bearer(helper, PlayerClass.SORCERESS);
        QuestEngine.run(sorceress, List.of(new QuestEffect.StartQuest("sofe:bearer/cassian_act1")));
        helper.assertTrue(StoryCapability.get(sorceress).orElseThrow().questStep("sofe:bearer/cassian_act1") == 0, "another Bearer's quest started");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void theEndlessWarSetsNordrathsFate(GameTestHelper helper) {
        ServerPlayer player = bearer(helper, PlayerClass.THIEF);
        var story = StoryCapability.get(player).orElseThrow();
        story.advanceTo(2);
        QuestEngine.run(player, List.of(new QuestEffect.SetFate("nordrath", "peace")));
        helper.assertTrue(story.fate(Region.NORDRATH).map("peace"::equals).orElse(false), "Nordrath's fate was not set");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void twoKnightsEachTravelWithTheirOwnSorceress(GameTestHelper helper) {
        ServerPlayer first = bearer(helper, PlayerClass.KNIGHT, "sofe_test_knight_a");
        ServerPlayer second = bearer(helper, PlayerClass.KNIGHT, "sofe_test_knight_b");
        helper.assertTrue(Companions.hire(first, PlayerClass.SORCERESS), "the first Knight could not hire Shirin");
        helper.assertTrue(Companions.hire(second, PlayerClass.SORCERESS), "a second Knight could not hire his own Shirin");
        Companions.order(first, "dismiss");
        Companions.order(second, "dismiss");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void twoThievesKeepTheirOwnMarksOnOneEnemy(GameTestHelper helper) {
        ServerPlayer a = bearer(helper, PlayerClass.THIEF, "sofe_test_thief_a");
        ServerPlayer b = bearer(helper, PlayerClass.THIEF, "sofe_test_thief_b");
        var zombie = helper.spawnWithNoFreeWill(net.minecraft.world.entity.EntityType.ZOMBIE, 2, 2, 2);
        com.sofe.skill.thief.ThiefSkills.addMarks(a, zombie, 3);
        com.sofe.skill.thief.ThiefSkills.addMarks(b, zombie, 1);
        helper.assertTrue(com.sofe.skill.thief.ThiefSkills.marks(a, zombie) == 3, "the second Thief's mark wiped the first's");
        helper.assertTrue(com.sofe.skill.thief.ThiefSkills.marks(b, zombie) == 1, "the second Thief has no mark");
        helper.assertTrue(com.sofe.skill.thief.ThiefSkills.takeMarks(a, zombie) == 3, "the finisher did not take the first Thief's marks");
        helper.assertTrue(com.sofe.skill.thief.ThiefSkills.marks(b, zombie) == 1, "one Thief's finisher took the other's marks");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void twoNecromancersBothMarkOneEnemy(GameTestHelper helper) {
        ServerPlayer a = bearer(helper, PlayerClass.NECROMANCER, "sofe_test_necro_a");
        ServerPlayer b = bearer(helper, PlayerClass.NECROMANCER, "sofe_test_necro_b");
        var zombie = helper.spawnWithNoFreeWill(net.minecraft.world.entity.EntityType.ZOMBIE, 2, 2, 2);
        com.sofe.skill.necromancer.NecromancerSkills.markThreshold(zombie, a);
        com.sofe.skill.necromancer.NecromancerSkills.markThreshold(zombie, b);
        String marked = zombie.getPersistentData().getString(com.sofe.skill.necromancer.NecromancerSkills.THRESHOLD_TAG);
        helper.assertTrue(marked.contains(a.getStringUUID()) && marked.contains(b.getStringUUID()), "one Necromancer's mark replaced the other's");
        helper.succeed();
    }
}
