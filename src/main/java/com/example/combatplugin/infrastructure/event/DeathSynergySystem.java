package com.example.combatplugin.infrastructure.event;

import com.example.combatplugin.application.port.IStatApplicator;
import com.example.combatplugin.application.port.ISummonAdapter;
import com.example.combatplugin.application.service.ModifierService;
import com.example.combatplugin.application.service.ProfileService;
import com.example.combatplugin.application.service.ProgressionService;
import com.example.combatplugin.application.usecase.AwardXpUseCase;
import com.example.combatplugin.config.CombatConfig;
import com.example.combatplugin.domain.model.EffectTrigger;
import com.example.combatplugin.domain.model.PlayerProfile;
import com.example.combatplugin.domain.model.TriggeredEffect;
import com.example.combatplugin.infrastructure.ui.XpProgressHud;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * Handles ON_KILL triggered effects for all classes, plus XP rewards for kills.
 *
 * INTEGRATION POINT: DeathComponent.getDeathInfo().getSource() returns a Damage.Source,
 * not a Ref<EntityStore>. The killer Ref must be extracted via the Damage.Source API.
 * TODO: Replace resolveKillerRef() stub once ./gradlew build reveals the Damage.Source API.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class DeathSynergySystem extends DeathSystems.OnDeathSystem {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final int STAT_GAIN_PER_LEVEL = 5;

    private final ProfileService profileService;
    private final ModifierService modifierService;
    private final ISummonAdapter summonAdapter;
    private final AwardXpUseCase awardXpUseCase;
    private final IStatApplicator statApplicator;
    private final CombatConfig config;
    private final ProgressionService progressionService;

    public DeathSynergySystem(ProfileService profileService,
                               ModifierService modifierService,
                               ISummonAdapter summonAdapter,
                               AwardXpUseCase awardXpUseCase,
                               IStatApplicator statApplicator,
                               CombatConfig config,
                               ProgressionService progressionService) {
        this.profileService = profileService;
        this.modifierService = modifierService;
        this.summonAdapter = summonAdapter;
        this.awardXpUseCase = awardXpUseCase;
        this.statApplicator = statApplicator;
        this.config = config;
        this.progressionService = progressionService;
    }

    @Override
    public void onComponentAdded(@Nonnull Ref ref,
                                  @Nonnull DeathComponent deathComponent,
                                  @Nonnull Store store,
                                  @Nonnull CommandBuffer commandBuffer) {
        // INTEGRATION POINT: Damage.Source → Ref<EntityStore> conversion is a stub.
        Ref<EntityStore> killerRef = resolveKillerRef(deathComponent);
        if (killerRef == null) return;

        UUID killerUuid = resolveUuid((Store<EntityStore>) store, killerRef);
        if (killerUuid == null) return;

        PlayerProfile killerProfile = profileService.find(killerUuid).orElse(null);
        if (killerProfile == null) return;

        // ── Grant XP for the kill (formula: 20 + 2 * level before kill) ─────
        long xpGain = 20L + 2L * killerProfile.getLevel();
        AwardXpUseCase.Result xpResult = awardXpUseCase.execute(killerUuid, xpGain);

        // ── Re-apply stats so level-based bonuses take effect immediately ─────
        PlayerProfile updatedProfile = profileService.find(killerUuid).orElse(null);
        if (updatedProfile != null) {
            statApplicator.applyProfileModifiers(killerUuid, updatedProfile,
                    (Store<EntityStore>) store, killerRef);
        }

        // ── Send feedback to the killer player ────────────────────────────────
        Player killerPlayer = null;
        try {
            killerPlayer = ((Store<EntityStore>) store).getComponent(killerRef, Player.getComponentType());
        } catch (Exception e) {
            LOGGER.atWarning().log("[CombatPlugin] Could not resolve Player for XP feedback (uuid=%s): %s",
                    killerUuid, e.toString());
        }

        if (killerPlayer != null) {
            if (config.xpDebugMessages()) {
                if (xpResult.leveledUp()) {
                    int statGain = (xpResult.newLevel() - xpResult.oldLevel()) * STAT_GAIN_PER_LEVEL;
                    killerPlayer.sendMessage(Message.raw(
                            "§d§l⬆ LEVEL UP! §r§fYou are now §dlevel " + xpResult.newLevel()
                            + " §7(+" + xpResult.talentPointsGranted() + " talent pt"
                            + (xpResult.talentPointsGranted() != 1 ? "s" : "")
                            + " | §c+" + statGain + " HP §b+" + statGain + " Mana §a+" + statGain + " Stamina§7)"));
                }
                killerPlayer.sendMessage(Message.raw("§e+" + xpResult.xpAwarded() + " XP"));
            }

            // ── Update XP HUD ─────────────────────────────────────────────────
            if (updatedProfile != null) {
                try {
                    @SuppressWarnings("removal")
                    com.hypixel.hytale.server.core.universe.PlayerRef killerPlayerRef =
                            killerPlayer.getPlayerRef();
                    if (killerPlayerRef != null) {
                        XpProgressHud hud = new XpProgressHud(killerPlayerRef,
                                updatedProfile.getLevel(), updatedProfile.getXp(), progressionService);
                        killerPlayer.getHudManager().setCustomHud(killerPlayerRef, hud);
                    }
                } catch (Exception e) {
                    LOGGER.atWarning().log("[CombatPlugin] Failed to update XP HUD for %s: %s",
                            killerUuid, e.toString());
                }
            }
        }

        // ── Handle ON_KILL triggered effects ──────────────────────────────────
        for (TriggeredEffect te : modifierService.computeActiveTriggeredEffects(killerProfile)) {
            for (TriggeredEffect.TriggerEntry entry : te.triggers()) {
                if (entry.trigger() == EffectTrigger.ON_KILL) {
                    handleOnKill(entry, killerUuid, killerRef,
                            (Store<EntityStore>) store, (CommandBuffer<EntityStore>) commandBuffer);
                }
            }
        }
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(); // Match all entity deaths
    }

    // ── Effect handlers ────────────────────────────────────────────────────────

    private void handleOnKill(TriggeredEffect.TriggerEntry entry, UUID killerUuid,
                               Ref<EntityStore> killerRef,
                               Store<EntityStore> store,
                               CommandBuffer<EntityStore> commandBuffer) {
        switch (entry.effectKey()) {
            case "nc_death_surge" ->
                    summonAdapter.spawnSummon(killerUuid, "skeleton_warrior", entry.magnitude(), store);

            case "nc_soul_harvest" -> {
                // INTEGRATION POINT: EntityStatMap import unconfirmed — see HytaleStatApplicator.
                // TODO: Call statMap.addStatValue(getMana(), magnitude) once import is confirmed.
                LOGGER.atInfo().log("[DEBUG] nc_soul_harvest: +%.0f mana (API pending).",
                        entry.magnitude());
            }
            default ->
                    LOGGER.atInfo().log("[DEBUG] Unknown ON_KILL effect: %s", entry.effectKey());
        }
    }

    // ── Stubs pending API confirmation ─────────────────────────────────────────

    /**
     * INTEGRATION POINT: Damage.Source → Ref<EntityStore>
     * TODO: Replace with confirmed accessor once ./gradlew build decompiles Damage.Source.
     * Logs a warning when the stub fails so XP loss is visible in server logs.
     */
    @SuppressWarnings("unchecked")
    private Ref<EntityStore> resolveKillerRef(DeathComponent deathComponent) {
        try {
            if (deathComponent.getDeathInfo() == null) {
                LOGGER.atWarning().log("[CombatPlugin] resolveKillerRef: getDeathInfo() is null — no XP awarded.");
                return null;
            }
            Object source = deathComponent.getDeathInfo().getSource();
            if (source == null) {
                LOGGER.atWarning().log("[CombatPlugin] resolveKillerRef: getSource() is null — no XP awarded.");
                return null;
            }
            java.lang.reflect.Method getRef = source.getClass().getMethod("getRef");
            Ref<EntityStore> killerRef = (Ref<EntityStore>) getRef.invoke(source);
            if (killerRef == null) {
                LOGGER.atWarning().log("[CombatPlugin] resolveKillerRef: getRef() returned null — no XP awarded. Source class: %s",
                        source.getClass().getName());
            }
            return killerRef;
        } catch (NoSuchMethodException e) {
            LOGGER.atWarning().log("[CombatPlugin] resolveKillerRef: Damage.Source has no getRef() method — stub needs updating. Source class: %s",
                    e.getMessage());
            return null;
        } catch (Exception e) {
            LOGGER.atWarning().log("[CombatPlugin] resolveKillerRef: reflection failed — %s", e.toString());
            return null;
        }
    }

    private UUID resolveUuid(Store<EntityStore> store, Ref<EntityStore> ref) {
        try {
            com.hypixel.hytale.server.core.entity.UUIDComponent uuidComp =
                    store.getComponent(ref, com.hypixel.hytale.server.core.entity.UUIDComponent.getComponentType());
            return uuidComp != null ? uuidComp.getUuid() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
