package com.alicornlunaa.spacegame.objects.simulation.orbits;

import java.util.ArrayList;
import java.util.HashMap;

import com.alicornlunaa.selene_engine.core.BaseEntity;
import com.alicornlunaa.spacegame.objects.simulation.Celestial;
import com.alicornlunaa.spacegame.objects.simulation.Universe;
import com.alicornlunaa.spacegame.util.Constants;
import com.alicornlunaa.spacegame.util.RootSolver;
import com.alicornlunaa.spacegame.util.RootSolver.EquationInterface;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

/**
 * Solves an entire orbit with the patched conics algorithm, using different conic sections
 * It will take an entity and a universe, allowing it to simulate every body in the system
 */
public class Orbit {

    // Variables
    private final Universe universe;
    private final BaseEntity entity;

    private ArrayList<GenericConic> conics = new ArrayList<>();
    private HashMap<Celestial, GenericConic> celestialConics = new HashMap<>();

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
    private Double getPatchAnomaly(final GenericConic childConic, final GenericConic parentConic, final double currentTime){
        // Variables
        final Celestial celestial = (Celestial)parentConic.getChild();
        double startIntersectionGuess = -1.f;
        double endIntersectionGuess = -1.f;

        // Find first intersection by the changing value
        for(double i = 0; i < Constants.PATCHED_CONIC_STEPS; i++){
            double meanAnomaly = (i / (Constants.PATCHED_CONIC_STEPS)) * 2.0 * Math.PI;

            if(childConic instanceof HyperbolicConic)
                meanAnomaly = (Math.pow(childConic.getSemiMajorAxis(), 2.0) * meanAnomaly) / 2.0;

            double futureTime = childConic.meanAnomalyToTime(meanAnomaly) + currentTime;
            double parentAnomaly = parentConic.timeToMeanAnomaly(futureTime) + parentConic.getMeanAnomaly();
            double childAnomaly = meanAnomaly + childConic.getMeanAnomaly();
            double distInsideSOI = childConic.getPosition(childAnomaly).dst(parentConic.getPosition(parentAnomaly)) - (celestial.getSphereOfInfluence() / Constants.PPM);

            if(distInsideSOI > 0.05){
                startIntersectionGuess = meanAnomaly;
            }

            if(distInsideSOI < -0.05){
                endIntersectionGuess = meanAnomaly;
                break;
            }
        }

        // Error check
        if(endIntersectionGuess == -1) return null;

        // Refine the guesses into answers using a root finding algorithm
        double intersection = RootSolver.bisection(startIntersectionGuess, endIntersectionGuess, new EquationInterface() {
            @Override
            public double func(double x){
                double futureTime = childConic.meanAnomalyToTime(x) + currentTime;
                double parentAnomaly = parentConic.timeToMeanAnomaly(futureTime) + parentConic.getMeanAnomaly();
                double childAnomaly = x + childConic.getMeanAnomaly();
                return (childConic.getPosition(childAnomaly).dst(parentConic.getPosition(parentAnomaly)) - (celestial.getSphereOfInfluence() / Constants.PPM));
            }
        });
        intersection += childConic.getMeanAnomaly(); // Convert to periapsis-based anomaly
        return intersection;
    }

    /**
     * Gets the exit anomaly for moving up a celestial level
     * @param childConic Conic to check
     * @param parentConic Conic of the parent celestial
     * @param currentTime Current time of the simulation
     * @return null if not exitted, or the anomaly if it did
     */
    private Double getExitAnomaly(final GenericConic childConic, final GenericConic parentConic, final double currentTime){
        Celestial parent = childConic.getParent();

        // Error check
        if((childConic.getEccentricity() < 1.f && Math.abs(childConic.getApoapsis()) < parent.getSphereOfInfluence() / 2 / Constants.PPM) || parent.getCelestialParent() == null) return null;
        
        // Find first intersection by the changing value
        double startIntersectionGuess = -1.f;
        double endIntersectionGuess = -1.f;

        for(double i = 0; i < Constants.PATCHED_CONIC_STEPS; i++){
            double meanAnomaly = (i / (Constants.PATCHED_CONIC_STEPS - 1)) * 2.0 * Math.PI;

            if(childConic instanceof HyperbolicConic)
                meanAnomaly = (i / (Constants.PATCHED_CONIC_STEPS - 1)) * 2.0 * Math.PI * 30;

            double distInsideSOI = childConic.getPosition(meanAnomaly).len() - (parent.getSphereOfInfluence() / Constants.PPM);

            if(distInsideSOI < -0.05){
                startIntersectionGuess = meanAnomaly;
            }

            if(distInsideSOI > 0.05){
                endIntersectionGuess = meanAnomaly;
                break;
            }
        }

        // Error check
        if(endIntersectionGuess == -1) return null;
        
        // Refine the guesses into answers using a root finding algorithm
        double intersection = RootSolver.bisection(startIntersectionGuess, endIntersectionGuess, new EquationInterface() {
            @Override
            public double func(double x){
                return (childConic.getPosition(x).len() - (childConic.getParent().getSphereOfInfluence() / Constants.PPM));
            }
        });

        return intersection;
    }

