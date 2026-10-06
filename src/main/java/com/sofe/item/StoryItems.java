package com.sofe.item;

import com.sofe.gear.GearNbt;
import com.sofe.registry.ItemRegistry;
import com.sofe.story.Sin;
import com.sofe.story.StoryCapability;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Council of Sulthari gives back the story items a Bearer has lost (docs/Anexos.md, A6, "Recovery"; UC-21): the
 * Bearer's Flask, and a Codex Shard for every Archsin they have beaten, from the story's own credit, never from the
 * items. What the Bearer still carries is not given twice.
 */
public final class StoryItems {
    /** Which Archsin's fall gives which Shard (Prython's Pride Shard; Nahrazel gives none). */
    public static final Map<String, Sin> SHARDS = Map.of("sofe:vorath", Sin.WRATH, "sofe:luxara", Sin.LUST, "sofe:morthis", Sin.SLOTH,
            "sofe:gularth", Sin.GLUTTONY, "sofe:avarok", Sin.GREED, "sofe:envyris", Sin.ENVY, "sofe:prython", Sin.PRIDE);

    private StoryItems() {
    }

    /** The Shards a Bearer is owed by their victories. */
    public static Set<Sin> owedShards(Set<String> defeated) {
        Set<Sin> out = EnumSet.noneOf(Sin.class);
        SHARDS.forEach((boss, sin) -> {
            if (defeated.contains(boss)) out.add(sin);
        });
        return out;
    }

    /** Gives back what is missing; returns what was given. */
    public static List<ItemStack> restore(ServerPlayer player) {
        List<ItemStack> given = new ArrayList<>();
        boolean hasFlask = false;
        Set<Sin> carried = EnumSet.noneOf(Sin.class);
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (s.is(ItemRegistry.BEARERS_FLASK.get())) hasFlask = true;
            if (s.is(ItemRegistry.CODEX_SHARD.get())) CodexShardItem.sin(s).ifPresent(carried::add);
        }
        if (!hasFlask) {
            ItemStack flask = new ItemStack(ItemRegistry.BEARERS_FLASK.get());
            GearNbt.bind(flask, player);
            given.add(flask);
        }
        Set<String> defeated = StoryCapability.get(player).map(p -> p.bosses()).orElse(Set.of());
        for (Sin sin : owedShards(defeated)) {
            if (!carried.contains(sin)) given.add(CodexShardItem.of(sin));
        }
        for (ItemStack s : given) {
            ItemStack copy = s.copy();
            if (!player.getInventory().add(copy)) player.drop(copy, false);
        }
        return given;
    }
}
