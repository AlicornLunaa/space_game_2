package com.alicornlunaa.spacegame.objects.Simulation.Orbits;

import com.alicornlunaa.spacegame.objects.Entity;
import com.alicornlunaa.spacegame.objects.Simulation.Celestial;
import com.alicornlunaa.spacegame.util.Constants;
import com.alicornlunaa.spacegame.util.RootSolver;
import com.alicornlunaa.spacegame.util.RootSolver.EquationInterface;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/** One conic section of an orbit */
public class ConicSectionOld {

    // Variables
    private Celestial parent;
    private Entity child;

    private double semiMajorAxis = 0.f;
    private double eccentricity = 0.f;
    private double argumentOfPeriapsis = 0.f;
    private double initialTrueAnomaly = 0.f;
    private double inclination = 0.f;
    private double periapsis = 0.f;
    private double apoapsis = 0.f;

    private TextureRegion apoapsisMarker;
    private TextureRegion periapsisMarker;

    // Constructor
    public ConicSectionOld(Celestial parent, Entity child, Vector2 position, Vector2 velocity){
        this.parent = parent;
        this.child = child;

        calculate(position, velocity);
    }

    public ConicSectionOld(Celestial parent, Entity child){
        this(parent, child, child.getBody().getPosition(), child.getBody().getLinearVelocity());
    }
    public static double meanAnomalyToEccentricAnomaly(double ma, double eccentricity){
        final double m = ma;
        final double e = eccentricity;
            
        // Mean anomaly => Eccentric anomaly
        if(eccentricity >= 1){
            // Hyperbolic
            return RootSolver.newtonian(m, new EquationInterface() {
                @Override
                public double func(double x){
                    return (m - e * Math.sinh(x) + x);
                }
            });
        } else {
            return RootSolver.newtonian(m, new EquationInterface() {
                @Override
                public double func(double x){
                    return (m - x + e * Math.sin(x));
                }
            });
        }
    }

    // Anomaly functions
    /**
     * Converts mean anomaly to eccentric anomaly
     * @param orbit The conic section to use
     * @param ma The mean anomaly to convert
     * @return The eccentric anomaly
     */
    public double meanAnomalyToEccentricAnomaly(double ma){
        final double m = ma;
        final double e = eccentricity;
            
        // Mean anomaly => Eccentric anomaly
        if(eccentricity >= 1){
            // Hyperbolic
            return RootSolver.newtonian(m, new EquationInterface() {
                @Override
                public double func(double x){
                    return (m - e * Math.sinh(x) + x);
                }
            });
        } else {
            return RootSolver.newtonian(m, new EquationInterface() {
                @Override
                public double func(double x){
                    return (m - x + e * Math.sin(x));
                }
            });
        }
    }

    /**
     * Converts the true anomaly to eccentric anomaly
     * @param orbit The conic section to convert
     * @param ta The true anomaly to convert
     * @return Eccentric anomaly
     */
    public double trueAnomalyToEccentricAnomaly(double ta){
        // True anomaly => Eccentric anomaly
        if(eccentricity >= 1){
            // Hyperbolic
            return (2.0 * Math.atan(Math.sqrt((eccentricity - 1) / (eccentricity + 1)) * Math.tan(ta / 2)));
        } else {
            // Elliptic
            return Math.atan2(Math.sqrt(1 - Math.pow(eccentricity, 2)) * Math.sin(ta), eccentricity + Math.cos(ta));
        }
    }

    /**
     * Converts the eccentric anomaly to mean anomaly
     * @param orbit The conic section to convert
     * @param ea The eccentric anomaly to convert
     * @return Mean anomaly
     */
    public double eccentricAnomalyToMeanAnomaly(double ea){
        // Eccentric anomaly => Mean anomaly
        if(eccentricity >= 1){
            // Hyperbolic
            return (eccentricity * Math.sinh(ea) - ea);
        } else {
            // Elliptic
            return (ea - eccentricity * Math.sin(ea));
        }
    }

