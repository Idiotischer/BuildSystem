package de.idiotischeryt.buildSystem.menusystem.menu;

import de.idiotischeryt.buildSystem.BuildManager;
import de.idiotischeryt.buildSystem.BuildSystem;
import de.idiotischeryt.buildSystem.menusystem.Menu;
import de.idiotischeryt.buildSystem.menusystem.PlayerMenuUtility;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.bukkit.Bukkit.getServer;

public class BuildSettingsMenu extends Menu {
    String[] mapName = new String[]{""};
    String[] minigameName = new String[]{""};
    String[] biomeName = new String[]{"PLAINS"};
    boolean emptys = false;
    boolean daylightCycles = false;
    boolean spawnMobs = false;
    boolean weatherCycles = false;

    final ItemStack spaceItem1 = makeItem(Material.BARRIER,
            ChatColor.GOLD.toString() + ChatColor.BOLD + "Empty",
            true, new NamespacedKey(BuildSystem.getInstance(), "Empty_Object"));
    final ItemStack spaceItem2 = makeItem(Material.NAME_TAG,
            ChatColor.GOLD.toString() + ChatColor.BOLD + "Worldname",
            true, new NamespacedKey(BuildSystem.getInstance(), "Mapname_Object"));
    final ItemStack spaceItem3 = makeItem(Material.NAME_TAG,
            ChatColor.GOLD.toString() + ChatColor.BOLD + "Worldtype",
            true, new NamespacedKey(BuildSystem.getInstance(), "Minigame_Object"));
    final ItemStack spaceItem4 = makeItem(Material.GRASS_BLOCK,
            ChatColor.GOLD.toString() + ChatColor.BOLD + "Biome",
            true, new NamespacedKey(BuildSystem.getInstance(), "Biome_Object"));
    final ItemStack spaceItem5 = makeItem(Material.STRIDER_SPAWN_EGG,
            ChatColor.GOLD.toString() + ChatColor.BOLD + "Spawn Mobs",
            true, new NamespacedKey(BuildSystem.getInstance(), "SpawnMobs_Object"));
    final ItemStack spaceItem6 = makeItem(Material.YELLOW_GLAZED_TERRACOTTA,
            ChatColor.GOLD.toString() + ChatColor.BOLD + "DaylightCycle",
            true, new NamespacedKey(BuildSystem.getInstance(), "DaylightCycle_Object"));

    Menu current;

