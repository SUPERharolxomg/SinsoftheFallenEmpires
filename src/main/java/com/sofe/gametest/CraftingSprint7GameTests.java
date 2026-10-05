package com.sofe.gametest;

import com.mojang.authlib.GameProfile;
import com.sofe.SoFEMod;
import com.sofe.crafting.TemperingAnvilBlock;
import com.sofe.gear.GearBonuses;
import com.sofe.gear.GearData;
import com.sofe.gear.GearMaker;
import com.sofe.gear.GearNbt;
import com.sofe.gear.GearStat;
import com.sofe.gear.Rarity;
import com.sofe.gear.SinGem;
import com.sofe.gear.Sockets;
import com.sofe.registry.ItemRegistry;
import com.sofe.world.region.Region;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

/** Sprint 7 crafting: sockets and sin gems, the Tempering Anvil, the Jeweler's and Purifier's recipes, the land healing. */
@GameTestHolder(SoFEMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class CraftingSprint7GameTests {

    private static ServerPlayer player(GameTestHelper helper, String name) {
        return FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), name));
    }

    private static ItemStack temperedSword(GameTestHelper helper) {
        return GearMaker.rollItem("sofe:brass_scimitar", 20, Rarity.TEMPERED, null, helper.getLevel().getRandom()).orElseThrow();
    }

    @GameTest(template = "empty")
    public static void aGemSetInAnOpenSocketGivesItsStat(GameTestHelper helper) {
        ItemStack sword = temperedSword(helper);
        helper.assertTrue(Sockets.socketable(sword), "a sword takes sockets");
        helper.assertFalse(Sockets.set(sword, SinGem.WRATH_RUBY, SinGem.Form.CUT), "a gem was set with no socket open");
        helper.assertTrue(Sockets.open(sword), "the first socket did not open");
        helper.assertFalse(Sockets.set(sword, SinGem.WRATH_RUBY, SinGem.Form.ROUGH), "a rough gem was set");
        double before = GearBonuses.of(List.of(GearNbt.read(sword).orElseThrow()), 100, a -> 100).get(GearStat.PHYSICAL_DAMAGE);
        helper.assertTrue(Sockets.set(sword, SinGem.WRATH_RUBY, SinGem.Form.OATH), "the Oath Ruby was not set");
        double after = GearBonuses.of(List.of(GearNbt.read(sword).orElseThrow()), 100, a -> 100).get(GearStat.PHYSICAL_DAMAGE);
        helper.assertTrue(after - before == SinGem.WRATH_RUBY.value(SinGem.Form.OATH),
                "the Oath Ruby should add " + SinGem.WRATH_RUBY.value(SinGem.Form.OATH) + "% physical damage, added " + (after - before));
        helper.assertFalse(Sockets.set(sword, SinGem.ENVY_EMERALD, SinGem.Form.CUT), "a second gem was set in a single socket");
        for (int i = 1; i < Sockets.MAX; i++) Sockets.open(sword);
        helper.assertTrue(Sockets.openCost(sword).isEmpty(), "a fourth socket can be opened");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void theTemperingAnvilRaisesAnAffixThreeTimesAtMost(GameTestHelper helper) {
        ServerPlayer player = player(helper, "sofe_test_anvil");
        player.getInventory().clearContent();
        player.getInventory().add(new ItemStack(TemperingAnvilBlock.aetherium(), 10));
        ItemStack sword = temperedSword(helper);
        int total = GearNbt.read(sword).orElseThrow().affixes().stream().mapToInt(GearData.Roll::value).sum();
        for (int i = 0; i < TemperingAnvilBlock.MAX_TEMPERS; i++) {
            helper.assertTrue(TemperingAnvilBlock.temper(player, sword, helper.getLevel().getRandom()) == TemperingAnvilBlock.Result.TEMPERED,
                    "temper " + (i + 1) + " failed");
        }
        int after = GearNbt.read(sword).orElseThrow().affixes().stream().mapToInt(GearData.Roll::value).sum();
        helper.assertTrue(after >= total + TemperingAnvilBlock.MAX_TEMPERS, "three tempers should raise the affixes: " + total + " -> " + after);
        helper.assertTrue(TemperingAnvilBlock.temper(player, sword, helper.getLevel().getRandom()) == TemperingAnvilBlock.Result.WORN_OUT,
                "a fourth temper was allowed");
        helper.assertTrue(player.getInventory().countItem(TemperingAnvilBlock.aetherium()) == 10 - TemperingAnvilBlock.MAX_TEMPERS,
                "each temper should cost one aetherium shard");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void theJewelerCutsEveryRoughGemAndThePurifierCleansBlackAetherium(GameTestHelper helper) {
        var recipes = helper.getLevel().getServer().getRecipeManager();
        for (SinGem gem : SinGem.values()) {
            var cut = recipes.byKey(new ResourceLocation(SoFEMod.MOD_ID, "jeweler/cut_" + gem.id()));
            helper.assertTrue(cut.isPresent(), "the Jeweler cannot cut " + gem.id());
            helper.assertTrue(cut.get().getResultItem(helper.getLevel().registryAccess()).is(ItemRegistry.sinGem(gem, SinGem.Form.CUT)),
                    "cutting " + gem.id() + " gives the wrong gem");
        }
        helper.assertTrue(recipes.byKey(new ResourceLocation(SoFEMod.MOD_ID, "purifier/aetherium_shard")).isPresent(), "the Purifier has no recipe");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void everyRegionGivesTheGemOfItsSin(GameTestHelper helper) {
        helper.assertTrue(SinGem.minedIn(Region.NORDRATH, 40, 2).orElseThrow() == SinGem.WRATH_RUBY, "Nordrath's gem");
        helper.assertTrue(SinGem.minedIn(Region.NORDRATH, -10, 2).orElseThrow() == SinGem.GLUTTONY_AMBER, "Nordrath's deep gem");
        helper.assertTrue(SinGem.minedIn(Region.PARSIVAN, 100, 3).orElseThrow() == SinGem.LUST_AMETHYST, "Parsivan's gem");
        helper.assertTrue(SinGem.minedIn(Region.KHEMET, 0, 3).orElseThrow() == SinGem.SLOTH_MOONSTONE, "Khemet's gem");
        helper.assertTrue(SinGem.minedIn(Region.AUREUM, 60, 4).orElseThrow() == SinGem.GREED_TOPAZ, "Aureum's gem");
        helper.assertTrue(SinGem.minedIn(Region.SULTHARI, 10, 4).isEmpty(), "Sulthari gave a gem before Act V");
        helper.assertTrue(SinGem.minedIn(Region.SULTHARI, 10, 5).orElseThrow() == SinGem.PRIDE_SUNSTONE, "Sulthari's gem in Act V");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void aCorruptedBlockHealsWhenItsRegionIsLiberated(GameTestHelper helper) {
        var corrupted = com.sofe.registry.SoFEBlocks.CORRUPTED_AUREUM_MARBLE_BRICKS.get().defaultBlockState();
        var healed = com.sofe.world.build.EmpireBlocks.heal(corrupted);
        helper.assertTrue(healed.isPresent() && healed.get().is(com.sofe.registry.SoFEBlocks.AUREUM_MARBLE_BRICKS.get()),
                "cracked marble does not heal into marble bricks");
        helper.assertTrue(com.sofe.world.build.RegionHealing.healedBy("sofe:envyris") == Region.AUREUM, "Envyris's fall should heal Aureum");
        helper.assertTrue(com.sofe.world.build.RegionHealing.healedBy("sofe:goldarc") == null, "a Broken Oath healed a region");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void vanillaMobsGrowMeanerInTheLaterActs(GameTestHelper helper) {
        var creeper = helper.spawnWithNoFreeWill(net.minecraft.world.entity.EntityType.CREEPER, 2, 2, 2);
        com.sofe.mob.MobTraits.apply(creeper, 3);
        var tag = new net.minecraft.nbt.CompoundTag();
        creeper.addAdditionalSaveData(tag);
        helper.assertTrue(tag.getShort("Fuse") == com.sofe.mob.MobTraits.SHORT_FUSE, "an Act III creeper keeps its long fuse: " + tag.getShort("Fuse"));
        var zombie = helper.spawnWithNoFreeWill(net.minecraft.world.entity.EntityType.ZOMBIE, 3, 2, 3);
        float plain = zombie.getMaxHealth();
        double help = zombie.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.SPAWN_REINFORCEMENTS_CHANCE);
        com.sofe.mob.MobTraits.apply(zombie, 4);
        helper.assertTrue(zombie.getMaxHealth() > plain, "an Act IV zombie is no tougher");
        helper.assertTrue(zombie.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.SPAWN_REINFORCEMENTS_CHANCE) > help,
                "an Act IV zombie calls no more reinforcements");
        creeper.discard();
        zombie.discard();
        helper.succeed();
    }

    // --- economy: Favor, camps, Kerem, Nilufar and Zahir

    @GameTest(template = "empty")
    public static void favorRisesInRanksAndLowersPrices(GameTestHelper helper) {
        var e = new com.sofe.economy.EconomyData();
        helper.assertTrue(e.favorRank("parsivan") == 0, "a stranger has favor");
        helper.assertTrue(e.addFavor("parsivan", 99) == -1, "99 points should not reach Known");
        helper.assertTrue(e.addFavor("parsivan", 1) == 1, "100 points should make the Bearer Known");
        e.addFavor("parsivan", 5000);
        helper.assertTrue(e.favorRank("parsivan") == com.sofe.economy.EconomyData.MAX_FAVOR_RANK, "lots of favor is not Exalted");
        helper.assertTrue(e.favorRank("aureum") == 0, "favor with one empire leaked into another");
        helper.assertTrue(com.sofe.economy.EconomyData.discounted(100, 5) == 90, "Exalted should take 10% off");
        var offer = new com.sofe.economy.MerchantOffer("t#0", "minecraft:bread", 1, 10, 1, 3, 5);
        helper.assertTrue(e.buy(offer, 5, 2, 10, 0) == com.sofe.economy.EconomyData.BuyResult.LOCKED, "an offer for Honored was sold to a Trusted Bearer");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void campMerchantsWaitForTheirArchsinAndZahirForALiberatedCamp(GameTestHelper helper) {
        for (String region : List.of("nordrath", "parsivan", "khemet", "aureum")) {
            var catalog = com.sofe.economy.MerchantService.catalog("ferid_camp_" + region);
            helper.assertTrue(catalog.isPresent(), "no alchemist in the " + region + " camp");
            helper.assertTrue(catalog.get().requires() != null && catalog.get().empire().equals(region),
                    "the " + region + " camp's alchemist trades before its Archsin falls or for another empire");
            helper.assertTrue(com.sofe.world.zone.StructurePositions.get().structure("sofe:" + region + "/camp").isPresent(), "no " + region + " camp");
        }
        helper.assertTrue(com.sofe.economy.MerchantService.catalog("kerem").isPresent(), "Kerem has no shop");
        helper.assertTrue(com.sofe.economy.MerchantService.catalog("nilufar").isPresent(), "Sister Nilufar has no shop");
        helper.assertTrue(com.sofe.economy.MerchantService.catalog("zahir").isPresent(), "Zahir has no wares");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60)
    public static void everyBearerGetsACofferOfTheirOwnThatOnlyTheyOpenAndThatFades(GameTestHelper helper) {
        ServerPlayer first = player(helper, "sofe_test_coffer_a"), second = player(helper, "sofe_test_coffer_b");
        var level = helper.getLevel();
        var middle = helper.absolutePos(new net.minecraft.core.BlockPos(4, 1, 4));
        var a = com.sofe.entity.boss.RewardCoffer.place(level, middle, 0, 2, first, List.of(new ItemStack(net.minecraft.world.item.Items.DIAMOND)));
        var b = com.sofe.entity.boss.RewardCoffer.place(level, middle, 1, 2, second, List.of(new ItemStack(net.minecraft.world.item.Items.EMERALD)));
        helper.assertFalse(a.equals(b), "two Bearers got the same coffer");
        var coffer = (com.sofe.entity.boss.RewardCoffer.Entity) level.getBlockEntity(a);
        helper.assertFalse(com.sofe.entity.boss.RewardCoffer.open(second, level, a, coffer), "a Bearer opened another's coffer");
        helper.assertTrue(com.sofe.entity.boss.RewardCoffer.open(first, level, a, coffer), "the owner could not open their coffer");
        helper.assertTrue(level.getBlockState(a).isAir(), "an opened coffer did not vanish");
        var other = (com.sofe.entity.boss.RewardCoffer.Entity) level.getBlockEntity(b);
        helper.assertFalse(other.expired(level.getGameTime()), "a new coffer has already faded");
        helper.assertTrue(other.expired(level.getGameTime() + com.sofe.entity.boss.RewardCoffer.LIFETIME_TICKS), "a coffer does not fade after thirty minutes");
        level.setBlock(b, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void aSoulboundItemWithAFullPackFallsInsteadOfLoopingForever(GameTestHelper helper) {
        ServerPlayer player = player(helper, "sofe_test_full_pack");
        player.setPos(helper.absoluteVec(new net.minecraft.world.phys.Vec3(2.5, 2, 2.5)));
        for (int slot = 0; slot < player.getInventory().items.size(); slot++) {
            player.getInventory().items.set(slot, new ItemStack(net.minecraft.world.item.Items.COBBLESTONE, 64));
        }
        ItemStack shard = com.sofe.item.CodexShardItem.of(com.sofe.story.Sin.WRATH);
        var dropped = player.drop(shard, false);   // looped forever before (the toss put it back, and tossed it again)
        helper.assertTrue(dropped != null, "the shard did not fall from a full pack");
        helper.assertTrue(dropped.getItem().is(ItemRegistry.CODEX_SHARD.get()), "what fell is not the shard");
        dropped.discard();
        player.getInventory().clearContent();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void everyBossLootTableLoads(GameTestHelper helper) {
        var loot = helper.getLevel().getServer().getLootData();
        for (String boss : List.of("brass_sentinel", "kaleth", "serath", "vorath", "mirael", "thessyn", "dormiel", "luxara", "morthis", "goldarc",
                "nixara", "avarok", "fenrath", "gularth", "shadeyn", "envyris", "solrath", "prython", "nahrazel")) {
            var table = loot.getLootTable(new ResourceLocation(SoFEMod.MOD_ID, "entities/" + boss));
            helper.assertTrue(table != net.minecraft.world.level.storage.loot.LootTable.EMPTY, boss + "'s loot table did not load");
        }
        helper.succeed();
    }

    // --- Acts III and IV: the refugees in the camps and their quests

    private static java.util.Optional<String> talkWith(ServerPlayer player, String npc) {
        return com.sofe.quest.DialogueLogic.forNpc(com.sofe.quest.StoryDataManager.dialogues().values(), npc,
                com.sofe.story.PlayerProgressView.of(player)).map(d -> d.id());
    }

    @GameTest(template = "empty")
    public static void theRefugeesOfferTheirRegionsFateQuestsInTheirAct(GameTestHelper helper) {
        ServerPlayer player = player(helper, "sofe_test_refugees");
        var story = com.sofe.story.StoryCapability.get(player).orElseThrow();
        story.advanceTo(3);
        helper.assertTrue(talkWith(player, "parsivan_gardener").map(d -> d.contains("q_parsivan_dreaming_court_offer")).orElse(false),
                "Omid does not offer The Dreaming Court in Act III: " + talkWith(player, "parsivan_gardener"));
        helper.assertTrue(talkWith(player, "khemet_ferryman").map(d -> d.contains("q_khemet_souls_below_offer")).orElse(false),
                "Ani does not offer The Souls Below in Act III: " + talkWith(player, "khemet_ferryman"));
        helper.assertFalse(talkWith(player, "aureum_senator").map(d -> d.contains("q_aureum_gold_of_the_courts_offer")).orElse(false),
                "Varro offers Aureum's quest before Act IV");
        helper.assertTrue(talkWith(player, "aureum_widow").isPresent(), "Livia has nothing to say");
        story.advanceTo(4);
        helper.assertTrue(talkWith(player, "aureum_senator").map(d -> d.contains("q_aureum_gold_of_the_courts_offer")).orElse(false),
                "Varro does not offer The Gold of the Courts in Act IV: " + talkWith(player, "aureum_senator"));
        for (String q : List.of("side/parsivan_dreaming_court", "side/khemet_souls_below", "side/aureum_gold_of_the_courts",
                "bearer/cassian_act3", "bearer/azhar_act4")) {
            helper.assertTrue(com.sofe.quest.StoryDataManager.quest("sofe:" + q).isPresent(), "the quest " + q + " did not load");
        }
        helper.succeed();
    }
}
