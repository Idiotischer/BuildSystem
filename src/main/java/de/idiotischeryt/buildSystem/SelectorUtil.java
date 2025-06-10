package de.idiotischeryt.buildSystem;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;

public class SelectorUtil {
    public static List<Entity> resolveSelector(Player player, String selector) {
        try {
            List<Entity> entities = Bukkit.selectEntities(player, selector);
            return entities.stream()
                    .filter(e -> e.getWorld().equals(player.getWorld()))
                    .filter(e -> !(e instanceof Player))
                    .toList();
        } catch (IllegalArgumentException e) {
            player.sendMessage(Component.text("Invalid selector: " + selector, NamedTextColor.RED));
            return List.of();
        }
    }

}
