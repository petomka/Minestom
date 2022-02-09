package net.minestom.server.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EntityTemplateTest {

    @Test
    public void basic() {
        var template = EntityTemplate.ofType(EntityType.PIG);
        assertEquals(EntityType.PLAYER, template.type());
    }
}
