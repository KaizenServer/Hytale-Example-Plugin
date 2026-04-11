package com.example.combatplugin.infrastructure.ui;

import com.example.combatplugin.application.port.IUiPresenter;
import com.example.combatplugin.application.usecase.ChooseClassUseCase;
import com.example.combatplugin.application.usecase.RemoveTalentRankUseCase;
import com.example.combatplugin.application.usecase.UnlockTalentUseCase;
import com.example.combatplugin.domain.model.ClassDefinition;
import com.example.combatplugin.domain.model.PlayerProfile;
import com.example.combatplugin.domain.model.TalentDefinition;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;

import java.util.List;

/**
 * Interactive GUI presenter backed by Hytale's InteractiveCustomUIPage.
 *
 * Confirmed API (from inventory-management docs):
 *   player.getPageManager().openCustomPage(ref, store, page)
 *
 * No fallback: if the UI fails, the error is logged and nothing is sent to chat.
 * Fix UI errors by reading the server logs.
 */
public class HytaleUiPresenter implements IUiPresenter {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final ChooseClassUseCase chooseClassUseCase;
    private final UnlockTalentUseCase unlockTalentUseCase;
    private final RemoveTalentRankUseCase removeTalentRankUseCase;

    public HytaleUiPresenter(ChooseClassUseCase chooseClassUseCase,
                              UnlockTalentUseCase unlockTalentUseCase,
                              RemoveTalentRankUseCase removeTalentRankUseCase) {
        this.chooseClassUseCase = chooseClassUseCase;
        this.unlockTalentUseCase = unlockTalentUseCase;
        this.removeTalentRankUseCase = removeTalentRankUseCase;
    }

    @Override
    public void showClassMenu(PlayerContext pctx, List<ClassDefinition> availableClasses,
                              PlayerProfile currentProfile) {
        try {
            ClassSelectionPage page = new ClassSelectionPage(
                    pctx.playerRef(), pctx.uuid(),
                    availableClasses, currentProfile,
                    chooseClassUseCase);
            pctx.player().getPageManager().openCustomPage(pctx.ref(), pctx.store(), page);
        } catch (Throwable e) {
            LOGGER.atWarning().log("[HytaleUiPresenter] Failed to open ClassSelectionPage: %s", e.toString());
        }
    }

    @Override
    public void showTalentMenu(PlayerContext pctx, List<TalentDefinition> classTalents,
                               PlayerProfile currentProfile) {
        try {
            TalentTreePage page = new TalentTreePage(
                    pctx.playerRef(), pctx.uuid(),
                    classTalents, currentProfile,
                    unlockTalentUseCase, removeTalentRankUseCase);
            pctx.player().getPageManager().openCustomPage(pctx.ref(), pctx.store(), page);
        } catch (Throwable e) {
            LOGGER.atWarning().log("[HytaleUiPresenter] Failed to open TalentTreePage: %s", e.toString());
        }
    }

    @Override
    public void sendInfo(PlayerContext pctx, String message) {
        Player player = pctx.player();
        if (player != null) player.sendMessage(Message.raw("§7" + message));
    }

    @Override
    public void sendError(PlayerContext pctx, String message) {
        Player player = pctx.player();
        if (player != null) player.sendMessage(Message.raw("§c✘ " + message));
    }

    @Override
    public void sendSuccess(PlayerContext pctx, String message) {
        Player player = pctx.player();
        if (player != null) player.sendMessage(Message.raw("§a✔ " + message));
    }
}
