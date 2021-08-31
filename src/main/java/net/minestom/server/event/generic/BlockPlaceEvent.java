package net.minestom.server.event.generic;

import net.minestom.server.coordinate.Point;
import net.minestom.server.event.trait.BlockEvent;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.RecursiveEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import org.jetbrains.annotations.NotNull;

public class BlockPlaceEvent implements RecursiveEvent, BlockEvent, CancellableEvent {
    private final BlockHandler.Placement placement;

    public BlockPlaceEvent(BlockHandler.Placement placement) {
        this.placement = placement;
    }

    public BlockHandler.Placement getPlacement() {
        return placement;
    }

    /**
     * Gets the block which will be placed.
     *
     * @return the block to place
     */
    @Override
    public @NotNull Block getBlock() {
        return placement.getBlock();
    }

    /**
     * Changes the block to be placed.
     *
     * @param block the new block
     */
    public void setBlock(@NotNull Block block) {
        this.placement.setBlock(block);
    }

    /**
     * Gets the block position.
     *
     * @return the block position
     */
    public @NotNull Point getBlockPosition() {
        return placement.getBlockPosition();
    }

    @Override
    public boolean isCancelled() {
        return placement.isCancelled();
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.placement.setCancelled(cancel);
    }
}
