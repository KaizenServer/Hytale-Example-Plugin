package com.example.combatplugin.application.usecase;

import com.example.combatplugin.application.service.ProfileService;
import com.example.combatplugin.application.service.ProgressionService;
import com.example.combatplugin.domain.model.PlayerProfile;

import java.util.UUID;

/** Admin use case: forcefully set a player's level, adjusting XP and talent points accordingly. */
public class SetLevelUseCase {

    private final ProfileService profileService;
    private final ProgressionService progressionService;

    public SetLevelUseCase(ProfileService profileService, ProgressionService progressionService) {
        this.profileService = profileService;
        this.progressionService = progressionService;
    }

    public void execute(UUID uuid, int targetLevel) {
        int clamped = Math.max(1, Math.min(targetLevel, 9999));
        PlayerProfile profile = profileService.getOrDefault(uuid);

        long newXp = progressionService.xpRequiredForLevel(clamped);
        int earnedPoints = progressionService.totalEarnedTalentPoints(clamped);
        int spentPoints = profile.getTalentPoints() >= 0
                ? (earnedPoints - profile.getTalentPoints())
                : 0;
        // Keep spent points spent; adjust available pool based on new level
        int newAvailable = Math.max(0, earnedPoints - spentPoints);

        PlayerProfile updated = profile.withLevel(clamped).withXp(newXp).withTalentPoints(newAvailable);
        profileService.save(uuid, updated);
    }
}
