package me.yogurt0811.linhtien.tasks;

import me.yogurt0811.linhtien.LinhTienPlugin;
import me.yogurt0811.linhtien.config.MessageUtils;
import me.yogurt0811.linhtien.data.PlayerData;
import me.yogurt0811.linhtien.realms.Realm;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class LoiKiepTask extends BukkitRunnable implements Listener {

    private final LinhTienPlugin plugin;
    private final Player player;
    private final PlayerData playerData;
    private final Realm oldRealm;
    private final Realm nextRealm;

    private final int totalStrikes;
    private int strikesLeft;
    private final double damagePerStrike;
    private final long intervalTicks;
    private long timer;
    private boolean failed = false;

    public LoiKiepTask(LinhTienPlugin plugin, Player player, PlayerData playerData, Realm oldRealm, Realm nextRealm, ConfigurationSection config) {
        this.plugin = plugin;
        this.player = player;
        this.playerData = playerData;
        this.oldRealm = oldRealm;
        this.nextRealm = nextRealm;

        this.totalStrikes = config.getInt("so-lan-danh", 3);
        this.strikesLeft = this.totalStrikes;
        this.damagePerStrike = config.getDouble("sat-thuong", 2.0);
        this.intervalTicks = (long) (config.getDouble("thoi-gian-cho", 3.0) * 20);
        this.timer = intervalTicks; // Bắt đầu đánh sau khoảng chờ đầu tiên
        
        String startMessage = config.getString("thong-bao-bat-dau", "&c&lThiên kiếp đã tới!");
        player.sendMessage(MessageUtils.format(startMessage.replace("{so_lan}", String.valueOf(this.totalStrikes))));
        player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 0.5f);

        Bukkit.getPluginManager().registerEvents(this, plugin);
        plugin.addActiveTribulation(player, this);
    }

    @Override
    public void run() {
        if (player == null || !player.isOnline() || failed) {
            cancelTask(true);
            return;
        }

        timer -= 20;
        if (timer <= 0) {
            if (strikesLeft > 0) {
                World world = player.getWorld();
                Location loc = player.getLocation();
                world.strikeLightningEffect(loc);
                
                // Kiểm tra lại nếu người chơi còn sống trước khi gây sát thương
                if (player.isDead()) {
                    failed = true;
                    return; // Chờ event xử lý
                }
                player.damage(damagePerStrike);
                
                int currentStrikeNumber = totalStrikes - strikesLeft + 1;
                player.sendMessage(MessageUtils.format("&c&lĐạo thiên lôi thứ " + currentStrikeNumber + "!"));
                
                strikesLeft--;
                timer = intervalTicks;
            } else {
                plugin.getRealmManager().promotePlayer(player, playerData, oldRealm, nextRealm);
                cancelTask(false);
            }
        }
    }
    
    private void cancelTask(boolean didFail) {
        if (didFail) {
            playerData.setCurrentExp(0);
            plugin.getPlayerDataManager().savePlayerData(playerData);
            MessageUtils.sendMessage(player, "breakthrough-fail");
        }
        
        HandlerList.unregisterAll(this);
        plugin.removeActiveTribulation(player);
        
        try {
            this.cancel();
        } catch (IllegalStateException ignored) {}
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getPlayer().equals(player)) {
            this.failed = true;
            String deathMessage = plugin.getConfigManager().getConfig().getString("prefix") + "&4&lThân thể không chịu nổi thiên uy, độ kiếp thất bại! Tu vi bị đả kích, phải tu luyện lại từ đầu!";
            Bukkit.broadcast(MessageUtils.format("&b&l[Thiên Đạo Bảng] &fTiếc thay! Đạo hữu &e" + player.getName() + "&f đã thất bại khi độ kiếp, thân tử đạo tiêu!"));
            // Gửi tin nhắn sau 1 tick để đảm bảo người chơi thấy nó trước màn hình hồi sinh
            Bukkit.getScheduler().runTaskLater(plugin, () -> player.sendMessage(MessageUtils.format(deathMessage)), 1L);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (event.getPlayer().equals(player)) {
            this.failed = true;
        }
    }
}