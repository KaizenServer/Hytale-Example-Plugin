package com.example.combatplugin.infrastructure.command;

import com.example.combatplugin.application.port.IStatApplicator;
import com.example.combatplugin.application.port.IUiPresenter;
import com.example.combatplugin.application.service.ProfileService;
import com.example.combatplugin.application.service.TalentService;
import com.example.combatplugin.application.usecase.ResetTalentsUseCase;
import com.example.combatplugin.application.usecase.UnlockTalentUseCase;
import com.example.combatplugin.domain.model.PlayerProfile;
import com.example.combatplugin.domain.model.TalentDefinition;
import com.example.combatplugin.infrastructure.ui.PlayerContext;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * /talents — opens the talent tree UI by default.
 *
 * Optional flags:
 *   --unlock <talentId>  unlock a talent directly (power-user / admin shortcut)
 *   --reset              refund all spent talent points
 */
public class TalentsCommand extends AbstractPlayerCommand {

    private final TalentService talentService;
    private final ProfileService profileService;
    private final UnlockTalentUseCase unlockUseCase;
    private final ResetTalentsUseCase resetUseCase;
    private final IUiPresenter uiPresenter;

    private final OptionalArg<String> unlockArg;
    private final FlagArg resetFlag;

    public TalentsCommand(UnlockTalentUseCase unlockUseCase,
                          TalentService talentService,
                          ProfileService profileService,
                          IStatApplicator statApplicator,
                          IUiPresenter uiPresenter) {
        super("talents", "Open your talent tree UI. Optional: --unlock <id> or --reset");
        this.talentService = talentService;
        this.profileService = profileService;
        this.unlockUseCase = unlockUseCase;
        this.uiPresenter = uiPresenter;
        this.resetUseCase = new ResetTalentsUseCase(profileService, talentService, statApplicator);

        this.unlockArg = withOptionalArg("unlock", "Talent ID to unlock directly", ArgTypes.STRING);
        this.resetFlag = withFlagArg("reset", "Reset all talent points");
    }

    @Override
    protected void execute(@Nonnull CommandContext ctx,
                           @Nonnull Store<EntityStore> store,
                           @Nonnull Ref<EntityStore> ref,
                           @Nonnull PlayerRef playerRef,
                           @Nonnull World world) {
        PlayerContext pctx = CommandHelper.buildContext(store, ref, playerRef);
        PlayerProfile profile = profileService.getOrDefault(pctx.uuid());

        if (!profile.hasClass()) {
            uiPresenter.sendError(pctx, "Choose a class first with /class choose.");
            return;
        }

        boolean doReset = resetFlag.get(ctx);
        String unlockId = unlockArg.get(ctx);

        if (doReset) {
            try {
                resetUseCase.execute(pctx.uuid(), store, ref);
                uiPresenter.sendSuccess(pctx, "All talents reset. Points refunded.");
            } catch (Exception e) {
                uiPresenter.sendError(pctx, e.getMessage());
            }
        } else if (unlockId != null) {
            try {
                unlockUseCase.execute(pctx.uuid(), unlockId, store, ref);
                uiPresenter.sendSuccess(pctx, "Talent unlocked: " + unlockId);
            } catch (Exception e) {
                uiPresenter.sendError(pctx, e.getMessage());
            }
        } else {
            List<TalentDefinition> talents = talentService.getTalentsForClass(profile.getCombatClass());
            uiPresenter.showTalentMenu(pctx, talents, profile);
        }
    }
}
