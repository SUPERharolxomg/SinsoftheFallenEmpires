package com.sofe.entity.army;

import com.sofe.economy.EconomyCapability;
import com.sofe.economy.EconomyHandler;
import com.sofe.registry.EntityRegistry;
import com.sofe.world.region.Region;
import com.sofe.world.zone.ProtectedZone;
import com.sofe.world.zone.StructurePositions;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

/**
 * The armies of the five empires (docs/Ejercitos.md). Every district of a city has its garrison, a captain, a soldier
 * and an archer in the empire's colors, who keep to their post and fight what comes into it; the fallen are replaced
 * after a while, out of sight. A Bearer can hire soldiers and archers from any captain to follow them (a company of
 * up to three), and bring back the fallen for a smaller price, or hire others in their place.
 */
public final class Army {
    public static final int COMPANY_SIZE = 3;
    public static final int SOLDIER_PRICE = 40, ARCHER_PRICE = 50, REVIVE_PRICE = 25;
    /** How often the garrisons are made whole, and how far away a player must be not to see it. */
    private static final int MUSTER_TICKS = 20 * 120, UNSEEN = 32;
    private static final String COMPANY = "sofe_company", GARRISON_TAG = "sofe_garrison_";
    /** A district's garrison: a captain, three soldiers and three archers (seven; some 35 to 42 a city). */
    public static final List<SoldierEntity.Rank> GARRISON = List.of(SoldierEntity.Rank.CAPTAIN, SoldierEntity.Rank.SOLDIER,
            SoldierEntity.Rank.SOLDIER, SoldierEntity.Rank.SOLDIER, SoldierEntity.Rank.ARCHER, SoldierEntity.Rank.ARCHER, SoldierEntity.Rank.ARCHER);
    /** Sulthari's northern post, on the avenue up to the Observatory (where the Void's northern rift opens in Act I). */
    static final String SULTHARI_NORTH = "sofe:sulthari/observatory_avenue";
    static final int SULTHARI_NORTH_X = 0, SULTHARI_NORTH_Z = -58;

    private Army() {
    }

    // ------------------------------------------------------------------------------------------------ the garrisons

    /** A district's garrison: where it stands and whose army it is. */
    record Post(String key, Region empire, int x, int z) {
    }

    /** Sulthari's districts (its other places are halls and workshops) and the Observatory's avenue; each other city has a center and four quarters. */
    private static final List<String> SULTHARI_DISTRICTS = List.of("sofe:sulthari/plaza", "sofe:sulthari/lower_district",
            "sofe:sulthari/low_bazaar", "sofe:sulthari/training_grounds", "sofe:sulthari/forge");

    /** The posts of every city in the layout. */
    static List<Post> posts(StructurePositions.Layout layout) {
        List<Post> posts = new ArrayList<>();
        for (StructurePositions.Structure city : layout.structures().values()) {
            if (city.zone() != ProtectedZone.Kind.CITY) continue;
            String name = city.id().substring(city.id().indexOf(':') + 1, city.id().indexOf('/'));
            Region empire = Region.byId(name).orElse(null);
            if (empire == null) continue;
            if (empire == Region.SULTHARI) {
                for (String id : SULTHARI_DISTRICTS) layout.structure(id).ifPresent(s -> posts.add(new Post(id, empire, s.x(), s.z())));
                posts.add(new Post(SULTHARI_NORTH, empire, city.x() + SULTHARI_NORTH_X, city.z() + SULTHARI_NORTH_Z));
                continue;
            }
            int qx = city.sizeX() / 4, qz = city.sizeZ() / 4;
            int[][] at = {{0, 0}, {-qx, -qz}, {qx, -qz}, {-qx, qz}, {qx, qz}};
            for (int i = 0; i < at.length; i++) posts.add(new Post(city.id() + "#" + i, empire, city.x() + at[i][0], city.z() + at[i][1]));
        }
        return posts;
    }

    /** Places the garrisons of the posts near enough (with the story places, StoryPlacements.placeNear). */
    public static int placeGarrisons(MinecraftServer server, StructurePositions.Layout layout, BiPredicate<Integer, Integer> near) {
        Musters data = Musters.get(server);
        int placed = 0;
        for (Post post : posts(layout)) {
            if (data.placed.contains(post.key()) || !near.test(post.x(), post.z())) continue;
            if (post.empire() == Region.SULTHARI && !data.defended) continue; // Sulthari's garrisons muster once the Void is driven out

            muster(server.overworld(), post, GARRISON);
            data.placed.add(post.key());
            data.setDirty();
            placed++;
        }
        return placed;
    }

