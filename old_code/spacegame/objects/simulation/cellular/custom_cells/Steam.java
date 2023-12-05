package com.alicornlunaa.spacegame.objects.simulation.cellular.custom_cells;

public class Steam extends Gas {
    // Constructor
    public Steam() {
        super("stone", 5, 1.f);
    }

    // Functions
    @Override
    public Steam cpy() {
        return new Steam();
    }
}
