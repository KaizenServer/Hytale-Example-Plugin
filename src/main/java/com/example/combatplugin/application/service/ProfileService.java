package com.example.combatplugin.application.service;

import com.example.combatplugin.application.port.IProfileRepository;
import com.example.combatplugin.domain.model.PlayerProfile;

import java.util.Optional;
import java.util.UUID;

/**
 * Thin facade over IProfileRepository.
 * Ensures consistent defaults and centralises null-safety.
 */
public class ProfileService {

    private final IProfileRepository repository;

    public ProfileService(IProfileRepository repository) {
        this.repository = repository;
    }

    /** Returns the player's profile, or a fresh default if not yet loaded. */
    public PlayerProfile getOrDefault(UUID uuid) {
        return repository.find(uuid).orElse(PlayerProfile.defaultProfile());
    }

    /** Persists the profile to in-memory store (flushed to ECS on disconnect). */
    public void save(UUID uuid, PlayerProfile profile) {
        repository.save(uuid, profile);
    }

    public Optional<PlayerProfile> find(UUID uuid) {
        return repository.find(uuid);
    }

    public boolean isOnline(UUID uuid) {
        return repository.isLoaded(uuid);
    }
}
