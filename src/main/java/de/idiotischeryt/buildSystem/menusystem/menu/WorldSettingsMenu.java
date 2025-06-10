package de.idiotischeryt.buildSystem.menusystem.menu;

import de.idiotischeryt.buildSystem.BuildManager;
import de.idiotischeryt.buildSystem.BuildSystem;
import de.idiotischeryt.buildSystem.menusystem.Menu;
import de.idiotischeryt.buildSystem.menusystem.PlayerMenuUtility;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WorldSettingsMenu extends Menu {

    World world;

    public WorldSettingsMenu(PlayerMenuUtility playerMenuUtility, World world) {
        super(playerMenuUtility);

        this.world = world;
    }

    @Contract(pure = true)
    @Override
    public @NotNull String getMenuName() {
        return "Settings";
    }

    @Override
    public int getSlots() {
        return 9 * 5;
    }

    @Override
    public void handleMenu(@NotNull InventoryClickEvent e) {
        if (e.getCurrentItem() == null) return;

        if (e.getCurrentItem().getType() == Material.ENDER_PEARL) {
            playerMenuUtility.getOwner().teleport(world.getSpawnLocation());
        } else if (e.getCurrentItem().getType() == Material.RED_STAINED_GLASS_PANE) {

            assert world != null;
            BuildManager.delete(world, (Player) e.getWhoClicked());
        } else if (e.getCurrentItem().getType() == Material.NETHERITE_SCRAP) {
            BuildManager.copy(world, (Player) e.getWhoClicked());
        }
    }

    @Override
    public void setMenuItems() {
        String[] strings = BuildManager.namesByWorld(world);

        String mapName = strings[0];
        String minigameName = strings[1];

        try {
            BuildSystem.getConfiguration().load(BuildSystem.getInstance().registryPath.toFile());
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }


        ConfigurationSection sec = BuildSystem.getConfiguration().getConfigurationSection(minigameName);

        if (sec.isConfigurationSection(mapName)) {
            ConfigurationSection section = sec.getConfigurationSection(mapName);
            if (section != null) {

                Map<String, Object> stats = section.getValues(false);

                List<String> statsList = new ArrayList<>();

                statsList.add(ChatColor.GOLD + "mapName: " + ChatColor.WHITE + mapName);

                statsList.add(ChatColor.GOLD + "templateName: " + ChatColor.WHITE + minigameName);

                stats.forEach((key, value) -> statsList.add(ChatColor.GOLD + key + ": " + ChatColor.WHITE + value));

                inventory.setItem(13, makeItem(Material.PAPER, ChatColor.AQUA + "Stats", statsList.toArray(new String[0])));

            } else {

                inventory.setItem(13, makeItem(Material.PAPER, ChatColor.AQUA + "Stats", "nothing to show,", "please reopen", "the menu!"));

            }
        } else if (sec.isConfigurationSection(world.getName())) {
            ConfigurationSection section = sec.getConfigurationSection(minigameName + "." + world.getName());
            if (section != null) {

                Map<String, Object> stats = section.getValues(false);

                List<String> statsList = new ArrayList<>();

                statsList.add(ChatColor.GOLD + "mapName: " + ChatColor.WHITE + mapName);

                statsList.add(ChatColor.GOLD + "templateName: " + ChatColor.WHITE + minigameName);

                stats.forEach((key, value) -> statsList.add(ChatColor.GOLD + key + ": " + ChatColor.WHITE + value));

                inventory.setItem(13, makeItem(Material.PAPER, ChatColor.AQUA + "Stats", statsList.toArray(new String[0])));

            } else {
                System.out.println(minigameName + "." + world.getName());
                System.out.println(minigameName + "." + mapName);

                inventory.setItem(13, makeItem(Material.PAPER, ChatColor.AQUA + "Stats", "nothing to show,", "please reopen", "the menu!"));

            }
        } else {
            inventory.setItem(13, makeItem(Material.PAPER, ChatColor.AQUA + "Stats", "nothing to show,", "please reopen", "the menu!"));
        }


        inventory.setItem(11, makeItem(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "Delete"));

        inventory.setItem(15, makeItem(Material.ENDER_PEARL, ChatColor.GREEN + "Teleport"));

        inventory.setItem(29, makeItem(Material.NETHERITE_SCRAP, ChatColor.YELLOW + "Copy"));
        inventory.setItem(31, makeItem(Material.BARRIER, ChatColor.RED + "Coming Soon..."));
        inventory.setItem(33, makeItem(Material.BARRIER, ChatColor.RED + "Coming Soon..."));

        setFillerGlass();
    }

}
