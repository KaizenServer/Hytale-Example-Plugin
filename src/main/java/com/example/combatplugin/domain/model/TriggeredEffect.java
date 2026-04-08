package com.example.combatplugin.domain.model;

import java.util.List;

/**
 * A TalentEffect that fires callbacks on specific gameplay events.
 * Can optionally combine with passive modifiers.
 *
 * The effectKey is a unique string identifier that ECS systems use to dispatch
 * the correct logic at runtime (looked up in TalentEffects.TRIGGERED_HANDLERS).
 */
public record TriggeredEffect(List<TriggerEntry> triggers) implements TalentEffect {

    @Override
    public List<CombatModifier> getModifiers() {
        return List.of();
    }

    @Override
    public List<TriggeredEffect> getTriggeredEffects() {
        return List.of(this);
    }

    /**
     * A single (trigger, effectKey, magnitude) tuple.
     *
     * @param trigger   When this fires.
     * @param effectKey Unique key dispatched to the runtime handler map.
     * @param magnitude Numeric parameter passed to the handler (damage amount, heal %, etc.)
     */
    public record TriggerEntry(EffectTrigger trigger, String effectKey, float magnitude) {}

    /** Factory: a single trigger entry. */
    public static TriggeredEffect of(EffectTrigger trigger, String effectKey, float magnitude) {
        return new TriggeredEffect(List.of(new TriggerEntry(trigger, effectKey, magnitude)));
    }
}
