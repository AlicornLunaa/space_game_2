package com.alicornlunaa.spacegame.systems;

import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.selene_engine.ecs.ISystem;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.components.CustomSpriteComponent;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;

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
		batch.setProjectionMatrix(game.activeCamera.combined);
		batch.setTransformMatrix(new Matrix4());
		batch.begin();
	}

	@Override
	public void afterRender() {
		batch.end();
	}

	@Override
	public void render(IEntity entity) {
		CustomSpriteComponent[] sprites = entity.getComponents(CustomSpriteComponent.class);

		for(CustomSpriteComponent sprite : sprites)
			sprite.render(batch);
	}

	@Override
	public boolean shouldRunOnEntity(IEntity entity) {
		return entity.hasComponent(CustomSpriteComponent.class);
	}
	
}
