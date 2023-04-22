package com.alicornlunaa.spacegame.objects.simulation.orbits;

import com.alicornlunaa.spacegame.objects.Entity;
import com.alicornlunaa.spacegame.objects.simulation.Celestial;
import com.alicornlunaa.spacegame.util.Constants;
import com.alicornlunaa.spacegame.util.RootSolver;
import com.alicornlunaa.spacegame.util.RootSolver.EquationInterface;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

public class EllipticalConic extends GenericConic {

    public EllipticalConic(double parentMass, double a, double e, double w, double v, double i) { super(parentMass, a, e, w, v, i); }
    public EllipticalConic(double parentMass, Vector2 position, Vector2 velocity) { super(parentMass, position, velocity); }
    public EllipticalConic(Celestial parent, Entity child, Vector2 position, Vector2 velocity) { super(parent, child, position, velocity); }
    public EllipticalConic(Celestial parent, Entity child) { super(parent, child); }

    @Override
    public double meanAnomalyToEccentricAnomaly(final double ma) {
        double initialGuess = (ma + ((e * Math.sin(ma)) / (1 - Math.sin(ma + e) + Math.sin(ma))));

        return RootSolver.newtonian(initialGuess, new EquationInterface() {
            @Override
            public double func(double x){
                return (x - e * Math.sin(x)) - ma;
            }
        }, new EquationInterface() {
            @Override
            public double func(double x){
                return (1 - e * Math.cos(x));
            }
        });
    }

    @Override
    public double trueAnomalyToEccentricAnomaly(double ta) {
        return Math.atan2(Math.sqrt(1 - Math.pow(e, 2)) * Math.sin(ta), e + Math.cos(ta));
    }

    @Override
    public double eccentricAnomalyToMeanAnomaly(double ea) {
        return (ea - e * Math.sin(ea));
    }

    @Override
    public double eccentricAnomalyToTrueAnomaly(double ea) {
        return (2.0 * Math.atan(Math.sqrt((1 + e) / (1 - e)) * Math.tan(ea / 2.0)));
    }

    @Override
    public double timeToMeanAnomaly(double t) {
        return (Math.sqrt(mu / Math.pow(a, 3.0)) * t);
    }

    @Override
    public double meanAnomalyToTime(double ma) {
        return (ma / Math.sqrt(mu / Math.pow(a, 3.0)));
    }

    @Override
    public void draw(ShapeRenderer renderer, float lineWidth){
        super.draw(renderer, lineWidth);
        
        // Elliptic or circular
        double linearE = a * e;
        double semiMinorAxis = (Math.sqrt(Math.pow(a, 2.0) - Math.pow(linearE, 2.0)));

        double startEccentricAnomaly = meanAnomalyToEccentricAnomaly(startMeanAnomaly * (i > Math.PI / 2 ? -1.0 : 1.0));
        double endEccentricAnomaly = meanAnomalyToEccentricAnomaly(endMeanAnomaly * (i > Math.PI / 2 ? -1.0 : 1.0));

        for(int i = 0; i < Constants.ORBIT_RESOLUTION; i++){
            double ang1 = (i / (Constants.ORBIT_RESOLUTION - 1.f)) * (endEccentricAnomaly - startEccentricAnomaly) + startEccentricAnomaly;
            double ang2 = ((i + 1) / (Constants.ORBIT_RESOLUTION - 1.f)) * (endEccentricAnomaly - startEccentricAnomaly) + startEccentricAnomaly;

            Vector2 p1 = new Vector2((float)(Math.cos(ang1) * a - linearE), (float)(Math.sin(ang1) * semiMinorAxis)).scl(Constants.PPM);
            Vector2 p2 = new Vector2((float)(Math.cos(ang2) * a - linearE), (float)(Math.sin(ang2) * semiMinorAxis)).scl(Constants.PPM);

            Color segmentStartColor = startColor.cpy().lerp(endColor, (float)(ang1 / (2.0 * Math.PI)));
            Color segmentEndColor = startColor.cpy().lerp(endColor, (float)(ang2 / (2.0 * Math.PI)));

            renderer.rectLine(p1.x, p1.y, p2.x, p2.y, lineWidth, segmentStartColor, segmentEndColor);
        }
    }

}
