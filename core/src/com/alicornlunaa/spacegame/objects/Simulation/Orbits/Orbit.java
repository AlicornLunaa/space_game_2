package com.alicornlunaa.spacegame.objects.Simulation.Orbits;

import com.alicornlunaa.spacegame.objects.Entity;
import com.alicornlunaa.spacegame.objects.Simulation.Celestial;
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
    private float initialTrueAnomaly = 0.f;
    private float inclination = 0.f;
    private float periapsis = 0.f;
    private float apoapsis = 0.f;

    // Private functions
    private float meanAnomalyToEccentricAnomaly(float ma){
        // Mean anomaly => Eccentric anomaly
        float step = 0.0001f;
        float epsilon = 0.0000001f;
        float guess = 0;

        for(int i = 0; i < 100; i++){
            float y = (float)(ma - guess + eccentricity * Math.sin(guess));

            if(Math.abs(y) < epsilon) break;

            float slope = (float)((ma - (guess + step) + eccentricity * Math.sin(guess + step)) - y) / step;
            float s = y / slope;
            guess -= s;
        }

        return guess;
    }

    private float trueAnomalyToEccentricAnomaly(float ta){
        return (float)Math.atan2(Math.sqrt(1 - Math.pow(eccentricity, 2)) * Math.sin(ta), eccentricity + Math.cos(ta));
    }

    private float eccentricAnomalyToMeanAnomaly(float ea){
        return (float)(ea - eccentricity * Math.sin(ea));
    }
    
    private float eccentricAnomalyToTrueAnomaly(float ea){
        return (float)(2.0 * Math.atan(Math.sqrt((1 + eccentricity) / (1 - eccentricity)) * Math.tan(ea / 2.0)));
    }

    private float meanAnomalyToTrueAnomaly(float ma){
        float ea = meanAnomalyToEccentricAnomaly(ma);
        return eccentricAnomalyToTrueAnomaly(ea);
    }

    private float trueAnomalyToMeanAnomaly(float ta){
        float ea = trueAnomalyToEccentricAnomaly(ta);
        return eccentricAnomalyToMeanAnomaly(ea);
    }

    private float timeToMeanAnomaly(float t){
        // M = M0 + n(t - t0)
        double mu = Constants.GRAVITY_CONSTANT * parent.getBody().getMass();
        double n = Math.sqrt(mu / Math.pow(semiMajorAxis, 3.0));
        return (float)(n * t);
    }

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
        float mu = Constants.GRAVITY_CONSTANT * parentBody.getMass();

        Vector3 pos = new Vector3().set(childBody.getPosition().cpy(), 0.f);
        Vector3 vel = new Vector3().set(childBody.getLinearVelocity().cpy(), 0.f);

        Vector3 h = pos.cpy().crs(vel);
        Vector3 eV = (vel.cpy().crs(h).scl(1 / mu).sub(pos.cpy().nor()));
        float anomaly = (float)((pos.cpy().nor().dot(vel) >= 0) ? Math.acos(pos.cpy().nor().dot(eV.cpy().nor())) : (2 * Math.PI - Math.acos(pos.cpy().nor().dot(eV.cpy().nor()))));
        float i = (float)Math.acos(h.cpy().nor().z);
        float e = eV.len();
        float w = (float)Math.atan2(eV.y, eV.x);
        float a = (float)(1 / ((2 / pos.len()) - (vel.len2() / mu)));

        semiMajorAxis = a;
        eccentricity = e;
        argumentOfPeriapsis = w;
        initialTrueAnomaly = anomaly;
        inclination = i;
        periapsis = a * (1 - e);
        apoapsis = a * (1 + e);
    }

    public void draw(ShapeRenderer render){
        float linearE = semiMajorAxis - periapsis;
        float semiMinorAxis = (float)(Math.sqrt(Math.pow(semiMajorAxis, 2.0) - Math.pow(linearE, 2.0)));
        Vector2 center = new Vector2(-linearE, 0);

        render.setTransformMatrix(new Matrix4().set(parent.getUniverseTransform()).rotateRad(0, 0, 1, argumentOfPeriapsis));
        render.setColor(eccentricity <= 1 ? Color.CYAN : Color.RED);

        for(int i = 0; i < Constants.ORBIT_RESOLUTION; i++){
            float ang1 = (float)((i / (Constants.ORBIT_RESOLUTION - 1.f)) * Math.PI * 2.0);
            float ang2 = (float)((((i + 1) % Constants.ORBIT_RESOLUTION) / (Constants.ORBIT_RESOLUTION - 1.f)) * Math.PI * 2.0);
            Vector2 p1 = new Vector2((float)(Math.cos(ang1) * semiMajorAxis + center.x), (float)(Math.sin(ang1) * semiMinorAxis + center.y)).scl(Constants.PPM);
            Vector2 p2 = new Vector2((float)(Math.cos(ang2) * semiMajorAxis + center.x), (float)(Math.sin(ang2) * semiMinorAxis + center.y)).scl(Constants.PPM);

            render.rectLine(p1, p2, 100);
        }
    }

    public Vector2 getVelocityAtTime(float t){
        // Kepler to cartesian
        float mu = Constants.GRAVITY_CONSTANT * parent.getBody().getMass();

        float initialMeanAnomaly = trueAnomalyToMeanAnomaly(initialTrueAnomaly);
        float futureMeanAnomaly = timeToMeanAnomaly(t);
        float futureTrueAnomaly = meanAnomalyToTrueAnomaly(initialMeanAnomaly + futureMeanAnomaly);

        double p = semiMajorAxis * (1 - eccentricity * eccentricity); // Semilatus rectum
        double v = Math.sqrt(mu / p); // Orbital plane velocity

        Vector3 velocity = new Vector3((float)(-v * Math.sin(futureTrueAnomaly)), (float)(v * (eccentricity + Math.cos(futureTrueAnomaly))), 0.f);
        velocity.rotateRad(inclination, 1, 0, 0);
        velocity.rotateRad(argumentOfPeriapsis, 0, 0, 1);

        return new Vector2(velocity.x, velocity.y);
    }

    public Vector2 getPositionAtTime(float t){
        // Kepler to cartesian
        float initialMeanAnomaly = trueAnomalyToMeanAnomaly(initialTrueAnomaly);
        float futureMeanAnomaly = timeToMeanAnomaly(t);
        float futureTrueAnomaly = meanAnomalyToTrueAnomaly(initialMeanAnomaly + futureMeanAnomaly);

        double p = semiMajorAxis * (1 - eccentricity * eccentricity); // Semilatus rectum
        double r = p / (1 + eccentricity * Math.cos(futureTrueAnomaly)); // Orbital plane position

        Vector3 position = new Vector3((float)(r * Math.cos(futureTrueAnomaly)), (float)(r * Math.sin(futureTrueAnomaly)), 0.f); // Orbital plane position
        position.rotateRad(inclination, 1, 0, 0);
        position.rotateRad(argumentOfPeriapsis, 0, 0, 1);

        return new Vector2(position.x, position.y);
    }

    public Celestial getCelestial(){ return parent; }

    public Entity getEntity(){ return child; }

    public float getApoapsisHeight(){ return apoapsis; }
    public float getPeriapsisHeight(){ return periapsis; }
    public float getInclination(){ return inclination; }
    public float getEccentricity(){ return eccentricity; }
    public float getSemiMajorAxis(){ return semiMajorAxis; }

    @Override
    public String toString(){
        return String.format("a: %f%ne: %f%nw: %f%nv: %f%ni: %f%n", semiMajorAxis, eccentricity, argumentOfPeriapsis, initialTrueAnomaly, inclination);
    }

}
