package com.example.combatplugin.infrastructure.ui;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.UUID;

/**
 * Carrier for all player context needed by UI presenters.
 *
 * {@link TextFallbackUiPresenter} uses {@link #player()} to send chat messages.
 * {@link HytaleUiPresenter} uses {@link #player()}.getPageManager() to open custom pages,
 * passing {@link #ref()} and {@link #store()} as required by the confirmed API:
 *   player.getPageManager().openCustomPage(ref, store, page)
 */
public record PlayerContext(
        PlayerRef playerRef,
        UUID uuid,
        Player player,
        Ref<EntityStore> ref,
        Store<EntityStore> store) {}
