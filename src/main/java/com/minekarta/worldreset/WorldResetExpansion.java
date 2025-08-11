package com.minekarta.worldreset;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WorldResetExpansion extends PlaceholderExpansion {

    private final MinekartaWorldResetPlugin plugin;
    private final String version;

    public WorldResetExpansion(MinekartaWorldResetPlugin plugin, String version) {
        this.plugin = plugin;
        this.version = version;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "worldreset";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Minekarta Network";
    }

    @Override
    public @NotNull String getVersion() {
        return this.version;
    }

    @Override
    public boolean persist() {
        return true; // This expansion should persist through /papi reload
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (params.equals("time_until_reset")) {
            long nextReset = plugin.getNextResetTimestamp();
            if (nextReset == 0) {
                return "Not scheduled";
            }

            long remainingMillis = nextReset - System.currentTimeMillis();
            if (remainingMillis <= 0) {
                return "Resetting now...";
            }

            long totalSeconds = remainingMillis / 1000;
            long hours = totalSeconds / 3600;
            long minutes = (totalSeconds % 3600) / 60;
            long seconds = totalSeconds % 60;

            return String.format("%dh %dm %ds", hours, minutes, seconds);
        }

        return null; // Placeholder not found
    }
}
