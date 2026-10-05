package com.sofe.gametest;

import com.mojang.authlib.GameProfile;
import com.sofe.SoFEMod;
import com.sofe.entity.boss.KalethEntity;
import com.sofe.story.StoryCapability;
import com.sofe.world.lair.BossLairs;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

/** Sprint 7: the bosses waiting in their lairs, and the places of Acts III and IV. */
@GameTestHolder(SoFEMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class Sprint7GameTests {

    private static ServerPlayer bearer(GameTestHelper helper, String name) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), name));
        Vec3 at = helper.absoluteVec(new Vec3(2.5, 1, 2.5));
        player.moveTo(at.x, at.y, at.z, 0, 0);
        return player;
    }

    private static BossLairs.Lair lairHere(GameTestHelper helper, String boss) {
        BlockPos middle = helper.absolutePos(new BlockPos(2, 1, 2));
        return new BossLairs.Lair(boss, middle.getX(), middle.getZ(), 6);
    }

    @GameTest(template = "empty", batch = "sofe_lairs")
    public static void kalethRisesForABearerWhoHasNotBeatenHim(GameTestHelper helper) {
        BossLairs.forget();
        ServerPlayer player = bearer(helper, "sofe_test_lair_a");
        // other tests fight Kaleth nearby; a living Kaleth near the lair rightly keeps it from raising another
        helper.getLevel().getEntities(com.sofe.registry.EntityRegistry.KALETH.get(), Entity::isAlive).forEach(Entity::discard);
        var raised = BossLairs.wake(helper.getLevel(), lairHere(helper, KalethEntity.BOSS_ID), List.of(player));
        helper.assertTrue(raised.isPresent() && raised.get() instanceof KalethEntity, "Kaleth did not rise in his lair");
        var again = BossLairs.wake(helper.getLevel(), lairHere(helper, KalethEntity.BOSS_ID), List.of(player));
        helper.assertTrue(again.isEmpty(), "a second Kaleth rose while the first one lives");
        raised.ifPresent(Entity::discard);
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "sofe_lairs")
    public static void anEmptyArenaForThoseWhoBeatTheBoss(GameTestHelper helper) {
        BossLairs.forget();
        ServerPlayer player = bearer(helper, "sofe_test_lair_b");
        StoryCapability.get(player).orElseThrow().defeat(KalethEntity.BOSS_ID);
        var raised = BossLairs.wake(helper.getLevel(), lairHere(helper, KalethEntity.BOSS_ID), List.of(player));
        helper.assertTrue(raised.isEmpty(), "Kaleth rose for a Bearer who already beat him");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "sofe_lairs")
    public static void aLairOfABossNotYetMadeStaysEmpty(GameTestHelper helper) {
        BossLairs.forget();
        ServerPlayer player = bearer(helper, "sofe_test_lair_c");
        var raised = BossLairs.wake(helper.getLevel(), lairHere(helper, "sofe:no_such_boss"), List.of(player));
        helper.assertTrue(raised.isEmpty(), "an unknown boss raised something (the registry's default pig?)");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void eachPieceOfAnubisGivesItsOwnBonusAndTheyAddUp(GameTestHelper helper) {
        ServerPlayer player = bearer(helper, "sofe_test_anubis");
        var fire = player.damageSources().inFire();
        helper.assertTrue(com.sofe.gear.ArmorSets.resistance(player, fire) == 0, "no armor, yet fire is resisted");
        player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, new net.minecraft.world.item.ItemStack(com.sofe.registry.ItemRegistry.ANUBIS_CHESTPLATE.get()));
        helper.assertTrue(Math.abs(com.sofe.gear.ArmorSets.resistance(player, fire) - 0.05) < 1e-6, "the chestplate of Anubis should resist 5% of fire");
        player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.FEET, new net.minecraft.world.item.ItemStack(com.sofe.registry.ItemRegistry.ANUBIS_BOOTS.get()));
        helper.assertTrue(Math.abs(com.sofe.gear.ArmorSets.resistance(player, fire) - 0.10) < 1e-6, "chestplate and boots should resist 10% of fire");
        helper.assertTrue(com.sofe.gear.ArmorSets.resistance(player, player.damageSources().fall()) == 0, "Anubis does not soften falls");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void aClassUniqueWeaponRollsRanksInARandomSkillOfItsClass(GameTestHelper helper) {
        java.util.Set<String> seen = new java.util.HashSet<>();
        for (int i = 0; i < 30; i++) {
            var stack = com.sofe.gear.GearMaker.relic("anubets_judgement", null).orElseThrow();
            var rolls = com.sofe.gear.GearNbt.read(stack).orElseThrow().affixes().stream()
                    .filter(r -> r.stat() == com.sofe.gear.GearStat.SKILL_RANKS).toList();
            helper.assertTrue(rolls.size() == 1, "Anubet's Final Judgement should roll one skill bonus");
            var skill = com.sofe.skill.SkillCatalog.byId(rolls.get(0).parameter()).orElseThrow();
            helper.assertTrue(skill.owner() == com.sofe.player.PlayerClass.NECROMANCER && !skill.isUpgrade(), "the bonus is not a Necromancer skill: " + skill.id());
            helper.assertTrue(rolls.get(0).value() >= 1 && rolls.get(0).value() <= 2, "the bonus should be +1 or +2");
            seen.add(skill.id());
        }
        helper.assertTrue(seen.size() > 1, "thirty copies all rolled the same skill");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void anEliteIsStrongerAndLeavesItsTreasure(GameTestHelper helper) {
        var zombie = helper.spawnWithNoFreeWill(net.minecraft.world.entity.EntityType.ZOMBIE, 2, 2, 2);
        float plain = zombie.getMaxHealth();
        com.sofe.mob.EliteMobs.make(zombie, 2, helper.getLevel().getRandom());
        helper.assertTrue(com.sofe.mob.EliteMobs.isElite(zombie), "the zombie is not an elite");
        helper.assertTrue(zombie.getMaxHealth() >= plain * 2.4f, "an elite should have two and a half times the health: " + plain + " -> " + zombie.getMaxHealth());
        helper.assertTrue(zombie.hasCustomName() && !com.sofe.mob.EliteMobs.traits(zombie).isEmpty(), "an elite has a name and its traits");
        ServerPlayer player = bearer(helper, "sofe_test_elite");
        com.sofe.player.PlayerClassCapability.get(player).orElseThrow().set(com.sofe.player.PlayerClass.KNIGHT);
        com.sofe.progression.ProgressionCapability.get(player).orElseThrow().load(30, 0, 0, 0, true);
        zombie.hurt(player.damageSources().playerAttack(player), 10_000);
        helper.runAfterDelay(5, () -> {
            var drops = helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                    new net.minecraft.world.phys.AABB(helper.absolutePos(new BlockPos(2, 2, 2))).inflate(4));
            helper.assertTrue(drops.size() >= 2, "an elite should leave its treasure, left " + drops.size() + " items");
            drops.forEach(Entity::discard);
            helper.succeed();
        });
    }

    @GameTest(template = "empty", batch = "sofe_hordes")
    public static void aVoidHordeNeverPassesThirty(GameTestHelper helper) {
        ServerPlayer player = bearer(helper, "sofe_test_horde");
        var first = com.sofe.mob.VoidHordes.spawnWave(helper.getLevel(), player, 20);
        var second = com.sofe.mob.VoidHordes.spawnWave(helper.getLevel(), player, 20);
        helper.assertTrue(!first.isEmpty(), "no Void creature came");
        helper.assertTrue(first.size() + second.size() <= com.sofe.mob.VoidHordes.MAX, "the horde passed thirty: " + (first.size() + second.size()));
        helper.assertTrue(first.stream().allMatch(m -> m.getTarget() == player), "the horde does not hunt the Bearer");
        first.forEach(Entity::discard);
        second.forEach(Entity::discard);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void everyEmpireCreatureStandsWithItsNumbers(GameTestHelper helper) {
        for (var type : com.sofe.registry.EntityRegistry.empireMobs()) {
            var mob = helper.spawnWithNoFreeWill(type.get(), 2, 2, 2);
            var kind = mob.kind();
            helper.assertTrue(mob.getMaxHealth() >= kind.health, kind.id() + " has too little health: " + mob.getMaxHealth());
            helper.assertTrue(mob.modelName().equals(type.getId().getPath()), kind.id() + " is drawn with another model");
            mob.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void theLegionnairesScutumTakesHalfOfABlowFromTheFront(GameTestHelper helper) {
        var front = helper.spawnWithNoFreeWill(com.sofe.registry.EntityRegistry.GILDED_LEGIONNAIRE.get(), 2, 2, 2);
        var back = helper.spawnWithNoFreeWill(com.sofe.registry.EntityRegistry.GILDED_LEGIONNAIRE.get(), 6, 2, 2);
        front.setYRot(0);
        front.setYBodyRot(0);
        back.setYRot(180);
        back.setYBodyRot(180);
        var hitterA = helper.spawnWithNoFreeWill(net.minecraft.world.entity.EntityType.ZOMBIE, 2, 2, 4); // south of the first: in front of it
        var hitterB = helper.spawnWithNoFreeWill(net.minecraft.world.entity.EntityType.ZOMBIE, 6, 2, 4); // south of the second: behind it
        float a0 = front.getHealth(), b0 = back.getHealth();
        front.hurt(helper.getLevel().damageSources().mobAttack(hitterA), 10);
        back.hurt(helper.getLevel().damageSources().mobAttack(hitterB), 10);
        float tookFront = a0 - front.getHealth(), tookBack = b0 - back.getHealth();
        helper.assertTrue(tookFront < tookBack, "the scutum did not take the blow from the front: " + tookFront + " vs " + tookBack);
        helper.succeed();
    }

    // --- the bosses of Acts III and IV

    @GameTest(template = "empty", batch = "sofe_bosses")
    public static void everyBossOfActsThreeAndFourStandsAndHasALair(GameTestHelper helper) {
        for (var lair : BossLairs.lairs()) {
            var id = net.minecraft.resources.ResourceLocation.tryParse(lair.boss());
            helper.assertTrue(net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.containsKey(id), lair.boss() + " has a lair but no creature");
        }
        for (var type : java.util.List.of(com.sofe.registry.EntityRegistry.MIRAEL, com.sofe.registry.EntityRegistry.THESSYN,
                com.sofe.registry.EntityRegistry.DORMIEL, com.sofe.registry.EntityRegistry.LUXARA, com.sofe.registry.EntityRegistry.MORTHIS,
                com.sofe.registry.EntityRegistry.GOLDARC, com.sofe.registry.EntityRegistry.NIXARA, com.sofe.registry.EntityRegistry.AVAROK,
                com.sofe.registry.EntityRegistry.FENRATH, com.sofe.registry.EntityRegistry.GULARTH, com.sofe.registry.EntityRegistry.SHADEYN,
                com.sofe.registry.EntityRegistry.ENVYRIS)) {
            var boss = helper.spawnWithNoFreeWill(type.get(), 2, 2, 2);
            helper.assertTrue(boss.bossId().equals("sofe:" + type.getId().getPath()), "wrong boss id for " + type.getId());
            helper.assertTrue(boss.getMaxHealth() >= 240, type.getId() + " is too frail: " + boss.getMaxHealth());
            helper.assertTrue(BossLairs.lair(boss.bossId()).isPresent(), type.getId() + " has no lair");
            boss.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "sofe_bosses")
    public static void avarokStealsButNeverTheFlaskOrARelicAndGivesItBack(GameTestHelper helper) {
        ServerPlayer player = bearer(helper, "sofe_test_avarok");
        var avarok = helper.spawnWithNoFreeWill(com.sofe.registry.EntityRegistry.AVAROK.get(), 2, 2, 2);
        player.getInventory().items.set(5, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND, 7));
        player.getInventory().items.set(6, new net.minecraft.world.item.ItemStack(com.sofe.registry.ItemRegistry.BEARERS_FLASK.get()));
        player.getInventory().items.set(7, com.sofe.gear.GearMaker.relic("hoard_of_avarok", null).orElseThrow());
        player.getInventory().selected = 0;
        helper.assertTrue(avarok.steal(helper.getLevel(), player), "Avarok stole nothing");
        helper.assertTrue(player.getInventory().items.get(5).isEmpty(), "the diamonds are still there");
        helper.assertTrue(!player.getInventory().items.get(6).isEmpty() && !player.getInventory().items.get(7).isEmpty(),
                "Avarok took the Flask or a Relic");
        helper.assertTrue(!avarok.steal(helper.getLevel(), player), "there was nothing left he may take");
        var hoard = com.sofe.entity.boss.AvarokEntity.Hoard.get(helper.getLevel().getServer());
        helper.assertTrue(hoard.of(player.getUUID()).size() == 1, "the hoard does not hold the diamonds");
        hoard.returnTo(player);
        helper.assertTrue(player.getInventory().countItem(net.minecraft.world.item.Items.DIAMOND) == 7, "the diamonds did not come back");
        avarok.discard();
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "sofe_bosses")
    public static void gularthEatsTheFloorGrowsAndTheFloorComesBack(GameTestHelper helper) {
        var gularth = helper.spawnWithNoFreeWill(com.sofe.registry.EntityRegistry.GULARTH.get(), 1, 2, 1);
        BlockPos floor = helper.absolutePos(new BlockPos(4, 1, 4));
        helper.getLevel().setBlock(floor, net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(), 3);
        float before = gularth.renderScale();
        gularth.devour(helper.getLevel(), floor);
        helper.assertTrue(helper.getLevel().getBlockState(floor).isAir(), "the floor was not eaten");
        helper.assertTrue(gularth.renderScale() > before, "Gularth did not grow");
        com.sofe.entity.boss.BossKit.restore(gularth);
        helper.assertTrue(helper.getLevel().getBlockState(floor).is(net.minecraft.world.level.block.Blocks.STONE), "the floor did not come back");
        gularth.discard();
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "sofe_bosses")
    public static void envyrisCopiesTheThreeWhoHurtHerMost(GameTestHelper helper) {
        var envyris = helper.spawnWithNoFreeWill(com.sofe.registry.EntityRegistry.ENVYRIS.get(), 2, 2, 2);
        java.util.List<ServerPlayer> four = new java.util.ArrayList<>();
        for (int i = 0; i < 4; i++) {
            ServerPlayer p = bearer(helper, "sofe_test_envy_" + i);
            four.add(p);
            envyris.invulnerableTime = 0;
            envyris.hurt(envyris.damageSources().playerAttack(p), 2 + i * 3);
        }
        var copied = envyris.copied(four);
        helper.assertTrue(copied.size() == 3, "she should copy three, copied " + copied.size());
        helper.assertTrue(!copied.contains(four.get(0)), "she copied the one who hurt her least");
        envyris.discard();
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "sofe_bosses")
    public static void theOneFenrathSwallowsCannotStrikeHim(GameTestHelper helper) {
        ServerPlayer player = bearer(helper, "sofe_test_fenrath");
        var fenrath = helper.spawnWithNoFreeWill(com.sofe.registry.EntityRegistry.FENRATH.get(), 2, 2, 2);
        fenrath.swallow(helper.getLevel(), player);
        float before = fenrath.getHealth();
        fenrath.hurt(fenrath.damageSources().playerAttack(player), 20);
        helper.assertTrue(fenrath.getHealth() == before, "the swallowed Bearer hurt him from inside");
        fenrath.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void theEasternShadowsRunFromParsivanToTheSultansDiary(GameTestHelper helper) {
        ServerPlayer player = bearer(helper, "sofe_test_act3");
        com.sofe.player.PlayerClassCapability.get(player).orElseThrow().set(com.sofe.player.PlayerClass.SORCERESS);
        var story = StoryCapability.get(player).orElseThrow();
        story.advanceTo(3);
        String quest = "sofe:act3_east";
        com.sofe.quest.QuestEngine.run(player, java.util.List.of(new com.sofe.quest.QuestEffect.StartQuest(quest)));
        helper.assertTrue(story.questStep(quest) == 1, "Act III did not start");
        com.sofe.quest.QuestEngine.event(player, new com.sofe.quest.QuestEvent.EnteredRegion("parsivan"));
        for (String boss : java.util.List.of("mirael", "thessyn", "luxara")) com.sofe.quest.QuestEngine.bossDefeated(player, "sofe:" + boss);
        helper.assertTrue(story.questStep(quest) == 5, "after Luxara the quest should send the Bearer to Khemet, is at " + story.questStep(quest));
        com.sofe.quest.QuestEngine.event(player, new com.sofe.quest.QuestEvent.EnteredRegion("khemet"));
        com.sofe.quest.QuestEngine.bossDefeated(player, "sofe:dormiel");
        com.sofe.quest.QuestEngine.bossDefeated(player, "sofe:morthis");
        helper.assertTrue(story.questStep(quest) == Integer.MAX_VALUE, "Act III is not complete");
        helper.assertTrue(story.act() == 4, "Act IV did not begin, act " + story.act());
        helper.succeed();
    }

    // --- Act V

    @GameTest(template = "empty", batch = "sofe_act5")
    public static void anEchoIsWeakerAndLeavesNothing(GameTestHelper helper) {
        var kaleth = helper.spawnWithNoFreeWill(com.sofe.registry.EntityRegistry.KALETH.get(), 2, 2, 2);
        float full = kaleth.getMaxHealth();
        kaleth.makeEcho();
        helper.assertTrue(kaleth.isEcho(), "Kaleth is not an echo");
        helper.assertTrue(kaleth.getMaxHealth() <= full / 2.9f, "an echo should have a third of the health: " + full + " -> " + kaleth.getMaxHealth());
        kaleth.hurt(kaleth.damageSources().genericKill(), Float.MAX_VALUE);
        helper.runAfterDelay(5, () -> {
            var drops = helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                    new net.minecraft.world.phys.AABB(helper.absolutePos(new BlockPos(2, 2, 2))).inflate(4));
            helper.assertTrue(drops.isEmpty(), "an echo left loot: " + drops.size());
            helper.succeed();
        });
    }

    @GameTest(template = "empty", batch = "sofe_act5", timeoutTicks = 200)
    public static void insideTheCodexNahrazelIsHeldUntilTheSealsBreak(GameTestHelper helper) {
        var nahrazel = helper.spawn(com.sofe.registry.EntityRegistry.NAHRAZEL.get(), new net.minecraft.world.phys.Vec3(2.5, 2, 2.5));
        nahrazel.enterPhase(helper.getLevel(), 3, List.of());
        helper.assertTrue(nahrazel.form() == 3, "Nahrazel did not take his third form");
        long seals = nahrazel.sealsStanding(helper.getLevel());
        helper.assertTrue(seals == com.sofe.entity.boss.NahrazelEntity.SEALS, "the seven Seals did not rise: " + seals);
        // after the pause of the phase change (TRANSITION_TICKS) he can be struck: the Seals hold most of it
        helper.runAfterDelay(com.sofe.entity.boss.SoFEBossEntity.TRANSITION_TICKS + 5, () -> {
            float before = nahrazel.getHealth();
            nahrazel.invulnerableTime = 0;
            nahrazel.hurt(nahrazel.damageSources().magic(), 100);
            float held = before - nahrazel.getHealth();
            helper.assertTrue(held > 0 && held <= 100 * com.sofe.entity.boss.NahrazelEntity.SEALED_DAMAGE + 0.01f, "the Seals did not hold him: took " + held);
            helper.getLevel().getEntitiesOfClass(com.sofe.entity.boss.SealGlyph.class, nahrazel.getBoundingBox().inflate(40)).forEach(g -> g.kill());
            helper.runAfterDelay(5, () -> {
                float now = nahrazel.getHealth();
                nahrazel.invulnerableTime = 0;
                nahrazel.hurt(nahrazel.damageSources().magic(), 100);
                float free = now - nahrazel.getHealth();
                helper.assertTrue(free > held * 3, "with the Seals broken he should take the blow: " + free + " vs " + held);
                com.sofe.entity.boss.BossKit.restore(nahrazel);
                nahrazel.discard();
                helper.succeed();
            });
        });
    }

    // --- signature attacks

    @GameTest(template = "empty", batch = "sofe_signature")
    public static void everyBossHasASignatureBlowThatHurtsWhoeverItCatches(GameTestHelper helper) {
        // Forge's fake players take no damage at all (and a new player is shielded while it spawns); this one takes every blow
        ServerPlayer player = new net.minecraftforge.common.util.FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "sofe_test_signature")) {
            @Override
            public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
                setHealth(getHealth() - amount);
                return true;
            }
        };
        helper.getLevel().getServer().setDifficulty(net.minecraft.world.Difficulty.NORMAL, true);
        for (var type : java.util.List.of(com.sofe.registry.EntityRegistry.KALETH, com.sofe.registry.EntityRegistry.VORATH,
                com.sofe.registry.EntityRegistry.ENVYRIS, com.sofe.registry.EntityRegistry.BRASS_SENTINEL)) {
            var boss = (com.sofe.entity.boss.SoFEBossEntity) helper.spawnWithNoFreeWill(type.get(), 2, 2, 2);
            boss.setYRot(0);
            player.setHealth(player.getMaxHealth());
            player.invulnerableTime = 0;
            player.moveTo(boss.getX(), boss.getY(), boss.getZ() + 2, 180, 0);   // just before it (yaw 0 faces +z)
            player.setOnGround(true);                                           // the Sentinel's shockwave runs along the floor
            float before = player.getHealth();
            helper.assertTrue(boss.signatureNow(helper.getLevel(), player, java.util.List.of(player)), type.getId() + " has no signature attack");
            helper.assertTrue(player.getHealth() < before, type.getId() + "'s signature blow did not hurt the Bearer before it");
            boss.discard();
        }
        player.setHealth(player.getMaxHealth());
        helper.succeed();
    }
}
