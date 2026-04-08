package com.example.combatplugin.data;

import com.example.combatplugin.domain.model.CombatClass;
import com.example.combatplugin.domain.model.CombatModifier;
import com.example.combatplugin.domain.model.EffectTrigger;
import com.example.combatplugin.domain.model.StatTarget;
import com.example.combatplugin.domain.model.TalentDefinition;
import com.example.combatplugin.domain.model.TalentEffect;
import com.example.combatplugin.domain.model.TriggeredEffect;

import java.util.List;
import java.util.Map;

/**
 * 16-node talent tree for Sword Master.
 *
 * Tree structure:
 *   T0 (Lvl 0) : sm_iron_will                                        [root, 0/2]
 *   T1 (Lvl 5) : sm_shield_mastery, sm_battle_hardened, sm_aggression [3 nodes]
 *   T2 (Lvl 9) : sm_fortress, sm_resilience, sm_sword_expertise       [3 nodes]
 *   T3 (Lvl14) : sm_perfect_block, sm_counterattack, sm_blade_storm   [3 nodes]
 *   T4 (Lvl14) : sm_unbreakable, sm_vengeance, sm_berserker,
 *                sm_veteran_guard, sm_keen_edge, sm_fortitude          [6 nodes]
 *
 * Prerequisites:
 *   fortress      ← shield_mastery
 *   resilience    ← battle_hardened
 *   sword_expertise ← aggression
 *   perfect_block ← fortress
 *   counterattack ← resilience
 *   blade_storm   ← sword_expertise
 *   unbreakable   ← perfect_block
 *   vengeance     ← counterattack
 *   berserker     ← blade_storm
 *   veteran_guard, keen_edge, fortitude: no prereq (T4 general)
 */
public final class SwordMasterTalents {

    private SwordMasterTalents() {}

