package com.alicornlunaa.spacegame.panels;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.util.ControlSchema;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FillViewport;

public class MapPanel extends Stage {

    // Variables
    final App game;

    private final SpacePanel spacePanel;
    private final OrthographicCamera cam;
    private float oldZoom = 0.0f;

    // Constructor
    public MapPanel(final App game, final Screen previousScreen){
        super(new FillViewport(1280, 720));
        this.game = game;

        spacePanel = game.spaceScene.spacePanel;
        cam = (OrthographicCamera)spacePanel.getCamera();
        oldZoom = cam.zoom;
        cam.zoom = 25.0f;
        cam.update();

        // Controls
        this.addListener(new InputListener(){
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if(keycode == ControlSchema.OPEN_ORBITAL_MAP){
                    cam.zoom = oldZoom;
                    game.setScreen(previousScreen);
                    return true;
                }

                return false;
            }

            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY){
                cam.zoom = Math.min(Math.max(cam.zoom + (amountY), 20.0f), 150.0f);
                return true;
            }
        });
    }

    // Functions
    @Override
    public void act(float delta){
        spacePanel.act();
        // cam.position.set(game.planetScene.planetPanel.planet.getPosition(), 0);
        super.act(delta);
    }

    @Override
    public void draw(){
        spacePanel.draw();
        super.draw();
    }
    
    @Override
    public void dispose(){
        super.dispose();
    }

}