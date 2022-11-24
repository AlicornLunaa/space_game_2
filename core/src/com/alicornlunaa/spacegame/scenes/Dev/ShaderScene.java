package com.alicornlunaa.spacegame.scenes.Dev;

import com.alicornlunaa.spacegame.App;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
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

    public ShaderScene(final App game){
        this.game = game;
        stage = new Stage(new ScreenViewport());
        cam = (OrthographicCamera)stage.getCamera();

        // cam.zoom = 0.05f;
        cam.position.set(0, 0, 0);
        cam.update();

        texture = new Texture(128, 128, Format.RGBA8888);

        // Controls
        stage.addListener(new InputListener(){
            @Override
            public boolean keyDown(InputEvent event, int keycode){
                if(keycode == Keys.F5){
                    game.manager.reloadShaders("shaders/atmosphere");
                }

                return false;
            }
        });
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        ShaderProgram shader = game.manager.get("shaders/atmosphere", ShaderProgram.class);

        Batch batch = stage.getBatch();
        batch.begin();
        batch.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, 1, 1));
        batch.setTransformMatrix(new Matrix4());

        batch.setShader(shader);
        shader.setUniformMatrix("u_invProjViewMatrix", cam.invProjectionView);
        shader.setUniformf("atmosColor", Color.WHITE);
        shader.setUniformf("atmosPlanetRatio", 0.85f);
        batch.draw(texture, 0, 0, 1, 1);

        batch.setShader(null);
        batch.end();
    }

    @Override
    public void show() {}

    @Override
    public void resize(int width, int height) {

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