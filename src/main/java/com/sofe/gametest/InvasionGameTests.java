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
        var invasion = new QuestDefinition.Invasion(new QuestDefinition.Target(at.getX(), at.getZ()), 48, 8, Map.of(),
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
        var level = helper.getLevel(); // the rifts open on the ground of the world, wherever the test stands
        AABB around = new AABB(at.getX() - 40, level.getMinBuildHeight(), at.getZ() - 40, at.getX() + 40, level.getMaxBuildHeight(), at.getZ() + 40);
        String mine = VoidInvasion.forTag(hero.getUUID());
        List<Mob> out = helper.getLevel().getEntitiesOfClass(Mob.class, around, m -> m.getTags().contains(mine) && m.getTags().contains(VoidInvasion.INVADER));
        helper.assertTrue(out.size() == 2, "the east rift should let out the wave's two creatures, found " + out.size());
        helper.assertTrue(out.stream().allMatch(m -> m.getX() > at.getX() + 3), "the creatures did not come out of the east");
        helper.assertFalse(helper.getLevel().getEntitiesOfClass(Display.BlockDisplay.class, around, d -> d.getTags().contains(VoidInvasion.RIFT)).isEmpty(),
                "the rift is not drawn");

        for (int i = 0; i < 2; i++) QuestEngine.event(hero, new QuestEvent.Killed("sofe:void_wretch"));
        VoidInvasion.tick(hero);
        VoidInvasion.hurry(hero);
        VoidInvasion.tick(hero);
        helper.assertTrue(VoidInvasion.wave(hero) == 2, "the second wave did not come");
        helper.assertTrue(VoidInvasion.openSides(hero).equals(List.of("west", "north")), "the second wave should come from the west and the north: " + VoidInvasion.openSides(hero));

        for (int i = 0; i < 4; i++) QuestEngine.event(hero, new QuestEvent.Killed("sofe:void_wretch"));
        helper.assertTrue(StoryCapability.get(hero).orElseThrow().quest(id).map(s -> s.completed()).orElse(false), "the invasion's quest did not end");
        VoidInvasion.tick(hero);
        helper.assertTrue(VoidInvasion.openSides(hero).isEmpty(), "the rifts stayed open");
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
}
