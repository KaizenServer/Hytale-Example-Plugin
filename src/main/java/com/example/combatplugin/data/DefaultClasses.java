package com.example.combatplugin.data;

import com.example.combatplugin.domain.model.ClassDefinition;
import com.example.combatplugin.domain.model.CombatClass;

import java.util.List;
import java.util.Map;

/**
 * Registers the four base combat class definitions.
 * Call register() once during CombatPlugin.setup().
 */
public final class DefaultClasses {

    private DefaultClasses() {}

    public static void register(Map<CombatClass, ClassDefinition> registry) {
        registry.put(CombatClass.SWORD_MASTER, swordMaster());
        registry.put(CombatClass.ELEMENTALIST, elementalist());
        registry.put(CombatClass.TECHNOCRAT,   technocrat());
        registry.put(CombatClass.NECROMANCER,  necromancer());
    }

    private static ClassDefinition swordMaster() {
        return new ClassDefinition(
                CombatClass.SWORD_MASTER,
                "Sword Master",
                "A battle-hardened warrior who excels at melee combat and shield techniques. " +
                "Rewards aggressive play and precise blocking.",
                "Tank",
                "Bruiser",
                List.of(
                        "sm_iron_will",
                        "sm_shield_mastery",
                        "sm_battle_hardened",
                        "sm_counterattack",
                        "sm_sword_expertise",
                        "sm_perfect_block"
                )
        );
    }

    private static ClassDefinition elementalist() {
        return new ClassDefinition(
                CombatClass.ELEMENTALIST,
                "Elementalist",
                "A master of arcane forces who amplifies spells and healing. " +
                "Rewards efficient mana usage and elemental synergy.",
                "Damage Dealer",
                "Healer",
                List.of(
                        "el_arcane_surge",
                        "el_mana_font",
                        "el_efficient_casting",
                        "el_lifebind",
                        "el_elemental_mastery",
                        "el_mana_regen"
                )
        );
    }

    private static ClassDefinition technocrat() {
        return new ClassDefinition(
                CombatClass.TECHNOCRAT,
                "Technocrat",
                "A cunning engineer who leverages gadgets, consumables, and tactical tools. " +
                "Rewards preparation and quick item switching.",
                "Support",
                "Damage Dealer",
                List.of(
                        "tc_overdrive",
                        "tc_gadget_cache",
                        "tc_quick_hands",
                        "tc_overclock",
                        "tc_field_medic",
                        "tc_cooldown_hacker"
                )
        );
    }

    private static ClassDefinition necromancer() {
        return new ClassDefinition(
                CombatClass.NECROMANCER,
                "Necromancer",
                "A dark summoner who commands undead forces. " +
                "Grows stronger the larger their army becomes.",
                "Summoner",
                "Damage Dealer",
                List.of(
                        "nc_death_surge",
                        "nc_undying_army",
                        "nc_soul_harvest",
                        "nc_lich_form",
                        "nc_army_of_darkness",
                        "nc_death_pact"
                )
        );
    }
}
