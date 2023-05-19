package com.alicornlunaa.spacegame.objects.simulation;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.engine.core.BaseEntity;
import com.alicornlunaa.spacegame.engine.phys.CelestialPhysWorld;
import com.alicornlunaa.spacegame.engine.phys.PhysWorld;
import com.alicornlunaa.spacegame.engine.phys.PlanetaryPhysWorld;
import com.alicornlunaa.spacegame.objects.Player;
import com.alicornlunaa.spacegame.objects.planet.Planet;
import com.alicornlunaa.spacegame.objects.simulation.orbits.GenericConic;
import com.alicornlunaa.spacegame.objects.simulation.orbits.Orbit;
import com.alicornlunaa.spacegame.objects.simulation.orbits.OrbitPropagator;
import com.alicornlunaa.spacegame.objects.simulation.orbits.OrbitUtils;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;

/**
 * This is the main class that will hold all the celestial bodies and
 * entities designed to interact with them.
 */
public class Universe extends Actor {

    // Variables
    private final App game;

    private Array<Celestial> celestials = new Array<>();
    private Array<GenericConic> celestialPaths = new Array<>();
    private Array<Orbit> entityPaths = new Array<>();

    private PhysWorld universalWorld;

    private float timeWarpAccumulator = 0.0f;
    private float currentFuture = 0.0f;
    private float timewarp = 1.0f;

    // Private functions
    private boolean checkCelestialTransfer(Celestial celestial){
        // Check whether or not this entity has a celestial parent or not
        Celestial curParent = getParentCelestial(celestial);
        Array<Celestial> celestialsToCheck = (curParent == null) ? celestials : curParent.getChildren();

        // Find closest celestial with proper SOI parameters
        Celestial parent = null;
        float minDist = Float.MAX_VALUE;

        for(Celestial c : celestialsToCheck){
            // Only transfer to next-level celestials.
            if(curParent == null && c.getCelestialParent() != null) continue;

            float curDist = c.getPosition().dst2(celestial.getPosition());
            if(curDist < minDist && curDist < Math.pow(c.getSphereOfInfluence(), 2) && c != celestial){
                parent = c;
                minDist = curDist;
            }
        }

        if(parent == null) return false;

        if(curParent != null){
            curParent.getChildren().removeValue(celestial, true);
        }

        // Update the target's parent to be the parent celestial
        celestial.setCelestialParent(parent);
        parent.getChildren().add(celestial);

        // Convert the target celestial's body to the new Box2D world
        celestial.setPosition(celestial.getPosition().mul(parent.getTransform().inv()));
        game.simulation.addEntity(parent.getInfluenceWorld(), celestial);

        return true;
    }

    // Constructor
    public Universe(final App game){
        this.game = game;
        universalWorld = game.simulation.addWorld(new CelestialPhysWorld(game, null, Constants.PPM));
    }

    // Functions
    /**
     * Adds a new entity to this universe
     * @param e The entity to add
     */
    public void addEntity(BaseEntity e){
        game.simulation.addEntity(universalWorld, e);

        boolean res = false;
        do { res = checkTransfer(e); } while(res == true);
    }

    /**
     * Adds a new celestial to this universe
     * @param c The celestial to add
     * @param parent The celestial to parent it to
     */
    public void addCelestial(Celestial c){
        game.simulation.addEntity(universalWorld, c);
        celestials.add(c);

        boolean res = false;
        do { res = checkCelestialTransfer(c); } while(res == true);
    }

    /**
     * Checks if the entity supplied is either within or outside a sphere
     * of influence. If it is outside, transfer to the parent celestial or universe.
     * If it is inside a new SOI, transfer to the appropriate child.
     * @param e The entity to check
     */
    public boolean checkTransfer(BaseEntity e){
        // Only transfer active entities
        if(!e.getBody().isActive()) return false;

        // Check whether or not this entity has a celestial parent or not
        Celestial parent = getParentCelestial(e);
        Array<Celestial> celestialsToCheck = (parent == null) ? celestials : parent.getChildren();

        // Find closest celestial with proper SOI parameters
        Celestial closest = null;
        float minDist = Float.MAX_VALUE;

        for(Celestial c : celestialsToCheck){
            // Only transfer to next-level celestials.
            if(parent == null && c.getCelestialParent() != null) continue;

            float curDist = c.getPosition().dst2(e.getPosition());
            if(curDist < minDist && curDist < Math.pow(c.getSphereOfInfluence(), 2)){
                closest = c;
                minDist = curDist;
            }
        }

        // Add to celestial if a suitable target was found.
        if(closest != null){ addToCelestial(closest, e); return true; }
        if(parent != null && e.getPosition().len2() > Math.pow(parent.getSphereOfInfluence(), 2)){ removeFromCelestial(e); return true; }

        return false;
    }

    /**
     * Gets all the celestials in the simulation.
     * @return Array of Celestial
     */
    public Array<Celestial> getCelestials(){ return celestials; }

    /**
     * Shorthand to get one specific celestial
     * @param i Celestial index
     * @return Celestial or null
     */
    public Celestial getCelestial(int i){
        if(i >= celestials.size) return null;
        return celestials.get(i);
    }

    /**
     * Gets the celestial that is the parent of this entity or celestial
     * @param e The entity to check. This supports celestials.
     * @return The celestial parent
     */
    public Celestial getParentCelestial(BaseEntity e){
        PhysWorld w = e.getWorld();

        if(w instanceof CelestialPhysWorld){
            return ((CelestialPhysWorld)w).getParent();
        } else if(w instanceof PlanetaryPhysWorld){
            return ((PlanetaryPhysWorld)w).getPlanet();
        }

        return null;
    }
    
