package com.example.combatplugin.application.usecase;

import com.example.combatplugin.application.port.IStatApplicator;
import com.example.combatplugin.application.service.ProfileService;
import com.example.combatplugin.application.service.TalentService;
import com.example.combatplugin.domain.exception.NoClassChosenException;
import com.example.combatplugin.domain.model.PlayerProfile;

import java.util.UUID;

public class ResetClassUseCase {

    private final ProfileService profileService;
    private final TalentService talentService;
    private final IStatApplicator statApplicator;

    public ResetClassUseCase(ProfileService profileService, TalentService talentService,
                              IStatApplicator statApplicator) {
        this.profileService = profileService;
        this.talentService = talentService;
        this.statApplicator = statApplicator;
    }

    /**
     * Removes the player's class, refunds all spent talent points, and clears all stat modifiers.
     * Level and XP are preserved.
     */
    public void execute(UUID uuid, Object store, Object ref) throws NoClassChosenException {
        PlayerProfile profile = profileService.getOrDefault(uuid);
        if (!profile.hasClass()) throw new NoClassChosenException();

        int refunded = talentService.countSpentPoints(profile);
        // Recalculate total earned points (spent + remaining) and keep that as the new pool
        int totalPoints = profile.getTalentPoints() + refunded;

        PlayerProfile reset = profile.withClassReset(totalPoints);
        profileService.save(uuid, reset);

        statApplicator.clearAllModifiers(uuid, store, ref);
    }
}
