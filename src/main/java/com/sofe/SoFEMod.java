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
import com.sofe.registry.EntityRegistry;
import com.sofe.registry.ItemRegistry;
import com.sofe.registry.WorldgenRegistry;
import com.sofe.mob.MobLevels;
import com.sofe.network.SoFENetwork;
import com.sofe.progression.ProgressionCapability;
import com.sofe.progression.ProgressionHandler;
import com.sofe.progression.ProgressionRulesManager;
import com.sofe.player.ClassSelectionHandler;
import com.sofe.player.PlayerClassCapability;
import com.sofe.registry.material.MaterialRegistry;
import com.sofe.skill.data.SkillDataManager;
import com.sofe.world.region.RegionTitleHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
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
        BlockRegistry.BLOCKS.register(modBus);
        ItemRegistry.ITEMS.register(modBus);
        EntityRegistry.ENTITIES.register(modBus);
        CreativeTabRegistry.TABS.register(modBus);
        WorldgenRegistry.BIOME_SOURCES.register(modBus);
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
