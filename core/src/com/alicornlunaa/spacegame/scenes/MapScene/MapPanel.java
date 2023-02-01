package com.alicornlunaa.spacegame.scenes.MapScene;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Entity;
import com.alicornlunaa.spacegame.objects.Simulation.Celestial;
import com.alicornlunaa.spacegame.objects.Simulation.Universe;
import com.alicornlunaa.spacegame.objects.Simulation.Orbits.ConicSection;
import com.alicornlunaa.spacegame.objects.Simulation.Orbits.OrbitUtils;
import com.alicornlunaa.spacegame.objects.Simulation.Orbits.PatchedConicSolver;
import com.alicornlunaa.spacegame.scenes.SpaceScene.SpacePanel;
import com.alicornlunaa.spacegame.util.Constants;
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

    // Private functoins
    /**
     * Creates the paths to show the predicted location of something going
     */
    private void initiatePaths(){
        Universe u = game.spaceScene.spacePanel.universe;
        
        for(Entity e : u.getEntities()){
            Celestial parent = u.getParentCelestial(e);

            if(parent != null && e.getDriving() == null){
                patchedConics.add(new PatchedConicSolver(game, u, e));
            }
        }

        for(Celestial c : u.getCelestials()){
            Celestial parent = u.getParentCelestial(c);

            if(parent != null){
                orbits.add(new ConicSection(game, parent, c));
            }
        }
    }

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

        // Initializations
        initiatePaths();

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
                float speed = Constants.MAP_VIEW_ZOOM_SENSITIVITY * cam.zoom * amountY;
                cam.zoom = Math.min(Math.max(cam.zoom + speed, Constants.MAP_VIEW_MIN_ZOOM), Constants.MAP_VIEW_MAX_ZOOM);
                return true;
            }
        });
    }

    // Functions
    @Override
    public void act(float delta){
        spacePanel.act();

        // Keep the predicted paths up to date
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
        // Draw stars in the map view
        spacePanel.drawSkybox();

        // Begin a shape drawing pass
        game.shapeRenderer.setProjectionMatrix(cam.combined);
        game.shapeRenderer.begin(ShapeType.Filled);
        for(ConicSection o : orbits){
            o.draw(game.shapeRenderer);
        }
        for(PatchedConicSolver cs : patchedConics){
            cs.draw(game.shapeRenderer);
        }
        game.shapeRenderer.end();

        // Begin a batch renderer pass
        Batch batch = getBatch();
        batch.setProjectionMatrix(cam.combined);
        batch.setTransformMatrix(new Matrix4());
        batch.begin();

        Vector2 size = new Vector2(512, 512).scl(1.f / 20).scl(cam.zoom);
        Vector2 plyPos = OrbitUtils.getUniverseSpacePosition(spacePanel.universe, game.player);
        batch.setTransformMatrix(new Matrix4());
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
        spacePanel.draw(); // Draw planets
    }
    
    @Override
    public void dispose(){
        super.dispose();
    }

}
