package com.example.combatplugin.infrastructure.ui;

import com.example.combatplugin.application.usecase.RemoveTalentRankUseCase;
import com.example.combatplugin.application.usecase.UnlockTalentUseCase;
import com.example.combatplugin.domain.model.PlayerProfile;
import com.example.combatplugin.domain.model.TalentDefinition;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

/**
 * Custom UI page for the talent tree.
 *
 * Layout: 16 nodes arranged in 5 tiers (T0–T4) matching the tree structure defined
 * in each *Talents.java file. Left-click assigns a rank, right-click removes a rank.
 *
 * Nodes 0–15 map directly to the order talents are registered per class.
 *
 * ASSUMPTION: The .ui file "Pages/TalentTreePage.ui" is resolved by the client
 * from the plugin's Common/Pages/ resources directory.
 */
public class TalentTreePage extends InteractiveCustomUIPage<TalentTreePage.TalentPageEvent> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final String UI_KEY = "TalentTreePage.ui";

    private final List<TalentDefinition> talents;
    private PlayerProfile profile;
    private final UUID playerUuid;
    private final UnlockTalentUseCase unlockUseCase;
    private final RemoveTalentRankUseCase removeUseCase;

    public TalentTreePage(PlayerRef playerRef, UUID playerUuid,
                          List<TalentDefinition> talents, PlayerProfile profile,
                          UnlockTalentUseCase unlockUseCase,
                          RemoveTalentRankUseCase removeUseCase) {
        super(playerRef, CustomPageLifetime.CanDismiss, TalentPageEvent.codec());
        this.playerUuid = playerUuid;
        this.talents = talents;
        this.profile = profile;
        this.unlockUseCase = unlockUseCase;
        this.removeUseCase = removeUseCase;
    }

    // ── Build ─────────────────────────────────────────────────────────────────

    @Override
    public void build(@Nonnull Ref<EntityStore> ref,
                      @Nonnull UICommandBuilder cmd,
                      @Nonnull UIEventBuilder evt,
                      @Nonnull Store<EntityStore> store) {
        cmd.append(UI_KEY);

        // Header
        cmd.set("#PageClass", Message.raw(profile.getCombatClass().name() + " Talent Tree"));
        cmd.set("#PagePoints", Message.raw("Points: " + profile.getTalentPoints()));

        // Populate each node
        for (int i = 0; i < 16 && i < talents.size(); i++) {
            TalentDefinition talent = talents.get(i);
            String nodeId = "#Node" + i;
            int currentRank = profile.getTalentRank(talent.id());
            boolean locked = isLocked(talent);

            cmd.set(nodeId + " #NodeName", Message.raw(talent.displayName()));
            cmd.set(nodeId + " #NodeRank",
                    Message.raw(currentRank + "/" + talent.maxRank()));

            String lvlText = talent.levelRequirement() > 0
                    ? "Lvl " + talent.levelRequirement() : "";
            cmd.set(nodeId + " #NodeLvlReq", Message.raw(lvlText));
            cmd.set(nodeId + " #NodeLock", locked);

            // Left-click → unlock / add rank
            evt.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    nodeId + " #NodeBtn",
                    EventData.of("action", "unlock").append("talentId", talent.id())
            );
            // Right-click → remove rank
            evt.addEventBinding(
                    CustomUIEventBindingType.RightClicking,
                    nodeId + " #NodeBtn",
                    EventData.of("action", "remove").append("talentId", talent.id())
            );
        }
    }

    // ── Event handling ────────────────────────────────────────────────────────

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref,
                                @Nonnull Store<EntityStore> store,
                                @Nonnull TalentPageEvent data) {
        if (data.talentId == null || data.talentId.isBlank()) return;

        try {
            if ("unlock".equals(data.action)) {
                unlockUseCase.execute(playerUuid, data.talentId, store, ref);
                // Sync local snapshot — withTalentRankIncremented already decrements talentPoints
                profile = profile.withTalentRankIncremented(data.talentId);
            } else if ("remove".equals(data.action)) {
                removeUseCase.execute(playerUuid, data.talentId, store, ref);
                // withTalentRankDecremented already increments talentPoints
                profile = profile.withTalentRankDecremented(data.talentId);
            }
            // Rebuild so updated ranks are displayed
            rebuild();
        } catch (Exception e) {
            LOGGER.atWarning().log("[TalentTreePage] %s failed for %s: %s",
                    data.action, data.talentId, e.getMessage());
            // Show error in status line without full rebuild
            UICommandBuilder update = new UICommandBuilder();
            update.set("#StatusLine", Message.raw("§c" + e.getMessage()));
            sendUpdate(update);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * A talent is visually locked if prerequisites are not met or level is insufficient.
     * The server still enforces this; this is only for the visual lock overlay.
     */
    private boolean isLocked(TalentDefinition talent) {
        if (profile.getLevel() < talent.levelRequirement()) return true;
        for (String prereq : talent.prerequisiteIds()) {
            if (!profile.hasTalent(prereq)) return true;
        }
        return false;
    }

    // ── Event data codec ──────────────────────────────────────────────────────

    public static final class TalentPageEvent {
        public String action;   // "unlock" or "remove"
        public String talentId;

        /** Returns a fresh BuilderCodec each call — avoids ExceptionInInitializerError. */
        public static BuilderCodec<TalentPageEvent> codec() {
            return BuilderCodec.builder(TalentPageEvent.class, TalentPageEvent::new)
                    .append(new KeyedCodec<>("action",   Codec.STRING),
                            (d, v) -> d.action   = v, d -> d.action).add()
                    .append(new KeyedCodec<>("talentId", Codec.STRING),
                            (d, v) -> d.talentId = v, d -> d.talentId).add()
                    .build();
        }

        public TalentPageEvent() {}
    }
}
