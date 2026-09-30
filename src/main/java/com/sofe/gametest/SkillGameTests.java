package com.sofe.gametest;

import com.sofe.SoFEMod;
import com.sofe.combat.CombatCapability;
import com.sofe.combat.CombatData;
import com.sofe.combat.CombatHandler;
import com.sofe.combat.ResourcePool;
import com.sofe.combat.Rune;
import com.sofe.player.PlayerClass;
import com.sofe.player.PlayerClassCapability;
import com.sofe.skill.SkillCaster;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.phys.Vec3;
import com.mojang.authlib.GameProfile;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

/** The whole cast pipeline on a real server: class, resource, cooldown, effect, runes and constellations. */
@GameTestHolder(SoFEMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class SkillGameTests {

    private record Setup(ServerPlayer sorceress, Zombie target, CombatData combat) {
        float mana() {
            return combat.resource().map(ResourcePool::current).orElse(-1f);
        }
    }

    /** A Sorceress facing a still zombie four blocks away. */
    private static Setup setup(GameTestHelper helper) {
        // A Forge FakePlayer: a real ServerPlayer without a network connection (the vanilla mock
        // server player needs a netty channel that GameTests do not have)
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "sofe_test_sorceress"));
        Vec3 at = helper.absoluteVec(new Vec3(1.5, 1, 1.5));
        player.setNoGravity(true);
        player.moveTo(at.x, at.y, at.z, 0, 0);

        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new Vec3(1.5, 1, 5.5));
        zombie.setNoAi(true);
        zombie.setNoGravity(true);
        player.lookAt(EntityAnchorArgument.Anchor.EYES, zombie.position().add(0, zombie.getBbHeight() / 2, 0));

        PlayerClassCapability.get(player).orElseThrow().choose(PlayerClass.SORCERESS);
        CombatHandler.refresh(player);
        return new Setup(player, zombie, CombatCapability.get(player).orElseThrow());
    }

    @GameTest(template = "empty")
    public static void emberVerseHitsSpendsManaAndLeavesAFireRune(GameTestHelper helper) {
        Setup s = setup(helper);
        float health = s.target().getHealth();
        float mana = s.mana();
        helper.assertTrue(mana == 100, "the Sorceress should start with 100 mana, has " + mana);

        SkillCaster.cast(s.sorceress(), 0);
        helper.assertTrue(s.target().getHealth() < health, "Ember Verse did not hurt the zombie");
        helper.assertTrue(s.target().isOnFire(), "Ember Verse did not set the zombie on fire");
        helper.assertTrue(s.mana() == mana - 8, "Ember Verse should cost 8 mana, mana is now " + s.mana());
        helper.assertTrue(s.combat().runes().current().equals(List.of(Rune.FIRE)), "expected one Fire rune, got " + s.combat().runes().current());

        SkillCaster.cast(s.sorceress(), 0);
        helper.assertTrue(s.mana() == mana - 8, "a second cast during the cooldown must not spend mana");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void noCastWithoutEnoughMana(GameTestHelper helper) {
        Setup s = setup(helper);
        s.combat().resource().orElseThrow().set(5);
        float health = s.target().getHealth();

        SkillCaster.cast(s.sorceress(), 0); // costs 8
        helper.assertTrue(s.target().getHealth() == health, "a spell cast without mana hurt the zombie");
        helper.assertTrue(s.mana() == 5, "mana changed on a refused cast");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void threeDifferentRunesMakeASteamBurst(GameTestHelper helper) {
        Setup s = setup(helper);
        s.target().setHealth(s.target().getMaxHealth());
        s.target().getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(200);
        s.target().setHealth(200);

        for (int slot = 0; slot < 3; slot++) {
            s.combat().cooldowns().clear();
            SkillCaster.cast(s.sorceress(), slot);
        }
        helper.assertTrue(s.combat().runes().current().isEmpty(), "runes should clear after the constellation");
        MobEffectInstance slow = s.target().getEffect(MobEffects.MOVEMENT_SLOWDOWN);
        helper.assertTrue(slow != null && slow.getAmplifier() >= 9, "Steam Burst should stun (strong slowness), got " + slow);
        helper.succeed();
    }
}
