package net.minestom.server.instance;

import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static net.minestom.server.utils.chunk.ChunkUtils.forDifferingChunksInRange;
import static net.minestom.server.utils.chunk.ChunkUtils.getChunkIndex;

final class EntityTrackerImpl {

    static final AtomicInteger TARGET_COUNTER = new AtomicInteger();

    static List<EntityTracker.Target<?>> targets;

    static List<EntityTracker.Target<?>> targets() {
        // Lazy init required to avoid initializer error
        List<EntityTracker.Target<?>> local = targets;
        if (local == null) {
            local = List.of(EntityTracker.Target.ENTITIES, EntityTracker.Target.PLAYERS, EntityTracker.Target.ITEMS, EntityTracker.Target.EXPERIENCE_ORBS);
            targets = local;
        }
        return local;
    }

    static <T extends Entity> EntityTracker.Target<T> create(Class<T> type) {
        final int ordinal = TARGET_COUNTER.getAndIncrement();
        return new EntityTracker.Target<>() {
            @Override
            public Class<T> type() {
                return type;
            }

            @Override
            public int ordinal() {
                return ordinal;
            }
        };
    }

    /**
     * Default tracking implementation storing entities per-chunk.
     */
    static final class PerChunk implements EntityTracker {
        private static final Long2ObjectFunction<List<Entity>> LIST_SUPPLIER = l -> new CopyOnWriteArrayList<>();

        // Store all data associated to a Target
        // The array index is the Target enum ordinal
        private final TargetEntry<Entity>[] entries = new TargetEntry[targets().size()];

        {
            Arrays.setAll(entries, value -> new TargetEntry<>());
        }

        @Override
        public synchronized void register(@NotNull Entity entity, @NotNull Point point, @Nullable Update<Entity> update) {
            for (var target : targets()) {
                if (target.type().isInstance(entity)) {
                    this.entries[target.ordinal()].entities.add(entity);
                }
            }
            addTo(point, entity);
            if (update != null) visibleEntities(point, findViewingTarget(entity), update::add);
        }

        @Override
        public synchronized void unregister(@NotNull Entity entity, @NotNull Point point, @Nullable Update<Entity> update) {
            for (var target : targets()) {
                if (target.type().isInstance(entity)) {
                    this.entries[target.ordinal()].entities.remove(entity);
                }
            }
            removeFrom(point, entity);
            if (update != null) visibleEntities(point, findViewingTarget(entity), update::remove);
        }

        @Override
        public synchronized void move(@NotNull Entity entity, @NotNull Point oldPoint, @NotNull Point newPoint, @Nullable Update<Entity> update) {
            if (!oldPoint.sameChunk(newPoint)) {
                removeFrom(oldPoint, entity);
                addTo(newPoint, entity);
                if (update != null) difference(oldPoint, newPoint, (Target<Entity>) findViewingTarget(entity), update);
            }
        }

        @Override
        public <T extends Entity> void difference(int oldChunkX, int oldChunkZ,
                                                  int newChunkX, int newChunkZ,
                                                  @NotNull Target<T> target, @NotNull Update<T> update) {
            final TargetEntry<Entity> entry = entries[target.ordinal()];
            forDifferingChunksInRange(newChunkX, newChunkZ, oldChunkX, oldChunkZ,
                    MinecraftServer.getEntityViewDistance(), (chunkX, chunkZ) -> {
                        // Add
                        final List<Entity> entities = entry.chunkEntities.get(getChunkIndex(chunkX, chunkZ));
                        if (entities == null || entities.isEmpty()) return;
                        for (Entity entity : entities) update.add((T) entity);
                    }, (chunkX, chunkZ) -> {
                        // Remove
                        final List<Entity> entities = entry.chunkEntities.get(getChunkIndex(chunkX, chunkZ));
                        if (entities == null || entities.isEmpty()) return;
                        for (Entity entity : entities) update.remove((T) entity);
                    });
        }

