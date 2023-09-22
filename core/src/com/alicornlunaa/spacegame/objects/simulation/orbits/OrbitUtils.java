package com.alicornlunaa.spacegame.objects.simulation.orbits;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.core.BaseEntity;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.spacegame.objects.simulation.Celestial;
import com.alicornlunaa.spacegame.objects.simulation.Star;
import com.alicornlunaa.spacegame.objects.simulation.Universe;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.math.Vector2;

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
     * Creates a stable orbit for the given object using math equations
     * @param u Universe to affect
     * @param e The entity to stablize into a near-circular orbit
     */
    public static void createOrbit(Universe u, IEntity e){
        Celestial parent = u.getParentCelestial(e);
        if(parent == null) return; // Cant create an orbit for no parent

        TransformComponent celestialTransform = parent.getComponent(TransformComponent.class);
        TransformComponent entityTransform = e.getComponent(TransformComponent.class);
        BodyComponent celestialBodyComponent = parent.getComponent(BodyComponent.class);

        if(celestialTransform == null || entityTransform == null || celestialBodyComponent == null) return; // Cant create an orbit for something that doesnt have a position

        Vector2 tangentDirection = entityTransform.position.cpy().sub(celestialTransform.position).nor().rotateDeg(90);
        float orbitalRadius = entityTransform.position.dst(celestialTransform.position) / celestialBodyComponent.world.getPhysScale();
        float speed = (float)Math.sqrt((Constants.GRAVITY_CONSTANT * celestialBodyComponent.body.getMass()) / orbitalRadius);

        entityTransform.velocity.set(tangentDirection.scl(speed).add(parent.getComponent(TransformComponent.class).velocity));
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
        return (gc.getPeriapsis() <= (parent.getRadius() * 1.2f) / parent.getComponent(BodyComponent.class).world.getPhysScale()) && !(gc instanceof HyperbolicConic);
    }
}
