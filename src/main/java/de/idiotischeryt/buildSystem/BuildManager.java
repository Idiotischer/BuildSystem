package de.idiotischeryt.buildSystem;

import de.idiotischeryt.buildSystem.world.TemplateSettings;
import de.idiotischeryt.buildSystem.world.WorldCreator;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.apache.commons.io.FileUtils;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.bukkit.Bukkit.getServer;

public class BuildManager {
    public static void createWorld(Player p, String mapName, String minigame, boolean empty, Biome biome, boolean spawnMobs, boolean dayNightCycle, boolean weatherCycle) throws IOException, InvalidConfigurationException {
        if (!p.hasPermission("buildsystem.permission.create") || !p.isOp()) {
            p.sendMessage(Component.text("You don't have the right permissions to do this!")
                    .color(NamedTextColor.DARK_RED)
                    .decorate(TextDecoration.BOLD));
            return;
        }
        if (minigame.isBlank()) {
            minigame = "Other";
        } else {
            YamlConfiguration configuration = (YamlConfiguration) BuildSystem.getInstance().getConfigManager().createConfig(mapName, minigame);
        }

        World world = WorldCreator.createWorld(p, mapName, minigame, empty, spawnMobs, dayNightCycle, weatherCycle, biome);

        assert world != null;

        createTemplateSection(
                minigame,
                mapName,
                empty,
                spawnMobs,
                dayNightCycle,
                biome
        );
    }

    public static void createTemplateSection(String template, String mapName, boolean empty, boolean spawnMobs, boolean dayNightCycle, Biome biome, Pair<String,?>... extra) throws IOException {
        ConfigurationSection minigameSection = BuildSystem.configuration.getConfigurationSection(template);

        if (minigameSection == null)
            minigameSection = BuildSystem.configuration.createSection(template);

        ConfigurationSection section = minigameSection.getConfigurationSection(mapName);

        if (section == null)
            section = minigameSection.createSection(mapName);

        section.set("empty", empty);
        section.set("spawnMobs", spawnMobs);
        section.set("dayNightCycle", dayNightCycle);
        section.set("biome", biome.toString());
        section.set("world-material", Material.GRASS_BLOCK.toString().toUpperCase());

        if (extra != null) {
            for (Pair<String, ?> pair : extra) {
                if (pair != null) {
                    section.set(pair.left(), pair.right());
                }
            }
        }

        BuildSystem.configuration.save(BuildSystem.getInstance().registryPath.toFile());
    }
    public static TemplateSettings getTemplateSettings(String template, String mapName) {
        ConfigurationSection minigameSection = BuildSystem.configuration.getConfigurationSection(template);
        if (minigameSection == null) {
            Bukkit.getLogger().warning("[BuildSystem] Missing section for template: " + template);
            return null;
        }

        ConfigurationSection section = minigameSection.getConfigurationSection(mapName);
        if (section == null) {
            Bukkit.getLogger().warning("[BuildSystem] Missing section for map: " + mapName + " in template: " + template);
            return null;
        }

        try {
            boolean empty = section.getBoolean("empty");
            boolean spawnMobs = section.getBoolean("spawnMobs");
            boolean dayNightCycle = section.getBoolean("dayNightCycle");
            String biomeStr = section.getString("biome");
            String materialStr = section.getString("world-material");

            if (biomeStr == null || materialStr == null) {
                Bukkit.getLogger().warning("[BuildSystem] Missing biome or world-material in " + template + "/" + mapName);
                return null;
            }

            Biome biome = Biome.valueOf(biomeStr);
            Material material = Material.valueOf(materialStr);

            return new TemplateSettings(empty, spawnMobs, dayNightCycle, biome, material);
        } catch (Exception e) {
            Bukkit.getLogger().warning("[BuildSystem] Failed to parse template settings for " + template + "/" + mapName + ": " + e.getMessage());
            return null;
        }
    }

    public static void delete(@NotNull World world, Player deleter) {
        if (!deleter.hasPermission("buildsystem.permission.delete") || !deleter.isOp()) {
            deleter.sendMessage(
                    Component.text("You don't have the permissions needed!")
                            .color(NamedTextColor.DARK_RED)
                            .decorate(TextDecoration.BOLD)
            );

            return;
        }

        world.getPlayers().forEach(player -> {
                    player.teleport(
                            Objects.requireNonNull(getServer().getWorlds().getFirst()).getSpawnLocation(),
                            PlayerTeleportEvent.TeleportCause.PLUGIN
                    );
                    player.showTitle(Title.title(
                            Component.text("World is getting")
                                    .color(NamedTextColor.DARK_RED)
                                    .decorate(TextDecoration.BOLD),
                            Component.text("deleted")
                                    .color(NamedTextColor.DARK_RED)
                                    .decorate(TextDecoration.BOLD)
                    ));


                }
        );

        PlayerManager.deleteInventoryFor(world);
        PlayerManager.deleteLocationFor(world);

        Bukkit.unloadWorld(world, false);

        Bukkit.getScheduler().runTask(BuildSystem.getInstance(), () -> {

            Path worldFolder = world.getWorldFolder().toPath();

            try {
                FileUtils.deleteDirectory(worldFolder.toFile());
            } catch (IOException e) {
                new RuntimeException("Failed to delete world directory: " + worldFolder, e).printStackTrace();
            }

            String[] strings = namesByWorld(world);

            String mapName = strings[0];

            String minigameName = strings[1];

            try {
                BuildSystem.getInstance().getConfigManager().deleteConfig(mapName, minigameName);

                BuildSystem.getInstance().getConfigManager().deleteSection(
                        BuildSystem.getConfiguration(),
                        minigameName,
                        mapName,
                        BuildSystem.getInstance().registryPath.toFile()
                );
            } catch (IOException | InvalidConfigurationException e) {
                new RuntimeException(e).printStackTrace();
            }

        });
    }

