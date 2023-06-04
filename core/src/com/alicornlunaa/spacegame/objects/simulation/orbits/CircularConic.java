package com.alicornlunaa.spacegame.objects.simulation.orbits;

import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.spacegame.objects.simulation.Celestial;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;

public class CircularConic extends GenericConic {

    public CircularConic(double parentMass, double a, double e, double w, double v, double i) { super(parentMass, a, e, w, v, i); }
    public CircularConic(double parentMass, Vector2 position, Vector2 velocity) { super(parentMass, position, velocity); }
    public CircularConic(Celestial parent, IEntity child, Vector2 position, Vector2 velocity) { super(parent, child, position, velocity); }
    public CircularConic(Celestial parent, IEntity child) { super(parent, child); }

    @Override
    public double meanAnomalyToEccentricAnomaly(double ma) {
        return ma;
    }

    @Override
    public double trueAnomalyToEccentricAnomaly(double ta) {
        return ta;
    }

    @Override
    public double eccentricAnomalyToMeanAnomaly(double ea) {
        return ea;
    }

    @Override
    public double eccentricAnomalyToTrueAnomaly(double ea) {
        return ea;
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
        // Set drawing transform
        Matrix4 m = new Matrix4();

        if(parent != null)
            m.set(parent.getUniverseSpaceTransform());

        // Render position at initial anomaly
        renderer.setTransformMatrix(m);
        
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
