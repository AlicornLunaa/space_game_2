package com.alicornlunaa.selene_engine.events;

import com.alicornlunaa.selene_engine.ecs.BodyComponent;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;

public class PhysicsEntityListener implements EntityListener {
    // Variables
    private ComponentMapper<BodyComponent> bm = ComponentMapper.getFor(BodyComponent.class);

    // Functions
    @Override
    public void entityAdded(Entity entity) {
    }

    @Override
    public void entityRemoved(Entity entity) {
        // Remove the body from the world
        BodyComponent bodyComponent = bm.get(entity);
        bodyComponent.world.getBox2DWorld().destroyBody(bodyComponent.body);
    }
}
