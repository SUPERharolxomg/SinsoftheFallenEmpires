package com.sofe.gametest;

import com.mojang.authlib.GameProfile;
import com.sofe.SoFEMod;
import com.sofe.crafting.StationService;
import com.sofe.economy.EconomyCapability;
import com.sofe.economy.EconomyData;
import com.sofe.economy.MerchantService;
import com.sofe.entity.boss.KalethEntity;
import com.sofe.entity.boss.RewardCoffer;
import com.sofe.entity.npc.MerchantNpcEntity;
import com.sofe.entity.npc.MerchantRole;
import com.sofe.gear.GearData;
import com.sofe.gear.GearNbt;
import com.sofe.gear.GearStat;
import com.sofe.gear.PlayerGear;
import com.sofe.gear.Rarity;
import com.sofe.item.ConsumableItems;
import com.sofe.item.Soulbound;
import com.sofe.player.PlayerClass;
import com.sofe.player.PlayerClassCapability;
import com.sofe.progression.CharacterStats;
import com.sofe.registry.EntityRegistry;
import com.sofe.registry.ItemRegistry;
import com.sofe.registry.SoFEBlocks;
import com.sofe.registry.material.Material;
import com.sofe.registry.material.MaterialForm;
import com.sofe.registry.material.MaterialRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

