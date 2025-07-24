package me.yogurt0811.linhtien.sects;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Sect {
    private final String id; // ID duy nhất, có thể là tên viết liền không dấu
    private String displayName;
    private UUID owner;
    private final Map<UUID, SectMember> members = new ConcurrentHashMap<>();

    // Các trường khác có thể thêm sau: level, exp, bank, description...

    public Sect(String id, String displayName, UUID owner) {
        this.id = id.toLowerCase();
        this.displayName = displayName;
        this.owner = owner;
        // Tông chủ cũng là một thành viên
        addMember(owner, SectRank.LEADER);
    }
    
    // Thêm thành viên mới
    public void addMember(UUID playerUuid, SectRank rank) {
        members.put(playerUuid, new SectMember(playerUuid, rank));
    }
    
    // Xóa thành viên
    public void removeMember(UUID playerUuid) {
        members.remove(playerUuid);
    }

    public SectMember getMember(UUID playerUuid) {
        return members.get(playerUuid);
    }
    
    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public UUID getOwner() {
        return owner;
    }
    
    public Map<UUID, SectMember> getMembers() {
        return members;
    }
}