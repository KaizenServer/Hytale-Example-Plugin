package com.example.combatplugin.infrastructure.event;

import com.example.combatplugin.application.port.ISummonAdapter;
import com.example.combatplugin.application.service.ModifierService;
import com.example.combatplugin.application.service.ProfileService;
import com.example.combatplugin.domain.model.EffectTrigger;
import com.example.combatplugin.domain.model.PlayerProfile;
import com.example.combatplugin.domain.model.TriggeredEffect;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * Handles ON_KILL triggered effects for all classes.
 *
 * INTEGRATION POINT: DeathComponent.getDeathInfo().getSource() returns a Damage.Source,
 * not a Ref<EntityStore>. The killer Ref must be extracted via the Damage.Source API.
 * TODO: Replace resolveKillerRef() stub once ./gradlew build reveals the Damage.Source API.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class DeathSynergySystem extends DeathSystems.OnDeathSystem {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final ProfileService profileService;
    private final ModifierService modifierService;
    private final ISummonAdapter summonAdapter;

    public DeathSynergySystem(ProfileService profileService,
                               ModifierService modifierService,
                               ISummonAdapter summonAdapter) {
        this.profileService = profileService;
        this.modifierService = modifierService;
        this.summonAdapter = summonAdapter;
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
     */
    @SuppressWarnings("unchecked")
    private Ref<EntityStore> resolveKillerRef(DeathComponent deathComponent) {
        try {
            if (deathComponent.getDeathInfo() == null) return null;
            Object source = deathComponent.getDeathInfo().getSource();
            if (source == null) return null;
            java.lang.reflect.Method getRef = source.getClass().getMethod("getRef");
            return (Ref<EntityStore>) getRef.invoke(source);
        } catch (Exception e) {
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
