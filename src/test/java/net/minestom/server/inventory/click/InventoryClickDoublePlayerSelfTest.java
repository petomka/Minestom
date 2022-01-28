package net.minestom.server.inventory.click;

import net.minestom.server.MinecraftServer;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class InventoryClickDoublePlayerSelfTest extends ClickUtils {

    static {
        // Required for now
        MinecraftServer.init();
    }

    @Test
    public void empty() {
        assertPlayerSingleClick(inventory -> ClickProcessor.doubleWithinPlayer(inventory, ItemStack.AIR), ItemStack.AIR, Map.of());
    }

    @Test
    public void emptyInventory() {
        assertPlayerSingleClick(inventory -> ClickProcessor.doubleWithinPlayer(inventory, ItemStack.of(Material.DIAMOND)), ItemStack.of(Material.DIAMOND), Map.of());
    }

    @Test
    public void takeOne() {
        assertPlayerSingleClick(inventory -> {
            inventory.setItemStack(9, ItemStack.of(Material.DIAMOND));
            return ClickProcessor.doubleWithinPlayer(inventory, ItemStack.of(Material.DIAMOND));
        }, ItemStack.of(Material.DIAMOND, 2), Map.of(9, ItemStack.AIR));

        assertPlayerSingleClick(inventory -> {
            inventory.setItemStack(0, ItemStack.of(Material.DIAMOND));
            return ClickProcessor.doubleWithinPlayer(inventory, ItemStack.of(Material.DIAMOND));
        }, ItemStack.of(Material.DIAMOND, 2), Map.of(0, ItemStack.AIR));
    }

    @Test
    public void order() {
        assertPlayerSingleClick(inventory -> {
            inventory.setItemStack(0, ItemStack.of(Material.DIAMOND, 48));
            inventory.setItemStack(9, ItemStack.of(Material.DIAMOND, 48));
            return ClickProcessor.doubleWithinPlayer(inventory, ItemStack.of(Material.DIAMOND, 8));
        }, ItemStack.of(Material.DIAMOND, 64), Map.of(0, ItemStack.of(Material.DIAMOND, 40), 9, ItemStack.AIR));
    }
}
