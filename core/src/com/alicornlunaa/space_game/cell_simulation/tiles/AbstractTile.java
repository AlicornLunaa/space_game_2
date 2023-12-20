package com.alicornlunaa.space_game.cell_simulation.tiles;

import com.alicornlunaa.space_game.cell_simulation.Simulation;

public abstract class AbstractTile {
    // Enumerations
    public static enum State { SOLID, LIQUID, GAS, PLASMA };

    // Variables
    public final State state;
    public float temperature = 0.f; // In Kelvin
    public float mass = 0.f; // In kilograms

    // Constructor
    public AbstractTile(State state){
        this.state = state;
    }

    // Functions
    public void update(Simulation simulation){
        // Basic functionality of every cell, like heat
        
    }
}
