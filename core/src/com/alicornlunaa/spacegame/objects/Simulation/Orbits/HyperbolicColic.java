package com.alicornlunaa.spacegame.objects.Simulation.Orbits;

import com.alicornlunaa.spacegame.objects.Entity;
import com.alicornlunaa.spacegame.objects.Simulation.Celestial;
import com.alicornlunaa.spacegame.util.Constants;
import com.alicornlunaa.spacegame.util.RootSolver;
import com.alicornlunaa.spacegame.util.RootSolver.EquationInterface;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

public class HyperbolicColic extends GenericConic {
    
    public HyperbolicColic(double parentMass, double a, double e, double w, double v, double i) { super(parentMass, a, e, w, v, i); }
    public HyperbolicColic(double parentMass, Vector2 position, Vector2 velocity) { super(parentMass, position, velocity); }
    public HyperbolicColic(Celestial parent, Entity child, Vector2 position, Vector2 velocity) { super(parent, child, position, velocity); }
    public HyperbolicColic(Celestial parent, Entity child) { super(parent, child); }

    private double atanh(double x){ return 0.5 * Math.log((1 + x) / (1 - x)); }

    @Override
    public double meanAnomalyToEccentricAnomaly(final double ma) {
        return RootSolver.newtonian(ma, new EquationInterface() {
            @Override
            public double func(double x){
                return (ma - e * Math.sinh(x) + x);
            }
        });
    }

    @Override
    public double trueAnomalyToEccentricAnomaly(double ta) {
        return 2.0 * atanh(Math.sqrt((e - 1) / (e + 1)) * Math.tan(ta / 2));
    }

    @Override
    public double eccentricAnomalyToMeanAnomaly(double ea) {
        return (e * Math.sinh(ea) - ea);
    }

    @Override
    public double eccentricAnomalyToTrueAnomaly(double ea) {
        return 2.0 * Math.atan(Math.tanh(ea / 2) / Math.sqrt((e - 1) / (e + 1)));
    }

    @Override
    public double timeToMeanAnomaly(double t) {
        return (Math.sqrt(mu / Math.pow(-a, 3.0)) * t);
    }

    @Override
    public double meanAnomalyToTime(double ma) {
        return (ma / Math.sqrt(mu / Math.pow(-a, 3.0)));
    }
    
    @Override
    public void draw(ShapeRenderer renderer, float lineWidth){
        super.draw(renderer, lineWidth);
        
        // Hyperbolic or parabolic
        double linearE = a * e;
        double b = (a * Math.sqrt(e * e - 1));

        for(int i = 0; i < Constants.ORBIT_RESOLUTION; i++){
            double ang1 = (i / (Constants.ORBIT_RESOLUTION - 1.f)) * 2.0 * Math.PI - Math.PI;
            double ang2 = ((i + 1) / (Constants.ORBIT_RESOLUTION - 1.f)) * 2.0 * Math.PI - Math.PI;
            
            Vector2 p1 = new Vector2((float)(linearE - a * Math.cosh(ang1)) * -1, (float)(b * Math.sinh(ang1))).scl(Constants.PPM);
            Vector2 p2 = new Vector2((float)(linearE - a * Math.cosh(ang2)) * -1, (float)(b * Math.sinh(ang2))).scl(Constants.PPM);

            Color segmentStartColor = startColor.cpy().lerp(endColor, (float)ang1);
            Color segmentEndColor = startColor.cpy().lerp(endColor, (float)ang2);

            renderer.rectLine(p1.x, p1.y, p2.x, p2.y, lineWidth, segmentStartColor, segmentEndColor);
        }
    }
    
}
