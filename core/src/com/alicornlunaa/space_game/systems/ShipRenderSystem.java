package com.alicornlunaa.space_game.systems;

import com.alicornlunaa.selene_engine.ecs.BodyComponent;
import com.alicornlunaa.selene_engine.ecs.TransformComponent;
import com.alicornlunaa.space_game.App;
import com.alicornlunaa.space_game.components.ship.ShipComponent;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;

public class ShipRenderSystem extends EntitySystem {
    // Variables
    private ImmutableArray<Entity> entities;
    private ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);
    private ComponentMapper<BodyComponent> bm = ComponentMapper.getFor(BodyComponent.class);
    private ComponentMapper<ShipComponent> sm = ComponentMapper.getFor(ShipComponent.class);
	private SpriteBatch batch = App.instance.spriteBatch;

    // Constructor
    public ShipRenderSystem(){
        super(3);
    }

    // Functions
    @Override
    public void addedToEngine(Engine engine){
        entities = engine.getEntitiesFor(Family.all(TransformComponent.class, BodyComponent.class, ShipComponent.class).get());
    }

    @Override
    public void update(float deltaTime){
        // Start render
        Matrix4 renderMatrix = new Matrix4();

		batch.setProjectionMatrix(App.instance.camera.combined);
		batch.setTransformMatrix(renderMatrix);
		batch.begin();

        // Update every entity
        for(int i = 0; i < entities.size(); i++){
            // Get entity info
            Entity entity = entities.get(i);
            TransformComponent transform = tm.get(entity);
            BodyComponent bodyComp = bm.get(entity);
            ShipComponent shipComp = sm.get(entity);

            // Render the root part
            renderMatrix.set(transform.getMatrix());
            renderMatrix.translate(-bodyComp.body.getLocalCenter().x, -bodyComp.body.getLocalCenter().y, 0);
            shipComp.rootPart.draw(batch, renderMatrix);
        }

        // Finish render
        batch.end();
    }
}
