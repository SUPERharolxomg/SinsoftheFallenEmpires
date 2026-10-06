package com.sofe.world.build;

import com.sofe.registry.SoFEBlocks;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

import static com.sofe.world.build.CapitalCity.AIR;
import static com.sofe.world.build.CapitalCity.b;
import static com.sofe.world.build.CapitalCity.stair;

/**
 * Neferet, the city of Khemet, after its concept (art/concepts/city_khemet.png): the river along the west wall with its
 * quays, palms and boats, and canals off it; north of the plaza the great temple behind two pylons painted with golden
 * ankhs, a hall of columns under a teal dome with gold hieroglyphs, the jackal god at its end; an avenue of sphinxes
 * and two obelisks before it; behind, the Great Pyramid capped in gold and a lesser one; the royal tomb cut into a
 * cliff of red rock, its door between banners of the ankh; stepped pyramids in the south; houses of sandstone, many
 * under little pyramids of teal tiles, with striped awnings; and a pylon gate in the south wall.
 */
final class Neferet {
    private final CapitalCity c;
    private final BlockState carved = b(SoFEBlocks.KHEMET_CARVED_SANDSTONE), glyphs = b(SoFEBlocks.KHEMET_GOLD_HIEROGLYPHS);
    private final BlockState lime = b(SoFEBlocks.KHEMET_PAINTED_LIMESTONE), teal = Blocks.PRISMARINE_BRICKS.defaultBlockState();
    private final BlockState obelisk = b(SoFEBlocks.KHEMET_OBELISK).setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
    private final BlockState smooth = Blocks.SMOOTH_SANDSTONE.defaultBlockState(), gold = Blocks.GOLD_BLOCK.defaultBlockState();
    private final BlockState blueField = Blocks.BLUE_TERRACOTTA.defaultBlockState(), cyan = Blocks.CYAN_TERRACOTTA.defaultBlockState();

    private Neferet(CapitalCity c) {
        this.c = c;
    }

    private static final String[] ANKH = {".###.", "#...#", "#...#", ".###.", "#####", "..#..", "..#..", "..#.."};

    static void landmarks(CapitalCity c) {
        Neferet n = new Neferet(c);
        n.river();
        n.canal();
        n.temple(c.cx, c.cz - 60);
        for (int sx : new int[]{-1, 1}) {
            n.obelisk(c.cx + sx * 6, c.cz - 38, 20);
            for (int z = c.cz - 32; z <= c.cz - 18; z += 14) n.sphinx(c.cx + sx * 12, z, 0.6);
        }
        n.pyramid(c.cx + 70, c.cz - 74, 38, false);
        n.pyramid(c.cx - 64, c.cz - 84, 24, false);
        n.pyramid(c.cx - 84, c.cz + 94, 14, true);
        n.pyramid(c.cx + 84, c.cz + 96, 12, true);
        n.tomb(c.cx + 92, c.cz + 44);
        n.sphinx(c.cx + 30, c.cz - 22, 1.0); // the great sphinx at the foot of the Great Pyramid
    }

    static void southGate(CapitalCity c) {
        new Neferet(c).pylonGate();
    }

    /** An ankh of gold on a field of blue, 7 wide and 10 high, on a wall facing out (its plane across the given axis). */
    private void ankh(int x0, int y0, int z0, Direction facing) {
        Direction right = facing.getClockWise();
        for (int row = -1; row <= ANKH.length; row++) {
            for (int col = -1; col <= 5; col++) {
                boolean on = row >= 0 && row < ANKH.length && col >= 0 && col < 5 && ANKH[row].charAt(col) == '#';
                int x = x0 + right.getStepX() * (col - 2), z = z0 + right.getStepZ() * (col - 2);
                c.put(x, y0 + ANKH.length - 1 - row, z, on ? gold : blueField);
            }
        }
    }

    // ------------------------------------------------------------------ water

