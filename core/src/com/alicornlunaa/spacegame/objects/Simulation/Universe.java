package com.alicornlunaa.spacegame.objects.Simulation;

import java.util.HashMap;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Entity;
import com.alicornlunaa.spacegame.objects.Simulation.Orbits.GenericConic;
import com.alicornlunaa.spacegame.objects.Simulation.Orbits.Orbit;
import com.alicornlunaa.spacegame.objects.Simulation.Orbits.OrbitPropagator;
import com.alicornlunaa.spacegame.phys.PhysWorld;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.Gdx;
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
    private HashMap<Entity, Celestial> entityParents = new HashMap<>();
    
    private Array<GenericConic> celestialPaths = new Array<>();
    private Array<Orbit> entityPaths = new Array<>();

    private PhysWorld universalWorld;

    private float timeWarpAccumulator = 0.0f;
    private float currentFuture = 0.0f;
    private float timewarp = 1.0f;

    // Private functions
    /**
     * Transfers the parent of one celestial to another celestial
     * @param target The celestial to reparent
     * @param parent The new parent of the celestial
     */
    private void parentCelestial(Celestial target, Celestial parent){
        // Remove target from existing celestial parent
        Celestial curParent = entityParents.get(target);
        if(curParent != null){
            curParent.getChildren().removeValue(target, true);
        }

        // Update the target's parent to be the parent celestial
        target.setCelestialParent(parent);
        parent.getChildren().add(target);
        entityParents.put(target, parent);

        // Convert the target celestial's body to the new Box2D world
        target.setPosition(target.getPosition().mul(parent.getUniverseSpaceTransform().inv()));
        target.loadBodyToWorld(parent.getWorld(), Constants.PPM);
    }

    /**
     * Checks if the entity supplied is either within or outside a sphere
     * of influence. If it is outside, transfer to the parent celestial or universe.
     * If it is inside a new SOI, transfer to the appropriate child.
     * @param e The entity to check
     */
    private boolean checkTransfer(Entity e){
        // Check whether or not this entity has a celestial parent or not
        Celestial parent = entityParents.get(e);
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

    // Constructor
    public Universe(final App game){
        this.game = game;
        universalWorld = game.simulation.addWorld(new PhysWorld(Constants.PPM){
            @Override
            public void onEntityUpdate(Entity e) {
                // Check keplerian approximation transfer
                checkTransfer(e);
                
                // Custom gravity update
                Celestial parent = entityParents.get(e);
                if(parent != null){
                    e.getBody().applyForceToCenter(parent.applyPhysics(Gdx.graphics.getDeltaTime(), e), true);
                }
            }

            @Override
            public void onUpdate() {
                // Update every celestial and its physics
                for(Celestial c : celestials){
                    c.fixedUpdate(Constants.TIME_STEP);
                    
                    Celestial parent = entityParents.get(c);
                    if(parent != null){
                        c.getBody().applyForceToCenter(parent.applyPhysics(Gdx.graphics.getDeltaTime(), c), true);
                    }
                }
            }
        });
    }

    // Functions
    /**
     * Adds a new entity to this universe
     * @param e The entity to add
     */
    public void addEntity(Entity e){
        game.simulation.addEntity(universalWorld, e);

        boolean res = false;
        do { res = checkTransfer(e); } while(res == true);
    }

    /**
     * Adds a new celestial to this universe
     * @param c The celestial to add
     * @param parent The celestial to parent it to
     */
    public void addCelestial(Celestial c, Celestial parent){
        c.loadBodyToWorld(universalWorld, Constants.PPM);
        celestials.add(c);

        if(parent != null) parentCelestial(c, parent);
    }

    /**
     * Gets all the entities in the simulation. This does not
     * include celestials.
     * @return Array of Entity
     */
    public Array<Entity> getEntities(){ return game.simulation.getEntities(); }

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
    public Celestial getCelestial(int i){ return celestials.get(i); }

    /**
     * Gets the celestial that is the parent of this entity or celestial
     * @param e The entity to check. This supports celestials.
     * @return The celestial parent
     */
    public Celestial getParentCelestial(Entity e){ return entityParents.get(e); }
    
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
            for(Entity e : game.simulation.getEntities()){
                Celestial parent = getParentCelestial(e);
                if(parent == null) continue;
                entityPaths.add(new Orbit(this, e));
            }

            for(Celestial c : celestials){
                Celestial parent = getParentCelestial(c);
                if(parent == null) continue;
                GenericConic path = OrbitPropagator.getConic(parent, c);
                celestialPaths.add(path);
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
    public void addToCelestial(Celestial c, Entity e){
        if(e.getDriver() != null) this.addToCelestial(c, e.getDriver());

        Celestial parent = entityParents.get(e);
        if(parent != null){
            parent.getEntities().removeValue(e, true);
        }

        e.getBody().setLinearVelocity(e.getBody().getLinearVelocity().cpy().sub(c.getBody().getLinearVelocity()));
        e.setPosition(e.getPosition().sub(c.getPosition()));

        // Add body
        entityParents.put(e, c);
        e.loadBodyToWorld(c.getWorld(), Constants.PPM);
        c.getEntities().add(e);
    }

    /**
     * Raises the entity up a level in terms of worlds
     * @param e Entity to be converted
     */
    public void removeFromCelestial(Entity e){
        if(e.getDriver() != null) this.removeFromCelestial(e.getDriver());

        // Raise up a level
        Celestial parent = entityParents.get(e);
        Celestial celestialParent = entityParents.get(parent);
        PhysWorld targetWorld = (celestialParent == null) ? universalWorld : celestialParent.getWorld();

        if(parent == null) return;
        if(celestialParent != null) celestialParent.getEntities().add(e);

        e.getBody().setLinearVelocity(e.getBody().getLinearVelocity().cpy().add(parent.getBody().getLinearVelocity()));
        e.setPosition(e.getPosition().add(parent.getPosition()));

        // Remove body
        entityParents.put(e, celestialParent);
        e.loadBodyToWorld(targetWorld, Constants.PPM);
        parent.getEntities().removeValue(e, true);
    }

    /**
     * Steps the physics worlds
     * @param delta
     */
    public void update(float delta){
        if(timewarp == 1){
            // Step the physics on the world
            game.simulation.update();
            for(Entity e : game.simulation.getEntities()){ e.update(delta); }
            for(Celestial c : celestials){ c.update(delta); }
        } else if(timewarp >= 0){
            // Freezes everything and starts using the predicted path
            timeWarpAccumulator += Math.min(delta, 0.25f);
            while(timeWarpAccumulator >= Constants.TIME_STEP){
                timeWarpAccumulator -= Constants.TIME_STEP;

                for(int i = 0; i < celestialPaths.size; i++){
                    GenericConic path = celestialPaths.get(i);
                    Entity e = path.getChild();
    
                    if(e.getDriving() != null) continue;
    
                    Vector2 curPos = path.getPosition(path.getMeanAnomaly() + path.timeToMeanAnomaly(currentFuture));
                    Vector2 curVel = path.getVelocity(path.getMeanAnomaly() + path.timeToMeanAnomaly(currentFuture));
    
                    e.getBody().setTransform(curPos.cpy(), e.getBody().getAngle());
                    e.getBody().setLinearVelocity(curVel.cpy());
                }

                for(int i = 0; i < entityPaths.size; i++){
                    Orbit path = entityPaths.get(i);
                    Entity e = path.getEntity();
    
                    if(e.getDriving() != null) continue;
    
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

        for(Entity e : game.simulation.getEntities()){
            batch.setTransformMatrix(oldMat);

            Celestial parent = entityParents.get(e);
            if(parent != null)
                batch.setTransformMatrix(new Matrix4().set(parent.getUniverseSpaceTransform()));

            e.draw(batch, a);
        }
        
        for(Celestial c : celestials){
            batch.setTransformMatrix(new Matrix4().set(c.getUniverseSpaceTransform()));
            c.draw(batch, a);
        }

        batch.setTransformMatrix(oldMat);
    }
    
}
