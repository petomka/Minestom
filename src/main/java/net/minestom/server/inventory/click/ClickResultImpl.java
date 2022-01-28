package net.minestom.server.inventory.click;

import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

class ClickResultImpl {
    record Single(@NotNull ItemStack remaining,
                  @NotNull Map<Integer, ItemStack> changedSlots) implements ClickResult.Single {
        static @NotNull Single empty() {
            return new ClickResultImpl.Single(ItemStack.AIR, Map.of());
        }

        public Single {
            changedSlots = Map.copyOf(changedSlots);
        }
    }

    record Double(@NotNull ItemStack remaining,
                  @NotNull Map<Integer, ItemStack> playerChanges,
                  @NotNull Map<Integer, ItemStack> inventoryChanges) implements ClickResult.Double {

        public Double {
            playerChanges = Map.copyOf(playerChanges);
            inventoryChanges = Map.copyOf(inventoryChanges);
        }
    }
}
