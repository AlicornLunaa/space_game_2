package com.alicornlunaa.spacegame.panels;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Player;
import com.alicornlunaa.spacegame.objects.Ship;
import com.alicornlunaa.spacegame.objects.Planet.Planet;
import com.alicornlunaa.spacegame.util.Constants;
import com.alicornlunaa.spacegame.util.ControlSchema;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FillViewport;

public class SpacePanel extends Stage {

    // Variables
    final App game;

    private World world;
    private float physAccumulator = 0.0f;

    public Player player;
    public Ship ship;
    public Planet planet;
    
    private Box2DDebugRenderer debug = new Box2DDebugRenderer();

    // Constructor
    public SpacePanel(final App game){
        super(new FillViewport(1280, 720));
        this.game = game;

        world = new World(new Vector2(), true);

        player = new Player(game, world, 0, 0, Constants.PPM);
        this.addActor(player);
        
        ship = new Ship(game, world, 0, 0, 0);
        ship.load("./saves/ships/null.ship");
		this.addActor(ship);
        player.drive(ship);

        planet = new Planet(game, world, -1300, 0);
        this.addActor(planet);

        // Initialize orbit
        float radius = ship.getBody().getPosition().dst(planet.getBody().getPosition());
        float velScl = (float)Math.sqrt((Constants.GRAVITY_CONSTANT * ship.getBody().getMass() * planet.getBody().getMass()) / radius);
        ship.getBody().applyForceToCenter(0, velScl * 2.5f, true);

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
        physAccumulator += Math.min(delta, 0.25f);
        while(physAccumulator >= Constants.TIME_STEP){
            world.step(Constants.TIME_STEP, Constants.VELOCITY_ITERATIONS, Constants.POSITION_ITERATIONS);
            physAccumulator -= Constants.TIME_STEP;
        }

        planet.applyGravity(delta, ship.getBody());
        planet.applyDrag(delta, ship.getBody());
        planet.checkTransfer(ship);

        // Parent camera to the player
        player.updateCamera((OrthographicCamera)getCamera());
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
