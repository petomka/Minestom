package net.minestom.server.inventory.click;

import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

record ClickResultImpl(@NotNull ItemStack cursor,
                       @NotNull Map<Integer, ItemStack> changedSlots) implements ClickResult {
    public ClickResultImpl {
        changedSlots = Map.copyOf(changedSlots);
    }
}
