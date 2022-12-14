package com.alicornlunaa.spacegame.scenes.Dev;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Ship.Ship;
import com.alicornlunaa.spacegame.objects.Ship.interior.Interior;
import com.alicornlunaa.spacegame.objects.Simulation.Orbit;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class TestScreen implements Screen {

    // private final App game;
    private final App game;
    private OrthographicCamera cam;
    private Stage stage;

    private World world;
    private Ship ship;
    private Interior interior;

    public TestScreen(final App game){
        this.game = game;
        stage = new Stage(new ScreenViewport());
        cam = (OrthographicCamera)stage.getCamera();
        // cam.zoom = 0.05f;
        cam.position.set(0, 0, 0);
        cam.update();

        world = new World(new Vector2(), true);
        ship = new Ship(game, world, 0, 0, 0);
        interior = new Interior(game, ship);

        System.out.println("");
        // Orbit o = new Orbit(
        //     new Vector3(-5060f, 549f, -4419f),
        //     new Vector3(-3.53f, -5.97f, 3.3f),
        //     (float)(5.97219e24)
        // );
        // Orbit o = new Orbit(
        //     new Vector3(154.252550f, 105.699660f, 0.f),
        //     new Vector3(-4.779790f, 9.428806f, 0),
        //     (float)(27611.654000f)
        // );
        // System.out.println(o);

        Gdx.app.exit();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        Batch batch = stage.getBatch();
        batch.begin();
        batch.setProjectionMatrix(stage.getCamera().combined);

        interior.draw(batch);

        batch.end();
        
        game.debug.render(interior.getWorld(), batch.getProjectionMatrix().cpy().scl(Constants.SHIP_PPM));
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