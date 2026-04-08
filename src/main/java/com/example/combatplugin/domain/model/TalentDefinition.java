package com.example.combatplugin.domain.model;

import java.util.List;
import java.util.Set;

/**
 * Immutable definition of a single talent.
 * Created at startup by data/*Talents.java files and registered in TalentRegistry.
 */
public record TalentDefinition(
        String id,
        String displayName,
        String description,
        int cost,                        // Talent points required per rank
        List<String> prerequisiteIds,    // Other talent IDs that must be unlocked first
        int maxRank,                     // 1 = single unlock (0/1), 2 = two ranks (0/2)
        int levelRequirement,            // Minimum player level to unlock (0, 5, 9, 14)
        Set<CombatClass> allowedClasses  // Which classes can access this talent
) {
    /** Single-class, single-rank talent with no prerequisites, no level requirement. */
    public static TalentDefinition simple(String id, String displayName, String description,
                                          int cost, CombatClass allowedClass) {
        return new TalentDefinition(id, displayName, description, cost,
                List.of(), 1, 0, Set.of(allowedClass));
    }

    /** Single-class talent with prerequisites, no level requirement. */
    public static TalentDefinition withPrereqs(String id, String displayName, String description,
                                               int cost, CombatClass allowedClass,
                                               String... prereqIds) {
        return new TalentDefinition(id, displayName, description, cost,
                List.of(prereqIds), 1, 0, Set.of(allowedClass));
    }

    /** Full constructor shorthand: single-rank, specific level requirement. */
    public static TalentDefinition tiered(String id, String displayName, String description,
                                          int cost, int levelReq, CombatClass allowedClass,
                                          String... prereqIds) {
        return new TalentDefinition(id, displayName, description, cost,
                List.of(prereqIds), 1, levelReq, Set.of(allowedClass));
    }

    /** Multi-rank talent (up to 2 ranks), with level requirement and optional prerequisites. */
    public static TalentDefinition ranked(String id, String displayName, String description,
                                          int costPerRank, int maxRank, int levelReq,
                                          CombatClass allowedClass, String... prereqIds) {
        return new TalentDefinition(id, displayName, description, costPerRank,
                List.of(prereqIds), maxRank, levelReq, Set.of(allowedClass));
    }

    public boolean isAvailableTo(CombatClass combatClass) {
        return allowedClasses.contains(combatClass);
    }

    public boolean hasPrerequisites() {
        return !prerequisiteIds.isEmpty();
    }

    public boolean isMaxRank(int currentRank) {
        return currentRank >= maxRank;
    }
}
