package com.alicornlunaa.spacegame.objects.Simulation.Orbits;

import com.alicornlunaa.spacegame.objects.Entity;
import com.alicornlunaa.spacegame.objects.Simulation.Celestial;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/** One conic section of an orbit */
public class ConicSection {

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

    // Constructor
    public ConicSection(Celestial parent, Entity child, Vector2 position, Vector2 velocity){
        this.parent = parent;
        this.child = child;
        calculate(position, velocity);
    }

    public ConicSection(Celestial parent, Entity child){
        this.parent = parent;
        this.child = child;
        calculate();
    }

    // Functions
    public void calculate(Vector2 position, Vector2 velocity){
        // Calculate orbits based on the passed references
        float mu = Constants.GRAVITY_CONSTANT * parent.getBody().getMass();

        Vector3 pos = new Vector3().set(position, 0.f);
        Vector3 vel = new Vector3().set(velocity, 0.f);

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

    public void calculate(){
        calculate(child.getBody().getPosition().cpy(), child.getBody().getLinearVelocity().cpy());
    }

    public Vector2 getVelocityAtAnomaly(float t){
        if(eccentricity >= 1){
            // Hyperbolic
            return HyperbolicOrbit.getVelocityAtAnomaly(this, t);
        } else {
            // Elliptic
            return EllipticOrbit.getVelocityAtAnomaly(this, t);
        }
    }

    public Vector2 getPositionAtAnomaly(float t){
        if(eccentricity > 1){
            // Hyperbolic
            return HyperbolicOrbit.getPositionAtAnomaly(this, t);
        } else {
            // Elliptic
            return EllipticOrbit.getPositionAtAnomaly(this, t);
        }
    }

    public Vector2 getVelocityAtTime(float t){
        if(eccentricity >= 1){
            // Hyperbolic
            return HyperbolicOrbit.getVelocityAtTime(this, t);
        } else {
            // Elliptic
            return EllipticOrbit.getVelocityAtTime(this, t);
        }
    }

    public Vector2 getPositionAtTime(float t){
        if(eccentricity > 1){
            // Hyperbolic
            return HyperbolicOrbit.getPositionAtTime(this, t);
        } else {
            // Elliptic
            return EllipticOrbit.getPositionAtTime(this, t);
        }
    }

    public float getInitialMeanAnomaly(){
        if(eccentricity > 1){
            // Hyperbolic
            return HyperbolicOrbit.trueAnomalyToMeanAnomaly(this, getInitialTrueAnomaly());
        } else {
            // Elliptic
            return EllipticOrbit.trueAnomalyToMeanAnomaly(this, getInitialTrueAnomaly());
        }
    }

    public Celestial getParent(){ return parent; }
    public Entity getChild(){ return child; }

    public float getSemiMajorAxis() { return semiMajorAxis; }
    public float getEccentricity() { return eccentricity; }
    public float getArgumentOfPeriapsis() { return argumentOfPeriapsis; }
    public float getInitialTrueAnomaly() { return initialTrueAnomaly; }
    public float getInclination() { return inclination; }
    public float getPeriapsis() { return periapsis; }
    public float getApoapsis() { return apoapsis; }

    public void draw(ShapeRenderer renderer){
        if(eccentricity >= 1){
            // Hyperbolic or parabolic
            HyperbolicOrbit.draw(this, renderer);
        } else {
            // Elliptic or circular
            EllipticOrbit.draw(this, renderer);
        }
    }

}
