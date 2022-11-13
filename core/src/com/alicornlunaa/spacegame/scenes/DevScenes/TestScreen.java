package com.alicornlunaa.spacegame.scenes.DevScenes;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Ship;
import com.alicornlunaa.spacegame.parts.Part;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
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
        testPart.setParent(ship.getBody(), 1);

        cam.zoom = 0.05f;
        cam.position.set(0, 0, 0);
        cam.update();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        Batch batch = stage.getBatch();
        batch.begin();
        batch.setProjectionMatrix(stage.getCamera().combined);
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
