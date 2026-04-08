package com.example.combatplugin.domain.exception;

public class UnknownClassException extends CombatPluginException {
    public UnknownClassException(String input) {
        super("Unknown class: '" + input + "'. Use /class info to see available classes.");
    }
}
