package me.yogurt0811.linhtien.data;

import me.yogurt0811.linhtien.LinhTienPlugin;
import me.yogurt0811.linhtien.config.MessageUtils;
import me.yogurt0811.linhtien.realms.Realm;
import me.yogurt0811.linhtien.talents.Talent;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class PlayerDataManager {
    private final LinhTienPlugin plugin;
    private final Map<UUID, PlayerData> playerDataMap = new ConcurrentHashMap<>();
    private final File dataFolder;

    public PlayerDataManager(LinhTienPlugin plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    public PlayerData getPlayerData(Player player) {
        return playerDataMap.get(player.getUniqueId());
    }

    public void loadPlayerData(Player player) {
        UUID uuid = player.getUniqueId();
        File playerFile = new File(dataFolder, uuid + ".yml");

        if (!playerFile.exists()) {
            Realm firstRealm = plugin.getRealmManager().getFirstRealm();
            Talent randomTalent = plugin.getTalentManager().getRandomTalent();
            String talentId = (randomTalent != null) ? randomTalent.id() : "pham_the";

            PlayerData newData = new PlayerData(uuid, (firstRealm != null) ? firstRealm.id() : "pham_nhan", 0.0, talentId);
            newData.learnSkill("hoa_cau_thuat"); 

            playerDataMap.put(uuid, newData);
            savePlayerData(newData);

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                String talentName = (randomTalent != null) ? randomTalent.displayName() : "&7Phàm Thể";
                MessageUtils.sendMessage(player, "talent-assigned", "talent_name", talentName);
            });
            return;
        }

        FileConfiguration dataConfig = YamlConfiguration.loadConfiguration(playerFile);
        String realmId = dataConfig.getString("realmId", "pham_nhan");
        double currentExp = dataConfig.getDouble("currentExp", 0.0);
        double currentMana = dataConfig.getDouble("currentMana", -1.0);
        String talentId = dataConfig.getString("talentId", "pham_the");
        String sectId = dataConfig.getString("sectId");
        List<String> skills = dataConfig.getStringList("skillSlots");
        List<String> learnedSkills = dataConfig.getStringList("learnedSkills");

        PlayerData loadedData = new PlayerData(uuid, realmId, currentExp, talentId);
        loadedData.setCurrentMana(currentMana);
        loadedData.setSectId(sectId);
        for(int i = 0; i < skills.size() && i < 4; i++) {
            if (skills.get(i) != null && !skills.get(i).equalsIgnoreCase("null")) {
                loadedData.setSkillInSlot(i, skills.get(i));
            }
        }
        if (learnedSkills != null) {
            learnedSkills.forEach(loadedData::learnSkill);
        }
        playerDataMap.put(uuid, loadedData);
    }

    public void savePlayerData(PlayerData playerData) {
        File playerFile = new File(dataFolder, playerData.getUuid() + ".yml");
        FileConfiguration dataConfig = new YamlConfiguration();
        dataConfig.set("realmId", playerData.getRealmId());
        dataConfig.set("currentExp", playerData.getCurrentExp());
        dataConfig.set("currentMana", playerData.getCurrentMana());
        dataConfig.set("talentId", playerData.getTalentId());
        dataConfig.set("sectId", playerData.getSectId());
        dataConfig.set("skillSlots", playerData.getSkillSlots());
        dataConfig.set("learnedSkills", playerData.getLearnedSkills());
        try {
            dataConfig.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Không thể lưu dữ liệu cho người chơi " + playerData.getUuid(), e);
        }
    }
    
    public void unloadPlayerData(Player player) {
        PlayerData data = playerDataMap.remove(player.getUniqueId());
        if (data != null) {
            savePlayerData(data);
        }
    }

    public void saveAllPlayerData() {
        plugin.getLogger().info("Đang lưu dữ liệu của tất cả người chơi...");
        playerDataMap.values().forEach(this::savePlayerData);
        plugin.getLogger().info("Lưu dữ liệu hoàn tất.");
    }

    public void updateOfflinePlayerData(UUID uuid, String key, Object value) {
        if (playerDataMap.containsKey(uuid)) {
            PlayerData data = playerDataMap.get(uuid);
            if ("sectId".equals(key)) {
                data.setSectId((String) value);
            }
            savePlayerData(data);
            return;
        }

        File playerFile = new File(dataFolder, uuid + ".yml");
        if (!playerFile.exists()) { return; }

        FileConfiguration dataConfig = YamlConfiguration.loadConfiguration(playerFile);
        dataConfig.set(key, value);
        try {
            dataConfig.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Không thể cập nhật dữ liệu offline cho người chơi " + uuid, e);
        }
    }
}