package com.sofe.client;

import com.sofe.combat.CombatCapability;
import com.sofe.entity.summon.Ally;
import com.sofe.player.PlayerClass;
import com.sofe.player.PlayerClassCapability;
import com.sofe.progression.ProgressionCapability;
import com.sofe.skill.ClassState;
import com.sofe.skill.SkillBook;
import com.sofe.skill.SkillCaster;
import com.sofe.skill.SkillCatalog;
import com.sofe.skill.SkillInfo;
import com.sofe.skill.SkillType;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A development tool, off unless the JVM is started with -Dsofe.skillShots=skill1,skill2,... (or "all"):
 * on a stone floor in the sky it makes the player the Bearer of each skill in turn, teaches that skill,
 * fills the resource and the souls, sets three husks before them and casts it from the Combat Bar as a
 * key press would. A moment later it takes a screenshot (run/screenshots/skill_&lt;id&gt;.png) and writes what
 * happened to run/skillshots.log: the zombies' health and how many allies stand round the player.
 */
public final class SkillShots {
    private static final int SETTLE = 13, WATCH = 40;
    private static List<SkillInfo> skills;
    private static final List<String> LOG = new ArrayList<>();
    private static int ticks, index = -1, wait, phase;
    private static List<Zombie> zombies = new ArrayList<>();

    private SkillShots() {
    }

