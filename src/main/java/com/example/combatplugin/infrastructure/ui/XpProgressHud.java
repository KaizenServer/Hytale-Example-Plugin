package com.example.combatplugin.infrastructure.ui;

import com.example.combatplugin.application.service.ProgressionService;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;

/**
 * Persistent HUD element showing the player's current level and XP progress.
 *
 * Displayed as: [Lv. 5]  [1200 / 2025 XP]
 *
 * Text-only: Anchor is a structured type and cannot be set via cmd.set().
 * Only .Text properties are used.
 */
public class XpProgressHud extends CustomUIHud {

    private static final String UI_KEY = "XpProgressHud.ui";

    private final int level;
    private final long xp;
    private final ProgressionService progressionService;

    public XpProgressHud(PlayerRef playerRef, int level, long xp,
                         ProgressionService progressionService) {
        super(playerRef);
        this.level = level;
        this.xp = xp;
        this.progressionService = progressionService;
    }

    @Override
    public void build(@Nonnull UICommandBuilder cmd) {
        cmd.append(UI_KEY);

        cmd.set("#XpLevel.Text", "Lv. " + level);

        long xpForCurrentLevel = progressionService.xpRequiredForLevel(level);
        long xpForNextLevel    = progressionService.xpRequiredForLevel(level + 1);
        long xpProgress = xp - xpForCurrentLevel;
        long xpNeeded   = xpForNextLevel - xpForCurrentLevel;

        if (xpNeeded > 0) {
            cmd.set("#XpText.Text", xpProgress + " / " + xpNeeded + " XP");
        } else {
            cmd.set("#XpText.Text", "MAX LEVEL");
        }
    }
}
