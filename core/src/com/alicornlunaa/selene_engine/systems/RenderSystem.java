package com.alicornlunaa.selene_engine.systems;

import com.alicornlunaa.selene_engine.components.SpriteComponent;
import com.alicornlunaa.selene_engine.components.TextureComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.selene_engine.ecs.ISystem;
import com.alicornlunaa.spacegame.App;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;

public class RenderSystem implements ISystem, Disposable {
	// Variables
	private final App game;
	private SpriteBatch batch;

	// Constructor
	public RenderSystem(App game) {
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
		TransformComponent transform = entity.getComponent(TransformComponent.class);
		TextureComponent img = entity.getComponent(TextureComponent.class);
		SpriteComponent sprite = entity.getComponent(SpriteComponent.class);

		// Calculate render offset based on anchor point and size
		float offsetX = 0;
		float offsetY = 0;

		switch(sprite.anchor){
		case CENTER:
			offsetX = sprite.size.x / 2.f;
			offsetY = sprite.size.y / 2.f;
			break;
		default:
			break;
		}

		// Draw entity
		batch.draw(
			img.texture,
			transform.position.x - offsetX, transform.position.y - offsetY,
			offsetX, offsetY,
			sprite.size.x, sprite.size.y,
			1.f, 1.f,
			(float)Math.toDegrees(transform.rotation)
		);
	}

	@Override
	public boolean shouldRunOnEntity(IEntity entity) {
		return entity.hasComponents(TransformComponent.class, TextureComponent.class, SpriteComponent.class);
	}

	@Override
	public void dispose() {
		batch.dispose();
	}
}
