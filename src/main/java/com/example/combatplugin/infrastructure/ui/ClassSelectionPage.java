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
    // Official docs: .ui files in Common/UI/Custom/ are referenced by filename only.
    // See: HytaleModding.dev/guides/plugin/ui — "uiCommandBuilder.append("MyUI.ui")"
    private static final String UI_KEY = "ClassSelectionPage.ui";

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
        // Note: cmd.set uses ".Text" property selector — reference: Ejemplos/UI/ClassSelectionPage.java
        String headerText = profile.hasClass()
                ? "Your Class: " + profile.getCombatClass().name()
                : "Choose Your Class";
        cmd.set("#PageTitle.Text", headerText);

        // Populate 4 class cards.
        // IDs are flat and unique: #C0Title, #C0Role, #C0Short, #C0Desc, #C0Btn, #C0BtnLabel, ...
        // Layout: header band (Title + Role) → Short tagline → icon zone → Desc → button.
        // #CnShort — short tagline above icon zone (cls.description() for now).
        // #CnDesc  — detailed description below icon zone (set "" until ClassDefinition
        //            is extended with a detailedDescription() field).
        for (int i = 0; i < 4 && i < classes.size(); i++) {
            ClassDefinition cls = classes.get(i);

            cmd.set("#C" + i + "Title.Text", cls.displayName());
            cmd.set("#C" + i + "RolePrimary.Text", cls.primaryRole());
            cmd.set("#C" + i + "RoleSecondary.Text", cls.secondaryRole());
            cmd.set("#C" + i + "Short.Text", cls.description());
            cmd.set("#C" + i + "Desc.Text", cls.detailedDescription());

            boolean isSelected = profile.hasClass()
                    && profile.getCombatClass() == cls.id();
            cmd.set("#C" + i + "BtnLabel.Text", isSelected ? "Selected" : "Choose");

            evt.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    "#C" + i + "Btn",
                    EventData.of("Action", "choose").append("ClassId", cls.id().name().toLowerCase())
            );
        }

        // Status line
        String status = profile.hasClass()
                ? "Current class: " + profile.getCombatClass().name() + " | Level " + profile.getLevel()
                : "You have not chosen a class yet.";
        cmd.set("#StatusLine.Text", status);
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
            update.set("#StatusLine.Text", "§c" + e.getMessage());
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
                    .append(new KeyedCodec<>("Action",  Codec.STRING),
                            (d, v) -> d.action  = v, d -> d.action).add()
                    .append(new KeyedCodec<>("ClassId", Codec.STRING),
                            (d, v) -> d.classId = v, d -> d.classId).add()
                    .build();
        }

        public ClassEventData() {}
    }
}
