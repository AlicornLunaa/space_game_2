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
    private HashMap<Celestial, ConicSection> celestialConics = new HashMap<>();
    private ArrayList<ConicSection> conics = new ArrayList<>();
    private ArrayList<Double> anomalies = new ArrayList<>();

    private ArrayList<Celestial> parents = new ArrayList<>();
    private ArrayList<Vector2> points = new ArrayList<>();

    // Private functions
    /**
     * Gets conics for every celestial in the system
     */
    private void integrateCelestialConics(){
        for(Celestial child : universe.getCelestials()){
            celestialConics.put(child, new ConicSection(child.getCelestialParent(), child));
        }
    }

    /**
     * Runs an intersection algorithm to get the start and the end of a conic, if there is any
     * @param childConic The conic to intersect with the parent. This is the entity.
     * @param parentConic The conic to test against. This is a celestial.
     * @return Successful finding the start
     */
    private boolean getPatchAnomaly(final ConicSection childConic, final ConicSection parentConic, final double currentTime){
        // Variables
        // TODO: Try converting to a periapsis to periapsis approach, then convert that to a from initial
        final Celestial celestial = (Celestial)parentConic.getChild();
        double intersectionGuess = -1.f;
        double previousSign = Math.signum(childConic.getPosition(childConic.getInitialMeanAnomaly()).dst2(parentConic.getPosition(parentConic.timeToMeanAnomaly(currentTime) + parentConic.getInitialMeanAnomaly())) - Math.pow(celestial.getSphereOfInfluence() / Constants.PPM, 2.0));

        // Find first intersection by the changing value
        for(double i = 0; i < Constants.PATCHED_CONIC_STEPS; i++){
            double meanAnomaly = (i / (Constants.PATCHED_CONIC_STEPS - 1.0)) * 2.0 * Math.PI;
            double parentAnomaly = parentConic.timeToMeanAnomaly(childConic.meanAnomalyToTime(meanAnomaly) + currentTime) + parentConic.getInitialMeanAnomaly();
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
        // TODO: Fix bisection root finding
        double intersection = RootSolver.bisection(0, intersectionGuess, new EquationInterface() {
            @Override
            public double func(double x){
                double parentAnomaly = parentConic.timeToMeanAnomaly(childConic.meanAnomalyToTime(x) + currentTime) + parentConic.getInitialMeanAnomaly();
                double childAnomaly = x + childConic.getInitialMeanAnomaly();
                return (childConic.getPosition(childAnomaly).dst2(parentConic.getPosition(parentAnomaly)) - Math.pow(celestial.getSphereOfInfluence() / Constants.PPM, 2.0));
            }
        });
        // double intersection = intersectionGuess;

        // Add to the list
        anomalies.add(intersection);
        return true;
    }

    /**
     * A recursive function to get every conic in the orbital approximation
     * @param parent The parent celestial
     * @param section The section being checked
     * @param depth How deep the approximation has gone
     * @param currentTime Current time for this iteration
     */
    private void checkSOITransition(final Celestial parent, final ConicSection section, int depth, double currentTime){
        // Checks whether or not the entity in question exits or enters the sphere of influence
        if(parent == null) return;
        if(depth > Constants.PATCHED_CONIC_LIMIT) return;

        if(section.getEccentricity() >= 1.f || section.getApoapsis() >= parent.getSphereOfInfluence() / 2 / Constants.PPM){
            if(parent.getCelestialParent() == null) return;
            
            ConicSection parentConic = celestialConics.get(parent);
            double intersectionGuess = -1.f;
            double previousSign = Math.signum(section.getPosition(0).len2() - Math.pow(section.getParent().getSphereOfInfluence() / Constants.PPM, 2.0));
            
            // Find first intersection by the changing value
            for(double i = 0; i < Constants.PATCHED_CONIC_STEPS; i++){
                double meanAnomaly = (i / (Constants.PATCHED_CONIC_STEPS - 1)) * Math.PI;
                double childAnomaly = meanAnomaly + section.getInitialMeanAnomaly();
                double sign = Math.signum(section.getPosition(childAnomaly).len2() - Math.pow(section.getParent().getSphereOfInfluence() / Constants.PPM, 2.0));

                if(sign != previousSign){
                    previousSign = sign;
                    intersectionGuess = meanAnomaly;
                    break;
                }
            }

            // Error check
            if(intersectionGuess != -1){
                points.add(section.getPosition(section.getInitialMeanAnomaly())); parents.add(parent);
                points.add(section.getPosition(section.getInitialMeanAnomaly() + intersectionGuess)); parents.add(parent);

                // Refine the guesses into answers using a root finding algorithm
                double intersection = RootSolver.bisection(0, intersectionGuess, new EquationInterface() {
                    @Override
                    public double func(double x){
                        return (section.getPosition(x + section.getInitialMeanAnomaly()).len2() - Math.pow(section.getParent().getSphereOfInfluence() / Constants.PPM, 2.0));
                    }
                });
                // double intersection = intersectionGuess;

                double entityAnomaly = intersection + section.getInitialMeanAnomaly();
                double celestialAnomaly = parentConic.timeToMeanAnomaly(section.meanAnomalyToTime(intersection) + currentTime) + parentConic.getInitialMeanAnomaly();

                anomalies.add(intersection);
                //! points.add(section.getPosition(intersection + section.getInitialMeanAnomaly())); parents.add(parent);

                // Get state vectors at the moment of intersection
                Vector2 posAtSOITransfer = section.getPosition(entityAnomaly).add(parentConic.getPosition(celestialAnomaly));
                Vector2 velAtSOITransfer = section.getVelocity(entityAnomaly).add(parentConic.getVelocity(celestialAnomaly));

                // Add new conic relative to the child as a new parent
                conics.add(new ConicSection(parent.getCelestialParent(), entity, posAtSOITransfer, velAtSOITransfer));
                checkSOITransition(parent.getCelestialParent(), conics.get(conics.size() - 1), depth + 1, currentTime + section.meanAnomalyToTime(intersection));
                return;
            }
        }

        for(final Celestial child : parent.getChildren()){
            //ඞ
            ConicSection celestialConic = celestialConics.get(child);

            if(getPatchAnomaly(section, celestialConic, currentTime)){
                double endAnomaly = anomalies.get(anomalies.size() - 1);
                double entityAnomaly = endAnomaly + section.getInitialMeanAnomaly();
                double celestialAnomaly = celestialConic.timeToMeanAnomaly(section.meanAnomalyToTime(endAnomaly) + currentTime) + celestialConic.getInitialMeanAnomaly();

                Vector2 posAtSOITransfer = section.getPosition(entityAnomaly).sub(celestialConic.getPosition(celestialAnomaly));
                Vector2 velAtSOITransfer = section.getVelocity(entityAnomaly).sub(celestialConic.getVelocity(celestialAnomaly));

                // Add new conic relative to the child as a new parent
                conics.add(new ConicSection(child, entity, posAtSOITransfer, velAtSOITransfer));
                checkSOITransition(child, conics.get(conics.size() - 1), depth + 1, currentTime + section.meanAnomalyToTime(endAnomaly));
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
        points.clear();
        parents.clear();

        // Get first orbit line
        Celestial parent = universe.getParentCelestial(entity);
        conics.add(new ConicSection(parent, entity));

        // Search the first orbit for an intersection with a new sphere of influence
        integrateCelestialConics();
        checkSOITransition(parent, conics.get(0), 0, 0);
    }

    public Vector2 positionAtEpoch(float t){
        return null;
    }

    public Vector2 velocityAtEpoch(float t){
        return null;
    }

    public void draw(ShapeRenderer renderer){
        renderer.setColor(Color.GOLD);

        Color lastColor = Color.RED.cpy();
        float a = 1.f / (conics.size() + 1.f);
        float b = 0.f;

        for(int i = 0; i < conics.size(); i++){
            ConicSection c = conics.get(i);
            Color color = lastColor.cpy().lerp(Color.GREEN, b + a);

            if(anomalies.size() > i){
                c.draw(renderer, c.getInitialMeanAnomaly(), anomalies.get(i) + c.getInitialMeanAnomaly(), lastColor, color);
            } else {
                c.draw(renderer);
            }

            lastColor = color.cpy();
            b += a;
        }

        for(int i = 0; i < points.size(); i++){
            Celestial parent = parents.get(i);
            renderer.setTransformMatrix(new Matrix4().set(parent.getUniverseSpaceTransform()));
            renderer.circle(points.get(i).x * Constants.PPM, points.get(i).y * Constants.PPM, 400);
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
