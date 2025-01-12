package de.idiotischeryt.buildSystem.menusystem.menu;

import de.idiotischeryt.buildSystem.BuildSystem;
import de.idiotischeryt.buildSystem.menusystem.Menu;
import de.idiotischeryt.buildSystem.menusystem.PlayerMenuUtility;
import de.rapha149.signgui.exception.SignGUIVersionException;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.function.Consumer;

public class BooleanMenu extends Menu {
    private final String title;
    private final NamespacedKey booleanKey;
    private final Consumer<Boolean> callback;
    private final BuildSettingsMenu menu;
    private boolean currentValue;

    public BooleanMenu(PlayerMenuUtility playerMenuUtility, BuildSettingsMenu menu, String title, NamespacedKey booleanKey, boolean initialValue, Consumer<Boolean> callback) {
        super(playerMenuUtility);
        this.title = title;
        this.booleanKey = booleanKey;
        this.currentValue = initialValue;
        this.callback = callback;
        this.menu = menu;
    }

    @Override
    public String getMenuName() {
        return title;
    }

    @Override
    public int getSlots() {
        return 5 * 9;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        if (e.getClickedInventory() != inventory) return;

        e.setCancelled(true);

        if (e.getCurrentItem() == null) return;

        if (e.getCurrentItem().getItemMeta().getPersistentDataContainer().has(booleanKey, PersistentDataType.BOOLEAN)) {
            currentValue = !currentValue;

            ItemStack stack = currentValue
                    ? makeItem(Material.LIME_STAINED_GLASS_PANE, "True", true, booleanKey, true)
                    : makeItem(Material.RED_STAINED_GLASS_PANE, "False", true, booleanKey, false);

            inventory.setItem(e.getSlot(), stack);

            callback.accept(currentValue);
        }
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
        ItemStack initialItem = currentValue
                ? makeItem(Material.LIME_STAINED_GLASS_PANE, "True", true, booleanKey, true)
                : makeItem(Material.RED_STAINED_GLASS_PANE, "False", true, booleanKey, false);

        inventory.setItem(22, initialItem);
    }
}
