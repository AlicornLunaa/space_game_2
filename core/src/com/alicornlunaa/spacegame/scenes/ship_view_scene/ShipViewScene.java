package com.alicornlunaa.spacegame.scenes.ship_view_scene;

import com.alicornlunaa.selene_engine.components.BodyComponent;
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

        game.gameScene.player.bodyComponent.setWorld(vehicle.getInteriorWorld());
        game.gameScene.player.transform.position.set(0, 0);
        game.gameScene.player.transform.rotation = 0;
        game.gameScene.player.getComponent(BodyComponent.class).body.setLinearVelocity(0, 0);

        Gdx.input.setInputProcessor(inputs);
    }

    @Override
    public void hide() {
        game.gameScene.player.bodyComponent.setWorld(vehicle.bodyComponent.world);
        vehicle.drive(game.gameScene.player);
    }

    @Override
    public void dispose() {
        shipPanel.dispose();
        uiPanel.dispose();
    }

}
