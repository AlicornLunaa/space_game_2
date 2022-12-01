package com.alicornlunaa.spacegame.scenes.Dev;

import com.alicornlunaa.spacegame.App;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class ShaderScene implements Screen {

    private final App game;
    private OrthographicCamera cam;
    private Stage stage;
    private Label fpsCounter;

    private int[] pastFPS = new int[30];
    private int fpsIndex = 0;
    private int avgFps = 0;
    private float lastUpdate = 0.0f;

    private Texture tex;

    public ShaderScene(final App game){
        this.game = game;
        stage = new Stage(new ScreenViewport());

        fpsCounter = new Label("FPS: N/A", game.skin);
        fpsCounter.setPosition(5, 5);
        stage.addActor(fpsCounter);

        for(int i = 0; i < pastFPS.length; i++){
            pastFPS[i] = 0;
        }

        cam = (OrthographicCamera)stage.getCamera();
        cam.position.set(0, 0, 0);
        cam.update();

        tex = new Texture(16, 16, Format.RGBA8888);

        // Controls
        stage.addListener(new InputListener(){
            @Override
            public boolean keyDown(InputEvent event, int keycode){
                if(keycode == Keys.F5){
                    game.manager.reloadShaders("shaders/cartesian_atmosphere");
                }

                return false;
            }
        });
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1);

        lastUpdate += delta;
        if(lastUpdate > 0.01f){
            avgFps = 0;
            pastFPS[fpsIndex] = (int)(1 / Gdx.graphics.getDeltaTime());
            fpsIndex = (fpsIndex + 1) % pastFPS.length;
            for(int i = 0; i < pastFPS.length; i++){
                avgFps += pastFPS[i];
            }
            avgFps /= pastFPS.length;

            lastUpdate = 0.f;
        }

        if(Gdx.input.isKeyJustPressed(Keys.F5)){
            game.manager.reloadShaders("shaders/cartesian_atmosphere");
        }

        ShaderProgram shader = game.manager.get("shaders/cartesian_atmosphere", ShaderProgram.class);
        Batch batch = stage.getBatch();
        batch.begin();

        batch.setShader(shader);
        shader.setUniformf("u_atmosColor", Color.WHITE);
        shader.setUniformf("u_starDirection", new Vector3(1, 0, 0));
        shader.setUniformf("u_planetRadius", 0.8f);
        shader.setUniformf("u_occluder.pos", new Vector2(0, 100));
        shader.setUniformf("u_occluder.radius", 1.f);
        shader.setUniformf("u_occlusionEnabled", 0.f);
        batch.draw(tex, -1024 / 2, -512 / 2, 1024, 512);

        batch.setShader(null);
        batch.end();
        
        fpsCounter.setText(String.valueOf(avgFps));
        stage.draw();
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