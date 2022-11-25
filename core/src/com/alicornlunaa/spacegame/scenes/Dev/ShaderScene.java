package com.alicornlunaa.spacegame.scenes.Dev;

import com.alicornlunaa.spacegame.App;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class ShaderScene implements Screen {

    // private final App game;
    private final App game;
    private OrthographicCamera cam;
    private Stage stage;

    private Texture texture;
    private float time = 0.0f;
    private float lastUpdate = 0.0f;

    public ShaderScene(final App game){
        this.game = game;
        stage = new Stage(new ScreenViewport());
        cam = (OrthographicCamera)stage.getCamera();

        cam.position.set(0, 0, 0);
        cam.update();

        texture = new Texture(16, 16, Format.RGBA8888);
        texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);

        // Controls
        stage.addListener(new InputListener(){
            @Override
            public boolean keyDown(InputEvent event, int keycode){
                if(keycode == Keys.F5){
                    game.manager.reloadShaders("shaders/planet");
                }

                return false;
            }
        });
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        lastUpdate += delta;
        time += delta;
        if(lastUpdate > 2.f){
            game.manager.reloadShaders("shaders/planet");
            lastUpdate = 0.f;
        }

        ShaderProgram shader = game.manager.get("shaders/planet", ShaderProgram.class);

        Batch batch = stage.getBatch();
        batch.begin();
        batch.setProjectionMatrix(cam.combined);

        batch.setShader(shader);
        shader.setUniformf("u_planetColor", Color.WHITE);
        shader.setUniformf("u_starDirection", new Vector3(0, 1, 0));
        batch.draw(texture, -128, -128, 256, 256);

        batch.setShader(null);
        batch.end();
    }

    @Override
    public void show() {}

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {}
    
}