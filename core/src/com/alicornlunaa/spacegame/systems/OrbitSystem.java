package com.alicornlunaa.spacegame.systems;

import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.selene_engine.ecs.ISystem;
import com.alicornlunaa.spacegame.components.OrbitComponent;

public class OrbitSystem implements ISystem {

    @Override
    public void beforeUpdate() {}

    @Override
    public void afterUpdate() {}

    @Override
    public void update(IEntity entity) {
        TransformComponent transform = entity.getComponent(TransformComponent.class);
        OrbitComponent orbitComponent = entity.getComponent(OrbitComponent.class);
    }

    @Override
    public void beforeRender() {}

    @Override
    public void afterRender() {}

    @Override
    public void render(IEntity entity) {
        TransformComponent transform = entity.getComponent(TransformComponent.class);
        OrbitComponent orbitComponent = entity.getComponent(OrbitComponent.class);
    }

    @Override
    public boolean shouldRunOnEntity(IEntity entity) {
        return entity.hasComponents(TransformComponent.class, OrbitComponent.class);
    }
    
}
