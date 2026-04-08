package com.example.combatplugin.application.usecase;

import com.example.combatplugin.application.port.IStatApplicator;
import com.example.combatplugin.application.service.ProfileService;
import com.example.combatplugin.application.service.TalentService;
import com.example.combatplugin.domain.exception.InsufficientLevelException;
import com.example.combatplugin.domain.exception.InsufficientTalentPointsException;
import com.example.combatplugin.domain.exception.NoClassChosenException;
import com.example.combatplugin.domain.exception.TalentAlreadyUnlockedException;
import com.example.combatplugin.domain.exception.TalentPrerequisiteNotMetException;
import com.example.combatplugin.domain.exception.UnknownTalentException;
import com.example.combatplugin.domain.model.PlayerProfile;
import com.example.combatplugin.domain.model.TalentDefinition;

import java.util.UUID;

public class UnlockTalentUseCase {

    private final TalentService talentService;
    private final ProfileService profileService;
    private final IStatApplicator statApplicator;

    public UnlockTalentUseCase(TalentService talentService, ProfileService profileService,
                                IStatApplicator statApplicator) {
        this.talentService = talentService;
        this.profileService = profileService;
        this.statApplicator = statApplicator;
    }

    /**
     * Unlocks a talent for the player, deducting the talent point cost and applying its modifiers.
     *
     * @return The unlocked TalentDefinition (for UI feedback).
     */
    public TalentDefinition execute(UUID uuid, String talentId, Object store, Object ref)
            throws NoClassChosenException, UnknownTalentException, TalentAlreadyUnlockedException,
                   TalentPrerequisiteNotMetException, InsufficientTalentPointsException,
                   InsufficientLevelException {

        PlayerProfile profile = profileService.getOrDefault(uuid);
        TalentDefinition talent = talentService.validateUnlock(profile, talentId);

        PlayerProfile updated = profile.withTalentRankIncremented(talentId);
        profileService.save(uuid, updated);

        // Re-apply all modifiers so the new talent's effects take effect immediately.
        // IStatApplicator.applyProfileModifiers internally recomputes from the profile.
        statApplicator.applyProfileModifiers(uuid, updated, store, ref);

        return talent;
    }
}
