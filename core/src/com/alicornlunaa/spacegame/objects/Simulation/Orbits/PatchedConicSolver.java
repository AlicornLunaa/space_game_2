package com.alicornlunaa.spacegame.objects.Simulation.Orbits;

import java.util.ArrayList;
import java.util.HashMap;

import com.alicornlunaa.spacegame.objects.Entity;
import com.alicornlunaa.spacegame.objects.Simulation.Universe;
import com.badlogic.gdx.math.Vector2;

/**
 * Create a planet state interface that holds a radius, position, and velocity
 * and implement it in the Celestial entity and a fake celestial used for
 * future simulations
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
    

    // Constructor
    public PatchedConicSolver(final Universe universe, Entity entity){
        this.universe = universe;
        this.entity = entity;
        recalculate();
    }

    // Functions
    public void recalculate(){

    }

    public Vector2 positionAtEpoch(float t){
        return null;
    }

    public Vector2 velocityAtEpoch(float t){
        return null;
    }

    // Static functions
    public static PatchedConicSolver getPatchedConics(Entity e){
        return entities.get(e);
    }

    public static PatchedConicSolver solveEntity(final Universe universe, Entity e){
        return entities.put(e, new PatchedConicSolver(universe, e));
    }
    
}
