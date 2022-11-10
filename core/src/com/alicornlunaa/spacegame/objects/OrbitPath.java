package com.alicornlunaa.spacegame.objects;

import java.util.ArrayList;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Simulation.Celestial;
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

    private final ShapeRenderer render;
    private Color color = Color.CYAN;

    private Celestial parent;
    private Entity entity;
    private float semiMajorAxis;
    private float eccentricity;
    private float argumentOfPeriapsis;
    private float period;

    private ArrayList<Vector2> points = new ArrayList<>();

    // Constructor
    public OrbitPath(final App game, Celestial parent, Entity entity){
        // Constructor to obtain keplarian elements from position and velocity
        render = game.shapeRenderer;
        this.parent = parent;
        this.entity = entity;
        recalculate();
    }

    // Functions
    public float getSemiMajorAxis(){ return semiMajorAxis; }
    public float getEccentricity(){ return eccentricity; }
    public float getArgumentOfPeriapsis(){ return argumentOfPeriapsis; }
    public float getPeriod(){ return period; }

    public void setParent(Celestial c){ parent = c; }
    
    public void recalculate(){
        // Recalculate orbital path
        Vector2 position = entity.getBody().getPosition();
        Vector2 velocity = entity.getBody().getLinearVelocity();
        
        float mu = (float)(Constants.GRAVITY_CONSTANT * parent.getBody().getMass());
        float a = -(mu * position.len()) / (position.len() * velocity.len2() - (2 * mu));
        float T = (float)(2 * Math.PI * Math.sqrt(Math.pow(a, 3) / mu));
        float p = position.x * velocity.y - position.y * velocity.x;
        float epsilon = (velocity.len2() / 2 - (mu / position.len()));
        float e = (float)(Math.sqrt(1 + ((2 * epsilon * p * p) / (mu * mu))));
        Vector2 ev = (position.cpy().scl(((velocity.len2() / mu) - (1 / position.len())))).sub(velocity.cpy().scl((position.dot(velocity) / mu)));
        e = ev.len();
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
        }
    }

    public void simulate(int steps){
        // Simulates path using newtonian physics
    }

    public void draw(Batch batch){
        batch.end();
        render.begin(ShapeRenderer.ShapeType.Filled);
        render.setProjectionMatrix(batch.getProjectionMatrix());
        render.setTransformMatrix(batch.getTransformMatrix());
        render.setColor(color);

        for(int i = 0; i < points.size(); i++){
            Vector2 p1 = points.get(i);
            Vector2 p2 = points.get((i + 1) % points.size());
            render.rectLine(
                p1.x * Constants.PPM + parent.getPosition().x,
                p1.y * Constants.PPM + parent.getPosition().y,
                p2.x * Constants.PPM + parent.getPosition().x,
                p2.y * Constants.PPM + parent.getPosition().y,
                50
            );
        }

        render.end();
        batch.begin();
    }
    
}
