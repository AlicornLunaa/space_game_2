package com.alicornlunaa.spacegame.objects.simulation.orbits;

import com.alicornlunaa.selene_engine.components_old.BodyComponent;
import com.alicornlunaa.selene_engine.components_old.TransformComponent;
import com.alicornlunaa.selene_engine.core.BaseEntity;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.selene_engine.ecs.Registry;
import com.alicornlunaa.spacegame.components.CelestialComponent;
import com.alicornlunaa.spacegame.components.GravityComponent;
import com.alicornlunaa.spacegame.objects.simulation.Celestial;
import com.alicornlunaa.spacegame.objects.simulation.Star;
import com.alicornlunaa.spacegame.objects.simulation.Universe;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/** Holds static functions for different orbital actions */
public class OrbitUtils {
    /**
     * Returns a directional vector to the nearest star entity within the
     * universal simulation.
     * @param u The universe to search in
     * @param e The entity being focused on
     * @return A direction to the nearest star
     */
    public static Vector2 directionToNearestStar(Universe u, IEntity e){
        float minDist = Float.MAX_VALUE;
        Vector2 dir = new Vector2();
        Vector2 absPos = e.getComponent(TransformComponent.class).position.cpy();

        for(Celestial c : u.getCelestials()){
            Vector2 cAbsPos = c.getComponent(TransformComponent.class).position.cpy();
            float curDist = cAbsPos.dst2(absPos);

            if(curDist < minDist && c instanceof Star){
                minDist = curDist;
                dir = cAbsPos.cpy().sub(absPos);
            }
        }

        return dir.nor();
    }
 
    /**
     * Forces stable orbit
     * @param registry
     * @param entity
     */
    public static void createOrbit(Registry registry, IEntity entity){
        TransformComponent entityTrans = entity.getComponent(TransformComponent.class);
        BodyComponent entityBodyComponent = entity.getComponent(BodyComponent.class);
        Vector2 force = new Vector2();
        
        for(int i = 0; i < registry.getEntities().size; i++){
            IEntity otherEntity = registry.getEntity(i);

            if(otherEntity == entity) continue;

            TransformComponent otherTrans = otherEntity.getComponent(TransformComponent.class);
            BodyComponent otherBodyComponent = otherEntity.getComponent(BodyComponent.class);
            GravityComponent otherGravity = otherEntity.getComponent(GravityComponent.class);

            if(otherGravity != null){
                // Get variables
                float radius = entityTrans.position.dst(otherTrans.position);
                float soi = otherGravity.getSphereOfInfluence();
                
                // Prevent insignificant forces
                if(radius > soi)
                    continue;

                // Calculate gravitational force
                Vector2 tangent = entityTrans.position.cpy().sub(otherTrans.position).nor().rotate90(1);
                float magnitude = (float)Math.sqrt(Constants.GRAVITY_CONSTANT * otherBodyComponent.body.getMass() / radius);
    
                force.add(tangent.scl(magnitude));
            }
        }

        entityBodyComponent.body.setLinearVelocity(force);
    }

    /**
     * Returns true ro false depending on if the orbit is decaying
     * @param parent
     * @param entity
     * @return True or false
     */
    public static boolean isOrbitDecaying(Celestial parent, BaseEntity entity){
        // TODO: Switch to velocity approach
        GenericConic gc = OrbitPropagator.getConic(parent, entity);
        return (gc.getPeriapsis() <= (parent.getComponent(CelestialComponent.class).radius * 1.2f) / parent.getComponent(BodyComponent.class).world.getPhysScale()) && !(gc instanceof HyperbolicConic);
    }

    public static Vector3 cartesianToSpherical(Vector3 c){
        float theta = (float)Math.atan2(c.y, c.x);
        float phi = (float)Math.acos(c.z / c.len());
        float p = c.len();
        return new Vector3(p, theta, phi);
    }

    public static Vector3 sphericalToCartesian(Vector3 c){
        float x = (float)(c.x * Math.sin(c.z) * Math.cos(c.y));
        float y = (float)(c.x * Math.sin(c.z) * Math.sin(c.y));
        float z = (float)(c.x * Math.cos(c.z));
        return new Vector3(x, y, z);
    }

    public static Vector3 rectToSphere(Vector3 r){
        float theta = (float)(r.x * 2.0 * Math.PI);
        float radius = r.y;
        float phi = (float)(r.z * 2.0 * Math.PI);
        return new Vector3(radius, theta, phi);
    }

    public static Vector3 sphereToRect(Vector3 s){
        float x = (float)(s.y / (2.0 * Math.PI));
        float y = s.x;
        float z = (float)(s.z / (2.0 * Math.PI));
        return new Vector3(x, y, z);
    }
}
