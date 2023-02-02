package com.alicornlunaa.spacegame.scenes.Dev;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Simulation.Orbits.ConicSectionOld;
import com.alicornlunaa.spacegame.objects.Simulation.Orbits.EllipticalConic;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Scene to just test orbital paths reliably and quickly
 */
public class OrbitTest implements Screen {

    // private final App game;
    private final App game;
    private OrthographicCamera cam;
    private Stage stage;

    private EllipticalConic testConic;

    public OrbitTest(final App game) {
        this.game = game;
        stage = new Stage(new ScreenViewport());
        cam = (OrthographicCamera) stage.getCamera();
        cam.position.set(0, 0, 0);
        cam.update();

        testConic = new EllipticalConic(100, 70, 0.01, 0.1, 0, 0);

        stage.addListener(new InputListener() {
            private float dx = 0;
            private float dy = 0;
            private int button = 0;

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                dx = x;
                dy = y;
                this.button = button;
                return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                dx = x - dx;
                dy = y - dy;

                if (button == Input.Buttons.RIGHT) {
                    cam.position.add(dx * -0.2f, dy * -0.2f, 0);
                    cam.update();
                }

                dx = x;
                dy = y;
            }

            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY) {
                cam.zoom = Math.min(Math.max(cam.zoom + amountY * 0.05f, 0.05f), 100.f);
                cam.update();
                return true;
            }
        });

        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        Batch batch = stage.getBatch();
        batch.setProjectionMatrix(stage.getCamera().combined);
        batch.setTransformMatrix(new Matrix4());
        batch.begin();

        batch.end();

        ShapeRenderer renderer = game.shapeRenderer;
        renderer.setProjectionMatrix(stage.getCamera().combined);
        renderer.setTransformMatrix(new Matrix4());
        renderer.setAutoShapeType(true);
        renderer.begin(ShapeType.Filled);

        renderer.setColor(Color.RED);
        renderer.circle(0, 0, 50);

        renderer.set(ShapeType.Line);
        renderer.setColor(Color.LIME);
        renderer.circle(0, 0, 55);
        testConic.draw(renderer, 5);

        renderer.end();
        renderer.setAutoShapeType(false);
    }

    @Override
    public void show() {
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
    }

}
