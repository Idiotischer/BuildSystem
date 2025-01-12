package de.idiotischeryt.buildSystem.menusystem.menu;

import de.idiotischeryt.buildSystem.BuildSystem;
import de.idiotischeryt.buildSystem.menusystem.Menu;
import de.idiotischeryt.buildSystem.menusystem.PlayerMenuUtility;
import de.rapha149.signgui.exception.SignGUIVersionException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.function.Consumer;

public class ToggleMenu extends Menu {

    private final NamespacedKey daylightKey;
    private final NamespacedKey weatherKey;

    BuildSettingsMenu menu;

    private final Consumer<Boolean> callback;
    private final Consumer<Boolean> callback1;

    public ToggleMenu(PlayerMenuUtility playerMenuUtility, BuildSettingsMenu menu, NamespacedKey key, NamespacedKey key1, Consumer<Boolean> callback, Consumer<Boolean> callback1) {
        super(playerMenuUtility);
        this.daylightKey = key;
        this.weatherKey = key1;

        this.callback = callback;
        this.callback1 = callback1;

        this.menu = menu;
    }

    @Override
    public String getMenuName() {
        return "Do daylight and/or weather change?";
    }

    @Override
    public int getSlots() {
        return 5 * 9;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        if (e.getClickedInventory() != inventory) return;

        e.setCancelled(true);

        if (e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta()) return;

        ItemMeta meta = e.getCurrentItem().getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        if (container.has(daylightKey, PersistentDataType.BOOLEAN)) {
            toggleBoolean(e, daylightKey, "DaylightCycle", 21);
        } else if (container.has(weatherKey, PersistentDataType.BOOLEAN)) {
            toggleBoolean(e, weatherKey, "WeatherCycle", 23);
        }
    }

    private void toggleBoolean(InventoryClickEvent e, NamespacedKey key, String displayName, int slot) {
        ItemMeta meta = e.getCurrentItem().getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        boolean currentValue = Boolean.TRUE.equals(container.get(key, PersistentDataType.BOOLEAN));

        ItemStack stack = currentValue
                ? createBooleanItem(Material.RED_STAINED_GLASS_PANE, displayName + ": False", key, false)
                : createBooleanItem(Material.LIME_STAINED_GLASS_PANE, displayName + ": True", key, true);

        if (key.equals(daylightKey)) {
            callback.accept(!currentValue);
        } else if (key.equals(weatherKey)) {
            callback1.accept(!currentValue);
        }

        inventory.setItem(slot, stack);
    }

    private ItemStack createBooleanItem(Material material, String displayName, NamespacedKey key, boolean value) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD.toString() + ChatColor.BOLD + displayName);
        meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, value);
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public void handleClose(InventoryCloseEvent e) {
        if (e.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;

        Bukkit.getScheduler().runTask(BuildSystem.getInstance(), () -> {
            try {
                menu.open();
            } catch (SignGUIVersionException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    @Override
    public void setMenuItems() {
        this.FILLER_GLASS = makeItem(Material.GRAY_STAINED_GLASS_PANE, " ", false);

        inventory.setItem(21, createBooleanItem(Material.RED_STAINED_GLASS_PANE,
                "DaylightCycle: False", daylightKey, false));

        inventory.setItem(23, createBooleanItem(Material.RED_STAINED_GLASS_PANE,
                "WeatherCycle: False", weatherKey, false));

        setFillerGlass();
    }
}
