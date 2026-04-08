package com.example.combatplugin.infrastructure.ui;

import com.example.combatplugin.application.usecase.ChooseClassUseCase;
import com.example.combatplugin.domain.model.ClassDefinition;
import com.example.combatplugin.domain.model.PlayerProfile;
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
// Note: EventData is the Hytale UI event payload record (factory: EventData.of(key, value))
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

/**
 * Custom UI page for class selection.
 *
 * Sends a 4-card layout to the client. Left-clicking a card chooses that class.
 *
 * ASSUMPTION: The .ui file "Pages/ClassSelectionPage.ui" is resolved by the client
 * from the plugin's Common/Pages/ resources directory. Adjust the key if the actual
 * client path resolution differs.
 */
public class ClassSelectionPage extends InteractiveCustomUIPage<ClassSelectionPage.ClassEventData> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    // ASSUMPTION: Plugin .ui files are served by the client at "Pages/<filename>"
    // matching the same convention as game-internal pages (Pages/RespawnPage.ui, etc.)
    // ASSUMPTION: Hytale resolves .ui files relative to Common/UI/
    private static final String UI_KEY = "Custom/ClassSelectionPage.ui";

    private final List<ClassDefinition> classes;
    private PlayerProfile profile;
    private final UUID playerUuid;
    private final ChooseClassUseCase chooseUseCase;

    public ClassSelectionPage(PlayerRef playerRef, UUID playerUuid,
                              List<ClassDefinition> classes, PlayerProfile profile,
                              ChooseClassUseCase chooseUseCase) {
        super(playerRef, CustomPageLifetime.CanDismiss, ClassEventData.codec());
        this.playerUuid = playerUuid;
        this.classes = classes;
        this.profile = profile;
        this.chooseUseCase = chooseUseCase;
    }

    // ── Build ─────────────────────────────────────────────────────────────────

    @Override
    public void build(@Nonnull Ref<EntityStore> ref,
                      @Nonnull UICommandBuilder cmd,
                      @Nonnull UIEventBuilder evt,
                      @Nonnull Store<EntityStore> store) {
        // Load the base layout
        cmd.append(UI_KEY);

        // Header
        String headerText = profile.hasClass()
                ? "Your Class: " + profile.getCombatClass().name()
                : "Choose Your Class";
        cmd.set("#PageTitle", Message.raw(headerText));

        // Populate 4 class cards
        for (int i = 0; i < 4 && i < classes.size(); i++) {
            ClassDefinition cls = classes.get(i);
            String cardPrefix = "#Card" + i;

            cmd.set(cardPrefix + " #CardTitle", Message.raw(cls.displayName()));
            cmd.set(cardPrefix + " #CardRole",
                    Message.raw(cls.primaryRole() + " / " + cls.secondaryRole()));
            cmd.set(cardPrefix + " #CardDesc", Message.raw(cls.description()));

            boolean isSelected = profile.hasClass()
                    && profile.getCombatClass() == cls.id();
            String btnLabel = isSelected ? "✓ Selected" : "Choose";
            cmd.set(cardPrefix + " #CardBtnLabel", Message.raw(btnLabel));

            // Bind left-click to choose action
            evt.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    cardPrefix + " #CardBtn",
                    EventData.of("action", "choose").append("classId", cls.id().name().toLowerCase())
            );
        }

        // Status line
        String status = profile.hasClass()
                ? "Current class: " + profile.getCombatClass().name() + " | Level " + profile.getLevel()
                : "You have not chosen a class yet.";
        cmd.set("#StatusLine", Message.raw(status));
    }

    // ── Event handling ────────────────────────────────────────────────────────

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref,
                                @Nonnull Store<EntityStore> store,
                                @Nonnull ClassEventData data) {
        if (!"choose".equals(data.action)) return;
        if (data.classId == null || data.classId.isBlank()) return;

        try {
            chooseUseCase.execute(playerUuid, data.classId, store, ref);
            // Close the page — the player has chosen
            close();
        } catch (Exception e) {
            LOGGER.atWarning().log("[ClassSelectionPage] Class choose failed: %s", e.getMessage());
            // Update status line with the error
            UICommandBuilder update = new UICommandBuilder();
            update.set("#StatusLine", Message.raw("§c" + e.getMessage()));
            sendUpdate(update);
        }
    }

    // ── Event data codec ──────────────────────────────────────────────────────

    /** BSON event data payload sent by the client on button click. */
    public static final class ClassEventData {
        public String action;  // "choose"
        public String classId; // e.g. "sword_master"

        /** Returns a fresh BuilderCodec each call — avoids ExceptionInInitializerError. */
        public static BuilderCodec<ClassEventData> codec() {
            return BuilderCodec.builder(ClassEventData.class, ClassEventData::new)
                    .append(new KeyedCodec<>("action",  Codec.STRING),
                            (d, v) -> d.action  = v, d -> d.action).add()
                    .append(new KeyedCodec<>("classId", Codec.STRING),
                            (d, v) -> d.classId = v, d -> d.classId).add()
                    .build();
        }

        public ClassEventData() {}
    }
}
