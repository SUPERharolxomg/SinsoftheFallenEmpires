package com.sofe;

import com.mojang.logging.LogUtils;
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
        LOGGER.info("Sins of the Fallen Empires loading");
    }
}
