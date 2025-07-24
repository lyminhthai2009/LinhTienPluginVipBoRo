package me.yogurt0811.linhtien.commands;

import me.yogurt0811.linhtien.LinhTienPlugin;
import me.yogurt0811.linhtien.config.MessageUtils;
import me.yogurt0811.linhtien.data.PlayerData;
import me.yogurt0811.linhtien.realms.Realm;
import me.yogurt0811.linhtien.tasks.LoiKiepTask;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class DotPhaCommand implements CommandExecutor {

    private final LinhTienPlugin plugin;
    private final File loiKiepFile;
    private FileConfiguration loiKiepConfig;

    public DotPhaCommand(LinhTienPlugin plugin) {
        this.plugin = plugin;
        this.loiKiepFile = new File(plugin.getDataFolder(), "loikiep.yml");
        if (!loiKiepFile.exists()) {
            plugin.saveResource("loikiep.yml", false);
        }
        this.loiKiepConfig = YamlConfiguration.loadConfiguration(loiKiepFile);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtils.sendMessage(sender, "player-only-command");
            return true;
        }

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
        if (data == null) return true;

        if (plugin.isPlayerInTribulation(player)) {
            player.sendMessage(MessageUtils.format(plugin.getConfigManager().getConfig().getString("prefix") + "&cBạn đang trong quá trình độ kiếp, không thể phân tâm!"));
            return true;
        }

        Realm currentRealm = plugin.getRealmManager().getPlayerRealm(data);
        if (currentRealm == null) return true;

        if (data.getCurrentExp() < currentRealm.expToNext()) {
            MessageUtils.sendMessage(player, "breakthrough-not-ready");
            return true;
        }

        Realm nextRealm = plugin.getRealmManager().getNextRealm(currentRealm.id());
        if (nextRealm == null) {
            MessageUtils.sendMessage(player, "breakthrough-max-realm");
            return true;
        }

        // Tải lại config mỗi lần chạy để nhận thay đổi mà không cần reload plugin
        this.loiKiepConfig = YamlConfiguration.loadConfiguration(loiKiepFile);
        String path = "loi_kiep." + nextRealm.id();

        // Kiểm tra xem cảnh giới tiếp theo có cấu hình lôi kiếp không
        if (!loiKiepConfig.isConfigurationSection(path)) {
            // Nếu không có, đột phá thành công ngay lập tức
            plugin.getRealmManager().promotePlayer(player, data, currentRealm, nextRealm);
            return true;
        }
        
        ConfigurationSection tribulationConfig = loiKiepConfig.getConfigurationSection(path);
        if (tribulationConfig == null) {
            // Fallback an toàn
            plugin.getRealmManager().promotePlayer(player, data, currentRealm, nextRealm);
            return true;
        }
        
        // Bắt đầu tác vụ Lôi Kiếp, chờ 2 giây rồi bắt đầu
        new LoiKiepTask(plugin, player, data, currentRealm, nextRealm, tribulationConfig).runTaskTimer(plugin, 40L, 20L);

        return true;
    }
}