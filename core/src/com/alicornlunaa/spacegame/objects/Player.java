package com.alicornlunaa.spacegame.objects;

import java.util.HashMap;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.engine.core.BaseEntity;
import com.alicornlunaa.spacegame.engine.core.DriveableEntity;
import com.alicornlunaa.spacegame.engine.phys.CelestialPhysWorld;
import com.alicornlunaa.spacegame.engine.phys.PhysWorld;
import com.alicornlunaa.spacegame.engine.phys.PlanetaryPhysWorld;
import com.alicornlunaa.spacegame.objects.simulation.Celestial;
import com.alicornlunaa.spacegame.objects.simulation.orbits.OrbitUtils;
import com.alicornlunaa.spacegame.scenes.map_scene.MapScene;
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
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
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
    private Vector3 cameraAngle = new Vector3();

    private RayCastCallback jumpCallback;
    private float animationTimer = 0.f;
    private State animationState = State.IDLE;
    private HashMap<State, Animation<TextureRegion>> animations = new HashMap<>();

    private static final float PLAYER_WIDTH = 8.0f;
    private static final float PLAYER_HEIGHT = 16.0f;
    private static final float MOVEMENT_SPEED = 0.5f;
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
    public Player(final App game, PhysWorld world, float x, float y, float physScale){
        this.game = game;

        // TODO: Reimplement
        // setBounds(0, 0, PLAYER_WIDTH, PLAYER_HEIGHT);
        // setOrigin(PLAYER_WIDTH / 2, PLAYER_HEIGHT / 2);
        // setPhysScale(physScale);

        BodyDef def = new BodyDef();
        def.type = BodyType.DynamicBody;
        def.position.set(0, 0);
        setBody(world.getBox2DWorld().createBody(def));
        setWorld(world);
        setPosition(x, y);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(
            PLAYER_WIDTH / 2 / getPhysScale(),
            PLAYER_HEIGHT / 2 / getPhysScale(),
            new Vector2(
                0,
                0
            ),
            0
        );
        getBody().createFixture(shape, 1.0f);
        getBody().setFixedRotation(true);
        shape.dispose();

        jumpCallback = new RayCastCallback(){
            @Override
            public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction){
                grounded = true;
                return fraction;
            }
        };

        initializeAnims();
    }

    // Functions
    public boolean isDriving(){ return (vehicle != null); }

    public DriveableEntity getVehicle(){ return vehicle; }

    public void setVehicle(DriveableEntity de){ vehicle = de; }

    public void updateCamera(OrthographicCamera cam, boolean localToPhysWorld){
        BaseEntity e = (vehicle == null) ? this : vehicle;
        Celestial c = game.universe.getParentCelestial(e);
        Vector2 pos = (e.getBody() != null) ? e.getBody().getWorldCenter().cpy().scl(getPhysScale()) : e.getPosition();
        cameraAngle.set(pos.cpy().nor(), 0);

        if((e.getWorld() instanceof PlanetaryPhysWorld) || c == null || !OrbitUtils.isOrbitDecaying(c, e)){
            cameraAngle.set(0, 1, 0);
        }

        if(!localToPhysWorld){
            pos = OrbitUtils.getUniverseSpacePosition(game.universe, this);
        }

        cam.up.interpolate(cameraAngle, 0.25f, Interpolation.circle);
        cam.position.set(pos, 0);
        cam.update();
    }

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
            vehicle.update();
            setPosition(vehicle.getPosition());
        }
    }

    @Override
    public void render(Batch batch){
        if(vehicle != null) return;

        animationTimer += Gdx.graphics.getDeltaTime();

        Matrix3 transform = getTransform();
        batch.setTransformMatrix(batch.getTransformMatrix().mul(new Matrix4().set(transform)));

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

        batch.setTransformMatrix(batch.getTransformMatrix().mul(new Matrix4().set(transform.inv())));
    }

    // Overrides
    @Override
    public void afterWorldChange(PhysWorld world){
        // Change scenes depending on world
        if(game.getScreen() instanceof MapScene) return;

        if(world instanceof CelestialPhysWorld || world instanceof PlanetaryPhysWorld){
            game.setScreen(game.activeSpaceScreen);
        }
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
