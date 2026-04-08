package com.example.combatplugin.domain.model;

/**
 * An atomic stat modification produced by a talent.
 * Immutable value object.
 *
 * @param target     Which stat is affected.
 * @param type       How the value is combined with the base stat.
 * @param value      The numeric magnitude (flat units or fraction, e.g. 0.20 for +20%).
 * @param sourceId   The talent ID that produced this modifier — used to remove it on reset.
 */
public record CombatModifier(StatTarget target, ModifierType type, float value, String sourceId) {

    /** Creates a flat addition modifier. value is in stat units (e.g. 20.0f for +20 HP). */
    public static CombatModifier flatAdd(StatTarget target, float value, String sourceId) {
        return new CombatModifier(target, ModifierType.FLAT_ADD, value, sourceId);
    }

    /** Creates an additive percent modifier. value is a fraction (e.g. 0.20f for +20%). */
    public static CombatModifier percentAdd(StatTarget target, float value, String sourceId) {
        return new CombatModifier(target, ModifierType.PERCENT_ADD, value, sourceId);
    }

    /** Creates a multiplicative percent modifier. value is a fraction (e.g. 0.15f for ×1.15). */
    public static CombatModifier percentMultiply(StatTarget target, float value, String sourceId) {
        return new CombatModifier(target, ModifierType.PERCENT_MULTIPLY, value, sourceId);
    }
}
