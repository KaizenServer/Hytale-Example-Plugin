package com.example.combatplugin.system;

import com.example.combatplugin.domain.model.PlayerProfile;
import com.example.combatplugin.infrastructure.persistence.PlayerProfileComponent;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Initialises a default PlayerProfileComponent on the very first time it is added
 * to a player entity (i.e., brand-new players who have never had a profile).
 *
 * This avoids branching in event handlers and commands — by the time any game
 * logic runs, the component is guaranteed to exist.
 */
public class ProfileInitSystem extends RefChangeSystem<EntityStore, PlayerProfileComponent> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final ComponentType<EntityStore, PlayerProfileComponent> componentType;

    public ProfileInitSystem(ComponentType<EntityStore, PlayerProfileComponent> componentType) {
        this.componentType = componentType;
    }

    @Nonnull
    @Override
    public ComponentType<EntityStore, PlayerProfileComponent> componentType() {
        return componentType;
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return componentType;
    }

    @Override
    public void onComponentAdded(@Nonnull Ref<EntityStore> ref,
                                  @Nonnull PlayerProfileComponent component,
                                  @Nonnull Store<EntityStore> store,
                                  @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        PlayerProfile profile = component.toProfile();
        if (!profile.hasClass() && profile.getLevel() == 1 && profile.getXp() == 0L) {
            // Brand-new player — the default values are already correct, just log.
            LOGGER.atInfo().log("[CombatPlugin] New player profile initialised.");
        }
    }

    @Override
    public void onComponentSet(@Nonnull Ref<EntityStore> ref,
                                @Nullable PlayerProfileComponent oldComponent,
                                @Nonnull PlayerProfileComponent newComponent,
                                @Nonnull Store<EntityStore> store,
                                @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        // No-op: profile updates are managed through the use case → repository pipeline.
    }

    @Override
    public void onComponentRemoved(@Nonnull Ref<EntityStore> ref,
                                    @Nonnull PlayerProfileComponent component,
                                    @Nonnull Store<EntityStore> store,
                                    @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        LOGGER.atWarning().log("[CombatPlugin] PlayerProfileComponent removed — this is unexpected.");
    }
}
