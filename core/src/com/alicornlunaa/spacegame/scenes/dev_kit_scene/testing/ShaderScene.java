package com.alicornlunaa.spacegame.scenes.dev_kit_scene.testing;

import com.alicornlunaa.spacegame.App;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisSlider;
import com.kotcrab.vis.ui.widget.VisTable;

public class ShaderScene implements Screen {

    private final App game;
    private OrthographicCamera cam;
    private Stage stage;
    private Stage uiStage;

    private VisTable ui;
    private Label fpsCounter;

    private float atmosRad = 10.f;
    private float planetRad = 10.f;

    private int[] pastFPS = new int[30];
    private int fpsIndex = 0;
    private int avgFps = 0;
    private float lastUpdate = 0.0f;

    private Texture tex;

    public ShaderScene(final App game){
        this.game = game;
        stage = new Stage(new ScreenViewport());
        uiStage = new Stage(new ScreenViewport());

        ui = new VisTable();
        uiStage.addActor(ui);
        uiStage.setDebugAll(true);

        fpsCounter = new Label("FPS: N/A", game.skin);
        fpsCounter.setPosition(5, 5);
        ui.addActor(fpsCounter);
        ui.left().top().pad(5);

        ui.setFillParent(true);
        ui.row();
        VisSlider slider = new VisSlider(10, 1000, 0.01f, false);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor){
                VisSlider s = (VisSlider)actor;
                atmosRad = s.getValue();
            }
        });
        ui.add(slider);
        slider = new VisSlider(10, 1000, 0.01f, false);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor){
                VisSlider s = (VisSlider)actor;
                planetRad = s.getValue();
            }
        });
        ui.add(slider);

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
                    // game.manager.reloadShaders("shaders/atmosphere");
                }

                return false;
            }

            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY) {
                cam.zoom = Math.min(Math.max(cam.zoom + amountY * 0.05f, 0.05f), 100.f);
                cam.update();
                return true;
            }
        });

        Gdx.input.setInputProcessor(new InputMultiplexer(uiStage, stage));
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
            game.manager.reload();
        }else if(Gdx.input.isKeyJustPressed(Keys.A)){
            cam.position.add(10, 0, 0);
            cam.update();
        }

        ShaderProgram shader = game.manager.get("shaders/atmosphere", ShaderProgram.class);
        Batch batch = stage.getBatch();
        batch.begin();

        batch.setShader(shader);
        shader.setUniformMatrix("u_invCamTrans", cam.invProjectionView);
        shader.setUniformf("u_atmosColor", Color.CYAN);
        shader.setUniformf("u_starDirection", new Vector3(1, 0, 0));
        shader.setUniformf("u_cameraWorldPos", cam.position);
        shader.setUniformf("u_planetWorldPos", new Vector2(0, 0));
        shader.setUniformf("u_planetRadius", planetRad);
        shader.setUniformf("u_atmosRadius", atmosRad);

        batch.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, 1280, 720));
        batch.setTransformMatrix(new Matrix4());
        batch.draw(tex, 0, 0, 1280, 720);

        batch.setShader(null);
        batch.end();
        
        fpsCounter.setText(String.valueOf(avgFps));
        stage.draw();
        uiStage.draw();
        uiStage.act();
    }

    @Override
    public void show() {}

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height);
        uiStage.getViewport().update(width, height);
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