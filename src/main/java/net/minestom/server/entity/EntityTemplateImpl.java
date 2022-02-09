package net.minestom.server.entity;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

record EntityTemplateImpl(EntityType type) implements EntityTemplate {

    @Override
    public @NotNull CompletableFuture<IEntity> spawnAsync(@NotNull Instance instance, @NotNull Point position) {
        var entity = new Entity(type);
        return entity.setInstanceAsync(instance, position).thenApply(unused -> entity);
    }

    static final class Builder implements EntityTemplate.Builder {
        private EntityType type = EntityType.PIG;

        @Override
        public @NotNull EntityTemplate build() {
            return new EntityTemplateImpl(type);
        }

        @Override
        public EntityTemplate.@NotNull Builder type(@NotNull EntityType type) {
            this.type = type;
            return this;
        }
    }
}
