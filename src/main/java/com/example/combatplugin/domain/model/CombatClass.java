package com.example.combatplugin.domain.model;

/**
 * The available combat classes a player can choose.
 * NONE represents the unclassed state (default after reset).
 */
public enum CombatClass {
    NONE("None", "No class selected"),
    SWORD_MASTER("Sword Master", "Melee warrior specializing in weapons and shields"),
    ELEMENTALIST("Elementalist", "Arcane caster focusing on spells and healing"),
    TECHNOCRAT("Technocrat", "Support specialist leveraging gadgets and consumables"),
    NECROMANCER("Necromancer", "Dark summoner commanding undead armies");

    private final String displayName;
    private final String description;

    CombatClass(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }

    /** Case-insensitive lookup by name or display name. Returns NONE if not found. */
    public static CombatClass fromString(String input) {
        if (input == null || input.isBlank()) return NONE;
        String normalized = input.trim().toUpperCase().replace(" ", "_").replace("-", "_");
        for (CombatClass c : values()) {
            if (c.name().equals(normalized)) return c;
        }
        return NONE;
    }
}
