package com.example.combatplugin.domain.exception;

public class UnknownTalentException extends CombatPluginException {
    public UnknownTalentException(String talentId) {
        super("Unknown talent: '" + talentId + "'. Use /talents list to see available talents.");
    }
}
