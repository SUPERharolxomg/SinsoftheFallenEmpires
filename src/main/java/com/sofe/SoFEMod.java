package com.sofe;

import com.mojang.logging.LogUtils;
import com.sofe.client.ClientSetup;
import com.sofe.condition.ConditionManager;
import com.sofe.config.SoFEConfig;
import com.sofe.datagen.DataGenerators;
import com.sofe.registry.BlockRegistry;
import com.sofe.registry.CreativeTabRegistry;
import com.sofe.registry.EntityRegistry;
import com.sofe.registry.ItemRegistry;
import com.sofe.registry.material.MaterialRegistry;
import net.minecraft.resources.ResourceLocation;
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
        modBus.addListener(DataGenerators::gatherData);

        MinecraftForge.EVENT_BUS.addListener(ConditionManager::onAddReloadListeners);

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
