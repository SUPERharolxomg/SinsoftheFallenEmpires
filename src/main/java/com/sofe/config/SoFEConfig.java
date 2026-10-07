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
        public final ForgeConfigSpec.BooleanValue showQuestCompass, showQuestPath;
        public final ForgeConfigSpec.BooleanValue hideBearerOutfit;
        public final ForgeConfigSpec.BooleanValue replaceHealthHud, enemyHealth;
        public final ForgeConfigSpec.BooleanValue rarityLabels, reduceMotion;

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
            showQuestPath = builder
                    .comment("Show the way to the tracked quest in the world: golden motes on the ground, a column of light",
                            "over the place and a crown of light over the person to talk to.")
                    .translation("config.sofe.show_quest_path")
                    .define("showQuestPath", true);
            hideBearerOutfit = builder
                    .comment("Hide the Bearer outfit layer and show only your own skin and armor.")
                    .translation("config.sofe.hide_bearer_outfit")
                    .define("hideBearerOutfit", false);
            replaceHealthHud = builder
                    .comment("Show health as a bar in the SoFE HUD instead of the vanilla hearts.")
                    .translation("config.sofe.replace_health_hud")
                    .define("replaceHealthHud", true);
            enemyHealth = builder
                    .comment("Write over each enemy near you how much health it has left (18 / 24), not as a bar.")
                    .translation("config.sofe.enemy_health")
                    .define("enemyHealth", true);
            // accessibility (docs/Jugabilidad.md, G13)
            rarityLabels = builder
                    .comment("Write the rarity's name over gear lying on the ground, not only the colour of its beam.")
                    .translation("config.sofe.rarity_labels")
                    .define("rarityLabels", true);
            reduceMotion = builder
                    .comment("Calmer story scenes: no flickering fire, no scrolling skylines, fewer embers. Also on with Minecraft's 'Hide Lightning Flashes'.")
                    .translation("config.sofe.reduce_motion")
                    .define("reduceMotion", false);
            builder.pop();
        }
    }

    public static final class Server {
        public final ForgeConfigSpec.BooleanValue openClassSelectOnJoin;
        public final ForgeConfigSpec.BooleanValue opsBypass;
        public final ForgeConfigSpec.BooleanValue protectZones;
        public final ForgeConfigSpec.BooleanValue corpseSystem, allowIncompatibleMods;
        public final ForgeConfigSpec.ConfigValue<String> relicBinding;
        public final ForgeConfigSpec.IntValue pactMaxMembers;
        public final ForgeConfigSpec.DoubleValue pactRange, pactXpBonus, pactFindBonus, pactFindBonusCap, bossHealthPerPlayer;
        public final ForgeConfigSpec.BooleanValue revive, uniqueBearersPerServer;
        public final ForgeConfigSpec.IntValue downedSeconds, reviveSeconds;
        public final ForgeConfigSpec.ConfigValue<String> gateMode;

        private Server(ForgeConfigSpec.Builder builder) {
            builder.push("bearers");
            openClassSelectOnJoin = builder
                    .comment("In a SoFE journey, open the Bearer selection for players who have not chosen one yet.")
                    .translation("config.sofe.open_class_select_on_join")
                    .define("openClassSelectOnJoin", true);
            uniqueBearersPerServer = builder
                    .comment("Each Bearer (class) can be chosen by one player only on this server, as in the story.")
                    .translation("config.sofe.unique_bearers_per_server")
                    .define("uniqueBearersPerServer", false);
            builder.pop();
            builder.push("pact");
            pactMaxMembers = builder.comment("Most players in a Pact of the Empires.").translation("config.sofe.pact_max_members")
                    .defineInRange("maxMembers", com.sofe.pact.PactRules.MAX_MEMBERS, 2, 10);
            pactRange = builder.comment("How near (blocks, same dimension) Pact members must be to count as together.")
                    .translation("config.sofe.pact_range").defineInRange("togetherRange", com.sofe.pact.PactRules.TOGETHER_RANGE, 8, 256);
            pactXpBonus = builder.comment("Extra kill XP per extra member together, split among them (0.10 = +10%).")
                    .translation("config.sofe.pact_xp_bonus").defineInRange("xpBonusPerMember", com.sofe.pact.PactRules.XP_BONUS_PER_MEMBER, 0, 1);
            pactFindBonus = builder.comment("Better rarity odds per ally together (0.05 = +5%).")
                    .translation("config.sofe.pact_find_bonus").defineInRange("findBonusPerAlly", com.sofe.pact.PactRules.FIND_BONUS_PER_ALLY, 0, 1);
            pactFindBonusCap = builder.comment("Cap of the rarity bonus.")
                    .translation("config.sofe.pact_find_bonus_cap").defineInRange("findBonusCap", com.sofe.pact.PactRules.FIND_BONUS_CAP, 0, 2);
            bossHealthPerPlayer = builder.comment("Extra boss health per extra player in its arena (0.6 = +60%); damage does not change.")
                    .translation("config.sofe.boss_health_per_player").defineInRange("bossHealthPerPlayer", com.sofe.pact.PactRules.BOSS_HEALTH_PER_PLAYER, 0, 3);
            revive = builder.comment("In a boss arena a player who falls is downed instead of dying, while an ally still stands; an ally revives them by crouching beside them.")
                    .translation("config.sofe.revive").define("revive", true);
            downedSeconds = builder.comment("How long a downed player can wait for help before dying.")
                    .translation("config.sofe.downed_seconds").defineInRange("downedSeconds", 30, 5, 120);
            reviveSeconds = builder.comment("How long an ally must crouch beside a downed player to raise them.")
                    .translation("config.sofe.revive_seconds").defineInRange("reviveSeconds", 3, 1, 10);
            builder.pop();
            builder.push("world");
            opsBypass = builder
                    .comment("Operators pass through the Seal Veil (protected places stay protected even for them).")
                    .translation("config.sofe.ops_bypass")
                    .define("opsBypass", true);
            protectZones = builder
                    .comment("Keep the protected places (cities, camps, dungeons, arenas) as they were built: no one, not even an operator in creative, can break, place or take anything there. Turn off only to work on a place.")
                    .translation("config.sofe.protect_zones")
                    .define("protectZones", true);
            gateMode = builder
                    .comment("Who may pass a Sealed Gate: per_player (each needs its condition) or pact_escort (a Pact member who meets it",
                            "lets the members beside them through).")
                    .translation("config.sofe.gate_mode")
                    .define("gateMode", "per_player");
            builder.pop();
            builder.push("death");
            corpseSystem = builder
                    .comment("Leave the Bearer's corpse with the gear where a player dies (Diablo II style).",
                            "Turn off when the server uses another grave or corpse mod.")
                    .translation("config.sofe.corpse_system")
                    .define("corpseSystem", true);
            allowIncompatibleMods = builder
                    .comment("Play the story even with a mod listed in data/sofe/compat/incompatible_mods.json installed (at your own risk).")
                    .translation("config.sofe.allow_incompatible_mods")
                    .define("allowIncompatibleMods", false);
            builder.pop();
            builder.push("items");
            relicBinding = builder
                    .comment("Who may hold Relics and Imperial Legacy pieces: free (anyone), pact_only (the owner and their Pact) or soulbound.",
                            "Story items (the Bearer's Flask, Codex Shards) are always soulbound.")
                    .translation("config.sofe.relic_binding")
                    .define("relicBinding", "free");
            builder.pop();
        }
    }
}
