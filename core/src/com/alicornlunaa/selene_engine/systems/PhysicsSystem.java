package com.alicornlunaa.selene_engine.systems;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.selene_engine.ecs.ISystem;
import com.alicornlunaa.spacegame.App;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;

public class PhysicsSystem implements ISystem {

    // Variables
    private final App game;
    private World world;
    private Box2DDebugRenderer debugRenderer;

	// Constructor
	public PhysicsSystem(App game, World world) {
        this.game = game;
        this.world = world;
        debugRenderer = new Box2DDebugRenderer();
	}

	// Functions
    @Override
    public void beforeUpdate() {
        world.step(1.0f / 60.0f, 6, 2);
    }

    @Override
    public void afterUpdate() {}

    @Override
    public void update(IEntity entity) {
		TransformComponent transform = entity.getComponent(TransformComponent.class);
		BodyComponent rb = entity.getComponent(BodyComponent.class);

        transform.position.set(rb.body.getWorldCenter());
        transform.velocity.set(rb.body.getLinearVelocity());
        transform.rotation = rb.body.getAngle();
    }

    @Override
    public void beforeRender() {}

    @Override
    public void afterRender() {
        debugRenderer.render(world, game.activeCamera.combined.cpy().scl(256));
    }

    @Override
    public void render(IEntity entity) {}

    @Override
    public boolean shouldRunOnEntity(IEntity entity) {
        return (entity.hasComponents(TransformComponent.class, BodyComponent.class));
    }
    
}
