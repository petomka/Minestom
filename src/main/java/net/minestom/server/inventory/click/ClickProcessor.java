package net.minestom.server.inventory.click;

import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.inventory.TransactionType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.StackingRule;
import net.minestom.server.utils.MathUtils;

import java.util.HashMap;
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

    public static ClickResult.Single shiftToInventory(Inventory inventory, ItemStack shifted) {
        if (shifted.isAir()) return ClickResultImpl.Single.empty();
        // TODO: check inventory type to avoid certain slots (e.g. crafting result)
        var result = TransactionType.ADD.process(inventory, shifted);
        return new ClickResultImpl.Single(result.first(), result.second());
    }

    public static ClickResult.Single shiftToPlayer(PlayerInventory inventory, ItemStack shifted) {
        if (shifted.isAir()) return ClickResultImpl.Single.empty();
        // Try shifting from 8->0
        var result = TransactionType.ADD.process(inventory, shifted, (slot, itemStack) -> true, 8, 0, -1);
        var remaining = result.first();
        if (remaining.isAir()) {
            return new ClickResultImpl.Single(result.first(), result.second());
        }
        // Try 35->9
        var result2 = TransactionType.ADD.process(inventory, remaining, (slot, itemStack) -> true, 35, 9, -1);

        remaining = result2.first();
        var changes = result2.second();
        changes.putAll(result.second());
        return new ClickResultImpl.Single(remaining, changes);
    }

    public static ClickResult.Single shiftWithinPlayer(PlayerInventory inventory, int slot, ItemStack shifted) {
        if (shifted.isAir()) return ClickResultImpl.Single.empty();

        // Handle equipment
        if (MathUtils.isBetween(slot, 0, 35)) {
            final Material material = shifted.getMaterial();
            final EquipmentSlot equipmentSlot = material.registry().equipmentSlot();
            if (equipmentSlot != null) {
                // Shift-click equip
                final ItemStack currentArmor = inventory.getEquipment(equipmentSlot);
                if (currentArmor.isAir()) {
                    final int armorSlot = equipmentSlot.armorSlot();
                    return new ClickResultImpl.Single(ItemStack.AIR, Map.of(armorSlot, shifted));
                } else {
                    // Equipment already present, do not change anything
                    return new ClickResultImpl.Single(shifted, Map.of());
                }
            }
        }

        // General shift
        if (MathUtils.isBetween(slot, 0, 8)) {
            // Shift from 9->35
            var result = TransactionType.ADD.process(inventory, shifted, (s, itemStack) -> true, 9, 36, 1);
            return new ClickResultImpl.Single(result.first(), result.second());
        } else if (MathUtils.isBetween(slot, 9, 35)) {
            // Shift from 0->8
            var result = TransactionType.ADD.process(inventory, shifted, (s, itemStack) -> true, 0, 9, 1);
            return new ClickResultImpl.Single(result.first(), result.second());
        } else {
            // Try shifting from 9->35
            var result = TransactionType.ADD.process(inventory, shifted, (s, itemStack) -> true, 9, 36, 1);
            var remaining = result.first();
            if (remaining.isAir()) {
                return new ClickResultImpl.Single(result.first(), result.second());
            }
            // Try shifting from 0->8
            var result2 = TransactionType.ADD.process(inventory, remaining, (s, itemStack) -> true, 0, 9, 1);

            remaining = result2.first();
            var changes = result2.second();
            changes.putAll(result.second());
            return new ClickResultImpl.Single(remaining, changes);
        }
    }

    public static ClickResult.Double doubleClick(PlayerInventory playerInventory, Inventory inventory, ItemStack cursor) {
        if (cursor.isAir()) return ClickResultImpl.Double.empty();
        final StackingRule cursorRule = cursor.getStackingRule();
        final int amount = cursorRule.getAmount(cursor);
        final int maxSize = cursorRule.getMaxSize(cursor);
        final int remainingAmount = maxSize - amount;
        if (remainingAmount == 0) {
            // Item is already full
            return new ClickResultImpl.Double(cursor, Map.of(), Map.of());
        }
        ItemStack remaining = cursorRule.apply(cursor, remainingAmount);
        Map<Integer, ItemStack> playerChanges = new HashMap<>();
        Map<Integer, ItemStack> inventoryChanges = new HashMap<>();
        // Loop through open inventory
        {
            // TODO: check inventory type to avoid certain slots (e.g. crafting result)
            var result = TransactionType.TAKE.process(inventory, remaining);
            remaining = result.first();
            inventoryChanges.putAll(result.second());
        }
        // Loop through player inventory
        {
            // 9->36
            if (!remaining.isAir()) {
                var result = TransactionType.TAKE.process(playerInventory, remaining, (slot, itemStack) -> true, 9, 36, 1);
                remaining = result.first();
                playerChanges.putAll(result.second());
            }
            // 8->0
            if (!remaining.isAir()) {
                var result = TransactionType.TAKE.process(playerInventory, remaining, (slot, itemStack) -> true, 8, 0, -1);
                remaining = result.first();
                playerChanges.putAll(result.second());
            }
        }

        // Update cursor based on the remaining
        if (remaining.isAir()) {
            // Item has been filled
            remaining = cursorRule.apply(cursor, maxSize);
        } else {
            final int tookAmount = remainingAmount - cursorRule.getAmount(remaining);
            remaining = cursorRule.apply(cursor, amount + tookAmount);
        }
        return new ClickResultImpl.Double(remaining, playerChanges, inventoryChanges);
    }

    public static ClickResult.Single doubleWithinPlayer(PlayerInventory inventory, ItemStack cursor) {
        if (cursor.isAir()) return ClickResultImpl.Single.empty();
        final StackingRule cursorRule = cursor.getStackingRule();
        final int amount = cursorRule.getAmount(cursor);
        final int maxSize = cursorRule.getMaxSize(cursor);
        final int remainingAmount = maxSize - amount;
        if (remainingAmount == 0) {
            // Item is already full
            return new ClickResultImpl.Single(cursor, Map.of());
        }
        ItemStack remaining = cursorRule.apply(cursor, remainingAmount);
        // Try taking from 9->35
        var result = TransactionType.TAKE.process(inventory, remaining, (slot, itemStack) -> true, 9, 36, 1);
        remaining = result.first();
        Map<Integer, ItemStack> changes = result.second();
        // Try 0->8
        if (!remaining.isAir()) {
            var result2 = TransactionType.TAKE.process(inventory, remaining, (slot, itemStack) -> true, 0, 9, 1);
            remaining = result2.first();
            changes.putAll(result2.second());
        }
        // Try 37->40 (crafting slots)
        if (!remaining.isAir()) {
            var result2 = TransactionType.TAKE.process(inventory, remaining, (slot, itemStack) -> true, 37, 40, 1);
            remaining = result2.first();
            changes.putAll(result2.second());
        }

        // Update cursor based on the remaining
        if (remaining.isAir()) {
            // Item has been filled
            remaining = cursorRule.apply(cursor, maxSize);
        } else {
            final int tookAmount = remainingAmount - cursorRule.getAmount(remaining);
            remaining = cursorRule.apply(cursor, amount + tookAmount);
        }
        return new ClickResultImpl.Single(remaining, changes);
    }
}
