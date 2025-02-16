package de.idiotischeryt.buildSystem.command;

import de.idiotischeryt.buildSystem.BuildManager;
import de.idiotischeryt.buildSystem.BuildSystem;
import de.idiotischeryt.buildSystem.LocationViewer;
import de.idiotischeryt.buildSystem.menusystem.menu.WorldManagementMenu;
import de.rapha149.signgui.exception.SignGUIVersionException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormatSymbols;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.bukkit.Bukkit.getServer;

public class BuildCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!commandSender.hasPermission("buildsystem.permission.command") || !commandSender.isOp())
            return false;

        if (args.length > 0 && args[0].equals("add")) {
            if (args.length < 3) {
                commandSender.sendMessage(
                        ChatColor.YELLOW
                                + "["
                                + ChatColor.RED
                                + "BuildSystem"
                                + ChatColor.YELLOW
                                + "]"
                                + ChatColor.RED
                                + " Did you mean: build add <key> <value> ?"
                );
                return false;
            }

            if (!(commandSender instanceof Player p)) return false;

            if (p.getWorld() == Bukkit.getWorlds().getFirst() || p.getWorld() == Bukkit.getWorlds().get(1) || p.getWorld() == Bukkit.getWorlds().get(2)) {
                p.sendMessage(Component.text("Cannot write to config in world: " + p.getWorld().getName() + "!").color(NamedTextColor.RED));
                return false;
            }

            String key = args[1];
            String value = args[2];

            if (args.length > 3) {
                StringBuilder valueBuilder = new StringBuilder();
                for (int i = 2; i < args.length; i++) {
                    valueBuilder.append(args[i]).append(" ");
                }
                value = valueBuilder.toString().trim();
            }

            String[] names = BuildManager.namesByWorld(p.getWorld());

            String map = names[0];
            String minigame = names[1];

            if (args.length == 5) {
                String value2 = args[3];
                String value3 = args[4];

                double x = parseCoordinate(p.getLocation().getX(), value);
                double y = parseCoordinate(p.getLocation().getY(), value2);
                double z = parseCoordinate(p.getLocation().getZ(), value3);

                BuildManager.addToConfig(map, minigame, key, String.valueOf(x), String.valueOf(y), String.valueOf(z));
                return true;
            }

            if (args.length == 7) {
                String value2 = args[3];
                String value3 = args[4];
                String yaw = args[5];
                String pitch = args[6];

                double x = parseCoordinate(p.getLocation().getX(), value);
                double y = parseCoordinate(p.getLocation().getY(), value2);
                double z = parseCoordinate(p.getLocation().getZ(), value3);
                float yawParsed = (float) parseCoordinate(p.getLocation().getYaw(), yaw);
                float pitchParsed = (float) parseCoordinate(p.getLocation().getPitch(), pitch);

                BuildManager.addToConfig(map, minigame, key, String.valueOf(x), String.valueOf(y), String.valueOf(z), String.valueOf(yawParsed), String.valueOf(pitchParsed));
                return true;
            }

            Number parsedValue;
            if (NumberUtils.isParsable(value)) {
                if (value.contains(".")) {
                    parsedValue = NumberUtils.createBigDecimal(value);
                } else {
                    parsedValue = NumberUtils.createLong(value);
                }
                BuildManager.addToConfig(map, minigame, key, parsedValue);
            } else {
                BuildManager.addToConfig(map, minigame, key, value);
            }

            return true;
        } else if (args.length > 0 && args[0].equals("hide")) {
            if (!(commandSender instanceof Player p)) return false;
            LocationViewer.hide(p);
        } else if (args.length > 0 && args[0].equals("default")) {
            if (!(commandSender instanceof Player p)) return false;

            p.teleport(getServer().getWorlds().getFirst().getSpawnLocation());
        } else if (args.length > 0 && (args[0].equalsIgnoreCase("teleport") || args[0].equalsIgnoreCase("tp"))) {
            if (!(commandSender instanceof Player) && args.length < 2) {
                commandSender.sendMessage(ChatColor.RED + "Only players can teleport when not specifying a player.");
                return true;
            }

            Player target = commandSender instanceof Player ? (Player) commandSender : null;

            if (args.length >= 2) {
                Player specifiedTarget = Bukkit.getPlayer(args[1]);
                if (specifiedTarget != null) {
                    target = specifiedTarget;
                }
            }

            if (target == null) {
                commandSender.sendMessage(ChatColor.RED + "Player not found.");
                return true;
            }

            if (args.length == 1) {
                commandSender.sendMessage(ChatColor.RED + "Invalid teleport command usage. Provide coordinates or a target.");
            } else if (args.length == 2) {
                Player destination = Bukkit.getPlayer(args[1]);
                if (destination == null) {
                    commandSender.sendMessage(ChatColor.RED + "Target player not found.");
                    return true;
                }
                target.teleport(destination.getLocation());
                commandSender.sendMessage(ChatColor.GREEN + "Teleported to " + destination.getName() + "!");
            } else if (args.length == 3) {
                Player targetPlayer = Bukkit.getPlayer(args[1]);
                Player destinationPlayer = Bukkit.getPlayer(args[2]);
                if (targetPlayer == null || destinationPlayer == null) {
                    commandSender.sendMessage(ChatColor.RED + "One of the players was not found.");
                    return true;
                }
                targetPlayer.teleport(destinationPlayer.getLocation());
                commandSender.sendMessage(ChatColor.GREEN + targetPlayer.getName() + " teleported to " + destinationPlayer.getName() + "!");
            } else if (args.length >= 4) {
                try {
                    double x = Double.parseDouble(args[1]);
                    double y = Double.parseDouble(args[2]);
                    double z = Double.parseDouble(args[3]);
                    float yaw = args.length >= 5 ? Float.parseFloat(args[4]) : target.getLocation().getYaw();
                    float pitch = args.length >= 6 ? Float.parseFloat(args[5]) : target.getLocation().getPitch();

                    target.teleport(new Location(target.getWorld(), x, y, z, yaw, pitch));
                } catch (NumberFormatException e) {
                    commandSender.sendMessage(ChatColor.RED + "Invalid coordinates or rotation values!");
                }
            } else if (args.length >= 5) {
                Player targetPlayer = Bukkit.getPlayer(args[1]);
                if (targetPlayer == null) {
                    commandSender.sendMessage(ChatColor.RED + "Target player not found.");
                    return true;
                }
                try {
                    double x = Double.parseDouble(args[2]);
                    double y = Double.parseDouble(args[3]);
                    double z = Double.parseDouble(args[4]);
                    float yaw = args.length >= 6 ? Float.parseFloat(args[5]) : targetPlayer.getLocation().getYaw();
                    float pitch = args.length >= 7 ? Float.parseFloat(args[6]) : targetPlayer.getLocation().getPitch();

                    targetPlayer.teleport(new Location(targetPlayer.getWorld(), x, y, z, yaw, pitch));
                } catch (NumberFormatException e) {
                    commandSender.sendMessage(ChatColor.RED + "Invalid coordinates or rotation values!");
                }
            } else if (args.length >= 6) {
                World world = Bukkit.getWorld(args[1]);
                if (world == null) {
                    commandSender.sendMessage(ChatColor.RED + "World not found. Using player's current world instead.");
                    world = target.getWorld();
                }
                try {
                    double x = Double.parseDouble(args[2]);
                    double y = Double.parseDouble(args[3]);
                    double z = Double.parseDouble(args[4]);
                    float yaw = args.length >= 7 ? Float.parseFloat(args[5]) : target.getLocation().getYaw();
                    float pitch = args.length >= 8 ? Float.parseFloat(args[6]) : target.getLocation().getPitch();

                    target.teleport(new Location(world, x, y, z, yaw, pitch));
                } catch (NumberFormatException e) {
                    commandSender.sendMessage(ChatColor.RED + "Invalid coordinates or rotation values!");
                }
            } else {
                commandSender.sendMessage(ChatColor.RED + "Invalid teleport command usage.");
            }
        } else if (args.length > 0 && args[0].equals("show")) {
            if (args.length < 2) {
                commandSender.sendMessage(
                        ChatColor.YELLOW
                                + "["
                                + ChatColor.RED
                                + "BuildSystem"
                                + ChatColor.YELLOW
                                + "]"
                                + ChatColor.RED
                                + " Did you mean: build show <key> ?"
                );
                return false;
            }

            if (!(commandSender instanceof Player p)) return false;

            try {
                String key = args[1];
                String[] names = BuildManager.namesByWorld(p.getWorld());

                if (names == null) {
                    commandSender.sendMessage(BuildSystem.prefix().append(Component.text("Can't find your world as build world. Please go into the right world!").color(NamedTextColor.DARK_RED)));
                    return false;
                }

                FileConfiguration config = new YamlConfiguration();
                Path dataFolder = Paths.get(BuildSystem.getInstance().getDataPath().toString(), names[1]);
                if (Files.notExists(dataFolder)) Files.createDirectories(dataFolder);

                Path ymlPath = Paths.get(dataFolder.toString(), names[0] + "-" + names[1] + ".yml");
                if (!Files.exists(ymlPath)) {
                    BuildSystem.getInstance().getSLF4JLogger().error("[BuildSystem]: Error... config not found!");
                    return false;
                }

                config.load(ymlPath.toFile());
                config.save(ymlPath.toFile());

                LocationViewer.show(p, Map.of(ymlPath.toFile(), key));
            } catch (IOException | InvalidConfigurationException e) {
                throw new RuntimeException(e);
            }
        } else if (args.length == 0) {
            if (!(commandSender instanceof Player p)) return false;
            try {
                new WorldManagementMenu(BuildSystem.getPlayerMenuUtility(p)).open();
            } catch (SignGUIVersionException e) {
                throw new RuntimeException(e);
            }
            return true;
        }

        return true;
    }

    /**
     * Parses a coordinate, supporting relative (~) values.
     *
     * @param base  The base value (e.g., player's current position).
     * @param input The input string to parse.
     * @return The parsed coordinate as a double.
     */
    private double parseCoordinate(double base, String input) {
        if (input.startsWith("~")) {
            if (input.length() == 1) {
                return base;
            }
            try {
                return base + Double.parseDouble(input.substring(1));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid relative coordinate: " + input);
            }
        }
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid coordinate: " + input);
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length == 1) return List.of("add", "show", "hide", "default", "teleport", "tp");

        if (strings[0].equals("add")) {
            if (strings.length == 2 && strings[1].isBlank()) return List.of("<key>");
            if (strings.length == 3 && strings[2].isBlank())
                return List.of("<value> [if coordinate use another 2 values]");
        }

        return Collections.emptyList();
    }

    public static boolean isStringNumeric(String str) {
        DecimalFormatSymbols currentLocaleSymbols = DecimalFormatSymbols.getInstance();
        char localeMinusSign = currentLocaleSymbols.getMinusSign();

        if (!Character.isDigit(str.charAt(0)) && str.charAt(0) != localeMinusSign) return true;

        boolean isDecimalSeparatorFound = false;
        char localeDecimalSeparator = currentLocaleSymbols.getDecimalSeparator();

        for (char c : str.substring(1).toCharArray()) {
            if (!Character.isDigit(c)) {
                if (c == localeDecimalSeparator && !isDecimalSeparatorFound) {
                    isDecimalSeparatorFound = true;
                    continue;
                }
                return true;
            }
        }
        return false;
    }
}
