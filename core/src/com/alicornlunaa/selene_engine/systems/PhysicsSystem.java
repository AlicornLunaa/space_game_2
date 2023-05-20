package com.alicornlunaa.selene_engine.systems;

import java.util.HashMap;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.selene_engine.ecs.ISystem;
import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.badlogic.gdx.utils.Array;

public class PhysicsSystem implements ISystem {

    // Variables
    private Array<PhysWorld> physWorlds = new Array<>();
    private HashMap<IEntity, PhysWorld> containers = new HashMap<>();

	// Constructor
	public PhysicsSystem() {}

    // Physics functions
    public PhysWorld getWorld(int index){ return physWorlds.get(index); }
    public int getWorldID(PhysWorld world){ return physWorlds.indexOf(world, true); }

    public PhysWorld addWorld(float physScale){
        physWorlds.add(new PhysWorld(physScale));
        return physWorlds.peek();
    }

    public PhysWorld addWorld(PhysWorld world){
        physWorlds.add(world);
        return physWorlds.peek();
    }

    public void addEntityToWorld(int index, IEntity e){ addEntityToWorld(physWorlds.get(index), e); }

    public void addEntityToWorld(PhysWorld world, IEntity e){
        if(!e.hasComponent(BodyComponent.class)) return;
        if(e.getComponent(BodyComponent.class).world == world) return;

        if(containers.containsKey(e)){
            containers.get(e).getEntities().removeValue(e, true);
        }

        containers.put(e, world);
        world.getEntities().add(e);
        e.getComponent(BodyComponent.class).setWorld(world);
    }

	// Functions
    @Override
    public void beforeUpdate() {
        for(PhysWorld world : physWorlds){
            world.update();
        }
    }

    @Override
    public void afterUpdate() {}

    @Override
    public void update(IEntity entity) {
		TransformComponent transform = entity.getComponent(TransformComponent.class);
		BodyComponent rb = entity.getComponent(BodyComponent.class);

        transform.position.set(rb.body.getWorldCenter().cpy().scl(rb.world.getPhysScale()));
        transform.velocity.set(rb.body.getLinearVelocity());
        transform.rotation = rb.body.getAngle();
    }

    @Override
    public void beforeRender() {}

    @Override
    public void afterRender() {
        // for(PhysWorld world : physWorlds){
            // debugRenderer.render(world.getBox2DWorld(), game.activeCamera.combined.cpy().scl(world.getPhysScale()));
        // }
    }

    @Override
    public void render(IEntity entity) {}

    @Override
    public boolean shouldRunOnEntity(IEntity entity) {
        return entity.hasComponents(TransformComponent.class, BodyComponent.class);
    }
    
}
