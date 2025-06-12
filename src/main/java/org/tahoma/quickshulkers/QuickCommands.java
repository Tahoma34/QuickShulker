package org.tahoma.quickshulkers;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class QuickCommands implements CommandExecutor {

    private final QuickShulker plugin;
    public static final Map<UUID, Boolean> playerAutoPickup = new HashMap<>();
    public static final Map<UUID, Boolean> playerAutoOpen = new HashMap<>();

    public QuickCommands(QuickShulker plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = ColorUtil.colorize(plugin.getConfig().getString("messages.prefix", "&7[&6QuickShulker&7] "));

        if (args.length == 0) {
            sender.sendMessage(ColorUtil.colorize(prefix + plugin.getConfig().getString("messages.usageInfo")));
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtil.colorize(prefix + plugin.getConfig().getString("messages.playerOnly")));
            return true;
        }
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        switch (args[0].toLowerCase()) {
            case "reload":
                if (!player.hasPermission("quickshulker.reload")) {
                    player.sendMessage(ColorUtil.colorize(prefix + plugin.getConfig().getString("messages.noPermission")));
                    return true;
                }
                plugin.reloadConfig();
                player.sendMessage(ColorUtil.colorize(prefix + plugin.getConfig().getString("messages.configReloaded")));
                break;

            case "autoup":
                if (!player.hasPermission("quickshulker.autoup")) {
                    player.sendMessage(ColorUtil.colorize(prefix + plugin.getConfig().getString("messages.noPermission")));
                    return true;
                }
                boolean currentAutoPickup = playerAutoPickup.getOrDefault(uuid, false);
                boolean newAutoPickup = !currentAutoPickup;
                playerAutoPickup.put(uuid, newAutoPickup);
                String autoPickupMessage = newAutoPickup ?
                        plugin.getConfig().getString("messages.autoPickupEnabled") :
                        plugin.getConfig().getString("messages.autoPickupDisabled");
                player.sendMessage(ColorUtil.colorize(prefix + autoPickupMessage));
                break;

            case "autoopen":
                if (!player.hasPermission("quickshulker.autoopen")) {
                    player.sendMessage(ColorUtil.colorize(prefix + plugin.getConfig().getString("messages.noPermission")));
                    return true;
                }
                boolean currentAutoOpen = playerAutoOpen.getOrDefault(uuid, false);
                boolean newAutoOpen = !currentAutoOpen;
                playerAutoOpen.put(uuid, newAutoOpen);
                String autoOpenMessage = newAutoOpen ?
                        plugin.getConfig().getString("messages.autoOpenEnabled") :
                        plugin.getConfig().getString("messages.autoOpenDisabled");
                player.sendMessage(ColorUtil.colorize(prefix + autoOpenMessage));
                break;

            default:
                player.sendMessage(ColorUtil.colorize(prefix + plugin.getConfig().getString("messages.unknownCommand")));
                break;
        }

        return true;
    }
}