package com.alicornlunaa.spacegame.scenes;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.panels.SpacePanel;
import com.alicornlunaa.spacegame.panels.SpaceUIPanel;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.ScreenUtils;

public class SpaceScene implements Screen {

    // Variables
    final App game;

    public SpacePanel spacePanel;
    public SpaceUIPanel uiPanel;

    private InputMultiplexer inputs = new InputMultiplexer();

    // Constructor
    public SpaceScene(final App game){
        this.game = game;
        
        spacePanel = new SpacePanel(game);
        uiPanel = new SpaceUIPanel(game);

        inputs.addProcessor(uiPanel);
        inputs.addProcessor(spacePanel);

        // Initialize UI
        uiPanel.shipCompass.setTarget(spacePanel.ship);
        uiPanel.shipCompass.setUniverse(spacePanel.universe);
        
        // spacePanel.setDebugAll(true);
        // uiPanel.setDebugAll(true);
    }

    // Functions
    @Override
    public void render(float delta) {
        // Render the stage
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1.0f);

        spacePanel.act(delta);
        uiPanel.act(delta);

        spacePanel.draw();
        uiPanel.draw();
    }

    @Override
    public void resize(int width, int height) {
        spacePanel.getViewport().update(width, height, true);
        uiPanel.getViewport().update(width, height, true);
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
        spacePanel.dispose();
        uiPanel.dispose();
    }
    
}
