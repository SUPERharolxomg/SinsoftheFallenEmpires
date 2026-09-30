package com.sofe;

import com.mojang.logging.LogUtils;
import com.sofe.condition.ConditionManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

/**
 * Entry point of Sins of the Fallen Empires.
 * Registries, config and event handlers are wired from here as each sprint adds them.
 */
@Mod(SoFEMod.MOD_ID)
public class SoFEMod {
    public static final String MOD_ID = "sofe";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SoFEMod() {
        MinecraftForge.EVENT_BUS.addListener(ConditionManager::onAddReloadListeners);
        LOGGER.info("Sins of the Fallen Empires loading");
    }

    /** A key in the sofe namespace, e.g. id("khemet_catacombs") is sofe:khemet_catacombs. */
    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
