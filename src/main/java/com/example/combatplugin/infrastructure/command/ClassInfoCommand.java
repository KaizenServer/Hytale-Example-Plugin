package com.example.combatplugin.infrastructure.command;

import com.example.combatplugin.application.port.IUiPresenter;
import com.example.combatplugin.application.service.ClassService;
import com.example.combatplugin.application.service.ProfileService;
import com.example.combatplugin.domain.model.ClassDefinition;
import com.example.combatplugin.domain.model.PlayerProfile;
import com.example.combatplugin.infrastructure.ui.PlayerContext;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/** /class info [className] — shows all classes or details of a specific one */
public class ClassInfoCommand extends AbstractPlayerCommand {

    private final ClassService classService;
    private final ProfileService profileService;
    private final IUiPresenter uiPresenter;
    private final OptionalArg<String> classNameArg;

    public ClassInfoCommand(ClassService classService, ProfileService profileService,
                             IUiPresenter uiPresenter) {
        super("info", "Show class information. Usage: /class info [className]");
        this.classService = classService;
        this.profileService = profileService;
        this.uiPresenter = uiPresenter;
        this.classNameArg = withOptionalArg("className", "Class to inspect", ArgTypes.STRING);
    }

    @Override
    protected void execute(@Nonnull CommandContext ctx,
                           @Nonnull Store<EntityStore> store,
                           @Nonnull Ref<EntityStore> ref,
                           @Nonnull PlayerRef playerRef,
                           @Nonnull World world) {
        PlayerContext pctx = CommandHelper.buildContext(store, ref, playerRef);
        PlayerProfile profile = profileService.getOrDefault(pctx.uuid());

        String specificClass = classNameArg.get(ctx);
        List<ClassDefinition> toShow;

        if (specificClass != null && !specificClass.isBlank()) {
            try {
                toShow = List.of(classService.resolveClass(specificClass));
            } catch (Exception e) {
                uiPresenter.sendError(pctx, e.getMessage());
                return;
            }
        } else {
            toShow = new ArrayList<>(classService.getAllClasses());
        }

        uiPresenter.showClassMenu(pctx, toShow, profile);
    }
}
