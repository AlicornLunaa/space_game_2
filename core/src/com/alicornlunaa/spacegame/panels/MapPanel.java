package com.alicornlunaa.spacegame.panels;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Entity;
import com.alicornlunaa.spacegame.objects.OrbitPath;
import com.alicornlunaa.spacegame.objects.Simulation.Celestial;
import com.alicornlunaa.spacegame.objects.Simulation.Universe;
import com.alicornlunaa.spacegame.util.ControlSchema;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FillViewport;

public class MapPanel extends Stage {

    // Variables
    final App game;

    private final SpacePanel spacePanel;
    private final OrthographicCamera cam;
    private float oldZoom = 0.0f;

    private Array<OrbitPath> orbits = new Array<>();

    // Constructor
    public MapPanel(final App game, final Screen previousScreen){
        super(new FillViewport(1280, 720));
        this.game = game;

        spacePanel = game.spaceScene.spacePanel;
        cam = (OrthographicCamera)spacePanel.getCamera();
        oldZoom = cam.zoom;
        cam.zoom = 25.0f;
        cam.update();

        // Create paths from entities
        Universe u = game.spaceScene.spacePanel.universe;
        for(Entity e : u.getEntities()){
            Celestial parent = u.getParentCelestial(e);

            if(parent != null){
                orbits.add(new OrbitPath(game, u, parent, e));
            }
        }
        for(Celestial c : u.getCelestials()){
            Celestial parent = u.getParentCelestial(c);

            if(parent != null){
                orbits.add(new OrbitPath(game, u, parent, c));
            }
        }

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
                cam.zoom = Math.min(Math.max(cam.zoom + (amountY * 5), 20.0f), 1500.0f);
                return true;
            }
        });
    }

    // Functions
    @Override
    public void act(float delta){
        spacePanel.act();

        for(OrbitPath o : orbits){
            o.simulate(2048);
            // o.recalculate();
        }

        super.act(delta);
    }

    @Override
    public void draw(){
        spacePanel.draw();

        Batch batch = getBatch();
        batch.begin();
        batch.setProjectionMatrix(cam.combined);
        batch.setTransformMatrix(new Matrix4());

        for(OrbitPath o : orbits){
            o.draw(batch);
        }

        batch.end();
        super.draw();
    }
    
    @Override
    public void dispose(){
        super.dispose();
    }

}
