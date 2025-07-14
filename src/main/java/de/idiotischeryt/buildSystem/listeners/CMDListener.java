package de.idiotischeryt.buildSystem.listeners;

import de.idiotischeryt.buildSystem.BuildSystem;
import de.idiotischeryt.buildSystem.ConfirmHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Locale;

public class CMDListener implements Listener {
    @EventHandler
    public void onKill(PlayerCommandPreprocessEvent event) {
        if(!BuildSystem.getInstance().getProperties().contains("safeKillAll"))  return;
        if(!BuildSystem.getInstance().getProperties().getBoolean("safeKillAll")) return;

        String msg = event.getMessage().toLowerCase(Locale.ROOT).trim();
        Player player = event.getPlayer();

        if (msg.startsWith("/kill @e")) {
            event.setCancelled(true);

            if (!ConfirmHandler.isPending(player.getUniqueId())) {
                ConfirmHandler.setPending(player.getUniqueId());

                String selector = msg.substring("/kill ".length());

                player.sendMessage(Component.text("⚠ Confirm kill for selector: " + selector)
                        .color(NamedTextColor.RED)
                        .append(Component.text(" [Click to Confirm]")
                                .color(NamedTextColor.YELLOW)
                                .clickEvent(ClickEvent.runCommand("/confirmkill " + selector))));
            } else {
                player.sendMessage(Component.text("✔ You're already confirming. Click again!").color(NamedTextColor.GRAY));
            }
        }
    }
}
