package com.alicornlunaa.spacegame.objects.Simulation.Orbits;

import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * Math for when eccentricity > 1
 */
class HyperbolicOrbit {

    // Functions
    public static float meanAnomalyToEccentricAnomaly(ConicSection orbit, float ma){
        // Mean anomaly => Eccentric anomaly
        float epsilon = 0.0000001f;
        float guess = ma;

        for(int i = 0; i < 100; i++){
            // M = e sinh(F) - F
            float y = (float)((guess - orbit.getEccentricity() * Math.sinh(guess) - ma) / (1 - orbit.getEccentricity() * Math.cosh(guess)));
            guess = guess - y;

            if(Math.abs(y) < epsilon) break;
        }

        return guess;
    }

    public static float trueAnomalyToEccentricAnomaly(ConicSection orbit, float ta){
        return (float)(2.0 * Math.atan(Math.sqrt((orbit.getEccentricity() - 1) / (orbit.getEccentricity() + 1)) * Math.tan(ta / 2)));
    }

    public static float eccentricAnomalyToMeanAnomaly(ConicSection orbit, float ea){
        return (float)(orbit.getEccentricity() * Math.sinh(ea) - ea);
    }
    
    public static float eccentricAnomalyToTrueAnomaly(ConicSection orbit, float ea){
        return (float)(2.0 * Math.atan(Math.sqrt((orbit.getEccentricity() + 1) / (orbit.getEccentricity() - 1)) * Math.tan(ea / 2.0)));
    }

    public static float meanAnomalyToTrueAnomaly(ConicSection orbit, float ma){
        float ea = meanAnomalyToEccentricAnomaly(orbit, ma);
        return -eccentricAnomalyToTrueAnomaly(orbit, ea);
    }

    public static float trueAnomalyToMeanAnomaly(ConicSection orbit, float ta){
        float ea = trueAnomalyToEccentricAnomaly(orbit, ta);
        return eccentricAnomalyToMeanAnomaly(orbit, ea);
    }

    public static float timeToMeanAnomaly(ConicSection orbit, float t){
        // M = M0 + n(t - t0)
        double mu = Constants.GRAVITY_CONSTANT * orbit.getParent().getBody().getMass();
        double n = Math.sqrt(mu / Math.pow(-orbit.getSemiMajorAxis(), 3.0));
        return (float)(n * t);
    }

    public static void draw(ConicSection orbit, ShapeRenderer render){
        float linearE = orbit.getSemiMajorAxis() * orbit.getEccentricity();
        float semiMinorAxis = (float)Math.sqrt(linearE * linearE - Math.pow(orbit.getSemiMajorAxis(), 2.0));
        Vector2 center = new Vector2(-linearE, 0);

        render.setTransformMatrix(new Matrix4().set(orbit.getParent().getUniverseSpaceTransform()).rotateRad(0, 0, 1, orbit.getArgumentOfPeriapsis()));
        render.setColor(Color.RED);

        for(float i = 0; i < Constants.ORBIT_RESOLUTION; i++){
            float x1 = (i / Constants.ORBIT_RESOLUTION) * semiMinorAxis;
            float y1 = (float)Math.sqrt((1 + (x1 * x1) / (semiMinorAxis * semiMinorAxis)) * Math.pow(orbit.getSemiMajorAxis(), 2.0));

            float x2 = ((i + 1) / Constants.ORBIT_RESOLUTION) * semiMinorAxis;
            float y2 = (float)Math.sqrt((1 + (x2 * x2) / (semiMinorAxis * semiMinorAxis)) * Math.pow(orbit.getSemiMajorAxis(), 2.0));

            render.rectLine(
                (-y1 + center.x) * Constants.PPM,
                (x1 + center.y) * Constants.PPM,
                (-y2 + center.x) * Constants.PPM,
                (x2 + center.y) * Constants.PPM,
                100
            );

            render.rectLine(
                (-y1 + center.x) * Constants.PPM,
                (-x1 + center.y) * Constants.PPM,
                (-y2 + center.x) * Constants.PPM,
                (-x2 + center.y) * Constants.PPM,
                100
            );
        }
    }

    public static Vector2 getVelocityAtAnomalyAbsolute(ConicSection orbit, float t){
        // Kepler to cartesian
        float mu = Constants.GRAVITY_CONSTANT * orbit.getParent().getBody().getMass();
        float futureTrueAnomaly = meanAnomalyToTrueAnomaly(orbit, t);

        double p = orbit.getSemiMajorAxis() * (1 - orbit.getEccentricity() * orbit.getEccentricity()); // Semilatus rectum
        double v = Math.sqrt(mu / p); // Orbital plane velocity

        Vector3 velocity = new Vector3((float)(-v * Math.sin(futureTrueAnomaly)), (float)(v * (orbit.getEccentricity() + Math.cos(futureTrueAnomaly))), 0.f);
        velocity.rotateRad(orbit.getInclination(), 1, 0, 0);
        velocity.rotateRad(orbit.getArgumentOfPeriapsis(), 0, 0, 1);

        return new Vector2(velocity.x, velocity.y);
    }

    public static Vector2 getPositionAtAnomalyAbsolute(ConicSection orbit, float t){
        // Kepler to cartesian
        float futureTrueAnomaly = meanAnomalyToTrueAnomaly(orbit, t);

        double p = orbit.getSemiMajorAxis() * (1 - orbit.getEccentricity() * orbit.getEccentricity()); // Semilatus rectum
        double r = p / (1 + orbit.getEccentricity() * Math.cos(futureTrueAnomaly)); // Orbital plane position

        Vector3 position = new Vector3((float)(r * Math.cos(futureTrueAnomaly)), (float)(r * Math.sin(futureTrueAnomaly)), 0.f); // Orbital plane position
        position.rotateRad(orbit.getInclination(), 1, 0, 0);
        position.rotateRad(orbit.getArgumentOfPeriapsis(), 0, 0, 1);

        return new Vector2(position.x, position.y);
    }

    public static Vector2 getVelocityAtAnomaly(ConicSection orbit, float t){
        // Kepler to cartesian
        float initialMeanAnomaly = trueAnomalyToMeanAnomaly(orbit, orbit.getInitialTrueAnomaly());
        return getVelocityAtAnomalyAbsolute(orbit, initialMeanAnomaly + t);
    }

    public static Vector2 getPositionAtAnomaly(ConicSection orbit, float t){
        // Kepler to cartesian
        float initialMeanAnomaly = trueAnomalyToMeanAnomaly(orbit, orbit.getInitialTrueAnomaly());
        return getPositionAtAnomalyAbsolute(orbit, initialMeanAnomaly + t);
    }

    public static Vector2 getVelocityAtTime(ConicSection orbit, float t){ return getVelocityAtAnomaly(orbit, timeToMeanAnomaly(orbit, t)); }

    public static Vector2 getPositionAtTime(ConicSection orbit, float t){ return getPositionAtAnomaly(orbit, timeToMeanAnomaly(orbit, t)); }

}