        @Override
        public synchronized <T extends Entity> void chunkEntities(int chunkX, int chunkZ, @NotNull Target<T> target, @NotNull Query<T> query) {
            final TargetEntry<Entity> entry = entries[target.ordinal()];
            final List<Entity> entities = entry.chunkEntities.get(getChunkIndex(chunkX, chunkZ));
            if (entities == null || entities.isEmpty()) return;
            for (Entity entity : entities) query.consume((T) entity);
        }

        @Override
        public <T extends Entity> void visibleEntities(@NotNull Point point, @NotNull Target<T> target, @NotNull Query<T> query) {
            // Gets reference to all chunk entities lists within the range
            // This is used to avoid a map lookup per chunk
            final TargetEntry<Entity> entry = entries[target.ordinal()];
            final List<Entity>[] range;
            synchronized (this) {
                range = entry.chunkRangeEntities.computeIfAbsent(ChunkUtils.getChunkIndex(point),
                        chunkIndex -> {
                            final int chunkX = ChunkUtils.getChunkCoordX(chunkIndex);
                            final int chunkZ = ChunkUtils.getChunkCoordZ(chunkIndex);
                            List<List<Entity>> entities = new ArrayList<>();
                            ChunkUtils.forChunksInRange(chunkX, chunkZ, MinecraftServer.getEntityViewDistance(),
                                    (x, z) -> entities.add(entry.chunkEntities.computeIfAbsent(getChunkIndex(x, z), LIST_SUPPLIER)));
                            return entities.toArray(List[]::new);
                        });
            }
            for (List<Entity> entities : range) { // LIST_SUPPLIER provide thread-safe lists
                if (entities.isEmpty()) continue;
                for (Entity entity : entities) query.consume((T) entity);
            }
        }

        @Override
        public synchronized <T extends Entity> void nearbyEntities(@NotNull Point point, double range, @NotNull Target<T> target, @NotNull Query<T> query) {
            final int chunkRange = Math.abs((int) (range / Chunk.CHUNK_SECTION_SIZE)) + 1;
            final double squaredRange = range * range;
            ChunkUtils.forChunksInRange(point, chunkRange, (chunkX, chunkZ) ->
                    chunkEntities(chunkX, chunkZ, target, entity -> {
                        if (point.distanceSquared(entity.getPosition()) < squaredRange) {
                            query.consume(entity);
                        }
                    }));
        }

        @Override
        public @UnmodifiableView @NotNull <T extends Entity> Set<@NotNull T> entities(@NotNull Target<T> target) {
            return (Set<T>) entries[target.ordinal()].entitiesView;
        }

        private static Target<? extends Entity> findViewingTarget(Entity entity) {
            if (entity instanceof Player) {
                // Players must be aware of all surrounding entities
                return Target.ENTITIES;
            }
            // General entities should only be aware of surrounding players to update their viewing list
            return Target.PLAYERS;
        }

        private void addTo(Point chunkPoint, Entity entity) {
            final long index = getChunkIndex(chunkPoint);
            for (var target : targets()) {
                if (target.type().isInstance(entity)) {
                    List<Entity> entities = entries[target.ordinal()].chunkEntities.computeIfAbsent(index, LIST_SUPPLIER);
                    entities.add(entity);
                }
            }
        }

        private void removeFrom(Point chunkPoint, Entity entity) {
            final long index = getChunkIndex(chunkPoint);
            for (var target : targets()) {
                if (target.type().isInstance(entity)) {
                    List<Entity> entities = entries[target.ordinal()].chunkEntities.get(index);
                    if (entities != null) entities.remove(entity);
                }
            }
        }

        private static final class TargetEntry<T extends Entity> {
            private final Set<T> entities = ConcurrentHashMap.newKeySet(); // Thread-safe since exposed
            private final Set<T> entitiesView = Collections.unmodifiableSet(entities);
            // Chunk index -> entities inside it
            private final Long2ObjectMap<List<T>> chunkEntities = new Long2ObjectOpenHashMap<>();
            // Chunk index -> lists of visible entities (references to chunkEntities entries)
            private final Long2ObjectMap<List<T>[]> chunkRangeEntities = new Long2ObjectOpenHashMap<>();
        }
    }

}