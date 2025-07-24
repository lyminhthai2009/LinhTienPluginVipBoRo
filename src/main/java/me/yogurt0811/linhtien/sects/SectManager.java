package me.yogurt0811.linhtien.sects;

import me.yogurt0811.linhtien.LinhTienPlugin;
import me.yogurt0811.linhtien.config.MessageUtils;
import me.yogurt0811.linhtien.data.PlayerData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Quản lý tất cả các hoạt động liên quan đến Tông Môn.
 * Bao gồm việc tải, lưu, tạo, xóa và quản lý thành viên.
 */
public class SectManager {
    private final LinhTienPlugin plugin;
    private final File sectsFolder; // Thư mục lưu dữ liệu của các tông môn
    private final Map<String, Sect> sectMap = new ConcurrentHashMap<>(); // Key là ID tông môn (viết liền, không dấu)

    public SectManager(LinhTienPlugin plugin) {
        this.plugin = plugin;
        this.sectsFolder = new File(plugin.getDataFolder(), "sects");
        if (!sectsFolder.exists()) {
            // Nếu thư mục chưa tồn tại, tạo nó
            if (!sectsFolder.mkdirs()) {
                plugin.getLogger().severe("KHÔNG THỂ TẠO THƯ MỤC LƯU DỮ LIỆU TÔNG MÔN!");
            }
        }
        loadSects();
    }

