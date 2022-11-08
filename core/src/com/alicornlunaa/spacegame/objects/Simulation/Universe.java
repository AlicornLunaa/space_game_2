package com.alicornlunaa.spacegame.objects.Simulation;

import java.util.ArrayList;
import java.util.HashMap;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Entity;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * 

            if(e.getPosition().dst2(getPosition()) > sphereOfInfluence * sphereOfInfluence){
                // Move it to the parent world
                ents.remove(i);
                this.delEntity(e);
                i--;
            }
 */

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

    // Private functions
    private void parentCelestial(Celestial target, Celestial parent){
        Celestial curParent = celestialParents.get(target);
        if(curParent != null){
            curParent.getChildren().remove(target);
        }

        target.setCelestialParent(parent);
        parent.getChildren().add(target);
        celestialParents.put(target, parent);

        target.setPosition(target.getPosition().sub(parent.getPosition()));
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

        celestials.add(new Celestial(game, universalWorld, 600, 1200));
        celestials.add(new Celestial(game, universalWorld, 100, 200));
        celestials.get(0).setPosition(-1300, 0);
        celestials.get(1).setPosition(-400, 0);
        parentCelestial(celestials.get(1), celestials.get(0));
    }

    // Functions
    public Vector2 getUniversalPosition(Entity e){
        Celestial parent = entParents.get(e);
        if(parent != null) return e.getPosition().mul(parent.getUniverseTransform());
        return e.getPosition();
    }

    public Vector2 getUniversalPosition(Celestial c){ return c.getUniverseTransform().getTranslation(new Vector2()); }

    public void addEntity(Entity e){
        e.loadBodyToWorld(universalWorld, Constants.PPM);
        ents.add(e);
    }

    public Celestial getCelestial(int i){ return celestials.get(i); }

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

        e.setPosition(e.getPosition().add(parent.getPosition()));

        // Remove body
        entParents.put(e, celestialParent);
        e.loadBodyToWorld(targetWorld, Constants.PPM);
        parent.getEntities().remove(e);
    }

    /**
     * Steps the physics worlds
     * @param delta
     */
    public void update(float delta){
        // Step the physics on the world
        physAccumulator += Math.min(delta, 0.25f);
        while(physAccumulator >= Constants.TIME_STEP){
            universalWorld.step(Constants.TIME_STEP, Constants.VELOCITY_ITERATIONS, Constants.POSITION_ITERATIONS);
            physAccumulator -= Constants.TIME_STEP;
        }

        for(int i = 0; i < ents.size(); i++){
            Entity e = ents.get(i);
            e.act(delta);
            checkTransfer(e);
        }

        for(Celestial c : celestials){
            c.update(delta);
        }
    }

    /**
     * Render everything at universal scale
     */
    @Override
    public void draw(Batch batch, float a){
        Matrix4 oldMat = batch.getTransformMatrix().cpy();

        batch.setTransformMatrix(oldMat);
        for(Entity e : ents){
            Celestial parent = entParents.get(e);
            if(parent != null){
                batch.setTransformMatrix(new Matrix4().set(parent.getUniverseTransform()));
            }

            e.draw(batch, a);
        }
        
        batch.setTransformMatrix(oldMat);
        for(Celestial c : celestials){
            Celestial parent = celestialParents.get(c);
            if(parent != null){
                batch.setTransformMatrix(new Matrix4().set(parent.getUniverseTransform()));
            }

            c.draw(batch, a);
        }

        batch.setTransformMatrix(oldMat);
    }
    
}