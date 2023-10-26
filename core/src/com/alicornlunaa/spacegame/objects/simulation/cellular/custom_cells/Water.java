package com.alicornlunaa.spacegame.objects.simulation.cellular.custom_cells;

public class Water extends Liquid {
    // Constructor
    public Water() {
        super("water", 5, 0.5f, 1.f, 1.f);
    }

    // Functions
    @Override
    public Water cpy() {
        return new Water();
    }
}
