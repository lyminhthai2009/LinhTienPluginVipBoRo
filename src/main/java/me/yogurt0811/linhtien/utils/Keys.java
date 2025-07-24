package me.yogurt0811.linhtien.utils;

import me.yogurt0811.linhtien.LinhTienPlugin;
import org.bukkit.NamespacedKey;

public final class Keys {
    private Keys() {}

    public static final NamespacedKey SKILL_SLOT_ID = new NamespacedKey(LinhTienPlugin.getInstance(), "skill_slot_id");
    
    public static final NamespacedKey GUI_SKILL_ID = new NamespacedKey(LinhTienPlugin.getInstance(), "gui_skill_id");
    public static final NamespacedKey GUI_EMPTY_SKILL_SLOT = new NamespacedKey(LinhTienPlugin.getInstance(), "gui_empty_skill_slot");
    public static final NamespacedKey GUI_FILLER_ITEM = new NamespacedKey(LinhTienPlugin.getInstance(), "gui_filler_item");
    public static final NamespacedKey GUI_EQUIPPED_SLOT_NUM = new NamespacedKey(LinhTienPlugin.getInstance(), "gui_equipped_slot_num");

    // KEY NÀY CÓ THỂ KHÔNG CÒN DÙNG TRỰC TIẾP TRONG CORE NỮA VÌ MYTHICMOBS SẼ QUẢN LÝ DAMAGE
    // NHƯNG GIỮ LẠI NẾU CÁC ADDON KHÁC VẪN CẦN KIỂM TRA NGUỒN DAMAGE
    public static final NamespacedKey SKILL_DAMAGE_SOURCE = new NamespacedKey(LinhTienPlugin.getInstance(), "skill_damage_source");
}