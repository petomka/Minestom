package net.minestom.server.inventory.click;

import net.minestom.server.MinecraftServer;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class InventoryClickLeftTest extends ClickUtils {

    static {
        // Required for now
        MinecraftServer.init();
    }

    @Test
    public void empty() {
        assertClick(inventory -> ClickProcessor.left(inventory, 0, ItemStack.AIR), ItemStack.AIR, Map.of());
    }

    @Test
    public void emptyClicked() {
        var item = ItemStack.of(Material.DIAMOND, 5);
        assertClick(inventory -> {
            inventory.setItemStack(0, item);
            return ClickProcessor.left(inventory, 0, ItemStack.AIR);
        }, item, Map.of(0, ItemStack.AIR));
    }

    @Test
    public void merge() {
        var item = ItemStack.of(Material.DIAMOND, 5);
        assertClick(inventory -> {
            inventory.setItemStack(1, item);
            return ClickProcessor.left(inventory, 1, item);
        }, ItemStack.AIR, Map.of(1, ItemStack.of(Material.DIAMOND, 10)));
    }

    @Test
    public void missClick() {
        var item = ItemStack.of(Material.DIAMOND);
        assertClick(inventory -> {
            inventory.setItemStack(0, item);
            return ClickProcessor.left(inventory, 1, ItemStack.AIR);
        }, ItemStack.AIR, Map.of());
    }
}
