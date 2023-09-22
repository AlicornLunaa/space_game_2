package com.alicornlunaa.spacegame.scenes.game_scene;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Starfield;
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

    // Constructor
    public SpacePanel(final App game){
        super(new FillViewport(1280, 720));
        this.game = game;

        getViewport().setCamera(game.camera);
        addActor(game.gameScene.universe);

        backgroundTexture = new Starfield(game, (int)getWidth(), (int)getHeight());

        // Controls
        this.addListener(new InputListener(){
            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY){
                game.camera.zoom = Math.min(Math.max(game.camera.zoom + (amountY / 30), 0.05f), 3.0f);
                return true;
            }
        });
    }

    // Functions
    public Starfield getStarfield(){ return backgroundTexture; }

    @Override
    public void act(float delta){
        super.act(delta);
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
        drawSkybox();
        super.draw();

        if(Constants.DEBUG){
            game.debug.render(game.gameScene.universe.getUniversalWorld().getBox2DWorld(), game.camera.combined.cpy().scl(Constants.PPM));
        }
    }
    
    @Override
    public void dispose(){
        super.dispose();
    }

}
