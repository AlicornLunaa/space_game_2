package com.alicornlunaa.spacegame.scenes.Dev;

import com.alicornlunaa.spacegame.App;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class ShaderScene implements Screen {

    // private final App game;
    private final App game;
    private OrthographicCamera cam;
    private Stage stage;
    private Label fpsCounter;

    private int[] pastFPS = new int[30];
    private int fpsIndex = 0;
    private int avgFps = 0;
    private float lastUpdate = 0.0f;

    private Texture casterTexture;

    private Vector2 lightPos = new Vector2(0, 0);
    private int lightResolution = 512;
    private FrameBuffer occlusionFBO;
    private TextureRegion occlusionMap;
    private FrameBuffer shadowMapFBO;
    private TextureRegion shadowMap;

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

        casterTexture = new Texture(Gdx.files.internal("textures/ui/caster.png"));

        occlusionFBO = new FrameBuffer(Format.RGBA8888, lightResolution, lightResolution, false);
        occlusionMap = new TextureRegion(occlusionFBO.getColorBufferTexture());
        occlusionMap.flip(false, true);
        
        shadowMapFBO = new FrameBuffer(Format.RGBA8888, lightResolution, 1, false);
        Texture shadowTexture = shadowMapFBO.getColorBufferTexture();
        shadowTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        shadowTexture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
        shadowMap = new TextureRegion(shadowTexture);
        shadowMap.flip(false, true);

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
        ShaderProgram shadowShader = game.manager.get("shaders/shadow_map", ShaderProgram.class);
        ShaderProgram lightShader = game.manager.get("shaders/light", ShaderProgram.class);

        lightPos.set(Gdx.input.getX(), Gdx.input.getY());
        lightPos = stage.screenToStageCoordinates(lightPos);

        occlusionFBO.begin();
        ScreenUtils.clear(0, 0, 0, 1);
        cam.setToOrtho(false, occlusionFBO.getWidth(), occlusionFBO.getHeight());
        cam.translate(lightPos.x - lightResolution / 2.f, lightPos.y - lightResolution / 2.f);
        cam.update();
        batch.setProjectionMatrix(cam.combined);
        batch.setShader(null);
        batch.begin();
        batch.draw(casterTexture, 0, 0);
        batch.end();
        occlusionFBO.end();

        shadowMapFBO.begin();
        ScreenUtils.clear(0, 0, 0, 1);
        batch.setShader(shadowShader);
        batch.begin();
        shadowShader.setUniformf("u_resolution", lightResolution, lightResolution);
        cam.setToOrtho(false, shadowMapFBO.getWidth(), shadowMapFBO.getHeight());
        batch.setProjectionMatrix(cam.combined);
        batch.draw(occlusionFBO.getColorBufferTexture(), 0, 0, lightResolution, shadowMapFBO.getHeight());
        batch.end();
        shadowMapFBO.end();

        batch.setShader(lightShader);
        batch.begin();
        cam.setToOrtho(false);
        cam.update();
        batch.setProjectionMatrix(cam.combined);
        lightShader.setUniformf("u_resolution", lightResolution, lightResolution);
        batch.draw(shadowMap.getTexture(), lightPos.x - lightResolution / 2.f, lightPos.y - lightResolution / 2.f, lightResolution, lightResolution);
        batch.end();

        batch.setShader(null);
        batch.begin();
        batch.draw(casterTexture, 0, 0);
		// batch.setColor(Color.BLACK);
		// batch.draw(occlusionMap, stage.getWidth() - lightResolution, 0);
		// batch.setColor(Color.WHITE);
		// batch.draw(shadowMap, stage.getWidth() - lightResolution, lightResolution + 5, lightResolution, 15);
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