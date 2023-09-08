package com.alicornlunaa.spacegame.scenes.game_scene;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.ship.Ship;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FillViewport;

public class ShipViewPanel extends Stage {

    // Variables
    private final App game;
    public final Ship ship;

    // Constructor
    public ShipViewPanel(final App game, final Ship ship){
        super(new FillViewport(1280, 720));
        this.game = game;
        this.ship = ship;
    }

    // Functions
    public void act(float delta){
        game.gameScene.universe.update(delta);
    }

    public void draw(){
        super.draw();

        OrthographicCamera cam = (OrthographicCamera)getCamera();
        cam.position.set(game.gameScene.player.getPosition(), 0);
        cam.zoom = 0.45f;
        cam.update();

        Batch batch = getBatch();
        batch.setProjectionMatrix(cam.combined);
        batch.setTransformMatrix(new Matrix4());
        batch.begin();
        ship.drawWorld(batch, 1);
        batch.end();

        if(Constants.DEBUG){
            game.debug.render(ship.getInteriorWorld().getBox2DWorld(), cam.combined.cpy().scl(ship.getInteriorWorld().getPhysScale()));
        }
    }
    
}
