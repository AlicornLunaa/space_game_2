package com.alicornlunaa.spacegame.systems;

import com.alicornlunaa.selene_engine.components_old.BodyComponent;
import com.alicornlunaa.selene_engine.components_old.TransformComponent;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.selene_engine.ecs.ISystem;
import com.alicornlunaa.space_game.App;
import com.alicornlunaa.spacegame.components.CustomSpriteComponent;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;

public class SpaceRenderSystem implements ISystem {

	// Variables
	private boolean active = true;
	private SpriteBatch batch;

	// Constructor
	public SpaceRenderSystem(){
		batch = new SpriteBatch();
	}
	
	// Functions
	public void setActive(boolean a){
		this.active = a;
	}

	@Override
	public void beforeUpdate() {}

	@Override
	public void afterUpdate() {}

	@Override
	public void update(IEntity entity) {}

	@Override
	public void beforeRender() {
		batch.setProjectionMatrix(App.instance.camera.combined);
		batch.setTransformMatrix(new Matrix4());
		batch.begin();
	}

	@Override
	public void afterRender() {
		batch.end();
	}

	@Override
	public void render(IEntity entity) {
		// Skip if not active
		if(!active) return;
		batch.setProjectionMatrix(App.instance.camera.combined);
		batch.setTransformMatrix(new Matrix4());

		// Get components
		TransformComponent transform = entity.getComponent(TransformComponent.class);
		BodyComponent bodyComponent = entity.getComponent(BodyComponent.class);
		CustomSpriteComponent[] sprites = entity.getComponents(CustomSpriteComponent.class);

		// Get global position
		Matrix4 trans = new Matrix4();

		trans.translate(transform.position.x, transform.position.y, 0.0f);
		trans.rotateRad(0, 0, 1, transform.rotation);

		if(bodyComponent != null){
			Vector2 localCenter = bodyComponent.body.getLocalCenter();
			trans.translate(-localCenter.x, -localCenter.y, 0);
		}
		
		batch.setTransformMatrix(trans);

		// Render every sprite
		for(CustomSpriteComponent sprite : sprites)
			sprite.render(batch);
	}

	@Override
	public boolean shouldRunOnEntity(IEntity entity) {
		return entity.hasComponents(CustomSpriteComponent.class, TransformComponent.class) && active;
	}
	
}
