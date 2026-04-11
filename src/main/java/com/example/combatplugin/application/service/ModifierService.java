package com.example.combatplugin.application.service;

import com.example.combatplugin.domain.model.CombatModifier;
import com.example.combatplugin.domain.model.PlayerProfile;
import com.example.combatplugin.domain.model.TalentEffect;
import com.example.combatplugin.domain.model.TriggeredEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Computes the full set of active modifiers and triggered effects for a player profile.
 * Stateless — always derives the state from the profile's unlocked talent IDs.
 */
public class ModifierService {

    /** Maps talentId → TalentEffect. Populated by TalentEffects.register(). */
    private final Map<String, TalentEffect> effectRegistry;

    public ModifierService(Map<String, TalentEffect> effectRegistry) {
        this.effectRegistry = effectRegistry;
    }

    /**
     * Builds the full list of passive CombatModifiers active for the given profile.
     * Called when talents change and when applying stats on login.
     * Includes both talent-based and level-based stat bonuses.
     */
    public List<CombatModifier> computeActiveModifiers(PlayerProfile profile) {
        List<CombatModifier> result = new ArrayList<>();

        // Add talent-based modifiers
        for (Map.Entry<String, Integer> entry : profile.getTalentRanks().entrySet()) {
            TalentEffect effect = effectRegistry.get(entry.getKey());
            if (effect == null) continue;
            // Each rank adds one full set of modifiers (additive stacking per rank).
            int rank = entry.getValue();
            for (int i = 0; i < rank; i++) {
                result.addAll(effect.getModifiers());
            }
        }

        // Add level-based stat bonuses: +5 HP/Mana and +0.1 Stamina per level above 1
        int levelsAboveOne = Math.max(0, profile.getLevel() - 1);
        if (levelsAboveOne > 0) {
            int hpManaBonus = levelsAboveOne * 5;
            float staminaBonus = levelsAboveOne * 0.1f;
            result.add(CombatModifier.flatAdd(com.example.combatplugin.domain.model.StatTarget.HEALTH, hpManaBonus, "leveling"));
            result.add(CombatModifier.flatAdd(com.example.combatplugin.domain.model.StatTarget.MANA, hpManaBonus, "leveling"));
            result.add(CombatModifier.flatAdd(com.example.combatplugin.domain.model.StatTarget.STAMINA, staminaBonus, "leveling"));
        }

        return result;
    }

    /**
     * Builds the full list of active TriggeredEffects for the given profile.
     * ECS systems iterate this list when dispatching events.
     */
    public List<TriggeredEffect> computeActiveTriggeredEffects(PlayerProfile profile) {
        List<TriggeredEffect> result = new ArrayList<>();
        for (String talentId : profile.getUnlockedTalentIds()) {
            TalentEffect effect = effectRegistry.get(talentId);
            if (effect != null) {
                result.addAll(effect.getTriggeredEffects());
            }
        }
        return result;
    }

    /**
     * Applies additive and multiplicative modifiers to a base damage value.
     * Used by DamageModifierSystem at event time.
     */
    public float applyDamageModifiers(float baseDamage, List<CombatModifier> modifiers) {
        float flatBonus = 0f;
        float percentAdd = 0f;
        float percentMultiply = 1f;

        for (CombatModifier mod : modifiers) {
            switch (mod.target()) {
                case DAMAGE -> {
                    switch (mod.type()) {
                        case FLAT_ADD       -> flatBonus       += mod.value();
                        case PERCENT_ADD    -> percentAdd      += mod.value();
                        case PERCENT_MULTIPLY -> percentMultiply *= (1f + mod.value());
                    }
                }
                default -> { /* other targets handled by IStatApplicator */ }
            }
        }

        return (baseDamage + flatBonus) * (1f + percentAdd) * percentMultiply;
    }

    /**
     * Applies healing modifiers to a base heal value.
     */
    public float applyHealingModifiers(float baseHeal, List<CombatModifier> modifiers) {
        float percentAdd = 0f;
        for (CombatModifier mod : modifiers) {
            if (mod.target() == com.example.combatplugin.domain.model.StatTarget.HEALING
                    && mod.type() == com.example.combatplugin.domain.model.ModifierType.PERCENT_ADD) {
                percentAdd += mod.value();
            }
        }
        return baseHeal * (1f + percentAdd);
    }
}
