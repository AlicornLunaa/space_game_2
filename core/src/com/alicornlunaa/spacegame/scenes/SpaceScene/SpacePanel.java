package com.alicornlunaa.spacegame.scenes.SpaceScene;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Starfield;
import com.alicornlunaa.spacegame.objects.Planet.Planet;
import com.alicornlunaa.spacegame.objects.Ship.Ship;
import com.alicornlunaa.spacegame.objects.Simulation.Star;
import com.alicornlunaa.spacegame.objects.Simulation.Universe;
import com.alicornlunaa.spacegame.objects.Simulation.Orbits.OrbitUtils;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
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
    private Starfield backgroundTexture;

    public Universe universe;
    public Ship ship;

    // Constructor
    public SpacePanel(final App game){
        super(new FillViewport(1280, 720));
        this.game = game;

        world = new World(new Vector2(), true);
        backgroundTexture = new Starfield(game, (int)getWidth(), (int)getHeight());

        ship = new Ship(game, world, 0, 0, 0);
        ship.load("./saves/ships/null.ship");

        universe = new Universe(game);
        universe.addCelestial(new Star(game, world, 1000000, 0, 695700 * Constants.CONVERSION_FACTOR), null);
        universe.addCelestial(new Planet(game, universe, world, 1000000 - 5632704 * Constants.CONVERSION_FACTOR, 0, 24390 * Constants.CONVERSION_FACTOR, 26400 * Constants.CONVERSION_FACTOR, new Color(.67f, 0.65f, 0.64f, 1), new Color(0.6f, 1.0f, 0.6f, 1.0f)), universe.getCelestial(0)); // Mercury
        universe.addCelestial(new Planet(game, universe, world, 1000000 - 10782604 * Constants.CONVERSION_FACTOR, 0, 60518 * Constants.CONVERSION_FACTOR, 62700 * Constants.CONVERSION_FACTOR, new Color(.22f, 1.0f, 0.1f, 1), new Color(0.6f, 1.0f, 0.6f, 1.0f)), universe.getCelestial(0)); // Venus
        universe.addCelestial(new Planet(game, universe, world, 1000000 - 14966899 * Constants.CONVERSION_FACTOR, 0, 63780 * Constants.CONVERSION_FACTOR, 68000 * Constants.CONVERSION_FACTOR, new Color(.72f, 0.7f, 0.9f, 1), new Color(0.6f, 0.6f, 1.0f, 1.0f)), universe.getCelestial(0)); // Earth
        universe.addCelestial(new Planet(game, universe, world, 1000000 - 22852684 * Constants.CONVERSION_FACTOR, 0, 33890 * Constants.CONVERSION_FACTOR, 36890 * Constants.CONVERSION_FACTOR, new Color(.22f, 1.0f, 0.1f, 1), new Color(0.6f, 1.0f, 0.6f, 1.0f)), universe.getCelestial(0)); // Mars
        universe.addCelestial(new Planet(game, universe, world, 1000000 - 14966899 * Constants.CONVERSION_FACTOR + 405400 * Constants.CONVERSION_FACTOR, 0, 17374 * Constants.CONVERSION_FACTOR, 0, new Color(.88f, 0.88f, 0.88f, 1), new Color(.88f, 0.88f, 0.88f, 1.0f)), universe.getCelestial(3)); // Moon
        universe.addEntity(ship);
        universe.addEntity(game.player);
        OrbitUtils.createOrbit(universe, universe.getCelestial(1));
        OrbitUtils.createOrbit(universe, universe.getCelestial(2));
        OrbitUtils.createOrbit(universe, universe.getCelestial(3));
        OrbitUtils.createOrbit(universe, universe.getCelestial(4));
        OrbitUtils.createOrbit(universe, universe.getCelestial(5));
        OrbitUtils.createOrbit(universe, ship);
        OrbitUtils.createOrbit(universe, game.player);
        this.addActor(universe);
        
        Body b = universe.getCelestial(1).getBody();
        b.applyForceToCenter(0, 100000, true);
        
        b = ship.getBody();
        b.applyForceToCenter(0, 40, true);

        game.player.drive(ship);

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
        OrthographicCamera cam = (OrthographicCamera)getCamera();
        cam.position.set(OrbitUtils.getUniverseSpacePosition(universe, ship, ship.getBody().getWorldCenter().cpy().scl(ship.getPhysScale())), 0);
        cam.update();
    }

    public void drawSkybox(){
        Batch batch = getBatch();
        Matrix4 oldProj = batch.getProjectionMatrix().cpy();
        Matrix4 oldTrans = batch.getTransformMatrix().cpy();
        OrthographicCamera cam = (OrthographicCamera)getCamera();
        
        float oldZoom = cam.zoom;
        cam.zoom = 1;
        cam.update();

        batch.begin();
        batch.setProjectionMatrix(new Matrix4());
        batch.setTransformMatrix(new Matrix4());
        backgroundTexture.setOffset(cam.position.x / 10000000, cam.position.y / 10000000);
        backgroundTexture.draw(batch, -1, -1, 2, 2);
        batch.setProjectionMatrix(oldProj);
        batch.setTransformMatrix(oldTrans);
        batch.end();

        cam.zoom = oldZoom;
        cam.update();
    }

    @Override
    public void draw(){
        super.draw();

        if(Constants.DEBUG){
            game.debug.render(world, getCamera().combined.cpy().scl(Constants.PPM));
        }
    }
    
    @Override
    public void dispose(){
        super.dispose();
    }

}
