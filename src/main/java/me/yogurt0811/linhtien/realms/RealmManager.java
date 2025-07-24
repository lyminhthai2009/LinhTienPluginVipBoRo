package me.yogurt0811.linhtien.realms;

import me.yogurt0811.linhtien.LinhTienPlugin;
import me.yogurt0811.linhtien.config.MessageUtils;
import me.yogurt0811.linhtien.data.PlayerData;
import me.yogurt0811.linhtien.events.PlayerRealmChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class RealmManager {
    private final LinhTienPlugin plugin;
    private final List<Realm> realms = new ArrayList<>();
    private final Map<String, Realm> realmMap = new LinkedHashMap<>();

    public RealmManager(LinhTienPlugin plugin) {
        this.plugin = plugin;
        loadRealms();
    }
    
    private double getDoubleFromMap(Map<?, ?> map, String key, double defaultValue) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return defaultValue;
    }

    public void loadRealms() {
        realms.clear();
        realmMap.clear();

        FileConfiguration realmsConfig = plugin.getConfigManager().getRealmsConfig();
        List<Map<?, ?>> realmConfigList = realmsConfig.getMapList("realms");

        if (realmConfigList == null) {
            plugin.getLogger().severe("Không tìm thấy mục 'realms' hoặc nó không phải là một danh sách trong realms.yml!");
            return;
        }

        for (Map<?, ?> realmConfig : realmConfigList) {
            try {
                String id = (String) realmConfig.get("id");
                if (id == null) {
                    plugin.getLogger().warning("Một cảnh giới trong realms.yml không có ID. Bỏ qua...");
                    continue;
                }

                String displayName = (String) realmConfig.get("display-name");
                double expToNext = getDoubleFromMap(realmConfig, "exp-to-next", Double.MAX_VALUE);
                
                Map<String, Double> stats = new HashMap<>();
                Object statsObject = realmConfig.get("stats");
                if (statsObject instanceof Map) {
                    Map<?, ?> statsMap = (Map<?, ?>) statsObject;
                    for (Map.Entry<?, ?> entry : statsMap.entrySet()) {
                        if (entry.getKey() instanceof String && entry.getValue() instanceof Number) {
                            stats.put((String) entry.getKey(), ((Number) entry.getValue()).doubleValue());
                        }
                    }
                }
                
                List<RealmPhase> phases = new ArrayList<>();
                Object phasesObject = realmConfig.get("phases");
                if (phasesObject instanceof List) {
                    List<?> phaseListRaw = (List<?>) phasesObject;
                    for (Object phaseObj : phaseListRaw) {
                        if (phaseObj instanceof Map) {
                            Map<?, ?> phaseMap = (Map<?, ?>) phaseObj;
                            String phaseName = (String) phaseMap.get("display-name");
                            double threshold = getDoubleFromMap(phaseMap, "exp-threshold", 0.0);
                            phases.add(new RealmPhase(phaseName, threshold));
                        }
                    }
                }

                phases.sort((p1, p2) -> Double.compare(p2.expThreshold(), p1.expThreshold()));

                Realm realm = new Realm(id, displayName, expToNext, stats, phases);
                realms.add(realm);
                realmMap.put(id.toLowerCase(), realm);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Lỗi khi xử lý một cảnh giới trong realms.yml. Vui lòng kiểm tra định dạng.", e);
            }
        }
        plugin.getLogger().info("Đã tải " + realms.size() + " cảnh giới từ realms.yml");
    }

    public Realm getRealmById(String id) {
        if (id == null) return null;
        return realmMap.get(id.toLowerCase());
    }

    public Realm getPlayerRealm(PlayerData data) {
        if (data == null) return getFirstRealm();
        return getRealmById(data.getRealmId());
    }

    public RealmPhase getPlayerPhase(PlayerData data) {
        Realm realm = getPlayerRealm(data);
        if (realm == null || realm.phases().isEmpty()) return null;

        double progress = data.getCurrentExp() / realm.expToNext();
        if (Double.isInfinite(progress) || Double.isNaN(progress)) {
            progress = 0.0;
        }

        for (RealmPhase phase : realm.phases()) {
            if (progress >= phase.expThreshold()) {
                return phase;
            }
        }
        // Fallback for safety, should not be reached if sorted correctly
        return realm.phases().get(realm.phases().size() - 1);
    }

    public Realm getFirstRealm() {
        return realms.isEmpty() ? null : realms.get(0);
    }

    public Realm getNextRealm(String currentRealmId) {
        for (int i = 0; i < realms.size() - 1; i++) {
            if (realms.get(i).id().equalsIgnoreCase(currentRealmId)) {
                return realms.get(i + 1);
            }
        }
        return null;
    }

    public List<Realm> getRealms() {
        return Collections.unmodifiableList(realms);
    }
    
    /**
     * Xử lý logic thăng cấp cho người chơi sau khi đột phá thành công.
     * @param player Người chơi được thăng cấp.
     * @param data Dữ liệu của người chơi.
     * @param oldRealm Cảnh giới cũ.
     * @param newRealm Cảnh giới mới.
     */
    public void promotePlayer(Player player, PlayerData data, Realm oldRealm, Realm newRealm) {
        // Cập nhật dữ liệu người chơi
        data.setRealmId(newRealm.id());
        data.setCurrentExp(0);
        plugin.getPlayerDataManager().savePlayerData(data);
        
        // Gửi tin nhắn thành công cho người chơi
        MessageUtils.sendMessage(player, "breakthrough-success", "realm_name", newRealm.displayName());
        
        // Thông báo cho toàn server
        String broadcastMessage = "&b&l[Thiên Đạo Bảng] &fChúc mừng đạo hữu &e" + player.getName() + "&f đã nghịch thiên hành sự, độ kiếp thành công, chính thức bước vào cảnh giới &b" + newRealm.displayName() + "&f!";
        Bukkit.broadcast(MessageUtils.format(broadcastMessage));
        
        // Gọi Event để các hệ thống khác (như StatManager) có thể bắt và cập nhật chỉ số
        Bukkit.getPluginManager().callEvent(new PlayerRealmChangeEvent(player, oldRealm, newRealm));
    }
}