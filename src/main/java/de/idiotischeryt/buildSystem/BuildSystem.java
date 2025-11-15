package de.idiotischeryt.buildSystem;

import de.idiotischeryt.buildSystem.command.BuildCommand;
import de.idiotischeryt.buildSystem.gui.InventoryUI;
import de.idiotischeryt.buildSystem.listeners.AnvilListener;
import de.idiotischeryt.buildSystem.listeners.CMDListener;
import de.idiotischeryt.buildSystem.listeners.MenuListener;
import de.idiotischeryt.buildSystem.listeners.PlayerListener;
import de.idiotischeryt.buildSystem.menusystem.PlayerMenuUtility;
import de.idiotischeryt.buildSystem.menusystem.menu.RenameData;
import de.idiotischeryt.buildSystem.menusystem.menu.WorldManagementMenu;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

//TODO: use only nio, its currently too messy with the old io stuff AND add default world in the menu (world, world_end, world_nether, etc)
//TODO: add import world feature
//TODO: make copies be listed under the source world, for this i'd need to fix the logic in signGUI (not) and worldmanagementmenu which filters the world and lists it, so i'd need to filter all worlds with copied: true
//TODO: add /docs which opens a UI with knowledge book which leads to docs
public final class BuildSystem extends JavaPlugin {
    static BuildSystem inst = null;
    ConfigManager configManager = null;
    private final List<String> configSection = new ArrayList<>();
    static FileConfiguration configuration = new YamlConfiguration();
    public Path registryPath = null;
    public Path propertiesPath = null;

    public static Path worldFolder = null;

    public static Path inventoryConfig = null;

    public static FileConfiguration invConfiguration = new YamlConfiguration();

    public static Path spawnConfig = null;

    public static FileConfiguration spawnConfiguration = new YamlConfiguration();

    private static final HashMap<Player, PlayerMenuUtility> playerMenuUtilityMap = new HashMap<>();

    private static InventoryUI ui = null;

    LocationViewer viewer;
    private final Map<UUID, RenameData> renameContexts = new HashMap<>();
    private final static Component PREFIX =
            Component.text("[")
                    .color(NamedTextColor.YELLOW)
                    .append(Component.text("BuildSystem")
                            .color(NamedTextColor.RED))
                    .append(Component.text("]")
                            .color(NamedTextColor.YELLOW));
    public FileConfiguration propertiesConfig = new YamlConfiguration();

    public static @NotNull Component sendError(String start, String error, String end) {
        return PREFIX
                .append(Component.space())
                .append(Component.text(start).color(NamedTextColor.RED))
                .append(Component.space())
                .append(Component.text(error).color(NamedTextColor.YELLOW))
                .append(Component.space())
                .append(Component.text(end).color(NamedTextColor.RED));
    }