    /**
     * Converts the eccentric anomaly to true anomaly
     * @param orbit The conic section to convert
     * @param ea The eccentric anomaly to convert
     * @return True anomaly
     */
    public double eccentricAnomalyToTrueAnomaly(double ea){
        // Eccentric anomaly => True anomaly
        if(eccentricity >= 1){
            // Hyperbolic
            return (2.0 * Math.atan(Math.sqrt((eccentricity + 1) / (eccentricity - 1)) * Math.tan(ea / 2.0)));
        } else {
            // Elliptic
            return (2.0 * Math.atan(Math.sqrt((1 + eccentricity) / (1 - eccentricity)) * Math.tan(ea / 2.0)));
        }
    }

    /**
     * Converts mean anomaly to true anomaly. Helper function to bypass eccentric conversion.
     * @param orbit The conic section to convert
     * @param ma The mean anomaly to convert
     * @return True anomaly
     */
    public double meanAnomalyToTrueAnomaly(double ma){
        // Mean anomaly => True anomaly
        double ea = meanAnomalyToEccentricAnomaly(ma);
        return eccentricAnomalyToTrueAnomaly(ea);
    }

    /**
     * Converts true anomaly to mean anomaly. Helper function to bypass eccentric conversion.
     * @param orbit The conic section to convert
     * @param ta The true anomaly to convert
     * @return Mean anomaly
     */
    public double trueAnomalyToMeanAnomaly(double ta){
        // True anomaly => Mean anomaly
        double ea = trueAnomalyToEccentricAnomaly(ta);
        return eccentricAnomalyToMeanAnomaly(ea);
    }

    /**
     * Converts time to mean anomaly
     * @param orbit The conic section to convert
     * @param t The time to convert
     * @return Mean anomaly
     */
    public double timeToMeanAnomaly(double t){
        double mu = Constants.GRAVITY_CONSTANT * parent.getBody().getMass();
        double n = Math.sqrt(mu / Math.pow(Math.abs(semiMajorAxis), 3.0));
        return (n * t);
    }

