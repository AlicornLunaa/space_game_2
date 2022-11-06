package com.alicornlunaa.spacegame.panels;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Ship;
import com.alicornlunaa.spacegame.objects.Planet.SpacePlanet;
import com.alicornlunaa.spacegame.states.PlanetState;
import com.alicornlunaa.spacegame.util.Constants;
import com.alicornlunaa.spacegame.util.ControlSchema;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FillViewport;

public class GamePanel extends Stage {

    // Variables
    final App game;

    private World world;
    private float physAccumulator = 0.0f;

    public Ship ship;
    public SpacePlanet planet;
    
    private Box2DDebugRenderer debug = new Box2DDebugRenderer();

    // Constructor
    public GamePanel(final App game){
        super(new FillViewport(1280, 720));
        this.game = game;

        world = new World(new Vector2(), true);
        
        ship = new Ship(game, world, 0, 0, 0);
        ship.load("./saves/ships/null.ship");
		this.addActor(ship);

        planet = new SpacePlanet(game, world, new PlanetState());
        this.addActor(planet);

        // Initialize orbit
        float R = ship.getPosition().dst(planet.getPosition());
        float velScl = (float)Math.sqrt((SpacePlanet.GRAVITY_CONSTANT * planet.getBody().getMass()) / R);
        Vector2 planetToShip = new Vector2(0, 1).scl(velScl * 0.12f);
        // ship.getBody().applyForceToCenter(planetToShip, true);

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
                } else if(keycode == ControlSchema.SHIP_FULL_THROTTLE){
                    ship.state.throttle = 1;
                } else if(keycode == ControlSchema.SHIP_NO_THROTTLE){
                    ship.state.throttle = 0;
                }

                return false;
            }

            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY){
                OrthographicCamera cam = (OrthographicCamera)getCamera();
                cam.zoom = Math.min(Math.max(cam.zoom + (amountY / 30), 0.05f), 3.0f);

                return true;
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

        planet.applyGravity(delta, ship.getBody());

        // Parent camera to the ship
        OrthographicCamera cam = (OrthographicCamera)getCamera();
        cam.position.set(ship.getBody().getWorldCenter().cpy().scl(Constants.PPM), 0);
        cam.update();

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
            debug.render(world, this.getCamera().combined.cpy().scl(Constants.PPM));
        }
    }
    
    @Override
    public void dispose(){
        super.dispose();
    }

}
