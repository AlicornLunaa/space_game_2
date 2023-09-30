package com.alicornlunaa.spacegame.scenes.testing_scene;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.ScriptComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.core.BaseEntity;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.selene_engine.ecs.Registry;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.math.Vector2;

public class GravityComponent extends ScriptComponent {
    // Variables
    public static final float MIN_FORCE = 0.1f;

    private TransformComponent transform = getEntity().getComponent(TransformComponent.class);
    private BodyComponent bodyComponent = getEntity().getComponent(BodyComponent.class);
    private Registry registry;

    // Private functions
    private Vector2 calculateGravity(){
        Vector2 a = new Vector2();

        for(int i = 0; i < registry.getEntities().size; i++){
            // Calculate gravity for every n-body
            IEntity otherEntity = registry.getEntity(i);

            if(otherEntity == getEntity()) continue; // Prevent infinite forces

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

    // Constructor
    public GravityComponent(BaseEntity entity, Registry registry) {
        super(entity);
        this.registry = registry;
    }

    // Functions
    public float getSphereOfInfluence(){
        return (float)Math.sqrt((Constants.GRAVITY_CONSTANT * bodyComponent.body.getMass()) / MIN_FORCE);
    }

    @Override
    public void start() {}

    @Override
    public void update() {
        // Update gravity
        bodyComponent.body.applyForceToCenter(calculateGravity(), true);
    }

    @Override
    public void render() {}
}
