package de.idiotischeryt.buildSystem;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandSendEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ConfirmHandler {
    private static final Map<UUID, Long> pending = new HashMap<>();

    public static void setPending(UUID uuid) {
        pending.put(uuid, System.currentTimeMillis());
    }

    public static boolean isPending(UUID uuid) {
        Long time = pending.get(uuid);
        return time != null && (System.currentTimeMillis() - time) < 10_000;
    }

    public static void clear(UUID uuid) {
        pending.remove(uuid);
    }

    public static void executeKill(Player player, String selector) {
        List<Entity> toKill = SelectorUtil.resolveSelector(player, selector);

        for (Entity entity : toKill) {
            entity.remove();
        }

        player.sendMessage(Component.text("✔ Removed " + toKill.size() + " entities using selector: " + selector)
                .color(NamedTextColor.GREEN));

    }

    public static void executeTp(Player player, String selector) {
        List<Entity> toTPp = SelectorUtil.resolveSelector(player, selector);

        for (Entity entity : toTPp) {
            entity.teleport(entity);
        }

        player.sendMessage(Component.text("✔ Teleported " + toTPp.size() + " entities using selector: " + selector)
                .color(NamedTextColor.GREEN));
    }
}
