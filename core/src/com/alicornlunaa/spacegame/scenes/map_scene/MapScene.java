package com.alicornlunaa.spacegame.scenes.map_scene;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Player;
import com.alicornlunaa.spacegame.scenes.space_scene.SpaceUIPanel;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.ScreenUtils;

public class MapScene implements Screen {
    
    // Variables
    final App game;

    public MapPanel mapPanel;
    // public MapUIPanel uiPanel;
    public SpaceUIPanel uiShip;

    private InputMultiplexer inputs = new InputMultiplexer();

    // Constructor
    public MapScene(final App game, final Player player, OrthographicCamera oldCam){
        this.game = game;

        mapPanel = new MapPanel(game, oldCam);
        uiShip = game.spaceScene.uiPanel;

        inputs.addProcessor(mapPanel);
        // inputs.addProcessor(uiPanel);
        inputs.addProcessor(uiShip);

        mapPanel.setDebugAll(true);
        // uiPanel.setDebugAll(true);
    }

    // Functions
    @Override
    public void render(float delta) {
        // Render the stage
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1.0f);

        // Update vfx manager
        game.vfxManager.update(delta);

        // Render
        mapPanel.act(delta);
        uiShip.act(delta);

        mapPanel.draw();
        uiShip.draw();
    }

    @Override
    public void resize(int width, int height) {
        mapPanel.getViewport().update(width, height, true);
        uiShip.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void show() {
        Gdx.input.setInputProcessor(inputs);
    }

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        mapPanel.dispose();
        // uiPanel.dispose();
    }
}
