package com.alicornlunaa.selene_engine.systems;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.selene_engine.ecs.ISystem;
import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.badlogic.gdx.utils.Array;

public class PhysicsSystem implements ISystem {

    // Variables
    private Array<PhysWorld> physWorlds = new Array<>();

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

    @Override
    public void beforeRender() {}

    @Override
    public void afterRender() {
        // for(PhysWorld world : physWorlds){
            // debugRenderer.render(world.getBox2DWorld(), game.gameScene.activeCamera.combined.cpy().scl(world.getPhysScale()));
        // }
    }

    @Override
    public void render(IEntity entity) {}

    @Override
    public boolean shouldRunOnEntity(IEntity entity) {
        return entity.hasComponents(TransformComponent.class, BodyComponent.class);
    }
    
}