    /**
     * Converts mean to time anomaly
     * @param orbit The conic section to convert
     * @param ma The mean anomaly to convert
     * @return Time anomaly
     */
    public double meanAnomalyToTime(double ma){
        double mu = Constants.GRAVITY_CONSTANT * parent.getBody().getMass();
        double n = Math.sqrt(mu / Math.pow(Math.abs(semiMajorAxis), 3.0));
        return (ma / n);
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

        double mu = Constants.GRAVITY_CONSTANT * parent.getBody().getMass();

        Vector3 pos = new Vector3().set(position, 0.f);
        Vector3 vel = new Vector3().set(velocity, 0.f);

        Vector3 h = pos.cpy().crs(vel);
        Vector3 eV = (vel.cpy().crs(h).scl((float)(1 / mu)).sub(pos.cpy().nor()));
        double anomaly = ((pos.cpy().nor().dot(vel) >= 0) ? Math.acos(pos.cpy().nor().dot(eV.cpy().nor())) : (2 * Math.PI - Math.acos(pos.cpy().nor().dot(eV.cpy().nor()))));
        double i = Math.acos(h.cpy().nor().z);
        double e = eV.len();
        double w = Math.atan2(eV.y, eV.x);
        double a = (1 / ((2 / pos.len()) - (vel.len2() / mu)));

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
    public Vector2 getVelocity(double ma){
        // Kepler to cartesian
        double mu = Constants.GRAVITY_CONSTANT * parent.getBody().getMass();
        double futureTrueAnomaly = meanAnomalyToTrueAnomaly(ma);
        
        double p = semiMajorAxis * (1 - eccentricity * eccentricity); // Semilatus rectum
        double v = Math.sqrt(mu / p); // Orbital plane velocity
        
        Vector3 velocity = new Vector3((float)(-v * Math.sin(futureTrueAnomaly)), (float)(v * (eccentricity + Math.cos(futureTrueAnomaly))), 0.f);
        velocity.rotateRad((float)inclination, 1, 0, 0);
        velocity.rotateRad((float)argumentOfPeriapsis, 0, 0, 1);

        return new Vector2(velocity.x, velocity.y);
    }

    /**
     * Gets the position at the given mean anomaly
     * @param ma The mean anomaly
     * @return Vector2 position
     */
    public Vector2 getPosition(double ma){
        // Kepler to cartesian
        double futureTrueAnomaly = meanAnomalyToTrueAnomaly(ma);
        
        double p = semiMajorAxis * (1 - eccentricity * eccentricity); // Semilatus rectum
        double r = p / (1 + eccentricity * Math.cos(futureTrueAnomaly)); // Orbital plane position

        Vector3 position = new Vector3((float)(r * Math.cos(futureTrueAnomaly)), (float)(r * Math.sin(futureTrueAnomaly)), 0.f); // Orbital plane position
        position.rotateRad((float)inclination, 1, 0, 0);
        position.rotateRad((float)argumentOfPeriapsis, 0, 0, 1);

        return new Vector2(position.x, position.y);
    }

    /**
     * Gets a point on the orbit, ignoring spacing caused by physics
     * @param ma The mean anomaly
     * @return Point on the orbital path
     */
    public Vector2 getPointOnOrbit(double ma){
        if(eccentricity >= 1){
            // Hyperbolic or parabolic
            double linearE = semiMajorAxis * eccentricity;
            double semiMinorAxis = Math.sqrt(linearE * linearE - Math.pow(semiMajorAxis, 2.0));
            Vector2 center = new Vector2(-(float)linearE, 0);

            float x = (float)((ma / (2.0 * Math.PI)) * semiMinorAxis);
            float y = (float)Math.sqrt((1 + (x * x) / (semiMinorAxis * semiMinorAxis)) * Math.pow(semiMajorAxis, 2.0));

            Vector3 v = new Vector3(-y + center.x, x + center.y, 0.f);
            v.rotateRad((float)inclination, 1, 0, 0);
            v.rotateRad((float)argumentOfPeriapsis, 0, 0, 1);
            return new Vector2(v.x, v.y);
        } else {
            // Elliptic or circular
            double linearE = semiMajorAxis - periapsis;
            double semiMinorAxis = (Math.sqrt(Math.pow(semiMajorAxis, 2.0) - Math.pow(linearE, 2.0)));
            Vector2 center = new Vector2(-(float)linearE, 0);
            
            Vector3 v = new Vector3((float)(Math.cos(ma) * semiMajorAxis + center.x), (float)(Math.sin(ma) * semiMinorAxis + center.y), 0.f);
            v.rotateRad((float)inclination, 1, 0, 0);
            v.rotateRad((float)argumentOfPeriapsis, 0, 0, 1);
            return new Vector2(v.x, v.y);
        }
    }

    public Celestial getParent() { return parent; }
    public Entity getChild(){ return child; }

    public double getSemiMajorAxis() { return semiMajorAxis; }
    public double getEccentricity() { return eccentricity; }
    public double getArgumentofPeriapsis() { return argumentOfPeriapsis; }
    public double getInitialTrueAnomaly() { return initialTrueAnomaly; }
    public double getInitialMeanAnomaly(){ return trueAnomalyToMeanAnomaly(initialTrueAnomaly); }
    public double getInclination() { return inclination; }
    public double getPeriapsis() { return periapsis; }
    public double getApoapsis() { return apoapsis; }

    public void draw(Batch batch, double startAnomaly, double endAnomaly){
        if(parent == null) return;

        batch.setTransformMatrix(new Matrix4().set(parent.getUniverseSpaceTransform()).rotateRad(0, 0, 1, (float)argumentOfPeriapsis));

        if(startAnomaly <= 0 && endAnomaly >= 0){
            batch.draw(
                periapsisMarker,
                756 / -2 + (float)getPeriapsis() * Constants.PPM,
                0,
                0,
                0,
                756,
                756,
                1,
                1,
                -(float)Math.toDegrees(argumentOfPeriapsis)
            );
        }

        if(startAnomaly <= Math.PI && endAnomaly >= Math.PI){
            batch.draw(
                apoapsisMarker,
                756 / -2 - (float)getApoapsis() * Constants.PPM,
                0,
                0,
                0,
                756,
                756,
                1,
                1,
                -(float)Math.toDegrees(argumentOfPeriapsis)
            );
        }
    }

    public void draw(ShapeRenderer renderer, float zoom, double startAnomaly, double endAnomaly, Color c1, Color c2){
        if(parent == null) return;

        double eaStart = meanAnomalyToEccentricAnomaly(startAnomaly * (inclination > Math.PI / 2 ? -1.0 : 1.0));
        double eaEnd = meanAnomalyToEccentricAnomaly(endAnomaly * (inclination > Math.PI / 2 ? -1.0 : 1.0));
        renderer.setTransformMatrix(new Matrix4().set(parent.getUniverseSpaceTransform()).rotateRad(0, 0, 1, (float)argumentOfPeriapsis));

        if(eccentricity >= 1){
            // Hyperbolic or parabolic
            double linearE = semiMajorAxis * eccentricity;
            double semiMinorAxis = semiMajorAxis * Math.sqrt(eccentricity * eccentricity - 1);

            renderer.setColor(Color.YELLOW);

            for(double i = 0; i < Constants.ORBIT_RESOLUTION; i++){
                double ang1 = (((i / (Constants.ORBIT_RESOLUTION - 1.f)) * -2.0 * Math.PI) + Math.PI);
                double ang2 = ((((i + 1) / (Constants.ORBIT_RESOLUTION - 1.f)) * -2.0 * Math.PI) + Math.PI);
                Vector2 p1 = new Vector2(-(float)(linearE - semiMajorAxis * Math.cosh(ang1)), (float)(semiMinorAxis * Math.sinh(ang1))).scl(Constants.PPM);
                Vector2 p2 = new Vector2(-(float)(linearE - semiMajorAxis * Math.cosh(ang2)), (float)(semiMinorAxis * Math.sinh(ang2))).scl(Constants.PPM);

                if(p1.len() > getParent().getSphereOfInfluence()) continue;
                if(p2.len() > getParent().getSphereOfInfluence()) continue;

                renderer.rectLine(p1.x, p1.y, p2.x, p2.y, 1.5f * zoom, c1, c2);
            }
        } else {
            // Elliptic or circular
            double linearE = semiMajorAxis * eccentricity;
            double semiMinorAxis = (Math.sqrt(Math.pow(semiMajorAxis, 2.0) - Math.pow(linearE, 2.0)));
            Vector2 center = new Vector2(-(float)linearE, 0);

            for(int i = 0; i < Constants.ORBIT_RESOLUTION; i++){
                double ang1 = ((i / (Constants.ORBIT_RESOLUTION - 1.f)) * (eaEnd - eaStart)) + eaStart;
                double ang2 = (((i + 1) / (Constants.ORBIT_RESOLUTION - 1.f)) * (eaEnd - eaStart)) + eaStart;
                Vector2 p1 = new Vector2((float)(Math.cos(ang1) * semiMajorAxis + center.x), (float)(Math.sin(ang1) * semiMinorAxis + center.y)).scl(Constants.PPM);
                Vector2 p2 = new Vector2((float)(Math.cos(ang2) * semiMajorAxis + center.x), (float)(Math.sin(ang2) * semiMinorAxis + center.y)).scl(Constants.PPM);
                Color mC1 = c1.cpy().lerp(c2, i / (Constants.ORBIT_RESOLUTION - 1.f));
                Color mC2 = c1.cpy().lerp(c2, (i + 1) / (Constants.ORBIT_RESOLUTION - 1.f));
                renderer.rectLine(p1.x, p1.y, p2.x, p2.y, 1.5f * zoom, mC1, mC2);
            }
        }
    }

    public void draw(ShapeRenderer renderer, float zoom){
        renderer.setColor(Color.CYAN);
        draw(renderer, zoom, 0.0, 2.0 * Math.PI, Color.CYAN, Color.CYAN);
    }

}
