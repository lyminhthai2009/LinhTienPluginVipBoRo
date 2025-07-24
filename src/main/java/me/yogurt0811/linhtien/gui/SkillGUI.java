package me.yogurt0811.linhtien.gui;

import me.yogurt0811.linhtien.LinhTienPlugin;
import me.yogurt0811.linhtien.config.MessageUtils;
import me.yogurt0811.linhtien.data.PlayerData;
import me.yogurt0811.linhtien.skills.Skill;
import me.yogurt0811.linhtien.utils.Keys;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class SkillGUI implements Listener {

    private final LinhTienPlugin plugin;
    private final Map<UUID, String> selectingPlayers = new HashMap<>();

    public SkillGUI(LinhTienPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    public void open(Player player) {
        ConfigurationSection guiConfig = plugin.getConfigManager().getConfig().getConfigurationSection("skill-gui");
        if (guiConfig == null) {
            player.sendMessage(MessageUtils.format("&c Lỗi: Cấu hình GUI kỹ năng không tồn tại."));
            return;
        }

        String title = guiConfig.getString("title", "Bảng Kỹ Năng");
        int rows = guiConfig.getInt("rows", 6);
        Inventory gui = Bukkit.createInventory(player, rows * 9, MessageUtils.format(title));

        if (guiConfig.getBoolean("items.filler-glass.enabled", true)) {
            ItemStack filler = createFillerItem(guiConfig.getConfigurationSection("items.filler-glass"));
            for (int i = 0; i < gui.getSize(); i++) {
                gui.setItem(i, filler);
            }
        }

        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
        if (playerData == null) return;

        drawEquippedSlots(gui, guiConfig.getConfigurationSection("items.skill-slots"), playerData);
        drawLearnedSkills(gui, guiConfig.getIntegerList("items.learned-skills-slots"), playerData);

        player.openInventory(gui);
    }

    private void drawEquippedSlots(Inventory gui, ConfigurationSection slotsConfig, PlayerData data) {
        if (slotsConfig == null) return;
        for (String key : slotsConfig.getKeys(false)) {
            int guiSlot = slotsConfig.getInt(key + ".slot");
            int skillSlotNum = Integer.parseInt(key);

            String equippedSkillId = data.getSkillInSlot(skillSlotNum - 1);
            ItemStack displayItem;

            if (equippedSkillId != null && !equippedSkillId.isEmpty()) {
                Skill skill = plugin.getSkillManager().getSkill(equippedSkillId);
                displayItem = (skill != null) ? createSkillDisplayItem(skill, true, skillSlotNum) : createEmptySlotItem(slotsConfig.getConfigurationSection(key), skillSlotNum);
            } else {
                displayItem = createEmptySlotItem(slotsConfig.getConfigurationSection(key), skillSlotNum);
            }
            gui.setItem(guiSlot, displayItem);
        }
    }

    private void drawLearnedSkills(Inventory gui, List<Integer> slots, PlayerData data) {
        List<String> learnedSkills = data.getLearnedSkills();
        int currentSlotIndex = 0;

        for (String skillId : learnedSkills) {
            if (currentSlotIndex >= slots.size()) break;
            Skill skill = plugin.getSkillManager().getSkill(skillId);
            if (skill != null) {
                gui.setItem(slots.get(currentSlotIndex), createSkillDisplayItem(skill, false, 0));
                currentSlotIndex++;
            }
        }
    }

    private ItemStack createSkillDisplayItem(Skill skill, boolean isEquipped, int skillSlotNum) {
        ItemStack item = new ItemStack(skill.material());
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MessageUtils.format(skill.name()));
        List<Component> lore = new ArrayList<>();
        skill.lore().forEach(line -> lore.add(MessageUtils.format(line)));
        lore.add(Component.empty());
        if (isEquipped) {
            lore.add(MessageUtils.format("&c&lĐã trang bị &7(Click để gỡ)"));
        } else {
            lore.add(MessageUtils.format("&a&lĐã học &7(Click để trang bị)"));
        }
        meta.lore(lore);
        if (skill.customModelData() > 0) meta.setCustomModelData(skill.customModelData());

        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(Keys.GUI_SKILL_ID, PersistentDataType.STRING, skill.id());
        if (skillSlotNum > 0) {
            container.set(Keys.GUI_EQUIPPED_SLOT_NUM, PersistentDataType.INTEGER, skillSlotNum);
        }

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createEmptySlotItem(ConfigurationSection config, int skillSlotNum) {
        Material material = Material.matchMaterial(config.getString("material-locked", "GLASS_BOTTLE"));
        ItemStack item = new ItemStack(material != null ? material : Material.GLASS_BOTTLE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MessageUtils.format(config.getString("name")));
        meta.lore(config.getStringList("lore-locked").stream().map(MessageUtils::format).collect(Collectors.toList()));
        
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(Keys.GUI_EMPTY_SKILL_SLOT, PersistentDataType.INTEGER, 1);
        container.set(Keys.GUI_EQUIPPED_SLOT_NUM, PersistentDataType.INTEGER, skillSlotNum);

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createFillerItem(ConfigurationSection config) {
        Material material = Material.matchMaterial(config.getString("material", "GRAY_STAINED_GLASS_PANE"));
        ItemStack item = new ItemStack(material != null ? material : Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MessageUtils.format(config.getString("name", " ")));
        meta.getPersistentDataContainer().set(Keys.GUI_FILLER_ITEM, PersistentDataType.INTEGER, 1);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().title().equals(MessageUtils.format(plugin.getConfigManager().getConfig().getString("skill-gui.title", "Bảng Kỹ Năng")))) return;

        event.setCancelled(true);
        if (event.getClickedInventory() == null || event.getClickedInventory().equals(player.getInventory())) {
             return; 
        }

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR || !clickedItem.hasItemMeta()) return;

        PersistentDataContainer container = clickedItem.getItemMeta().getPersistentDataContainer();
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
        if (playerData == null) return;
        
        String skillIdToEquip = selectingPlayers.get(player.getUniqueId());

        if (container.has(Keys.GUI_EQUIPPED_SLOT_NUM)) {
            int slotNum = container.get(Keys.GUI_EQUIPPED_SLOT_NUM, PersistentDataType.INTEGER);
            
            if (skillIdToEquip != null) {
                playerData.setSkillInSlot(slotNum - 1, skillIdToEquip);
                selectingPlayers.remove(player.getUniqueId());
                player.setItemOnCursor(new ItemStack(Material.AIR));
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.5f);
                plugin.getPlayerSkillItemManager().updatePlayerSkillItems(player);
                open(player);
            } 
            else if (container.has(Keys.GUI_SKILL_ID)) {
                playerData.setSkillInSlot(slotNum - 1, null);
                player.playSound(player.getLocation(), Sound.BLOCK_CHAIN_BREAK, 1f, 1f);
                plugin.getPlayerSkillItemManager().updatePlayerSkillItems(player);
                open(player);
            }
        }
        else if (container.has(Keys.GUI_SKILL_ID)) {
            String clickedSkillId = container.get(Keys.GUI_SKILL_ID, PersistentDataType.STRING);
            
            selectingPlayers.put(player.getUniqueId(), clickedSkillId);
            Skill skill = plugin.getSkillManager().getSkill(clickedSkillId);
            if(skill == null) return;
            
            ItemStack cursorItem = new ItemStack(skill.material());
            ItemMeta meta = cursorItem.getItemMeta();
            meta.displayName(MessageUtils.format(skill.name()));
            meta.lore(List.of(MessageUtils.format("&eClick vào một ô kỹ năng để trang bị.")));
            if(skill.customModelData() > 0) meta.setCustomModelData(skill.customModelData());
            cursorItem.setItemMeta(meta);
            
            player.setItemOnCursor(cursorItem);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (selectingPlayers.remove(player.getUniqueId()) != null) {
            player.setItemOnCursor(new ItemStack(Material.AIR));
        }
    }
}