package com.sofe.gametest;

import com.mojang.authlib.GameProfile;
import com.sofe.SoFEMod;
import com.sofe.combat.CombatCapability;
import com.sofe.combat.CombatHandler;
import com.sofe.entity.summon.EmbalmedDead;
import com.sofe.player.PlayerClass;
import com.sofe.player.PlayerClassCapability;
import com.sofe.progression.ProgressionCapability;
import com.sofe.skill.SkillCaster;
import com.sofe.skill.SkillCatalog;
import com.sofe.skill.Upgrades;
import com.sofe.skill.data.SkillDataManager;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** The 30-skill trees: upgrades change the skills they improve, and the Necromancer raises his Embalmed Dead. */
@GameTestHolder(SoFEMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class SkillTreeGameTests {

    private static ServerPlayer bearer(GameTestHelper helper, PlayerClass cls, Map<String, Integer> ranks, String slot0) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "sofe_test_tree"));
        Vec3 at = helper.absoluteVec(new Vec3(2.5, 1, 2.5));
        player.moveTo(at.x, at.y, at.z, 0, 0);
        PlayerClassCapability.get(player).orElseThrow().set(cls);
        var progress = ProgressionCapability.get(player).orElseThrow();
        progress.load(100, 0, 0, 0, true);
        List<String> slots = new ArrayList<>(Collections.nCopies(6, ""));
        slots.set(0, slot0);
        progress.skills().load(ranks, slots);
        CombatHandler.refresh(player);
        CombatCapability.get(player).orElseThrow().resource().ifPresent(r -> r.set(r.max()));
        return player;
    }

    @GameTest(template = "empty")
    public static void anUpgradeRaisesTheNumbersOfTheSkillItImproves(GameTestHelper helper) {
        var data = SkillDataManager.forClass(PlayerClass.KNIGHT).orElseThrow();
        var strike = data.skill("scale_strike").orElseThrow();
        var without = Upgrades.apply("scale_strike", strike, id -> 0);
        var with = Upgrades.apply("scale_strike", strike, id -> id.equals("weighted_blade") ? 5 : 0);
        helper.assertTrue(with.param("damage", 0) > without.param("damage", 0) * 1.5, "Weighted Blade at rank 5 should add 60% damage");
        var charge = data.skill("legionary_charge").orElseThrow();
        var relentless = Upgrades.apply("legionary_charge", charge, id -> id.equals("relentless_advance") ? 5 : 0);
        helper.assertTrue(relentless.cooldownTicks() < charge.cooldownTicks(), "Relentless Advance did not cut the cooldown");
        helper.assertTrue(SkillCatalog.upgradesOf("scale_strike").size() >= 3, "Scale Strike should have its upgrades");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void theNecromancerRaisesEmbalmedDeadThatGrowWithHisUpgrades(GameTestHelper helper) {
        ServerPlayer necro = bearer(helper, PlayerClass.NECROMANCER,
                Map.of("clay_warden", 1, "raise_the_embalmed", 1, "embalming_salts", 4), "raise_the_embalmed");
        SkillCaster.cast(necro, 0);
        List<EmbalmedDead> raised = helper.getLevel().getEntitiesOfClass(EmbalmedDead.class, new AABB(necro.blockPosition()).inflate(8));
        helper.assertTrue(raised.size() == 3, "Embalming Salts rank 4 should raise 3 dead, raised " + raised.size());
        helper.assertTrue(raised.get(0).getMaxHealth() >= 16 + 6 * 4, "the Embalmed should have 40 health, have " + raised.get(0).getMaxHealth());
        helper.assertTrue(raised.stream().allMatch(d -> necro.getUUID().equals(d.owner()) && d.isAlliedTo(necro)), "the Embalmed are not his");
        raised.forEach(EmbalmedDead::discard);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void eachStandingWardenMakesTheNextSlowerToShape(GameTestHelper helper) {
        ServerPlayer necro = bearer(helper, PlayerClass.NECROMANCER, Map.of("clay_warden", 1), "clay_warden");
        var combat = com.sofe.combat.CombatCapability.get(necro).orElseThrow();
        combat.setSouls(10);
        SkillCaster.cast(necro, 0);
        var wardens = helper.getLevel().getEntitiesOfClass(com.sofe.entity.summon.ClayGolem.class, new AABB(necro.blockPosition()).inflate(8));
        helper.assertTrue(wardens.size() == 1, "no Clay Warden was shaped (souls left " + combat.souls() + ")");
        long now = necro.level().getGameTime();
        long first = combat.cooldowns().remaining("clay_warden", now);
        combat.cooldowns().start("clay_warden", now, 0);
        SkillCaster.cast(necro, 0);
        long second = combat.cooldowns().remaining("clay_warden", now);
        helper.assertTrue(second > first, "the second Warden's cooldown (" + second + ") should be longer than the first's (" + first + ")");
        helper.getLevel().getEntitiesOfClass(com.sofe.entity.summon.ClayGolem.class, new AABB(necro.blockPosition()).inflate(8)).forEach(g -> g.discard());
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void aClassUniqueServesOnlyItsOwnClass(GameTestHelper helper) {
        ServerPlayer knight = bearer(helper, PlayerClass.KNIGHT, Map.of(), "");
        var crown = com.sofe.gear.GearMaker.relic("crown_of_the_scale", null).orElseThrow();
        knight.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, crown);
        com.sofe.gear.PlayerGear.invalidate(knight);
        helper.assertTrue(com.sofe.gear.PlayerGear.bonuses(knight).bonusRanks("scale_strike", "knight") >= 1, "the Knight got no ranks from the Crown of the Scale");
        PlayerClassCapability.get(knight).orElseThrow().set(PlayerClass.SORCERESS);
        com.sofe.gear.PlayerGear.invalidate(knight);
        helper.assertTrue(com.sofe.gear.PlayerGear.get(knight, com.sofe.gear.GearStat.ALL_ATTRIBUTES) == 0, "a Sorceress drew stats from a Knight unique");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void dropsOfferOnlyUniquesTheBearerCanUse(GameTestHelper helper) {
        var all = com.sofe.gear.GearDataManager.droppableRelics();
        helper.assertTrue(all.size() >= 150, "there should be at least 150 droppable uniques, there are " + all.size());
        var forSorceress = com.sofe.gear.GearDataManager.droppableRelicsFor("sorceress");
        helper.assertTrue(forSorceress.stream().noneMatch(r -> "knight".equals(r.playerClass())), "a Knight unique was offered to a Sorceress");
        helper.assertTrue(forSorceress.stream().anyMatch(r -> "sorceress".equals(r.playerClass())), "no Sorceress unique was offered");
        helper.succeed();
    }
}
