package com.alicornlunaa.spacegame.scenes.testing_scene;

import com.alicornlunaa.selene_engine.components.ShapeDrawableComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.core.BaseEntity;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.selene_engine.ecs.Registry;
import com.alicornlunaa.spacegame.App;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;

public class PathComponent extends ShapeDrawableComponent {
    // Static classes
    static private class VirtualBody {
        private Vector2 position = new Vector2();
        private Vector2 velocity = new Vector2();
        private Vector2 acceleration = new Vector2();
        private float mass = 0.f;

        private VirtualBody(Vector2 position, Vector2 velocity, Vector2 acceleration, float mass){
            this.position = position;
            this.velocity = velocity;
            this.acceleration = acceleration;
            this.mass = mass;
        }
    };

    // Variable
    public static final float GRAV_C = 0.0002f;

    private TransformComponent transform = getEntity().getComponent(TransformComponent.class);
    private GravityComponent gravityComponent = getEntity().getComponent(GravityComponent.class);

    private @Null TransformComponent relativeTransform = null;

    private Registry registry;
    public float dt = 4.0f;
    public int substeps = 8;
    public int steps = 1000;

    // Constructor
    public PathComponent(BaseEntity entity, @Null BaseEntity relative, Registry registry) {
        super(entity);

        if(relative != null)
            relativeTransform = relative.getComponent(TransformComponent.class);

        this.registry = registry;
    }

    // Functions
    public Vector2 calculateGravity(Vector2 position, Array<VirtualBody> virtualBodies, @Null VirtualBody ignore){
        Vector2 a = new Vector2();

        for(int i = 0; i < virtualBodies.size; i++){
            VirtualBody vb = virtualBodies.get(i);

            if(vb == ignore) continue;

            float radiusSqr = position.dst2(vb.position);
            Vector2 direction = vb.position.cpy().sub(position).nor();
            a.add(direction.scl(GRAV_C * vb.mass / radiusSqr));
        }

        return a;
    }

    public void integrate(Array<VirtualBody> virtualBodies, Vector2 position, Vector2 velocity, Vector2 acceleration, @Null VirtualBody ignore){
        float sub_dt = dt / substeps;

        for(int i = 0; i < substeps; i++){
            Vector2 newPosition = position.cpy().add(velocity.cpy().scl(sub_dt)).add(acceleration.cpy().scl(sub_dt).scl(sub_dt).scl(0.5f));
            Vector2 newAccel = calculateGravity(position, virtualBodies, ignore);
            Vector2 newVelocity = velocity.cpy().add(acceleration.cpy().add(newAccel).scl(sub_dt).scl(0.5f));
            position.set(newPosition);
            velocity.set(newVelocity);
            acceleration.set(newAccel);
        }
    }

    @Override
    public void draw(ShapeRenderer renderer) {
        Array<VirtualBody> virtualBodies = new Array<>();
        for(int i = 0; i < registry.getEntities().size; i++){
            IEntity otherEntity = registry.getEntity(i);

            if(otherEntity == getEntity()) continue;

            TransformComponent transform = otherEntity.getComponent(TransformComponent.class);
            GravityComponent gravityComponent = otherEntity.getComponent(GravityComponent.class);

            if(gravityComponent != null){
                virtualBodies.add(new VirtualBody(
                    transform.position.cpy(),
                    gravityComponent.velocity.cpy(),
                    gravityComponent.acceleration.cpy(),
                    gravityComponent.mass
                ));
            }
        }

        Vector2 position = transform.position.cpy();
        Vector2 velocity = gravityComponent.velocity.cpy();
        Vector2 acceleration = new Vector2();

        renderer.setProjectionMatrix(App.instance.camera.combined);
        renderer.setTransformMatrix(new Matrix4());
        
        for(int i = 0; i < steps; i++){
            Vector2 relativePosition = (relativeTransform == null) ? Vector2.Zero.cpy() : relativeTransform.position.cpy();
            Vector2 oldPosition = position.cpy();

            for(int k = 0; k < 100; k++){
                integrate(virtualBodies, position, velocity, acceleration, null);

                for(VirtualBody vb : virtualBodies){
                    integrate(virtualBodies, vb.position, vb.velocity, vb.acceleration, vb);
                }
            }

            renderer.rectLine(oldPosition.sub(relativePosition), position.cpy().sub(relativePosition), 3);
        }
    }
}
