package com.alicornlunaa.spacegame.scenes.ship_view_scene;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.ship.Ship;
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
    private ShipView shipPanel;
    private ShipViewUI uiPanel;
    private InputMultiplexer inputs = new InputMultiplexer();

    private Ship vehicle;

    // Constructor
    public ShipViewScene(final App game, final Ship ship){
        this.game = game;

        shipPanel = new ShipView(game, ship);
        uiPanel = new ShipViewUI(game);
        inputs.addProcessor(shipPanel);
        inputs.addProcessor(uiPanel);

        vehicle = ship;
    }

    // Functions
    @Override
    public void render(float delta) {
        // Render the stage
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1.0f);
        game.vfxManager.update(delta);

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
        vehicle.stopDriving();

        game.simulation.addEntity(vehicle.getInteriorWorld(), game.player);

        game.player.setPosition(0, 0);
        game.player.setRotation(0);
        game.player.getBody().setLinearVelocity(0, 0);

        Gdx.input.setInputProcessor(inputs);
    }

    @Override
    public void hide() {
        game.simulation.addEntity(vehicle.getWorld(), game.player);
        vehicle.drive(game.player);
    }

    @Override
    public void dispose() {
        shipPanel.dispose();
        uiPanel.dispose();
    }

}
