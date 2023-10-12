package com.alicornlunaa.spacegame.systems;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.selene_engine.ecs.ISystem;
import com.alicornlunaa.selene_engine.ecs.Registry;
import com.alicornlunaa.spacegame.components.GravityComponent;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.math.Vector2;

public class GravitySystem implements ISystem {
    // Variables
    public static final float MIN_FORCE = 0.05f;
    private Registry registry;

    // Constructor
    public GravitySystem(Registry registry){
        this.registry = registry;
    }

    // Private functions
    private Vector2 calculateGravity(IEntity entity, GravityComponent gravityComponent){
        TransformComponent transform = gravityComponent.getTransform();
        BodyComponent bodyComponent = gravityComponent.getBodyComponent();

        Vector2 a = new Vector2();

        for(int i = 0; i < registry.getEntities().size; i++){
            // Calculate gravity for every n-body
            IEntity otherEntity = registry.getEntity(i);

            if(otherEntity == entity) continue; // Prevent infinite forces

            TransformComponent otherTransform = otherEntity.getComponent(TransformComponent.class);
            BodyComponent otherBodyComponent = otherEntity.getComponent(BodyComponent.class);
            GravityComponent otherGravity = otherEntity.getComponent(GravityComponent.class);

            // Only apply if they also have a gravity component
            if(otherGravity != null){
                // Get variables
                float radiusSqr = transform.position.dst2(otherTransform.position);
                float soi = otherGravity.getSphereOfInfluence();
                
                // Prevent insignificant forces
                if(radiusSqr > soi * soi)
                    continue;

                // Calculate gravitational force
                Vector2 direction = otherTransform.position.cpy().sub(transform.position).nor();
                a.add(direction.scl(Constants.GRAVITY_CONSTANT * otherBodyComponent.body.getMass() * bodyComponent.body.getMass() / radiusSqr));
            }
        }

        return a;
    }

    // Functions
    @Override
    public void beforeUpdate() {}

    @Override
    public void afterUpdate() {}

    @Override
    public void update(IEntity entity) {
        // Update gravity for entity
        GravityComponent gravityComponent = entity.getComponent(GravityComponent.class);
        gravityComponent.getBodyComponent().body.applyForceToCenter(calculateGravity(entity, gravityComponent), true);
    }

    @Override
    public void beforeRender() {}

    @Override
    public void afterRender() {}

    @Override
    public void render(IEntity entity) {}

    @Override
    public boolean shouldRunOnEntity(IEntity entity) {
        return entity.hasComponent(GravityComponent.class);
    }
}
