package com.alicornlunaa.spacegame.objects.Simulation.Orbits;

import com.alicornlunaa.spacegame.util.RootSolver;
import com.alicornlunaa.spacegame.util.RootSolver.EquationInterface;
import com.badlogic.gdx.math.Vector2;

public class EllipticalConic extends GenericConic {

    public EllipticalConic(double parentMass, Vector2 position, Vector2 velocity) { super(parentMass, position, velocity); }

    @Override
    public double meanAnomalyToEccentricAnomaly(final double ma) {
        return RootSolver.newtonian(ma, new EquationInterface() {
            @Override
            public double func(double x){
                return (ma - x + e * Math.sin(x));
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
        return (Math.sqrt(mu / Math.pow(Math.abs(a), 3.0)) * t);
    }

    @Override
    public double meanAnomalyToTime(double ma) {
        return (ma / Math.sqrt(mu / Math.pow(Math.abs(a), 3.0)));
    }

}
