package com.sofe.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.context.CommandContext;
import com.sofe.SoFEMod;
import com.sofe.world.zone.StructurePositions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.loading.FMLLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * The /sofe commands. Every player has {@code /sofe pact} (invite, accept, decline, leave, kick, list; UC-18) and
 * {@code /sofe council restore} (the Council gives back lost story items, in Sulthari; UC-21). Operators (permission
 * level 2) also have, UC-36: {@code /sofe progress <player> act|defeat|show}, {@code /sofe unstuck <player>} (every player
 * has {@code /sofe unstuck} for themselves, every five minutes),
 * {@code /sofe item restore <player>}, {@code /sofe pacts} and {@code /sofe export}.
 *
 *
 * <p>{@code /sofe export <piece>} saves a story place as it now stands in the world, so a build
 * improved by hand replaces the generated one in every new journey. Stand on the floor of the place:
 * the template covers its footprint from structure_positions.json, from the layer under the player's
 * feet up to its highest block, with no 48-block limit. It is saved in the world
 * (generated/sofe/structures/&lt;piece&gt;.nbt) and, when run from the development workspace, also into
 * src/main/resources/data/sofe/structures, where {@link com.sofe.world.build.StructureBuilder} finds it.
 */
public final class SoFECommands {
    private static final int MAX_HEIGHT = 96;

