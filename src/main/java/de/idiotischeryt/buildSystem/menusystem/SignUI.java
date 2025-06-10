package de.idiotischeryt.buildSystem.menusystem;

import de.idiotischeryt.buildSystem.BuildManager;
import de.idiotischeryt.buildSystem.BuildSystem;
import de.idiotischeryt.buildSystem.menusystem.menu.WorldManagementMenu;
import de.idiotischeryt.buildSystem.menusystem.menu.WorldSettingsMenu;
import de.rapha149.signgui.SignGUI;
import de.rapha149.signgui.SignGUIAction;
import de.rapha149.signgui.exception.SignGUIVersionException;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.bukkit.Bukkit.getServer;

public class SignUI extends PaginatedMenu {

    boolean wasInSign = false;

    private @NonNull NamespacedKey key;

    public SignUI(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        key = new NamespacedKey(BuildSystem.getInstance(), "world");
    }

    @Override
    public void open() throws SignGUIVersionException {
        inventory = Bukkit.createInventory(this, getSlots(), getMenuName());

        this.setMenuItems();

        playerMenuUtility.menu = this;

        if (!wasInSign) {
            SignGUI ui = SignGUI.builder()
                    .setType(Material.OAK_SIGN)
                    .setGlow(true)
                    .setLine(2, "--------------")
                    .setLine(1, "<< write here >>")
                    .setLine(0, "--------------")
                    .setHandler((player, signGUIResult) -> {
                        String input = signGUIResult.getLines()[1];
                        List<String> filteredWorlds = searchInput(input, WorldManagementMenu.getWorlds());

                        return List.of(SignGUIAction.runSync(BuildSystem.getInstance(), () -> {
                            if (!filteredWorlds.isEmpty()) {
                                for (int i = 0; i < getMaxItemsPerPage(); i++) {
                                    index = getMaxItemsPerPage() * page + i;
                                    if (index >= filteredWorlds.size()) break;
                                    if (filteredWorlds.get(index) != null) {
                                        String[] strings = BuildManager.namesByString(filteredWorlds.get(index));

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
                            } else {
                                inventory.addItem(makeItem(Material.BARRIER, ChatColor.RED + "No world(s) found!", false));
                            }

                            playerMenuUtility.getOwner().openInventory(inventory);
                        }));
                    })
                    .callHandlerSynchronously(BuildSystem.getInstance())
                    .build();

            wasInSign = true;

            ui.open(playerMenuUtility.getOwner());
        } else {
            playerMenuUtility.getOwner().openInventory(inventory);
        }

    }

    @Override
    public void addMenuBorder() {
        inventory.setItem(45, makeItem(Material.OAK_SIGN, ChatColor.GREEN + "Search"));

        inventory.setItem(48, makeItem(Material.DARK_OAK_BUTTON, ChatColor.GREEN + "Left"));

        inventory.setItem(49, makeItem(Material.BARRIER, ChatColor.DARK_RED + "Close"));

        inventory.setItem(50, makeItem(Material.DARK_OAK_BUTTON, ChatColor.GREEN + "Right"));

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

    public List<String> searchInput(String input, List<String> toSearch) {
        String i = ChatColor.stripColor(input.toLowerCase());

        List<String> result = new ArrayList<>();

        for (String worldName : toSearch) {
            if (worldName.toLowerCase().contains(i)) {
                result.add(worldName);
            }

            String[] strings = BuildManager.namesByString(worldName);

            ConfigurationSection section = BuildSystem.getConfiguration().getConfigurationSection(strings[1]);

            if (section != null) {
                if (section.contains(strings[0])) {
                    if (biomeMatch(section.getConfigurationSection(strings[0]), i)) {
                        result.add(worldName);
                    }
                } else if (section.contains(worldName)) {
                    if (biomeMatch(section.getConfigurationSection(worldName), i)) {
                        result.add(worldName);
                    }
                }
            }

        }

        return result;
    }

    private boolean biomeMatch(ConfigurationSection section, String input) {
        if (section != null && section.contains("biome")) {
            String biome = section.getString("biome", "").toLowerCase();
            return biome.contains(input);
        }
        return false;
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
    public void handleMenu(InventoryClickEvent e) throws SignGUIVersionException {
        Player p = (Player) e.getWhoClicked();

        ArrayList<Player> players = new ArrayList<>(getServer().getOnlinePlayers());

        if (playerMenuUtility.getOwner() != p) return;

        if (!p.getOpenInventory().getTopInventory().equals(this.inventory)) return;

        if (e.getCurrentItem().getType().equals(Material.BARRIER) &&
                e.getCurrentItem().getPersistentDataContainer().has(new NamespacedKey(BuildSystem.getInstance(), "menuPart"))) {
            p.closeInventory();

        } else if (e.getCurrentItem().getType().equals(Material.OAK_SIGN)) {
            SignUI ui = new SignUI(playerMenuUtility);
            ui.open();
        } else if (e.getCurrentItem().getType().equals(Material.DARK_OAK_BUTTON) &&
                e.getCurrentItem().getPersistentDataContainer().has(new NamespacedKey(BuildSystem.getInstance(), "menuPart"))) {
            if (ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName()).equalsIgnoreCase("Left")) {
                if (page == 0) {
                    p.sendMessage(ChatColor.GRAY + "You are already on the first page.");
                } else {
                    page = page - 1;
                    try {
                        open();
                    } catch (SignGUIVersionException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            } else if (ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName()).equalsIgnoreCase("Right") &&
                    e.getCurrentItem().getPersistentDataContainer().has(new NamespacedKey(BuildSystem.getInstance(), "menuPart"))) {
                if (!((index + 1) >= players.size())) {
                    page = page + 1;
                    try {
                        open();
                    } catch (SignGUIVersionException ex) {
                        throw new RuntimeException(ex);
                    }
                } else {
                    p.sendMessage(ChatColor.GRAY + "You are on the last page.");
                }
            }
        } else if (e.getCurrentItem().getPersistentDataContainer().has(key)) {
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
        }

    }

    @Override
    public void handleClose(InventoryCloseEvent e) {
        if (e.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;

        Bukkit.getScheduler().runTask(BuildSystem.getInstance(), () -> {
            try {
                new WorldManagementMenu(playerMenuUtility).open();
            } catch (SignGUIVersionException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    @Override
    public void setMenuItems() {
        addMenuBorder();
    }
}