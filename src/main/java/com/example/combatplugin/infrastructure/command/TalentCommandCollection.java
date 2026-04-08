package com.example.combatplugin.infrastructure.command;

import com.example.combatplugin.application.port.IUiPresenter;
import com.example.combatplugin.application.service.ProfileService;
import com.example.combatplugin.application.service.TalentService;
import com.example.combatplugin.application.usecase.ResetTalentsUseCase;
import com.example.combatplugin.application.usecase.UnlockTalentUseCase;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

/**
 * Alternative /talents command collection with explicit subcommands.
 * Subcommands: unlock, reset, list
 *
 * Not registered by default — CombatPlugin uses TalentsCommand (standalone) instead.
 * Available as an alternative registration if explicit subcommands are preferred
 * over TalentsCommand's flag-based interface.
 */
public class TalentCommandCollection extends AbstractCommandCollection {

    public TalentCommandCollection(UnlockTalentUseCase unlockUseCase,
                                    ResetTalentsUseCase resetUseCase,
                                    TalentService talentService,
                                    ProfileService profileService,
                                    IUiPresenter uiPresenter) {
        super("talents", "Manage your combat talents. Use /talents unlock|reset|list.");

        addSubCommand(new UnlockTalentCommand(unlockUseCase, uiPresenter));
        addSubCommand(new ResetTalentsCommand(resetUseCase, uiPresenter));
        addSubCommand(new ListTalentsCommand(talentService, profileService, uiPresenter));
    }
}
