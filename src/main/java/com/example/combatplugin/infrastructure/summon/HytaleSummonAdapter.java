package com.example.combatplugin.infrastructure.summon;

import com.example.combatplugin.application.port.ISummonAdapter;
import com.hypixel.hytale.logger.HytaleLogger;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Adapter for spawning and tracking summons in the Hytale world.
 *
 * INTEGRATION POINT: NPCPlugin.get().spawnNPC() requires a Store<EntityStore>,
 * model key, config, position (Vector3d), and rotation (Vector3f).
 * The position must come from the owner's TransformComponent.
 *
 * TODO: Implement spawnSummon() fully once TransformComponent + NPCPlugin imports are confirmed.
 * Until then, active summon counts are tracked in memory for nc_undying_army stacking.
 */
public class HytaleSummonAdapter implements ISummonAdapter {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    /** Maps player UUID → number of currently active summons. */
    private final Map<UUID, AtomicInteger> activeSummons = new ConcurrentHashMap<>();

    @Override
    public void spawnSummon(UUID ownerUuid, String summonType, float power, Object worldStore) {
        // INTEGRATION POINT: replace the log with NPCPlugin.get().spawnNPC() once confirmed.
        // Pattern:
        //   Store<EntityStore> store = (Store<EntityStore>) worldStore;
        //   TransformComponent ownerPos = store.getComponent(ownerRef, TransformComponent.getComponentType());
        //   Vector3d spawnPos = ownerPos.getPosition().add(1, 0, 1);
        //   NPCPlugin.get().spawnNPC(store, summonType, null, spawnPos, Vector3f.ZERO);
        LOGGER.atInfo().log("[STUB] spawnSummon: %s for %s (API pending)", summonType, ownerUuid);
        activeSummons.computeIfAbsent(ownerUuid, k -> new AtomicInteger(0)).incrementAndGet();
    }

    @Override
    public int getActiveSummonCount(UUID ownerUuid) {
        AtomicInteger count = activeSummons.get(ownerUuid);
        return count != null ? count.get() : 0;
    }

    @Override
    public void despawnAllSummons(UUID ownerUuid) {
        // TODO: Track spawned entity Refs per owner and remove them from the world.
        activeSummons.remove(ownerUuid);
        LOGGER.atInfo().log("[STUB] despawnAllSummons for %s (tracking only, no world removal yet)",
                ownerUuid);
    }
}
