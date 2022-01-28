package net.minestom.server.inventory.click;

import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface ClickResult {
    static @NotNull ClickResult empty() {
        return new ClickResultImpl(ItemStack.AIR, Map.of());
    }

    @NotNull ItemStack cursor();

    @NotNull Map<Integer, ItemStack> changedSlots();
}
