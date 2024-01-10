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
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;

public class GridPhysicsSystem extends EntitySystem {
    // Static classes
    private static class GridPhysicsListener implements EntityListener {
        // Variables
        private ComponentMapper<GridComponent> tm = ComponentMapper.getFor(GridComponent.class);
        private ComponentMapper<BodyComponent> gm = ComponentMapper.getFor(BodyComponent.class);

        // Constructor
        public GridPhysicsListener(){}

        // Functions
        @Override
        public void entityAdded(Entity entity) {
            GridComponent gridComp = tm.get(entity);
            BodyComponent bodyComp = gm.get(entity);
            gridComp.grid.assemble(bodyComp);
        }

        @Override
        public void entityRemoved(Entity entity) {
            GridComponent gridComp = tm.get(entity);
            gridComp.grid.disassemble();
        }
    }

    // Variables
    private ImmutableArray<Entity> entities;
    private ComponentMapper<GridComponent> gm = ComponentMapper.getFor(GridComponent.class);
	private SpriteBatch batch = App.instance.spriteBatch;

    // Constructor
    public GridPhysicsSystem(){
        super(1);
    }

    // Functions
    @Override
    public void addedToEngine(Engine engine){
        entities = engine.getEntitiesFor(Family.all(TransformComponent.class, BodyComponent.class, GridComponent.class).get());
        engine.addEntityListener(Family.all(GridComponent.class, BodyComponent.class).get(), new GridPhysicsListener());
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
            final Entity entity = entities.get(i);
            GridComponent gridComp = gm.get(entity);

            gridComp.grid.iterate(Layer.MIDDLE, new GridIterator() {
                @Override
                public void iterate(AbstractTile tile) {
                    tile.update(entity, deltaTime);
                }
            });
        }

        // Finish render
        batch.end();
    }
}
