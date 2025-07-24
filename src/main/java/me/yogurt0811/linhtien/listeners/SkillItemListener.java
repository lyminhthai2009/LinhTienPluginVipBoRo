package me.yogurt0811.linhtien.listeners;

import me.yogurt0811.linhtien.LinhTienPlugin;
import me.yogurt0811.linhtien.config.MessageUtils;
import me.yogurt0811.linhtien.data.PlayerData;
import me.yogurt0811.linhtien.utils.Keys;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class SkillItemListener implements Listener {

    private final LinhTienPlugin plugin;

    public SkillItemListener(LinhTienPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean isSkillItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(Keys.SKILL_SLOT_ID);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (isSkillItem(event.getCurrentItem()) || isSkillItem(event.getCursor())) {
            if (event.getClickedInventory() == event.getWhoClicked().getInventory() &&
               (event.getAction() == InventoryAction.HOTBAR_SWAP || event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD)) {
                if (isSkillItem(event.getWhoClicked().getInventory().getItem(event.getHotbarButton()))) {
                    event.setCancelled(true);
                }
                return;
            }
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (isSkillItem(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSwapHand(PlayerSwapHandItemsEvent event) {
        if (isSkillItem(event.getMainHandItem()) || isSkillItem(event.getOffHandItem())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        ItemStack item = event.getItem();
        if (!isSkillItem(item)) {
            return;
        }

        event.setCancelled(true);

        Player player = event.getPlayer();
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        int slotId = container.getOrDefault(Keys.SKILL_SLOT_ID, PersistentDataType.INTEGER, 0);
        if (slotId == 0) return;

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        String skillId = data.getSkillInSlot(slotId - 1);

        if (skillId == null || skillId.isEmpty()) {
            MessageUtils.sendMessage(player, "skill-no-skill-equipped");
            return;
        }

        plugin.getSkillManager().activateSkill(player, skillId);
    }
}