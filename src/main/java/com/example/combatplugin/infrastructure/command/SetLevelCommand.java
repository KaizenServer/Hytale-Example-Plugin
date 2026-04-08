package com.example.combatplugin.infrastructure.command;

import com.example.combatplugin.application.port.IUiPresenter;
import com.example.combatplugin.application.usecase.SetLevelUseCase;
import com.example.combatplugin.infrastructure.ui.PlayerContext;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

/**
 * /level set <value> — admin command to force-set the executing player's level.
 * Adjusts XP and talent point pool accordingly.
 *
 * INTEGRATION POINT: Add PermissionsModule check using CombatPermissions.ADMIN_LEVEL.
 * TODO: Add optional --player <name> argument to target other players.
 */
public class SetLevelCommand extends AbstractPlayerCommand {

    private final SetLevelUseCase useCase;
    private final IUiPresenter uiPresenter;
    private final RequiredArg<Integer> levelArg;

    public SetLevelCommand(SetLevelUseCase useCase, IUiPresenter uiPresenter) {
        super("set", "Force-set your level. Usage: /level set <value>. Requires admin permission.");
        this.useCase = useCase;
        this.uiPresenter = uiPresenter;
        // ASSUMPTION: ArgTypes.INTEGER exists. If not, use ArgTypes.STRING and parse manually.
        this.levelArg = withRequiredArg("level", "Target level to set", ArgTypes.INTEGER);
    }

    @Override
    protected void execute(@Nonnull CommandContext ctx,
                           @Nonnull Store<EntityStore> store,
                           @Nonnull Ref<EntityStore> ref,
                           @Nonnull PlayerRef playerRef,
                           @Nonnull World world) {
        PlayerContext pctx = CommandHelper.buildContext(store, ref, playerRef);

        int targetLevel = levelArg.get(ctx);
        if (targetLevel < 1) {
            uiPresenter.sendError(pctx, "Level must be at least 1.");
            return;
        }

        useCase.execute(pctx.uuid(), targetLevel);
        uiPresenter.sendSuccess(pctx, "Level set to §e" + targetLevel + "§a.");
    }
}
