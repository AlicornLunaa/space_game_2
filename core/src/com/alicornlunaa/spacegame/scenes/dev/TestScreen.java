package com.alicornlunaa.spacegame.scenes.dev;

import com.alicornlunaa.spacegame.App;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

@SuppressWarnings("unused")
public class TestScreen implements Screen {

    private static class TestActor extends Actor {
        public TestActor(){
            setSize(100, 100);

            addListener(new ClickListener(){
                @Override
                public void clicked(InputEvent event, float x, float y){
                    System.out.println("Clicked!");
                }
            });
        }
    }

    private final App game;
    private OrthographicCamera cam;
    private Stage stage;

    public TestScreen(final App game){
        this.game = game;
        stage = new Stage(new ScreenViewport());
        stage.setDebugAll(true);

        cam = (OrthographicCamera)stage.getCamera();
        cam.zoom = 0.5f;
        cam.position.set(0, 0, 0);
        cam.update();

        TestActor test = new TestActor();
        test.setPosition(50, 50);
        stage.addActor(test);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        Batch batch = stage.getBatch();
        batch.setProjectionMatrix(stage.getCamera().combined);
        batch.setTransformMatrix(new Matrix4());
        batch.begin();
        batch.end();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
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
    public void dispose() {}
    
}