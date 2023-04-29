package com.alicornlunaa.spacegame.engine.phys;

import com.alicornlunaa.spacegame.objects.planet.Planet;

public class PlanetaryPhysWorld extends PhysWorld {

    // Variables
    private Planet planet;

    // Constructor
    public PlanetaryPhysWorld(Planet planet, float physScale) {
        super(physScale);
        this.planet = planet;
    }

    // Functions
    public Planet getPlanet(){
        return planet;
    }

}
