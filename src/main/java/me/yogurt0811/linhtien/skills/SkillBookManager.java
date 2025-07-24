package me.yogurt0811.linhtien.skills;

import me.yogurt0811.linhtien.LinhTienPlugin;
import me.yogurt0811.linhtien.config.MessageUtils;
import me.yogurt0811.linhtien.data.PlayerData;
import me.yogurt0811.linhtien.realms.Realm;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import net.kyori.adventure.text.Component;
import java.util.ArrayList;
import java.util.List;

public class SkillBookManager implements Listener {
    private final LinhTienPlugin plugin;
    public static final NamespacedKey SKILL_BOOK_ID = new NamespacedKey(LinhTienPlugin.getInstance(), "skill_book_id");

    public SkillBookManager(LinhTienPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public ItemStack createSkillBook(String skillId) {
        me.yogurt0811.linhtien.skills.Skill skill = this.plugin.getSkillManager().getSkill(skillId); 
        if (skill == null) return null;

        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = book.getItemMeta();
        if (meta == null) return null;

        meta.displayName(MessageUtils.format("&bSách Kỹ Năng: " + skill.name()));
        List<Component> lore = new ArrayList<>();
        lore.add(MessageUtils.format("&7Chuột phải để học kỹ năng này."));

        String requiredRealmId = this.plugin.getSkillManager().getRequiredRealm(skillId);
        if (requiredRealmId != null) {
            Realm requiredRealm = this.plugin.getRealmManager().getRealmById(requiredRealmId); 
            if (requiredRealm != null) {
                lore.add(MessageUtils.format("&cYêu cầu: " + requiredRealm.displayName()));
            }
        }

        lore.add(Component.empty());
        skill.lore().forEach(line -> lore.add(MessageUtils.format(line)));
        meta.lore(lore);
        
        if (skill.customModelData() > 0) meta.setCustomModelData(skill.customModelData());
        meta.getPersistentDataContainer().set(SkillBookManager.SKILL_BOOK_ID, PersistentDataType.STRING, skill.id());
        book.setItemMeta(meta);
        return book;
    }

    @EventHandler
    public void onSkillBookUse(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) return;
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;
        String skillId = item.getItemMeta().getPersistentDataContainer().get(SkillBookManager.SKILL_BOOK_ID, PersistentDataType.STRING);
        if (skillId == null) return;

        event.setCancelled(true);
        Player player = event.getPlayer();
        PlayerData data = this.plugin.getPlayerDataManager().getPlayerData(player); 
        if (data == null) return;

        if (data.hasLearnedSkill(skillId)) {
            player.sendMessage(MessageUtils.format("&cBạn đã học kỹ năng này rồi!"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        String requiredRealmId = this.plugin.getSkillManager().getRequiredRealm(skillId);
        if (requiredRealmId != null) {
            Realm playerRealm = this.plugin.getRealmManager().getPlayerRealm(data); 
            Realm requiredRealm = this.plugin.getRealmManager().getRealmById(requiredRealmId); 
            int playerRealmIndex = this.plugin.getRealmManager().getRealms().indexOf(playerRealm); 
            int requiredRealmIndex = this.plugin.getRealmManager().getRealms().indexOf(requiredRealm); 
            if (playerRealmIndex < requiredRealmIndex) {
                player.sendMessage(MessageUtils.format("&cCảnh giới của bạn không đủ để lĩnh ngộ công pháp này!"));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                return;
            }
        }

        data.learnSkill(skillId);
        me.yogurt0811.linhtien.skills.Skill skill = this.plugin.getSkillManager().getSkill(skillId); 
        player.sendMessage(MessageUtils.format("&aBạn đã lĩnh ngộ thành công " + skill.name() + "&a!"));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
        
        item.setAmount(item.getAmount() - 1);
    }
}