/** Sprint 5.5 on a real server: gear on the sheet, merchants, the Imperial Forge, Relics, the Flask and soulbound items. */
@GameTestHolder(SoFEMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class LootAndEconomyGameTests {

    private static ServerPlayer bearer(GameTestHelper helper, String name, Vec3 at) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), name));
        PlayerClassCapability.get(player).orElseThrow().set(PlayerClass.KNIGHT);
        Vec3 abs = helper.absoluteVec(at);
        player.moveTo(abs.x, abs.y, abs.z);
        return player;
    }

    private static ItemStack gear(net.minecraft.world.item.Item item, int itemLevel, GearData.Roll... rolls) {
        ItemStack stack = new ItemStack(item);
        GearNbt.write(stack, new GearData(itemLevel, Rarity.TEMPERED, List.of(rolls), null, 0, null, List.of()));
        return stack;
    }

    @GameTest(template = "empty")
    public static void gearRaisesTheSheetOnlyWhenItsRequirementsAreMet(GameTestHelper helper) {
        ServerPlayer player = bearer(helper, "sofe_test_gear", new Vec3(1.5, 1, 1.5));
        double before = CharacterStats.effects(player).orElseThrow().maxHealthBonus();
        player.setItemSlot(EquipmentSlot.HEAD, gear(ItemRegistry.BRASS_HELMET.get(), 1,
                new GearData.Roll("sofe:of_the_oak", GearStat.VITALITY, null, 4), new GearData.Roll("sofe:hale", GearStat.MAX_HEALTH, null, 6)));
        PlayerGear.changed(player);
        double with = CharacterStats.effects(player).orElseThrow().maxHealthBonus();
        helper.assertTrue(with > before + 6, "the helmet's Vitality and Hale did not raise health: " + before + " → " + with);

        player.setItemSlot(EquipmentSlot.HEAD, gear(ItemRegistry.BRASS_HELMET.get(), 20, new GearData.Roll("sofe:hale", GearStat.MAX_HEALTH, null, 6)));
        PlayerGear.changed(player);
        helper.assertTrue(PlayerGear.bonuses(player).isEmpty(), "a level 20 item gave bonuses to a level 1 Bearer");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void merchantsSellBuyAndBuyBack(GameTestHelper helper) {
        ServerPlayer player = bearer(helper, "sofe_test_trade", new Vec3(1.5, 1, 1.5));
        MerchantNpcEntity ferid = helper.spawn(EntityRegistry.MERCHANT.get(), new Vec3(2.5, 1, 1.5));
        ferid.setMerchant("ferid", MerchantRole.ALCHEMIST);
        EconomyData economy = EconomyCapability.get(player).orElseThrow();
        economy.addDinars(100);
        MerchantService.open(player, ferid, "ferid");

        MerchantService.act(player, "ferid", MerchantService.Action.BUY, 0); // the minor elixir, 15 Dinars
        helper.assertTrue(player.getInventory().countItem(ItemRegistry.MINOR_POMEGRANATE_ELIXIR.get()) == 1, "the elixir was not bought");
        helper.assertTrue(economy.dinars() == 85, "wrong price paid: " + economy.dinars());

        player.getInventory().setItem(5, new ItemStack(ItemRegistry.BRASS_SCIMITAR.get()));
        MerchantService.act(player, "ferid", MerchantService.Action.SELL, 5);
        helper.assertTrue(player.getInventory().getItem(5).isEmpty(), "the scimitar was not sold");
        helper.assertTrue(economy.dinars() > 85 && economy.buyback().size() == 1, "selling did not pay or was not kept for buyback");
        MerchantService.act(player, "ferid", MerchantService.Action.BUYBACK, 0);
        helper.assertTrue(player.getInventory().countItem(ItemRegistry.BRASS_SCIMITAR.get()) == 1, "the buyback did not return the scimitar");
        helper.assertTrue(economy.dinars() == 85, "the buyback should cost what the sale paid");

        player.getInventory().setItem(6, new ItemStack(ItemRegistry.BEARERS_FLASK.get()));
        MerchantService.act(player, "ferid", MerchantService.Action.SELL, 6);
        helper.assertFalse(player.getInventory().getItem(6).isEmpty(), "a soulbound story item was sold");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void theImperialForgeMakesTemperedGearFromABlueprint(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 1, 1), SoFEBlocks.IMPERIAL_FORGE.get());
        ServerPlayer player = bearer(helper, "sofe_test_forge", new Vec3(2.5, 1, 1.5));
        BlockPos forge = helper.absolutePos(new BlockPos(1, 1, 1));
        helper.getLevel().getBlockState(forge).use(helper.getLevel(), player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(forge), Direction.UP, forge, false));
        var recipe = SoFEMod.id("forge/glacial_iron_sword");
        player.getInventory().add(new ItemStack(MaterialRegistry.item(Material.GLACIAL_IRON, MaterialForm.INGOT), 3));
        player.getInventory().add(new ItemStack(Items.STICK));

        helper.assertTrue(StationService.make(player, recipe) == StationService.Result.UNKNOWN_BLUEPRINT, "the Forge worked without the Blueprint");
        EconomyCapability.get(player).orElseThrow().learn("sofe:glacial_iron_sword");
        helper.assertTrue(StationService.make(player, recipe) == StationService.Result.MADE, "the Forge did not make the sword");
        ItemStack sword = ItemStack.EMPTY;
        for (ItemStack s : player.getInventory().items) if (s.is(ItemRegistry.GLACIAL_IRON_SWORD.get())) sword = s;
        helper.assertFalse(sword.isEmpty(), "no sword in the inventory");
        GearData gear = GearNbt.read(sword).orElseThrow();
        helper.assertTrue(gear.rarity().atLeast(Rarity.TEMPERED) && !gear.affixes().isEmpty(), "the Forge must make Tempered gear with an affix");
        helper.assertTrue(player.getInventory().countItem(MaterialRegistry.item(Material.GLACIAL_IRON, MaterialForm.INGOT)) == 0, "the ingots were not used");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void kalethLeavesARewardCofferWithABoundRelic(GameTestHelper helper) {
        ServerPlayer hero = bearer(helper, "sofe_test_relic", new Vec3(1.5, 1, 1.5));
        KalethEntity kaleth = helper.spawn(EntityRegistry.KALETH.get(), new Vec3(3.5, 1, 3.5));
        kaleth.setNoAi(true);
        BlockPos at = kaleth.blockPosition();
        kaleth.hurt(hero.damageSources().playerAttack(hero), 100_000);
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(helper.getLevel().getBlockEntity(at) instanceof RewardCoffer.Entity, "no Reward Coffer where Kaleth fell");
            RewardCoffer.Entity coffer = (RewardCoffer.Entity) helper.getLevel().getBlockEntity(at);
            List<ItemStack> share = coffer.take(hero.getUUID());
            ItemStack relic = share.stream().filter(s -> s.is(ItemRegistry.KALETH_BLADE.get())).findFirst().orElse(ItemStack.EMPTY);
            helper.assertFalse(relic.isEmpty(), "Kaleth's Blade is not in the share: " + share);
            helper.assertTrue(GearNbt.read(relic).map(g -> g.rarity() == Rarity.RELIC && "kaleth_blade".equals(g.relic())).orElse(false), "the Relic has no Relic data");
            helper.assertTrue(GearNbt.owner(relic).map(hero.getUUID()::equals).orElse(false), "the Relic is not bound to its owner");
            helper.assertTrue(coffer.take(hero.getUUID()).isEmpty(), "a share can be taken only once");
            helper.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void theFlaskHealsAndSoulboundItemsSurviveDeath(GameTestHelper helper) {
        ServerPlayer player = bearer(helper, "sofe_test_flask", new Vec3(1.5, 1, 1.5));
        player.getInventory().add(new ItemStack(ItemRegistry.BEARERS_FLASK.get()));
        player.setHealth(4);
        helper.assertTrue(ConsumableItems.drinkFlask(player), "the Flask could not be used");
        helper.assertTrue(player.getHealth() > 4, "the Flask did not heal");
        helper.assertTrue(EconomyCapability.get(player).orElseThrow().flaskCharges() == EconomyData.FLASK_START - 1, "the Flask did not spend a charge");

        helper.assertTrue(Soulbound.is(new ItemStack(ItemRegistry.BEARERS_FLASK.get())), "the Flask should be soulbound");
        helper.assertFalse(Soulbound.is(new ItemStack(ItemRegistry.BRASS_SCIMITAR.get())), "ordinary gear is not soulbound");
        Soulbound.keepOnDeath(player);
        helper.assertTrue(player.getInventory().countItem(ItemRegistry.BEARERS_FLASK.get()) == 0, "the Flask was left in the dying inventory");
        helper.assertTrue(player.getPersistentData().contains(SoFEMod.MOD_ID + ":soulbound_kept"), "the Flask was not kept for the respawn");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void aRipeHerbGivesItsProduceAndSeeds(GameTestHelper helper) {
        var herb = com.sofe.registry.HerbRegistry.SAGE;
        helper.setBlock(new BlockPos(1, 0, 1), net.minecraft.world.level.block.Blocks.FARMLAND);
        helper.setBlock(new BlockPos(1, 1, 1), herb.crop().get().defaultBlockState().setValue(net.minecraft.world.level.block.CropBlock.AGE, 7));
        ServerPlayer farmer = bearer(helper, "sofe_test_farmer", new Vec3(2.5, 1, 1.5));
        farmer.gameMode.destroyBlock(helper.absolutePos(new BlockPos(1, 1, 1)));
        helper.assertItemEntityPresent(herb.produce().get(), new BlockPos(1, 1, 1), 2);
        helper.assertItemEntityPresent(herb.seeds().get(), new BlockPos(1, 1, 1), 2);
        helper.succeed();
    }
}
