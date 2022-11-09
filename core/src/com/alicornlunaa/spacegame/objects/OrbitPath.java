package com.alicornlunaa.spacegame.objects;

import java.util.ArrayList;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * Periapsis distance = a(1-e)
    Apoapsis distance = a(1+e)
    */
/**
 * Orbit path drawn out using shaperenderer
 */
public class OrbitPath {

    // Variables
    private final ShapeRenderer render;
    private Color color = Color.CYAN;

    private Vector2 centerOfMass;
    private float periapsis;
    private float apoapsis;
    
    private float semiMajorAxis;
    private float linearEccentricity;
    private float eccentricity;
    private float semiMinorAxis;
    private float ellipseCenterX;
    private float ellipseCenterY;

    private ArrayList<Vector2> points = new ArrayList<>();

    // Private function
    private void recalculate(){
        semiMajorAxis = (periapsis + apoapsis) / 2;
        linearEccentricity = semiMajorAxis - periapsis;
        eccentricity = linearEccentricity / semiMajorAxis;
        semiMinorAxis = (float)Math.sqrt(Math.pow(semiMajorAxis, 2) - Math.pow(linearEccentricity, 2));
        ellipseCenterX = centerOfMass.x - linearEccentricity;
        ellipseCenterY = centerOfMass.y;
    }

    // Constructor
    public OrbitPath(final App game, Vector2 centerOfMass, float periapsis, float apoapsis){
        // Constructor to obtain keplarian elements from periapsis and apoapsis
        render = game.shapeRenderer;
        
        this.centerOfMass = centerOfMass;
        this.periapsis = periapsis;
        this.apoapsis = apoapsis;

        recalculate();
    }

    public OrbitPath(final App game, Vector2 position, Vector2 velocity, float centralBodyMass){
        // Constructor to obtain keplarian elements from position and velocity
        render = game.shapeRenderer;
        
        Vector3 pos3 = new Vector3().set(position, 0);
        Vector3 vel3 = new Vector3().set(velocity, 0);
        Vector3 h = pos3.cpy().crs(vel3);
        float mu = centralBodyMass / Constants.GRAVITY_CONSTANT;
        Vector3 e = (vel3.cpy().crs(h).scl(1 / mu)).sub(pos3.cpy().nor());
        Vector3 n = new Vector3(0, 0, 1).crs(h);
        float v = 0.0f;

        if(pos3.x * pos3.y * pos3.z + vel3.x * vel3.y * vel3.z >= 0){
            v = (float)(Math.acos((e.x * e.y * e.z + pos3.x * pos3.y * pos3.z) / (e.cpy().nor().dot(pos3.cpy().nor()))));
        } else {
            v = (float)(2 * Math.PI - Math.acos((e.x * e.y * e.z + pos3.x * pos3.y * pos3.z) / (e.cpy().nor().dot(pos3.cpy().nor()))));
        }

        eccentricity = e.len();
        semiMajorAxis = (float)(1 / (2 / pos3.len() - Math.pow(vel3.len(), 2) / mu)) * Constants.PPM;
        
        semiMinorAxis = 50 * Constants.PPM;
        ellipseCenterX = position.x;
        ellipseCenterY = position.y;
    }

    // Functions
    public void draw(Batch batch){
        batch.end();
        render.begin(ShapeRenderer.ShapeType.Filled);
        render.setProjectionMatrix(batch.getProjectionMatrix());
        render.setTransformMatrix(batch.getTransformMatrix());
        render.setColor(color);

        Vector2 p = new Vector2(semiMajorAxis + ellipseCenterX, ellipseCenterX);
        for(int i = 0; i < Constants.ORBIT_RESOLUTION; i++){
            float theta = (float)((i / (float)(Constants.ORBIT_RESOLUTION - 1)) * Math.PI * 2); // Current circle angle given resolution
            float oX = (float)Math.cos(theta) * semiMajorAxis + ellipseCenterX;
            float oY = (float)Math.sin(theta) * semiMinorAxis + ellipseCenterX;

            render.rectLine(p.x, p.y, oX, oY, 50);
            p.set(oX, oY);
        }

        render.end();
        batch.begin();
    }
    
}
