package com.alicornlunaa.spacegame.scenes.ShipViewScene;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Entity;
import com.alicornlunaa.spacegame.objects.Ship.Ship;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.ScreenUtils;

/*
 * The ShipView class is the stage for inside a ship
 */
public class ShipViewScene implements Screen {
    
    // Variables
    private final App game;
    private Entity oldDrivingEnt;
    private ShipView shipPanel;
    private ShipViewUI uiPanel;
    private InputMultiplexer inputs = new InputMultiplexer();

    // Constructor
    public ShipViewScene(final App game, final Ship ship){
        this.game = game;

        shipPanel = new ShipView(game, ship);
        uiPanel = new ShipViewUI(game);
        inputs.addProcessor(shipPanel);
        inputs.addProcessor(uiPanel);

        // game.player.loadBodyToWorld(ship.getInteriorWorld(), Constants.SHIP_PPM);
    }

    // Functions
    @Override
    public void render(float delta) {
        // Render the stage
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1.0f);
        shipPanel.act(delta);
        uiPanel.act(delta);
        shipPanel.draw();
        uiPanel.draw();
    }

    @Override
    public void resize(int width, int height) {
        shipPanel.getViewport().update(width, height);
        uiPanel.getViewport().update(width, height);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void show() {
        oldDrivingEnt = game.player.getDriving();
        game.player.stopDriving();
        game.player.setPosition(0, 0);
        game.player.getBody().setLinearVelocity(0, 0);

        Gdx.input.setInputProcessor(inputs);
    }

    @Override
    public void hide() {
        game.player.drive(oldDrivingEnt);
        oldDrivingEnt = null;
    }

    @Override
    public void dispose() {
        shipPanel.dispose();
        uiPanel.dispose();
    }

}
