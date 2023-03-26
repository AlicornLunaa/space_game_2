package com.alicornlunaa.spacegame.scenes.SpaceScene;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Starfield;
import com.alicornlunaa.spacegame.objects.Planet.Planet;
import com.alicornlunaa.spacegame.objects.Ship.Ship;
import com.alicornlunaa.spacegame.objects.Simulation.Star;
import com.alicornlunaa.spacegame.objects.Simulation.Orbits.OrbitUtils;
import com.alicornlunaa.spacegame.phys.PhysWorld;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FillViewport;

public class SpacePanel extends Stage {

    // Variables
    final App game;

    private PhysWorld world;
    private Starfield backgroundTexture;

    public Ship ship;

    // Constructor
    public SpacePanel(final App game){
        super(new FillViewport(1280, 720));
        this.game = game;

        world = game.simulation.addWorld(Constants.PPM);
        backgroundTexture = new Starfield(game, (int)getWidth(), (int)getHeight());

        ship = new Ship(game, world, 0, 0, 0);
        ship.load("./saves/ships/null.ship");

        game.universe.addCelestial(new Star(game, world, 1000000, 0, 695700 * Constants.CONVERSION_FACTOR), null);
        game.universe.addCelestial(new Planet(game, world, 1000000 - 5632704 * Constants.CONVERSION_FACTOR, 0, 24390 * Constants.CONVERSION_FACTOR, 29400 * Constants.CONVERSION_FACTOR, 1), game.universe.getCelestial(0)); // Mercury
        game.universe.addCelestial(new Planet(game, world, 1000000 - 10782604 * Constants.CONVERSION_FACTOR, 0, 60518 * Constants.CONVERSION_FACTOR, 62700 * Constants.CONVERSION_FACTOR, 1), game.universe.getCelestial(0)); // Venus
        game.universe.addCelestial(new Planet(game, world, 1000000 - 14966899 * Constants.CONVERSION_FACTOR, 0, 63780 * Constants.CONVERSION_FACTOR, 68000 * Constants.CONVERSION_FACTOR, 1), game.universe.getCelestial(0)); // Earth
        game.universe.addCelestial(new Planet(game, world, 1000000 - 22852684 * Constants.CONVERSION_FACTOR, 0, 33890 * Constants.CONVERSION_FACTOR, 36890 * Constants.CONVERSION_FACTOR, 1), game.universe.getCelestial(0)); // Mars
        game.universe.addCelestial(new Planet(game, world, 1000000 - 14966899 * Constants.CONVERSION_FACTOR + 405400 * Constants.CONVERSION_FACTOR, 0, 17374 * Constants.CONVERSION_FACTOR, 0, 0), game.universe.getCelestial(3)); // Moon
        game.universe.addEntity(ship);
        game.universe.addEntity(game.player);
        OrbitUtils.createOrbit(game.universe, game.universe.getCelestial(1));
        OrbitUtils.createOrbit(game.universe, game.universe.getCelestial(2));
        OrbitUtils.createOrbit(game.universe, game.universe.getCelestial(3));
        OrbitUtils.createOrbit(game.universe, game.universe.getCelestial(4));
        OrbitUtils.createOrbit(game.universe, game.universe.getCelestial(5));
        // OrbitUtils.createOrbit(game.universe, ship);
        // OrbitUtils.createOrbit(game.universe, game.player);
        this.addActor(game.universe);

        // game.player.drive(ship);

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
    public PhysWorld getWorld(){ return world; }
    public Starfield getStarfield(){ return backgroundTexture; }

    @Override
    public void act(float delta){
        super.act(delta);

        // Physics updates
        game.universe.update(delta);

        // Parent camera to the player
        OrthographicCamera cam = (OrthographicCamera)getCamera();
        game.player.updateCamera(cam, false);
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
            game.debug.render(world.getBox2DWorld(), getCamera().combined.cpy().scl(Constants.PPM));
        }
    }
    
    @Override
    public void dispose(){
        super.dispose();
    }

}
