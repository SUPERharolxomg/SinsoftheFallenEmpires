package com.sofe.combat;

import com.sofe.skill.data.ResourceRules;

/** A class resource (Mana, Resolve...): spent by skills, refilled by regeneration and class mechanics. */
public final class ResourcePool {
    private ResourceRules rules;
    private float current;

    public ResourcePool(ResourceRules rules) {
        this.rules = rules;
        this.current = rules.start();
    }

    /** Applies new rules (class chosen, data reloaded) and keeps the current value within the new maximum. */
    public void setRules(ResourceRules rules) {
        this.rules = rules;
        this.current = Math.min(current, rules.max());
    }

    public ResourceRules rules() {
        return rules;
    }

    public float current() {
        return current;
    }

    public int max() {
        return rules.max();
    }

    public boolean canAfford(int cost) {
        return current >= cost;
    }

    /** Spends the cost if there is enough; nothing changes otherwise. */
    public boolean spend(int cost) {
        if (!canAfford(cost)) return false;
        current -= cost;
        return true;
    }

    public void gain(float amount) {
        current = Math.min(rules.max(), current + Math.max(0, amount));
    }

    /** Passive regeneration for one game tick (20 per second). */
    public void tick() {
        gain(rules.regenPerSecond() / 20f);
    }

    public void set(float value) {
        current = Math.max(0, Math.min(rules.max(), value));
    }

    public void reset() {
        current = rules.start();
    }
}
