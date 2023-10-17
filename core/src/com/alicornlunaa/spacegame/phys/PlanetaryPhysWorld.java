package com.alicornlunaa.spacegame.phys;

import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.alicornlunaa.spacegame.objects.simulation.Planet;

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
