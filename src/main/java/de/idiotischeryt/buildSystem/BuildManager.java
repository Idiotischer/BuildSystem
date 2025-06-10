package de.idiotischeryt.buildSystem;

import de.idiotischeryt.buildSystem.world.TemplateSettings;
import de.idiotischeryt.buildSystem.world.WorldCreator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
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

import static org.bukkit.Bukkit.getServer;

public class BuildManager {
    public static void createWorld(Player p, String mapName, String minigame, boolean empty, Biome biome, boolean spawnMobs, boolean dayNightCycle, boolean weatherCycle) throws IOException, InvalidConfigurationException {
        if (!p.hasPermission("buildsystem.permission.create") || !p.isOp()) return;

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

    public static void createTemplateSection(String template, String mapName, boolean empty, boolean spawnMobs, boolean dayNightCycle, Biome biome) throws IOException {
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

        Bukkit.getScheduler().runTask(BuildSystem.getInstance(), () -> {
            Bukkit.unloadWorld(world, false);

            Path worldFolder = world.getWorldFolder().toPath();

            try {
                Files.walkFileTree(worldFolder, new SimpleFileVisitor<>() {
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

        if (strings.length != 2) {

            String first = String.join("-", Arrays.copyOfRange(strings, 0, strings.length - 1));
            String second = strings[strings.length - 1];

            if (!configSections.contains(second)) return new String[]{name};

            return new String[]{first, second};
        }

        return strings;
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

    public static void copy(@NotNull World sourceWorld, Player pl) {
        if (!pl.hasPermission("buildsystem.permission.copy") && !pl.isOp()) {
            pl.sendMessage(Component.text("You don't have the permissions needed!")
                    .color(NamedTextColor.DARK_RED)
                    .decorate(TextDecoration.BOLD));
            return;
        }

        String[] worldNames = namesByWorld(sourceWorld);

        Bukkit.getScheduler().runTask(BuildSystem.getInstance(), () -> {
            File sourceFolder = sourceWorld.getWorldFolder();
            File targetFolder;
            int suffix = 1;

            File newTarget;
            String newWorldName;
            do {
                newWorldName = worldNames[0] + "-" + suffix + "-" + worldNames[1];
                newTarget = new File(Bukkit.getWorldContainer(), newWorldName);
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
                Files.walkFileTree(sourceFolder.toPath(), new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        Path targetDir = targetFolder.toPath().resolve(sourceFolder.toPath().relativize(dir));
                        Files.createDirectories(targetDir);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Path targetFile = targetFolder.toPath().resolve(sourceFolder.toPath().relativize(file));
                        if(targetFile.getFileName().toString().equalsIgnoreCase("uid.dat")) {
                            return FileVisitResult.CONTINUE;
                        }
                        Files.copy(file, targetFile);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                BuildSystem.sendError("Failed to copy world", e.getMessage(), "");
                deleteWorldFolder(targetFolder);
                return;
            }

            final World[] copiedWorld = new World[1];

            String finalNewWorldName = newWorldName;
            Bukkit.getScheduler().runTask(BuildSystem.getInstance(), () -> copiedWorld[0] =new org.bukkit.WorldCreator(finalNewWorldName).createWorld());
            int finalSuffix = suffix;
            Bukkit.getScheduler().runTask(BuildSystem.getInstance(), () -> {
                if (copiedWorld[0] == null) {
                BuildSystem.sendError("World copy failed to load!", "", "");
                deleteWorldFolder(targetFolder);
                return;
            }

            System.out.println("Copied world: "+ worldNames[0]);
            System.out.println("Copied template: "+ worldNames[1]);

            TemplateSettings settings = getTemplateSettings(worldNames[1], worldNames[0]);

            if (settings == null) {
                pl.sendMessage(ChatColor.RED + "Failed to copy: Missing or invalid configuration section.");
                delete(copiedWorld[0], pl);
                return;
            }

            System.out.println("empty: " + settings.empty());
            System.out.println("spawnMobs: " + settings.spawnMobs());
            System.out.println("dayNightCycle: " + settings.dayNightCycle());
            System.out.println("biome: " + settings.biome());

            if (settings.biome() == null) {
                pl.sendMessage(ChatColor.RED + "Failed to copy: Missing or invalid biome configuration.");
                delete(copiedWorld[0], pl);
                return;
            }

            try {
                createTemplateSection(worldNames[1], finalNewWorldName, settings.empty(), settings.spawnMobs(), settings.dayNightCycle(), settings.biome());
            } catch (IOException e) {
                pl.sendMessage(ChatColor.RED + "An error occurred while creating the template.");
                e.printStackTrace();
                delete(copiedWorld[0], pl);
                return;
            }

            String mapName = worldNames[0];
            String minigameName = worldNames[1];

            try {
                BuildSystem.getInstance().getConfigManager().copyConfig(mapName, minigameName, finalSuffix, pl);
            } catch (IOException | InvalidConfigurationException e) {
                BuildSystem.sendError("Failed to copy config", e.getMessage(), "");
                delete(copiedWorld[0], pl);
                return;
            }

            pl.teleport(copiedWorld[0].getSpawnLocation());
            Title title = Title.title(Component.text("World copied")
                    .color(NamedTextColor.GREEN)
                    .decorate(TextDecoration.BOLD), Component.text("successfully!")
                    .color(NamedTextColor.GREEN)
                    .decorate(TextDecoration.BOLD));
            pl.showTitle(title);});
        });
    }



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
