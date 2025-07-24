package me.yogurt0811.linhtien.config;

import me.yogurt0811.linhtien.LinhTienPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ConfigManager {
    private final LinhTienPlugin plugin;
    private FileConfiguration config;
    private FileConfiguration realmsConfig;
    private FileConfiguration skillsConfig;

    public ConfigManager(LinhTienPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadConfigs() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig(); // Tải lại file config.yml
        config = plugin.getConfig();

        realmsConfig = loadCustomConfig("realms.yml");
        skillsConfig = loadCustomConfig("skills.yml");

        MessageUtils.initialize(plugin);
    }

    /**
     * Tải một file config tùy chỉnh.
     * Sửa lỗi: Chuyển từ private sang public để các Manager khác có thể dùng.
     * @param fileName Tên file (ví dụ: "realms.yml")
     * @return FileConfiguration đã được tải.
     */
    public FileConfiguration loadCustomConfig(String fileName) {
        File configFile = new File(plugin.getDataFolder(), fileName);
        if (!configFile.exists()) {
            plugin.saveResource(fileName, false);
        }
        return YamlConfiguration.loadConfiguration(configFile);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getRealmsConfig() {
        return realmsConfig;
    }

    public FileConfiguration getSkillsConfig() {
        return skillsConfig;
    }
}