package de.idiotischeryt.buildSystem;

import de.idiotischeryt.buildSystem.command.BuildCommand;
import de.idiotischeryt.buildSystem.gui.InventoryUI;
import de.idiotischeryt.buildSystem.listeners.MenuListener;
import de.idiotischeryt.buildSystem.listeners.PlayerListener;
import de.idiotischeryt.buildSystem.menusystem.PlayerMenuUtility;
import de.idiotischeryt.buildSystem.menusystem.menu.WorldManagementMenu;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class BuildSystem extends JavaPlugin {
    static BuildSystem inst = null;
    ConfigManager configManager = null;
    private final List<String> configSection = new ArrayList<>();
    static FileConfiguration configuration = new YamlConfiguration();
    public Path registryPath = null;

    public static Path worldFolder = null;

    public static Path inventoryConfig = null;

    public static FileConfiguration invConfiguration = new YamlConfiguration();

    public static Path spawnConfig = null;

    public static FileConfiguration spawnConfiguration = new YamlConfiguration();

    private static final HashMap<Player, PlayerMenuUtility> playerMenuUtilityMap = new HashMap<>();

    private static InventoryUI ui = null;

    private final static Component PREFIX =
            Component.text("[")
                    .color(NamedTextColor.YELLOW)
                    .append(Component.text("BuildSystem")
                            .color(NamedTextColor.RED))
                    .append(Component.text("]")
                            .color(NamedTextColor.YELLOW));

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

        new LocationViewer();

        ui = new InventoryUI();

        defaultComment();

        saveDefaultConfig();

        loop();

        getCommand("build").setExecutor(new BuildCommand());

        getServer().getPluginManager().registerEvents(ui, this);

        getServer().getPluginManager().registerEvents(new MenuListener(), this);

        configManager = new ConfigManager();
        try {

            worldFolder = Paths.get(this.getDataPath().toString(), "playerdata");

            if (Files.notExists(worldFolder)) {
                Files.createDirectory(worldFolder);
            }

            spawnConfig = Paths.get(worldFolder.toString(), "playerspawns.yml");

            if (Files.notExists(spawnConfig)) {
                Files.createFile(spawnConfig);

                defaultComment(spawnConfig.toFile(), "Here, all player spawnlocaions are saved!");

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

            configuration.save(registryPath.toFile());
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }

        defaultComment(registryPath.toFile(), "This file will be auto-filled with the according registry sections.");

        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);

        for (String name : WorldManagementMenu.getWorlds()) {

            if (Bukkit.getWorld(name) != null) continue;

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

            boolean hasComment = !lines.isEmpty() && lines.get(0).startsWith("# Example Template:");

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

}
