package com.example.combatplugin.infrastructure.command;

import com.example.combatplugin.application.port.IStatApplicator;
import com.example.combatplugin.application.port.IUiPresenter;
import com.example.combatplugin.application.service.ProfileService;
import com.example.combatplugin.application.service.ProgressionService;
import com.example.combatplugin.application.usecase.AwardXpUseCase;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

/**
 * Top-level /xp command collection (admin).
 * Subcommands: add
 */
public class XpCommandCollection extends AbstractCommandCollection {

    public XpCommandCollection(ProfileService profileService,
                                ProgressionService progressionService,
                                IUiPresenter uiPresenter,
                                IStatApplicator statApplicator) {
        super("xp", "Manage player XP (admin). Use /xp add <amount>.");

        AwardXpUseCase awardUseCase = new AwardXpUseCase(profileService, progressionService);
        addSubCommand(new AddXpCommand(awardUseCase, uiPresenter, statApplicator,
                profileService, progressionService));
    }
}
