package com.alicornlunaa.spacegame.objects;

import java.util.ArrayList;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Planet.Planet;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;

/**
 * Used for gravity simulations in the physics environment
 */
public class PlanetController {

    // Variables
    private final App game;

    private Star star;
    private ArrayList<Planet> planets = new ArrayList<>();
    private ArrayList<Entity> ents = new ArrayList<>();

    // Constructor
    public PlanetController(final App game, Star star){
        this.game = game;
        this.star = star;
    }

    // Functions
    public float getSphereOfInfluence(Planet p){
        // Formula: R = ((planetMass / parentMass) ^ 2/5) * planetRad
        return (float)(Math.pow(p.getBody().getMass() / star.getBody().getMass(), 2 / 5) * p.getRadius() * 2);
    }
    
    public float getSphereOfInfluence(Star s){
        // Formula: R = ((planetMass / parentMass) ^ 2/5) * planetRad
        return (float)(Math.pow(s.getBody().getMass() / star.getBody().getMass(), 2 / 5) * s.getRadius() * 2);
    }

    public Planet getEntityParent(Entity e){
        // Finds the planet with the smallest sphere of influence that the entity resides in
        if(planets.size() == 0) return null;

        Planet closest = null;
        float smallestRad = Float.MAX_VALUE;

        for(int i = 0; i < planets.size(); i++){
            Planet current = planets.get(i);
            float currentRad = e.getPosition().dst(planets.get(i).getPosition());
            float soi = getSphereOfInfluence(current);

            if(currentRad < smallestRad && currentRad < soi){
                smallestRad = currentRad;
                closest = current;
            }
        }

        return closest;
    }
    
    public void update(float delta){
        for(Entity e : ents){
            Planet p = getEntityParent(e);

            if(p != null){
                p.applyGravity(delta, e.getBody());
                p.applyDrag(delta, e.getBody());
                // p.checkTransfer(e);
            }

            star.applyGravity(delta, e.getBody());
        }

        for(Planet p : planets){
            star.applyGravity(delta, p.getBody());
        }
    }

    public void draw(Matrix4 proj, Matrix4 trans){
        ShapeRenderer s = game.shapeRenderer;
        s.begin(ShapeRenderer.ShapeType.Line);
        s.setProjectionMatrix(proj);
        s.setTransformMatrix(trans);
        s.setColor(Color.RED);

        for(Entity e : ents){
            Planet p = getEntityParent(e);

            if(p != null){
                s.circle(p.getX(), p.getY(), getSphereOfInfluence(p));
            }
        }

        s.end();
    }

    public void addPlanet(Planet e){
        planets.add(e);
    }

    public void addEntity(Entity e){
        ents.add(e);
    }
    
}
