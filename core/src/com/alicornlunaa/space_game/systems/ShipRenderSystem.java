package com.alicornlunaa.space_game.systems;

import com.alicornlunaa.selene_engine.ecs.BodyComponent;
import com.alicornlunaa.selene_engine.ecs.CameraComponent;
import com.alicornlunaa.selene_engine.ecs.TransformComponent;
import com.alicornlunaa.space_game.App;
import com.alicornlunaa.space_game.components.player.PlayerComponent;
import com.alicornlunaa.space_game.components.ship.ShipComponent;
import com.alicornlunaa.space_game.util.ControlSchema;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class ShipRenderSystem extends EntitySystem {
    // Variables
    private ImmutableArray<Entity> entities;
    private ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);
    private ComponentMapper<BodyComponent> bm = ComponentMapper.getFor(BodyComponent.class);
    private ComponentMapper<ShipComponent> sm = ComponentMapper.getFor(ShipComponent.class);
    private ComponentMapper<PlayerComponent> pm = ComponentMapper.getFor(PlayerComponent.class);
    private ComponentMapper<CameraComponent> cm = ComponentMapper.getFor(CameraComponent.class);
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
        batch.setColor(Color.WHITE);
		batch.begin();

        // Update every entity
        for(int i = 0; i < entities.size(); i++){
            // Get entity info
            Entity entity = entities.get(i);
            TransformComponent transform = tm.get(entity);
            BodyComponent bodyComp = bm.get(entity);
            ShipComponent shipComp = sm.get(entity);
            
            // Check for ship input such as clicking on the ship
            if(Gdx.input.isButtonJustPressed(Buttons.LEFT)){
                Vector3 clickPosInWorld = App.instance.camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
                Vector2 position = new Vector2(clickPosInWorld.x, clickPosInWorld.y).sub(transform.position).add(bodyComp.body.getLocalCenter());
                
                if(shipComp.rootPart.contains(position)){
                    // Drive the ship
                    PlayerComponent playerComp = pm.get(App.instance.playerEntity);
                    CameraComponent cameraComp = cm.get(App.instance.playerEntity);
                    shipComp.controlEnabled = !shipComp.controlEnabled;
                    playerComp.enabled = !shipComp.controlEnabled;
                    cameraComp.active = !shipComp.controlEnabled;

                    if(!shipComp.controlEnabled){
                        entity.remove(CameraComponent.class);
                    } else {
                        entity.add(new CameraComponent(1280, 720));
                    }
                }
            }

            if(shipComp.controlEnabled){
                if(Gdx.input.isKeyPressed(ControlSchema.SHIP_TRANSLATE_UP)) shipComp.vertical = 1;
                else if(Gdx.input.isKeyPressed(ControlSchema.SHIP_TRANSLATE_DOWN)) shipComp.vertical = -1;
                else shipComp.vertical = 0;
                
                if(Gdx.input.isKeyPressed(ControlSchema.SHIP_TRANSLATE_LEFT)) shipComp.horizontal = -1;
                else if(Gdx.input.isKeyPressed(ControlSchema.SHIP_TRANSLATE_RIGHT)) shipComp.horizontal = 1;
                else shipComp.horizontal = 0;
                
                if(Gdx.input.isKeyPressed(ControlSchema.SHIP_ROLL_LEFT)) shipComp.roll = 1;
                else if(Gdx.input.isKeyPressed(ControlSchema.SHIP_ROLL_RIGHT)) shipComp.roll = -1;
                else shipComp.roll = 0;
                
                if(Gdx.input.isKeyPressed(ControlSchema.SHIP_INCREASE_THROTTLE)) shipComp.throttle = Math.min(shipComp.throttle + 1, 100);
                else if(Gdx.input.isKeyPressed(ControlSchema.SHIP_DECREASE_THROTTLE)) shipComp.throttle = Math.max(shipComp.throttle - 1, 0);
                else if(Gdx.input.isKeyPressed(ControlSchema.SHIP_FULL_THROTTLE)) shipComp.throttle = 100;
                else if(Gdx.input.isKeyPressed(ControlSchema.SHIP_NO_THROTTLE)) shipComp.throttle = 0;

                if(Gdx.input.isKeyJustPressed(ControlSchema.SHIP_TOGGLE_RCS)) shipComp.rcs = !shipComp.rcs;
                if(Gdx.input.isKeyJustPressed(ControlSchema.SHIP_TOGGLE_SAS)) shipComp.sas = !shipComp.sas;
            }

            // Render the root part
            renderMatrix.set(transform.getMatrix());
            renderMatrix.translate(-bodyComp.body.getLocalCenter().x, -bodyComp.body.getLocalCenter().y, 0);
            shipComp.rootPart.draw(batch, renderMatrix);
        }

        // Finish render
        batch.end();
    }
}
