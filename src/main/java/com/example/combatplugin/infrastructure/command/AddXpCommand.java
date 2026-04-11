package com.example.combatplugin.infrastructure.command;

import com.example.combatplugin.application.port.IStatApplicator;
import com.example.combatplugin.application.port.IUiPresenter;
import com.example.combatplugin.application.service.ProfileService;
import com.example.combatplugin.application.service.ProgressionService;
import com.example.combatplugin.application.usecase.AwardXpUseCase;
import com.example.combatplugin.domain.model.PlayerProfile;
import com.example.combatplugin.infrastructure.ui.PlayerContext;
import com.example.combatplugin.infrastructure.ui.XpProgressHud;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
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

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final int STAT_GAIN_PER_LEVEL = 5;

    private final AwardXpUseCase useCase;
    private final IUiPresenter uiPresenter;
    private final IStatApplicator statApplicator;
    private final ProfileService profileService;
    private final ProgressionService progressionService;
    private final RequiredArg<Integer> amountArg;

    public AddXpCommand(AwardXpUseCase useCase, IUiPresenter uiPresenter,
                        IStatApplicator statApplicator, ProfileService profileService,
                        ProgressionService progressionService) {
        super("add", "Award XP to yourself. Usage: /xp add <amount>. Requires admin permission.");
        this.useCase = useCase;
        this.uiPresenter = uiPresenter;
        this.statApplicator = statApplicator;
        this.profileService = profileService;
        this.progressionService = progressionService;
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

        // Re-apply stats so level-based bonuses take effect immediately
        PlayerProfile updatedProfile = profileService.find(pctx.uuid()).orElse(null);
        if (updatedProfile != null) {
            statApplicator.applyProfileModifiers(pctx.uuid(), updatedProfile, store, ref);
        }

        // Send feedback message
        if (result.leveledUp()) {
            int statGain = (result.newLevel() - result.oldLevel()) * STAT_GAIN_PER_LEVEL;
            uiPresenter.sendSuccess(pctx,
                    "+" + amount + " XP! §eLEVEL UP: " + result.oldLevel() + " → " + result.newLevel()
                    + " §a(+" + result.talentPointsGranted() + " talent pt"
                    + (result.talentPointsGranted() != 1 ? "s" : "")
                    + " | §c+" + statGain + " HP §b+" + statGain + " Mana §a+" + statGain + " Stamina§a)");
        } else {
            uiPresenter.sendSuccess(pctx, "+" + amount + " XP awarded.");
        }

        // Update XP HUD
        if (updatedProfile != null && pctx.player() != null && pctx.playerRef() != null) {
            try {
                XpProgressHud hud = new XpProgressHud(pctx.playerRef(),
                        updatedProfile.getLevel(), updatedProfile.getXp(), progressionService);
                pctx.player().getHudManager().setCustomHud(pctx.playerRef(), hud);
            } catch (Exception e) {
                LOGGER.atWarning().log("[CombatPlugin] Failed to update XP HUD after /xp add: %s", e.toString());
            }
        }
    }
}
