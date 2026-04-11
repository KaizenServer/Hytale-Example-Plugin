package com.example.combatplugin.infrastructure.event;

import com.example.combatplugin.application.port.IStatApplicator;
import com.example.combatplugin.application.service.ProgressionService;
import com.example.combatplugin.domain.model.PlayerProfile;
import com.example.combatplugin.infrastructure.persistence.EcsProfileRepository;
import com.example.combatplugin.infrastructure.ui.XpProgressHud;
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
 * NOTE: playerRef.getHolder() returns null at PlayerReadyEvent time (the ECS holder is
 * not yet attached when this event fires). We initialize a default profile in-memory and
 * skip ECS persistence for join. ECS sync still runs on disconnect if the holder is available.
 *
 * NOTE: Player.getPlayerRef() is deprecated. We suppress the warning here while the
 * replacement API is being identified.
 */
public final class PlayerEventListener {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final EcsProfileRepository repository;
    private final IStatApplicator statApplicator;
    private final ProgressionService progressionService;

    public PlayerEventListener(EcsProfileRepository repository,
                               IStatApplicator statApplicator,
                               ProgressionService progressionService) {
        this.repository = repository;
        this.statApplicator = statApplicator;
        this.progressionService = progressionService;
    }

    @SuppressWarnings("removal") // player.getPlayerRef() is @Deprecated(forRemoval=true).
    // TODO: Replace once the non-deprecated replacement is confirmed in decompiled sources.
    public void onJoin(PlayerReadyEvent event) {
        try {
            Player player = event.getPlayer();
            PlayerRef playerRef = player.getPlayerRef();
            UUID uuid = playerRef.getUuid();
            Holder<EntityStore> holder = playerRef.getHolder();

            PlayerProfile profile;
            if (holder != null) {
                // ECS holder available — use it (uncommon in current Hytale build)
                repository.loadFromEcs(uuid, holder);
                profile = repository.find(uuid).orElse(PlayerProfile.defaultProfile());
                statApplicator.applyProfileModifiers(uuid, profile, holder, null);
            } else {
                // Holder is null (always the case in this Hytale build).
                // Load from file — ProfileInitSystem will apply stats once Store+Ref are available.
                profile = repository.loadFromFile(uuid).orElseGet(() -> {
                    PlayerProfile def = PlayerProfile.defaultProfile();
                    repository.save(uuid, def); // writes default to disk for new players
                    LOGGER.atInfo().log("[CombatPlugin] New player %s — default profile created.", uuid);
                    return def;
                });
                LOGGER.atInfo().log("[CombatPlugin] Profile loaded from file for %s — Lv.%d", uuid, profile.getLevel());
            }

            // Set initial XP HUD (stats applied later by ProfileInitSystem when Store+Ref ready)
            setXpHud(player, playerRef, profile);

            LOGGER.atInfo().log("[CombatPlugin] Profile ready for %s (%s)", playerRef.getUsername(), uuid);
        } catch (Exception e) {
            LOGGER.atWarning().withCause(e).log("[CombatPlugin] Failed to load profile on join.");
        }
    }

    public void onLeave(PlayerDisconnectEvent event) {
        try {
            PlayerRef playerRef = event.getPlayerRef();
            UUID uuid = playerRef.getUuid();
            Holder<EntityStore> holder = playerRef.getHolder();

            if (holder != null) {
                repository.syncToEcs(uuid, holder);
            }
            // File was already written by the last repository.save() call during this session.
            // Just clear the in-memory cache.
            repository.remove(uuid);
            LOGGER.atInfo().log("[CombatPlugin] Profile cache cleared for %s (%s)", playerRef.getUsername(), uuid);
        } catch (Exception e) {
            LOGGER.atWarning().withCause(e).log("[CombatPlugin] Failed to clean up profile on leave.");
        }
    }

    // ── HUD helpers ───────────────────────────────────────────────────────────

    private void setXpHud(Player player, PlayerRef playerRef, PlayerProfile profile) {
        try {
            XpProgressHud hud = new XpProgressHud(playerRef,
                    profile.getLevel(), profile.getXp(), progressionService);
            player.getHudManager().setCustomHud(playerRef, hud);
        } catch (Exception e) {
            LOGGER.atWarning().log("[CombatPlugin] Failed to set XP HUD on join for %s: %s",
                    playerRef.getUuid(), e.toString());
        }
    }
}
