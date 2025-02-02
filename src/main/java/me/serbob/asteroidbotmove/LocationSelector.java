package me.serbob.asteroidbotmove;

import me.serbob.asteroidapi.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.util.Random;

public class LocationSelector {
    private final Random random = new Random();

    public RandomLoc getRandomLocation(FileConfiguration config, int loopNumber) {
        ConfigurationSection locationConfig = config.getConfigurationSection("loop." + loopNumber + ".location");
        if (locationConfig == null) {
            throw new IllegalArgumentException("No location configuration found for loop " + loopNumber);
        }

        double totalChance = locationConfig.getKeys(false).stream()
                .mapToDouble(key -> locationConfig.getDouble(key + ".chance", 0.0))
                .sum();

        if (Math.abs(totalChance - 100.0) > 0.01) {
            throw new IllegalStateException("Total chances for loop " + loopNumber + " must sum to 100%, but got " + totalChance + "%");
        }

        double randomValue = random.nextDouble() * totalChance;
        double currentSum = 0.0;

        for (String locationKey : locationConfig.getKeys(false)) {
            ConfigurationSection section = locationConfig.getConfigurationSection(locationKey);
            if (section == null) continue;

            currentSum += section.getDouble("chance", 0.0);
            if (randomValue <= currentSum) {
                return createRandomLoc(section, locationKey);
            }
        }

        return null;
    }

    private RandomLoc createRandomLoc(ConfigurationSection section, String locationName) {
        try {
            String worldName = section.getString("world", "world");
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                world = Bukkit.getWorlds().get(0);
            }

            double minX = section.getDouble("minX");
            double minZ = section.getDouble("minZ");
            double maxX = section.getDouble("maxX");
            double maxZ = section.getDouble("maxZ");

            double y = section.getDouble("y", -1);  // -1 means no Y specified

            double x = minX + (maxX - minX) * random.nextDouble();
            double z = minZ + (maxZ - minZ) * random.nextDouble();

            if (y == -1) {
                y = world.getHighestBlockYAt((int)x, (int)z);
            }

            return new RandomLoc(world, x, y, z);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
