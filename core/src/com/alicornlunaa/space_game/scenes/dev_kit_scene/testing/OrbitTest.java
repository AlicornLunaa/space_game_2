package com.alicornlunaa.space_game.scenes.dev_kit_scene.testing;

import com.alicornlunaa.space_game.App;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSlider;
import com.kotcrab.vis.ui.widget.VisTable;

/**
 * Scene to just test orbital paths reliably and quickly
 */
public class OrbitTest implements Screen {

    // private final App game;
    private final App game;
    private OrthographicCamera cam;
    private Stage stage;
    private Stage uiStage;

    private VisTable ui;
    private VisSlider aSlider;
    private VisSlider eSlider;
    private VisSlider wSlider;
    private VisSlider vSlider;
    private VisCheckBox iCheckBox;

    // private GenericConic testConic;

    public OrbitTest(final App game) {
        this.game = game;
        stage = new Stage(new ScreenViewport());
        uiStage = new Stage(new ScreenViewport());
        cam = (OrthographicCamera) stage.getCamera();
        cam.position.set(0, 0, 0);
        cam.update();

        // testConic = new EllipticalConic(100, 80, 0.3, 0.1, 0, 0);

        ui = new VisTable();
        ui.setFillParent(true);
        ui.left().top();
        ui.row().minWidth(120).pad(4).left().top();
        ui.add(new VisLabel("Semimajor axis"));
        aSlider = new VisSlider(1, 500, 1f, false); ui.add(aSlider);
        ui.row().minWidth(120).pad(4).left().top();
        ui.add(new VisLabel("Eccentricity"));
        eSlider = new VisSlider(0, 5, 0.0001f, false); ui.add(eSlider);
        ui.row().minWidth(120).pad(4).left().top();
        ui.add(new VisLabel("Argument of periapsis"));
        wSlider = new VisSlider(0, (float)(Math.PI * 2.0), 0.1f, false); ui.add(wSlider);
        ui.row().minWidth(120).pad(4).left().top();
        ui.add(new VisLabel("True anomaly"));
        vSlider = new VisSlider(0, (float)(Math.PI * 2.0), 0.1f, false); ui.add(vSlider);
        ui.row().minWidth(120).pad(4).left().top();
        iCheckBox = new VisCheckBox("Retrograde"); ui.add(iCheckBox).colspan(2);
        uiStage.addActor(ui);

        // aSlider.setValue((float)testConic.getSemiMajorAxis());
        // eSlider.setValue((float)testConic.getEccentricity());
        // wSlider.setValue((float)testConic.getArgumentofPeriapsis());
        // iCheckBox.setChecked(testConic.getInclination() > Math.PI / 2);

        ChangeListener l = new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor){
                // testConic = new EllipticalConic(100, aSlider.getValue(), eSlider.getValue(), wSlider.getValue(), 0, iCheckBox.isChecked() ? Math.PI : 0);
                // testConic = OrbitPropagator.getConic(100, aSlider.getValue(), eSlider.getValue(), wSlider.getValue(), vSlider.getValue(), iCheckBox.isChecked() ? Math.PI : 0);
            }
        };
        aSlider.addListener(l);
        eSlider.addListener(l);
        wSlider.addListener(l);
        vSlider.addListener(l);
        iCheckBox.addListener(l);

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

        Gdx.input.setInputProcessor(new InputMultiplexer(uiStage, stage));
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        Batch batch = stage.getBatch();
        batch.setProjectionMatrix(stage.getCamera().combined);
        batch.setTransformMatrix(new Matrix4());
        stage.getViewport().setCamera(cam);
        batch.begin();

        batch.end();

        ShapeRenderer renderer = game.shapeRenderer;
        renderer.setProjectionMatrix(stage.getCamera().combined);
        renderer.setTransformMatrix(new Matrix4());
        renderer.setAutoShapeType(true);
        renderer.begin(ShapeType.Filled);

        renderer.setColor(Color.CORAL);
        renderer.circle(0, 0, 50);

        // testConic.draw(renderer, 1);

        renderer.end();
        renderer.setAutoShapeType(false);

        uiStage.act();
        uiStage.draw();
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
