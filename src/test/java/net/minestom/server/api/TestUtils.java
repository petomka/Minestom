package net.minestom.server.api;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;

import java.net.SocketAddress;
import java.util.UUID;

public final class TestUtils {

    public static Player createDummyPlayer() {
        return new Player(UUID.randomUUID(), "test", new PlayerConnection() {
            @Override
            public void sendPacket(@NotNull SendablePacket packet) {

            }

            @Override
            public @NotNull SocketAddress getRemoteAddress() {
                return null;
            }

            @Override
            public void disconnect() {

            }
        });
    }
}
