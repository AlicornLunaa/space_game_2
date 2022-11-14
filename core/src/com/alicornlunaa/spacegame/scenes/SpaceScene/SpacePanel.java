package com.alicornlunaa.spacegame.scenes.SpaceScene;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Ship;
import com.alicornlunaa.spacegame.objects.Starfield;
import com.alicornlunaa.spacegame.objects.Planet.Planet;
import com.alicornlunaa.spacegame.objects.Simulation.Star;
import com.alicornlunaa.spacegame.objects.Simulation.Universe;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
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
        backgroundTexture = new Starfield((int)getWidth(), (int)getHeight());

        ship = new Ship(game, world, 0, 0, 0);
        ship.load("./saves/ships/null.ship");

        universe = new Universe(game);
        universe.addEntity(ship);
        universe.addEntity(game.player);
        universe.addCelestial(new Star(game, world, 78000, 0, 15000), null);
        universe.addCelestial(new Planet(game, world, -18000, 0, 12000, 15000, new Color(.72f, 0.7f, 0.9f, 1), new Color(0.6f, 0.6f, 1, 0.5f)), universe.getCelestial(0));
        universe.addCelestial(new Planet(game, world, 5000, 0, 1000, 1500, new Color(.22f, 1.0f, 0.1f, 1), new Color(0.26f, 1.0f, 0.1f, 0.5f)), universe.getCelestial(1));
        universe.createCelestialOrbit(universe.getCelestial(1));
        universe.createCelestialOrbit(universe.getCelestial(2));
        universe.createEntityOrbit(ship);
        universe.createEntityOrbit(game.player);
        this.addActor(universe);

        game.player.drive(ship);

        universe.getCelestial(2).getBody().applyLinearImpulse(0, 350, universe.getCelestial(2).getBody().getWorldCenter().x, universe.getCelestial(2).getBody().getWorldCenter().y, true);

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
        cam.position.set(universe.getUniversalPosition(ship, ship.getBody().getWorldCenter().cpy().scl(ship.getPhysScale())), 0);
        cam.update();
    }

    @Override
    public void draw(){
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
        backgroundTexture.setOffset(cam.position.x / 100000, cam.position.y / 100000);
        backgroundTexture.draw(batch, -1, -1, 2, 2);
        batch.setProjectionMatrix(oldProj);
        batch.setTransformMatrix(oldTrans);
        batch.end();

        cam.zoom = oldZoom;
        cam.update();

        super.draw();

        if(Constants.DEBUG){
            game.debug.render(world, getCamera().combined.cpy());

            batch.begin();
            ship.draw(batch, 1);
            batch.end();
        }
    }
    
    @Override
    public void dispose(){
        super.dispose();
    }

}
