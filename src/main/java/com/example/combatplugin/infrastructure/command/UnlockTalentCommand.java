package com.example.combatplugin.infrastructure.command;

import com.example.combatplugin.application.port.IUiPresenter;
import com.example.combatplugin.application.usecase.UnlockTalentUseCase;
import com.example.combatplugin.domain.model.TalentDefinition;
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

/** /talents unlock <talentId> */
public class UnlockTalentCommand extends AbstractPlayerCommand {

    private final UnlockTalentUseCase useCase;
    private final IUiPresenter uiPresenter;
    private final RequiredArg<String> talentIdArg;

    public UnlockTalentCommand(UnlockTalentUseCase useCase, IUiPresenter uiPresenter) {
        super("unlock", "Unlock a talent. Usage: /talents unlock <talentId>");
        this.useCase = useCase;
        this.uiPresenter = uiPresenter;
        this.talentIdArg = withRequiredArg("talentId", "The talent ID to unlock", ArgTypes.STRING);
    }

    @Override
    protected void execute(@Nonnull CommandContext ctx,
                           @Nonnull Store<EntityStore> store,
                           @Nonnull Ref<EntityStore> ref,
                           @Nonnull PlayerRef playerRef,
                           @Nonnull World world) {
        PlayerContext pctx = CommandHelper.buildContext(store, ref, playerRef);
        String talentId = talentIdArg.get(ctx);

        try {
            TalentDefinition unlocked = useCase.execute(pctx.uuid(), talentId, store, ref);
            uiPresenter.sendSuccess(pctx,
                    "Talent §e" + unlocked.displayName() + "§a unlocked! (cost: " + unlocked.cost() + " point)");
        } catch (Exception e) {
            uiPresenter.sendError(pctx, e.getMessage());
        }
    }
}
