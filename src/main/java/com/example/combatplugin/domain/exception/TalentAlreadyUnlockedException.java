package com.example.combatplugin.domain.exception;

public class TalentAlreadyUnlockedException extends CombatPluginException {
    public TalentAlreadyUnlockedException(String talentId) {
        super("Talent '" + talentId + "' is already unlocked.");
    }
}
