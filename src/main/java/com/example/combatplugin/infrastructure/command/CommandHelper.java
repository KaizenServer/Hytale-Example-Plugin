package com.example.combatplugin.infrastructure.command;

import com.example.combatplugin.infrastructure.ui.PlayerContext;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.UUID;

/**
 * Shared utility methods for command implementations.
 *
 * Uses the API pattern confirmed in the official docs:
 *   Player player = store.getComponent(ref, Player.getComponentType());
 *   UUIDComponent uuid = store.getComponent(ref, UUIDComponent.getComponentType());
 */
final class CommandHelper {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private CommandHelper() {}

    /**
     * Builds a {@link PlayerContext} carrying the PlayerRef, UUID, and Player component.
     * Logs a warning if any component lookup fails instead of silently ignoring it.
     */
    static PlayerContext buildContext(Store<EntityStore> store, Ref<EntityStore> ref,
                                      PlayerRef playerRef) {
        UUID uuid = null;
        Player player = null;
        try {
            UUIDComponent uuidComp = store.getComponent(ref, UUIDComponent.getComponentType());
            if (uuidComp != null) {
                uuid = uuidComp.getUuid();
            }
        } catch (Exception e) {
            LOGGER.atWarning().log("[CommandHelper] UUID lookup failed: %s", e.toString());
        }
        try {
            player = store.getComponent(ref, Player.getComponentType());
        } catch (Exception e) {
            LOGGER.atWarning().log("[CommandHelper] Player lookup failed: %s", e.toString());
        }
        return new PlayerContext(playerRef, uuid, player, ref, store);
    }
}
