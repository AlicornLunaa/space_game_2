package com.alicornlunaa.spacegame.scenes.Dev.PlanetEditor;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Planet2.Planet;
import com.alicornlunaa.spacegame.objects.Simulation.Star;
import com.alicornlunaa.spacegame.objects.Simulation.Universe;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisSlider;
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
        uiStage.addActor(ui);

        ui.row();
        VisSlider s = new VisSlider(0.0001f, 100.f, 0.1f, false);
        s.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent e, Actor a){
                VisSlider slider = (VisSlider)a;
                // planet.setSurface(slider.getValue());
            }
        });
        ui.add(s);
        ui.row();
        s = new VisSlider(1, 50, 1, false);
        s.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent e, Actor a){
                VisSlider slider = (VisSlider)a;
                // planet.setSlice((int)slider.getValue());
            }
        });
        ui.add(s);

        cam.position.set(0, 0, 0);
        cam.update();

        universe = new Universe(game);
        planet = new Planet(game, universe.getUniversalWorld(), 0, 0, 300, 350, 1.f);
        universe.addCelestial(planet, null);
        universe.addCelestial(new Star(game, universe.getUniversalWorld(), 10000, 0, 100), null);
        stage.addActor(universe);

        // Controls
        stage.addListener(new InputListener(){
            @Override
            public boolean keyDown(InputEvent event, int keycode){
                if(keycode == Keys.F5){
                    game.manager.reloadShaders("shaders/atmosphere");
                    game.manager.reloadShaders("shaders/planet");
                    return true;
                } else if(keycode == Keys.A){
                    cam.position.x -= 10.f;
                    cam.update();
                    return true;
                } else if(keycode == Keys.D){
                    cam.position.x += 10.f;
                    cam.update();
                    return true;
                } else if(keycode == Keys.W){
                    cam.position.y -= 10.f;
                    cam.update();
                    return true;
                } else if(keycode == Keys.S){
                    cam.position.y += 10.f;
                    cam.update();
                    return true;
                }

                return false;
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
