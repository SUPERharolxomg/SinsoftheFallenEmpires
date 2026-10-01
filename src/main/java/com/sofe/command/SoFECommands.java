package com.sofe.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.sofe.SoFEMod;
import com.sofe.world.zone.StructurePositions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
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
 * Admin commands (permission level 2).
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
        dispatcher.register(Commands.literal("sofe").requires(source -> source.hasPermission(2))
                .then(Commands.literal("export")
                        .then(Commands.argument("piece", StringArgumentType.greedyString())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(StructurePositions.get().structures().keySet().stream()
                                        .map(id -> id.substring(id.indexOf(':') + 1)), builder))
                                .executes(SoFECommands::export))));
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
