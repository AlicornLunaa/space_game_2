package com.alicornlunaa.selene_engine.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ScreenUtils;

public abstract class BaseScene implements IScene {
    // Variables
    protected Color backgroundColor = new Color(0, 0, 0, 1);
    protected InputMultiplexer inputs = new InputMultiplexer();

    // Functions
    @Override
    public void render(float deltaTime) {
        ScreenUtils.clear(backgroundColor);
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
