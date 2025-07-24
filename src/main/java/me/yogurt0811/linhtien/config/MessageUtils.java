package me.yogurt0811.linhtien.config;

import me.yogurt0811.linhtien.LinhTienPlugin; // <-- THÊM DÒNG NÀY ĐỂ SỬA LỖI
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

public class MessageUtils {
    private static String prefix;
    private static FileConfiguration messagesConfig;
    private static final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.builder().character('&').hexColors().build();

    public static void initialize(LinhTienPlugin plugin) {
        messagesConfig = plugin.getConfigManager().getConfig();
        prefix = messagesConfig.getString("prefix", "&8[&bLinh&3Tiên&8] &r");
    }

    public static void sendMessage(CommandSender sender, String path, String... replacements) {
        String message = messagesConfig.getString("messages." + path, "&cMessage not found: messages." + path);
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                // Sử dụng replaceAll để tránh lỗi với các ký tự đặc biệt trong regex
                message = message.replace("{" + replacements[i] + "}", replacements[i + 1]);
            }
        }
        sender.sendMessage(format(prefix + message));
    }
    
    public static Component format(String message) {
        if (message == null || message.isEmpty()) {
            return Component.empty();
        }
        return legacySerializer.deserialize(message);
    }
    
    public static String stripColor(String messageWithColorCodes) {
        if (messageWithColorCodes == null) return "";
        // Chuyển đổi message có mã màu (&) sang Component, rồi serialize lại thành chuỗi không màu.
        Component component = legacySerializer.deserialize(messageWithColorCodes);
        return LegacyComponentSerializer.legacySection().serialize(component).replaceAll("§.", "");
    }
}