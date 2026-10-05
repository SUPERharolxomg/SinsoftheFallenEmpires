package com.sofe.client.screen;

import com.sofe.client.ClientEconomyData;
import com.sofe.crafting.StationRecipe;
import com.sofe.crafting.StationService;
import com.sofe.network.SoFENetwork;
import com.sofe.network.StationPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Set;

/**
 * The Imperial Forge and Alembic screen (UC-11, UC-14): the recipes of the station with their
 * ingredients (red when missing). The Forge only lists the Blueprints the player has learned.
 */
public class StationScreen extends Screen {
    private static final int PANEL_W = 300, PANEL_H = 200, ROW = 24, ROWS = 6;
    private static final int GOLD = 0xE8B64A, PARCHMENT = 0xE6DCC8, MUTED = 0xA89F8E, MISSING = 0xD05050;
    private final StationRecipe.Kind kind;
    private int scroll;

    public StationScreen(StationRecipe.Kind kind) {
        super(Component.translatable(kind.titleKey()));
        this.kind = kind;
    }

    private List<StationRecipe> recipes() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return List.of();
        Set<String> known = ClientEconomyData.get().map(e -> e.blueprints()).orElse(Set.of());
        return StationService.all(mc.level.getRecipeManager(), kind).stream()
                .filter(r -> kind != StationRecipe.Kind.IMPERIAL_FORGE || known.contains(r.blueprint()))
                .sorted(java.util.Comparator.comparing(r -> r.id().toString()))
                .toList();
    }

    private int left() {
        return (this.width - PANEL_W) / 2;
    }

    private int top() {
        return (this.height - PANEL_H) / 2;
    }

    @Override
    protected void init() {
        clearWidgets();
        List<StationRecipe> recipes = recipes();
        Inventory inventory = Minecraft.getInstance().player.getInventory();
        int y = top() + 26;
        for (int i = scroll; i < Math.min(recipes.size(), scroll + ROWS); i++) {
            StationRecipe recipe = recipes.get(i);
            Button b = addRenderableWidget(Button.builder(Component.translatable("gui.sofe.station.make"),
                    btn -> SoFENetwork.sendToServer(new StationPackets.Make(recipe.id()))).bounds(left() + PANEL_W - 60, y + 2, 52, 18).build());
            b.active = recipe.canMake(inventory);
            y += ROW;
        }
    }

    @Override
    public void tick() {
        init(); // the inventory changes after making something
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        scroll = Math.max(0, Math.min(Math.max(0, recipes().size() - ROWS), scroll - (int) Math.signum(delta)));
        init();
        return true;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        int l = left(), t = top();
        g.fill(l - 2, t - 2, l + PANEL_W + 2, t + PANEL_H + 2, 0xFF000000 | GOLD);
        g.fill(l, t, l + PANEL_W, t + PANEL_H, 0xF0181410);
        g.drawString(this.font, this.title, l + 8, t + 8, GOLD, false);
        List<StationRecipe> recipes = recipes();
        if (recipes.isEmpty()) g.drawString(this.font, Component.translatable("gui.sofe.station.no_recipes"), l + 10, t + 32, MUTED, false);
        Inventory inventory = Minecraft.getInstance().player.getInventory();
        int y = t + 26;
        ItemStack hovered = ItemStack.EMPTY;
        for (int i = scroll; i < Math.min(recipes.size(), scroll + ROWS); i++) {
            StationRecipe r = recipes.get(i);
            ItemStack result = new ItemStack(r.result(), r.count());
            g.renderItem(result, l + 8, y + 3);
            g.drawString(this.font, result.getHoverName(), l + 28, y + 2, PARCHMENT, false);
            int x = l + 28;
            for (StationRecipe.Ingredient ing : r.ingredients()) {
                ItemStack stack = new ItemStack(ing.item(), ing.count());
                boolean enough = inventory.countItem(ing.item()) >= ing.count();
                g.pose().pushPose();
                g.pose().translate(x, y + 12, 0);
                g.pose().scale(0.6f, 0.6f, 1f);
                g.renderItem(stack, 0, 0);
                g.pose().popPose();
                g.drawString(this.font, "×" + ing.count(), x + 10, y + 13, enough ? MUTED : MISSING, false);
                if (mouseX >= x && mouseX < x + 10 && mouseY >= y + 12 && mouseY < y + 22) hovered = stack;
                x += 34;
            }
            if (r.dinars() > 0) {   // the Jeweler's fee
                long have = com.sofe.client.ClientEconomyData.get().map(e -> e.dinars()).orElse(0L);
                g.drawString(this.font, Component.translatable("gui.sofe.station.dinars", r.dinars()), x + 2, y + 13,
                        have >= r.dinars() ? GOLD : MISSING, false);
            }
            if (mouseX >= l + 8 && mouseX < l + 24 && mouseY >= y + 3 && mouseY < y + 19) hovered = result;
            y += ROW;
        }
        super.render(g, mouseX, mouseY, partialTick);
        if (!hovered.isEmpty()) g.renderTooltip(this.font, hovered, mouseX, mouseY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
