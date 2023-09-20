package com.alicornlunaa.spacegame.objects.simulation.orbits;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Null;

/**
 * A single conic section used for orbits. This is an abstract class to hold
 * the standard data of conic section. Classes will extend this in order to
 * implement the equations for every kind of orbit.
 */
public abstract class GenericConic {
    // Variables
    protected @Null IEntity parent = null;
    protected @Null IEntity child = null;

    protected double mu; // Gravitational constant
    protected double a; // Semi major axis
    protected double e; // Eccentricity
    protected double w; // Argument of periapsis
    protected double v; // True anomaly
    protected double i; // Inclination
    protected double pa; // Periapsis height
    protected double ap; // Apoapsis height

    protected double startMeanAnomaly = 0.0;
    protected double endMeanAnomaly = 2.0 * Math.PI;
    protected double startTime;
    protected double endTime;

    protected boolean dashed = false;
    protected Color startColor = Color.CYAN;
    protected Color endColor = Color.MAGENTA;

    // Constructor
    public GenericConic(double parentMass, double a, double e, double w, double v, double i){
        // Calculate the orbital variables
        mu = Constants.GRAVITY_CONSTANT * parentMass;
        this.a = a;
        this.e = e;
        this.w = w;
        this.v = v;
        this.i = i;

        pa = a * (1 - e);
        ap = a * (1 + e);

        startTime = meanAnomalyToTime(startMeanAnomaly);
        endTime = meanAnomalyToTime(endMeanAnomaly);
    }

    public GenericConic(double parentMass, Vector2 position, Vector2 velocity) {
        // Calculate the orbital variables
        mu = Constants.GRAVITY_CONSTANT * parentMass;

        // Convert 2d position and velocity to 3d vectors in order to unlock the 3d
        // functions
        Vector3 p3d = new Vector3().set(position, 0.f);
        Vector3 v3d = new Vector3().set(velocity, 0.f);

        // Convert from cartesian ECI frame to keplerian orbital elements
        Vector3 h = p3d.cpy().crs(v3d);
        Vector3 ev = (v3d.cpy().crs(h).scl((float)(1 / mu)).sub(p3d.cpy().nor()));

        v = ((p3d.cpy().nor().dot(v3d) >= 0) ? Math.acos(p3d.cpy().nor().dot(ev.cpy().nor()))
                : (2 * Math.PI - Math.acos(p3d.cpy().nor().dot(ev.cpy().nor()))));
        i = Math.acos(h.cpy().nor().z);
        e = ev.len();
        w = Math.atan2(ev.y, ev.x);
        a = (1 / ((2 / p3d.len()) - (v3d.len2() / mu)));

        pa = a * (1 - e);
        ap = a * (1 + e);

        startTime = meanAnomalyToTime(startMeanAnomaly);
        endTime = meanAnomalyToTime(endMeanAnomaly);
    }

    public GenericConic(IEntity parent, IEntity child) {
        this(
            parent.getComponent(BodyComponent.class).body.getMass(),
            child.getComponent(BodyComponent.class).body.getWorldCenter().cpy().sub(parent.getComponent(BodyComponent.class).body.getWorldCenter()),
            child.getComponent(BodyComponent.class).body.getLinearVelocity().cpy().sub(parent.getComponent(BodyComponent.class).body.getLinearVelocity())
        );
        this.parent = parent;
        this.child = child;
    }

    public GenericConic(IEntity parent, IEntity child, Vector2 position, Vector2 velocity) {
        this(parent.getComponent(BodyComponent.class).body.getMass(), position, velocity);
        this.parent = parent;
        this.child = child;
    }

    // Functions
    /**
     * Converts mean anomaly to eccentric anomaly
     * 
     * @param orbit The conic section to use
     * @param ma    The mean anomaly to convert
     * @return The eccentric anomaly
     */
    public abstract double meanAnomalyToEccentricAnomaly(double ma);

    /**
     * Converts the true anomaly to eccentric anomaly
     * 
     * @param orbit The conic section to convert
     * @param ta    The true anomaly to convert
     * @return Eccentric anomaly
     */
    public abstract double trueAnomalyToEccentricAnomaly(double ta);

    /**
     * Converts the eccentric anomaly to mean anomaly
     * 
     * @param orbit The conic section to convert
     * @param ea    The eccentric anomaly to convert
     * @return Mean anomaly
     */
    public abstract double eccentricAnomalyToMeanAnomaly(double ea);

    /**
     * Converts the eccentric anomaly to true anomaly
     * 
     * @param orbit The conic section to convert
     * @param ea    The eccentric anomaly to convert
     * @return True anomaly
     */
    public abstract double eccentricAnomalyToTrueAnomaly(double ea);

    /**
     * Converts mean anomaly to true anomaly. Helper function to bypass eccentric
     * conversion.
     * 
     * @param orbit The conic section to convert
     * @param ma    The mean anomaly to convert
     * @return True anomaly
     */
    public double meanAnomalyToTrueAnomaly(double ma) {
        // Mean anomaly => True anomaly
        double ea = meanAnomalyToEccentricAnomaly(ma);
        return eccentricAnomalyToTrueAnomaly(ea);
    }

