package de.idiotischeryt.buildSystem.menusystem.menu;

import de.idiotischeryt.buildSystem.BuildManager;
import de.idiotischeryt.buildSystem.BuildSystem;
import de.idiotischeryt.buildSystem.menusystem.Menu;
import de.idiotischeryt.buildSystem.menusystem.PaginatedMenu;
import de.idiotischeryt.buildSystem.menusystem.PlayerMenuUtility;
import de.idiotischeryt.buildSystem.menusystem.SignUI;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.*;
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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;

import static org.bukkit.Bukkit.getServer;

public class WorldManagementMenu extends PaginatedMenu {

    static Map<Player, String> mapName = new HashMap<>();
    static Map<Player, String> minigameName = new HashMap<>();
    static Map<Player, String> biomeName = new HashMap<>();
    static Map<Player, Boolean> emptys = new HashMap<>();
    static Map<Player, Boolean> daylightCycles = new HashMap<>();
    static Map<Player, Boolean> spawnMobs = new HashMap<>();
    static Map<Player, Boolean> weatherCycles = new HashMap<>();


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
    public void handleMenu(@NotNull InventoryClickEvent e) {
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

            Menu menu = new Menu(playerMenuUtility) {
                @Contract(pure = true)
                @Override
                public @NotNull String getMenuName() {
                    return "Settings";
                }

                @Override
                public int getSlots() {
                    return 9 * 3;
                }

                @Override
                public void handleMenu(@NotNull InventoryClickEvent e) {
                    if (e.getCurrentItem() == null) return;

                    if (e.getCurrentItem().getType() == Material.ENDER_PEARL) {
                        playerMenuUtility.getOwner().teleport(world.getSpawnLocation());
                    } else if (e.getCurrentItem().getType() == Material.RED_STAINED_GLASS_PANE) {

                        assert world != null;
                        BuildManager.delete(world);
                    }
                }

                @Override
                public void setMenuItems() {
                    String[] strings = BuildManager.namesByWorld(world);

                    String mapName = strings[0];
                    String minigameName = strings[1];

                    if (BuildSystem.getConfiguration().isConfigurationSection(minigameName + "." + world.getName())) {
                        ConfigurationSection section = BuildSystem.getConfiguration().getConfigurationSection(minigameName + "." + world.getName());
                        if (section != null) {
                            Map<String, Object> stats = section.getValues(false);

                            List<String> statsList = new ArrayList<>();

                            statsList.add(ChatColor.GOLD + "mapName: " + ChatColor.WHITE + mapName);

                            statsList.add(ChatColor.GOLD + "templateName: " + ChatColor.WHITE + minigameName);

                            stats.forEach((key, value) -> statsList.add(ChatColor.GOLD + key + ": " + ChatColor.WHITE + value));

                            inventory.setItem(13, makeItem(Material.PAPER, ChatColor.AQUA + "Stats", statsList.toArray(new String[0])));
                        } else {
                            inventory.setItem(13, makeItem(Material.PAPER, ChatColor.AQUA + "Stats", "nothing to show,", "please reopen", "the menu!"));
                        }
                    } else {
                        inventory.setItem(13, makeItem(Material.PAPER, ChatColor.AQUA + "Stats", "nothing to show,", "please reopen", "the menu!"));
                    }


                    inventory.setItem(11, makeItem(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "Delete"));

                    inventory.setItem(15, makeItem(Material.ENDER_PEARL, ChatColor.GREEN + "Teleport"));

                    setFillerGlass();
                }
            };

            menu.open();
        } else if (e.getCurrentItem().getType().equals(Material.OAK_SIGN)) {
            SignUI ui = new SignUI(playerMenuUtility);
            ui.open();
        } else if (e.getCurrentItem().getType().equals(Material.PLAYER_HEAD)) {
            Menu menu = new Menu(BuildSystem.getPlayerMenuUtility(p)) {
                final ItemStack spaceItem1 = makeItem(Material.BARRIER,
                        ChatColor.GOLD.toString() + ChatColor.BOLD + "Empty",
                        true, new NamespacedKey(BuildSystem.getInstance(), "Empty_Object"));
                final ItemStack spaceItem2 = makeItem(Material.NAME_TAG,
                        ChatColor.GOLD.toString() + ChatColor.BOLD + "Worldname",
                        true, new NamespacedKey(BuildSystem.getInstance(), "Mapname_Object")); // opens rename ui
                final ItemStack spaceItem3 = makeItem(Material.NAME_TAG,
                        ChatColor.GOLD.toString() + ChatColor.BOLD + "Worldtype",
                        true, new NamespacedKey(BuildSystem.getInstance(), "Minigame_Object")); // opens rename ui

                // öffnet auch n rename ui und ist animated, bzw. wird noch animated
                final ItemStack spaceItem4 = makeItem(Material.GRASS_BLOCK,
                        ChatColor.GOLD.toString() + ChatColor.BOLD + "Biome",
                        true, new NamespacedKey(BuildSystem.getInstance(), "Biome_Object"));

                final ItemStack spaceItem5 = makeItem(Material.STRIDER_SPAWN_EGG,
                        ChatColor.GOLD.toString() + ChatColor.BOLD + "Spawn Mobs",
                        true, new NamespacedKey(BuildSystem.getInstance(), "SpawnMobs_Object")); // boolean without extra ui
                final ItemStack spaceItem6 = makeItem(Material.YELLOW_GLAZED_TERRACOTTA,
                        ChatColor.GOLD.toString() + ChatColor.BOLD + "DaylightCycle",
                        true, new NamespacedKey(BuildSystem.getInstance(), "DaylightCycle_Object")); // boolean without extra ui

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
                        Menu currentMenu = this;

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

                                            assert strings != null;

                                            String mapName = strings[0];

                                            configSections.add(mapName);
                                        }
                                    }
                                }
                            }

                            System.out.println(configSections);

                            handleRename(playerMenuUtility.getOwner(), currentMenu, "Rename me", "Map name here", mapName, configSections, ChatColor.RED + "That map already exists!", true);
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

                            handleRename(playerMenuUtility.getOwner(), currentMenu, "Rename me", "Template name here", minigameName, configSections, ChatColor.RED + "That type doesn't exist!", false);
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
                                        Bukkit.getScheduler().runTask(BuildSystem.getInstance(), currentMenu::open);
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

                                            if (biomeName.get(playerMenuUtility.getOwner()) != null) {
                                                biomeName.replace(playerMenuUtility.getOwner(), state.getText());
                                            } else {
                                                biomeName.put(playerMenuUtility.getOwner(), state.getText());
                                            }

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

                                            if (emptys.get(playerMenuUtility.getOwner()) == null) {
                                                emptys.put(playerMenuUtility.getOwner(), false);
                                            } else {
                                                emptys.replace(playerMenuUtility.getOwner(), false);
                                            }

                                            System.out.println("red");
                                        } else {
                                            stack = new ItemStack(Material.LIME_STAINED_GLASS_PANE);

                                            meta.setDisplayName(ChatColor.GOLD.toString() + ChatColor.BOLD + "True");

                                            meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);

                                            if (emptys.get(playerMenuUtility.getOwner()) == null) {
                                                emptys.put(playerMenuUtility.getOwner(), true);
                                            } else {
                                                emptys.replace(playerMenuUtility.getOwner(), true);
                                            }

                                            System.out.println("lime");
                                        }

                                        stack.setItemMeta(meta);
                                        inventory.setItem(e.getSlot(), stack);
                                    }
                                }

                                @Override
                                public void handleClose(InventoryCloseEvent e) {
                                    if (e.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;

                                    Bukkit.getScheduler().runTask(BuildSystem.getInstance(), currentMenu::open);
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

                                            if (daylightCycles.get(playerMenuUtility.getOwner()) == null) {
                                                daylightCycles.put(playerMenuUtility.getOwner(), false);
                                            } else {
                                                daylightCycles.replace(playerMenuUtility.getOwner(), false);
                                            }

                                        } else {
                                            stack = new ItemStack(Material.LIME_STAINED_GLASS_PANE);

                                            meta.setDisplayName(ChatColor.GOLD.toString() + ChatColor.BOLD + "DaylightCycle: True");

                                            meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);

                                            if (daylightCycles.get(playerMenuUtility.getOwner()) == null) {
                                                daylightCycles.put(playerMenuUtility.getOwner(), true);
                                            } else {
                                                daylightCycles.replace(playerMenuUtility.getOwner(), true);
                                            }
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

                                            if (weatherCycles.get(playerMenuUtility.getOwner()) == null) {
                                                weatherCycles.put(playerMenuUtility.getOwner(), false);
                                            } else {
                                                weatherCycles.replace(playerMenuUtility.getOwner(), false);
                                            }

                                        } else {
                                            stack = new ItemStack(Material.LIME_STAINED_GLASS_PANE);

                                            meta.setDisplayName(ChatColor.GOLD.toString() + ChatColor.BOLD + "WeatherCycle: True");

                                            meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);

                                            if (weatherCycles.get(playerMenuUtility.getOwner()) == null) {
                                                weatherCycles.put(playerMenuUtility.getOwner(), true);
                                            } else {
                                                weatherCycles.replace(playerMenuUtility.getOwner(), true);
                                            }
                                        }

                                        stack.setItemMeta(meta);
                                        inventory.setItem(e.getSlot(), stack);
                                    }
                                }

                                @Override
                                public void handleClose(InventoryCloseEvent e) {
                                    if (e.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;

                                    Bukkit.getScheduler().runTask(BuildSystem.getInstance(), currentMenu::open);
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

                                            if (spawnMobs.get(playerMenuUtility.getOwner()) == null) {
                                                spawnMobs.put(playerMenuUtility.getOwner(), false);
                                            } else {
                                                spawnMobs.replace(playerMenuUtility.getOwner(), false);
                                            }

                                        } else {
                                            stack = new ItemStack(Material.LIME_STAINED_GLASS_PANE);

                                            meta.setDisplayName(ChatColor.GOLD.toString() + ChatColor.BOLD + "True");

                                            meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);

                                            if (spawnMobs.get(playerMenuUtility.getOwner()) == null) {
                                                spawnMobs.put(playerMenuUtility.getOwner(), true);
                                            } else {
                                                spawnMobs.replace(playerMenuUtility.getOwner(), true);
                                            }
                                        }

                                        stack.setItemMeta(meta);
                                        inventory.setItem(e.getSlot(), stack);
                                    }
                                }

                                @Override
                                public void handleClose(InventoryCloseEvent e) {
                                    if (e.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;

                                    Bukkit.getScheduler().runTask(BuildSystem.getInstance(), currentMenu::open);
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

                            if (mapName.get(playerMenuUtility.getOwner()) == null) return;
                            if (minigameName.get(playerMenuUtility.getOwner()) == null) return;

                            try {
                                BuildManager.createWorld(playerMenuUtility.getOwner(),
                                        mapName.get(playerMenuUtility.getOwner()),
                                        minigameName.get(playerMenuUtility.getOwner()),
                                        emptys.getOrDefault(playerMenuUtility.getOwner(), false),
                                        Biome.valueOf(biomeName.getOrDefault(playerMenuUtility.getOwner(), "PLAINS").toUpperCase()),
                                        spawnMobs.getOrDefault(playerMenuUtility.getOwner(), false),
                                        daylightCycles.getOrDefault(playerMenuUtility.getOwner(), false),
                                        weatherCycles.getOrDefault(playerMenuUtility.getOwner(), false)
                                );
                            } catch (IOException | InvalidConfigurationException ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                    }
                }

                @Override
                public void handleClose(InventoryCloseEvent e) {
                    if (e.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;
                    if (e.getReason() == InventoryCloseEvent.Reason.UNKNOWN) return;
                    if (e.getReason() == InventoryCloseEvent.Reason.TELEPORT) return;

                    Bukkit.getScheduler().runTask(BuildSystem.getInstance(), () -> new WorldManagementMenu(playerMenuUtility).open());

                    clearFor((Player) e.getPlayer());
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
            };

            menu.open();
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

    private void handleRename(Player p, Menu currentMenu, String label, String defaultText, Map<Player, String> map, List<String> needed, String errText, boolean contains) {
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
                        if (needed.contains(state.getText()))
                            return Arrays.asList(
                                    AnvilGUI.ResponseAction.replaceInputText(defaultText),
                                    AnvilGUI.ResponseAction.updateTitle(errText, true)
                            );
                    } else {
                        if (!needed.contains(state.getText()))
                            return Arrays.asList(
                                    AnvilGUI.ResponseAction.replaceInputText(defaultText),
                                    AnvilGUI.ResponseAction.updateTitle(errText, true)
                            );
                    }

                    Bukkit.getScheduler().runTaskLater(BuildSystem.getInstance(), () -> {
                        state.getPlayer().getItemOnCursor().setType(Material.AIR);
                    }, 2);

                    if (map.get(p) != null) {
                        map.replace(p, state.getText());
                    } else {
                        map.put(p, state.getText());
                    }

                    if (map.get(p) != null) {
                        p.closeInventory();

                        return Collections.singletonList(AnvilGUI.ResponseAction.close());
                    }

                    return Collections.singletonList(AnvilGUI.ResponseAction.replaceInputText(defaultText));
                });

        builder.open(playerMenuUtility.getOwner());
    }

    public static ArrayList<String> worlds = new ArrayList<>();

    @Override
    public void setMenuItems() {

        addMenuBorder();

        loop();

        //The thing you will be looping through to place items
        // Pagination loop template
        if (!worlds.isEmpty()) {
            for (int i = 0; i < getMaxItemsPerPage(); i++) {
                index = getMaxItemsPerPage() * page + i;
                if (index >= worlds.size()) break;
                if (worlds.get(index) != null) {

                    inventory.addItem(makeItem(Material.GRASS_BLOCK, worlds.get(index), false));
                }
            }
        }
    }

    public void loop() {
        FileConfiguration config = BuildSystem.getConfiguration();

        worlds.clear();

        for (String section : config.getKeys(false)) {
            if (config.isConfigurationSection(section)) {
                add(Objects.requireNonNull(config.getConfigurationSection(section)));
            }
        }
    }

    private void clearFor(Player p) {
        biomeName.remove(p);
        mapName.remove(p);
        minigameName.remove(p);
        emptys.remove(p);
        daylightCycles.remove(p);
        spawnMobs.remove(p);
        weatherCycles.remove(p);
    }

    private void add(ConfigurationSection config) {
        for (String section : config.getKeys(false)) {
            if (config.isConfigurationSection(section)) {
                if (worlds.contains(section)) return;

                worlds.add(section);
            }
        }
    }
}
