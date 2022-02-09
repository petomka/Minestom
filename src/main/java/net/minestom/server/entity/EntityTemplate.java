package net.minestom.server.entity;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

@ApiStatus.Experimental
public interface EntityTemplate {

    @NotNull EntityType type();

    @NotNull CompletableFuture<IEntity> spawnAsync(@NotNull Instance instance, @NotNull Point position);

    default @NotNull IEntity spawn(@NotNull Instance instance, @NotNull Point position) {
        return spawnAsync(instance, position).join();
    }

    static EntityTemplate ofType(@NotNull EntityType type) {
        return builder().type(type).build();
    }

    static @NotNull Builder builder() {
        return new EntityTemplateImpl.Builder();
    }

    interface Builder {
        @NotNull EntityTemplate build();

        @NotNull Builder type(@NotNull EntityType type);
    }
}
