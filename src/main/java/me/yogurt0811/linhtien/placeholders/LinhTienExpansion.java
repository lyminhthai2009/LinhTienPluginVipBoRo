package me.yogurt0811.linhtien.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.yogurt0811.linhtien.LinhTienPlugin;
import me.yogurt0811.linhtien.config.MessageUtils;
import me.yogurt0811.linhtien.data.PlayerData;
import me.yogurt0811.linhtien.realms.Realm;
import me.yogurt0811.linhtien.realms.RealmManager;
import me.yogurt0811.linhtien.realms.RealmPhase;
import me.yogurt0811.linhtien.sects.Sect;
import me.yogurt0811.linhtien.sects.SectManager;
import me.yogurt0811.linhtien.talents.Talent;
import me.yogurt0811.linhtien.talents.TalentManager;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LinhTienExpansion extends PlaceholderExpansion {

    private final LinhTienPlugin plugin;
    private final RealmManager realmManager;
    private final TalentManager talentManager;
    private final SectManager sectManager;

    public LinhTienExpansion(LinhTienPlugin plugin) {
        this.plugin = plugin;
        this.realmManager = plugin.getRealmManager();
        this.talentManager = plugin.getTalentManager();
        this.sectManager = plugin.getSectManager();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "linhtien";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Yogurt0811";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";

        Player onlinePlayer = player.getPlayer();
        if (onlinePlayer == null) return "Offline";
        
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(onlinePlayer);
        if (data == null) return "Đang tải...";

        Realm realm = realmManager.getPlayerRealm(data);
        
        switch (params.toLowerCase()) {
            case "realm":
            case "realm_name":
                return realm != null ? LegacyComponentSerializer.legacyAmpersand().serialize(MessageUtils.format(realm.displayName())) : "Không rõ";

            case "phase":
            case "phase_name":
                RealmPhase phase = realmManager.getPlayerPhase(data);
                return phase != null ? LegacyComponentSerializer.legacyAmpersand().serialize(MessageUtils.format(phase.displayName())) : "";

            case "realm_and_phase":
                if (realm == null) return "Không rõ";
                RealmPhase currentPhase = realmManager.getPlayerPhase(data);
                String phaseName = (currentPhase != null && currentPhase.displayName() != null && !currentPhase.displayName().isEmpty()) 
                                   ? " " + currentPhase.displayName() 
                                   : "";
                return LegacyComponentSerializer.legacyAmpersand().serialize(MessageUtils.format(realm.displayName() + phaseName));

            case "exp":
                return String.format("%.0f", data.getCurrentExp());
                
            case "exp_max":
                return realm != null ? String.format("%.0f", realm.expToNext()) : "0";
                
            case "exp_percent":
                 if (realm == null || realm.expToNext() == 0) return "0.00";
                 return String.format("%.2f", (data.getCurrentExp() / realm.expToNext()) * 100);

            case "dotpha_ready":
                if (realm == null) return "&cChưa đủ";
                return data.getCurrentExp() >= realm.expToNext() ? "&aSẵn sàng" : "&cChưa đủ";

            case "talent_name":
                Talent talent = talentManager.getTalent(data.getTalentId());
                return talent != null ? LegacyComponentSerializer.legacyAmpersand().serialize(MessageUtils.format(talent.displayName())) : "Chưa có";

            case "sect_name":
                if (data.getSectId() == null) return "Tán Tu";
                Sect sect = sectManager.getSectById(data.getSectId());
                return sect != null ? sect.getDisplayName() : "Không rõ";
                
            case "mana":
                return String.format("%.0f", data.getCurrentMana());
            
            case "mana_max":
                return realm != null ? String.format("%.0f", realm.stats().getOrDefault("max-mana", 100.0)) : "100";
            
            case "mana_regen":
                 return realm != null ? String.format("%.1f", realm.stats().getOrDefault("mana-regen", 2.0)) : "2.0";

            case "health":
                 return String.format("%.0f", onlinePlayer.getHealth());
            
            case "max_health":
                AttributeInstance healthAttr = onlinePlayer.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                return healthAttr != null ? String.format("%.0f", healthAttr.getValue()) : "20";
                
            case "damage_bonus":
                 return realm != null ? String.format("%.1f", realm.stats().getOrDefault("damage-bonus", 0.0)) : "0.0";
                 
            case "defense_bonus_percent":
                double defense = realm != null ? realm.stats().getOrDefault("defense-bonus", 0.0) : 0.0;
                return String.format("%.0f", defense * 100);

            default:
                return null;
        }
    }
}