    private SoFECommands() {
    }

    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("sofe")
                .then(Commands.literal("pact")
                        .then(Commands.literal("invite").then(Commands.argument("player", EntityArgument.player())
                                .executes(c -> ok(com.sofe.pact.Pacts.invite(c.getSource().getPlayerOrException(), EntityArgument.getPlayer(c, "player"))))))
                        .then(Commands.literal("accept").executes(c -> ok(com.sofe.pact.Pacts.accept(c.getSource().getPlayerOrException()))))
                        .then(Commands.literal("decline").executes(c -> ok(com.sofe.pact.Pacts.decline(c.getSource().getPlayerOrException()))))
                        .then(Commands.literal("leave").executes(c -> ok(com.sofe.pact.Pacts.leave(c.getSource().getPlayerOrException()))))
                        .then(Commands.literal("kick").then(Commands.argument("name", StringArgumentType.word())
                                .executes(c -> ok(com.sofe.pact.Pacts.kick(c.getSource().getPlayerOrException(), StringArgumentType.getString(c, "name"))))))
                        .then(Commands.literal("list").executes(c -> {
                            com.sofe.pact.Pacts.list(c.getSource().getPlayerOrException());
                            return 1;
                        })))
                .then(Commands.literal("council").then(Commands.literal("restore").executes(SoFECommands::councilRestore)))
                .then(Commands.literal("progress").requires(src -> src.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.literal("show").executes(SoFECommands::progressShow))
                                .then(Commands.literal("act").then(Commands.argument("act", IntegerArgumentType.integer(1, 5))
                                        .executes(SoFECommands::progressAct)))
                                .then(Commands.literal("defeat").then(Commands.argument("boss", StringArgumentType.greedyString())
                                        .suggests((c, b) -> SharedSuggestionProvider.suggest(com.sofe.world.lair.BossLairs.lairs().stream()
                                                .map(com.sofe.world.lair.BossLairs.Lair::boss), b))
                                        .executes(SoFECommands::progressDefeat)))))
                .then(Commands.literal("unstuck").executes(SoFECommands::unstuckSelf)
                        .then(Commands.argument("player", EntityArgument.player()).requires(src -> src.hasPermission(2))
                                .executes(c -> unstuck(c.getSource(), EntityArgument.getPlayer(c, "player")))))
                .then(Commands.literal("item").requires(src -> src.hasPermission(2))
                        .then(Commands.literal("restore").then(Commands.argument("player", EntityArgument.player())
                                .executes(c -> restore(c.getSource(), EntityArgument.getPlayer(c, "player"))))))
                .then(Commands.literal("pacts").requires(src -> src.hasPermission(2)).executes(SoFECommands::allPacts))
                .then(Commands.literal("export").requires(src -> src.hasPermission(2))
                        .then(Commands.argument("piece", StringArgumentType.greedyString())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(StructurePositions.get().structures().keySet().stream()
                                        .map(id -> id.substring(id.indexOf(':') + 1)), builder))
                                .executes(SoFECommands::export))));
    }

    private static int ok(boolean done) {
        return done ? 1 : 0;
    }

    /** The Council gives back lost story items, to a Bearer standing in Sulthari (UC-21). */
    private static int councilRestore(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer player = c.getSource().getPlayerOrException();
        boolean inSulthari = com.sofe.world.SoFEWorld.regionMap(player.server)
                .map(m -> m.regionAt(player.getBlockX(), player.getBlockZ()) == com.sofe.world.region.Region.SULTHARI).orElse(true);
        if (!inSulthari) {
            c.getSource().sendFailure(Component.translatable("command.sofe.council.away"));
            return 0;
        }
        return restore(c.getSource(), player);
    }

    private static int restore(CommandSourceStack source, ServerPlayer player) {
        var given = com.sofe.item.StoryItems.restore(player);
        if (given.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("command.sofe.council.nothing", player.getDisplayName()), false);
            return 0;
        }
        com.sofe.economy.EconomyHandler.sync(player);
        source.sendSuccess(() -> Component.translatable("command.sofe.council.restored", player.getDisplayName(), given.size()), true);
        SoFEMod.LOGGER.info("Story items restored to {}: {}", player.getGameProfile().getName(), given.size());
        return given.size();
    }

    private static int progressShow(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(c, "player");
        var story = com.sofe.story.StoryCapability.get(player);
        int level = com.sofe.progression.ProgressionCapability.get(player).map(p -> p.level()).orElse(0);
        String cls = com.sofe.skill.ClassState.classOf(player).map(com.sofe.player.PlayerClass::id).orElse("-");
        int act = story.map(s -> s.act()).orElse(0);
        String bosses = String.join(", ", story.map(s -> s.bosses()).orElse(java.util.Set.of()));
        c.getSource().sendSuccess(() -> Component.translatable("command.sofe.progress.show", player.getDisplayName(), cls, level, act, bosses), false);
        return 1;
    }

    private static int progressAct(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(c, "player");
        int act = IntegerArgumentType.getInteger(c, "act");
        boolean moved = com.sofe.story.StoryCapability.get(player).map(s -> s.advanceTo(act)).orElse(false);
        com.sofe.quest.QuestEngine.sync(player);
        com.sofe.progression.ProgressionHandler.sync(player);
        c.getSource().sendSuccess(() -> Component.translatable(moved ? "command.sofe.progress.act" : "command.sofe.progress.act_not_moved",
                player.getDisplayName(), act), true);
        SoFEMod.LOGGER.info("{} set {} to act {} (moved: {})", c.getSource().getTextName(), player.getGameProfile().getName(), act, moved);
        return moved ? 1 : 0;
    }

    private static int progressDefeat(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(c, "player");
        String boss = StringArgumentType.getString(c, "boss");
        String id = boss.contains(":") ? boss : SoFEMod.MOD_ID + ":" + boss;
        com.sofe.story.StoryCapability.get(player).ifPresent(s -> s.defeat(id));
        com.sofe.quest.QuestEngine.sync(player);
        c.getSource().sendSuccess(() -> Component.translatable("command.sofe.progress.defeat", player.getDisplayName(), id), true);
        SoFEMod.LOGGER.info("{} credited {} with {}", c.getSource().getTextName(), player.getGameProfile().getName(), id);
        return 1;
    }

    /** How often a player may send themselves back: often enough to get out of a hole, not a free journey home. */
    private static final long UNSTUCK_COOLDOWN_TICKS = 20 * 60 * 5;
    private static final java.util.Map<java.util.UUID, Long> LAST_UNSTUCK = new java.util.HashMap<>();

    /** {@code /sofe unstuck}: any player stuck somewhere (in a hole, on a roof) goes back to the plaza, every five minutes. */
    private static int unstuckSelf(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer player = c.getSource().getPlayerOrException();
        long now = player.server.overworld().getGameTime();
        Long last = LAST_UNSTUCK.get(player.getUUID());
        if (last != null && now - last < UNSTUCK_COOLDOWN_TICKS && !c.getSource().hasPermission(2)) {
            long seconds = (UNSTUCK_COOLDOWN_TICKS - (now - last)) / 20;
            c.getSource().sendFailure(Component.translatable("command.sofe.unstuck.wait", seconds / 60, String.format("%02d", seconds % 60)));
            return 0;
        }
        LAST_UNSTUCK.put(player.getUUID(), now);
        return unstuck(c.getSource(), player);
    }

    /** Sends a stuck player back to the plaza of Sulthari, on the ground in front of the fountain. */
    private static int unstuck(CommandSourceStack source, ServerPlayer player) {
        ServerLevel level = player.server.overworld();
        BlockPos spawn = level.getSharedSpawnPos();
        BlockPos at = com.sofe.world.Grounding.groundFloor(level, spawn.getX(), spawn.getZ());
        player.teleportTo(level, at.getX() + 0.5, at.getY(), at.getZ() + 0.5, player.getYRot(), 0);
        source.sendSuccess(() -> Component.translatable("command.sofe.unstuck", player.getDisplayName()), true);
        SoFEMod.LOGGER.info("{} sent {} back to Sulthari", source.getTextName(), player.getGameProfile().getName());
        return 1;
    }

    private static int allPacts(CommandContext<CommandSourceStack> c) {
        var data = com.sofe.pact.PactData.get(c.getSource().getServer());
        if (data.all().isEmpty()) {
            c.getSource().sendSuccess(() -> Component.translatable("command.sofe.pacts.none"), false);
            return 0;
        }
        for (var pact : data.all()) {
            String names = pact.members().stream().map(pact::name).collect(java.util.stream.Collectors.joining(", "));
            c.getSource().sendSuccess(() -> Component.literal("- " + names), false);
        }
        return data.all().size();
    }

    private static int export(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String piece = StringArgumentType.getString(context, "piece");
        Optional<StructurePositions.Structure> found = StructurePositions.get().structure(SoFEMod.MOD_ID + ":" + piece);
        if (found.isEmpty()) {
            source.sendFailure(Component.translatable("command.sofe.export.unknown", piece));
            return 0;
        }
        StructurePositions.Structure s = found.get();
        ServerLevel level = source.getLevel();
        int base = BlockPos.containing(source.getPosition()).getY() - 1; // the floor the player stands on
        BlockPos corner = new BlockPos(s.x() - s.sizeX() / 2, base, s.z() - s.sizeZ() / 2);
        int top = highestBlock(level, corner, s.sizeX(), s.sizeZ());
        Vec3i size = new Vec3i(s.sizeX(), top - base + 1, s.sizeZ());

        ResourceLocation id = SoFEMod.id(piece);
        StructureTemplate template = level.getStructureManager().getOrCreate(id);
        template.fillFromWorld(level, corner, size, false, null);
        level.getStructureManager().save(id);
        String where = "generated/" + SoFEMod.MOD_ID + "/structures/" + piece + ".nbt";
        Path workspace = Path.of("..", "src", "main", "resources", "data", SoFEMod.MOD_ID, "structures");
        if (!FMLLoader.isProduction() && Files.isDirectory(workspace.getParent())) {
            try {
                Path file = workspace.resolve(piece + ".nbt");
                Files.createDirectories(file.getParent());
                NbtIo.writeCompressed(template.save(new CompoundTag()), file.toFile());
                where = file.normalize().toString();
            } catch (IOException e) {
                SoFEMod.LOGGER.warn("Could not copy {} into the workspace: {}", piece, e.getMessage());
            }
        }
        String saved = where;
        source.sendSuccess(() -> Component.translatable("command.sofe.export.done", piece, size.getX(), size.getY(), size.getZ(), saved), true);
        return 1;
    }

    /** The highest non-air block over the footprint, at most MAX_HEIGHT above the corner. */
    private static int highestBlock(ServerLevel level, BlockPos corner, int sizeX, int sizeZ) {
        int top = corner.getY();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = 0; x < sizeX; x++) {
            for (int z = 0; z < sizeZ; z++) {
                for (int y = corner.getY() + MAX_HEIGHT; y > top; y--) {
                    pos.set(corner.getX() + x, y, corner.getZ() + z);
                    if (!level.getBlockState(pos).isAir()) {
                        top = y;
                        break;
                    }
                }
            }
        }
        return top;
    }
}
