package com.example.combatplugin.infrastructure.persistence;

import com.example.combatplugin.domain.model.CombatClass;
import com.example.combatplugin.domain.model.PlayerProfile;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
// ASSUMPTION: KeyedCodec import path from docs — verify after ./gradlew build decompiles the game.
// If unresolved, check com.hypixel.hytale.codec.* for the actual package.
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * ECS component that persists a player's CombatPlugin profile to Hytale's auto-save system.
 *
 * Fields are intentionally flat primitives / strings because BuilderCodec works best with
 * basic Codec types. The domain PlayerProfile is reconstructed from these on load.
 *
 * Prefix "Cp" on every KeyedCodec key ensures uniqueness across the entire server.
 */
public final class PlayerProfileComponent implements Component<EntityStore> {

    // ── Wire fields (serialised by Hytale's ECS persistence) ──────────────────

    private String combatClassName = CombatClass.NONE.name();
    private int level = 1;
    private long xp = 0L;
    private int talentPoints = 0;
    /** Comma-separated "talentId:rank" pairs, e.g. "sm_iron_will:2,sm_sword_expertise:1" */
    private String unlockedTalentsRaw = "";

    // ── Codec definition ───────────────────────────────────────────────────────

    public static final BuilderCodec<PlayerProfileComponent> CODEC =
            BuilderCodec.builder(PlayerProfileComponent.class, PlayerProfileComponent::new)
                    .append(
                            new KeyedCodec<>("CpClass", Codec.STRING),
                            (c, v) -> c.combatClassName = v,
                            c -> c.combatClassName
                    ).add()
                    .append(
                            new KeyedCodec<>("CpLevel", Codec.INTEGER),
                            (c, v) -> c.level = v,
                            c -> c.level
                    ).add()
                    .append(
                            new KeyedCodec<>("CpXp", Codec.LONG),
                            (c, v) -> c.xp = v,
                            c -> c.xp
                    ).add()
                    .append(
                            new KeyedCodec<>("CpTalentPoints", Codec.INTEGER),
                            (c, v) -> c.talentPoints = v,
                            c -> c.talentPoints
                    ).add()
                    .append(
                            new KeyedCodec<>("CpTalents", Codec.STRING),
                            (c, v) -> c.unlockedTalentsRaw = v,
                            c -> c.unlockedTalentsRaw
                    ).add()
                    .build();

    // ── Domain conversion ──────────────────────────────────────────────────────

    public PlayerProfile toProfile() {
        CombatClass combatClass = CombatClass.fromString(combatClassName);
        Map<String, Integer> ranks = parseTalentRanks(unlockedTalentsRaw);
        return new PlayerProfile(combatClass, level, xp, talentPoints, ranks);
    }

    public static PlayerProfileComponent fromProfile(PlayerProfile profile) {
        PlayerProfileComponent c = new PlayerProfileComponent();
        c.combatClassName = profile.getCombatClass().name();
        c.level = profile.getLevel();
        c.xp = profile.getXp();
        c.talentPoints = profile.getTalentPoints();
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> entry : profile.getTalentRanks().entrySet()) {
            if (sb.length() > 0) sb.append(',');
            sb.append(entry.getKey()).append(':').append(entry.getValue());
        }
        c.unlockedTalentsRaw = sb.toString();
        return c;
    }

    // ── Component contract ─────────────────────────────────────────────────────

    @Nonnull
    @Override
    public Component<EntityStore> clone() {
        PlayerProfileComponent copy = new PlayerProfileComponent();
        copy.combatClassName = this.combatClassName;
        copy.level = this.level;
        copy.xp = this.xp;
        copy.talentPoints = this.talentPoints;
        copy.unlockedTalentsRaw = this.unlockedTalentsRaw;
        return copy;
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    /**
     * Parses "id:rank,id:rank,..." into a Map. Gracefully handles legacy format (no ":rank")
     * by defaulting to rank 1 for backward compatibility.
     */
    private static Map<String, Integer> parseTalentRanks(String raw) {
        Map<String, Integer> result = new HashMap<>();
        if (raw == null || raw.isBlank()) return result;
        for (String entry : raw.split(",")) {
            if (entry.isBlank()) continue;
            int colon = entry.indexOf(':');
            if (colon < 0) {
                result.put(entry.trim(), 1); // legacy: no rank stored → assume rank 1
            } else {
                String id = entry.substring(0, colon).trim();
                int rank = Integer.parseInt(entry.substring(colon + 1).trim());
                if (rank > 0) result.put(id, rank);
            }
        }
        return result;
    }
}
