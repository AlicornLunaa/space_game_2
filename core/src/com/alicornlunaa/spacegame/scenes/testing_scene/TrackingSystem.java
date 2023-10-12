package com.alicornlunaa.spacegame.scenes.testing_scene;

import java.util.HashMap;

import com.alicornlunaa.selene_engine.components.BodyComponent;
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

        private VirtualBody(IEntity ref, Vector2 position, Vector2 velocity, float mass){
            this.ref = ref;
            this.position = position;
            this.velocity = velocity;
            this.mass = mass;
        }

        private Vector2 calculateGravity(Array<VirtualBody> virtualBodies, Vector2 position){
            Vector2 a = new Vector2();
    
            for(int i = 0; i < virtualBodies.size; i++){
                VirtualBody vb = virtualBodies.get(i);
                GravityComponent vbGravity = vb.ref.getComponent(GravityComponent.class);
    
                if(vb == this) continue; // Prevent infinite forces
    
                float radiusSqr = position.dst2(vb.position);
                float soi = vbGravity.getSphereOfInfluence();
    
                // Prevent insignificant forces
                if(radiusSqr > soi * soi)
                    continue;
    
                // Calculate forces
                Vector2 direction = vb.position.cpy().sub(position).nor();
                a.add(direction.scl(Constants.GRAVITY_CONSTANT * vb.mass / radiusSqr));
            }
    
            return a;
        }

        private void integrate(Array<VirtualBody> virtualBodies, float dt){
            Vector2 newAcc = calculateGravity(virtualBodies, position.add(velocity.cpy().scl(dt)).add(acceleration.cpy().scl(dt * dt * 0.5f)));
            velocity.add(acceleration.cpy().add(newAcc).scl(dt * 0.5f));
            acceleration.set(newAcc);
        }
    };
    
    // Variables
    private ShapeRenderer renderer = App.instance.shapeRenderer;
    private Registry registry;

    private @Null IEntity referenceEntity;
    private @Null TransformComponent referenceTransform = null;

    private Array<VirtualBody> virtualBodies = new Array<>();
    private HashMap<IEntity, Array<Vector2>> paths = new HashMap<>();

    // Constructor
    public TrackingSystem(Registry registry){
        this.registry = registry;
    }

    // Functions
    public void setReferenceEntity(@Null IEntity ref){
        referenceEntity = ref;
        referenceTransform = (ref == null) ? null : ref.getComponent(TransformComponent.class);
    }

    public void translate(Vector2 v){
        // Translates everything by vector
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
        @Null VirtualBody referenceVirtualBody = null;
        virtualBodies.clear();

        for(int i = 0; i < registry.getEntities().size; i++){
            IEntity entity = registry.getEntity(i);

            TransformComponent transform = entity.getComponent(TransformComponent.class);
            BodyComponent bodyComponent = entity.getComponent(BodyComponent.class);
            GravityComponent gravityComponent = entity.getComponent(GravityComponent.class);

            if(gravityComponent != null){
                virtualBodies.add(new VirtualBody(
                    entity,
                    transform.position.cpy(),
                    bodyComponent.body.getLinearVelocity().cpy(),
                    bodyComponent.body.getMass()
                ));

                if(entity == referenceEntity){
                    referenceVirtualBody = virtualBodies.peek();
                }
            }
        }

        for(int i = 0; i < 3000; i++){
            Vector2 referencePoint = (referenceVirtualBody == null) ? Vector2.Zero.cpy() : referenceVirtualBody.position.cpy();

            for(VirtualBody vb : virtualBodies){
                if(vb.ref == null) continue;
                if(!vb.ref.hasComponent(TrackedEntityComponent.class)) continue;
                
                Array<Vector2> points = paths.getOrDefault(vb.ref, new Array<Vector2>());

                if((points.size - 1) < i){
                    points.add(vb.position.cpy().sub(referencePoint));
                } else {
                    points.get(i).set(vb.position).sub(referencePoint);
                }

                paths.put(vb.ref, points);
            }

            int substeps = 12;
            float dt = 0.05f;
            float sub_dt = dt / substeps;

            for(int steps = 0; steps < substeps; steps++){
                for(VirtualBody vb : virtualBodies){
                    vb.integrate(virtualBodies, sub_dt);
                }
            }
        }

        // Start render
        Vector2 referencePoint = (referenceEntity == null) ? Vector2.Zero.cpy() : referenceTransform.position.cpy();
        renderer.setProjectionMatrix(App.instance.camera.combined);
        renderer.setTransformMatrix(new Matrix4().translate(referencePoint.x, referencePoint.y, 0.0f));
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