    public BuildSettingsMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);

        current = this;
    }

    @Override
    public String getMenuName() {
        return "Settings";
    }

    @Override
    public int getSlots() {
        return 9 * 5;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {

        if (e.getClickedInventory() == inventory) {

            if (e.getCurrentItem().getType() == Material.NAME_TAG &&
                    e.getCurrentItem()
                            .getPersistentDataContainer()
                            .has(new NamespacedKey(BuildSystem.getInstance(), "Mapname_Object"))) {
                List<String> configSections = new ArrayList<>();
                FileConfiguration config = BuildSystem.getConfiguration();

                for (String topLevelKey : config.getKeys(false)) {
                    if (config.isConfigurationSection(topLevelKey)) {
                        System.out.println(topLevelKey);

                        ConfigurationSection topLevelSection = config.getConfigurationSection(topLevelKey);
                        for (String secondLevelKey : topLevelSection.getKeys(false)) {
                            if (topLevelSection.isConfigurationSection(secondLevelKey)) {
                                System.out.println(secondLevelKey);

                                String[] strings = BuildManager.namesByString(secondLevelKey);

                                String mapName = strings[0];

                                configSections.add(mapName);
                            }
                        }
                    }
                }

                configSections.add(getServer().getWorlds().getFirst().getName());

                handleRename(playerMenuUtility.getOwner(), this, "Rename me", "Map name here", mapName, configSections, ChatColor.RED + "That map already exists!", true);
            } else if (e.getCurrentItem().getType() == Material.NAME_TAG && e.getCurrentItem()
                    .getPersistentDataContainer()
                    .has(new NamespacedKey(BuildSystem.getInstance(), "Minigame_Object"))
            ) {
                List<String> configSections = new ArrayList<>();
                FileConfiguration config = BuildSystem.getInstance().getConfig();

                for (String section : config.getKeys(false)) {
                    if (config.isConfigurationSection(section)) {
                        configSections.add(section);
                    }
                }

                handleRename(playerMenuUtility.getOwner(), this, "Rename me", "Template name here", minigameName, configSections, ChatColor.RED + "That type doesn't exist!", false);
            } else if (e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR && e.getCurrentItem()
                    .getPersistentDataContainer()
                    .has(new NamespacedKey(BuildSystem.getInstance(), "Biome_Object"))
            ) {
                AnvilGUI.Builder builder = new AnvilGUI.Builder();
                builder.plugin(BuildSystem.getInstance())
                        .interactableSlots(1)
                        .text("Biome name here")
                        .itemLeft(makeItem(Material.NAME_TAG, "Rename me", false))
                        .onClose((state) -> {
                            Bukkit.getScheduler().runTask(BuildSystem.getInstance(), current::open);
                        })
                        .onClick((slot, state) -> {

                            String inputText = state.getText();

                            boolean isValidBiome = false;
                            for (Biome biome : Biome.values()) {
                                if (biome.name().equalsIgnoreCase(inputText)) {
                                    isValidBiome = true;
                                    break;
                                }
                            }

                            if (isValidBiome) {

                                Bukkit.getScheduler().runTaskLater(BuildSystem.getInstance(), () -> {
                                    state.getPlayer().getItemOnCursor().setType(Material.AIR);
                                }, 2);

                                biomeName[0] = inputText;

                                playerMenuUtility.getOwner().closeInventory();
                                return Collections.singletonList(AnvilGUI.ResponseAction.close());
                            } else {
                                return Arrays.asList(
                                        AnvilGUI.ResponseAction.replaceInputText("Biome Name here"),
                                        AnvilGUI.ResponseAction.updateTitle(ChatColor.RED + "Biome not found!", true)
                                );
                            }
                        });

                builder.open(playerMenuUtility.getOwner());


            } else if (e.getCurrentItem().getType() == Material.BARRIER &&
                    e.getCurrentItem()
                            .getPersistentDataContainer()
                            .has(new NamespacedKey(BuildSystem.getInstance(), "Empty_Object"))
            ) {
                Menu menu = new Menu(
                        BuildSystem.getPlayerMenuUtility(((Player) e.getWhoClicked()))
                ) {
                    @Override
                    public String getMenuName() {
                        return "Choose if the World is Empty";
                    }

                    @Override
                    public int getSlots() {
                        return 5 * 9;
                    }

                    @Override
                    public void handleMenu(InventoryClickEvent e) {
                        if (e.getClickedInventory() != inventory) return;

                        e.setCancelled(true);

                        if (e.getCurrentItem() == null) return;

                        if (e.getCurrentItem()
                                .getItemMeta()
                                .getPersistentDataContainer()
                                .has(new NamespacedKey(BuildSystem.getInstance(), "Boolean_Object"))) {
                            NamespacedKey key = new NamespacedKey(BuildSystem.getInstance(), "Boolean_Object");

                            ItemMeta meta = e.getCurrentItem().getItemMeta();
                            PersistentDataContainer container = meta.getPersistentDataContainer();

                            boolean on = Boolean.TRUE.equals(container.get(key, PersistentDataType.BOOLEAN));
                            System.out.println("Boolean_Object before toggle: " + on);

                            ItemStack stack;
                            if (on) {
                                stack = new ItemStack(Material.RED_STAINED_GLASS_PANE);

                                meta.setDisplayName(ChatColor.GOLD.toString() + ChatColor.BOLD + "False");

                                meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, false);

                                emptys = false;

                                System.out.println("red");
                            } else {
                                stack = new ItemStack(Material.LIME_STAINED_GLASS_PANE);

                                meta.setDisplayName(ChatColor.GOLD.toString() + ChatColor.BOLD + "True");

                                meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);

                                emptys = true;

                                System.out.println("lime");
                            }

                            stack.setItemMeta(meta);
                            inventory.setItem(e.getSlot(), stack);
                        }
                    }

                    @Override
                    public void handleClose(InventoryCloseEvent e) {
                        if (e.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;

                        Bukkit.getScheduler().runTask(BuildSystem.getInstance(), current::open);
                    }

                    @Override
                    public void setMenuItems() {

                        this.FILLER_GLASS = makeItem(Material.GRAY_STAINED_GLASS_PANE, " ", false);

                        placeInMiddle(makeItem(Material.RED_STAINED_GLASS_PANE,
                                ChatColor.GOLD.toString() + ChatColor.BOLD + "False",
                                true,
                                new NamespacedKey(BuildSystem.getInstance(), "Boolean_Object"),
                                false
                        ));

                        setFillerGlass();
                    }
                };

                menu.open();
            } else if (e.getCurrentItem()
                    .getPersistentDataContainer()
                    .has(new NamespacedKey(BuildSystem.getInstance(), "DaylightCycle_Object"))
            ) {
                Menu menu = new Menu(
                        BuildSystem.getPlayerMenuUtility(((Player) e.getWhoClicked()))
                ) {
                    @Override
                    public String getMenuName() {
                        return "Do daylight and/or weather change?";
                    }

                    @Override
                    public int getSlots() {
                        return 5 * 9;
                    }

                    @Override
                    public void handleMenu(InventoryClickEvent e) {
                        if (e.getClickedInventory() != inventory) return;

                        e.setCancelled(true);

                        if (e.getCurrentItem() == null) return;

                        if (e.getCurrentItem()
                                .getItemMeta()
                                .getPersistentDataContainer()
                                .has(new NamespacedKey(BuildSystem.getInstance(), "Boolean1_Object"))) {
                            NamespacedKey key = new NamespacedKey(BuildSystem.getInstance(), "Boolean1_Object");

                            ItemMeta meta = e.getCurrentItem().getItemMeta();
                            PersistentDataContainer container = meta.getPersistentDataContainer();

                            boolean on = Boolean.TRUE.equals(container.get(key, PersistentDataType.BOOLEAN));

                            ItemStack stack;
                            if (on) {
                                stack = new ItemStack(Material.RED_STAINED_GLASS_PANE);

                                meta.setDisplayName(ChatColor.GOLD.toString() + ChatColor.BOLD + "DaylightCycle: False");

                                meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, false);

                                daylightCycles = false;

                            } else {
                                stack = new ItemStack(Material.LIME_STAINED_GLASS_PANE);

                                meta.setDisplayName(ChatColor.GOLD.toString() + ChatColor.BOLD + "DaylightCycle: True");

                                meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);

                                daylightCycles = true;
                            }

                            stack.setItemMeta(meta);
                            inventory.setItem(e.getSlot(), stack);
                        } else if (e.getCurrentItem()
                                .getItemMeta()
                                .getPersistentDataContainer()
                                .has(new NamespacedKey(BuildSystem.getInstance(), "Boolean_Object"))) {
                            NamespacedKey key = new NamespacedKey(BuildSystem.getInstance(), "Boolean_Object");

                            ItemMeta meta = e.getCurrentItem().getItemMeta();
                            PersistentDataContainer container = meta.getPersistentDataContainer();

                            boolean on = Boolean.TRUE.equals(container.get(key, PersistentDataType.BOOLEAN));

                            ItemStack stack;
                            if (on) {
                                stack = new ItemStack(Material.RED_STAINED_GLASS_PANE);

                                meta.setDisplayName(ChatColor.GOLD.toString() + ChatColor.BOLD + "WeatherCycle: False");

                                meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, false);

                                weatherCycles = false;

                            } else {
                                stack = new ItemStack(Material.LIME_STAINED_GLASS_PANE);

                                meta.setDisplayName(ChatColor.GOLD.toString() + ChatColor.BOLD + "WeatherCycle: True");

                                meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);

                                weatherCycles = true;
                            }

                            stack.setItemMeta(meta);
                            inventory.setItem(e.getSlot(), stack);
                        }
                    }

                    @Override
                    public void handleClose(InventoryCloseEvent e) {
                        if (e.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;

                        Bukkit.getScheduler().runTask(BuildSystem.getInstance(), current::open);
                    }

                    @Override
                    public void setMenuItems() {

                        this.FILLER_GLASS = makeItem(Material.GRAY_STAINED_GLASS_PANE, " ", false);

                        inventory.setItem(21, makeItem(Material.RED_STAINED_GLASS_PANE,
                                ChatColor.GOLD.toString() + ChatColor.BOLD + "DaylightCycle: False",
                                true,
                                new NamespacedKey(BuildSystem.getInstance(), "Boolean1_Object"),
                                false
                        ));

                        inventory.setItem(23, makeItem(Material.RED_STAINED_GLASS_PANE,
                                ChatColor.GOLD.toString() + ChatColor.BOLD + "WeatherCycle: False",
                                true,
                                new NamespacedKey(BuildSystem.getInstance(), "Boolean_Object"),
                                false
                        ));

                        setFillerGlass();
                    }
                };

                menu.open();
            } else if (e.getCurrentItem()
                    .getPersistentDataContainer()
                    .has(new NamespacedKey(BuildSystem.getInstance(), "SpawnMobs_Object"))
            ) {
                Menu menu = new Menu(
                        BuildSystem.getPlayerMenuUtility(((Player) e.getWhoClicked()))
                ) {
                    @Override
                    public String getMenuName() {
                        return "Choose if mobs should spawn";
                    }

                    @Override
                    public int getSlots() {
                        return 5 * 9;
                    }

                    @Override
                    public void handleMenu(InventoryClickEvent e) {
                        if (e.getClickedInventory() != inventory) return;

                        e.setCancelled(true);

                        if (e.getCurrentItem() == null) return;

                        if (e.getCurrentItem()
                                .getItemMeta()
                                .getPersistentDataContainer()
                                .has(new NamespacedKey(BuildSystem.getInstance(), "Boolean_Object"))) {
                            NamespacedKey key = new NamespacedKey(BuildSystem.getInstance(), "Boolean_Object");

                            ItemMeta meta = e.getCurrentItem().getItemMeta();
                            PersistentDataContainer container = meta.getPersistentDataContainer();

                            boolean on = Boolean.TRUE.equals(container.get(key, PersistentDataType.BOOLEAN));

                            ItemStack stack;
                            if (on) {
                                stack = new ItemStack(Material.RED_STAINED_GLASS_PANE);

                                meta.setDisplayName(ChatColor.GOLD.toString() + ChatColor.BOLD + "False");

                                meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, false);

                                spawnMobs = false;
                            } else {
                                stack = new ItemStack(Material.LIME_STAINED_GLASS_PANE);

                                meta.setDisplayName(ChatColor.GOLD.toString() + ChatColor.BOLD + "True");

                                meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);

                                spawnMobs = true;
                            }

                            stack.setItemMeta(meta);
                            inventory.setItem(e.getSlot(), stack);
                        }
                    }

                    @Override
                    public void handleClose(InventoryCloseEvent e) {
                        if (e.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;

                        Bukkit.getScheduler().runTask(BuildSystem.getInstance(), current::open);
                    }

                    @Override
                    public void setMenuItems() {

                        this.FILLER_GLASS = makeItem(Material.GRAY_STAINED_GLASS_PANE, " ", false);


                        placeInMiddle(makeItem(Material.RED_STAINED_GLASS_PANE,
                                ChatColor.GOLD.toString() + ChatColor.BOLD + "False",
                                true,
                                new NamespacedKey(BuildSystem.getInstance(), "Boolean_Object"),
                                false
                        ));

                        setFillerGlass();
                    }
                };

                menu.open();
            } else if (e.getCurrentItem().getType().equals(Material.PLAYER_HEAD)) {

                if (minigameName[0].trim().isEmpty()) return;
                if (mapName[0].trim().isEmpty()) return;

                try {
                    BuildManager.createWorld(playerMenuUtility.getOwner(),
                            mapName[0],
                            minigameName[0],
                            emptys,
                            Biome.valueOf(biomeName[0].toUpperCase()),
                            spawnMobs,
                            daylightCycles,
                            weatherCycles
                    );

                    System.out.println("created world");

                } catch (IOException | InvalidConfigurationException ex) {
                    throw new RuntimeException(ex);
                }

            }
        }
    }

    @Override
    public void handleClose(@NotNull InventoryCloseEvent e) {
        if (e.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;
        if (e.getReason() == InventoryCloseEvent.Reason.UNKNOWN) return;
        if (e.getReason() == InventoryCloseEvent.Reason.TELEPORT) return;

        Bukkit.getScheduler().runTask(BuildSystem.getInstance(), () -> new WorldManagementMenu(playerMenuUtility).open());

        clear();
    }

    @Override
    public void setMenuItems() {
        ItemStack playerHead;
        try {
            playerHead = createPlayerHead(
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2VkZDIwYmU5MzUyMDk0OWU2Y2U3ODlkYzRmNDNlZmFlYjI4YzcxN2VlNmJmY2JiZTAyNzgwMTQyZjcxNiJ9fX0=",
                    ChatColor.GREEN + "Create"
            );
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }

        Inventory inventory = this.inventory;

        ItemStack[] row = {spaceItem1, spaceItem2, spaceItem3};
        ItemStack[] row2 = {spaceItem4, spaceItem5, spaceItem6};

        for (int col = 0; col < 9; col++) {
            inventory.setItem(col, this.FILLER_GLASS);
            inventory.setItem(2 * 9 + col, this.FILLER_GLASS);
            inventory.setItem(4 * 9 + col, this.FILLER_GLASS);
        }

        for (int col = 0; col < 9; col++) {
            int slr2 = 9 + col;
            ItemStack itemToPlace = this.FILLER_GLASS;

            if (col >= 2 && col <= 6 && col % 2 == 0) {
                itemToPlace = row[(col / 2) % row.length];
            }

            inventory.setItem(slr2, itemToPlace);
        }

        for (int col = 0; col < 9; col++) {
            int slr4 = 3 * 9 + col;
            ItemStack itemToPlace = this.FILLER_GLASS;

            if (col >= 2 && col <= 6 && col % 2 == 0) {
                itemToPlace = row2[(col / 2) % row2.length];
            }

            if (itemToPlace == spaceItem5) {
                animate(slr4,
                        new Material[]{
                                Material.STRIDER_SPAWN_EGG,
                                Material.ALLAY_SPAWN_EGG,
                                Material.BEE_SPAWN_EGG,
                                Material.ARMADILLO_SPAWN_EGG,
                                Material.BREEZE_SPAWN_EGG,
                                Material.AXOLOTL_SPAWN_EGG,
                                Material.WARDEN_SPAWN_EGG,
                                Material.VILLAGER_SPAWN_EGG,
                                Material.TURTLE_SPAWN_EGG,
                                Material.TADPOLE_SPAWN_EGG
                        }, itemToPlace, 14);
            }

            if (itemToPlace == spaceItem6) {
                animateXChangeName(slr4,
                        new Material[]{
                                Material.YELLOW_GLAZED_TERRACOTTA,
                                Material.BLUE_GLAZED_TERRACOTTA
                        }, itemToPlace, 14, new String[]{
                                ChatColor.GOLD.toString() + ChatColor.BOLD + "DaylightCycle",
                                ChatColor.GOLD.toString() + ChatColor.BOLD + "WeatherCycle"
                        }
                );
            }

            if (itemToPlace == spaceItem4) {
                animate(slr4,
                        new Material[]{
                                Material.GRASS_BLOCK,
                                Material.SAND,
                                Material.RED_SAND,
                                Material.ICE,
                                Material.MYCELIUM,
                                Material.PODZOL,
                                Material.NETHERRACK,
                                Material.BASALT,
                                Material.WARPED_NYLIUM,
                                Material.CRIMSON_NYLIUM
                        }, itemToPlace, 14);
            }

            inventory.setItem(slr4, itemToPlace);
        }

        inventory.setItem(inventory.getSize() - 1, playerHead);
    }

    public void handleRename(Player p, Menu currentMenu, String label, String defaultText,
                             String[] map, List<String> needed, String errText, boolean contains) {
        AnvilGUI.Builder builder = new AnvilGUI.Builder();

        builder.plugin(BuildSystem.getInstance())
                .interactableSlots(1)
                .text(defaultText)
                .itemLeft(makeItem(Material.NAME_TAG, label, false))
                .onClose(state -> {
                    Bukkit.getScheduler().runTask(BuildSystem.getInstance(), currentMenu::open);
                    p.updateInventory();
                })
                .onClick((slot, state) -> {
                    if (contains) {
                        if (needed.contains(state.getText())) {
                            return Arrays.asList(
                                    AnvilGUI.ResponseAction.replaceInputText(defaultText),
                                    AnvilGUI.ResponseAction.updateTitle(errText, true)
                            );
                        }
                    } else {
                        if (!needed.contains(state.getText())) {
                            return Arrays.asList(
                                    AnvilGUI.ResponseAction.replaceInputText(defaultText),
                                    AnvilGUI.ResponseAction.updateTitle(errText, true)
                            );
                        }
                    }

                    map[0] = state.getText();

                    Bukkit.getScheduler().runTaskLater(BuildSystem.getInstance(), () -> {
                        state.getPlayer().getItemOnCursor().setType(Material.AIR);
                    }, 2);

                    if (!map[0].trim().isEmpty()) {
                        p.closeInventory();
                        return Collections.singletonList(AnvilGUI.ResponseAction.close());
                    }

                    return Collections.singletonList(AnvilGUI.ResponseAction.replaceInputText(defaultText));
                });

        builder.open(p);
    }

    public void clear() {
        mapName = new String[]{""};
        minigameName = new String[]{""};
        biomeName = new String[]{"PLAINS"};
        emptys = false;
        daylightCycles = false;
        spawnMobs = false;
        weatherCycles = false;
    }
}
