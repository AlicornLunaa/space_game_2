package com.alicornlunaa.spacegame.objects.simulation;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.core.DriveableEntity;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.selene_engine.ecs.Registry;
import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.alicornlunaa.spacegame.objects.Player;
import com.alicornlunaa.spacegame.objects.planet.Planet;
import com.alicornlunaa.spacegame.objects.simulation.orbits.GenericConic;
import com.alicornlunaa.spacegame.objects.simulation.orbits.Orbit;
import com.alicornlunaa.spacegame.objects.simulation.orbits.OrbitUtils;
import com.alicornlunaa.spacegame.phys.CelestialPhysWorld;
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
    private Array<Celestial2> celestials = new Array<>();
    private Array<Orbit> entityPaths = new Array<>();
    
    private Registry registry;
    private PhysWorld universalWorld;

    private float timeWarpAccumulator = 0.0f;
    private float currentFuture = 0.0f;
    private float timewarp = 1.0f;

    // Constructor
    public Universe(Registry registry){
        this.registry = registry;
        universalWorld = new CelestialPhysWorld(null, Constants.PPM);
    }

    // Functions
    public void addEntity(IEntity e){
        if(!e.hasComponent(BodyComponent.class)) return;
        e.getComponent(BodyComponent.class).setWorld(universalWorld);
    }

    public void addCelestial(Celestial2 c){
        // c.getComponent(BodyComponent.class).setWorld(universalWorld);
        celestials.add(c);
    }

    public Array<Celestial2> getCelestials(){ return celestials; }

    public Celestial2 getCelestial(int i){
        if(i >= celestials.size) return null;
        return celestials.get(i);
    }

    public Celestial2 getParentCelestial(IEntity e){
        TransformComponent transform = e.getComponent(TransformComponent.class);
        BodyComponent bodyComponent = e.getComponent(BodyComponent.class);
        
        if(bodyComponent == null) return null;
        // if(bodyComponent.world instanceof PlanetaryPhysWorld) return ((PlanetaryPhysWorld)bodyComponent.world).getPlanet();

        // Find closest
        Celestial2 parent = null;
        float minDistance = Float.MAX_VALUE;
        float minSOISize = Float.MAX_VALUE;

        for(Celestial2 c : celestials){
            float curDistance = c.getComponent(TransformComponent.class).position.dst(transform.position);
            float curSOI = c.getSphereOfInfluence();
            
            if(e == c) continue;
            if(curDistance >= minDistance || curDistance >= c.getSphereOfInfluence()) continue;
            if(curSOI >= minSOISize) continue;
            if(bodyComponent.body.getMass() >= c.getComponent(BodyComponent.class).body.getMass()) continue;

            parent = c;
            minDistance = curDistance;
            minSOISize = curSOI;
        }

        return parent;
    }
    
    public void setTimewarp(float warp){
        // Reset timewarp
        if(warp == 1){
            entityPaths.clear();
            currentFuture = 0.0f;
        }

        // Starting the timewarp for first time
        if(warp != 1 && timewarp == 1){
            // Get conic sections for projected positions using keplerian transforms
            for(IEntity e : registry.getEntities()){
                if(!(e instanceof Celestial2)){
                    Celestial2 parent = getParentCelestial(e);

                    if(parent != null){
                        entityPaths.add(new Orbit(this, e));
                    }
                }
            }
        }

        timewarp = warp;
    }

    public void update(float delta){
        // Update physics
        if(timewarp == 1){
            // Step the physics on the world
            registry.update(delta);
        } else if(timewarp >= 0){
            // Freezes everything and starts using the predicted path
            timeWarpAccumulator += Math.min(delta, 0.25f);
            
            while(timeWarpAccumulator >= Constants.TIME_STEP){
                timeWarpAccumulator -= Constants.TIME_STEP;

                for(int i = 0; i < celestials.size; i++){
                    GenericConic path = celestials.get(i).getConic();
                    IEntity e = path.getChild();

                    if(e instanceof Planet){
                        ((Planet)e).getStarDirection().set(OrbitUtils.directionToNearestStar(this, e), 0);
                    }
    
                    TransformComponent transform = e.getComponent(TransformComponent.class);
                    BodyComponent bodyComponent = e.getComponent(BodyComponent.class);
                    Vector2 curPos = path.getPosition(path.getMeanAnomaly() + path.timeToMeanAnomaly(currentFuture)).scl(universalWorld.getPhysScale());
                    Vector2 curVel = path.getVelocity(path.getMeanAnomaly() + path.timeToMeanAnomaly(currentFuture));
    
                    if(transform == null) continue;
                    if(bodyComponent == null) continue;

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
    
                    // Celestial2 parent = path.getParent(currentFuture);
                    Vector2 curPos = path.getPosition(currentFuture).scl(universalWorld.getPhysScale());
                    Vector2 curVel = path.getVelocity(currentFuture);

                    // if(parent != getParentCelestial(e)){
                    //     // Transfer to new world
                    //     if(parent == getParentCelestial(e).getCelestialParent()){
                    //         // Tranferring to the parent
                    //         removeFromCelestial(e);
                    //     } else {
                    //         // Transferring to a child
                    //         addToCelestial(parent, e);
                    //     }
                    // }

                    transform.position.set(curPos);
                    transform.velocity.set(curVel);
                    bodyComponent.sync(transform);
                }
            }

            currentFuture += (timewarp - 1) * 8;
        }
    }

    public PhysWorld getUniversalWorld(){ return universalWorld; }

    @Override
    public void draw(Batch batch, float a){
        registry.render();
    }
}
