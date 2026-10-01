package com.sofe.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.sofe.SoFEMod;
import com.sofe.config.SoFEConfig;
import com.sofe.network.DialogueAnswerPacket;
import com.sofe.network.DialogueLinePacket;
import com.sofe.network.SoFENetwork;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Entity;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * The Warcraft III style dialogue box (docs/Jugabilidad.md, "Dialogue"): portrait on the left,
 * the speaker's name in gold, the text appearing letter by letter and up to four answers.
 * Cinematic lines add letterbox bars and keep the player still; in a plain conversation the
 * player can keep walking (walking away ends it on the server).
 */
public class DialogueScreen extends Screen {
    private static final int GOLD = 0xF2C25A, TEXT = 0xF6E7C1, OUTLINE = 0x2A1A0C, MUTED = 0xA89F8E;
    /** The widest the bar is drawn, in GUI pixels (about 1:1 with the 4x bar textures at GUI scale 2). */
    private static final float MAX_BAR_WIDTH = 880f;
    private static final int LINES_PER_PAGE = 3;
    private static final float LETTERS_PER_TICK = 1.6f;
    private static final int MAX_ANSWERS = 4;

    private DialogueLinePacket line;
    private List<FormattedCharSequence> wrapped = List.of();
    private int page;
    private float letters;
    private int lastBlip;
    private final List<Button> answerButtons = new ArrayList<>();

    /** Where the text goes, computed in render and used to wrap the text to the box. */
    private int textX, textY, textW;

    public DialogueScreen(DialogueLinePacket line) {
        super(Component.translatable("gui.sofe.dialogue"));
        this.line = line;
    }

    /** A new line from the server while the box is open. */
    public void show(DialogueLinePacket next) {
        this.line = next;
        this.page = 0;
        this.letters = 0;
        this.lastBlip = 0;
        rebuild();
    }

    public String dialogueId() {
        return line.dialogue();
    }

    @Override
    protected void init() {
        rebuild();
    }

    private void rebuild() {
        layout();
        wrapped = this.font.split(Component.translatable(line.text()), Math.max(40, textW));
        answerButtons.forEach(this::removeWidget);
        answerButtons.clear();
        int count = Math.min(MAX_ANSWERS, line.answers().size());
        int w = Math.min(260, this.width - 40);
        int x = (this.width - w) / 2;
        int y = boxTop() - 8 - count * 22;
        for (int i = 0; i < count; i++) {
            int index = i;
            Button b = Button.builder(Component.literal((i + 1) + ". ").append(Component.translatable(line.answers().get(i))),
                    btn -> answer(index)).bounds(x, y + i * 22, w, 20).build();
            b.visible = false;
            answerButtons.add(addRenderableWidget(b));
        }
    }

    // --- layout ---

    private float barScale() {
        return DialogueArt.bar(line.style()).map(bar -> Math.min(this.width - 16, MAX_BAR_WIDTH) / bar.width()).orElse(1f);
    }

    private int boxWidth() {
        return DialogueArt.bar(line.style()).map(bar -> Math.round(bar.width() * barScale())).orElse(Math.min(this.width - 16, 420));
    }

    private int boxHeight() {
        return DialogueArt.bar(line.style()).map(bar -> Math.round(bar.height() * barScale())).orElse(64);
    }

    private int boxLeft() {
        return (this.width - boxWidth()) / 2;
    }

    private int boxTop() {
        int bottomMargin = line.cinematic() ? 10 : 6; // in a cinematic the box sits inside the lower black band
        return this.height - bottomMargin - boxHeight();
    }

    /** Portrait window in screen pixels: x, y, size. */
    private int[] portraitWindow() {
        int l = boxLeft(), t = boxTop();
        return DialogueArt.bar(line.style()).map(bar -> {
            float s = barScale();
            int size = Math.round(Math.min(bar.windowW(), bar.windowH()) * s);
            return new int[]{l + Math.round(bar.windowX() * s), t + Math.round(bar.windowY() * s), size};
        }).orElse(new int[]{l + 8, t + 8, 48});
    }

    /**
     * The framed portrait: larger than the bar's window and centered on it, rising above the bar,
     * so the speaker is clearly seen. Returns x, y, size of the picture (the frame goes around it).
     */
    private int[] portraitCard() {
        int[] window = portraitWindow();
        int h = boxHeight();
        int size = Math.max(40, Math.round(h * 1.25f));
        int centerX = window[0] + window[2] / 2;
        int left = Math.max(boxLeft() + size / 8 + 2, centerX - size / 2);
        int bottom = boxTop() + h - Math.round(h * 0.12f);
        return new int[]{left, bottom - size, size};
    }

