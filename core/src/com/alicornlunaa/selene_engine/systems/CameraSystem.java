package com.alicornlunaa.selene_engine.systems;

import com.alicornlunaa.selene_engine.components.CameraComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.selene_engine.ecs.ISystem;
import com.alicornlunaa.spacegame.App;

public class CameraSystem implements ISystem {

    // Variables
    private final App game;

    // Constructor
    public CameraSystem(App game){
        this.game = game;
    }

    // Functions
    @Override
    public void beforeUpdate() {}

    @Override
    public void afterUpdate() {}

    @Override
    public void update(IEntity entity) {}

    @Override
    public void beforeRender() {}

    @Override
    public void afterRender() {}

    @Override
    public void render(IEntity entity) {
        TransformComponent transform = entity.getComponent(TransformComponent.class);
        CameraComponent cameraComponent = entity.getComponent(CameraComponent.class);
        
        cameraComponent.camera.position.set(transform.position, 0.0f);
        cameraComponent.camera.update();
        
        if(cameraComponent.active && game.activeCamera != cameraComponent.camera){
            game.activeCamera = cameraComponent.camera;
        }
    }

    @Override
    public boolean shouldRunOnEntity(IEntity entity) {
        return (entity.hasComponents(CameraComponent.class, TransformComponent.class));
    }
    
}
