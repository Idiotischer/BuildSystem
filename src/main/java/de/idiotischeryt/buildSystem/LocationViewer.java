package de.idiotischeryt.buildSystem;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class LocationViewer {

    private final int height = 80;
    private final float distanceBetween = 0.4F;

    private static BukkitTask task = null;
    private static final Map<Player, Map<File, String>> playersNLocations = new HashMap<>();
    private static final Map<Player, Particle.DustOptions> dusts = new HashMap<>();
    private final float size = 0.3F;

    public LocationViewer() {
        if (task == null) {
            task = Bukkit.getScheduler().runTaskTimer(BuildSystem.getInstance(), new Runnable() {
                private int currentHeight = 0;

                @Override
                public void run() {
                    if (currentHeight > height) {
                        currentHeight = 0;
                        return;
                    }

                    for (Map.Entry<Player, Map<File, String>> entry : playersNLocations.entrySet()) {
                        Player player = entry.getKey();
                        if (!player.isOnline()) {
                            playersNLocations.remove(player);
                            continue;
                        }

                        for (Map.Entry<File, String> entry1 : entry.getValue().entrySet()) {
                            for (Location baseLocation : BuildManager.getLocations(entry1.getKey(), player, entry1.getValue())) {
                                Particle.DustOptions opt = dusts.computeIfAbsent(player, k -> new Particle.DustOptions(genUniqueColor(), 0.6F));
                                summonCircle(baseLocation, size, opt);
                            }
                        }
                    }

                    currentHeight++;
                }
            }, 0, 2);
        }
    }

    private void summonCircle(Location location, float size, Particle.DustOptions dustOptions) {
        for (int d = 0; d <= 360; d += 10) {
            Location particleLoc = location.clone();
            particleLoc.add(Math.cos(Math.toRadians(d)) * size, 0, Math.sin(Math.toRadians(d)) * size);
            location.getWorld().spawnParticle(Particle.DUST, particleLoc, 1, dustOptions);

            for (int currentHeight = 1; currentHeight <= height; currentHeight++) {
                Location upRay = particleLoc.clone().add(0, currentHeight * distanceBetween, 0);
                Location downRay = particleLoc.clone().add(0, -currentHeight * distanceBetween, 0);
                location.getWorld().spawnParticle(Particle.DUST, upRay, 1, dustOptions);
                location.getWorld().spawnParticle(Particle.DUST, downRay, 1, dustOptions);
            }
        }
    }

    public static void show(Player player, Map<File, String> fileMap) {
        playersNLocations.put(player, fileMap);
    }

    public static void hide(Player player) {
        playersNLocations.remove(player);
        dusts.remove(player);
    }

    private static Color genUniqueColor() {
        var ref = new Object() {
            Color randomColor;
        };

        do {
            ref.randomColor = Color.fromRGB(
                    (int) (Math.random() * 255),
                    (int) (Math.random() * 255),
                    (int) (Math.random() * 255)
            );
        } while (dusts.values().stream().anyMatch(opt -> opt.getColor().equals(ref.randomColor)));

        return ref.randomColor;
    }
}
