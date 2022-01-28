package net.minestom.server.inventory.click;

import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;

import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClickUtils {
    void assertSingleClick(Function<Inventory, ClickResult.Single> filler,
                           ItemStack expectedCursor,
                           Map<Integer, ItemStack> expectedChangedSlots) {
        Inventory inventory = new Inventory(InventoryType.CHEST_1_ROW, "test");
        var result = filler.apply(inventory);
        assertEquals(expectedCursor, result.cursor(), "Invalid cursor");
        assertEquals(expectedChangedSlots, result.changedSlots(), "Invalid changed slots");
    }
}
