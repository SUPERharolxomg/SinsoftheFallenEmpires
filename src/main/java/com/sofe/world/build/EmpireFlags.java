package com.sofe.world.build;

import com.sofe.registry.SoFEBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import static com.sofe.world.build.SultharisBuilder.set;

/**
 * The flags and banners of the empires, as Skarnhold has the clans' (NordrathCity): a Supplementaries flag or a
 * banner in the empire's colour with its emblem in banner patterns, flagpoles with two flags, and the great banners
 * (ClanBanner with the empire's cloth).
 * <ul>
 * <li>Parsivan: violet, a turquoise disk, a white flower (the star), a gold border;</li>
 * <li>Khemet: blue, a gold sun disk over gold teeth (the pyramids), a cyan border;</li>
 * <li>Aureum: blue, a gold lozenge with a blue flower (the laurel), a gold border.</li>
 * </ul>
 */
final class EmpireFlags {
    private EmpireFlags() {
    }

    static String base(CapitalCity.Empire empire) {
        return switch (empire) {
            case PARSIVAN -> "purple";
            case KHEMET, AUREUM -> "blue";
        };
    }

    private static String[][] emblem(CapitalCity.Empire empire) {
        return switch (empire) {
            case PARSIVAN -> new String[][]{{"mc", "cyan"}, {"flo", "white"}, {"bo", "yellow"}};
            case KHEMET -> new String[][]{{"bts", "yellow"}, {"mc", "yellow"}, {"bo", "cyan"}};
            case AUREUM -> new String[][]{{"mr", "yellow"}, {"flo", "blue"}, {"bo", "yellow"}};
        };
    }

    /** Puts the empire's emblem on the flag or banner standing at pos. */
    static void decorate(ServerLevel level, BlockPos pos, CapitalCity.Empire empire) {
        var be = level.getBlockEntity(pos);
        if (be == null) return;
        CompoundTag tag = be.saveWithoutMetadata();
        ListTag patterns = new ListTag();
        for (String[] layer : emblem(empire)) {
            CompoundTag pat = new CompoundTag();
            pat.putString("Pattern", layer[0]);
            pat.putInt("Color", DyeColor.byName(layer[1], DyeColor.WHITE).getId());
            patterns.add(pat);
        }
        tag.put("Patterns", patterns);
        be.load(tag);
        be.setChanged();
        level.sendBlockUpdated(pos, be.getBlockState(), be.getBlockState(), Block.UPDATE_CLIENTS);
    }

    /** A flag of the empire flying toward a direction. */
    static void flag(ServerLevel level, int x, int y, int z, CapitalCity.Empire empire, Direction flying) {
        set(level, x, y, z, Furniture.flag(base(empire), flying));
        decorate(level, new BlockPos(x, y, z), empire);
    }

    /** A wall banner of the empire with its emblem. */
    static void banner(ServerLevel level, int x, int y, int z, CapitalCity.Empire empire, Direction facing) {
        Block banner = CapitalCity.banner(base(empire));
        set(level, x, y, z, banner.defaultBlockState().setValue(net.minecraft.world.level.block.WallBannerBlock.FACING, facing));
        decorate(level, new BlockPos(x, y, z), empire);
    }

    /** A tall flagpole on a stone foot with two flags of the empire, one over the other, and a lantern on top. */
    static void flagpole(ServerLevel level, int x, int y, int z, CapitalCity.Empire empire, Direction flying, BlockState foot) {
        for (int dy = 0; dy < 8; dy++) set(level, x, y + dy, z, dy == 0 ? foot : Blocks.SPRUCE_FENCE.defaultBlockState());
        flag(level, x + flying.getStepX(), y + 7, z + flying.getStepZ(), empire, flying);
        flag(level, x + flying.getStepX(), y + 6, z + flying.getStepZ(), empire, flying);
        set(level, x, y + 8, z, empire == CapitalCity.Empire.PARSIVAN ? Blocks.GOLD_BLOCK.defaultBlockState() : Blocks.LANTERN.defaultBlockState());
    }

    /**
     * A great banner: its bar at height y in front of a wall, the cloth (three wide, six long) hanging below it and
     * facing out. The six blocks under the bar are cleared for the cloth.
     */
    static void greatBanner(ServerLevel level, int x, int y, int z, CapitalCity.Empire empire, Direction out) {
        Block block = switch (empire) {
            case PARSIVAN -> SoFEBlocks.PARSIVAN_BANNER.get();
            case KHEMET -> SoFEBlocks.KHEMET_BANNER.get();
            case AUREUM -> SoFEBlocks.AUREUM_BANNER.get();
        };
        set(level, x, y, z, block.defaultBlockState().setValue(com.sofe.block.ClanBanner.FACING, out));
        Direction across = out.getClockWise();
        for (int dy = 1; dy <= 6; dy++) {
            for (int w = -1; w <= 1; w++) set(level, x + across.getStepX() * w, y - dy, z + across.getStepZ() * w, Blocks.AIR.defaultBlockState());
        }
    }
}
