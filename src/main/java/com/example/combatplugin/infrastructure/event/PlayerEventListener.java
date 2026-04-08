package com.example.combatplugin.infrastructure.event;

import com.example.combatplugin.application.port.IStatApplicator;
import com.example.combatplugin.domain.model.PlayerProfile;
import com.example.combatplugin.infrastructure.persistence.EcsProfileRepository;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.UUID;

/**
 * Bridges Hytale player lifecycle events to the plugin's profile system.
 *
 * Register in CombatPlugin.setup():
 *   getEventRegistry().registerGlobal(PlayerReadyEvent.class,    playerListener::onJoin);
 *   getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, playerListener::onLeave);
 *
 * PlayerReadyEvent  (extends PlayerEvent)    → getPlayer() → Player → getPlayerRef() → PlayerRef
 * PlayerDisconnectEvent (extends PlayerRefEvent) → getPlayerRef() → PlayerRef
 */
public final class PlayerEventListener {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final EcsProfileRepository repository;
    private final IStatApplicator statApplicator;

    public PlayerEventListener(EcsProfileRepository repository, IStatApplicator statApplicator) {
        this.repository = repository;
        this.statApplicator = statApplicator;
    }

    public void onJoin(PlayerReadyEvent event) {
        try {
            Player player = event.getPlayer();
            PlayerRef playerRef = player.getPlayerRef();
            UUID uuid = playerRef.getUuid();
            Holder<EntityStore> holder = playerRef.getHolder();

            repository.loadFromEcs(uuid, holder);

            // Re-apply any active modifiers from persisted profile.
            // Commands also call applyProfileModifiers, but we do it here for
            // stats that should be active from the moment the player enters the world.
            PlayerProfile profile = repository.find(uuid).orElse(PlayerProfile.defaultProfile());
            statApplicator.applyProfileModifiers(uuid, profile, holder, null);

            LOGGER.atInfo().log("[CombatPlugin] Profile loaded for %s (%s)", playerRef.getUsername(), uuid);
        } catch (Exception e) {
            LOGGER.atWarning().withCause(e).log("[CombatPlugin] Failed to load profile on join.");
        }
    }

    public void onLeave(PlayerDisconnectEvent event) {
        try {
            // PlayerDisconnectEvent extends PlayerRefEvent — getPlayerRef() returns PlayerRef component.
            PlayerRef playerRef = event.getPlayerRef();
            UUID uuid = playerRef.getUuid();
            Holder<EntityStore> holder = playerRef.getHolder();

            repository.syncToEcs(uuid, holder);

            LOGGER.atInfo().log("[CombatPlugin] Profile saved for %s (%s)", playerRef.getUsername(), uuid);
        } catch (Exception e) {
            LOGGER.atWarning().withCause(e).log("[CombatPlugin] Failed to save profile on leave.");
        }
    }
}
