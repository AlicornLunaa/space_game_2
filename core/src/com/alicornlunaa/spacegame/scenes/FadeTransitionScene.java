package com.alicornlunaa.spacegame.scenes;

import com.alicornlunaa.spacegame.App;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class FadeTransitionScene implements Screen {

    // Variables
    private final App game;
    private final Screen fromScreen;
    private final Screen toScreen;

    private float frameTime = 0.0f;
    private float transitionlength = 0.0f;
    
    private Batch batch;
    private Pixmap pixmap;
    private Texture texture;

    // Constructor
    public FadeTransitionScene(final App game, final Screen from, final Screen to, float time){
        this.game = game;
        fromScreen = from;
        toScreen = to;
        transitionlength = time;

        batch = new SpriteBatch(1);
        pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.BLACK);
        pixmap.fill();
        texture = new Texture(pixmap);
        pixmap.dispose();

        game.setScreen(this);
    }

    // Functions
    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        float a = 0;

        if(frameTime < transitionlength){
            fromScreen.render(delta);
            a = (frameTime / transitionlength);
        } else if(frameTime < transitionlength * 2){
            toScreen.render(delta);
            a = 1 - (frameTime / transitionlength * 2);
        } else {
            game.setScreen(toScreen);
            this.dispose();
        }

        batch.begin();
        batch.setColor(1, 1, 1, a);
        batch.draw(texture, 0, 0, 600000, 600000);
        batch.end();

        frameTime += delta;
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        texture.dispose();
    }
    
}
