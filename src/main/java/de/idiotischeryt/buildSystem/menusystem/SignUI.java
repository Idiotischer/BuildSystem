package de.idiotischeryt.buildSystem.menusystem;

import de.idiotischeryt.buildSystem.BuildManager;
import de.idiotischeryt.buildSystem.BuildSystem;
import de.idiotischeryt.buildSystem.menusystem.menu.WorldManagementMenu;
import de.rapha149.signgui.SignGUI;
import de.rapha149.signgui.SignGUIAction;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.bukkit.Bukkit.getServer;

public class SignUI extends PaginatedMenu {

    boolean wasInSign = false;

    public SignUI(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
    }

    @Override
    public void open() {
        inventory = Bukkit.createInventory(this, getSlots(), getMenuName());

        this.setMenuItems();

        playerMenuUtility.menu = this;

        if (!wasInSign) {
            SignGUI ui = SignGUI.builder()
                    .setType(Material.OAK_SIGN)
                    .setGlow(true)
                    .setLine(1, "<< write here >>")
                    .setLine(0, "--------------")
                    .setHandler((player, signGUIResult) -> {
                        String input = signGUIResult.getLines()[1];
                        List<String> filteredWorlds = searchInput(input, WorldManagementMenu.worlds);

                        return List.of(SignGUIAction.runSync(BuildSystem.getInstance(), () -> {

                            if (!filteredWorlds.isEmpty()) {
                                for (int i = 0; i < getMaxItemsPerPage(); i++) {
                                    index = getMaxItemsPerPage() * page + i;
                                    if (index >= filteredWorlds.size()) break;
                                    if (filteredWorlds.get(index) != null) {
                                        inventory.addItem(makeItem(Material.GRASS_BLOCK, filteredWorlds.get(index)));
                                    }
                                }
                            } else {
                                inventory.addItem(makeItem(Material.BARRIER, ChatColor.RED + "No worlds found!"));
                            }

                            playerMenuUtility.getOwner().openInventory(inventory);
                        }));
                    })
                    .callHandlerSynchronously(BuildSystem.getInstance())
                    .build();

            wasInSign = true;

            ui.open(playerMenuUtility.getOwner());
        } else {
            playerMenuUtility.getOwner().openInventory(inventory);
        }

    }

    @Override
    public void addMenuBorder() {
        inventory.setItem(48, makeItem(Material.DARK_OAK_BUTTON, ChatColor.GREEN + "Left"));

        inventory.setItem(49, makeItem(Material.BARRIER, ChatColor.DARK_RED + "Close"));

        inventory.setItem(50, makeItem(Material.DARK_OAK_BUTTON, ChatColor.GREEN + "Right"));


        for (int i = 0; i < 10; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, super.FILLER_GLASS);
            }
        }

        inventory.setItem(17, super.FILLER_GLASS);
        inventory.setItem(18, super.FILLER_GLASS);
        inventory.setItem(26, super.FILLER_GLASS);
        inventory.setItem(27, super.FILLER_GLASS);
        inventory.setItem(35, super.FILLER_GLASS);
        inventory.setItem(36, super.FILLER_GLASS);

        for (int i = 44; i < 54; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, super.FILLER_GLASS);
            }
        }
    }

    public List<String> searchInput(String input, List<String> toSearch) {
        String i = input.toLowerCase();
        return toSearch.stream()
                .filter(s -> s.toLowerCase().contains(i))
                .toList();
    }

    @Override
    public String getMenuName() {
        return "WorldManager";
    }

    @Override
    public int getSlots() {
        return 9 * 6;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {

        Player p = (Player) e.getWhoClicked();

        ArrayList<Player> players = new ArrayList<>(getServer().getOnlinePlayers());

        if (playerMenuUtility.getOwner() != p) return;

        if (!p.getOpenInventory().getTopInventory().equals(this.inventory)) return;

        if (e.getCurrentItem().getType().equals(Material.BARRIER) &&
                e.getCurrentItem().getPersistentDataContainer().has(new NamespacedKey(BuildSystem.getInstance(), "menuPart"))) {
            p.closeInventory();

        } else if (e.getCurrentItem().getType().equals(Material.DARK_OAK_BUTTON) &&
                e.getCurrentItem().getPersistentDataContainer().has(new NamespacedKey(BuildSystem.getInstance(), "menuPart"))) {
            if (ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName()).equalsIgnoreCase("Left")) {
                if (page == 0) {
                    p.sendMessage(ChatColor.GRAY + "You are already on the first page.");
                } else {
                    page = page - 1;
                    open();
                }
            } else if (ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName()).equalsIgnoreCase("Right") &&
                    e.getCurrentItem().getPersistentDataContainer().has(new NamespacedKey(BuildSystem.getInstance(), "menuPart"))) {
                if (!((index + 1) >= players.size())) {
                    page = page + 1;
                    open();
                } else {
                    p.sendMessage(ChatColor.GRAY + "You are on the last page.");
                }
            }
        } else if (e.getCurrentItem().getType().equals(Material.GRASS_BLOCK)) {
            World world;

            if (Bukkit.getWorld(e.getCurrentItem().getItemMeta().getDisplayName()) == null) {
                world = new WorldCreator(e.getCurrentItem().getItemMeta().getDisplayName()).createWorld();
            } else {
                world = Bukkit.getWorld(e.getCurrentItem().getItemMeta().getDisplayName());
            }

            Menu menu = new Menu(playerMenuUtility) {
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
            };

            menu.open();
        }

    }


    @Override
    public void handleClose(InventoryCloseEvent e) {
        if (e.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;
        
        Bukkit.getScheduler().runTask(BuildSystem.getInstance(), () -> {
            new WorldManagementMenu(playerMenuUtility).open();
        });
    }

    @Override
    public void setMenuItems() {
        addMenuBorder();
    }
}