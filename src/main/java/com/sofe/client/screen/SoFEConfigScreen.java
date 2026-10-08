package com.sofe.client.screen;

import com.sofe.config.SoFEConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

/**
 * In-game editor for the client options (Mods → Sins of the Fallen Empires → Config).
 * Forge 1.20.1 has no generic config screen, so each option is a toggle here.
 */
public class SoFEConfigScreen extends Screen {
    private record Option(ForgeConfigSpec.BooleanValue value, String key) {
    }

    private final Screen parent;

    public SoFEConfigScreen(Screen parent) {
        super(Component.translatable("config.sofe.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        List<Option> options = List.of(
                new Option(SoFEConfig.CLIENT.replaceTitleScreen, "config.sofe.replace_title_screen"),
                new Option(SoFEConfig.CLIENT.preselectJourneyPreset, "config.sofe.preselect_journey_preset"),
                new Option(SoFEConfig.CLIENT.dialogueBlip, "config.sofe.dialogue_blip"),
                new Option(SoFEConfig.CLIENT.showQuestCompass, "config.sofe.show_quest_compass"),
                new Option(SoFEConfig.CLIENT.showQuestPath, "config.sofe.show_quest_path"),
                new Option(SoFEConfig.CLIENT.hideBearerOutfit, "config.sofe.hide_bearer_outfit"),
                new Option(SoFEConfig.CLIENT.replaceHealthHud, "config.sofe.replace_health_hud"),
                new Option(SoFEConfig.CLIENT.enemyHealth, "config.sofe.enemy_health"));

        // two columns, so every option fits above the Done button even on a small screen
        int column = 200, gap = 8, width = 260;
        int left = (this.width - column * 2 - gap) / 2, top = 44;
        for (int i = 0; i < options.size(); i++) {
            Option option = options.get(i);
            int bx = left + (i % 2) * (column + gap), by = top + (i / 2) * 24;
            addRenderableWidget(CycleButton.onOffBuilder(option.value().get())
                    .withTooltip(v -> Tooltip.create(Component.translatable(option.key() + ".tooltip")))
                    .create(bx, by, column, 20, Component.translatable(option.key()),
                            (button, value) -> option.value().set(value)));
        }
        int x = (this.width - width) / 2;

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose())
                .bounds(x, this.height - 40, width, 20)
                .build());
    }

    @Override
    public void onClose() {
        SoFEConfig.CLIENT_SPEC.save();
        this.minecraft.setScreen(parent);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }
}
