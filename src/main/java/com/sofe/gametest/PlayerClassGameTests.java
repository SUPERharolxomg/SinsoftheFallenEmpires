package com.sofe.gametest;

import com.sofe.SoFEMod;
import com.sofe.player.PlayerClass;
import com.sofe.player.PlayerClassCapability;
import com.sofe.player.PlayerClassData;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Optional;

@GameTestHolder(SoFEMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class PlayerClassGameTests {

    /** Every player gets the Bearer data attached, starting without a class. */
    @GameTest(template = "empty")
    public static void playersGetClassData(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        Optional<PlayerClassData> data = PlayerClassCapability.get(player);
        helper.assertTrue(data.isPresent(), "player has no Bearer data attached");
        helper.assertTrue(!data.get().hasClass(), "a new player should not have a class yet");
        helper.succeed();
    }

    /** The chosen class survives saving and loading the player. */
    @GameTest(template = "empty")
    public static void classIsSavedWithThePlayer(GameTestHelper helper) {
        PlayerClassCapability.Provider saved = new PlayerClassCapability.Provider();
        saved.getCapability(PlayerClassCapability.CAPABILITY, null).ifPresent(d -> d.choose(PlayerClass.NECROMANCER));
        CompoundTag tag = saved.serializeNBT();

        PlayerClassCapability.Provider loaded = new PlayerClassCapability.Provider();
        loaded.deserializeNBT(tag);
        Optional<PlayerClass> result = loaded.getCapability(PlayerClassCapability.CAPABILITY, null).resolve().flatMap(PlayerClassData::get);
        helper.assertTrue(result.equals(Optional.of(PlayerClass.NECROMANCER)), "expected necromancer after reload, got " + result);
        helper.succeed();
    }
}
