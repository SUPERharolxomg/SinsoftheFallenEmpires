package com.sofe.travel;

import net.minecraft.core.BlockPos;
import net.minecraft.world.SimpleContainer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * One player's Waystones and Personal Vault (docs/Jugabilidad.md, G2 and G5). Each player has their
 * own activated Waystones and their own Vault contents, the same from every Vault block.
 */
public final class TravelData {
    public static final int VAULT_ROWS = 3, MAX_VAULT_ROWS = 9;

    /** An activated Waystone in the overworld: its position and the lang key of its name (or null). */
    public record Waystone(BlockPos pos, String nameKey) {
    }

    private final Map<Long, Waystone> waystones = new LinkedHashMap<>();
    private Long lastActivated;
    private final SimpleContainer vault = new SimpleContainer(MAX_VAULT_ROWS * 9);
    private int vaultRows = VAULT_ROWS;

    /** @return true the first time this Waystone is activated */
    public boolean activate(BlockPos pos, String nameKey) {
        boolean isNew = !waystones.containsKey(pos.asLong());
        waystones.put(pos.asLong(), new Waystone(pos.immutable(), nameKey));
        lastActivated = pos.asLong();
        return isNew;
    }

    public boolean isActivated(BlockPos pos) {
        return waystones.containsKey(pos.asLong());
    }

    public Map<Long, Waystone> waystones() {
        return Map.copyOf(waystones);
    }

    public java.util.List<Waystone> waystoneList() {
        return java.util.List.copyOf(waystones.values());
    }

    /** Respawn point after death (docs/Jugabilidad.md, G1). */
    public Optional<Waystone> lastActivated() {
        return lastActivated == null ? Optional.empty() : Optional.ofNullable(waystones.get(lastActivated));
    }

    public void forget(BlockPos pos) {
        waystones.remove(pos.asLong());
        if (lastActivated != null && lastActivated == pos.asLong()) lastActivated = null;
    }

    public SimpleContainer vault() {
        return vault;
    }

    public int vaultRows() {
        return vaultRows;
    }

    /** 27 slots to start, 54 and 81 bought with Dinars later. */
    public void setVaultRows(int rows) {
        this.vaultRows = Math.max(VAULT_ROWS, Math.min(MAX_VAULT_ROWS, rows));
    }

    public void copyFrom(TravelData other) {
        waystones.clear();
        waystones.putAll(other.waystones);
        lastActivated = other.lastActivated;
        vaultRows = other.vaultRows;
        for (int i = 0; i < vault.getContainerSize(); i++) vault.setItem(i, other.vault.getItem(i).copy());
    }

    void restore(Map<Long, Waystone> saved, Long last, int rows) {
        waystones.clear();
        waystones.putAll(saved);
        lastActivated = last != null && saved.containsKey(last) ? last : null;
        setVaultRows(rows);
    }
}
