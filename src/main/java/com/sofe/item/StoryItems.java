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
 * Bearer's Flask, a Codex Shard for every Archsin they have beaten and the Sealing Quill once its step of the Act V
 * quest is behind them, from the story's own credit, never from the items. What the Bearer still carries is not given
 * twice.
 */
public final class StoryItems {
    /** Which Archsin's fall gives which Shard (Prython's Pride Shard; Nahrazel gives none). */
    public static final Map<String, Sin> SHARDS = Map.of("sofe:vorath", Sin.WRATH, "sofe:luxara", Sin.LUST, "sofe:morthis", Sin.SLOTH,
            "sofe:gularth", Sin.GLUTTONY, "sofe:avarok", Sin.GREED, "sofe:envyris", Sin.ENVY, "sofe:prython", Sin.PRIDE);

    /** The Act V quest. */
    public static final String ASCENSION = "sofe:act5_ascension";

    /** The step of the Act V quest (from 0) that asks for the Sealing Quill: found in the quest, as steps come before it. */
    public static int quillStep() {
        return com.sofe.quest.StoryDataManager.quest(ASCENSION).map(q -> {
            for (int i = 0; i < q.steps().size(); i++) {
                if (q.steps().get(i).objective() instanceof com.sofe.quest.Objective.Obtain) return i;
            }
            return -1;
        }).orElse(-1);
    }

    private StoryItems() {
    }

    /** Whether the Bearer forged the Sealing Quill: the quest has gone past the step that asked for it. */
    public static boolean owedQuill(com.sofe.story.StoryProgress story) {
        int quill = quillStep();
        return story.quest(ASCENSION).map(s -> s.completed() || quill >= 0 && s.step() > quill).orElse(false);
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
        boolean hasFlask = false, hasQuill = false;
        Set<Sin> carried = EnumSet.noneOf(Sin.class);
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (s.is(ItemRegistry.BEARERS_FLASK.get())) hasFlask = true;
            if (s.is(ItemRegistry.SEALING_QUILL.get())) hasQuill = true;
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
        if (!hasQuill && StoryCapability.get(player).map(StoryItems::owedQuill).orElse(false)) {
            given.add(new ItemStack(ItemRegistry.SEALING_QUILL.get()));
        }
        for (ItemStack s : given) {
            ItemStack copy = s.copy();
            if (!player.getInventory().add(copy)) player.drop(copy, false);
        }
        return given;
    }
}
