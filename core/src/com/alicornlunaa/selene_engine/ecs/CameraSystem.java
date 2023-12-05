package com.alicornlunaa.selene_engine.ecs;

import com.alicornlunaa.space_game.App;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;

public class CameraSystem extends EntitySystem {
    // Variables
    private ImmutableArray<Entity> entities;
    private ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);
    private ComponentMapper<CameraComponent> cm = ComponentMapper.getFor(CameraComponent.class);

    // Constructor
    public CameraSystem(){}

    // Functions
    @Override
    public void addedToEngine(Engine engine){
        entities = engine.getEntitiesFor(Family.all(TransformComponent.class, CameraComponent.class).get());
    }

    @Override
    public void update(float deltaTime){
        // Update every entity
        for(int i = 0; i < entities.size(); i++){
            // Get entity info
            Entity entity = entities.get(i);
            TransformComponent transform = tm.get(entity);
            CameraComponent cameraComponent = cm.get(entity);
            
            cameraComponent.camera.position.set(transform.position, 0.0f);
            cameraComponent.camera.update();
            
            if(cameraComponent.active && App.instance.camera != cameraComponent.camera){
                App.instance.camera = cameraComponent.camera;
            }
        }
    }
}