    /** The river along the west wall: quays of carved stone, steps down to it, palms, boats, a bridge on the avenue. */
    private void river() {
        int top = c.top, x0 = c.cx - c.a + 8, x1 = c.cx - c.a + 18;
        for (int z = c.cz - c.a + 2; z <= c.cz + c.a - 2; z++) {
            boolean bridge = Math.abs(z - c.cz) <= 3;
            for (int x = x0 - 1; x <= x1 + 1; x++) {
                boolean quay = x == x0 - 1 || x == x1 + 1;
                c.put(x, top - 4, z, carved);
                if (quay) {
                    for (int y = top - 3; y < top; y++) c.put(x, y, z, carved);
                    c.put(x, top, z, bridge ? AIR : Math.floorMod(z, 2) == 0 ? b(SoFEBlocks.KHEMET_SANDSTONE_WALL) : AIR);
                    continue;
                }
                c.put(x, top - 3, z, Blocks.WATER.defaultBlockState());
                c.put(x, top - 2, z, Blocks.WATER.defaultBlockState());
                c.put(x, top - 1, z, bridge ? Blocks.CUT_SANDSTONE.defaultBlockState() : Blocks.WATER.defaultBlockState());
                for (int y = top; y < top + 6; y++) c.put(x, y, z, AIR);
            }
            if (Math.floorMod(z, 7) == 0 && !bridge) c.palm(x1 + 3, z);
            if (Math.floorMod(z, 24) == 12 && !bridge) { // steps down to the water
                for (int k = 0; k < 3; k++) c.put(x1 - k, top - 1 - k, z, stair(b(SoFEBlocks.KHEMET_SANDSTONE_STAIRS), Direction.EAST));
            }
            if (Math.floorMod(z, 31) == 5 && !bridge) boat(x0 + 3 + Math.floorMod(z, 5), z);
        }
        c.claim(x0 - 2, c.cz - c.a, x1 + 4, c.cz + c.a);
    }

    /** The canal along the east-west avenue, fed by the river, with palms on its bank and boats. */
    private void canal() {
        int top = c.top;
        for (int x = c.cx - c.a + 19; x <= c.cx + c.a - 4; x++) {
            if (Math.abs(x - c.cx) < 16) continue; // it runs under the plaza
            for (int w = 6; w <= 8; w++) {
                c.put(x, top - 3, c.cz + w, carved);
                c.put(x, top - 2, c.cz + w, Blocks.WATER.defaultBlockState());
                c.put(x, top - 1, c.cz + w, Blocks.WATER.defaultBlockState());
                for (int y = top; y < top + 3; y++) c.put(x, y, c.cz + w, AIR);
            }
            c.put(x, top - 1, c.cz + 9, carved);
            c.put(x, top, c.cz + 9, Math.floorMod(x, 2) == 0 ? b(SoFEBlocks.KHEMET_SANDSTONE_SLAB) : AIR);
            if (Math.floorMod(x, 9) == 0) c.palm(x, c.cz + 11);
            if (Math.floorMod(x, 37) == 3) boat(x, c.cz + 7);
        }
        c.claim(c.cx - c.a, c.cz + 5, c.cx + c.a, c.cz + 13);
    }

    private void boat(int x, int z) {
        Boat boat = EntityType.BOAT.create(c.level);
        if (boat == null) return;
        boat.setVariant(Boat.Type.JUNGLE);
        boat.moveTo(x + 0.5, c.top - 1, z + 0.5, 90 * c.random.nextInt(4), 0);
        c.level.addFreshEntity(boat);
    }

    // ------------------------------------------------------------------ the great temple

