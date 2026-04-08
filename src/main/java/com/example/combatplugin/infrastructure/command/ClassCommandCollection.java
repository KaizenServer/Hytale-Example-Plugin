package com.example.combatplugin.infrastructure.command;

import com.example.combatplugin.application.port.IStatApplicator;
import com.example.combatplugin.application.port.IUiPresenter;
import com.example.combatplugin.application.service.ClassService;
import com.example.combatplugin.application.service.ProfileService;
import com.example.combatplugin.application.service.TalentService;
import com.example.combatplugin.application.usecase.ResetClassUseCase;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

/**
 * Top-level /class command collection.
 * Subcommands: choose (opens UI), reset, info
 */
public class ClassCommandCollection extends AbstractCommandCollection {

    public ClassCommandCollection(ClassService classService,
                                   ProfileService profileService,
                                   TalentService talentService,
                                   IStatApplicator statApplicator,
                                   IUiPresenter uiPresenter) {
        super("class", "Manage your combat class.");

        ResetClassUseCase resetUseCase = new ResetClassUseCase(
                profileService, talentService, statApplicator);

        addSubCommand(new ChooseClassCommand(classService, profileService, uiPresenter));
        addSubCommand(new ResetClassCommand(resetUseCase, uiPresenter));
        addSubCommand(new ClassInfoCommand(classService, profileService, uiPresenter));
    }
}
