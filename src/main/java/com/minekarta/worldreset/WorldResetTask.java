package com.minekarta.worldreset;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.stream.Stream;

public class WorldResetTask extends BukkitRunnable {

    private final MinekartaWorldResetPlugin plugin;
    private final MVWorldManager worldManager;
    private final String worldName;
    private final Path worldFolderPath;
    private final Path backupFolderPath;

    public WorldResetTask(MinekartaWorldResetPlugin plugin, String worldName, String backupFolderName) {
        this.plugin = plugin;
        this.worldManager = plugin.getMultiverseCore().getMVWorldManager();
        this.worldName = worldName;

        // Get server's root directory to build absolute paths
        Path serverRoot = plugin.getServer().getWorldContainer().toPath().getParent();
        this.worldFolderPath = plugin.getServer().getWorldContainer().toPath().resolve(worldName);
        this.backupFolderPath = serverRoot.resolve(backupFolderName);
    }

    @Override
    public void run() {
        // Update the timestamp for the *next* reset before this one starts.
        long intervalMillis = plugin.getConfigManager().getResetIntervalMinutes() * 60 * 1000;
        plugin.setNextResetTimestamp(System.currentTimeMillis() + intervalMillis);

        // This task runs on the main server thread.
        Bukkit.broadcast(plugin.getConfigManager().getResetStartingMessage());

        MultiverseWorld mvWorld = worldManager.getMVWorld(worldName);
        if (mvWorld == null) {
            plugin.getLogger().warning("World '" + worldName + "' not managed by Multiverse. Cannot reset.");
            return;
        }

        // Unloading must be done on the main thread
        if (!worldManager.unloadWorld(worldName)) {
            plugin.getLogger().severe("Failed to unload world '" + worldName + "'. Aborting reset.");
            // Potentially broadcast a failure message
            return;
        }

        plugin.getLogger().info("World '" + worldName + "' unloaded. Starting file operations asynchronously...");

        // Run file operations on an async thread to avoid server lag
        runAsyncFileOperations();
    }

    private void runAsyncFileOperations() {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    // --- Delete existing world folder ---
                    if (Files.exists(worldFolderPath)) {
                        plugin.getLogger().info("Deleting world folder: " + worldFolderPath);
                        try (Stream<Path> walk = Files.walk(worldFolderPath)) {
                            walk.sorted(Comparator.reverseOrder())
                                .map(Path::toFile)
                                .forEach(File::delete);
                        }
                    }

                    // --- Copy backup world folder ---
                    plugin.getLogger().info("Copying backup from: " + backupFolderPath);
                    if (!Files.exists(backupFolderPath)) {
                        plugin.getLogger().severe("Backup folder not found at " + backupFolderPath + "! Aborting reset.");
                        // The world is unloaded and deleted, this is a critical error state.
                        // We should probably try to reload the world if it failed here.
                        return;
                    }

                    try (Stream<Path> stream = Files.walk(backupFolderPath)) {
                        stream.forEach(source -> {
                            try {
                                Files.copy(source, worldFolderPath.resolve(backupFolderPath.relativize(source)), StandardCopyOption.REPLACE_EXISTING);
                            } catch (IOException e) {
                                plugin.getLogger().log(Level.SEVERE, "Failed to copy file during backup restoration.", e);
                            }
                        });
                    }
                    plugin.getLogger().info("World backup copied successfully.");

                    // Now that file ops are done, schedule the world loading back on the main thread
                    runPostResetTask();

                } catch (IOException e) {
                    plugin.getLogger().log(Level.SEVERE, "An error occurred during file operations for world reset.", e);
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    private void runPostResetTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                // This must run on the main thread
                plugin.getLogger().info("Loading world '" + worldName + "' back into the server...");
                if (!worldManager.loadWorld(worldName)) {
                    plugin.getLogger().severe("Failed to load the new world '" + worldName + "' after reset!");
                    return;
                }

                plugin.getLogger().info("World '" + worldName + "' has been reset and loaded.");
                Bukkit.broadcast(plugin.getConfigManager().getResetFinishedMessage());
            }
        }.runTask(plugin);
    }
}
