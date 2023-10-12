package com.alicornlunaa.spacegame.components;

import com.alicornlunaa.selene_engine.ecs.IComponent;

public class CelestialComponent implements IComponent {
    // Variables
    public float radius = 100.f;
    
    // Constructor
    public CelestialComponent(float radius) {
        this.radius = radius;
    }

    // Functions
    public float getRadius(){
        return radius;
    }

    @Deprecated
    public float getSphereOfInfluence(){ return 0; }
}
