package com.alicornlunaa.spacegame.objects.Simulation.Orbits;

import com.alicornlunaa.spacegame.objects.Entity;
import com.alicornlunaa.spacegame.objects.Simulation.Celestial;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class OrbitPropagator {

    private enum ConicType { ELLIPTIC, HYPERBOLIC, PARABOLIC, CIRCULAR };
    private static final double ECCENTRICITY_FOR_CIRCULAR = 1e-4;

    private static ConicType getConicType(double parentMass, Vector2 position, Vector2 velocity){
        // Convert 2d position and velocity to 3d vectors in order to unlock the 3d
        // functions
        Vector3 p3d = new Vector3().set(position, 0.f);
        Vector3 v3d = new Vector3().set(velocity, 0.f);

        // Convert from cartesian ECI frame to keplerian orbital elements
        Vector3 h = p3d.cpy().crs(v3d);
        Vector3 ev = (v3d.cpy().crs(h).scl((float)(1 / (parentMass * Constants.GRAVITY_CONSTANT))).sub(p3d.cpy().nor()));

        // Returns
        if(ev.len() >= 1.f){
            return ConicType.HYPERBOLIC;
        } else if(ev.len() <= ECCENTRICITY_FOR_CIRCULAR){
            return ConicType.CIRCULAR;
        }
        
        return ConicType.ELLIPTIC;
    }

    public static GenericConic getConic(double parentMass, double a, double e, double w, double v, double i){
        if(e >= 1.f){
            return new HyperbolicConic(parentMass, a, e, w, v, i);
        } else if(e >= ECCENTRICITY_FOR_CIRCULAR){
            return new CircularConic(parentMass, a, e, w, v, i);
        }

        return new EllipticalConic(parentMass, a, e, w, v, i);
    }

    public static GenericConic getConic(double parentMass, Vector2 position, Vector2 velocity){
        ConicType type = getConicType(parentMass, position, velocity);

        if(type == ConicType.HYPERBOLIC){
            return new HyperbolicConic(parentMass, position, velocity);
        } else if(type == ConicType.CIRCULAR){
            return new CircularConic(parentMass, position, velocity);
        }

        return new EllipticalConic(parentMass, position, velocity);
    }

    public static GenericConic getConic(Celestial parent, Entity child, Vector2 position, Vector2 velocity){
        if(parent == null) return null;

        ConicType type = getConicType(parent.getBody().getMass(), position, velocity);
        
        if(type == ConicType.HYPERBOLIC){
            return new HyperbolicConic(parent, child, position, velocity);
        } else if(type == ConicType.CIRCULAR){
            return new CircularConic(parent, child, position, velocity);
        }

        return new EllipticalConic(parent, child, position, velocity);
    }

    public static GenericConic getConic(Celestial parent, Entity child){
        if(parent == null) return null;

        ConicType type = getConicType(parent.getBody().getMass(), child.getBody().getPosition(), child.getBody().getLinearVelocity());

        if(type == ConicType.HYPERBOLIC){
            return new HyperbolicConic(parent, child);
        } else if(type == ConicType.CIRCULAR){
            return new CircularConic(parent, child);
        }

        return new EllipticalConic(parent, child);
    }
    
}
