package com.example.combatplugin.application.service;

import com.example.combatplugin.domain.exception.InsufficientLevelException;
import com.example.combatplugin.domain.exception.InsufficientTalentPointsException;
import com.example.combatplugin.domain.exception.NoClassChosenException;
import com.example.combatplugin.domain.exception.TalentAlreadyUnlockedException;
import com.example.combatplugin.domain.exception.TalentPrerequisiteNotMetException;
import com.example.combatplugin.domain.exception.UnknownTalentException;
import com.example.combatplugin.domain.model.CombatClass;
import com.example.combatplugin.domain.model.PlayerProfile;
import com.example.combatplugin.domain.model.TalentDefinition;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Pure domain logic for talent operations.
 * Validates business rules; does NOT write to the repository.
 */
public class TalentService {

    private final Map<String, TalentDefinition> talentRegistry;

    public TalentService(Map<String, TalentDefinition> talentRegistry) {
        this.talentRegistry = talentRegistry;
    }

    /**
     * Full validation of a talent unlock request.
     * Throws a typed domain exception if any rule is violated.
     */
    public TalentDefinition validateUnlock(PlayerProfile profile, String talentId)
            throws NoClassChosenException, UnknownTalentException,
                   TalentAlreadyUnlockedException, TalentPrerequisiteNotMetException,
                   InsufficientTalentPointsException, InsufficientLevelException {

        if (!profile.hasClass()) throw new NoClassChosenException();

        TalentDefinition talent = talentRegistry.get(talentId);
        if (talent == null) throw new UnknownTalentException(talentId);
        if (!talent.isAvailableTo(profile.getCombatClass())) throw new UnknownTalentException(talentId);

        // Rank check: throws if talent is already at its maximum rank
        int currentRank = profile.getTalentRank(talentId);
        if (talent.isMaxRank(currentRank)) throw new TalentAlreadyUnlockedException(talentId);

        // Level requirement
        if (profile.getLevel() < talent.levelRequirement()) {
            throw new InsufficientLevelException(profile.getLevel(), talent.levelRequirement());
        }

        // Check prerequisites (must have at least rank 1 in each prereq)
        List<String> missing = talent.prerequisiteIds().stream()
                .filter(prereq -> !profile.hasTalent(prereq))
                .collect(Collectors.toList());
        if (!missing.isEmpty()) throw new TalentPrerequisiteNotMetException(talentId, missing);

        if (profile.getTalentPoints() < talent.cost()) {
            throw new InsufficientTalentPointsException(profile.getTalentPoints(), talent.cost());
        }

        return talent;
    }

    /** Returns all talents available to the given class, sorted by prerequisite depth. */
    public List<TalentDefinition> getTalentsForClass(CombatClass combatClass) {
        return talentRegistry.values().stream()
                .filter(t -> t.isAvailableTo(combatClass))
                .collect(Collectors.toList());
    }

    /** Returns total talent points spent, accounting for per-rank costs. */
    public int countSpentPoints(PlayerProfile profile) {
        int total = 0;
        for (Map.Entry<String, Integer> entry : profile.getTalentRanks().entrySet()) {
            TalentDefinition def = talentRegistry.get(entry.getKey());
            if (def != null) total += def.cost() * entry.getValue();
        }
        return total;
    }
}
