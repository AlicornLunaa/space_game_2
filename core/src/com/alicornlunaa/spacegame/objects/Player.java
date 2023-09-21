package com.alicornlunaa.spacegame.objects;

import java.util.HashMap;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.CameraComponent;
import com.alicornlunaa.selene_engine.components.IScriptComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.core.BaseEntity;
import com.alicornlunaa.selene_engine.core.DriveableEntity;
import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.components.CustomSpriteComponent;
import com.alicornlunaa.spacegame.components.OrbitComponent;
import com.alicornlunaa.spacegame.components.SimulatedPathScript;
import com.alicornlunaa.spacegame.scripts.GravityScript;
import com.alicornlunaa.spacegame.scripts.PlanetPhysScript;
import com.alicornlunaa.spacegame.util.Constants;
import com.alicornlunaa.spacegame.util.ControlSchema;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Vector2;
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
    public TransformComponent transform = getComponent(TransformComponent.class);
    public BodyComponent bodyComponent;
    public OrbitComponent orbitComponent;

    private float vertical = 0.0f;
    private float horizontal = 0.0f;
    private boolean grounded = false;

    private @Null DriveableEntity vehicle = null;
    // private Vector3 cameraAngle = new Vector3(0, 1, 0);

    private RayCastCallback jumpCallback;
    private float animationTimer = 0.f;
    private State animationState = State.IDLE;
    private HashMap<State, Animation<TextureRegion>> animations = new HashMap<>();

    private static final float PLAYER_WIDTH = 8.0f;
    private static final float PLAYER_HEIGHT = 16.0f;
    private static final float MOVEMENT_SPEED = 0.05f;//1.f;
    private static final float JUMP_FORCE = 0.05f;//1.25f;

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
        bodyComponent = addComponent(new BodyComponent(world, def){
            @Override
            public void afterWorldChange(PhysWorld world){
                
            }
        });
        // addComponent(new GravityScript(game, this));
        addComponent(new PlanetPhysScript(this));

        float ppm = bodyComponent.world.getPhysScale();
        bodyComponent.body.setFixedRotation(true);
        bodyComponent.body.setTransform(x / ppm, y / ppm, bodyComponent.body.getAngle());
        transform.position.set(x, y);
        transform.dp.set(x, y);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.friction = 0.1f;
        fixtureDef.restitution = 0.05f;
        fixtureDef.density = 1.4f;

        PolygonShape shape = new PolygonShape();
        float rad = PLAYER_WIDTH / 2.f / ppm;
        shape.setAsBox(
            PLAYER_WIDTH / 2 / ppm,
            PLAYER_HEIGHT / 2 / ppm - rad,
            new Vector2(
                0,
                0
            ),
            0
        );
        fixtureDef.shape = shape;
        bodyComponent.body.createFixture(fixtureDef);
        shape.dispose();

        CircleShape cShape = new CircleShape();
        cShape.setRadius(rad);
        cShape.setPosition(new Vector2(0, PLAYER_HEIGHT / 2 / ppm - rad));
        fixtureDef.shape = cShape;
        bodyComponent.body.createFixture(fixtureDef);
        cShape.setPosition(new Vector2(0, PLAYER_HEIGHT / -2 / ppm + rad));
        bodyComponent.body.createFixture(fixtureDef);
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
        super();
        this.game = game;

        initializePhys(world, x, y);
        initializeAnims();

        addComponent(new IScriptComponent() {
            @Override
            public void start(){}
            
            @Override
            public void update() {
                // Groundchecking
                grounded = true;
                bodyComponent.world.getBox2DWorld().rayCast(jumpCallback, bodyComponent.body.getWorldPoint(new Vector2(0, PLAYER_HEIGHT / -2 + 1.f).scl(1 / bodyComponent.world.getPhysScale())).cpy(), bodyComponent.body.getWorldPoint(new Vector2(0, PLAYER_HEIGHT / -2 - 1.5f).scl(1 / bodyComponent.world.getPhysScale())));

                // Movement
                if(vertical != 0 || horizontal != 0){
                    bodyComponent.body.applyLinearImpulse(new Vector2(horizontal, grounded ? vertical : 0).scl(MOVEMENT_SPEED, JUMP_FORCE).scl(Constants.TIME_STEP).scl(128.f / bodyComponent.world.getPhysScale() * 1.f), bodyComponent.body.getWorldCenter(), true);
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
                    transform.position.set(vehicle.getComponent(TransformComponent.class).position);
                }
            }

            @Override
            public void render() {}
        });
    
        addComponent(new CustomSpriteComponent() {
            @Override
            public void render(Batch batch) {
                // Dont render if player is driving something
                if(vehicle != null) return;

                // Advance animation
                animationTimer += Gdx.graphics.getDeltaTime();

                // Render to screen
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
            }
        });

        addComponent(new CameraComponent(1280, 720));
        addComponent(new SimulatedPathScript());
        
        // orbitComponent = addComponent(new OrbitComponent(game.gameScene.universe, this));
    }

    // Functions
    public boolean isDriving(){ return (vehicle != null); }

    public DriveableEntity getVehicle(){ return vehicle; }

    public void setVehicle(DriveableEntity de){ vehicle = de; }

    // Overrides
    public Vector2 getCenter(){
        if(isDriving()) return vehicle.getComponent(TransformComponent.class).position.cpy();
        return transform.position.cpy();
    }

    public Vector2 getPosition(){
        if(isDriving()) return vehicle.getComponent(TransformComponent.class).position.cpy();
        return transform.position.cpy();
    }

    public float getRotation(){
        if(isDriving()) return vehicle.getComponent(TransformComponent.class).rotation;
        return transform.rotation;
    }

    public Vector2 getVelocity(){
        if(isDriving()) return vehicle.getComponent(TransformComponent.class).velocity.cpy();
        return transform.velocity.cpy();
    }
}
