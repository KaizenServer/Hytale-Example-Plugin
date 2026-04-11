package com.example.combatplugin.system;

import com.example.combatplugin.application.port.IStatApplicator;
import com.example.combatplugin.application.service.ProgressionService;
import com.example.combatplugin.domain.model.PlayerProfile;
import com.example.combatplugin.infrastructure.persistence.EcsProfileRepository;
import com.example.combatplugin.infrastructure.ui.XpProgressHud;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Applies level-based stat modifiers when a player's EntityStatMap is initialised.
 *
 * Strategy: watch EntityStatMap additions with Query.and() (no pre-filter).
 * This fires when any entity gets a stat map — we guard with a UUIDComponent + Player
 * check inside the handler to limit work to player entities only.
 *
 * Query.and() is intentional: if we filter by Player.getComponentType() the system
 * misses entities where Player is not yet attached when the query is evaluated.
 *
 * Fallback: if onComponentAdded never fires (built-in component limitation),
 * stats are also applied on the first XP gain via AddXpCommand / DeathSynergySystem.
 */
public class ProfileInitSystem extends RefChangeSystem<EntityStore, EntityStatMap> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final EcsProfileRepository repository;
    private final IStatApplicator statApplicator;
    private final ProgressionService progressionService;

    public ProfileInitSystem(EcsProfileRepository repository,
                              IStatApplicator statApplicator,
                              ProgressionService progressionService) {
        this.repository = repository;
        this.statApplicator = statApplicator;
        this.progressionService = progressionService;
    }

    @Nonnull
    @Override
    public ComponentType<EntityStore, EntityStatMap> componentType() {
        return EntityStatMap.getComponentType();
    }

    /**
     * Query.and() with no arguments = match ALL entities unconditionally.
     * This avoids the chicken-and-egg problem of filtering by a component that may not
     * be present yet when the query is evaluated.
     */
    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and();
    }

    @Override
    public void onComponentAdded(@Nonnull Ref<EntityStore> ref,
                                  @Nonnull EntityStatMap statMap,
                                  @Nonnull Store<EntityStore> store,
                                  @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        tryApplyStats(ref, store, "onComponentAdded");
    }

    /**
     * Also hook onComponentSet: the EntityStatMap may be SET (updated) rather than
     * freshly added if it's a pre-existing component being refreshed at join time.
     */
    @Override
    public void onComponentSet(@Nonnull Ref<EntityStore> ref,
                                @Nullable EntityStatMap oldComponent,
                                @Nonnull EntityStatMap newComponent,
                                @Nonnull Store<EntityStore> store,
                                @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        tryApplyStats(ref, store, "onComponentSet");
    }

    @Override
    public void onComponentRemoved(@Nonnull Ref<EntityStore> ref,
                                    @Nonnull EntityStatMap component,
                                    @Nonnull Store<EntityStore> store,
                                    @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        // No-op
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private void tryApplyStats(Ref<EntityStore> ref, Store<EntityStore> store, String trigger) {
        try {
            // Only act on entities that have a UUID (filters out non-ECS entities)
            UUIDComponent uuidComp = store.getComponent(ref, UUIDComponent.getComponentType());
            if (uuidComp == null) return;
            UUID uuid = uuidComp.getUuid();

            // Only act on player entities
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player == null) return;

            // Retrieve profile — should be in cache from PlayerEventListener.onJoin
            PlayerProfile profile = repository.find(uuid)
                    .orElseGet(() -> repository.loadFromFile(uuid).orElse(null));
            if (profile == null) return;

            // Diagnostic: log every time the system fires for a player (even Lv.1)
            LOGGER.atInfo().log("[CombatPlugin] ProfileInitSystem[%s]: FIRED for %s — Lv.%d",
                    trigger, uuid, profile.getLevel());

            // Skip if level 1 with no talents — base stats apply, no bonuses to add
            if (profile.getLevel() <= 1 && profile.getTalentRanks().isEmpty()) {
                LOGGER.atInfo().log("[CombatPlugin] ProfileInitSystem[%s]: skipped (Lv.1, no talents) for %s",
                        trigger, uuid);
                return;
            }

            statApplicator.applyProfileModifiers(uuid, profile, store, ref);
            setXpHud(player, profile);

            LOGGER.atInfo().log("[CombatPlugin] ProfileInitSystem[%s]: stats applied for %s — Lv.%d",
                    trigger, uuid, profile.getLevel());

        } catch (Exception e) {
            LOGGER.atWarning().withCause(e).log("[CombatPlugin] ProfileInitSystem: failed — %s", e.getMessage());
        }
    }

    @SuppressWarnings("removal")
    private void setXpHud(Player player, PlayerProfile profile) {
        try {
            PlayerRef playerRef = player.getPlayerRef();
            if (playerRef == null) return;
            XpProgressHud hud = new XpProgressHud(playerRef,
                    profile.getLevel(), profile.getXp(), progressionService);
            player.getHudManager().setCustomHud(playerRef, hud);
        } catch (Exception e) {
            LOGGER.atWarning().log("[CombatPlugin] ProfileInitSystem: HUD set failed — %s", e);
        }
    }
}
