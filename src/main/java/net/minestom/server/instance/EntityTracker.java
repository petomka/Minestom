package net.minestom.server.instance;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.ExperienceOrb;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Set;

/**
 * Defines how {@link Entity entities} are tracked within an {@link Instance instance}.
 * <p>
 * Implementations are expected to be thread-safe.
 */
@ApiStatus.Experimental
public interface EntityTracker {

    /**
     * Register an entity to be tracked.
     */
    void register(@NotNull Entity entity, @NotNull Point point, @Nullable Update<Entity> update);

    /**
     * Unregister an entity tracking.
     */
    void unregister(@NotNull Entity entity, @NotNull Point point, @Nullable Update<Entity> update);

    /**
     * Called every time an entity move, you may want to verify if the new
     * position is in a different chunk.
     */
    void move(@NotNull Entity entity, @NotNull Point oldPoint, @NotNull Point newPoint, @Nullable Update<Entity> update);

    /**
     * Gets the entities newly visible and invisible from one chunk to another.
     */
    <T extends Entity> void difference(int oldChunkX, int oldChunkZ,
                                       int newChunkX, int newChunkZ,
                                       @NotNull Target<T> target, @NotNull Update<T> update);

    default <T extends Entity> void difference(@NotNull Point from, @NotNull Point to, @NotNull Target<T> target, @NotNull Update<T> update) {
        difference(from.chunkX(), from.chunkZ(), to.chunkX(), to.chunkZ(), target, update);
    }

    /**
     * Gets the entities present in the specified chunk.
     */
    <T extends Entity> void chunkEntities(int chunkX, int chunkZ, @NotNull Target<T> target, @NotNull Query<T> query);

    default <T extends Entity> void chunkEntities(@NotNull Point point, @NotNull Target<T> target, @NotNull Query<T> query) {
        chunkEntities(point.chunkX(), point.chunkZ(), target, query);
    }

    /**
     * Gets the entities present in range of the specified chunk.
     * <p>
     * This is used for auto-viewable features.
     */
    <T extends Entity> void visibleEntities(@NotNull Point point, @NotNull Target<T> target, @NotNull Query<T> query);

    /**
     * Gets the entities within a range.
     */
    <T extends Entity> void nearbyEntities(@NotNull Point point, double range, @NotNull Target<T> target, @NotNull Query<T> query);

    /**
     * Gets all the entities tracked by this class.
     */
    @UnmodifiableView
    @NotNull <T extends Entity> Set<@NotNull T> entities(@NotNull Target<T> target);

    @UnmodifiableView
    default @NotNull Set<@NotNull Entity> entities() {
        return entities(Target.ENTITIES);
    }

    /**
     * Represents the type of entity you want to retrieve.
     *
     * @param <E> the entity type
     */
    @ApiStatus.NonExtendable
    interface Target<E extends Entity> {
        Target<Entity> ENTITIES = EntityTrackerImpl.create(Entity.class);
        Target<Player> PLAYERS = EntityTrackerImpl.create(Player.class);
        Target<ItemEntity> ITEMS = EntityTrackerImpl.create(ItemEntity.class);
        Target<ExperienceOrb> EXPERIENCE_ORBS = EntityTrackerImpl.create(ExperienceOrb.class);

        Class<E> type();

        int ordinal();
    }

    /**
     * Callback to know the newly visible entities and those to remove.
     */
    interface Update<E extends Entity> {
        void add(E entity);

        void remove(E entity);
    }

    /**
     * Query entities.
     * <p>
     * This is not a functional interface, we reserve the right to add other methods.
     */
    interface Query<E extends Entity> {
        void consume(E entity);
    }
}