package com.example.combatplugin.infrastructure.event;

import com.example.combatplugin.application.service.ProfileService;
import com.hypixel.hytale.logger.HytaleLogger;

/**
 * Placeholder for gameplay events whose Hytale API classes are not yet confirmed.
 *
 * Each method below corresponds to a future INTEGRATION POINT. Once the API is
 * confirmed after ./gradlew build, replace each method with a proper
 * EntityEventSystem<EntityStore, XxxEvent> or IEvent handler.
 *
 * Events waiting for confirmation:
 *   - SpellCastEvent      → feeds el_arcane_surge, el_elemental_mastery damage bonuses
 *   - ConsumableUsedEvent → feeds tc_overdrive consumable amplification
 *   - HealEvent           → feeds el_lifebind, tc_field_medic healing bonuses
 *   - SummonCreatedEvent  → increments ISummonAdapter active summon count
 *   - SummonDestroyedEvent→ decrements ISummonAdapter active summon count
 *   - ShieldBlockEvent    → feeds sm_shield_mastery, sm_perfect_block
 */
public final class StubCombatEventListener {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public StubCombatEventListener() {}

    // ── INTEGRATION POINT: SpellCastEvent ─────────────────────────────────────
    // TODO: Extend EntityEventSystem<EntityStore, SpellCastEvent>
    // Logic: look up attacker profile, apply DAMAGE modifiers for ELEMENTALIST
    public void onSpellCast(Object event) {
        LOGGER.atInfo().log("[STUB] SpellCastEvent not yet integrated. Implement when confirmed.");
    }

    // ── INTEGRATION POINT: ConsumableUsedEvent ────────────────────────────────
    // TODO: Extend EntityEventSystem<EntityStore, ConsumableUsedEvent>
    // Logic: if player has tc_overdrive, amplify consumable effect by 25%
    public void onConsumableUsed(Object event) {
        LOGGER.atInfo().log("[STUB] ConsumableUsedEvent not yet integrated.");
    }

    // ── INTEGRATION POINT: HealEvent ─────────────────────────────────────────
    // TODO: Extend EntityEventSystem<EntityStore, HealEvent>
    // Logic: if target has el_lifebind, call modifierService.applyHealingModifiers()
    public void onHeal(Object event) {
        LOGGER.atInfo().log("[STUB] HealEvent not yet integrated.");
    }

    // ── INTEGRATION POINT: SummonCreatedEvent ────────────────────────────────
    // TODO: Track active summons in ISummonAdapter on creation
    public void onSummonCreated(Object event) {
        LOGGER.atInfo().log("[STUB] SummonCreatedEvent not yet integrated.");
    }

    // ── INTEGRATION POINT: SummonDestroyedEvent ──────────────────────────────
    // TODO: Decrement summon counter in ISummonAdapter on destruction
    public void onSummonDestroyed(Object event) {
        LOGGER.atInfo().log("[STUB] SummonDestroyedEvent not yet integrated.");
    }

    // ── INTEGRATION POINT: ShieldBlockEvent ──────────────────────────────────
    // TODO: Feed sm_shield_mastery block reduction and sm_perfect_block cancel check
    public void onShieldBlock(Object event) {
        LOGGER.atInfo().log("[STUB] ShieldBlockEvent not yet integrated.");
    }
}
