package net.minestom.server.inventory.click;

import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

class ClickResultImpl {
    record Single(@NotNull ItemStack cursor,
                  @NotNull Map<Integer, ItemStack> changedSlots) implements ClickResult.Single {
        static @NotNull Single empty() {
            return new ClickResultImpl.Single(ItemStack.AIR, Map.of());
        }

        public Single {
            changedSlots = Map.copyOf(changedSlots);
        }
    }

    record Shift(@NotNull ItemStack remaining,
                 @NotNull Map<Integer, ItemStack> changedSlots) implements ClickResult.Shift {
        static @NotNull Shift empty() {
            return new ClickResultImpl.Shift(ItemStack.AIR, Map.of());
        }

        public Shift {
            changedSlots = Map.copyOf(changedSlots);
        }
    }
}
