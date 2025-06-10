package de.idiotischeryt.buildSystem;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ConfigManager {
    final BuildSystem plugin;
    private @Nullable String configVersion = "v1";

    public ConfigManager() {
        this.plugin = BuildSystem.getInstance();
    }

    public FileConfiguration createConfig(String mapName, String templateName) throws IOException, InvalidConfigurationException {
        FileConfiguration config = new YamlConfiguration();

        Path dataFolder = Paths.get(plugin.getDataPath().toString(), templateName);
        if (Files.notExists(dataFolder)) Files.createDirectories(dataFolder);

        Path ymlPath = dataFolder.resolve(mapName + ".yml");
        Path legacyPath = dataFolder.resolve(mapName + "-" + templateName + ".yml");

        if (Files.exists(ymlPath)) {
            plugin.getSLF4JLogger().error("[BuildSystem]: Error creating config... '{}' already exists!", ymlPath);
            return null;
        }

        if (Files.exists(legacyPath)) {
            if (Files.exists(ymlPath)) {
                plugin.getSLF4JLogger().error("[BuildSystem]: Error creating config... Both '{}' and '{}' exist!", ymlPath, legacyPath);
                return null;
            }

            Files.move(legacyPath, ymlPath, StandardCopyOption.REPLACE_EXISTING);
            config.load(legacyPath.toFile());
            config.save(legacyPath.toFile());
            plugin.getSLF4JLogger().info("[BuildSystem]: Migrated legacy config '{}' to '{}'", legacyPath, ymlPath);
        } else {
            Files.createFile(ymlPath);
            config.load(ymlPath.toFile());
            config.save(ymlPath.toFile());
        }

        transferConfigurationSection(BuildSystem.getInstance().getConfig(), config, templateName, ymlPath.toFile());

        return config;
    }


    public FileConfiguration getConfig(String mapName, String templateName) {
        Path dataFolder = Paths.get(plugin.getDataPath().toString(), templateName);

        Path ymlPath = dataFolder.resolve(mapName + ".yml");
        Path legacyPath = dataFolder.resolve(mapName + "-" + templateName + ".yml");

        if (Files.exists(ymlPath)) {
            return YamlConfiguration.loadConfiguration(ymlPath.toFile());
        }

        if (Files.exists(legacyPath)) {
            FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(legacyPath.toFile());

            if (!fileConfiguration.contains("config-version")) {
                try {
                    Files.move(legacyPath, ymlPath, StandardCopyOption.REPLACE_EXISTING);
                    plugin.getSLF4JLogger().info("[BuildSystem]: Migrated legacy config '{}' to '{}'", legacyPath, ymlPath);
                    fileConfiguration.set("config-version", configVersion);

                    fileConfiguration.save(legacyPath.toFile());
                    return YamlConfiguration.loadConfiguration(ymlPath.toFile());
                } catch (IOException e) {
                    plugin.getSLF4JLogger().error("[BuildSystem]: Failed to migrate legacy config '{}'", legacyPath, e);
                }
            } else {
                return fileConfiguration;
            }
        }


        plugin.getSLF4JLogger().error("[BuildSystem]: Config file '{}' and legacy '{}' not found! File doesn't exit!", ymlPath, legacyPath);
        return null;
    }


    public void deleteConfig(String mapName, String templateName) throws IOException, InvalidConfigurationException {
        Path dataFolder = Paths.get(plugin.getDataPath().toString(), templateName);
        if (Files.notExists(dataFolder)) return;

        Path ymlPath = dataFolder.resolve(mapName + ".yml");
        Path legacyPath = dataFolder.resolve(mapName + "-" + templateName + ".yml");

        if (Files.exists(ymlPath)) {
            Files.delete(ymlPath);
            plugin.getSLF4JLogger().info("[BuildSystem]: Deleted config file '{}'.", ymlPath);
            return;
        }

        if (Files.exists(legacyPath)) {
            FileConfiguration legacyConfig = YamlConfiguration.loadConfiguration(legacyPath.toFile());

            if (!legacyConfig.contains("config-version")) {
                Files.delete(legacyPath);
                plugin.getSLF4JLogger().info("[BuildSystem]: Deleted legacy config file '{}'.", legacyPath);
            } else {
                plugin.getSLF4JLogger().warn("[BuildSystem]: Not deleting legacy config '{}' as it contains 'config-version'.", legacyPath);
            }
        } else {
            plugin.getSLF4JLogger().warn("[BuildSystem]: No config file found to delete for '{}' or legacy '{}'.", ymlPath, legacyPath);
        }
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
                                             File file
    ) {
        if (sourceConfig.contains(sectionPath)) {
            ConfigurationSection section = sourceConfig.getConfigurationSection(sectionPath);
            if (section != null) {
                for (String key : section.getKeys(false)) {
                    targetConfig.set(key, section.get(key));
                }

                try {
                    targetConfig.set("config-version", configVersion);

                    targetConfig.save(file);
                } catch (IOException e) {
                    plugin.getLogger().severe("Failed to save " + file.getName() + ": " + e.getMessage());
                }
            }
        } else {
            plugin.getLogger().warning("Section '" + sectionPath + "' not found in config.yml");
        }
    }

    public FileConfiguration copyConfig(String fromMapName, String templateName, int suffix, Player pl) throws IOException, InvalidConfigurationException {
        Path dataFolder = Paths.get(plugin.getDataPath().toString(), templateName);

        Path sourcePath = dataFolder.resolve(fromMapName + ".yml");
        if (Files.notExists(sourcePath)) {
            if(sourcePath.getParent().getFileName().toString().equalsIgnoreCase("Other")) {
                plugin.getSLF4JLogger().error("[BuildSystem]: No config for template \"other\" skipping!");
                return null;
            }
            plugin.getSLF4JLogger().error("[BuildSystem]: Source config '{}' does not exist!", sourcePath);
            return null;
        }

        Path targetPath;
        String copyMapName = fromMapName + "-" + suffix;

        targetPath = dataFolder.resolve(copyMapName + ".yml");

        if(Files.exists(targetPath)) {
            BuildSystem.sendError("Copied config already exists, skipping for: ", copyMapName, "with template " + templateName);
            pl.sendMessage(Component.text("Copied config already exists, skipping").color(NamedTextColor.DARK_RED));
        } else Files.copy(sourcePath, targetPath);

        FileConfiguration copiedConfig = new YamlConfiguration();
        copiedConfig.load(targetPath.toFile());

        plugin.getSLF4JLogger().info("[BuildSystem]: Successfully copied config '{}' to '{}'", sourcePath, targetPath);

        return copiedConfig;
    }
}
