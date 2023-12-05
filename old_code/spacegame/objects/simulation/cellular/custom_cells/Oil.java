package com.alicornlunaa.spacegame.objects.simulation.cellular.custom_cells;

public class Oil extends Liquid {
    // Constructor
    public Oil() {
        super("oil", 1, 0.05f, 0.7f, 1.f);
    }

    // Functions
    @Override
    public Oil cpy() {
        return new Oil();
    }
}
