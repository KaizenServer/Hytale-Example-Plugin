package com.example.combatplugin.domain.exception;

import java.util.List;

public class TalentPrerequisiteNotMetException extends CombatPluginException {
    public TalentPrerequisiteNotMetException(String talentId, List<String> missingPrereqs) {
        super("Cannot unlock '" + talentId + "'. Missing prerequisites: " + String.join(", ", missingPrereqs));
    }
}
