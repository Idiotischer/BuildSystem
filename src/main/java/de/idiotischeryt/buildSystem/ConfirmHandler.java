package de.idiotischeryt.buildSystem;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ConfirmHandler {
    private static final Map<UUID, Long> pending = new HashMap<>();

    public static void setPending(UUID uuid) {
        pending.put(uuid, System.currentTimeMillis());
    }

    public static boolean isPending(UUID uuid) {
        Long time = pending.get(uuid);
        return time != null && (System.currentTimeMillis() - time) < 10_000; // 10s timeout
    }

    public static void clear(UUID uuid) {
        pending.remove(uuid);
    }

    public static void executeKill(Player player) {
        World world = player.getWorld();
        int count = 0;
        for (Entity e : world.getEntities()) {
            if (!(e instanceof Player)) {
                e.remove();
                count++;
            }
        }
        player.sendMessage(Component.text("✔ Removed " + count + " entities from world: " + world.getName())
            .color(NamedTextColor.GREEN));
    }
}