    private void temple(int x0, int z0) {
        int top = c.top, hw = 22, hd = 14, h = 13;
        // the hall of columns: walls of carved sandstone banded with painted limestone and hieroglyphs
        for (int x = x0 - hw; x <= x0 + hw; x++) {
            for (int z = z0 - hd; z <= z0 + hd; z++) {
                boolean edge = Math.abs(x - x0) == hw || Math.abs(z - z0) == hd;
                c.put(x, top - 1, z, Math.floorMod(x + z, 2) == 0 ? lime : smooth);
                for (int dy = 0; dy < h; dy++) {
                    BlockState s = !edge ? AIR : dy == 2 || dy == 9 ? lime : dy == h - 2 ? glyphs : dy == h - 1 ? cyan : carved;
                    c.put(x, top + dy, z, s);
                }
                c.put(x, top + h, z, edge ? glyphs : carved);
                if (edge) c.put(x, top + h + 1, z, Math.floorMod(x + z, 2) == 0 ? b(SoFEBlocks.KHEMET_SANDSTONE_SLAB) : AIR);
            }
        }
        // the columns inside: papyrus shafts with teal capitals, a nave down the middle
        for (int x = x0 - hw + 4; x <= x0 + hw - 4; x += 4) {
            if (Math.abs(x - x0) <= 3) continue;
            for (int z = z0 - hd + 3; z <= z0 + hd - 3; z += 4) {
                for (int dy = 0; dy < h; dy++) c.put(x, top + dy, z, dy == h - 1 ? teal : dy == h - 2 ? glyphs : obelisk);
            }
        }
        // the dome on its square drum with a frieze of gold, two lesser domes, two slender towers
        Architecture.openRoof(c.level, x0, top + h, z0, 8);
        for (int dx = -9; dx <= 9; dx++) for (int dz = -9; dz <= 9; dz++) {
            boolean edge = Math.abs(dx) == 9 || Math.abs(dz) == 9;
            if (!edge) continue;
            for (int dy = 1; dy <= 4; dy++) {
                boolean window = dy >= 2 && dy <= 3 && Math.floorMod(dx + dz, 4) == 2;
                c.put(x0 + dx, top + h + dy, z0 + dz, window ? Blocks.CYAN_STAINED_GLASS_PANE.defaultBlockState() : dy == 4 ? glyphs : carved);
            }
        }
        Architecture.dome(c.level, x0, top + h + 5, z0, 9, teal, Blocks.DARK_PRISMARINE.defaultBlockState(), gold);
        for (int sx : new int[]{-16, 16}) {
            Architecture.drum(c.level, x0 + sx, top + h + 1, z0 - 3, 4, 2, carved, glyphs);
            Architecture.dome(c.level, x0 + sx, top + h + 3, z0 - 3, 4, teal, Blocks.DARK_PRISMARINE.defaultBlockState(), gold);
            Architecture.minaret(c.level, x0 + sx + Integer.signum(sx) * 4, top, z0 - hd - 3, 30, carved, glyphs, teal);
            c.flag(x0 + sx + Integer.signum(sx) * 4, top + 30 + 3, z0 - hd - 3, Direction.EAST);
        }
        // the two pylons before the hall, with golden ankhs, and the portico between them
        int pz = z0 + hd + 1;
        for (int side : new int[]{-1, 1}) {
            int px = x0 + side * 13;
            for (int dy = 0; dy < h + 6; dy++) {
                int half = 8 - dy / 6; // the pylon's walls lean in as they rise
                for (int dx = -half; dx <= half; dx++) {
                    for (int dz = 0; dz <= 5; dz++) {
                        c.put(px + dx, top + dy, pz + dz, dy == h + 5 ? glyphs : dy == h + 4 ? cyan : carved);
                    }
                }
            }
            ankh(px, top + 5, pz + 6, Direction.SOUTH);
            c.greatBanner(px - 6, top + 15, pz + 6, Direction.SOUTH);
            c.greatBanner(px + 6, top + 15, pz + 6, Direction.SOUTH);
            for (int dy = h + 6; dy < h + 11; dy++) c.put(px, top + dy, pz + 3, Blocks.SPRUCE_FENCE.defaultBlockState());
            c.flag(px, top + h + 11, pz + 3, Direction.EAST);
        }
        for (int x = x0 - 4; x <= x0 + 4; x++) {
            for (int z = pz; z <= pz + 5; z++) {
                boolean col = Math.abs(x - x0) == 4 && (z == pz + 1 || z == pz + 4);
                for (int dy = 0; dy < h - 1; dy++) c.put(x, top + dy, z, col ? (dy == h - 2 ? teal : obelisk) : AIR);
                c.put(x, top + h - 1, z, glyphs);
                c.put(x, top + h, z, carved);
            }
        }
        for (int dx = -3; dx <= 3; dx++) for (int dy = 0; dy < 9; dy++) c.put(x0 + dx, top + dy, z0 + hd, Math.abs(dx) == 3 || dy == 8 ? glyphs : AIR);
        templeInside(x0, z0, hw, hd, h);
        c.claim(x0 - hw - 6, z0 - hd - 6, x0 + hw + 6, pz + 8);
    }

