package com.alicornlunaa.selene_engine.systems;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.TextureComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.selene_engine.ecs.ISystem;
import com.alicornlunaa.spacegame.App;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ScreenUtils;

public class RenderSystem implements ISystem, Disposable {

	// Variables
	private final App game;
	private SpriteBatch batch;
	private float PPM = 256.f;

	// Constructor
	public RenderSystem(App game) {
		this.game = game;
		batch = new SpriteBatch();
	}

	// Functions
	@Override
	public void beforeUpdate() {
	}

	@Override
	public void afterUpdate() {
	}

	@Override
	public void update(IEntity entity) {
	}

	@Override
	public void beforeRender() {
		ScreenUtils.clear(0, 0, 0, 1);
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
		Texture img = entity.getComponent(TextureComponent.class).texture;

		if(entity.hasComponent(BodyComponent.class)){
			BodyComponent bodyComponent = entity.getComponent(BodyComponent.class);
			batch.draw(img, bodyComponent.body.getPosition().x * PPM, bodyComponent.body.getPosition().y * PPM);
		} else if(entity.hasComponent(TransformComponent.class)){
			TransformComponent transform = entity.getComponent(TransformComponent.class);
			batch.draw(img, transform.position.x * PPM, transform.position.y * PPM);
		}
	}

	@Override
	public boolean shouldRunOnEntity(IEntity entity) {
		return ((entity.hasComponent(TransformComponent.class) || entity.hasComponent(BodyComponent.class)) && entity.hasComponent(TextureComponent.class));
	}

	@Override
	public void dispose() {
		batch.dispose();
	}

}
