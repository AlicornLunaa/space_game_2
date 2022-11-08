package com.alicornlunaa.spacegame.panels;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Player;
import com.alicornlunaa.spacegame.objects.Ship;
import com.alicornlunaa.spacegame.objects.Simulation.Celestial;
import com.alicornlunaa.spacegame.objects.Simulation.Universe;
import com.alicornlunaa.spacegame.util.Constants;
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

    public Universe universe;
    public Player player;
    public Ship ship;
    
    private Box2DDebugRenderer debug = new Box2DDebugRenderer();

    // Constructor
    public SpacePanel(final App game){
        super(new FillViewport(1280, 720));
        this.game = game;

        world = new World(new Vector2(), true);

        player = new Player(game, world, 0, 0, Constants.PPM);
        ship = new Ship(game, world, 0, 0, 0);
        ship.load("./saves/ships/null.ship");
        // planet = new Planet(game, world, -2200, 0, new Color(.72f, 0.7f, 0.9f, 1), new Color(0.6f, 0.6f, 1, 0.5f));

        universe = new Universe(game);
        universe.addEntity(ship);
        universe.addCelestial(new Celestial(game, world, 1000), null);
        universe.addCelestial(new Celestial(game, world, 100), universe.getCelestial(0));
        universe.getCelestial(0).setPosition(universe.getCelestial(0).getRadius() * -2 - 1000, 0);
        universe.getCelestial(1).setPosition(1800, 0);
        universe.createCelestialOrbit(universe.getCelestial(1));
        this.addActor(universe);

        player.drive(ship);

        // Controls
        this.addListener(new InputListener(){
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
        
        universe.update(delta);

        // Parent camera to the player
        // player.updateCamera((OrthographicCamera)getCamera());
        OrthographicCamera cam = (OrthographicCamera)getCamera();
        cam.position.set(universe.getUniversalPosition(ship), 0);
        cam.update();
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
