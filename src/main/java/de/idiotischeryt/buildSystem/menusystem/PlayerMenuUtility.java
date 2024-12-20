package de.idiotischeryt.buildSystem.menusystem;

import org.bukkit.entity.Player;

/*
Companion class to all menus. This is needed to pass information across the entire
 menu system no matter how many inventories are opened or closed.

 Each player has one of these objects, and only one.
    credits kody simpson :)
    to lazy to code my own :)
 */

public class PlayerMenuUtility {

    private Player owner;
    //store the player that will be killed so we can access him in the next menu
    private Player playerToKill;

    public Menu menu;

    public Menu lastMenu;

    public PlayerMenuUtility(Player p) {
        this.owner = p;
    }

    public Player getOwner() {
        return owner;
    }

    public Player getPlayerToKill() {
        return playerToKill;
    }

    public void setPlayerToKill(Player playerToKill) {
        this.playerToKill = playerToKill;
    }
}
