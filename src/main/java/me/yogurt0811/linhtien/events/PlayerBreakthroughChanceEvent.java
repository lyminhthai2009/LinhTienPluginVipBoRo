// START OF FILE TuTien/src/main/java/me/yogurt0811/linhtien/events/PlayerBreakthroughChanceEvent.java
package me.yogurt0811.linhtien.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable; // Có thể hủy bỏ (nếu muốn)
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Sự kiện được gọi trước khi plugin tính toán tỷ lệ thành công đột phá cuối cùng.
 * Cho phép các plugin khác sửa đổi tỷ lệ này.
 */
public class PlayerBreakthroughChanceEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private double finalChance; // Tỷ lệ đã được tính toán bởi plugin gốc (base + talent)
    private boolean cancelled;

    public PlayerBreakthroughChanceEvent(Player player, double initialChance) {
        super(false); // Synchronous event
        this.player = player;
        this.finalChance = initialChance;
        this.cancelled = false;
    }

    public Player getPlayer() {
        return player;
    }

    /**
     * Lấy tỷ lệ thành công đột phá hiện tại.
     * Các plugin có thể sửa đổi giá trị này.
     * @return Tỷ lệ thành công.
     */
    public double getFinalChance() {
        return finalChance;
    }

    /**
     * Đặt tỷ lệ thành công đột phá mới.
     * @param finalChance Tỷ lệ thành công mới.
     */
    public void setFinalChance(double finalChance) {
        this.finalChance = finalChance;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
// END OF FILE TuTien/src/main/java/me/yogurt0811/linhtien/events/PlayerBreakthroughChanceEvent.java