    public static boolean enabled() {
        return System.getProperty("sofe.skillShots") != null;
    }

    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.getSingleplayerServer() == null) return;
        mc.options.pauseOnLostFocus = false; // the window may be behind others while it runs
        if (mc.screen instanceof net.minecraft.client.gui.screens.PauseScreen) mc.setScreen(null);
        if (skills == null) {
            String wanted = System.getProperty("sofe.skillShots");
            skills = new ArrayList<>(SkillCatalog.all().stream()
                    .filter(s -> s.type() != SkillType.PASSIVE && (wanted.equals("all") || List.of(wanted.split(",")).contains(s.id()))).toList());
            mc.options.hideGui = false;
            var srv = mc.getSingleplayerServer();
            srv.execute(() -> { // from the first moment: unharmed, by day
                ServerPlayer p = srv.getPlayerList().getPlayer(mc.player.getUUID());
                if (p != null) p.setInvulnerable(true);
                srv.overworld().setDayTime(6000);
            });
            mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        }
        if (mc.player.isDeadOrDying()) mc.player.respawn();
        if (++ticks < 200 || wait-- > 0) return;
        var server = mc.getSingleplayerServer();
        if (phase == 3) { // the moment of the cast: the pose and the bolt in flight
            Screenshot.grab(mc.gameDirectory, "skill_" + skills.get(index).id() + "_cast.png", mc.getMainRenderTarget(), m -> { });
            phase = 1;
            wait = WATCH;
            return;
        }
        if (phase == 1) { // the skill has had time to work: take the picture and note the result
            SkillInfo info = skills.get(index);
            Screenshot.grab(mc.gameDirectory, "skill_" + info.id() + ".png", mc.getMainRenderTarget(), m -> { });
            server.execute(() -> LOG.add(report(server.getPlayerList().getPlayer(mc.player.getUUID()), info)));
            phase = 0;
            wait = 5;
            return;
        }
        if (index + 1 >= skills.size() && phase < 4) { // last of all: the skill tree of the last class
            mc.setScreen(new com.sofe.client.screen.SkillTreeScreen());
            phase = 4;
            wait = 15;
            return;
        }
        if (phase == 4) {
            Screenshot.grab(mc.gameDirectory, "skill_tree.png", mc.getMainRenderTarget(), m -> { });
            phase = 5;
            wait = 5;
            return;
        }
        if (++index >= skills.size()) {
            try {
                Files.write(Path.of(mc.gameDirectory.getPath(), "skillshots.log"), LOG);
            } catch (IOException ignored) {
            }
            mc.stop();
            return;
        }
        SkillInfo info = skills.get(index);
        server.execute(() -> prepare(server.getPlayerList().getPlayer(mc.player.getUUID()), info));
        castLater(mc, info);
    }

    private static void castLater(Minecraft mc, SkillInfo info) {
        var server = mc.getSingleplayerServer();
        server.execute(() -> {
            ServerPlayer p = server.getPlayerList().getPlayer(mc.player.getUUID());
            if (p == null) return;
            // a few ticks after preparing, the server casts the skill from its slot
            com.sofe.skill.SkillTasks.run(p, 12, tick -> {
                if (tick == 8) SkillCaster.cast(p, info.type() == SkillType.ULTIMATE ? SkillBook.ULTIMATE_SLOT : 0);
                return true;
            });
        });
        wait = SETTLE;
        phase = 3;
    }

    private static void prepare(ServerPlayer p, SkillInfo info) {
        if (p == null) return;
        ServerLevel level = p.serverLevel();
        level.getServer().setDifficulty(Difficulty.EASY, true);
        level.setDayTime(6000);
        BlockPos floor = new BlockPos(p.getBlockX(), 230, p.getBlockZ());
        for (int dx = -6; dx <= 6; dx++) {
            for (int dz = -4; dz <= 14; dz++) level.setBlockAndUpdate(floor.offset(dx, 0, dz), Blocks.SMOOTH_STONE.defaultBlockState());
        }
        for (var old : level.getEntitiesOfClass(LivingEntity.class, new AABB(floor).inflate(40), e -> !(e instanceof ServerPlayer))) old.discard();
        ClassState.forget(p);
        PlayerClass cls = info.owner();
        PlayerClassCapability.get(p).ifPresent(d -> d.set(cls));
        ProgressionCapability.get(p).ifPresent(prog -> {
            List<String> slots = new ArrayList<>(java.util.Collections.nCopies(SkillBook.SLOTS, ""));
            slots.set(info.type() == SkillType.ULTIMATE ? SkillBook.ULTIMATE_SLOT : 0, info.id());
            prog.skills().load(Map.of(info.id(), info.maxRank()), slots);
        });
        com.sofe.network.SoFENetwork.sendTo(p, new com.sofe.network.SyncClassPacket(java.util.Optional.of(cls)));
        com.sofe.progression.ProgressionHandler.sync(p);
        com.sofe.combat.CombatHandler.refresh(p);
        CombatCapability.get(p).ifPresent(c -> {
            c.resource().ifPresent(r -> r.set(r.max()));
            c.setSouls(10);
            c.cooldowns().start(info.id(), 0, 0);
        });
        p.setHealth(p.getMaxHealth());
        p.setInvulnerable(true); // the tool must not die to whatever wanders by
        String companion = System.getProperty("sofe.companion"); // a companion beside the caster, to see it in game
        if (companion != null) {
            com.sofe.companion.Companions.order(p, "dismiss");
            PlayerClass.byId(companion).ifPresent(c -> com.sofe.companion.Companions.hire(p, c));
        }
        p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 4, true, false));
        p.connection.teleport(floor.getX() + 0.5, floor.getY() + 1, floor.getZ() + 0.5, 0, 10);
        zombies = new ArrayList<>();
        for (int i = -1; i <= 1; i++) {
            Zombie z = EntityType.HUSK.create(level); // husks do not burn in the sun
            if (z == null) continue;
            z.moveTo(floor.getX() + 0.5 + i * 2, floor.getY() + 1, floor.getZ() + 4.5, 180, 0);
            z.setNoAi(true);
            z.setPersistenceRequired();
            level.addFreshEntity(z);
            zombies.add(z);
        }
    }

    private static String report(ServerPlayer p, SkillInfo info) {
        if (p == null) return info.id() + ": no player";
        StringBuilder sb = new StringBuilder(info.owner().id() + "/" + info.id() + ":");
        for (Zombie z : zombies) sb.append(String.format(" zombie %.1f%s", z.getHealth(), z.isAlive() ? "" : " (dead)"));
        long allies = p.serverLevel().getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(16),
                m -> m instanceof Ally a && p.getUUID().equals(a.owner()) || m instanceof com.sofe.entity.summon.BronzeCannon).size();
        sb.append(" | allies ").append(allies);
        CombatCapability.get(p).ifPresent(c -> sb.append(String.format(" | resource %.0f souls %d marks %d", c.resource().map(r -> r.current()).orElse(0f), c.souls(), c.marks())));
        sb.append(String.format(" | health %.1f", p.getHealth()));
        p.getActiveEffects().forEach(e -> sb.append(" ").append(e.getEffect().getDescriptionId().replace("effect.minecraft.", "")));
        return sb.toString();
    }
}
