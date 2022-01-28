package net.minestom.server.inventory.click;

import net.minestom.server.entity.Player;
import net.minestom.server.inventory.AbstractInventory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public final class DragHelper {
    private final Map<Player, List<Entry>> leftDraggingMap = new ConcurrentHashMap<>();
    private final Map<Player, List<Entry>> rightDraggingMap = new ConcurrentHashMap<>();

    public record Entry(int slot, AbstractInventory inventory) {
    }

    public boolean test(Player player, int slot, int button, int clickSlot, AbstractInventory clickInventory,
                        Predicate<List<Entry>> leftHandler,
                        Predicate<List<Entry>> rightHandler) {
        if (slot != -999) {
            // Add slot
            if (button == 1) { // Add left
                List<Entry> left = leftDraggingMap.get(player);
                if (left == null) return false;
                left.add(new Entry(clickSlot, clickInventory));
                return true;
            } else if (button == 5) { // Add right
                List<Entry> right = rightDraggingMap.get(player);
                if (right == null) return false;
                right.add(new Entry(clickSlot, clickInventory));
                return true;
            } else if (button == 9) { // Add middle TODO
                return false;
            }
            return false; // Shouldn't happen
        } else {
            // Drag instruction
            if (button == 0) { // Start left
                this.leftDraggingMap.put(player, new ArrayList<>());
                return true;
            } else if (button == 2) { // End left
                List<Entry> left = leftDraggingMap.remove(player);
                if (left == null) return false;
                return leftHandler.test(left);
            } else if (button == 4) { // Start right
                this.rightDraggingMap.put(player, new ArrayList<>());
                return true;
            } else if (button == 6) { // End right
                List<Entry> right = rightDraggingMap.remove(player);
                if (right == null) return false;
                return rightHandler.test(right);
            }
            return false; // Shouldn't happen
        }
    }

    public void clearCache(@NotNull Player player) {
        this.leftDraggingMap.remove(player);
        this.rightDraggingMap.remove(player);
    }
}
