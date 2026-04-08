package com.example.combatplugin.application.port;

import java.util.UUID;

/**
 * Contract for spawning summons on behalf of a player.
 *
 * INTEGRATION POINT: HytaleSummonAdapter implements this using NPCPlugin.get().spawnNPC().
 * The summonType string maps to model asset keys defined in game data.
 *
 * @param ownerUuid  UUID of the player who owns the summon.
 * @param summonType String key identifying the summon model/type (e.g. "skeleton_warrior").
 * @param power      Relative power level (scales summon stats; 1 = base).
 */
public interface ISummonAdapter {
    void spawnSummon(UUID ownerUuid, String summonType, float power, Object worldStore);
    int getActiveSummonCount(UUID ownerUuid);
    void despawnAllSummons(UUID ownerUuid);
}
