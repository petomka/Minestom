package net.minestom.server.inventory.click;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.condition.InventoryCondition;
import net.minestom.server.inventory.condition.InventoryConditionResult;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.StackingRule;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@ApiStatus.Internal
public final class InventoryClickProcessor {
    public @NotNull InventoryClickResult drop(@NotNull Player player, @NotNull AbstractInventory inventory,
                                              boolean all, int slot, int button,
                                              @NotNull ItemStack clicked, @NotNull ItemStack cursor) {
        final InventoryClickResult clickResult = startCondition(player, inventory, slot, ClickType.DROP, clicked, cursor);
        if (clickResult.isCancel()) return clickResult;

        final StackingRule clickedRule = clicked.getStackingRule();
        final StackingRule cursorRule = cursor.getStackingRule();

        ItemStack resultClicked = clicked;
        ItemStack resultCursor = cursor;

        if (slot == -999) {
            // Click outside
            if (button == 0) {
                // Left (drop all)
                final int amount = cursorRule.getAmount(resultCursor);
                final ItemStack dropItem = cursorRule.apply(resultCursor, amount);
                final boolean dropResult = player.dropItem(dropItem);
                clickResult.setCancel(!dropResult);
                if (dropResult) {
                    resultCursor = cursorRule.apply(resultCursor, 0);
                }
            } else if (button == 1) {
                // Right (drop 1)
                final ItemStack dropItem = cursorRule.apply(resultCursor, 1);
                final boolean dropResult = player.dropItem(dropItem);
                clickResult.setCancel(!dropResult);
                if (dropResult) {
                    final int amount = cursorRule.getAmount(resultCursor);
                    final int newAmount = amount - 1;
                    resultCursor = cursorRule.apply(resultCursor, newAmount);
                }
            }

        } else if (!all) {
            if (button == 0) {
                // Drop key Q (drop 1)
                final ItemStack dropItem = cursorRule.apply(resultClicked, 1);
                final boolean dropResult = player.dropItem(dropItem);
                clickResult.setCancel(!dropResult);
                if (dropResult) {
                    final int amount = clickedRule.getAmount(resultClicked);
                    final int newAmount = amount - 1;
                    resultClicked = cursorRule.apply(resultClicked, newAmount);
                }
            } else if (button == 1) {
                // Ctrl + Drop key Q (drop all)
                final int amount = cursorRule.getAmount(resultClicked);
                final ItemStack dropItem = clickedRule.apply(resultClicked, amount);
                final boolean dropResult = player.dropItem(dropItem);
                clickResult.setCancel(!dropResult);
                if (dropResult) {
                    resultClicked = cursorRule.apply(resultClicked, 0);
                }
            }
        }

        clickResult.setClicked(resultClicked);
        clickResult.setCursor(resultCursor);

        return clickResult;
    }

    private @NotNull InventoryClickResult startCondition(@NotNull Player player,
                                                         @Nullable AbstractInventory inventory,
                                                         int slot, @NotNull ClickType clickType,
                                                         @NotNull ItemStack clicked, @NotNull ItemStack cursor) {
        final InventoryClickResult clickResult = new InventoryClickResult(clicked, cursor);
        final Inventory eventInventory = inventory instanceof Inventory ? (Inventory) inventory : null;

        // Reset the didCloseInventory field
        // Wait for inventory conditions + events to possibly close the inventory
        player.UNSAFE_changeDidCloseInventory(false);
        // InventoryPreClickEvent
        {
            InventoryPreClickEvent inventoryPreClickEvent = new InventoryPreClickEvent(eventInventory, player, slot, clickType,
                    clickResult.getClicked(), clickResult.getCursor());
            EventDispatcher.call(inventoryPreClickEvent);
            clickResult.setCursor(inventoryPreClickEvent.getCursorItem());
            clickResult.setClicked(inventoryPreClickEvent.getClickedItem());
            if (inventoryPreClickEvent.isCancelled()) {
                clickResult.setCancel(true);
            }
        }
        // Inventory conditions
        {
            if (inventory != null) {
                final List<InventoryCondition> inventoryConditions = inventory.getInventoryConditions();
                if (!inventoryConditions.isEmpty()) {
                    for (InventoryCondition inventoryCondition : inventoryConditions) {
                        var result = new InventoryConditionResult(clickResult.getClicked(), clickResult.getCursor());
                        inventoryCondition.accept(player, slot, clickType, result);

                        clickResult.setCursor(result.getCursorItem());
                        clickResult.setClicked(result.getClickedItem());
                        if (result.isCancel()) {
                            clickResult.setCancel(true);
                        }
                    }
                    // Cancel the click if the inventory has been closed by Player#closeInventory within an inventory listener
                    if (player.didCloseInventory()) {
                        clickResult.setCancel(true);
                        player.UNSAFE_changeDidCloseInventory(false);
                    }
                }
            }
        }
        return clickResult;
    }
}
