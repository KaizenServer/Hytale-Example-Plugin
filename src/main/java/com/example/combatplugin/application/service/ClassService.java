package com.example.combatplugin.application.service;

import com.example.combatplugin.domain.exception.ClassAlreadyChosenException;
import com.example.combatplugin.domain.exception.UnknownClassException;
import com.example.combatplugin.domain.model.ClassDefinition;
import com.example.combatplugin.domain.model.CombatClass;

import java.util.Collection;
import java.util.Map;

/**
 * Pure domain logic for class operations.
 * Stateless — receives the registry at construction time.
 */
public class ClassService {

    private final Map<CombatClass, ClassDefinition> classRegistry;

    public ClassService(Map<CombatClass, ClassDefinition> classRegistry) {
        this.classRegistry = classRegistry;
    }

    /**
     * Resolves a string input to a valid ClassDefinition.
     * Throws UnknownClassException if not found or if NONE is specified.
     */
    public ClassDefinition resolveClass(String input) throws UnknownClassException {
        CombatClass combatClass = CombatClass.fromString(input);
        if (combatClass == CombatClass.NONE) {
            throw new UnknownClassException(input);
        }
        ClassDefinition def = classRegistry.get(combatClass);
        if (def == null) {
            throw new UnknownClassException(input);
        }
        return def;
    }

    /**
     * Validates that the player can choose the given class (must be unclassed).
     * Throws ClassAlreadyChosenException if the player already has a class.
     */
    public void validateCanChoose(CombatClass current, CombatClass desired)
            throws ClassAlreadyChosenException {
        if (current != CombatClass.NONE) {
            throw new ClassAlreadyChosenException(current);
        }
    }

    public ClassDefinition getDefinition(CombatClass combatClass) {
        return classRegistry.get(combatClass);
    }

    public Collection<ClassDefinition> getAllClasses() {
        return classRegistry.values();
    }
}
