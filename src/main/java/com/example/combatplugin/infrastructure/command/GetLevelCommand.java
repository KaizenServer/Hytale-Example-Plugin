package com.example.combatplugin.infrastructure.command;

import com.example.combatplugin.application.port.IUiPresenter;
import com.example.combatplugin.application.service.ProfileService;
import com.example.combatplugin.domain.model.PlayerProfile;
import com.example.combatplugin.infrastructure.ui.PlayerContext;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

/** /level get — displays the executing player's current level, XP, and talent points */
public class GetLevelCommand extends AbstractPlayerCommand {

    private final ProfileService profileService;
    private final IUiPresenter uiPresenter;

    public GetLevelCommand(ProfileService profileService, IUiPresenter uiPresenter) {
        super("get", "Show your current level, XP, and talent points.");
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

        String classInfo = profile.hasClass()
                ? profile.getCombatClass().name()
                : "None";

        uiPresenter.sendInfo(pctx,
                "§6=== Your Profile ===\n"
                + "§eClass: §f" + classInfo + "\n"
                + "§eLevel: §f" + profile.getLevel() + "\n"
                + "§eXP: §f" + profile.getXp() + "\n"
                + "§eTalent Points Available: §f" + profile.getTalentPoints() + "\n"
                + "§eTalents Unlocked: §f" + profile.getUnlockedTalentIds().size());
    }
}
