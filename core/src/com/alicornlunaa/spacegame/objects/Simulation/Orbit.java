package com.alicornlunaa.spacegame.objects.Simulation;

import javax.management.relation.RelationTypeSupport;

import com.alicornlunaa.spacegame.objects.Entity;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;

@SuppressWarnings("unused")
public class Orbit {
    
    // Variables
    private Celestial parent;
    private Entity child;

    private float semiMajorAxis = 0.f;
    private float eccentricity = 0.f;
    private float argumentOfPeriapsis = 0.f;
    private float trueAnomaly = 0.f;
    private float ascendingNode = 0.f;
    private float inclination = 0.f;
    private float periapsis = 0.f;
    private float apoapsis = 0.f;

    // Private functions
    private float solveKeplersEquation(float M, float e){
        float step = 0.0001f;
        float epsilon = 0.0000001f;
        float guess = 0;

        for(int i = 0; i < 100; i++){
            float y = (float)(M - guess + e * Math.sin(guess));

            if(Math.abs(y) < epsilon) break;

            float slope = (float)((M - (guess + step) + e * Math.sin(guess + step)) - y) / step;
            float s = y / slope;
            guess -= s;
        }

        return guess;
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
        Vector3 pos = new Vector3().set(childBody.getPosition().cpy(), 0.f);
        Vector3 vel = new Vector3().set(childBody.getLinearVelocity().cpy(), 0.f);
        float mu = Constants.GRAVITY_CONSTANT * parentBody.getMass();

        Vector3 h = pos.cpy().crs(vel);
        Vector3 eV = (vel.cpy().crs(h).scl(1 / mu).sub(pos.cpy().nor()));
        Vector3 n = new Vector3(0, 0, 1).crs(h);
        float anomaly = (float)((pos.cpy().nor().dot(vel) >= 0) ? Math.acos(pos.cpy().nor().dot(eV.cpy().nor())) : (2 * Math.PI - Math.acos(pos.cpy().nor().dot(eV.cpy().nor()))));
        float i = (float)Math.acos(h.cpy().nor().z);
        float e = eV.len();
        float O = (float)((n.y >= 0) ? Math.acos(n.cpy().nor().x) : (2 * Math.PI - Math.acos(n.cpy().nor().x)));
        float w = (float)Math.atan2(eV.y, eV.x);
        float a = (float)(1 / ((2 / pos.len()) - (vel.len2() / mu)));

        semiMajorAxis = a;
        eccentricity = e;
        argumentOfPeriapsis = (float)Math.toDegrees(w);
        trueAnomaly = (float)Math.toDegrees(anomaly);
        ascendingNode = (float)Math.toDegrees(O);
        inclination = (float)Math.toDegrees(i);
        periapsis = a * (1 - e);
        apoapsis = a * (1 + e);
    }

    public void draw(ShapeRenderer render){
        float linearE = semiMajorAxis - periapsis;
        float semiMinorAxis = (float)(Math.sqrt(Math.pow(semiMajorAxis, 2.0) - Math.pow(linearE, 2.0)));
        Vector2 center = new Vector2(-linearE, 0);

        render.setTransformMatrix(new Matrix4().set(parent.getUniverseTransform()).rotate(0, 0, 1, argumentOfPeriapsis));
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
        // Kepler to cartesian TODO: FIX
        float meanAnomaly = (float)(t * Math.PI * 2.0);
        float ecceAnomaly = solveKeplersEquation(meanAnomaly, eccentricity);

        // Convert from degrees to radians
        float inclination = (float)Math.toRadians(this.inclination);
        float longitudeOfAscendingNode = (float)Math.toRadians(this.ascendingNode);
        float argumentOfPerigee = (float)Math.toRadians(this.argumentOfPeriapsis);
        float trueAnomaly = (float)Math.toRadians(this.trueAnomaly);

        // Compute the radius and true anomaly
        float radius = (float)(semiMajorAxis * (1 - eccentricity * Math.cos(ecceAnomaly)));
        float trueAnomalyRadians = (float)(Math.atan2(Math.sqrt(1 - eccentricity * eccentricity) * Math.sin(ecceAnomaly), eccentricity + Math.cos(ecceAnomaly)));

        // Compute the position in the orbital plane
        float xOrbital = (float)(radius * Math.cos(trueAnomalyRadians));
        float yOrbital = (float)(radius * Math.sin(trueAnomalyRadians));

        // Rotate the position about the z-axis to the plane of the orbit
        float x = (float)(xOrbital * Math.cos(longitudeOfAscendingNode) - yOrbital * Math.sin(longitudeOfAscendingNode) * Math.cos(inclination));
        float y = (float)(xOrbital * Math.sin(longitudeOfAscendingNode) + yOrbital * Math.cos(longitudeOfAscendingNode) * Math.cos(inclination));

        // Rotate the position about the y-axis to the argument of perigee
        float xRotated = (float)(x * Math.cos(argumentOfPerigee));
        float yRotated = (float)(y);

        // Compute the velocity in the orbital plane
        float vxOrbital = (float)(-semiMajorAxis * Math.sin(ecceAnomaly) / radius);
        float vyOrbital = (float)(semiMajorAxis * Math.sqrt(1 - eccentricity * eccentricity) * Math.cos(ecceAnomaly) / radius);

        // Rotate the velocity about the z-axis to the plane of the orbit
        float vx = (float)(vxOrbital * Math.cos(longitudeOfAscendingNode) - vyOrbital * Math.sin(longitudeOfAscendingNode) * Math.cos(inclination));
        float vy = (float)(vxOrbital * Math.sin(longitudeOfAscendingNode) + vyOrbital * Math.cos(longitudeOfAscendingNode) * Math.cos(inclination));

        // Rotate the velocity about the y-axis to the argument of perigee
        float vxRotated = (float)(vx * Math.cos(argumentOfPerigee));
        float vyRotated = (float)(vy);

        return new Vector2(vxRotated, vyRotated);
    }

    public Vector2 getPositionAtTime(float t){
        // Kepler to cartesian
        float linearE = semiMajorAxis - periapsis;
        float semiMinorAxis = (float)(Math.sqrt(Math.pow(semiMajorAxis, 2.0) - Math.pow(linearE, 2.0)));
        Vector2 center = new Vector2(-linearE, 0);
        
        float meanAnomaly = (float)(t * Math.PI * 2.0);
        float ecceAnomaly = solveKeplersEquation(meanAnomaly, eccentricity);

        return new Vector2((float)Math.cos(ecceAnomaly) * semiMajorAxis + center.x, (float)Math.sin(ecceAnomaly) * semiMinorAxis + center.y);
    }

    public Celestial getCelestial(){ return parent; }

    public Entity getEntity(){ return child; }

    @Override
    public String toString(){
        return String.format("a: %f%ne: %f%nw: %f%nv: %f%nO: %f%ni: %f%n", semiMajorAxis, eccentricity, argumentOfPeriapsis, trueAnomaly, ascendingNode, inclination);
    }

}
