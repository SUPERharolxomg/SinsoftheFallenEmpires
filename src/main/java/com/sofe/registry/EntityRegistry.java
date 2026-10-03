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
            () -> EntityType.Builder.of(VoidWretch::new, MobCategory.MONSTER).sized(0.6f, 1.95f).clientTrackingRange(8).build("void_wretch"));
    public static final RegistryObject<EntityType<VoidStalker>> VOID_STALKER = ENTITIES.register("void_stalker",
            () -> EntityType.Builder.of(VoidStalker::new, MobCategory.MONSTER).sized(0.6f, 1.6f).clientTrackingRange(8).build("void_stalker"));

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
            () -> EntityType.Builder.of(BrassSentinelEntity::new, MobCategory.MONSTER).sized(1.4f, 2.9f).fireImmune()
                    .clientTrackingRange(10).build("brass_sentinel"));

    public static final RegistryObject<EntityType<KalethEntity>> KALETH = ENTITIES.register("kaleth",
            () -> EntityType.Builder.of(KalethEntity::new, MobCategory.MONSTER).sized(0.8f, 2.4f).fireImmune()
                    .clientTrackingRange(10).build("kaleth"));
    public static final RegistryObject<EntityType<SerathEntity>> SERATH = ENTITIES.register("serath",
            () -> EntityType.Builder.of(SerathEntity::new, MobCategory.MONSTER).sized(0.7f, 2.3f).fireImmune()
                    .clientTrackingRange(10).build("serath"));
    public static final RegistryObject<EntityType<VorathEntity>> VORATH = ENTITIES.register("vorath",
            () -> EntityType.Builder.of(VorathEntity::new, MobCategory.MONSTER).sized(1.6f, 3.6f).fireImmune()
                    .clientTrackingRange(12).build("vorath"));

    private EntityRegistry() {
    }

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(VOID_WRETCH.get(), VoidWretch.attributes().build());
        event.put(VOID_STALKER.get(), VoidStalker.attributes().build());
        event.put(STORY_NPC.get(), StoryNpcEntity.attributes().build());
        event.put(BEARER_NPC.get(), StoryNpcEntity.attributes().build());
        event.put(MERCHANT.get(), StoryNpcEntity.attributes().build());
        event.put(CITIZEN.get(), com.sofe.entity.npc.CitizenEntity.attributes().build());
        event.put(BRASS_SENTINEL.get(), BrassSentinelEntity.attributes().build());
        event.put(KALETH.get(), KalethEntity.attributes().build());
        event.put(SERATH.get(), SerathEntity.attributes().build());
        event.put(VORATH.get(), VorathEntity.attributes().build());
        event.put(SUMMONED_ALLY.get(), com.sofe.entity.summon.SummonedAlly.createAttributes().build());
        event.put(BRONZE_CANNON.get(), com.sofe.entity.summon.BronzeCannon.createAttributes().build());
        event.put(CLAY_GOLEM.get(), net.minecraft.world.entity.animal.IronGolem.createAttributes().build());
        event.put(EMBALMED_DEAD.get(), net.minecraft.world.entity.monster.Zombie.createAttributes().build());
        event.put(COMPANION.get(), com.sofe.companion.CompanionEntity.createAttributes().build());
    }

    public static void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
        event.register(VOID_WRETCH.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                VoidCreature::checkSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(VOID_STALKER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                VoidCreature::checkSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
    }
}
