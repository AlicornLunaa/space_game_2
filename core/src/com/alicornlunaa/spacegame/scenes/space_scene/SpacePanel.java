package com.alicornlunaa.spacegame.scenes.space_scene;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Starfield;
import com.alicornlunaa.spacegame.objects.ship.Ship;
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
    private final App game;
    private Starfield backgroundTexture;

    public Ship ship;

    // Constructor
    public SpacePanel(final App game){
        super(new FillViewport(1280, 720));
        this.game = game;

        getViewport().setCamera(game.activeCamera);
        addActor(game.universe);

        backgroundTexture = new Starfield(game, (int)getWidth(), (int)getHeight());

        ship = new Ship(game, game.universe.getUniversalWorld(), -49, 0, 0);
        ship.load("./saves/ships/null.ship");
        // ship.drive(game.player);
        game.universe.addEntity(ship);

        // Controls
        this.addListener(new InputListener(){
            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY){
                game.activeCamera.zoom = Math.min(Math.max(game.activeCamera.zoom + (amountY / 30), 0.05f), 3.0f);
                return true;
            }
        });
    }

    // Functions
    public Starfield getStarfield(){ return backgroundTexture; }

    @Override
    public void act(float delta){
        super.act(delta);
        game.universe.update(delta);
    }

    public void drawSkybox(){
        Batch batch = getBatch();
        Matrix4 oldProj = batch.getProjectionMatrix().cpy();
        Matrix4 oldTrans = batch.getTransformMatrix().cpy();
        OrthographicCamera cam = (OrthographicCamera)getCamera();

        batch.begin();
        batch.setProjectionMatrix(new Matrix4());
        batch.setTransformMatrix(new Matrix4());
        backgroundTexture.setOffset(cam.position.x / 10000000, cam.position.y / 10000000);
        backgroundTexture.draw(batch, -1, -1, 2, 2);
        batch.setProjectionMatrix(oldProj);
        batch.setTransformMatrix(oldTrans);
        batch.end();
    }

    @Override
    public void draw(){
        game.player.updateCamera();

        drawSkybox();
        super.draw();

        if(Constants.DEBUG){
            game.debug.render(game.universe.getUniversalWorld().getBox2DWorld(), game.activeCamera.combined.cpy().scl(Constants.PPM));
        }
    }
    
    @Override
    public void dispose(){
        super.dispose();
    }

}
