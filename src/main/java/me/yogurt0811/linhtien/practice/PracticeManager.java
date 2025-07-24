package me.yogurt0811.linhtien.practice;

import dev.geco.gsit.api.GSitAPI;
import me.yogurt0811.linhtien.LinhTienPlugin;
import me.yogurt0811.linhtien.data.PlayerData;
import me.yogurt0811.linhtien.talents.Talent;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PracticeManager {
    private final LinhTienPlugin plugin;
    private final Set<UUID> practicingPlayers = new HashSet<>();
    private BukkitTask practiceTask;

    private double expPerSecond;
    private boolean visualsEnabled;
    private Particle particle;

    public PracticeManager(LinhTienPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        if (practiceTask != null) {
            practiceTask.cancel();
        }
        
        ConfigurationSection config = plugin.getConfigManager().getConfig();
        this.expPerSecond = config.getDouble("practice.exp-per-second", 10);
        
        this.visualsEnabled = config.getBoolean("practice.visuals.enabled", true);
        try {
            this.particle = Particle.valueOf(config.getString("practice.visuals.particle", "ENCHANTMENT_TABLE").toUpperCase());
        } catch (IllegalArgumentException e) {
            this.particle = Particle.ENCHANTMENT_TABLE;
            plugin.getLogger().warning("Particle không hợp lệ trong practice.visuals, đã đặt về mặc định ENCHANTMENT_TABLE.");
        }

        startPracticeTask();
    }

    /**
     * Bắt đầu quá trình tu luyện cho người chơi.
     * @param player Người chơi bắt đầu tu luyện.
     */
    public void startPractice(Player player) {
        if (!isPracticing(player)) {
            practicingPlayers.add(player.getUniqueId());
        }
    }

    /**
     * Dừng quá trình tu luyện cho người chơi.
     * @param player Người chơi dừng tu luyện.
     */
    public void stopPractice(Player player) {
        if (isPracticing(player)) {
            practicingPlayers.remove(player.getUniqueId());
            // Nếu người chơi đang ngồi (do GSit), buộc họ đứng dậy
            if (plugin.isGsitEnabled() && GSitAPI.isSitting(player)) {
                GSitAPI.stopSit(player);
            }
        }
    }

    public boolean isPracticing(Player player) {
        return practicingPlayers.contains(player.getUniqueId());
    }

    public Set<UUID> getPracticingPlayers() {
        return practicingPlayers;
    }

    private void startPracticeTask() {
        practiceTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Sử dụng new HashSet<>(...) để tránh lỗi ConcurrentModificationException
                for (UUID uuid : new HashSet<>(practicingPlayers)) {
                    Player player = plugin.getServer().getPlayer(uuid);
                    if (player == null || !player.isOnline()) {
                        practicingPlayers.remove(uuid);
                        continue;
                    }
                    
                    // Thêm tu vi sau mỗi 10 ticks (nửa giây) để giảm tải
                    if (plugin.getServer().getCurrentTick() % 10 == 0) {
                        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
                        if (data == null) continue;
                        
                        Talent talent = plugin.getTalentManager().getTalent(data.getTalentId());
                        double bonusMultiplier = (talent != null) ? talent.expBonus() : 1.0;
                        
                        // Lượng exp mỗi nửa giây
                        data.addExp((expPerSecond / 2.0) * bonusMultiplier);
                    }

                    // Hiệu ứng hạt
                    if (visualsEnabled) {
                        player.getWorld().spawnParticle(particle, player.getLocation().add(0, 1, 0), 1, 0.5, 0.5, 0.5, 0);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L); // Chạy mỗi tick để hiệu ứng mượt mà
    }
}