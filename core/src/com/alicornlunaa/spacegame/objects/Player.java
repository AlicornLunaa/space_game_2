package com.alicornlunaa.spacegame.objects;

import java.util.HashMap;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.engine.core.BaseEntity;
import com.alicornlunaa.spacegame.engine.core.DriveableEntity;
import com.alicornlunaa.spacegame.engine.phys.CelestialPhysWorld;
import com.alicornlunaa.spacegame.engine.phys.PhysWorld;
import com.alicornlunaa.spacegame.engine.phys.PlanetaryPhysWorld;
import com.alicornlunaa.spacegame.objects.planet.Planet;
import com.alicornlunaa.spacegame.objects.simulation.Celestial;
import com.alicornlunaa.spacegame.objects.simulation.orbits.OrbitUtils;
import com.alicornlunaa.spacegame.scenes.map_scene.MapScene;
import com.alicornlunaa.spacegame.scenes.planet_scene.PlanetScene;
import com.alicornlunaa.spacegame.util.Constants;
import com.alicornlunaa.spacegame.util.ControlSchema;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;

/**
 * Player controller class, will handle inputs and other
 * player related events.
 */
public class Player extends BaseEntity {

    // Enums
    public enum State { IDLE, MOVE_LEFT, MOVE_RIGHT, JUMP };

    // Variables
    private final App game;

    private float vertical = 0.0f;
    private float horizontal = 0.0f;
    private boolean grounded = false;

    private @Null DriveableEntity vehicle = null;
    private OrthographicCamera camera;
    private Vector3 cameraAngle = new Vector3(0, 1, 0);

    private RayCastCallback jumpCallback;
    private float animationTimer = 0.f;
    private State animationState = State.IDLE;
    private HashMap<State, Animation<TextureRegion>> animations = new HashMap<>();

    private static final float PLAYER_WIDTH = 8.0f;
    private static final float PLAYER_HEIGHT = 16.0f;
    private static final float MOVEMENT_SPEED = 0.05f;
    private static final float JUMP_FORCE = 1.0f;

    // Private functions
    private Array<TextureRegion> getTextureRegions(String path){
        Array<TextureRegion> out = new Array<>();
        Array<AtlasRegion> arr = game.atlas.findRegions(path);

        for(AtlasRegion a : arr){
            out.add(new TextureRegion(a));
        }

        return out;
    }

