package de.idiotischeryt.buildSystem;

import de.idiotischeryt.buildSystem.command.BuildCommand;
import de.idiotischeryt.buildSystem.gui.InventoryUI;
import de.idiotischeryt.buildSystem.listeners.MenuListener;
import de.idiotischeryt.buildSystem.menusystem.PlayerMenuUtility;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
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

        saveDefaultConfig();

        loop();

        getCommand("build").setExecutor(new BuildCommand());

        getServer().getPluginManager().registerEvents(ui, this);

        getServer().getPluginManager().registerEvents(new MenuListener(), this);

        configManager = new ConfigManager();

        try {
            registryPath = Paths.get(this.getDataPath().toString(), "registry.yml");

            if (Files.notExists(registryPath)) {
                Files.createFile(registryPath);
            }

            configuration.load(registryPath.toFile());

            configuration.save(registryPath.toFile());
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
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
