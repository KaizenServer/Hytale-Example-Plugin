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
 * 16-node talent tree for Technocrat.
 *
 * Tree structure:
 *   T0 (Lvl 0) : tc_overdrive                                                  [root, 0/2]
 *   T1 (Lvl 5) : tc_gadget_cache, tc_quick_hands, tc_field_medic               [3 nodes]
 *   T2 (Lvl 9) : tc_adaptive_armor, tc_cooldown_hacker, tc_combat_medic        [3 nodes]
 *   T3 (Lvl14) : tc_shield_gen, tc_overclock, tc_medkit_pro                    [3 nodes]
 *   T4 (Lvl14) : tc_fortress_mode, tc_overdrive_v2, tc_nano_repair,
 *                tc_tech_mastery, tc_swift_loadout, tc_efficiency               [6 nodes]
 *
 * Prerequisites:
 *   adaptive_armor  ← gadget_cache
 *   cooldown_hacker ← quick_hands
 *   combat_medic    ← field_medic
 *   shield_gen      ← adaptive_armor
 *   overclock       ← cooldown_hacker
 *   medkit_pro      ← combat_medic
 *   fortress_mode   ← shield_gen
 *   overdrive_v2    ← overclock
 *   nano_repair     ← medkit_pro
 *   tech_mastery, swift_loadout, efficiency: no prereq (T4 general)
 */
public final class TechnocratTalents {

    private TechnocratTalents() {}

