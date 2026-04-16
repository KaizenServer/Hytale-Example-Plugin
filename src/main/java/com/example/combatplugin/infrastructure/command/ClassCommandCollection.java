package com.example.combatplugin.infrastructure.command;

import com.example.combatplugin.application.port.IUiPresenter;
import com.example.combatplugin.application.service.ClassService;
import com.example.combatplugin.application.service.ProfileService;
import com.example.combatplugin.application.usecase.ResetClassUseCase;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

/**
 * /class command collection.
 * Subcommands:
 *   choose — open the class selection UI
 *   reset  — remove current class and refund talent points
 */
public class ClassCommandCollection extends AbstractCommandCollection {

    public ClassCommandCollection(ClassService classService,
                                   ProfileService profileService,
                                   ResetClassUseCase resetClassUseCase,
                                   IUiPresenter uiPresenter) {
        super("class", "Manage your combat class. Use /class choose | reset.");

        addSubCommand(new ChooseClassCommand(classService, profileService, uiPresenter));
        addSubCommand(new ResetClassCommand(resetClassUseCase, uiPresenter));
    }
}
