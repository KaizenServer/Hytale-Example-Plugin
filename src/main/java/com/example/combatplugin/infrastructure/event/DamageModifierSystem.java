package com.example.combatplugin.infrastructure.event;

import com.example.combatplugin.application.service.ModifierService;
import com.example.combatplugin.application.service.ProfileService;
import com.example.combatplugin.domain.model.EffectTrigger;
import com.example.combatplugin.domain.model.PlayerProfile;
import com.example.combatplugin.domain.model.TriggeredEffect;
import com.example.combatplugin.infrastructure.persistence.PlayerProfileComponent;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * Intercepts the Damage ECS event and applies active talent modifiers.
 *
 * Runs in the damage filter group (before ApplyDamage).
 * ASSUMPTION: getGroup() should return DamageModule.get().getFilterDamageGroup().
 * TODO: Uncomment the getGroup() override once DamageModule import is confirmed.
 *
 * INTEGRATION POINT: Damage.getSource() returns a Damage.Source object, NOT a
 * Ref<EntityStore> directly. The exact method to obtain a Ref from Damage.Source
 * is unknown until ./gradlew build decompiles the API.
 * TODO: Replace getAttackerRef() stub with the correct accessor.
 */
public class DamageModifierSystem extends EntityEventSystem<EntityStore, Damage> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final ComponentType<EntityStore, PlayerProfileComponent> profileComponentType;
    private final ProfileService profileService;
    private final ModifierService modifierService;

    public DamageModifierSystem(ComponentType<EntityStore, PlayerProfileComponent> profileComponentType,
                                ProfileService profileService,
                                ModifierService modifierService) {
        super(Damage.class);
        this.profileComponentType = profileComponentType;
        this.profileService = profileService;
        this.modifierService = modifierService;
    }

    @Override
    public void handle(int index,
                       @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
                       @Nonnull Store<EntityStore> store,
                       @Nonnull CommandBuffer<EntityStore> commandBuffer,
                       @Nonnull Damage event) {

        // Defensive modifier: target is taking damage
        Ref<EntityStore> targetRef = archetypeChunk.getReferenceTo(index);
        UUID targetUuid = resolveUuid(store, targetRef);
        if (targetUuid != null) {
            PlayerProfile targetProfile = profileService.find(targetUuid).orElse(null);
            if (targetProfile != null) {
                applyDefensiveTriggers(event, targetProfile, targetRef, store, commandBuffer);
            }
        }

        // Offensive modifier: attacker is dealing damage
        // INTEGRATION POINT: Damage.Source API unknown — see class javadoc.
        // TODO: Replace null with: resolveAttackerRef(event.getSource())
        Ref<EntityStore> attackerRef = resolveAttackerRef(event);
        if (attackerRef != null) {
            UUID attackerUuid = resolveUuid(store, attackerRef);
            if (attackerUuid != null) {
                PlayerProfile attackerProfile = profileService.find(attackerUuid).orElse(null);
                if (attackerProfile != null) {
                    float modified = modifierService.applyDamageModifiers(
                            event.getAmount(),
                            modifierService.computeActiveModifiers(attackerProfile));
                    modified = applyOffensiveTriggers(
                            modified, attackerProfile, attackerRef, store, commandBuffer);
                    event.setAmount(modified);
                }
            }
        }
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(profileComponentType);
    }

    // ── Trigger dispatchers ────────────────────────────────────────────────────

    private float applyOffensiveTriggers(float damage, PlayerProfile profile,
                                          Ref<EntityStore> ref,
                                          Store<EntityStore> store,
                                          CommandBuffer<EntityStore> commandBuffer) {
        for (TriggeredEffect te : modifierService.computeActiveTriggeredEffects(profile)) {
            for (TriggeredEffect.TriggerEntry entry : te.triggers()) {
                if (entry.trigger() == EffectTrigger.ON_HIT) {
                    damage = applyOnHit(entry, damage, ref, store);
                }
            }
        }
        return damage;
    }

    private void applyDefensiveTriggers(Damage event, PlayerProfile profile,
                                         Ref<EntityStore> targetRef,
                                         Store<EntityStore> store,
                                         CommandBuffer<EntityStore> commandBuffer) {
        for (TriggeredEffect te : modifierService.computeActiveTriggeredEffects(profile)) {
            for (TriggeredEffect.TriggerEntry entry : te.triggers()) {
                if (entry.trigger() == EffectTrigger.ON_TAKE_DAMAGE) {
                    applyOnTakeDamage(entry, event, targetRef, store, commandBuffer);
                }
            }
        }
    }

    private float applyOnHit(TriggeredEffect.TriggerEntry entry, float damage,
                               Ref<EntityStore> attackerRef, Store<EntityStore> store) {
        return switch (entry.effectKey()) {
            case "nc_death_pact" -> {
                // Bonus only if attacker HP < 30%
                // TODO: Add HP ratio check once EntityStatMap import is confirmed.
                LOGGER.atInfo().log("[DEBUG] nc_death_pact: HP check pending API confirmation.");
                yield damage * (1f + entry.magnitude());
            }
            default -> damage;
        };
    }

    private void applyOnTakeDamage(TriggeredEffect.TriggerEntry entry, Damage event,
                                    Ref<EntityStore> targetRef,
                                    Store<EntityStore> store,
                                    CommandBuffer<EntityStore> commandBuffer) {
        switch (entry.effectKey()) {
            case "sm_counterattack" -> {
                // INTEGRATION POINT: need attacker Ref from Damage.Source to deal counter damage.
                // TODO: Execute counter-damage via DamageSystems.executeDamage() once
                // Damage.Source → Ref conversion is confirmed.
                float counterDmg = event.getAmount() * entry.magnitude();
                LOGGER.atInfo().log("[DEBUG] sm_counterattack: would deal %.1f counter (API pending).",
                        counterDmg);
            }
            case "sm_perfect_block" -> {
                event.setCancelled(true);
                // TODO: Add 10s cooldown using a TimedCooldownComponent in v2.
                LOGGER.atInfo().log("[DEBUG] sm_perfect_block: damage cancelled.");
            }
            default -> { /* unknown key */ }
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    /**
     * INTEGRATION POINT: extracts a Ref<EntityStore> from the Damage.Source.
     * The exact accessor method is unknown until ./gradlew build decompiles Damage.Source.
     * Common patterns: source.getRef(), source.getEntityRef(), source.asRef().
     *
     * TODO: Replace this stub once the API is confirmed.
     */
    @SuppressWarnings("unchecked")
    private Ref<EntityStore> resolveAttackerRef(Damage event) {
        try {
            Object source = event.getSource();
            if (source == null) return null;
            // ASSUMPTION: Damage.Source has a getRef() method.
            // Replace with the confirmed method name after ./gradlew build.
            java.lang.reflect.Method getRef = source.getClass().getMethod("getRef");
            return (Ref<EntityStore>) getRef.invoke(source);
        } catch (Exception e) {
            return null; // silently return null until API is confirmed
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
