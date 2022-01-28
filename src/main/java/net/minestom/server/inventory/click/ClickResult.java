package net.minestom.server.inventory.click;

import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@ApiStatus.Internal
public interface ClickResult {

    /**
     * Result affecting a single inventory/slot.
     */
    interface Single extends ClickResult {
        @NotNull ItemStack cursor();

        @NotNull Map<Integer, ItemStack> changedSlots();
    }

    /**
     * Handles shift clicks.
     */
    interface Shift extends ClickResult {
        @NotNull ItemStack remaining();

        @NotNull Map<Integer, ItemStack> changedSlots();
    }
}
