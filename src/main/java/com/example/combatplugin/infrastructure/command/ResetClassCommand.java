package com.example.combatplugin.infrastructure.command;

import com.example.combatplugin.application.port.IUiPresenter;
import com.example.combatplugin.application.usecase.ResetClassUseCase;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import com.example.combatplugin.infrastructure.ui.PlayerContext;

import javax.annotation.Nonnull;

/** /class reset */
public class ResetClassCommand extends AbstractPlayerCommand {

    private final ResetClassUseCase useCase;
    private final IUiPresenter uiPresenter;

    public ResetClassCommand(ResetClassUseCase useCase, IUiPresenter uiPresenter) {
        super("reset", "Reset your class and refund all talent points.");
        this.useCase = useCase;
        this.uiPresenter = uiPresenter;
    }

    @Override
    protected void execute(@Nonnull CommandContext ctx,
                           @Nonnull Store<EntityStore> store,
                           @Nonnull Ref<EntityStore> ref,
                           @Nonnull PlayerRef playerRef,
                           @Nonnull World world) {
        PlayerContext pctx = CommandHelper.buildContext(store, ref, playerRef);
        try {
            useCase.execute(pctx.uuid(), store, ref);
            uiPresenter.sendSuccess(pctx, "Class reset. All talent points refunded.");
        } catch (Exception e) {
            uiPresenter.sendError(pctx, e.getMessage());
        }
    }
}
