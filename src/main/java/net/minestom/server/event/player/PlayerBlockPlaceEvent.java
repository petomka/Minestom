package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.generic.BlockPlaceEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player tries placing a block.
 */
public class PlayerBlockPlaceEvent extends BlockPlaceEvent implements PlayerEvent {

    private final Player player;
    private final BlockFace blockFace;
    private final Player.Hand hand;

    private boolean consumeBlock;

    public PlayerBlockPlaceEvent(BlockHandler.PlayerPlacement placement) {
        super(placement);
        this.player = placement.getPlayer();
        this.blockFace = placement.getBlockFace();
        this.hand = placement.getHand();
    }

    public @NotNull BlockFace getBlockFace() {
        return blockFace;
    }

    /**
     * Gets the hand with which the player is trying to place.
     *
     * @return the hand used
     */
    public @NotNull Player.Hand getHand() {
        return hand;
    }

    /**
     * Should the block be consumed if not cancelled.
     *
     * @param consumeBlock true if the block should be consumer (-1 amount), false otherwise
     */
    public void consumeBlock(boolean consumeBlock) {
        this.consumeBlock = consumeBlock;
    }

    /**
     * Should the block be consumed if not cancelled.
     *
     * @return true if the block will be consumed, false otherwise
     */
    public boolean doesConsumeBlock() {
        return consumeBlock;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }
}
