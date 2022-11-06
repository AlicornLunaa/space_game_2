package com.alicornlunaa.spacegame.objects.Planet;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Entity;
import com.alicornlunaa.spacegame.states.PlanetState;
import com.alicornlunaa.spacegame.util.OpenSimplexNoise;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class SpacePlanet extends Entity {

    // Static
    public final static float GRAVITY_CONSTANT = 8000.0f;

    // Variables
    private PlanetState stateRef;
    private CircleShape shape = new CircleShape();

    private final OpenSimplexNoise noise;
    
    private Pixmap map;
    private Texture texture;
    private TextureRegionDrawable sprite;

    // Private functions
    private void generateSprite(){
        map = new Pixmap(Math.min((int)stateRef.radius * 2, 2000), Math.min((int)stateRef.radius * 2, 2000), Format.RGBA8888);

        for(int x = 0; x < map.getWidth(); x++){
            for(int y = 0; y < map.getHeight(); y++){
                int mX = x - map.getWidth() / 2;
                int mY = y - map.getHeight() / 2;
                int radSqr = mX * mX + mY * mY;
                float rand = (float)noise.eval(mX * 0.1f, mY * 0.1f);
                
                Color c = new Color(0.72f, 0.7f, 0.9f, 1);

                if(radSqr > (float)Math.pow(map.getWidth() / 2, 2)) c.a = 0;
                if(rand < 0){ c.mul(0.9f); c.a *= 1.1; }

                map.setColor(c);
                map.drawPixel(x, y);
            }
        }

        texture = new Texture(map);
        sprite = new TextureRegionDrawable(texture);
        map.dispose();
    }

    // Constructors
    public SpacePlanet(final App game, final World world, PlanetState state, float x, float y){
        stateRef = state;

        setSize(state.radius * 2, state.radius * 2);
        setOrigin(getWidth() / 2, getHeight() / 2);

        BodyDef def = new BodyDef();
        def.type = BodyType.DynamicBody;
        def.position.set(0, 0);
        setBody(world.createBody(def));
        setPosition(x, y);

        shape.setRadius(state.radius / getPhysScale());
        shape.setPosition(new Vector2(0, 0));
        body.createFixture(shape, 1.0f);

        noise = new OpenSimplexNoise(state.seed);
        generateSprite();
    }

    // Functions
    public void applyGravity(float delta, Body b){
        // Newtons gravitational law: F = G((m1 * m2) / r^2)
        Vector2 dir = body.getWorldCenter().cpy().sub(b.getWorldCenter());
        float radSqr = dir.len2();
        float f = GRAVITY_CONSTANT * ((b.getMass() * stateRef.radius) / radSqr);

        b.applyForceToCenter(dir.nor().scl(f * (1 / getPhysScale()) * delta), true);
    }

    @Override
    public void draw(Batch batch, float parentAlpha){
        super.draw(batch, parentAlpha);

        Matrix4 oldMatrix = batch.getTransformMatrix();
        batch.setTransformMatrix(new Matrix4().set(getTransform()));
        sprite.draw(batch, 0, 0, getWidth(), getHeight());
        batch.setTransformMatrix(oldMatrix);
    }

    @Override
    public boolean remove(){
        texture.dispose();
        shape.dispose();
        return super.remove();
    }
    
}
