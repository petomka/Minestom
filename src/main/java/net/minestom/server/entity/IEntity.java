package net.minestom.server.entity;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.minestom.server.Tickable;
import net.minestom.server.Viewable;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.permission.PermissionHandler;
import net.minestom.server.tag.TagHandler;
import net.minestom.server.timer.Schedulable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

@ApiStatus.Experimental
public interface IEntity extends Viewable, Tickable, Schedulable,
        TagHandler, PermissionHandler,
        HoverEventSource<HoverEvent.ShowEntity>, Sound.Emitter {

    /**
     * Each entity has a unique id (server-wide) which will change after a restart.
     *
     * @return the unique entity id
     * @see Entity#getEntity(int) to retrive an entity based on its id
     */
    int id();

    @NotNull EntityType type();

    @NotNull EntityMeta meta();

    @NotNull Instance instance();

    @NotNull Chunk currentChunk();

    @NotNull Pos position();

    @NotNull CompletableFuture<Void> teleportAsync(@NotNull Point point);

    default void teleport(@NotNull Point point) {
        teleportAsync(point).join();
    }

    void remove();

    boolean isRemoved();

    // DEPRECATIONS

    @Deprecated
    default int getEntityId() {
        return id();
    }

    @Deprecated
    default EntityType getEntityType() {
        return type();
    }

    @Deprecated
    default EntityMeta getEntityMeta() {
        return meta();
    }

    @Deprecated
    default Instance getInstance() {
        return instance();
    }

    @Deprecated
    default Chunk getChunk() {
        return currentChunk();
    }

    @Deprecated
    default Pos getPosition() {
        return position();
    }
}
