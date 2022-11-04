package com.alicornlunaa.spacegame.objects;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.states.PlayerState;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * Player controller class, will handle inputs and other
 * player related events.
 */
public class Player extends Entity {
    
    // Variables
    public PlayerState state = new PlayerState();

    private Body body;
    private PolygonShape shape = new PolygonShape();

    private TextureRegionDrawable idleTexture;

    private static final float PLAYER_WIDTH = 8.0f;
    private static final float PLAYER_HEIGHT = 16.0f;
    private static final float MOVEMENT_SPEED = 1600.0f;

    // Constructor
    public Player(final App game, World world, float x, float y){
        BodyDef def = new BodyDef();
        def.type = BodyType.DynamicBody;
        def.position.set(x, y);
        body = world.createBody(def);

        shape.setAsBox(PLAYER_WIDTH / 2, PLAYER_HEIGHT / 2, new Vector2(PLAYER_WIDTH / 2, PLAYER_HEIGHT / 2), 0);
        body.createFixture(shape, 1.0f);
        body.setFixedRotation(true);

        setBounds(0, 0, PLAYER_WIDTH, PLAYER_HEIGHT);
        setOrigin(PLAYER_WIDTH / 2, PLAYER_HEIGHT / 2);
        setPosition(body.getPosition().x, body.getPosition().y);
        setRotation((float)Math.toDegrees(body.getAngle()));

        idleTexture = new TextureRegionDrawable(game.atlas.findRegion("player/idle"));
    }

    // Functions
    public Body getBody(){ return body; }

    @Override
    public void act(float delta){
        super.act(delta);

        if(state.vertical != 0 || state.horizontal != 0){
            body.applyLinearImpulse(new Vector2(state.horizontal, state.vertical).scl(MOVEMENT_SPEED), body.getWorldCenter(), true);
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha){
        super.draw(batch, parentAlpha);
        setPosition(body.getPosition().x, body.getPosition().y);
        setRotation((float)Math.toDegrees(body.getAngle()));

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
