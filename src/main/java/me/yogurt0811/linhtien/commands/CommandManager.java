package me.yogurt0811.linhtien.commands;

import me.yogurt0811.linhtien.LinhTienPlugin;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;

public class CommandManager {
    private final LinhTienPlugin plugin;

    public CommandManager(LinhTienPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerCommands() {
        // Lệnh cho người chơi luôn hoạt động
        registerCommand("tuluyen", new TuLuyenCommand(plugin));
        registerCommand("dotpha", new DotPhaCommand(plugin));
        registerCommand("tongmon", new TongMonCommand(plugin));
        registerCommand("linhtien", new LinhTienHelpCommand());

        // Đăng ký lệnh kỹ năng CÓ ĐIỀU KIỆN
        if (plugin.isSkillsEnabled()) {
            // Giả sử bạn có class KyNangCommand để xử lý lệnh /kynang.
            // Nếu không có class riêng, bạn có thể tạo một executor nhỏ ngay đây,
            // hoặc để trống nếu lệnh chỉ mở GUI và GUI tự xử lý.
            // Ở đây, chúng ta sẽ giả định lệnh này mở SkillGUI
             registerCommand("kynang", (sender, command, label, args) -> {
                if (!(sender instanceof org.bukkit.entity.Player player)) {
                    sender.sendMessage("Chỉ người chơi mới có thể dùng lệnh này.");
                    return true;
                }
                if (plugin.getSkillGUI() != null) {
                    plugin.getSkillGUI().open(player);
                }
                return true;
            });
        }

        // Lệnh admin
        LinhTienAdminCommand adminCommand = new LinhTienAdminCommand(plugin);
        PluginCommand ltaCmd = plugin.getCommand("linhtienadmin");
        if (ltaCmd != null) {
            ltaCmd.setExecutor(adminCommand);
            ltaCmd.setTabCompleter(adminCommand);
        }
    }

    private void registerCommand(String commandName, CommandExecutor executor) {
        PluginCommand command = plugin.getCommand(commandName);
        if (command != null) {
            command.setExecutor(executor);
        } else {
            // Không cảnh báo lỗi cho lệnh /kynang nếu nó không được đăng ký
            if (!commandName.equals("kynang")) {
                plugin.getLogger().warning("Lỗi khi đăng ký lệnh: " + commandName + ". Lệnh không được định nghĩa trong plugin.yml?");
            }
        }
    }
}