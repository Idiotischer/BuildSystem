package de.idiotischeryt.buildSystem.listeners;

import de.idiotischeryt.buildSystem.BuildSystem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class Listener implements org.bukkit.event.Listener {
    @EventHandler(ignoreCancelled = true)
    public void onLeave(PlayerQuitEvent e) {
        if (!e.getPlayer().getWorld().getName().equals("world")) {

            BuildSystem.updateOrSetPlayer(BuildSystem.invConfiguration, e.getPlayer(), e.getPlayer().getLocation());

            ItemStack[] contents = e.getPlayer().getInventory().getContents();
            ItemStack[] armor = e.getPlayer().getInventory().getArmorContents();
            ItemStack[] extras = e.getPlayer().getInventory().getExtraContents();

            ItemStack[] stacks = Arrays.copyOf(contents, contents.length + armor.length + extras.length);
            System.arraycopy(armor, 0, stacks, contents.length, armor.length);
            System.arraycopy(extras, 0, stacks, contents.length + armor.length, extras.length);


            BuildSystem.updateOrSetPlayer(BuildSystem.spawnConfiguration, e.getPlayer(), stacks);
        }
    }
}