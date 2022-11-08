package com.alicornlunaa.spacegame.objects.Simulation;

import java.util.ArrayList;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Entity;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Null;

/**
 * A celstial is one of the super massive objects in space, ex. star, planet, moon
 * Each celestial will have its own box2d world which entities will be added to
 * when they are in the sphere of influence, and removed when they leave
 */
public class Celestial extends Entity {
    
    // Variables
    protected final App game;

    // Planet variables
    protected float radius;
    protected float sphereOfInfluence;

    // Physics variables
    private final World influenceWorld;
    private float physAccumulator;

    private @Null Celestial parent = null;
    private ArrayList<Celestial> children = new ArrayList<>();
    private ArrayList<Entity> ents = new ArrayList<>();
    
    // Constructor
    public Celestial(final App game, float radius, float sphereOfInfluence){
        this.game = game;
        this.radius = radius;
        this.sphereOfInfluence = sphereOfInfluence;
        influenceWorld = new World(getPosition(), true);
    }

    // Functions
    public float getRadius(){ return radius; }
    public float getSphereOfInfluence(){ return sphereOfInfluence; }
    public World getWorld(){ return influenceWorld; }
    public ArrayList<Entity> getEntities(){ return ents; }
    public ArrayList<Celestial> getChildren(){ return children; }
    public Celestial getCelestialParent(){ return parent; }
    public void setCelestialParent(Celestial c){ parent = c; }

    public Matrix3 getUniverseTransform(){
        if(parent == null) return getTransform();
        return parent.getUniverseTransform().mul(getTransform());
    }

    @Override
    public void draw(Batch batch, float a){
        ShapeRenderer s = game.shapeRenderer;
        s.begin(ShapeRenderer.ShapeType.Line);
        s.setProjectionMatrix(batch.getProjectionMatrix());
        s.setTransformMatrix(batch.getTransformMatrix());
        s.setColor(Color.RED);
        s.circle(getX(), getY(), getSphereOfInfluence());
        s.setColor(Color.YELLOW);
        s.circle(getX(), getY(), getRadius());
        s.end();
    }

    public void update(float delta){
        // Step the physics on the world
        physAccumulator += Math.min(delta, 0.25f);
        while(physAccumulator >= Constants.TIME_STEP){
            influenceWorld.step(Constants.TIME_STEP, Constants.VELOCITY_ITERATIONS, Constants.POSITION_ITERATIONS);
            physAccumulator -= Constants.TIME_STEP;
        }
    }

}
