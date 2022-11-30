package com.alicornlunaa.spacegame.objects.Simulation;

import java.util.ArrayList;
import java.util.HashMap;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Entity;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;

/**
 * This is the main class that will hold all the celestial bodies and
 * entities designed to interact with them.
 */
public class Universe extends Actor {

    // Variables
    private final App game;
    private ArrayList<Entity> ents = new ArrayList<>();
    private ArrayList<Celestial> celestials = new ArrayList<>();

    private HashMap<Entity, Celestial> entParents = new HashMap<>();
    private HashMap<Celestial, Celestial> celestialParents = new HashMap<>();

    private final World universalWorld;
    private float physAccumulator;

    private float currentFuture = 0.0f;
    private float timewarp = 1.0f;
    private Array<Vector2> initPos = new Array<>();
    private Array<OrbitPath> paths = new Array<>();

    // Private functions
    private void parentCelestial(Celestial target, Celestial parent){
        Celestial curParent = celestialParents.get(target);
        if(curParent != null){
            curParent.getChildren().remove(target);
        }

        target.setCelestialParent(parent);
        parent.getChildren().add(target);
        celestialParents.put(target, parent);

        target.setPosition(target.getPosition().mul(parent.getUniverseTransform().inv()));
        target.loadBodyToWorld(parent.getWorld(), Constants.PPM);
    }

    private void checkTransfer(Entity e){
        // Checks if the entity can transfer to a smaller sphere of influence
        Celestial parent = entParents.get(e);

        if(parent == null){
            // No parent, check every celestial that doesnt have a parent
            Celestial closest = null;
            float minDist = Float.MAX_VALUE;

            for(Celestial c : celestials){
                if(c.getCelestialParent() != null) continue;

                float curDist = c.getPosition().dst2(e.getPosition());
                if(curDist < minDist && curDist < Math.pow(c.getSphereOfInfluence(), 2)){
                    closest = c;
                    minDist = curDist;
                }
            }

            // Add to celestial if it has a parent
            if(closest != null){
                this.addToCelestial(closest, e);
            }
        } else {
            // Has a parent, only check the children or for an exit
            Celestial closest = null;
            float minDist = Float.MAX_VALUE;

            for(Celestial c : parent.getChildren()){
                float curDist = c.getPosition().dst2(e.getPosition());
                if(curDist < minDist && curDist < Math.pow(c.getSphereOfInfluence(), 2)){
                    closest = c;
                    minDist = curDist;
                }
            }

            // Add to celestial if it has a parent
            if(closest != null){
                this.addToCelestial(closest, e);
            } else if(e.getPosition().len2() > Math.pow(parent.getSphereOfInfluence(), 2)){
                this.removeFromCelestial(e);
            }
        }
    }

    // Constructor
    public Universe(final App game){
        super();
        this.game = game;
        universalWorld = new World(new Vector2(), true);
    }

    // Functions
    public Vector2 getUniversalPosition(Entity e){
        Celestial parent = entParents.get(e);
        if(parent != null) return e.getPosition().mul(parent.getUniverseTransform());
        return e.getPosition();
    }

    public Vector2 getUniversalPosition(Entity e, Vector2 pos){
        Celestial parent = entParents.get(e);
        if(parent != null) return pos.mul(parent.getUniverseTransform());
        return pos;
    }

    public Vector2 getUniversalPosition(Celestial c){ return c.getUniverseTransform().getTranslation(new Vector2()); }

    public void addEntity(Entity e){
        e.loadBodyToWorld(universalWorld, Constants.PPM);
        ents.add(e);
    }

    public void addCelestial(Celestial c, Celestial parent){
        c.loadBodyToWorld(universalWorld, Constants.PPM);
        celestials.add(c);

        if(parent != null) parentCelestial(c, parent);
    }

    public ArrayList<Entity> getEntities(){ return ents; }

    public ArrayList<Celestial> getCelestials(){ return celestials; }

    public Celestial getCelestial(int i){ return celestials.get(i); }

    public Celestial getParentCelestial(Entity e){ return entParents.get(e); }
    
    public Celestial getParentCelestial(Celestial c){ return celestialParents.get(c); }

    public Celestial getCelestialFromUniversalPos(Vector2 pos){
        Celestial closest = null;
        float minDist = Float.MAX_VALUE;

        for(Celestial c : celestials){
            float curDist = c.getPosition().dst2(pos);
            if(curDist < minDist && curDist < Math.pow(c.getSphereOfInfluence(), 2)){
                closest = c;
                minDist = curDist;
            }
        }
        
        return closest;
    }

    public Vector2 getDirToNearestStar(Entity e){
        float minDist = Float.MAX_VALUE;
        Vector2 dir = new Vector2();
        Vector2 absPos = getUniversalPosition((Celestial)e);

        for(Celestial c : celestials){
            Vector2 cAbsPos = getUniversalPosition(c);
            float curDist = cAbsPos.dst2(absPos);

            if(curDist < minDist && c instanceof Star){
                minDist = curDist;
                dir = cAbsPos.cpy().sub(absPos);
            }
        }

        return dir.nor().scl(1, -1);
    }

    public Star getNearestStar(Entity e){
        float minDist = Float.MAX_VALUE;
        Celestial ent = null;
        Vector2 absPos = getUniversalPosition((Celestial)e);

        for(Celestial c : celestials){
            Vector2 cAbsPos = getUniversalPosition(c);
            float curDist = cAbsPos.dst2(absPos);

            if(curDist < minDist && c instanceof Star){
                minDist = curDist;
                ent = c;
            }
        }

        return (Star)ent;
    }

