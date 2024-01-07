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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class GridRenderSystem extends EntitySystem {
    // Variables
    private ImmutableArray<Entity> entities;
    private ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);
    private ComponentMapper<BodyComponent> bm = ComponentMapper.getFor(BodyComponent.class);
    private ComponentMapper<GridComponent> gm = ComponentMapper.getFor(GridComponent.class);
	private SpriteBatch batch = App.instance.spriteBatch;

    private TextureRegion texture;

    // Constructor
    public GridRenderSystem(){
        super(3);

        Pixmap data = new Pixmap(1, 1, Format.RGBA8888);
        data.setColor(Color.WHITE);
        data.fill();
        texture = new TextureRegion(new Texture(data));
        data.dispose();
    }

    // Functions
    @Override
    public void addedToEngine(Engine engine){
        entities = engine.getEntitiesFor(Family.all(TransformComponent.class, BodyComponent.class, GridComponent.class).get());
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
            GridComponent gridComp = gm.get(entity);

            // Get matrix for the body
            renderMatrix.set(transform.getMatrix());
            renderMatrix.translate(-bodyComp.body.getLocalCenter().x, -bodyComp.body.getLocalCenter().y, 0);
            
            // Check for ship input such as clicking on the ship
            // if(Gdx.input.isButtonJustPressed(Buttons.LEFT)){
            //     Vector3 clickPosInWorld = App.instance.camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
            //     Vector2 position = new Vector2(clickPosInWorld.x, clickPosInWorld.y).sub(transform.position).add(bodyComp.body.getLocalCenter());
                
            //     if(shipComp.rootPart.contains(position)){
            //         // Drive the ship
            //         PlayerComponent playerComp = pm.get(App.instance.playerEntity);
            //         CameraComponent cameraComp = cm.get(App.instance.playerEntity);
            //         shipComp.controlEnabled = !shipComp.controlEnabled;
            //         playerComp.enabled = !shipComp.controlEnabled;
            //         cameraComp.active = !shipComp.controlEnabled;

            //         if(!shipComp.controlEnabled){
            //             entity.remove(CameraComponent.class);
            //         } else {
            //             entity.add(new CameraComponent(1280, 720));
            //         }
            //     }
            // }

            // if(shipComp.controlEnabled){
            //     if(Gdx.input.isKeyPressed(ControlSchema.SHIP_TRANSLATE_UP)) shipComp.vertical = 1;
            //     else if(Gdx.input.isKeyPressed(ControlSchema.SHIP_TRANSLATE_DOWN)) shipComp.vertical = -1;
            //     else shipComp.vertical = 0;
                
            //     if(Gdx.input.isKeyPressed(ControlSchema.SHIP_TRANSLATE_LEFT)) shipComp.horizontal = -1;
            //     else if(Gdx.input.isKeyPressed(ControlSchema.SHIP_TRANSLATE_RIGHT)) shipComp.horizontal = 1;
            //     else shipComp.horizontal = 0;
                
            //     if(Gdx.input.isKeyPressed(ControlSchema.SHIP_ROLL_LEFT)) shipComp.roll = 1;
            //     else if(Gdx.input.isKeyPressed(ControlSchema.SHIP_ROLL_RIGHT)) shipComp.roll = -1;
            //     else shipComp.roll = 0;
                
            //     if(Gdx.input.isKeyPressed(ControlSchema.SHIP_INCREASE_THROTTLE)) shipComp.throttle = Math.min(shipComp.throttle + 1, 100);
            //     else if(Gdx.input.isKeyPressed(ControlSchema.SHIP_DECREASE_THROTTLE)) shipComp.throttle = Math.max(shipComp.throttle - 1, 0);
            //     else if(Gdx.input.isKeyPressed(ControlSchema.SHIP_FULL_THROTTLE)) shipComp.throttle = 100;
            //     else if(Gdx.input.isKeyPressed(ControlSchema.SHIP_NO_THROTTLE)) shipComp.throttle = 0;

            //     if(Gdx.input.isKeyJustPressed(ControlSchema.SHIP_TOGGLE_RCS)) shipComp.rcs = !shipComp.rcs;
            //     if(Gdx.input.isKeyJustPressed(ControlSchema.SHIP_TOGGLE_SAS)) shipComp.sas = !shipComp.sas;
            // }

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
                    ((TileEntity)tile).click(buttonClicked);
            }

            // Render the grid
            batch.setTransformMatrix(renderMatrix);

            gridComp.grid.iterate(new GridIterator() {
                @Override
                public void iterate(AbstractTile tile) {
                    tile.render(batch, Gdx.graphics.getDeltaTime());
                }
            });
        }

        // Finish render
        batch.end();
    }
}
