package com.alicornlunaa.spacegame.objects.Simulation.Orbits;

import java.util.ArrayList;
import java.util.HashMap;

import com.alicornlunaa.spacegame.objects.Entity;
import com.alicornlunaa.spacegame.objects.Simulation.Celestial;
import com.alicornlunaa.spacegame.objects.Simulation.Universe;
import com.alicornlunaa.spacegame.util.Constants;
import com.alicornlunaa.spacegame.util.RootSolver;
import com.alicornlunaa.spacegame.util.RootSolver.EquationInterface;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
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
    private ArrayList<Double> anomalies = new ArrayList<>(); // Every two values is the start and end of a conic

    private ArrayList<Celestial> parents = new ArrayList<>();
    private ArrayList<Vector2> ps = new ArrayList<>();

    // Private functions
    /**
     * Runs an intersection algorithm to get the start and the end of a conic, if there is any
     * @param childConic The conic to intersect with the parent. This is the entity.
     * @param parentConic The conic to test against. This is a celestial.
     * @return Successful finding the start
     */
    private boolean getPatchAnomaly(final ConicSection childConic, final ConicSection parentConic){
        // Variables
        final Celestial celestial = (Celestial)parentConic.getChild();
        double intersectionGuess = -1.f;
        double previousSign = Math.signum(childConic.getChild().getBody().getPosition().dst2(celestial.getBody().getPosition()) - Math.pow(celestial.getSphereOfInfluence() / Constants.PPM, 2.0));

        // Find first intersection by the changing value
        for(double i = 0; i < Constants.PATCHED_CONIC_STEPS; i++){
            double meanAnomaly = (i / (Constants.PATCHED_CONIC_STEPS - 1)) * 2.0 * Math.PI;
            double parentAnomaly = parentConic.timeToMeanAnomaly(childConic.meanAnomalyToTime(meanAnomaly)) + parentConic.getInitialMeanAnomaly();
            double childAnomaly = meanAnomaly + childConic.getInitialMeanAnomaly();
            double sign = Math.signum(childConic.getPosition(childAnomaly).dst2(parentConic.getPosition(parentAnomaly)) - Math.pow(celestial.getSphereOfInfluence() / Constants.PPM, 2.0));

            if(sign != previousSign){
                previousSign = sign;
                intersectionGuess = meanAnomaly;
                break;
            }
        }

        // Error check
        if(intersectionGuess == -1) return false;

        // Refine the guesses into answers using a root finding algorithm
        double intersection = RootSolver.bisection(0, intersectionGuess, new EquationInterface() {
            @Override
            public double func(double x){
                double parentAnomaly = parentConic.timeToMeanAnomaly(childConic.meanAnomalyToTime(x)) + parentConic.getInitialMeanAnomaly();
                double childAnomaly = x + childConic.getInitialMeanAnomaly();
                return (childConic.getPosition(childAnomaly).dst2(parentConic.getPosition(parentAnomaly)) - Math.pow(celestial.getSphereOfInfluence() / Constants.PPM, 2.0));
            }
        });

        // Add to the list
        anomalies.add(intersection);
        return true;
    }

    private void checkSOITransition(final Celestial parent, final ConicSection section, int depth){
        // Checks whether or not the entity in question exits or enters the sphere of influence
        if(parent == null) return;
        if(depth > Constants.PATCHED_CONIC_LIMIT) return;

        parents.clear();
        ps.clear();

        if(section.getEccentricity() >= 1.f || section.getSemiMajorAxis() >= parent.getSphereOfInfluence() / Constants.PPM){
            float roughGuessInside = -1.f;
            
            for(float i = 0; i < Constants.PATCHED_CONIC_STEPS; i++){
                // Search entire orbit for an intersection
                float meanAnomaly = (float)((i / (Constants.PATCHED_CONIC_STEPS - 1)) * 2.0 * Math.PI);
                Vector2 entPosAtAnomaly = section.getPosition(meanAnomaly);
                float distanceToSOI = (parent.getSphereOfInfluence() / Constants.PPM) - entPosAtAnomaly.len();

                if(distanceToSOI < 0){
                    roughGuessInside = meanAnomaly;
                    break;
                }
            }

            if(roughGuessInside != -1){
                double meanAnomalyToIntersection = RootSolver.bisection(0, roughGuessInside, new EquationInterface() {
                    @Override
                    public double func(double x){
                        Vector2 entPosAtAnomaly = section.getPosition(x);
                        double distanceToSOI = (parent.getSphereOfInfluence() / Constants.PPM) - entPosAtAnomaly.len();
                        return distanceToSOI;
                    }
                });

                // Get state vectors at the moment of intersection
                Vector2 posAtSOITransfer = section.getPosition(meanAnomalyToIntersection).add(parent.getBody().getPosition().cpy());
                Vector2 velAtSOITransfer = section.getVelocity(meanAnomalyToIntersection).add(parent.getBody().getLinearVelocity().cpy());

                parents.add(parent);
                ps.add(section.getPosition(meanAnomalyToIntersection).cpy());

                // Add new conic relative to the child as a new parent
                conics.add(new ConicSection(parent.getCelestialParent(), entity, posAtSOITransfer, velAtSOITransfer));
                checkSOITransition(parent.getCelestialParent(), conics.get(conics.size() - 1), depth + 1);
                return;
            }
        }

        for(final Celestial child : parent.getChildren()){
            //ඞ
            ConicSection celestialConic = new ConicSection(parent, child);

            if(getPatchAnomaly(section, celestialConic)){
                double endAnomaly = anomalies.get(anomalies.size() - 1);
                double entityAnomaly = endAnomaly + section.getInitialMeanAnomaly();
                double celestialAnomaly = celestialConic.timeToMeanAnomaly(section.meanAnomalyToTime(endAnomaly)) + celestialConic.getInitialMeanAnomaly();

                Vector2 posAtSOITransfer = section.getPosition(entityAnomaly).sub(celestialConic.getPosition(celestialAnomaly));
                Vector2 velAtSOITransfer = section.getVelocity(entityAnomaly).sub(celestialConic.getVelocity(celestialAnomaly));

                // Add new conic relative to the child as a new parent
                conics.add(new ConicSection(child, entity, posAtSOITransfer, velAtSOITransfer));
                checkSOITransition(child, conics.get(conics.size() - 1), depth + 1);
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
        anomalies.clear();

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
        renderer.setColor(Color.GOLD);

        for(int i = 0; i < conics.size(); i++){
            ConicSection c = conics.get(i);

            if(anomalies.size() > i){
                double endAnomaly = anomalies.get(i) + c.getInitialMeanAnomaly();
                c.draw(renderer, c.getInitialMeanAnomaly(), endAnomaly);

                Vector2 p = c.getPosition(endAnomaly);
                renderer.setTransformMatrix(new Matrix4().set(c.getParent().getUniverseSpaceTransform()));
                renderer.circle(p.x * Constants.PPM, p.y * Constants.PPM, 500);
            } else {
                c.draw(renderer);
            }
        }

        for(int i = 0; i < ps.size(); i++){
            Celestial parent = parents.get(i);
            Vector2 p = ps.get(i);

            renderer.setColor(Color.CORAL);
            renderer.setTransformMatrix(new Matrix4().set(parent.getUniverseSpaceTransform()));
            renderer.circle(p.x * Constants.PPM, p.y * Constants.PPM, 500);
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
