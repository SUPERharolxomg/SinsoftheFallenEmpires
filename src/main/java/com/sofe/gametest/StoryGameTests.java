package com.sofe.gametest;

import com.mojang.authlib.GameProfile;
import com.sofe.SoFEMod;
import com.sofe.player.PlayerClass;
import com.sofe.player.PlayerClassCapability;
import com.sofe.quest.DialogueService;
import com.sofe.quest.QuestEngine;
import com.sofe.story.StoryAct;
import com.sofe.story.StoryCapability;
import com.sofe.story.StoryProgress;
import com.sofe.world.region.Region;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

/** The story on a real server: quests from data/sofe/quests, dialogue, the King's variant and fates. */
@GameTestHolder(SoFEMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class StoryGameTests {
    private static final String SIDE = "sofe:side/sulthari_embers";

    private static ServerPlayer bearer(GameTestHelper helper, PlayerClass playerClass) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "sofe_story_" + playerClass.id()));
        PlayerClassCapability.get(player).orElseThrow().set(playerClass);
        return player;
    }

    private static StoryProgress story(ServerPlayer player) {
        return StoryCapability.get(player).orElseThrow();
    }

    /** Moves through lines without answers until one asks for a choice. */
    private static void continueUntilChoice(ServerPlayer player, int maxLines) {
        for (int i = 0; i < maxLines && DialogueService.isTalking(player); i++) {
            String current = DialogueService.current(player).orElseThrow();
            int line = Integer.parseInt(current.substring(current.indexOf('#') + 1));
            DialogueService.answer(player, line, -1);
            if (DialogueService.current(player).map(current::equals).orElse(false)) return; // the line waits for an answer
        }
    }

    private static int line(ServerPlayer player) {
        String current = DialogueService.current(player).orElseThrow();
        return Integer.parseInt(current.substring(current.indexOf('#') + 1));
    }

    @GameTest(template = "empty")
    public static void becomingABearerStartsActOneWithTheShardScene(GameTestHelper helper) {
        ServerPlayer player = bearer(helper, PlayerClass.SORCERESS);
        QuestEngine.onBearerChosen(player);
        helper.assertTrue(story(player).questStep(QuestEngine.FIRST_QUEST) == 1, "the Act I quest did not start");
        helper.assertTrue(DialogueService.current(player).map(c -> c.startsWith("sofe:act1/shard#")).orElse(false),
                "the shard scene did not open: " + DialogueService.current(player));
        DialogueService.close(player);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void theKingMeetsTheCouncilLedByOzhan(GameTestHelper helper) {
        ServerPlayer king = bearer(helper, PlayerClass.KING);
        ServerPlayer knight = bearer(helper, PlayerClass.KNIGHT);
        for (ServerPlayer p : new ServerPlayer[]{king, knight}) {
            QuestEngine.startQuest(p, QuestEngine.FIRST_QUEST);
            QuestEngine.advance(p, QuestEngine.FIRST_QUEST);
            QuestEngine.advance(p, QuestEngine.FIRST_QUEST);
            DialogueService.close(p);
            DialogueService.talkTo(p, "ozhan", null);
        }
        helper.assertTrue(DialogueService.current(king).orElse("").equals("sofe:act1/council#2"),
                "the King should start on Ozhan's disguise line, got " + DialogueService.current(king));
        helper.assertTrue(DialogueService.current(knight).orElse("").equals("sofe:act1/council#0"),
                "the Knight should meet the Council with Azhar, got " + DialogueService.current(knight));

        continueUntilChoice(king, 10);
        DialogueService.answer(king, line(king), 0); // "I will stop it."
        helper.assertTrue(story(king).questStep(QuestEngine.FIRST_QUEST) == 4, "answering did not move to the Brass Sentinel step");
        DialogueService.close(king);
        DialogueService.close(knight);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void theSultharisFateIsChosenOnce(GameTestHelper helper) {
        ServerPlayer player = bearer(helper, PlayerClass.THIEF);
        QuestEngine.startQuest(player, QuestEngine.FIRST_QUEST);
        QuestEngine.advance(player, QuestEngine.FIRST_QUEST);
        QuestEngine.advance(player, QuestEngine.FIRST_QUEST);
        DialogueService.close(player);

        DialogueService.talkTo(player, "dilara", null);
        continueUntilChoice(player, 5);
        DialogueService.answer(player, line(player), 0); // "I will talk to him."
        helper.assertTrue(story(player).questStep(SIDE) == 1, "Dilara did not start the side quest");

        DialogueService.talkTo(player, "council_elder", null); // talking counts for step 1, then the choice opens
        helper.assertTrue(story(player).questStep(SIDE) == 2, "talking to the elder did not count");
        continueUntilChoice(player, 5);
        DialogueService.answer(player, line(player), 1); // protect the Observatory
        helper.assertTrue(story(player).fate(Region.SULTHARI).orElse("").equals("observatory"), "the fate was not saved");
        helper.assertTrue(story(player).questStep(SIDE) == 3, "the choice did not advance the quest");
        helper.assertTrue(!story(player).setFate(Region.SULTHARI, "bazaar"), "a fate can be set only once");
        DialogueService.close(player);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void theActComesFromTheStoryAndSurvivesDeath(GameTestHelper helper) {
        ServerPlayer player = bearer(helper, PlayerClass.NECROMANCER);
        helper.assertTrue(StoryAct.of(player) == 1, "a new player should be in Act I");
        story(player).advanceTo(3);
        helper.assertTrue(StoryAct.of(player) == 3, "StoryAct does not read the player's story");

        StoryProgress copy = new StoryProgress();
        StoryCapability.load(copy, StoryCapability.save(story(player)));
        helper.assertTrue(copy.act() == 3, "the act was not saved");
        helper.succeed();
    }
}
