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

public class InventoryClickShiftPlayerTest extends ClickUtils {

    static {
        // Required for now
        MinecraftServer.init();
    }

    @Test
    public void empty() {
        assertInventoryShift(inventory -> ClickProcessor.shiftToPlayer(inventory, ItemStack.of(Material.AIR)), ItemStack.AIR, Map.of());
    }

    @Test
    public void insertOne() {
        assertInventoryShift(inventory -> ClickProcessor.shiftToPlayer(inventory, ItemStack.of(Material.DIAMOND)), ItemStack.AIR, Map.of(8, ItemStack.of(Material.DIAMOND)));
    }

    @Test
    public void incrOne() {
        assertInventoryShift(inventory -> {
            inventory.setItemStack(8, ItemStack.of(Material.DIAMOND));
            return ClickProcessor.shiftToPlayer(inventory, ItemStack.of(Material.DIAMOND));
        }, ItemStack.AIR, Map.of(8, ItemStack.of(Material.DIAMOND, 2)));
    }

    @Test
    public void insertSecondPart() {
        assertInventoryShift(inventory -> {
            for (int i = 0; i < 9; i++) {
                inventory.setItemStack(i, ItemStack.of(Material.DIAMOND, 64));
            }
            return ClickProcessor.shiftToPlayer(inventory, ItemStack.of(Material.DIAMOND));
        }, ItemStack.AIR, Map.of(35, ItemStack.of(Material.DIAMOND)));
    }

    @Test
    public void overflow() {
        assertInventoryShift(inventory -> {
            for (int i = 0; i < 36; i++) {
                inventory.setItemStack(i, ItemStack.of(Material.DIAMOND, 64));
            }
            return ClickProcessor.shiftToPlayer(inventory, ItemStack.of(Material.DIAMOND));
        }, ItemStack.of(Material.DIAMOND), Map.of());
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