    /**
     * A recursive function to get every conic in the orbital approximation
     * @param parent The parent celestial
     * @param section The section being checked
     * @param depth How deep the approximation has gone
     * @param currentTime Current time for this iteration
     */
    private void patchConics(final Celestial parent, final GenericConic section, int depth, double currentTime){
        // Checks whether or not the entity in question exits or enters the sphere of influence
        if(parent == null) return;
        if(depth > Constants.PATCHED_CONIC_LIMIT) return;

        GenericConic parentConic = celestialConics.get(parent);
        Double exitAnomaly = getExitAnomaly(section, parentConic, currentTime);

        if(exitAnomaly != null){
            // Get state vectors at the moment of intersection
            double futureTime = currentTime + section.meanAnomalyToTime(exitAnomaly - section.getMeanAnomaly());
            double celestialAnomaly = parentConic.timeToMeanAnomaly(futureTime) + parentConic.getMeanAnomaly();
            Vector2 posAtSOITransfer = section.getPosition(exitAnomaly).add(parentConic.getPosition(celestialAnomaly));
            Vector2 velAtSOITransfer = section.getVelocity(exitAnomaly).add(parentConic.getVelocity(celestialAnomaly));

            section.setStart(section.getMeanAnomaly());
            section.setEnd(exitAnomaly);

            // Add new conic relative to the child as a new parent
            conics.add(OrbitPropagator.getConic(parent.getCelestialParent(), entity, posAtSOITransfer, velAtSOITransfer));
            patchConics(parent.getCelestialParent(), conics.get(conics.size() - 1), depth + 1, futureTime);
            return;
        }

        for(final Celestial child : parent.getChildren()){
            //ඞ
            GenericConic celestialConic = celestialConics.get(child);
            Double enterAnomaly = getPatchAnomaly(section, celestialConic, currentTime);

            if(enterAnomaly != null){
                // Get state vectors at the moment of intersection
                double futureTime = currentTime + section.meanAnomalyToTime(enterAnomaly - section.getMeanAnomaly()); // Subject anomaly to get distance travelled
                double celestialAnomaly = celestialConic.timeToMeanAnomaly(futureTime) + celestialConic.getMeanAnomaly();
                Vector2 posAtSOITransfer = section.getPosition(enterAnomaly).sub(celestialConic.getPosition(celestialAnomaly));
                Vector2 velAtSOITransfer = section.getVelocity(enterAnomaly).sub(celestialConic.getVelocity(celestialAnomaly));

                section.setStart(section.getMeanAnomaly());
                section.setEnd(enterAnomaly);
                
                // Add new conic relative to the child as a new parent
                GenericConic c = OrbitPropagator.getConic(child, entity, posAtSOITransfer, velAtSOITransfer);
                c.setStart(c.getMeanAnomaly());

                if(c instanceof HyperbolicConic)
                    c.setEnd(c.getMeanAnomaly() * -1);

                conics.add(c);
                patchConics(child, c, depth + 1, futureTime);
                return;
            }
        }
    }

    // Constructors
    public Orbit(Universe universe, BaseEntity e){
        this.universe = universe;
        entity = e;
        recalculate();
    }

    // Functions
    public void recalculate(){
        // Reset
        conics.clear();

        // Get first orbit line
        Celestial parent = universe.getParentCelestial(entity);
        conics.add(OrbitPropagator.getConic(parent, entity));

        // Search the first orbit for an intersection with a new sphere of influence
        integrateCelestialConics();
        patchConics(parent, conics.get(0), 1, 0);
    }

    public void draw(ShapeRenderer renderer, float lineWidth){
        for(int i = 0; i < conics.size(); i++){
            GenericConic c = conics.get(i);
            c.draw(renderer, lineWidth);
        }
    }

    public BaseEntity getEntity(){ return entity; }

    public ArrayList<GenericConic> getConics(){ return conics; }

    private Vector2 getPosition(float t, int conicIndex, double timeIntoFuture){
        if(conicIndex == conics.size())
            return conics.get(conics.size() - 1).getPosition(conics.get(conics.size() - 1).timeToMeanAnomaly(t - timeIntoFuture) + conics.get(conics.size() - 1).getMeanAnomaly());

        GenericConic c = conics.get(conicIndex);
        double deltaT = c.meanAnomalyToTime(Math.abs(c.getEndAnomaly() - c.getStartAnomaly()));

        if(t - timeIntoFuture > deltaT){
            return getPosition(t, conicIndex + 1, timeIntoFuture + deltaT);
        }

        return c.getPosition(c.timeToMeanAnomaly(t - timeIntoFuture) + c.getMeanAnomaly());
    }
    
    public Vector2 getPosition(float t){
        if(conics.size() == 0) return null;
        return getPosition(t, 0, 0);    
    }

    private Vector2 getVelocity(float t, int conicIndex, double timeIntoFuture){
        if(conicIndex == conics.size())
            return conics.get(conics.size() - 1).getVelocity(conics.get(conics.size() - 1).timeToMeanAnomaly(t - timeIntoFuture) + conics.get(conics.size() - 1).getMeanAnomaly());
        
        GenericConic c = conics.get(conicIndex);
        double deltaT = c.meanAnomalyToTime(Math.abs(c.getEndAnomaly() - c.getStartAnomaly()));

        if(t - timeIntoFuture > deltaT){
            return getVelocity(t, conicIndex + 1, timeIntoFuture + deltaT);
        }

        return c.getVelocity(c.timeToMeanAnomaly(t - timeIntoFuture) + c.getMeanAnomaly());
    }
    
    public Vector2 getVelocity(float t){
        if(conics.size() == 0) return null;
        return getVelocity(t, 0, 0);    
    }
    
    private Celestial getParent(float t, int conicIndex, double timeIntoFuture){
        if(conicIndex == conics.size()) return conics.get(conics.size() - 1).getParent();

        GenericConic c = conics.get(conicIndex);
        double deltaT = c.meanAnomalyToTime(Math.abs(c.getEndAnomaly() - c.getStartAnomaly()));

        if(t - timeIntoFuture > deltaT){
            return getParent(t, conicIndex + 1, timeIntoFuture + deltaT);
        }

        return c.getParent();
    }
    
    public Celestial getParent(float t){
        if(conics.size() == 0) return null;
        return getParent(t, 0, 0);    
    }

}
