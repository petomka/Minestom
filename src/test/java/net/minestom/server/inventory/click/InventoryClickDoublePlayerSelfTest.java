package net.minestom.server.inventory.click;

import net.minestom.server.MinecraftServer;
import net.minestom.server.api.TestUtils;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InventoryClickDoublePlayerSelfTest {

    static {
        // Required for now
        MinecraftServer.init();
    }

    @Test
    public void empty() {
        assertInventoryShift(inventory -> ClickProcessor.doubleWithinPlayer(inventory, ItemStack.AIR), ItemStack.AIR, Map.of());
    }

    @Test
    public void emptyInventory() {
        assertInventoryShift(inventory -> ClickProcessor.doubleWithinPlayer(inventory, ItemStack.of(Material.DIAMOND)), ItemStack.of(Material.DIAMOND), Map.of());
    }

    @Test
    public void takeOne() {
        assertInventoryShift(inventory -> {
            inventory.setItemStack(9, ItemStack.of(Material.DIAMOND));
            return ClickProcessor.doubleWithinPlayer(inventory, ItemStack.of(Material.DIAMOND));
        }, ItemStack.of(Material.DIAMOND, 2), Map.of(9, ItemStack.AIR));

        assertInventoryShift(inventory -> {
            inventory.setItemStack(0, ItemStack.of(Material.DIAMOND));
            return ClickProcessor.doubleWithinPlayer(inventory, ItemStack.of(Material.DIAMOND));
        }, ItemStack.of(Material.DIAMOND, 2), Map.of(0, ItemStack.AIR));
    }

    @Test
    public void order() {
        assertInventoryShift(inventory -> {
            inventory.setItemStack(0, ItemStack.of(Material.DIAMOND, 48));
            inventory.setItemStack(9, ItemStack.of(Material.DIAMOND, 48));
            return ClickProcessor.doubleWithinPlayer(inventory, ItemStack.of(Material.DIAMOND, 8));
        }, ItemStack.of(Material.DIAMOND, 64), Map.of(0, ItemStack.of(Material.DIAMOND, 40), 9, ItemStack.AIR));
    }

    void assertInventoryShift(Function<PlayerInventory, ClickResult.Shift> filler,
                              ItemStack expectedRemaining,
                              Map<Integer, ItemStack> expectedChangedSlots) {
        var player = TestUtils.createDummyPlayer();
        PlayerInventory inventory = new PlayerInventory(player);
        var result = filler.apply(inventory);
        assertEquals(expectedRemaining, result.remaining(), "Invalid remaining");
        assertEquals(expectedChangedSlots, result.changedSlots(), "Invalid changed slots");
    }
}
