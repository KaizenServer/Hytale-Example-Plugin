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
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * Handles ON_SLOT_SWITCH triggered effects (Technocrat: Quick Hands, Overclock).
 *
 * INTEGRATION POINT: SwitchActiveSlotEvent's exact package is unknown until
 * ./gradlew build decompiles the game. This system is a complete stub.
 *
 * HOW TO ACTIVATE:
 *   1. Run ./gradlew build.
 *   2. Find SwitchActiveSlotEvent in the decompiled sources.
 *   3. Change the class signature to:
 *      public class SlotSwitchSystem extends EntityEventSystem<EntityStore, SwitchActiveSlotEvent>
 *   4. Implement the handle() method using the pattern in DamageModifierSystem.
 *   5. Register in CombatPlugin.setup():
 *      getEntityStoreRegistry().registerSystem(new SlotSwitchSystem(...))
 *
 * Until then this class exists to hold the effect-dispatch logic so it isn't lost.
 */
public class SlotSwitchSystem {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final ComponentType<EntityStore, PlayerProfileComponent> profileComponentType;
    private final ProfileService profileService;
    private final ModifierService modifierService;

    public SlotSwitchSystem(ComponentType<EntityStore, PlayerProfileComponent> profileComponentType,
                             ProfileService profileService,
                             ModifierService modifierService) {
        this.profileComponentType = profileComponentType;
        this.profileService = profileService;
        this.modifierService = modifierService;
    }

    /**
     * Called by the ECS system once SwitchActiveSlotEvent is confirmed.
     * Paste this body into the EntityEventSystem.handle() override.
     */
    public void onSlotSwitch(Ref<EntityStore> ref, Store<EntityStore> store,
                              CommandBuffer<EntityStore> commandBuffer,
                              int oldSlot, int newSlot) {
        UUID uuid = resolveUuid(store, ref);
        if (uuid == null) return;

        PlayerProfile profile = profileService.find(uuid).orElse(null);
        if (profile == null) return;

        for (TriggeredEffect te : modifierService.computeActiveTriggeredEffects(profile)) {
            for (TriggeredEffect.TriggerEntry entry : te.triggers()) {
                if (entry.trigger() == EffectTrigger.ON_SLOT_SWITCH) {
                    dispatchEffect(entry, ref, store, commandBuffer);
                }
            }
        }
    }

    private void dispatchEffect(TriggeredEffect.TriggerEntry entry,
                                 Ref<EntityStore> ref,
                                 Store<EntityStore> store,
                                 CommandBuffer<EntityStore> commandBuffer) {
        switch (entry.effectKey()) {
            case "tc_quick_hands" ->
                // TODO: Apply temporary stamina buff via EntityStatMap once import is confirmed.
                // TODO: Remove buff after 3s using a DelayedEntitySystem in v2.
                LOGGER.atInfo().log("[DEBUG] tc_quick_hands: +%.0f%% stamina burst (API pending).",
                        entry.magnitude() * 100f);

            case "tc_overclock" ->
                // TODO: Apply temporary damage buff tracked by a TimedBuffComponent in v2.
                LOGGER.atInfo().log("[DEBUG] tc_overclock: +%.0f%% damage burst (API pending).",
                        entry.magnitude() * 100f);

            default ->
                LOGGER.atInfo().log("[DEBUG] Unknown ON_SLOT_SWITCH effect: %s", entry.effectKey());
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
