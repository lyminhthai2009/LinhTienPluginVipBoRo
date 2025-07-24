package me.yogurt0811.linhtien.skills;

import org.bukkit.Material;
import java.util.List;

public record Skill(
        String id,
        String name,
        List<String> lore,
        Material material,
        int customModelData,
        double cost,
        int cooldown,
        String requiredRealm,
        String mythicSkillId // THAY ĐỔI: ID kỹ năng trong MythicMobs
) {}