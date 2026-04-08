package com.example.combatplugin.application.port;

import com.example.combatplugin.domain.model.ClassDefinition;
import com.example.combatplugin.domain.model.PlayerProfile;
import com.example.combatplugin.domain.model.TalentDefinition;
import com.example.combatplugin.infrastructure.ui.PlayerContext;

import java.util.List;

/**
 * Contract for presenting UI to the player.
 *
 * {@link com.example.combatplugin.infrastructure.ui.TextFallbackUiPresenter} uses chat messages.
 * {@link com.example.combatplugin.infrastructure.ui.HytaleUiPresenter} opens interactive pages
 * via PageManager once the API is confirmed.
 */
public interface IUiPresenter {

    /** Show the class selection menu / class status to the player. */
    void showClassMenu(PlayerContext pctx, List<ClassDefinition> availableClasses,
                       PlayerProfile currentProfile);

    /** Show the talent tree for the player's current class. */
    void showTalentMenu(PlayerContext pctx, List<TalentDefinition> classTalents,
                        PlayerProfile currentProfile);

    /** Send a plain informational message to the player. */
    void sendInfo(PlayerContext pctx, String message);

    /** Send an error/failure message to the player. */
    void sendError(PlayerContext pctx, String message);

    /** Send a success/confirmation message to the player. */
    void sendSuccess(PlayerContext pctx, String message);
}
