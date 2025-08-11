package com.minekarta.worldreset;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final MinekartaWorldResetPlugin plugin;
    private String worldToReset;
    private String backupWorldFolder;
    private long resetIntervalMinutes;
    private String resetStartingMessage;
    private String resetFinishedMessage;

    public ConfigManager(MinekartaWorldResetPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        worldToReset = config.getString("world-to-reset");
        backupWorldFolder = config.getString("backup-world-folder");
        resetIntervalMinutes = config.getLong("reset-interval-minutes", 180);

        String startingMsg = config.getString("messages.reset-starting", "&c[World Reset] The %world% world is resetting now!");
        resetStartingMessage = ChatColor.translateAlternateColorCodes('&', startingMsg);

        String finishedMsg = config.getString("messages.reset-finished", "&a[World Reset] The %world% world has been reset and is now available.");
        resetFinishedMessage = ChatColor.translateAlternateColorCodes('&', finishedMsg);
    }

    public String getWorldToReset() {
        return worldToReset;
    }

    public String getBackupWorldFolder() {
        return backupWorldFolder;
    }

    public long getResetIntervalMinutes() {
        return resetIntervalMinutes;
    }

    public String getResetStartingMessage() {
        // Replace the %world% placeholder
        return resetStartingMessage.replace("%world%", worldToReset);
    }

    public String getResetFinishedMessage() {
        // Replace the %world% placeholder
        return resetFinishedMessage.replace("%world%", worldToReset);
    }
}
