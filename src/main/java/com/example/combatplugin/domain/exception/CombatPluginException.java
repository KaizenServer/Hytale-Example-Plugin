package com.example.combatplugin.domain.exception;

/** Base checked exception for all domain-level errors in CombatPlugin. */
public class CombatPluginException extends Exception {
    public CombatPluginException(String message) {
        super(message);
    }
    public CombatPluginException(String message, Throwable cause) {
        super(message, cause);
    }
}
