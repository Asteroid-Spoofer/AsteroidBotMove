package me.serbob.asteroidbotmove;

import me.serbob.asteroidapi.logging.Logger;
import me.serbob.asteroidapi.registries.FakePlayerEntity;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import java.util.List;

public class MovementRunner implements Runnable {
    private final Player player;
    private final FakePlayerEntity entity;
    private final FileConfiguration config;
    private final LocationSelector locationSelector;
    private final Plugin plugin;
    private int currentLoop = 1;

    public MovementRunner(Player player, FakePlayerEntity entity, FileConfiguration config,
                          LocationSelector locationSelector, Plugin plugin) {
        this.player = player;
        this.entity = entity;
        this.config = config;
        this.locationSelector = locationSelector;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        try {
            RandomLoc location = locationSelector.getRandomLocation(config, currentLoop);
            if (location == null) {
                return;
            }

            executeCommands();
            movePlayer(location);
            currentLoop = (currentLoop == 1) ? 2 : 1;
        } catch (Exception ignored) {}
    }

    private void executeCommands() {
        List<String> commands = config.getStringList("loop." + currentLoop + ".commands");
        commands.forEach(command ->
                Bukkit.dispatchCommand(
                        Bukkit.getConsoleSender(),
                        command.replace("{player}", player.getName())
                )
        );
    }

    private void movePlayer(RandomLoc location) {
        Bukkit.getScheduler().runTaskLater(
                plugin,
                () -> entity.getMovement().moveTo(location.getX(), location.getZ()),
                40L
        );
    }
}
