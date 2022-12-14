package com.alicornlunaa.spacegame.objects.Simulation;

import com.alicornlunaa.spacegame.objects.Entity;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;

public class Orbit {
    
    // Variables
    private Celestial parent;
    private Entity child;

    private float semiMajorAxis = 0.f;
    private float eccentricity = 0.f;
    private float argumentOfPeriapsis = 0.f;
    private float trueAnomaly = 0.f;
    private float ascendingNode = 0.f;
    private float inclination = 0.f;
    private float periapsis = 0.f;
    private float apoapsis = 0.f;

    // Constructor
    public Orbit(Celestial parent, Entity child){
        this.parent = parent;
        this.child = child;
        calculate();
    }

    // Functions
    public void calculate(){
        // Calculate orbits based on the passed references
        Body parentBody = parent.getBody();
        Body childBody = child.getBody();
        Vector3 pos = new Vector3().set(childBody.getPosition().cpy(), 0.f);
        Vector3 vel = new Vector3().set(childBody.getLinearVelocity().cpy(), 0.f);
        float mu = Constants.GRAVITY_CONSTANT * parentBody.getMass();

        Vector3 h = pos.cpy().crs(vel);
        Vector3 eV = (vel.cpy().crs(h).scl(1 / mu).sub(pos.cpy().nor()));
        Vector3 n = new Vector3(0, 0, 1).crs(h);
        float anomaly = (float)((pos.cpy().nor().dot(vel) >= 0) ? Math.acos(pos.cpy().nor().dot(eV.cpy().nor())) : (2 * Math.PI - Math.acos(pos.cpy().nor().dot(eV.cpy().nor()))));
        float i = (float)Math.acos(h.cpy().nor().z);
        float e = eV.len();
        float O = (float)((n.y >= 0) ? Math.acos(n.cpy().nor().x) : (2 * Math.PI - Math.acos(n.cpy().nor().x)));
        float w = (float)Math.atan2(eV.y, eV.x);
        float a = (float)(1 / ((2 / pos.len()) - (vel.len2() / mu)));
        
        // float E = (float)(2 * Math.atan(Math.tan(anomaly / 2) / Math.sqrt((1 + e) / (1 - e))));
        // float M = (float)(E - e * Math.sin(E));

        semiMajorAxis = a;
        eccentricity = e;
        argumentOfPeriapsis = (float)Math.toDegrees(w);
        trueAnomaly = (float)Math.toDegrees(anomaly);
        ascendingNode = (float)Math.toDegrees(O);
        inclination = (float)Math.toDegrees(i);
        periapsis = a * (1 - e);
        apoapsis = a * (1 + e);
    }

    public void draw(ShapeRenderer render){
        Vector2 center = new Vector2(0, 0);
        float linearE = semiMajorAxis - periapsis;
        float semiMinorAxis = (float)(Math.sqrt(Math.pow(semiMajorAxis, 2.0) - Math.pow(linearE, 2.0)));

        // TODO: Implement newtonian method

        render.setTransformMatrix(new Matrix4().set(parent.getUniverseTransform()).rotate(0, 0, 1, argumentOfPeriapsis));
        render.setColor(Color.MAGENTA);

        for(int i = 0; i < Constants.ORBIT_RESOLUTION; i++){
            float angle1 = (float)((i / (Constants.ORBIT_RESOLUTION - 1.0)) * Math.PI * 2.0);
            float angle2 = (float)((((i + 1) % Constants.ORBIT_RESOLUTION) / (Constants.ORBIT_RESOLUTION - 1.0)) * Math.PI * 2.0);
            Vector2 p1 = new Vector2((float)(Math.cos(angle1) * semiMajorAxis + center.x - linearE), (float)(Math.sin(angle1) * semiMinorAxis + center.y)).scl(Constants.PPM);
            Vector2 p2 = new Vector2((float)(Math.cos(angle2) * semiMajorAxis + center.x - linearE), (float)(Math.sin(angle2) * semiMinorAxis + center.y)).scl(Constants.PPM);

            render.rectLine(p1, p2, 225);
        }

        render.circle(0, 0, 600);
    }

    public Vector2 getVelocityAtTime(float t){
        // Kepler to cartesian TODO: Implement
        return Vector2.Zero.cpy();
    }

    public Vector2 getPositionAtTime(float t){
        // Kepler to cartesian TODO: Implement
        return Vector2.Zero.cpy();
    }

    @Override
    public String toString(){
        return String.format("a: %f%ne: %f%nw: %f%nv: %f%nO: %f%ni: %f%n", semiMajorAxis, eccentricity, argumentOfPeriapsis, trueAnomaly, ascendingNode, inclination);
    }

}
