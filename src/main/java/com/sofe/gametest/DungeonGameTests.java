package com.sofe.gametest;

import com.mojang.authlib.GameProfile;
import com.sofe.SoFEMod;
import com.sofe.puzzle.PuzzleData;
import com.sofe.puzzle.PuzzleDefinition;
import com.sofe.puzzle.PuzzleService;
import com.sofe.puzzle.RuneStoneBlock;
import com.sofe.quest.QuestEngine;
import com.sofe.quest.StoryDataManager;
import com.sofe.registry.SoFEBlocks;
import com.sofe.story.StoryCapability;
import com.sofe.story.StoryProgress;
import com.sofe.world.build.StructureBuilder;
import com.sofe.world.zone.StructurePositions;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

/** The rune puzzles before the bosses and the lesser dungeons (scripts/make_dungeons.py). */
@GameTestHolder(SoFEMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class DungeonGameTests {

    private static ServerPlayer bearer(GameTestHelper helper, String name) {
        ServerPlayer p = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), name));
        BlockPos at = helper.absolutePos(new BlockPos(2, 2, 2));
        p.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5);
        return p;
    }

    /** The runes in the riddle's order open the seal for the Bearer; a wrong rune puts them all out first. */
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void theRunesInTheRiddlesOrderOpenTheSeal(GameTestHelper helper) {
        String tag = Long.toHexString(System.nanoTime());
        BlockPos column = helper.absolutePos(new BlockPos(4, 2, 4));
        PuzzleDefinition puzzle = new PuzzleDefinition("sofe:test_" + tag, PuzzleDefinition.Kind.ORDER,
                List.of(PuzzleDefinition.Rune.MOON, PuzzleDefinition.Rune.SUN, PuzzleDefinition.Rune.EYE),
                List.of(PuzzleDefinition.Rune.SUN, PuzzleDefinition.Rune.EYE, PuzzleDefinition.Rune.MOON), List.of(),
                "sofe:test/gate_" + tag, null, null, column.getX(), column.getZ(), "south", 1);
        StoryDataManager.putPuzzleForTest(puzzle);
        var level = helper.getLevel();
        helper.assertTrue(PuzzleService.place(level, puzzle, StructurePositions.Layout.EMPTY), "the puzzle was not placed");
        List<BlockPos> stones = PuzzleData.get(level.getServer()).stones(puzzle.id());
        helper.assertTrue(stones.size() == 3, "three Rune Stones should stand");
        helper.assertTrue(level.getBlockState(stones.get(0)).is(SoFEBlocks.RUNE_STONE.get())
                && level.getBlockState(stones.get(0)).getValue(RuneStoneBlock.RUNE) == PuzzleDefinition.Rune.MOON.ordinal(), "the first stone is not the Moon");
        ServerPlayer hero = bearer(helper, "sofe_test_runes");
        helper.assertFalse(PuzzleService.opens(hero, puzzle.gate()), "the seal opened before the runes");
        PuzzleService.press(level, stones.get(1), hero); // the sun: right
        helper.assertTrue(level.getBlockState(stones.get(1)).getValue(RuneStoneBlock.LIT), "the Sun does not burn");
        PuzzleService.press(level, stones.get(0), hero); // the moon: wrong
        helper.assertFalse(level.getBlockState(stones.get(1)).getValue(RuneStoneBlock.LIT), "a wrong rune should put them all out");
        PuzzleService.press(level, stones.get(1), hero);
        PuzzleService.press(level, stones.get(2), hero);
        PuzzleService.press(level, stones.get(0), hero);
        helper.assertTrue(StoryCapability.get(hero).orElseThrow().hasSolved(puzzle.id()), "solving the runes did not count for the Bearer");
        helper.assertTrue(PuzzleService.opens(hero, puzzle.gate()), "the seal did not open");
        for (BlockPos s : stones) level.setBlock(s, Blocks.AIR.defaultBlockState(), 3);
        helper.succeed();
    }

    /** A Bearer who already beat a dungeon's boss never has to solve its runes again (an Echo fight, an old world). */
    @GameTest(template = "empty")
    public static void aBeatenBossOpensItsSealWithoutTheRunes(GameTestHelper helper) {
        ServerPlayer hero = bearer(helper, "sofe_test_veteran");
        var forge = PuzzleService.forGate("sofe:nordrath/forge");
        helper.assertTrue(forge.isPresent(), "the Forge of Nordrath has no puzzle");
        helper.assertFalse(PuzzleService.opens(hero, "sofe:nordrath/forge"), "the Forge opened with no runes and no Kaleth");
        StoryCapability.get(hero).orElseThrow().defeat(forge.get().boss());
        helper.assertTrue(PuzzleService.opens(hero, "sofe:nordrath/forge"), "Kaleth fell, the Forge should open");
        helper.assertTrue(PuzzleService.opens(hero, "sofe:nordrath/feast_halls"), "a gate with no puzzle should not ask for one");
        helper.succeed();
    }

    /** The Ruin: walls, its Sealed Gate, spawners and chests; the Crypt: spawners and a chest. */
    @GameTest(template = "empty", timeoutTicks = 400)
    public static void aRuinAndACryptAreBuilt(GameTestHelper helper) {
        var level = helper.getLevel();
        StructurePositions.Structure ruin = StructurePositions.get().structure("sofe:nordrath/ruin").orElseThrow();
        BlockPos base = helper.absolutePos(new BlockPos(8, 1, 8)).offset(-600, 0, 0);
        var here = new StructurePositions.Structure("sofe:nordrath/ruin", base.getX(), base.getZ(), ruin.sizeX(), ruin.sizeZ(), -64, 319, ruin.zone());
        helper.assertTrue(StructureBuilder.build(level, here), "the Ruin has no blockout");
        var crypt = new StructurePositions.Structure("sofe:khemet/crypt", base.getX(), base.getZ() + 60, 17, 17, -64, 319, null);
        helper.assertTrue(StructureBuilder.build(level, crypt), "the Crypt has no blockout");
        int spawners = 0, chests = 0;
        for (int x = base.getX() - 20; x <= base.getX() + 20; x++) {
            for (int z = base.getZ() - 16; z <= base.getZ() + 70; z++) {
                int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
                for (int y = top - 12; y <= top; y++) {
                    var state = level.getBlockState(new BlockPos(x, y, z));
                    if (state.is(Blocks.SPAWNER)) spawners++;
                    if (state.is(Blocks.CHEST)) chests++;
                }
            }
        }
        helper.assertTrue(spawners >= 5, "the Ruin and the Crypt should hold at least five spawners, found " + spawners);
        helper.assertTrue(chests >= 3, "the Ruin's two chests and the Crypt's one, found " + chests);
        helper.succeed();
    }

    /** Coming near a Crypt once its act has come begins its dungeon quest; before that act, it does not. */
    @GameTest(template = "empty")
    public static void aDungeonQuestBeginsNearItsDungeon(GameTestHelper helper) {
        ServerPlayer hero = bearer(helper, "sofe_test_explorer");
        var quest = StoryDataManager.quest("sofe:dungeon/nordrath_crypt").orElseThrow();
        helper.assertTrue(quest.discovery() != null, "the Barrow has no place to be found at");
        hero.moveTo(quest.discovery().x() + 0.5, 80, quest.discovery().z() + 0.5);
        StoryProgress story = StoryCapability.get(hero).orElseThrow();
        QuestEngine.discover(hero);
        helper.assertTrue(story.quest(quest.id()).isEmpty(), "the Barrow's quest began in Act I");
        story.advanceTo(2);
        QuestEngine.discover(hero);
        helper.assertTrue(story.quest(quest.id()).isPresent(), "the Barrow's quest did not begin");
        helper.succeed();
    }

    /** A Bearer who had gone on before a Ruin came into the act is not asked twice: runes solved and bosses beaten move on. */
    @GameTest(template = "empty")
    public static void stepsAlreadyDoneMoveOnByThemselves(GameTestHelper helper) {
        ServerPlayer hero = bearer(helper, "sofe_test_veteran_north");
        StoryProgress story = StoryCapability.get(hero).orElseThrow();
        story.advanceTo(2);
        String act2 = "sofe:act2_north";
        QuestEngine.startQuest(hero, act2);
        QuestEngine.event(hero, new com.sofe.quest.QuestEvent.EnteredRegion("nordrath"));
        helper.assertTrue(story.questStep(act2) == 2, "the Burnt Longhall's runes should come right after Nordrath");
        story.solvePuzzle("sofe:nordrath_ruin");
        QuestEngine.skipDone(hero);
        helper.assertTrue(story.questStep(act2) == 3, "solved runes should move on to the guardian");
        QuestEngine.advance(hero, act2); // the guardian
        story.defeat("sofe:kaleth");
        story.defeat("sofe:serath");
        QuestEngine.skipDone(hero);
        helper.assertTrue(story.questStep(act2) == 6, "Kaleth and Serath were beaten: Vorath should be next, is at " + story.questStep(act2));
        com.sofe.quest.DialogueService.close(hero);
        helper.succeed();
    }
}
