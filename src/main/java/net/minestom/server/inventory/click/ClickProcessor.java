package net.minestom.server.inventory.click;

import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.TransactionType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.StackingRule;

import java.util.Map;

public final class ClickProcessor {

    public static ClickResult.Single left(AbstractInventory inventory, int slot, ItemStack cursor) {
        ItemStack clicked = inventory.getItemStack(slot);

        final StackingRule cursorRule = cursor.getStackingRule();
        final StackingRule clickedRule = clicked.getStackingRule();
        if (cursorRule.canBeStacked(cursor, clicked)) {
            // Try to stack items
            final int totalAmount = cursorRule.getAmount(cursor) + clickedRule.getAmount(clicked);
            final int maxSize = cursorRule.getMaxSize(cursor);
            if (!clickedRule.canApply(clicked, totalAmount)) {
                // Size is too big, stack as much as possible into clicked
                cursor = cursorRule.apply(cursor, totalAmount - maxSize);
                clicked = clickedRule.apply(clicked, maxSize);
            } else {
                // Merge cursor item clicked
                cursor = cursorRule.apply(cursor, 0);
                clicked = clickedRule.apply(clicked, totalAmount);
            }
        } else {
            // Items are not compatible, swap them
            var temp = cursor;
            cursor = clicked;
            clicked = temp;
        }
        if (cursor.isAir() && clicked.isAir()) {
            // return empty
            return ClickResultImpl.Single.empty();
        }
        return new ClickResultImpl.Single(cursor, Map.of(slot, clicked));
    }

    public static ClickResult.Single right(AbstractInventory inventory, int slot, ItemStack cursor) {
        ItemStack clicked = inventory.getItemStack(slot);
        final StackingRule cursorRule = cursor.getStackingRule();
        final StackingRule clickedRule = clicked.getStackingRule();
        if (clickedRule.canBeStacked(clicked, cursor)) {
            // Items can be stacked
            final int amount = clickedRule.getAmount(clicked) + 1;
            if (!clickedRule.canApply(clicked, amount)) {
                // Size too large, stop here
                return new ClickResultImpl.Single(cursor, Map.of());
            } else {
                // Add 1 to clicked
                cursor = cursorRule.apply(cursor, operand -> operand - 1);
                clicked = clickedRule.apply(clicked, amount);
            }
        } else {
            // Items cannot be stacked
            if (cursor.isAir()) {
                // Take half of clicked
                final int amount = (int) Math.ceil((double) clickedRule.getAmount(clicked) / 2d);
                cursor = cursorRule.apply(clicked, amount);
                clicked = clickedRule.apply(clicked, operand -> operand / 2);
            } else {
                if (clicked.isAir()) {
                    // Put 1 to clicked
                    cursor = cursorRule.apply(cursor, operand -> operand - 1);
                    clicked = clickedRule.apply(cursor, 1);
                } else {
                    // Swap items
                    var temp = cursor;
                    cursor = clicked;
                    clicked = temp;
                }
            }
        }
        if (cursor.isAir() && clicked.isAir()) {
            // return empty
            return ClickResultImpl.Single.empty();
        }
        return new ClickResultImpl.Single(cursor, Map.of(slot, clicked));
    }

    public static ClickResult.Shift shiftToInventory(Inventory inventory, ItemStack shifted) {
        if (shifted.isAir()) return ClickResultImpl.Shift.empty();
        var result = TransactionType.ADD.process(inventory, shifted);
        return new ClickResultImpl.Shift(result.first(), result.second());
    }
}
