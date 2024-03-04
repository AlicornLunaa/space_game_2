package com.alicornlunaa.space_game.systems;

import com.alicornlunaa.selene_engine.ecs.BodyComponent;
import com.alicornlunaa.selene_engine.ecs.TransformComponent;
import com.alicornlunaa.space_game.App;
import com.alicornlunaa.space_game.components.celestial.GravityComponent;
import com.alicornlunaa.space_game.components.celestial.TrackedEntityComponent;
import com.alicornlunaa.space_game.util.Constants;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;

public class TrackingSystem extends EntitySystem {
    // Static classes
    static private class VirtualBody {
        private Entity ref;
        private @Null TrackedEntityComponent trackedEntityComponent;
        private Vector2 position = new Vector2();
        private Vector2 velocity = new Vector2();
        private Vector2 acceleration = new Vector2();
        private float mass = 0.f;

        private VirtualBody(Entity ref, Vector2 position, Vector2 velocity, float mass){
            this.ref = ref;
            this.trackedEntityComponent = ref.getComponent(TrackedEntityComponent.class);
            this.position = position;
            this.velocity = velocity;
            this.mass = mass;
        }

        private Vector2 calculateGravity(Array<VirtualBody> virtualBodies, Vector2 position){
            Vector2 a = new Vector2();
    
            for(int i = 0; i < virtualBodies.size; i++){
                VirtualBody vb = virtualBodies.get(i);
                BodyComponent bodyComp = vb.ref.getComponent(BodyComponent.class);
                GravityComponent vbGravity = vb.ref.getComponent(GravityComponent.class);
    
                if(vb == this) continue; // Prevent infinite forces
    
                float radiusSqr = position.dst2(vb.position);
                float soi = GravitySystem.getSphereOfInfluence(bodyComp);
    
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
    private ImmutableArray<Entity> entities;
    private ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);
	private ShapeRenderer shapeBatch = App.instance.shapeRenderer;

    private Array<VirtualBody> virtualBodies = new Array<>();

    private @Null Entity referenceEntity;
    private @Null TransformComponent referenceTransform;
    public boolean refreshTrails = false;
    public float startWidth = 0.1f;
    public float endWidth = 0.01f;

    // Constructor
    public TrackingSystem(){
        super(3);
    }

    // Functions
    @Override
    public void addedToEngine(Engine engine){
        entities = engine.getEntitiesFor(Family.all(TransformComponent.class, TrackedEntityComponent.class).get());
    }

    @Override
    public void update(final float deltaTime){
        // Initialize all virtual bodies
        @Null VirtualBody referenceVirtualBody = generateVirtualBodies();
        generateFuturePoints(referenceVirtualBody);
        
        // Start render
        Vector2 referencePoint = (referenceEntity == null) ? Vector2.Zero.cpy() : referenceTransform.position.cpy();
        Matrix4 renderMatrix = new Matrix4();
        renderMatrix.translate(referencePoint.x, referencePoint.y, 0.0f);

		shapeBatch.setProjectionMatrix(App.instance.camera.combined);
		shapeBatch.setTransformMatrix(renderMatrix);
        shapeBatch.setAutoShapeType(true);
		shapeBatch.begin(ShapeType.Filled);

        // Update every entity
        for(int i = 0; i < entities.size(); i++){
            // Get entity info
            Entity entity = entities.get(i);
            TransformComponent transform = tm.get(entity);
            TrackedEntityComponent trackedEntityComponent = entity.getComponent(TrackedEntityComponent.class);
            
            savePreviousPath(entity, trackedEntityComponent);
            shapeBatch.setColor(trackedEntityComponent.color);

            Array<Vector2> futurePoints = trackedEntityComponent.futurePoints;
            Array<Vector2> pastPoints = trackedEntityComponent.pastPoints;

            // Draw past path
            for(int k = 0; k < pastPoints.size - 1; k++){
                Vector2 point1 = pastPoints.get(k);
                Vector2 point2 = pastPoints.get(k + 1);
                float lineScale = (1.0f - (float)k / pastPoints.size);
                shapeBatch.rectLine(point1, point2, Interpolation.linear.apply(startWidth, endWidth, lineScale));
            }

            // Draw future path
            int count = 0;
            int direction = 1;
            int spacing = 10;

            for(int k = 0; k < futurePoints.size - 1 && trackedEntityComponent.predictFuture; k++){
                count += direction;
                if(count <= 0 || count >= spacing) direction *= -1;
                if(count < spacing / 2) continue;

                Vector2 point1 = futurePoints.get(k);
                Vector2 point2 = futurePoints.get(k + 1);
                float lineScale = ((float)k / futurePoints.size);
                shapeBatch.rectLine(point1, point2, Interpolation.linear.apply(startWidth, endWidth, lineScale));
            }
        }

        // Finish render
        shapeBatch.end();
        refreshTrails = false;
    }

    private @Null VirtualBody generateVirtualBodies(){
        @Null VirtualBody referenceVirtualBody = null;
        virtualBodies.clear();

        for(int i = 0; i < entities.size(); i++){
            Entity entity = entities.get(i);
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

        return referenceVirtualBody;
    }

    private void generateFuturePoints(@Null VirtualBody referenceVirtualBody){
        for(int i = 0; i < 600; i++){
            Vector2 referencePoint = (referenceVirtualBody == null) ? Vector2.Zero.cpy() : referenceVirtualBody.position.cpy();

            // Generate the points for every tracked entity
            for(VirtualBody vb : virtualBodies){
                if(vb.ref == null) continue; // Must have a real-world counterpart
                if(vb.trackedEntityComponent == null) continue; // Must have tracking component
                if(!vb.trackedEntityComponent.predictFuture) continue; // Must have tracking component
                
                Array<Vector2> points = vb.trackedEntityComponent.futurePoints;

                // Save memory by reusing points
                if((points.size - 1) < i){
                    points.add(vb.position.cpy().sub(referencePoint)); 
                } else {
                    points.get(i).set(vb.position).sub(referencePoint);
                }
            }

            // Predict future path of entities
            int substeps = 8;
            float dt = 0.5f;
            float sub_dt = dt / substeps;

            for(int steps = 0; steps < substeps; steps++){
                for(VirtualBody vb : virtualBodies){
                    vb.integrate(virtualBodies, sub_dt);
                }
            }
        }
    }

    private void savePreviousPath(Entity entity, TrackedEntityComponent trackedEntityComponent){
        // Focusing point
        TransformComponent transform = entity.getComponent(TransformComponent.class);
        Vector2 referencePoint = (referenceEntity == null) ? Vector2.Zero.cpy() : referenceTransform.position.cpy();

        // Refresh traisl if needed
        if(refreshTrails)
            trackedEntityComponent.pastPoints.clear();

        // Create new trail points as it moves
        if(trackedEntityComponent.pastPoints.size < trackedEntityComponent.pathTracingLength){
            // Size isnt filled, add like normal
            trackedEntityComponent.pastPoints.add(transform.position.cpy().sub(referencePoint));
        } else {
            // Size is filled, add and then remove the first
            trackedEntityComponent.pastPoints.removeIndex(0);
            trackedEntityComponent.pastPoints.add(transform.position.cpy().sub(referencePoint));
        }
    }

    public void setReferenceEntity(@Null Entity ref){
        referenceEntity = ref;
        referenceTransform = (ref == null) ? null : ref.getComponent(TransformComponent.class);
        refreshTrails = true;
    }

    public void setLineWidth(float start, float end){
        startWidth = start;
        endWidth = end;
    }
}
