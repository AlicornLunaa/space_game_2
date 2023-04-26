package com.alicornlunaa.spacegame.engine.scenes;

import com.alicornlunaa.spacegame.App;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ScreenUtils;

public abstract class BaseScene implements IScene {

    // Variables
    protected final App game;
    protected Color backgroundColor = new Color(0, 0, 0, 1);
    protected InputMultiplexer inputs = new InputMultiplexer();

    // Constructor
    public BaseScene(final App game){
        this.game = game;
    }

    // Functions
    @Override
    public void render(float delta) {
        ScreenUtils.clear(backgroundColor);
        game.vfxManager.update(delta);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(inputs);
    }

    @Override
    public void hide() {}

    @Override
    public void dispose() {}

    @Override
    public void resize(int width, int height) {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}
    
}