    /**
     * Inside the temple: a carpet of the river's colours up the nave to the jackal god on his dais, braziers, offering
     * tables with gold, canopic urns along the walls, banners and lamps.
     */
    private void templeInside(int x0, int z0, int hw, int hd, int h) {
        int top = c.top;
        for (int z = z0 - hd + 5; z <= z0 + hd - 1; z++) {
            for (int dx = -2; dx <= 2; dx++) c.put(x0 + dx, top, z, CapitalCity.carpet(Math.abs(dx) == 2 ? "yellow" : "cyan"));
        }
        // the dais and the god
        for (int step = 0; step < 2; step++) {
            for (int x = x0 - 5 + step; x <= x0 + 5 - step; x++) for (int z = z0 - hd + 1; z <= z0 - hd + 4 - step; z++) c.put(x, top + step, z, glyphs);
        }
        jackal(x0, top + 2, z0 - hd + 2);
        for (int sx : new int[]{-4, 4}) {
            c.brazier(x0 + sx, top + 2, z0 - hd + 2, carved);
            c.put(x0 + sx, top, z0 - hd + 6, Furniture.desk("jungle", Direction.SOUTH));
            c.put(x0 + sx, top + 1, z0 - hd + 6, gold);
        }
        for (int sx : new int[]{-1, 1}) {
            for (int z = z0 - hd + 2; z <= z0 + hd - 2; z += 3) {
                c.put(x0 + sx * (hw - 1), top, z, Blocks.DECORATED_POT.defaultBlockState());
                if (Math.floorMod(z, 6) == 0) c.wallBanner(x0 + sx * (hw - 1), top + 6, z, sx > 0 ? Direction.WEST : Direction.EAST, Blocks.BLUE_WALL_BANNER);
            }
            for (int z = z0 - hd + 4; z <= z0 + hd - 4; z += 8) c.brazier(x0 + sx * 6, top, z, carved);
        }
        Architecture.grandChandelier(c.level, x0, top + h + 10, top + h - 2, z0, 3);
    }

    /** The jackal god seated, black stone with a collar of gold, looking south down the nave. */
    private void jackal(int x, int y, int z) {
        BlockState black = Blocks.POLISHED_BLACKSTONE.defaultBlockState(), dark = Blocks.BLACKSTONE.defaultBlockState();
        for (int dx = -1; dx <= 1; dx++) for (int dz = 0; dz <= 1; dz++) c.put(x + dx, y, z + dz, gold); // the throne
        for (int dy = 1; dy <= 3; dy++) for (int dx = -1; dx <= 1; dx++) c.put(x + dx, y + dy, z, dy == 3 ? gold : black); // the body, the collar
        c.put(x - 1, y + 1, z + 1, Blocks.POLISHED_BLACKSTONE_STAIRS.defaultBlockState().setValue(net.minecraft.world.level.block.StairBlock.FACING, Direction.NORTH));
        c.put(x + 1, y + 1, z + 1, Blocks.POLISHED_BLACKSTONE_STAIRS.defaultBlockState().setValue(net.minecraft.world.level.block.StairBlock.FACING, Direction.NORTH));
        c.put(x, y + 4, z, dark); // the head, the long snout and the tall ears
        c.put(x, y + 4, z + 1, Blocks.POLISHED_BLACKSTONE_SLAB.defaultBlockState());
        c.put(x - 1, y + 5, z, Blocks.BLACKSTONE_WALL.defaultBlockState());
        c.put(x + 1, y + 5, z, Blocks.BLACKSTONE_WALL.defaultBlockState());
        c.put(x, y + 5, z, gold);
        for (int dy = 0; dy <= 6; dy++) c.put(x + 2, y + dy, z, dy == 6 ? gold : obelisk); // his staff
    }

