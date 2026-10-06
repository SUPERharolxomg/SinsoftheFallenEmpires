package com.sofe.gametest;

import com.mojang.authlib.GameProfile;
import com.sofe.SoFEMod;
import com.sofe.block.KinshipChest;
import com.sofe.economy.EconomyCapability;
import com.sofe.entity.boss.KalethEntity;
import com.sofe.item.CodexShardItem;
import com.sofe.item.StoryItems;
import com.sofe.pact.Pact;
import com.sofe.pact.PactData;
import com.sofe.registry.EntityRegistry;
import com.sofe.registry.ItemRegistry;
import com.sofe.registry.SoFEBlocks;
import com.sofe.story.Sin;
import com.sofe.story.StoryCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Optional;
import java.util.UUID;

/** Sprint 7.5: the Pact of the Empires, bosses for many players, Kinship Chests and the Council's restoring. */
@GameTestHolder(SoFEMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class Sprint75GameTests {

    private static ServerPlayer bearer(GameTestHelper helper, String name, double x) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), name));
        Vec3 at = helper.absoluteVec(new Vec3(x, 1, 2.5));
        player.moveTo(at.x, at.y, at.z, 0, 0);
        return player;
    }

    @GameTest(template = "empty")
    public static void aPactIsMadeJoinedAndDissolved(GameTestHelper helper) {
        PactData data = PactData.get(helper.getLevel().getServer());
        UUID a = UUID.randomUUID(), b = UUID.randomUUID(), c = UUID.randomUUID();
        Pact pact = data.create(a, "sofe_test_a");
        data.join(pact, b, "sofe_test_b");
        data.join(pact, c, "sofe_test_c");
        helper.assertTrue(data.of(c).map(p -> p.size() == 3).orElse(false), "three Bearers should share the Pact");
        Optional<Pact> after = data.leave(a);
        helper.assertTrue(after.isPresent() && after.get().leader().equals(b), "when the leader leaves, the next member should lead");
        data.leave(b);
        helper.assertTrue(data.of(c).isEmpty(), "a Pact left with one member should dissolve");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void aBossGrowsForMorePlayersButNotItsDamage(GameTestHelper helper) {
        KalethEntity kaleth = EntityRegistry.KALETH.get().create(helper.getLevel());
        helper.assertTrue(kaleth != null, "Kaleth was not made");
        float alone = kaleth.getMaxHealth();
        double damage = kaleth.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
        kaleth.scaleFor(3);
        helper.assertTrue(Math.abs(kaleth.getMaxHealth() - alone * 2.2f) < 0.5f, "three players should give Kaleth +120% health, got "
                + kaleth.getMaxHealth() + " from " + alone);
        helper.assertTrue(kaleth.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE) == damage, "his damage changed");
        kaleth.scaleFor(2);
        helper.assertTrue(kaleth.scaledFor() == 3, "a player leaving should not shrink the boss mid-fight");
        kaleth.reset();
        helper.assertTrue(Math.abs(kaleth.getMaxHealth() - alone) < 0.5f, "a reset should bring the boss back to one player's health");
        kaleth.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void aKinshipChestOpensOnlyForTwoOrMore(GameTestHelper helper) {
        BlockPos at = new BlockPos(2, 1, 2);
        helper.setBlock(at, SoFEBlocks.KINSHIP_CHEST.get());
        BlockPos abs = helper.absolutePos(at);
        ServerPlayer first = bearer(helper, "sofe_test_kin_a", 1.5);
        helper.getLevel().addFreshEntity(first);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false);
        long before = EconomyCapability.get(first).map(e -> e.dinars()).orElse(0L);
        helper.getBlockState(at).use(helper.getLevel(), first, InteractionHand.MAIN_HAND, hit);
        helper.assertTrue(EconomyCapability.get(first).map(e -> e.dinars()).orElse(0L) == before, "one Bearer alone opened the Kinship Chest");
        ServerPlayer second = bearer(helper, "sofe_test_kin_b", 3.5);
        helper.getLevel().addFreshEntity(second);
        helper.getBlockState(at).use(helper.getLevel(), first, InteractionHand.MAIN_HAND, hit);
        helper.assertTrue(EconomyCapability.get(first).map(e -> e.dinars()).orElse(0L) > before, "two Bearers together should get their shares");
        helper.assertTrue(helper.getLevel().getBlockEntity(abs) instanceof KinshipChest.Entity chest && chest.openedBy(second.getUUID()),
                "the second Bearer did not get their share");
        long after = EconomyCapability.get(first).map(e -> e.dinars()).orElse(0L);
        helper.getBlockState(at).use(helper.getLevel(), first, InteractionHand.MAIN_HAND, hit);
        helper.assertTrue(EconomyCapability.get(first).map(e -> e.dinars()).orElse(0L) == after, "a share was taken twice");
        first.discard();
        second.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void theCouncilGivesBackTheFlaskAndTheShardsOfBeatenArchsins(GameTestHelper helper) {
        ServerPlayer player = bearer(helper, "sofe_test_council", 2.5);
        StoryCapability.get(player).orElseThrow().defeat("sofe:vorath");
        StoryCapability.get(player).orElseThrow().defeat("sofe:luxara");
        player.getInventory().add(CodexShardItem.of(Sin.WRATH)); // the Wrath Shard was kept; the Lust Shard and the Flask were lost
        var given = StoryItems.restore(player);
        helper.assertTrue(given.size() == 2, "expected the Flask and the Lust Shard, got " + given.size());
        helper.assertTrue(given.stream().anyMatch(s -> s.is(ItemRegistry.BEARERS_FLASK.get())), "the Flask was not given back");
        helper.assertTrue(given.stream().anyMatch(s -> CodexShardItem.sin(s).orElse(null) == Sin.LUST), "the Lust Shard was not given back");
        helper.assertTrue(StoryItems.restore(player).isEmpty(), "the Council gave the same items twice");
        helper.succeed();
    }
}
