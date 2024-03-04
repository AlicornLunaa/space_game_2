package com.alicornlunaa.space_game.components.celestial;

import com.badlogic.ashley.core.Component;

public class CelestialComponent implements Component {
    // Variables
    public float radius = 100.f;
    
    // Constructor
    public CelestialComponent(float radius) {
        this.radius = radius;
    }
}
