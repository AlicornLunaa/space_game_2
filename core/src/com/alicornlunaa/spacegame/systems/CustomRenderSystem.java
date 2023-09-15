package com.alicornlunaa.spacegame.systems;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.selene_engine.ecs.ISystem;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.components.CustomSpriteComponent;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;

public class CustomRenderSystem implements ISystem {

	// Variables
	private final App game;
	private SpriteBatch batch;

	// Constructor
	public CustomRenderSystem(App game){
		this.game = game;
		batch = new SpriteBatch();
	}
	
	// Functions
	@Override
	public void beforeUpdate() {}

	@Override
	public void afterUpdate() {}

	@Override
	public void update(IEntity entity) {}

	@Override
	public void beforeRender() {
		batch.setProjectionMatrix(game.camera.combined);
		batch.setTransformMatrix(new Matrix4());
		batch.begin();
	}

	@Override
	public void afterRender() {
		batch.end();
	}

	@Override
	public void render(IEntity entity) {
		// Get components
		TransformComponent transform = entity.getComponent(TransformComponent.class);
		BodyComponent bodyComponent = entity.getComponent(BodyComponent.class);
		CustomSpriteComponent[] sprites = entity.getComponents(CustomSpriteComponent.class);

		// Get global position
		Matrix4 trans = new Matrix4();
		trans.translate(transform.position.x, transform.position.y, 0.0f);

		if(bodyComponent != null){
			Vector2 localCenter = bodyComponent.body.getLocalCenter().cpy().scl(bodyComponent.world.getPhysScale());
			trans.translate(-localCenter.x, -localCenter.y, 0);
		}

		trans.rotateRad(0, 0, 1, transform.rotation);
		batch.setTransformMatrix(trans);

		// Render every sprite
		for(CustomSpriteComponent sprite : sprites)
			sprite.render(batch);
	}

	@Override
	public boolean shouldRunOnEntity(IEntity entity) {
		return entity.hasComponent(CustomSpriteComponent.class);
	}
	
}
