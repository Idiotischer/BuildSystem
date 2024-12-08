package de.idiotischeryt.buildSystem.command;

import de.idiotischeryt.buildSystem.BuildManager;
import de.idiotischeryt.buildSystem.BuildSystem;
import de.idiotischeryt.buildSystem.LocationViewer;
import de.idiotischeryt.buildSystem.menusystem.menu.WorldManagementMenu;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
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

            String key = args[1];
            String value = args[2];

            String[] names = BuildManager.namesByWorld(p.getWorld());
            if (names == null) {
                commandSender.sendMessage(BuildSystem.prefix().append(Component.text("Can't find your world as build world. Please go into the right world!").color(NamedTextColor.DARK_RED)));
                return false;
            }

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

            BuildManager.addToConfig(map, minigame, key, value);
            return true;
        } else if (args.length > 0 && args[0].equals("hide")) {
            if (!(commandSender instanceof Player p)) return false;
            LocationViewer.hide(p);
        } else if (args.length > 0 && args[0].equals("default")) {
            if (!(commandSender instanceof Player p)) return false;

            p.teleport(getServer().getWorlds().getFirst().getSpawnLocation());
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
            new WorldManagementMenu(BuildSystem.getPlayerMenuUtility(p)).open();
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
        if (strings.length == 1) return List.of("add", "show", "hide", "default");

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
