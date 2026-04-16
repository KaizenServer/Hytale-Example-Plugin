package com.example.combatplugin.data;

import com.example.combatplugin.domain.model.ClassDefinition;
import com.example.combatplugin.domain.model.CombatClass;

import java.util.List;
import java.util.Map;

/**
 * Registers the four base combat class definitions.
 * Call register() once during CombatPlugin.setup().
 *
 * description         = short tagline (shown above modifier icons)
 * detailedDescription = full lore/gameplay text (shown below modifier icons)
 */
public final class DefaultClasses {

    private DefaultClasses() {}

    public static void register(Map<CombatClass, ClassDefinition> registry) {
        registry.put(CombatClass.SWORD_MASTER, swordMaster());
        registry.put(CombatClass.ELEMENTALIST, elementalist());
        registry.put(CombatClass.TECHNOCRAT,   technocrat());
        registry.put(CombatClass.NECROMANCER,  summoner());
    }

    private static ClassDefinition swordMaster() {
        return new ClassDefinition(
                CombatClass.SWORD_MASTER,
                "Weaponmaster",
                "A master of arms focused on increasing the effects of weapons, " +
                "such as attacks and signatures.",
                "This versatile class can perform multirole due to increasing the " +
                "efectivity of weapons. Whether by improving melee or ranged damage, " +
                "shield effectiveness, or enhancing elemental staves, weaponmasters " +
                "dominate the battlefield with combat tailored to their weapon of choice.",
                "Tank",
                "Damage Dealer",
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
                "Harness the power of the elements to destroy your enemies or aid " +
                "your allies with strong magic.",
                "From warriors who imbue their weapons with magic, sorcerers who " +
                "destroy their enemies, magical archers, or healers who support their " +
                "allies in combat, elementalists manipulate the elements of the universe " +
                "to use them as a tool of combat.",
                "Damage Dealer",
                "Support",
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
                "An estrategic scientist that uses gadgets, bombs and alchemy " +
                "to boost allies and decay enemies.",
                "Technocrats are fighters obsessed with science. Their arsenal encompasses " +
                "all manner of gadgets, such as bombs or throwable potions, traps, and " +
                "turrets that can destroy their enemies or aid their allies. Furthermore, " +
                "they have perfected these gadgets so that they are no longer merely " +
                "consumables, but genuine weapons of war.",
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

    private static ClassDefinition summoner() {
        return new ClassDefinition(
                CombatClass.NECROMANCER,
                "Summoner",
                "A lord of death that summons aberrations at its command and " +
                "siphons life from its enemies.",
                "Fighting a summoner is not fighting an enemy, but an entire army. " +
                "These combatants use entities to fight alongside them, either to defeat " +
                "their master's enemies or to protect him from harm. Different types of " +
                "entities can be summoned, each with its own effects.",
                "Tank",
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
