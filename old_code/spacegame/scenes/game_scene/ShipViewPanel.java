package com.alicornlunaa.spacegame.scenes.game_scene;

import com.alicornlunaa.space_game.App;
import com.alicornlunaa.spacegame.objects.ship.Ship;
import com.alicornlunaa.spacegame.util.Constants;
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
        // game.gameScene.universe.update(delta);
    }

    public void draw(){
        super.draw();

        Batch batch = getBatch();
        batch.setProjectionMatrix(game.camera.combined);
        batch.setTransformMatrix(new Matrix4());
        batch.begin();

        ship.getInterior().draw(batch);
        // game.gameScene.player.render(batch); // TODO: REPLACE WITH COMPONENT SYSTEM

        batch.end();

        if(Constants.DEBUG){
            game.debug.render(ship.getInterior().getWorld().getBox2DWorld(), game.camera.combined.cpy().scl(ship.getInterior().getWorld().getPhysScale()));
        }
    }
    
}
