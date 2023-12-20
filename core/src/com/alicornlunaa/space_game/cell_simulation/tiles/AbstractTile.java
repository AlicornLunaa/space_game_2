package com.alicornlunaa.space_game.cell_simulation.tiles;

public abstract class AbstractTile {
    // Enumerations
    public static enum State { SOLID, LIQUID, GAS, PLASMA };

    // Variables
    public final State state;

    // Constructor
    public AbstractTile(State state){
        this.state = state;
    }
}
