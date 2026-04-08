package com.example.combatplugin.application.usecase;

import com.example.combatplugin.application.port.IStatApplicator;
import com.example.combatplugin.application.service.ProfileService;
import com.example.combatplugin.domain.exception.CombatPluginException;
import com.example.combatplugin.domain.exception.NoClassChosenException;
import com.example.combatplugin.domain.exception.UnknownTalentException;
import com.example.combatplugin.domain.model.PlayerProfile;
import com.example.combatplugin.domain.model.TalentDefinition;

import java.util.Map;
import java.util.UUID;

/**
 * Removes one rank from an already-invested talent, refunding the talent point cost.
 * Right-click action in the talent tree UI.
 *
 * v1 note: no prerequisite chain check on removal for simplicity.
 */
public class RemoveTalentRankUseCase {

    private final ProfileService profileService;
    private final Map<String, TalentDefinition> talentRegistry;
    private final IStatApplicator statApplicator;

    public RemoveTalentRankUseCase(ProfileService profileService,
                                   Map<String, TalentDefinition> talentRegistry,
                                   IStatApplicator statApplicator) {
        this.profileService = profileService;
        this.talentRegistry = talentRegistry;
        this.statApplicator = statApplicator;
    }

    /**
     * Decrements the rank of the given talent by 1 and refunds the point.
     *
     * @return the talent definition (for UI feedback)
     * @throws CombatPluginException if the player has no class or the talent has no ranks invested
     */
    public TalentDefinition execute(UUID uuid, String talentId, Object store, Object ref)
            throws NoClassChosenException, UnknownTalentException, CombatPluginException {

        PlayerProfile profile = profileService.getOrDefault(uuid);
        if (!profile.hasClass()) throw new NoClassChosenException();

        TalentDefinition talent = talentRegistry.get(talentId);
        if (talent == null || !talent.isAvailableTo(profile.getCombatClass())) {
            throw new UnknownTalentException(talentId);
        }

        if (profile.getTalentRank(talentId) <= 0) {
            throw new CombatPluginException("Talent '" + talentId + "' has no ranks invested.");
        }

        PlayerProfile updated = profile.withTalentRankDecremented(talentId);
        profileService.save(uuid, updated);

        statApplicator.applyProfileModifiers(uuid, updated, store, ref);

        return talent;
    }
}
