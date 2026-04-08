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
 * 16-node talent tree for Necromancer.
 *
 * Tree structure:
 *   T0 (Lvl 0) : nc_death_surge                                              [root, 0/2]
 *   T1 (Lvl 5) : nc_lich_form, nc_soul_harvest, nc_dark_pact                 [3 nodes]
 *   T2 (Lvl 9) : nc_bone_shield, nc_mana_drain, nc_necro_surge               [3 nodes]
 *   T3 (Lvl14) : nc_undying_army, nc_army_of_darkness, nc_death_pact         [3 nodes]
 *   T4 (Lvl14) : nc_lich_king, nc_risen_army, nc_sacrifice,
 *                nc_grave_robber, nc_dark_energy, nc_abomination              [6 nodes]
 *
 * Prerequisites:
 *   bone_shield      ← lich_form
 *   mana_drain       ← soul_harvest
 *   necro_surge      ← dark_pact
 *   undying_army     ← bone_shield
 *   army_of_darkness ← mana_drain
 *   death_pact       ← necro_surge
 *   lich_king        ← undying_army
 *   risen_army       ← army_of_darkness
 *   sacrifice        ← death_pact
 *   grave_robber, dark_energy, abomination: no prereq (T4 general)
 */
public final class NecromancerTalents {

    private NecromancerTalents() {}

