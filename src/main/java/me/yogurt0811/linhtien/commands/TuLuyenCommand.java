package me.yogurt0811.linhtien.commands;

import dev.geco.gsit.api.GSitAPI;
import me.yogurt0811.linhtien.LinhTienPlugin;
import me.yogurt0811.linhtien.config.MessageUtils;
import me.yogurt0811.linhtien.practice.PracticeManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TuLuyenCommand implements CommandExecutor {
    private final LinhTienPlugin plugin;
    private final PracticeManager practiceManager;

    public TuLuyenCommand(LinhTienPlugin plugin) {
        this.plugin = plugin;
        this.practiceManager = plugin.getPracticeManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtils.sendMessage(sender, "player-only-command");
            return true;
        }

        boolean isPracticing = practiceManager.isPracticing(player);

        if (isPracticing) {
            // Nếu đang tu luyện -> dừng lại
            practiceManager.stopPractice(player); // Đã bao gồm cả việc cho đứng dậy nếu có GSit
            MessageUtils.sendMessage(player, "practice-stop");
        } else {
            // Nếu chưa tu luyện -> bắt đầu
            if (plugin.isGsitEnabled()) {
                // Cố gắng cho người chơi ngồi xuống. GSit sẽ tự tìm vị trí thích hợp.
                boolean successfullySat = GSitAPI.sitPlayer(player);
                if (!successfullySat) {
                    player.sendMessage(MessageUtils.format(plugin.getConfigManager().getConfig().getString("prefix") + "&cKhông có vị trí thích hợp để tĩnh tọa!"));
                    return true;
                }
            }
            // Sau khi ngồi (hoặc nếu không có GSit), bắt đầu tu luyện
            practiceManager.startPractice(player);
            MessageUtils.sendMessage(player, "practice-start");
        }
        return true;
    }
}