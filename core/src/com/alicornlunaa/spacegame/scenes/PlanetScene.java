package com.alicornlunaa.spacegame.scenes;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.panels.PlanetPanel;
import com.alicornlunaa.spacegame.panels.PlanetUIPanel;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.ScreenUtils;

/**
 * The planet scene is the scene while on a world
 * this removes the need to do janky physics tricks
 * to get an actual spherical planet to render.
 * This will of course simulate a spherical planet.
 */
public class PlanetScene implements Screen {

    // Variables
    final App game;

    public PlanetPanel planetPanel;
    public PlanetUIPanel uiPanel;

    private InputMultiplexer inputs = new InputMultiplexer();

    // Constructor
    public PlanetScene(final App game){
        this.game = game;

        planetPanel = new PlanetPanel(game);
        uiPanel = new PlanetUIPanel(game);

        inputs.addProcessor(planetPanel);
        inputs.addProcessor(uiPanel);

        // planetPanel.setDebugAll(true);
        uiPanel.setDebugAll(true);
    }

    // Functions
    @Override
    public void render(float delta) {
        // Render the stage
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1.0f);

        planetPanel.act(delta);
        uiPanel.act(delta);

        planetPanel.draw();
        uiPanel.draw();
    }

    @Override
    public void resize(int width, int height) {
        planetPanel.getViewport().update(width, height, true);
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
        planetPanel.dispose();
        uiPanel.dispose();
    }
    
}
