package de.idiotischeryt.buildSystem;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigManager {
    final BuildSystem plugin;

    public ConfigManager() {
        this.plugin = BuildSystem.getInstance();
    }

    public FileConfiguration createConfig(String mapName, String minigameName) throws IOException, InvalidConfigurationException {
        FileConfiguration config = new YamlConfiguration();

        Path dataFolder = Paths.get(plugin.getDataPath().toString(), minigameName);

        if (Files.notExists(dataFolder)) Files.createDirectories(dataFolder);

        Path ymlPath = Paths.get(dataFolder.toString(), mapName + "-" + minigameName + ".yml");

        if (Files.exists(ymlPath)) {
            plugin.getSLF4JLogger().error("[BuildSystem]: Error creating Config... File already exists!");
            return null;
        }

        Files.createFile(ymlPath);

        config.load(ymlPath.toFile());

        config.save(ymlPath.toFile());

        transferConfigurationSection(BuildSystem.getInstance().getConfig(), config, minigameName, ymlPath.toFile());

        return config;
    }

    public void deleteConfig(String mapName, String minigameName) throws IOException, InvalidConfigurationException {
        Path dataFolder = Paths.get(plugin.getDataPath().toString(), minigameName);

        if (Files.notExists(dataFolder)) Files.createDirectories(dataFolder);

        Path ymlPath = Paths.get(dataFolder.toString(), mapName + "-" + minigameName + ".yml");


        Files.deleteIfExists(ymlPath);
    }

    public void deleteSection(@NotNull FileConfiguration sourceConfig, String section, String section2, File file) throws IOException, InvalidConfigurationException {

        if (!sourceConfig.contains(section)) {
            plugin.getLogger().warning("Section not found in registry.yml");

            return;
        }

        if (!sourceConfig.isConfigurationSection(section)) return;

        ConfigurationSection configSection = sourceConfig.getConfigurationSection(section);

        if (!configSection.contains(section2)) {
            plugin.getLogger().warning("Section not found in first config-section");

            return;
        }

        if (!sourceConfig.isConfigurationSection(section)) return;

        configSection.set(section2, null);

        try {
            sourceConfig.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save " + file.getName() + ": " + e.getMessage());
        }

    }

    public void transferConfigurationSection(@NotNull FileConfiguration sourceConfig,
                                             @NotNull FileConfiguration targetConfig,
                                             @NotNull String sectionPath,
                                             File file) {
        if (sourceConfig.contains(sectionPath)) {
            ConfigurationSection section = sourceConfig.getConfigurationSection(sectionPath);
            if (section != null) {
                for (String key : section.getKeys(false)) {
                    targetConfig.set(key, section.get(key));
                }

                try {
                    targetConfig.save(file);
                } catch (IOException e) {
                    plugin.getLogger().severe("Failed to save " + file.getName() + ": " + e.getMessage());
                }
            }
        } else {
            plugin.getLogger().warning("Section '" + sectionPath + "' not found in config.yml");
        }
    }


}
