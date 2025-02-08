package de.idiotischeryt.buildSystem;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.Map;

public class PlayerManager {
    public static void loadInventory(Player player) {
        loadInventory(player, player.getWorld());
    }

    public static void loadInventory(Player player, World world) {
        try {
            BuildSystem.invConfiguration.load(BuildSystem.inventoryConfig.toFile());
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }

        ConfigurationSection section1 = BuildSystem.invConfiguration.getConfigurationSection(player.getUniqueId().toString());
        if (section1 == null) {
            return;
        }

        ConfigurationSection section2 = section1.getConfigurationSection(world.getName());
        if (section2 == null) {
            return;
        }

        for (String key : section2.getKeys(false)) {
            int slot = Integer.parseInt(key);
            Map<String, Object> itemData = section2.getConfigurationSection(key).getValues(false);
            player.getInventory().setItem(slot, ItemStack.deserialize(itemData));
        }

    }

    public static void saveInventory(Player player) {
        saveInventory(player, player.getWorld());
    }

    public static void saveInventory(Player player, World world) {
        ConfigurationSection section1 = BuildSystem.invConfiguration.getConfigurationSection(player.getUniqueId().toString());
        if (section1 == null) {
            section1 = BuildSystem.invConfiguration.createSection(player.getUniqueId().toString());
        }

        ConfigurationSection section2 = section1.getConfigurationSection(world.getName());
        if (section2 == null) {
            section2 = section1.createSection(world.getName());
        } else {
            ConfigurationSection finalSection = section2;
            section2.getKeys(false).forEach(key -> finalSection.set(key, null));
        }

        for (int i = 0; i < player.getInventory().getContents().length; i++) {
            if (player.getInventory().getItem(i) == null) continue;

            section2.set(String.valueOf(i), player.getInventory().getItem(i).serialize());
        }

        try {
            BuildSystem.invConfiguration.save(BuildSystem.inventoryConfig.toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveLocation(Player player, Location world) {
        ConfigurationSection section1 = BuildSystem.spawnConfiguration.getConfigurationSection(player.getUniqueId().toString());
        if (section1 == null) {
            section1 = BuildSystem.spawnConfiguration.createSection(player.getUniqueId().toString());
        }

        ConfigurationSection section2 = section1.getConfigurationSection(world.getWorld().getName());
        if (section2 == null) {
            section2 = section1.createSection(world.getWorld().getName());
        } else {
            ConfigurationSection finalSection = section2;
            section2.getKeys(false).forEach(key -> finalSection.set(key, null));
        }

        section2.set("location.x", world.getX());
        section2.set("location.y", world.getY());
        section2.set("location.z", world.getZ());
        section2.set("location.yaw", world.getYaw());
        section2.set("location.pitch", world.getPitch());

        try {
            BuildSystem.spawnConfiguration.save(BuildSystem.spawnConfig.toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void loadLocation(Player player) {
        loadLocation(player, player.getWorld());
    }

    public static void loadLocation(Player player, World world) {
        try {
            BuildSystem.spawnConfiguration.load(BuildSystem.spawnConfig.toFile());
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }

        ConfigurationSection section1 = BuildSystem.spawnConfiguration.getConfigurationSection(player.getUniqueId().toString());
        if (section1 == null) {
            return;
        }

        ConfigurationSection section2 = section1.getConfigurationSection(world.getName());
        if (section2 == null) {
            return;
        }

        double x = section2.getDouble("location.x");
        double y = section2.getDouble("location.y");
        double z = section2.getDouble("location.z");
        float yaw = (float) section2.getDouble("location.yaw");
        float pitch = (float) section2.getDouble("location.pitch");

        Location location = new Location(world, x, y, z, yaw, pitch);

        player.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    public static void deleteInventoryFor(World world) {
        try {
            BuildSystem.invConfiguration.load(BuildSystem.inventoryConfig.toFile());
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }

        for (String playerUUID : BuildSystem.invConfiguration.getKeys(false)) {
            ConfigurationSection section1 = BuildSystem.invConfiguration.getConfigurationSection(playerUUID);
            if (section1 != null) {
                ConfigurationSection section2 = section1.getConfigurationSection(world.getName());
                if (section2 != null) {
                    section1.set(world.getName(), null);
                }
            }
        }

        try {
            BuildSystem.invConfiguration.save(BuildSystem.inventoryConfig.toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void deleteLocationFor(World world) {
        try {
            BuildSystem.spawnConfiguration.load(BuildSystem.spawnConfig.toFile());
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }

        for (String playerUUID : BuildSystem.spawnConfiguration.getKeys(false)) {
            ConfigurationSection section1 = BuildSystem.spawnConfiguration.getConfigurationSection(playerUUID);
            if (section1 != null) {
                ConfigurationSection section2 = section1.getConfigurationSection(world.getName());
                if (section2 != null) {
                    section1.set(world.getName(), null);
                }
            }
        }

        try {
            BuildSystem.spawnConfiguration.save(BuildSystem.spawnConfig.toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