    /**
     * Tải tất cả các tông môn từ các file .yml trong thư mục /sects vào bộ nhớ.
     * Được gọi khi plugin bật hoặc reload.
     */
    public void loadSects() {
        sectMap.clear();
        File[] files = sectsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            try {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                String id = file.getName().replace(".yml", "");
                String displayName = config.getString("displayName");
                UUID owner = UUID.fromString(config.getString("owner"));

                Sect sect = new Sect(id, displayName, owner);

                // Tải danh sách thành viên từ file config
                ConfigurationSection membersSection = config.getConfigurationSection("members");
                if (membersSection != null) {
                    for (String uuidString : membersSection.getKeys(false)) {
                        UUID memberUuid = UUID.fromString(uuidString);
                        // Lấy rank từ config, nếu không có hoặc sai thì mặc định là MEMBER
                        SectRank rank = SectRank.valueOf(membersSection.getString(uuidString + ".rank", "MEMBER"));
                        
                        // Tông chủ đã được tự động thêm khi tạo Sect, nên ta bỏ qua ở đây
                        if (!memberUuid.equals(owner)) {
                            sect.addMember(memberUuid, rank);
                        }
                    }
                }
                sectMap.put(id, sect);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Lỗi khi tải tông môn từ file: " + file.getName(), e);
            }
        }
        plugin.getLogger().info("Đã tải " + sectMap.size() + " tông môn.");
    }

    /**
     * Lưu dữ liệu của một tông môn cụ thể vào file .yml.
     * @param sect Tông môn cần lưu.
     */
    public void saveSect(Sect sect) {
        File sectFile = new File(sectsFolder, sect.getId() + ".yml");
        FileConfiguration config = new YamlConfiguration();
        config.set("displayName", sect.getDisplayName());
        config.set("owner", sect.getOwner().toString());

        // Lưu thông tin của từng thành viên
        for (SectMember member : sect.getMembers().values()) {
            String path = "members." + member.uuid().toString();
            config.set(path + ".rank", member.rank().name());
            // Có thể lưu thêm các thông tin khác như điểm cống hiến ở đây
            // config.set(path + ".contribution", member.getContribution());
        }

        try {
            config.save(sectFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Không thể lưu tông môn: " + sect.getDisplayName(), e);
        }
    }

    /**
     * Lấy một tông môn bằng ID của nó.
     * @param id ID của tông môn (viết liền, không dấu).
     * @return Đối tượng Sect nếu tìm thấy, ngược lại là null.
     */
    public Sect getSectById(String id) {
        if (id == null) return null;
        return sectMap.get(id.toLowerCase());
    }

    /**
     * Lấy tông môn mà người chơi đang tham gia.
     * @param player Người chơi cần kiểm tra.
     * @return Đối tượng Sect nếu người chơi có tông môn, ngược lại là null.
     */
    public Sect getPlayerSect(Player player) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
        if (data == null || data.getSectId() == null) {
            return null;
        }
        return getSectById(data.getSectId());
    }

    /**
     * Xử lý logic tạo một tông môn mới.
     * @param creator Người chơi sáng lập tông môn.
     * @param name Tên hiển thị của tông môn.
     */
    public void createSect(Player creator, String name) {
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(creator);
        if (playerData == null) return; // Không thể tạo nếu dữ liệu người chơi chưa được tải

        if (playerData.getSectId() != null) {
            MessageUtils.sendMessage(creator, "sect-already-in-one");
            return;
        }

        // Tạo ID duy nhất bằng cách chuyển tên thành chữ thường và loại bỏ ký tự đặc biệt.
        String id = name.toLowerCase().replaceAll("[^a-z0-9]", "");
        if (id.isEmpty()) {
            MessageUtils.sendMessage(creator, "sect-invalid-name"); // Thêm tin nhắn này vào config.yml
            return;
        }

        if (sectMap.containsKey(id)) {
            MessageUtils.sendMessage(creator, "sect-name-taken");
            return;
        }
        
        // TODO: Thêm logic kiểm tra yêu cầu tạo tông môn (tiền, vật phẩm, cảnh giới...)
        // if (creator.getLevel() < 30) { ... }

        Sect newSect = new Sect(id, name, creator.getUniqueId());
        sectMap.put(id, newSect);

        // Cập nhật dữ liệu người chơi và lưu lại cả hai
        playerData.setSectId(id);
        plugin.getPlayerDataManager().savePlayerData(playerData);
        saveSect(newSect);

        MessageUtils.sendMessage(creator, "sect-created", "sect_name", name);
    }

    /**
     * Xử lý logic khi một người chơi rời khỏi tông môn của họ.
     * @param player Người chơi muốn rời tông môn.
     */
    public void leaveSect(Player player) {
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
        if (playerData == null) return;
        
        Sect sect = getPlayerSect(player);

        if (sect == null) {
            MessageUtils.sendMessage(player, "sect-not-in-one");
            return;
        }

        // Xử lý trường hợp đặc biệt: Tông chủ rời đi
        if (sect.getOwner().equals(player.getUniqueId())) {
            // Nếu còn thành viên khác, không cho rời, yêu cầu chuyển chức
            if (sect.getMembers().size() > 1) {
                MessageUtils.sendMessage(player, "sect-owner-cant-leave"); // Thêm tin nhắn này
                return;
            } else {
                // Nếu là thành viên cuối cùng, giải tán tông môn
                deleteSect(sect);
                MessageUtils.sendMessage(player, "sect-disbanded"); // Thêm tin nhắn này
            }
        } else {
            // Thành viên thường rời đi
            sect.removeMember(player.getUniqueId());
            MessageUtils.sendMessage(player, "sect-left");
        }
        
        playerData.setSectId(null);
        saveSect(sect);
        plugin.getPlayerDataManager().savePlayerData(playerData);
    }
    
    /**
     * Xóa hoàn toàn một tông môn khỏi hệ thống.
     * @param sect Tông môn cần xóa.
     */
    public void deleteSect(Sect sect) {
        // Xóa khỏi map
        sectMap.remove(sect.getId());
        
        // Xóa file dữ liệu
        File sectFile = new File(sectsFolder, sect.getId() + ".yml");
        if (sectFile.exists()) {
            sectFile.delete();
        }
        
        // Cập nhật tất cả thành viên (nếu có) để họ không còn trong tông môn nữa
        for (SectMember member : sect.getMembers().values()) {
            Player p = plugin.getServer().getPlayer(member.uuid());
            if (p != null && p.isOnline()) {
                PlayerData data = plugin.getPlayerDataManager().getPlayerData(p);
                if (data != null) {
                    data.setSectId(null);
                }
            } else {
                // TODO: Xử lý người chơi offline (cần một hệ thống hàng đợi hoặc sửa file trực tiếp)
            }
        }
    }
}