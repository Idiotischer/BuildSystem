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
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class BooleanMenuExtended extends BooleanMenu {

    boolean normalGen;
    
    LayerMenu layerMenu;
    
    public BooleanMenuExtended(PlayerMenuUtility playerMenuUtility, BuildSettingsMenu menu, String title, NamespacedKey booleanKey, boolean initialValue, Consumer<Boolean> callback) {
        super(playerMenuUtility, menu, title, booleanKey, initialValue, callback);
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
        } else if(e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.GRASS_BLOCK) {
            //hier alle items außer dieses so auf barrier machen und dann nd mehr clickable
            this.normalGen = !this.normalGen;
        } else if(e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.BOOK) {
            this.layerMenu = new LayerMenu(this, playerMenuUtility);
            layerMenu.open();
        }
    }

    @Override
    public void setMenuItems() {
        ItemStack renameItem = makeItem(Material.BOOK, "Generator Settings");

        ItemStack normalModeItem = makeItem(Material.GRASS_BLOCK, "NormalMode");

        ItemStack initialItem = this.currentValue
                ? makeItem(Material.LIME_STAINED_GLASS_PANE, "True", true, booleanKey, true)
                : makeItem(Material.RED_STAINED_GLASS_PANE, "False", true, booleanKey, false);

        //guck mal ob so gut aussieht mit center usw
        inventory.setItem(20, initialItem);
        inventory.setItem(22, renameItem);
        inventory.setItem(24, normalModeItem); // when press dann alle anderen barrier und später wird ne normal world generated
    }
}
