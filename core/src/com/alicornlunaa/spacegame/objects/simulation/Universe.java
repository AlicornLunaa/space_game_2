package com.alicornlunaa.spacegame.objects.simulation;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.core.BaseEntity;
import com.alicornlunaa.selene_engine.core.DriveableEntity;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Player;
import com.alicornlunaa.spacegame.objects.planet.Planet;
import com.alicornlunaa.spacegame.objects.simulation.orbits.GenericConic;
import com.alicornlunaa.spacegame.objects.simulation.orbits.Orbit;
import com.alicornlunaa.spacegame.objects.simulation.orbits.OrbitPropagator;
import com.alicornlunaa.spacegame.objects.simulation.orbits.OrbitUtils;
import com.alicornlunaa.spacegame.phys.CelestialPhysWorld;
import com.alicornlunaa.spacegame.phys.PlanetaryPhysWorld;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.graphics.g2d.Batch;
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
        Celestial currentParent = getParentCelestial(celestial);
        Array<Celestial> celestialsToCheck = (currentParent == null) ? celestials : currentParent.getChildren();

        // Find closest celestial with proper SOI parameters
        Celestial parent = null;
        float minDist = Float.MAX_VALUE;

        for(Celestial c : celestialsToCheck){
            // Only transfer to next-level celestials.
            if(currentParent == null && c.getCelestialParent() != null) continue;

            float curDist = c.transform.position.dst2(celestial.transform.position);
            if(curDist < minDist && curDist < Math.pow(c.getSphereOfInfluence(), 2) && c != celestial){
                parent = c;
                minDist = curDist;
            }
        }

        if(parent == null) return false;

        if(currentParent != null){
            currentParent.getChildren().removeValue(celestial, true);
        }

        // Update the target's parent to be the parent celestial
        celestial.setCelestialParent(parent);
        parent.getChildren().add(celestial);

        // Convert the target celestial's body to the new Box2D world
        celestial.transform.position.set(celestial.transform.position.mul(parent.transform.getMatrix().inv()));
        celestial.bodyComponent.setWorld(parent.getInfluenceWorld());
        return true;
    }

    // Constructor
    public Universe(final App game){
        this.game = game;
        universalWorld = game.simulation.addWorld(new CelestialPhysWorld(null, Constants.PPM));
    }

    // Functions
    /**
     * Adds a new entity to this universe
     * @param e The entity to add
     */
    public void addEntity(IEntity e){
        game.registry.addEntity(e);

        if(!e.hasComponent(BodyComponent.class)) return;
        e.getComponent(BodyComponent.class).setWorld(universalWorld);

        boolean res = false;
        do { res = checkTransfer(e); } while(res == true);
    }

    /**
     * Adds a new celestial to this universe
     * @param c The celestial to add
     * @param parent The celestial to parent it to
     */
    public void addCelestial(Celestial c){
        game.registry.addEntity(c);
        c.bodyComponent.setWorld(universalWorld);
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
    public boolean checkTransfer(IEntity e){
        // Only transfer active entities
        TransformComponent transform = e.getComponent(TransformComponent.class);
        BodyComponent bodyComponent = e.getComponent(BodyComponent.class);

        if(transform == null) return false;
        if(bodyComponent == null) return false;
        if(!bodyComponent.body.isActive()) return false;

        // Check whether or not this entity has a celestial parent or not
        Celestial parent = getParentCelestial(e);
        Array<Celestial> celestialsToCheck = (parent == null) ? celestials : parent.getChildren();

        // Find closest celestial with proper SOI parameters
        Celestial closest = null;
        float minDist = Float.MAX_VALUE;

        for(Celestial c : celestialsToCheck){
            // Only transfer to next-level celestials.
            if(parent == null && c.getCelestialParent() != null) continue;

            float curDist = c.transform.position.dst2(transform.position);
            if(curDist < minDist && curDist < Math.pow(c.getSphereOfInfluence(), 2)){
                closest = c;
                minDist = curDist;
            }
        }

        // Add to celestial if a suitable target was found.
        if(closest != null){ addToCelestial(closest, e); return true; }
        if(parent != null && transform.position.len2() > Math.pow(parent.getSphereOfInfluence(), 2)){ removeFromCelestial(e); return true; }

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
    public Celestial getParentCelestial(IEntity e){
        BodyComponent bodyComponent = e.getComponent(BodyComponent.class);
        
        if(bodyComponent == null) return null;

        if(bodyComponent.world instanceof CelestialPhysWorld){
            return ((CelestialPhysWorld)bodyComponent.world).getParent();
        } else if(bodyComponent.world instanceof PlanetaryPhysWorld){
            return ((PlanetaryPhysWorld)bodyComponent.world).getPlanet();
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
            for(IEntity eRaw : game.registry.getEntities()){
                BaseEntity e = (BaseEntity)eRaw;

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
    public void addToCelestial(Celestial c, IEntity e){
        TransformComponent parentTransform = c.getComponent(TransformComponent.class);
        TransformComponent targetTransform = e.getComponent(TransformComponent.class);
        BodyComponent targetBodyComponent = e.getComponent(BodyComponent.class);

        if(targetTransform != null && parentTransform != null && targetBodyComponent != null){
            targetTransform.velocity.set(targetTransform.velocity.cpy().sub(parentTransform.velocity));
            targetTransform.position.set(targetTransform.position.cpy().sub(parentTransform.position));
            targetBodyComponent.setWorld(c.getInfluenceWorld());
        }
    }

    /**
     * Raises the entity up a level in terms of worlds
     * @param e Entity to be converted
     */
    public void removeFromCelestial(IEntity e){
        Celestial entityParent = getParentCelestial(e);
        PhysWorld targetWorld = (getParentCelestial(entityParent) == null) ? universalWorld : getParentCelestial(entityParent).getInfluenceWorld();

        if(entityParent == null) return;

        TransformComponent parentTransform = entityParent.getComponent(TransformComponent.class);
        TransformComponent targetTransform = e.getComponent(TransformComponent.class);
        BodyComponent targetBodyComponent = e.getComponent(BodyComponent.class);

        if(targetTransform != null && parentTransform != null && targetBodyComponent != null){
            targetTransform.velocity.add(parentTransform.velocity);
            targetTransform.position.add(parentTransform.position);
            targetBodyComponent.setWorld(targetWorld);
        }
    }

    /**
     * Steps the physics worlds
     * @param delta
     */
    public void update(float delta){
        // Update physics
        if(timewarp == 1){
            // Step the physics on the world
            game.registry.update(delta);
        } else if(timewarp >= 0){
            // Freezes everything and starts using the predicted path
            timeWarpAccumulator += Math.min(delta, 0.25f);
            
            while(timeWarpAccumulator >= Constants.TIME_STEP){
                timeWarpAccumulator -= Constants.TIME_STEP;

                for(int i = 0; i < celestialPaths.size; i++){
                    GenericConic path = celestialPaths.get(i);
                    IEntity e = path.getChild();

                    if(e instanceof Planet){
                        ((Planet)e).getStarDirection().set(OrbitUtils.directionToNearestStar(this, e), 0);
                    }
    
                    TransformComponent transform = e.getComponent(TransformComponent.class);
                    BodyComponent bodyComponent = e.getComponent(BodyComponent.class);
                    Vector2 curPos = path.getPosition(path.getMeanAnomaly() + path.timeToMeanAnomaly(currentFuture));
                    Vector2 curVel = path.getVelocity(path.getMeanAnomaly() + path.timeToMeanAnomaly(currentFuture));
    
                    if(transform == null) continue;
                    if(bodyComponent == null) continue;

                    curPos.scl(128);
                    transform.position.set(curPos);
                    transform.velocity.set(curVel);
                    bodyComponent.sync(transform);
                }

                for(int i = 0; i < entityPaths.size; i++){
                    Orbit path = entityPaths.get(i);
                    IEntity e = path.getEntity();
                    TransformComponent transform = e.getComponent(TransformComponent.class);
                    BodyComponent bodyComponent = e.getComponent(BodyComponent.class);
    
                    if(transform == null) continue;
                    if(bodyComponent == null) continue;
                    if(!(bodyComponent.world instanceof CelestialPhysWorld)) continue; // Skip entities on a planet surface

                    if(e instanceof Player && ((Player)e).isDriving()){
                        // Skip player if player is inside a vehicle after parenting to vehicle
                        DriveableEntity vehicle = ((Player)e).getVehicle();
                        TransformComponent vehicleTrans = vehicle.getComponent(TransformComponent.class);
                        transform.position.set(vehicleTrans.position);
                        transform.velocity.set(vehicleTrans.velocity);
                        continue;
                    }
    
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

                    curPos.scl(128);
                    transform.position.set(curPos);
                    transform.velocity.set(curVel);
                    bodyComponent.sync(transform);
                }
            }

            currentFuture += (timewarp - 1) * 8;
        }
    }

    public PhysWorld getUniversalWorld(){ return universalWorld; }

    /**
     * Render everything at universal scale
     */
    @Override
    public void draw(Batch batch, float a){
        game.registry.render();
    }
    
}
