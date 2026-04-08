package com.example.combatplugin.domain.model;

import java.util.List;

/**
 * Declares what gameplay effects a talent produces.
 * A single talent can have both passive modifiers and triggered callbacks.
 *
 * Implementations live in data/TalentEffects.java and return concrete instances.
 */
public interface TalentEffect {
    /** Stat modifiers that are always active while this talent is unlocked. */
    List<CombatModifier> getModifiers();

    /** Event-driven effects that fire on specific gameplay triggers. */
    List<TriggeredEffect> getTriggeredEffects();

    /** Convenience factory: a purely passive talent with only stat modifiers. */
    static TalentEffect passive(List<CombatModifier> modifiers) {
        return new PassiveEffect(modifiers);
    }

    /** Convenience factory: a talent with only triggered effects. */
    static TalentEffect triggered(List<TriggeredEffect.TriggerEntry> entries) {
        return new TriggeredEffect(entries);
    }

    /** Convenience factory: a combined talent with both modifiers and triggers. */
    static TalentEffect combined(List<CombatModifier> modifiers, List<TriggeredEffect> effects) {
        return new CombinedEffect(modifiers, effects);
    }

    record CombinedEffect(List<CombatModifier> getModifiers, List<TriggeredEffect> getTriggeredEffects)
            implements TalentEffect {}
}
