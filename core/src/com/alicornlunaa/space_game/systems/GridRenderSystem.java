package com.alicornlunaa.space_game.systems;

import com.alicornlunaa.selene_engine.ecs.BodyComponent;
import com.alicornlunaa.selene_engine.ecs.TransformComponent;
import com.alicornlunaa.space_game.App;
import com.alicornlunaa.space_game.components.ship.GridComponent;
import com.alicornlunaa.space_game.grid.Grid.GridIterator;
import com.alicornlunaa.space_game.grid.tiles.AbstractTile;
import com.alicornlunaa.space_game.grid.tiles.TileEntity;
import com.alicornlunaa.space_game.util.Constants;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class GridRenderSystem extends EntitySystem {
    // Variables
    private ImmutableArray<Entity> entities;
    private ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);
    private ComponentMapper<BodyComponent> bm = ComponentMapper.getFor(BodyComponent.class);
    private ComponentMapper<GridComponent> gm = ComponentMapper.getFor(GridComponent.class);
	private SpriteBatch batch = App.instance.spriteBatch;

    // Constructor
    public GridRenderSystem(){
        super(3);
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

            // Get just clicked for tile entities
            boolean justLeftClicked = Gdx.input.isButtonJustPressed(Buttons.LEFT);
            boolean justRightClicked = Gdx.input.isButtonJustPressed(Buttons.RIGHT);
            boolean justMiddleClicked = Gdx.input.isButtonJustPressed(Buttons.MIDDLE);
            int buttonClicked = justLeftClicked ? 0 : (justRightClicked ? 1 : (justMiddleClicked ? 2 : -1));

            if(buttonClicked > -1){
                // Get tile touched
                Vector3 clickPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
                clickPos.set(App.instance.camera.unproject(clickPos));
                clickPos.mul(renderMatrix.cpy().inv());
                clickPos.set((int)(clickPos.x / Constants.TILE_SIZE - (clickPos.x < 0 ? 1 : 0)), (int)(clickPos.y / Constants.TILE_SIZE - (clickPos.y < 0 ? 1 : 0)), 0);

                AbstractTile tile = gridComp.grid.getTile((int)clickPos.x, (int)clickPos.y);

                if(tile != null && tile instanceof TileEntity)
                    ((TileEntity)tile).click(entity, App.instance.playerEntity, buttonClicked);
            }

            // Render the grid
            batch.setTransformMatrix(renderMatrix);

            gridComp.grid.iterate(new GridIterator() {
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
