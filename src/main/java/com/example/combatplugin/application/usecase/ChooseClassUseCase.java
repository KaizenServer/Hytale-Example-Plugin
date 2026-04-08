package com.example.combatplugin.application.usecase;

import com.example.combatplugin.application.port.IStatApplicator;
import com.example.combatplugin.application.service.ClassService;
import com.example.combatplugin.application.service.ProfileService;
import com.example.combatplugin.domain.exception.ClassAlreadyChosenException;
import com.example.combatplugin.domain.exception.UnknownClassException;
import com.example.combatplugin.domain.model.ClassDefinition;
import com.example.combatplugin.domain.model.PlayerProfile;

import java.util.UUID;

public class ChooseClassUseCase {

    private final ClassService classService;
    private final ProfileService profileService;
    private final IStatApplicator statApplicator;

    public ChooseClassUseCase(ClassService classService, ProfileService profileService,
                               IStatApplicator statApplicator) {
        this.classService = classService;
        this.profileService = profileService;
        this.statApplicator = statApplicator;
    }

    /**
     * Assigns a class to the player.
     *
     * @param uuid       The player's UUID.
     * @param classInput The class name string typed by the player.
     * @param store      Hytale EntityStore (passed as Object to avoid import in application layer).
     * @param ref        Hytale entity Ref.
     * @return The chosen ClassDefinition (for UI feedback).
     */
    public ClassDefinition execute(UUID uuid, String classInput, Object store, Object ref)
            throws UnknownClassException, ClassAlreadyChosenException {

        PlayerProfile profile = profileService.getOrDefault(uuid);
        ClassDefinition classDef = classService.resolveClass(classInput);
        classService.validateCanChoose(profile.getCombatClass(), classDef.id());

        PlayerProfile updated = profile.withClass(classDef.id());
        profileService.save(uuid, updated);

        // Apply base class stat modifiers (from any initially unlocked talents, none in v1)
        statApplicator.applyProfileModifiers(uuid, updated, store, ref);

        return classDef;
    }
}