    /**
     * Sets the timewarp speed. If set to 1, resumes the normal simulation.
     * @param warp The speed to set. (0-100)
     */
    public void setTimewarp(float warp){
        // Reset timewarp
        if(warp == 1){
            celestialPaths.clear();
            entityPaths.clear();
            currentFuture = 0.0f;
        }

        // Starting the timewarp for first time
        if(warp != 1 && timewarp == 1){
            // Get conic sections for projected positions using keplerian transforms
            for(BaseEntity e : game.simulation.getEntities()){
                if(e instanceof Celestial){
                    Celestial parent = getParentCelestial(e);
                    if(parent == null) continue;
                    GenericConic path = OrbitPropagator.getConic(parent, e);
                    celestialPaths.add(path);
                } else {
                    Celestial parent = getParentCelestial(e);
                    if(parent == null) continue;
                    entityPaths.add(new Orbit(this, e));
                }
            }
        }

        timewarp = warp;
    }

    /**
     * This function adds an entity to a celestial and converts their coordinates
     * the celestial's scale
     * @param c Celestial target
     * @param e Entity to be converted
     */
    public void addToCelestial(Celestial c, BaseEntity e){
        e.getBody().setLinearVelocity(e.getBody().getLinearVelocity().cpy().sub(c.getBody().getLinearVelocity()));
        e.setPosition(e.getPosition().sub(c.getPosition()));

        // Add body
        game.simulation.addEntity(c.getInfluenceWorld(), e);
    }

    /**
     * Raises the entity up a level in terms of worlds
     * @param e Entity to be converted
     */
    public void removeFromCelestial(BaseEntity e){
        Celestial parent = getParentCelestial(e);
        Celestial celestialParent = getParentCelestial(parent);
        PhysWorld targetWorld = (celestialParent == null) ? universalWorld : celestialParent.getInfluenceWorld();

        if(parent == null) return;

        // TODO: bug here
        e.getBody().setLinearVelocity(e.getBody().getLinearVelocity().cpy().add(parent.getBody().getLinearVelocity()));
        e.setPosition(e.getPosition().add(parent.getPosition()));

        // Remove body
        game.simulation.addEntity(targetWorld, e);
    }

    /**
     * Steps the physics worlds
     * @param delta
     */
    public void update(float delta){
        // Update physics
        if(timewarp == 1){
            // Step the physics on the world
            game.simulation.update();
        } else if(timewarp >= 0){
            // Freezes everything and starts using the predicted path
            timeWarpAccumulator += Math.min(delta, 0.25f);
            
            while(timeWarpAccumulator >= Constants.TIME_STEP){
                timeWarpAccumulator -= Constants.TIME_STEP;

                for(int i = 0; i < celestialPaths.size; i++){
                    GenericConic path = celestialPaths.get(i);
                    BaseEntity e = path.getChild();

                    if(e instanceof Planet){
                        ((Planet)e).getStarDirection().set(OrbitUtils.directionToNearestStar(this, e), 0);
                    }
    
                    Vector2 curPos = path.getPosition(path.getMeanAnomaly() + path.timeToMeanAnomaly(currentFuture));
                    Vector2 curVel = path.getVelocity(path.getMeanAnomaly() + path.timeToMeanAnomaly(currentFuture));
    
                    e.getBody().setTransform(curPos.cpy(), e.getBody().getAngle());
                    e.getBody().setLinearVelocity(curVel.cpy());
                }

                for(int i = 0; i < entityPaths.size; i++){
                    Orbit path = entityPaths.get(i);
                    BaseEntity e = path.getEntity();
    
                    if(e instanceof Player && ((Player)e).isDriving()) continue; // Skip player if player is inside a vehicle
                    if(!(e.getWorld() instanceof CelestialPhysWorld)) continue; // Skip entities on a planet surface
    
                    Celestial parent = path.getParent(currentFuture);
                    Vector2 curPos = path.getPosition(currentFuture);
                    Vector2 curVel = path.getVelocity(currentFuture);

                    if(parent != getParentCelestial(e)){
                        // Transfer to new world
                        if(parent == getParentCelestial(e).getCelestialParent()){
                            // Tranferring to the parent
                            removeFromCelestial(e);
                        } else {
                            // Transferring to a child
                            addToCelestial(parent, e);
                        }
                    }
    
                    e.getBody().setTransform(curPos.cpy(), e.getBody().getAngle());
                    e.getBody().setLinearVelocity(curVel.cpy());
                }
            }

            currentFuture += (timewarp - 1) * 8;
        }
    }

    public void setCelestialOpacity(float a){
        for(Celestial c : getCelestials()){
            c.setCelestialOpacity(a);
        }
    }

    public PhysWorld getUniversalWorld(){ return universalWorld; }

    /**
     * Render everything at universal scale
     */
    @Override
    public void draw(Batch batch, float a){
        Matrix4 oldMat = batch.getTransformMatrix().cpy();

        for(BaseEntity e : game.simulation.getEntities()){
            if(e instanceof Celestial){
                batch.setTransformMatrix(new Matrix4().set(((Celestial)e).getUniverseSpaceTransform()));
                e.render(batch);
            } else {
                batch.setTransformMatrix(new Matrix4());

                Celestial parent = getParentCelestial(e);
                if(parent != null)
                    batch.setTransformMatrix(new Matrix4().set(parent.getUniverseSpaceTransform()));

                e.render(batch);
            }
        }

        batch.setTransformMatrix(oldMat);
    }
    
}
