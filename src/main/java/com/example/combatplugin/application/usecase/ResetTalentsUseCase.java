package com.example.combatplugin.application.usecase;

import com.example.combatplugin.application.port.IStatApplicator;
import com.example.combatplugin.application.service.ProfileService;
import com.example.combatplugin.application.service.TalentService;
import com.example.combatplugin.domain.exception.NoClassChosenException;
import com.example.combatplugin.domain.model.PlayerProfile;

import java.util.UUID;

public class ResetTalentsUseCase {

    private final ProfileService profileService;
    private final TalentService talentService;
    private final IStatApplicator statApplicator;

    public ResetTalentsUseCase(ProfileService profileService, TalentService talentService,
                                IStatApplicator statApplicator) {
        this.profileService = profileService;
        this.talentService = talentService;
        this.statApplicator = statApplicator;
    }

    /**
     * Refunds all talent points for the player's current class.
     * Class is preserved; no cost in v1.
     */
    public void execute(UUID uuid, Object store, Object ref) throws NoClassChosenException {
        PlayerProfile profile = profileService.getOrDefault(uuid);
        if (!profile.hasClass()) throw new NoClassChosenException();

        int refunded = talentService.countSpentPoints(profile);
        int totalPoints = profile.getTalentPoints() + refunded;

        PlayerProfile reset = profile.withTalentsReset(totalPoints);
        profileService.save(uuid, reset);

        // Clear all modifiers — the player now has no talents active
        statApplicator.clearAllModifiers(uuid, store, ref);
    }
}
