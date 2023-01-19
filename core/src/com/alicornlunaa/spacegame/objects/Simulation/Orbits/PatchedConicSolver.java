package com.alicornlunaa.spacegame.objects.Simulation.Orbits;

import java.util.ArrayList;
import java.util.HashMap;

import com.alicornlunaa.spacegame.objects.Entity;
import com.alicornlunaa.spacegame.objects.Simulation.Celestial;
import com.alicornlunaa.spacegame.objects.Simulation.Universe;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

/**
 *! Create a planet state interface that holds a radius, position, and velocity
 *! and implement it in the Celestial entity and a fake celestial used for
 *! future simulations

    Simplify to solving an equation for distance to each child.
    If distance is less or equal to zero, its inside the new sphere of influence
    Solve this equation with a root solver
 */

/**
 * Solves an entire orbit with the patched conics algorithm, using different conic sections
 * It will take an entity and a universe, allowing it to simulate every body in the system
 */
public class PatchedConicSolver {

    // Variables
    private static final HashMap<Entity, PatchedConicSolver> entities = new HashMap<>();

    private final Universe universe;
    private Entity entity;
    private ArrayList<ConicSection> conics = new ArrayList<>();

    // Private functions
    private boolean checkSOITransition(Celestial parent, ConicSection section){
        // Checks whether or not the entity in question exits or enters the sphere of influence
        if(section.getSemiMajorAxis() >= parent.getSphereOfInfluence() / Constants.PPM) return true;
        if(section.getEccentricity() >= 1.f) return true;
        
        for(float i = 0; i < Constants.PATCHED_CONIC_STEPS; i++){
            double meanAnomaly = (2.0 * Math.PI * (i / (Constants.PATCHED_CONIC_STEPS - 1)));
            Vector2 entPosAtAnomaly = section.getPositionAtAnomaly((float)meanAnomaly);

            for(Celestial child : parent.getChildren()){
                ConicSection celestialConic = new ConicSection(child.getCelestialParent(), child);
                Vector2 celestialPosAtAnomaly = celestialConic.getPositionAtAnomaly((float)meanAnomaly); //! CONVERT TO UNIVERSAL POSITION HERE
                float distanceToSOI = celestialPosAtAnomaly.dst2(entPosAtAnomaly) - (child.getSphereOfInfluence() * child.getSphereOfInfluence());

                //! Solve distanceToSOI <= 0 here
                // if(distanceToSOI <= 0) return true;
            }
        }

        return false;
    }

    // Constructor
    public PatchedConicSolver(final Universe universe, Entity entity){
        this.universe = universe;
        this.entity = entity;
        recalculate();
    }

    // Functions
    public void recalculate(){
        // Reset
        conics.clear();

        // Get first orbit line
        Celestial parent = universe.getParentCelestial(entity);
        conics.add(new ConicSection(parent, entity));

        // Search the first orbit for an intersection with a new sphere of influence
        System.out.println(checkSOITransition(parent, conics.get(0)));
    }

    public Vector2 positionAtEpoch(float t){
        return null;
    }

    public Vector2 velocityAtEpoch(float t){
        return null;
    }

    public void draw(ShapeRenderer renderer){
        for(ConicSection c : conics){
            c.draw(renderer);
        }
    }

    // Static functions
    public static PatchedConicSolver getPatchedConics(Entity e){
        return entities.get(e);
    }

    public static PatchedConicSolver solveEntity(final Universe universe, Entity e){
        return entities.put(e, new PatchedConicSolver(universe, e));
    }
    
}
