package org.tahoma.quickshulkers;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.Bukkit;

public class QuickProtect {

    private final QuickShulker plugin;
    private boolean combatLogXEnabled;

    public QuickProtect(QuickShulker plugin) {
        this.plugin = plugin;
        this.combatLogXEnabled = Bukkit.getPluginManager().getPlugin("CombatLogX") != null;

        if (combatLogXEnabled) {
            plugin.getLogger().info("CombatLogX detected. Anti-combat logging protection activated.");
        } else {
            plugin.getLogger().info("CombatLogX not detected. Anti-combat logging protection disabled.");
        }
    }

    public boolean canUseFeatures(Player player) {
        if (!combatLogXEnabled) {
            return true;
        }

        try {
            Plugin combatLogXPlugin = Bukkit.getPluginManager().getPlugin("CombatLogX");
            if (combatLogXPlugin instanceof com.github.sirblobman.combatlogx.api.ICombatLogX) {
                com.github.sirblobman.combatlogx.api.ICombatLogX api =
                        (com.github.sirblobman.combatlogx.api.ICombatLogX) combatLogXPlugin;
                return !api.getCombatManager().isInCombat(player);
            }
            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("Error checking combat status via CombatLogX: " + e.getMessage());
            return true;
        }
    }

    public void sendCombatMessage(Player player) {
        String prefix = plugin.getConfig().getString("messages.prefix", "&7[&6QuickShulker&7] ");
        String message = plugin.getConfig().getString("messages.inCombat", "&cYou cannot use this feature while in combat!");
        player.sendMessage(ColorUtil.colorize(prefix + message));
    }
}