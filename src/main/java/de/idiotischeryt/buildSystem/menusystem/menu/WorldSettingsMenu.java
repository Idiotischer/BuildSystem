package de.idiotischeryt.buildSystem.menusystem.menu;

import de.idiotischeryt.buildSystem.BuildManager;
import de.idiotischeryt.buildSystem.BuildSystem;
import de.idiotischeryt.buildSystem.menusystem.Menu;
import de.idiotischeryt.buildSystem.menusystem.PlayerMenuUtility;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

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
        return 9 * 3;
    }

    @Override
    public void handleMenu(@NotNull InventoryClickEvent e) {
        if (e.getCurrentItem() == null) return;

        if (e.getCurrentItem().getType() == Material.ENDER_PEARL) {
            playerMenuUtility.getOwner().teleport(world.getSpawnLocation());
        } else if (e.getCurrentItem().getType() == Material.RED_STAINED_GLASS_PANE) {

            assert world != null;
            BuildManager.delete(world);
        }
    }

    @Override
    public void setMenuItems() {
        String[] strings = BuildManager.namesByWorld(world);

        String mapName = strings[0];
        String minigameName = strings[1];

        if (BuildSystem.getConfiguration().isConfigurationSection(minigameName + "." + world.getName())) {
            ConfigurationSection section = BuildSystem.getConfiguration().getConfigurationSection(minigameName + "." + world.getName());
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
        } else {
            inventory.setItem(13, makeItem(Material.PAPER, ChatColor.AQUA + "Stats", "nothing to show,", "please reopen", "the menu!"));
        }


        inventory.setItem(11, makeItem(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "Delete"));

        inventory.setItem(15, makeItem(Material.ENDER_PEARL, ChatColor.GREEN + "Teleport"));

        setFillerGlass();
    }

}
