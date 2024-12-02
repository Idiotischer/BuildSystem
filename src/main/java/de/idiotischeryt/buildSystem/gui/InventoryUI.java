package de.idiotischeryt.buildSystem.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class InventoryUI implements Listener {

    private static final int ROWS = 6;
    private static final int INVENTORY_SIZE = 9 * ROWS;


    public InventoryUI() {
    }


    public Inventory fillBorder(Inventory inventory, ItemStack stack) {
        for (int row = 0; row < ROWS; row++) {
            int startIndex = row * 9;
            for (int slot = startIndex; slot < startIndex + 9; slot++) {
                if (row == 0 || row == ROWS - 1 || slot == startIndex || slot == startIndex + 8) {
                    inventory.setItem(slot, stack);
                }
            }
        }
        return inventory;
    }

    public void openInv(Player p) {
    }

    public ItemStack setupItem(Material material, Component displayName, boolean enchanted) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();

        if (meta != null) {
            meta.displayName(displayName);
            stack.setItemMeta(meta);

            stack.addItemFlags(
                    ItemFlag.HIDE_ATTRIBUTES,
                    ItemFlag.HIDE_ENCHANTS,
                    ItemFlag.HIDE_UNBREAKABLE,
                    ItemFlag.HIDE_PLACED_ON,
                    ItemFlag.HIDE_DESTROYS,
                    ItemFlag.HIDE_ARMOR_TRIM,
                    ItemFlag.HIDE_ADDITIONAL_TOOLTIP
            );
            if (enchanted) {
                stack.addUnsafeEnchantment(Enchantment.LURE, 1);
            }
        }
        return stack;
    }
    
}