    /**
     * Converts true anomaly to mean anomaly. Helper function to bypass eccentric
     * conversion.
     * 
     * @param orbit The conic section to convert
     * @param ta    The true anomaly to convert
     * @return Mean anomaly
     */
    public double trueAnomalyToMeanAnomaly(double ta) {
        // True anomaly => Mean anomaly
        double ea = trueAnomalyToEccentricAnomaly(ta);
        return eccentricAnomalyToMeanAnomaly(ea);
    }

    /**
     * Converts time to mean anomaly
     * 
     * @param orbit The conic section to convert
     * @param t     The time to convert
     * @return Mean anomaly
     */
    public abstract double timeToMeanAnomaly(double t);

    /**
     * Converts mean to time anomaly
     * 
     * @param orbit The conic section to convert
     * @param ma    The mean anomaly to convert
     * @return Time anomaly
     */
    public abstract double meanAnomalyToTime(double ma);

    /**
     * Gets the position at the given mean anomaly
     * @param ma The mean anomaly
     * @return Vector2 position
     */
    public Vector2 getPosition(double ma){
        // Kepler to cartesian
        double ta = meanAnomalyToTrueAnomaly(ma);
        
        double p = a * (1 - e * e); // Semilatus rectum
        double r = p / (1 + e * Math.cos(ta));

        Vector3 position = new Vector3((float)(r * Math.cos(ta)), (float)(r * Math.sin(ta)), 0.f);
        position.rotateRad((float)i, 1, 0, 0);
        position.rotateRad((float)w, 0, 0, 1);

        return new Vector2(position.x, position.y);
    }

    /**
     * Gets the velocity at the given mean anomaly
     * @param ma The mean anomaly
     * @return Vector2 velocity
     */
    public Vector2 getVelocity(double ma){
        // Kepler to cartesian
        double ta = meanAnomalyToTrueAnomaly(ma);
        
        double p = a * (1 - e * e); // Semilatus rectum
        double v = Math.sqrt(mu / p); // Orbital plane velocity
        
        Vector3 velocity = new Vector3((float)(-v * Math.sin(ta)), (float)(v * (e + Math.cos(ta))), 0.f);
        velocity.rotateRad((float)i, 1, 0, 0);
        velocity.rotateRad((float)w, 0, 0, 1);

        return new Vector2(velocity.x, velocity.y);
    }
    
    /**
     * Draws the conic to the screen
     * @param renderer
     * @param lineWidth
     */
    public void draw(ShapeRenderer renderer, float lineWidth){
        // Set drawing transform
        TransformComponent transform = parent.getComponent(TransformComponent.class);
        Matrix4 m = new Matrix4();

        if(parent != null)
            // m.set(parent.getComponent(TransformComponent.class).getMatrix());
            m.translate(transform.position.x, transform.position.y, 0);

        // Render position at initial anomaly
        if(Constants.DEBUG){
            Vector2 p = getPosition(getMeanAnomaly()).scl(Constants.PPM);
            renderer.setTransformMatrix(m);
            renderer.setColor(Color.GOLD);
            renderer.circle(p.x, p.y, 400);

            p = getPosition(getMeanAnomaly()).scl(Constants.PPM);
            renderer.setColor(Color.MAGENTA);
            renderer.circle(p.x, p.y, 800);

            p = getPosition(startMeanAnomaly).scl(Constants.PPM);
            renderer.setColor(Color.RED);
            renderer.circle(p.x, p.y, 800);

            p = getPosition(endMeanAnomaly).scl(Constants.PPM);
            renderer.setColor(Color.LIME);
            renderer.circle(p.x, p.y, 800);
        }
        
        // Final rotation
        m.rotateRad(0, 0, 1, (float)w);
        renderer.setTransformMatrix(m);
    }

    // Setters
    /**
     * Sets the starting mean anomaly and time
     * @param ma The mean anomaly
     */
    public void setStart(double ma){
        startMeanAnomaly = ma;
        startTime = meanAnomalyToTime(ma);
    }

    /**
     * Sets the ending mean anomaly and time
     * @param ma The mean anomaly
     */
    public void setEnd(double ma){
        endMeanAnomaly = ma;
        endTime = meanAnomalyToTime(ma);
    }

    // Getters
    public IEntity getParent(){ return parent; }
    public IEntity getChild(){ return child; }
    public double getSemiMajorAxis() { return a; }
    public double getEccentricity() { return e; }
    public double getArgumentofPeriapsis() { return w; }
    public double getTrueAnomaly() { return v; }
    public double getEccentricAnomaly() { return trueAnomalyToEccentricAnomaly(v); }
    public double getMeanAnomaly() { return trueAnomalyToMeanAnomaly(v); }
    public double getInclination() { return i; }
    public double getPeriapsis() { return pa; }
    public double getApoapsis() { return ap; }
    public double getStartAnomaly(){ return startMeanAnomaly; }
    public double getStartTime(){ return startTime; }
    public double getEndAnomaly(){ return endMeanAnomaly; }
    public double getEndTime(){ return endTime; }
}
