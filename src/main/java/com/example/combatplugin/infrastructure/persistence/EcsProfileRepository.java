package com.example.combatplugin.infrastructure.persistence;

import com.example.combatplugin.application.port.IProfileRepository;
import com.example.combatplugin.domain.model.CombatClass;
import com.example.combatplugin.domain.model.PlayerProfile;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Profile store with two layers:
 *  1. In-memory ConcurrentHashMap cache for fast runtime access.
 *  2. File-based persistence (playerdata/<uuid>.properties) that survives server restarts.
 *
 * ECS Holder persistence (loadFromEcs / syncToEcs) is kept as a fallback but is currently
 * unreachable because PlayerRef.getHolder() always returns null in this Hytale build.
 *
 * Lifecycle:
 *  - loadFromFile()  called by PlayerEventListener on PlayerReadyEvent
 *  - save()          called by use cases — writes to both cache AND disk immediately
 *  - remove()        called on disconnect — clears in-memory cache only (file persists)
 */
public class EcsProfileRepository implements IProfileRepository {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final Path PLAYER_DATA_DIR = Path.of("playerdata");

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

    /** Writes to in-memory cache AND persists to disk immediately. */
    @Override
    public void save(UUID uuid, PlayerProfile profile) {
        cache.put(uuid, profile);
        saveToFile(uuid, profile);
    }

    @Override
    public void remove(UUID uuid) {
        cache.remove(uuid);
    }

    @Override
    public boolean isLoaded(UUID uuid) {
        return cache.containsKey(uuid);
    }

    // ── File persistence ───────────────────────────────────────────────────────

    /**
     * Reads the player's profile from disk into the in-memory cache.
     * Returns the loaded profile, or empty if no save file exists yet.
     */
    public Optional<PlayerProfile> loadFromFile(UUID uuid) {
        Path file = PLAYER_DATA_DIR.resolve(uuid + ".properties");
        if (!Files.exists(file)) return Optional.empty();
        try {
            Properties props = new Properties();
            try (InputStream in = Files.newInputStream(file)) {
                props.load(in);
            }
            CombatClass combatClass = CombatClass.fromString(props.getProperty("class", "NONE"));
            int level = Integer.parseInt(props.getProperty("level", "1"));
            long xp = Long.parseLong(props.getProperty("xp", "0"));
            int talentPoints = Integer.parseInt(props.getProperty("talentPoints", "0"));
            Map<String, Integer> ranks = parseTalentRanks(props.getProperty("talents", ""));
            PlayerProfile profile = new PlayerProfile(combatClass, level, xp, talentPoints, ranks);
            cache.put(uuid, profile);
            LOGGER.atInfo().log("[CombatPlugin] Loaded profile from file for %s — Lv.%d, %d XP", uuid, level, xp);
            return Optional.of(profile);
        } catch (Exception e) {
            LOGGER.atWarning().log("[CombatPlugin] Failed to load profile file for %s: %s", uuid, e);
            return Optional.empty();
        }
    }

    private void saveToFile(UUID uuid, PlayerProfile profile) {
        try {
            Files.createDirectories(PLAYER_DATA_DIR);
            Path file = PLAYER_DATA_DIR.resolve(uuid + ".properties");
            Properties props = new Properties();
            props.setProperty("class", profile.getCombatClass().name());
            props.setProperty("level", String.valueOf(profile.getLevel()));
            props.setProperty("xp", String.valueOf(profile.getXp()));
            props.setProperty("talentPoints", String.valueOf(profile.getTalentPoints()));
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, Integer> entry : profile.getTalentRanks().entrySet()) {
                if (sb.length() > 0) sb.append(',');
                sb.append(entry.getKey()).append(':').append(entry.getValue());
            }
            props.setProperty("talents", sb.toString());
            try (OutputStream out = Files.newOutputStream(file)) {
                props.store(out, "CombatPlugin player data");
            }
        } catch (Exception e) {
            LOGGER.atWarning().log("[CombatPlugin] Failed to save profile file for %s: %s", uuid, e);
        }
    }

    private static Map<String, Integer> parseTalentRanks(String raw) {
        Map<String, Integer> result = new HashMap<>();
        if (raw == null || raw.isBlank()) return result;
        for (String entry : raw.split(",")) {
            if (entry.isBlank()) continue;
            int colon = entry.indexOf(':');
            if (colon < 0) {
                result.put(entry.trim(), 1);
            } else {
                String id = entry.substring(0, colon).trim();
                int rank = Integer.parseInt(entry.substring(colon + 1).trim());
                if (rank > 0) result.put(id, rank);
            }
        }
        return result;
    }

    // ── ECS integration (fallback — currently unreachable, holder always null) ──

    public void loadFromEcs(UUID uuid, Holder<EntityStore> holder) {
        PlayerProfileComponent component = holder.ensureAndGetComponent(componentType);
        cache.put(uuid, component.toProfile());
    }

    public void syncToEcs(UUID uuid, Holder<EntityStore> holder) {
        PlayerProfile profile = cache.get(uuid);
        if (profile == null) return;
        PlayerProfileComponent component = PlayerProfileComponent.fromProfile(profile);
        holder.putComponent(componentType, component);
        cache.remove(uuid);
    }
}
