package com.alicornlunaa.space_game.systems;

import com.alicornlunaa.selene_engine.ecs.BodyComponent;
import com.alicornlunaa.selene_engine.ecs.TransformComponent;
import com.alicornlunaa.space_game.App;
import com.alicornlunaa.space_game.components.ship.GridComponent;
import com.alicornlunaa.space_game.grid.Grid.GridIterator;
import com.alicornlunaa.space_game.grid.Grid.Layer;
import com.alicornlunaa.space_game.grid.tiles.AbstractTile;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;

public class GridBottomRenderSystem extends EntitySystem {
    // Variables
    private ImmutableArray<Entity> entities;
    private ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);
    private ComponentMapper<BodyComponent> bm = ComponentMapper.getFor(BodyComponent.class);
    private ComponentMapper<GridComponent> gm = ComponentMapper.getFor(GridComponent.class);
	private SpriteBatch batch = App.instance.spriteBatch;

    // Constructor
    public GridBottomRenderSystem(){
        super(2);
    }

    // Functions
    @Override
    public void addedToEngine(Engine engine){
        entities = engine.getEntitiesFor(Family.all(TransformComponent.class, BodyComponent.class, GridComponent.class).get());
    }

    @Override
    public void update(final float deltaTime){
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
            GridComponent gridComp = gm.get(entity);

            // Get matrix for the body
            renderMatrix.set(transform.getMatrix());
            renderMatrix.translate(-bodyComp.body.getLocalCenter().x, -bodyComp.body.getLocalCenter().y, 0);
            batch.setTransformMatrix(renderMatrix);
            batch.setColor(0.5f, 0.5f, 0.5f, 1);

            // Render the grid
            gridComp.grid.iterate(Layer.BOTTOM, new GridIterator() {
                @Override
                public void iterate(AbstractTile tile) {
                    tile.render(batch, deltaTime);
                }
            });
        }

        // Finish render
        batch.end();
    }
}
