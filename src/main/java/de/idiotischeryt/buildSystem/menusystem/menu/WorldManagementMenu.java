package de.idiotischeryt.buildSystem.menusystem.menu;

import de.idiotischeryt.buildSystem.BuildManager;
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
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class WorldManagementMenu extends PaginatedMenu {

    private @NonNull NamespacedKey key;

    public WorldManagementMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);

        key = new NamespacedKey(BuildSystem.getInstance(), "world");
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
    public void open() throws SignGUIVersionException {
        super.open();

    }

    @Override
    public void handleMenu(@NotNull InventoryClickEvent e) throws SignGUIVersionException {
        Player p = (Player) e.getWhoClicked();

        ArrayList<String> worlds = getWorlds();

        if (playerMenuUtility.getOwner() != p) return;

        if (!p.getOpenInventory().getTopInventory().equals(this.inventory)) return;

        if (e.getCurrentItem().getPersistentDataContainer().has(key)) {

            World world;

            String name = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName()).replace(" ", "-");
            name = name.replace("(", "");
            name = name.replace(")", "");

            if (Bukkit.getWorld(name) == null) {
                world = new WorldCreator(name).createWorld();
            } else {
                world = Bukkit.getWorld(name);
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
                if (page > 0) {
                    page = page - 1;
                    super.open();
                }
            } else if (ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName()).equalsIgnoreCase("Right") &&
                    e.getCurrentItem().getPersistentDataContainer().has(new NamespacedKey(BuildSystem.getInstance(), "menuPart"))) {
                if (!((index + 1) >= worlds.size())) {
                    page = page + 1;
                    super.open();
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
                    String[] strings = BuildManager.namesByString(worlds.get(index));

                    if (strings.length < 2) {
                        Bukkit.getLogger().warning("Invalid world entry: " + Arrays.toString(strings));
                        continue;
                    }

                    Material defaultMat = Material.GRASS_BLOCK;
                    Material worldMat = null;
                    Material templateMat = null;

                    ConfigurationSection section = BuildSystem.getConfiguration().getConfigurationSection(strings[1]);
                    if (section != null) {
                        String templateMatName = section.getString("template-material");
                        if (templateMatName != null) {
                            try {
                                templateMat = Material.valueOf(templateMatName.toUpperCase());
                            } catch (IllegalArgumentException e) {
                                Bukkit.getLogger().warning("Invalid template material: " + templateMatName);
                            }
                        }
                    } else {
                        Bukkit.getLogger().warning("Configuration section '" + strings[1] + "' is missing!");
                    }

                    FileConfiguration config = BuildSystem.getInstance().getConfigManager().getConfig(strings[0], strings[1]);
                    if (config != null) {
                        String worldMatName = config.getString("world-material");
                        if (worldMatName != null) {
                            try {
                                worldMat = Material.valueOf(worldMatName.toUpperCase());
                            } catch (IllegalArgumentException e) {
                                Bukkit.getLogger().warning("Invalid world material: " + worldMatName);
                            }
                        }
                    } else {
                        Bukkit.getLogger().warning("[BuildSystem] Config for '" + strings[0] + "' / '" + strings[1] + "' is missing! Using defaults...");
                    }

                    String lastElement = strings[strings.length - 1];
                    String[] newArray = new String[strings.length - 1];
                    System.arraycopy(strings, 0, newArray, 0, strings.length - 1);
                    strings = newArray;

                    String combinedString = ChatColor.RESET + String.join("-", strings) + ChatColor.GRAY + " (" + lastElement + ")";

                    Material useMat = (worldMat != null) ? worldMat : (templateMat != null) ? templateMat : defaultMat;
                    inventory.addItem(makeItem(useMat, combinedString, true, key));

                }
            }
        }
    }


    public static void loop() {
        try {
            BuildSystem.getConfiguration().load(BuildSystem.getInstance().registryPath.toFile());
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }

        worlds.clear();

        for (String section : BuildSystem.getConfiguration().getKeys(false)) {
            if (BuildSystem.getConfiguration().isConfigurationSection(section)) {
                add(Objects.requireNonNull(BuildSystem.getConfiguration().getConfigurationSection(section)));
            }
        }
    }


    private static void add(@NotNull ConfigurationSection config) {
        for (String section : config.getKeys(false)) {
            if (config.isConfigurationSection(section)) {
                if (section.endsWith("-" + config.getName()))
                    worlds.remove(section);
                else
                    worlds.remove(section + "-" + config.getName());


                /* updating the section for some edge cases */

                if (section.endsWith("-" + config.getName()))
                    worlds.add(section);
                else
                    worlds.add(section + "-" + config.getName());
            }
        }
    }
}