    private void initializePhys(PhysWorld world, float x, float y){
        BodyDef def = new BodyDef();
        def.type = BodyType.DynamicBody;
        def.position.set(0, 0);
        setBody(world.getBox2DWorld().createBody(def));
        setWorld(world);
        setPosition(x, y);
        getBody().setFixedRotation(true);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.friction = 0.1f;
        fixtureDef.restitution = 0.05f;
        fixtureDef.density = 1.4f;

        PolygonShape shape = new PolygonShape();
        float rad = PLAYER_WIDTH / 2.f / getPhysScale();
        shape.setAsBox(
            PLAYER_WIDTH / 2 / getPhysScale(),
            PLAYER_HEIGHT / 2 / getPhysScale() - rad,
            new Vector2(
                0,
                0
            ),
            0
        );
        fixtureDef.shape = shape;
        getBody().createFixture(fixtureDef);
        shape.dispose();

        CircleShape cShape = new CircleShape();
        cShape.setRadius(rad);
        cShape.setPosition(new Vector2(0, PLAYER_HEIGHT / 2 / getPhysScale() - rad));
        fixtureDef.shape = cShape;
        getBody().createFixture(fixtureDef);
        cShape.setPosition(new Vector2(0, PLAYER_HEIGHT / -2 / getPhysScale() + rad));
        getBody().createFixture(fixtureDef);
        cShape.dispose();

        jumpCallback = new RayCastCallback(){
            @Override
            public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction){
                grounded = true;
                return fraction;
            }
        };
    }

    private void initializeAnims(){
        animations.put(State.IDLE, new Animation<>(1 / 12.f, getTextureRegions("player/idle"), PlayMode.LOOP));
        animations.put(State.MOVE_LEFT, new Animation<>(1 / 12.f, getTextureRegions("player/move_left"), PlayMode.LOOP));
        animations.put(State.MOVE_RIGHT, new Animation<>(1 / 12.f, getTextureRegions("player/move_right"), PlayMode.LOOP));
    }

    private void resolveAnimState(){
        if(horizontal == 0){
            animationState = State.IDLE;
        } else if(horizontal == 1){
            animationState = State.MOVE_RIGHT;
        } else if(horizontal == -1){
            animationState = State.MOVE_LEFT;
        }
    }

    // Constructor
    public Player(final App game, PhysWorld world, float x, float y){
        this.game = game;

        camera = new OrthographicCamera(1280, 720);
        game.activeCamera = camera;

        initializePhys(world, x, y);
        initializeAnims();
    }

    // Functions
    public boolean isDriving(){ return (vehicle != null); }

    public DriveableEntity getVehicle(){ return vehicle; }

    public void setVehicle(DriveableEntity de){ vehicle = de; }

    public OrthographicCamera getCamera(){ return camera; }

    public void updateCamera(boolean instant){
        // Update the camera positioning in order to be relative to the player
        BaseEntity ent = (vehicle == null) ? this : vehicle;
        Celestial c = game.universe.getParentCelestial(ent);
        Vector2 pos = ent.getCenter();

        if((ent.getWorld() instanceof PlanetaryPhysWorld) || c == null || !OrbitUtils.isOrbitDecaying(c, ent)){
            // Set the desired camera angle to upright if the orbit is not decaying
            cameraAngle.set(0, 1, 0);
        } else {
            cameraAngle.set(pos.cpy().nor(), 0);
        }

        if(!(ent.getWorld() instanceof PlanetaryPhysWorld)){
            // If the player is on a planet, use the universe-based position system
            pos.set(OrbitUtils.getUniverseSpaceCenter(game.universe, ent));
        }

        if(instant){
            camera.up.set(cameraAngle);
        } else {
            camera.up.interpolate(cameraAngle, 0.25f, Interpolation.circle);
        }

        camera.position.set(pos, 0);
        camera.update();
    }

    public void updateCamera(){ updateCamera(false); }

    @Override
    public void update(){
        // Groundchecking
        grounded = false;
        getWorld().getBox2DWorld().rayCast(jumpCallback, getBody().getWorldCenter(), getBody().getWorldPoint(new Vector2(0, -1 * (PLAYER_HEIGHT / 2 + 4.5f) / getPhysScale())));
        grounded = true;

        // Movement
        if(vertical != 0 || horizontal != 0){
            getBody().applyLinearImpulse(new Vector2(horizontal, grounded ? vertical : 0).scl(MOVEMENT_SPEED, JUMP_FORCE).scl(Constants.TIME_STEP).scl(128.f / this.getPhysScale() * 1.f), getBody().getWorldCenter(), true);
        }

        // Controls
        if(vehicle == null){
            // Controls for player
            if(Gdx.input.isKeyPressed(ControlSchema.PLAYER_UP)){
                vertical = 1;
            } else if(Gdx.input.isKeyPressed(ControlSchema.PLAYER_DOWN)){
                vertical = -1;
            } else {
                vertical = 0;
            }
            
            if(Gdx.input.isKeyPressed(ControlSchema.PLAYER_RIGHT)){
                horizontal = 1;
            } else if(Gdx.input.isKeyPressed(ControlSchema.PLAYER_LEFT)){
                horizontal = -1;
            } else {
                horizontal = 0;
            }

            resolveAnimState();
        } else {
            setPosition(vehicle.getPosition());
        }

        // Parent camera to the player's position
        updateCamera();
    }

    @Override
    public void render(Batch batch){
        // Dont render if player is driving something
        if(vehicle != null) return;

        animationTimer += Gdx.graphics.getDeltaTime();

        Matrix4 oldTrans = batch.getTransformMatrix().cpy();
        batch.setTransformMatrix(batch.getTransformMatrix().mul(new Matrix4().set(getTransform())));

        Animation<TextureRegion> curAnimation = animations.get(animationState);
        TextureRegion animFrame = curAnimation.getKeyFrame(animationTimer);
        batch.draw(
            animFrame,
            -PLAYER_WIDTH / 2, -PLAYER_HEIGHT / 2,
            0, 0,
            PLAYER_WIDTH, PLAYER_HEIGHT,
            1, 1,
            0
        );

        batch.setTransformMatrix(oldTrans);
    }

    // Overrides
    @Override
    public void afterWorldChange(PhysWorld world){
        // Change scenes depending on world
        if(game.getScreen() instanceof MapScene) return;
        
        if(world instanceof PlanetaryPhysWorld && !(game.activeSpaceScreen instanceof PlanetScene)){
            game.activeSpaceScreen = new PlanetScene(game, (Planet)game.universe.getParentCelestial(game.player));
        } else if(world instanceof CelestialPhysWorld && !(game.activeSpaceScreen instanceof CelestialPhysWorld)){
            game.activeSpaceScreen = game.spaceScene;
        }

        if(world instanceof CelestialPhysWorld || world instanceof PlanetaryPhysWorld){
            game.setScreen(game.activeSpaceScreen);
            updateCamera(true);
        }
    }

    @Override
    public Vector2 getCenter(){
        if(isDriving()) return vehicle.getCenter();
        return super.getCenter();
    }

    @Override
    public Vector2 getPosition(){
        if(isDriving()) return vehicle.getCenter();
        return super.getPosition();
    }

    @Override
    public float getRotation(){
        if(isDriving()) return vehicle.getRotation();
        return super.getRotation();
    }

    @Override
    public Vector2 getVelocity(){
        if(isDriving()) return vehicle.getVelocity();
        return super.getVelocity();
    }

}
