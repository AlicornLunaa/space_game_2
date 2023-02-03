package com.alicornlunaa.spacegame.objects.Simulation.Orbits;

import java.util.ArrayList;
import java.util.HashMap;

import com.alicornlunaa.spacegame.App;
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
    private final App game;
    private final Universe universe;
    private Entity entity;
    private HashMap<Celestial, GenericConic> celestialConics = new HashMap<>();
    private ArrayList<GenericConic> conics = new ArrayList<>();
    private ArrayList<Double> anomalies = new ArrayList<>();

    // Private functions
    /**
     * Gets conics for every celestial in the system
     */
    private void integrateCelestialConics(){
        for(Celestial child : universe.getCelestials()){
            celestialConics.put(child, OrbitPropagator.getConic(child.getCelestialParent(), child));
        }
    }

    /**
     * Runs an intersection algorithm to get the start and the end of a conic, if there is any
     * @param childConic The conic to intersect with the parent. This is the entity.
     * @param parentConic The conic to test against. This is a celestial.
     * @return Successful finding the start
     */
    private boolean getPatchAnomaly(final GenericConic childConic, final GenericConic parentConic, final double currentTime){
        // Variables
        final Celestial celestial = (Celestial)parentConic.getChild();
        double intersectionGuess = -1.f;

        // Find first intersection by the changing value
        for(double i = 0; i < Constants.PATCHED_CONIC_STEPS; i++){
            double meanAnomaly = (i / (Constants.PATCHED_CONIC_STEPS)) * 2.0 * Math.PI;
            double parentAnomaly = parentConic.timeToMeanAnomaly(childConic.meanAnomalyToTime(meanAnomaly) + currentTime) + parentConic.getMeanAnomaly();
            double childAnomaly = meanAnomaly + childConic.getMeanAnomaly();
            double distInsideSOI = childConic.getPosition(childAnomaly).dst(parentConic.getPosition(parentAnomaly)) - (celestial.getSphereOfInfluence() / Constants.PPM);

            if(distInsideSOI <= -0.05){
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
                double parentAnomaly = parentConic.timeToMeanAnomaly(childConic.meanAnomalyToTime(x) + currentTime) + parentConic.getMeanAnomaly();
                double childAnomaly = x + childConic.getMeanAnomaly();
                return (childConic.getPosition(childAnomaly).dst(parentConic.getPosition(parentAnomaly)) - (celestial.getSphereOfInfluence() / Constants.PPM));
            }
        });

        // Add to the list
        anomalies.add(intersection + childConic.getMeanAnomaly());
        return true;
    }

    /**
     * A recursive function to get every conic in the orbital approximation
     * @param parent The parent celestial
     * @param section The section being checked
     * @param depth How deep the approximation has gone
     * @param currentTime Current time for this iteration
     */
    private void checkSOITransition(final Celestial parent, final GenericConic section, int depth, double currentTime){
        // Checks whether or not the entity in question exits or enters the sphere of influence
        if(parent == null) return;
        if(depth > Constants.PATCHED_CONIC_LIMIT) return;
        if(section.getPeriapsis() < section.getParent().getRadius() / Constants.PPM) return;

        if((section.getEccentricity() >= 1.f || Math.abs(section.getApoapsis()) >= parent.getSphereOfInfluence() / 2 / Constants.PPM) && parent.getCelestialParent() != null){
            GenericConic parentConic = celestialConics.get(parent);
            double intersectionGuess = -1.f;
            
            // Find first intersection by the changing value
            for(double i = 0; i < Constants.PATCHED_CONIC_STEPS; i++){
                double meanAnomaly = (i / (Constants.PATCHED_CONIC_STEPS - 1)) * 2.0 * Math.PI;
                double distInsideSOI = section.getPosition(meanAnomaly).len() - (section.getParent().getSphereOfInfluence() / Constants.PPM);

                if(distInsideSOI >= 0.05){
                    intersectionGuess = meanAnomaly;
                    break;
                }
            }

            // Error check
            if(intersectionGuess != -1){
                // Refine the guesses into answers using a root finding algorithm
                double intersection = RootSolver.bisection(0, intersectionGuess, new EquationInterface() {
                    @Override
                    public double func(double x){
                        return (section.getPosition(x).len() - (section.getParent().getSphereOfInfluence() / Constants.PPM));
                    }
                });
                double celestialAnomaly = parentConic.timeToMeanAnomaly(section.meanAnomalyToTime(intersection - section.getMeanAnomaly()) + currentTime) + parentConic.getMeanAnomaly();
                anomalies.add(intersection);

                // Get state vectors at the moment of intersection
                Vector2 posAtSOITransfer = section.getPosition(intersection).add(parentConic.getPosition(celestialAnomaly));
                Vector2 velAtSOITransfer = section.getVelocity(intersection).add(parentConic.getVelocity(celestialAnomaly));

                // Add new conic relative to the child as a new parent
                conics.add(OrbitPropagator.getConic(parent.getCelestialParent(), entity, posAtSOITransfer, velAtSOITransfer));
                checkSOITransition(parent.getCelestialParent(), conics.get(conics.size() - 1), depth + 1, currentTime + section.meanAnomalyToTime(intersection - section.getMeanAnomaly()));
                return;
            }
        }

        for(final Celestial child : parent.getChildren()){
            //ඞ
            GenericConic celestialConic = celestialConics.get(child);

            if(getPatchAnomaly(section, celestialConic, currentTime)){
                double endAnomaly = anomalies.get(anomalies.size() - 1);
                double celestialAnomaly = celestialConic.timeToMeanAnomaly(section.meanAnomalyToTime(endAnomaly - section.getMeanAnomaly()) + currentTime) + celestialConic.getMeanAnomaly();

                Vector2 posAtSOITransfer = section.getPosition(endAnomaly).sub(celestialConic.getPosition(celestialAnomaly));
                Vector2 velAtSOITransfer = section.getVelocity(endAnomaly).sub(celestialConic.getVelocity(celestialAnomaly));
                
                // Add new conic relative to the child as a new parent
                conics.add(OrbitPropagator.getConic(child, entity, posAtSOITransfer, velAtSOITransfer));
                checkSOITransition(child, conics.get(conics.size() - 1), depth + 1, currentTime + section.meanAnomalyToTime(endAnomaly - section.getMeanAnomaly()));
                return;
            }
        }
    }

    // Constructor
    public PatchedConicSolver(final App game, final Universe universe, Entity entity){
        this.game = game;
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
        conics.add(OrbitPropagator.getConic(parent, entity));

        // Search the first orbit for an intersection with a new sphere of influence
        integrateCelestialConics();
        checkSOITransition(parent, conics.get(0), 0, 0);
    }

    public GenericConic getConicAtEpoch(float t){
        if(conics.size() == 0) return null;

        // Get which conic will be intercepted at this time
        double timeIntoFuture = 0.0;
        int conicIndex = 0;
        while(anomalies.size() > conicIndex){
            GenericConic c = conics.get(conicIndex);
            double normalizedInitAnomaly = (2.0 * Math.PI) + (c.getMeanAnomaly() % (2.0 * Math.PI));
            double normalizedAnomaly = (2.0 * Math.PI) + (anomalies.get(conicIndex) % (2.0 * Math.PI));
            double timeToIntersection = c.meanAnomalyToTime(Math.abs(normalizedInitAnomaly - normalizedAnomaly));

            if(t - timeIntoFuture > timeToIntersection){
                // Not this conic, try the next
                timeIntoFuture += timeToIntersection;
                conicIndex++;
            } else {
                break;
            }
        }

        return conics.get(conicIndex);
    }

    public Celestial getParentAtEpoch(float t){
        if(conics.size() == 0) return null;
        return getConicAtEpoch(t).getParent();
    }

    public Vector2 getPositionAtEpoch(float t){
        if(conics.size() == 0) return null;

        // Get which conic will be intercepted at this time
        double timeIntoFuture = 0.0;
        int conicIndex = 0;
        while(anomalies.size() > conicIndex){
            GenericConic c = conics.get(conicIndex);
            double normalizedInitAnomaly = (2.0 * Math.PI) + (c.getMeanAnomaly() % (2.0 * Math.PI));
            double normalizedAnomaly = (2.0 * Math.PI) + (anomalies.get(conicIndex) % (2.0 * Math.PI));
            double timeToIntersection = c.meanAnomalyToTime(Math.abs(normalizedInitAnomaly - normalizedAnomaly));

            if(t - timeIntoFuture > timeToIntersection){
                // Not this conic, try the next
                timeIntoFuture += timeToIntersection;
                conicIndex++;
            } else {
                break;
            }
        }

        GenericConic c = conics.get(conicIndex);
        double anomalyForConic = c.timeToMeanAnomaly(t - timeIntoFuture) + c.getMeanAnomaly();
        return c.getPosition(anomalyForConic);
    }

    public Vector2 getVelocityAtEpoch(float t){
        if(conics.size() == 0) return null;

        // Get which conic will be intercepted at this time
        double timeIntoFuture = 0.0;
        int conicIndex = 0;
        while(anomalies.size() > conicIndex){
            GenericConic c = conics.get(conicIndex);
            double normalizedInitAnomaly = (2.0 * Math.PI) + (c.getMeanAnomaly() % (2.0 * Math.PI));
            double normalizedAnomaly = (2.0 * Math.PI) + (anomalies.get(conicIndex) % (2.0 * Math.PI));
            double timeToIntersection = c.meanAnomalyToTime(Math.abs(normalizedInitAnomaly - normalizedAnomaly));

            if(t - timeIntoFuture > timeToIntersection){
                // Not this conic, try the next
                timeIntoFuture += timeToIntersection;
                conicIndex++;
            } else {
                break;
            }
        }
        
        // Start at the first conic
        GenericConic c = conics.get(conicIndex);
        double anomalyForConic = c.timeToMeanAnomaly(t - timeIntoFuture) + c.getMeanAnomaly();
        return c.getVelocity(anomalyForConic);
    }

    public Entity getEntity(){ return entity; }

    public void draw(ShapeRenderer renderer, float zoom){
        renderer.setColor(Color.GOLD);

        Color lastColor = Color.RED.cpy();
        float a = 1.f / (conics.size() + 1.f);
        float b = 0.f;

        for(int i = 0; i < conics.size(); i++){
            GenericConic c = conics.get(i);
            Color color = lastColor.cpy().lerp(Color.GREEN, b + a);

            if(anomalies.size() > i){
                c.draw(renderer, 1.5f * zoom);

                Vector2 p = c.getPosition(anomalies.get(i));
                renderer.setTransformMatrix(new Matrix4().set(c.getParent().getUniverseSpaceTransform()));
                renderer.circle(p.x * Constants.PPM, p.y * Constants.PPM, 500);
            } else {
                c.draw(renderer, zoom);
            }

            lastColor = color.cpy();
            b += a;
        }
    }
    
}