    /** Puts these ranks of a post's garrison on the open street round it, spread in a ring (the captain in the middle). */
    private static void muster(ServerLevel level, Post post, List<SoldierEntity.Rank> ranks) {
        int i = 0;
        for (SoldierEntity.Rank rank : ranks) {
            double angle = i * Math.PI * 2 / Math.max(1, ranks.size() - 1);
            int r = rank == SoldierEntity.Rank.CAPTAIN ? 0 : 4;
            BlockPos at = outdoors(level, post.x() + (int) Math.round(Math.cos(angle) * r), post.z() + (int) Math.round(Math.sin(angle) * r));
            i++;
            if (at == null) continue;
            SoldierEntity soldier = EntityRegistry.SOLDIER.get().create(level);
            if (soldier == null) continue;
            soldier.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, level.random.nextFloat() * 360f, 0f);
            soldier.enlist(post.empire(), rank);
            soldier.post(at);
            soldier.addTag(GARRISON_TAG + post.key());
            level.addFreshEntity(soldier);
        }
    }

    /**
     * Open ground of the street round a point (under the sky, not in water): of the open spots, the lowest, nearest the
     * point; a roof or a fountain's dome is open to the sky too, but higher than the street. Null when there is none.
     */
    public static BlockPos outdoors(ServerLevel level, int x, int z) {
        return outdoors(level, x, z, 16);
    }

    /** The same, looked for no farther than this from the point (a rift keeps to its side of the place). */
    public static BlockPos outdoors(ServerLevel level, int x, int z, int range) {
        BlockPos best = null;
        double bestDistance = 0;
        for (int r = 0; r <= range; r += 2) {
            for (int a = 0; a < (r == 0 ? 1 : 8); a++) {
                int px = x + (int) Math.round(Math.cos(a * Math.PI / 4) * r), pz = z + (int) Math.round(Math.sin(a * Math.PI / 4) * r);
                BlockPos floor = com.sofe.world.Grounding.groundFloor(level, px, pz);
                if (!level.canSeeSky(floor) || !level.getFluidState(floor).isEmpty() || !level.getFluidState(floor.below()).isEmpty()
                        || !level.getBlockState(floor).isAir() || !level.getBlockState(floor.above()).isAir()) continue;
                double d = (double) (px - x) * (px - x) + (double) (pz - z) * (pz - z);
                if (best == null || floor.getY() < best.getY() || floor.getY() == best.getY() && d < bestDistance) {
                    best = floor;
                    bestDistance = d;
                }
            }
        }
        return best;
    }

    /** Disbands every garrison and musters them again round the world spawn (PlaceShots' "army", to look at a change). */
    public static int remuster(MinecraftServer server) {
        ServerLevel level = server.overworld();
        for (var soldier : level.getEntities(EntityRegistry.SOLDIER.get(), s -> s.owner() == null)) soldier.discard();
        Musters data = Musters.get(server);
        data.placed.clear();
        data.defended = true;
        data.setDirty();
        BlockPos spawn = level.getSharedSpawnPos();
        int r = com.sofe.world.StoryPlacements.START_DISTANCE;
        return placeGarrisons(server, StructurePositions.get(), (x, z) -> (long) (x - spawn.getX()) * (x - spawn.getX()) + (long) (z - spawn.getZ()) * (z - spawn.getZ()) <= (long) r * r);
    }

    /**
     * Sulthari has been held against the Void (Act I's invasion won by a Bearer): its garrisons muster in every district
     * near enough to the Bearers, the rest when a Bearer comes near. Until then only the soldiers who hold the rifts stand
     * in the city, three at each (VoidInvasion), so the districts the Void attacks are not crowded with a garrison too.
     */
    public static void sultharisHeld(MinecraftServer server) {
        Musters data = Musters.get(server);
        if (data.defended) return;
        data.defended = true;
        data.setDirty();
        placeGarrisons(server, StructurePositions.get(), (x, z) -> server.overworld().players().stream()
                .anyMatch(p -> (p.getX() - x) * (p.getX() - x) + (p.getZ() - z) * (p.getZ() - z) <= 256.0 * 256.0));
    }

    /** For GameTests: whether Sulthari counts as held, and setting it back. */
    public static boolean sultharisIsHeld(MinecraftServer server) {
        return Musters.get(server).defended;
    }

    public static void forgetSultharisHeld(MinecraftServer server) {
        Musters data = Musters.get(server);
        data.defended = false;
        data.placed.removeIf(k -> k.startsWith("sofe:sulthari/"));
        data.setDirty();
    }

    /** Whether a Bearer has gone past Act I's invasion (in a world from before, or one who won it while offline of this rule). */
    private static boolean pastInvasion(ServerPlayer player) {
        return com.sofe.story.StoryCapability.get(player).map(story -> story.act() >= 2
                || story.quest(com.sofe.quest.QuestEngine.FIRST_QUEST).map(q -> q.completed() || q.step() > 1).orElse(false)).orElse(false);
    }

    /** Every two minutes the garrisons whose fallen nobody is near to see are made whole again. */
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.getServer().getTickCount() % MUSTER_TICKS != 0) return;
        MinecraftServer server = event.getServer();
        if (!com.sofe.world.SoFEWorld.isJourney(server)) return;
        ServerLevel level = server.overworld();
        Musters data = Musters.get(server);
        if (!data.defended && level.players().stream().anyMatch(Army::pastInvasion)) sultharisHeld(server);
        for (Post post : posts(StructurePositions.get())) {
            if (!data.placed.contains(post.key())) continue;
            BlockPos at = new BlockPos(post.x(), 64, post.z());
            if (!level.isLoaded(at)) continue;
            if (level.getNearestPlayer(post.x(), level.getSeaLevel(), post.z(), UNSEEN, false) != null) continue;
            var box = new net.minecraft.world.phys.AABB(post.x() - 48, level.getMinBuildHeight(), post.z() - 48, post.x() + 48, level.getMaxBuildHeight(), post.z() + 48);
            List<SoldierEntity> standing = level.getEntitiesOfClass(SoldierEntity.class, box, s -> s.isAlive() && s.getTags().contains(GARRISON_TAG + post.key()));
            List<SoldierEntity.Rank> missing = new ArrayList<>(GARRISON);
            for (SoldierEntity s : standing) missing.remove(s.rank());
            if (!missing.isEmpty()) muster(level, post, missing);
        }
    }

    /** Which garrisons this world has placed. */
    static final class Musters extends SavedData {
        private static final String NAME = "sofe_army";
        final java.util.Set<String> placed = new java.util.HashSet<>();
        /** Whether Sulthari has been held against the Void, so its garrisons muster. */
        boolean defended;

        static Musters get(MinecraftServer server) {
            return server.overworld().getDataStorage().computeIfAbsent(Musters::load, Musters::new, NAME);
        }

        @Override
        public CompoundTag save(CompoundTag tag) {
            ListTag list = new ListTag();
            for (String key : placed) list.add(net.minecraft.nbt.StringTag.valueOf(key));
            tag.put("placed", list);
            tag.putBoolean("defended", defended);
            return tag;
        }

        private static Musters load(CompoundTag tag) {
            Musters data = new Musters();
            ListTag list = tag.getList("placed", Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) data.placed.add(list.getString(i));
            // a world from before this rule had its garrisons placed already: it counts as held
            data.defended = tag.getBoolean("defended") || data.placed.stream().anyMatch(k -> k.startsWith("sofe:sulthari/"));
            return data;
        }
    }

    // ------------------------------------------------------------------------------------------------ a Bearer's company

    /** One of a Bearer's hired soldiers, standing or fallen. */
    record Member(Region empire, SoldierEntity.Rank rank, boolean fallen) {
        CompoundTag save() {
            CompoundTag t = new CompoundTag();
            t.putString("empire", empire.id());
            t.putString("rank", rank.id());
            t.putBoolean("fallen", fallen);
            return t;
        }

        static Member load(CompoundTag t) {
            return new Member(Region.byId(t.getString("empire")).orElse(Region.SULTHARI), SoldierEntity.Rank.byId(t.getString("rank")), t.getBoolean("fallen"));
        }
    }

    /** The company, kept with the player through death and logging out. */
    static List<Member> company(Player player) {
        ListTag list = kept(player).getList(COMPANY, Tag.TAG_COMPOUND);
        List<Member> members = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) members.add(Member.load(list.getCompound(i)));
        return members;
    }

    private static void save(Player player, List<Member> members) {
        ListTag list = new ListTag();
        for (Member m : members) list.add(m.save());
        CompoundTag kept = kept(player);
        kept.put(COMPANY, list);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, kept);
    }

    private static CompoundTag kept(Player player) {
        return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
    }

    public static int price(SoldierEntity.Rank rank) {
        return rank == SoldierEntity.Rank.ARCHER ? ARCHER_PRICE : SOLDIER_PRICE;
    }

    /** Hires a soldier or an archer of this empire: a free place in the company, or the place of one who fell. */
    public static boolean hire(ServerPlayer player, Region empire, SoldierEntity.Rank rank) {
        List<Member> members = company(player);
        int slot = members.size() < COMPANY_SIZE ? members.size() : firstFallen(members);
        if (slot < 0) {
            tell(player, Component.translatable("message.sofe.army.company_full", COMPANY_SIZE), ChatFormatting.RED);
            return false;
        }
        if (!pay(player, price(rank))) return false;
        Member member = new Member(empire, rank, false);
        if (slot < members.size()) members.set(slot, member);
        else members.add(member);
        save(player, members);
        spawnMember(player, member, slot);
        tell(player, Component.translatable("message.sofe.army.hired", Component.translatable("entity.sofe.soldier." + empire.id() + "." + rank.id()), price(rank)),
                ChatFormatting.GOLD);
        return true;
    }

    /** Brings back every fallen member of the company, for REVIVE_PRICE each. */
    public static boolean revive(ServerPlayer player) {
        List<Member> members = company(player);
        int fallen = (int) members.stream().filter(Member::fallen).count();
        if (fallen == 0) {
            tell(player, Component.translatable("message.sofe.army.none_fallen"), ChatFormatting.GRAY);
            return false;
        }
        if (!pay(player, fallen * REVIVE_PRICE)) return false;
        for (int i = 0; i < members.size(); i++) {
            if (!members.get(i).fallen()) continue;
            members.set(i, new Member(members.get(i).empire(), members.get(i).rank(), false));
            spawnMember(player, members.get(i), i);
        }
        save(player, members);
        tell(player, Component.translatable("message.sofe.army.revived", fallen, fallen * REVIVE_PRICE), ChatFormatting.GOLD);
        return true;
    }

    private static int firstFallen(List<Member> members) {
        for (int i = 0; i < members.size(); i++) if (members.get(i).fallen()) return i;
        return -1;
    }

    private static boolean pay(ServerPlayer player, int price) {
        boolean paid = EconomyCapability.get(player).map(e -> e.spend(price)).orElse(false);
        if (!paid) {
            tell(player, Component.translatable("message.sofe.army.no_dinars", price), ChatFormatting.RED);
            return false;
        }
        EconomyHandler.sync(player);
        return true;
    }

    private static void tell(ServerPlayer player, Component text, ChatFormatting color) {
        player.sendSystemMessage(text.copy().withStyle(color));
    }

    private static void spawnMember(ServerPlayer player, Member member, int slot) {
        ServerLevel level = player.serverLevel();
        SoldierEntity soldier = EntityRegistry.SOLDIER.get().create(level);
        if (soldier == null) return;
        double angle = level.random.nextDouble() * Math.PI * 2;
        soldier.moveTo(player.getX() + Math.cos(angle) * 2, player.getY(), player.getZ() + Math.sin(angle) * 2, player.getYRot(), 0f);
        soldier.enlist(member.empire(), member.rank());
        soldier.hire(player, slot);
        level.addFreshEntity(soldier);
    }

    /** A hired soldier fell: their place stays, to bring them back at a captain or hire another. */
    static void onHiredFell(SoldierEntity soldier) {
        ServerPlayer player = soldier.ownerPlayer(soldier);
        if (player != null) fell(player, soldier.companySlot(), soldier.getDisplayName());
    }

    /** Marks a member of the company as fallen. */
    public static void fell(ServerPlayer player, int slot, Component name) {
        List<Member> members = company(player);
        if (slot < 0 || slot >= members.size()) return;
        Member m = members.get(slot);
        members.set(slot, new Member(m.empire(), m.rank(), true));
        save(player, members);
        tell(player, Component.translatable("message.sofe.army.fell", name), ChatFormatting.RED);
    }

    /** The company's soldiers standing anywhere in the world (GameTests). */
    public static List<SoldierEntity> standingFor(ServerPlayer player) {
        return standing(player);
    }

    /** The company's soldiers standing anywhere in the world. */
    private static List<SoldierEntity> standing(ServerPlayer player) {
        List<SoldierEntity> found = new ArrayList<>();
        for (ServerLevel level : player.server.getAllLevels()) {
            for (var e : level.getEntities(EntityRegistry.SOLDIER.get(), s -> player.getUUID().equals(s.owner()))) found.add(e);
        }
        return found;
    }

    /** The company stands with the Bearer again (logging in, coming back to life, through a portal). */
    private static void regroup(ServerPlayer player) {
        standing(player).forEach(s -> s.discard());
        List<Member> members = company(player);
        for (int i = 0; i < members.size(); i++) if (!members.get(i).fallen()) spawnMember(player, members.get(i), i);
    }

    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) regroup(player);
    }

    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) standing(player).forEach(s -> s.discard());
    }

    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !event.isEndConquered()) regroup(player);
    }

    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) regroup(player);
    }

    /** For GameTests: the company as written in the player's data. */
    public static List<String> describe(Player player) {
        return company(player).stream().map(m -> m.empire().id() + "_" + m.rank().id() + (m.fallen() ? "*" : "")).toList();
    }
}
