package com.alicornlunaa.space_game.systems;

import com.alicornlunaa.selene_engine.ecs.TransformComponent;
import com.alicornlunaa.space_game.App;
import com.alicornlunaa.space_game.components.celestial.CelestialComponent;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;

public class CelestialRenderSystem extends EntitySystem {
    // Variables
    private ImmutableArray<Entity> entities;
    private ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);
    private ComponentMapper<CelestialComponent> cm = ComponentMapper.getFor(CelestialComponent.class);
	private ShapeRenderer shapeBatch = App.instance.shapeRenderer;

    // Constructor
    public CelestialRenderSystem(){
        super(3);
    }

    // Functions
    @Override
    public void addedToEngine(Engine engine){
        entities = engine.getEntitiesFor(Family.all(TransformComponent.class, CelestialComponent.class).get());
    }

    @Override
    public void update(final float deltaTime){
        // Start render
        Matrix4 renderMatrix = new Matrix4();

		shapeBatch.setProjectionMatrix(App.instance.camera.combined);
		shapeBatch.setTransformMatrix(renderMatrix);
        shapeBatch.setAutoShapeType(true);
        shapeBatch.setColor(Color.WHITE);
		shapeBatch.begin();

        // Update every entity
        for(int i = 0; i < entities.size(); i++){
            // Get entity info
            Entity entity = entities.get(i);
            TransformComponent transform = tm.get(entity);
            CelestialComponent celestialComp = cm.get(entity);

            // Get matrix for the body
            renderMatrix.set(transform.getMatrix());

            // Render the grid
            shapeBatch.setTransformMatrix(renderMatrix);
            shapeBatch.circle(0, 0, 1, 30);
        }

        // Finish render
        shapeBatch.end();
    }
}
