package net.minestom.server.event.server;

import net.minestom.server.event.generic.BlockPlaceEvent;
import net.minestom.server.instance.block.BlockHandler;

public class ServerBlockPlaceEvent extends BlockPlaceEvent {
    public ServerBlockPlaceEvent(BlockHandler.Placement placement) {
        super(placement);
    }
}
