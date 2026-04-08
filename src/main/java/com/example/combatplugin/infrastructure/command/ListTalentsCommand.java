package com.example.combatplugin.infrastructure.command;

import com.example.combatplugin.application.port.IUiPresenter;
import com.example.combatplugin.application.service.ProfileService;
import com.example.combatplugin.application.service.TalentService;
import com.example.combatplugin.domain.model.PlayerProfile;
import com.example.combatplugin.domain.model.TalentDefinition;
import com.example.combatplugin.infrastructure.ui.PlayerContext;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.List;

/** /talents list — shows all talents for the player's current class */
public class ListTalentsCommand extends AbstractPlayerCommand {

    private final TalentService talentService;
    private final ProfileService profileService;
    private final IUiPresenter uiPresenter;

    public ListTalentsCommand(TalentService talentService, ProfileService profileService,
                               IUiPresenter uiPresenter) {
        super("list", "List all talents available for your current class.");
        this.talentService = talentService;
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

        if (!profile.hasClass()) {
            uiPresenter.sendError(pctx,
                    "You have not chosen a class yet. Use /class choose <className>.");
            return;
        }

        List<TalentDefinition> talents = talentService.getTalentsForClass(profile.getCombatClass());
        uiPresenter.showTalentMenu(pctx, talents, profile);
    }
}
