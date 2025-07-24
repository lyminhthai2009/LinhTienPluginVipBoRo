package me.yogurt0811.linhtien.stats;

import me.yogurt0811.linhtien.LinhTienPlugin;
import me.yogurt0811.linhtien.data.PlayerData;
import me.yogurt0811.linhtien.events.PlayerRealmChangeEvent;
import me.yogurt0811.linhtien.realms.Realm;
import me.yogurt0811.linhtien.utils.Keys;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

public class StatManager implements Listener {

    private final LinhTienPlugin plugin;

    public StatManager(LinhTienPlugin plugin) {
        this.plugin = plugin;
        startManaRegenTask();
    }

    public void applyStats(Player player) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
            if (data == null) return;

            Realm realm = plugin.getRealmManager().getPlayerRealm(data);
            if (realm == null) return;

            Map<String, Double> stats = realm.stats();
            
            AttributeInstance maxHealthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (maxHealthAttr != null) {
                double healthBonus = stats.getOrDefault("health-bonus", 0.0);
                maxHealthAttr.setBaseValue(20.0 + healthBonus);
            }
            
            double maxMana = stats.getOrDefault("max-mana", 100.0);
            if (data.getCurrentMana() < 0 || data.getCurrentMana() > maxMana) {
                data.setCurrentMana(maxMana);
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        // SỬA ĐỔI: Không còn kiểm tra SKILL_DAMAGE_SOURCE ở đây vì MythicMobs sẽ quản lý sát thương skill
        // if (event.getEntity().hasMetadata(Keys.SKILL_DAMAGE_SOURCE.getKey())) {
        //     return;
        // }

        if (!(event.getDamager() instanceof Player attacker)) return;

        PlayerData attackerData = plugin.getPlayerDataManager().getPlayerData(attacker);
        if (attackerData == null) return;

        Realm realm = plugin.getRealmManager().getPlayerRealm(attackerData);
        if (realm == null) return;

        double damageBonus = realm.stats().getOrDefault("damage-bonus", 0.0);
        event.setDamage(event.getDamage() + damageBonus);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDamaged(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        PlayerData victimData = plugin.getPlayerDataManager().getPlayerData(victim);
        if (victimData == null) return;

        Realm realm = plugin.getRealmManager().getPlayerRealm(victimData);
        if (realm == null) return;

        double defenseBonus = realm.stats().getOrDefault("defense-bonus", 0.0);
        double finalDamage = event.getDamage() * (1.0 - defenseBonus);
        event.setDamage(finalDamage);
    }

    private void startManaRegenTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
                    if (data == null) continue;
                    
                    Realm realm = plugin.getRealmManager().getPlayerRealm(data);
                    if (realm == null) continue;
                    
                    double maxMana = realm.stats().getOrDefault("max-mana", 100.0);
                    double manaRegen = realm.stats().getOrDefault("mana-regen", 2.0);

                    if (data.getCurrentMana() < maxMana) {
                        data.addMana(manaRegen);
                        if (data.getCurrentMana() > maxMana) {
                            data.setCurrentMana(maxMana);
                        }
                    }
                }
            }
        }.runTaskTimerAsynchronously(plugin, 100L, 20L);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> applyStats(event.getPlayer()), 20L);
    }
    
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
         plugin.getServer().getScheduler().runTaskLater(plugin, () -> applyStats(event.getPlayer()), 1L);
    }

    @EventHandler
    public void onRealmChange(PlayerRealmChangeEvent event) {
        applyStats(event.getPlayer());
    }
}