    private void obelisk(int x, int z, int height) {
        int top = c.top;
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
            c.put(x + dx, top, z + dz, carved);
            c.put(x + dx, top + 1, z + dz, glyphs);
        }
        for (int dy = 2; dy < height; dy++) c.put(x, top + dy, z, obelisk);
        c.put(x, top + height, z, gold);
        c.put(x, top + height + 1, z, Blocks.LIGHTNING_ROD.defaultBlockState());
        c.claim(x - 3, z - 3, x + 3, z + 3);
    }

    /**
     * A sphinx lying on a plinth and looking south: a lion's body with its paws stretched out, a man's head in a
     * striped headdress of blue and gold. scale 1 is the great one (24 long), smaller ones line the avenue.
     */
    private void sphinx(int x0, int z0, double scale) {
        int top = c.top;
        int len = (int) Math.round(24 * scale), half = Math.max(2, (int) Math.round(4 * scale)), body = Math.max(3, (int) Math.round(6 * scale));
        int head = Math.max(2, (int) Math.round(3 * scale)), back = z0 - len / 2, front = z0 + len / 2;
        for (int x = x0 - half - 1; x <= x0 + half + 1; x++) for (int z = back - 1; z <= front + 1; z++) c.put(x, top, z, smooth); // the plinth
        int y0 = top + 1;
        int chest = front - (int) Math.round(len * 0.25);
        for (int z = back; z <= chest; z++) {
            double t = (z - back) / (double) (chest - back);
            int hgt = (int) Math.round(body * (0.75 + 0.25 * Math.sin(t * Math.PI))); // the haunches and the shoulders
            for (int x = x0 - half; x <= x0 + half; x++) {
                int h = Math.abs(x - x0) == half ? hgt - 1 : hgt;
                for (int dy = 0; dy < h; dy++) c.put(x, y0 + dy, z, smooth);
            }
        }
        for (int side : new int[]{-1, 1}) { // the paws
            for (int z = chest; z <= front; z++) {
                for (int w = 0; w < Math.max(1, half / 2); w++) {
                    int x = x0 + side * (half - w);
                    c.put(x, y0, z, smooth);
                    if (z < front - 1) c.put(x, y0 + 1, z, Blocks.SANDSTONE.defaultBlockState());
                }
            }
        }
        // the head in its headdress: stripes of lapis and gold down the sides, the face of sandstone
        int hz = chest - 1, hy = y0 + body - 1;
        for (int dx = -head; dx <= head; dx++) {
            for (int dy = 0; dy <= head * 2 + 1; dy++) {
                for (int dz = -head; dz <= 0; dz++) {
                    boolean face = dz == 0 && Math.abs(dx) < head && dy >= 1 && dy <= head * 2 - 1;
                    BlockState s = face ? Blocks.CUT_SANDSTONE.defaultBlockState() : dy % 2 == 0 ? Blocks.LAPIS_BLOCK.defaultBlockState() : gold;
                    c.put(x0 + dx, hy + dy, hz + dz, s);
                }
            }
        }
        for (int dx = -1; dx <= 1; dx += 2) c.put(x0 + dx, hy + head + 1, hz, Blocks.BLACK_TERRACOTTA.defaultBlockState()); // the eyes
        c.put(x0, hy + 1, hz + 1, Blocks.SANDSTONE_WALL.defaultBlockState()); // the beard
        c.put(x0, hy + head * 2 + 2, hz - 1, b(SoFEBlocks.KHEMET_SANDSTONE_SLAB));
        c.claim(x0 - half - 2, back - 2, x0 + half + 2, front + 2);
    }

    /** A pyramid of half-width r, smooth (its last courses of gold) or stepped with a gold cap. */
    private void pyramid(int x0, int z0, int r, boolean stepped) {
        int top = c.top, goldCourses = stepped ? 2 : Math.max(2, r / 9);
        for (int layer = 0; layer <= r; layer++) {
            int half = r - layer;
            for (int dx = -half; dx <= half; dx++) {
                for (int dz = -half; dz <= half; dz++) {
                    boolean edge = Math.abs(dx) == half || Math.abs(dz) == half;
                    if (!edge && layer > 0 && layer < r - goldCourses) { // hollow inside, a floor at the bottom
                        continue;
                    }
                    BlockState s;
                    boolean cap = layer >= r - goldCourses;
                    if (stepped) s = cap ? gold : layer % 2 == 1 && edge ? carved : smooth;
                    else if (edge && half > 0) s = stair(cap ? b(SoFEBlocks.KHEMET_SANDSTONE_STAIRS) : Blocks.SANDSTONE_STAIRS.defaultBlockState(),
                            Math.abs(dx) >= Math.abs(dz) ? (dx > 0 ? Direction.WEST : Direction.EAST) : (dz > 0 ? Direction.NORTH : Direction.SOUTH));
                    else s = cap ? gold : smooth;
                    if (!stepped && cap && edge && half > 0) s = gold; // the gold capstone
                    c.put(x0 + dx, top + layer, z0 + dz, s);
                    if (edge && !stepped && half > 0) c.put(x0 + dx - Integer.signum(dx) * (Math.abs(dx) == half ? 1 : 0),
                            top + layer, z0 + dz - Integer.signum(dz) * (Math.abs(dz) == half ? 1 : 0), smooth); // a solid course behind the steps
                }
            }
        }
        if (!stepped) { // the door in the north face, high up, and the way in
            for (int dy = 6; dy <= 8; dy++) for (int dx = -1; dx <= 1; dx++) c.put(x0 + dx, top + dy, z0 - r + 6, AIR);
            c.put(x0, top + 9, z0 - r + 6, glyphs);
        }
        c.claim(x0 - r - 3, z0 - r - 3, x0 + r + 3, z0 + r + 3);
    }

    /**
     * The royal tomb cut into a cliff of red rock: a forecourt before a sheer face, a door framed in carved stone between
     * two banners of the golden ankh, a passage lit by sconces to the chamber of the king: his sarcophagus of gold, urns,
     * treasure and braziers.
     */
    private void tomb(int x0, int z0) {
        int top = c.top, rx = 16, rz = 24;
        BlockState[] rock = {Blocks.RED_SANDSTONE.defaultBlockState(), Blocks.TERRACOTTA.defaultBlockState(),
                Blocks.ORANGE_TERRACOTTA.defaultBlockState(), Blocks.SMOOTH_RED_SANDSTONE.defaultBlockState()};
        int face = x0 - 10;
        for (int dx = -rx; dx <= rx; dx++) {
            for (int dz = -rz; dz <= rz; dz++) {
                int x = x0 + dx, z = z0 + dz;
                // a massif of red rock: a high broken top with crags, its sides and back falling away over a few blocks
                double edge = Math.max(Math.abs(dz) / (double) rz, (dx + 10) / (double) (rx + 10));
                double fall = Math.max(0, Math.min(1, (1 - edge) * 4.5));
                if (fall <= 0) continue;
                int h = (int) Math.round(fall * (22 + 4 * Math.sin(x * 0.35) * Math.cos(z * 0.27) + 2.5 * Math.sin(z * 0.9 + x * 0.2)));
                if (x < face) h = 0; // the forecourt before the cut face
                for (int dy = 0; dy < h; dy++) {
                    int k = Math.floorMod((int) (dy / 3 + Math.sin(x * 0.3) * 2 + Math.cos(z * 0.25) * 2), rock.length);
                    c.put(x, top + dy, z, x == face ? (dy % 4 == 3 ? Blocks.CUT_RED_SANDSTONE.defaultBlockState() : Blocks.SMOOTH_RED_SANDSTONE.defaultBlockState()) : rock[k]);
                }
                if (x < face) c.put(x, top - 1, z, Blocks.SMOOTH_RED_SANDSTONE.defaultBlockState());
            }
        }
        // the door and its frame
        for (int dz = -4; dz <= 4; dz++) for (int dy = 0; dy <= 10; dy++) {
            boolean opening = Math.abs(dz) <= 2 && dy <= 7;
            c.put(face, top + dy, z0 + dz, opening ? AIR : Math.abs(dz) >= 3 || dy >= 9 ? Blocks.CHISELED_RED_SANDSTONE.defaultBlockState() : glyphs);
        }
        for (int side : new int[]{-1, 1}) {
            ankh(face - 1, top + 3, z0 + side * 8, Direction.WEST);
            c.put(face - 1, top + 2, z0 + side * 5, Furniture.sconce(Direction.WEST));
        }
        // the passage and the chamber
        for (int x = face; x <= x0 + 10; x++) {
            boolean chamber = x >= x0 + 1;
            int w = chamber ? 5 : 2, hgt = chamber ? 7 : 7;
            for (int dz = -w - 1; dz <= w + 1; dz++) {
                for (int dy = -1; dy <= hgt + 1; dy++) {
                    boolean inside = Math.abs(dz) <= w && dy >= 0 && dy <= hgt;
                    BlockState wall = dy == -1 ? lime : Math.abs(dz) == w + 1 && dy == 3 ? glyphs : Blocks.SMOOTH_RED_SANDSTONE.defaultBlockState();
                    if (x == face && !inside) continue;
                    c.put(x, top + dy, z0 + dz, inside ? AIR : wall);
                }
            }
            if (!chamber && Math.floorMod(x - face, 4) == 2) {
                c.put(x, top + 3, z0 - 2, Furniture.sconce(Direction.SOUTH));
                c.put(x, top + 3, z0 + 2, Furniture.sconce(Direction.NORTH));
            }
        }
        int sx = x0 + 6;
        for (int dx = -2; dx <= 2; dx++) { // the sarcophagus
            c.put(sx + dx, top, z0, gold);
            c.put(sx + dx, top + 1, z0, Math.abs(dx) == 2 ? glyphs : lime);
        }
        for (int dz : new int[]{-4, 4}) {
            for (int dx = -1; dx <= 3; dx += 2) c.put(x0 + 4 + dx, top, z0 + dz, Blocks.DECORATED_POT.defaultBlockState());
            c.brazier(x0 + 9, top, z0 + dz, carved);
            c.put(x0 + 2, top, z0 + dz, gold);
            c.put(x0 + 2, top + 1, z0 + dz, Blocks.RAW_GOLD_BLOCK.defaultBlockState());
        }
        c.put(x0 + 10, top + 4, z0, c.hanging());
        c.claim(x0 - rx - 2, z0 - rz - 2, x0 + rx + 2, z0 + rz + 2);
    }

    /** The south gate: two pylons whose walls lean in, golden ankhs on their faces, flags on poles, a lintel of glyphs. */
    private void pylonGate() {
        int top = c.top, gz = c.cz + c.a, h = 20;
        for (int side : new int[]{-1, 1}) {
            int px = c.cx + side * 10;
            for (int dy = 0; dy < h; dy++) {
                int half = 6 - dy / 5;
                for (int dx = -half; dx <= half; dx++) for (int dz = -4; dz <= 4; dz++) {
                    c.put(px + dx, top + dy, gz + dz, dy == h - 1 ? glyphs : dy == h - 2 ? cyan : carved);
                }
            }
            ankh(px, top + 5, gz + 5, Direction.SOUTH);
            ankh(px, top + 5, gz - 5, Direction.NORTH);
            for (int dy = h; dy < h + 6; dy++) c.put(px, top + dy, gz, Blocks.SPRUCE_FENCE.defaultBlockState());
            c.flag(px, top + h + 6, gz, Direction.EAST);
        }
        for (int dx = -4; dx <= 4; dx++) for (int dz = -4; dz <= 4; dz++) { // the lintel between them
            for (int dy = 10; dy <= 12; dy++) c.put(c.cx + dx, top + dy, gz + dz, dy == 11 ? glyphs : carved);
            for (int dy = 0; dy < 10; dy++) if (Math.abs(dx) <= 3) c.put(c.cx + dx, top + dy, gz + dz, AIR);
        }
        c.put(c.cx, top + 13, gz, gold);
    }
}
