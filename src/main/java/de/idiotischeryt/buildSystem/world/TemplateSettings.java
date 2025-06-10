package de.idiotischeryt.buildSystem.world;

import org.bukkit.Material;
import org.bukkit.block.Biome;

public record TemplateSettings(boolean empty, boolean spawnMobs, boolean dayNightCycle, Biome biome,
                               Material worldMaterial) {
}
