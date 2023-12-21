package com.alicornlunaa.space_game.cell_simulation.tiles;

import com.alicornlunaa.space_game.cell_simulation.Simulation;

public abstract class AbstractTile {
    // Enumerations
    public static enum State { SOLID, LIQUID, GAS, PLASMA };

    // Variables
    public final Element element;
    public final State state;
    public float temperature = 0.f; // In Kelvin
    public float mass = 0.f; // In kilograms
    public boolean isUpdated = false; // If this tile was updated this frame or not

    // Constructor
    public AbstractTile(Element element, State state){
        this.element = element;
        this.state = state;
    }

    // Functions
    public boolean update(Simulation simulation, int currX, int currY){
        // Basic functionality of every cell, like heat
        AbstractTile current = simulation.getTile(currX, currY);
        current.isUpdated = true;

        return true; // Continue the update
    }
}
