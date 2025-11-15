package de.idiotischeryt.buildSystem.menusystem;

import org.bukkit.ChatColor;
import org.bukkit.Material;

/*

A class extending the functionality of the regular Menu, but making it Paginated

This pagination system was made from Jer's code sample. <3

    credits kody simpson :)
    to lazy to code my own :)
 */

public abstract class PaginatedMenu extends Menu {

    //Keep track of what page the menu is on
    public int page = 0;
    //28 is max items because with the border set below,
    //28 empty slots are remaining.
    public int maxItemsPerPage = 28;
    //the index represents the index of the slot
    //that the loop is on
    public int index = 0;

    public PaginatedMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
    }

    //Set the border and menu buttons for the menu

    public int getMaxItemsPerPage() {
        return maxItemsPerPage;
    }

    @Override
    public void addMenuBorder() {
        inventory.setItem(48, makeItem(Material.DARK_OAK_BUTTON, ChatColor.GREEN + "Left"));

        inventory.setItem(49, makeItem(Material.BARRIER, ChatColor.DARK_RED + "Close"));

        inventory.setItem(50, makeItem(Material.DARK_OAK_BUTTON, ChatColor.GREEN + "Right"));

        inventory.setItem(53, makeItem(Material.PLAYER_HEAD, ChatColor.GREEN + "Create World"));

        super.addMenuBorder();
    }
}
