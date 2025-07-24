package me.yogurt0811.linhtien.events;

import me.yogurt0811.linhtien.realms.Realm;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerRealmChangeEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Realm oldRealm;
    private final Realm newRealm;

    public PlayerRealmChangeEvent(Player player, Realm oldRealm, Realm newRealm) {
        // Sự kiện này chạy bất đồng bộ nếu được gọi từ task async,
        // nhưng các listener có thể xử lý nó đồng bộ.
        super(false); // false = synchronous event
        this.player = player;
        this.oldRealm = oldRealm;
        this.newRealm = newRealm;
    }

    public Player getPlayer() { return player; }
    public Realm getOldRealm() { return oldRealm; }
    public Realm getNewRealm() { return newRealm; }

    @Override public @NotNull HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}