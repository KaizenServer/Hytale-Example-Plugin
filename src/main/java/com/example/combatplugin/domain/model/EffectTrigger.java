package com.example.combatplugin.domain.model;

/**
 * When a TriggeredEffect fires during gameplay.
 * PASSIVE means the effect is always-on (no trigger needed).
 */
public enum EffectTrigger {
    /** Always active — used by PassiveEffect, not TriggeredEffect */
    PASSIVE,
    /** Fires when this entity deals damage to another */
    ON_HIT,
    /** Fires when this entity kills another entity */
    ON_KILL,
    /** Fires when this entity receives damage */
    ON_TAKE_DAMAGE,
    /** Fires when this entity is healed */
    ON_HEAL,
    /** Fires when the player switches active hotbar slot */
    ON_SLOT_SWITCH,
    /**
     * Fires when this entity casts a spell.
     * INTEGRATION POINT: depends on SpellCastEvent (not yet confirmed in Hytale API).
     */
    ON_SPELL_CAST,
    /**
     * Fires when this entity uses a consumable.
     * INTEGRATION POINT: depends on ConsumableUsedEvent (not yet confirmed).
     */
    ON_CONSUMABLE_USED,
    /**
     * Fires when a summon owned by this player is created.
     * INTEGRATION POINT: depends on SummonCreatedEvent (not yet confirmed).
     */
    ON_SUMMON_CREATED,
    /**
     * Fires when a summon owned by this player dies.
     * INTEGRATION POINT: depends on SummonDestroyedEvent (not yet confirmed).
     */
    ON_SUMMON_DESTROYED
}
