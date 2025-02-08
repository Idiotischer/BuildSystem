package de.idiotischeryt.buildSystem.listeners;

import de.idiotischeryt.buildSystem.PlayerManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.jetbrains.annotations.NotNull;

public class PlayerListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onLeave(PlayerQuitEvent e) {
        Player player = e.getPlayer();

        PlayerManager.saveInventory(player);
        PlayerManager.saveLocation(player, player.getLocation());
    }

    @EventHandler(ignoreCancelled = true)
    public void onLKick(PlayerKickEvent e) {
        Player player = e.getPlayer();

        PlayerManager.saveInventory(player);
        PlayerManager.saveLocation(player, player.getLocation());
    }

    @EventHandler(ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        player.getInventory().clear();

        PlayerManager.loadInventory(player);
        PlayerManager.loadLocation(player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onSwitch(PlayerTeleportEvent e) {
        Player player = e.getPlayer();

        if (!e.getFrom().getWorld().equals(e.getTo().getWorld()) && e.getCause() == PlayerTeleportEvent.TeleportCause.PLUGIN) {
            Location from = e.getFrom();

            PlayerManager.saveLocation(player, from);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void changeWorld(@NotNull PlayerChangedWorldEvent e) {
        World from = e.getFrom();
        Player p = e.getPlayer();

        PlayerManager.saveInventory(p, from);

        p.getInventory().clear();

        PlayerManager.loadInventory(p);

        if (e.getPlayer().getLocation().getNearbyEntitiesByType(Player.class, 1).isEmpty())
            PlayerManager.loadLocation(p);
    }
}