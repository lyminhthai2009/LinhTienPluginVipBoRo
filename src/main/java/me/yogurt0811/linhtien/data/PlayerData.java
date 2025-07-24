package me.yogurt0811.linhtien.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class PlayerData {
    private final UUID uuid;
    private String realmId;
    private double currentExp;
    private String talentId;
    private String sectId;
    private final String[] skillSlots = new String[4];
    private double currentMana;
    private final List<String> learnedSkills = new ArrayList<>();

    public PlayerData(UUID uuid, String realmId, double currentExp, String talentId) {
        this.uuid = uuid;
        this.realmId = realmId;
        this.currentExp = currentExp;
        this.talentId = talentId;
        this.currentMana = -1; // -1 to signal it needs to be set to max on first load
    }

    public UUID getUuid() { return uuid; }
    public String getRealmId() { return realmId; }
    public void setRealmId(String realmId) { this.realmId = realmId; }
    public double getCurrentExp() { return currentExp; }
    public void setCurrentExp(double currentExp) { this.currentExp = currentExp; }
    public void addExp(double amount) { this.currentExp += amount; }
    public String getTalentId() { return talentId; }
    public void setTalentId(String talentId) { this.talentId = talentId; }
    public String getSectId() { return sectId; }
    public void setSectId(String sectId) { this.sectId = sectId; }

    public String getSkillInSlot(int slot) {
        if (slot >= 0 && slot < skillSlots.length) { return skillSlots[slot]; }
        return null;
    }

    public void setSkillInSlot(int slot, String skillId) {
        if (slot >= 0 && slot < skillSlots.length) { skillSlots[slot] = skillId; }
    }

    public String[] getSkillSlots() { return skillSlots; }
    
    public double getCurrentMana() { return currentMana; }
    public void setCurrentMana(double currentMana) { this.currentMana = currentMana; }
    public void addMana(double amount) { this.currentMana += amount; }

    public List<String> getLearnedSkills() { return learnedSkills; }
    public boolean hasLearnedSkill(String skillId) { return learnedSkills.contains(skillId.toLowerCase()); }
    public void learnSkill(String skillId) {
        String lowerCaseId = skillId.toLowerCase();
        if (!learnedSkills.contains(lowerCaseId)) { learnedSkills.add(lowerCaseId); }
    }
    public void forgetSkill(String skillId) { learnedSkills.remove(skillId.toLowerCase()); }

    @Override
    public String toString() {
        return "PlayerData{" + "uuid=" + uuid + ", realmId='" + realmId + '\'' + ", currentExp=" + currentExp + ", currentMana=" + currentMana + ", talentId='" + talentId + '\'' + ", sectId='" + sectId + '\'' + ", skillSlots=" + Arrays.toString(skillSlots) + ", learnedSkills=" + learnedSkills + '}';
    }
}