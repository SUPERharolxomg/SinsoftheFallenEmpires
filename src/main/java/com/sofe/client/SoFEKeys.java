package com.sofe.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.sofe.network.CastSkillPacket;
import com.sofe.network.SoFENetwork;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.event.TickEvent;
import org.lwjgl.glfw.GLFW;

/**
 * The Combat Bar (docs/Jugabilidad.md, G4): hold Left Alt and press 1-5 for skills, 6 for the
 * ultimate. All keys can be changed in Controls, under "Sins of the Fallen Empires".
 */
public final class SoFEKeys {
    private static final String CATEGORY = "key.categories.sofe";

    public static final KeyMapping[] SKILLS = new KeyMapping[6];
    /** The potion belt: Left Alt + 7, 8, 9 and 0 (docs/Jugabilidad.md, G4). */
    public static final KeyMapping[] BELT = new KeyMapping[4];
    public static final KeyMapping FLASK = new KeyMapping("key.sofe.flask", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_H, CATEGORY);
    public static final KeyMapping SKILL_TREE = new KeyMapping("key.sofe.skill_tree", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, CATEGORY);
    public static final KeyMapping CHARACTER = new KeyMapping("key.sofe.character", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_I, CATEGORY);
    public static final KeyMapping JOURNAL = new KeyMapping("key.sofe.journal", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_U, CATEGORY);
    /** The Knight's stance: Shield or Charge (docs/Clases.md). */
    public static final KeyMapping STANCE = new KeyMapping("key.sofe.stance", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, CATEGORY);

    static {
        for (int i = 0; i < 5; i++) {
            SKILLS[i] = combatKey("key.sofe.skill_" + (i + 1), GLFW.GLFW_KEY_1 + i);
        }
        SKILLS[5] = combatKey("key.sofe.ultimate", GLFW.GLFW_KEY_6);
        int[] beltKeys = {GLFW.GLFW_KEY_7, GLFW.GLFW_KEY_8, GLFW.GLFW_KEY_9, GLFW.GLFW_KEY_0};
        for (int i = 0; i < BELT.length; i++) BELT[i] = combatKey("key.sofe.belt_" + (i + 1), beltKeys[i]);
    }

    private SoFEKeys() {
    }

    private static KeyMapping combatKey(String name, int key) {
        return new KeyMapping(name, KeyConflictContext.IN_GAME, KeyModifier.ALT, InputConstants.Type.KEYSYM, key, CATEGORY);
    }

    public static void register(RegisterKeyMappingsEvent event) {
        for (KeyMapping key : SKILLS) {
            event.register(key);
        }
        event.register(FLASK);
        for (KeyMapping key : BELT) event.register(key);
        event.register(SKILL_TREE);
        event.register(CHARACTER);
        event.register(JOURNAL);
        event.register(STANCE);
    }

    /**
     * Runs before Minecraft handles its own keys each tick. A Combat Bar press casts the slot and
     * swallows the same press on the vanilla hotbar key, so Alt+1 never also changes the hotbar slot.
     */
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null) return;

        if (ClientClassData.get().isPresent()) {
            while (SKILL_TREE.consumeClick()) {
                minecraft.setScreen(new com.sofe.client.screen.SkillTreeScreen());
            }
            while (CHARACTER.consumeClick()) {
                minecraft.setScreen(new com.sofe.client.screen.CharacterSheetScreen());
            }
            while (JOURNAL.consumeClick()) {
                minecraft.setScreen(new com.sofe.client.screen.JournalScreen());
            }
            if (minecraft.screen != null) return;
        }

        for (int slot = 0; slot < SKILLS.length; slot++) {
            if (pressed(SKILLS[slot], minecraft)) SoFENetwork.sendToServer(new CastSkillPacket(slot));
        }
        for (int slot = 0; slot < BELT.length; slot++) {
            if (pressed(BELT[slot], minecraft)) SoFENetwork.sendToServer(new com.sofe.network.StationPackets.Drink(slot));
        }
        while (STANCE.consumeClick()) {
            SoFENetwork.sendToServer(new com.sofe.network.StancePacket());
        }
        while (FLASK.consumeClick()) {
            SoFENetwork.sendToServer(new com.sofe.network.StationPackets.Drink(com.sofe.network.StationPackets.Drink.FLASK));
        }
    }

    /** Whether a Combat Bar key was pressed; the same press on a vanilla hotbar key is swallowed. */
    private static boolean pressed(KeyMapping key, Minecraft minecraft) {
        boolean pressed = false;
        while (key.consumeClick()) {
            pressed = true;
        }
        if (pressed) {
            for (KeyMapping hotbar : minecraft.options.keyHotbarSlots) {
                if (hotbar.getKey().equals(key.getKey())) {
                    while (hotbar.consumeClick()) {
                        // discard: this press belonged to the Combat Bar
                    }
                }
            }
        }
        return pressed;
    }
}
