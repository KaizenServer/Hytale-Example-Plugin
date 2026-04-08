package com.example.combatplugin.infrastructure.command;

import com.example.combatplugin.application.port.IStatApplicator;
import com.example.combatplugin.application.port.IUiPresenter;
import com.example.combatplugin.application.service.ProfileService;
import com.example.combatplugin.application.service.TalentService;
import com.example.combatplugin.application.usecase.ResetTalentsUseCase;
import com.example.combatplugin.application.usecase.UnlockTalentUseCase;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

/**
 * Top-level /talents command collection.
 * Subcommands: unlock, reset, list
 */
public class TalentCommandCollection extends AbstractCommandCollection {

    public TalentCommandCollection(UnlockTalentUseCase unlockUseCase,
                                    TalentService talentService,
                                    ProfileService profileService,
                                    IStatApplicator statApplicator,
                                    IUiPresenter uiPresenter) {
        super("talents", "Manage your combat talents. Use /talents unlock|reset|list.");

        ResetTalentsUseCase resetUseCase = new ResetTalentsUseCase(
                profileService, talentService, statApplicator);

        addSubCommand(new UnlockTalentCommand(unlockUseCase, uiPresenter));
        addSubCommand(new ResetTalentsCommand(resetUseCase, uiPresenter));
        addSubCommand(new ListTalentsCommand(talentService, profileService, uiPresenter));
    }
}
