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
        }
    }

    public static final class Server {
        public final ForgeConfigSpec.BooleanValue openClassSelectOnJoin;

        private Server(ForgeConfigSpec.Builder builder) {
            builder.push("bearers");
            openClassSelectOnJoin = builder
                    .comment("In a SoFE journey, open the Bearer selection for players who have not chosen one yet.")
                    .translation("config.sofe.open_class_select_on_join")
                    .define("openClassSelectOnJoin", true);
            builder.pop();
        }
    }
}
