package com.alicornlunaa.spacegame.objects;

import java.util.HashMap;

import com.alicornlunaa.selene_engine.components_old.BodyComponent;
import com.alicornlunaa.selene_engine.components_old.ScriptComponent;
import com.alicornlunaa.selene_engine.components_old.ShapeDrawableComponent;
import com.alicornlunaa.selene_engine.components_old.TransformComponent;
import com.alicornlunaa.selene_engine.core.BaseEntity;
import com.alicornlunaa.selene_engine.core.DriveableEntity;
import com.alicornlunaa.selene_engine.ecs.CameraComponent;
import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.alicornlunaa.space_game.App;
import com.alicornlunaa.spacegame.components.CustomSpriteComponent;
import com.alicornlunaa.spacegame.components.GravityComponent;
import com.alicornlunaa.spacegame.scripts.PlanetPhysScript;
import com.alicornlunaa.spacegame.util.Constants;
import com.alicornlunaa.spacegame.util.ControlSchema;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
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

    private @Null DriveableEntity vehicle = null;
    // private Vector3 cameraAngle = new Vector3(0, 1, 0);

    private RayCastCallback jumpCallback;
    private float animationTimer = 0.f;
    private State animationState = State.IDLE;
    private HashMap<State, Animation<TextureRegion>> animations = new HashMap<>();

    private void initializePhys(PhysWorld world, float x, float y){
        BodyDef def = new BodyDef();
        def.type = BodyType.DynamicBody;
        bodyComponent = addComponent(new BodyComponent(world, def));
        bodyComponent.body.setFixedRotation(true);

        transform.position.set(x, y);
        transform.dp.set(x, y);
        bodyComponent.sync(transform);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.friction = 0.1f;
        fixtureDef.restitution = 0.05f;
        fixtureDef.density = 1.4f;

        PolygonShape shape = new PolygonShape();
        float rad = PLAYER_WIDTH / 2.f - Constants.HITBOX_LINEUP_FACTOR;
        shape.setAsBox(
            PLAYER_WIDTH / 2 - Constants.HITBOX_LINEUP_FACTOR,
            PLAYER_HEIGHT / 2 - rad,
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

        cShape.setPosition(new Vector2(0, PLAYER_HEIGHT / 2 - rad));
        fixtureDef.shape = cShape;
        bodyComponent.body.createFixture(fixtureDef);
        
        cShape.setPosition(new Vector2(0, PLAYER_HEIGHT / -2 + rad));
        fixtureDef.friction = 1;
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
    public Player(PhysWorld world, float x, float y){
        super();

        initializePhys(world, x, y);
        initializeAnims();

        addComponent(new ScriptComponent(this) {
            @Override
            public void start(){}
            
            @Override
            public void update() {
                if(noclip) return;

                // Groundchecking
                grounded = false;
                bodyComponent.world.getBox2DWorld().rayCast(
                    jumpCallback,
                    bodyComponent.body.getWorldPoint(new Vector2(0, PLAYER_HEIGHT / -2)).cpy(),
                    bodyComponent.body.getWorldPoint(new Vector2(0, PLAYER_HEIGHT / -2 - 0.1f)).cpy()
                );

                // Movement
                if(vertical != 0 || horizontal != 0 && bodyComponent.body.getLinearVelocity().len() < 5){
                    bodyComponent.body.applyLinearImpulse(new Vector2(horizontal, grounded ? vertical : 0).scl(MOVEMENT_SPEED, JUMP_FORCE).scl(Constants.TIME_STEP), bodyComponent.body.getWorldCenter(), true);
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
            public void render() {
                // Noclip
                if(Gdx.input.isKeyJustPressed(ControlSchema.PLAYER_NOCLIP))
                    toggleNoclip();

                if(noclip){
                    bodyComponent.body.setLinearVelocity(0, 0);

                    if(Gdx.input.isKeyPressed(ControlSchema.PLAYER_UP)){
                        transform.position.y += (Gdx.input.isKeyPressed(ControlSchema.PLAYER_SPRINT) ? 1000 : 100) * Gdx.graphics.getDeltaTime();
                    } else if(Gdx.input.isKeyPressed(ControlSchema.PLAYER_DOWN)){
                        transform.position.y -= (Gdx.input.isKeyPressed(ControlSchema.PLAYER_SPRINT) ? 1000 : 100) * Gdx.graphics.getDeltaTime();
                    }
                    
                    if(Gdx.input.isKeyPressed(ControlSchema.PLAYER_RIGHT)){
                        transform.position.x += (Gdx.input.isKeyPressed(ControlSchema.PLAYER_SPRINT) ? 1000 : 100) * Gdx.graphics.getDeltaTime();
                    } else if(Gdx.input.isKeyPressed(ControlSchema.PLAYER_LEFT)){
                        transform.position.x -= (Gdx.input.isKeyPressed(ControlSchema.PLAYER_SPRINT) ? 1000 : 100) * Gdx.graphics.getDeltaTime();
                    }

                    bodyComponent.sync(transform);
                    return;
                }
            }
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
        addComponent(new ShapeDrawableComponent(this) {
            @Override
            public void draw(ShapeRenderer renderer) {
                Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0.f);
                mouse.set(App.instance.camera.unproject(mouse));
                mouse.set((int)(mouse.x / Constants.TILE_SIZE) * Constants.TILE_SIZE, (int)(mouse.y / Constants.TILE_SIZE) * Constants.TILE_SIZE, 0.f);
                
                renderer.set(ShapeType.Line);
                renderer.rect(mouse.x, mouse.y, Constants.TILE_SIZE, Constants.TILE_SIZE);
            }
        });
        addComponent(new CameraComponent(1280, 720));
        addComponent(new PlanetPhysScript(this));
        addComponent(new GravityComponent(this, false));
    }

    // Functions
    public boolean isDriving(){ return (vehicle != null); }

    public DriveableEntity getVehicle(){ return vehicle; }

    public void setVehicle(DriveableEntity de){ vehicle = de; }

    public void toggleNoclip(){
        // fuck krentist
        noclip = !noclip;
        bodyComponent.body.setType(noclip ? BodyType.KinematicBody : BodyType.DynamicBody);
        bodyComponent.body.setActive(!noclip);
        Gdx.app.log("Noclip", noclip ? "Enabled" : "Disabled");
    }

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
        if(isDriving()) return vehicle.getComponent(BodyComponent.class).body.getLinearVelocity().cpy();
        return bodyComponent.body.getLinearVelocity().cpy();
    }
}
