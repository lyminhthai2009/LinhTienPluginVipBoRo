package me.yogurt0811.linhtien.commands;

import me.yogurt0811.linhtien.config.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class LinhTienHelpCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Hiển thị menu trợ giúp cho người chơi
        sender.sendMessage(MessageUtils.format("&8&m                                                                                "));
        sender.sendMessage(MessageUtils.format("                    &b&lLINH TIÊN PLUGIN &f- &7Trợ giúp"));
        sender.sendMessage(MessageUtils.format(""));
        sender.sendMessage(MessageUtils.format(" &e/tuluyen &8- &7Bắt đầu/dừng tu luyện để tăng tu vi."));
        sender.sendMessage(MessageUtils.format(" &e/dotpha &8- &7Thử đột phá lên cảnh giới tiếp theo."));
        sender.sendMessage(MessageUtils.format(" &e/tongmon &8- &7Các lệnh liên quan đến Tông Môn."));
        sender.sendMessage(MessageUtils.format(" &e/kynang &8- &7Mở bảng kỹ năng của bạn."));
        sender.sendMessage(MessageUtils.format(""));
        sender.sendMessage(MessageUtils.format(" &7Để xem thông tin tu vi, hãy sử dụng các plugin scoreboard hoặc chat có PlaceholderAPI."));
        sender.sendMessage(MessageUtils.format("&8&m                                                                                "));
        return true;
    }
}