    public static void register(Map<String, TalentDefinition> talentRegistry,
                                Map<String, TalentEffect> effectRegistry) {

        // ── T0 — Root (Lvl 0) ────────────────────────────────────────────────
        add(talentRegistry, effectRegistry,
            TalentDefinition.ranked("tc_overdrive", "Overdrive",
                "+25% consumable effectiveness per rank (max 2 ranks).", 1, 2, 0,
                CombatClass.TECHNOCRAT),
            TalentEffect.passive(List.of(
                CombatModifier.percentAdd(StatTarget.HEALING, 0.25f, "tc_overdrive")
            ))
        );

        // ── T1 — Tier 1 (Lvl 5) ──────────────────────────────────────────────
        add(talentRegistry, effectRegistry,
            TalentDefinition.tiered("tc_gadget_cache", "Gadget Cache",
                "+2 maximum gadget charges.", 1, 5,
                CombatClass.TECHNOCRAT),
            // INTEGRATION POINT: gadget charge system not yet available — no stat modifier
            TalentEffect.passive(List.of())
        );

        add(talentRegistry, effectRegistry,
            TalentDefinition.tiered("tc_quick_hands", "Quick Hands",
                "+10% stamina (swap speed proxy).", 1, 5,
                CombatClass.TECHNOCRAT),
            TalentEffect.passive(List.of(
                CombatModifier.percentAdd(StatTarget.STAMINA, 0.10f, "tc_quick_hands")
            ))
        );

        add(talentRegistry, effectRegistry,
            TalentDefinition.ranked("tc_field_medic", "Field Medic",
                "+20% healing given and received per rank (max 2 ranks).", 1, 2, 5,
                CombatClass.TECHNOCRAT),
            TalentEffect.passive(List.of(
                CombatModifier.percentAdd(StatTarget.HEALING, 0.20f, "tc_field_medic")
            ))
        );

        // ── T2 — Tier 2 (Lvl 9) ──────────────────────────────────────────────
        add(talentRegistry, effectRegistry,
            TalentDefinition.tiered("tc_adaptive_armor", "Adaptive Armor",
                "-10% all incoming damage.", 1, 9,
                CombatClass.TECHNOCRAT, "tc_gadget_cache"),
            TalentEffect.passive(List.of(
                CombatModifier.percentAdd(StatTarget.DAMAGE, -0.10f, "tc_adaptive_armor")
            ))
        );

        add(talentRegistry, effectRegistry,
            TalentDefinition.tiered("tc_cooldown_hacker", "Cooldown Hacker",
                "-20% gadget cooldown.", 1, 9,
                CombatClass.TECHNOCRAT, "tc_quick_hands"),
            TalentEffect.passive(List.of(
                CombatModifier.percentAdd(StatTarget.COOLDOWN_REDUCTION, 0.20f, "tc_cooldown_hacker")
            ))
        );

        add(talentRegistry, effectRegistry,
            TalentDefinition.ranked("tc_combat_medic", "Combat Medic",
                "+10% healing effectiveness per rank (max 2 ranks).", 1, 2, 9,
                CombatClass.TECHNOCRAT, "tc_field_medic"),
            TalentEffect.passive(List.of(
                CombatModifier.percentAdd(StatTarget.HEALING, 0.10f, "tc_combat_medic")
            ))
        );

        // ── T3 — Tier 3 (Lvl 14) ─────────────────────────────────────────────
        add(talentRegistry, effectRegistry,
            TalentDefinition.tiered("tc_shield_gen", "Shield Generator",
                "Generate a damage shield every 30 seconds (triggered effect).",
                1, 14, CombatClass.TECHNOCRAT, "tc_adaptive_armor"),
            TriggeredEffect.of(EffectTrigger.PASSIVE, "tc_shield_gen", 1.0f)
        );

        add(talentRegistry, effectRegistry,
            TalentDefinition.tiered("tc_overclock", "Overclock",
                "+15% damage for 3 seconds after switching active slot.",
                1, 14, CombatClass.TECHNOCRAT, "tc_cooldown_hacker"),
            TriggeredEffect.of(EffectTrigger.ON_SLOT_SWITCH, "tc_overclock", 0.15f)
        );

        add(talentRegistry, effectRegistry,
            TalentDefinition.tiered("tc_medkit_pro", "Medkit Pro",
                "+25% healing when using a gadget.",
                1, 14, CombatClass.TECHNOCRAT, "tc_combat_medic"),
            TalentEffect.passive(List.of(
                CombatModifier.percentAdd(StatTarget.HEALING, 0.25f, "tc_medkit_pro")
            ))
        );

        // ── T4 — Tier 4 (Lvl 14, advanced) ──────────────────────────────────
        add(talentRegistry, effectRegistry,
            TalentDefinition.tiered("tc_fortress_mode", "Fortress Mode",
                "+20% all damage reduction.", 1, 14,
                CombatClass.TECHNOCRAT, "tc_shield_gen"),
            TalentEffect.passive(List.of(
                CombatModifier.percentAdd(StatTarget.DAMAGE, -0.20f, "tc_fortress_mode")
            ))
        );

        add(talentRegistry, effectRegistry,
            TalentDefinition.tiered("tc_overdrive_v2", "Overdrive V2",
                "+15% all damage for 5 seconds after Overclock activates.",
                1, 14, CombatClass.TECHNOCRAT, "tc_overclock"),
            TriggeredEffect.of(EffectTrigger.ON_SLOT_SWITCH, "tc_overdrive_v2", 0.15f)
        );

        add(talentRegistry, effectRegistry,
            TalentDefinition.tiered("tc_nano_repair", "Nano Repair",
                "Regenerate 5% HP over 5 seconds after using a gadget.",
                1, 14, CombatClass.TECHNOCRAT, "tc_medkit_pro"),
            TriggeredEffect.of(EffectTrigger.ON_HEAL, "tc_nano_repair", 0.05f)
        );

        add(talentRegistry, effectRegistry,
            TalentDefinition.ranked("tc_tech_mastery", "Tech Mastery",
                "+10% gadget effectiveness per rank (max 2 ranks).", 1, 2, 14,
                CombatClass.TECHNOCRAT),
            TalentEffect.passive(List.of(
                CombatModifier.percentAdd(StatTarget.HEALING, 0.10f, "tc_tech_mastery")
            ))
        );

        add(talentRegistry, effectRegistry,
            TalentDefinition.ranked("tc_swift_loadout", "Swift Loadout",
                "+5% stamina per rank (max 2 ranks).", 1, 2, 14,
                CombatClass.TECHNOCRAT),
            TalentEffect.passive(List.of(
                CombatModifier.percentAdd(StatTarget.STAMINA, 0.05f, "tc_swift_loadout")
            ))
        );

        add(talentRegistry, effectRegistry,
            TalentDefinition.tiered("tc_efficiency", "Efficiency",
                "-5% cooldown on all abilities.", 1, 14,
                CombatClass.TECHNOCRAT),
            TalentEffect.passive(List.of(
                CombatModifier.percentAdd(StatTarget.COOLDOWN_REDUCTION, 0.05f, "tc_efficiency")
            ))
        );
    }

    private static void add(Map<String, TalentDefinition> tr, Map<String, TalentEffect> er,
                             TalentDefinition def, TalentEffect effect) {
        tr.put(def.id(), def);
        er.put(def.id(), effect);
    }
}
