package de.idiotischeryt.buildSystem.menusystem;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.idiotischeryt.buildSystem.BuildSystem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/*
    Defines the behavior and attributes of all menus in our plugin
    credits kody simpson :)
    to lazy to code my own :)
 */
public abstract class Menu implements InventoryHolder {

    //Protected values that can be accessed in the menus
    protected static PlayerMenuUtility playerMenuUtility;
    public Inventory inventory;
    public ItemStack FILLER_GLASS = makeItem(Material.LIME_STAINED_GLASS_PANE, " ", false);

    //Constructor for Menu. Pass in a PlayerMenuUtility so that
    // we have information on who's menu this is and
    // what info is to be transfered
    public Menu(PlayerMenuUtility playerMenuUtility) {
        Menu.playerMenuUtility = playerMenuUtility;
    }

    //let each menu decide their name
    public abstract String getMenuName();

    //let each menu decide their slot amount
    public abstract int getSlots();

    //let each menu decide how the items in the menu will be handled when clicked
    public abstract void handleMenu(InventoryClickEvent e);

    public void handleClose(InventoryCloseEvent e) {
        playerMenuUtility.menu = null;
        playerMenuUtility.lastMenu = this;
    }

    //let each menu decide what items are to be placed in the inventory menu
    public abstract void setMenuItems();

    //When called, an inventory is created and opened for the player
    public void open() {
        //The owner of the inventory created is the Menu itself,
        // so we are able to reverse engineer the Menu object from the
        // inventoryHolder in the MenuListener class when handling clicks
        inventory = Bukkit.createInventory(this, getSlots(), getMenuName());

        //grab all the items specified to be used for this menu and add to inventory
        this.setMenuItems();

        //open the inventory for the player
        playerMenuUtility.getOwner().openInventory(inventory);

        playerMenuUtility.menu = this;
    }

    public void animate(int slot, Material @NotNull [] mats, ItemStack toChange, int changeMaterialSpeed) {
        if (mats.length == 0) {
            throw new IllegalArgumentException("Material array cannot be empty");
        }

        AtomicInteger currentIndex = new AtomicInteger(0);

        Bukkit.getScheduler().runTaskTimer(BuildSystem.getInstance(), () -> {

            ItemStack itemStack = new ItemStack(mats[currentIndex.get()]);

            itemStack.setItemMeta(toChange.getItemMeta().clone());

            getInventory().setItem(slot, itemStack);

            currentIndex.set((currentIndex.get() + 1) % mats.length);
        }, 0, changeMaterialSpeed);

    }

    public void animateXChangeName(int slot, Material @NotNull [] mats, ItemStack toChange, int changeMaterialSpeed, String[] strings) {
        if (mats.length == 0) {
            throw new IllegalArgumentException("Material array cannot be empty");
        }
        if (strings.length == 0) {
            throw new IllegalArgumentException("Strings array cannot be empty");
        }

        AtomicInteger currentIndex = new AtomicInteger(0);
        AtomicInteger currentIndex1 = new AtomicInteger(0);

        Bukkit.getScheduler().runTaskTimer(BuildSystem.getInstance(), () -> {

            ItemStack itemStack = new ItemStack(mats[currentIndex.get()]);

            ItemMeta meta = toChange.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(strings[currentIndex1.get()]);
                itemStack.setItemMeta(meta);
            }

            getInventory().setItem(slot, itemStack);

            currentIndex.set((currentIndex.get() + 1) % mats.length);
            currentIndex1.set((currentIndex1.get() + 1) % strings.length);
        }, 0, changeMaterialSpeed);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void setFillerGlass() {
        for (int i = 0; i < getSlots(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, FILLER_GLASS);
            }
        }
    }

    public void placeInMiddle(ItemStack middleItem) {
        int inventorySize = getSlots();
        int middleIndex = inventorySize / 2;

        if (inventorySize % 2 != 0) {
            if (inventory.getItem(middleIndex) == null || inventory.getItem(middleIndex).getType() == Material.AIR) {
                inventory.setItem(middleIndex, middleItem);
            }
        } else {
            int index1 = middleIndex - 1;

            if ((inventory.getItem(index1) == null || inventory.getItem(index1).getType() == Material.AIR) &&
                    (inventory.getItem(middleIndex) == null || inventory.getItem(middleIndex).getType() == Material.AIR)) {
                inventory.setItem(index1, middleItem);
                inventory.setItem(middleIndex, middleItem);
            }
        }
    }


    public static @NotNull ItemStack makeItem(Material material, String displayName, boolean menuPart, NamespacedKey k, String... lore) {

        return makeItem(material, displayName, menuPart, k, true, lore);
    }

    public static @NotNull ItemStack makeItem(Material material, String displayName, boolean menuPart, NamespacedKey k, boolean standard, String... lore) {

        ItemStack item = new ItemStack(material);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(displayName);

        itemMeta.setLore(Arrays.asList(lore));

        if (menuPart) {
            itemMeta.getPersistentDataContainer().set(
                    new NamespacedKey(BuildSystem.getInstance(), "menuPart"),
                    PersistentDataType.BOOLEAN,
                    true);
        }

        itemMeta.getPersistentDataContainer().set(k, PersistentDataType.BOOLEAN, standard);

        item.setItemMeta(itemMeta);

        return item;
    }

    public static ItemStack makeItem(Material material, String displayName, boolean menuPart, String... lore) {

        ItemStack item = new ItemStack(material);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(displayName);

        itemMeta.setLore(Arrays.asList(lore));

        if (menuPart) {
            itemMeta.getPersistentDataContainer().set(
                    new NamespacedKey(BuildSystem.getInstance(), "menuPart"),
                    PersistentDataType.BOOLEAN,
                    true);
        }

        item.setItemMeta(itemMeta);

        return item;
    }

    public ItemStack makeItem(Material material, String displayName, NamespacedKey addKey, String... lore) {

        return makeItem(material, displayName, true, addKey, lore);
    }


    public ItemStack makeItem(Material material, String displayName, String... lore) {

        return makeItem(material, displayName, true, lore);
    }

    public ItemStack createPlayerHead(String textureValue, String displayName, String... lore) throws MalformedURLException {
        return createPlayerHead(textureValue, displayName, true, lore);
    }

    public ItemStack createPlayerHead(String textureValue, String displayName, boolean menuPart, String... lore) throws MalformedURLException {
        String url = extractUrl(textureValue);

        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);

        if (itemStack.getItemMeta() instanceof SkullMeta skullMeta) {
            var playerProfile = Bukkit.createPlayerProfile(UUID.randomUUID());
            playerProfile.getTextures().setSkin(URI.create(url).toURL());
            skullMeta.setOwnerProfile(playerProfile);

            skullMeta.setDisplayName(displayName);

            skullMeta.setLore(Arrays.asList(lore));

            if (menuPart) {
                skullMeta.getPersistentDataContainer().set(
                        new NamespacedKey(BuildSystem.getInstance(), "menuPart"),
                        PersistentDataType.BOOLEAN,
                        true);
            }

            itemStack.setItemMeta(skullMeta);
        }
        return itemStack;
    }

    public String extractUrl(String base64) {
        byte[] decodedBytes = Base64.getDecoder().decode(base64);
        String decodedString = new String(decodedBytes);

        JsonObject jsonObject = JsonParser.parseString(decodedString).getAsJsonObject();
        return jsonObject.getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString();
    }

}
