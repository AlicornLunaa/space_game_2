package com.alicornlunaa.spacegame.systems;

import com.alicornlunaa.selene_engine.components_old.BodyComponent;
import com.alicornlunaa.selene_engine.components_old.TextureComponent;
import com.alicornlunaa.selene_engine.components_old.TransformComponent;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.selene_engine.ecs.ISystem;
import com.alicornlunaa.selene_engine.ecs.SpriteComponent;
import com.alicornlunaa.space_game.App;
import com.alicornlunaa.spacegame.components.CustomSpriteComponent;
import com.alicornlunaa.spacegame.components.PlanetComponent;
import com.alicornlunaa.spacegame.objects.simulation.Planet;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Null;

public class PlanetRenderSystem implements ISystem {
	// Variables
	private @Null Planet planet;
	private SpriteBatch batch;

	// Constructor
	public PlanetRenderSystem(){
		batch = new SpriteBatch();
	}
	
	// Functions
	public void setPlanet(@Null Planet planet){
		this.planet = planet;
	}

	@Override
	public void beforeUpdate() {}

	@Override
	public void afterUpdate() {}

	@Override
	public void update(IEntity entity) {}

	@Override
	public void beforeRender() {
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
		TextureComponent textureComponent = entity.getComponent(TextureComponent.class);
		SpriteComponent[] sprites = entity.getComponents(SpriteComponent.class);
		CustomSpriteComponent[] customSprites = entity.getComponents(CustomSpriteComponent.class);
		
		batch.setProjectionMatrix(App.instance.camera.combined);
		batch.setTransformMatrix(new Matrix4());

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
		for(SpriteComponent sprite : sprites){
			Vector2 offset = new Vector2(0, 0);
	
			switch(sprite.anchor){
				case CENTER:
					offset.set(sprite.size.cpy().scl(1.f / 2.f));
					break;
				default:
					break;
			}

			batch.draw(
				textureComponent.texture,
				offset.x * -1, offset.y * -1,
				offset.x, offset.y,
				sprite.size.x, sprite.size.y,
				1.f, 1.f,
				0
			);
		}

		for(CustomSpriteComponent sprite : customSprites)
			sprite.render(batch);
	}

	@Override
	public boolean shouldRunOnEntity(IEntity entity) {
		if(planet == null) return false;
		if(!planet.getComponent(PlanetComponent.class).isOnPlanet(entity)) return false;
		return entity.hasComponent(CustomSpriteComponent.class) || entity.hasComponent(SpriteComponent.class);
	}
}
