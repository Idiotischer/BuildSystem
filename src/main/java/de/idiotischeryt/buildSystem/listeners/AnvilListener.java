package de.idiotischeryt.buildSystem.listeners;

import de.idiotischeryt.buildSystem.BuildSystem;
import de.idiotischeryt.buildSystem.event.BiomeFetchEvent;
import de.idiotischeryt.buildSystem.menusystem.menu.RenameData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class AnvilListener implements Listener {

    static final List<Character> allowedChars = Arrays.asList(
            'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
            'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
            '0','1','2','3','4','5','6','7','8','9',
            '/', '.', '_', '-'
    );

    @EventHandler
    public void onAnvilClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() == null) return;

        RenameData context = BuildSystem.getInstance().getRenameContext(player.getUniqueId());
        if (context == null) return;
        if (!event.getClickedInventory().equals(context.getThisMenu().inventory)) return;

        if (event.getSlot() == 2) {
            event.setCancelled(true);
            handleAnvilPrepare(player, context);
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onAnvilClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        RenameData context = BuildSystem.getInstance().getRenameContext(player.getUniqueId());
        if (context == null) return;

        if (!event.getInventory().equals(context.getThisMenu().inventory)) return;

        event.getInventory().clear();
        BuildSystem.getInstance().unregisterRename(player.getUniqueId());
        if(event.getReason() != InventoryCloseEvent.Reason.OPEN_NEW) {
            Bukkit.getScheduler().runTask(BuildSystem.getInstance(), context.getThisMenu().currentMenu::open);
        }
    }

    private void handleAnvilPrepare(Player player, RenameData context) {
        String inputText = context.getThisMenu().inventoryView.getTopInventory().getItem(2).getItemMeta().getDisplayName();

        if (inputText.isEmpty()) {
            return;
        }

        if(!context.getThisMenu().biome) {
            if (!isValid(inputText)) {
                reopenWithError(context, player, ChatColor.RED + "Only [a-z0-9/._-] allowed!");
                return;
            }

            if (!context.allowEndWith()) {
                for (String s : context.getEndWith()) {
                    if (inputText.endsWith(s)) {
                        reopenWithError(context, player, ChatColor.RED + "Can't use template Name!");
                        return;
                    }
                }
            }

            if (context.emptyAllowed() && !context.getNeeded().contains("")) context.getNeeded().add("");
            if (context.emptyAllowed() && !context.getNeeded().contains("Other")) context.getNeeded().add("Other");

            boolean isInNeeded = context.getNeeded().contains(inputText);
            if ((context.contains() && isInNeeded) || (!context.contains() && !isInNeeded)) {
                reopenWithError(context, player, context.getErrorText());
                return;
            }

            context.getMap()[0] = inputText;

            Bukkit.getScheduler().runTaskLater(BuildSystem.getInstance(), () -> {
                if (player.getItemOnCursor() != null && !player.getItemOnCursor().getType().isAir()) {
                    player.getItemOnCursor().setType(Material.AIR);
                }
            }, 2);
        } else {
            boolean isValidBiome = false;

            for (Biome biome : Biome.values()) {
                if (biome.name().equalsIgnoreCase(inputText)) {
                    isValidBiome = true;
                    break;
                }
            }

            BiomeFetchEvent event = new BiomeFetchEvent(inputText, isValidBiome);
            event.callEvent();

            Bukkit.getScheduler().runTask(BuildSystem.getInstance(), () -> {
                if (!event.isValid()) {
                    reopenWithError(context, player, ChatColor.RED + "Not a valid Biome");
                    return;
                }

                context.getMap()[0] = inputText;

                Bukkit.getScheduler().runTaskLater(BuildSystem.getInstance(), () -> {
                    if (player.getItemOnCursor() != null && !player.getItemOnCursor().getType().isAir()) {
                        player.getItemOnCursor().setType(Material.AIR);
                    }
                }, 2);
            });
        }

        context.getOnFinish().accept(inputText);
        Bukkit.getScheduler().runTask(BuildSystem.getInstance(), context.getThisMenu().currentMenu::open);
    }


    public static boolean isValid(String input) {
        for (char c : input.toCharArray()) {
            if (!allowedChars.contains(c)) return false;
        }
        return true;
    }

    private void reopenWithError(RenameData context, Player player, String message) {
        //context.getThisMenu().inventory.close();
        context.getThisMenu().setDefaultText(message);
        Bukkit.getScheduler().runTask(BuildSystem.getInstance(), () -> {
            context.getThisMenu().openWithTitle(message);
        });
        context.getThisMenu().setMenuItems();
        player.setItemOnCursor(null);
    }
}
