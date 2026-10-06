package com.sofe.registry;

import com.sofe.SoFEMod;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * The mod's music (docs/Anexos.md, "Music and sound"): one event per region, for the bosses and for the title. Each
 * plays a Minecraft track chosen for its place until the mod's own music exists; assets/sofe/sounds.json says which,
 * so a composer's OGG files replace them there without any code.
 */
public final class SoFESounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, SoFEMod.MOD_ID);

    public static final RegistryObject<SoundEvent> MUSIC_TITLE = music("title");
    public static final RegistryObject<SoundEvent> MUSIC_SULTHARI = music("region.sulthari");
    public static final RegistryObject<SoundEvent> MUSIC_ASHEN_WASTES = music("region.ashen_wastes");
    public static final RegistryObject<SoundEvent> MUSIC_NORDRATH = music("region.nordrath");
    public static final RegistryObject<SoundEvent> MUSIC_VOLCANIC_FORGES = music("region.volcanic_forges");
    public static final RegistryObject<SoundEvent> MUSIC_PARSIVAN = music("region.parsivan");
    public static final RegistryObject<SoundEvent> MUSIC_KHEMET = music("region.khemet");
    public static final RegistryObject<SoundEvent> MUSIC_AUREUM = music("region.aureum");
    public static final RegistryObject<SoundEvent> MUSIC_BOSS = music("boss");
    public static final RegistryObject<SoundEvent> MUSIC_ARCHSIN = music("archsin");
    public static final RegistryObject<SoundEvent> MUSIC_FINAL = music("final");

    private SoFESounds() {
    }

    private static RegistryObject<SoundEvent> music(String name) {
        return SOUNDS.register("music." + name, () -> SoundEvent.createVariableRangeEvent(SoFEMod.id("music." + name)));
    }

    /** A region's music, as vanilla plays a biome's: a while between tracks, never cutting another short. */
    public static Music regionMusic(Holder<SoundEvent> sound) {
        return new Music(sound, 12000, 24000, false);
    }

    /** Fight music: at once, over whatever was playing. */
    public static Music fightMusic(RegistryObject<SoundEvent> sound) {
        return new Music(sound.getHolder().orElseThrow(), 0, 0, true);
    }
}
