package com.example.combatplugin.infrastructure.command;

import com.example.combatplugin.application.port.IUiPresenter;
import com.example.combatplugin.application.service.ProfileService;
import com.example.combatplugin.application.service.ProgressionService;
import com.example.combatplugin.application.usecase.SetLevelUseCase;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

/**
 * Top-level /level command collection (admin).
 * Subcommands: set, get
 */
public class LevelCommandCollection extends AbstractCommandCollection {

    public LevelCommandCollection(ProfileService profileService,
                                   ProgressionService progressionService,
                                   IUiPresenter uiPresenter) {
        super("level", "Manage player levels (admin). Use /level set|get.");

        SetLevelUseCase setLevelUseCase = new SetLevelUseCase(profileService, progressionService);

        addSubCommand(new SetLevelCommand(setLevelUseCase, uiPresenter));
        addSubCommand(new GetLevelCommand(profileService, uiPresenter));
    }
}
