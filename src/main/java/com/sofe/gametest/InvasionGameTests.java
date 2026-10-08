package com.sofe.gametest;

import com.mojang.authlib.GameProfile;
import com.sofe.SoFEMod;
import com.sofe.quest.Objective;
import com.sofe.quest.QuestDefinition;
import com.sofe.quest.QuestEngine;
import com.sofe.quest.QuestEvent;
import com.sofe.quest.StoryDataManager;
import com.sofe.quest.VoidInvasion;
import com.sofe.story.StoryCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** The Void's invasions in waves (VoidInvasion): each wave from its own sides, out of rifts, until its count has fallen. */
@GameTestHolder(SoFEMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class InvasionGameTests {

    /** The first wave comes from the east; once it has fallen, the next from the west and the north; then the rifts close. */
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void eachWaveComesFromItsSides(GameTestHelper helper) {
        BlockPos at = helper.absolutePos(new BlockPos(2, 2, 2));
        ServerPlayer hero = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "sofe_test_defender"));
        hero.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5);
        String id = "sofe:test_invasion_" + Long.toHexString(System.nanoTime());
        var invasion = new QuestDefinition.Invasion(new QuestDefinition.Target(at.getX(), at.getZ()), 48, 12, Map.of(),
                List.of("sofe:void_wretch"), List.of(new QuestDefinition.Wave(List.of("east"), 2), new QuestDefinition.Wave(List.of("west", "north"), 4)));
        StoryDataManager.putForTest(new QuestDefinition(id, QuestDefinition.Type.SIDE, 1,
                List.of(new QuestDefinition.Step(new Objective.Kill("sofe:void_*", 6), List.of(), null, invasion, null)), List.of(), null));
        QuestEngine.startQuest(hero, id);
        StoryCapability.get(hero).orElseThrow().track(id);

        VoidInvasion.tick(hero);
        helper.assertTrue(VoidInvasion.wave(hero) == 1 && VoidInvasion.openSides(hero).isEmpty(), "the first wave should wait a breath before its rift opens");
        VoidInvasion.hurry(hero);
        VoidInvasion.tick(hero);
        helper.assertTrue(VoidInvasion.openSides(hero).equals(List.of("east")), "the first wave should come from the east: " + VoidInvasion.openSides(hero));
        helper.assertTrue(VoidInvasion.guards(hero) == 3, "three soldiers should hold the east rift, found " + VoidInvasion.guards(hero));
        var level = helper.getLevel(); // the rifts open on the ground of the world, wherever the test stands
        AABB around = new AABB(at.getX() - 40, level.getMinBuildHeight(), at.getZ() - 40, at.getX() + 40, level.getMaxBuildHeight(), at.getZ() + 40);
        String mine = VoidInvasion.forTag(hero.getUUID());
        List<Mob> out = helper.getLevel().getEntitiesOfClass(Mob.class, around, m -> m.getTags().contains(mine) && m.getTags().contains(VoidInvasion.INVADER));
        helper.assertTrue(out.size() == 2, "the east rift should let out the wave's two creatures, found " + out.size());
        helper.assertTrue(out.stream().allMatch(m -> m.getX() > at.getX() + 2), "the creatures did not come out of the east");
        helper.assertFalse(helper.getLevel().getEntitiesOfClass(Display.BlockDisplay.class, around, d -> d.getTags().contains(VoidInvasion.RIFT)).isEmpty(),
                "the rift is not drawn");

        for (int i = 0; i < 2; i++) QuestEngine.event(hero, new QuestEvent.Killed("sofe:void_wretch"));
        VoidInvasion.tick(hero);
        VoidInvasion.hurry(hero);
        VoidInvasion.tick(hero);
        helper.assertTrue(VoidInvasion.wave(hero) == 2, "the second wave did not come");
        helper.assertTrue(VoidInvasion.openSides(hero).equals(List.of("west", "north")), "the second wave should come from the west and the north: " + VoidInvasion.openSides(hero));
        helper.assertTrue(VoidInvasion.guards(hero) == 9, "three soldiers should hold each rift, found " + VoidInvasion.guards(hero));

        for (int i = 0; i < 4; i++) QuestEngine.event(hero, new QuestEvent.Killed("sofe:void_wretch"));
        helper.assertTrue(StoryCapability.get(hero).orElseThrow().quest(id).map(s -> s.completed()).orElse(false), "the invasion's quest did not end");
        VoidInvasion.tick(hero);
        helper.assertTrue(VoidInvasion.openSides(hero).isEmpty(), "the rifts stayed open");
        helper.assertTrue(level.getEntitiesOfClass(com.sofe.entity.army.SoldierEntity.class, around, m -> m.getTags().contains(VoidInvasion.GUARD)).isEmpty(),
                "the rifts' soldiers did not go back");
        helper.assertTrue(helper.getLevel().getEntitiesOfClass(Display.BlockDisplay.class, around, d -> d.getTags().contains(VoidInvasion.RIFT)).isEmpty(),
                "a rift is still drawn");
        helper.assertTrue(helper.getLevel().getEntitiesOfClass(Mob.class, around, m -> m.getTags().contains(mine)).isEmpty(), "the stragglers did not fade");
        helper.succeed();
    }

    /** Act I's invasion: five waves, eighty creatures, east, west, north, south and then every side. */
    @GameTest(template = "empty")
    public static void sultharisInvasionHasFiveWaves(GameTestHelper helper) {
        var step = StoryDataManager.quest(QuestEngine.FIRST_QUEST).orElseThrow().step(1).orElseThrow();
        var invasion = step.invasion();
        helper.assertTrue(invasion != null, "Act I's second step is not an invasion");
        helper.assertTrue(invasion.waves().size() == 5 && invasion.total() == 80, "five waves of eighty in all, found " + invasion.waves().size() + " of " + invasion.total());
        helper.assertTrue(invasion.waves().get(4).from().size() == 4, "the last wave should come from every side");
        for (int i = 1; i < 4; i++) {
            helper.assertTrue(invasion.waves().get(i).count() > invasion.waves().get(i - 1).count(), "each wave should be bigger than the one before");
        }
        helper.succeed();
    }

    /** A crypt's halls step never hangs on its spawners: inside the crypt, the dead it still asks for rise near the Bearer. */
    @GameTest(template = "empty", timeoutTicks = 100)
    public static void aCryptsDeadRiseWithoutItsSpawners(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos at = helper.absolutePos(new BlockPos(4, 2, 4));
        level.setBlockAndUpdate(at.above(12), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState()); // the crypt's ground, far over the Bearer
        ServerPlayer hero = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "sofe_test_delver"));
        hero.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5);
        String id = "sofe:test_crypt_" + Long.toHexString(System.nanoTime());
        var haunt = new QuestDefinition.Haunt(at.getX(), at.getZ(), 6, 30, "sofe:sand_ghoul");
        StoryDataManager.putForTest(new QuestDefinition(id, QuestDefinition.Type.DUNGEON, 1,
                List.of(new QuestDefinition.Step(new Objective.Kill("sofe:sand_ghoul", 4), List.of(), null, null, null, haunt)), List.of(), null));
        QuestEngine.startQuest(hero, id);
        com.sofe.quest.CryptLord.wake(hero);
        AABB around = new AABB(at).inflate(16);
        List<Mob> risen = level.getEntitiesOfClass(Mob.class, around, m -> m.getTags().contains(com.sofe.quest.CryptLord.RISEN));
        helper.assertTrue(risen.size() == 3, "three of the crypt's dead should rise at once, found " + risen.size());
        // each is the Bearer's, so it counts for them whoever strikes it (QuestEngine.onKill finds a real player by this tag)
        helper.assertTrue(risen.stream().allMatch(m -> m.getTags().contains(VoidInvasion.forTag(hero.getUUID()))), "the risen dead are not the Bearer's");
        risen.forEach(net.minecraft.world.entity.Entity::discard);
        level.setBlockAndUpdate(at.above(12), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    /** A step's foes that are all gone (despawned, lost) are called again a little later: the step never hangs. */
    @GameTest(template = "empty", timeoutTicks = 320)
    public static void aStepsLostFoesComeAgain(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos at = helper.absolutePos(new BlockPos(4, 2, 4));
        ServerPlayer hero = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "sofe_test_hunter"));
        hero.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5);
        String id = "sofe:test_foes_" + Long.toHexString(System.nanoTime());
        StoryDataManager.putForTest(new QuestDefinition(id, QuestDefinition.Type.SIDE, 1, List.of(new QuestDefinition.Step(
                new Objective.Kill("sofe:sand_ghoul", 2), List.of(new com.sofe.quest.QuestEffect.Spawn("sofe:sand_ghoul", 2, 6)), null)), List.of(), null));
        QuestEngine.startQuest(hero, id);
        String mine = VoidInvasion.forTag(hero.getUUID());
        var called = level.getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(Mob.class), m -> m.getTags().contains(mine) && m.getTags().contains(QuestEngine.FOE));
        helper.assertTrue(called.size() == 2, "the step did not call its two foes as the Bearer's, found " + called.size());
        called.forEach(net.minecraft.world.entity.Entity::discard); // lost
        QuestEngine.keepFoes(hero);
        helper.runAfterDelay(QuestEngine.FOES_AGAIN_TICKS + 5, () -> {
            QuestEngine.keepFoes(hero);
            var again = level.getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(Mob.class), m -> m.isAlive() && m.getTags().contains(mine));
            helper.assertTrue(again.size() == 2, "the lost foes were not called again, found " + again.size());
            again.forEach(net.minecraft.world.entity.Entity::discard);
            helper.succeed();
        });
    }
}
