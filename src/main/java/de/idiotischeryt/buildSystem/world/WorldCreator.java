package de.idiotischeryt.buildSystem.world;

import de.idiotischeryt.buildSystem.BuildSystem;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import java.util.Objects;

public class WorldCreator {

    public static World createWorld(Player p, String worldName, String minigame, boolean empty, boolean spawnMobs, boolean dayNightCycle, boolean weatherCycle, Biome biome) {
        org.bukkit.WorldCreator creator = new org.bukkit.WorldCreator(worldName + "-" + minigame);
        if (empty) {

            //creator.generator(new ChunkGenerator() {
            //    @Override
            //    public @NotNull ChunkData generateChunkData(@NotNull World world, @NotNull Random random, int x, int z, @NotNull BiomeGrid biome) {
            //        return super.generateChunkData(world, random, x, z, biome);
            //    }
            //});
//
            //creator.generateStructures(false);
            //creator.biomeProvider(new BiomeProvider() {
            //    @Override
            //    public @NotNull Biome getBiome(@NotNull WorldInfo worldInfo, int i, int i1, int i2) {
            //        if (biome == null) return Biome.PLAINS;
//
            //        return biome;
            //    }
//
            //    @Override
            //    public @NotNull List<Biome> getBiomes(@NotNull WorldInfo worldInfo) {
            //        if (biome == null) return List.of(Biome.PLAINS);
//
            //        return List.of(biome);
            //    }
            //});


            creator.type(WorldType.FLAT);

            String lowerCase = Objects.requireNonNullElse(biome, Biome.PLAINS).toString().toLowerCase();

            creator.generatorSettings("{\"biome\":\"minecraft:" + lowerCase + "\",\"layers\":[{\"block\":\"minecraft:air\",\"height\":4}]}");

            creator.generateStructures(false);

            World world = creator.createWorld();
            world.setTime(1000);

            world.getBlockAt(0, 64, 0).setType(Material.BEDROCK);

            world.setSpawnLocation(0, 65, 0);


            if (!spawnMobs) {
                world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
            }

            if (!dayNightCycle) {
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            }

            if (!weatherCycle) {
                world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            }

            teleport(p, world);

            return world;
        }

        creator.type(WorldType.FLAT);

        String lowerCase = Objects.requireNonNullElse(biome, Biome.PLAINS).toString().toLowerCase();

        creator.generatorSettings("{\"biome\":\"minecraft:" + lowerCase + "\",\"layers\":[{\"block\":\"minecraft:bedrock\",\"height\":1},{\"block\":\"minecraft:dirt\",\"height\":127},{\"block\":\"minecraft:grass_block\",\"height\":1}]}");

        creator.generateStructures(false);

        World world = creator.createWorld();
        world.setTime(1000);

        world.setSpawnLocation(0, 65, 0);

        if (world == null) {
            BuildSystem.sendError("Map not found:", worldName, "");

            return null;
        }


        if (!spawnMobs) {
            world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        }

        if (!dayNightCycle) {
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        }

        if (!weatherCycle) {
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        }

        teleport(p, world);

        return world;
    }

    public static void teleport(Player p, World world) {
        p.teleport(world.getSpawnLocation());
    }

    public static void teleport(Player p, String worldName) {
        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            BuildSystem.sendError("Map not found:", worldName, "");
            return;
        }

        teleport(p, world);
    }

}
