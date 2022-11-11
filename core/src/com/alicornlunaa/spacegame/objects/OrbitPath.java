package com.alicornlunaa.spacegame.objects;

import java.util.ArrayList;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Simulation.Celestial;
import com.alicornlunaa.spacegame.objects.Simulation.Universe;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

/**
 * Orbit path drawn out using shaperenderer
 */
public class OrbitPath {

    // Variables
    private static final int MAX_TRIES = 8;
    private static final int STEPS = 512;

    private final Universe universe;
    private final ShapeRenderer render;
    private Color color = Color.CYAN;

    private Celestial parent;
    private Entity entity;
    private Vector2 center;
    private float semiMajorAxis;
    private float eccentricity;
    private float argumentOfPeriapsis;
    private float period;

    private boolean ends = false;
    private ArrayList<Vector2> absolutePoints = new ArrayList<>();
    private ArrayList<Vector2> velocities = new ArrayList<>();
    private ArrayList<Vector2> points = new ArrayList<>();

    // Constructor
    public OrbitPath(final App game, final Universe universe, Celestial parent, Entity entity){
        // Constructor to obtain keplarian elements from position and velocity
        render = game.shapeRenderer;
        this.universe = universe;
        this.parent = parent;
        this.entity = entity;
        recalculate();
    }

    // Functions
    public float getSemiMajorAxis(){ return semiMajorAxis; }
    public float getEccentricity(){ return eccentricity; }
    public float getArgumentOfPeriapsis(){ return argumentOfPeriapsis; }
    public float getPeriod(){ return period; }
    public Vector2 getPoint(int i){ return points.get(i); }
    public Vector2 getAbsolute(int i){ return absolutePoints.get(i); }
    public Vector2 getVelocity(int i){ return velocities.get(i); }
    public ArrayList<Vector2> getPoints(){ return points; }
    public Entity getEntity(){ return entity; }
    public boolean getEnds(){ return ends; }

    public void setParent(Celestial c){ parent = c; }
    
    public void recalculate(){
        // Recalculate orbital path
        center = parent.getPosition();

        // Convert to inertial reference frame
        Vector2 position = entity.getBody().getWorldCenter().cpy();
        Vector2 velocity = entity.getBody().getLinearVelocity().cpy().rotateDeg(position.angleDeg() + 90);

        float mu = Constants.GRAVITY_CONSTANT * parent.getBody().getMass();
        float a = -(mu * position.len()) / (position.len() * velocity.len2() - (2 * mu));
        float T = (float)(2 * Math.PI * Math.sqrt(Math.pow(a, 3) / mu));
        float p = position.x * velocity.y - position.y * velocity.x;
        float epsilon = (velocity.len2() / 2 - (mu / position.len()));
        float e = (float)(Math.sqrt(1 + ((2 * epsilon * p * p) / (mu * mu))));
        Vector2 ev = (position.cpy().scl(((velocity.len2() / mu) - (1 / position.len())))).sub(velocity.cpy().scl((position.dot(velocity) / mu)));
        float w = (float)(Math.atan2(ev.y, ev.x));
        float n = (float)(2 * Math.PI / T);

        semiMajorAxis = a;
        eccentricity = e;
        argumentOfPeriapsis = w;
        period = T;

        // Get points along path
        points.clear();
        for(int i = 0; i < STEPS; i++){
            float ma = n * (T * ((float)i / STEPS));
            
            float ea = ma;
            for(int tries = 0; tries < MAX_TRIES; tries++){
                float dx = (float)(ea - e * Math.sin(ea) - ma);
                float dy = (float)(1 - e * Math.cos(ea));
                float dt = dx / dy;
                ea -= dt;   
                if(Math.abs(dt) < 0.0000000001) break;
            }

            float pa = (float)(a * (Math.cos(ea) - e));
            float qa = (float)(a * Math.sin(ea) * Math.sqrt(1 - (e * e)));

            float x = (float)(Math.cos(w) * pa - Math.sin(w) * qa);
            float y = (float)(Math.sin(w) * pa - Math.cos(w) * qa);

            points.add(new Vector2(x, y));
            if(i == STEPS - 1) points.add(new Vector2(x, y));
        }
    }

    public void simulate(int maxSteps){
        if(entity.getDriving() != null) return;
        
        // Simulates path using newtonian physics
        if(entity instanceof Celestial){
            center = universe.getUniversalPosition((Celestial)entity);
            parent = universe.getParentCelestial((Celestial)entity);
        } else {
            center = universe.getUniversalPosition(entity);
            parent = universe.getParentCelestial(entity);
        }

        Vector2 p = entity.getBody().getPosition().cpy();
        Vector2 v = entity.getBody().getLinearVelocity().cpy();

        boolean lookForEnd = false;
        float minDistance = 10.0f;
        Celestial currentParent = parent;
        
        points.clear();
        absolutePoints.clear();
        velocities.clear();
        ends = false;

        points.add(p.cpy().sub(entity.getBody().getPosition()));
        absolutePoints.add(p.cpy());
        velocities.add(v.cpy());
        for(int i = 0; i < maxSteps; i++){
            if(currentParent == null) break;

            // Newtons gravitational law: F = (G(m1 * m2)) / r^2
            float orbitRadius = p.len(); // Entity radius in physics scale
            Vector2 direction = p.cpy().nor().scl(-1);
            float force = Constants.GRAVITY_CONSTANT * ((entity.getBody().getMass() * parent.getBody().getMass()) / (orbitRadius * orbitRadius));

            v.add(direction.scl(force / entity.getBody().getMass()));
            p.add(v);

            points.add(p.cpy().sub(entity.getBody().getPosition()));
            absolutePoints.add(p.cpy());
            velocities.add(v.cpy());

            if(p.len() * entity.getPhysScale() > parent.getSphereOfInfluence()){
                // Outside of the sphere of influence, calculate new forces based on new parent
                break;
            }

            if(p.len() * entity.getPhysScale() < parent.getRadius()){ ends = true; break; };
            if(p.dst(entity.getBody().getPosition()) < minDistance){
                if(lookForEnd){
                    // Path ends hear, it's close enough
                    i = maxSteps - 2;
                }
            } else {
                lookForEnd = true;
            }
        }
    }

    public void draw(Batch batch){
        if(entity.getDriving() != null) return;
        
        batch.end();
        render.begin(ShapeRenderer.ShapeType.Filled);
        render.setProjectionMatrix(batch.getProjectionMatrix());
        render.setTransformMatrix(batch.getTransformMatrix());
        render.setColor(color);

        for(int i = 0; i < points.size() - 1; i++){
            Vector2 p1 = points.get(i);
            Vector2 p2 = points.get((i + 1) % points.size());
            render.rectLine(
                p1.x * Constants.PPM + center.x,
                p1.y * Constants.PPM + center.y,
                p2.x * Constants.PPM + center.x,
                p2.y * Constants.PPM + center.y,
                50
            );
        }

        render.end();
        batch.begin();
    }
    
}
