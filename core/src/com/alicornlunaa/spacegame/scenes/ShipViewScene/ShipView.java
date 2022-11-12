package com.alicornlunaa.spacegame.scenes.ShipViewScene;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Ship;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FillViewport;

public class ShipView extends Stage {

    // Variables
    private final Ship ship;

    // Constructor
    public ShipView(final App game, final Ship ship){
        super(new FillViewport(1280, 720));
        this.ship = ship;
    }

    // Functions
    public void act(float delta){
        ship.updateWorld(delta);
    }

    public void draw(){
        super.draw();

        Batch batch = getBatch();
        batch.begin();
        ship.drawWorld(batch, 255);
        batch.end();
    }
    
}
