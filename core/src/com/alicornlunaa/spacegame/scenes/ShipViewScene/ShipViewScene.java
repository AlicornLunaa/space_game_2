package com.alicornlunaa.spacegame.scenes.ShipViewScene;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.ScreenUtils;

/*
 * The ShipView class is the stage for inside a ship
 */
public class ShipViewScene implements Screen {
    
    // Variables

    // Constructor

    // Functions
    @Override
    public void render(float delta) {
        // Render the stage
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1.0f);
    }

    @Override
    public void resize(int width, int height) {
        
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void show() {

    }

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        
    }

}
