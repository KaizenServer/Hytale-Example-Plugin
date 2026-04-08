package com.example.combatplugin;

import com.example.combatplugin.application.port.IStatApplicator;
import com.example.combatplugin.application.port.IUiPresenter;
import com.example.combatplugin.application.service.ClassService;
import com.example.combatplugin.application.service.ModifierService;
import com.example.combatplugin.application.service.ProfileService;
import com.example.combatplugin.application.service.ProgressionService;
import com.example.combatplugin.application.service.TalentService;
import com.example.combatplugin.application.usecase.ChooseClassUseCase;
import com.example.combatplugin.application.usecase.RemoveTalentRankUseCase;
import com.example.combatplugin.application.usecase.UnlockTalentUseCase;
import com.example.combatplugin.config.CombatConfig;
import com.example.combatplugin.data.DefaultClasses;
import com.example.combatplugin.data.TalentEffects;
import com.example.combatplugin.domain.model.ClassDefinition;
import com.example.combatplugin.domain.model.CombatClass;
import com.example.combatplugin.infrastructure.command.ClassCommandCollection;
import com.example.combatplugin.infrastructure.command.LevelCommandCollection;
import com.example.combatplugin.infrastructure.command.TalentsCommand;
import com.example.combatplugin.infrastructure.command.XpCommandCollection;
import com.example.combatplugin.infrastructure.persistence.EcsProfileRepository;
import com.example.combatplugin.infrastructure.persistence.PlayerProfileComponent;
import com.example.combatplugin.infrastructure.stat.HytaleStatApplicator;
import com.example.combatplugin.infrastructure.ui.HytaleUiPresenter;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
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
 *   3. Services
 *   4. ECS component registration → ComponentType
 *   5. Infrastructure (repository, stat applicator, summons)
 *   6. Use cases (shared by UI presenter and commands)
 *   7. UI presenter (HytaleUiPresenter wrapping the use cases)
 *   8. ECS systems  [constructed but registration commented out — INTEGRATION POINT]
 *   9. Global events [same]
 *  10. Commands
 */
public class CombatPlugin extends JavaPlugin {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public CombatPlugin(JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {

        // ── 1. Config ─────────────────────────────────────────────────────────
        // TODO: Load from combat_config.json via Hytale config API when available.
        CombatConfig config = CombatConfig.DEFAULT;

        // ── 2. Data registries ────────────────────────────────────────────────
        Map<CombatClass, ClassDefinition> classRegistry = new EnumMap<>(CombatClass.class);
        DefaultClasses.register(classRegistry);

        TalentEffects talentEffects = new TalentEffects();
        talentEffects.registerAll();

        // ── 3. Services (no Hytale deps) ──────────────────────────────────────
        ClassService       classService       = new ClassService(classRegistry);
        TalentService      talentService      = new TalentService(talentEffects.getTalentMap());
        ProgressionService progressionService = new ProgressionService(config);
        ModifierService    modifierService    = new ModifierService(talentEffects.getEffectMap());

        // ── 4. ECS component registration ─────────────────────────────────────
        // INTEGRATION POINT: Uncomment once Hytale JARs are decompiled via ./gradlew build.
        //
        // ComponentType<EntityStore, PlayerProfileComponent> profileComponentType =
        //     this.getEntityStoreRegistry().registerComponent(
        //         PlayerProfileComponent.class, PlayerProfileComponent::new);
        //
        ComponentType<EntityStore, PlayerProfileComponent> profileComponentType = null; // replace above

        // ── 5. Infrastructure ─────────────────────────────────────────────────
        EcsProfileRepository profileRepository = new EcsProfileRepository(profileComponentType);
        ProfileService       profileService    = new ProfileService(profileRepository);
        IStatApplicator      statApplicator    = new HytaleStatApplicator(modifierService);
        // summonAdapter used by DeathSynergySystem — wire when ECS registration is uncommented
        // HytaleSummonAdapter summonAdapter = new HytaleSummonAdapter();

        // ── 6. Use cases (shared by UI and commands) ──────────────────────────
        ChooseClassUseCase      chooseClassUseCase  = new ChooseClassUseCase(
                classService, profileService, statApplicator);
        UnlockTalentUseCase     unlockTalentUseCase = new UnlockTalentUseCase(
                talentService, profileService, statApplicator);
        RemoveTalentRankUseCase removeTalentUseCase = new RemoveTalentRankUseCase(
                profileService, talentEffects.getTalentMap(), statApplicator);

        // ── 7. UI presenter ───────────────────────────────────────────────────
        // HytaleUiPresenter opens ClassSelectionPage / TalentTreePage via
        //   player.getPageManager().openCustomPage(ref, store, page)
        // and falls back to chat messages if anything fails.
        // Swap to new TextFallbackUiPresenter() here for chat-only output.
        IUiPresenter uiPresenter = new HytaleUiPresenter(
                chooseClassUseCase, unlockTalentUseCase, removeTalentUseCase);

        // ── 8. ECS systems ────────────────────────────────────────────────────
        // INTEGRATION POINT: Uncomment registrations once JARs are decompiled.
        //
        // new ProfileInitSystem(profileComponentType)
        // new DamageModifierSystem(profileComponentType, profileService, modifierService)
        // new DeathSynergySystem(profileService, modifierService, summonAdapter)
        //
        // this.getEntityStoreRegistry().registerSystem(profileInitSystem);
        // this.getEntityStoreRegistry().registerSystem(damageSystem);
        // this.getEntityStoreRegistry().registerSystem(deathSystem);

        // ── 9. Global events ──────────────────────────────────────────────────
        // INTEGRATION POINT: Uncomment once JARs are decompiled.
        //
        // PlayerEventListener playerListener = new PlayerEventListener(profileRepository, statApplicator);
        // StubCombatEventListener stubListener = new StubCombatEventListener();
        // this.getEventRegistry().registerGlobal(PlayerReadyEvent.class,    playerListener::onJoin);
        // this.getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, playerListener::onLeave);

        // ── 10. Commands ──────────────────────────────────────────────────────
        this.getCommandRegistry().registerCommand(
                new ClassCommandCollection(classService, profileService,
                        talentService, statApplicator, uiPresenter));
        this.getCommandRegistry().registerCommand(
                new TalentsCommand(unlockTalentUseCase, talentService, profileService,
                        statApplicator, uiPresenter));
        this.getCommandRegistry().registerCommand(
                new XpCommandCollection(profileService, progressionService, uiPresenter));
        this.getCommandRegistry().registerCommand(
                new LevelCommandCollection(profileService, progressionService, uiPresenter));

        LOGGER.atInfo().log("[CombatPlugin] Setup complete. Classes: %d, Talents: %d",
                classService.getAllClasses().size(),
                talentEffects.getTalentMap().size());
    }

}
