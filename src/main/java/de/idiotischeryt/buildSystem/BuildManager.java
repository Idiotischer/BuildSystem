package de.idiotischeryt.buildSystem;

import de.idiotischeryt.buildSystem.world.WorldCreator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import static org.bukkit.Bukkit.getServer;

public class BuildManager {
    public static void createWorld(Player p, String mapName, String minigame, boolean empty, Biome biome, boolean spawnMobs, boolean dayNightCycle, boolean weatherCycle) throws IOException, InvalidConfigurationException {
        YamlConfiguration configuration = (YamlConfiguration) BuildSystem.getInstance().getConfigManager().createConfig(mapName, minigame);

        World world = WorldCreator.createWorld(p, mapName, minigame, empty, spawnMobs, dayNightCycle, weatherCycle, biome);

        assert world != null;

        ConfigurationSection minigameSection = BuildSystem.configuration.getConfigurationSection(minigame);

        if (minigameSection == null)
            minigameSection = BuildSystem.configuration.createSection(minigame);

        ConfigurationSection section = minigameSection.getConfigurationSection(world.getName());

        if (section == null)
            section = minigameSection.createSection(world.getName());

        section.set("empty", empty);
        section.set("spawnMobs", spawnMobs);
        section.set("dayNightCycle", dayNightCycle);
        section.set("biome", biome.toString());

        BuildSystem.configuration.save(BuildSystem.getInstance().registryPath.toFile());
    }

    public static void delete(@NotNull World world) {
        world.getPlayers().forEach(player -> {
                    player.teleport(
                            Objects.requireNonNull(getServer().getWorlds().getFirst()).getSpawnLocation()
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
                throw new RuntimeException("Failed to delete world directory: " + worldFolder, e);
            }

            String[] strings = namesByWorld(world);

            assert strings != null;

            String mapName = strings[0];

            String minigameName = strings[1];

            try {
                BuildSystem.getInstance().getConfigManager().deleteConfig(mapName, minigameName);

                BuildSystem.getInstance().getConfigManager().deleteSection(
                        BuildSystem.getConfiguration(),
                        minigameName,
                        mapName + "-" + minigameName,
                        BuildSystem.getInstance().registryPath.toFile()
                );
            } catch (IOException | InvalidConfigurationException e) {
                throw new RuntimeException(e);
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

            List<Object> contents = (List<Object>) config.getList(key);
            if (contents == null) {
                contents = new ArrayList<>();
            }

            if (!contents.contains(value)) {
                contents.add(value);
                config.set(key, contents);
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
        String[] strings = name.split("-");

        if (strings.length != 2) {

            String first = String.join("-", Arrays.copyOfRange(strings, 0, strings.length - 1));
            String second = strings[strings.length - 1];

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
}
