package com.alicornlunaa.selene_engine.ecs;

import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.utils.Array;

public class PhysicsSystem extends EntitySystem {
    // Variables
    private ImmutableArray<Entity> entities;
    private ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);
    private ComponentMapper<BodyComponent> bm = ComponentMapper.getFor(BodyComponent.class);
    private Array<PhysWorld> physWorlds = new Array<>();

    // Constructor
    public PhysicsSystem(){}

    // Functions
    public PhysWorld addWorld(PhysWorld world){
        physWorlds.add(world);
        return physWorlds.peek();
    }

    @Override
    public void addedToEngine(Engine engine){
        entities = engine.getEntitiesFor(Family.all(TransformComponent.class, BodyComponent.class).get());
    }

    @Override
    public void update(float deltaTime){
        // Update every physics world
        for(PhysWorld world : physWorlds){
            world.update();
        }

        // Update every entity
        for(int i = 0; i < entities.size(); i++){
            // Get entity info
            Entity entity = entities.get(i);
            TransformComponent transform = tm.get(entity);
            BodyComponent rb = bm.get(entity);

            // Update entity
            transform.dp.set(transform.position.cpy().sub(transform.dp));
            transform.dr = transform.rotation - transform.dr;

            if(transform.dp.len() > 0.0f || transform.dr != 0.0f){
                rb.body.setTransform(rb.body.getPosition().cpy().add(transform.dp), rb.body.getAngle() + transform.dr);
            }

            transform.position.set(rb.body.getWorldCenter()).add(rb.world.getOffset());
            transform.rotation = rb.body.getAngle();

            transform.dp.set(transform.position);
            transform.dr = transform.rotation;
        }
    }
}
