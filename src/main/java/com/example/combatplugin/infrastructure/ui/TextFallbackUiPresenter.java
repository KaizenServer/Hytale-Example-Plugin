package com.example.combatplugin.infrastructure.ui;

import com.example.combatplugin.application.port.IUiPresenter;
import com.example.combatplugin.domain.model.ClassDefinition;
import com.example.combatplugin.domain.model.PlayerProfile;
import com.example.combatplugin.domain.model.TalentDefinition;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;

import java.util.List;

/**
 * Fully functional chat-based UI presenter.
 * Works immediately without any interactive UI API.
 * Replace with HytaleUiPresenter once PageManager API is confirmed.
 *
 * ASSUMPTION: Player.sendMessage(Message) is the confirmed API (from ExampleCommand).
 */
public class TextFallbackUiPresenter implements IUiPresenter {

    private static final String DIVIDER = "§8§m--------------------§r";

    @Override
    public void showClassMenu(PlayerContext pctx, List<ClassDefinition> availableClasses,
                              PlayerProfile currentProfile) {
        Player player = pctx.player();
        if (player == null) return;

        player.sendMessage(Message.raw(DIVIDER));
        player.sendMessage(Message.raw("§6§l  COMBAT CLASSES"));
        player.sendMessage(Message.raw(DIVIDER));

        if (currentProfile.hasClass()) {
            player.sendMessage(Message.raw("§aYour class: §f" +
                    currentProfile.getCombatClass().getDisplayName() +
                    " §7(Level " + currentProfile.getLevel() + ")"));
            player.sendMessage(Message.raw("§7Use §f/class reset§7 to change class."));
        } else {
            player.sendMessage(Message.raw("§7You have no class. Use §f/class choose <name>§7."));
        }

        player.sendMessage(Message.raw(""));
        for (ClassDefinition def : availableClasses) {
            String selected = currentProfile.getCombatClass() == def.id() ? " §a✔" : "";
            player.sendMessage(Message.raw("§e" + def.displayName() + selected));
            player.sendMessage(Message.raw("  §7" + def.description()));
            player.sendMessage(Message.raw("  §8Role: " + def.primaryRole() + " / " + def.secondaryRole()));
        }
        player.sendMessage(Message.raw(DIVIDER));
    }

    @Override
    public void showTalentMenu(PlayerContext pctx, List<TalentDefinition> classTalents,
                               PlayerProfile currentProfile) {
        Player player = pctx.player();
        if (player == null) return;

        player.sendMessage(Message.raw(DIVIDER));
        player.sendMessage(Message.raw("§d§l  TALENTS — " +
                currentProfile.getCombatClass().getDisplayName().toUpperCase()));
        player.sendMessage(Message.raw("§7Available points: §e" + currentProfile.getTalentPoints()));
        player.sendMessage(Message.raw(DIVIDER));

        for (TalentDefinition talent : classTalents) {
            int rank = currentProfile.getTalentRank(talent.id());
            boolean maxed = rank >= talent.maxRank();
            boolean canAfford = currentProfile.getTalentPoints() >= talent.cost();
            boolean prereqsMet = talent.prerequisiteIds().stream()
                    .allMatch(currentProfile::hasTalent);

            String status = maxed ? "§a[MAX]" : (canAfford && prereqsMet ? "§e[ ]" : "§c[✘]");
            String rankLabel = talent.maxRank() > 1 ? " [" + rank + "/" + talent.maxRank() + "]" : "";
            player.sendMessage(Message.raw(status + " §f" + talent.displayName() + rankLabel +
                    " §8(cost: " + talent.cost() + "pt)"));
            player.sendMessage(Message.raw("    §7" + talent.description()));

            if (!talent.prerequisiteIds().isEmpty() && rank == 0) {
                player.sendMessage(Message.raw("    §8Requires: " +
                        String.join(", ", talent.prerequisiteIds())));
            }
        }
        player.sendMessage(Message.raw("§7Use §f/talents unlock <id>§7 to unlock."));
        player.sendMessage(Message.raw(DIVIDER));
    }

    @Override
    public void sendInfo(PlayerContext pctx, String message) {
        Player player = pctx.player();
        if (player != null) player.sendMessage(Message.raw("§7" + message));
    }

    @Override
    public void sendError(PlayerContext pctx, String message) {
        Player player = pctx.player();
        if (player != null) player.sendMessage(Message.raw("§c✘ " + message));
    }

    @Override
    public void sendSuccess(PlayerContext pctx, String message) {
        Player player = pctx.player();
        if (player != null) player.sendMessage(Message.raw("§a✔ " + message));
    }
}
