package de.idiotischeryt.buildSystem.listeners;

import de.idiotischeryt.buildSystem.PlayerManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onLeave(PlayerQuitEvent e) {
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
        if (!e.getFrom().getWorld().getName().equals(e.getTo().getWorld().getName())) {
            Location from = e.getFrom();
            Player p = e.getPlayer();

            PlayerManager.saveLocation(p, from);

        }
    }

    @EventHandler(ignoreCancelled = true)
    public void changeWorld(@NotNull PlayerChangedWorldEvent e) {
        World from = e.getFrom();
        Player p = e.getPlayer();

        PlayerManager.saveInventory(p, from);

        p.getInventory().clear();

        PlayerManager.loadInventory(p);

        PlayerManager.loadLocation(p);
    }
}