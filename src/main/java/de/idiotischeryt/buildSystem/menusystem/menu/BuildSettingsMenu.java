package de.idiotischeryt.buildSystem.menusystem.menu;

import de.idiotischeryt.buildSystem.BuildManager;
import de.idiotischeryt.buildSystem.BuildSystem;
import de.idiotischeryt.buildSystem.menusystem.Menu;
import de.idiotischeryt.buildSystem.menusystem.PlayerMenuUtility;
import de.rapha149.signgui.exception.SignGUIVersionException;
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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.bukkit.Bukkit.getServer;

//class is too long, please refactor
public class BuildSettingsMenu extends Menu {
    String[] mapName = new String[]{""};
    String[] template = new String[]{""};
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
    public void handleMenu(InventoryClickEvent e) throws SignGUIVersionException {

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

                List<String> configSections1 = new ArrayList<>();

                try {
                    BuildSystem.getInstance().getConfig().load(Paths.get(BuildSystem.getInstance().getDataPath().toString(), "config.yml").toFile());
                } catch (IOException | InvalidConfigurationException ex) {
                    throw new RuntimeException(ex);
                }

                for (String section : BuildSystem.getInstance().getConfig().getKeys(false)) {
                    if (BuildSystem.getInstance().getConfig().isConfigurationSection(section)) {
                        configSections1.add("-" + section);
                    }
                }

                configSections.add(getServer().getWorlds().getFirst().getName());
                configSections.add("template-material");

                handleRename(playerMenuUtility.getOwner(), this, "Rename me", "Map name here", mapName, configSections, ChatColor.RED + "That map already exists!", true, false, false, configSections1);
            } else if (e.getCurrentItem().getType() == Material.NAME_TAG && e.getCurrentItem().getItemMeta()
                    .getPersistentDataContainer()
                    .has(new NamespacedKey(BuildSystem.getInstance(), "Minigame_Object"))
            ) {
                List<String> configSections = new ArrayList<>();

                try {
                    BuildSystem.getInstance().getConfig().load(Paths.get(BuildSystem.getInstance().getDataPath().toString(), "config.yml").toFile());
                } catch (IOException | InvalidConfigurationException ex) {
                    throw new RuntimeException(ex);
                }

                for (String section : BuildSystem.getInstance().getConfig().getKeys(false)) {
                    if (BuildSystem.getInstance().getConfig().isConfigurationSection(section)) {
                        configSections.add(section);
                    }
                }

                handleRename(playerMenuUtility.getOwner(), this, "Rename me", "Template name here", template, configSections, ChatColor.RED + "That type doesn't exist!", false, true, false, Collections.emptyList());
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
                            Bukkit.getScheduler().runTask(BuildSystem.getInstance(), () -> {
                                try {
                                    current.open();
                                } catch (SignGUIVersionException ex) {
                                    throw new RuntimeException(ex);
                                }
                            });
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
                Menu menu = new BooleanMenu(
                        playerMenuUtility,
                        this,
                        "Choose if the World is Empty",
                        new NamespacedKey(BuildSystem.getInstance(), "Boolean_Object"),
                        false,
                        value -> {
                            System.out.println("Boolean_Object toggled to: " + value);
                            emptys = value;
                        });

                try {
                    menu.open();
                } catch (SignGUIVersionException ex) {
                    throw new RuntimeException(ex);
                }
            } else if (e.getCurrentItem()
                    .getPersistentDataContainer()
                    .has(new NamespacedKey(BuildSystem.getInstance(), "DaylightCycle_Object"))
            ) {
                Menu menu = new ToggleMenu(
                        playerMenuUtility,
                        this,
                        new NamespacedKey(BuildSystem.getInstance(), "Boolean1_Object"),
                        new NamespacedKey(BuildSystem.getInstance(), "Boolean_Object"),
                        value -> emptys = value,
                        value1 -> emptys = value1
                );

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

                        Bukkit.getScheduler().runTask(BuildSystem.getInstance(), () -> {
                            try {
                                current.open();
                            } catch (SignGUIVersionException ex) {
                                throw new RuntimeException(ex);
                            }
                        });
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

                if (mapName[0].trim().isEmpty()) return;

                try {
                    BuildManager.createWorld(playerMenuUtility.getOwner(),
                            mapName[0],
                            template[0],
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

        Bukkit.getScheduler().runTask(BuildSystem.getInstance(), () -> {
            try {
                new WorldManagementMenu(playerMenuUtility).open();
            } catch (SignGUIVersionException ex) {
                throw new RuntimeException(ex);
            }
        });

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

    static List<Character> allowedChars = Arrays.asList(
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '/', '.', '_', '-'
    );

    public void handleRename(Player p, Menu currentMenu, String label, String defaultText,
                             String[] map, List<String> needed, String errText, boolean contains, boolean emptyAllowed, boolean allowEndWith, List<String> endWith) {
        AnvilGUI.Builder builder = new AnvilGUI.Builder();

        builder.plugin(BuildSystem.getInstance())
                .interactableSlots(1)
                .text(defaultText)
                .itemLeft(makeItem(Material.NAME_TAG, label, false))
                .onClose(state -> {
                    Bukkit.getScheduler().runTask(BuildSystem.getInstance(), () -> {
                        try {
                            currentMenu.open();
                        } catch (SignGUIVersionException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    p.updateInventory();
                })
                .onClick((slot, state) -> {
                    String inputText = state.getText().trim();

                    if (!isValid(inputText)) {
                        return Arrays.asList(
                                AnvilGUI.ResponseAction.replaceInputText(defaultText),
                                AnvilGUI.ResponseAction.updateTitle(ChatColor.RED + "Only [a-z0-9/._-] allowed!", true)
                        );
                    }

                    if (!allowEndWith) {
                        for (String s : endWith) {
                            if (inputText.endsWith(s)) {
                                return Arrays.asList(
                                        AnvilGUI.ResponseAction.replaceInputText(defaultText),
                                        AnvilGUI.ResponseAction.updateTitle(ChatColor.RED + "Can't use template Name!", true)
                                );
                            }
                        }
                    }

                    if (emptyAllowed && !needed.contains("")) {
                        needed.add("");
                    }

                    if (emptyAllowed && !needed.contains("Other")) {
                        needed.add("Other");
                    }

                    boolean isInNeeded = needed.contains(inputText);

                    if ((contains && isInNeeded) || (!contains && !isInNeeded)) {
                        return Arrays.asList(
                                AnvilGUI.ResponseAction.replaceInputText(defaultText),
                                AnvilGUI.ResponseAction.updateTitle(errText, true)
                        );
                    }

                    map[0] = inputText;

                    Bukkit.getScheduler().runTaskLater(BuildSystem.getInstance(), () -> {
                        if (!state.getPlayer().getItemOnCursor().getType().isEmpty()) {
                            state.getPlayer().getItemOnCursor().setType(Material.AIR);
                        }
                    }, 2);

                    if (!map[0].isEmpty()) {
                        p.closeInventory();
                        return Collections.singletonList(AnvilGUI.ResponseAction.close());
                    }

                    return Collections.singletonList(AnvilGUI.ResponseAction.replaceInputText(defaultText));
                });


        builder.open(p);
    }

    public static boolean isValid(String input) {
        for (char c : input.toCharArray()) {
            if (!allowedChars.contains(c)) {
                return false;
            }
        }
        return true;
    }

    public void clear() {
        mapName = new String[]{""};
        template = new String[]{""};
        biomeName = new String[]{"PLAINS"};
        emptys = false;
        daylightCycles = false;
        spawnMobs = false;
        weatherCycles = false;
    }
}