    public static void addToConfig(String mapName, String minigame, String key, Object value) {
        try {
            FileConfiguration config = new YamlConfiguration();

            Path dataFolder = Paths.get(BuildSystem.getInstance().getDataPath().toString(), minigame);
            Path ymlPath = Paths.get(dataFolder.toString(), mapName + "-" + minigame + ".yml");

            if (Files.notExists(ymlPath)) {
                BuildSystem.getInstance().getSLF4JLogger().error("[BuildSystem]: Config not found: " + mapName + "-" + minigame + ".yml");
                return;
            }

            config.load(ymlPath.toFile());

            if (value instanceof Location location) {
                Map<String, Object> map = location.serialize();
                map.remove("world");
                value = map;
            }

            Object existingValue = config.get(key);

            if (existingValue == null) {
                config.set(key, value);
            } else if (existingValue instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> list = (List<Object>) existingValue;
                if (!list.contains(value)) {
                    list.add(value);
                    config.set(key, list);
                }
            } else {
                List<Object> list = new ArrayList<>();
                list.add(existingValue);
                if (!list.contains(value)) {
                    list.add(value);
                }
                config.set(key, list);
            }

            config.save(ymlPath.toFile());
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
    }


    public static String[] namesByWorld(World world) {
        String[] strings = world.getName().split("-");

        if (strings.length != 2) {

            String first = String.join("-", Arrays.copyOfRange(strings, 0, strings.length - 1));
            String second = strings[strings.length - 1];

            return new String[]{first, second};
        }

        return strings;
    }

    public static String[] namesByString(String name) {
        List<String> configSections = new ArrayList<>();
        FileConfiguration config = BuildSystem.getInstance().getConfig();
        configSections.add("Other");

        try {
            config.load(Paths.get(BuildSystem.getInstance().getDataPath().toString(), "config.yml").toFile());
        } catch (IOException | InvalidConfigurationException ex) {
            throw new RuntimeException(ex);
        }

        for (String section : config.getKeys(false)) {
            if (config.isConfigurationSection(section)) {
                configSections.add(section);
            }
        }

        String[] strings = name.split("-");

        if (strings.length == 2) {
            return strings;
        } else if (strings.length > 2) {
            String first = String.join("-", Arrays.copyOfRange(strings, 0, strings.length - 1));
            String second = strings[strings.length - 1];

            //TODO: get it with this check to workign yk?
            //if (configSections.contains(second.toLowerCase())) {
            //    return new String[]{first, second};
            //} else {
            //    Bukkit.getLogger().warning("Invalid world entry (section not found): " + name);
            //    return new String[]{name, "unknown"};
            //}

            return new String[]{first, second};
        } else {
            Bukkit.getLogger().warning("Invalid world entry (no split parts): " + name);
            return new String[]{name, "unknown"};
        }
    }

    public static void addToConfig(World world, String key, Object value) {
        String[] strings = namesByWorld(world);

        String mapName = strings[0];
        String minigameName = strings[1];

        addToConfig(mapName, minigameName, key, value);
    }

    public static List<Location> getLocations(File yamlFile, Player p, String key) {
        List<Location> locations = new ArrayList<>();
        YamlConfiguration config = YamlConfiguration.loadConfiguration(yamlFile);

        if (config.isList(key)) {
            List<Map<?, ?>> locationList = config.getMapList(key);
            for (Map<?, ?> map : locationList) {
                try {
                    double x = (double) map.get("x");
                    double y = (double) map.get("y");
                    double z = (double) map.get("z");

                    Block block = p.getWorld().getBlockAt((int) x, (int) y, (int) z);
                    Location location = block.getLocation();


                    if (x % 1 == 0 && y % 1 == 0 && z % 1 == 0) {
                        location.add(0.5, 0.5, 0.5);
                    }

                    locations.add(location);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } else {
            System.out.println("Key " + key + " is not a list.");
        }

        if (locations.isEmpty()) {
            System.out.println("nothing");
        }

        return locations;
    }


    public static void addToConfig(String map, String minigame, String key, String value, String value2, String value3) {
        World world = Bukkit.getWorld(map + "-" + minigame);
        Location location = new Location(world, Double.parseDouble(value), Double.parseDouble(value2), Double.parseDouble(value3), 0, 0);

        addToConfig(map, minigame, key, location);
    }

    public static void addToConfig(String map, String minigame, String key, String value, String value2, String value3, String pitch, String yaw) {
        World world = Bukkit.getWorld(map + "-" + minigame);

        Location location = new Location(world, Double.parseDouble(value), Double.parseDouble(value2), Double.parseDouble(value3), Float.parseFloat(yaw), Float.parseFloat(pitch));

        addToConfig(map, minigame, key, location);
    }


    @SuppressWarnings("unchecked")
    public static void copy(@NotNull World sourceWorld, Player pl) {
        if (!pl.hasPermission("buildsystem.permission.copy") && !pl.isOp()) {
            pl.sendMessage(Component.text("You don't have the right permissions to do this!")
                    .color(NamedTextColor.DARK_RED)
                    .decorate(TextDecoration.BOLD));
            return;
        }

        sourceWorld.save();
        //optional ig unloaden mit force, also Bukkit.getServer().unloadWorld(sourceWorld, true);

        String[] worldNames = namesByWorld(sourceWorld);

        Bukkit.getScheduler().runTask(BuildSystem.getInstance(), () -> {
            File sourceFolder = sourceWorld.getWorldFolder();
            File targetFolder;
            int suffix = 1;
            File newTarget;
            String newWorldName1;
            String newWorldName;
            int cachedSuffix;

            do {
                newWorldName = worldNames[0] + "-" + suffix;
                newWorldName1 = worldNames[0] + "-" + suffix + "-" + worldNames[1];
                newTarget = new File(Bukkit.getWorldContainer(), newWorldName1);
                cachedSuffix = suffix;
                suffix++;
            } while (newTarget.exists());
            targetFolder = newTarget;

            if (targetFolder.exists()) {
                pl.sendMessage(Component.text("Target world already exists!")
                        .color(NamedTextColor.RED)
                        .decorate(TextDecoration.BOLD));
                return;
            }

            try {
                FileUtils.copyDirectory(sourceFolder, targetFolder, file -> {
                    String name = file.getName().toLowerCase();
                    return !name.equals("uid.dat") && !name.equals("session.lock");
                });
            } catch (IOException e) {
                deleteWorldFolder(targetFolder);
                pl.sendMessage(Component.text("Failed to copy the world files: " + e.getMessage())
                        .color(NamedTextColor.RED)
                        .decorate(TextDecoration.BOLD));
                e.printStackTrace();
                return;
            }

            World copiedWorld = new org.bukkit.WorldCreator(targetFolder.getName()).createWorld();
            if (copiedWorld == null) {
                deleteWorldFolder(targetFolder);
                pl.sendMessage(Component.text("World copy failed to load!")
                        .color(NamedTextColor.RED)
                        .decorate(TextDecoration.BOLD));
                return;
            }

            TemplateSettings settings = getTemplateSettings(worldNames[1], worldNames[0]);
            if (settings == null || settings.biome() == null) {
                pl.sendMessage(Component.text("Failed to copy: Missing or invalid configuration section or biome.")
                        .color(NamedTextColor.RED));
                delete(copiedWorld, pl);
                return;
            }

            try {
                createTemplateSection(worldNames[1], newWorldName, settings.empty(),
                        settings.spawnMobs(), settings.dayNightCycle(), settings.biome(), Pair.of("copied", true));
            } catch (IOException e) {
                pl.sendMessage(Component.text("An error occurred while creating the template.")
                        .color(NamedTextColor.RED));
                delete(copiedWorld, pl);
                return;
            }

            try {
                FileConfiguration copiedConfig = BuildSystem.getInstance().getConfigManager()
                        .copyConfig(worldNames[0], worldNames[1], cachedSuffix, pl);
                if (copiedConfig == null) {
                    pl.sendMessage(Component.text("No config found for this template, skipping config copy.")
                            .color(NamedTextColor.YELLOW)
                            .decorate(TextDecoration.BOLD));
                }
            } catch (IOException | InvalidConfigurationException e) {
                BuildSystem.sendError("Failed to copy config", e.getMessage(), "");
                delete(copiedWorld, pl);
                return;
            }

            pl.teleport(copiedWorld.getSpawnLocation());
            Title title = Title.title(Component.text("World copied")
                            .color(NamedTextColor.GREEN)
                            .decorate(TextDecoration.BOLD),
                    Component.text("successfully!")
                            .color(NamedTextColor.GREEN)
                            .decorate(TextDecoration.BOLD));
            pl.showTitle(title);
        });
    }

    private record CopyContext(String newWorldName, int suffix, File targetFolder) {}

    private static void deleteWorldFolder(File folder) {
        if (folder == null || !folder.exists()) return;
        try {
            Files.walkFileTree(folder.toPath(), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            Bukkit.getLogger().warning("Failed to delete world folder: " + folder.getAbsolutePath());
        }
    }

}
