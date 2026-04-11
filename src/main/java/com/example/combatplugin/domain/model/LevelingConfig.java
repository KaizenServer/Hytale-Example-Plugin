package com.example.combatplugin.domain.model;

/**
 * Configuration for the leveling system.
 * Uses the formula: XP(n) = 45n² + 100n
 */
public class LevelingConfig {

    private static final int MIN_LEVEL = 1;
    private static final int MAX_LEVEL = 30;
    private static final int STAT_GAIN_PER_LEVEL = 5; // +5 to each stat per level

    /**
     * Calculate total XP required to reach a specific level.
     * Formula: XP(n) = 45n² + 100n
     *
     * @param level the target level
     * @return total XP required to reach this level
     */
    public static long xpRequiredForLevel(int level) {
        if (level < MIN_LEVEL) return 0;
        if (level > MAX_LEVEL) return xpRequiredForLevel(MAX_LEVEL);
        return 45L * level * level + 100L * level;
    }

    /**
     * Calculate XP required to level up from current level to next level.
     *
     * @param currentLevel the current level
     * @return XP needed to level up
     */
    public static long xpForNextLevel(int currentLevel) {
        if (currentLevel >= MAX_LEVEL) return Long.MAX_VALUE;
        return xpRequiredForLevel(currentLevel + 1) - xpRequiredForLevel(currentLevel);
    }

    /**
     * Determine the current level based on total XP.
     *
     * @param totalXp total XP accumulated
     * @return the player's current level
     */
    public static int levelFromXp(long totalXp) {
        if (totalXp <= 0) return MIN_LEVEL;
        for (int level = MAX_LEVEL; level >= MIN_LEVEL; level--) {
            if (totalXp >= xpRequiredForLevel(level)) {
                return level;
            }
        }
        return MIN_LEVEL;
    }

    /**
     * Stat bonus for a given level.
     * Each stat (health, mana, stamina) gains 5 per level above level 1.
     *
     * @param level the player's level
     * @return stat bonus (0 at level 1, 5 at level 2, etc.)
     */
    public static int statBonusForLevel(int level) {
        return Math.max(0, (level - 1) * STAT_GAIN_PER_LEVEL);
    }

    public static int getMinLevel() {
        return MIN_LEVEL;
    }

    public static int getMaxLevel() {
        return MAX_LEVEL;
    }

    public static int getStatGainPerLevel() {
        return STAT_GAIN_PER_LEVEL;
    }
}
