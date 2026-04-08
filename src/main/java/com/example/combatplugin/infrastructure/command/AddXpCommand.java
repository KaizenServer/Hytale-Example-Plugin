package com.example.combatplugin.infrastructure.command;

import com.example.combatplugin.application.port.IUiPresenter;
import com.example.combatplugin.application.usecase.AwardXpUseCase;
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
 * /xp add <amount> — awards XP to the executing player.
 *
 * INTEGRATION POINT: In production, swap the permission stub for a real
 * PermissionsModule check using CombatPermissions.ADMIN_XP.
 */
public class AddXpCommand extends AbstractPlayerCommand {

    private final AwardXpUseCase useCase;
    private final IUiPresenter uiPresenter;
    private final RequiredArg<Integer> amountArg;

    public AddXpCommand(AwardXpUseCase useCase, IUiPresenter uiPresenter) {
        super("add", "Award XP to yourself. Usage: /xp add <amount>. Requires admin permission.");
        this.useCase = useCase;
        this.uiPresenter = uiPresenter;
        // ASSUMPTION: ArgTypes.INTEGER exists. If not, use ArgTypes.STRING and parse manually.
        this.amountArg = withRequiredArg("amount", "Amount of XP to award", ArgTypes.INTEGER);
    }

    @Override
    protected void execute(@Nonnull CommandContext ctx,
                           @Nonnull Store<EntityStore> store,
                           @Nonnull Ref<EntityStore> ref,
                           @Nonnull PlayerRef playerRef,
                           @Nonnull World world) {
        PlayerContext pctx = CommandHelper.buildContext(store, ref, playerRef);

        int amount = amountArg.get(ctx);
        if (amount <= 0) {
            uiPresenter.sendError(pctx, "XP amount must be a positive number.");
            return;
        }

        AwardXpUseCase.Result result = useCase.execute(pctx.uuid(), (long) amount);
        if (result.leveledUp()) {
            uiPresenter.sendSuccess(pctx,
                    "+" + amount + " XP! §eLEVEL UP: " + result.oldLevel() + " → " + result.newLevel()
                            + "§a (+" + result.talentPointsGranted() + " talent point(s))");
        } else {
            uiPresenter.sendSuccess(pctx, "+" + amount + " XP awarded.");
        }
    }
}
