package com.alicornlunaa.spacegame.scenes.DevScenes;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Ship;
import com.alicornlunaa.spacegame.parts_refactor.Part;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class TestScreen implements Screen {

    private final App game;
    private OrthographicCamera cam;
    private Stage stage;
    private World world;
    private Ship ship;
    private Part testPart;

    public TestScreen(final App game){
        this.game = game;
        stage = new Stage(new ScreenViewport());
        cam = (OrthographicCamera)stage.getCamera();
        world = new World(new Vector2(), true);
        ship = new Ship(game, world, 0, 0, 0);
        testPart = Part.spawn(game, ship, "AERO", "MED_CMD_POD");

        cam.zoom = 0.1f;
        cam.update();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        Batch batch = stage.getBatch();
        batch.begin();
        batch.setProjectionMatrix(stage.getCamera().combined);
        batch.setTransformMatrix(new Matrix4().translate(stage.getWidth() / 2, stage.getHeight() / 2, 0));
        testPart.draw(batch, delta);
        batch.end();

        game.debug.render(world, stage.getCamera().combined.cpy());
    }

    @Override
    public void show() {}

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
