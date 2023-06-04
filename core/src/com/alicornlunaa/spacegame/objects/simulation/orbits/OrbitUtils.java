package com.alicornlunaa.spacegame.objects.simulation.orbits;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.core.BaseEntity;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.spacegame.objects.planet.Planet;
import com.alicornlunaa.spacegame.objects.simulation.Celestial;
import com.alicornlunaa.spacegame.objects.simulation.Star;
import com.alicornlunaa.spacegame.objects.simulation.Universe;
import com.alicornlunaa.spacegame.phys.PlanetaryPhysWorld;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.math.Vector2;

/** Holds static functions for different orbital actions */
public class OrbitUtils {

    // Space functions
    /**
     * Returns the position of the entity local to the center of the universe.
     * This will be prone to float point precision errors, use with caution.
     * @param u The universe to search in
     * @param e The entity to transform into universe-space
     * @return A 2d vector containing the universe space coordinates
     */
    public static Vector2 getUniverseSpacePosition(Universe universe, IEntity entity){
        TransformComponent transform = entity.getComponent(TransformComponent.class);
        BodyComponent bodyComponent = entity.getComponent(BodyComponent.class);

        Celestial parentOfEntity = universe.getParentCelestial(entity);
        Vector2 systemSpacePosition = transform.position.cpy();

        if(parentOfEntity == null) return systemSpacePosition; // No parent, its in the universe world, therefore coordinates already global

        if(entity instanceof Celestial){
            // Its a celestial body, return its position directly
            return new Vector2().mul(((Celestial)entity).getUniverseSpaceTransform());
        }

        if(bodyComponent != null && bodyComponent.world instanceof PlanetaryPhysWorld){
            // Its on a rectangular phys world, return the position converted
            Planet planet = (Planet)parentOfEntity;
            systemSpacePosition.set(planet.getSpacePosition(entity));
        }

        return systemSpacePosition.mul(parentOfEntity.getUniverseSpaceTransform());
    }

    /**
     * Returns the center position of the entity local to the center of the universe.
     * This will be prone to float point precision errors, use with caution.
     * @param u The universe to search in
     * @param e The entity to transform into universe-space
     * @return A 2d vector containing the universe space coordinates
     */
    public static Vector2 getUniverseSpaceCenter(Universe u, BaseEntity e){
        Celestial parentOfEntity = u.getParentCelestial(e);
        TransformComponent transform = e.getComponent(TransformComponent.class);
        BodyComponent bodyComponent = e.getComponent(BodyComponent.class);
        Vector2 systemSpacePosition = transform.position.cpy();//bodyComponent.body.getWorldCenter().cpy().scl(e.getPhysScale());

        if(parentOfEntity == null) return systemSpacePosition; // No parent, its in the universe world.

        if(e instanceof Celestial){
            // Its a celestial body, return its position directly
            return new Vector2().mul(((Celestial)e).getUniverseSpaceTransform());
        }

        if(bodyComponent != null && bodyComponent.world instanceof PlanetaryPhysWorld){
            // Its on a rectangular phys world, return the position converted
            Planet planet = (Planet)parentOfEntity;
            systemSpacePosition.set(planet.getSpacePosition(e));
        }

        return systemSpacePosition.mul(parentOfEntity.getUniverseSpaceTransform());
    }

    /**
     * Returns the position of the entity local to the center of the universe.
     * This will be prone to float point precision errors, use with caution.
     * @param u The universe to search in
     * @param e The entity to transform into universe-space
     * @param localPos The local position to transform to this entity's parent
     * @return A 2d vector containing the universe space coordinates
     */
    public static Vector2 getUniverseSpacePosition(Universe u, BaseEntity e, Vector2 localPos){
        Celestial parentOfEntity = u.getParentCelestial(e);
        BodyComponent bodyComponent = e.getComponent(BodyComponent.class);

        if(parentOfEntity == null) return localPos; // No parent, its in the universe world.

        if(e instanceof Celestial){
            // Its a celestial body, return its position directly
            return localPos.mul(((Celestial)e).getUniverseSpaceTransform());
        }

        if(bodyComponent != null && bodyComponent.world instanceof PlanetaryPhysWorld){
            // Its on a rectangular phys world, return the position converted
            Planet planet = (Planet)parentOfEntity;
            localPos.set(planet.getSpacePosition(e));
        }

        return localPos.mul(parentOfEntity.getUniverseSpaceTransform());
    }

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
        Vector2 absPos = getUniverseSpacePosition(u, e);

        for(Celestial c : u.getCelestials()){
            Vector2 cAbsPos = getUniverseSpacePosition(u, c);
            float curDist = cAbsPos.dst2(absPos);

            if(curDist < minDist && c instanceof Star){
                minDist = curDist;
                dir = cAbsPos.cpy().sub(absPos);
            }
        }

        return dir.nor();
    }
 
    /**
     * Gets the celestial from the universe space coordinates.
     * @param u The universe to search
     * @param universeSpacePosition Universe space coordinates
     * @return Parent celestial for universal coordinates
     */
    public static Celestial getParentFromUniverseSpace(Universe u, Vector2 universeSpacePosition){
        // Simple search
        Celestial closest = null;
        float minDist = Float.MAX_VALUE;

        for(Celestial c : u.getCelestials()){
            float curDist = c.transform.position.dst2(universeSpacePosition);

            if(curDist < minDist && curDist < Math.pow(c.getSphereOfInfluence(), 2)){
                closest = c;
                minDist = curDist;
            }
        }
        
        return closest;
    }

    /**
     * Creates a stable orbit for the given object using math equations
     * @param u Universe to affect
     * @param e The entity to stablize into a near-circular orbit
     */
    public static void createOrbit(Universe u, IEntity e){
        TransformComponent transform = e.getComponent(TransformComponent.class);
        Celestial parent = u.getParentCelestial(e);
        if(parent == null) return; // Cant create an orbit for no parent
        if(transform == null) return; // Cant create an orbit for something that doesnt have a position
        if(!parent.hasComponent(BodyComponent.class)) return; // Cant create an orbit without physics

        BodyComponent parentBodyComponent = parent.getComponent(BodyComponent.class);

        Vector2 tangentDirection = transform.position.cpy().nor().rotateDeg(90);
        float orbitalRadius = transform.position.len() / parentBodyComponent.world.getPhysScale();
        float speed = (float)Math.sqrt((Constants.GRAVITY_CONSTANT * parentBodyComponent.body.getMass()) / orbitalRadius);

        transform.velocity.set(tangentDirection.scl(speed));
    }

    /**
     * Returns true ro false depending on if the orbit is decaying
     * @param parent
     * @param entity
     * @return True or false
     */
    public static boolean isOrbitDecaying(Celestial parent, BaseEntity entity){
        GenericConic gc = OrbitPropagator.getConic(parent, entity);
        return (gc.getPeriapsis() <= (parent.getRadius() * 1.2f) / parent.bodyComponent.world.getPhysScale()) && !(gc instanceof HyperbolicConic);
    }

}
