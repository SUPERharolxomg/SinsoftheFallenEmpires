package com.sofe.gametest;

import com.sofe.SoFEMod;
import com.sofe.condition.Condition;
import com.sofe.condition.ConditionManager;
import com.sofe.condition.ProgressView;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Optional;

@GameTestHolder(SoFEMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class ConditionGameTests {

    /** The datapack condition file is found by the reload listener and parsed. */
    @GameTest(template = "empty")
    public static void conditionsLoadFromTheDatapack(GameTestHelper helper) {
        Optional<Condition> condition = ConditionManager.get(SoFEMod.id("khemet_catacombs"));
        if (condition.isEmpty()) {
            helper.fail("sofe:khemet_catacombs was not loaded");
            return;
        }
        ProgressView actThreeWithLuxara = new ProgressView() {
            @Override public int act() { return 3; }
            @Override public boolean hasDefeated(String bossId) { return bossId.equals("sofe:luxara"); }
            @Override public int questStep(String questId) { return 0; }
            @Override public int countItem(String itemId) { return 0; }
        };
        helper.assertTrue(condition.get().test(actThreeWithLuxara), "Act III with Luxara defeated should open the Catacombs");
        helper.succeed();
    }
}