    public static void register(Map<String, TalentDefinition> talentRegistry,
                                Map<String, TalentEffect> effectRegistry) {

        // ── T0 — Root (Lvl 0) ────────────────────────────────────────────────
        add(talentRegistry, effectRegistry,
            TalentDefinition.ranked("nc_death_surge", "Death Surge",
                "On kill: summon a skeleton (+1 per rank, max 2 ranks).", 1, 2, 0,
                CombatClass.NECROMANCER),
            // INTEGRATION POINT: ISummonAdapter.spawnSummon() called by DeathSynergySystem
            TriggeredEffect.of(EffectTrigger.ON_KILL, "nc_death_surge", 1.0f)
        );

        // ── T1 — Tier 1 (Lvl 5) ──────────────────────────────────────────────
        add(talentRegistry, effectRegistry,
            TalentDefinition.ranked("nc_lich_form", "Lich Form",
                "+30 max health per rank (max 2 ranks).", 1, 2, 5,
                CombatClass.NECROMANCER),
            TalentEffect.passive(List.of(
                CombatModifier.flatAdd(StatTarget.HEALTH, 30f, "nc_lich_form")
            ))
        );

        add(talentRegistry, effectRegistry,
            TalentDefinition.tiered("nc_soul_harvest", "Soul Harvest",
                "On kill: restore +15 mana.", 1, 5,
                CombatClass.NECROMANCER),
            TriggeredEffect.of(EffectTrigger.ON_KILL, "nc_soul_harvest", 15f)
        );

        add(talentRegistry, effectRegistry,
            TalentDefinition.tiered("nc_dark_pact", "Dark Pact",
                "+10% damage when below 50% health.", 1, 5,
                CombatClass.NECROMANCER),
            TalentEffect.passive(List.of(
                CombatModifier.percentAdd(StatTarget.DAMAGE, 0.10f, "nc_dark_pact")
            ))
        );

        // ── T2 — Tier 2 (Lvl 9) ──────────────────────────────────────────────
        add(talentRegistry, effectRegistry,
            TalentDefinition.tiered("nc_bone_shield", "Bone Shield",
                "+15% damage reduction while a summon is active.", 1, 9,
                CombatClass.NECROMANCER, "nc_lich_form"),
            TalentEffect.passive(List.of(
                CombatModifier.percentAdd(StatTarget.DAMAGE, -0.15f, "nc_bone_shield")
            ))
        );

        add(talentRegistry, effectRegistry,
            TalentDefinition.ranked("nc_mana_drain", "Mana Drain",
                "On kill: restore +10 mana per rank (max 2 ranks).", 1, 2, 9,
                CombatClass.NECROMANCER, "nc_soul_harvest"),
            TriggeredEffect.of(EffectTrigger.ON_KILL, "nc_mana_drain", 10f)
        );

        add(talentRegistry, effectRegistry,
            TalentDefinition.ranked("nc_necro_surge", "Necro Surge",
                "+10% damage per active summon per rank (max 2 ranks).", 1, 2, 9,
                CombatClass.NECROMANCER, "nc_dark_pact"),
            TalentEffect.passive(List.of(
                CombatModifier.percentAdd(StatTarget.DAMAGE, 0.10f, "nc_necro_surge")
            ))
        );

        // ── T3 — Tier 3 (Lvl 14) ─────────────────────────────────────────────
        add(talentRegistry, effectRegistry,
            TalentDefinition.tiered("nc_undying_army", "Undying Army",
                "+8% damage for each active summon.", 1, 14,
                CombatClass.NECROMANCER, "nc_bone_shield"),
            TalentEffect.passive(List.of(
                CombatModifier.percentAdd(StatTarget.DAMAGE, 0.08f, "nc_undying_army")
            ))
        );

        add(talentRegistry, effectRegistry,
            TalentDefinition.tiered("nc_army_of_darkness", "Army of Darkness",
                "+1 maximum active summons.", 1, 14,
                CombatClass.NECROMANCER, "nc_mana_drain"),
            TalentEffect.passive(List.of(
                CombatModifier.flatAdd(StatTarget.MAX_SUMMONS, 1f, "nc_army_of_darkness")
            ))
        );

        add(talentRegistry, effectRegistry,
            TalentDefinition.tiered("nc_death_pact", "Death Pact",
                "+20% damage when below 30% health.", 1, 14,
                CombatClass.NECROMANCER, "nc_necro_surge"),
            TalentEffect.passive(List.of(
                CombatModifier.percentAdd(StatTarget.DAMAGE, 0.20f, "nc_death_pact")
            ))
        );

        // ── T4 — Tier 4 (Lvl 14, advanced) ──────────────────────────────────
        add(talentRegistry, effectRegistry,
            TalentDefinition.tiered("nc_lich_king", "Lich King",
                "+20 max health and +10% all damage.", 1, 14,
                CombatClass.NECROMANCER, "nc_undying_army"),
            TalentEffect.passive(List.of(
                CombatModifier.flatAdd(StatTarget.HEALTH, 20f, "nc_lich_king"),
                CombatModifier.percentAdd(StatTarget.DAMAGE, 0.10f, "nc_lich_king")
            ))
        );

        add(talentRegistry, effectRegistry,
            TalentDefinition.tiered("nc_risen_army", "Risen Army",
                "+2 max summons; summons gain +20% damage.",
                1, 14, CombatClass.NECROMANCER, "nc_army_of_darkness"),
            TalentEffect.passive(List.of(
                CombatModifier.flatAdd(StatTarget.MAX_SUMMONS, 2f, "nc_risen_army"),
                CombatModifier.percentAdd(StatTarget.DAMAGE, 0.20f, "nc_risen_army")
            ))
        );

        add(talentRegistry, effectRegistry,
            TalentDefinition.tiered("nc_sacrifice", "Sacrifice",
                "Convert 50% of active summon HP into a damage burst.",
                1, 14, CombatClass.NECROMANCER, "nc_death_pact"),
            TriggeredEffect.of(EffectTrigger.ON_KILL, "nc_sacrifice", 0.50f)
        );

        add(talentRegistry, effectRegistry,
            TalentDefinition.ranked("nc_grave_robber", "Grave Robber",
                "+15 max health per rank (max 2 ranks).", 1, 2, 14,
                CombatClass.NECROMANCER),
            TalentEffect.passive(List.of(
                CombatModifier.flatAdd(StatTarget.HEALTH, 15f, "nc_grave_robber")
            ))
        );

        add(talentRegistry, effectRegistry,
            TalentDefinition.ranked("nc_dark_energy", "Dark Energy",
                "+10 mana on kill per rank (max 2 ranks).", 1, 2, 14,
                CombatClass.NECROMANCER),
            TriggeredEffect.of(EffectTrigger.ON_KILL, "nc_dark_energy", 10f)
        );

        add(talentRegistry, effectRegistry,
            TalentDefinition.tiered("nc_abomination", "Abomination",
                "Summons deal +15% additional damage.", 1, 14,
                CombatClass.NECROMANCER),
            TalentEffect.passive(List.of(
                CombatModifier.percentAdd(StatTarget.DAMAGE, 0.15f, "nc_abomination")
            ))
        );
    }

    private static void add(Map<String, TalentDefinition> tr, Map<String, TalentEffect> er,
                             TalentDefinition def, TalentEffect effect) {
        tr.put(def.id(), def);
        er.put(def.id(), effect);
    }
}
