package org.tahoma.quickshulkers;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

public class QuickListener implements Listener {

    private final QuickShulker plugin;
    private final NamespacedKey shulkerInvKey;

    private final Map<UUID, OpenedShulker> openedShulkers = new HashMap<>();

    public QuickListener(QuickShulker plugin, NamespacedKey shulkerInvKey) {
        this.plugin = plugin;
        this.shulkerInvKey = shulkerInvKey;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!plugin.isHandInteractEnabled()) {
            return;
        }
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Player player = event.getPlayer();
        if (!isPlayerAutoOpenEnabled(player)) {
            return;
        }
        if (!plugin.getQuickProtect().canUseFeatures(player)) {
            plugin.getQuickProtect().sendCombatMessage(player);
            return;
        }

        ItemStack itemInHand = event.getItem();
        if (itemInHand == null || itemInHand.getType() == Material.AIR) {
            return;
        }
        if (!QuickUtils.isShulkerBox(itemInHand)) {
            return;
        }
        event.setCancelled(true);

        Inventory shulkerInv = QuickUtils.getShulkerInventory(itemInHand, shulkerInvKey);
        openedShulkers.put(player.getUniqueId(), new OpenedShulker(-1, itemInHand, shulkerInv));
        player.openInventory(shulkerInv);
        player.playSound(player.getLocation(), Sound.BLOCK_SHULKER_BOX_OPEN, 1.0F, 1.0F);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!plugin.isInventoryInteractEnabled()) {
            return;
        }
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }
        ItemStack clickedItem = event.getCurrentItem();
        if (!QuickUtils.isShulkerBox(clickedItem)) {
            return;
        }
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        if (!plugin.getQuickProtect().canUseFeatures(player)) {
            plugin.getQuickProtect().sendCombatMessage(player);
            return;
        }
        event.setCancelled(true);
        int slot = event.getSlot();


        Inventory shulkerInv = QuickUtils.getShulkerInventory(clickedItem, shulkerInvKey);
        openedShulkers.put(player.getUniqueId(), new OpenedShulker(slot, clickedItem, shulkerInv));
        player.openInventory(shulkerInv);
        player.playSound(player.getLocation(), Sound.BLOCK_SHULKER_BOX_OPEN, 1.0F, 1.0F);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        UUID uuid = player.getUniqueId();
        OpenedShulker os = openedShulkers.get(uuid);
        if (os == null) {
            return;
        }
        Inventory closedInv = event.getInventory();
        if (closedInv.equals(os.shulkerInventory)) {
            ItemStack updatedShulker = QuickUtils.updateShulkerItem(os.shulkerItem, closedInv, shulkerInvKey);
            if (os.slot >= 0) {
                player.getInventory().setItem(os.slot, updatedShulker);
            } else {
                ItemStack mainHand = player.getInventory().getItemInMainHand();
                if (mainHand.getType() != Material.AIR && QuickUtils.isShulkerBox(mainHand)) {
                    player.getInventory().setItemInMainHand(updatedShulker);
                } else {
                    ItemStack offHand = player.getInventory().getItemInOffHand();
                    if (offHand.getType() != Material.AIR && QuickUtils.isShulkerBox(offHand)) {
                        player.getInventory().setItemInOffHand(updatedShulker);
                    }
                }
            }
            openedShulkers.remove(uuid);
        }
    }

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();

        if (!isPlayerAutoPickupEnabled(player)) {
            return;
        }

        if (!player.hasPermission("quickshulker.autoup")) {
            return;
        }

        if (plugin.getQuickProtect() != null && !plugin.getQuickProtect().canUseFeatures(player)) {
            return;
        }

        Item item = event.getItem();
        ItemStack pickedItem = item.getItemStack();
        Material pickedMaterial = pickedItem.getType();

        java.util.List<String> allowedItems = plugin.getConfig().getStringList("allowedItems");

        if (!allowedItems.contains(pickedMaterial.name())) {
            return;
        }

        for (ItemStack invItem : player.getInventory().getContents()) {
            if (invItem == null || !QuickUtils.isShulkerBox(invItem)) {
                continue;
            }

            Inventory shulkerInv = QuickUtils.getShulkerInventory(invItem, shulkerInvKey);

            if (shulkerInv.firstEmpty() != -1) {
                event.setCancelled(true);

                shulkerInv.addItem(pickedItem);

                ItemStack updatedShulker = QuickUtils.updateShulkerItem(invItem, shulkerInv, shulkerInvKey);

                int shulkerSlot = player.getInventory().first(invItem);
                if (shulkerSlot != -1) {
                    player.getInventory().setItem(shulkerSlot, updatedShulker);
                }

                item.remove();

                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.0f);

                break;
            }
        }
    }

    private boolean isPlayerAutoPickupEnabled(Player player) {
        UUID uuid = player.getUniqueId();
        Boolean toggle = QuickCommands.playerAutoPickup.get(uuid);
        return toggle != null && toggle;
    }

    private boolean isPlayerAutoOpenEnabled(Player player) {
        UUID uuid = player.getUniqueId();
        Boolean toggle = QuickCommands.playerAutoOpen.get(uuid);
        return toggle != null && toggle;
    }

    private static class OpenedShulker {
        public final int slot;
        public final ItemStack shulkerItem;
        public final Inventory shulkerInventory;

        public OpenedShulker(int slot, ItemStack shulkerItem, Inventory shulkerInventory) {
            this.slot = slot;
            this.shulkerItem = shulkerItem;
            this.shulkerInventory = shulkerInventory;
        }
    }
}
