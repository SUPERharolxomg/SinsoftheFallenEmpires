package com.sofe.progression;

/**
 * A player's level, experience and unspent points (UC-03). Plain Java so it is unit tested directly.
 */
public final class ProgressionData {
    private final com.sofe.skill.SkillBook skills = new com.sofe.skill.SkillBook();
    private final AttributeSheet attributes = new AttributeSheet();
    private int level = 1;
    private long xp;
    private int skillPoints;
    private int attributePoints;
    private boolean startingPointsGranted;

    /**
     * The level-1 points (1 skill, 5 attribute), given once when the Bearer is chosen, so the
     * totals at level 30 are 30 and 150 (docs/Clases.md). Returns false if they were already given.
     */
    public boolean grantStartingPoints(LevelingRules rules) {
        if (startingPointsGranted) return false;
        startingPointsGranted = true;
        skillPoints += rules.skillPointsPerLevel();
        attributePoints += rules.attributePointsPerLevel();
        return true;
    }

    public boolean startingPointsGranted() {
        return startingPointsGranted;
    }

    public com.sofe.skill.SkillBook skills() {
        return skills;
    }

    public AttributeSheet attributes() {
        return attributes;
    }

    /** Respec (Sulthari Training Grounds): every skill and attribute point comes back. */
    public void respec() {
        refund(skills.reset(), attributes.reset());
    }

    public int level() {
        return level;
    }

    /** XP gathered toward the next level. */
    public long xp() {
        return xp;
    }

    public int skillPoints() {
        return skillPoints;
    }

    public int attributePoints() {
        return attributePoints;
    }

    /**
     * Adds experience and applies every level-up it causes; XP beyond the maximum level is dropped.
     *
     * @return how many levels were gained
     */
    public int addXp(long amount, LevelingRules rules) {
        return addXp(amount, rules, rules.maxLevel());
    }

    /** As {@link #addXp(long, LevelingRules)}, stopping at this level (the cap of the player's act). */
    public int addXp(long amount, LevelingRules rules, int cap) {
        int top = Math.min(cap, rules.maxLevel());
        if (amount <= 0 || level >= top) return 0;
        xp += amount;
        int gained = 0;
        while (level < top && xp >= rules.xpToNext(level)) {
            xp -= rules.xpToNext(level);
            level++;
            gained++;
            skillPoints += rules.skillPointsPerLevel();
            attributePoints += rules.attributePointsPerLevel();
        }
        if (level >= top) {
            xp = 0;
        }
        return gained;
    }

    public boolean spendSkillPoint() {
        if (skillPoints <= 0) return false;
        skillPoints--;
        return true;
    }

    public boolean spendAttributePoint() {
        if (attributePoints <= 0) return false;
        attributePoints--;
        return true;
    }

    /** Respec: gives back points already spent. */
    public void refund(int skill, int attribute) {
        skillPoints += Math.max(0, skill);
        attributePoints += Math.max(0, attribute);
    }

    public void load(int level, long xp, int skillPoints, int attributePoints, boolean startingPointsGranted) {
        this.startingPointsGranted = startingPointsGranted;
        this.level = Math.max(1, level);
        this.xp = Math.max(0, xp);
        this.skillPoints = Math.max(0, skillPoints);
        this.attributePoints = Math.max(0, attributePoints);
    }
}
