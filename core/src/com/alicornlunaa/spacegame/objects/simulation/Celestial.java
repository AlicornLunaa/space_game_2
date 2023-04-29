package com.alicornlunaa.spacegame.objects.simulation;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.engine.core.BaseEntity;
import com.alicornlunaa.spacegame.engine.phys.CelestialPhysWorld;
import com.alicornlunaa.spacegame.engine.phys.PhysWorld;
import com.alicornlunaa.spacegame.objects.simulation.orbits.GenericConic;
import com.alicornlunaa.spacegame.objects.simulation.orbits.OrbitPropagator;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;

/**
 * A celstial is one of the super massive objects in space, ex. star, planet, moon
 * Each celestial will have its own box2d world which entities will be added to
 * when they are in the sphere of influence, and removed when they leave
 */
public class Celestial extends BaseEntity {

    // Static vars
    private static int NEXT_CELESTIAL_ID = 0;
    
    // Variables
    protected final App game;

    // Planet variables
    protected float radius;
    protected float opacity = 1.f;
    private int celestialID;

    // Physics variables
    protected CelestialPhysWorld influenceWorld;
    private Body localBody;

    private @Null Celestial parent = null;
    private Array<Celestial> children = new Array<>();
    private Array<BaseEntity> ents = new Array<>();
    
    // Constructor
    public Celestial(App game, PhysWorld parentWorld, float radius){
        this.game = game;
        this.radius = radius;
        this.celestialID = NEXT_CELESTIAL_ID++;

        influenceWorld = new CelestialPhysWorld(game, Constants.PPM);
        game.simulation.addWorld(influenceWorld);
        
        CircleShape shape = new CircleShape();
        shape.setRadius(radius / getPhysScale());
        shape.setPosition(Vector2.Zero.cpy());

        BodyDef def = new BodyDef();
        def.type = BodyType.DynamicBody;
        def.position.set(0, 0);
        setBody(parentWorld.getBox2DWorld().createBody(def));
        getBody().createFixture(shape, 1.0f);

        def = new BodyDef();
        def.type = BodyType.StaticBody;
        def.position.set(0, 0);
        localBody = influenceWorld.getBox2DWorld().createBody(def);
        localBody.createFixture(shape, 1.0f);

        shape.dispose();
    }

    // Functions
    public float getRadius(){ return radius; }
    public int getCelestialID(){ return celestialID; }
    public PhysWorld getWorld(){ return influenceWorld; }
    public Array<BaseEntity> getEntities(){ return ents; }
    public Array<Celestial> getChildren(){ return children; }
    public Celestial getCelestialParent(){ return parent; }
    public void setCelestialParent(Celestial c){ parent = c; }
    public void setCelestialOpacity(float a){ opacity = a; }

    public float getSphereOfInfluence(){
        if(getCelestialParent() == null) return radius * 200; // Star radius

        GenericConic c = OrbitPropagator.getConic(getCelestialParent(), this);
        return (float)(c.getSemiMajorAxis() * Math.pow(getBody().getMass() / getCelestialParent().getBody().getMass(), 2.0 / 5.0)) * Constants.PPM;
    }

    public Matrix3 getUniverseSpaceTransform(){
        if(parent == null) return new Matrix3().translate(getPosition());
        return parent.getUniverseSpaceTransform().mul(new Matrix3().translate(getPosition()));
    }

    public Matrix3 getSystemSpaceTransform(){
        return getUniverseSpaceTransform().inv();
    }

    @Override
    public void update(){}

    @Override
    public void render(Batch batch){
        if(!Constants.DEBUG) return;
        batch.end();

        ShapeRenderer s = game.shapeRenderer;
        s.begin(ShapeRenderer.ShapeType.Line);
        s.setProjectionMatrix(batch.getProjectionMatrix());
        s.setTransformMatrix(batch.getTransformMatrix());
        s.setColor(Color.RED);
        s.circle(0, 0, getSphereOfInfluence(), 500);
        s.setColor(Color.YELLOW);
        s.circle(0, 0, getRadius(), 500);
        s.end();
        
        game.debug.render(influenceWorld.getBox2DWorld(), batch.getProjectionMatrix().cpy().mul(new Matrix4().set(this.getUniverseSpaceTransform())).scl(Constants.PPM));

        batch.begin();
    }

    // Physics functions
    public Vector2 applyGravity(float delta, Body b){
        // Newtons gravitational law: F = (G(m1 * m2)) / r^2
        float orbitRadius = b.getPosition().len(); // Entity radius in physics scale
        Vector2 direction = b.getPosition().cpy().nor().scl(-1);
        float force = Constants.GRAVITY_CONSTANT * ((b.getMass() * getBody().getMass()) / (orbitRadius * orbitRadius));
        return direction.scl(force);
    }

    public Vector2 applyPhysics(float delta, BaseEntity e){
        return applyGravity(delta, e.getBody());
    }

}
