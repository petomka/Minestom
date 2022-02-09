package net.minestom.server.entity;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.minestom.server.Tickable;
import net.minestom.server.Viewable;
import net.minestom.server.instance.Instance;
import net.minestom.server.permission.PermissionHandler;
import net.minestom.server.tag.TagHandler;
import net.minestom.server.timer.Schedulable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Experimental
public interface IEntity extends Viewable, Tickable, Schedulable,
        TagHandler, PermissionHandler,
        HoverEventSource<HoverEvent.ShowEntity>, Sound.Emitter {

    @NotNull EntityType type();

    @NotNull Instance instance();
}
