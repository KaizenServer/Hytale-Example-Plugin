package com.example.combatplugin.domain.exception;

import com.example.combatplugin.domain.model.CombatClass;

public class ClassAlreadyChosenException extends CombatPluginException {
    public ClassAlreadyChosenException(CombatClass current) {
        super("You already have the class: " + current.getDisplayName() + ". Use /class reset first.");
    }
}
