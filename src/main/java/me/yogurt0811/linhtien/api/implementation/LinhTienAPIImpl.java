package me.yogurt0811.linhtien.api.implementation;

import me.yogurt0811.linhtien.LinhTienPlugin;
import me.yogurt0811.linhtien.api.LinhTienAPI;
import me.yogurt0811.linhtien.data.PlayerData;
import me.yogurt0811.linhtien.practice.PracticeManager; // Thêm import này
import me.yogurt0811.linhtien.realms.RealmManager;
import me.yogurt0811.linhtien.sects.SectManager;
import me.yogurt0811.linhtien.skills.SkillManager;
import me.yogurt0811.linhtien.talents.TalentManager;
import org.bukkit.entity.Player;

import java.util.Optional;

public class LinhTienAPIImpl implements LinhTienAPI {

    private final LinhTienPlugin plugin;

    public LinhTienAPIImpl(LinhTienPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Optional<PlayerData> getPlayerData(Player player) {
        return Optional.ofNullable(plugin.getPlayerDataManager().getPlayerData(player));
    }

    @Override
    public RealmManager getRealmManager() {
        return plugin.getRealmManager();
    }

    @Override
    public TalentManager getTalentManager() {
        return plugin.getTalentManager();
    }

    @Override
    public SkillManager getSkillManager() {
        return plugin.getSkillManager();
    }

    @Override
    public SectManager getSectManager() {
        return plugin.getSectManager();
    }

    // ==========================================================
    // TRIỂN KHAI 2 PHƯƠNG THỨC MỚI
    @Override
    public PracticeManager getPracticeManager() {
        return plugin.getPracticeManager();
    }

    @Override
    public LinhTienPlugin getLinhTienPlugin() {
        return plugin;
    }
    // ==========================================================
}