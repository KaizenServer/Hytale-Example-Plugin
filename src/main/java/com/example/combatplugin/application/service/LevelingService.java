package com.example.combatplugin.application.service;

import com.example.combatplugin.domain.model.LevelingConfig;
import com.example.combatplugin.domain.model.PlayerProfile;

/**
 * Pure domain service for leveling logic.
 * Handles XP gains, level-ups, talent point allocation, and stat reward calculations.
 */
public class LevelingService {

    /**
     * Add XP to a player and return an updated profile.
     * If the player levels up, allocate 1 talent point per level gained.
     *
     * @param profile current player profile
     * @param xpGain amount of XP to add
     * @return updated profile (or same profile if no level-up)
     */
    public PlayerProfile addXpAndCheckLevelUp(PlayerProfile profile, long xpGain) {
        if (xpGain <= 0) {
            return profile;
        }

        int oldLevel = profile.getLevel();
        long newXp = profile.getXp() + xpGain;
        int newLevel = LevelingConfig.levelFromXp(newXp);

        if (newLevel > oldLevel) {
            // Player leveled up
            int levelsMissed = newLevel - oldLevel;
            int talentPointsGained = levelsMissed; // 1 talent point per level
            return profile
                    .withXp(newXp)
                    .withLevel(newLevel)
                    .withTalentPoints(profile.getTalentPoints() + talentPointsGained);
        } else {
            // No level-up, just add XP
            return profile.withXp(newXp);
        }
    }

    /**
     * Get the stat bonus (health, mana, stamina) for a player's current level.
     * Used to calculate what modifiers should be active.
     *
     * @param profile player profile
     * @return stat bonus value
     */
    public int getStatBonusForProfile(PlayerProfile profile) {
        return LevelingConfig.statBonusForLevel(profile.getLevel());
    }

    /**
     * Get XP progress towards the next level.
     *
     * @param profile player profile
     * @return object with current level, XP, and XP to next level
     */
    public LevelProgressInfo getLevelProgress(PlayerProfile profile) {
        int level = profile.getLevel();
        long totalXp = profile.getXp();
        long xpForCurrentLevel = LevelingConfig.xpRequiredForLevel(level);
        long xpForNextLevel = LevelingConfig.xpRequiredForLevel(level + 1);
        long xpProgress = totalXp - xpForCurrentLevel;
        long xpNeeded = xpForNextLevel - xpForCurrentLevel;

        return new LevelProgressInfo(level, totalXp, xpProgress, xpNeeded);
    }

    /**
     * Data class for level progression info.
     */
    public static class LevelProgressInfo {
        public final int level;
        public final long totalXp;
        public final long xpProgress;      // XP earned towards current level
        public final long xpNeeded;        // XP needed for next level

        public LevelProgressInfo(int level, long totalXp, long xpProgress, long xpNeeded) {
            this.level = level;
            this.totalXp = totalXp;
            this.xpProgress = xpProgress;
            this.xpNeeded = xpNeeded;
        }

        public int getProgressPercent() {
            if (xpNeeded == 0) return 0;
            return (int) ((xpProgress * 100) / xpNeeded);
        }
    }
}
