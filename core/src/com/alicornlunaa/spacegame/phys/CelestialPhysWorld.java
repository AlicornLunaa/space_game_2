package com.alicornlunaa.spacegame.phys;

import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.alicornlunaa.spacegame.objects.simulation.Celestial;

public class CelestialPhysWorld extends PhysWorld {
    // Variables
    private Celestial parent;

    // Constructor
    public CelestialPhysWorld(Celestial parent, float physScale) {
        super(physScale);
        this.parent = parent;
    }

    // Functions
    public Celestial getParent(){
        return parent;
    }
}
