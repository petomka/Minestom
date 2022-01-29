package net.minestom.server.inventory.click.integration;

import net.minestom.server.api.Env;
import net.minestom.server.api.EnvTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.client.play.ClientClickWindowPacket;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@EnvTest
public class LeftClickIntegrationTest {

    @Test
    public void leftSelf(Env env) {
        var instance = env.createFlatInstance();
        var player = env.createPlayer(instance, new Pos(0, 40, 0));
        var inventory = player.getInventory();
        var listener = env.listen(InventoryPreClickEvent.class);
        inventory.setItemStack(1, ItemStack.of(Material.DIAMOND));
        // Empty click
        {
            listener.followup(event -> {
                assertNull(event.getInventory()); // Player inventory
                assertEquals(ClickType.LEFT_CLICK, event.getClickType());
                assertEquals(ItemStack.AIR, inventory.getCursorItem());
            });
            leftClick(player, null, 0);
        }
        // Pickup diamond
        {
            listener.followup(event -> {
                // Ensure that the inventory didn't change yet
                assertEquals(ItemStack.AIR, inventory.getCursorItem());
                assertEquals(ItemStack.of(Material.DIAMOND), inventory.getItemStack(1));
            });
            leftClick(player, null, 1);
            // Verify inventory changes
            assertEquals(ItemStack.of(Material.DIAMOND), inventory.getCursorItem());
            assertEquals(ItemStack.AIR, inventory.getItemStack(1));
        }
        // Place it back
        {
            listener.followup(event -> {
                assertEquals(ItemStack.of(Material.DIAMOND), inventory.getCursorItem());
                assertEquals(ItemStack.AIR, inventory.getItemStack(1));
            });
            leftClick(player, null, 1);
            assertEquals(ItemStack.AIR, inventory.getCursorItem());
            assertEquals(ItemStack.of(Material.DIAMOND), inventory.getItemStack(1));
        }
        // Cancel event
        {
            listener.followup(event -> event.setCancelled(true));
            leftClick(player, null, 1);
            assertEquals(ItemStack.AIR, inventory.getCursorItem(), "Left click cancellation did not work");
            assertEquals(ItemStack.of(Material.DIAMOND), inventory.getItemStack(1));
        }
    }

    private void leftClick(Player player, Inventory inventory, int slot) {
        byte windowId = inventory != null ? inventory.getWindowId() : 0;
        player.addPacketToQueue(new ClientClickWindowPacket(windowId, 0, (short) PlayerInventoryUtils.convertToPacketSlot(slot), (byte) 0,
                ClientClickWindowPacket.ClickType.PICKUP, List.of(), ItemStack.AIR));
        player.interpretPacketQueue();
    }
}
