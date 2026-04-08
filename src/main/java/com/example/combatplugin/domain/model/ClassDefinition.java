package com.example.combatplugin.domain.model;

import java.util.List;

/**
 * Immutable definition of a combat class.
 * Created at startup by data/*Data.java files and registered in ClassRegistry.
 */
public record ClassDefinition(
        CombatClass id,
        String displayName,
        String description,
        String primaryRole,
        String secondaryRole,
        List<String> baseTalentIds   // Talent IDs available to this class
) {
    /** Returns true if this class has access to the given talent ID. */
    public boolean hasTalent(String talentId) {
        return baseTalentIds.contains(talentId);
    }
}
