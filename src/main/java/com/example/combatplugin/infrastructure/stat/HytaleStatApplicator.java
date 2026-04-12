package com.example.combatplugin.infrastructure.stat;

import com.example.combatplugin.application.port.IStatApplicator;
import com.example.combatplugin.application.service.ModifierService;
import com.example.combatplugin.domain.model.CombatModifier;
import com.example.combatplugin.domain.model.ModifierType;
import com.example.combatplugin.domain.model.PlayerProfile;
import com.example.combatplugin.domain.model.StatTarget;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.List;
import java.util.UUID;

/**
 * Translates domain CombatModifiers into Hytale EntityStatMap API calls.
 *
 * Strategy: remove all plugin-managed named modifiers, then re-apply the full list.
 * Uses putModifier(statIdx, key, StaticModifier) which targets the MAX stat value —
 * the value displayed in the character panel. addStatValue/resetStatValue only affect
 * the current value (like healing), not the max, so they have no visible effect on
 * the character panel.
 */
public class HytaleStatApplicator implements IStatApplicator {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final ModifierService modifierService;

    public HytaleStatApplicator(ModifierService modifierService) {
        this.modifierService = modifierService;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void applyProfileModifiers(UUID uuid, PlayerProfile profile, Object rawStore, Object rawRef) {
        EntityStatMap statMap = resolveStatMap(uuid, rawStore, rawRef);
        if (statMap == null) return;

        // Reset all stats we manage back to base before re-applying
        resetManagedStats(statMap);

        List<CombatModifier> modifiers = modifierService.computeActiveModifiers(profile);
        for (CombatModifier mod : modifiers) {
            applyModifier(statMap, mod, uuid);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void clearAllModifiers(UUID uuid, Object rawStore, Object rawRef) {
        EntityStatMap statMap = resolveStatMap(uuid, rawStore, rawRef);
        if (statMap == null) return;
        resetManagedStats(statMap);
    }

    /**
     * Resolves EntityStatMap from either a Holder (event context) or a Store+Ref pair
     * (command/system context). The IStatApplicator interface uses raw Object params to
     * avoid importing Hytale ECS types into the application layer.
     */
    @SuppressWarnings("unchecked")
    private EntityStatMap resolveStatMap(UUID uuid, Object rawStore, Object rawRef) {
        EntityStatMap statMap;
        if (rawStore instanceof Holder) {
            // Event context (PlayerReadyEvent): rawStore is Holder<EntityStore>, rawRef is null.
            Holder<EntityStore> holder = (Holder<EntityStore>) rawStore;
            statMap = holder.getComponent(EntityStatMap.getComponentType());
        } else {
            // Command/system context: rawStore is Store<EntityStore>, rawRef is Ref<EntityStore>.
            Store<EntityStore> store = (Store<EntityStore>) rawStore;
            Ref<EntityStore> ref = (Ref<EntityStore>) rawRef;
            statMap = store.getComponent(ref, EntityStatMap.getComponentType());
        }
        if (statMap == null) {
            LOGGER.atWarning().log("[CombatPlugin] No EntityStatMap for player %s — skipping.", uuid);
        }
        return statMap;
    }

    /** Key prefix used for all plugin-managed named modifiers. One key per stat index. */
    private static final String MODIFIER_KEY = "combat_plugin_level_bonus";

    // ── Private helpers ────────────────────────────────────────────────────────

    private void resetManagedStats(EntityStatMap statMap) {
        statMap.removeModifier(DefaultEntityStatTypes.getHealth(),   MODIFIER_KEY);
        statMap.removeModifier(DefaultEntityStatTypes.getMana(),     MODIFIER_KEY);
        statMap.removeModifier(DefaultEntityStatTypes.getStamina(),  MODIFIER_KEY);
        statMap.removeModifier(DefaultEntityStatTypes.getSignatureEnergy(), MODIFIER_KEY);
    }

    private void applyModifier(EntityStatMap statMap, CombatModifier mod, UUID uuid) {
        int statIdx = resolveStatIndex(mod.target());
        if (statIdx < 0) return; // DAMAGE / HEALING / virtual stats handled at event time, not here

        switch (mod.type()) {
            case FLAT_ADD -> {
                StaticModifier modifier = new StaticModifier(
                        Modifier.ModifierTarget.MAX,
                        StaticModifier.CalculationType.ADDITIVE,
                        mod.value());
                statMap.putModifier(statIdx, MODIFIER_KEY, modifier);
            }
            case PERCENT_ADD, PERCENT_MULTIPLY -> {
                StaticModifier modifier = new StaticModifier(
                        Modifier.ModifierTarget.MAX,
                        StaticModifier.CalculationType.MULTIPLICATIVE,
                        mod.value());
                statMap.putModifier(statIdx, MODIFIER_KEY, modifier);
            }
        }
    }

    /**
     * Maps a StatTarget to the corresponding Hytale stat index.
     * Returns -1 for virtual / event-time-only stats.
     */
    private int resolveStatIndex(StatTarget target) {
        return switch (target) {
            case HEALTH           -> DefaultEntityStatTypes.getHealth();
            case MANA             -> DefaultEntityStatTypes.getMana();
            case STAMINA          -> DefaultEntityStatTypes.getStamina();
            case SIGNATURE_ENERGY -> DefaultEntityStatTypes.getSignatureEnergy();
            // These are applied at event time by ECS systems, not persisted as base stats:
            case DAMAGE, HEALING, SHIELD_REDUCTION, COOLDOWN_REDUCTION, MAX_SUMMONS -> -1;
        };
    }
}
