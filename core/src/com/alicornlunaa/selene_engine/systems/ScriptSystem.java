package com.alicornlunaa.selene_engine.systems;

import com.alicornlunaa.selene_engine.components.ScriptComponent;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.selene_engine.ecs.ISystem;

public class ScriptSystem implements ISystem {

    @Override
    public void beforeUpdate() {}

    @Override
    public void afterUpdate() {}

    @Override
    public void update(IEntity entity) {
        ScriptComponent[] scripts = entity.getComponents(ScriptComponent.class);

        for(ScriptComponent script : scripts)
            script.update();
    }

    @Override
    public void beforeRender() {}

    @Override
    public void afterRender() {}

    @Override
    public void render(IEntity entity) {
        ScriptComponent[] scripts = entity.getComponents(ScriptComponent.class);

        for(ScriptComponent script : scripts)
            script.render();
    }

    @Override
    public boolean shouldRunOnEntity(IEntity entity) {
        return (entity.hasComponent(ScriptComponent.class));
    }
    
}
