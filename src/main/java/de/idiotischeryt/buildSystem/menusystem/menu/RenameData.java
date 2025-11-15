package de.idiotischeryt.buildSystem.menusystem.menu;

import de.idiotischeryt.buildSystem.menusystem.Menu;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Consumer;

public class RenameData {
    private final Player player;
    private final Menu menu;
    private final String label;
    private final String defaultText;
    private final String[] map;
    private final List<String> needed;
    private final String errorText;
    private final boolean contains;
    private final boolean emptyAllowed;
    private final boolean allowEndWith;
    private final List<String> endWith;
    private final Consumer<String> onFinish;
    private final AnvilMenu thisMenu;

    public RenameData(Player player, Menu menu, String label, String defaultText,
                      String[] map, List<String> needed, String errorText,
                      boolean contains, boolean emptyAllowed, boolean allowEndWith,
                      List<String> endWith, AnvilMenu thisMenu) {
        this.player = player;
        this.menu = menu;
        this.label = label;
        this.defaultText = defaultText;
        this.map = map;
        this.needed = needed;
        this.errorText = errorText;
        this.contains = contains;
        this.emptyAllowed = emptyAllowed;
        this.allowEndWith = allowEndWith;
        this.endWith = endWith;
        this.thisMenu = thisMenu;

        this.onFinish = input -> {
            map[0] = input;
        };
    }

    public Player getPlayer() {
        return player;
    }

    public Menu getMenu() {
        return menu;
    }

    public String getLabel() {
        return label;
    }

    public String getDefaultText() {
        return defaultText;
    }

    public String[] getMap() {
        return map;
    }

    public List<String> neededList() {
        return needed;
    }

    public List<String> endWithList() {
        return endWith;
    }

    public boolean contains() {
        return contains;
    }

    public boolean emptyAllowed() {
        return emptyAllowed;
    }

    public boolean allowEndWith() {
        return allowEndWith;
    }

    public String getErrorText() {
        return errorText;
    }

    public Consumer<String> getOnFinish() {
        return onFinish;
    }

    public AnvilMenu getThisMenu() {
        return thisMenu;
    }

    public List<String> getEndWith() {
        return endWith;
    }

    public List<String> getNeeded() {
        return needed;
    }
}
