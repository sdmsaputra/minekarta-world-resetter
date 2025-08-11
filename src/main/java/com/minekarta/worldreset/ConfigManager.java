package com.minekarta.worldreset;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
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

        resetStartingMessage = config.getString("messages.reset-starting", "<red>[World Reset] The <world> world is resetting now!");
        resetFinishedMessage = config.getString("messages.reset-finished", "<green>[World Reset] The <world> world has been reset and is now available.");
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

    public Component getResetStartingMessage() {
        return MiniMessage.miniMessage().deserialize(resetStartingMessage,
                Placeholder.unparsed("world", worldToReset));
    }

    public Component getResetFinishedMessage() {
        return MiniMessage.miniMessage().deserialize(resetFinishedMessage,
                Placeholder.unparsed("world", worldToReset));
    }
}
