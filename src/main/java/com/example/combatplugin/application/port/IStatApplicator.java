package com.example.combatplugin.application.port;

import com.example.combatplugin.domain.model.PlayerProfile;

import java.util.UUID;

/**
 * Contract for applying combat modifiers to live game stats.
 *
 * Implementations translate CombatModifier domain objects into Hytale EntityStatMap calls.
 * The store/ref context is passed from command or system callers — the applicator
 * itself does not hold ECS references.
 *
 * NOTE: This interface accepts raw Object parameters for store/ref to avoid
 * importing Hytale ECS types into the application layer. Implementations cast internally.
 * TODO: Replace Object params with a proper IGamePlayerContext once the pattern stabilises.
 */
public interface IStatApplicator {

    /**
     * Recomputes and applies all stat modifiers derived from the player's current profile.
     * Replaces any previously applied modifiers for this player.
     *
     * @param uuid    The player's unique ID.
     * @param profile The current profile (determines which talents are active).
     * @param store   The world's EntityStore (passed as Object to avoid Hytale import in port).
     * @param ref     The player's entity Ref (passed as Object).
     */
    void applyProfileModifiers(UUID uuid, PlayerProfile profile, Object store, Object ref);

    /**
     * Removes all modifiers applied by this plugin and resets stats to base values.
     * Called on class reset or player disconnect.
     */
    void clearAllModifiers(UUID uuid, Object store, Object ref);
}
