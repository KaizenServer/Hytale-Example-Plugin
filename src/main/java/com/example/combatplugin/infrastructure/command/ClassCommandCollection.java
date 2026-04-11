package com.example.combatplugin.infrastructure.command;

import com.example.combatplugin.application.port.IUiPresenter;
import com.example.combatplugin.application.service.ClassService;
import com.example.combatplugin.application.service.ProfileService;
import com.example.combatplugin.domain.model.ClassDefinition;
import com.example.combatplugin.domain.model.PlayerProfile;
import com.example.combatplugin.infrastructure.ui.PlayerContext;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * /class — opens the class selection UI.
 * No subcommands. All class interaction happens inside the UI.
 */
public class ClassCommandCollection extends AbstractPlayerCommand {

    private final ClassService classService;
    private final ProfileService profileService;
    private final IUiPresenter uiPresenter;

    public ClassCommandCollection(ClassService classService,
                                   ProfileService profileService,
                                   IUiPresenter uiPresenter) {
        super("class", "Open the class selection UI.");
        this.classService = classService;
        this.profileService = profileService;
        this.uiPresenter = uiPresenter;
    }

    @Override
    protected void execute(@Nonnull CommandContext ctx,
                           @Nonnull Store<EntityStore> store,
                           @Nonnull Ref<EntityStore> ref,
                           @Nonnull PlayerRef playerRef,
                           @Nonnull World world) {
        PlayerContext pctx = CommandHelper.buildContext(store, ref, playerRef);
        PlayerProfile profile = profileService.getOrDefault(pctx.uuid());
        List<ClassDefinition> classes = new ArrayList<>(classService.getAllClasses());
        uiPresenter.showClassMenu(pctx, classes, profile);
    }
}
