package com.alicornlunaa.spacegame.objects;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.states.PlayerState;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * Player controller class, will handle inputs and other
 * player related events.
 */
public class Player extends Entity {

    // Variables
    private final World world;

    public PlayerState state = new PlayerState();

    private PolygonShape shape = new PolygonShape();
    private RayCastCallback jumpCallback;
    private TextureRegionDrawable idleTexture;

    private static final float PLAYER_WIDTH = 8.0f;
    private static final float PLAYER_HEIGHT = 16.0f;
    private static final float MOVEMENT_SPEED = 5500.0f;
    private static final float JUMP_FORCE = 30000.0f;

    // Constructor
    public Player(final App game, World world, float x, float y, float physScale){
        this.world = world;

        setBounds(0, 0, PLAYER_WIDTH, PLAYER_HEIGHT);
        setOrigin(PLAYER_WIDTH / 2, PLAYER_HEIGHT / 2);
        setPhysScale(physScale);

        BodyDef def = new BodyDef();
        def.type = BodyType.DynamicBody;
        def.position.set(x, y);
        setBody(world.createBody(def));

        shape.setAsBox(
            PLAYER_WIDTH / 2 / getPhysScale(),
            PLAYER_HEIGHT / 2 / getPhysScale(),
            new Vector2(
                PLAYER_WIDTH / 2 / getPhysScale(),
                PLAYER_HEIGHT / 2 / getPhysScale()
            ),
            0
        );
        body.createFixture(shape, 1.0f);
        body.setFixedRotation(true);

        jumpCallback = new RayCastCallback(){
            @Override
            public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction){
                state.grounded = true;
                return fraction;
            }
        };

        idleTexture = new TextureRegionDrawable(game.atlas.findRegion("player/idle"));
    }

    // Functions
    @Override
    public void act(float delta){
        super.act(delta);

        state.grounded = false;
        world.rayCast(jumpCallback, getBody().getWorldCenter(), getBody().getWorldPoint(new Vector2(0, -1 * (getHeight() / 2) / getPhysScale())));

        if(state.vertical != 0 || state.horizontal != 0){
            body.applyLinearImpulse(new Vector2(state.horizontal, state.grounded ? state.vertical : 0).scl(MOVEMENT_SPEED, JUMP_FORCE).scl(delta), body.getWorldCenter(), true);
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha){
        super.draw(batch, parentAlpha);

        Matrix3 transform = getTransform();
        batch.setTransformMatrix(batch.getTransformMatrix().mul(new Matrix4().set(transform)));

        idleTexture.draw(batch, 0, 0, 0, 0, getWidth(), getHeight(), 1, 1, 0);
        
        batch.setTransformMatrix(batch.getTransformMatrix().mul(new Matrix4().set(transform.inv())));
    }

    @Override
    public boolean remove(){
        shape.dispose();
        return super.remove();
    }

}
