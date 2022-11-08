package com.alicornlunaa.spacegame.objects.Simulation;

import java.util.ArrayList;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Entity;
import com.alicornlunaa.spacegame.objects.Star;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;

/**
 * Used for gravity simulations in the physics environment
 */
public class CelestialController {

    // Variables
    private final App game;

    private Celestial center; // Central body, also root of the tree holding the other objects
    private ArrayList<Entity> ents = new ArrayList<>();

    // Constructor
    public CelestialController(final App game, Star star){
        this.game = game;
    }

    // Functions
    public Celestial getEntityParent(Entity e){
        // Finds the planet with the smallest sphere of influence that the entity resides in
        // if(celestials.size() == 0) return null;

        // Celestial closest = null;
        // float smallestRad = Float.MAX_VALUE;

        // for(int i = 0; i < celestials.size(); i++){
        //     Celestial current = celestials.get(i);
        //     float currentRad = e.getPosition().dst(celestials.get(i).getPosition());
        //     float soi = getSphereOfInfluence(current);

        //     if(currentRad < smallestRad && currentRad < soi){
        //         smallestRad = currentRad;
        //         closest = current;
        //     }
        // }

        // return closest;
        return null;
    }
    
    public void update(float delta){
        for(Entity e : ents){
            Celestial p = getEntityParent(e);

            if(p != null){
                // p.checkTransferEnter(e);

                // p.applyGravity(delta, e.getBody());
                // p.applyDrag(delta, e.getBody());
                // p.checkTransfer(e);
            }
        }

        center.update(delta);
    }

    public void draw(Matrix4 proj, Matrix4 trans){
        ShapeRenderer s = game.shapeRenderer;
        s.begin(ShapeRenderer.ShapeType.Line);
        s.setProjectionMatrix(proj);
        s.setTransformMatrix(trans);
        s.setColor(Color.RED);

        // for(Celestial c : celestials){
        //     if(c != null){
        //         s.circle(c.getX(), c.getY(), c.getSphereOfInfluence());
        //     }
        // }

        s.end();
    }

    public void addPlanet(Celestial c){
        center = c;
    }

    public void addEntity(Entity e){
        ents.add(e);
    }
    
}
