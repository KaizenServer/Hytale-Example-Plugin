package com.example.combatplugin.domain.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Immutable snapshot of a player's combat progression state.
 * Every mutation returns a new instance. Never mutate in place.
 *
 * talentRanks maps talentId → current rank (1..maxRank).
 * A talent not present in the map has rank 0 (not unlocked).
 */
public final class PlayerProfile {

    private final CombatClass combatClass;
    private final int level;
    private final long xp;
    private final int talentPoints;
    private final Map<String, Integer> talentRanks;

    public PlayerProfile(CombatClass combatClass, int level, long xp,
                         int talentPoints, Map<String, Integer> talentRanks) {
        this.combatClass = combatClass;
        this.level = level;
        this.xp = xp;
        this.talentPoints = talentPoints;
        this.talentRanks = Collections.unmodifiableMap(new HashMap<>(talentRanks));
    }

    public static PlayerProfile defaultProfile() {
        return new PlayerProfile(CombatClass.NONE, 1, 0L, 0, Map.of());
    }

    // ── Accessors ──────────────────────────────────────────────────────────────

    public CombatClass getCombatClass()                 { return combatClass; }
    public int getLevel()                               { return level; }
    public long getXp()                                 { return xp; }
    public int getTalentPoints()                        { return talentPoints; }
    public Map<String, Integer> getTalentRanks()        { return talentRanks; }
    /** All talent IDs with at least one rank invested. */
    public Set<String> getUnlockedTalentIds()           { return talentRanks.keySet(); }
    public int getTalentRank(String talentId)           { return talentRanks.getOrDefault(talentId, 0); }
    public boolean hasTalent(String talentId)           { return talentRanks.containsKey(talentId); }
    public boolean hasClass()                           { return combatClass != CombatClass.NONE; }

    // ── Mutation helpers (each returns a new instance) ─────────────────────────

    public PlayerProfile withClass(CombatClass newClass) {
        return new PlayerProfile(newClass, level, xp, talentPoints, talentRanks);
    }

    public PlayerProfile withLevel(int newLevel) {
        return new PlayerProfile(combatClass, newLevel, xp, talentPoints, talentRanks);
    }

    public PlayerProfile withXp(long newXp) {
        return new PlayerProfile(combatClass, level, newXp, talentPoints, talentRanks);
    }

    public PlayerProfile withTalentPoints(int newPoints) {
        return new PlayerProfile(combatClass, level, xp, newPoints, talentRanks);
    }

    /**
     * Increments the rank of a talent by 1, deducting 1 talent point.
     * The caller is responsible for validating that rank < maxRank before calling this.
     */
    public PlayerProfile withTalentRankIncremented(String talentId) {
        Map<String, Integer> newMap = new HashMap<>(talentRanks);
        newMap.merge(talentId, 1, Integer::sum);
        return new PlayerProfile(combatClass, level, xp, talentPoints - 1, newMap);
    }

    /**
     * Decrements the rank of a talent by 1, refunding 1 talent point.
     * If rank reaches 0, the talent is removed from the map.
     * The caller is responsible for validating that rank > 0 before calling this.
     */
    public PlayerProfile withTalentRankDecremented(String talentId) {
        Map<String, Integer> newMap = new HashMap<>(talentRanks);
        int newRank = newMap.getOrDefault(talentId, 0) - 1;
        if (newRank <= 0) {
            newMap.remove(talentId);
        } else {
            newMap.put(talentId, newRank);
        }
        return new PlayerProfile(combatClass, level, xp, talentPoints + 1, newMap);
    }

    /** Reset class: clears class, all unlocked talents, but keeps level and XP. */
    public PlayerProfile withClassReset(int refundedPoints) {
        return new PlayerProfile(CombatClass.NONE, level, xp, refundedPoints, Map.of());
    }

    /** Reset only talents: keeps class and level, refunds all talent points. */
    public PlayerProfile withTalentsReset(int refundedPoints) {
        return new PlayerProfile(combatClass, level, xp, refundedPoints, Map.of());
    }

    @Override
    public String toString() {
        return "PlayerProfile{class=" + combatClass + ", level=" + level +
               ", xp=" + xp + ", talentPoints=" + talentPoints +
               ", talents=" + talentRanks.size() + "}";
    }
}
