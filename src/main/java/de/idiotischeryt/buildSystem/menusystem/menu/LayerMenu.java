package de.idiotischeryt.buildSystem.menusystem.menu;

import de.idiotischeryt.buildSystem.BuildSystem;
import de.idiotischeryt.buildSystem.menusystem.Menu;
import de.idiotischeryt.buildSystem.menusystem.PlayerMenuUtility;
import de.idiotischeryt.buildSystem.util.Layer;
import de.rapha149.signgui.exception.SignGUIVersionException;
import it.unimi.dsi.fastutil.Pair;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Biome;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.util.*;

//wenn man die numme drückt increased die und current wird im peristant datacontainer gesetzt (current anzahl also 1,2, 3...) und das wird dann geutzt um nummer zu increasen, bei 9 ist wieder 0
public class LayerMenu extends Menu {
    private final Menu menuBefore;
    private final int rows = 6;
    private final List<Pair<Integer, String>> numbers = new ArrayList<>();
    private final List<Layer> layers = new ArrayList<>();
    private final ItemStack arrowDown;
    private ItemStack arrow;
    private final @NotNull NamespacedKey layerKey;
    private final NamespacedKey key;
    private final NamespacedKey key1;
    private final NamespacedKey key2;
    private final NamespacedKey key3;

    private final String[] biomeName = new String[0];
    private final NamespacedKey key4;

