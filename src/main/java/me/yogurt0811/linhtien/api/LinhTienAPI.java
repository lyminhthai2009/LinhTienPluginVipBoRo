package me.yogurt0811.linhtien.api;

import me.yogurt0811.linhtien.LinhTienPlugin; // Thêm import này
import me.yogurt0811.linhtien.data.PlayerData;
import me.yogurt0811.linhtien.practice.PracticeManager; // Thêm import này
import me.yogurt0811.linhtien.realms.RealmManager;
import me.yogurt0811.linhtien.sects.SectManager;
import me.yogurt0811.linhtien.skills.SkillManager;
import me.yogurt0811.linhtien.talents.TalentManager;
import org.bukkit.entity.Player;

import java.util.Optional;

public interface LinhTienAPI {

    Optional<PlayerData> getPlayerData(Player player);

    // GETTER CHO CÁC MANAGER
    RealmManager getRealmManager();
    TalentManager getTalentManager();
    SkillManager getSkillManager();
    SectManager getSectManager();
    
    // ==========================================================
    // THÊM 2 PHƯƠNG THỨC NÀY VÀO
    PracticeManager getPracticeManager();
    LinhTienPlugin getLinhTienPlugin();
    // ==========================================================
}