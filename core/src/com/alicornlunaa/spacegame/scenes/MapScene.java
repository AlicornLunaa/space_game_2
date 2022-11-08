package com.alicornlunaa.spacegame.scenes;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Player;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.ScreenUtils;

public class MapScene implements Screen {
    
    // Variables
    final App game;

    // public MapPanel mapPanel;
    // public MapUIPanel uiPanel;

    private InputMultiplexer inputs = new InputMultiplexer();

    // Constructor
    public MapScene(final App game, final Player player){
        this.game = game;

        // inputs.addProcessor(mapPanel);
        // inputs.addProcessor(uiPanel);

        // Initialize UI
        // uiPanel.shipCompass.setTarget(game.planetScene.planetPanel.player);

        // mapPanel.setDebugAll(true);
        // uiPanel.setDebugAll(true);
    }

    // Functions
    @Override
    public void render(float delta) {
        // Render the stage
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1.0f);

        // mapPanel.act(delta);
        // uiPanel.act(delta);

        // mapPanel.draw();
        // uiPanel.draw();
    }

    @Override
    public void resize(int width, int height) {
        // mapPanel.getViewport().update(width, height, true);
        // uiPanel.getViewport().update(width, height, true);
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
        // mapPanel.dispose();
        // uiPanel.dispose();
    }
}
