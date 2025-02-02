package me.serbob.asteroidbotmove;

import me.serbob.asteroidapi.behaviour.FakePlayerSpawn;
import me.serbob.asteroidapi.behaviour.FakePlayerTick;
import me.serbob.asteroidapi.enums.MinecraftVersion;
import me.serbob.asteroidapi.interfaces.Version;
import me.serbob.asteroidapi.registries.FakePlayerEntity;
import me.serbob.asteroidapi.extension.ExtensionLifecycle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Version(MinecraftVersion.ALL)
public class BotMove extends ExtensionLifecycle implements FakePlayerSpawn, FakePlayerTick {
    private final Map<UUID, BukkitTask> movementTasks = new HashMap<>();
    private FileConfiguration config;
    private final LocationSelector locationSelector;

    public BotMove() {
        this.locationSelector = new LocationSelector();
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.config = getConfig();

        getLogger().info("Loaded Asteroid Bot Move Extension!");
    }

    @Override
    public void onDisable() {
        movementTasks.values().forEach(BukkitTask::cancel);
        movementTasks.clear();
        getLogger().info("Disabled Asteroid Bot Move Extension!");
    }

    @Override
    public void onSpawnFakePlayerNMSAsync(FakePlayerEntity fakePlayerEntity, JavaPlugin instance) {}

    @Override
    public void onSpawnFakePlayerAfterLoadAsync(FakePlayerEntity fakePlayerEntity, JavaPlugin instance) {
        Player fakePlayer = fakePlayerEntity.getEntityPlayer();
        int timer = config.getInt("timer", 50);

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(instance, new MovementRunner(
                fakePlayer,
                fakePlayerEntity,
                config,
                locationSelector,
                instance
        ), 20L * timer, 20L * timer);

        movementTasks.put(fakePlayer.getUniqueId(), task);
    }

    @Override
    public void individualPlayerTickAsync(FakePlayerEntity fakePlayerEntity, JavaPlugin javaPlugin) {
        Location ahead = fakePlayerEntity.getEntityPlayer().getEyeLocation().add(
                fakePlayerEntity.getEntityPlayer().getLocation().getDirection());
        Block block = ahead.getBlock();

        if (block.getType().isSolid()) {
            fakePlayerEntity.getMovement().stopMovement();
        }
    }
}
