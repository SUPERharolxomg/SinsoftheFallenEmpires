package com.sofe.registry;

import com.sofe.SoFEMod;
import com.sofe.entity.BearerCorpseEntity;
import com.sofe.entity.VoidCreature;
import com.sofe.entity.VoidStalker;
import com.sofe.entity.VoidWretch;
import com.sofe.entity.boss.BrassSentinelEntity;
import com.sofe.entity.boss.KalethEntity;
import com.sofe.entity.boss.SerathEntity;
import com.sofe.entity.boss.VorathEntity;
import com.sofe.entity.npc.BearerNpcEntity;
import com.sofe.entity.npc.MerchantNpcEntity;
import com.sofe.entity.npc.StoryNpcEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Void creatures, story NPCs, merchants, the Bearer's corpse and the bosses. */
public final class EntityRegistry {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, SoFEMod.MOD_ID);

    public static final RegistryObject<EntityType<VoidWretch>> VOID_WRETCH = ENTITIES.register("void_wretch",
            () -> EntityType.Builder.of(VoidWretch::new, MobCategory.MONSTER).sized(0.9f, 2.8f).clientTrackingRange(8).build("void_wretch"));
    public static final RegistryObject<EntityType<com.sofe.entity.voidkin.VoidZombie>> VOID_ZOMBIE = ENTITIES.register("void_zombie",
            () -> EntityType.Builder.of(com.sofe.entity.voidkin.VoidZombie::new, MobCategory.MONSTER).sized(0.6f, 1.95f).clientTrackingRange(8).build("void_zombie"));
    public static final RegistryObject<EntityType<com.sofe.entity.voidkin.VoidSkeleton>> VOID_SKELETON = ENTITIES.register("void_skeleton",
            () -> EntityType.Builder.of(com.sofe.entity.voidkin.VoidSkeleton::new, MobCategory.MONSTER).sized(0.6f, 1.99f).clientTrackingRange(8).build("void_skeleton"));
    public static final RegistryObject<EntityType<VoidStalker>> VOID_STALKER = ENTITIES.register("void_stalker",
            () -> EntityType.Builder.of(VoidStalker::new, MobCategory.MONSTER).sized(1.2f, 1.3f).clientTrackingRange(8).build("void_stalker"));

    public static final RegistryObject<EntityType<StoryNpcEntity>> STORY_NPC = ENTITIES.register("story_npc",
            () -> EntityType.Builder.of(StoryNpcEntity::new, MobCategory.MISC).sized(0.6f, 1.95f).clientTrackingRange(10).build("story_npc"));
    public static final RegistryObject<EntityType<BearerNpcEntity>> BEARER_NPC = ENTITIES.register("bearer_npc",
            () -> EntityType.Builder.of(BearerNpcEntity::new, MobCategory.MISC).sized(0.6f, 1.95f).clientTrackingRange(10).build("bearer_npc"));
    public static final RegistryObject<EntityType<com.sofe.entity.npc.CitizenEntity>> CITIZEN = ENTITIES.register("citizen",
            () -> EntityType.Builder.of(com.sofe.entity.npc.CitizenEntity::new, MobCategory.MISC).sized(0.6f, 1.95f).clientTrackingRange(10).build("citizen"));
    public static final RegistryObject<EntityType<MerchantNpcEntity>> MERCHANT = ENTITIES.register("merchant",
            () -> EntityType.Builder.of(MerchantNpcEntity::new, MobCategory.MISC).sized(0.6f, 1.95f).clientTrackingRange(10).build("merchant"));

    // the arsenal, batch 3: what is thrown and cast
    public static final RegistryObject<EntityType<com.sofe.entity.projectile.ThrownWeapon>> THROWN_WEAPON = ENTITIES.register("thrown_weapon",
            () -> EntityType.Builder.<com.sofe.entity.projectile.ThrownWeapon>of(com.sofe.entity.projectile.ThrownWeapon::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f).clientTrackingRange(4).updateInterval(20).build("thrown_weapon"));
    public static final RegistryObject<EntityType<com.sofe.entity.projectile.SpellBolt>> SPELL_BOLT = ENTITIES.register("spell_bolt",
            () -> EntityType.Builder.<com.sofe.entity.projectile.SpellBolt>of(com.sofe.entity.projectile.SpellBolt::new, MobCategory.MISC)
                    .sized(0.3f, 0.3f).clientTrackingRange(6).updateInterval(2).build("spell_bolt"));
    public static final RegistryObject<EntityType<com.sofe.entity.projectile.Bomb>> BOMB = ENTITIES.register("bomb",
            () -> EntityType.Builder.<com.sofe.entity.projectile.Bomb>of(com.sofe.entity.projectile.Bomb::new, MobCategory.MISC)
                    .sized(0.3f, 0.3f).clientTrackingRange(4).updateInterval(10).build("bomb"));

    // Sprint 6: what the Bearers summon
    public static final RegistryObject<EntityType<com.sofe.entity.summon.SummonedAlly>> SUMMONED_ALLY = ENTITIES.register("summoned_ally",
            () -> EntityType.Builder.<com.sofe.entity.summon.SummonedAlly>of(com.sofe.entity.summon.SummonedAlly::new, MobCategory.MISC)
                    .sized(0.6f, 1.95f).clientTrackingRange(10).build("summoned_ally"));
    public static final RegistryObject<EntityType<com.sofe.entity.summon.ClayGolem>> CLAY_GOLEM = ENTITIES.register("clay_golem",
            () -> EntityType.Builder.<com.sofe.entity.summon.ClayGolem>of(com.sofe.entity.summon.ClayGolem::new, MobCategory.MISC)
                    .sized(1.1f, 2.1f).clientTrackingRange(10).build("clay_golem"));
    public static final RegistryObject<EntityType<com.sofe.entity.summon.EmbalmedDead>> EMBALMED_DEAD = ENTITIES.register("embalmed_dead",
            () -> EntityType.Builder.<com.sofe.entity.summon.EmbalmedDead>of(com.sofe.entity.summon.EmbalmedDead::new, MobCategory.MISC)
                    .sized(0.6f, 1.95f).clientTrackingRange(8).build("embalmed_dead"));
    public static final RegistryObject<EntityType<com.sofe.companion.CompanionEntity>> COMPANION = ENTITIES.register("companion",
            () -> EntityType.Builder.<com.sofe.companion.CompanionEntity>of(com.sofe.companion.CompanionEntity::new, MobCategory.MISC)
                    .sized(0.6f, 1.8f).clientTrackingRange(10).build("companion"));
    public static final RegistryObject<EntityType<com.sofe.entity.summon.BronzeCannon>> BRONZE_CANNON = ENTITIES.register("bronze_cannon",
            () -> EntityType.Builder.<com.sofe.entity.summon.BronzeCannon>of(com.sofe.entity.summon.BronzeCannon::new, MobCategory.MISC)
                    .sized(1.2f, 1.0f).clientTrackingRange(10).build("bronze_cannon"));

    public static final RegistryObject<EntityType<BearerCorpseEntity>> BEARER_CORPSE = ENTITIES.register("bearer_corpse",
            () -> EntityType.Builder.<BearerCorpseEntity>of(BearerCorpseEntity::new, MobCategory.MISC).sized(1.8f, 0.5f)
                    .fireImmune().clientTrackingRange(16).build("bearer_corpse"));

    public static final RegistryObject<EntityType<BrassSentinelEntity>> BRASS_SENTINEL = ENTITIES.register("brass_sentinel",
            () -> EntityType.Builder.of(BrassSentinelEntity::new, MobCategory.MONSTER).sized(1.8f, 3.8f).fireImmune()
                    .clientTrackingRange(10).build("brass_sentinel"));

    public static final RegistryObject<EntityType<KalethEntity>> KALETH = ENTITIES.register("kaleth",
            () -> EntityType.Builder.of(KalethEntity::new, MobCategory.MONSTER).sized(1.0f, 2.9f).fireImmune()
                    .clientTrackingRange(10).build("kaleth"));
    public static final RegistryObject<EntityType<SerathEntity>> SERATH = ENTITIES.register("serath",
            () -> EntityType.Builder.of(SerathEntity::new, MobCategory.MONSTER).sized(0.8f, 2.6f).fireImmune()
                    .clientTrackingRange(10).build("serath"));
    public static final RegistryObject<EntityType<VorathEntity>> VORATH = ENTITIES.register("vorath",
            () -> EntityType.Builder.of(VorathEntity::new, MobCategory.MONSTER).sized(2.0f, 4.5f).fireImmune()
                    .clientTrackingRange(12).build("vorath"));

    // <generated-bosses> by scripts/make_boss_data.py: the bosses of Acts III and IV
    public static final RegistryObject<EntityType<com.sofe.entity.boss.MiraelEntity>> MIRAEL = ENTITIES.register("mirael",
            () -> EntityType.Builder.of(com.sofe.entity.boss.MiraelEntity::new, MobCategory.MONSTER).sized(1.0f, 3.2f).fireImmune().clientTrackingRange(10).build("mirael"));
    public static final RegistryObject<EntityType<com.sofe.entity.boss.ThessynEntity>> THESSYN = ENTITIES.register("thessyn",
            () -> EntityType.Builder.of(com.sofe.entity.boss.ThessynEntity::new, MobCategory.MONSTER).sized(1.6f, 3.2f).fireImmune().clientTrackingRange(10).build("thessyn"));
    public static final RegistryObject<EntityType<com.sofe.entity.boss.DormielEntity>> DORMIEL = ENTITIES.register("dormiel",
            () -> EntityType.Builder.of(com.sofe.entity.boss.DormielEntity::new, MobCategory.MONSTER).sized(1.0f, 3.0f).fireImmune().clientTrackingRange(10).build("dormiel"));
    public static final RegistryObject<EntityType<com.sofe.entity.boss.LuxaraEntity>> LUXARA = ENTITIES.register("luxara",
            () -> EntityType.Builder.of(com.sofe.entity.boss.LuxaraEntity::new, MobCategory.MONSTER).sized(1.2f, 3.4f).fireImmune().clientTrackingRange(10).build("luxara"));
    public static final RegistryObject<EntityType<com.sofe.entity.boss.MorthisEntity>> MORTHIS = ENTITIES.register("morthis",
            () -> EntityType.Builder.of(com.sofe.entity.boss.MorthisEntity::new, MobCategory.MONSTER).sized(1.8f, 3.8f).fireImmune().clientTrackingRange(10).build("morthis"));
    public static final RegistryObject<EntityType<com.sofe.entity.boss.GoldarcEntity>> GOLDARC = ENTITIES.register("goldarc",
            () -> EntityType.Builder.of(com.sofe.entity.boss.GoldarcEntity::new, MobCategory.MONSTER).sized(1.2f, 3.3f).fireImmune().clientTrackingRange(10).build("goldarc"));
    public static final RegistryObject<EntityType<com.sofe.entity.boss.NixaraEntity>> NIXARA = ENTITIES.register("nixara",
            () -> EntityType.Builder.of(com.sofe.entity.boss.NixaraEntity::new, MobCategory.MONSTER).sized(1.0f, 3.0f).fireImmune().clientTrackingRange(10).build("nixara"));
    public static final RegistryObject<EntityType<com.sofe.entity.boss.AvarokEntity>> AVAROK = ENTITIES.register("avarok",
            () -> EntityType.Builder.of(com.sofe.entity.boss.AvarokEntity::new, MobCategory.MONSTER).sized(1.6f, 4.0f).fireImmune().clientTrackingRange(10).build("avarok"));
    public static final RegistryObject<EntityType<com.sofe.entity.boss.FenrathEntity>> FENRATH = ENTITIES.register("fenrath",
            () -> EntityType.Builder.of(com.sofe.entity.boss.FenrathEntity::new, MobCategory.MONSTER).sized(2.0f, 3.4f).fireImmune().clientTrackingRange(10).build("fenrath"));
    public static final RegistryObject<EntityType<com.sofe.entity.boss.GularthEntity>> GULARTH = ENTITIES.register("gularth",
            () -> EntityType.Builder.of(com.sofe.entity.boss.GularthEntity::new, MobCategory.MONSTER).sized(2.0f, 3.8f).fireImmune().clientTrackingRange(10).build("gularth"));
    public static final RegistryObject<EntityType<com.sofe.entity.boss.ShadeynEntity>> SHADEYN = ENTITIES.register("shadeyn",
            () -> EntityType.Builder.of(com.sofe.entity.boss.ShadeynEntity::new, MobCategory.MONSTER).sized(1.1f, 3.4f).fireImmune().clientTrackingRange(10).build("shadeyn"));
    public static final RegistryObject<EntityType<com.sofe.entity.boss.SolrathEntity>> SOLRATH = ENTITIES.register("solrath",
            () -> EntityType.Builder.of(com.sofe.entity.boss.SolrathEntity::new, MobCategory.MONSTER).sized(1.1f, 3.4f).fireImmune().clientTrackingRange(10).build("solrath"));
    public static final RegistryObject<EntityType<com.sofe.entity.boss.PrythonEntity>> PRYTHON = ENTITIES.register("prython",
            () -> EntityType.Builder.of(com.sofe.entity.boss.PrythonEntity::new, MobCategory.MONSTER).sized(1.4f, 4.2f).fireImmune().clientTrackingRange(10).build("prython"));
    public static final RegistryObject<EntityType<com.sofe.entity.boss.NahrazelEntity>> NAHRAZEL = ENTITIES.register("nahrazel",
            () -> EntityType.Builder.of(com.sofe.entity.boss.NahrazelEntity::new, MobCategory.MONSTER).sized(2.6f, 6.0f).fireImmune().clientTrackingRange(10).build("nahrazel"));
    public static final RegistryObject<EntityType<com.sofe.entity.boss.EnvyrisEntity>> ENVYRIS = ENTITIES.register("envyris",
            () -> EntityType.Builder.of(com.sofe.entity.boss.EnvyrisEntity::new, MobCategory.MONSTER).sized(1.2f, 3.6f).fireImmune().clientTrackingRange(10).build("envyris"));
    public static final RegistryObject<EntityType<com.sofe.entity.boss.SealGlyph>> SEAL_GLYPH = ENTITIES.register("seal_glyph",
            () -> EntityType.Builder.of(com.sofe.entity.boss.SealGlyph::new, MobCategory.MISC).sized(1.2f, 1.8f).fireImmune().clientTrackingRange(10).build("seal_glyph"));
    public static final RegistryObject<EntityType<com.sofe.entity.boss.EnvyCopy>> ENVY_COPY = ENTITIES.register("envy_copy",
            () -> EntityType.Builder.of(com.sofe.entity.boss.EnvyCopy::new, MobCategory.MONSTER).sized(0.6f, 1.8f).clientTrackingRange(8).build("envy_copy"));
    // </generated-bosses>
    // <generated-empire-mobs> the creatures of each empire (EmpireMob, scripts/make_empire_mobs.py)
    public static final RegistryObject<EntityType<com.sofe.entity.empire.EmpireMob>> SAND_GHOUL = ENTITIES.register("sand_ghoul",
            () -> EntityType.Builder.<com.sofe.entity.empire.EmpireMob>of((t, l) -> new com.sofe.entity.empire.EmpireMob(t, l, com.sofe.entity.empire.EmpireMob.Kind.SAND_GHOUL), MobCategory.MONSTER).sized(0.6f, 1.9f).clientTrackingRange(8).build("sand_ghoul"));
    public static final RegistryObject<EntityType<com.sofe.entity.empire.EmpireMob>> CLOCKWORK_SCARAB = ENTITIES.register("clockwork_scarab",
            () -> EntityType.Builder.<com.sofe.entity.empire.EmpireMob>of((t, l) -> new com.sofe.entity.empire.EmpireMob(t, l, com.sofe.entity.empire.EmpireMob.Kind.CLOCKWORK_SCARAB), MobCategory.MONSTER).sized(1.0f, 0.8f).clientTrackingRange(8).build("clockwork_scarab"));
    public static final RegistryObject<EntityType<com.sofe.entity.empire.EmpireMob>> DRAUGR = ENTITIES.register("draugr",
            () -> EntityType.Builder.<com.sofe.entity.empire.EmpireMob>of((t, l) -> new com.sofe.entity.empire.EmpireMob(t, l, com.sofe.entity.empire.EmpireMob.Kind.DRAUGR), MobCategory.MONSTER).sized(0.8f, 2.3f).clientTrackingRange(8).build("draugr"));
    public static final RegistryObject<EntityType<com.sofe.entity.empire.EmpireMob>> RIME_WOLF = ENTITIES.register("rime_wolf",
            () -> EntityType.Builder.<com.sofe.entity.empire.EmpireMob>of((t, l) -> new com.sofe.entity.empire.EmpireMob(t, l, com.sofe.entity.empire.EmpireMob.Kind.RIME_WOLF), MobCategory.MONSTER).sized(1.1f, 1.2f).clientTrackingRange(8).build("rime_wolf"));
    public static final RegistryObject<EntityType<com.sofe.entity.empire.EmpireMob>> MIRAGE_DANCER = ENTITIES.register("mirage_dancer",
            () -> EntityType.Builder.<com.sofe.entity.empire.EmpireMob>of((t, l) -> new com.sofe.entity.empire.EmpireMob(t, l, com.sofe.entity.empire.EmpireMob.Kind.MIRAGE_DANCER), MobCategory.MONSTER).sized(0.6f, 1.9f).clientTrackingRange(8).build("mirage_dancer"));
    public static final RegistryObject<EntityType<com.sofe.entity.empire.EmpireMob>> BOG_MUMMY = ENTITIES.register("bog_mummy",
            () -> EntityType.Builder.<com.sofe.entity.empire.EmpireMob>of((t, l) -> new com.sofe.entity.empire.EmpireMob(t, l, com.sofe.entity.empire.EmpireMob.Kind.BOG_MUMMY), MobCategory.MONSTER).sized(0.8f, 2.3f).clientTrackingRange(8).build("bog_mummy"));
    public static final RegistryObject<EntityType<com.sofe.entity.empire.EmpireMob>> GILDED_LEGIONNAIRE = ENTITIES.register("gilded_legionnaire",
            () -> EntityType.Builder.<com.sofe.entity.empire.EmpireMob>of((t, l) -> new com.sofe.entity.empire.EmpireMob(t, l, com.sofe.entity.empire.EmpireMob.Kind.GILDED_LEGIONNAIRE), MobCategory.MONSTER).sized(0.7f, 2.05f).clientTrackingRange(8).build("gilded_legionnaire"));
    public static final RegistryObject<EntityType<com.sofe.entity.empire.EmpireMob>> GLADIATOR_SHADE = ENTITIES.register("gladiator_shade",
            () -> EntityType.Builder.<com.sofe.entity.empire.EmpireMob>of((t, l) -> new com.sofe.entity.empire.EmpireMob(t, l, com.sofe.entity.empire.EmpireMob.Kind.GLADIATOR_SHADE), MobCategory.MONSTER).sized(0.6f, 1.9f).clientTrackingRange(8).build("gladiator_shade"));

    public static java.util.List<RegistryObject<EntityType<com.sofe.entity.empire.EmpireMob>>> empireMobs() {
        return java.util.List.of(SAND_GHOUL, CLOCKWORK_SCARAB, DRAUGR, RIME_WOLF, MIRAGE_DANCER, BOG_MUMMY, GILDED_LEGIONNAIRE, GLADIATOR_SHADE);
    }
    // </generated-empire-mobs>

    private EntityRegistry() {
    }

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(VOID_WRETCH.get(), VoidWretch.attributes().build());
        event.put(VOID_STALKER.get(), VoidStalker.attributes().build());
        event.put(VOID_ZOMBIE.get(), com.sofe.entity.voidkin.VoidZombie.attributes().build());
        event.put(VOID_SKELETON.get(), com.sofe.entity.voidkin.VoidSkeleton.attributes().build());
        event.put(STORY_NPC.get(), StoryNpcEntity.attributes().build());
        event.put(BEARER_NPC.get(), StoryNpcEntity.attributes().build());
        event.put(MERCHANT.get(), StoryNpcEntity.attributes().build());
        event.put(CITIZEN.get(), com.sofe.entity.npc.CitizenEntity.attributes().build());
        event.put(BRASS_SENTINEL.get(), BrassSentinelEntity.attributes().build());
        event.put(KALETH.get(), KalethEntity.attributes().build());
        event.put(SERATH.get(), SerathEntity.attributes().build());
        event.put(VORATH.get(), VorathEntity.attributes().build());
        // <generated-boss-attributes>
        event.put(MIRAEL.get(), com.sofe.entity.boss.MiraelEntity.attributes().build());
        event.put(THESSYN.get(), com.sofe.entity.boss.ThessynEntity.attributes().build());
        event.put(DORMIEL.get(), com.sofe.entity.boss.DormielEntity.attributes().build());
        event.put(LUXARA.get(), com.sofe.entity.boss.LuxaraEntity.attributes().build());
        event.put(MORTHIS.get(), com.sofe.entity.boss.MorthisEntity.attributes().build());
        event.put(GOLDARC.get(), com.sofe.entity.boss.GoldarcEntity.attributes().build());
        event.put(NIXARA.get(), com.sofe.entity.boss.NixaraEntity.attributes().build());
        event.put(AVAROK.get(), com.sofe.entity.boss.AvarokEntity.attributes().build());
        event.put(FENRATH.get(), com.sofe.entity.boss.FenrathEntity.attributes().build());
        event.put(GULARTH.get(), com.sofe.entity.boss.GularthEntity.attributes().build());
        event.put(SHADEYN.get(), com.sofe.entity.boss.ShadeynEntity.attributes().build());
        event.put(SOLRATH.get(), com.sofe.entity.boss.SolrathEntity.attributes().build());
        event.put(PRYTHON.get(), com.sofe.entity.boss.PrythonEntity.attributes().build());
        event.put(NAHRAZEL.get(), com.sofe.entity.boss.NahrazelEntity.attributes().build());
        event.put(ENVYRIS.get(), com.sofe.entity.boss.EnvyrisEntity.attributes().build());
        event.put(ENVY_COPY.get(), com.sofe.entity.boss.EnvyCopy.attributes().build());
        event.put(SEAL_GLYPH.get(), com.sofe.entity.boss.SealGlyph.attributes().build());
        // </generated-boss-attributes>
        event.put(SUMMONED_ALLY.get(), com.sofe.entity.summon.SummonedAlly.createAttributes().build());
        event.put(BRONZE_CANNON.get(), com.sofe.entity.summon.BronzeCannon.createAttributes().build());
        event.put(CLAY_GOLEM.get(), net.minecraft.world.entity.animal.IronGolem.createAttributes().build());
        event.put(EMBALMED_DEAD.get(), net.minecraft.world.entity.monster.Zombie.createAttributes().build());
        event.put(COMPANION.get(), com.sofe.companion.CompanionEntity.createAttributes().build());
        for (var mob : empireMobs()) event.put(mob.get(), com.sofe.entity.empire.EmpireMob.Kind.valueOf(mob.getId().getPath().toUpperCase(java.util.Locale.ROOT)).attributes().build());
    }

    public static void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
        event.register(VOID_WRETCH.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                VoidCreature::checkSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(VOID_STALKER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                VoidCreature::checkSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(VOID_ZOMBIE.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(VOID_SKELETON.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        for (var mob : empireMobs()) {
            event.register(mob.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        }
    }
}
