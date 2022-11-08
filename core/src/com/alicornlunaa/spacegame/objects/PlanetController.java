package com.alicornlunaa.spacegame.objects;

import java.util.ArrayList;

import com.alicornlunaa.spacegame.objects.Planet.Planet;

/**
 * Used for gravity simulations in the physics environment
 */
public class PlanetController {

    // Variables
    private ArrayList<Planet> planets = new ArrayList<>();
    private ArrayList<Entity> ents = new ArrayList<>();

    // Constructor
    public PlanetController(){

    }

    // Functions
    public void addPlanet(Planet e){
        planets.add(e);
    }

    public void addEntity(Entity e){
        ents.add(e);
    }

    public void getEntityParent(Entity e){
        
    }
    
}
