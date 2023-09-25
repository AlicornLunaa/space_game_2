package com.alicornlunaa.spacegame.scenes.testing_scene;

import com.alicornlunaa.selene_engine.components.ScriptComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.core.BaseEntity;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.selene_engine.ecs.Registry;
import com.badlogic.gdx.math.Vector2;

public class GravityComponent extends ScriptComponent {
    public static final float GRAV_C = 0.0002f;

    private TransformComponent transform = getEntity().getComponent(TransformComponent.class);

    private Registry registry;
    public Vector2 velocity = new Vector2();
    public Vector2 acceleration = new Vector2();
    public float mass = 0.f;
    public float dt = 1.0f;
    public int substeps = 8;

    public GravityComponent(BaseEntity entity, Registry registry, float vx, float vy, float mass) {
        super(entity);
        this.registry = registry;
        this.velocity.set(vx, vy);
        this.mass = mass;
    }

    public Vector2 calculateGravity(){
        Vector2 a = new Vector2();

        for(int i = 0; i < registry.getEntities().size; i++){
            IEntity otherEntity = registry.getEntity(i);

            if(otherEntity == getEntity()) continue;

            TransformComponent parentTransform = otherEntity.getComponent(TransformComponent.class);
            GravityComponent parentGravity = otherEntity.getComponent(GravityComponent.class);

            if(parentGravity != null){
                float radiusSqr = transform.position.dst2(parentTransform.position);
                Vector2 direction = parentTransform.position.cpy().sub(transform.position).nor();
                a.add(direction.scl(GRAV_C * parentGravity.mass / radiusSqr));
            }
        }

        return a;
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
