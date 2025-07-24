package me.yogurt0811.linhtien.sects;

public enum SectRank {
    MEMBER("Thành Viên", 1),
    ELITE("Tinh Anh", 2),
    ELDER("Trưởng Lão", 3),
    VICE_LEADER("Phó Tông Chủ", 9),
    LEADER("Tông Chủ", 10);

    private final String displayName;
    private final int level;

    SectRank(String displayName, int level) {
        this.displayName = displayName;
        this.level = level;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getLevel() {
        return level;
    }

    public boolean isHigherOrEqual(SectRank other) {
        return this.level >= other.level;
    }
}