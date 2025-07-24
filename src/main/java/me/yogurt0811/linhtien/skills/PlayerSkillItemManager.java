package me.yogurt0811.linhtien.skills;

import me.yogurt0811.linhtien.LinhTienPlugin;
import me.yogurt0811.linhtien.config.MessageUtils;
import me.yogurt0811.linhtien.data.PlayerData;
import me.yogurt0811.linhtien.utils.Keys;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class PlayerSkillItemManager {
    private final LinhTienPlugin plugin;

    public PlayerSkillItemManager(LinhTienPlugin plugin) {
        this.plugin = plugin;
    }

    public void giveOrUpdatePlayerSkillItems(Player player) {
        ConfigurationSection skillItemsSection = plugin.getConfigManager().getConfig().getConfigurationSection("skill-items");
        if (skillItemsSection == null) return;
        
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
        if (playerData == null) return;

        PlayerInventory inv = player.getInventory();
        for (String key : skillItemsSection.getKeys(false)) {
            ConfigurationSection itemConfig = skillItemsSection.getConfigurationSection(key);
            int invSlot = itemConfig.getInt("slot");
            int skillSlotNum = Integer.parseInt(key.replace("slot-", ""));

            String equippedSkillId = playerData.getSkillInSlot(skillSlotNum - 1);
            
            ItemStack skillItem;
            if (equippedSkillId != null && !equippedSkillId.isEmpty()) {
                me.yogurt0811.linhtien.skills.Skill skill = plugin.getSkillManager().getSkill(equippedSkillId);
                skillItem = (skill != null) ? createEquippedItem(skill, skillSlotNum) : createUnequippedItem(itemConfig, skillSlotNum);
            } else {
                skillItem = createUnequippedItem(itemConfig, skillSlotNum);
            }
            inv.setItem(invSlot, skillItem);
        }
    }
    
    public void updatePlayerSkillItems(Player player) {
        giveOrUpdatePlayerSkillItems(player);
    }

    private ItemStack createEquippedItem(me.yogurt0811.linhtien.skills.Skill skill, int skillSlotNum) {
        ItemStack item = new ItemStack(skill.material());
        ItemMeta meta = item.getItemMeta();

        meta.displayName(MessageUtils.format(skill.name()));
        List<Component> lore = new ArrayList<>();
        skill.lore().forEach(line -> lore.add(MessageUtils.format(line)));
        lore.add(Component.empty());
        lore.add(MessageUtils.format("&7Nhấn Chuột Phải để sử dụng")); // Thêm dòng này để người dùng biết cách dùng
        meta.lore(lore);
        if (skill.customModelData() > 0) meta.setCustomModelData(skill.customModelData());

        addSkillItemTag(meta, skillSlotNum);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createUnequippedItem(ConfigurationSection config, int skillSlotNum) {
        Material material = Material.matchMaterial(config.getString("material"));
        ItemStack item = new ItemStack(material != null ? material : Material.BARRIER);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(MessageUtils.format(config.getString("name")));
        List<Component> lore = new ArrayList<>();
        config.getStringList("lore").forEach(line -> lore.add(MessageUtils.format(line)));
        meta.lore(lore);
        meta.setCustomModelData(config.getInt("custom-model-data", 0));

        addSkillItemTag(meta, skillSlotNum);
        item.setItemMeta(meta);
        return item;
    }

    private void addSkillItemTag(ItemMeta meta, int skillSlotNum) {
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(Keys.SKILL_SLOT_ID, PersistentDataType.INTEGER, skillSlotNum);
    }
}