    public static void register(Map<String, TalentDefinition> talentRegistry,
                                Map<String, TalentEffect> effectRegistry) {

        // ── T0 — Root (Lvl 0) ────────────────────────────────────────────────
        add(talentRegistry, effectRegistry,
            TalentDefinition.ranked("sm_iron_will", "Iron Will",
                "+20 max health per rank (max 2 ranks).", 1, 2, 0,
                CombatClass.SWORD_MASTER),
            TalentEffect.passive(List.of(
                CombatModifier.flatAdd(StatTarget.HEALTH, 20f, "sm_iron_will")
            ))
        );

        // ── T1 — Tier 1 (Lvl 5) ──────────────────────────────────────────────
        add(talentRegistry, effectRegistry,
            TalentDefinition.tiered("sm_shield_mastery", "Shield Mastery",
                "+15% damage reduction when blocking.", 1, 5,
                CombatClass.SWORD_MASTER),
            TalentEffect.passive(List.of(
                CombatModifier.percentAdd(StatTarget.SHIELD_REDUCTION, 0.15f, "sm_shield_mastery")
            ))
        );

        add(talentRegistry, effectRegistry,
            TalentDefinition.ranked("sm_battle_hardened", "Battle Hardened",
                "-10% all incoming damage per rank (max 2 ranks).", 1, 2, 5,
                CombatClass.SWORD_MASTER),
            TalentEffect.passive(List.of(
                CombatModifier.percentAdd(StatTarget.DAMAGE, -0.10f, "sm_battle_hardened")
            ))
        );

        add(talentRegistry, effectRegistry,
            TalentDefinition.tiered("sm_aggression", "Aggression",
                "+8% melee damage dealt.", 1, 5,
                CombatClass.SWORD_MASTER),
            TalentEffect.passive(List.of(
                CombatModifier.percentAdd(StatTarget.DAMAGE, 0.08f, "sm_aggression")
            ))
        );

        // ── T2 — Tier 2 (Lvl 9) ──────────────────────────────────────────────
        add(talentRegistry, effectRegistry,
            TalentDefinition.tiered("sm_fortress", "Fortress",
                "+15 max health and +5% block reduction.", 1, 9,
                CombatClass.SWORD_MASTER, "sm_shield_mastery"),
            TalentEffect.passive(List.of(
                CombatModifier.flatAdd(StatTarget.HEALTH, 15f, "sm_fortress"),
                CombatModifier.percentAdd(StatTarget.SHIELD_REDUCTION, 0.05f, "sm_fortress")
            ))
        );

        add(talentRegistry, effectRegistry,
            TalentDefinition.ranked("sm_resilience", "Resilience",
                "-5% incoming damage per rank (max 2 ranks).", 1, 2, 9,
                CombatClass.SWORD_MASTER, "sm_battle_hardened"),
            TalentEffect.passive(List.of(
                CombatModifier.percentAdd(StatTarget.DAMAGE, -0.05f, "sm_resilience")
            ))
        );

        add(talentRegistry, effectRegistry,
            TalentDefinition.ranked("sm_sword_expertise", "Sword Expertise",
                "+12% melee damage per rank (max 2 ranks).", 1, 2, 9,
                CombatClass.SWORD_MASTER, "sm_aggression"),
            TalentEffect.passive(List.of(
                CombatModifier.percentAdd(StatTarget.DAMAGE, 0.12f, "sm_sword_expertise")
            ))
        );

        // ── T3 — Tier 3 (Lvl 14) ─────────────────────────────────────────────
        add(talentRegistry, effectRegistry,
            TalentDefinition.tiered("sm_perfect_block", "Perfect Block",
                "While blocking, cancel incoming damage completely once every 10s.",
                1, 14, CombatClass.SWORD_MASTER, "sm_fortress"),
            TriggeredEffect.of(EffectTrigger.ON_TAKE_DAMAGE, "sm_perfect_block", 1.0f)
        );

        add(talentRegistry, effectRegistry,
            TalentDefinition.tiered("sm_counterattack", "Counterattack",
                "When you take damage, deal 50% of it back to the attacker.",
                1, 14, CombatClass.SWORD_MASTER, "sm_resilience"),
            TriggeredEffect.of(EffectTrigger.ON_TAKE_DAMAGE, "sm_counterattack", 0.50f)
        );

        add(talentRegistry, effectRegistry,
            TalentDefinition.tiered("sm_blade_storm", "Blade Storm",
                "+20% melee damage when below 50% health.",
                1, 14, CombatClass.SWORD_MASTER, "sm_sword_expertise"),
            TalentEffect.passive(List.of(
                CombatModifier.percentAdd(StatTarget.DAMAGE, 0.20f, "sm_blade_storm")
            ))
        );

        // ── T4 — Tier 4 (Lvl 14, advanced) ──────────────────────────────────
        add(talentRegistry, effectRegistry,
            TalentDefinition.tiered("sm_unbreakable", "Unbreakable",
                "+30 max health and -10% damage taken.",
                1, 14, CombatClass.SWORD_MASTER, "sm_perfect_block"),
            TalentEffect.passive(List.of(
                CombatModifier.flatAdd(StatTarget.HEALTH, 30f, "sm_unbreakable"),
                CombatModifier.percentAdd(StatTarget.DAMAGE, -0.10f, "sm_unbreakable")
            ))
        );

        add(talentRegistry, effectRegistry,
            TalentDefinition.tiered("sm_vengeance", "Vengeance",
                "Counterattack also heals you for 5% of damage reflected.",
                1, 14, CombatClass.SWORD_MASTER, "sm_counterattack"),
            TriggeredEffect.of(EffectTrigger.ON_TAKE_DAMAGE, "sm_vengeance", 0.05f)
        );

        add(talentRegistry, effectRegistry,
            TalentDefinition.tiered("sm_berserker", "Berserker",
                "+30% melee damage when below 30% health.",
                1, 14, CombatClass.SWORD_MASTER, "sm_blade_storm"),
            TalentEffect.passive(List.of(
                CombatModifier.percentAdd(StatTarget.DAMAGE, 0.30f, "sm_berserker")
            ))
        );

        add(talentRegistry, effectRegistry,
            TalentDefinition.ranked("sm_veteran_guard", "Veteran Guard",
                "+10 max health per rank (max 2 ranks).", 1, 2, 14,
                CombatClass.SWORD_MASTER),
            TalentEffect.passive(List.of(
                CombatModifier.flatAdd(StatTarget.HEALTH, 10f, "sm_veteran_guard")
            ))
        );

        add(talentRegistry, effectRegistry,
            TalentDefinition.ranked("sm_keen_edge", "Keen Edge",
                "+5% melee damage per rank (max 2 ranks).", 1, 2, 14,
                CombatClass.SWORD_MASTER),
            TalentEffect.passive(List.of(
                CombatModifier.percentAdd(StatTarget.DAMAGE, 0.05f, "sm_keen_edge")
            ))
        );

        add(talentRegistry, effectRegistry,
            TalentDefinition.tiered("sm_fortitude", "Fortitude",
                "+10 max stamina.", 1, 14, CombatClass.SWORD_MASTER),
            TalentEffect.passive(List.of(
                CombatModifier.flatAdd(StatTarget.STAMINA, 10f, "sm_fortitude")
            ))
        );
    }

    private static void add(Map<String, TalentDefinition> tr, Map<String, TalentEffect> er,
                             TalentDefinition def, TalentEffect effect) {
        tr.put(def.id(), def);
        er.put(def.id(), effect);
    }
}
