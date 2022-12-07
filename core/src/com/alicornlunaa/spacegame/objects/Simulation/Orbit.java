package com.alicornlunaa.spacegame.objects.Simulation;

import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class Orbit {
    
    // Variables
    private float semiMajorAxis = 0.f;
    private float eccentricity = 0.f;
    private float argumentOfPeriapsis = 0.f;
    private float trueAnomaly = 0.f;
    private float ascendingNode = 0.f;
    private float inclination = 0.f;

    // Constructor
    public Orbit(Vector3 pos, Vector3 vel, float mass){
        Vector3 h = pos.cpy().crs(vel);
        float mu = Constants.GRAVITY_CONSTANT * mass;
        // float mu = (float)(6.6743e-11) * mass / 1000000000;

        Vector3 eV = (vel.cpy().crs(h).scl(1 / mu).sub(pos.cpy().nor()));
        Vector3 n = new Vector3(0, 0, 1).crs(h);
        float anomaly = (float)((pos.cpy().nor().dot(vel) >= 0) ? Math.acos(pos.cpy().nor().dot(eV.cpy().nor())) : (2 * Math.PI - Math.acos(pos.cpy().nor().dot(eV.cpy().nor()))));
        float i = (float)Math.acos(h.cpy().nor().z);
        float e = eV.len();
        float O = (float)((n.y >= 0) ? Math.acos(n.cpy().nor().x) : (2 * Math.PI - Math.acos(n.cpy().nor().x)));
        float w = (float)((eV.z >= 0) ? Math.acos(n.cpy().nor().dot(eV.cpy().nor())) : (2 * Math.PI - Math.acos(n.cpy().nor().dot(eV.cpy().nor()))));
        float a = (float)(1 / ((2 / pos.len()) - (vel.len2() / mu)));
        
        // float E = (float)(2 * Math.atan(Math.tan(anomaly / 2) / Math.sqrt((1 + e) / (1 - e))));
        // float M = (float)(E - e * Math.sin(E));

        semiMajorAxis = a;
        eccentricity = e;
        argumentOfPeriapsis = (float)Math.toDegrees(w);
        trueAnomaly = (float)Math.toDegrees(anomaly);
        ascendingNode = (float)Math.toDegrees(O);
        inclination = (float)Math.toDegrees(i);
    }

    // Functions
    public Vector2 getVelocityAtTime(float t){
        // Kepler to cartesian
        return Vector2.Zero.cpy();
    }

    public Vector2 getPositionAtTime(float t){
        // Kepler to cartesian
        return Vector2.Zero.cpy();
    }

    @Override
    public String toString(){
        return String.format("a: %f%ne: %f%nw: %f%nv: %f%nO: %f%ni: %f%n", semiMajorAxis, eccentricity, argumentOfPeriapsis, trueAnomaly, ascendingNode, inclination);
    }

}
