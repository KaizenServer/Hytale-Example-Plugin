package com.example.combatplugin.domain.model;

/**
 * How a CombatModifier is applied to its target stat.
 *
 * Evaluation order (when combining multiple modifiers of the same stat):
 *   1. Sum all FLAT_ADD values, add to base.
 *   2. Sum all PERCENT_ADD percentages, multiply the result.
 *   3. Multiply each PERCENT_MULTIPLY factor sequentially.
 */
public enum ModifierType {
    /** Add a flat numeric value directly to the stat. */
    FLAT_ADD,
    /** Add a percentage to the effective multiplier (additive with other PERCENT_ADD modifiers). */
    PERCENT_ADD,
    /** Multiply the stat by a factor (multiplicative, compounding). */
    PERCENT_MULTIPLY
}
