package com.example.combatplugin.infrastructure.persistence;

import com.example.combatplugin.application.port.IProfileRepository;
import com.example.combatplugin.domain.model.PlayerProfile;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory profile store backed by Hytale's ECS persistence via Holder<EntityStore>.
 *
 * Lifecycle:
 *  - loadFromEcs()   called by PlayerEventListener on PlayerReadyEvent
 *  - save()          called by use cases (writes to in-memory map only)
 *  - syncToEcs()     called by PlayerEventListener on PlayerDisconnectEvent
 *
 * Uses Holder<EntityStore> (from PlayerRef.getHolder()) instead of Store+Ref pair,
 * since global event listeners do not receive a Store reference directly.
 */
public class EcsProfileRepository implements IProfileRepository {

    private final ComponentType<EntityStore, PlayerProfileComponent> componentType;
    private final ConcurrentHashMap<UUID, PlayerProfile> cache = new ConcurrentHashMap<>();

    public EcsProfileRepository(ComponentType<EntityStore, PlayerProfileComponent> componentType) {
        this.componentType = componentType;
    }

    // ── IProfileRepository ─────────────────────────────────────────────────────

    @Override
    public Optional<PlayerProfile> find(UUID uuid) {
        return Optional.ofNullable(cache.get(uuid));
    }

    @Override
    public void save(UUID uuid, PlayerProfile profile) {
        cache.put(uuid, profile);
    }

    @Override
    public void remove(UUID uuid) {
        cache.remove(uuid);
    }

    @Override
    public boolean isLoaded(UUID uuid) {
        return cache.containsKey(uuid);
    }

    // ── ECS integration (called by PlayerEventListener) ────────────────────────

    /**
     * Loads the profile from the ECS component into the in-memory cache.
     * If the component doesn't exist yet, Holder creates a default instance
     * (via the factory registered during component type registration).
     */
    public void loadFromEcs(UUID uuid, Holder<EntityStore> holder) {
        PlayerProfileComponent component = holder.ensureAndGetComponent(componentType);
        cache.put(uuid, component.toProfile());
    }

    /**
     * Flushes the in-memory profile back to the ECS component so Hytale auto-saves it.
     * Called when the player disconnects.
     */
    public void syncToEcs(UUID uuid, Holder<EntityStore> holder) {
        PlayerProfile profile = cache.get(uuid);
        if (profile == null) return;

        PlayerProfileComponent component = PlayerProfileComponent.fromProfile(profile);
        holder.putComponent(componentType, component);
        cache.remove(uuid);
    }
}
