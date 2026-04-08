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
 * 16-node talent tree for Elementalist.
 *
 * Tree structure:
 *   T0 (Lvl 0) : el_arcane_surge                                            [root, 0/2]
 *   T1 (Lvl 5) : el_mana_font, el_efficient_casting, el_mental_acuity        [3 nodes]
 *   T2 (Lvl 9) : el_deep_reserves, el_mana_regen, el_elemental_mastery       [3 nodes]
 *   T3 (Lvl14) : el_mana_shield, el_lifebind, el_overload                    [3 nodes]
 *   T4 (Lvl14) : el_arcane_bastion, el_healing_surge, el_spell_weaving,
 *                el_arcane_focus, el_wisdom, el_clarity                       [6 nodes]
 */
public final class ElementalistTalents {

    private ElementalistTalents() {}

    public static void register(Map<String, TalentDefinition> talentRegistry,
                                Map<String, TalentEffect> effectRegistry) {

        // ── T0 — Root (Lvl 0) ────────────────────────────────────────────────
        add(talentRegistry, effectRegistry,
            TalentDefinition.ranked("el_arcane_surge", "Arcane Surge",
                "+20% spell damage per rank (max 2 ranks).", 1, 2, 0,
                CombatClass.ELEMENTALIST),
            TalentEffect.passive(List.of(
                CombatModifier.percentAdd(StatTarget.DAMAGE, 0.20f, "el_arcane_surge")
            ))
        );

        // ── T1 — Tier 1 (Lvl 5) ──────────────────────────────────────────────
        add(talentRegistry, effectRegistry,
            TalentDefinition.ranked("el_mana_font", "Mana Font",
                "+25 max mana per rank (max 2 ranks).", 1, 2, 5,
                CombatClass.ELEMENTALIST),
            TalentEffect.passive(List.of(
                CombatModifier.flatAdd(StatTarget.MANA, 25f, "el_mana_font")
            ))
        );

        add(talentRegistry, effectRegistry,
            TalentDefinition.tiered("el_efficient_casting", "Efficient Casting",
                "-20% mana cost on all spells.", 1, 5,
                CombatClass.ELEMENTALIST),
            TalentEffect.passive(List.of(
                CombatModifier.percentAdd(StatTarget.MANA, -0.20f, "el_efficient_casting")
            ))
        );

        add(talentRegistry, effectRegistry,
            TalentDefinition.tiered("el_mental_acuity", "Mental Acuity",
                "+5% spell damage.", 1, 5,
                CombatClass.ELEMENTALIST),
            TalentEffect.passive(List.of(
                CombatModifier.percentAdd(StatTarget.DAMAGE, 0.05f, "el_mental_acuity")
            ))
        );

        // ── T2 — Tier 2 (Lvl 9) ──────────────────────────────────────────────
        add(talentRegistry, effectRegistry,
            TalentDefinition.ranked("el_deep_reserves", "Deep Reserves",
                "+15 max mana per rank (max 2 ranks).", 1, 2, 9,
                CombatClass.ELEMENTALIST, "el_mana_font"),
            TalentEffect.passive(List.of(
                CombatModifier.flatAdd(StatTarget.MANA, 15f, "el_deep_reserves")
            ))
        );

        add(talentRegistry, effectRegistry,
            TalentDefinition.tiered("el_mana_regen", "Mana Regen",
                "+5 stamina (passive mana restoration proxy).", 1, 9,
                CombatClass.ELEMENTALIST, "el_efficient_casting"),
            TalentEffect.passive(List.of(
                CombatModifier.flatAdd(StatTarget.STAMINA, 5f, "el_mana_regen")
            ))
        );

        add(talentRegistry, effectRegistry,
            TalentDefinition.ranked("el_elemental_mastery", "Elemental Mastery",
                "+15% spell damage per rank (max 2 ranks).", 1, 2, 9,
                CombatClass.ELEMENTALIST, "el_mental_acuity"),
            TalentEffect.passive(List.of(
                CombatModifier.percentMultiply(StatTarget.DAMAGE, 0.15f, "el_elemental_mastery")
            ))
        );

        // ── T3 — Tier 3 (Lvl 14) ─────────────────────────────────────────────
        add(talentRegistry, effectRegistry,
            TalentDefinition.tiered("el_mana_shield", "Mana Shield",
                "Absorb incoming damage using mana (triggered effect).",
                1, 14, CombatClass.ELEMENTALIST, "el_deep_reserves"),
            TriggeredEffect.of(EffectTrigger.ON_TAKE_DAMAGE, "el_mana_shield", 1.0f)
        );

        add(talentRegistry, effectRegistry,
            TalentDefinition.tiered("el_lifebind", "Lifebind",
                "+30% healing received.", 1, 14,
                CombatClass.ELEMENTALIST, "el_mana_regen"),
            TalentEffect.passive(List.of(
                CombatModifier.percentAdd(StatTarget.HEALING, 0.30f, "el_lifebind")
            ))
        );

        add(talentRegistry, effectRegistry,
            TalentDefinition.tiered("el_overload", "Overload",
                "+25% spell damage burst when at full mana.",
                1, 14, CombatClass.ELEMENTALIST, "el_elemental_mastery"),
            TriggeredEffect.of(EffectTrigger.ON_HIT, "el_overload", 0.25f)
        );

        // ── T4 — Tier 4 (Lvl 14, advanced) ──────────────────────────────────
        add(talentRegistry, effectRegistry,
            TalentDefinition.tiered("el_arcane_bastion", "Arcane Bastion",
                "+20% block effectiveness using mana as a barrier.",
                1, 14, CombatClass.ELEMENTALIST, "el_mana_shield"),
            TalentEffect.passive(List.of(
                CombatModifier.percentAdd(StatTarget.SHIELD_REDUCTION, 0.20f, "el_arcane_bastion")
            ))
        );

        add(talentRegistry, effectRegistry,
            TalentDefinition.tiered("el_healing_surge", "Healing Surge",
                "+15% additional healing output.", 1, 14,
                CombatClass.ELEMENTALIST, "el_lifebind"),
            TalentEffect.passive(List.of(
                CombatModifier.percentAdd(StatTarget.HEALING, 0.15f, "el_healing_surge")
            ))
        );

        add(talentRegistry, effectRegistry,
            TalentDefinition.tiered("el_spell_weaving", "Spell Weaving",
                "+20% spell damage at the cost of 10% mana per cast.",
                1, 14, CombatClass.ELEMENTALIST, "el_overload"),
            TalentEffect.passive(List.of(
                CombatModifier.percentAdd(StatTarget.DAMAGE, 0.20f, "el_spell_weaving")
            ))
        );

        add(talentRegistry, effectRegistry,
            TalentDefinition.ranked("el_arcane_focus", "Arcane Focus",
                "+10 max mana per rank (max 2 ranks).", 1, 2, 14,
                CombatClass.ELEMENTALIST),
            TalentEffect.passive(List.of(
                CombatModifier.flatAdd(StatTarget.MANA, 10f, "el_arcane_focus")
            ))
        );

        add(talentRegistry, effectRegistry,
            TalentDefinition.ranked("el_wisdom", "Wisdom",
                "+5% spell damage per rank (max 2 ranks).", 1, 2, 14,
                CombatClass.ELEMENTALIST),
            TalentEffect.passive(List.of(
                CombatModifier.percentAdd(StatTarget.DAMAGE, 0.05f, "el_wisdom")
            ))
        );

        add(talentRegistry, effectRegistry,
            TalentDefinition.tiered("el_clarity", "Clarity",
                "-5% mana cost on all spells.", 1, 14,
                CombatClass.ELEMENTALIST),
            TalentEffect.passive(List.of(
                CombatModifier.percentAdd(StatTarget.MANA, -0.05f, "el_clarity")
            ))
        );
    }

    private static void add(Map<String, TalentDefinition> tr, Map<String, TalentEffect> er,
                             TalentDefinition def, TalentEffect effect) {
        tr.put(def.id(), def);
        er.put(def.id(), effect);
    }
}
