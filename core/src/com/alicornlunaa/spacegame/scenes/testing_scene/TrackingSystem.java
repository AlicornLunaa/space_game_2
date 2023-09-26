package com.alicornlunaa.spacegame.scenes.testing_scene;

import java.util.HashMap;

import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.selene_engine.ecs.ISystem;
import com.alicornlunaa.selene_engine.ecs.Registry;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;

public class TrackingSystem implements ISystem {
    // Static classes
    static private class VirtualBody {
        private IEntity ref;
        private Vector2 position = new Vector2();
        private Vector2 velocity = new Vector2();
        private Vector2 acceleration = new Vector2();
        private float mass = 0.f;

        private VirtualBody(IEntity ref, Vector2 position, Vector2 velocity, Vector2 acceleration, float mass){
            this.ref = ref;
            this.position = position;
            this.velocity = velocity;
            this.acceleration = acceleration;
            this.mass = mass;
        }
    };
    
    // Variables
    private ShapeRenderer renderer = App.instance.shapeRenderer;
    private Registry registry;

    private @Null IEntity referenceEntity;
    private Array<VirtualBody> virtualBodies = new Array<>();
    private HashMap<IEntity, Array<Vector2>> paths = new HashMap<>();

    // Constructor
    public TrackingSystem(Registry registry){
        this.registry = registry;
    }

    // Functions
    public void setReferenceEntity(@Null IEntity ref){
        referenceEntity = ref;
    }

    public Vector2 calculateGravity(Vector2 position, Array<VirtualBody> virtualBodies, @Null VirtualBody ignore){
        Vector2 a = new Vector2();

        for(int i = 0; i < virtualBodies.size; i++){
            VirtualBody vb = virtualBodies.get(i);

            if(vb == ignore) continue;

            float radiusSqr = position.dst2(vb.position);
            Vector2 direction = vb.position.cpy().sub(position).nor();
            a.add(direction.scl(Constants.GRAVITY_CONSTANT * vb.mass / radiusSqr));
        }

        return a;
    }

    public void integrate(Array<VirtualBody> virtualBodies, Vector2 position, Vector2 velocity, Vector2 acceleration, @Null VirtualBody ignore){
        int substeps = 10;
        float dt = 1.0f;
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
    public void beforeUpdate() {}

    @Override
    public void afterUpdate() {}

    @Override
    public void update(IEntity entity) {}

    @Override
    public void beforeRender() {
        // Initialize all virtual bodies
        virtualBodies.clear();

        for(int i = 0; i < registry.getEntities().size; i++){
            IEntity entity = registry.getEntity(i);

            TransformComponent transform = entity.getComponent(TransformComponent.class);
            GravityComponent gravityComponent = entity.getComponent(GravityComponent.class);

            if(gravityComponent != null){
                virtualBodies.add(new VirtualBody(
                    entity.hasComponent(TrackedEntityComponent.class) ? entity : null,
                    transform.position.cpy(),
                    gravityComponent.velocity.cpy(),
                    gravityComponent.acceleration.cpy(),
                    gravityComponent.getMass()
                ));
            }
        }

        for(int i = 0; i < 1000; i++){
            for(VirtualBody vb : virtualBodies){
                Array<Vector2> points = paths.getOrDefault(vb.ref, new Array<Vector2>());

                if((points.size - 1) < i){
                    points.add(vb.position.cpy());
                } else {
                    points.get(i).set(vb.position);
                }

                paths.put(vb.ref, points);
            }

            for(int k = 0; k < 20; k++){
                for(VirtualBody vb : virtualBodies){
                    integrate(virtualBodies, vb.position, vb.velocity, vb.acceleration, vb);
                }
            }
        }

        // Start render
        renderer.setProjectionMatrix(App.instance.camera.combined);
        renderer.setTransformMatrix(new Matrix4());
        renderer.begin(ShapeType.Filled);
    }

    @Override
    public void afterRender() {
        renderer.end();
    }

    @Override
    public void render(IEntity entity) {
        Array<Vector2> points = paths.get(entity);

        renderer.setColor(entity.getComponent(TrackedEntityComponent.class).color);

        if(points != null){
            for(int i = 0; i < points.size - 1; i++){
                renderer.rectLine(points.get(i), points.get(i + 1), 3 * (1.f - ((float)i / points.size)));
            }
        }
    }

    @Override
    public boolean shouldRunOnEntity(IEntity entity) {
        return entity.hasComponents(TrackedEntityComponent.class);
    }
}
