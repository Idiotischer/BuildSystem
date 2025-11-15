package de.idiotischeryt.buildSystem.listeners;

import de.idiotischeryt.buildSystem.BuildSystem;
import de.idiotischeryt.buildSystem.ConfirmHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.io.IOException;
import java.util.Locale;

public class CMDListener implements Listener {
    @EventHandler
    public void onKill(ServerCommandEvent event) {
        try {
            BuildSystem.getInstance().getProperties().load(BuildSystem.getInstance().propertiesPath.toFile());
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }

        if(!BuildSystem.getInstance().getProperties().contains("safeCommandExecute"))  return;
        if(!BuildSystem.getInstance().getProperties().getBoolean("safeCommandExecute")) return;
        String msg = event.getCommand().toLowerCase(Locale.ROOT).trim();
        CommandSender sender = event.getSender();

        if (msg.contains("kill") && msg.contains("@e")) {
            event.setCancelled(true);


            if(!(sender instanceof Player player)) return;
            if (!ConfirmHandler.isPending(player.getUniqueId())) {
                ConfirmHandler.setPending(player.getUniqueId());

                String selector = msg.substring("/kill ".length());


                player.sendMessage(
                        Component.text("⚠ Confirm kill for selector: " + selector)
                                .color(NamedTextColor.RED)
                                .append(
                                        Component.text(" [Click to Confirm]")
                                                .color(NamedTextColor.YELLOW)
                                                .clickEvent(ClickEvent.callback(audience -> {
                                                    if (audience instanceof Player p) {
                                                        ConfirmHandler.executeKill(p, selector);
                                                        p.sendMessage(Component.text("✔ Kill confirmed for selector: " + selector).color(NamedTextColor.GREEN));
                                                    }
                                                }))
                                )
                );
            } else {
                player.sendMessage(Component.text("✔ You're already confirming. Click again!").color(NamedTextColor.GRAY));
            }
        } else if ((msg.contains("tp") || msg.contains("teleport")) && msg.contains("@e")) {
            event.setCancelled(true);
            if(!(sender instanceof Player player)) return;
            if (!ConfirmHandler.isPending(player.getUniqueId())) {
                ConfirmHandler.setPending(player.getUniqueId());

                String selector = msg.substring("/tp ".length());


                player.sendMessage(
                        Component.text("⚠ Confirm teleport for selector: " + selector)
                                .color(NamedTextColor.RED)
                                .append(
                                        Component.text(" [Click to Confirm]")
                                                .color(NamedTextColor.YELLOW)
                                                .clickEvent(ClickEvent.callback(audience -> {
                                                    if (audience instanceof Player p) {
                                                        ConfirmHandler.executeTp(p, selector);
                                                        p.sendMessage(Component.text("✔ Teleport confirmed for selector: " + selector).color(NamedTextColor.GREEN));
                                                    }
                                                }))
                                )
                );
            }

        }
    }
}
