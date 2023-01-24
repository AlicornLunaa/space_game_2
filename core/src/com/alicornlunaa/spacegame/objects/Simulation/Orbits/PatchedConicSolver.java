package com.alicornlunaa.spacegame.objects.Simulation.Orbits;

import java.util.ArrayList;
import java.util.HashMap;

import com.alicornlunaa.spacegame.objects.Entity;
import com.alicornlunaa.spacegame.objects.Simulation.Celestial;
import com.alicornlunaa.spacegame.objects.Simulation.Universe;
import com.alicornlunaa.spacegame.util.Constants;
import com.alicornlunaa.spacegame.util.RootSolver;
import com.alicornlunaa.spacegame.util.RootSolver.EquationInterface;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

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
    private ArrayList<Vector2> intersectionPos = new ArrayList<>();

    // Private functions
    private void checkSOITransition(final Celestial parent, final ConicSection section, int depth){
        // Checks whether or not the entity in question exits or enters the sphere of influence
        if(parent == null) return;
        if(depth > Constants.PATCHED_CONIC_LIMIT) return;
        if(section.getEccentricity() >= 1.f || section.getSemiMajorAxis() >= parent.getSphereOfInfluence() / Constants.PPM){
            conics.add(new ConicSection(parent.getCelestialParent(), entity));
            checkSOITransition(parent.getCelestialParent(), conics.get(conics.size() - 1), depth + 1);
            return;
        }

        for(final Celestial child : parent.getChildren()){
            //ඞ
            final ConicSection childConic = new ConicSection(child.getCelestialParent(), child);
            float roughGuessInside = -1.f;
            
            for(float i = 0; i < Constants.PATCHED_CONIC_STEPS; i++){
                // Search entire orbit for an intersection
                float meanAnomaly = (float)((i / (Constants.PATCHED_CONIC_STEPS - 1)) * 2.0 * Math.PI);
                Vector2 entPosAtAnomaly = section.getPosition(meanAnomaly);
                Vector2 celestialPosAtAnomaly = childConic.getPosition(meanAnomaly); //! Convert mean anomaly for entity to the planet's mean anomaly
                float distanceToSOI = celestialPosAtAnomaly.dst(entPosAtAnomaly) - (child.getSphereOfInfluence() / Constants.PPM);

                if(distanceToSOI < 0){
                    roughGuessInside = meanAnomaly;
                    break;
                }
            }

            if(roughGuessInside != -1){
                float meanAnomalyToIntersection = RootSolver.bisection(0.f, roughGuessInside, new EquationInterface() {
                    @Override
                    public float func(float x){
                        Vector2 entPosAtAnomaly = section.getPosition(x);
                        Vector2 celestialPosAtAnomaly = childConic.getPosition(x);
                        float distanceToSOI = celestialPosAtAnomaly.dst(entPosAtAnomaly) - (child.getSphereOfInfluence() / Constants.PPM);
                        return distanceToSOI;
                    }
                });

                // Get state vectors at the moment of intersection
                Vector2 posAtSOITransfer = section.getPosition(meanAnomalyToIntersection);
                Vector2 velAtSOITransfer = section.getVelocity(meanAnomalyToIntersection);
                intersectionPos.add(posAtSOITransfer);

                // Add new conic relative to the child as a new parent
                conics.add(new ConicSection(child, entity, posAtSOITransfer, velAtSOITransfer));
                checkSOITransition(child, conics.get(conics.size() - 1), depth + 1);
                return;
            }
        }
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
        checkSOITransition(parent, conics.get(0), 0);
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
            break;
        }

        for(Vector2 p : intersectionPos){
            renderer.circle(p.x * Constants.PPM, p.y * Constants.PPM, 1900);
        }
        intersectionPos.clear();
    }

    // Static functions
    public static PatchedConicSolver getPatchedConics(Entity e){
        return entities.get(e);
    }

    public static PatchedConicSolver solveEntity(final Universe universe, Entity e){
        return entities.put(e, new PatchedConicSolver(universe, e));
    }
    
}
