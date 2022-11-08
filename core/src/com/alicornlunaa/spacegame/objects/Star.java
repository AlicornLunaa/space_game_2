package com.alicornlunaa.spacegame.objects;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.util.Constants;
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

public class Star extends Entity {

    // Variables
    // private final App game;

    private CircleShape shape = new CircleShape();

    private float radius = 1000.0f;
    // private float temperature = 0.0f;

    private Pixmap pixmap;
    private Texture starTexture;
    private TextureRegionDrawable starSprite;

    // Private functions
    public void generateSprite(){
        pixmap = new Pixmap(Math.min((int)radius * 2, 2000), Math.min((int)radius * 2, 2000), Format.RGBA8888);

        for(int x = 0; x < pixmap.getWidth(); x++){
            for(int y = 0; y < pixmap.getHeight(); y++){
                int mX = x - pixmap.getWidth() / 2;
                int mY = y - pixmap.getHeight() / 2;
                int radSqr = mX * mX + mY * mY;
                Color c = new Color(1, 1, 1, 1);

                if(radSqr > (float)Math.pow(pixmap.getWidth() / 2, 2)) c.a = 0;

                pixmap.setColor(c);
                pixmap.drawPixel(x, y);
            }
        }

        starTexture = new Texture(pixmap);
        starSprite = new TextureRegionDrawable(starTexture);
        pixmap.dispose();
    }

    // Constructor
    public Star(final App game, final World world, float x, float y){
        super();
        // this.game = game;

        generateSprite();

        BodyDef def = new BodyDef();
        def.type = BodyType.DynamicBody;
        def.position.set(0, 0);
        setBody(world.createBody(def));

        shape.setRadius(radius / getPhysScale());
        shape.setPosition(new Vector2(0, 0));
        // body.createFixture(shape, 1.0f);

        setSize(radius * 2, radius * 2);
        setOrigin(radius, radius);
        setPosition(x, y);
    }

    // Functions
    public float getRadius(){ return radius; }
    
    public void applyGravity(float delta, Body b){
        // Newtons gravitational law: F = (G(m1 * m2)) / r^2
        float orbitRadius = body.getPosition().dst(b.getPosition()); // Entity radius in physics scale
        Vector2 direction = body.getPosition().cpy().sub(b.getPosition()).nor();
        float force = (Constants.GRAVITY_CONSTANT * b.getMass() * body.getMass()) / (orbitRadius * orbitRadius);

        b.applyForceToCenter(direction.scl(force * delta), true);
    }

    @Override
    public void draw(Batch batch, float parentAlpha){
        Matrix4 oldMatrix = batch.getTransformMatrix();
        batch.setTransformMatrix(new Matrix4().set(getTransform()));
        starSprite.draw(batch, 0, 0, radius * 2, radius * 2);
        batch.setTransformMatrix(oldMatrix);
    }

    @Override
    public boolean remove(){
        starTexture.dispose();
        shape.dispose();
        return super.remove();
    }
    
}
