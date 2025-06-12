package org.tahoma.quickshulkers;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.block.ShulkerBox;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class QuickUtils {

    public static boolean isShulkerBox(ItemStack item) {
        if (item == null) return false;
        Material type = item.getType();
        return type.name().endsWith("SHULKER_BOX");
    }

    public static Inventory getShulkerInventory(ItemStack item, NamespacedKey key) {
        String inventoryTitle = ColorUtil.colorize(
                QuickShulker.getInstance().getConfig().getString("settings.shulkerInventoryTitle", "&0Shulker")
        );

        if (item == null) return Bukkit.createInventory(null, 27, inventoryTitle);

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return Bukkit.createInventory(null, 27, inventoryTitle);

        if (meta instanceof BlockStateMeta) {
            BlockStateMeta blockStateMeta = (BlockStateMeta) meta;
            BlockState blockState = blockStateMeta.getBlockState();

            if (blockState instanceof ShulkerBox) {
                ShulkerBox shulkerBox = (ShulkerBox) blockState;
                Inventory shulkerInventory = Bukkit.createInventory(null, 27, inventoryTitle);

                ItemStack[] contents = shulkerBox.getInventory().getContents();
                shulkerInventory.setContents(contents);

                return shulkerInventory;
            }
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(key, PersistentDataType.STRING)) {
            String serialized = container.get(key, PersistentDataType.STRING);
            Inventory loadedInv = deserializeInventory(serialized);
            if (loadedInv != null) {
                Inventory namedInventory = Bukkit.createInventory(null, 27, inventoryTitle);
                namedInventory.setContents(loadedInv.getContents());
                return namedInventory;
            }
        }

        return Bukkit.createInventory(null, 27, inventoryTitle);
    }

    public static void saveShulkerInventory(ItemStack item, Inventory inv, NamespacedKey key) {
        if (item == null) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        if (meta instanceof BlockStateMeta) {
            BlockStateMeta blockStateMeta = (BlockStateMeta) meta;
            BlockState blockState = blockStateMeta.getBlockState();

            if (blockState instanceof ShulkerBox) {
                ShulkerBox shulkerBox = (ShulkerBox) blockState;
                shulkerBox.getInventory().setContents(inv.getContents());

                blockStateMeta.setBlockState(shulkerBox);
                item.setItemMeta(blockStateMeta);
                return;
            }
        }

        String serialized = serializeInventory(inv);
        if (serialized == null) return;

        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(key, PersistentDataType.STRING, serialized);
        item.setItemMeta(meta);
    }

    public static ItemStack updateShulkerItem(ItemStack item, Inventory inv, NamespacedKey key) {
        if (item == null) return null;
        ItemStack newItem = item.clone();
        saveShulkerInventory(newItem, inv, key);
        return newItem;
    }

    private static String serializeInventory(Inventory inv) {
        try {
            YamlConfiguration config = new YamlConfiguration();
            config.set("size", inv.getSize());
            for (int i = 0; i < inv.getSize(); i++) {
                ItemStack item = inv.getItem(i);
                if (item != null) {
                    config.set("item" + i, item);
                }
            }
            return config.saveToString();
        } catch (Exception e) {
            Bukkit.getLogger().severe("Error inventories: " + e.getMessage());
            return null;
        }
    }

    private static Inventory deserializeInventory(String data) {
        try {
            YamlConfiguration config = new YamlConfiguration();
            config.loadFromString(data);
            int size = config.getInt("size", 27);
            Inventory inv = Bukkit.createInventory(null, size, "Shulker");
            for (int i = 0; i < size; i++) {
                if (config.contains("item" + i)) {
                    ItemStack item = config.getItemStack("item" + i);
                    inv.setItem(i, item);
                }
            }
            return inv;
        } catch (Exception e) {
            Bukkit.getLogger().severe("Inventory serialization error: " + e.getMessage());
            return null;
        }
    }
}