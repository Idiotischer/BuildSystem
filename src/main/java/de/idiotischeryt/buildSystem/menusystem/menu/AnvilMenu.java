package de.idiotischeryt.buildSystem.menusystem.menu;

import de.idiotischeryt.buildSystem.BuildSystem;
import de.idiotischeryt.buildSystem.menusystem.Menu;
import de.idiotischeryt.buildSystem.menusystem.PlayerMenuUtility;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.view.AnvilView;

import java.util.List;

public class AnvilMenu extends Menu {
    final Menu currentMenu;
    final String label;
    String defaultText;
    final String[] map;
    final List<String> needed;
    final String errText;
    final boolean contains;
    final boolean emptyAllowed;
    final boolean allowEndWith;
    final List<String> endWith;
    InventoryView inventoryView;

    public AnvilMenu(PlayerMenuUtility playerMenuUtility, Menu currentMenu, String label, String defaultText,
                     String[] map, List<String> needed, String errText, boolean contains, boolean emptyAllowed, boolean allowEndWith, List<String> endWith) {
        super(playerMenuUtility);
        this.inventory = null;
        this.allowEndWith = allowEndWith;
        this.emptyAllowed = emptyAllowed;
        this.contains = contains;
        this.endWith = endWith;
        this.errText = errText;
        this.needed = needed;
        this.map = map;
        this.defaultText = defaultText;
        this.label = label;
        this.currentMenu = currentMenu;
    }

    public void setDefaultText(String defaultText) {
        this.defaultText = defaultText;
    }

    @Override
    public String getMenuName() {
        return defaultText;
    }

    @Override
    public void open() {
        openWithTitle(defaultText);
    }

    public void openWithTitle(String title) {
        Player player = playerMenuUtility.getOwner();

        inventoryView = player.openAnvil(player.getLocation(), true);
        inventoryView.setTitle(title);
        this.inventory = inventoryView.getTopInventory();

        BuildSystem.getInstance().registerRename(player,
                new RenameData(player, currentMenu, label, title, map, needed, errText, contains, emptyAllowed, allowEndWith, endWith, this));

        this.setMenuItems();
        playerMenuUtility.menu = this;
    }

    @Override
    public void handleClose(InventoryCloseEvent e) {
        Bukkit.getScheduler().runTask(BuildSystem.getInstance(), currentMenu::open);
        super.handleClose(e);
    }

    @Override
    public int getSlots() {
        return 3;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {}

    @Override
    public void setMenuItems() {
        if (inventoryView == null) return;

        ItemStack nameTag = new ItemStack(Material.NAME_TAG);
        ItemMeta meta = nameTag.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + label);
        nameTag.setItemMeta(meta);

        inventoryView.setItem(0, nameTag);
    }
}
