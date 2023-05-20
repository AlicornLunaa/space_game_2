package com.alicornlunaa.selene_engine.systems;

import com.alicornlunaa.selene_engine.components.IScriptComponent;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.selene_engine.ecs.ISystem;

public class ScriptSystem implements ISystem {

    @Override
    public void beforeUpdate() {}

    @Override
    public void afterUpdate() {}

    @Override
    public void update(IEntity entity) {
        IScriptComponent script = ((IScriptComponent)entity.getComponent(IScriptComponent.class));
        script.update();
    }

    @Override
    public void beforeRender() {}

    @Override
    public void afterRender() {}

    @Override
    public void render(IEntity entity) {
        IScriptComponent script = ((IScriptComponent)entity.getComponent(IScriptComponent.class));
        script.render();
    }

    @Override
    public boolean shouldRunOnEntity(IEntity entity) {
        return (entity.hasComponent(IScriptComponent.class));
    }
    
}
