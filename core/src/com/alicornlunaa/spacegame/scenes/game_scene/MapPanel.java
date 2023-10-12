package com.alicornlunaa.spacegame.scenes.game_scene;

import com.alicornlunaa.selene_engine.core.BaseEntity;
import com.alicornlunaa.selene_engine.vfx.transitions.CameraZoomTransition;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Starfield;
import com.alicornlunaa.spacegame.objects.simulation.orbits.Orbit;
import com.alicornlunaa.spacegame.util.Constants;
import com.alicornlunaa.spacegame.util.ControlSchema;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class MapPanel extends Stage {

    // Variables
    private final App game;

    private OrthographicCamera mapCamera;
    private OrthographicCamera oldCamera;
    private @Null BaseEntity targetEntity = null;
    
    private float entityOpacity = 0.f;
    private Orbit orbit;

    private TextureRegion shipIcon;
    // private TextureRegion apoapsisMarkerTexture;
    // private TextureRegion periapsisMarkerTexture;
    private Starfield backgroundTexture;

    private Group markers = new Group();

    // Constructor
    public MapPanel(final App game, final Stage oldStage){
        super(new ScreenViewport());
        this.game = game;
        targetEntity = game.gameScene.player.isDriving() ? game.gameScene.player.getVehicle() : game.gameScene.player;

        getViewport().setCamera(game.camera);
        mapCamera = (OrthographicCamera)getCamera();
        mapCamera.zoom = 300.f;
        mapCamera.update();

        oldCamera = game.camera;
        game.camera = mapCamera;

        game.vfxManager.add(new CameraZoomTransition(mapCamera, game.camera.zoom, mapCamera.zoom, 0.4f));
        // game.gameScene.orbitSystem.visible = true;

        orbit = new Orbit(game.gameScene.universe, game.gameScene.player);

        // Load textures
        shipIcon = game.atlas.findRegion("ui/ship_icon");
        // apoapsisMarkerTexture = game.atlas.findRegion("ui/apoapsis");
        // periapsisMarkerTexture = game.atlas.findRegion("ui/periapsis");
        backgroundTexture = game.gameScene.spacePanel.getStarfield();

        // Initializations
        addActor(game.gameScene.universe);
        addActor(markers);

        // Controls
        this.addListener(new InputListener(){
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if(keycode == ControlSchema.OPEN_ORBITAL_MAP){
                    game.gameScene.closeMap();
                    game.camera = oldCamera;
                    // game.gameScene.orbitSystem.visible = false;
                    return true;
                }

                return false;
            }

            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY){
                float speed = Constants.MAP_VIEW_ZOOM_SENSITIVITY * mapCamera.zoom * amountY;
                mapCamera.zoom = Math.min(Math.max(mapCamera.zoom + speed, Constants.MAP_VIEW_MIN_ZOOM), Constants.MAP_VIEW_MAX_ZOOM);
                return true;
            }
        });
    }

    // Functions
    @Override
    public OrthographicCamera getCamera(){
        return (OrthographicCamera)super.getCamera();
    }

    @Override
    public void act(float delta){
        // Update the universe

        // Keep the predicted paths up to date
        markers.clear();
        
        // for(Orbit cs : patchedConics){
        //     cs.recalculate();

        //     // Add apoapsis and periapsis markers
        //     for(GenericConic conic : cs.getConics()){
        //         Celestial parent = conic.getParent();
        //         Vector2 apoapsis = conic.getPosition(Math.PI);
        //         Vector2 periapsis = conic.getPosition(0.0);

        //         if(conic.getEccentricity() <= 0.01f){
        //             apoapsis.set((float)conic.getApoapsis(), 0);
        //             periapsis.set((float)conic.getPeriapsis() * -1, 0);
        //         }

        //         markers.addActor(new Marker(game, parent, periapsis, periapsisMarkerTexture, 15.6f * getCamera().zoom, String.valueOf(Math.round(conic.getPeriapsis()))));

        //         if(!(conic instanceof HyperbolicConic)){
        //             markers.addActor(new Marker(game, parent, apoapsis, apoapsisMarkerTexture, 15.6f * getCamera().zoom, String.valueOf(Math.round(conic.getApoapsis()))));
        //         }
        //     }
        // }

        super.act(delta);
    }

    public void drawSkybox(){
        Batch batch = getBatch();
        Matrix4 oldProj = batch.getProjectionMatrix().cpy();
        Matrix4 oldTrans = batch.getTransformMatrix().cpy();

        batch.begin();
        batch.setProjectionMatrix(new Matrix4());
        batch.setTransformMatrix(new Matrix4());
        backgroundTexture.setOffset(getCamera().position.x / 10000000, getCamera().position.y / 10000000);
        backgroundTexture.draw(batch, -1, -1, 2, 2);
        batch.setProjectionMatrix(oldProj);
        batch.setTransformMatrix(oldTrans);
        batch.end();
    }

    public void drawUniverse(Batch batch){
        game.gameScene.registry.render();
    }

    @Override
    public void draw(){
        
        // Draw stars in the map view
        drawSkybox();

        // Get value dictating the opacity of simplified icons
        // celestialOpacity = 1 - Math.min(Math.max(mapCamera.zoom / Constants.MAP_VIEW_SIMPLE_ICONS_CELESTIAL, 0), 1);
        entityOpacity = Math.min(Math.max(mapCamera.zoom / Constants.MAP_VIEW_SIMPLE_ICONS_ENTS, 0), 1);

        // Begin a batch renderer pass
        Batch batch = getBatch();
        batch.setProjectionMatrix(mapCamera.combined);
        batch.setTransformMatrix(new Matrix4());
        batch.begin();
        batch.setColor(1, 1, 1, entityOpacity);

        drawUniverse(batch);

        Vector2 size = new Vector2(512, 512).scl(1.f / 20.f).scl(mapCamera.zoom);
        batch.setTransformMatrix(new Matrix4());
        batch.draw(
            shipIcon,
            game.camera.position.x - size.x / 2.f,
            game.camera.position.y - size.y / 2.f,
            size.x / 2.f,
            size.y / 2.f,
            size.x,
            size.y,
            1,
            1,
            (float)Math.toDegrees(game.gameScene.player.getRotation())
        );

        batch.setColor(1, 1, 1, 1);
        batch.end();

        App.instance.shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        App.instance.shapeRenderer.setTransformMatrix(batch.getTransformMatrix());
        App.instance.shapeRenderer.begin(ShapeType.Filled);
        orbit.draw(App.instance.shapeRenderer, 5000);
        orbit.recalculate();
        App.instance.shapeRenderer.end();
        
        super.draw();
    }
    
    @Override
    public void dispose(){
        super.dispose();
    }

}
