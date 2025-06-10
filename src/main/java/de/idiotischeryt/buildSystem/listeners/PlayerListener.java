package de.idiotischeryt.buildSystem.listeners;

import de.idiotischeryt.buildSystem.BuildSystem;
import de.idiotischeryt.buildSystem.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.event.server.PluginDisableEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onLeave(PlayerQuitEvent e) {
        savePlayerData(e.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onKick(PlayerKickEvent e) {
        savePlayerData(e.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        player.getInventory().clear();

        if (shouldLoad("saveAndLoadPlayerInventory")) {
            PlayerManager.loadInventory(player);
        }

        if (shouldLoad("saveAndLoadPlayerLocation")) {
            PlayerManager.loadLocation(player);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSwitch(PlayerTeleportEvent e) {
        Player player = e.getPlayer();

        if (e.getCause() == PlayerTeleportEvent.TeleportCause.EXIT_BED) return;

        if (!e.getFrom().getWorld().equals(e.getTo().getWorld()) && e.getCause() == PlayerTeleportEvent.TeleportCause.PLUGIN) {
            if (shouldLoad("saveAndLoadPlayerLocation")) {
                PlayerManager.saveLocation(player, e.getFrom());
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void changeWorld(@NotNull PlayerChangedWorldEvent e) {
        Player player = e.getPlayer();
        World from = e.getFrom();

        if (BuildSystem.getInstance().propertiesConfig == null) return;

        if (shouldLoad("saveAndLoadPlayerInventory")) {
            PlayerManager.saveInventory(player, from);
            player.getInventory().clear();
            PlayerManager.loadInventory(player);
        }

        if (shouldLoad("saveAndLoadPlayerLocation") && player.getLocation().getNearbyEntitiesByType(Player.class, 1).isEmpty()) {
            PlayerManager.loadLocation(player);
        }
    }

    @EventHandler
    public void onEnd(PluginDisableEvent e) {
        if (BuildSystem.getInstance().propertiesConfig == null) return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (shouldLoad("saveAndLoadPlayerLocation")) {
                PlayerManager.saveLocation(player, player.getLocation());
            }

            if (shouldLoad("saveAndLoadPlayerInventory")) {
                PlayerManager.saveInventory(player, player.getWorld());
            }
        }
    }

    private void savePlayerData(Player player) {
        if (shouldLoad("saveAndLoadPlayerInventory")) {
            PlayerManager.saveInventory(player);
        }

        if (shouldLoad("saveAndLoadPlayerLocation")) {
            PlayerManager.saveLocation(player, player.getLocation());
        }
    }

    private boolean shouldLoad(String key) {
        return BuildSystem.getInstance().propertiesConfig.contains(key) &&
                BuildSystem.getInstance().propertiesConfig.getBoolean(key);
    }
}