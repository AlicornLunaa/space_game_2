package com.alicornlunaa.spacegame.scenes.ShipViewScene;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Player;
import com.alicornlunaa.spacegame.objects.Ship;
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

    private InputMultiplexer inputs = new InputMultiplexer();

    // Constructor
    public ShipViewScene(final App game, final Ship ship){
        this.game = game;
        shipPanel = new ShipView(game, ship);
    }

    // Functions
    @Override
    public void render(float delta) {
        // Render the stage
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1.0f);

        shipPanel.act(delta);

        shipPanel.draw();
    }

    @Override
    public void resize(int width, int height) {
        shipPanel.getViewport().update(width, height);
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
        shipPanel.dispose();
    }

}
