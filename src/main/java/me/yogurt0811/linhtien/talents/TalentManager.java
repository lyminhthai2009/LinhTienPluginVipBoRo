package me.yogurt0811.linhtien.talents;

import me.yogurt0811.linhtien.LinhTienPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Collections; // Import thêm
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class TalentManager {
    private final LinhTienPlugin plugin;
    private final Map<String, Talent> talentMap = new ConcurrentHashMap<>();
    private final List<String> talentIds = new ArrayList<>();

    public TalentManager(LinhTienPlugin plugin) {
        this.plugin = plugin;
        loadTalents();
    }

    public void loadTalents() {
        talentMap.clear();
        talentIds.clear();
        FileConfiguration talentsConfig = plugin.getConfigManager().loadCustomConfig("talents.yml");
        ConfigurationSection section = talentsConfig.getConfigurationSection("talents");
        if (section == null) {
            plugin.getLogger().severe("Không tìm thấy mục 'talents' trong talents.yml!");
            return;
        }

        for (String id : section.getKeys(false)) {
            String displayName = section.getString(id + ".display-name", "Unknown Talent");
            double expBonus = section.getDouble(id + ".exp-bonus", 1.0);
            double breakthroughBonus = section.getDouble(id + ".breakthrough-bonus", 0.0);
            Talent talent = new Talent(id, displayName, expBonus, breakthroughBonus);
            talentMap.put(id, talent);
            talentIds.add(id);
        }
        plugin.getLogger().info("Đã tải " + talentMap.size() + " tư chất.");
    }

    public Talent getTalent(String id) {
        return talentMap.get(id);
    }

    public Talent getRandomTalent() {
        if (talentIds.isEmpty()) return null;
        String randomId = talentIds.get(ThreadLocalRandom.current().nextInt(talentIds.size()));
        return getTalent(randomId);
    }

    /**
     * SỬA LỖI: Thêm phương thức này để LinhTienAdminCommand có thể truy cập.
     * Trả về một Map chỉ đọc để đảm bảo an toàn.
     * @return Map chứa tất cả các talent.
     */
    public Map<String, Talent> getTalentMap() {
        return Collections.unmodifiableMap(talentMap);
    }
}