package me.yogurt0811.linhtien;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.yogurt0811.linhtien.api.LinhTienAPI;
import me.yogurt0811.linhtien.api.implementation.LinhTienAPIImpl;
import me.yogurt0811.linhtien.commands.CommandManager;
import me.yogurt0811.linhtien.config.ConfigManager;
import me.yogurt0811.linhtien.data.PlayerDataManager;
import me.yogurt0811.linhtien.gui.SkillGUI;
import me.yogurt0811.linhtien.listeners.PlayerConnectionListener;
import me.yogurt0811.linhtien.listeners.SkillItemListener;
import me.yogurt0811.linhtien.listeners.TuLuyenListener;
import me.yogurt0811.linhtien.placeholders.LinhTienExpansion;
import me.yogurt0811.linhtien.practice.PracticeManager;
import me.yogurt0811.linhtien.realms.RealmManager;
import me.yogurt0811.linhtien.sects.SectManager;
import me.yogurt0811.linhtien.skills.PlayerSkillItemManager;
import me.yogurt0811.linhtien.skills.SkillBookManager;
import me.yogurt0811.linhtien.skills.SkillManager;
import me.yogurt0811.linhtien.stats.StatManager;
import me.yogurt0811.linhtien.talents.TalentManager;
import me.yogurt0811.linhtien.tasks.LoiKiepTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public final class LinhTienPlugin extends JavaPlugin {

    private static LinhTienPlugin instance;
    private static LinhTienAPI api;

    private ConfigManager configManager;
    private RealmManager realmManager;
    private PlayerDataManager playerDataManager;
    private PracticeManager practiceManager;
    private TalentManager talentManager;
    private SectManager sectManager;
    private StatManager statManager;

    private SkillManager skillManager;
    private SkillGUI skillGUI;
    private PlayerSkillItemManager playerSkillItemManager;
    private SkillBookManager skillBookManager;

    private boolean skillsEnabled;
    private boolean gsitEnabled;
    private final Map<UUID, LoiKiepTask> activeTribulations = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;

        this.configManager = new ConfigManager(this);
        this.configManager.loadConfigs();
        
        this.skillsEnabled = getConfig().getBoolean("skills.enabled", true);
        this.gsitEnabled = getServer().getPluginManager().getPlugin("GSit") != null;

        if (gsitEnabled) {
            getLogger().info("Đã hook thành công vào GSit.");
        } else {
            getLogger().warning("Không tìm thấy GSit. Tính năng ngồi tu luyện sẽ bị vô hiệu hóa, người chơi có thể di chuyển khi tu luyện.");
        }

        this.realmManager = new RealmManager(this);
        this.playerDataManager = new PlayerDataManager(this);
        this.practiceManager = new PracticeManager(this);
        this.talentManager = new TalentManager(this);
        this.sectManager = new SectManager(this);
        this.statManager = new StatManager(this);
        
        if (this.skillsEnabled) {
            getLogger().info("Hệ thống Kỹ năng đang được bật.");
            this.skillManager = new SkillManager(this);
            this.skillGUI = new SkillGUI(this);
            this.playerSkillItemManager = new PlayerSkillItemManager(this);
            this.skillBookManager = new SkillBookManager(this);
        } else {
            getLogger().warning("Hệ thống Kỹ năng đã bị tắt trong config.yml.");
        }

        new CommandManager(this).registerCommands();
        registerListeners();
        setupPlaceholderAPI();
        generateSkillGuide();

        api = new LinhTienAPIImpl(this);
        getServer().getServicesManager().register(LinhTienAPI.class, api, this, ServicePriority.Normal);
        getLogger().info("LinhTienPluginVipBoRo v" + getDescription().getVersion() + " đã được bật!");
    }

    @Override
    public void onDisable() {
        for (LoiKiepTask task : activeTribulations.values()) {
            task.cancel();
        }
        activeTribulations.clear();
        
        if (playerDataManager != null) playerDataManager.saveAllPlayerData();
        getLogger().info("LinhTienPluginVipBoRo đã được tắt.");
    }

    public void reloadPlugin() {
        configManager.loadConfigs();
        this.skillsEnabled = getConfig().getBoolean("skills.enabled", true);
        
        realmManager.loadRealms();
        talentManager.loadTalents();
        practiceManager.reload();
        sectManager.loadSects();
        
        if (this.skillsEnabled && skillManager != null) {
            skillManager.loadSkills();
        }
        
        getLogger().info("Tất cả cấu hình đã được tải lại thành công.");
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
        getServer().getPluginManager().registerEvents(this.statManager, this);
        
        if (this.skillsEnabled) {
            getServer().getPluginManager().registerEvents(new SkillItemListener(this), this);
        }

        if (this.gsitEnabled) {
            getServer().getPluginManager().registerEvents(new TuLuyenListener(this), this);
        }
    }

    private void setupPlaceholderAPI() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new LinhTienExpansion(this).register();
            getLogger().info("Đã kết nối thành công với PlaceholderAPI.");
        } else {
            getLogger().warning("Không tìm thấy PlaceholderAPI.");
        }
    }

    private void generateSkillGuide() {
        if (!this.skillsEnabled) return;

        File guideFile = new File(getDataFolder(), "cachtaoskill.txt");
        if (!guideFile.exists()) {
            try (InputStream in = getResource("cachtaoskill.txt")) {
                if (in == null) {
                    getLogger().warning("Không tìm thấy cachtaoskill.txt trong jar!");
                    return;
                }
                Files.copy(in, guideFile.toPath());
                getLogger().info("Đã tạo file hướng dẫn 'cachtaoskill.txt'.");
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Không thể tạo file 'cachtaoskill.txt'.", e);
            }
        }
    }

    public boolean isSkillsEnabled() {
        return this.skillsEnabled;
    }

    public boolean isGsitEnabled() {
        return this.gsitEnabled;
    }

    public void addActiveTribulation(Player player, LoiKiepTask task) {
        activeTribulations.put(player.getUniqueId(), task);
    }

    public void removeActiveTribulation(Player player) {
        activeTribulations.remove(player.getUniqueId());
    }

    public boolean isPlayerInTribulation(Player player) {
        return activeTribulations.containsKey(player.getUniqueId());
    }

    public static LinhTienPlugin getInstance() { return instance; }
    public static LinhTienAPI getApi() { return api; }
    public ConfigManager getConfigManager() { return configManager; }
    public RealmManager getRealmManager() { return realmManager; }
    public PlayerDataManager getPlayerDataManager() { return playerDataManager; }
    public PracticeManager getPracticeManager() { return practiceManager; }
    public TalentManager getTalentManager() { return talentManager; }
    public SectManager getSectManager() { return sectManager; }
    public StatManager getStatManager() { return statManager; }
    public SkillManager getSkillManager() { return skillManager; }
    public SkillGUI getSkillGUI() { return skillGUI; }
    public PlayerSkillItemManager getPlayerSkillItemManager() { return playerSkillItemManager; }
    public SkillBookManager getSkillBookManager() { return skillBookManager; }
}