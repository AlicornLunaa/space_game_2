package com.alicornlunaa.spacegame.objects.Simulation.Orbits;

import com.alicornlunaa.spacegame.objects.Entity;
import com.alicornlunaa.spacegame.objects.Simulation.Celestial;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
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

    // Anomaly functions
    /**
     * Converts mean anomaly to eccentric anomaly
     * @param orbit The conic section to use
     * @param ma The mean anomaly to convert
     * @return The eccentric anomaly
     */
    public float meanAnomalyToEccentricAnomaly(float ma){
        // Mean anomaly => Eccentric anomaly
        if(eccentricity >= 1){
            // Hyperbolic
            float epsilon = 0.0000001f;
            float guess = ma;

            for(int i = 0; i < 100; i++){
                // M = e sinh(F) - F
                float y = (float)((guess - eccentricity * Math.sinh(guess) - ma) / (1 - eccentricity * Math.cosh(guess)));
                guess = guess - y;

                if(Math.abs(y) < epsilon) break;
            }

            return guess;
        } else {
            // Elliptic
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
    }

    /**
     * Converts the true anomaly to eccentric anomaly
     * @param orbit The conic section to convert
     * @param ta The true anomaly to convert
     * @return Eccentric anomaly
     */
    public float trueAnomalyToEccentricAnomaly(float ta){
        // True anomaly => Eccentric anomaly
        if(eccentricity >= 1){
            // Hyperbolic
            return (float)(2.0 * Math.atan(Math.sqrt((eccentricity - 1) / (eccentricity + 1)) * Math.tan(ta / 2)));
        } else {
            // Elliptic
            return (float)Math.atan2(Math.sqrt(1 - Math.pow(eccentricity, 2)) * Math.sin(ta), eccentricity + Math.cos(ta));
        }
    }

    /**
     * Converts the eccentric anomaly to mean anomaly
     * @param orbit The conic section to convert
     * @param ea The eccentric anomaly to convert
     * @return Mean anomaly
     */
    public float eccentricAnomalyToMeanAnomaly(float ea){
        // Eccentric anomaly => Mean anomaly
        if(eccentricity >= 1){
            // Hyperbolic
            return (float)(eccentricity * Math.sinh(ea) - ea);
        } else {
            // Elliptic
            return (float)(ea - eccentricity * Math.sin(ea));
        }
    }

    /**
     * Converts the eccentric anomaly to true anomaly
     * @param orbit The conic section to convert
     * @param ea The eccentric anomaly to convert
     * @return True anomaly
     */
    public float eccentricAnomalyToTrueAnomaly(float ea){
        // Eccentric anomaly => True anomaly
        if(eccentricity >= 1){
            // Hyperbolic
            return (float)(2.0 * Math.atan(Math.sqrt((eccentricity + 1) / (eccentricity - 1)) * Math.tan(ea / 2.0)));
        } else {
            // Elliptic
            return (float)(2.0 * Math.atan(Math.sqrt((1 + eccentricity) / (1 - eccentricity)) * Math.tan(ea / 2.0)));
        }
    }

    /**
     * Converts mean anomaly to true anomaly. Helper function to bypass eccentric conversion.
     * @param orbit The conic section to convert
     * @param ma The mean anomaly to convert
     * @return True anomaly
     */
    public float meanAnomalyToTrueAnomaly(float ma){
        // Mean anomaly => True anomaly
        float ea = meanAnomalyToEccentricAnomaly(ma);
        return eccentricAnomalyToTrueAnomaly(ea);
    }

    /**
     * Converts true anomaly to mean anomaly. Helper function to bypass eccentric conversion.
     * @param orbit The conic section to convert
     * @param ta The true anomaly to convert
     * @return Mean anomaly
     */
    public float trueAnomalyToMeanAnomaly(float ta){
        // True anomaly => Mean anomaly
        float ea = trueAnomalyToEccentricAnomaly(ta);
        return eccentricAnomalyToMeanAnomaly(ea);
    }

    /**
     * Converts time to mean anomaly
     * @param orbit The conic section to convert
     * @param t The time to convert
     * @return Mean anomaly
     */
    public float timeToMeanAnomaly(float t){
        double mu = Constants.GRAVITY_CONSTANT * parent.getBody().getMass();
        double n = Math.sqrt(mu / Math.pow(Math.abs(semiMajorAxis), 3.0));
        return (float)(n * t);
    }

    /**
     * Converts mean to time anomaly
     * @param orbit The conic section to convert
     * @param ma The mean anomaly to convert
     * @return Time anomaly
     */
    public float meanAnomalyToTime(float ma){
        double mu = Constants.GRAVITY_CONSTANT * parent.getBody().getMass();
        double n = Math.sqrt(mu / Math.pow(Math.abs(semiMajorAxis), 3.0));
        return (float)(ma / n);
    }

    // Functions
    /**
     * Generates the data for a Keplerian conic section with orbital elements
     * @param position Positional state vector in system space
     * @param velocity Velocity state vector in system space
     */
    public void calculate(Vector2 position, Vector2 velocity){
        // Calculate orbits based on the passed references
        if(parent == null) return;

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

    /**
     * Bypass manual position and velocity and use the entity's data
     */
    public void calculate(){
        calculate(child.getBody().getPosition(), child.getBody().getLinearVelocity());
    }

    /**
     * Gets the velocity at the given mean anomaly
     * @param ma The mean anomaly
     * @return Vector2 velocity
     */
    public Vector2 getVelocity(float ma){
        // Kepler to cartesian
        float mu = Constants.GRAVITY_CONSTANT * parent.getBody().getMass();
        float futureTrueAnomaly = meanAnomalyToTrueAnomaly(ma);
        
        double p = semiMajorAxis * (1 - eccentricity * eccentricity); // Semilatus rectum
        double v = Math.sqrt(mu / p); // Orbital plane velocity
        
        Vector3 velocity = new Vector3((float)(-v * Math.sin(futureTrueAnomaly)), (float)(v * (eccentricity + Math.cos(futureTrueAnomaly))), 0.f);
        velocity.rotateRad(inclination, 1, 0, 0);
        velocity.rotateRad(argumentOfPeriapsis, 0, 0, 1);

        return new Vector2(velocity.x, velocity.y);
    }

    /**
     * Gets the position at the given mean anomaly
     * @param ma The mean anomaly
     * @return Vector2 position
     */
    public Vector2 getPosition(float ma){
        // TODO: Problem here with unprecise calculations
        // Kepler to cartesian
        float futureTrueAnomaly = meanAnomalyToTrueAnomaly(ma);
        
        double p = semiMajorAxis * (1 - eccentricity * eccentricity); // Semilatus rectum
        double r = p / (1 + eccentricity * Math.cos(futureTrueAnomaly)); // Orbital plane position

        Vector3 position = new Vector3((float)(r * Math.cos(futureTrueAnomaly)), (float)(r * Math.sin(futureTrueAnomaly)), 0.f); // Orbital plane position
        position.rotateRad(inclination, 1, 0, 0);
        position.rotateRad(argumentOfPeriapsis, 0, 0, 1);

        return new Vector2(position.x, position.y);
    }

    public Celestial getParent() { return parent; }
    public Entity getChild(){ return child; }

    public float getSemiMajorAxis() { return semiMajorAxis; }
    public float getEccentricity() { return eccentricity; }
    public float getArgumentofPeriapsis() { return argumentOfPeriapsis; }
    public float getInitialTrueAnomaly() { return initialTrueAnomaly; }
    public float getInitialMeanAnomaly(){ return trueAnomalyToMeanAnomaly(initialTrueAnomaly); }
    public float getInclination() { return inclination; }
    public float getPeriapsis() { return periapsis; }
    public float getApoapsis() { return apoapsis; }

    public void draw(ShapeRenderer renderer){
        if(parent == null) return;

        renderer.setTransformMatrix(new Matrix4().set(parent.getUniverseSpaceTransform()).rotateRad(0, 0, 1, argumentOfPeriapsis));

        if(eccentricity >= 1){
            // Hyperbolic or parabolic
            float linearE = semiMajorAxis * eccentricity;
            float semiMinorAxis = (float)Math.sqrt(linearE * linearE - Math.pow(semiMajorAxis, 2.0));
            Vector2 center = new Vector2(-linearE, 0);

            renderer.setColor(Color.YELLOW);

            for(float i = 0; i < Constants.ORBIT_RESOLUTION; i++){
                float x1 = (i / Constants.ORBIT_RESOLUTION) * semiMinorAxis;
                float y1 = (float)Math.sqrt((1 + (x1 * x1) / (semiMinorAxis * semiMinorAxis)) * Math.pow(semiMajorAxis, 2.0));
                float x2 = ((i + 1) / Constants.ORBIT_RESOLUTION) * semiMinorAxis;
                float y2 = (float)Math.sqrt((1 + (x2 * x2) / (semiMinorAxis * semiMinorAxis)) * Math.pow(semiMajorAxis, 2.0));

                renderer.rectLine(
                    (-y1 + center.x) * Constants.PPM,
                    (x1 + center.y) * Constants.PPM,
                    (-y2 + center.x) * Constants.PPM,
                    (x2 + center.y) * Constants.PPM,
                    100
                );

                renderer.rectLine(
                    (-y1 + center.x) * Constants.PPM,
                    (-x1 + center.y) * Constants.PPM,
                    (-y2 + center.x) * Constants.PPM,
                    (-x2 + center.y) * Constants.PPM,
                    100
                );
            }
        } else {
            // Elliptic or circular
            float linearE = semiMajorAxis - periapsis;
            float semiMinorAxis = (float)(Math.sqrt(Math.pow(semiMajorAxis, 2.0) - Math.pow(linearE, 2.0)));
            Vector2 center = new Vector2(-linearE, 0);

            renderer.setColor(Color.CYAN);

            for(int i = 0; i < Constants.ORBIT_RESOLUTION; i++){
                float ang1 = (float)((i / (Constants.ORBIT_RESOLUTION - 1.f)) * Math.PI * 2.0);
                float ang2 = (float)((((i + 1) % Constants.ORBIT_RESOLUTION) / (Constants.ORBIT_RESOLUTION - 1.f)) * Math.PI * 2.0);
                Vector2 p1 = new Vector2((float)(Math.cos(ang1) * semiMajorAxis + center.x), (float)(Math.sin(ang1) * semiMinorAxis + center.y)).scl(Constants.PPM);
                Vector2 p2 = new Vector2((float)(Math.cos(ang2) * semiMajorAxis + center.x), (float)(Math.sin(ang2) * semiMinorAxis + center.y)).scl(Constants.PPM);

                renderer.rectLine(p1, p2, 100);
            }
        }
    }

}
