package com.sofe.client;

import com.sofe.entity.boss.ArchsinEntity;
import com.sofe.entity.boss.NahrazelEntity;
import com.sofe.entity.boss.SoFEBossEntity;
import com.sofe.registry.SoFESounds;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.Music;
import net.minecraftforge.event.TickEvent;

import java.util.Comparator;

/**
 * The music vanilla cannot choose (docs/Anexos.md, "Music and sound"): a boss's fight music while a living boss is
 * near, the Archsins' and Nahrazel's their own, and the mod's theme on its title screen. The regions' music is their
 * biomes' own (SoFEBiomes). Which tracks they are is assets/sofe/sounds.json's to say.
 */
public final class SoFEMusic {
    private static final double FIGHT_RANGE = 40;
    private static Music playing;

    private SoFEMusic() {
    }

    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (event.phase != TickEvent.Phase.END || mc.options.getSoundSourceVolume(net.minecraft.sounds.SoundSource.MUSIC) <= 0) return;
        if (mc.level != null && mc.player != null && mc.level.getGameTime() % 10 != 0) return;
        Music wanted = wanted(mc);
        var music = mc.getMusicManager();
        if (wanted != null) {
            if (!music.isPlayingMusic(wanted)) {
                music.stopPlaying();
                music.startPlaying(wanted);
            }
            playing = wanted;
        } else if (playing != null) { // the fight is over, or the title left: vanilla chooses again
            if (music.isPlayingMusic(playing)) music.stopPlaying();
            playing = null;
        }
    }

    private static Music wanted(Minecraft mc) {
        if (mc.level == null) {
            return mc.screen instanceof com.sofe.client.screen.SoFETitleScreen ? SoFESounds.fightMusic(SoFESounds.MUSIC_TITLE) : null;
        }
        if (mc.player == null) return null;
        return mc.level.getEntitiesOfClass(SoFEBossEntity.class, mc.player.getBoundingBox().inflate(FIGHT_RANGE), b -> b.isAlive())
                .stream().min(Comparator.comparingDouble(b -> b.distanceToSqr(mc.player)))
                .map(b -> b instanceof NahrazelEntity ? SoFESounds.MUSIC_FINAL : b instanceof ArchsinEntity ? SoFESounds.MUSIC_ARCHSIN : SoFESounds.MUSIC_BOSS)
                .map(SoFESounds::fightMusic).orElse(null);
    }
}