    public void setTimewarp(float warp){
        paths.clear();
        initPos.clear();
        currentFuture = 0.0f;

        if(warp != 1){
            for(Entity e : ents){
                Celestial parent = getParentCelestial(e);
                if(parent == null) continue;

                OrbitPath path = new OrbitPath(game, this, parent, e);
                path.simulate(2048);
                paths.add(path);
                initPos.add(e.getBody().getPosition());
            }
            for(Celestial c : celestials){
                Celestial parent = getParentCelestial(c);
                if(parent == null) continue;
                
                OrbitPath path = new OrbitPath(game, this, parent, c);
                path.simulate(2048);
                paths.add(path);
                initPos.add(c.getBody().getPosition());
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

        Celestial parent = entParents.get(e);
        if(parent != null){
            parent.getEntities().remove(e);
        }

        e.getBody().setLinearVelocity(e.getBody().getLinearVelocity().cpy().sub(c.getBody().getLinearVelocity()));
        e.setPosition(e.getPosition().sub(c.getPosition()));

        // Add body
        entParents.put(e, c);
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
        Celestial parent = entParents.get(e);
        Celestial celestialParent = celestialParents.get(parent);
        World targetWorld = (celestialParent == null) ? universalWorld : celestialParent.getWorld();

        if(parent == null) return;
        if(celestialParent != null) celestialParent.getEntities().add(e);

        e.getBody().setLinearVelocity(e.getBody().getLinearVelocity().cpy().add(parent.getBody().getLinearVelocity()));
        e.setPosition(e.getPosition().add(parent.getPosition()));

        // Remove body
        entParents.put(e, celestialParent);
        e.loadBodyToWorld(targetWorld, Constants.PPM);
        parent.getEntities().remove(e);
    }

    public void createCelestialOrbit(Celestial c){
        // Creates a stable orbital velocity
        if(c.getCelestialParent() == null) return;

        Vector2 tangent = c.getBody().getPosition().cpy().nor().rotateDeg(90);
        float radius = c.getBody().getPosition().len();
        float velScl = (float)Math.sqrt((Constants.GRAVITY_CONSTANT * c.getCelestialParent().getBody().getMass()) / radius);

        c.getBody().setLinearVelocity(tangent.scl(velScl));
    }

    public void createEntityOrbit(Entity e){
        // Creates a stable orbital velocity
        for(int i = 0; i < 10; i++)
            checkTransfer(e);

        Celestial parent = entParents.get(e);
        if(parent == null) return;

        float radius = e.getBody().getPosition().len();
        float velScl = (float)Math.sqrt((Constants.GRAVITY_CONSTANT * parent.getBody().getMass()) / radius);

        e.getBody().setLinearVelocity(0, velScl);
    }

    /**
     * Steps the physics worlds
     * @param delta
     */
    public void update(float delta){
        if(timewarp == 1){
            // Step the physics on the world
            physAccumulator += Math.min(delta, 0.25f);
            while(physAccumulator >= Constants.TIME_STEP){
                universalWorld.step(Constants.TIME_STEP, Constants.VELOCITY_ITERATIONS, Constants.POSITION_ITERATIONS);
                physAccumulator -= Constants.TIME_STEP;

                for(int i = 0; i < ents.size(); i++){
                    Entity e = ents.get(i);
                    e.fixedUpdate(Constants.TIME_STEP);
                    checkTransfer(e);
                    
                    Celestial parent = entParents.get(e);
                    if(parent != null){
                        e.getBody().applyForceToCenter(parent.applyPhysics(delta, e), true);
                    }
                }

                for(Celestial c : celestials){
                    c.fixedUpdate(Constants.TIME_STEP);
                    
                    Celestial parent = celestialParents.get(c);
                    if(parent != null){
                        c.getBody().applyForceToCenter(parent.applyPhysics(delta, c), true);
                    }
                }
            }

            for(Entity e : ents){ e.update(delta); }
            for(Celestial c : celestials){ c.update(delta); }
        } else if(timewarp >= 0){
            // Freezes everything and starts using the predicted path
            for(int i = 0; i < paths.size; i++){
                OrbitPath path = paths.get(i);
                Entity e = path.getEntity();

                if(e.getDriving() != null) continue;

                Vector2 curPos = path.getAbsolute(((int)currentFuture) % path.getPoints().size());
                Vector2 curVel = path.getVelocity(((int)currentFuture) % path.getPoints().size());
                Vector2 nextPos = path.getAbsolute(((int)currentFuture + 1) % path.getPoints().size());
                Vector2 nextVel = path.getVelocity(((int)currentFuture + 1) % path.getPoints().size());

                e.getBody().setTransform(curPos.cpy().lerp(nextPos, currentFuture % 1.0f), e.getBody().getAngle());
                e.getBody().setLinearVelocity(curVel.cpy().lerp(nextVel, currentFuture % 1.0f));
            }

            currentFuture += (timewarp - 1);
        }
    }

    /**
     * Render everything at universal scale
     */
    @Override
    public void draw(Batch batch, float a){
        Matrix4 oldMat = batch.getTransformMatrix().cpy();

        for(Entity e : ents){
            batch.setTransformMatrix(oldMat);

            Celestial parent = entParents.get(e);
            if(parent != null){
                batch.setTransformMatrix(new Matrix4().set(parent.getUniverseTransform()));
            }

            e.draw(batch, a);
        }
        
        for(Celestial c : celestials){
            batch.setTransformMatrix(new Matrix4().set(c.getUniverseTransform()));
            c.draw(batch, a);
        }

        batch.setTransformMatrix(oldMat);
    }
    
}
