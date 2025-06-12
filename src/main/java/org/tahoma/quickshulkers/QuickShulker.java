package org.tahoma.quickshulkers;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class QuickShulker extends JavaPlugin {

    private static QuickShulker instance;
    private NamespacedKey shulkerInvKey;
    private QuickProtect quickProtect;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        getCommand("quickshulker").setExecutor(new QuickCommands(this));
        shulkerInvKey = new NamespacedKey(this, "shulker_inventory");

        this.quickProtect = new QuickProtect(this);

        getServer().getPluginManager().registerEvents(new QuickListener(this, shulkerInvKey), this);
    }

    @Override
    public void onDisable() {
    }

    public static QuickShulker getInstance() {
        return instance;
    }

    public NamespacedKey getShulkerInvKey() {
        return shulkerInvKey;
    }

    public QuickProtect getQuickProtect() {
        return quickProtect;
    }

    public boolean isHandInteractEnabled() {
        FileConfiguration config = getConfig();
        return config.getBoolean("enableHandRightClick", true);
    }

    public boolean isInventoryInteractEnabled() {
        FileConfiguration config = getConfig();
        return config.getBoolean("enableInventoryRightClick", true);
    }

    public boolean isAutoPickupEnabled() {
        FileConfiguration config = getConfig();
        return config.getBoolean("enableAutoPickup", true);
    }
}
