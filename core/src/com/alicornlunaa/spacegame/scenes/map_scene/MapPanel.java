package com.alicornlunaa.spacegame.scenes.map_scene;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.engine.core.BaseEntity;
import com.alicornlunaa.spacegame.engine.phys.PlanetaryPhysWorld;
import com.alicornlunaa.spacegame.engine.vfx.transitions.CameraZoomTransition;
import com.alicornlunaa.spacegame.objects.Player;
import com.alicornlunaa.spacegame.objects.Starfield;
import com.alicornlunaa.spacegame.objects.blocks.Tile;
import com.alicornlunaa.spacegame.objects.planet.Planet;
import com.alicornlunaa.spacegame.objects.simulation.Celestial;
import com.alicornlunaa.spacegame.objects.simulation.orbits.GenericConic;
import com.alicornlunaa.spacegame.objects.simulation.orbits.HyperbolicConic;
import com.alicornlunaa.spacegame.objects.simulation.orbits.Orbit;
import com.alicornlunaa.spacegame.objects.simulation.orbits.OrbitPropagator;
import com.alicornlunaa.spacegame.objects.simulation.orbits.OrbitUtils;
import com.alicornlunaa.spacegame.util.Constants;
import com.alicornlunaa.spacegame.util.ControlSchema;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class MapPanel extends Stage {

    // Variables
    private final App game;

    private OrthographicCamera mapCamera;
    private @Null BaseEntity targetEntity = null;
    
    private float celestialOpacity = 0.f;
    private float entityOpacity = 0.f;

    private TextureRegion shipIcon;
    private TextureRegion apoapsisMarkerTexture;
    private TextureRegion periapsisMarkerTexture;
    private Starfield backgroundTexture;

    private Array<GenericConic> orbits = new Array<>();
    private Array<Orbit> patchedConics = new Array<>();
    private Group markers = new Group();

    // Private functions
    private void initiatePaths(){
        for(BaseEntity e : game.universe.getEntities()){
            Celestial parent = game.universe.getParentCelestial(e);

            if(e instanceof Player && ((Player)e).isDriving()) continue;

            if(parent != null){
                patchedConics.add(new Orbit(game.universe, e));
            }
        }

        for(Celestial c : game.universe.getCelestials()){
            Celestial parent = game.universe.getParentCelestial(c);

            if(parent != null){
                orbits.add(OrbitPropagator.getConic(parent, c));
            }
        }
    }

    // Constructor
    public MapPanel(final App game, final Stage oldStage){
        super(new ScreenViewport());
        this.game = game;
        targetEntity = game.player.isDriving() ? game.player.getVehicle() : game.player;

        mapCamera = (OrthographicCamera)getCamera();
        mapCamera.zoom = 300.f;
        mapCamera.update();

        game.vfxManager.add(new CameraZoomTransition(mapCamera, game.activeCamera.zoom, mapCamera.zoom, 0.4f));

        // Load textures
        shipIcon = game.atlas.findRegion("ui/ship_icon");
        apoapsisMarkerTexture = game.atlas.findRegion("ui/apoapsis");
        periapsisMarkerTexture = game.atlas.findRegion("ui/periapsis");
        backgroundTexture = game.spaceScene.getContent().getStarfield();

        // Initializations
        initiatePaths();
        addActor(markers);

        // Controls
        this.addListener(new InputListener(){
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if(keycode == ControlSchema.OPEN_ORBITAL_MAP){
                    game.setScreen(game.activeSpaceScreen);
                    game.vfxManager.add(new CameraZoomTransition(game.activeCamera, mapCamera.zoom, game.activeCamera.zoom, 0.3f));
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
        game.universe.update(delta);

        mapCamera.position.set(OrbitUtils.getUniverseSpacePosition(game.universe, targetEntity), 0);
        mapCamera.update();

        // Keep the predicted paths up to date
        orbits.clear();
        markers.clear();

        for(Celestial c : game.universe.getCelestials()){
            Celestial parent = game.universe.getParentCelestial(c);

            if(parent != null){
                orbits.add(OrbitPropagator.getConic(parent, c));
            }
        }
        
        for(Orbit cs : patchedConics){
            cs.recalculate();

            // Add apoapsis and periapsis markers
            for(GenericConic conic : cs.getConics()){
                Celestial parent = conic.getParent();
                Vector2 apoapsis = conic.getPosition(Math.PI);
                Vector2 periapsis = conic.getPosition(0.0);

                if(conic.getEccentricity() <= 0.01f){
                    apoapsis.set((float)conic.getApoapsis(), 0);
                    periapsis.set((float)conic.getPeriapsis() * -1, 0);
                }

                markers.addActor(new Marker(game, parent, periapsis, periapsisMarkerTexture, 15.6f * getCamera().zoom, String.valueOf(Math.round(conic.getPeriapsis()))));

                if(!(conic instanceof HyperbolicConic)){
                    markers.addActor(new Marker(game, parent, apoapsis, apoapsisMarkerTexture, 15.6f * getCamera().zoom, String.valueOf(Math.round(conic.getApoapsis()))));
                }
            }
        }

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
        batch.setTransformMatrix(new Matrix4());

        for(BaseEntity e : game.simulation.getEntities()){
            if(e instanceof Celestial){
                batch.setTransformMatrix(new Matrix4().set(((Celestial)e).getUniverseSpaceTransform()));

                if(e instanceof Planet){
                    ((Planet)e).setStarDirection(new Vector3(OrbitUtils.directionToNearestStar(game.universe, e), 0));
                }
                
                e.render(batch);
            } else {
                Matrix4 mat = new Matrix4();

                Celestial parent = game.universe.getEntityParents().get(e);
                if(parent != null){
                    mat.set(parent.getUniverseSpaceTransform());

                    if(e.getWorld() instanceof PlanetaryPhysWorld){
                        mat.mul(new Matrix4().set(e.getTransform().inv()));
    
                        // Convert the planetary coords to space coords
                        double theta = ((e.getX() / (((Planet)parent).getTerrestrialWidth() * Tile.TILE_SIZE)) * Math.PI * 2);
                        float radius = ((e.getY() / (((Planet)parent).getTerrestrialHeight() * Tile.TILE_SIZE)) * ((Planet)parent).getRadius());
    
                        // Convet to space position
                        float x = (float)(Math.cos(theta) * radius);
                        float y = (float)(Math.sin(theta) * radius);
                        mat.translate(x, y, 0);
                    }
                }

                batch.setTransformMatrix(mat);
                e.render(batch);
            }
        }
    }

    @Override
    public void draw(){
        // Draw stars in the map view
        drawSkybox();

        // Get value dictating the opacity of simplified icons
        celestialOpacity = 1 - Math.min(Math.max(mapCamera.zoom / Constants.MAP_VIEW_SIMPLE_ICONS_CELESTIAL, 0), 1);
        entityOpacity = Math.min(Math.max(mapCamera.zoom / Constants.MAP_VIEW_SIMPLE_ICONS_ENTS, 0), 1);

        // Begin a shape drawing pass
        game.shapeRenderer.setProjectionMatrix(mapCamera.combined);
        game.shapeRenderer.begin(ShapeType.Filled);
        for(GenericConic o : orbits){
            o.draw(game.shapeRenderer, mapCamera.zoom);

            // Render a circle to the closest point on the orbit by taking the angle to the mouse
            // TODO: Mouse point on orbit
            // Vector2 mouseUniversalSpace = this.screenToStageCoordinates(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
            // Vector2 celestialUniversalSpace = o.getParent().getUniverseSpaceTransform().getTranslation(new Vector2());
            // float ang = (mouseUniversalSpace.cpy().sub(celestialUniversalSpace)).nor().angleRad();
            // Vector2 p = o.getPosition(o.trueAnomalyToMeanAnomaly(ang)).scl(Constants.PPM);
            
            // game.shapeRenderer.setTransformMatrix(new Matrix4());
            // game.shapeRenderer.circle(mouseUniversalSpace.x, mouseUniversalSpace.y, 5000);
            // game.shapeRenderer.circle(celestialUniversalSpace.x, celestialUniversalSpace.y, 5000);
            // game.shapeRenderer.rectLine(mouseUniversalSpace, celestialUniversalSpace, 500);

            // game.shapeRenderer.setTransformMatrix(new Matrix4().set(o.getParent().getUniverseSpaceTransform()));
            // game.shapeRenderer.circle(p.x, p.y, 5000);
        }
        for(Orbit cs : patchedConics){
            cs.draw(game.shapeRenderer, 1.5f * mapCamera.zoom);
        }
        game.shapeRenderer.setTransformMatrix(new Matrix4());
        game.shapeRenderer.end();

        // Begin a batch renderer pass
        Batch batch = getBatch();
        batch.setProjectionMatrix(mapCamera.combined);
        batch.setTransformMatrix(new Matrix4());
        batch.begin();
        batch.setColor(1, 1, 1, entityOpacity);

        Vector2 size = new Vector2(512, 512).scl(1.f / 20.f).scl(mapCamera.zoom);
        Vector2 plyPos = OrbitUtils.getUniverseSpacePosition(game.universe, game.player);
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
            (float)Math.toDegrees(game.player.getRotation())
        );

        batch.setColor(1, 1, 1, 1);
        drawUniverse(batch);

        batch.end();
        super.draw();

        if(celestialOpacity < 0.5){
            game.shapeRenderer.begin(ShapeType.Filled);
            for(GenericConic o : orbits){
                Color c = Color.CYAN;

                if(o.getChild() instanceof Planet){
                    c = ((Planet)o.getChild()).getAtmosphereColor();
                }

                game.shapeRenderer.setTransformMatrix(new Matrix4().set(((Celestial)o.getChild()).getUniverseSpaceTransform()));
                game.shapeRenderer.setColor(c);
                game.shapeRenderer.circle(0, 0, ((Celestial)o.getChild()).getRadius() * 3);
            }
            game.shapeRenderer.end();
        }
    }
    
    @Override
    public void dispose(){
        super.dispose();
    }

}
