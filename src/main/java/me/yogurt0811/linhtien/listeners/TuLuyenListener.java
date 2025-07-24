package me.yogurt0811.linhtien.listeners;

import dev.geco.gsit.api.event.EntityStopSitEvent;
import me.yogurt0811.linhtien.LinhTienPlugin;
import me.yogurt0811.linhtien.config.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TuLuyenListener implements Listener {

    private final LinhTienPlugin plugin;

    public TuLuyenListener(LinhTienPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Bắt sự kiện khi một thực thể (bao gồm người chơi) ngừng ngồi.
     * Sự kiện này được cung cấp bởi GSit API.
     */
    @EventHandler
    public void onPlayerStopSitting(EntityStopSitEvent event) {
        // Chỉ xử lý nếu thực thể là người chơi
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // Kiểm tra xem người chơi có đang trong danh sách tu luyện không
        if (plugin.getPracticeManager().isPracticing(player)) {
            // Dừng tu luyện.
            // Chú ý: không gọi stopPractice() ở đây để tránh vòng lặp vô hạn
            // (stopPractice -> stopSit -> event này -> stopPractice)
            // Ta chỉ cần xóa người chơi khỏi danh sách.
            plugin.getPracticeManager().getPracticingPlayers().remove(player.getUniqueId());
            MessageUtils.sendMessage(player, "practice-stop");
        }
    }
}