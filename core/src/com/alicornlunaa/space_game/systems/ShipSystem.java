package com.alicornlunaa.space_game.systems;

import com.alicornlunaa.space_game.App;
import com.alicornlunaa.space_game.components.ship.ShipComponent;
import com.alicornlunaa.space_game.util.ControlSchema;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;

public class ShipSystem extends EntitySystem {
    // Variables
    private ImmutableArray<Entity> entities;
    private ComponentMapper<ShipComponent> sm = ComponentMapper.getFor(ShipComponent.class);
	private SpriteBatch batch = App.instance.spriteBatch;

    // Constructor
    public ShipSystem(){
        super(3);
    }

    // Functions
    @Override
    public void addedToEngine(Engine engine){
        entities = engine.getEntitiesFor(Family.all(ShipComponent.class).get());
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
            ShipComponent shipComp = sm.get(entity);

            // Get matrix for the body
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
        }

        // Finish render
        batch.end();
    }
}
