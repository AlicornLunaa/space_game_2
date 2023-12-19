package com.alicornlunaa.selene_engine.ecs;

import com.alicornlunaa.selene_engine.phys.Collider;
import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.alicornlunaa.space_game.util.Constants;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;

public class PhysicsSystem extends EntitySystem {
    // Static classes
    private static class PhysicsEntityListener implements EntityListener {
        // Variables
        private ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);
        private ComponentMapper<BodyComponent> bm = ComponentMapper.getFor(BodyComponent.class);
        private PhysicsSystem system;

        // Constructor
        public PhysicsEntityListener(PhysicsSystem system){
            this.system = system;
        }

        // Functions
        @Override
        public void entityAdded(Entity entity) {
            TransformComponent transform = tm.get(entity);
            BodyComponent bodyComp = bm.get(entity);

            bodyComp.bodyDef.position.set(transform.position);
            bodyComp.bodyDef.angle = transform.rotation;
            bodyComp.body = system.world.getBox2DWorld().createBody(bodyComp.bodyDef);
            bodyComp.world = system.world;

            transform.dp.set(transform.position);
            transform.dr = transform.rotation;

            for(Collider collider : bodyComp.colliders){
                collider.attach(bodyComp.body);
            }
        }

        @Override
        public void entityRemoved(Entity entity) {
            // Remove the body from the world
            BodyComponent bodyComp = bm.get(entity);

            for(Collider collider : bodyComp.colliders){
                collider.detach();
            }
            
            system.world.getBox2DWorld().destroyBody(bodyComp.body);
            bodyComp.body = null;
            bodyComp.world = null;
        }
    }

    // Variables
    private ImmutableArray<Entity> entities;
    private ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);
    private ComponentMapper<BodyComponent> bm = ComponentMapper.getFor(BodyComponent.class);

    private PhysWorld world;
    private float accumulator = 0.f;

    // Constructor
    public PhysicsSystem(float physScale){
        super(0);
        world = new PhysWorld(physScale);
    }

    // Functions
    public PhysWorld getWorld(){
        return world;
    }

    @Override
    public void addedToEngine(Engine engine){
        Family entityFamily = Family.all(TransformComponent.class, BodyComponent.class).get();
        entities = engine.getEntitiesFor(entityFamily);
        engine.addEntityListener(entityFamily, new PhysicsEntityListener(this));
    }

    @Override
    public void update(float deltaTime){
        // Fixed timestep for world
        accumulator += Math.min(deltaTime, 0.25f);
        while(accumulator >= Constants.TIME_STEP){
            accumulator -= Constants.TIME_STEP;
            world.update();
        }

        // Update every entity
        for(int i = 0; i < entities.size(); i++){
            // Get entity info
            Entity entity = entities.get(i);
            TransformComponent transform = tm.get(entity);
            BodyComponent rb = bm.get(entity);

            // Error guards
            if(rb.body == null) continue;
            if(rb.world == null) continue;

            // Update entity
            transform.dp.set(transform.position.cpy().sub(transform.dp));
            transform.dr = transform.rotation - transform.dr;

            if(transform.dp.len() > 0.0f || transform.dr != 0.0f){
                rb.body.setTransform(rb.body.getPosition().cpy().add(transform.dp), rb.body.getAngle() + transform.dr);
            }

            transform.position.set(rb.body.getWorldCenter());
            transform.rotation = rb.body.getAngle();

            transform.dp.set(transform.position);
            transform.dr = transform.rotation;
        }
    }
}
