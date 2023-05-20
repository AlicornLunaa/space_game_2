package com.alicornlunaa.selene_engine.systems;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.selene_engine.ecs.ISystem;
import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.alicornlunaa.spacegame.App;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;

public class PhysicsSystem implements ISystem {

    // Variables
    private final App game;
    private PhysWorld world;
    private Box2DDebugRenderer debugRenderer;

	// Constructor
	public PhysicsSystem(App game, PhysWorld world) {
        this.game = game;
        this.world = world;
        debugRenderer = new Box2DDebugRenderer();
	}

	// Functions
    @Override
    public void beforeUpdate() {
        world.update();
    }

    @Override
    public void afterUpdate() {}

    @Override
    public void update(IEntity entity) {
		TransformComponent transform = entity.getComponent(TransformComponent.class);
		BodyComponent rb = entity.getComponent(BodyComponent.class);

        transform.position.set(rb.body.getWorldCenter().cpy().scl(world.getPhysScale()));
        transform.velocity.set(rb.body.getLinearVelocity());
        transform.rotation = rb.body.getAngle();
    }

    @Override
    public void beforeRender() {}

    @Override
    public void afterRender() {
        debugRenderer.render(world.getBox2DWorld(), game.activeCamera.combined.cpy().scl(world.getPhysScale()));
    }

    @Override
    public void render(IEntity entity) {}

    @Override
    public boolean shouldRunOnEntity(IEntity entity) {
        return (entity.hasComponents(TransformComponent.class, BodyComponent.class));
    }
    
}