    private void layout() {
        int[] card = portraitCard();
        textX = card[0] + card[2] + card[2] / 8 + 10;
        textW = boxLeft() + boxWidth() - textX - Math.round(boxWidth() * 0.05f);
        int lines = Math.max(1, Math.min(LINES_PER_PAGE, wrapped.size() - page * LINES_PER_PAGE));
        // centered on the lower band of the bar (36%-88% of its height), clear of the plate and ornaments on top
        int bandTop = Math.round(boxHeight() * 0.36f), bandBottom = Math.round(boxHeight() * 0.88f);
        textY = boxTop() + bandTop + (bandBottom - bandTop - lines * 10) / 2 + 1;
    }

    /** Text with a dark outline, like engraved on the brass. */
    private void engraved(GuiGraphics g, FormattedCharSequence text, int x, int y, int color) {
        g.drawString(this.font, text, x - 1, y, OUTLINE, false);
        g.drawString(this.font, text, x + 1, y, OUTLINE, false);
        g.drawString(this.font, text, x, y - 1, OUTLINE, false);
        g.drawString(this.font, text, x, y + 1, OUTLINE, false);
        g.drawString(this.font, text, x, y, color, false);
    }

    // --- input ---

    private boolean fullyShown() {
        return letters >= lettersOnPage();
    }

    private int lettersOnPage() {
        int total = 0;
        for (FormattedCharSequence s : pageLines()) total += length(s);
        return total;
    }

    private List<FormattedCharSequence> pageLines() {
        int from = page * LINES_PER_PAGE;
        return wrapped.subList(Math.min(from, wrapped.size()), Math.min(from + LINES_PER_PAGE, wrapped.size()));
    }

    private boolean lastPage() {
        return (page + 1) * LINES_PER_PAGE >= wrapped.size();
    }

    /** Finish the line, then the next page, then the next line (only when there is nothing to choose). */
    private void advance() {
        if (!fullyShown()) {
            letters = lettersOnPage();
        } else if (!lastPage()) {
            page++;
            letters = 0;
            lastBlip = 0;
        } else if (line.answers().isEmpty()) {
            SoFENetwork.sendToServer(new DialogueAnswerPacket(line.line(), -1));
        }
    }

    private void answer(int index) {
        if (fullyShown() && lastPage()) {
            SoFENetwork.sendToServer(new DialogueAnswerPacket(line.line(), index));
        }
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER || (line.cinematic() && key == GLFW.GLFW_KEY_SPACE)) {
            advance();
            return true;
        }
        if (key >= GLFW.GLFW_KEY_1 && key < GLFW.GLFW_KEY_1 + Math.min(MAX_ANSWERS, line.answers().size())) {
            answer(key - GLFW.GLFW_KEY_1);
            return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if (button == 0) {
            advance();
            return true;
        }
        return false;
    }

