package com.sofe.gametest;

import com.mojang.authlib.GameProfile;
import com.sofe.SoFEMod;
import com.sofe.death.CorpseHandler;
import com.sofe.death.CorpseRegistry;
import com.sofe.entity.BearerCorpseEntity;
import com.sofe.entity.boss.BrassSentinelEntity;
import com.sofe.entity.npc.BearerNpcEntity;
import com.sofe.entity.npc.StoryNpcEntity;
import com.sofe.player.PlayerClass;
import com.sofe.player.PlayerClassCapability;
import com.sofe.quest.DialogueService;
import com.sofe.quest.QuestEngine;
import com.sofe.registry.EntityRegistry;
import com.sofe.registry.SoFEBlocks;
import com.sofe.story.StoryCapability;
import com.sofe.travel.TravelCapability;
import com.sofe.travel.TravelData;
import com.sofe.travel.WaystoneService;
import com.sofe.world.zone.ProtectedZone;
import com.sofe.world.zone.ProtectedZoneData;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

/** Sprint 4, parts 4.3 to 4.5 on a real server: protected places, the Veil, the corpse, travel, NPCs and the Sentinel. */
@GameTestHolder(SoFEMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class SultharisLifeGameTests {

    private static ServerPlayer player(GameTestHelper helper, String name) {
        return FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), name));
    }

    @GameTest(template = "empty")
    public static void protectedZonesStopBreakingButTheHomesteadAllowsIt(GameTestHelper helper) {
        BlockPos city = helper.absolutePos(new BlockPos(1, 1, 1));
        BlockPos plot = helper.absolutePos(new BlockPos(3, 1, 1));
        helper.setBlock(new BlockPos(1, 1, 1), Blocks.STONE);
        helper.setBlock(new BlockPos(3, 1, 1), Blocks.STONE);
        ProtectedZoneData zones = ProtectedZoneData.get(helper.getLevel().getServer());
        String cityId = "sofe:test_city_" + city.asLong(), plotId = "sofe:test_plot_" + plot.asLong();
        zones.add(new ProtectedZone(cityId, ProtectedZone.Kind.CITY, city.getX() - 1, city.getY() - 1, city.getZ() - 1, plot.getX() + 1, city.getY() + 3, city.getZ() + 1));
        zones.add(new ProtectedZone(plotId, ProtectedZone.Kind.HOMESTEAD, plot.getX(), plot.getY(), plot.getZ(), plot.getX(), plot.getY(), plot.getZ()));
        ServerPlayer builder = player(helper, "sofe_test_builder");
        try {
            builder.gameMode.destroyBlock(city);
            builder.gameMode.destroyBlock(plot);
            helper.assertBlockPresent(Blocks.STONE, new BlockPos(1, 1, 1));
            helper.assertBlockNotPresent(Blocks.STONE, new BlockPos(3, 1, 1));
        } finally {
            zones.remove(cityId);
            zones.remove(plotId);
        }
        helper.succeed();
    }

    /** In a town a Bearer may hunt its hens and harvest its fields, and sow them again; the rest of the town is protected. */
    @GameTest(template = "empty")
    public static void aTownsAnimalsAndFieldsAreTheBearers(GameTestHelper helper) {
        BlockPos city = helper.absolutePos(new BlockPos(1, 2, 1));
        helper.setBlock(new BlockPos(3, 1, 1), Blocks.FARMLAND);
        helper.setBlock(new BlockPos(3, 2, 1), Blocks.WHEAT.defaultBlockState().setValue(net.minecraft.world.level.block.CropBlock.AGE, 7));
        helper.setBlock(new BlockPos(4, 2, 1), Blocks.STONE);
        ProtectedZoneData zones = ProtectedZoneData.get(helper.getLevel().getServer());
        String cityId = "sofe:test_town_" + city.asLong();
        zones.add(new ProtectedZone(cityId, ProtectedZone.Kind.CITY, city.getX() - 1, city.getY() - 2, city.getZ() - 1, city.getX() + 4, city.getY() + 3, city.getZ() + 1));
        ServerPlayer farmer = player(helper, "sofe_test_farmer");
        try {
            var hen = helper.spawn(EntityType.CHICKEN, new BlockPos(1, 2, 1));
            hen.setNoAi(true);
            hen.hurt(farmer.damageSources().playerAttack(farmer), 2f);
            helper.assertTrue(hen.getHealth() < hen.getMaxHealth(), "a hen of the town could not be hurt");
            hen.discard();
            farmer.gameMode.destroyBlock(helper.absolutePos(new BlockPos(3, 2, 1)));
            helper.assertBlockNotPresent(Blocks.WHEAT, new BlockPos(3, 2, 1));
            farmer.gameMode.destroyBlock(helper.absolutePos(new BlockPos(4, 2, 1)));
            helper.assertBlockPresent(Blocks.STONE, new BlockPos(4, 2, 1));
        } finally {
            zones.remove(cityId);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void theSealVeilStopsMobsAndCannotBeBroken(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 1, 1), SoFEBlocks.SEAL_VEIL.get());
        BlockPos veil = helper.absolutePos(new BlockPos(1, 1, 1));
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new Vec3(3.5, 1, 3.5));
        var state = helper.getLevel().getBlockState(veil);
        helper.assertFalse(state.getCollisionShape(helper.getLevel(), veil, CollisionContext.of(zombie)).isEmpty(), "a mob passed the Veil");
        helper.assertTrue(state.getDestroySpeed(helper.getLevel(), veil) < 0, "the Veil should be unbreakable, like bedrock");
        helper.assertTrue(state.getPistonPushReaction() == net.minecraft.world.level.material.PushReaction.BLOCK, "pistons should not move the Veil");
        ServerPlayer walker = player(helper, "sofe_test_walker");
        helper.assertTrue(state.getCollisionShape(helper.getLevel(), veil, CollisionContext.of(walker)).isEmpty(),
                "outside a journey there are no locks, so a player passes");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void theBodyKeepsTheGearForItsOwnerOnly(GameTestHelper helper) {
        ServerPlayer owner = player(helper, "sofe_test_fallen");
        ServerPlayer stranger = player(helper, "sofe_test_stranger");
        Vec3 at = helper.absoluteVec(new Vec3(2.5, 1, 2.5));
        owner.moveTo(at.x, at.y, at.z);
        owner.getInventory().setItem(3, new ItemStack(Items.DIAMOND_SWORD));
        owner.getInventory().setItem(39, new ItemStack(Items.IRON_HELMET)); // armor slot
        BearerCorpseEntity corpse = CorpseHandler.leaveCorpse(owner);

        helper.assertTrue(owner.getInventory().isEmpty(), "the gear should stay on the body");
        helper.assertTrue(corpse.itemCount() == 2, "the body should hold 2 stacks, has " + corpse.itemCount());
        helper.assertTrue(CorpseRegistry.get(owner.server).latest(owner.getUUID()).isPresent(), "the compass has no body to point to");
        helper.assertFalse(corpse.hurt(owner.damageSources().lava(), 1000), "the body should not take damage");

        corpse.interact(stranger, net.minecraft.world.InteractionHand.MAIN_HAND);
        helper.assertTrue(corpse.itemCount() == 2, "another player took from the body");

        corpse.giveBack(owner);
        helper.assertTrue(owner.getInventory().getItem(3).is(Items.DIAMOND_SWORD), "the sword did not return to its slot");
        helper.assertTrue(owner.getInventory().getItem(39).is(Items.IRON_HELMET), "the helmet did not return to its slot");
        helper.assertTrue(corpse.isRemoved(), "an empty body should disappear");
        helper.assertTrue(CorpseRegistry.get(owner.server).latest(owner.getUUID()).isEmpty(), "the recovered body is still tracked");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void waystonesAreActivatedPerPlayerAndTheVaultIsPersonal(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 1, 1), SoFEBlocks.WAYSTONE.get());
        BlockPos stone = helper.absolutePos(new BlockPos(1, 1, 1));
        ServerPlayer traveller = player(helper, "sofe_test_traveller");
        ServerPlayer other = player(helper, "sofe_test_other");
        WaystoneService.activate(traveller, stone);
        TravelData travel = TravelCapability.get(traveller).orElseThrow();
        helper.assertTrue(travel.isActivated(stone), "touching the Waystone did not activate it");
        helper.assertTrue(travel.lastActivated().map(w -> w.pos().equals(stone)).orElse(false), "it should be the respawn Waystone");
        helper.assertFalse(TravelCapability.get(other).orElseThrow().isActivated(stone), "Waystones are per player");

        travel.vault().setItem(0, new ItemStack(Items.EMERALD, 12));
        helper.assertTrue(TravelCapability.get(other).orElseThrow().vault().getItem(0).isEmpty(), "each player sees only their own Vault");
        TravelData copy = new TravelData(); // saved and loaded, as when the world is reopened
        TravelCapability.load(copy, TravelCapability.save(travel));
        helper.assertTrue(copy.vault().getItem(0).getCount() == 12 && copy.isActivated(stone), "the Vault or Waystones were not kept");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void npcsTalkAndABearerIgnoresItsOwnHero(GameTestHelper helper) {
        StoryNpcEntity ozhan = helper.spawn(EntityRegistry.STORY_NPC.get(), new Vec3(1.5, 1, 1.5));
        ozhan.setNpcId("ozhan");
        helper.assertFalse(ozhan.hurt(ozhan.damageSources().generic(), 100), "a story NPC should not be hurt");

        ServerPlayer visitor = player(helper, "sofe_test_visitor");
        PlayerClassCapability.get(visitor).orElseThrow().set(PlayerClass.THIEF);
        DialogueService.talkTo(visitor, ozhan.npcId(), ozhan);
        helper.assertTrue(DialogueService.current(visitor).map(c -> c.startsWith("sofe:ozhan/default#")).orElse(false),
                "Ozhan did not talk: " + DialogueService.current(visitor));
        DialogueService.close(visitor);

        BearerNpcEntity rurik = helper.spawn(EntityRegistry.BEARER_NPC.get(), new Vec3(3.5, 1, 1.5));
        rurik.setBearer(PlayerClass.THIEF);
        helper.assertTrue(rurik.isSameHeroAs(visitor), "the Thief should be Rurik");
        helper.assertTrue("rurik".equals(rurik.npcId()), "the Thief NPC should be rurik");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void theBrassSentinelScalesAndItsFallEndsActOne(GameTestHelper helper) {
        ServerPlayer hero = player(helper, "sofe_test_hero");
        PlayerClassCapability.get(hero).orElseThrow().set(PlayerClass.KNIGHT);
        QuestEngine.startQuest(hero, QuestEngine.FIRST_QUEST);
        int steps = com.sofe.quest.StoryDataManager.quest(QuestEngine.FIRST_QUEST).orElseThrow().steps().size(); // the Sentinel is the last
        for (int i = 0; i < steps - 1; i++) QuestEngine.advance(hero, QuestEngine.FIRST_QUEST);
        DialogueService.close(hero);
        helper.assertTrue(StoryCapability.get(hero).orElseThrow().questStep(QuestEngine.FIRST_QUEST) == steps, "the hero should face the Sentinel");

        BrassSentinelEntity sentinel = helper.spawn(EntityRegistry.BRASS_SENTINEL.get(), new Vec3(2.5, 1, 2.5));
        sentinel.setNoAi(true);
        sentinel.applyDifficulty(Difficulty.HARD);
        float hard = sentinel.getMaxHealth();
        sentinel.applyDifficulty(Difficulty.EASY);
        float easy = sentinel.getMaxHealth();
        helper.assertTrue(Math.abs(hard / easy - 1.25f / 0.75f) < 0.01f, "boss health does not follow the difficulty: " + hard + " vs " + easy);

        sentinel.hurt(hero.damageSources().playerAttack(hero), 100_000);
        helper.runAfterDelay(2, () -> {
            var story = StoryCapability.get(hero).orElseThrow();
            helper.assertTrue(story.hasDefeated(BrassSentinelEntity.BOSS_ID), "no credit for the Sentinel");
            helper.assertTrue(story.quest(QuestEngine.FIRST_QUEST).map(s -> s.completed()).orElse(false), "Act I did not finish");
            helper.assertTrue(story.act() == 2, "Act II did not begin");
            DialogueService.close(hero);
            helper.succeed();
        });
    }
}
