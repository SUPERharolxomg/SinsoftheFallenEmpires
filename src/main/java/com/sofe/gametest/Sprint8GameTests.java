package com.sofe.gametest;

import com.mojang.authlib.GameProfile;
import com.sofe.SoFEMod;
import com.sofe.condition.ConditionManager;
import com.sofe.entity.boss.KalethEntity;
import com.sofe.item.Soulbound;
import com.sofe.item.StoryItems;
import com.sofe.quest.DialogueService;
import com.sofe.quest.QuestEngine;
import com.sofe.quest.SceneService;
import com.sofe.registry.EntityRegistry;
import com.sofe.registry.ItemRegistry;
import com.sofe.story.PlayerProgressView;
import com.sofe.story.StoryCapability;
import com.sofe.story.StoryProgress;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

/** Sprint 8: the Sealing Quill and the Inverted Throne, Prython's offer and "Crowned in Ash", bosses against many Bearers. */
@GameTestHolder(SoFEMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class Sprint8GameTests {
    private static final String OFFER = "sofe:act5/prythons_offer";

    private static ServerPlayer bearer(GameTestHelper helper, String name) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), name));
        Vec3 at = helper.absoluteVec(new Vec3(2.5, 1, 2.5));
        player.moveTo(at.x, at.y, at.z, 0, 0);
        return player;
    }

    /** A fake player that takes every blow (Forge's own take none). */
    private static ServerPlayer target(GameTestHelper helper, String name) {
        return new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), name)) {
            @Override
            public boolean hurt(DamageSource source, float amount) {
                setHealth(getHealth() - amount);
                return true;
            }
        };
    }

    private static StoryProgress story(ServerPlayer player) {
        return StoryCapability.get(player).orElseThrow();
    }

    @GameTest(template = "empty")
    public static void theInvertedThroneOpensOnlyWithTheSealingQuill(GameTestHelper helper) {
        ServerPlayer player = bearer(helper, "sofe_test_throne");
        var gate = ConditionManager.get(SoFEMod.id("inverted_throne")).orElseThrow();
        story(player).defeat("sofe:prython");
        helper.assertFalse(gate.test(PlayerProgressView.of(player)), "the Throne opened without the Sealing Quill");
        player.getInventory().add(new ItemStack(ItemRegistry.SEALING_QUILL.get()));
        helper.assertTrue(gate.test(PlayerProgressView.of(player)), "the Throne stayed shut with Prython beaten and the Quill carried");
        helper.assertTrue(ItemRegistry.SEALING_QUILL.get().getDefaultInstance().is(Soulbound.SOULBOUND), "the Quill should be soulbound");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void carryingTheQuillMovesTheAscensionToNahrazel(GameTestHelper helper) {
        ServerPlayer player = bearer(helper, "sofe_test_quill");
        story(player).start(StoryItems.ASCENSION);
        story(player).update(StoryItems.ASCENSION, new StoryProgress.QuestState(StoryItems.quillStep(), 0, false));
        QuestEngine.checkCarried(player);
        helper.assertTrue(story(player).questStep(StoryItems.ASCENSION) == StoryItems.quillStep() + 1, "the step moved without the Quill");
        player.getInventory().add(new ItemStack(ItemRegistry.SEALING_QUILL.get()));
        QuestEngine.checkCarried(player);
        helper.assertTrue(story(player).questStep(StoryItems.ASCENSION) == StoryItems.quillStep() + 2,
                "carrying the Quill should finish its step, at " + story(player).questStep(StoryItems.ASCENSION));
        helper.assertTrue(DialogueService.current(player).map(c -> c.startsWith("sofe:act5/the_quill_forged#")).orElse(false),
                "the Nahrazel step should open with the Quill's scene: " + DialogueService.current(player));
        DialogueService.close(player);
        player.getInventory().clearContent(); // lost: the Council gives it back
        helper.assertTrue(StoryItems.restore(player).stream().anyMatch(s -> s.is(ItemRegistry.SEALING_QUILL.get())), "the Council did not give back the Quill");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void takingPrythonsCrownPlaysCrownedInAshAndAsksAgain(GameTestHelper helper) {
        ServerPlayer player = bearer(helper, "sofe_test_crowned");
        DialogueService.open(player, OFFER, null);
        helper.assertTrue(SceneService.shielded(player), "a Bearer weighing the offer should be held out of the fight");
        helper.assertFalse(ForgeHooks.onPlayerAttack(player, player.damageSources().mobAttack(EntityType.ZOMBIE.create(helper.getLevel())), 4),
                "a creature's blow landed on a Bearer weighing the offer");
        DialogueService.answer(player, 0, 1); // (Take the crown.)
        helper.assertTrue(SceneService.watching(player).map("crowned_in_ash"::equals).orElse(false), "Crowned in Ash did not play");
        // Forge grants no advancement to a fake player: check the advancement loads and the answer gives it
        helper.assertTrue(player.server.getAdvancements().getAdvancement(SoFEMod.id("secret/crowned_in_ash")) != null, "the hidden advancement did not load");
        helper.assertTrue(com.sofe.quest.StoryDataManager.dialogue(OFFER).orElseThrow().lines().get(0).answers().get(1).effects()
                .contains(new com.sofe.quest.QuestEffect.AwardAdvancement("secret/crowned_in_ash")), "taking the crown does not give the hidden advancement");
        helper.assertTrue(SceneService.shielded(player), "a Bearer in the scene should not be hurt");
        SceneService.done(player);
        helper.assertTrue(SceneService.watching(player).isEmpty(), "the scene did not end");
        helper.assertTrue(DialogueService.current(player).map(c -> c.startsWith(OFFER + "#")).orElse(false),
                "after the scene the offer should come again: " + DialogueService.current(player));
        DialogueService.answer(player, 0, 0); // I refuse.
        helper.assertTrue(!DialogueService.isTalking(player) && SceneService.watching(player).isEmpty(), "refusing should simply end the offer");
        helper.assertFalse(SceneService.shielded(player), "the Bearer is still shielded after refusing");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "sofe_signature")
    public static void withMoreBearersTheSignatureMarksTheOthersToo(GameTestHelper helper) {
        helper.getLevel().getServer().setDifficulty(net.minecraft.world.Difficulty.NORMAL, true);
        KalethEntity kaleth = (KalethEntity) helper.spawnWithNoFreeWill(EntityRegistry.KALETH.get(), 4, 2, 4);
        kaleth.setYRot(0);
        ServerPlayer front = target(helper, "sofe_test_front"), behind = target(helper, "sofe_test_behind");
        front.moveTo(kaleth.getX(), kaleth.getY(), kaleth.getZ() + 2, 180, 0);   // in the Cleave of Embers (yaw 0 faces +z)
        behind.moveTo(kaleth.getX(), kaleth.getY(), kaleth.getZ() - 7, 0, 0);    // far behind it, out of its reach
        float before = behind.getHealth();
        kaleth.signatureNow(helper.getLevel(), front, List.of(front));
        helper.assertTrue(behind.getHealth() == before, "alone in the fight, nothing should reach the one behind");
        kaleth.signatureNow(helper.getLevel(), front, List.of(front, behind));
        helper.assertTrue(behind.getHealth() < before, "with two Bearers, the signature's echo should fall on the one behind too");
        helper.assertTrue(kaleth.signatureEchoes().isEmpty(), "the echoes' marks should be spent after the blow");
        kaleth.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void actFiveZombiesAreCorruptedAndOnlyThem(GameTestHelper helper) {
        var level = helper.getLevel();
        var five = EntityType.ZOMBIE.create(level);
        com.sofe.mob.MobTraits.apply(five, 5);
        var four = EntityType.ZOMBIE.create(level);
        com.sofe.mob.MobTraits.apply(four, 4);
        var voidZombie = EntityRegistry.VOID_ZOMBIE.get().create(level);
        com.sofe.mob.MobTraits.apply(voidZombie, 5);
        helper.assertTrue(com.sofe.mob.MobTraits.corrupted(five), "an Act V zombie should be corrupted");
        helper.assertFalse(com.sofe.mob.MobTraits.corrupted(four), "an Act IV zombie should not be corrupted");
        helper.assertFalse(com.sofe.mob.MobTraits.corrupted(voidZombie), "the Void Zombie keeps its own look");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void aetheriumGearIsMadeFromOrichalcumAetheriumAndTheVoid(GameTestHelper helper) {
        var recipes = helper.getLevel().getRecipeManager();
        var crystal = ItemRegistry.VOID_CRYSTAL.get();
        for (String tool : List.of("aetherium_pickaxe", "aetherium_axe", "aetherium_shovel", "aetherium_hoe", "aetherium_mining_hammer")) {
            var recipe = recipes.byKey(SoFEMod.id(tool));
            helper.assertTrue(recipe.isPresent(), "no recipe for " + tool);
            var needs = recipe.get().getIngredients();
            helper.assertTrue(needs.stream().anyMatch(in -> in.test(new ItemStack(crystal))), tool + " should need a Void Crystal");
            helper.assertTrue(needs.stream().anyMatch(in -> in.test(com.sofe.registry.material.MaterialRegistry.item(
                    com.sofe.registry.material.Material.ORICHALCUM, com.sofe.registry.material.MaterialForm.INGOT).getDefaultInstance())), tool + " should need Orichalcum");
        }
        for (String set : List.of("aetherium_aegis", "aetherium_requiem", "aetherium_astrolabe", "aetherium_shade", "aetherium_dominion")) {
            for (String piece : List.of("helmet", "chestplate", "leggings", "boots")) {
                helper.assertTrue(recipes.byKey(SoFEMod.id("forge/" + set + "_" + piece)).isPresent(), "the Imperial Forge cannot make " + set + "_" + piece);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void beatingNahrazelPlaysTheEndingThenTheCouncilSpeaks(GameTestHelper helper) {
        ServerPlayer player = bearer(helper, "sofe_test_ending");
        story(player).start(StoryItems.ASCENSION);
        story(player).update(StoryItems.ASCENSION, new StoryProgress.QuestState(StoryItems.quillStep() + 1, 0, false));
        QuestEngine.bossDefeated(player, "sofe:nahrazel");
        helper.assertTrue(story(player).quest(StoryItems.ASCENSION).map(StoryProgress.QuestState::completed).orElse(false), "the Ascension did not end");
        helper.assertTrue(story(player).finishedCampaign(), "beating Nahrazel should finish the campaign");
        helper.assertTrue(SceneService.watching(player).map("the_ending"::equals).orElse(false), "the ending did not play: " + SceneService.watching(player));
        SceneService.done(player);
        helper.assertTrue(DialogueService.current(player).map(c -> c.startsWith("sofe:act5/after_the_ending#")).orElse(false),
                "after the ending the Council should speak: " + DialogueService.current(player));
        DialogueService.close(player);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void aRelicIsGivenOnceAndNeverAgain(GameTestHelper helper) {
        ServerPlayer player = bearer(helper, "sofe_test_relic");
        var relic = com.sofe.gear.GearMaker.relic("vorath_wrath", player).orElseThrow();
        var gem = new ItemStack(ItemRegistry.VOID_CRYSTAL.get());
        var first = com.sofe.entity.boss.SoFEBossEntity.onlyNewRelics(player, List.of(relic.copy(), gem.copy()));
        helper.assertTrue(first.size() == 2, "the first fight should give the Relic");
        var again = com.sofe.entity.boss.SoFEBossEntity.onlyNewRelics(player, List.of(relic.copy(), gem.copy()));
        helper.assertTrue(again.size() == 1 && again.get(0).is(ItemRegistry.VOID_CRYSTAL.get()), "an Echo fight gave the same Relic again");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void kneelingInALairCallsItsEchoAfterTheCampaign(GameTestHelper helper) {
        com.sofe.world.lair.BossLairs.forget();
        Vec3 middle = helper.absoluteVec(new Vec3(4.5, 2, 4.5));
        var lair = new com.sofe.world.lair.BossLairs.Lair("sofe:prython", (int) Math.floor(middle.x), (int) Math.floor(middle.z), 12);
        ServerPlayer player = bearer(helper, "sofe_test_kneel");
        player.moveTo(middle.x, middle.y, middle.z, 0, 0);
        player.setPose(net.minecraft.world.entity.Pose.CROUCHING);
        story(player).defeat("sofe:prython"); // a boss no other test raises, so none stands nearby
        var players = List.of(player);
        for (int i = 0; i < com.sofe.world.lair.BossLairs.ECHO_KNEEL_CHECKS; i++) {
            helper.assertTrue(com.sofe.world.lair.BossLairs.kneel(helper.getLevel(), lair, players).isEmpty(), "an Echo rose before the campaign was over");
        }
        story(player).defeat("sofe:nahrazel");
        java.util.Optional<net.minecraft.world.entity.Entity> echo = java.util.Optional.empty();
        for (int i = 0; i < com.sofe.world.lair.BossLairs.ECHO_KNEEL_CHECKS && echo.isEmpty(); i++) {
            echo = com.sofe.world.lair.BossLairs.kneel(helper.getLevel(), lair, players);
        }
        helper.assertTrue(echo.isPresent() && echo.get() instanceof com.sofe.entity.boss.PrythonEntity, "kneeling should call Prython's Echo");
        helper.assertFalse(((com.sofe.entity.boss.SoFEBossEntity) echo.get()).isEcho(), "a called Echo is a whole fight, with its rewards");
        echo.get().discard();
        com.sofe.world.lair.BossLairs.forget();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void theIntegrityCheckPassesAWholeJourneyAndCatchesAMissingMap(GameTestHelper helper) {
        var whole = com.sofe.world.FreeMode.check(com.sofe.world.region.RegionMap.defaultLayout());
        helper.assertTrue(whole.isEmpty(), "a whole journey should pass the integrity check: " + whole.map(c -> c.getString()).orElse(""));
        var empty = com.sofe.world.FreeMode.check(new com.sofe.world.region.RegionMap(List.of()));
        helper.assertTrue(empty.isPresent(), "a journey with no map of Aetheris should go to free mode");
        helper.assertTrue(com.sofe.world.FreeMode.incompatibleInstalled().isEmpty(), "no mod of the pack should be on the incompatible list");
        helper.succeed();
    }

    private static com.google.gson.JsonObject lang(String code) {
        try (var in = SoFEMod.class.getResourceAsStream("/assets/sofe/lang/" + code + ".json")) {
            return com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @GameTest(template = "empty")
    public static void everythingTheModRegistersHasANameInBothLanguages(GameTestHelper helper) {
        List<String> missing = new java.util.ArrayList<>();
        for (String code : List.of("en_us", "es_es")) {
            var lang = lang(code);
            java.util.function.Consumer<String> need = key -> { if (!lang.has(key)) missing.add(code + ": " + key); };
            net.minecraftforge.registries.ForgeRegistries.ITEMS.getEntries().stream().filter(e -> e.getKey().location().getNamespace().equals(SoFEMod.MOD_ID))
                    .forEach(e -> need.accept(e.getValue().getDescriptionId()));
            net.minecraftforge.registries.ForgeRegistries.BLOCKS.getEntries().stream().filter(e -> e.getKey().location().getNamespace().equals(SoFEMod.MOD_ID))
                    .filter(e -> e.getValue().asItem() != net.minecraft.world.item.Items.AIR)
                    .forEach(e -> need.accept(e.getValue().getDescriptionId()));
            net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getEntries().stream().filter(e -> e.getKey().location().getNamespace().equals(SoFEMod.MOD_ID))
                    .forEach(e -> {
                        need.accept(e.getValue().getDescriptionId());
                        if (e.getValue().create(helper.getLevel()) instanceof com.sofe.entity.boss.SoFEBossEntity boss) {
                            boss.signatureId().ifPresent(id -> need.accept("signature.sofe." + id));
                            boss.discard();
                        }
                    });
        }
        helper.assertTrue(missing.isEmpty(), missing.size() + " names missing: " + String.join(", ", missing.subList(0, Math.min(12, missing.size()))));
        helper.succeed();
    }
}
