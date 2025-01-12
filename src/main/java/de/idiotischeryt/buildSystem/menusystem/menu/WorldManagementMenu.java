package de.idiotischeryt.buildSystem.menusystem.menu;

import de.idiotischeryt.buildSystem.BuildSystem;
import de.idiotischeryt.buildSystem.menusystem.PaginatedMenu;
import de.idiotischeryt.buildSystem.menusystem.PlayerMenuUtility;
import de.idiotischeryt.buildSystem.menusystem.SignUI;
import de.rapha149.signgui.exception.SignGUIVersionException;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Objects;

import static org.bukkit.Bukkit.getServer;

public class WorldManagementMenu extends PaginatedMenu {

    public WorldManagementMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
    }

    @Override
    public void addMenuBorder() {
        inventory.setItem(45, makeItem(Material.OAK_SIGN, ChatColor.GREEN + "Search"));

        inventory.setItem(48, makeItem(Material.DARK_OAK_BUTTON, ChatColor.GREEN + "Left"));

        inventory.setItem(49, makeItem(Material.BARRIER, ChatColor.DARK_RED + "Close"));

        inventory.setItem(50, makeItem(Material.DARK_OAK_BUTTON, ChatColor.GREEN + "Right"));


        try {
            inventory.setItem(53, createPlayerHead(
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2VkZDIwYmU5MzUyMDk0OWU2Y2U3ODlkYzRmNDNlZmFlYjI4YzcxN2VlNmJmY2JiZTAyNzgwMTQyZjcxNiJ9fX0=",
                    ChatColor.GREEN + "Create World"
            ));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        for (int i = 0; i < 10; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, super.FILLER_GLASS);
            }
        }

        inventory.setItem(17, super.FILLER_GLASS);
        inventory.setItem(18, super.FILLER_GLASS);
        inventory.setItem(26, super.FILLER_GLASS);
        inventory.setItem(27, super.FILLER_GLASS);
        inventory.setItem(35, super.FILLER_GLASS);
        inventory.setItem(36, super.FILLER_GLASS);

        for (int i = 44; i < 54; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, super.FILLER_GLASS);
            }
        }
    }

    @Override
    public String getMenuName() {
        return "WorldManager";
    }

    @Override
    public int getSlots() {
        return 9 * 6;
    }

    @Override
    public void handleMenu(@NotNull InventoryClickEvent e) throws SignGUIVersionException {
        Player p = (Player) e.getWhoClicked();

        ArrayList<Player> players = new ArrayList<>(getServer().getOnlinePlayers());

        if (playerMenuUtility.getOwner() != p) return;

        if (!p.getOpenInventory().getTopInventory().equals(this.inventory)) return;

        if (e.getCurrentItem().getType().equals(Material.GRASS_BLOCK)) {

            World world;

            if (Bukkit.getWorld(e.getCurrentItem().getItemMeta().getDisplayName()) == null) {
                world = new WorldCreator(e.getCurrentItem().getItemMeta().getDisplayName()).createWorld();
            } else {
                world = Bukkit.getWorld(e.getCurrentItem().getItemMeta().getDisplayName());
            }

            WorldSettingsMenu menu = new WorldSettingsMenu(playerMenuUtility, world);

            menu.open();
        } else if (e.getCurrentItem().getType().equals(Material.OAK_SIGN)) {
            SignUI ui = new SignUI(playerMenuUtility);
            ui.open();
        } else if (e.getCurrentItem().getType() == Material.PLAYER_HEAD) {
            new BuildSettingsMenu(playerMenuUtility).open();
        } else if (e.getCurrentItem().getType().equals(Material.BARRIER) &&
                e.getCurrentItem().getPersistentDataContainer().has(new NamespacedKey(BuildSystem.getInstance(), "menuPart"))) {

            p.closeInventory();

        } else if (e.getCurrentItem().getType().equals(Material.DARK_OAK_BUTTON) &&
                e.getCurrentItem().getPersistentDataContainer().has(new NamespacedKey(BuildSystem.getInstance(), "menuPart"))) {
            if (ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName()).equalsIgnoreCase("Left")) {
                if (page == 0) {
                    p.sendMessage(ChatColor.GRAY + "You are already on the first page.");
                } else {
                    page = page - 1;
                    super.open();
                }
            } else if (ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName()).equalsIgnoreCase("Right") &&
                    e.getCurrentItem().getPersistentDataContainer().has(new NamespacedKey(BuildSystem.getInstance(), "menuPart"))) {
                if (!((index + 1) >= players.size())) {
                    page = page + 1;
                    super.open();
                } else {
                    p.sendMessage(ChatColor.GRAY + "You are on the last page.");
                }
            }
        }
    }


    public static ArrayList<String> worlds = new ArrayList<>();

    public static ArrayList<String> getWorlds() {
        loop();

        return worlds;
    }


    @Override
    public void setMenuItems() {
        addMenuBorder();

        if (!getWorlds().isEmpty()) {
            for (int i = 0; i < getMaxItemsPerPage(); i++) {
                index = getMaxItemsPerPage() * page + i;
                if (index >= worlds.size()) break;
                if (worlds.get(index) != null) {

                    inventory.addItem(makeItem(Material.GRASS_BLOCK, worlds.get(index), false));
                }
            }
        }
    }

    public static void loop() {
        FileConfiguration config = BuildSystem.getConfiguration();

        try {
            config.load(BuildSystem.getInstance().registryPath.toFile());
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }

        worlds.clear();

        for (String section : config.getKeys(false)) {
            if (config.isConfigurationSection(section)) {
                add(Objects.requireNonNull(config.getConfigurationSection(section)));
            }
        }
    }


    private static void add(ConfigurationSection config) {
        for (String section : config.getKeys(false)) {
            if (config.isConfigurationSection(section)) {
                worlds.remove(section);

                /* updating the section for some edge cases */

                worlds.add(section);
            }
        }
    }
}
