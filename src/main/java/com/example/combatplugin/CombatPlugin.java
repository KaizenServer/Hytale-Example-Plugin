package com.example.combatplugin;

import com.example.combatplugin.application.port.IStatApplicator;
import com.example.combatplugin.application.port.ISummonAdapter;
import com.example.combatplugin.application.port.IUiPresenter;
import com.example.combatplugin.application.service.ClassService;
import com.example.combatplugin.application.service.ModifierService;
import com.example.combatplugin.application.service.ProfileService;
import com.example.combatplugin.application.service.ProgressionService;
import com.example.combatplugin.application.service.TalentService;
import com.example.combatplugin.application.usecase.AwardXpUseCase;
import com.example.combatplugin.application.usecase.ChooseClassUseCase;
import com.example.combatplugin.application.usecase.RemoveTalentRankUseCase;
import com.example.combatplugin.application.usecase.UnlockTalentUseCase;
import com.example.combatplugin.config.CombatConfig;
import com.example.combatplugin.infrastructure.command.LevelCommandCollection;
import com.example.combatplugin.infrastructure.command.XpCommandCollection;
import com.example.combatplugin.data.DefaultClasses;
import com.example.combatplugin.data.TalentEffects;
import com.example.combatplugin.domain.model.ClassDefinition;
import com.example.combatplugin.domain.model.CombatClass;
import com.example.combatplugin.infrastructure.command.ClassCommandCollection;
import com.example.combatplugin.infrastructure.command.TalentsCommand;
import com.example.combatplugin.infrastructure.event.DamageModifierSystem;
import com.example.combatplugin.infrastructure.event.DeathSynergySystem;
import com.example.combatplugin.infrastructure.event.PlayerEventListener;
import com.example.combatplugin.infrastructure.persistence.EcsProfileRepository;
import com.example.combatplugin.infrastructure.persistence.PlayerProfileComponent;
import com.example.combatplugin.infrastructure.stat.HytaleStatApplicator;
import com.example.combatplugin.infrastructure.summon.HytaleSummonAdapter;
import com.example.combatplugin.infrastructure.ui.HytaleUiPresenter;
import com.example.combatplugin.system.ProfileInitSystem;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.EnumMap;
import java.util.Map;

/**
 * Main entry point for the CombatPlugin.
 *
 * Wiring order:
 *   1. Config
 *   2. Data registries (classes + talents)
 *   3. Services (pure domain, no Hytale deps)
 *   4. ECS component registration → ComponentType
 *   5. Infrastructure (repository, stat applicator, summons)
 *   6. Use cases (shared by UI presenter and commands)
 *   7. UI presenter
 *   8. ECS systems
 *   9. Global events
 *  10. Commands — /class, /talents, /xp, /level
 */
public class CombatPlugin extends JavaPlugin {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public CombatPlugin(JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {

        // ── 1. Config ─────────────────────────────────────────────────────────
        CombatConfig config = CombatConfig.DEFAULT;

        // ── 2. Data registries ────────────────────────────────────────────────
        Map<CombatClass, ClassDefinition> classRegistry = new EnumMap<>(CombatClass.class);
        DefaultClasses.register(classRegistry);

        TalentEffects talentEffects = new TalentEffects();
        talentEffects.registerAll();

        // ── 3. Services (no Hytale deps) ──────────────────────────────────────
        ClassService       classService       = new ClassService(classRegistry);
        TalentService      talentService      = new TalentService(talentEffects.getTalentMap());
        ModifierService    modifierService    = new ModifierService(talentEffects.getEffectMap());
        ProgressionService progressionService = new ProgressionService(config);

        // ── 4. ECS component registration ─────────────────────────────────────
        ComponentType<EntityStore, PlayerProfileComponent> profileComponentType =
            this.getEntityStoreRegistry().registerComponent(
                PlayerProfileComponent.class, PlayerProfileComponent::new);

        // ── 5. Infrastructure ─────────────────────────────────────────────────
        EcsProfileRepository profileRepository = new EcsProfileRepository(profileComponentType);
        ProfileService       profileService    = new ProfileService(profileRepository);
        IStatApplicator      statApplicator    = new HytaleStatApplicator(modifierService);
        ISummonAdapter       summonAdapter     = new HytaleSummonAdapter();

        // ── 6. Use cases (shared by UI presenter and systems) ────────────────
        ChooseClassUseCase      chooseClassUseCase  = new ChooseClassUseCase(
                classService, profileService, statApplicator);
        UnlockTalentUseCase     unlockTalentUseCase = new UnlockTalentUseCase(
                talentService, profileService, statApplicator);
        RemoveTalentRankUseCase removeTalentUseCase = new RemoveTalentRankUseCase(
                profileService, talentEffects.getTalentMap(), statApplicator);
        AwardXpUseCase          awardXpUseCase      = new AwardXpUseCase(
                profileService, progressionService);

        // ── 7. UI presenter ───────────────────────────────────────────────────
        IUiPresenter uiPresenter = new HytaleUiPresenter(
                chooseClassUseCase, unlockTalentUseCase, removeTalentUseCase);

        // ── 8. ECS systems ────────────────────────────────────────────────────
        ProfileInitSystem    profileInitSystem = new ProfileInitSystem(
                profileRepository, statApplicator, progressionService);
        DamageModifierSystem damageSystem      = new DamageModifierSystem(
                profileComponentType, profileService, modifierService);
        DeathSynergySystem   deathSystem       = new DeathSynergySystem(
                profileService, modifierService, summonAdapter, awardXpUseCase,
                statApplicator, config, progressionService);

        this.getEntityStoreRegistry().registerSystem(profileInitSystem);
        this.getEntityStoreRegistry().registerSystem(damageSystem);
        this.getEntityStoreRegistry().registerSystem(deathSystem);

        // ── 9. Global events ──────────────────────────────────────────────────
        PlayerEventListener playerListener = new PlayerEventListener(
                profileRepository, statApplicator, progressionService);
        this.getEventRegistry().registerGlobal(PlayerReadyEvent.class,     playerListener::onJoin);
        this.getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, playerListener::onLeave);

        // ── 10. Commands ──────────────────────────────────────────────────────
        this.getCommandRegistry().registerCommand(
                new ClassCommandCollection(classService, profileService, uiPresenter));
        this.getCommandRegistry().registerCommand(
                new TalentsCommand(talentService, profileService, uiPresenter));
        this.getCommandRegistry().registerCommand(
                new XpCommandCollection(profileService, progressionService, uiPresenter, statApplicator));
        this.getCommandRegistry().registerCommand(
                new LevelCommandCollection(profileService, progressionService, uiPresenter));

        LOGGER.atInfo().log("[CombatPlugin] Setup complete. Classes: %d, Talents: %d",
                classService.getAllClasses().size(),
                talentEffects.getTalentMap().size());
    }
}
