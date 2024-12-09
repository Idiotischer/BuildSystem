package de.idiotischeryt.buildSystem.menusystem;

import de.idiotischeryt.buildSystem.BuildSystem;
import de.idiotischeryt.buildSystem.menusystem.menu.WorldManagementMenu;
import de.idiotischeryt.buildSystem.menusystem.menu.WorldSettingsMenu;
import de.rapha149.signgui.SignGUI;
import de.rapha149.signgui.SignGUIAction;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.ArrayList;
import java.util.List;

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
                        List<String> filteredWorlds = searchInput(input, WorldManagementMenu.getWorlds());

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
                                inventory.addItem(makeItem(Material.BARRIER, ChatColor.RED + "No world(s) found!", false));
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

            Menu menu = new WorldSettingsMenu(playerMenuUtility, world);

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