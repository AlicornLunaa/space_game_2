package com.alicornlunaa.spacegame.objects.Planet;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Entity;
import com.alicornlunaa.spacegame.states.PlanetState;
import com.alicornlunaa.spacegame.util.Constants;
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
    private Texture terrainTexture;
    private TextureRegionDrawable terrainSprite;
    private Texture atmosTexture;
    private TextureRegionDrawable atmosSprite;

    // Private functions
    private void generateTerrainSprite(){
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

        terrainTexture = new Texture(map);
        terrainSprite = new TextureRegionDrawable(terrainTexture);
        map.dispose();
    }

    private void generateAtmosphereSprite(){
        map = new Pixmap(Math.min((int)stateRef.atmosRadius * 2, 2500), Math.min((int)stateRef.atmosRadius * 2, 2500), Format.RGBA8888);

        for(int x = 0; x < map.getWidth(); x++){
            for(int y = 0; y < map.getHeight(); y++){
                int mX = x - map.getWidth() / 2;
                int mY = y - map.getHeight() / 2;
                int radSqr = mX * mX + mY * mY;
                float atmosDensity = Math.abs(1 - (radSqr / (stateRef.atmosRadius * stateRef.atmosRadius)));
                
                Color c = new Color(0.6f, 0.6f, 0.9f, 0.2f * atmosDensity);

                if(radSqr > (float)Math.pow(map.getWidth() / 2, 2)) c.a = 0;

                map.setColor(c);
                map.drawPixel(x, y);
            }
        }

        atmosTexture = new Texture(map);
        atmosSprite = new TextureRegionDrawable(atmosTexture);
        map.dispose();
    }

    // Constructors
    public SpacePlanet(final App game, final World world, PlanetState state){
        stateRef = state;

        setSize(state.radius * 2, state.radius * 2);
        setOrigin(getWidth() / 2, getHeight() / 2);

        BodyDef def = new BodyDef();
        def.type = BodyType.DynamicBody;
        def.position.set(0, 0);
        setBody(world.createBody(def));
        setPosition(state.position);

        shape.setRadius(state.radius / getPhysScale());
        shape.setPosition(new Vector2(0, 0));
        body.createFixture(shape, 1.0f);

        noise = new OpenSimplexNoise(state.seed);
        generateTerrainSprite();
        generateAtmosphereSprite();
    }

    // Functions
    public void applyGravity(float delta, Body b){
        // Newtons gravitational law: F = G((m1 * m2) / r^2)
        Vector2 dir = body.getWorldCenter().cpy().sub(b.getWorldCenter());
        float radSqr = dir.len2();
        float f = GRAVITY_CONSTANT * ((b.getMass() * stateRef.radius) / radSqr); // TODO: Fix mismatched scales

        b.applyForceToCenter(dir.nor().scl(f * (1 / getPhysScale()) * delta), true);
    }

    public void applyDrag(float delta, Body b){
        // Newtons gravitational law: F = 1/2(density * velocity^2 * dragCoefficient * Area)
        float planetRadPhys = stateRef.radius / physScale; // Planet radius in physics scale
        float atmosRadPhys = stateRef.atmosRadius / physScale; // Atmosphere radius in physics scale
        float entRadPhys = body.getPosition().dst(b.getPosition()); // Entity radius in physics sclae

        float atmosSurface = atmosRadPhys - planetRadPhys; // Atmosphere radius above surface
        float entSurface = entRadPhys - planetRadPhys; // Entity radius above surface
        float atmosDepth = Math.max(atmosSurface - entSurface, 0) / atmosSurface; // How far the entity is in the atmosphere, where zero is outside and 1 is submerged
        float density = stateRef.atmosDensity * atmosDepth;

        Vector2 relVel = body.getLinearVelocity().cpy().sub(b.getLinearVelocity());
        Vector2 velDir = b.getLinearVelocity().cpy().nor();
        float velSqr = relVel.len2();
        float force = (1.0f / 2.0f) * (density * velSqr * Constants.DRAG_COEFFICIENT);

        b.applyForceToCenter(velDir.scl(-1 * force * delta), true);
    }

    @Override
    public void draw(Batch batch, float parentAlpha){
        super.draw(batch, parentAlpha);

        Matrix4 oldMatrix = batch.getTransformMatrix();
        batch.setTransformMatrix(new Matrix4().set(getTransform()));
        atmosSprite.draw(batch, getOriginX() - stateRef.atmosRadius, getOriginY() - stateRef.atmosRadius, stateRef.atmosRadius * 2, stateRef.atmosRadius * 2);
        terrainSprite.draw(batch, 0, 0, stateRef.radius * 2, stateRef.radius * 2);
        batch.setTransformMatrix(oldMatrix);
    }

    @Override
    public boolean remove(){
        terrainTexture.dispose();
        atmosTexture.dispose();
        shape.dispose();
        return super.remove();
    }
    
}
