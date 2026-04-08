package com.example.combatplugin.application.usecase;

import com.example.combatplugin.application.service.ProfileService;
import com.example.combatplugin.application.service.ProgressionService;
import com.example.combatplugin.domain.model.PlayerProfile;

import java.util.UUID;

public class AwardXpUseCase {

    private final ProfileService profileService;
    private final ProgressionService progressionService;

    public AwardXpUseCase(ProfileService profileService, ProgressionService progressionService) {
        this.profileService = profileService;
        this.progressionService = progressionService;
    }

    /**
     * Adds XP to the player, potentially triggering one or more level-ups.
     *
     * @return A result describing what changed.
     */
    public Result execute(UUID uuid, long xpAmount) {
        PlayerProfile profile = profileService.getOrDefault(uuid);

        long newXp = profile.getXp() + xpAmount;
        int oldLevel = profile.getLevel();
        int newLevel = progressionService.levelFromXp(newXp);
        newLevel = Math.min(newLevel, progressionService.levelFromXp(Long.MAX_VALUE)); // cap guard

        int pointsGranted = 0;
        if (newLevel > oldLevel) {
            // Grant talent points for each level gained
            for (int lvl = oldLevel + 1; lvl <= newLevel; lvl++) {
                pointsGranted += progressionService.talentPointsForLevel(lvl);
            }
        }

        PlayerProfile updated = profile
                .withXp(newXp)
                .withLevel(newLevel)
                .withTalentPoints(profile.getTalentPoints() + pointsGranted);

        profileService.save(uuid, updated);

        return new Result(oldLevel, newLevel, xpAmount, pointsGranted, newLevel > oldLevel);
    }

    /** Immutable result of an XP award, used for UI feedback. */
    public record Result(int oldLevel, int newLevel, long xpAwarded,
                         int talentPointsGranted, boolean leveledUp) {}
}
