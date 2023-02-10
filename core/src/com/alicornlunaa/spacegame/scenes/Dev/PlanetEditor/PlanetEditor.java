package com.alicornlunaa.spacegame.scenes.Dev.PlanetEditor;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Planet.Planet;
import com.alicornlunaa.spacegame.objects.Simulation.Star;
import com.alicornlunaa.spacegame.objects.Simulation.Universe;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisTable;

public class PlanetEditor implements Screen {

    // Variables
    private Stage stage;
    private Stage uiStage;
    private VisTable ui;
    private OrthographicCamera cam;

    private Universe universe;
    private Planet planet;

    // Constructor
    public PlanetEditor(final App game){
        stage = new Stage(new ScreenViewport());
        uiStage = new Stage(new ScreenViewport());
        cam = (OrthographicCamera)stage.getCamera();
        ui = new VisTable();
        ui.setFillParent(true);
        ui.top().left();

        ui.row();

        cam.position.set(0, 0, 0);
        cam.update();

        universe = new Universe(game);
        planet = new Planet(game, (OrthographicCamera)stage.getCamera(), universe, universe.getUniversalWorld(), 0, 0, 100, 150, Color.GREEN, Color.CYAN);
        universe.addCelestial(planet, null);
        universe.addCelestial(new Star(game, universe.getUniversalWorld(), 10000, 0, 100), null);
        stage.addActor(universe);

        Gdx.input.setInputProcessor(new InputMultiplexer(uiStage, stage));
    }

    // Functions
    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.05f, 0.05f, 0.05f, 1.0f);

        stage.act();
        uiStage.act();
        stage.draw();
        uiStage.draw();
    }

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
    public void dispose() {
        stage.dispose();
        uiStage.dispose();
    }
    
}
