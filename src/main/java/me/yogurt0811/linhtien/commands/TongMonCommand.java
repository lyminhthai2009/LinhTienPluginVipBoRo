package me.yogurt0811.linhtien.commands;

import me.yogurt0811.linhtien.LinhTienPlugin;
import me.yogurt0811.linhtien.config.MessageUtils;
import me.yogurt0811.linhtien.sects.SectManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TongMonCommand implements CommandExecutor {

    private final SectManager sectManager;

    public TongMonCommand(LinhTienPlugin plugin) {
        this.sectManager = plugin.getSectManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtils.sendMessage(sender, "player-only-command");
            return true;
        }

        if (args.length == 0) {
            // Hiển thị info tông môn của mình hoặc hướng dẫn
            // TODO: Hiển thị GUI thông tin tông môn
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "create":
                if (args.length < 2) {
                    player.sendMessage(MessageUtils.format("&cCách dùng: /tongmon create <Tên Tông Môn>"));
                    return true;
                }
                String sectName = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
                sectManager.createSect(player, sectName);
                break;
            case "leave":
                sectManager.leaveSect(player);
                break;
            // TODO: Thêm các subcommand khác: invite, kick, join, info...
            default:
                player.sendMessage(MessageUtils.format("&cLệnh không tồn tại."));
                break;
        }
        return true;
    }
}