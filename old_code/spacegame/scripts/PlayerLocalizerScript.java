package com.alicornlunaa.spacegame.scripts;

import com.alicornlunaa.selene_engine.components_old.ScriptComponent;
import com.alicornlunaa.selene_engine.components_old.TransformComponent;
import com.alicornlunaa.selene_engine.core.BaseEntity;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.selene_engine.ecs.Registry;
import com.badlogic.gdx.math.Vector2;

public class PlayerLocalizerScript extends ScriptComponent {
    // Variables
    private Registry registry;

    // Constructor
    public PlayerLocalizerScript(Registry registry, IEntity entity){
        super((BaseEntity)entity);
        this.registry = registry;
    }

    // Functions
    @Override
    public void start() {}

    @Override
    public void update() {
        // Localize every object to the player
        TransformComponent transform = getEntity().getComponent(TransformComponent.class);
        Vector2 subFactor = transform.position.cpy();

        // Move every entity to make it zero
        for(int i = 0; i < registry.getEntities().size; i++){
            IEntity e = registry.getEntity(i);
            e.getComponent(TransformComponent.class).position.sub(subFactor);
        }
    }

    @Override
    public void render() {}    
}
