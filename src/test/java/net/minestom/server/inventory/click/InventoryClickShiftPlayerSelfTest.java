package net.minestom.server.inventory.click;

import net.minestom.server.MinecraftServer;
import net.minestom.server.api.TestUtils;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InventoryClickShiftPlayerSelfTest extends ClickUtils {

    static {
        // Required for now
        MinecraftServer.init();
    }

    @Test
    public void empty() {
        assertInventoryShift(inventory -> ClickProcessor.shiftWithinPlayer(inventory, 0, ItemStack.AIR), ItemStack.AIR, Map.of());
    }

    @Test
    public void insertOne() {
        assertInventoryShift(inventory -> ClickProcessor.shiftWithinPlayer(inventory, 0, ItemStack.of(Material.DIAMOND)),
                ItemStack.AIR, Map.of(9, ItemStack.of(Material.DIAMOND)));
    }

    @Test
    public void armorEquip() {
        assertInventoryShift(inventory -> ClickProcessor.shiftWithinPlayer(inventory, 0, ItemStack.of(Material.CHAINMAIL_HELMET)),
                ItemStack.AIR, Map.of(PlayerInventoryUtils.HELMET_SLOT, ItemStack.of(Material.CHAINMAIL_HELMET)));
    }

    @Test
    public void armorEquipFull() {
        assertInventoryShift(inventory -> {
            inventory.setHelmet(ItemStack.of(Material.DIAMOND_HELMET));
            return ClickProcessor.shiftWithinPlayer(inventory, 0, ItemStack.of(Material.CHAINMAIL_HELMET));
        }, ItemStack.of(Material.CHAINMAIL_HELMET), Map.of());
    }

    @Test
    public void armorUnequip() {
        assertInventoryShift(inventory -> {
            inventory.setHelmet(ItemStack.of(Material.CHAINMAIL_HELMET));
            return ClickProcessor.shiftWithinPlayer(inventory, PlayerInventoryUtils.HELMET_SLOT, ItemStack.of(Material.CHAINMAIL_HELMET));
        }, ItemStack.AIR, Map.of(9, ItemStack.of(Material.CHAINMAIL_HELMET)));
    }

    @Test
    public void armorUnequipFull() {
        assertInventoryShift(inventory -> {
            for (int i = 0; i < 36; i++) {
                inventory.setItemStack(i, ItemStack.of(Material.DIAMOND, 64));
            }
            inventory.setHelmet(ItemStack.of(Material.CHAINMAIL_HELMET));
            return ClickProcessor.shiftWithinPlayer(inventory, PlayerInventoryUtils.HELMET_SLOT, ItemStack.of(Material.CHAINMAIL_HELMET));
        }, ItemStack.of(Material.CHAINMAIL_HELMET), Map.of());
    }

    @Test
    public void incrOne() {
        assertInventoryShift(inventory -> {
            inventory.setItemStack(9, ItemStack.of(Material.DIAMOND));
            return ClickProcessor.shiftWithinPlayer(inventory, 0, ItemStack.of(Material.DIAMOND));
        }, ItemStack.AIR, Map.of(9, ItemStack.of(Material.DIAMOND, 2)));
    }

    @Test
    public void insertSecondPart() {
        assertInventoryShift(inventory -> ClickProcessor.shiftWithinPlayer(inventory, 9, ItemStack.of(Material.DIAMOND)),
                ItemStack.AIR, Map.of(0, ItemStack.of(Material.DIAMOND)));
    }

    @Test
    public void almostOverflow() {
        assertInventoryShift(inventory -> {
            for (int i = 0; i < 8; i++) {
                inventory.setItemStack(i, ItemStack.of(Material.DIAMOND, 64));
            }
            return ClickProcessor.shiftWithinPlayer(inventory, 9, ItemStack.of(Material.DIAMOND));
        }, ItemStack.AIR, Map.of(8, ItemStack.of(Material.DIAMOND)));
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
