package com.sofe.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Client options (config/sofe-client.toml) and per-world server options
 * (&lt;world&gt;/serverconfig/sofe-server.toml). Common options are added when the first one is needed.
 */
public final class SoFEConfig {
    public static final Client CLIENT;
    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final Server SERVER;
    public static final ForgeConfigSpec SERVER_SPEC;

    static {
        Pair<Client, ForgeConfigSpec> client = new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT = client.getLeft();
        CLIENT_SPEC = client.getRight();
        Pair<Server, ForgeConfigSpec> server = new ForgeConfigSpec.Builder().configure(Server::new);
        SERVER = server.getLeft();
        SERVER_SPEC = server.getRight();
    }

    private SoFEConfig() {
    }

    public static void register(ModLoadingContext context) {
        context.registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
        context.registerConfig(ModConfig.Type.SERVER, SERVER_SPEC);
    }

    public static final class Client {
        public final ForgeConfigSpec.BooleanValue replaceTitleScreen;
        public final ForgeConfigSpec.BooleanValue preselectJourneyPreset;
        public final ForgeConfigSpec.BooleanValue dialogueBlip;
        public final ForgeConfigSpec.BooleanValue showQuestCompass;
        public final ForgeConfigSpec.BooleanValue hideBearerOutfit;
        public final ForgeConfigSpec.BooleanValue replaceHealthHud;

        private Client(ForgeConfigSpec.Builder builder) {
            builder.push("menu");
            replaceTitleScreen = builder
                    .comment("Show the SoFE title screen and main menu instead of the vanilla one.",
                            "Turn off for modpacks that ship their own menu.")
                    .translation("config.sofe.replace_title_screen")
                    .define("replaceTitleScreen", true);
            preselectJourneyPreset = builder
                    .comment("When creating a new world, select the SoFE journey (sofe:aetheris) as the world type.")
                    .translation("config.sofe.preselect_journey_preset")
                    .define("preselectJourneyPreset", true);
            builder.pop();
            builder.push("story");
            dialogueBlip = builder
                    .comment("Play a soft blip while the letters of a dialogue line appear. There are no voices.")
                    .translation("config.sofe.dialogue_blip")
                    .define("dialogueBlip", true);
            showQuestCompass = builder
                    .comment("Show the Quest Compass at the top of the screen, pointing to the tracked quest.")
                    .translation("config.sofe.show_quest_compass")
                    .define("showQuestCompass", true);
            hideBearerOutfit = builder
                    .comment("Hide the Bearer outfit layer and show only your own skin and armor.")
                    .translation("config.sofe.hide_bearer_outfit")
                    .define("hideBearerOutfit", false);
            replaceHealthHud = builder
                    .comment("Show health as a bar in the SoFE HUD instead of the vanilla hearts.")
                    .translation("config.sofe.replace_health_hud")
                    .define("replaceHealthHud", true);
            builder.pop();
        }
    }

    public static final class Server {
        public final ForgeConfigSpec.BooleanValue openClassSelectOnJoin;
        public final ForgeConfigSpec.BooleanValue opsBypass;
        public final ForgeConfigSpec.BooleanValue protectZones;
        public final ForgeConfigSpec.BooleanValue corpseSystem;

        private Server(ForgeConfigSpec.Builder builder) {
            builder.push("bearers");
            openClassSelectOnJoin = builder
                    .comment("In a SoFE journey, open the Bearer selection for players who have not chosen one yet.")
                    .translation("config.sofe.open_class_select_on_join")
                    .define("openClassSelectOnJoin", true);
            builder.pop();
            builder.push("world");
            opsBypass = builder
                    .comment("Operators in creative or spectator mode pass through the Seal Veil and can build in protected zones.")
                    .translation("config.sofe.ops_bypass")
                    .define("opsBypass", true);
            protectZones = builder
                    .comment("Stop players from breaking or placing blocks in the city of Sulthari and other protected places.")
                    .translation("config.sofe.protect_zones")
                    .define("protectZones", true);
            builder.pop();
            builder.push("death");
            corpseSystem = builder
                    .comment("Leave the Bearer's corpse with the gear where a player dies (Diablo II style).",
                            "Turn off when the server uses another grave or corpse mod.")
                    .translation("config.sofe.corpse_system")
                    .define("corpseSystem", true);
            builder.pop();
        }
    }
}
