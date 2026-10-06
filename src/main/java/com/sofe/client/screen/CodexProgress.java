package com.sofe.client.screen;

import com.sofe.SoFEMod;
import com.sofe.client.ClientStoryData;
import com.sofe.network.SyncStoryPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * What the Codex may show (docs/Anexos.md, "The Codex"): the furthest act reached and the great foes defeated. In a
 * world it is the story the server sent; from the title screen it is read from the player's own local worlds (their
 * Bearer in each save, as saved by StoryCapability), all of them together.
 */
public record CodexProgress(int act, Set<String> bosses) {
    private static final String CAPABILITY = SoFEMod.MOD_ID + ":story";

    public static CodexProgress current() {
        return ClientStoryData.get().map(CodexProgress::of).orElseGet(CodexProgress::fromLocalWorlds);
    }

    static CodexProgress of(SyncStoryPacket story) {
        return new CodexProgress(story.act(), Set.copyOf(story.bosses()));
    }

    /** The player's Bearers in every local world: in level.dat for a world they host, in playerdata for the rest. */
    static CodexProgress fromLocalWorlds() {
        Minecraft mc = Minecraft.getInstance();
        int act = 0;
        Set<String> bosses = new HashSet<>();
        Path saves = mc.getLevelSource().getBaseDir();
        String me = mc.getUser().getProfileId() == null ? "" : mc.getUser().getProfileId().toString();
        try (Stream<Path> worlds = Files.isDirectory(saves) ? Files.list(saves) : Stream.empty()) {
            for (Path world : worlds.filter(Files::isDirectory).toList()) {
                for (CompoundTag player : new CompoundTag[]{hostPlayer(world.resolve("level.dat").toFile()),
                        read(world.resolve("playerdata").resolve(me + ".dat").toFile())}) {
                    if (player == null) continue;
                    CompoundTag story = player.getCompound("ForgeCaps").getCompound(CAPABILITY);
                    act = Math.max(act, story.getInt("act"));
                    var list = story.getList("bosses", Tag.TAG_STRING);
                    for (int i = 0; i < list.size(); i++) bosses.add(list.getString(i));
                }
            }
        } catch (Exception e) {
            SoFEMod.LOGGER.warn("The Codex could not read the local worlds: {}", e.getMessage());
        }
        return new CodexProgress(act, Set.copyOf(bosses));
    }

    private static CompoundTag hostPlayer(File levelDat) {
        CompoundTag level = read(levelDat);
        return level == null ? null : level.getCompound("Data").getCompound("Player");
    }

    private static CompoundTag read(File file) {
        try {
            return file.isFile() ? NbtIo.readCompressed(file) : null;
        } catch (Exception e) {
            return null;
        }
    }
}
