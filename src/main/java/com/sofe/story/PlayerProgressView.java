package com.sofe.story;

import com.sofe.condition.ProgressView;
import com.sofe.player.PlayerClass;
import com.sofe.player.PlayerClassCapability;
import com.sofe.player.PlayerClassData;
import com.sofe.world.region.Region;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

/** What conditions see of a real player: their story progress, class and inventory. */
public final class PlayerProgressView implements ProgressView {
    private final Player player;
    private final StoryProgress story;

    private PlayerProgressView(Player player, StoryProgress story) {
        this.player = player;
        this.story = story;
    }

    public static PlayerProgressView of(Player player) {
        return new PlayerProgressView(player, StoryCapability.get(player).orElseGet(StoryProgress::new));
    }

    @Override
    public int act() {
        return story.act();
    }

    @Override
    public boolean hasDefeated(String bossId) {
        return story.hasDefeated(bossId);
    }

    @Override
    public int questStep(String questId) {
        return story.questStep(questId);
    }

    @Override
    public int countItem(String itemId) {
        ResourceLocation id = ResourceLocation.tryParse(itemId);
        Item item = id == null ? null : ForgeRegistries.ITEMS.getValue(id);
        if (item == null) return 0;
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(item)) count += stack.getCount();
        }
        return count;
    }

    @Override
    public String fate(String region) {
        return Region.byId(region).flatMap(story::fate).orElse(null);
    }

    @Override
    public String playerClass() {
        return PlayerClassCapability.get(player).flatMap(PlayerClassData::get).map(PlayerClass::id).orElse(null);
    }
}
