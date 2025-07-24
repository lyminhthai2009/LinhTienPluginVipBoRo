package me.yogurt0811.linhtien.skills;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.skills.Skill;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillTrigger; // Đảm bảo import này đúng
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.skills.SkillMetadataImpl; 
import me.yogurt0811.linhtien.LinhTienPlugin;
import me.yogurt0811.linhtien.config.MessageUtils;
import me.yogurt0811.linhtien.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SkillManager implements org.bukkit.event.Listener {

    private final LinhTienPlugin plugin;
    private final Map<String, me.yogurt0811.linhtien.skills.Skill> skills = new HashMap<>(); 
    private final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();
    private boolean mythicMobsEnabled;

    public SkillManager(LinhTienPlugin plugin) {
        this.plugin = plugin;
        this.mythicMobsEnabled = Bukkit.getPluginManager().isPluginEnabled("MythicMobs");
        if (!mythicMobsEnabled) {
            plugin.getLogger().severe("MythicMobs không được tìm thấy! Hệ thống kỹ năng sẽ bị vô hiệu hóa!");
        }
        loadSkills();
    }

    public void loadSkills() {
        skills.clear();
        ConfigurationSection section = plugin.getConfigManager().getSkillsConfig().getConfigurationSection("skills");
        if (section == null) return;

        for (String id : section.getKeys(false)) {
            String name = section.getString(id + ".name");
            List<String> lore = section.getStringList(id + ".lore");
            Material material = Material.matchMaterial(section.getString(id + ".material", "BARRIER"));
            int modelData = section.getInt(id + ".custom-model-data", 0);
            double cost = section.getDouble(id + ".cost", 0);
            int cooldown = section.getInt(id + ".cooldown", 0);
            String requiredRealm = section.getString(id + ".required-realm");
            String mythicSkillId = section.getString(id + ".mythic-skill-id");

            if (mythicMobsEnabled && mythicSkillId != null) {
                if (MythicBukkit.inst().getSkillManager().getSkill(mythicSkillId).isEmpty()) { 
                    plugin.getLogger().warning("MythicMobs Skill ID '" + mythicSkillId + "' cho skill '" + id + "' không tồn tại trong MythicMobs. Skill này sẽ không hoạt động!");
                    mythicSkillId = null;
                }
            }

            skills.put(id.toLowerCase(), new me.yogurt0811.linhtien.skills.Skill(id, name, lore, material, modelData, cost, cooldown, requiredRealm, mythicSkillId));
        }
        plugin.getLogger().info("Đã tải " + skills.size() + " kỹ năng.");
    }
    
    public String getRequiredRealm(String skillId) { me.yogurt0811.linhtien.skills.Skill s=getSkill(skillId); return s!=null?s.requiredRealm():null; }
    public me.yogurt0811.linhtien.skills.Skill getSkill(String id) { return skills.get(id.toLowerCase()); }
    public Map<String, me.yogurt0811.linhtien.skills.Skill> getAllSkills() { return Collections.unmodifiableMap(skills); }

    public void activateSkill(Player player, String skillId) {
        me.yogurt0811.linhtien.skills.Skill skill = getSkill(skillId);
        if (skill == null || skill.mythicSkillId() == null || !mythicMobsEnabled) return;

        long now = System.currentTimeMillis();
        long cooldownEnd = cooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>()).getOrDefault(skillId, 0L);
        if (now < cooldownEnd) {
            MessageUtils.sendMessage(player, "skill-on-cooldown", "skill_name", skill.name(), "time", String.valueOf((cooldownEnd-now)/1000+1));
            return;
        }

        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
        if (playerData == null) return;
        if (playerData.getCurrentMana() < skill.cost()) {
            MessageUtils.sendMessage(player, "skill-no-mana", "skill_name", skill.name());
            return;
        }

        Optional<Skill> optionalMythicSkill = MythicBukkit.inst().getSkillManager().getSkill(skill.mythicSkillId());
        if (optionalMythicSkill.isEmpty()) {
            plugin.getLogger().warning("Không thể tìm thấy MythicMobs Skill '" + skill.mythicSkillId() + "'!");
            return;
        }
        Skill mythicSkill = optionalMythicSkill.get();

        AbstractEntity caster = BukkitAdapter.adapt(player);
        
        // SỬA LỖI DỨT ĐIỂM: Lấy SkillCaster bằng MythicBukkit.inst().getSkillManager().getCaster(AbstractEntity)
        SkillCaster skillCaster = MythicBukkit.inst().getSkillManager().getCaster(caster);
        
        if (skillCaster == null) {
            plugin.getLogger().warning("Không thể tạo SkillCaster cho người chơi " + player.getName() + "!");
            return;
        }
        
        // SỬA LỖI DỨT ĐIỂM: Lấy SkillTrigger "API" bằng SkillTrigger.get("API")
        // MythicMobs có các trigger được định nghĩa sẵn, API là một trong số đó.
        // Đây là cách đúng đắn và tương thích với nhiều phiên bản của MM.
        SkillTrigger apiTrigger = SkillTrigger.get("API");
        if (apiTrigger == null) {
            plugin.getLogger().warning("Không tìm thấy SkillTrigger 'API'. Vui lòng kiểm tra cấu hình MythicMobs!");
            return; // Không thể kích hoạt skill nếu không có trigger này
        }

        SkillMetadata skillMetadata = new SkillMetadataImpl(
                apiTrigger,       // Dùng SkillTrigger đã lấy được
                skillCaster,      
                caster,          
                BukkitAdapter.adapt(player.getLocation()), 
                new HashSet<>(),  
                new HashSet<>(),  
                1.0f              
        );
        
        mythicSkill.execute(skillMetadata); 

        playerData.addMana(-skill.cost());
        cooldowns.get(player.getUniqueId()).put(skillId, now + (skill.cooldown() * 1000L));
    }
}
