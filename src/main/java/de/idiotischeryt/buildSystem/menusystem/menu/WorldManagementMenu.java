package de.idiotischeryt.buildSystem.menusystem.menu;

import de.idiotischeryt.buildSystem.BuildSystem;
import de.idiotischeryt.buildSystem.menusystem.PaginatedMenu;
import de.idiotischeryt.buildSystem.menusystem.PlayerMenuUtility;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static org.bukkit.Bukkit.getServer;

public class KillPlayerMenu extends PaginatedMenu {

    Map<Player, List<Component>> texts = new HashMap<>();
    Inventory settings = null;

    public KillPlayerMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);

        settings = createGUI();
    }

    @Override
    public String getMenuName() {
        return "Choose a Player to Murder";
    }

    @Override
    public int getSlots() {
        return 9 * 6;
    }

    public Inventory createGUI() {
        Inventory gui = Bukkit.createInventory(null, 9 * 5, ChatColor.GREEN + "Custom GUI");

        //gui.setItem(0, makeItem(Material.GREEN_STAINED_GLASS_PANE, "", false));
        //gui.setItem(1, makeItem(Material.RED_STAINED_GLASS_PANE, "", false));
        //gui.setItem(2, makeItem(Material.STRUCTURE_VOID, "", false));


        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null || gui.getItem(i).getType() == Material.AIR) {
                gui.setItem(i, this.FILLER_GLASS);
            }
        }

        return gui;
    }

    @Override
    public void handleMenu(@NotNull InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();

        ArrayList<Player> players = new ArrayList<Player>(getServer().getOnlinePlayers());

        if (!p.getOpenInventory().getTopInventory().equals(this.inventory)) return;

        if (e.getCurrentItem().getType().equals(Material.GRASS_BLOCK)) {

            AnvilGUI.Builder builder = new AnvilGUI.Builder();
            builder.plugin(BuildSystem.getInstance())
                    .interactableSlots(1)
                    .text("Map Name here")
                    .itemLeft(makeItem(Material.NAME_TAG, "Rename me", false))
                    .onClose(state -> {

                    })
                    .onClick((slot, state) -> {
                        List<Component> renameList = texts.getOrDefault(p, new ArrayList<>());
                        renameList.add(Component.text(state.getText()));

                        Bukkit.getScheduler().runTaskLater(BuildSystem.getInstance(), () -> {
                            state.getPlayer().getItemOnCursor().setType(Material.AIR);
                        }, 2);

                        texts.put(p, renameList);

                        if (renameList.size() >= 2) {
                            p.closeInventory();

                            return Collections.singletonList(AnvilGUI.ResponseAction.close());
                        }

                        if (renameList.size() == 1) {
                            return Collections.singletonList(AnvilGUI.ResponseAction.replaceInputText("Minigame Name here"));
                        }

                        state.getPlayer().sendMessage(Component.text("Created world with name: " + texts.get(p).getFirst()).color(NamedTextColor.GREEN));

                        return List.of();
                    });

            builder.open(playerMenuUtility.getOwner());
        } else if (e.getCurrentItem().getType().equals(Material.BARRIER) &&
                e.getCurrentItem().getPersistentDataContainer().has(new NamespacedKey(BuildSystem.getInstance(), "menuPart"))) {

            p.closeInventory();

        } else if (e.getCurrentItem().getType().equals(Material.DARK_OAK_BUTTON) &&
                e.getCurrentItem().getPersistentDataContainer().has(new NamespacedKey(BuildSystem.getInstance(), "menuPart"))) {
            if (ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName()).equalsIgnoreCase("Left")) {
                if (page == 0) {
                    p.sendMessage(ChatColor.GRAY + "You are already on the first page.");
                } else {
                    page = page - 1;
                    super.open();
                }
            } else if (ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName()).equalsIgnoreCase("Right") &&
                    e.getCurrentItem().getPersistentDataContainer().has(new NamespacedKey(BuildSystem.getInstance(), "menuPart"))) {
                if (!((index + 1) >= players.size())) {
                    page = page + 1;
                    super.open();
                } else {
                    p.sendMessage(ChatColor.GRAY + "You are on the last page.");
                }
            }
        }
    }

    @Override
    public void handleClose(InventoryCloseEvent e) {
    }

    ArrayList<String> worlds = new ArrayList<>();


    @Override
    public void setMenuItems() {

        addMenuBorder();

        loop();

        //The thing you will be looping through to place items
        // Pagination loop template
        if (!worlds.isEmpty()) {
            for (int i = 0; i < getMaxItemsPerPage(); i++) {
                index = getMaxItemsPerPage() * page + i;
                if (index >= worlds.size()) break;
                if (worlds.get(index) != null) {

                    inventory.addItem(makeItem(Material.GRASS_BLOCK, worlds.get(index), false));
                }
            }
        }
    }

    private void loop() {
        FileConfiguration config = BuildSystem.getConfiguration();

        for (String section : config.getKeys(false)) {
            if (config.isConfigurationSection(section)) {
                BuildSystem.getInstance().getLogger().info(section + ":");

                add(Objects.requireNonNull(config.getConfigurationSection(section)));
            }
        }
    }

    private void add(ConfigurationSection config) {
        for (String section : config.getKeys(false)) {
            if (config.isConfigurationSection(section)) {
                if (worlds.contains(section)) return;

                BuildSystem.getInstance().getLogger().info(section + ":");

                worlds.add(section);
            }
        }
    }
}
