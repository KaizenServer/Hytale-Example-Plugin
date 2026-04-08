package com.example.combatplugin.domain.exception;

public class InsufficientTalentPointsException extends CombatPluginException {
    public InsufficientTalentPointsException(int have, int need) {
        super("Not enough talent points. Have: " + have + ", need: " + need + ".");
    }
}
