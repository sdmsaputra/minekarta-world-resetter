package com.minekarta.worldreset;

import com.minekarta.worldreset.ConfigManager;
import com.minekarta.worldreset.WorldResetExpansion;
import com.minekarta.worldreset.WorldResetTask;
import com.onarandombox.MultiverseCore.MultiverseCore;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class MinekartaWorldResetPlugin extends JavaPlugin {

    private MultiverseCore multiverseCore;
    private ConfigManager configManager;
    private long nextResetTimestamp = 0;

    @Override
    public void onEnable() {
        Logger logger = getLogger();

        // --- Configuration ---
        this.configManager = new ConfigManager(this);
        this.configManager.load();

        // --- Dependency Checks ---
        if (!hookMultiverse()) {
            logger.severe("Could not hook into Multiverse-Core. This plugin is required.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        logger.info("Successfully hooked into Multiverse-Core.");


        String worldName = configManager.getWorldToReset();
        if (worldName == null || worldName.isBlank()) {
            logger.severe("'world-to-reset' is not defined in config.yml. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        String backupFolderName = configManager.getBackupWorldFolder();
        if (backupFolderName == null || backupFolderName.isBlank()) {
            logger.severe("'backup-world-folder' is not defined in config.yml. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // --- PlaceholderAPI Hook ---
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            logger.info("PlaceholderAPI found. Registering expansion...");
            new WorldResetExpansion(this, this.getPluginMeta().getVersion()).register();
        } else {
            logger.info("PlaceholderAPI not found, so no placeholders will be available.");
        }

        // --- Task Scheduling ---
        long intervalMinutes = configManager.getResetIntervalMinutes();
        if (intervalMinutes <= 0) {
            logger.warning("'reset-interval-minutes' is not set to a positive value. The world will not be reset automatically.");
        } else {
            long intervalMillis = intervalMinutes * 60 * 1000;
            this.nextResetTimestamp = System.currentTimeMillis() + intervalMillis;

            long intervalTicks = intervalMinutes * 60 * 20;
            logger.info("Scheduling world reset for '" + worldName + "' every " + intervalMinutes + " minutes.");
            WorldResetTask resetTask = new WorldResetTask(this, worldName, backupFolderName);
            resetTask.runTaskTimer(this, intervalTicks, intervalTicks);
        }

        logger.info("MinekartaWorldReset has been enabled!");
    }

    @Override
    public void onDisable() {
        // Cancel any Bukkit tasks associated with this plugin
        Bukkit.getScheduler().cancelTasks(this);
        getLogger().info("MinekartaWorldReset has been disabled.");
    }

    private boolean hookMultiverse() {
        if (getServer().getPluginManager().getPlugin("Multiverse-Core") != null) {
            this.multiverseCore = (MultiverseCore) getServer().getPluginManager().getPlugin("Multiverse-Core");
            return true;
        }
        return false;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MultiverseCore getMultiverseCore() {
        return this.multiverseCore;
    }

    public long getNextResetTimestamp() {
        return nextResetTimestamp;
    }

    public void setNextResetTimestamp(long nextResetTimestamp) {
        this.nextResetTimestamp = nextResetTimestamp;
    }
}
