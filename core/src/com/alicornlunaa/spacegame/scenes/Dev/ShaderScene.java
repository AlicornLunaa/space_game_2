package com.alicornlunaa.spacegame.scenes.Dev;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Lights.PointLight;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class ShaderScene implements Screen {

    // private final App game;
    private OrthographicCamera cam;
    private Stage stage;
    private Label fpsCounter;

    private int[] pastFPS = new int[30];
    private int fpsIndex = 0;
    private int avgFps = 0;
    private float lastUpdate = 0.0f;

    private Texture casterTexture;
    private PointLight light;

    public ShaderScene(final App game){
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

        casterTexture = new Texture(Gdx.files.internal("textures/ui/caster.png"));
        light = new PointLight(game, new Vector2());

        // Controls
        stage.addListener(new InputListener(){
            @Override
            public boolean keyDown(InputEvent event, int keycode){
                if(keycode == Keys.F5){
                    game.manager.reloadShaders("shaders/shadow_map");
                    game.manager.reloadShaders("shaders/light");
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

        Batch batch = stage.getBatch();

        light.getPosition().set(Gdx.input.getX(), Gdx.input.getY());
        light.getPosition().set(stage.screenToStageCoordinates(light.getPosition()));

        light.beginOcclusion(batch);
        batch.draw(casterTexture, 0, 0);
        light.endOcclusion(batch);

        batch.begin();
        cam.setToOrtho(false);
        cam.update();
        batch.setProjectionMatrix(cam.combined);
        light.drawLight(batch);
        batch.end();

        batch.setShader(null);
        batch.begin();
        batch.draw(casterTexture, 0, 0);
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