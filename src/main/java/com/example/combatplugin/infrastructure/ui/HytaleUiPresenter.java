package com.example.combatplugin.infrastructure.ui;

import com.example.combatplugin.application.port.IUiPresenter;
import com.example.combatplugin.application.usecase.ChooseClassUseCase;
import com.example.combatplugin.application.usecase.RemoveTalentRankUseCase;
import com.example.combatplugin.application.usecase.UnlockTalentUseCase;
import com.example.combatplugin.domain.model.ClassDefinition;
import com.example.combatplugin.domain.model.PlayerProfile;
import com.example.combatplugin.domain.model.TalentDefinition;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.entities.Player;

import java.util.List;

/**
 * Interactive GUI presenter backed by Hytale's InteractiveCustomUIPage.
 *
 * Confirmed API (from inventory-management docs):
 *   player.getPageManager().openCustomPage(ref, store, page)
 *
 * INTEGRATION POINT: If getPageManager() or openCustomPage() have a different
 * name in the decompiled JARs, update the two method calls in showClassMenu /
 * showTalentMenu below.
 */
public class HytaleUiPresenter implements IUiPresenter {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final ChooseClassUseCase chooseClassUseCase;
    private final UnlockTalentUseCase unlockTalentUseCase;
    private final RemoveTalentRankUseCase removeTalentRankUseCase;
    private final TextFallbackUiPresenter fallback = new TextFallbackUiPresenter();

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
        Player player = pctx.player();
        if (player == null || pctx.ref() == null || pctx.store() == null) {
            fallback.showClassMenu(pctx, availableClasses, currentProfile);
            return;
        }
        try {
            ClassSelectionPage page = new ClassSelectionPage(
                    pctx.playerRef(), pctx.uuid(),
                    availableClasses, currentProfile,
                    chooseClassUseCase);
            // Confirmed API from docs: player.getPageManager().openCustomPage(ref, store, page)
            player.getPageManager().openCustomPage(pctx.ref(), pctx.store(), page);
        } catch (Throwable e) {
            LOGGER.atWarning().log("[HytaleUiPresenter] Failed to open ClassSelectionPage: %s", e.toString());
            fallback.showClassMenu(pctx, availableClasses, currentProfile);
        }
    }

    @Override
    public void showTalentMenu(PlayerContext pctx, List<TalentDefinition> classTalents,
                               PlayerProfile currentProfile) {
        Player player = pctx.player();
        if (player == null || pctx.ref() == null || pctx.store() == null) {
            fallback.showTalentMenu(pctx, classTalents, currentProfile);
            return;
        }
        try {
            TalentTreePage page = new TalentTreePage(
                    pctx.playerRef(), pctx.uuid(),
                    classTalents, currentProfile,
                    unlockTalentUseCase, removeTalentRankUseCase);
            // Confirmed API from docs: player.getPageManager().openCustomPage(ref, store, page)
            player.getPageManager().openCustomPage(pctx.ref(), pctx.store(), page);
        } catch (Throwable e) {
            LOGGER.atWarning().log("[HytaleUiPresenter] Failed to open TalentTreePage: %s", e.toString());
            fallback.showTalentMenu(pctx, classTalents, currentProfile);
        }
    }

    @Override
    public void sendInfo(PlayerContext pctx, String message) {
        fallback.sendInfo(pctx, message);
    }

    @Override
    public void sendError(PlayerContext pctx, String message) {
        fallback.sendError(pctx, message);
    }

    @Override
    public void sendSuccess(PlayerContext pctx, String message) {
        fallback.sendSuccess(pctx, message);
    }
}
