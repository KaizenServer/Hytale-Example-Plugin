package com.example.combatplugin.infrastructure.command;

import com.example.combatplugin.application.port.IUiPresenter;
import com.example.combatplugin.application.usecase.ResetTalentsUseCase;
import com.example.combatplugin.infrastructure.ui.PlayerContext;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

/** /talents reset — refunds all talent points, preserves class */
public class ResetTalentsCommand extends AbstractPlayerCommand {

    private final ResetTalentsUseCase useCase;
    private final IUiPresenter uiPresenter;

    public ResetTalentsCommand(ResetTalentsUseCase useCase, IUiPresenter uiPresenter) {
        super("reset", "Reset all talents and refund your talent points.");
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
            uiPresenter.sendSuccess(pctx, "All talents reset. Your talent points have been refunded.");
        } catch (Exception e) {
            uiPresenter.sendError(pctx, e.getMessage());
        }
    }
}
