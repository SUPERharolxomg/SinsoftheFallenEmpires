package com.sofe.gametest;

import com.mojang.authlib.GameProfile;
import com.sofe.SoFEMod;
import com.sofe.economy.EconomyCapability;
import com.sofe.economy.EconomyData;
import com.sofe.entity.army.Army;
import com.sofe.entity.army.SoldierEntity;
import com.sofe.registry.EntityRegistry;
import com.sofe.world.region.Region;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

/** The armies of the empires: hiring a company from a captain, the fallen brought back, soldiers who never turn on a Bearer. */
@GameTestHolder(SoFEMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class ArmyGameTests {

    private static ServerPlayer bearer(GameTestHelper helper, String name) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), name));
        Vec3 at = helper.absoluteVec(new Vec3(1.5, 1, 1.5));
        player.moveTo(at.x, at.y, at.z);
        return player;
    }

    /** A captain hires soldiers and archers for dinars, three at most; the fallen come back for less, or others take their place. */
    @GameTest(template = "empty")
    public static void aCaptainHiresACompanyAndBringsBackTheFallen(GameTestHelper helper) {
        ServerPlayer bearer = bearer(helper, "sofe_test_general");
        EconomyData purse = EconomyCapability.get(bearer).orElseThrow();
        purse.addDinars(300);
        try {
            helper.assertTrue(Army.hire(bearer, Region.SULTHARI, SoldierEntity.Rank.SOLDIER), "the soldier was not hired");
            helper.assertTrue(purse.dinars() == 300 - Army.SOLDIER_PRICE, "wrong price for a soldier: " + purse.dinars());
            List<SoldierEntity> company = Army.standingFor(bearer);
            helper.assertTrue(company.size() == 1 && company.get(0).rank() == SoldierEntity.Rank.SOLDIER
                    && company.get(0).empire() == Region.SULTHARI && bearer.getUUID().equals(company.get(0).owner()), "the hired soldier does not stand with the Bearer");
            helper.assertTrue(Army.hire(bearer, Region.NORDRATH, SoldierEntity.Rank.ARCHER), "the archer was not hired");
            helper.assertTrue(Army.hire(bearer, Region.AUREUM, SoldierEntity.Rank.SOLDIER), "the third was not hired");
            long before = purse.dinars();
            helper.assertFalse(Army.hire(bearer, Region.KHEMET, SoldierEntity.Rank.SOLDIER), "a fourth joined a full company");
            helper.assertTrue(purse.dinars() == before, "a full company still took the dinars");

            SoldierEntity first = Army.standingFor(bearer).stream().filter(s -> s.companySlot() == 0).findFirst().orElseThrow();
            first.discard();
            Army.fell(bearer, 0, Component.literal("test"));
            helper.assertTrue(Army.describe(bearer).get(0).endsWith("*"), "the fallen soldier was not marked: " + Army.describe(bearer));
            helper.assertTrue(Army.revive(bearer), "the fallen was not brought back");
            helper.assertTrue(purse.dinars() == before - Army.REVIVE_PRICE, "wrong price to bring back one: " + purse.dinars());
            helper.assertTrue(Army.describe(bearer).stream().noneMatch(m -> m.endsWith("*")), "still fallen after coming back: " + Army.describe(bearer));
            helper.assertTrue(Army.standingFor(bearer).size() == 3, "the company is not whole again: " + Army.standingFor(bearer).size());

            Army.standingFor(bearer).stream().filter(s -> s.companySlot() == 1).findFirst().orElseThrow().discard();
            Army.fell(bearer, 1, Component.literal("test"));
            helper.assertTrue(Army.hire(bearer, Region.KHEMET, SoldierEntity.Rank.SOLDIER), "no one could take a fallen archer's place");
            helper.assertTrue(Army.describe(bearer).get(1).equals("khemet_soldier"), "the new soldier did not take the fallen's place: " + Army.describe(bearer));
        } finally {
            Army.standingFor(bearer).forEach(SoldierEntity::discard);
            bearer.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).remove("sofe_company");
        }
        helper.succeed();
    }

    /** A soldier never fights a Bearer, and a Bearer's blow does not hurt them; a Void creature is their foe. */
    @GameTest(template = "empty")
    public static void soldiersFightTheVoidAndNeverABearer(GameTestHelper helper) {
        ServerPlayer bearer = bearer(helper, "sofe_test_ally");
        SoldierEntity soldier = helper.spawn(EntityRegistry.SOLDIER.get(), new Vec3(2.5, 1, 2.5));
        soldier.enlist(Region.PARSIVAN, SoldierEntity.Rank.CAPTAIN);
        helper.assertTrue(soldier.getMaxHealth() == SoldierEntity.Rank.CAPTAIN.health, "a captain's health was not set");
        helper.assertTrue(soldier.skinId().equals("parsivan_captain"), "wrong skin: " + soldier.skinId());
        helper.assertFalse(soldier.canAttack(bearer), "a soldier could attack a Bearer");
        float health = soldier.getHealth();
        soldier.hurt(bearer.damageSources().playerAttack(bearer), 5f);
        helper.assertTrue(soldier.getHealth() == health, "a Bearer's blow hurt a soldier");
        @SuppressWarnings("unchecked")
        EntityType<? extends net.minecraft.world.entity.Mob> wretchType = (EntityType<? extends net.minecraft.world.entity.Mob>)
                net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getValue(new net.minecraft.resources.ResourceLocation("sofe:void_wretch"));
        var wretch = helper.spawn(wretchType, new Vec3(4.5, 1, 2.5));
        wretch.setNoAi(true);
        helper.succeedWhen(() -> helper.assertTrue(soldier.getTarget() == wretch, "the soldier did not turn on the Void creature"));
    }

    /** Sulthari's garrisons muster only once the Void's attack on the city is won: during it, only the rifts' soldiers stand. */
    @GameTest(template = "empty")
    public static void sultharisGarrisonsWaitForTheInvasion(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        boolean was = Army.sultharisIsHeld(server);
        Army.forgetSultharisHeld(server);
        int placed = Army.placeGarrisons(server, com.sofe.world.zone.StructurePositions.get(), (x, z) -> Math.abs(x) < 400 && Math.abs(z) < 400);
        helper.assertTrue(placed == 0, "Sulthari's garrisons mustered before the Void was driven out: " + placed);
        if (was) Army.sultharisHeld(server); // the test world as it was
        helper.succeed();
    }
}
