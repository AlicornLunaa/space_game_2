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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class SpacePlanet extends Entity {

    // Static
    public final static float GRAVITY_CONSTANT = 1000000.0f;

    // Variables
    private PlanetState stateRef;

    private final OpenSimplexNoise noise;
    
    private Pixmap map;
    private Texture texture;
    private TextureRegionDrawable sprite;

    // Private functions
    private void generateSprite(){
        map = new Pixmap((int)stateRef.radius * 2, (int)stateRef.radius * 2, Format.RGBA8888);

        for(int x = 0; x < map.getWidth(); x++){
            for(int y = 0; y < map.getHeight(); y++){
                int mX = x - map.getWidth() / 2;
                int mY = y - map.getHeight() / 2;
                int radSqr = mX * mX + mY * mY;
                float rand = (float)noise.eval(mX * 0.1f, mY * 0.1f);
                
                Color c = new Color(0.72f, 0.7f, 0.9f, 1);

                if(radSqr > stateRef.radius * stateRef.radius) c.a = 0;
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
    public SpacePlanet(final App game, PlanetState state, float x, float y){
        stateRef = state;

        setOrigin(state.radius, state.radius);
        setBounds(x, y, state.radius * 2, state.radius * 2);

        noise = new OpenSimplexNoise(state.seed);
        generateSprite();
    }

    // Functions
    public void applyGravity(Body body){
        // Newtons gravitational law: F = G((m1 * m2) / r^2)
        Vector2 dir = new Vector2(getX() + getOriginX(), getY() + getOriginY()).sub(body.getWorldCenter());
        float radSqr = dir.len2();
        float f = GRAVITY_CONSTANT * ((body.getMass() * stateRef.radius) / radSqr);

        body.applyForceToCenter(dir.nor().scl(f), true);
    }

    @Override
    public void draw(Batch batch, float parentAlpha){
        sprite.draw(
            batch,
            getX(),
            getY(),
            getOriginX(),
            getOriginY(),
            getWidth(),
            getHeight(),
            getScaleX(),
            getScaleY(),
            getRotation()
        );
    }

    @Override
    public boolean remove(){
        texture.dispose();
        return super.remove();
    }
    
}
