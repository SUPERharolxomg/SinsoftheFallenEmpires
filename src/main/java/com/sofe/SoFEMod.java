package com.sofe;

import com.mojang.logging.LogUtils;
import com.sofe.client.ClientSetup;
import com.sofe.combat.CombatCapability;
import com.sofe.combat.CombatHandler;
import com.sofe.condition.ConditionManager;
import com.sofe.config.SoFEConfig;
import com.sofe.datagen.DataGenerators;
import com.sofe.registry.BlockRegistry;
import com.sofe.registry.CreativeTabRegistry;
import com.sofe.registry.SoFEBlocks;
import com.sofe.death.CorpseHandler;
import com.sofe.travel.TravelCapability;
import com.sofe.travel.WaystoneService;
import com.sofe.world.JourneyRules;
import com.sofe.world.StoryPlacements;
import com.sofe.world.lock.RegionEnforcer;
import com.sofe.world.zone.StructurePositions;
import com.sofe.world.zone.ZoneProtectionHandler;
import com.sofe.registry.EntityRegistry;
import com.sofe.registry.ItemRegistry;
import com.sofe.registry.WorldgenRegistry;
import com.sofe.mob.MobLevels;
import com.sofe.network.SoFENetwork;
import com.sofe.progression.CharacterStats;
import com.sofe.progression.ProgressionCapability;
import com.sofe.progression.ProgressionHandler;
import com.sofe.progression.ProgressionRulesManager;
import com.sofe.quest.DialogueService;
import com.sofe.quest.QuestEngine;
import com.sofe.quest.StoryDataManager;
import com.sofe.story.StoryCapability;
import com.sofe.player.ClassSelectionHandler;
import com.sofe.player.PlayerClassCapability;
import com.sofe.registry.material.MaterialRegistry;
import com.sofe.skill.data.SkillDataManager;
import com.sofe.world.region.RegionTitleHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

/**
 * Entry point of Sins of the Fallen Empires.
 * Registries, config and event handlers are wired from here as each sprint adds them.
 */
@Mod(SoFEMod.MOD_ID)
public class SoFEMod {
    public static final String MOD_ID = "sofe";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SoFEMod(FMLJavaModLoadingContext context) {
        IEventBus modBus = context.getModEventBus();

        SoFEConfig.register(context);

        MaterialRegistry.init();
        SoFEBlocks.init();
        com.sofe.registry.HerbRegistry.init();
        BlockRegistry.BLOCKS.register(modBus);
        ItemRegistry.ITEMS.register(modBus);
        EntityRegistry.ENTITIES.register(modBus);
        CreativeTabRegistry.TABS.register(modBus);
        WorldgenRegistry.BIOME_SOURCES.register(modBus);
        WorldgenRegistry.FEATURES.register(modBus);
        com.sofe.registry.SoFEBlocks.BLOCK_ENTITIES.register(modBus);
        com.sofe.registry.SoFEEffects.EFFECTS.register(modBus);
        com.sofe.travel.Homeward.ENCHANTMENTS.register(modBus);
        com.sofe.registry.SoFERecipes.TYPES.register(modBus);
        com.sofe.registry.SoFERecipes.SERIALIZERS.register(modBus);
        com.sofe.gear.loot.GearLoot.FUNCTIONS.register(modBus);
        com.sofe.gear.loot.GearLoot.MODIFIERS.register(modBus);
        modBus.addListener((net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent e) -> e.enqueueWork(com.sofe.gear.SoFETiers::register));
        modBus.addListener(DataGenerators::gatherData);

        SoFENetwork.register();
        MinecraftForge.EVENT_BUS.addListener(ConditionManager::onAddReloadListeners);
        MinecraftForge.EVENT_BUS.addListener(RegionTitleHandler::onPlayerTick);
        MinecraftForge.EVENT_BUS.addListener(RegionTitleHandler::onLogout);

        modBus.addListener(PlayerClassCapability::register);
        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, PlayerClassCapability::attach);
        MinecraftForge.EVENT_BUS.addListener(PlayerClassCapability::onClone);
        MinecraftForge.EVENT_BUS.addListener(ClassSelectionHandler::onLogin);
        MinecraftForge.EVENT_BUS.addListener(ClassSelectionHandler::onRespawn);
        MinecraftForge.EVENT_BUS.addListener(ClassSelectionHandler::onChangeDimension);