    public LayerMenu(Menu menuBefore, PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
        this.menuBefore = menuBefore;

        this.inventory = Bukkit.createInventory(playerMenuUtility.getOwner(), getSlots(), getMenuName());

        layerKey = new NamespacedKey(BuildSystem.getInstance(), "Layer_Arrow_Key");
        key = new NamespacedKey(BuildSystem.getInstance(), "Biome_Object");
        key1 = new NamespacedKey(BuildSystem.getInstance(), "Button_Minus");
        key2 = new NamespacedKey(BuildSystem.getInstance(), "Button_Plus");
        key3 = new NamespacedKey(BuildSystem.getInstance(), "Increase_Or_Decrease1");
        key4 = new NamespacedKey(BuildSystem.getInstance(), "Increase_Or_Decrease2");

        layers.add(new Layer(Material.BEDROCK, layers.size(), 1));
        layers.add(new Layer(Material.DIRT, layers.size(), 2));
        layers.add(new Layer(Material.GRASS_BLOCK, layers.size(), 1));

        try {
            arrow = createPlayerHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzA0MGZlODM2YTZjMmZiZDJjN2E5YzhlYzZiZTUxNzRmZGRmMWFjMjBmNTVlMzY2MTU2ZmE1ZjcxMmUxMCJ9fX0=", "move down");
            arrow.editMeta(meta -> meta.getPersistentDataContainer().set(layerKey, PersistentDataType.INTEGER, 1));

            arrowDown = createPlayerHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzQzNzM0NmQ4YmRhNzhkNTI1ZDE5ZjU0MGE5NWU0ZTc5ZGFlZGE3OTVjYmM1YTEzMjU2MjM2MzEyY2YifX19", "move up");
            arrowDown.editMeta(meta -> meta.getPersistentDataContainer().set(layerKey, PersistentDataType.INTEGER, 0));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        putNumbers();
        updateLayer();
    }

    @Override
    public String getMenuName() {
        return "Layer Menu";
    }

    @Override
    public int getSlots() {
        return rows * 9;
    }

    private boolean isValid(@Nullable ItemStack item) {
        return item != null && item.getType() != Material.AIR;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        ItemStack clicked = e.getCurrentItem();
        if (!isValid(clicked)) return;

        if (clicked.getItemMeta().getPersistentDataContainer().has(key)) openBiomeUI(this);
        else if (clicked.getItemMeta().getPersistentDataContainer().has(key1)) rmLayer(e.getSlot());
        else if (clicked.getItemMeta().getPersistentDataContainer().has(key2)) addLayer();
        else if(clicked.getItemMeta().getPersistentDataContainer().has(layerKey))
            if(e.getClick() == ClickType.LEFT) moveUp(e.getSlot());
            else if(e.getClick() == ClickType.RIGHT) moveDown(e.getSlot());
        else if(clicked.getPersistentDataContainer().has(key3)) changeNumber(e.getSlot(), clicked.getPersistentDataContainer().get(key3, PersistentDataType.INTEGER), clicked,key3,true);
        else if(clicked.getPersistentDataContainer().has(key4)) changeNumber(e.getSlot(), clicked.getPersistentDataContainer().get(key4, PersistentDataType.INTEGER), clicked,key3,false);
    }

    private void changeNumber(int slot, int i, ItemStack stack, NamespacedKey containerKey, boolean increase) {
        int number = i;
        if(increase) number += 1;
        else number -= 1;
        ItemStack stack1 = null;

        if(number > 9) number = 0;

        try {
            stack1 = createPlayerHead(numbers.get(number).second(), String.valueOf(i));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        stack1.setItemMeta(stack.getItemMeta());

        int finalNumber = number;
        stack1.editMeta(itemMeta -> itemMeta.getPersistentDataContainer().set(containerKey, PersistentDataType.INTEGER, finalNumber));

        inventory.setItem(slot, stack1);
    }

    public void moveUp(int slot) {
        int index = rowFSlot(slot);

        if (index <= 0 || index >= layers.size()) return;

        Collections.swap(layers, index, index - 1);

        for (int i = 0; i < layers.size(); i++) {
            layers.get(i).setRowPos(i + 1);
        }

        updateLayer();
    }

    public void moveDown(int slot) {
        int index = rowFSlot(slot);

        if (index >= layers.size() - 1) return;

        Collections.swap(layers, index, index + 1);

        for (int i = 0; i < layers.size(); i++) {
            layers.get(i).setRowPos(i + 1);
        }

        updateLayer();
    }


    private void addLayer() {
        layers.add(new Layer(Material.GRASS_BLOCK, layers.size() + 1, 1));
        updateLayer();
    }

    private void rmLayer(int slot) {
        if (!layers.isEmpty()) {
            layers.remove(rowFSlot(slot));
            updateLayer();
        }
    }

    public int rowFSlot(int slot) {
        return slot / 9;
    }

    private void updateLayer() {
        inventory.clear();

        for (int row = 0; row < rows; row++) {
            int slot = row * 9;
            if(row == 1 || row == rows - 1) continue;

            inventory.setItem(slot + 2, makeItem(Material.BARRIER, "No layer yet!"));
            inventory.setItem(slot + 3, FILLER_GLASS);
            inventory.setItem(slot + 4, makeItem(Material.BARRIER, "No layer yet!"));
            inventory.setItem(slot + 5, FILLER_GLASS);
            inventory.setItem(slot + 6, makeItem(Material.BARRIER, "No layer yet!"));
            inventory.setItem(slot + 7, makeItem(Material.BARRIER, "No layer yet!"));
            inventory.setItem(slot + 8, FILLER_GLASS);
        }

        for (int i = 0; i < layers.size(); i++) {
            Layer layer = layers.get(i);
            int rowIndex = getRow(i);

            if (rowIndex < 0 || rowIndex >= rows) {
                Bukkit.getLogger().warning("[BuildSystem] Skipping layer row out of bounds: " + rowIndex);
                continue;
            }

            ItemStack block = new ItemStack(layer.getMaterial());
            setupLayerRow(block, rowIndex * 9, layer.getNumber());
        }
    }

    private void setupLayerRow(ItemStack block, int baseSlot, int number) {
        if (baseSlot < 0 || baseSlot + 7 >= inventory.getSize()) {
            Bukkit.getLogger().warning("[BuildSystem] Skipping setupLayerRow, baseSlot out of bounds: " + baseSlot);
            return;
        }

        inventory.setItem(baseSlot + 2, arrow);
        inventory.setItem(baseSlot + 4, block);

        int clampedNumber = Math.max(0, Math.min(99, number));
        int first = clampedNumber / 10;
        int second = clampedNumber % 10;

        try {
            ItemStack number1 = createPlayerHead(numbers.get(first).right(), String.valueOf(first));
            number1.editMeta(meta -> meta.getPersistentDataContainer().set(key3, PersistentDataType.INTEGER, first));

            ItemStack number2 = createPlayerHead(numbers.get(second).right(), String.valueOf(second));
            number2.editMeta(meta -> meta.getPersistentDataContainer().set(key4, PersistentDataType.INTEGER, second));

            inventory.setItem(baseSlot + 6, number1);
            inventory.setItem(baseSlot + 7, number2);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public int getRow(int rowIndex) {
        int result = rows - 1 - rowIndex;
        return (result >= 0 && result < rows) ? result : -1;
    }

    public void openBiomeUI(Menu current) {
        AnvilGUI.Builder builder = new AnvilGUI.Builder()
                .plugin(BuildSystem.getInstance())
                .interactableSlots(1)
                .text("Biome name here")
                .itemLeft(makeItem(Material.NAME_TAG, "Rename me", false))
                .onClose(state -> Bukkit.getScheduler().runTask(BuildSystem.getInstance(), current::open))
                .onClick((slot, state) -> {
                    String inputText = state.getText();
                    boolean isValidBiome = Arrays.stream(Biome.values()).anyMatch(b -> b.name().equalsIgnoreCase(inputText));

                    if (isValidBiome) {
                        Bukkit.getScheduler().runTaskLater(BuildSystem.getInstance(), () -> state.getPlayer().getItemOnCursor().setType(Material.AIR), 2);
                        biomeName[0] = inputText;
                        playerMenuUtility.getOwner().closeInventory();
                        return Collections.singletonList(AnvilGUI.ResponseAction.close());
                    } else {
                        return Arrays.asList(
                                AnvilGUI.ResponseAction.replaceInputText("Biome Name here"),
                                AnvilGUI.ResponseAction.updateTitle(ChatColor.RED + "Biome not found!", true)
                        );
                    }
                });

        builder.open(playerMenuUtility.getOwner());
    }

    @Override
    public void handleClose(InventoryCloseEvent e) {
        if (e.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;
        Bukkit.getScheduler().runTask(BuildSystem.getInstance(), menuBefore::open);
    }

    @Override
    public void setMenuItems() {
        ItemStack itemToPlace = makeItem(Material.GRASS_BLOCK, "lol", key);

        ItemStack plusHead;
        try {
            plusHead = createPlayerHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2VkZDIwYmU5MzUyMDk0OWU2Y2U3ODlkYzRmNDNlZmFlYjI4YzcxN2VlNmJmY2JiZTAyNzgwMTQyZjcxNiJ9fX0=", ChatColor.GREEN + "Create");
            plusHead.editMeta(meta -> meta.getPersistentDataContainer().set(key2, PersistentDataType.INTEGER, 1));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        ItemStack minusHead;
        try {
            minusHead = createPlayerHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ4YTk5ZGIyYzM3ZWM3MWQ3MTk5Y2Q1MjYzOTk4MWE3NTEzY2U5Y2NhOTYyNmEzOTM2Zjk2NWIxMzExOTMifX19", ChatColor.RED + "Remove");
            minusHead.editMeta(meta -> meta.getPersistentDataContainer().set(key1, PersistentDataType.INTEGER, 0));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        animate(inventory.getSize(), new Material[]{
                Material.GRASS_BLOCK, Material.SAND, Material.RED_SAND, Material.ICE,
                Material.MYCELIUM, Material.PODZOL, Material.NETHERRACK, Material.BASALT,
                Material.WARPED_NYLIUM, Material.CRIMSON_NYLIUM
        }, itemToPlace, 14);

        inventory.setItem(4 * 9, plusHead);
        inventory.setItem(5 * 9, minusHead);

        addMenuBorder();
    }

    public void putNumbers() {
        numbers.add(Pair.of(0, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjA3MjNjMWVmM2U2ZTUxZWM0ZmQzOTZiNmJhMzg5NGE3Njc0YmU5MzVmNmVkY2E1ODNmYjU4Y2M5NGRmODE4NCJ9fX0="));
        numbers.add(Pair.of(1, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODZiYzE4NzcyYWNkMzQzZTZhYjIwMTAzYzRhOWU0MjExNWYyMjQ4NDI3ZWNiZDIwNjcxZDZjZDI0ZWU3ZWY4YyJ9fX0="));
        numbers.add(Pair.of(2, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWI3ODFjOTUzZjg1ZGY3NzVmOTdjNjRmZWYyZjUwYjczMTM2NjI1NjgyN2UwZjg4YzIyOTcwYzIzMzE3YzY5NSJ9fX0="));
        numbers.add(Pair.of(3, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTk0ZGM4ZWU0ODJiMTkxN2JkZWQ2Yjk1MjM5ZDAwODY2MzZkNWI2YjA3MGQ3MWRkNWViNWY0YjI3NWQ2OTNiMCJ9fX0="));
        numbers.add(Pair.of(4, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzc2NTc5YTQxZDU3OWExZmUxN2RkZTg4Zjg5ZmE4Mzc3NmRlNzE2YmM5ZjI0ZGEyN2YyMTFiZTlmZTIxOWFmZSJ9fX0="));
        numbers.add(Pair.of(5, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTEwYTZmY2MyMjRkYzg1YjNlNzI1YWUyYjg2ZmQ1ZmRhNGUyNGMwMDQ3NTUzMDdjZDNmZGNlNGFlNmRhYTFmMSJ9fX0="));
        numbers.add(Pair.of(6, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmExZDgxNGExMTZiNGI1MGQ0ODYyZTVjZTUzZDFhNDExYjhiYTJhNmYxOTUzMWIzYWUyMmNlZDdmNzA5ZTJhZCJ9fX0="));
        numbers.add(Pair.of(7, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTY5YTdmNTZlMWJmZWIwZDU5ZjU4OWUzNzc0MDZlYTViMWQxYzFkYzFjNDA4Nzk0ZDAzNWY3M2NmODhiNjdmNSJ9fX0="));
        numbers.add(Pair.of(8, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjU4NjlmNjJhYmM1ZGM3M2NlNzZiMGQ3OTAxMzZlNzQ3MTNhYTIxNWRmYWNmMWRmNzMxNzVjMTkxMDJlNzYzYiJ9fX0="));
        numbers.add(Pair.of(9, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzVlOWRlNDBmNjdiM2YyN2Y4ZGE5Y2I0NDYwMGIyM2U4NTBkNWVkYTYxZDVjMzgyNTU1NGM1ZWQ1ODk5MGU0MSJ9fX0="));
    }
}
