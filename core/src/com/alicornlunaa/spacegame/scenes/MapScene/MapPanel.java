package com.alicornlunaa.spacegame.scenes.MapScene;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Entity;
import com.alicornlunaa.spacegame.objects.Simulation.Celestial;
import com.alicornlunaa.spacegame.objects.Simulation.Universe;
import com.alicornlunaa.spacegame.objects.Simulation.Orbits.ConicSection;
import com.alicornlunaa.spacegame.objects.Simulation.Orbits.OrbitUtils;
import com.alicornlunaa.spacegame.objects.Simulation.Orbits.PatchedConicSolver;
import com.alicornlunaa.spacegame.scenes.SpaceScene.SpacePanel;
import com.alicornlunaa.spacegame.util.ControlSchema;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class MapPanel extends Stage {

    // Variables
    final App game;

    public final SpacePanel spacePanel;
    private final OrthographicCamera cam;
    private float oldZoom = 0.0f;

    private TextureRegion shipIcon;
    private Array<ConicSection> orbits = new Array<>();
    private Array<PatchedConicSolver> patchedConics = new Array<>();

    // Constructor
    public MapPanel(final App game, final Screen previousScreen){
        super(new ScreenViewport());
        this.game = game;

        spacePanel = game.spaceScene.spacePanel;
        cam = (OrthographicCamera)spacePanel.getCamera();
        oldZoom = cam.zoom;
        cam.zoom = 25.0f;
        cam.update();

        // Load textures
        shipIcon = game.atlas.findRegion("ui/ship_icon");

        // Create paths from entities
        Universe u = game.spaceScene.spacePanel.universe;
        for(Entity e : u.getEntities()){
            Celestial parent = u.getParentCelestial(e);

            if(parent != null && e.getDriving() == null){
                orbits.add(new ConicSection(parent, e));
                patchedConics.add(new PatchedConicSolver(u, e));
            }
        }
        for(Celestial c : u.getCelestials()){
            Celestial parent = u.getParentCelestial(c);

            if(parent != null){
                orbits.add(new ConicSection(parent, c));
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

        for(ConicSection o : orbits){
            o.calculate();
        }
        for(PatchedConicSolver cs : patchedConics){
            cs.recalculate();
        }

        super.act(delta);
    }

    @Override
    public void draw(){
        spacePanel.drawSkybox();

        Batch batch = getBatch();
        batch.begin();
        batch.setProjectionMatrix(cam.combined);
        batch.setTransformMatrix(new Matrix4());

        batch.end();
        game.shapeRenderer.setProjectionMatrix(cam.combined);
        game.shapeRenderer.begin(ShapeType.Filled);
        for(ConicSection o : orbits){
            o.draw(game.shapeRenderer);
        }
        for(PatchedConicSolver cs : patchedConics){
            cs.draw(game.shapeRenderer);
        }
        game.shapeRenderer.end();
        batch.begin();

        Vector2 size = new Vector2(1024, 1024);
        Vector2 plyPos = OrbitUtils.getUniverseSpacePosition(spacePanel.universe, game.player);
        batch.draw(
            shipIcon,
            plyPos.x - size.x / 2.f,
            plyPos.y - size.y / 2.f,
            size.x / 2.f,
            size.y / 2.f,
            size.x,
            size.y,
            1,
            1,
            game.player.getRotation()
        );

        batch.end();
        super.draw();

        spacePanel.draw();
    }
    
    @Override
    public void dispose(){
        super.dispose();
    }

}
