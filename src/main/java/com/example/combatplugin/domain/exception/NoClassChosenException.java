package com.example.combatplugin.domain.exception;

public class NoClassChosenException extends CombatPluginException {
    public NoClassChosenException() {
        super("You have not chosen a class yet. Use /class choose <className>.");
    }
}
