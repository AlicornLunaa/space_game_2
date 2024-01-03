package com.alicornlunaa.selene_engine.ecs;

import com.alicornlunaa.space_game.App;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;

public class RenderSystem extends EntitySystem {
	// Variables
    private ImmutableArray<Entity> entities;
    private ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);
    private ComponentMapper<SpriteComponent> sm = ComponentMapper.getFor(SpriteComponent.class);
	private SpriteBatch batch = App.instance.spriteBatch;

	// Constructor
	public RenderSystem() {
		super(3);
	}

	// Functions
    @Override
    public void addedToEngine(Engine engine){
        entities = engine.getEntitiesFor(Family.all(TransformComponent.class, SpriteComponent.class).get());
    }

    @Override
    public void update(float deltaTime){
		batch.setProjectionMatrix(App.instance.camera.combined);
		batch.setTransformMatrix(new Matrix4());
		batch.begin();

        // Update every entity
        for(int i = 0; i < entities.size(); i++){
            // Get entity info
            Entity entity = entities.get(i);
            TransformComponent transform = tm.get(entity);
            SpriteComponent sprite = sm.get(entity);

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
			if(sprite.texture != null){
				batch.setColor(Color.WHITE);
				batch.draw(
					sprite.texture,
					transform.position.x - offsetX, transform.position.y - offsetY,
					offsetX, offsetY,
					sprite.size.x, sprite.size.y,
					1.f, 1.f,
					(float)Math.toDegrees(transform.rotation)
				);
			}
        }

		batch.end();
    }
}
