package com.alicornlunaa.spacegame.panels;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Ground;
import com.alicornlunaa.spacegame.objects.Ship;
import com.alicornlunaa.spacegame.util.Constants;
import com.alicornlunaa.spacegame.util.ControlSchema;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class GamePanel extends Stage {

    // Variables
    final App game;

    private World world;
    private float physAccumulator = 0.0f;

    public Ship ship;
    
    private Box2DDebugRenderer debug = new Box2DDebugRenderer();

    // Constructor
    public GamePanel(final App game){
        super(new ScreenViewport());
        this.game = game;

        world = new World(new Vector2(), true);

        ship = new Ship(game.manager, game.partManager, world, 640/2, 250, 0);
        ship.load("./saves/ships/null.ship");
		this.addActor(ship);
		this.addActor(new Ground(game, world, getWidth() / 2, 30, getWidth() - 30, 25));

        // Controls
        this.addListener(new InputListener(){
            @Override
            public boolean keyDown(InputEvent event, int keycode){
                if(keycode == ControlSchema.SHIP_TOGGLE_RCS){
                    ship.state.rcs = !ship.state.rcs;
                    return true;
                } else if(keycode == ControlSchema.SHIP_TOGGLE_SAS){
                    ship.state.sas = !ship.state.sas;
                    return true;
                }

                return false;
            }
        });
    }

    // Functions
    public World getWorld(){ return world; }

    @Override
    public void act(float delta){
        super.act(delta);

        // Physics updates
        physAccumulator += Math.min(delta, 0.25f);;
        while(physAccumulator >= Constants.TIME_STEP){
            world.step(Constants.TIME_STEP, Constants.VELOCITY_ITERATIONS, Constants.POSITION_ITERATIONS);
            physAccumulator -= Constants.TIME_STEP;
        }

        // Controls for the ship
        if(Gdx.input.isKeyPressed(ControlSchema.SHIP_INCREASE_THROTTLE)){
            ship.state.throttle = Math.min(ship.state.throttle + 0.01f, 1);
        } else if(Gdx.input.isKeyPressed(ControlSchema.SHIP_DECREASE_THROTTLE)){
            ship.state.throttle = Math.max(ship.state.throttle - 0.01f, 0);
        }
        
        if(Gdx.input.isKeyPressed(ControlSchema.SHIP_ROLL_LEFT)){
            ship.state.roll = -1;
        } else if(Gdx.input.isKeyPressed(ControlSchema.SHIP_ROLL_RIGHT)){
            ship.state.roll = 1;
        } else {
            ship.state.roll = 0;
        }
        
        if(Gdx.input.isKeyPressed(ControlSchema.SHIP_TRANSLATE_UP)){
            ship.state.vertical = 1;
        } else if(Gdx.input.isKeyPressed(ControlSchema.SHIP_TRANSLATE_DOWN)){
            ship.state.vertical = -1;
        } else {
            ship.state.vertical = 0;
        }
        
        if(Gdx.input.isKeyPressed(ControlSchema.SHIP_TRANSLATE_LEFT)){
            ship.state.horizontal = -1;
        } else if(Gdx.input.isKeyPressed(ControlSchema.SHIP_TRANSLATE_RIGHT)){
            ship.state.horizontal = 1;
        } else {
            ship.state.horizontal = 0;
        }
    }

    @Override
    public void draw(){
        super.draw();

        if(this.isDebugAll()){
            debug.render(world, this.getCamera().combined);
        }
    }
    
    @Override
    public void dispose(){
        super.dispose();
    }

}
