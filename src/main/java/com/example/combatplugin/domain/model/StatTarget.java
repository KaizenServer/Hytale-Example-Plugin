package com.example.combatplugin.domain.model;

/**
 * Which game stat a CombatModifier affects.
 * These map to Hytale's EntityStatMap stats plus plugin-level virtual stats.
 */
public enum StatTarget {
    /** Maps to DefaultEntityStatTypes.getHealth() */
    HEALTH,
    /** Maps to DefaultEntityStatTypes.getMana() */
    MANA,
    /** Maps to DefaultEntityStatTypes.getStamina() */
    STAMINA,
    /** Maps to DefaultEntityStatTypes.getSignatureEnergy() */
    SIGNATURE_ENERGY,
    /** Outgoing damage multiplier applied in DamageModifierSystem */
    DAMAGE,
    /** Incoming healing multiplier applied in heal events */
    HEALING,
    /** Incoming damage reduction when blocking (shield-specific) */
    SHIELD_REDUCTION,
    /**
     * Cooldown reduction percentage (placeholder — no native stat confirmed yet).
     * TODO: replace with confirmed Hytale cooldown stat when API is available.
     */
    COOLDOWN_REDUCTION,
    /**
     * Maximum number of summons allowed (plugin-managed counter, not a native Hytale stat).
     * Stored as a virtual stat in PlayerProfileComponent's extra data.
     */
    MAX_SUMMONS
}