        modBus.addListener(CombatCapability::register);
        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, CombatCapability::attach);
        MinecraftForge.EVENT_BUS.addListener(CombatCapability::onClone);
        MinecraftForge.EVENT_BUS.addListener(SkillDataManager::onAddReloadListeners);
        MinecraftForge.EVENT_BUS.addListener(CombatHandler::onLogin);
        MinecraftForge.EVENT_BUS.addListener(CombatHandler::onRespawn);
        MinecraftForge.EVENT_BUS.addListener(CombatHandler::onDatapackSync);
        MinecraftForge.EVENT_BUS.addListener(CombatHandler::onPlayerTick);

        modBus.addListener(ProgressionCapability::register);
        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, ProgressionCapability::attach);
        MinecraftForge.EVENT_BUS.addListener(ProgressionCapability::onClone);
        MinecraftForge.EVENT_BUS.addListener(ProgressionRulesManager::onAddReloadListeners);
        MinecraftForge.EVENT_BUS.addListener(ProgressionHandler::onLogin);
        MinecraftForge.EVENT_BUS.addListener(ProgressionHandler::onRespawn);
        MinecraftForge.EVENT_BUS.addListener(ProgressionHandler::onKill);
        MinecraftForge.EVENT_BUS.addListener(MobLevels::onJoin);
        MinecraftForge.EVENT_BUS.addListener(CharacterStats::onHurt);
        MinecraftForge.EVENT_BUS.addListener(net.minecraftforge.eventbus.api.EventPriority.LOW, com.sofe.gear.WeaponTraitHandler::onHurt);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.gear.ranged.RangedHandler::onHurt);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.gear.ranged.RangedHandler::onEntityJoin);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.gear.Tools::onBlockBreak);
        MinecraftForge.EVENT_BUS.addListener(net.minecraftforge.eventbus.api.EventPriority.LOWEST, com.sofe.gear.GemMining::onBlockBreak);
        // Sprint 6: the class mechanics, lasting skills and the Royal Treasury
        MinecraftForge.EVENT_BUS.addListener(com.sofe.skill.ClassMechanics::onHurt);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.skill.ClassMechanics::onShieldBlock);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.skill.ClassMechanics::onDeath);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.skill.ClassMechanics::onDrops);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.skill.ClassMechanics::onPlayerTick);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.skill.ClassMechanics::onLogout);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.skill.SkillTasks::onServerTick);
        // Sprint 6: companions and Class Concord
        MinecraftForge.EVENT_BUS.addListener(com.sofe.companion.Companions::onLogin);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.companion.Companions::onLogout);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.companion.Companions::onChangedDimension);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.companion.Companions::onRespawn);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.companion.Companions::onDeath);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.companion.ClassConcord::onPlayerTick);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.companion.ClassConcord::onHurt);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.companion.ClassConcord::onHeal);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.companion.ClassConcord::onLogout);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.skill.king.KingSkills::onPickup);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.gear.WeaponTraitHandler::onBlock);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.gear.ArmorSets::onPlayerTick);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.gear.ArmorSets::onHurt);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.gear.RelicEffects::onPlayerTick);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.gear.RelicEffects::onKill);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.gear.UniqueEffects::onKill);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.gear.UniqueEffects::onPlayerTick);
        MinecraftForge.EVENT_BUS.addListener(net.minecraftforge.eventbus.api.EventPriority.LOW, com.sofe.gear.UniqueEffects::onHurt);
        // last, so the Wrappings of the Undying see the damage after every reduction
        MinecraftForge.EVENT_BUS.addListener(net.minecraftforge.eventbus.api.EventPriority.LOWEST, com.sofe.gear.RelicEffects::onHurt);
        MinecraftForge.EVENT_BUS.addListener(CharacterStats::onAttacked);

        modBus.addListener(StoryCapability::register);
        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, StoryCapability::attach);
        MinecraftForge.EVENT_BUS.addListener(StoryCapability::onClone);
        MinecraftForge.EVENT_BUS.addListener(StoryDataManager::onAddReloadListeners);
        MinecraftForge.EVENT_BUS.addListener(QuestEngine::onLogin);
        MinecraftForge.EVENT_BUS.addListener(QuestEngine::onRespawn);
        MinecraftForge.EVENT_BUS.addListener(QuestEngine::onKill);
        MinecraftForge.EVENT_BUS.addListener(QuestEngine::onPlayerTick);
        MinecraftForge.EVENT_BUS.addListener(DialogueService::onPlayerTick);
        MinecraftForge.EVENT_BUS.addListener(DialogueService::onLogout);

        MinecraftForge.EVENT_BUS.addListener(RegionEnforcer::onPlayerTick);
        MinecraftForge.EVENT_BUS.addListener(RegionEnforcer::onTeleport);
        MinecraftForge.EVENT_BUS.addListener(RegionEnforcer::onLogout);
        MinecraftForge.EVENT_BUS.addListener(StructurePositions::onAddReloadListeners);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.world.lair.BossLairs::onAddReloadListeners);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.world.lair.BossLairs::onServerTick);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.mob.EliteMobs::onSpawn);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.mob.VoidHordes::onServerTick);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.entity.boss.BossKit::onServerTick);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.world.build.RegionHealing::onServerTick);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.economy.ZahirCaravan::onServerTick);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.economy.ZahirCaravan::onJoin);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.entity.boss.ThessynEntity::onRightClickBlock);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.entity.boss.NixaraEntity::onPickup);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.entity.boss.LuxaraEntity::onAttack);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.entity.boss.AvarokEntity::onLogin);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.travel.Homeward::onPlayerTick);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.travel.Homeward::onUseStart);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.mob.EliteMobs::onTick);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.mob.EliteMobs::onHurt);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.mob.EliteMobs::onDrops);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.mob.EliteMobs::onExperience);
        MinecraftForge.EVENT_BUS.addListener(ZoneProtectionHandler::onServerStarted);
        MinecraftForge.EVENT_BUS.addListener(ZoneProtectionHandler::onNeighborNotify);
        MinecraftForge.EVENT_BUS.addListener(ZoneProtectionHandler::onCreateSpawn);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.command.SoFECommands::register);
        MinecraftForge.EVENT_BUS.addListener(ZoneProtectionHandler::onSpawnCheck);
        MinecraftForge.EVENT_BUS.addListener(ZoneProtectionHandler::onBreak);
        MinecraftForge.EVENT_BUS.addListener(ZoneProtectionHandler::onPlace);
        MinecraftForge.EVENT_BUS.addListener(ZoneProtectionHandler::onRightClickBlock);
        MinecraftForge.EVENT_BUS.addListener(ZoneProtectionHandler::onExplosion);
        MinecraftForge.EVENT_BUS.addListener(ZoneProtectionHandler::onPiston);
        MinecraftForge.EVENT_BUS.addListener(ZoneProtectionHandler::onFluid);
        MinecraftForge.EVENT_BUS.addListener(ZoneProtectionHandler::onTrample);
        MinecraftForge.EVENT_BUS.addListener(ZoneProtectionHandler::onMobGriefing);

        modBus.addListener(EntityRegistry::registerAttributes);
        modBus.addListener(EntityRegistry::registerSpawnPlacements);
        modBus.addListener(TravelCapability::register);
        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, TravelCapability::attach);
        MinecraftForge.EVENT_BUS.addListener(TravelCapability::onClone);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOW, CorpseHandler::onDeath);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, com.sofe.death.DeathSpectate::onDeath);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.death.DeathSpectate::onRespawn);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.death.DeathSpectate::onServerTick);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.death.DeathSpectate::onLogout);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.death.DeathSpectate::onServerStarted);
        MinecraftForge.EVENT_BUS.addListener(WaystoneService::onRespawn);
        MinecraftForge.EVENT_BUS.addListener(WaystoneService::onLogout);
        MinecraftForge.EVENT_BUS.addListener(JourneyRules::onEntityJoin);
        MinecraftForge.EVENT_BUS.addListener(JourneyRules::onServerStarted);
        MinecraftForge.EVENT_BUS.addListener(JourneyRules::onServerTick);
        MinecraftForge.EVENT_BUS.addListener(StoryPlacements::onServerStarted);
        MinecraftForge.EVENT_BUS.addListener(StoryPlacements::onServerTick);

        MinecraftForge.EVENT_BUS.addListener(com.sofe.world.lock.BurningDeep::onPortalSpawn);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.world.lock.BurningDeep::onTravel);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.world.lock.BurningDeep::onDrops);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.world.region.RegionTitleHandler::onChangeDimension);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOW, com.sofe.mob.MobTraits::onJoin);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.mob.MobTraits::onHurt);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.mob.MobTraits::onArrow);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.mob.MobTraits::onTick);

        // Sprint 5.5: gear, economy, stations, potions
        MinecraftForge.EVENT_BUS.addListener(com.sofe.gear.GearDataManager::onAddReloadListeners);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.gear.ArmorSets::onAddReloadListeners);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.gear.ArmorSets::onKill);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.economy.MerchantService::onAddReloadListeners);
        modBus.addListener(com.sofe.economy.EconomyCapability::register);
        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, com.sofe.economy.EconomyCapability::attach);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.economy.EconomyCapability::onClone);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.economy.EconomyHandler::onPickup);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.economy.EconomyHandler::onLogin);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.economy.EconomyHandler::onRespawn);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.economy.MerchantService::onLogout);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.crafting.StationService::onLogout);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.gear.PlayerGear::onEquipmentChange);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.gear.PlayerGear::onCurioChange);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.gear.PlayerGear::onLogout);
        MinecraftForge.EVENT_BUS.addListener(CharacterStats::onKill);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.gear.loot.SecondaryDrops::onDrops);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.item.Soulbound::onToss);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.item.Soulbound::onClone);
        MinecraftForge.EVENT_BUS.addListener(ClassSelectionHandler::onStartTracking);

        if (FMLEnvironment.dist.isClient()) {
            ClientSetup.init(modBus, context);
        }
        LOGGER.info("Sins of the Fallen Empires loading");
    }

    /** A key in the sofe namespace, e.g. id("khemet_catacombs") is sofe:khemet_catacombs. */
    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
