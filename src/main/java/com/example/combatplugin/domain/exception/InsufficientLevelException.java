package com.example.combatplugin.domain.exception;

public class InsufficientLevelException extends CombatPluginException {
    private final int currentLevel;
    private final int requiredLevel;

    public InsufficientLevelException(int currentLevel, int requiredLevel) {
        super("Requires level " + requiredLevel + " (you are level " + currentLevel + ").");
        this.currentLevel = currentLevel;
        this.requiredLevel = requiredLevel;
    }

    public int getCurrentLevel()  { return currentLevel; }
    public int getRequiredLevel() { return requiredLevel; }
}