    @Override
    public void onClose() {
        SoFENetwork.sendToServer(DialogueAnswerPacket.close());
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        if (!fullyShown()) {
            letters = Math.min(lettersOnPage(), letters + LETTERS_PER_TICK);
            if (SoFEConfig.CLIENT.dialogueBlip.get() && (int) letters / 3 > lastBlip) {
                lastBlip = (int) letters / 3;
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_HAT.get(), 1.6f, 0.15f));
            }
        }
        boolean choosing = fullyShown() && lastPage();
        answerButtons.forEach(b -> b.visible = choosing);
        if (!line.cinematic()) passMovementKeys();
        faceSpeaker();
    }

    /** In a conversation the player keeps walking: forward the movement keys the screen would swallow. */
    private void passMovementKeys() {
        Minecraft mc = Minecraft.getInstance();
        long window = mc.getWindow().getWindow();
        for (KeyMapping key : new KeyMapping[]{mc.options.keyUp, mc.options.keyDown, mc.options.keyLeft,
                mc.options.keyRight, mc.options.keyJump, mc.options.keySprint, mc.options.keyShift}) {
            InputConstants.Key bound = key.getKey();
            if (bound.getType() == InputConstants.Type.KEYSYM && bound.getValue() != InputConstants.UNKNOWN.getValue()) {
                KeyMapping.set(bound, InputConstants.isKeyDown(window, bound.getValue()));
            }
        }
    }

    /** The NPC turns to the player (the simple animation for now); the player looks at the NPC in cinematics. */
    private void faceSpeaker() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || line.npcEntity() < 0) return;
        Entity npc = mc.level.getEntity(line.npcEntity());
        if (npc != null && line.cinematic()) {
            mc.player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, npc.getEyePosition());
        }
    }

    // --- drawing ---

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        layout();
        if (line.cinematic()) {
            // letterbox: the lower band is tall enough to hold the whole box, portrait and name included
            int[] frame = portraitCard();
            int lower = Math.min(frame[1] - frame[2] / 8, boxTop() - 14) - 8;
            g.fill(0, 0, this.width, this.height / 8, 0xFF000000);
            g.fill(0, lower, this.width, this.height, 0xFF000000);
        }
        int l = boxLeft(), t = boxTop(), w = boxWidth(), h = boxHeight();
        int[] card = portraitCard();

        DialogueArt.bar(line.style()).ifPresentOrElse(
                bar -> g.blit(bar.texture(), l, t, w, h, 0f, 0f, bar.width(), bar.height(), bar.width(), bar.height()),
                () -> DialogueArt.nineSlice(g, DialogueArt.box(line.style()), l, t, w, h, 8, 64));
        // the portrait sits over the bar's window, in the empire's frame (8 px border around a 64 px picture)
        drawPortrait(g, card);
        int border = card[2] / 8;
        g.pose().pushPose();
        g.pose().translate(0, 0, 50);
        g.blit(DialogueArt.portraitFrame(line.style()), card[0] - border, card[1] - border, card[2] + border * 2, card[2] + border * 2,
                0f, 0f, 80, 80, 80, 80);
        g.pose().popPose();

        Component name = speakerName();
        if (name != null) {
            int nameY = t - 11;
            engraved(g, name.getVisualOrderText(), textX, nameY, GOLD);
            g.fill(textX, nameY + 9, textX + this.font.width(name), nameY + 10, 0xFF000000 | 0xB5863A);
        }

        int remaining = (int) letters;
        int y = textY;
        for (FormattedCharSequence s : pageLines()) {
            int len = length(s);
            if (remaining <= 0) break;
            engraved(g, remaining >= len ? s : cut(s, remaining), textX, y, TEXT);
            remaining -= len;
            y += 10;
        }
        if (fullyShown() && (line.answers().isEmpty() || !lastPage()) && (System.currentTimeMillis() / 400) % 2 == 0) {
            engraved(g, Component.literal("\u25BC").getVisualOrderText(), l + w - Math.round(w * 0.04f), t + h - 16, GOLD);
        }
        if (fullyShown() && lastPage() && !line.answers().isEmpty()) {
            int count = Math.min(MAX_ANSWERS, line.answers().size());
            int bw = Math.min(260, this.width - 40);
            g.fill((this.width - bw) / 2 - 4, t - 12 - count * 22, (this.width + bw) / 2 + 4, t - 16, 0xAA000000);
        } else if (!line.cinematic()) {
            g.drawString(this.font, Component.translatable("gui.sofe.dialogue.hint"), l + 4, t + h + 1, MUTED, false);
        }
        super.render(g, mouseX, mouseY, partialTick);
    }

    private Component speakerName() {
        return switch (line.speaker()) {
            case "narrator" -> null;
            case "player" -> Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getDisplayName() : null;
            default -> Component.translatable("npc.sofe." + line.speaker());
        };
    }

    /**
     * NPCs use their still portrait; the player is drawn live from their skin and outfit; the narrator
     * shows the eclipse emblem.
     */
    private void drawPortrait(GuiGraphics g, int[] window) {
        int x = window[0], y = window[1], size = window[2];
        g.fill(x, y, x + size, y + size, 0xFF2A1E14);
        if ("narrator".equals(line.speaker())) {
            ResourceLocation emblem = SoFEMod.id("textures/gui/portrait/narrator.png");
            if (DialogueArt.exists(emblem)) blitWhole(g, emblem, x, y, size);
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if ("player".equals(line.speaker())) {
            if (mc.player != null) bust(g, mc.player, x, y, size);
            return;
        }
        ResourceLocation portrait = SoFEMod.id("textures/gui/portrait/" + line.speaker() + ".png");
        if (DialogueArt.exists(portrait)) {
            blitWhole(g, portrait, x, y, size);
        } else if (mc.level != null && line.npcEntity() >= 0
                && mc.level.getEntity(line.npcEntity()) instanceof net.minecraft.world.entity.LivingEntity npc) {
            bust(g, npc, x, y, size); // citizens and merchants without a painted portrait are drawn live
        }
    }

    /** A bust of a living model: the head and most of the torso fit in the frame (models are 1.8 blocks tall). */
    private static void bust(GuiGraphics g, net.minecraft.world.entity.LivingEntity entity, int x, int y, int size) {
        int scale = Math.round(size / 1.3f);
        int feet = y + Math.round(size * 0.08f) + Math.round(scale * 1.8f);
        g.enableScissor(x, y, x + size, y + size);
        InventoryScreen.renderEntityInInventoryFollowsMouse(g, x + size / 2, feet, scale, -18f, -6f, entity);
        g.disableScissor();
    }

    /** A whole texture drawn into a size by size square, whatever its resolution. */
    private static void blitWhole(GuiGraphics g, ResourceLocation texture, int x, int y, int size) {
        int[] tex = DialogueArt.size(texture);
        g.blit(texture, x, y, size, size, 0f, 0f, tex[0], tex[1], tex[0], tex[1]);
    }

    private static int length(FormattedCharSequence s) {
        int[] n = {0};
        s.accept((i, style, cp) -> {
            n[0]++;
            return true;
        });
        return n[0];
    }

    /** The first letters of a line, keeping their style. */
    private static FormattedCharSequence cut(FormattedCharSequence s, int letters) {
        return sink -> {
            int[] n = {0};
            return s.accept((i, style, cp) -> n[0]++ < letters && sink.accept(i, style, cp));
        };
    }
}