    @Override
    public void onEnable() {
        inst = this;

        File configFile = new File(this.getDataFolder(), "config.yml");

        if (!this.getDataFolder().exists()) {
            this.getDataFolder().mkdirs();
            if(!configFile.exists()) {
                try {
                    configFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        this.saveDefaultConfig();

        viewer = new LocationViewer();

        ui = new InventoryUI();

        defaultComment();

        loop();

        getCommand("build").setExecutor(new BuildCommand());

        getServer().getPluginManager().registerEvents(new CMDListener(), this);

        getServer().getPluginManager().registerEvents(ui, this);

        getServer().getPluginManager().registerEvents(new MenuListener(), this);

        getServer().getPluginManager().registerEvents(new AnvilListener(), this);

        configManager = new ConfigManager();
        try {

            worldFolder = Paths.get(this.getDataPath().toString(), "playerdata");

            if (Files.notExists(worldFolder)) {
                Files.createDirectory(worldFolder);
            }

            spawnConfig = Paths.get(worldFolder.toString(), "playerspawns.yml");

            if (Files.notExists(spawnConfig)) {
                Files.createFile(spawnConfig);

                defaultComment(spawnConfig.toFile(), "Here, all player spawnlocations are saved!");

                spawnConfiguration.save(spawnConfig.toFile());
            }

            inventoryConfig = Paths.get(worldFolder.toString(), "playerinvs.yml");

            if (Files.notExists(inventoryConfig)) {
                Files.createFile(inventoryConfig);

                defaultComment(inventoryConfig.toFile(), "Here, player inventories for each world are saved!");

                invConfiguration.save(inventoryConfig.toFile());
            }

            registryPath = Paths.get(this.getDataPath().toString(), "registry.yml");

            if (Files.notExists(registryPath)) {
                Files.createFile(registryPath);
            }

            configuration.load(registryPath.toFile());

            if (!configuration.contains("Other")) {
                configuration.set("Other", Collections.emptyList());
            }

            configuration.save(registryPath.toFile());

            propertiesPath = Paths.get(this.getDataPath().toString(), "properties.yml");

            if (Files.notExists(propertiesPath)) {
                Files.createFile(propertiesPath);
            }

            propertiesConfig.load(propertiesPath.toFile());

            if (!propertiesConfig.contains("saveAndLoadPlayerInventory")) {
                propertiesConfig.set("saveAndLoadPlayerInventory", true);
            }

            if (!propertiesConfig.contains("safeCommandExecute")) {
                propertiesConfig.set("safeCommandExecute", true);
            }

            if (!propertiesConfig.contains("saveAndLoadPlayerLocation")) {
                propertiesConfig.set("saveAndLoadPlayerLocation", true);
            }

            if (!propertiesConfig.contains("deleteSessionLock")) {
                propertiesConfig.set("deleteSessionLock", false);
            }
            if (!propertiesConfig.contains("deleteUidDat")) {
                propertiesConfig.set("deleteUidDat", false);
            }

            propertiesConfig.save(propertiesPath.toFile());

        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }

        defaultComment(registryPath.toFile(), "This file will be auto-filled with the according registry sections. \n Add template-material to your Template \n when you'd like another icon!");


        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);

        for (String name : WorldManagementMenu.getWorlds()) {

            if (Bukkit.getWorld(name) != null) continue;

            if (propertiesConfig.contains("deleteSessionLock") && propertiesConfig.getBoolean("deleteSessionLock")) {
                File folder = new File(Bukkit.getWorldContainer(), name);

                if (folder.exists()) {
                    for (File f : Objects.requireNonNull(folder.listFiles())) {
                        if (f.isFile() && f.getName().equalsIgnoreCase("session.lock")) f.delete();
                    }
                }
            }
            if (propertiesConfig.contains("deleteUidDat") && propertiesConfig.getBoolean("deleteUidDat")) {
                File folder = new File(Bukkit.getWorldContainer(), name);

                if (folder.exists()) {
                    for (File f : Objects.requireNonNull(folder.listFiles())) {
                        if (f.isFile() && f.getName().equalsIgnoreCase("uid.dat")) f.delete();
                    }
                }
            }

            name = ChatColor.stripColor(name);

            if (name.contains("(") || name.contains(")")) {
                name = name.replaceAll("\\(.*?\\)", "");
            }

            new WorldCreator(name).createWorld();
        }
    }


    public void defaultComment() {
        File configFile = new File(this.getDataFolder(), "config.yml");

        String comment = """
                Example Template:
                
                Skywars:
                   middleLocations: [ ]
                   middleContainerLocations: [ ]
                   spawnLocs: [ ]""";

        try {
            List<String> lines = new BufferedReader(new FileReader(configFile)).lines().toList();

            boolean hasComment = !lines.isEmpty() && lines.getFirst().startsWith("# Example Template:");

            if (!hasComment) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile))) {
                    writer.write("# " + comment.replace("\n", "\n# ") + "\n");

                    for (String line : lines) {
                        writer.write(line + "\n");
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void defaultComment(File configFile, String comment) {
        try {
            List<String> lines = new BufferedReader(new FileReader(configFile)).lines().toList();
            String normalizedComment = "# " + comment.replace("\n", "\n# ").trim();

            String fileHeader = lines.stream()
                    .takeWhile(line -> line.startsWith("#"))
                    .reduce((line1, line2) -> line1 + "\n" + line2)
                    .orElse("")
                    .trim();

            if (!fileHeader.equals(normalizedComment)) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile))) {
                    writer.write(normalizedComment + "\n");

                    for (String line : lines) {
                        writer.write(line + "\n");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void updateOrSetPlayer(FileConfiguration config, Player p, Object value) {
        config.set(p.getUniqueId().toString(), value);
    }

    //    credits kody simpson :)
    //    to lazy to code my own :)
    public static PlayerMenuUtility getPlayerMenuUtility(Player p) {
        PlayerMenuUtility playerMenuUtility;
        if (!(playerMenuUtilityMap.containsKey(p))) {

            playerMenuUtility = new PlayerMenuUtility(p);
            playerMenuUtilityMap.put(p, playerMenuUtility);

            return playerMenuUtility;
        } else {
            return playerMenuUtilityMap.get(p);
        }
    }

    private void loop() {
        FileConfiguration config = getConfig();

        for (String section : config.getKeys(false)) {
            if (config.isConfigurationSection(section)) {
                configSection.add(section);
            }
        }
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static BuildSystem getInstance() {
        return inst;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public FileConfiguration getProperties() {
        return propertiesConfig;
    }
    public List<String> getConfigSections() {
        return configSection;
    }

    public static Component prefix() {
        return PREFIX;
    }

    public static InventoryUI getUi() {
        return ui;
    }

    public static FileConfiguration getConfiguration() {
        return configuration;
    }
    public void registerRename(Player player, RenameData context) {
        renameContexts.put(player.getUniqueId(), context);
    }

    public RenameData getRenameContext(UUID uuid) {
        return renameContexts.get(uuid);
    }

    public void unregisterRename(UUID uuid) {
        renameContexts.remove(uuid);
    }
}
