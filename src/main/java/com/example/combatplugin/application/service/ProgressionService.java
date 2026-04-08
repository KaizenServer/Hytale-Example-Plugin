package com.example.combatplugin.application.service;

import com.example.combatplugin.config.CombatConfig;

/**
 * Handles XP and level calculations.
 * All methods are stateless and operate on raw numeric values.
 */
public class ProgressionService {

    private final CombatConfig config;

    public ProgressionService(CombatConfig config) {
        this.config = config;
    }

    /**
     * XP required to reach the given level from scratch.
     * Formula: base * (level ^ exponent)
     * Example defaults: base=100, exponent=1.5 → level 5 requires 100 * 5^1.5 ≈ 1118 XP.
     */
    public long xpRequiredForLevel(int level) {
        if (level <= 1) return 0L;
        return (long) (config.xpCurveBase() * Math.pow(level - 1, config.xpCurveExponent()));
    }

    /**
     * Derives the level from a raw XP total using a binary search over the level table.
     * Caps at maxLevel.
     */
    public int levelFromXp(long totalXp) {
        int level = 1;
        while (level < config.maxLevel() && totalXp >= xpRequiredForLevel(level + 1)) {
            level++;
        }
        return level;
    }

    /** Talent points granted when reaching the given level (always talentPointsPerLevel per level). */
    public int talentPointsForLevel(int level) {
        return config.talentPointsPerLevel();
    }

    /**
     * Computes total talent points the player should have earned up to their current level.
     * Used to recalculate on level-set (admin command).
     */
    public int totalEarnedTalentPoints(int level) {
        // Level 1 grants 0; each subsequent level grants talentPointsPerLevel.
        return Math.max(0, (level - 1) * config.talentPointsPerLevel());
    }
}
