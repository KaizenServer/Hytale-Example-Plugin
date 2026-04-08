package com.example.combatplugin.application.port;

import com.example.combatplugin.domain.model.PlayerProfile;

import java.util.Optional;
import java.util.UUID;

/**
 * Contract for reading and writing player profiles.
 *
 * The backing store is an in-memory concurrent map populated on player join
 * and flushed to the ECS component layer on player leave.
 * See EcsProfileRepository for the implementation.
 */
public interface IProfileRepository {
    Optional<PlayerProfile> find(UUID uuid);
    void save(UUID uuid, PlayerProfile profile);
    void remove(UUID uuid);
    boolean isLoaded(UUID uuid);
}
