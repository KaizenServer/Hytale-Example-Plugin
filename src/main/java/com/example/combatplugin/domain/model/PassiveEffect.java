package com.example.combatplugin.domain.model;

import java.util.List;

/**
 * A TalentEffect that only produces passive stat modifiers (no event triggers).
 * The simplest and most common effect type.
 */
public record PassiveEffect(List<CombatModifier> modifiers) implements TalentEffect {

    @Override
    public List<CombatModifier> getModifiers() {
        return modifiers;
    }

    @Override
    public List<TriggeredEffect> getTriggeredEffects() {
        return List.of();
    }
}
