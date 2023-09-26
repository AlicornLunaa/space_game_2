package com.alicornlunaa.spacegame.scenes.testing_scene;

import com.alicornlunaa.selene_engine.components.ScriptComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.core.BaseEntity;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.selene_engine.ecs.Registry;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.math.Vector2;

public class GravityComponent extends ScriptComponent {
    // Variables
    public static final float GRAV_C = 0.0002f;
    public static final float MIN_FORCE = 0.000001f;

    private TransformComponent transform = getEntity().getComponent(TransformComponent.class);
    private Registry registry;
    
    private float mass = 0.f;
    private float sphereOfInfluence;
    
    public Vector2 velocity = new Vector2();
    public Vector2 acceleration = new Vector2();

    public float dt = 1.0f;
    public int substeps = 8;

    // Private functions
    private Vector2 calculateGravity(){
        Vector2 a = new Vector2();

        for(int i = 0; i < registry.getEntities().size; i++){
            // Calculate gravity for every n-body
            IEntity otherEntity = registry.getEntity(i);

            if(otherEntity == getEntity()) continue; // Prevent infinite forces

            TransformComponent otherTransform = otherEntity.getComponent(TransformComponent.class);
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
                a.add(direction.scl(Constants.GRAVITY_CONSTANT * otherGravity.mass / radiusSqr));
            }
        }

        return a;
    }

    // Constructor
    public GravityComponent(BaseEntity entity, Registry registry, float vx, float vy, float mass) {
        super(entity);
        this.registry = registry;
        this.velocity.set(vx, vy);
        setMass(mass);
    }

    // Functions
    public float getMass(){ return mass; }
    public float getSphereOfInfluence(){ return sphereOfInfluence; }

    public void setMass(float mass){
        this.mass = mass;
        sphereOfInfluence = (float)Math.sqrt((Constants.GRAVITY_CONSTANT * mass) / MIN_FORCE);
    }

    @Override
    public void start() {}

    @Override
    public void update() {
        // Velocity verlet
        float sub_dt = dt / substeps;

        for(int i = 0; i < substeps; i++){
            Vector2 newPosition = transform.position.cpy().add(velocity.cpy().scl(sub_dt)).add(acceleration.cpy().scl(sub_dt).scl(sub_dt).scl(0.5f));
            Vector2 newAccel = calculateGravity();
            Vector2 newVelocity = velocity.cpy().add(acceleration.cpy().add(newAccel).scl(sub_dt).scl(0.5f));
            transform.position.set(newPosition);
            velocity.set(newVelocity);
            acceleration.set(newAccel);
        }
    }

    @Override
    public void render() {}
}
