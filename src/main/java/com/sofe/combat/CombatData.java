package com.sofe.combat;

import com.sofe.skill.data.ResourceRules;

import java.util.Optional;

/**
 * Combat state of one player: their class resource, cooldowns and runes. The resource only
 * exists once the player has a Bearer; its rules come from the class's skill data file.
 */
public final class CombatData {
    private ResourcePool resource;
    private final CooldownTracker cooldowns = new CooldownTracker();
    private final RuneTracker runes = new RuneTracker();
    /** Resource value read from the save before the class rules were known. */
    private Float savedValue;
    private boolean dirty = true;
    /** Thief Marks on the current target and Necromancer souls held; used by their skills (Sprint 6). */
    private int marks, souls;

    public Optional<ResourcePool> resource() {
        return Optional.ofNullable(resource);
    }

    public CooldownTracker cooldowns() {
        return cooldowns;
    }

    public RuneTracker runes() {
        return runes;
    }

    /** Sets the rules for the player's class; the first time, the saved value (or the start value) is used. */
    public void configure(ResourceRules rules) {
        if (resource == null) {
            resource = new ResourcePool(rules);
            if (savedValue != null) {
                resource.set(savedValue);
                savedValue = null;
            }
        } else {
            resource.setRules(rules);
        }
        markDirty();
    }

    public void loadSavedValue(float value) {
        if (resource != null) {
            resource.set(value);
        } else {
            savedValue = value;
        }
    }

    /** After death: full reset to the class's start value, no cooldowns, no runes. */
    public void resetAfterDeath() {
        resource().ifPresent(ResourcePool::reset);
        cooldowns.clear();
        runes.clear();
        marks = 0;
        souls = 0;
        markDirty();
    }

    public int marks() {
        return marks;
    }

    public int souls() {
        return souls;
    }

    public void setMarks(int value) {
        marks = Math.max(0, value);
        markDirty();
    }

    public void setSouls(int value) {
        souls = Math.max(0, value);
        markDirty();
    }

    public void markDirty() {
        dirty = true;
    }

    /** Whether the client needs an update; clears the flag. */
    public boolean takeDirty() {
        boolean was = dirty;
        dirty = false;
        return was;
    }
}
