package com.alicornlunaa.spacegame.scenes.Dev;

import com.alicornlunaa.spacegame.App;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
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

    private TextureRegion texture;
    private float time = 0.0f;
    private float lastUpdate = 0.0f;

    public ShaderScene(final App game){
        this.game = game;
        stage = new Stage(new ScreenViewport());
        cam = (OrthographicCamera)stage.getCamera();

        cam.position.set(0, 0, 0);
        cam.update();

        // texture = new TextureRegion(new Texture(16, 32, Format.RGBA8888));
        texture = game.atlas.findRegion("ui/test");

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
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1);

        lastUpdate += delta;
        time += delta;
        if(lastUpdate > 2.f){
            game.manager.reloadShaders("shaders/shadow_map");
            lastUpdate = 0.f;
        }

        ShaderProgram shader = game.manager.get("shaders/shadow_map", ShaderProgram.class);

        Batch batch = stage.getBatch();
        batch.begin();
        batch.setProjectionMatrix(cam.combined);

        batch.setShader(shader);
        shader.setUniformf("u_time", time);
        shader.setUniformf("u_lightMapRes", new Vector2(256, 256));
        shader.setUniformf("u_occlusionMapRes", new Vector2(texture.getRegionWidth(), texture.getRegionHeight()));
        shader.setUniformf("u_light.position", new Vector3(0.5f, 0.5f, 0.0f));
        shader.setUniformf("u_light.color", Color.RED);
        shader.setUniformf("u_light.intensity", 1.0f);
        shader.setUniformf("u_light.attenuation", 1.0f);
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