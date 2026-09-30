package com.sofe.gametest;

import com.sofe.SoFEMod;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/**
 * Runs in the GameTest server (CI): proves the mod and its required dependencies load together.
 */
@GameTestHolder(SoFEMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class SmokeGameTests {

    @GameTest(template = "empty")
    public static void modAndDependenciesLoad(GameTestHelper helper) {
        ModList mods = ModList.get();
        for (String modId : new String[]{SoFEMod.MOD_ID, "geckolib", "curios"}) {
            if (!mods.isLoaded(modId)) {
                helper.fail("Mod not loaded: " + modId);
                return;
            }
        }
        helper.succeed();
    }
}
