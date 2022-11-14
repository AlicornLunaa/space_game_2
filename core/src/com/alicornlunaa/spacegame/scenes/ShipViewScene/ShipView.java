package com.alicornlunaa.spacegame.scenes.ShipViewScene;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Ship.Ship;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FillViewport;

public class ShipView extends Stage {

    // Variables
    private final App game;
    private final Ship ship;

    // Constructor
    public ShipView(final App game, final Ship ship){
        super(new FillViewport(1280, 720));
        this.game = game;
        this.ship = ship;
    }

    // Functions
    public void act(float delta){
        ship.updateWorld(delta);
    }

    public void draw(){
        super.draw();

        OrthographicCamera cam = (OrthographicCamera)getCamera();
        cam.zoom = 0.25f;

        Batch batch = getBatch();
        batch.begin();
        game.player.updateCamera(cam);
        ship.drawWorld(batch, 1);
        batch.end();
    }
    
}
